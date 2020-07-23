package org.loed.framework;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.loed.framework.common.util.HttpUtils;

import java.io.IOException;

/**
 * @author thomason
 * @version 1.0
 * @since 2019/3/22 11:34 PM
 */
public class HttpUtilsTest {
	@Test
	public void testDownload() {
		String url = "https://designbundles.net/download.php?id=224769&free=1";
		HttpUtils.download(url, (r) -> {
			System.out.println(r.getStatusLine().getStatusCode());
			boolean streaming = r.getEntity().isStreaming();
			try {
				String s = IOUtils.toString(r.getEntity().getContent(), "UTF-8");
				System.out.println(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "/Users/Thomason/data/save.zip";
		});
	}
}
