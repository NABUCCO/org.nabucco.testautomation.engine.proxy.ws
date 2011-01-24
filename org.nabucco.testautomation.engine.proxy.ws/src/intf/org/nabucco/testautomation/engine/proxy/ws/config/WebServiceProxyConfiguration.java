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
package org.nabucco.testautomation.engine.proxy.ws.config;

import org.nabucco.testautomation.engine.proxy.config.ProxyEngineConfiguration;

/**
 * WebServiceProxyConfiguration
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public interface WebServiceProxyConfiguration extends ProxyEngineConfiguration {

	/**
     * The default WebService port.
     */
    public static final Integer DEFAULT_WEBSERVICE_PORT = 8080;
	
    /**
     * Gets the configured hostname.
     * 
     * @return the hostname
     */
    public String getServerHost();

    /**
     * Gets the configured server port.
     * 
     * @return the server port
     */
    public int getServerPort();

}
