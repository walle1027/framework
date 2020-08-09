package org.loed.framework.r2dbc.inspector;

import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.database.schema.Column;
import org.loed.framework.common.database.schema.Index;
import org.loed.framework.common.lock.ZKDistributeLock;
import org.loed.framework.common.orm.Table;
import org.loed.framework.r2dbc.inspector.dialect.DatabaseDialect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
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
public class R2dbcDbInspector implements ApplicationEventPublisherAware, InitializingBean {
	private static final String RESOURCE_PATTERN = "/**/*.class";
	private static final String PACKAGE_INFO_SUFFIX = ".package-info";
	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private final ConnectionFactory connectionFactory;

	private ApplicationEventPublisher applicationEventPublisher;

	private final DatabaseDialect dialect;

	@Autowired(required = false)
	private ZKDistributeLock zkDistributeLock;

	private boolean enabled = false;
	private boolean execute = true;
	private List<String> packagesToScan;

	public R2dbcDbInspector(ConnectionFactory connectionFactory, DatabaseDialect dialect) {
		this.connectionFactory = connectionFactory;
		this.dialect = dialect;
	}

	@Override
	public void afterPropertiesSet() {
		if (!enabled) {
			log.info("db inspector is not enabled");
			return;
		}
		if (CollectionUtils.isEmpty(packagesToScan)) {
			log.info("scan packages is empty , inspector will terminate");
			return;
		}
		if (zkDistributeLock != null) {
			Collections.sort(packagesToScan);
			String lockPath = StringUtils.replace(org.apache.commons.lang3.StringUtils.join(packagesToScan, ","), ".", "_");
			zkDistributeLock.accept(lockPath, p -> doInspect(packagesToScan));
		} else {
			doInspect(packagesToScan);
		}
	}

	private void doInspect(List<String> packages) {
		List<Table> tables = scanJpaTables(packages);
		//没有要同步的表，直接返回
		if (CollectionUtils.isEmpty(tables)) {
			return;
		}
		processOneDataSource(tables).sort().collectList().map(ddls -> {
			System.out.println(" _______  .______           __  .__   __.      _______..______    _______   ______ .___________.  ______   .______      \n" +
					"|       \\ |   _  \\         |  | |  \\ |  |     /       ||   _  \\  |   ____| /      ||           | /  __  \\  |   _  \\     \n" +
					"|  .--.  ||  |_)  |  ______|  | |   \\|  |    |   (----`|  |_)  | |  |__   |  ,----'`---|  |----`|  |  |  | |  |_)  |    \n" +
					"|  |  |  ||   _  <  |______|  | |  . `  |     \\   \\    |   ___/  |   __|  |  |         |  |     |  |  |  | |      /     \n" +
					"|  '--'  ||  |_)  |        |  | |  |\\   | .----)   |   |  |      |  |____ |  `----.    |  |     |  `--'  | |  |\\  \\----.\n" +
					"|_______/ |______/         |__| |__| \\__| |_______/    | _|      |_______| \\______|    |__|      \\______/  | _| `._____|\n" +
					"");
			List<String> mergedDdls = new ArrayList<>();
			for (List<String> ddl : ddls) {
				for (String s : ddl) {
					log.info(s);
					mergedDdls.add(s);
				}
			}
			String join = String.join(";", mergedDdls);
			return Mono.from(connectionFactory.create()).flatMap(connection -> {
				return Mono.from(connection.createBatch().add(join).execute());
			}).flatMap(result -> {
				return Mono.from(result.getRowsUpdated());
			}).map(rows -> {
				log.info("rows updated " + rows);
				applicationEventPublisher.publishEvent(new DbInspectFinishEnvent(tables));
				return rows;
			});
		}).defaultIfEmpty(Mono.just(0)).subscribe();
	}

	protected Flux<List<String>> processOneDataSource(List<Table> tables) {
		return Mono.from(connectionFactory.create()).flatMapMany(connection -> {
			return Flux.fromIterable(tables).flatMap(jpa -> {
				List<org.loed.framework.common.orm.Index> jpaIndices = jpa.getIndices();
				return dialect.getTable(connection, jpa.getSqlName()).map(table -> {
					List<Column> dbColumns = table.getColumns();
					Map<String, Column> columnMap = dbColumns.stream().collect(Collectors.toMap(Column::getSqlName, v -> v, (a, b) -> a));
					List<org.loed.framework.common.orm.Column> jpaColumns = jpa.getColumns();
					List<String> ddlList = new ArrayList<>();
					for (org.loed.framework.common.orm.Column jpaColumn : jpaColumns) {
						String sqlName = jpaColumn.getSqlName();
						if (columnMap.containsKey(sqlName)) {
							ddlList.add(dialect.addColumn(jpaColumn));
						}
					}
					List<Index> dbIndices = table.getIndices();
					if (CollectionUtils.isNotEmpty(jpaIndices)) {
						Map<String, Index> map = dbIndices == null ? new HashMap<>() : dbIndices.stream().collect(Collectors.toMap(Index::getName, i -> i, (a, b) -> a));
						for (org.loed.framework.common.orm.Index jpaIndex : jpaIndices) {
							String indexName = jpaIndex.getName();
							if (!map.containsKey(indexName)) {
								ddlList.add(dialect.addIndex(jpaIndex));
							}
						}
					}
					return ddlList;
				}).switchIfEmpty(Mono.fromSupplier(() -> {
					List<String> ddlList = new ArrayList<>();
					String createTable = dialect.createTable(jpa);
					ddlList.add(createTable);
					if (jpaIndices != null) {
						for (org.loed.framework.common.orm.Index index : jpaIndices) {
							ddlList.add(dialect.addIndex(index));
						}
					}
					return ddlList;
				}));
			});
		});
	}

	private List<Table> scanJpaTables(List<String> packages) {
		Set<String> classNames = scanPackages(packages);
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
}
