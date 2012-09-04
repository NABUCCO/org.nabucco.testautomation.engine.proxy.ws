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

import java.io.IOException;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.engine.proxy.ws.WsConstants;
import org.nabucco.testautomation.engine.proxy.ws.command.soap.AbstractSoapCommand;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.handler.SoapMessageHandler;
import org.nabucco.testautomation.engine.proxy.ws.handler.XmlMessageHandler;
import org.nabucco.testautomation.engine.proxy.ws.server.http.HttpRequest;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.XmlProperty;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyType;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;
import org.w3c.dom.Document;

/**
 * RespondSoapCommand
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class RespondSoapCommand extends AbstractSoapCommand {

    private HttpRequest httpRequest;

    private final String defaultNSPrefix;

    /**
     * Creates a new ReceiveSoapCommand
     * 
     * @param httpServer
     *            the hhtp server to use
     * @param defaultNSPrefix
     *            the default namespace prefix
     * @throws WebServiceException
     *             thrown, if the server is not started
     */
    public RespondSoapCommand(HttpRequest httpRequest, String defaultNSPrefix) throws WebServiceException {

        if (httpRequest == null) {
            throw new WebServiceException("No HttpRequest to respond to");
        }
        this.httpRequest = httpRequest;
        this.defaultNSPrefix = defaultNSPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyList execute(Metadata metadata, PropertyList propertyList) throws WebServiceException {

        // Get Response-Message
        String response = getResponse(metadata, propertyList);

        // Get XPath for transformation
        PropertyList transformProps = (PropertyList) PropertyHelper.getFromList(metadata.getPropertyList(),
                PropertyType.LIST, WsConstants.RESPONSE_ID);

        // Initialize XmlMessageHandler
        XmlMessageHandler messageHandler = new XmlMessageHandler(response, this.defaultNSPrefix);
        messageHandler.setTransformationProperties(transformProps);

        // Transform ResponseMessage
        Document xml = messageHandler.transform(propertyList);

        try {
            SoapMessageHandler soapHandler = new SoapMessageHandler(this.defaultNSPrefix);
            SOAPMessage soapResponse = soapHandler.addToSoapMessage(xml);

            String serializedMessage = soapHandler.serialize(soapResponse);
            this.setResponse(serializedMessage);
            this.start();
            this.httpRequest.respond(serializedMessage);
            return null;
        } catch (IOException ex) {
            this.setException(ex);
            throw new WebServiceException(ex.getMessage());
        } catch (SOAPException ex) {
            this.setException(ex);
            throw new WebServiceException(ex.getMessage());
        } finally {
            this.stop();
        }
    }

    /**
     * 
     * @return
     */
    public HttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    /**
     * 
     * @param metadata
     * @param properties
     * @return
     * @throws WebServiceException
     */
    private String getResponse(Metadata metadata, PropertyList properties) throws WebServiceException {

        // First, check PropertyList from Action
        XmlProperty messageProperty = (XmlProperty) PropertyHelper.getFromList(properties, PropertyType.XML);

        if (messageProperty != null) {
            return messageProperty.getValue().getValue();
        }

        // Second, check PropertyList from Metadata
        messageProperty = (XmlProperty) PropertyHelper.getFromList(metadata.getPropertyList(), PropertyType.XML);

        if (messageProperty != null) {
            return messageProperty.getValue().getValue();
        }

        throw new WebServiceException("No Response-Message found");
    }

}
