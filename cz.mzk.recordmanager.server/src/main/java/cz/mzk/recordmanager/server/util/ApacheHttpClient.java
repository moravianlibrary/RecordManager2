package cz.mzk.recordmanager.server.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.common.io.Closeables;

// Thread safe
public class ApacheHttpClient implements HttpClient, Closeable {

	private final CloseableHttpClient httpClient;
	
	private static class ApacheHttpClientInputStream extends InputStream implements Closeable {

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
		this.httpClient = HttpClients.createDefault();
	}

	@Override
	public InputStream executeGet(String url) throws IOException {
		CloseableHttpResponse result = httpClient.execute(new HttpGet(url));
		InputStream is = result.getEntity().getContent();
		return new ApacheHttpClientInputStream(result, is);
	}

	@Override
	public void close() throws IOException {
		httpClient.close();
	}

}
