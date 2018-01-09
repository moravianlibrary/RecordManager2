package cz.mzk.recordmanager.server.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.common.io.Closeables;

// Thread safe
public class ApacheHttpClient implements HttpClient, Closeable {

	private final CloseableHttpClient httpClient;

	private static final int TIMEOUT = 300;

	public static class ApacheHttpClientInputStream extends InputStream implements Closeable {

		private final CloseableHttpResponse response;
		
		private final InputStream delegate;

		public ApacheHttpClientInputStream(CloseableHttpResponse response,
				InputStream delegate) {
			super();
			this.response = response;
			this.delegate = delegate;
		}

		@Override
		public int available() throws IOException {
			return delegate.available();
		}

		@Override
		public void mark(int readlimit) {
			delegate.mark(readlimit);
		}

		@Override
		public boolean markSupported() {
			return delegate.markSupported();
		}

		@Override
		public int read() throws IOException {
			return delegate.read();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return delegate.read(b, off, len);
		}

		@Override
		public int read(byte[] b) throws IOException {
			return delegate.read(b);
		}

		@Override
		public void reset() throws IOException {
			delegate.reset();
		}

		@Override
		public long skip(long n) throws IOException {
			return delegate.skip(n);
		}
		
		@Override
		public void close() throws IOException {
			Closeables.closeQuietly(delegate);
			Closeables.close(response, true);
		}

	}

	public ApacheHttpClient() {
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(TIMEOUT * 1000)
				.setConnectionRequestTimeout(TIMEOUT * 1000)
				.setSocketTimeout(TIMEOUT * 1000)
				.build();
		this.httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	}

	@Override
	public InputStream executeGet(String url) throws IOException {
		return executeGet(url, Collections.emptyMap());
	}

	@Override
	public void close() throws IOException {
		httpClient.close();
	}

	@Override
	public ApacheHttpClientInputStream executeGet(String url, Map<String, String> headers)
			throws IOException {
		HttpGet get = new HttpGet(url);
		headers.forEach((key, value) -> get.addHeader(key, value));
		CloseableHttpResponse result = httpClient.execute(get);
		int statusCode = result.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			result.close();
			throw new IOException(String.format("Bad status code: %s", statusCode));
		}
		InputStream is = result.getEntity().getContent();
		return new ApacheHttpClientInputStream(result, is);
	}

}
