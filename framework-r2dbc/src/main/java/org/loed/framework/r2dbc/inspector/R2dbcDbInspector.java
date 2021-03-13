package org.loed.framework.r2dbc.inspector;

import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.autoconfigure.DbInspectorRegister;
import org.loed.framework.common.lock.ZKDistributeLock;
import org.loed.framework.common.orm.ORMapping;
import org.loed.framework.common.orm.Table;
import org.loed.framework.common.orm.schema.Column;
import org.loed.framework.common.orm.schema.Index;
import org.loed.framework.common.util.SerializeUtils;
import org.loed.framework.r2dbc.datasource.routing.RoutingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/8 12:43 下午
 */
@Slf4j
public class R2dbcDbInspector implements ApplicationEventPublisherAware, EnvironmentAware {
	private static final String RESOURCE_PATTERN = "/**/*.class";
	private static final String PACKAGE_INFO_SUFFIX = ".package-info";
	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private final ConnectionFactory connectionFactory;

	private DdlProvider ddlProvider;

	private ApplicationEventPublisher applicationEventPublisher;

	private Environment environment;

	@Autowired(required = false)
	private ZKDistributeLock zkDistributeLock;

	private boolean enabled = false;
	private boolean execute = true;
	private List<String> packagesToScan;

	public R2dbcDbInspector(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void inspect() {
		if (!enabled) {
			log.info("db inspector is not enabled");
			return;
		}
		if (CollectionUtils.isEmpty(packagesToScan)) {
			log.info("scan packages is empty , inspector will terminate");
			return;
		}
		this.ddlProvider = createDdlProvider();

		if (zkDistributeLock != null) {
			Collections.sort(packagesToScan);
			String lockPath = StringUtils.replace(org.apache.commons.lang3.StringUtils.join(packagesToScan, ","), ".", "_");
			zkDistributeLock.accept(lockPath, p -> doInspect(packagesToScan));
		} else {
			doInspect(packagesToScan);
		}
	}

	private void doInspect(List<String> packages) {
		List<Table> tables = scanJpaTables();
		//没有要同步的表，直接返回
		if (CollectionUtils.isEmpty(tables)) {
			return;
		}
		String schema = "";
		if (connectionFactory instanceof RoutingConnectionFactory) {
			//TODO getDatabaseName
		} else {
			BindResult<R2dbcProperties> bind = Binder.get(environment).bind("spring.r2dbc", R2dbcProperties.class);
			R2dbcProperties r2dbcProperties = bind.orElseGet(null);
			schema = r2dbcProperties.getName();
		}
		processOneDataSource(schema, tables).collectList().map(ddls -> {
			System.out.println(" _______  .______           __  .__   __.      _______..______    _______   ______ .___________.  ______   .______      \n" +
					"|       \\ |   _  \\         |  | |  \\ |  |     /       ||   _  \\  |   ____| /      ||           | /  __  \\  |   _  \\     \n" +
					"|  .--.  ||  |_)  |  ______|  | |   \\|  |    |   (----`|  |_)  | |  |__   |  ,----'`---|  |----`|  |  |  | |  |_)  |    \n" +
					"|  |  |  ||   _  <  |______|  | |  . `  |     \\   \\    |   ___/  |   __|  |  |         |  |     |  |  |  | |      /     \n" +
					"|  '--'  ||  |_)  |        |  | |  |\\   | .----)   |   |  |      |  |____ |  `----.    |  |     |  `--'  | |  |\\  \\----.\n" +
					"|_______/ |______/         |__| |__| \\__| |_______/    | _|      |_______| \\______|    |__|      \\______/  | _| `._____|\n" +
					"");
			List<String> mergedDdls = new ArrayList<>();
			for (List<String> ddl : ddls) {
				mergedDdls.addAll(ddl);
			}
			return mergedDdls;
		}).flatMap(ddls -> {
			for (String ddl : ddls) {
				System.out.println(ddl + ";");
			}
			if (CollectionUtils.isNotEmpty(ddls)) {
				if (execute) {
					return Mono.from(connectionFactory.create()).flatMap(connection -> {
						return Mono.from(connection.createStatement(String.join(";", ddls)).execute());
					}).flatMap(result -> {
						return Mono.from(result.getRowsUpdated());
					});
				} else {
					return Mono.just(ddls.size());
				}
			} else {
				return Mono.just(0);
			}
		}).defaultIfEmpty(0).doOnNext(count -> {
			applicationEventPublisher.publishEvent(new DbInspectFinishEvent(count));
		}).then().subscribe();
	}

	protected Flux<List<String>> processOneDataSource(final String databaseName, List<Table> tables) {
		return Mono.from(connectionFactory.create()).flatMapMany(connection -> {
			return Flux.fromIterable(tables).flatMap(jpa -> {
				List<org.loed.framework.common.orm.Index> jpaIndices = jpa.getIndices();
				return ddlProvider.getTable(connection, null, databaseName, jpa.getSqlName()).map(table -> {
					List<Column> dbColumns = table.getColumns();
					Map<String, Column> columnMap = dbColumns.stream().collect(Collectors.toMap(Column::getSqlName, v -> v, (a, b) -> a));
					List<org.loed.framework.common.orm.Column> jpaColumns = jpa.getColumns();
					List<String> ddlList = new ArrayList<>();
					for (org.loed.framework.common.orm.Column jpaColumn : jpaColumns) {
						String sqlName = jpaColumn.getSqlName();
						if (!columnMap.containsKey(sqlName)) {
							ddlList.add(ddlProvider.addColumn(jpaColumn));
						}
					}
					List<Index> dbIndices = table.getIndices();
					if (CollectionUtils.isNotEmpty(jpaIndices)) {
						Map<String, Index> map = dbIndices == null ? new HashMap<>() : dbIndices.stream().collect(Collectors.toMap(Index::getName, i -> i, (a, b) -> a));
						for (org.loed.framework.common.orm.Index jpaIndex : jpaIndices) {
							String indexName = jpaIndex.getName();
							if (!map.containsKey(indexName)) {
								ddlList.add(ddlProvider.addIndex(jpaIndex));
							}
						}
					}
					return ddlList;
				}).switchIfEmpty(Mono.fromSupplier(() -> {
					List<String> ddlList = new ArrayList<>();
					String createTable = ddlProvider.createTable(jpa);
					ddlList.add(createTable);
					if (jpaIndices != null) {
						for (org.loed.framework.common.orm.Index index : jpaIndices) {
							ddlList.add(ddlProvider.addIndex(index));
						}
					}
					return ddlList;
				}));
			});
		});
	}

	private List<Table> scanJpaTables() {
		Set<String> classNames = new TreeSet<>();
		if (CollectionUtils.isNotEmpty(packagesToScan)) {
			classNames.addAll(scanPackages(packagesToScan));
		}
		if (CollectionUtils.isNotEmpty(DbInspectorRegister.getPackages())) {
			classNames.addAll(scanPackages(DbInspectorRegister.getPackages()));
		}
		if (CollectionUtils.isNotEmpty(DbInspectorRegister.getClasses())) {
			classNames.addAll(DbInspectorRegister.getClasses());
		}
		List<Table> jpaTables = new ArrayList<>();
		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className);
				Table jpaTable = ORMapping.get(clazz);
				if (jpaTable == null) {
					continue;
				}
				jpaTables.add(jpaTable);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return jpaTables;
	}

