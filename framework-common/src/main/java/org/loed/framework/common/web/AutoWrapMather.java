package org.loed.framework.common.web;

import org.springframework.http.server.RequestPath;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.HashSet;
import java.util.Set;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/13 3:53 下午
 */
public class AutoWrapMather {
	private static final Set<PathPattern> IGNORE_PATH_PATTERN = new HashSet<>();

	static {
		PathPatternParser pathPatternParser = new PathPatternParser();
		pathPatternParser.setCaseSensitive(false);
		IGNORE_PATH_PATTERN.add(pathPatternParser.parse("/swagger-ui.html"));
		IGNORE_PATH_PATTERN.add(pathPatternParser.parse("/webjars/*"));
		IGNORE_PATH_PATTERN.add(pathPatternParser.parse("/swagger-resources/*"));
		IGNORE_PATH_PATTERN.add(pathPatternParser.parse("/v2/api-docs"));
		IGNORE_PATH_PATTERN.add(pathPatternParser.parse("/health"));
		IGNORE_PATH_PATTERN.add(pathPatternParser.parse("/actuator/*"));
		IGNORE_PATH_PATTERN.add(pathPatternParser.parse("/actuator"));
	}

	public static boolean shouldWrap(RequestPath path) {
		for (PathPattern pathPattern : IGNORE_PATH_PATTERN) {
			if (pathPattern.matches(path.pathWithinApplication())) {
				return false;
			}
		}
		return true;
	}

	public static void addIgnore(String pathPattern) {
		PathPatternParser pathPatternParser = new PathPatternParser();
		pathPatternParser.setCaseSensitive(false);
		IGNORE_PATH_PATTERN.add(pathPatternParser.parse(pathPattern));
	}
}
