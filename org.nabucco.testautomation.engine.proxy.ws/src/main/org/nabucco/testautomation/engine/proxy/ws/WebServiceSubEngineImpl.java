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
package org.nabucco.testautomation.engine.proxy.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.proxy.SubEngineActionType;
import org.nabucco.testautomation.engine.proxy.SubEngineOperationType;
import org.nabucco.testautomation.engine.proxy.base.AbstractSubEngine;
import org.nabucco.testautomation.engine.proxy.exception.SubEngineException;
import org.nabucco.testautomation.engine.proxy.ws.client.soap.SoapVersion;
import org.nabucco.testautomation.engine.proxy.ws.command.rest.client.RestClientImpl;
import org.nabucco.testautomation.engine.proxy.ws.command.soap.client.SoapClientImpl;
import org.nabucco.testautomation.engine.proxy.ws.command.soap.server.SoapServerImpl;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.rest.RestClient;
import org.nabucco.testautomation.engine.proxy.ws.soap.SoapClient;
import org.nabucco.testautomation.engine.proxy.ws.soap.SoapServer;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.result.facade.datatype.ActionResponse;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;
import org.nabucco.testautomation.settings.facade.datatype.engine.SubEngineType;

/**
 * WebServiceSubEngineImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class WebServiceSubEngineImpl extends AbstractSubEngine implements WebServiceEngine {

    private static final long serialVersionUID = 1L;

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(
            WebServiceSubEngineImpl.class);

    private SoapClient soapClient_1_1;

    private SoapClient soapClient_1_2;

    private SoapServer soapServer;
    
    private RestClient restClient;

    /**
     * Constructs a new WebServiceEngine instance interacting with the given host and port.
     * 
     * @throws WebServiceException
     */
    public WebServiceSubEngineImpl(String defaultNSPrefix) throws WebServiceException {
        super();
        this.soapClient_1_1 = new SoapClientImpl(SoapVersion.V_1_1, defaultNSPrefix);
        this.soapClient_1_2 = new SoapClientImpl(SoapVersion.V_1_2, defaultNSPrefix);
        this.soapServer = new SoapServerImpl(defaultNSPrefix);
        this.restClient = new RestClientImpl(defaultNSPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResponse executeSubEngineOperation(SubEngineOperationType operationType,
            SubEngineActionType actionType, List<Metadata> metadataList, PropertyList propertyList, TestContext context)
            throws SubEngineException {

        // Map OperationType
        WebServiceEngineOperationType wsEngineOperationType = (WebServiceEngineOperationType) operationType;

        // execute operation
        switch (wsEngineOperationType) {

        case SOAP_1_1_CLIENT:
            return this.getSoap11Client().execute(context, propertyList, metadataList, actionType);
        case SOAP_1_2_CLIENT:
            return this.getSoap12Client().execute(context, propertyList, metadataList, actionType);
        case SOAP_SERVER:
            return this.getSoapServer().execute(context, propertyList, metadataList, actionType);
        case REST_CLIENT:
            return this.getRestClient().execute(context, propertyList, metadataList, actionType);
        case REST_SERVER:
            // Not supported yet
        default:
            String error = "Unsupported WebServiceEngineOperationType = '" + operationType + "'";
            logger.error(error);
            throw new UnsupportedOperationException(error);
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
    public SoapClient getSoap11Client() {
        return this.soapClient_1_1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SoapClient getSoap12Client() {
        return this.soapClient_1_2;
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
    public RestClient getRestClient() {
        return this.restClient;
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
