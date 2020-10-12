package org.loed.framework.common.orm;

import org.apache.commons.lang3.StringUtils;
import org.loed.framework.common.util.ReflectionUtils;
import org.springframework.lang.NonNull;

import javax.persistence.GenerationType;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/9/16 6:33 下午
 */
public interface Filters {
	/**
	 * 默认过滤器
	 */
	Predicate<Column> ALWAYS_TRUE_FILTER = column -> true;

	/**
	 * 可插入列的过滤器
	 */
	Predicate<Column> INSERTABLE_FILTER = column -> {
		boolean pk = column.isPk();
		if (pk) {
			GenerationType generationType = column.getIdGenerationType();
			if (Objects.equals(generationType, GenerationType.AUTO)) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	};
	/**
	 * 可更新列的过滤器
	 */
	Predicate<Column> UPDATABLE_FILTER = column -> {
		return column.isUpdatable() && !column.isPk() && !column.isTenantId() && !column.isCreateTime() && !column.isCreateBy() && !column.isDeleted();
	};
	/**
	 * 版本列的过滤器
	 */
	Predicate<Column> ALWAYS_UPDATE_FILTER = column -> column.isVersioned() || column.isLastModifyTime() || column.isLastModifyBy();

	/**
	 * 指定更新列的过滤器
	 */
	class IncludeFilter implements Predicate<Column> {
		private final Collection<String> includes;

		public IncludeFilter(@NonNull Collection<String> includes) {
			this.includes = includes;
		}

		@Override
		public boolean test(Column column) {
			return includes.contains(column.getJavaName());
		}
	}

	/**
	 * 指定忽略列的过滤器
	 */
	class ExcludeFilter implements Predicate<Column> {
		private final Collection<String> excludes;

		public ExcludeFilter(@NonNull Collection<String> excludes) {
			this.excludes = excludes;
		}

		@Override
		public boolean test(Column column) {
			return !excludes.contains(column.getJavaName());
		}
	}

	/**
	 * 动态判断属性为空的过滤器
	 */
	class NonBlankFilter implements Predicate<Column> {
		private final Object object;

		public NonBlankFilter(@NonNull Object object) {
			this.object = object;
		}

		@Override
		public boolean test(Column column) {
			if (column.isPk()) {
				return false;
			}
			if (column.isVersioned()) {
				return false;
			}
			Object value = ReflectionUtils.getFieldValue(object, column.getJavaName());
			if (value == null) {
				return false;
			}
			if (value instanceof String) {
				return StringUtils.isNotBlank((String) value);
			}
			return true;
		}
	}
}
