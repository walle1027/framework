package org.loed.framework.jdbc.database.autoconfigure;

import org.apache.commons.collections.CollectionUtils;
import org.loed.framework.common.ORMapping;
import org.loed.framework.common.RoutingDataSource;
import org.loed.framework.common.ThreadPoolExecutor;
import org.loed.framework.common.database.Table;
import org.loed.framework.common.lock.ZKDistributeLock;
import org.loed.framework.jdbc.database.DatabaseResolver;
import org.loed.framework.jdbc.database.dialect.Dialect;
import org.loed.framework.jdbc.database.model.Column;
import org.loed.framework.jdbc.database.model.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author thomason
 * @version 1.0
 * @since 2017/8/12 下午1:00
 */
public class DbInspector {
	private static final String RESOURCE_PATTERN = "/**/*.class";
	private static final String PACKAGE_INFO_SUFFIX = ".package-info";
	private static List<String> extPackages;
	private Dialect dialect;
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private DataSource dataSource;
	@Autowired(required = false)
	private ZKDistributeLock zkDistributeLock;
	@Autowired
	private ThreadPoolExecutor threadPoolExecutor;
	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	private boolean enabled = false;
	private List<String> packagesToScan;

	public DbInspector(Dialect dialect) {
		this.dialect = dialect;
	}

	public static void addExtPackages(String extPackages) {
		if (DbInspector.extPackages == null) {
			DbInspector.extPackages = new ArrayList<>();
		}
		DbInspector.extPackages.add(extPackages);
	}

