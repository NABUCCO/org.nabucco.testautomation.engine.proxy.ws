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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.sun.net.httpserver.HttpExchange;

/**
 * HttpRequest
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class HttpRequest {

	public static final String DEFAULT_ENCODING = "ISO-8859-1";

	private final HttpExchange exchange;
	
	private String encoding = DEFAULT_ENCODING;
	
	private String requestBody;
	
	/**
	 * 
	 * @param exchange
	 * @throws NullpointerException thrown, if no HttpExchange is provided
	 */
	public HttpRequest(HttpExchange exchange) {
		
		if (exchange == null) {
			throw new NullPointerException("No HttpExchange provided");
		}
		this.exchange = exchange;
	}
	
	public String getRequestMethod() {
		String requestMethod = this.exchange.getRequestMethod();
		return requestMethod;
	}
	
	public String getClientAddress() {
		return this.exchange.getRemoteAddress().toString();
	}
	
	public String getRequestBody() throws UnsupportedEncodingException, IOException {
		
		if (this.requestBody == null) {
			InputStream in = this.exchange.getRequestBody();
			StringBuffer request = new StringBuffer();
			byte[] buf = new byte[1024];
			int c;
			
			while ((c = in.read(buf)) != -1) {
				request.append(new String(buf, 0, c, this.encoding));
			}
			this.requestBody = request.toString();
		}
		return this.requestBody;
	}
	
	public void respond(String response) throws UnsupportedEncodingException, IOException {
		
		this.exchange.sendResponseHeaders(200, 0);
		OutputStream out = this.exchange.getResponseBody();
		out.write(response.getBytes(this.encoding));
		out.close();
	}

	/**
	 * @param encoding the encoding to set
	 */
	public void setEncoding(String encoding) {
		
		if (encoding == null) {
			this.encoding = DEFAULT_ENCODING;
		} else {		
			this.encoding = encoding;
		}
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}
	
}
