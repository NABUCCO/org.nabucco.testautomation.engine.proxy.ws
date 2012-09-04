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
package org.nabucco.testautomation.engine.proxy.ws.soap;

import org.nabucco.testautomation.engine.proxy.ProxyCommand;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * SoapCommand
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public interface SoapCommand extends ProxyCommand {

	public static final String SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";
	
	public static final String PATH = "PATH";

	public static final String PORT = "PORT";

	public static final String HOST = "HOST";

	public static final String TIMEOUT = "TIMEOUT";
	
	public static final String XSD = "XSD";
	
	/**
	 * 
	 * @param metadata the metadata to execute
	 * @param properties the PropertyList from the calling Action
	 * @return a PropertyList containing the return values
	 * @throws WebServiceException thrown, if an error occurs during the webservice call
	 */
	public PropertyList execute(Metadata metadata, PropertyList properties) throws WebServiceException;
	
}
