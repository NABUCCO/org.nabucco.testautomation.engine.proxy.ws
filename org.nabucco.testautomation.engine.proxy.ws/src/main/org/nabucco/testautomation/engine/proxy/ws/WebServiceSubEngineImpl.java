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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.proxy.SubEngineActionType;
import org.nabucco.testautomation.engine.proxy.SubEngineOperationType;
import org.nabucco.testautomation.engine.proxy.base.AbstractSubEngine;
import org.nabucco.testautomation.engine.proxy.exception.SubEngineException;
import org.nabucco.testautomation.engine.proxy.ws.WebServiceActionType;
import org.nabucco.testautomation.engine.proxy.ws.WebServiceEngine;
import org.nabucco.testautomation.engine.proxy.ws.WebServiceEngineOperationType;
import org.nabucco.testautomation.engine.proxy.ws.command.soap.client.SoapClientImpl;
import org.nabucco.testautomation.engine.proxy.ws.command.soap.server.SoapServerImpl;
import org.nabucco.testautomation.engine.proxy.ws.soap.SoapClient;
import org.nabucco.testautomation.engine.proxy.ws.soap.SoapServer;

import org.nabucco.testautomation.facade.datatype.engine.SubEngineType;
import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.result.facade.datatype.ActionResponse;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * WebServiceSubEngineImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class WebServiceSubEngineImpl extends AbstractSubEngine implements WebServiceEngine {

    private static final long serialVersionUID = 1L;

    private static final NBCTestLogger logger = NBCTestLoggingFactory.getInstance().getLogger(
            WebServiceSubEngineImpl.class);

    private SoapClient soapClient;

    private SoapServer soapServer;

    /**
     * Constructs a new WebServiceEngine instance interacting with the given host and port.
     */
    public WebServiceSubEngineImpl() {
    	super();
		this.soapClient = new SoapClientImpl();
		this.soapServer = new SoapServerImpl();
	}
    
    /**
     * {@inheritDoc}
     */
	@Override
	public ActionResponse executeSubEngineOperation(
			SubEngineOperationType operationType,
			SubEngineActionType actionType, List<Metadata> metadataList,
			PropertyList propertyList, TestContext context)
			throws SubEngineException {

		// Map OperationType
		WebServiceEngineOperationType wsEngineOperationType = (WebServiceEngineOperationType) operationType;
		
		// execute operation
		switch (wsEngineOperationType) {

		case SOAP_CLIENT: {
			return this.getSoapClient().execute(context, propertyList,
					metadataList, actionType);
		}
		case SOAP_SERVER: {
			return this.getSoapServer().execute(context, propertyList,
					metadataList, actionType);
		}
		case HTTP_CLIENT: {
			
		}
		case HTTP_SERVER: {
			
		}
		default: {
			String error = "Unsupported WebServiceEngineOperationType = '"
					+ operationType + "'";
			logger.error(error);
			throw new UnsupportedOperationException(error);
		}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cleanup() {
		
		if (this.soapServer != null) {
			this.soapServer.cleanup();
		}
	}

	/**
     * {@inheritDoc}
     */
	@Override
    public SoapClient getSoapClient() {
        return this.soapClient;
    }
	
	/**
     * {@inheritDoc}
     */
	@Override
    public SoapServer getSoapServer() {
        return this.soapServer;
    }

	/**
     * {@inheritDoc}
     */
	@Override
	public Map<String, SubEngineActionType> getActions() {
		Map<String, SubEngineActionType> actions = new HashMap<String, SubEngineActionType>();
		
		for (WebServiceActionType action : WebServiceActionType.values()) {
			actions.put(action.toString(), action);
		}		
		return actions;
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public Map<String, SubEngineOperationType> getOperations() {
		Map<String, SubEngineOperationType> operations = new HashMap<String, SubEngineOperationType>();
		
		for (WebServiceEngineOperationType operation : WebServiceEngineOperationType.values()) {
			operations.put(operation.toString(), operation);
		}
		return operations;
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public SubEngineType getType() {
		return SubEngineType.WS;
	}

}
