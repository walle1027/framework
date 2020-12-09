package org.loed.framework.rpc;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author thomason
 * @version 1.0
 * @since 2018/8/25 下午9:03
 */
@Data
public class MethodProfile {
	private String baseUri;

	private WebClient webClient;

	private HttpMethod httpMethod;

	private String path;

	public MethodProfile() {
	}

	public void check() {
		if (StringUtils.isBlank(baseUri)) {
			throw new RuntimeException("redirectAddress or baseUri must be set at least");
		}
		Assert.notNull(httpMethod, "httpMethod is null");
		Assert.hasText(path, "path is empty");
	}

	public String getSchema() {
		if (baseUri.contains("://")) {
			return baseUri.substring(0, baseUri.indexOf("://"));
		}
		return null;
	}

	public int getPort() {
		if (baseUri.contains("://")) {
			String tmp = baseUri.substring(baseUri.indexOf("://") + 3);
			if (tmp.contains(":")) {
				return Integer.valueOf(tmp.substring(tmp.indexOf(":") + 1));
			}
			String schema = getSchema();
			if (StringUtils.equalsIgnoreCase("http", schema)) {
				return 80;
			} else if (StringUtils.equalsIgnoreCase("https", schema)) {
				return 443;
			}
		}
		return -1;
	}

	public String getHost() {
		if (baseUri.contains("://")) {
			String tmp = baseUri.substring(baseUri.indexOf("://") + 3);
			if (tmp.contains(":")) {
				return tmp.substring(0, tmp.indexOf(":"));
			}
			return tmp;
		}
		return null;
	}
}
