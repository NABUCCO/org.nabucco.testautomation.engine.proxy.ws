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
package org.nabucco.testautomation.engine.proxy.ws.command.soap.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.soap.SOAPException;

import org.nabucco.testautomation.engine.proxy.ws.WsConstants;
import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceClient;
import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceListener;
import org.nabucco.testautomation.engine.proxy.ws.command.soap.AbstractSoapCommand;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.handler.XmlMessageHandler;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.TextProperty;
import org.nabucco.testautomation.property.facade.datatype.XmlProperty;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyContainer;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyType;
import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;
import org.w3c.dom.Document;

/**
 * SoapCallCommand
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class SoapCallCommand extends AbstractSoapCommand implements WebServiceListener {

    private WebServiceClient client;

    private String defaultNSPrefix;

    /**
     * Creates a new command for the given {@link WebServiceClient}.
     * 
     * @param client
     *            the client to use
     * @param defaultNSPrefix
     *            the default namespace prefix
     */
    public SoapCallCommand(WebServiceClient client, String defaultNSPrefix) {
        this.client = client;
        this.defaultNSPrefix = defaultNSPrefix;
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

        // get the XML-Message to send
        List<String> messages = getXmlMessages(metadata, propertyList);

        // get the WebService-Method
        String method = getMethod(metadata, propertyList);

        // Get XPath for transformation
        PropertyList transformProps = (PropertyList) PropertyHelper.getFromList(metadata.getPropertyList(),
                PropertyType.LIST, WsConstants.REQUEST_ID);

        // Get XPath for extraction
        PropertyList extractProps = (PropertyList) PropertyHelper.getFromList(metadata.getPropertyList(),
                PropertyType.LIST, WsConstants.RESPONSE_ID);

        // Initialize XmlMessageHandler
        XmlMessageHandler messageHandler = null;
        List<Document> xmlList = new ArrayList<Document>();

        for (String message : messages) {
            messageHandler = new XmlMessageHandler(message, this.defaultNSPrefix);
            messageHandler.setTransformationProperties(transformProps);
            Document xml = messageHandler.transform(propertyList);
            xmlList.add(xml);
        }

        try {
            this.client.addWebServiceListener(this);
            Document rs;

            if (method != null) {
                rs = this.client.sendMessage(url, method, xmlList);
            } else {
                rs = this.client.sendMessage(url, xmlList);
            }
            this.client.removeWebServiceListener(this);
            messageHandler.setExtractionProperties(extractProps);
            messageHandler.setMessage(rs);
            PropertyList responseProperties = messageHandler.extract();
            return responseProperties;
        } catch (SOAPException ex) {
            setException(ex);
            throw new WebServiceException(ex);
        }
    }

    /**
     * Gets the target URL specified in the given Metadata object.
     * 
     * @param metadata
     *            the metadata
     * @return the URL specified by the given metadata
     * @throws WebServiceException
     *             thrown, if an error occurs
     */
    private URL getURL(Metadata metadata, PropertyList propertyList) throws WebServiceException {

        if (propertyList != null) {

            // First, check PropertyList from Action
            TextProperty urlProperty = (TextProperty) PropertyHelper.getFromList(propertyList, PropertyType.TEXT,
                    WsConstants.URL);

            if (urlProperty != null && urlProperty.getValue() != null) {
                try {
                    return new URL(urlProperty.getValue().getValue());
                } catch (MalformedURLException e) {
                    throw new WebServiceException("Invalid URL: " + e.toString());
                }
            }
        }

        // Second, check PropertyList of Metadata
        for (PropertyContainer container : metadata.getPropertyList().getPropertyList()) {
            Property property = container.getProperty();

            if (property.getName().getValue().equalsIgnoreCase(WsConstants.URL)) {
                try {
                    return new URL(PropertyHelper.toString(property));
                } catch (MalformedURLException e) {
                    throw new WebServiceException("Invalid URL: " + e.toString());
                }
            } else if (property.getName().getValue().equalsIgnoreCase(WsConstants.REQUEST_ID)
                    && property.getType() == PropertyType.LIST) {
                for (PropertyContainer container2 : ((PropertyList) property).getPropertyList()) {
                    Property prop = container2.getProperty();

                    if (prop.getName().getValue().equalsIgnoreCase(WsConstants.URL)) {
                        try {
                            return new URL(PropertyHelper.toString(prop));
                        } catch (MalformedURLException e) {
                            throw new WebServiceException("Invalid URL: " + e.toString());
                        }
                    }
                }
            }
        }
        throw new WebServiceException("URL not defined ");
    }

    /**
     * Tries to find the XML-Message for the request within the properties of the given metadata. If
     * the message is not found, an exception is thrown.
     * 
     * @param metadata
     *            the metadata to get the XML-message from
     * @throws WebServiceException
     *             thrown, if no message was found
     */
    private List<String> getXmlMessages(Metadata metadata, PropertyList propertyList) throws WebServiceException {

        List<String> messages = new ArrayList<String>();

        // First, check PropertyList from Action
        if (propertyList != null) {

            for (PropertyContainer container : propertyList.getPropertyList()) {
                Property property = container.getProperty();

                if (property.getType() == PropertyType.XML) {
                    messages.add(((XmlProperty) property).getValue().getValue());
                }
            }
        }

        // Second, check PropertyList of Metadata
        PropertyList metadataProperties = metadata.getPropertyList();

        if (metadataProperties != null) {

            for (PropertyContainer container : metadataProperties.getPropertyList()) {
                Property property = container.getProperty();

                if (property.getType() == PropertyType.XML) {
                    messages.add(((XmlProperty) property).getValue().getValue());
                }
            }
        }

        if (messages.isEmpty()) {
            throw new WebServiceException("No XML-Message defined");
        }

        return messages;
    }

    private String getMethod(Metadata metadata, PropertyList propertyList) throws WebServiceException {

        if (propertyList != null) {

            // First, check PropertyList from Action
            Property methodProperty = PropertyHelper.getFromList(propertyList, "METHOD");

            if (methodProperty != null) {
                return PropertyHelper.toString(methodProperty);
            }
        }

        // Second, check PropertyList of Metadata
        Property methodProperty = PropertyHelper.getFromList(metadata.getPropertyList(), "METHOD");

        if (methodProperty != null) {
            return PropertyHelper.toString(methodProperty);
        }

        return null;
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