	protected Set<String> scanPackages(List<String> packages) {
		Set<String> classNames = new TreeSet<>();
		if (packages != null) {
			try {
				for (String pkg : packages) {
					String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
							ClassUtils.convertClassNameToResourcePath(pkg) + RESOURCE_PATTERN;
					Resource[] resources = this.resourcePatternResolver.getResources(pattern);
					MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
					for (Resource resource : resources) {
						if (resource.isReadable()) {
							MetadataReader reader = readerFactory.getMetadataReader(resource);
							String className = reader.getClassMetadata().getClassName();
							if (className.endsWith(PACKAGE_INFO_SUFFIX)) {
								continue;
							}
							AnnotationMetadata annotationMetadata = reader.getAnnotationMetadata();
							boolean hasAnnotation = annotationMetadata.hasAnnotation(javax.persistence.Table.class.getName());
							if (!hasAnnotation) {
								continue;
							}
							classNames.add(reader.getClassMetadata().getClassName());
						}
					}
				}
			} catch (IOException ex) {
				throw new RuntimeException("Failed to scan classpath for unlisted classes", ex);
			}
		}
		return classNames;
	}

	private DdlProvider createDdlProvider() {
		Binder binder = Binder.get(environment);
		BindResult<org.loed.framework.r2dbc.autoconfigure.R2dbcProperties> bind = binder.bind(org.loed.framework.r2dbc.autoconfigure.R2dbcProperties.PREFIX, org.loed.framework.r2dbc.autoconfigure.R2dbcProperties.class);
		org.loed.framework.r2dbc.autoconfigure.R2dbcProperties r2dbcProperties = bind.orElseGet(org.loed.framework.r2dbc.autoconfigure.R2dbcProperties::new);
		DdlProvider ddlProvider = DdlProviderFactory.getInstance().getDdlProvider(r2dbcProperties.getDialect(), r2dbcProperties);
		if (ddlProvider == null) {
			throw new RuntimeException("can't find R2dbcSqlBuilder from properties:" + SerializeUtils.toJson(r2dbcProperties));
		}
		return ddlProvider;
	}

	@Override
	public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setPackagesToScan(List<String> packagesToScan) {
		this.packagesToScan = packagesToScan;
	}

	public void setExecute(boolean execute) {
		this.execute = execute;
	}

	@Override
	public void setEnvironment(@NonNull Environment environment) {
		this.environment = environment;
	}
}
