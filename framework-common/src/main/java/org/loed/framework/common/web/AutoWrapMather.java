package org.loed.framework.common.web;

import org.springframework.http.server.RequestPath;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thomason
 * @version 1.0
 * @since 2020/8/13 3:53 下午
 */
public class AutoWrapMather {
	private static final List<PathPattern> ignorePathPatternList = new ArrayList<>();

	{
		ignorePathPatternList.add(new PathPatternParser().parse("/swagger-ui.html"));
		ignorePathPatternList.add(new PathPatternParser().parse("/webjars/**"));
		ignorePathPatternList.add(new PathPatternParser().parse("/swagger-resources/**"));
		ignorePathPatternList.add(new PathPatternParser().parse("/v2/api-docs"));
		ignorePathPatternList.add(new PathPatternParser().parse("/health"));
	}

	public static boolean shouldWrap(RequestPath path) {
		for (PathPattern pathPattern : ignorePathPatternList) {
			if (pathPattern.matches(path.pathWithinApplication())) {
				return false;
			}
		}
		return true;
	}

	public static void addIgnore(String pathPattern) {
		ignorePathPatternList.add(new PathPatternParser().parse(pathPattern));
	}
}