	@PostConstruct
	public void inspect() {
		if (!enabled) {
			return;
		}
		if (CollectionUtils.isEmpty(packagesToScan) && CollectionUtils.isEmpty(extPackages)) {
			return;
		}
		List<String> packages = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(packagesToScan)) {
			packages.addAll(packagesToScan);
		}
		if (CollectionUtils.isNotEmpty(extPackages)) {
			packages.addAll(extPackages);
		}
		if (zkDistributeLock != null) {
			String lockPath = StringUtils.replace(org.apache.commons.lang3.StringUtils.join(packagesToScan, ","), ".", "_");
			zkDistributeLock.accept(lockPath, p -> doInspect(packages));
		} else {
			doInspect(packages);
		}
	}

	private void doInspect(List<String> packages) {
		List<Table> tables = scanJPATables(packages);
		//没有要同步的表，直接返回
		if (CollectionUtils.isEmpty(tables)) {
			return;
		}
		//如果是动态数据源
		if (dataSource instanceof RoutingDataSource) {
			List<DataSource> dataSources = ((RoutingDataSource) dataSource).getAllDataSource();
			if (CollectionUtils.isNotEmpty(dataSources)) {
				CountDownLatch latch = new CountDownLatch(dataSources.size());
				dataSources.forEach(ds -> {
					new Thread(() -> {
						try {
							processOneDataSource(tables, ds);
						} finally {
							latch.countDown();
						}
					}).start();
				});
				try {
					latch.await();
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
		//其他简单数据源
		else {
			processOneDataSource(tables, dataSource);
		}
	}

	private List<Table> scanJPATables(List<String> packages) {
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

	/**
	 * 对某个单库进行处理
	 *
	 * @param jpaTables  JPA对象
	 * @param dataSource 数据源
	 */
	private void processOneDataSource(List<Table> jpaTables, DataSource dataSource) {
		try {
			int size = jpaTables.size();
			CountDownLatch latch = new CountDownLatch(size);
			List<String> ddlList = new CopyOnWriteArrayList<>();
			//按照数据库的表的个数，创建对应个数的线程池
			for (Table table : jpaTables) {
				threadPoolExecutor.execute(() -> {
					String javaName = table.getJavaName();
					Connection connection = null;
					try {
						connection = dataSource.getConnection();
						connection.setAutoCommit(false);
						if (table.isSharding()) {
							for (int i = 1; i <= table.getShardingCount(); i++) {
								Table shardingTable = new Table();
								shardingTable.setSqlName(table.getSqlName() + "_" + i);
								shardingTable.setCatalog(table.getCatalog());
								shardingTable.setSchema(table.getSchema());
								shardingTable.setComment(table.getComment());
								shardingTable.setDatabase(table.getDatabase());
								shardingTable.setJavaName(javaName);
								shardingTable.setSimpleJavaName(table.getSimpleJavaName());
								shardingTable.setOwnerSynonymName(table.getOwnerSynonymName());
								shardingTable.setSharding(table.isSharding());
								shardingTable.setShardingCount(table.getShardingCount());
								if (table.getColumns() != null) {
									shardingTable.setColumns(table.getColumns().stream().map(c -> {
										org.loed.framework.common.database.Column column = new org.loed.framework.common.database.Column(shardingTable);
										column.setColumnDefinition(c.getColumnDefinition());
										column.setDefaultValue(c.getDefaultValue());
										column.setIndexed(c.isIndexed());
										column.setJavaName(c.getJavaName());
										column.setLength(c.getLength());
										column.setNullable(c.isNullable());
										column.setJavaType(c.getJavaType());
//										column.setJavaTypeName(c.getJavaTypeName());
										column.setPrecision(c.getPrecision());
										column.setScale(c.getScale());
										column.setShardingColumn(c.isShardingColumn());
										column.setSqlComment(c.getSqlComment());
										column.setSqlName(c.getSqlName());
										column.setUnique(c.isUnique());
										column.setPk(c.isPk());
										return column;
									}).collect(Collectors.toList()));
								}
								if (table.getIndices() != null) {
									shardingTable.setIndices(table.getIndices().stream().map(idx -> {
										org.loed.framework.common.database.Index index = new org.loed.framework.common.database.Index(table);
										index.setTable(shardingTable);
										index.setColumnList(idx.getColumnList());
										index.setName(idx.getName());
										index.setUnique(idx.isUnique());
										return index;
									}).collect(Collectors.toList()));
								}
								processOneTable(connection, shardingTable, ddlList);
							}
						} else {
							processOneTable(connection, table, ddlList);
						}
						connection.commit();
					} catch (SQLException e) {
						logger.error(e.getMessage(), e);
						if (connection != null) {
							try {
								connection.rollback();
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
						}
					} finally {
						try {
							if (connection != null) {
								connection.close();
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
						latch.countDown();
					}
				});
			}
			latch.await();
			if (CollectionUtils.isNotEmpty(ddlList)) {
				try (Connection connection = dataSource.getConnection()) {
					Statement statement = connection.createStatement();
					System.out.println(" _______  .______           __  .__   __.      _______..______    _______   ______ .___________.  ______   .______      \n" +
							"|       \\ |   _  \\         |  | |  \\ |  |     /       ||   _  \\  |   ____| /      ||           | /  __  \\  |   _  \\     \n" +
							"|  .--.  ||  |_)  |  ______|  | |   \\|  |    |   (----`|  |_)  | |  |__   |  ,----'`---|  |----`|  |  |  | |  |_)  |    \n" +
							"|  |  |  ||   _  <  |______|  | |  . `  |     \\   \\    |   ___/  |   __|  |  |         |  |     |  |  |  | |      /     \n" +
							"|  '--'  ||  |_)  |        |  | |  |\\   | .----)   |   |  |      |  |____ |  `----.    |  |     |  `--'  | |  |\\  \\----.\n" +
							"|_______/ |______/         |__| |__| \\__| |_______/    | _|      |_______| \\______|    |__|      \\______/  | _| `._____|\n" +
							"");
					System.out.println("DB-INFO:" + connection.getMetaData().getURL());
					for (String d : ddlList) {
						System.out.println(d + ";");
						statement.addBatch(d);
					}
					statement.executeBatch();
					System.out.println("==========================================END OF DB-INSPECTOR==========================================");
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
//		} finally {
//			if (executorService != null) {
//				executorService.shutdown();
//			}
//		}
	}

	private void processOneTable(Connection connection, Table table, List<String> ddlList) {
		DatabaseResolver resolver = DatabaseResolver.getInstance();
		try {
			String tableName = table.getSqlName();
			org.loed.framework.jdbc.database.model.Table dbTable = resolver.getTable(connection, tableName);
			List<org.loed.framework.common.database.Column> tableColumns = table.getColumns() == null ? Collections.EMPTY_LIST : table.getColumns();
			List<org.loed.framework.common.database.Index> indexList = table.getIndices() == null ? Collections.EMPTY_LIST : table.getIndices();
			if (dbTable == null) {
				List<String> createTables = dialect.buildCreateTableClause(table);
				if (CollectionUtils.isNotEmpty(createTables)) {
					ddlList.addAll(createTables);
				}

				if (CollectionUtils.isNotEmpty(indexList)) {
					indexList.forEach(index -> {
						List<String> indexClause = dialect.buildIndexClause(index);
						if (CollectionUtils.isNotEmpty(indexClause)) {
							ddlList.addAll(indexClause);
						}
					});
				}
			} else {
				List<Column> dbColumns = dbTable.getColumns() == null ? Collections.EMPTY_LIST : dbTable.getColumns();
				tableColumns.forEach(tableColumn -> {
					boolean hasColumn = dbColumns.stream().anyMatch(r -> r.getSqlName().equals(tableColumn.getSqlName()));
					if (!hasColumn) {
						List<String> addColumnClause = dialect.buildAddColumnClause(tableColumn);
						if (CollectionUtils.isNotEmpty(addColumnClause)) {
							ddlList.addAll(addColumnClause);
						}
					}
				});
				List<Index> dbIndices = dbTable.getIndices() == null ? Collections.EMPTY_LIST : dbTable.getIndices();
				indexList.forEach(index -> {
					boolean hasIndex = dbIndices.stream().anyMatch(r -> r.getName().equals(index.getName()));
					if (!hasIndex) {
						List<String> indexClause = dialect.buildIndexClause(index);
						if (CollectionUtils.isNotEmpty(indexClause)) {
							ddlList.addAll(indexClause);
						}

					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setZkDistributeLock(ZKDistributeLock zkDistributeLock) {
		this.zkDistributeLock = zkDistributeLock;
	}

	public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
		this.threadPoolExecutor = threadPoolExecutor;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setPackagesToScan(List<String> packagesToScan) {
		this.packagesToScan = packagesToScan;
	}
}
