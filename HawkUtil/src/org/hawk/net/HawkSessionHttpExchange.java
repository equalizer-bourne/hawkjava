package org.hawk.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

public class HawkSessionHttpExchange extends HttpExchange {
	/**
	 * 会话
	 */
	private HawkSession session;
	
	public HawkSessionHttpExchange(HawkSession session) {
		this.session = session;
	}
	
	@Override
	public void close() {
		
	}

	@Override
	public Object getAttribute(String arg0) {
		if (arg0 != null && arg0.equals("session")) {
			return session;
		}
		return null;
	}

	@Override
	public HttpContext getHttpContext() {
		return null;
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return null;
	}

	@Override
	public HttpPrincipal getPrincipal() {
		return null;
	}

	@Override
	public String getProtocol() {
		return "tcp";
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public InputStream getRequestBody() {
		return null;
	}

	@Override
	public Headers getRequestHeaders() {
		return null;
	}

	@Override
	public String getRequestMethod() {
		return null;
	}

	@Override
	public URI getRequestURI() {
		return null;
	}

	@Override
	public OutputStream getResponseBody() {
		return null;
	}

	@Override
	public int getResponseCode() {
		return 0;
	}

	@Override
	public Headers getResponseHeaders() {
		return null;
	}

	@Override
	public void sendResponseHeaders(int arg0, long arg1) throws IOException {
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
	}

	@Override
	public void setStreams(InputStream arg0, OutputStream arg1) {
	}
}
