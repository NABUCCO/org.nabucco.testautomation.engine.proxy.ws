/*
* Copyright 2010 PRODYNA AG
*
* Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.opensource.org/licenses/eclipse-1.0.php or
* http://www.nabucco-source.org/nabucco-license.html
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.nabucco.testautomation.engine.proxy.ws.server.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * HttpRequestHandler
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
class HttpRequestHandler implements HttpHandler {

	private List<HttpRequestListener> listener;
	
	HttpRequestHandler() {
		this.listener = new ArrayList<HttpRequestListener>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		HttpRequest request = new HttpRequest(exchange);
		requestreceived(request);
	}
	
	private void requestreceived(HttpRequest request) {
		
		for (HttpRequestListener listener : this.listener) {
			listener.httpRequestreceived(request);
		}
	}
	
	public void addHttpRequestListener(HttpRequestListener listener) {
		this.listener.add(listener);
	}
	
	public void removeHttpRequestListener(HttpRequestListener listener) {
		this.listener.remove(listener);
	}
	
}
