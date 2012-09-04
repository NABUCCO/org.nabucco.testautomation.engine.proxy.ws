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

import java.util.List;

import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.exception.InterruptionException;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.proxy.ProxyCommand;
import org.nabucco.testautomation.engine.proxy.SubEngineActionType;
import org.nabucco.testautomation.engine.proxy.ws.WebServiceActionType;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.server.http.BlockingHttpServer;
import org.nabucco.testautomation.engine.proxy.ws.server.http.HttpRequest;
import org.nabucco.testautomation.engine.proxy.ws.soap.SoapServer;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.result.facade.datatype.ActionResponse;
import org.nabucco.testautomation.result.facade.datatype.status.ActionStatusType;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * SoapServerImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class SoapServerImpl implements SoapServer {

    private static final long serialVersionUID = 1L;

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(SoapServerImpl.class);

    private BlockingHttpServer httpServer;

    private HttpRequest lastRequest;

    private final String defaultNSPrefix;

    /**
     * Constructs a new instance providing a SOAP-WebService received by the given HttpServer.
     * 
     * @param defaultNSPrefix
     *            the default namespace prefix
     */
    public SoapServerImpl(String defaultNSPrefix) {
        this.defaultNSPrefix = defaultNSPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResponse execute(TestContext context, PropertyList propertyList, List<Metadata> metadataList,
            SubEngineActionType actionType) throws WebServiceException {

        ActionResponse result = TestResultHelper.createActionResponse();
        ProxyCommand command = null;

        try {
            result.setMessage("Executing WebService action='" + actionType + "'");

            // check request-arguments
            validateArguments(context, propertyList, metadataList, actionType);

            // get the metadata to be executed
            Metadata metadata = getLeaf(metadataList);
            PropertyList returnProperties = null;

            switch ((WebServiceActionType) actionType) {
            case START: {
                StartServerCommand serverCommand = new StartServerCommand();
                serverCommand.execute(metadata, propertyList);
                this.httpServer = serverCommand.getHttpServer();
                command = serverCommand;
                break;
            }
            case RECEIVE: {
                ReceiveSoapCommand serverCommand = new ReceiveSoapCommand(this.httpServer, this.defaultNSPrefix);
                returnProperties = serverCommand.execute(metadata, propertyList);
                this.lastRequest = serverCommand.getHttpRequest();
                command = serverCommand;
                break;
            }
            case VALIDATE: {
                ValidateMessageCommand serverCommand = new ValidateMessageCommand();
                returnProperties = serverCommand.execute(metadata, propertyList);
                command = serverCommand;
                break;
            }
            case RESPOND: {
                RespondSoapCommand serverCommand = new RespondSoapCommand(this.lastRequest, this.defaultNSPrefix);
                returnProperties = serverCommand.execute(metadata, propertyList);
                command = serverCommand;
                this.lastRequest = null;
                break;
            }
            case STOP: {
                StopServerCommand serverCommand = new StopServerCommand(this.httpServer);
                serverCommand.execute(metadata, propertyList);
                this.lastRequest = null;
                this.httpServer = serverCommand.getHttpServer();
                command = serverCommand;
                break;
            }
            default:
                result.setErrorMessage("Unsupported WebServiceActionType for SoapServer: " + actionType);
                result.setActionStatus(ActionStatusType.FAILED);
                return result;
            }

            result.setReturnProperties(returnProperties);
            result.setActionStatus(ActionStatusType.EXECUTED);
            return result;
        } catch (InterruptionException ex) {
            throw ex;
        } catch (WebServiceException ex) {
            logger.error(ex);
            result.setErrorMessage("Could not execute SoapServer-action. Cause: " + ex.getMessage());
            result.setActionStatus(ActionStatusType.FAILED);
            return result;
        } catch (Exception ex) {
            logger.error(ex);
            result.setErrorMessage("Could not execute SoapServer-action. Cause: " + ex.toString());
            result.setActionStatus(ActionStatusType.FAILED);
            return result;
        } finally {

            if (context.isTracingEnabled() && command != null) {
                result.setActionTrace(command.getActionTrace());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {

        if (this.httpServer != null) {
            this.httpServer.stop();
            this.httpServer = null;
        }
    }

    /**
     * Initial validation of metadata and action type.
     * 
     * @param context
     *            the test context
     * @param propertyList
     *            the list of properties
     * @param metadataList
     *            the metadata list
     * @param actionType
     *            the action type
     * @throws WebServiceException
     *             thrown, if an error occurs
     */
    private void validateArguments(TestContext context, PropertyList propertyList, List<Metadata> metadataList,
            SubEngineActionType actionType) throws WebServiceException {

        if (context == null) {
            throw new WebServiceException("TestContext must not be null.");
        }

        if (propertyList == null) {
            throw new WebServiceException("PropertyList must not be null.");
        }

        if (metadataList == null || metadataList.isEmpty()) {
            throw new WebServiceException("MetadataList must not be null or empty.");
        }

        if (actionType == null) {
            throw new WebServiceException("ActionType must not be null.");
        }

        if (!(actionType instanceof WebServiceActionType)) {
            throw new WebServiceException("ActionType must be a WebServiceActionType.");
        }
    }

    /**
     * Returns the metadata for the WebService-call
     * 
     * @param metadataList
     *            the list of {@link Metadata}
     * 
     * @return the leaf element to work with
     */
    private Metadata getLeaf(List<Metadata> metadataList) {
        Metadata metadata = metadataList.get(metadataList.size() - 1);
        return metadata;
    }

}
