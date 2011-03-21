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
package org.nabucco.testautomation.engine.proxy.ws;

import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.proxy.SubEngine;
import org.nabucco.testautomation.engine.proxy.base.AbstractProxyEngine;
import org.nabucco.testautomation.engine.proxy.config.ProxyEngineConfiguration;
import org.nabucco.testautomation.engine.proxy.exception.ProxyConfigurationException;
import org.nabucco.testautomation.engine.proxy.ws.WebServiceEngine;
import org.nabucco.testautomation.engine.proxy.ws.config.WebServiceProxyConfigImpl;
import org.nabucco.testautomation.engine.proxy.ws.config.WebServiceProxyConfiguration;

import org.nabucco.testautomation.facade.datatype.engine.SubEngineType;
import org.nabucco.testautomation.facade.datatype.engine.proxy.ProxyConfiguration;

/**
 * WebServiceProxyEngineImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class WebServiceProxyEngineImpl extends AbstractProxyEngine {

	private static final NBCTestLogger logger = NBCTestLoggingFactory
			.getInstance().getLogger(WebServiceProxyEngineImpl.class);

	private WebServiceEngine webServiceEngine;
	
	/**
	 * Constructs a new ProxyEngine with {@link SubEngineType.WS}.
	 */
	protected WebServiceProxyEngineImpl() {
		super(SubEngineType.WS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initialize() {
		// nothing todo so far
	}

	/**
     * {@inheritDoc}
     */
	@Override
	protected void configure(ProxyEngineConfiguration config)
			throws ProxyConfigurationException {
//		WebServiceProxyConfiguration wsConfig = (WebServiceProxyConfiguration) config;
		logger.info("WebServiceProxyEngine configured");
	}

	/**
     * {@inheritDoc}
     */
	@Override
	protected SubEngine start()
			throws ProxyConfigurationException {
		logger.info("Starting WebServiceProxyEngine ...");
		this.webServiceEngine = new WebServiceSubEngineImpl();
		// No action so far
		logger.info("WebServiceSubEngine created");
		return this.webServiceEngine;
	}

	/**
     * {@inheritDoc}
     */
	@Override
	protected void stop() throws ProxyConfigurationException {
		logger.info("Stopping WebServiceProxyEngine ...");
		
		if (this.webServiceEngine != null) {
			this.webServiceEngine.cleanup();
			this.webServiceEngine = null;
		}
	}

	/**
     * {@inheritDoc}
     */
	@Override
	protected void unconfigure() {
		logger.info("WebServiceProxyEngine unconfigured");
	}

	/**
     * {@inheritDoc}
     */
	@Override
	protected ProxyEngineConfiguration getProxyConfiguration(
			ProxyConfiguration configuration) {
		WebServiceProxyConfiguration config = new WebServiceProxyConfigImpl(
				getProxySupport().getProxyClassloader(), configuration);
		return config;
	}

}
