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
package org.nabucco.testautomation.engine.proxy.ws.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.engine.proxy.ws.WsConstants;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.xpath.XPathProcessor;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.XPathProperty;
import org.nabucco.testautomation.property.facade.datatype.XmlProperty;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyContainer;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyType;
import org.w3c.dom.Document;

/**
 * XmlMessageHandler
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class XmlMessageHandler {

    private final XPathProcessor processor;

    private final List<XPathProperty> transformationXPath = new ArrayList<XPathProperty>();

    private final List<XPathProperty> extractionXPath = new ArrayList<XPathProperty>();
    
    /**
     * Constructs a new XmlMessageHandler instance initialized by the given XML-Message.
     * 
     * @param message
     *            the XML message to handle
     * @param defaultNSPrefix
     *            the default namespace prefix
     * @throws WebServiceException
     *             thrown, if an error occurs during this initialization
     */
    public XmlMessageHandler(String message, String defaultNSPrefix) throws WebServiceException {
    
    	if (message == null) {
    		throw new WebServiceException("Message must not be null");
    	}
    	
    	this.processor = new XPathProcessor(defaultNSPrefix);
    	
    	// Set the XML-Document into the XPathProcessor
        this.processor.setDocument(message);
    }
    
    /**
     * Constructs a new XmlMessageHandler instance initialized by the given XML-Message.
     * 
     * @param messageFile
     *            the XML message to handle
     * @param defaultNSPrefix
     *            the default namespace prefix
     * @throws WebServiceException
     *             thrown, if an error occurs during this initialization
     */
    public XmlMessageHandler(File messageFile, String defaultNSPrefix) throws WebServiceException {
    
    	if (messageFile == null || !messageFile.exists()) {
    		throw new WebServiceException("MessageFile must not be null");
    	}
    	
    	this.processor = new XPathProcessor(defaultNSPrefix);
    	
    	// Set the XML-Document into the XPathProcessor
        this.processor.setDocument(messageFile);
    }
    
    /**
     * Constructs a new XmlMessageHandler instance initialized by the given XML-Message.
     * 
     * @param message
     *            the XML message to handle
     * @param defaultNSPrefix
     *            the default namespace prefix
     * @throws WebServiceException
     *             thrown, if an error occurs during this initialization
     */
    public XmlMessageHandler(Document message, String defaultNSPrefix) throws WebServiceException {
    
    	if (message == null) {
    		throw new WebServiceException("Message must not be null");
    	}
    	
    	this.processor = new XPathProcessor(defaultNSPrefix);
    	
    	// Set the XML-Document into the XPathProcessor
        this.processor.setDocument(message);
    }
    
    public void setMessage(String message) throws WebServiceException {
    	this.processor.setDocument(message);
    }

    public void setMessage(File messageFile) throws WebServiceException {
    	this.processor.setDocument(messageFile);
    }
    
    public void setMessage(Document message) throws WebServiceException {
    	this.processor.setDocument(message);
    }
    
    public String getMessageAsString() throws WebServiceException {
    	return this.processor.getDocumentAsString();
    }
    
    public Document getMessage() throws WebServiceException {
    	return this.processor.getDocument();
    }

    /**
     * Processes the XML-message handled by this handler by evaluating the XPath-expressions read
     * from the metadata, and setting the values from the specified list into the message. For each
     * XPath-expression the list must contain a property with the same id. If no matching property
     * is found, null will be set.
     * 
     * @param requestValues
     *            the list of properties for the XPath-replacement
     * @return the processed XML-message
     * @throws WebServiceException
     *             thrown, if an error occurs during the process
     */
    public Document transform(PropertyList requestValues) throws WebServiceException {

        // Map holding the values for the xpath-targets
        Map<String, Property> requestValueMap = new HashMap<String, Property>();

        // put properties into Map
        for (PropertyContainer container : requestValues.getPropertyList()) {
        	Property requestValue = container.getProperty();
            addRequestValue(requestValue, requestValueMap, null);
        }

        // process each XPath-expression with the matching value from the properties
        for (XPathProperty xpath : this.transformationXPath) {
            processProperty(xpath, requestValueMap, null, null);
        }

        // return the processed XML-Document
        return processor.getDocument();
    }

    /**
     * Processes the given response message by extracting values using the specified
     * XPath-expression for the response. This operation returns the extracted values in a
     * {@link PropertyMap} with the id defined in {@link WsConstants.RESPONSE_CONTAINER_ID}.
     * 
     * @param responseDoc
     *            the response message to be processed
     * @return a PropertyList containing all values defined by the XPath-expressions
     * @throws WebServiceException
     *             thrown, if an error occurs during the process
     */
    public PropertyList extract() throws WebServiceException {

        // the PropertyMap for the response-properties
		PropertyList propertyList = PropertyHelper
				.createPropertyList(WsConstants.RESPONSE_CONTAINER_ID);
		
		// Add response message as RS to returnProperties
		XmlProperty responseProperty = PropertyHelper.createXmlProperty(
				WsConstants.MESSAGE_ID, processor.getDocumentAsString());
		PropertyHelper.add(responseProperty, propertyList);
        
        // process the xpath-expression and put the rs-values into the PropertyMap
        for (XPathProperty xpath : this.extractionXPath) {
            PropertyHelper.add(processProperty(xpath, null), propertyList);
        }
        return propertyList;
    }

    /**
     * Tries to get the PropertyMap with the id defined in {@link WsConstants.REQUEST_ID} from the
     * given metadata and extracts all XPath-properties from it.
     * 
     * @param metadata
     *            the metadata to get the XPath-expressions from
     * @throws WebServiceException
     *             thrown, if no PropertyMap with id "RQ" was found
     */
    public void setTransformationProperties(PropertyList propertyList) throws WebServiceException {

		if (propertyList != null) {
			for (PropertyContainer container : propertyList.getPropertyList()) {
				Property property = container.getProperty();
				
				if (property.getType() == PropertyType.XPATH) {
					transformationXPath.add((XPathProperty) property);
				}
			}
		}
    }

    /**
     * Tries to get the PropertyMap with the id defined in {@link WsConstants.RESPONSE_ID} from the
     * given metadata and extracts all XPath-properties from it.
     * 
     * @param metadata
     *            the metadata to get the XPath-expressions from
     */
    public void setExtractionProperties(PropertyList propertyList) {
		
		if (propertyList != null) {
			for (PropertyContainer container : propertyList.getPropertyList()) {
				Property property = container.getProperty();
				
				if (property.getType() == PropertyType.XPATH) {
					extractionXPath.add((XPathProperty) property);
				}
			}
		}
    }
    
    /**
     * Adds the value of a request property to the requestValueMap. If a parent id is given, this
     * operation concatenates the ids, for example : parentId.childId
     * 
     * @param requestValue
     *            the value to add
     * @param requestValueMap
     *            the map
     * @param parentId
     *            the id of the parent property
     */
    private void addRequestValue(Property requestValue, Map<String, Property> requestValueMap,
            String parentId) {

        String id = parentId != null ? parentId + WsConstants.ID_SEPARATOR + requestValue.getName().getValue()
                : requestValue.getName().getValue();

        if (requestValue.getType() == PropertyType.LIST) {
            for (PropertyContainer container : ((PropertyList) requestValue).getPropertyList()) {
                addRequestValue(container.getProperty(), requestValueMap, id);
            }
        } else {
            requestValueMap.put(id, requestValue);
        }
    }
    
    /**
     * Processes the request xml using the given {@link XPathProperty}.
     * 
     * @param xpathProp
     *            the property to process
     * @param requestValueMap
     *            the map with the request values
     * @param parentId
     *            the id of the parent property
     * @param parentXPath
     *            the xpath of parent property for relative xpath
     * @throws WebServiceException 
     */
    private void processProperty(XPathProperty xpathProp, Map<String, Property> requestValueMap,
            String parentId, String parentXPath) throws WebServiceException {

        String id = parentId != null ? parentId + WsConstants.ID_SEPARATOR + xpathProp.getName().getValue() : xpathProp
                .getName().getValue();
        Property value = requestValueMap.get(id);

        String xpath = parentXPath != null ? parentXPath + xpathProp.getValue().getValue() : xpathProp
                .getValue().getValue();

        if (!xpathProp.getPropertyList().isEmpty()) {
            for (PropertyContainer container : xpathProp.getPropertyList()) {
            	Property property = container.getProperty();
            	
                if (property.getType() == PropertyType.XPATH) {
                    processProperty((XPathProperty) property, requestValueMap, id, xpath);
                }
            }
        } else {
            processor.setValue(xpath, PropertyValueMapper.getValue(value));
        }
    }
    
    /**
     * Processes the response xml using the given {@link XPathProperty}.
     * 
     * @param prop
     *            the property to process
     * @param parentXPath
     *            the xpath of parent property for relative xpath
     * @return a new property containing the value determined by the given xpath
     */
    private Property processProperty(Property prop, String parentXPath) {

        XPathProperty xpathProp = (XPathProperty) prop;
        String xpath = parentXPath != null ? parentXPath + xpathProp.getValue().getValue() : xpathProp
                .getValue().getValue();

        if (!xpathProp.getPropertyList().isEmpty()) {
            PropertyList propertyList = PropertyHelper.createPropertyList(xpathProp.getName().getValue());
            
            for (PropertyContainer child : xpathProp.getPropertyList()) {
            	PropertyHelper.add(processProperty(child.getProperty(), xpath), propertyList);
            }
            return propertyList;
        } else {
            return processor.getValue(xpath, xpathProp.getName().getValue());
        }
    }

}
