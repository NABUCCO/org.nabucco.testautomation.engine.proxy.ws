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
package org.nabucco.testautomation.engine.proxy.ws.config;

import org.nabucco.testautomation.engine.proxy.config.AbstractProxyEngineConfiguration;
import org.nabucco.testautomation.settings.facade.datatype.engine.proxy.ProxyConfiguration;

/**
 * WebServiceProxyConfigImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class WebServiceProxyConfigImpl extends AbstractProxyEngineConfiguration
		implements WebServiceProxyConfiguration {

    /**
     * Creates a new instance getting the configuration from
     * the given Properties.
     * 
     * @param the classloader that loaded the proxy
     * @param properties the properties containing the configuration
     */
	public WebServiceProxyConfigImpl(ClassLoader classloader,
			ProxyConfiguration configuration) {
		super(classloader, configuration);
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public String getDefaultNamespacePrefix() {
		return this.getConfigurationValue(WebServiceProxyConfigurationType.DEFAULT_NS_PREFIX.getKey()).trim();
	}

}
