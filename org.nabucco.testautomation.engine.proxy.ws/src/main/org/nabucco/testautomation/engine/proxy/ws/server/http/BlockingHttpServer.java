/*
 * Copyright 2012 PRODYNA AG
 *
 * Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/eclipse-1.0.php or
 * http://www.nabucco.org/License.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nabucco.testautomation.engine.proxy.ws.server.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpServer;

/**
 * BlockingHttpServer
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class BlockingHttpServer implements HttpRequestListener {

	private static final String DEFAULT_HOST = "0.0.0.0";

	private static final String DEFAULT_PATH = "/";
	
	private static final int DEFAULT_PORT = 80;

	private static final int DEFAULT_QUEUE_SIZE = 3;
	
	private static final String EMPTY_STRING = "";
	
	private HttpServer server;
	
	private BlockingQueue<HttpRequest> requestQueue;
	
	private int queueSize = DEFAULT_QUEUE_SIZE;
	
	private String host = DEFAULT_HOST;
	
	private int port = DEFAULT_PORT;
	
	private String path = DEFAULT_PATH;
	
	public BlockingHttpServer() {
		this(DEFAULT_HOST, DEFAULT_PORT);
	}
	
	public BlockingHttpServer(String path) {
		this(DEFAULT_HOST, DEFAULT_PORT, path);
	}

	public BlockingHttpServer(String host, int port) {
		this(host, port, DEFAULT_PATH);
	}
	
	public BlockingHttpServer(String host, int port, String path) {
		this.setHost(host);
		this.setPort(port);
		this.setPath(path);
	}
	
	public void start() throws IOException {

		InetSocketAddress addr = new InetSocketAddress(host, port);
		HttpRequestHandler httpRequestHandler = new HttpRequestHandler();
	    httpRequestHandler.addHttpRequestListener(this);
		
		this.server = HttpServer.create(addr, 0);
		this.server.createContext(this.path, httpRequestHandler);		
		this.requestQueue = new ArrayBlockingQueue<HttpRequest>(this.queueSize, true);
		this.server.start();
	}
	
	public void stop() {
		this.requestQueue.clear();
		this.requestQueue = null;
		this.server.stop(0);
		this.server = null;
	}
	
	public HttpRequest receive(long timeout) throws InterruptedException, HttpException {
		
		if (this.requestQueue == null) {
			throw new HttpException("HttpServer is not started");
		}
		
		HttpRequest request = this.requestQueue.poll(timeout, TimeUnit.MILLISECONDS);
		
		if (request == null) {
			throw new HttpException("No message received in time");
		}
		return request;
	}

	@Override
	public void httpRequestreceived(HttpRequest request) {
		
		try {
			this.requestQueue.offer(request, 30000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}		
	}
	
	public int getQueueSize() {
		return this.queueSize;
	}

	public void setQueueSize(int queueSize) {
		
		if (queueSize <= 0) {
			this.queueSize = DEFAULT_QUEUE_SIZE;
		} else {		
			this.queueSize = queueSize;
		}
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		
		if (host == null || host.equals(EMPTY_STRING)) {
			this.host = DEFAULT_HOST;
		} else {
			this.host = host;
		}
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		
		if (port <= 0) {
			this.port = DEFAULT_PORT;
		} else {
			this.port = port;
		}
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		
		if (path == null || path.equals(EMPTY_STRING)) {
			this.path = DEFAULT_PATH;
		} else {
			this.path = path;
		}
	}
	
}
