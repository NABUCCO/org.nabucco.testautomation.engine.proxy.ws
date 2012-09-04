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

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.engine.base.exception.NBCTestConfigurationException;
import org.nabucco.testautomation.engine.base.xml.XMLToolkit;
import org.nabucco.testautomation.engine.proxy.base.AbstractProxyCommand;
import org.nabucco.testautomation.engine.proxy.ws.WsConstants;
import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceClient;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.handler.XmlMessageHandler;
import org.nabucco.testautomation.engine.proxy.ws.json.JsonMapper;
import org.nabucco.testautomation.engine.proxy.ws.rest.RestCommand;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.TextProperty;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyContainer;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyType;
import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;
import org.w3c.dom.Document;

/**
 * AbstractRestCommand
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public abstract class AbstractRestCommand extends AbstractProxyCommand implements RestCommand {

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(RestCommand.class);
    
    private static final String HEADER = "HEADER";

    protected String defaultNSPrefix;
    
    protected WebServiceClient client;

    private boolean xml = false;
    
    private boolean json = false;

    AbstractRestCommand(WebServiceClient client, String defaultNSPrefix) {
        this.client = client;
        this.defaultNSPrefix = defaultNSPrefix;
    }

    @Override
    protected void info(String msg) {
        logger.info(msg);
    }

    @Override
    protected void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    protected void error(String msg) {
        logger.error(msg);
    }

    @Override
    protected void warning(String msg) {
        logger.warning(msg);
    }
    
    protected boolean isXml() {
        return this.xml;
    }

    protected boolean isJson() {
        return this.json;
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
    protected URL getURL(Metadata metadata, PropertyList propertyList) throws WebServiceException {

        // Check PropertyList of Metadata and Action
        Property urlProperty = PropertyHelper.getFromList(metadata.getPropertyList(), WsConstants.URL);
        PropertyList urlList = (PropertyList) PropertyHelper.getFromList(propertyList, PropertyType.LIST,
                WsConstants.URL);

        if (urlProperty == null) {
            throw new WebServiceException("URL not defined");
        }

        String url = PropertyHelper.toString(urlProperty);

        if (urlList != null && !urlList.getPropertyList().isEmpty()) {

            for (PropertyContainer container : urlList.getPropertyList()) {
                Property prop = container.getProperty();
                String varName = prop.getName().getValue();
                String varValue = PropertyHelper.toString(prop);
                url = url.replaceAll("\\$\\{" + varName + "\\}", varValue);
            }
        }

        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new WebServiceException("Could not parse URL", e);
        }
    }

    /**
     * Gets the username specified in the given Metadata object or PropertyList.
     * 
     * @param metadata
     *            the Metadata
     * @param propertyList
     *            the PropertyList
     * @return the username or null, if not provided
     * @throws WebServiceException
     *             thrown, if an error occurs
     */
    protected String getUsername(Metadata metadata, PropertyList propertyList) throws WebServiceException {
        
        // Check PropertyList of Action and Metadata
        TextProperty usernameProperty = (TextProperty) PropertyHelper.getFromList(propertyList, PropertyType.TEXT, WsConstants.USERNAME);
        
        if (usernameProperty != null && usernameProperty.getValue() != null) {
            return usernameProperty.getValue().getValue();
        }
        
        usernameProperty = (TextProperty) PropertyHelper.getFromList(metadata.getPropertyList(), PropertyType.TEXT, WsConstants.USERNAME);
        
        if (usernameProperty != null && usernameProperty.getValue() != null) {
            return usernameProperty.getValue().getValue();
        }
        
        // No username provided
        return null;
    }
    
    /**
     * Gets the password specified in the given Metadata object or PropertyList.
     * 
     * @param metadata
     *            the Metadata
     * @param propertyList
     *            the PropertyList
     * @return the password or null, if not provided
     * @throws WebServiceException
     *             thrown, if an error occurs
     */
    protected String getPassword(Metadata metadata, PropertyList propertyList) throws WebServiceException {
        
        // Check PropertyList of Action and Metadata
        TextProperty passwordProperty = (TextProperty) PropertyHelper.getFromList(propertyList, PropertyType.TEXT, WsConstants.PASSWORD);
        
        if (passwordProperty != null && passwordProperty.getValue() != null) {
            return passwordProperty.getValue().getValue();
        }
        
        passwordProperty = (TextProperty) PropertyHelper.getFromList(metadata.getPropertyList(), PropertyType.TEXT, WsConstants.PASSWORD);
        
        if (passwordProperty != null && passwordProperty.getValue() != null) {
            return passwordProperty.getValue().getValue();
        }
        
        // No username provided
        return null;
    }

    /**
     * Tries to find the Message for the request within the properties of the given metadata. If the
     * message is not found, null is returned.
     * 
     * @param metadata
     *            the metadata to get the message from
     * @throws WebServiceException
     *             should not be thrown
     */
    protected String getMessage(Metadata metadata, PropertyList propertyList) throws WebServiceException {

        // First, check PropertyList from Action
        if (propertyList != null) {

            Property messageProperty = PropertyHelper.getFromList(propertyList, PropertyType.XML);
            
            if (messageProperty == null) {
                messageProperty = PropertyHelper.getFromList(metadata.getPropertyList(), PropertyType.XML);
            }

            if (messageProperty != null) {
                xml = true;
                String message = PropertyHelper.toString(messageProperty);

                // Get XPath for transformation
                PropertyList transformProps = (PropertyList) PropertyHelper.getFromList(metadata.getPropertyList(),
                        PropertyType.LIST, WsConstants.REQUEST_ID);

                if (transformProps != null) {
                    XmlMessageHandler messageHandler = new XmlMessageHandler(message, this.defaultNSPrefix);
                    messageHandler.setTransformationProperties(transformProps);
                    Document xml = messageHandler.transform(propertyList);
                    StringWriter writer = new StringWriter();
                    
                    try {
                        XMLToolkit.writeXMLDocument(xml, writer);
                    } catch (NBCTestConfigurationException e) {
                        throw new WebServiceException("Could not parse XML-Message", e);
                    }
                    return writer.toString();
                } else {
                    return message;
                }
            }

            messageProperty = PropertyHelper.getFromList(propertyList, PropertyType.LIST, JSON);

            if (messageProperty != null) {
                json = true;
                return JsonMapper.mapToString((PropertyList) messageProperty);
            }
        }

        return null;
    }
    
    /**
     * Gets the custom http header attributes specified in the given Metadata object or PropertyList.
     * 
     * @param metadata
     *            the Metadata
     * @param propertyList
     *            the PropertyList
     * @return the http header attributes or null, if not provided
     * @throws WebServiceException
     *             thrown, if an error occurs
     */
    protected List<Property> getHeaderAttributes(Metadata metadata, PropertyList propertyList) throws WebServiceException {
        
        // Check PropertyList of Action and Metadata
        PropertyList headerList = (PropertyList) PropertyHelper.getFromList(propertyList, PropertyType.LIST, HEADER);
        
        if (headerList != null) {
            return PropertyHelper.extract(headerList.getPropertyList());
        }
        
        headerList = (PropertyList) PropertyHelper.getFromList(metadata.getPropertyList(), PropertyType.LIST, HEADER);
        
        if (headerList != null) {
            return PropertyHelper.extract(headerList.getPropertyList());
        }
        
        // No header params provided
        return null;
    }
    
    protected PropertyList getExtractionProperties(Metadata metadata) {
        // Get XPath for transformation
        PropertyList extractProps = (PropertyList) PropertyHelper.getFromList(metadata.getPropertyList(),
                PropertyType.LIST, WsConstants.RESPONSE_ID);
        return extractProps;
    }

}
