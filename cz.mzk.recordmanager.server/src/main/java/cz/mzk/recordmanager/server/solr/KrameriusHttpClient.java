package cz.mzk.recordmanager.server.solr;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@SuppressWarnings("deprecation")
public class KrameriusHttpClient implements HttpClient {

	private static final String ACCEPT_HEADER_NAME = "Accept";

	private static final String ACCEPT_HEADER_VALUE = "application/xml";

	private HttpClient client = HttpClientBuilder.create().build();

	@Override
	public HttpParams getParams() {
		return client.getParams();
	}

	@Override
	public ClientConnectionManager getConnectionManager() {
		return client.getConnectionManager();
	}

	@Override
	public HttpResponse execute(HttpUriRequest request)
			throws IOException, ClientProtocolException {
		modifyRequest(request);
		return client.execute(request);
	}

	@Override
	public HttpResponse execute(HttpUriRequest request,
			HttpContext context) throws IOException,
			ClientProtocolException {
		modifyRequest(request);
		return client.execute(request, context);
	}

	@Override
	public HttpResponse execute(HttpHost target,
			HttpRequest request) throws IOException,
			ClientProtocolException {
		modifyRequest(request);
		return client.execute(target, request);
	}

	@Override
	public HttpResponse execute(HttpHost target,
			HttpRequest request, HttpContext context)
			throws IOException, ClientProtocolException {
		modifyRequest(request);
		return client.execute(target, request, context);
	}

	@Override
	public <T> T execute(HttpUriRequest request,
			ResponseHandler<? extends T> responseHandler)
			throws IOException, ClientProtocolException {
		modifyRequest(request);
		return client.execute(request, responseHandler);
	}

	@Override
	public <T> T execute(HttpUriRequest request,
			ResponseHandler<? extends T> responseHandler,
			HttpContext context) throws IOException,
			ClientProtocolException {
		modifyRequest(request);
		return client
				.execute(request, responseHandler, context);
	}

	@Override
	public <T> T execute(HttpHost target, HttpRequest request,
			ResponseHandler<? extends T> responseHandler)
			throws IOException, ClientProtocolException {
		modifyRequest(request);
		return client.execute(target, request, responseHandler);
	}

	@Override
	public <T> T execute(HttpHost target, HttpRequest request,
			ResponseHandler<? extends T> responseHandler,
			HttpContext context) throws IOException,
			ClientProtocolException {
		modifyRequest(request);
		return client.execute(target, request, responseHandler,
				context);
	}

	private void modifyRequest(HttpRequest request) {
		if (!request.containsHeader(ACCEPT_HEADER_NAME)) {
			request.addHeader(ACCEPT_HEADER_NAME, ACCEPT_HEADER_VALUE);
		}
	}

}
