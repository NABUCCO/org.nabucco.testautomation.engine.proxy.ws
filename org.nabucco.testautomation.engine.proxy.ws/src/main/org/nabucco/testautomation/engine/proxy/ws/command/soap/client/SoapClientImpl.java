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
package org.nabucco.testautomation.engine.proxy.ws.command.soap.client;

import java.util.List;

import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.proxy.SubEngineActionType;
import org.nabucco.testautomation.engine.proxy.ws.WebServiceActionType;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.soap.SoapClient;
import org.nabucco.testautomation.engine.proxy.ws.soap.SoapCommand;

import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.result.facade.datatype.ActionResponse;
import org.nabucco.testautomation.result.facade.datatype.status.ActionStatusType;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * SoapClientImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class SoapClientImpl implements SoapClient {

    private static final long serialVersionUID = 1L;

    private static final NBCTestLogger logger = NBCTestLoggingFactory.getInstance().getLogger(
            SoapClientImpl.class);

    /**
     * Constructs a new instance calling a SOAP-WebService.
     */
    public SoapClientImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResponse execute(TestContext context, PropertyList propertyList,
            List<Metadata> metadataList, SubEngineActionType actionType) throws WebServiceException {

    	ActionResponse result = TestResultHelper.createActionResponse();
    	SoapCommand command = null;
    	
        try {
            result.setMessage("Executing WebService action='" + actionType + "'");

            // check request-arguments
            validateArguments(context, propertyList, metadataList, actionType);

            // get the metadata to be executed
            Metadata metadata = getLeaf(metadataList);

            switch ((WebServiceActionType) actionType) {
            case CALL:
                command = new SoapCallCommand();
                break;
            default:
                result.setErrorMessage("Unsupported WebServiceActionType for SoapCall: " + actionType);
                result.setActionStatus(ActionStatusType.FAILED);
                return result;
            }

            // Execute SoapCommand
        	PropertyList returnProperties = command.execute(metadata, propertyList);
        	
            result.setReturnProperties(returnProperties);
            result.setActionStatus(ActionStatusType.EXECUTED);
            return result;
        } catch (WebServiceException ex) {
            logger.error(ex);
            result.setErrorMessage("Could not execute SoapCall. Cause: " + ex.getMessage());
            result.setActionStatus(ActionStatusType.FAILED);
            return result;
        } catch (Exception ex) {
            logger.error(ex);
            result.setErrorMessage("Could not execute SoapCall. Cause: " + ex.toString());
            result.setActionStatus(ActionStatusType.FAILED);
            return result;
        } finally {
        	
        	if (context.isTracingEnabled() && command != null) {
        		result.setActionTrace(command.getActionTrace());
        	}
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
     * @throws WebServiceException thrown, if an error occurs
     */
    private void validateArguments(TestContext context, PropertyList propertyList,
            List<Metadata> metadataList, SubEngineActionType actionType) throws WebServiceException {

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
