package org.loed.framework.common.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * @author Thomason
 * @version 1.0
 * @since 2015-12-24 13:33
 */

public class HttpUtils {
	private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	private static int CONNECTION_TIME_OUT = 10 * 60 * 1000;

	private static CloseableHttpClient httpClient;

	private static Lock lock = new ReentrantLock();

	public static CloseableHttpClient getHttpClient() {
		if (httpClient != null) {
			return httpClient;
		}
		try {
			lock.lock();
			if (httpClient != null) {
				return httpClient;
			}
			HttpClientBuilder httpClientBuilder = HttpClients.custom();
			Integer poolSize = 200;
			poolSize = poolSize > 1000 ? 1000 : poolSize;
			poolSize = poolSize < 10 ? 10 : poolSize;
			httpClientBuilder.setMaxConnTotal(poolSize);
			httpClientBuilder.setMaxConnPerRoute(poolSize);
			httpClientBuilder.disableCookieManagement();
			httpClientBuilder.disableAutomaticRetries();
			httpClientBuilder.setConnectionTimeToLive(60, TimeUnit.SECONDS);

			RequestConfig.Builder builder = RequestConfig.custom();
			builder.setConnectTimeout(CONNECTION_TIME_OUT);
			httpClientBuilder.setDefaultRequestConfig(builder.build());
			httpClientBuilder.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);
			httpClientBuilder.disableAuthCaching();
			httpClientBuilder.disableCookieManagement();
			httpClient = httpClientBuilder.build();
			return httpClient;
		} finally {
			lock.unlock();
		}
	}

	public static String get(String url, Map<String, String> headers, Map<String, String> parameterMap) throws IOException {
		CloseableHttpClient httpClient = getHttpClient();
		url = buildQueryString(url(url), parameterMap);
		HttpGet get = new HttpGet(url);
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				get.addHeader(entry.getKey(), entry.getValue());
			}
		}
		CloseableHttpResponse response = httpClient.execute(get);
		return getResponseString(url, response);
	}

	public static String delete(String url, Map<String, String> headers, Map<String, String> parameterMap) throws IOException {
		CloseableHttpClient httpClient = getHttpClient();
		url = buildQueryString(url, parameterMap);
		HttpDelete delete = new HttpDelete(url);
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				delete.addHeader(entry.getKey(), entry.getValue());
			}
		}
		CloseableHttpResponse response = httpClient.execute(delete);
		return getResponseString(url, response);
	}

	private static String buildQueryString(String url, Map<String, String> parameterMap) throws UnsupportedEncodingException {
		int i = 0;
		if (parameterMap != null) {
			StringBuilder urlBuilder = new StringBuilder(url);
			for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
				String key = URLEncoder.encode(entry.getKey(), "utf-8");
				String value = URLEncoder.encode(entry.getValue(), "utf-8");
				if (i == 0) {
					urlBuilder.append("?").append(key).append("=").append(value);
				} else {
					urlBuilder.append("&").append(key).append("=").append(value);
				}
				i++;
			}
			url = urlBuilder.toString();
		}
		return url;
	}

	public static String getResponseString(String url, CloseableHttpResponse response) throws IOException {
		try {
			if (response.getStatusLine().getStatusCode() != 200) {
				String error = EntityUtils.toString(response.getEntity(), "UTF-8");
				throw new RuntimeException("访问地址：" + url + "出错，错误代码：" + response.getStatusLine().getStatusCode() + "\n" + error);
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity, "UTF-8");
			}
			return null;
		} finally {
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}

	public static String post(String url, Map<String, String> headers, Map<String, String> parameterMap) throws IOException {
		CloseableHttpClient httpClient = getHttpClient();
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (parameterMap != null) {
			for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpPost.addHeader(entry.getKey(), entry.getValue());
			}
		}
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		CloseableHttpResponse response = httpClient.execute(httpPost);
		return getResponseString(url, response);
	}

	public static String postBody(String url, Map<String, String> headers, String body) {
		return postBody(url, headers, body, ContentType.APPLICATION_JSON);
	}


	public static String postBody(String url, Map<String, String> headers, String body, ContentType contentType) {
		CloseableHttpClient httpClient = getHttpClient();
		HttpPost post = new HttpPost(url);
		StringEntity stringEntity = new StringEntity(body, contentType);
		post.setEntity(stringEntity);
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				post.addHeader(entry.getKey(), entry.getValue());
			}
		}
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(post);
			return getResponseString(url, response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String putBody(String url, Map<String, String> headers, String body) {
		return putBody(url, headers, body, ContentType.APPLICATION_JSON);
	}


	public static String putBody(String url, Map<String, String> headers, String body, ContentType contentType) {
		CloseableHttpClient httpClient = getHttpClient();
		HttpPut put = new HttpPut(url);
		StringEntity stringEntity = new StringEntity(body, contentType);
		put.setEntity(stringEntity);
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				put.addHeader(entry.getKey(), entry.getValue());
			}
		}
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(put);
			return getResponseString(url, response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void download(String url, Map<String, String> headers, Map<String, String> parameterMap, String savedFileName) throws IOException {
		CloseableHttpClient httpClient = getHttpClient();
		HttpPost httpPost = new HttpPost(url(url));
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (parameterMap != null) {
			for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpPost.addHeader(entry.getKey(), entry.getValue());
			}
		}

		httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		FileOutputStream fos = null;
		try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
			if (response.getStatusLine().getStatusCode() != 200) {
				String error = EntityUtils.toString(response.getEntity(), "UTF-8");
				throw new RuntimeException("访问地址：" + url + "出错，错误代码：" + response.getStatusLine().getStatusCode() + "\n" + error);
			}
			HttpEntity entity = response.getEntity();
			if (entity.isStreaming()) {
				fos = new FileOutputStream(savedFileName);
				entity.writeTo(fos);
			}
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}

	public static void upload(String url, Map<String, String> headers, File file) {
		CloseableHttpClient httpClient = getHttpClient();
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpPost.addHeader(entry.getKey(), entry.getValue());
			}
		}
		HttpEntity httpEntity = MultipartEntityBuilder.create().addBinaryBody("file", file, ContentType.MULTIPART_FORM_DATA, file.getName()).build();
		httpPost.setEntity(httpEntity);
		try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
			if (response.getStatusLine().getStatusCode() != 200) {
				String error = EntityUtils.toString(response.getEntity(), "UTF-8");
				throw new RuntimeException("访问地址：" + url + "出错，错误代码：" + response.getStatusLine().getStatusCode() + "\n" + error);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String download(String url, Function<HttpResponse, String> consumer) {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		CloseableHttpResponse response = null;
		try {
			HttpGet get = new HttpGet(url(url));
			response = httpClient.execute(get);
			HttpEntity entity = response.getEntity();
			if (entity.isStreaming()) {
				String savePath = consumer.apply(response);
				if (savePath != null) {
					return writeEntityToFile(entity, savePath);
				}
			}
		} catch (Exception e) {
			logger.error("error download image from url:" + url, e);
		} finally {
			IOUtils.closeQuietly(response);
		}
		return null;
	}

	public static String downloadWithIp(String ip, String url, Function<HttpResponse, String> consumer) {
		if (StringUtils.isBlank(ip)) {
			return download(url, consumer);
		}
		CloseableHttpClient httpClient = createHttpClientWithIp(ip);
		CloseableHttpResponse response = null;
		try {
			HttpGet get = new HttpGet(url(url));
			response = httpClient.execute(get);
			HttpEntity entity = response.getEntity();
			if (entity.isStreaming()) {
				String savePath = consumer.apply(response);
				if (savePath != null) {
					return writeEntityToFile(entity, savePath);
				}
			}
		} catch (IOException e) {
			logger.error("error download image from url:" + url, e);
		} finally {
			IOUtils.closeQuietly(response);
		}
		return null;
	}

	public static CloseableHttpClient createHttpClientWithIp(String ip) {
		RequestConfig defaultRequestConfig = null;
		try {
			defaultRequestConfig = RequestConfig.custom()
					.setLocalAddress(InetAddress.getByName(ip))
					.build();

			HttpClientBuilder clientBuilder = HttpClients.custom();
			clientBuilder.setDefaultRequestConfig(defaultRequestConfig);
			return clientBuilder.build();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String writeEntityToFile(HttpEntity entity, String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		FileUtils.copyInputStreamToFile(entity.getContent(), file);
		return file.getAbsolutePath();
	}

	public static CloseableHttpClient createHttpClientWithProxy(String host, int port, String schema, String username, String password) {
		HttpHost httpHost = new HttpHost(host, port, schema);
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setProxy(httpHost)
				.build();
		HttpClientBuilder clientBuilder = HttpClients.custom();
		clientBuilder.setDefaultRequestConfig(defaultRequestConfig);
		if (username != null) {
			BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(username, password));
			clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		}
		return clientBuilder.build();
	}

	private static String url(String url) {
		String ret = StringUtils.replace(url, " ", "%20");
		ret = StringUtils.replace(ret, "!", "%21");
		ret = StringUtils.replace(ret, "\"", "%22");
		ret = StringUtils.replace(ret, "#", "%23");
		ret = StringUtils.replace(ret, "$", "%24");
		ret = StringUtils.replace(ret, "'", "%27");
		ret = StringUtils.replace(ret, "(", "%28");
		ret = StringUtils.replace(ret, ")", "%29");
		ret = StringUtils.replace(ret, "*", "%2A");
		ret = StringUtils.replace(ret, "+", "%2B");
		ret = StringUtils.replace(ret, ",", "%2C");
		ret = StringUtils.replace(ret, "-", "%2D");
		ret = StringUtils.replace(ret, ".", "%2E");
		return ret;
	}
}
