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
package org.nabucco.testautomation.engine.proxy.ws.command.soap.server;

import org.nabucco.testautomation.engine.proxy.ws.command.soap.AbstractSoapCommand;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.server.http.BlockingHttpServer;

import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * StopServerCommand
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class StopServerCommand extends AbstractSoapCommand {

	private BlockingHttpServer httpServer;
	
	/**
	 * 
	 * @param httpServer
	 */
	public StopServerCommand(BlockingHttpServer httpServer) {
		this.httpServer = httpServer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyList execute(Metadata metadata, PropertyList propertyList)
			throws WebServiceException {
		
		this.start();
		
		if (this.httpServer != null) {
			this.httpServer.stop();
		}
		this.httpServer = null;
		this.stop();
		return null;
	}
	
	/**
	 * 
	 * @return
	 */
	public BlockingHttpServer getHttpServer() {
		return this.httpServer;
	}
	
}
