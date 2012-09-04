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
package org.nabucco.testautomation.engine.proxy.ws.command.soap.server;

import java.io.IOException;

import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.engine.proxy.ws.command.soap.AbstractSoapCommand;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.server.http.BlockingHttpServer;
import org.nabucco.testautomation.property.facade.datatype.NumericProperty;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.TextProperty;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyType;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * StartServerCommand
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class StartServerCommand extends AbstractSoapCommand {

	private BlockingHttpServer httpServer;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyList execute(Metadata metadata, PropertyList propertyList)
			throws WebServiceException {
		
		String host = getHost(metadata, propertyList);
		Integer port = getPort(metadata, propertyList);
		String path = getPath(metadata, propertyList);
		
		try {
			this.httpServer = new BlockingHttpServer(host, port, path);
			this.start();
			this.httpServer.start();
			this.stop();
			this.info("WebService-Server started on: " + host + ":" + port + path);
			return null;
		} catch (IOException ex) {
			this.setException(ex);
			throw new WebServiceException(ex.getMessage());
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public BlockingHttpServer getHttpServer() {
		return this.httpServer;
	}
	
	/**
	 * 
	 * @param metadata
	 * @param properties
	 * @return
	 */
	private String getHost(Metadata metadata, PropertyList properties) {
		
		// First, check PropertyList from Action
		Property hostProperty = PropertyHelper.getFromList(properties,
				HOST);

		if (hostProperty != null && hostProperty.getType() == PropertyType.TEXT) {
			return ((TextProperty) hostProperty).getValue().getValue();
		}
		
		// Second, check PropertyList from Metadata
		hostProperty = PropertyHelper.getFromList(metadata.getPropertyList(),
				HOST);
		
		if (hostProperty != null && hostProperty.getType() == PropertyType.TEXT) {
			return ((TextProperty) hostProperty).getValue().getValue();
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param metadata
	 * @param properties
	 * @return
	 */
	private int getPort(Metadata metadata, PropertyList properties) {
		
		// First, check PropertyList from Action
		Property portProperty = PropertyHelper.getFromList(properties,
				PORT);

		if (portProperty != null && portProperty.getType() == PropertyType.NUMERIC) {
			return ((NumericProperty) portProperty).getValue().getValue().intValue();
		}
		
		// Second, check PropertyList from Metadata
		portProperty = PropertyHelper.getFromList(metadata.getPropertyList(),
				PORT);
		
		if (portProperty != null && portProperty.getType() == PropertyType.NUMERIC) {
			return ((NumericProperty) portProperty).getValue().getValue().intValue();
		}
		
		return 0;
	}

	/**
	 * 
	 * @param metadata
	 * @param properties
	 * @return
	 */
	private String getPath(Metadata metadata, PropertyList properties) {
		
		// First, check PropertyList from Action
		Property pathProperty = PropertyHelper.getFromList(properties,
				PATH);

		if (pathProperty != null && pathProperty.getType() == PropertyType.TEXT) {
			return ((TextProperty) pathProperty).getValue().getValue();
		}
		
		// Second, check PropertyList from Metadata
		pathProperty = PropertyHelper.getFromList(metadata.getPropertyList(),
				PATH);
		
		if (pathProperty != null && pathProperty.getType() == PropertyType.TEXT) {
			return ((TextProperty) pathProperty).getValue().getValue();
		}
		
		return null;
	}
	
}
