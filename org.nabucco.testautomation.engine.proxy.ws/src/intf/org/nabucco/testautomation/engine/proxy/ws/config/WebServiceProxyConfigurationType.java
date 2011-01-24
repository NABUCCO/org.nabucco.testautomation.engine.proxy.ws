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

/**
 * WebServiceProxyConfigurationType
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public enum WebServiceProxyConfigurationType {

    /**
     * Constant for the property-key for the hostname.
     */
    WS_SERVER_HOST("serverHost"),

    /**
     * Constant for the property-key for the server port.
     */
    WS_SERVER_PORT("serverPort");
    
	private String key;
    
    private WebServiceProxyConfigurationType(String key) {
    	this.key = key;    	 
    }
    
    public String getKey() {
    	return this.key;
    }
	
}
