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
package org.nabucco.testautomation.engine.proxy.ws.command.rest.client;

import java.net.URL;
import java.util.List;

import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceClient;
import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceListener;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.handler.XmlMessageHandler;
import org.nabucco.testautomation.engine.proxy.ws.json.JsonMapper;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * GetCommand
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class PutCommand extends AbstractRestCommand implements WebServiceListener {

    private static final String COMMAND = "PUT";

    /**
     * Creates a new command for the given {@link WebServiceClient}.
     * 
     * @param client
     *            the client to use
     */
    public PutCommand(WebServiceClient client, String defaultNSPrefix) {
        super(client, defaultNSPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyList execute(Metadata metadata, PropertyList propertyList) throws WebServiceException {

        if (metadata.getPropertyList() == null) {
            throw new WebServiceException("Metadata '" + metadata.getName() + "' has no PropertyList defined !");
        }

        // get the target URL
        URL url = getURL(metadata, propertyList);

        // get the Message to send
        String message = getMessage(metadata, propertyList);

        // Get username and password
        String username = getUsername(metadata, propertyList);
        String password = getPassword(metadata, propertyList);

        // get http header attributes
        List<Property> headerList = getHeaderAttributes(metadata, propertyList);
        
        if (headerList != null) {
            for (Property property : headerList) {
                this.client.addHeaderAttribute(property.getName().getValue(), PropertyHelper.toString(property));
            }
        }
        
        try {
            this.client.addWebServiceListener(this);
            this.client.setUsername(username);
            this.client.setPassword(password);
            String response = this.client.sendMessage(url, COMMAND, message);
            this.client.removeWebServiceListener(this);

            // Handle response
            PropertyList responseProperties = null;

            if (isXml()) {
                XmlMessageHandler handler = new XmlMessageHandler(response, this.defaultNSPrefix);
                handler.setExtractionProperties(getExtractionProperties(metadata));
                responseProperties = handler.extract();
            } else if (isJson()) {
                responseProperties = JsonMapper.mapFromString(response);
            }
            return responseProperties;
        } catch (WebServiceException ex) {
            setException(ex);
            throw ex;
        } catch (Exception ex) {
            setException(ex);
            throw new WebServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        super.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finished() {
        stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageSent(String request) {
        this.setRequest(request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(String response) {
        this.setResponse(response);
    }

}
