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
package org.nabucco.testautomation.engine.proxy.ws.xpath;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.nabucco.testautomation.engine.base.exception.NBCTestConfigurationException;
import org.nabucco.testautomation.engine.base.xml.XMLToolkit;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XPathProcessor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class XPathProcessor {

    private final String defaultNSPrefix;

    private static final String COUNT = "count(";

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    private Document document;

    private GenericNamespaceContext context;
    
    public XPathProcessor(String defaultNSPrefix) {
        this.defaultNSPrefix = defaultNSPrefix;
    }

    /**
     * Set the XML-message to the processor.
     * 
     * @param xml
     *            the XML message as string
     * @throws WebServiceException
     *             thrown, if the XML message cannot be parsed
     */
    public void setDocument(String xml) throws WebServiceException {
        try {
            document = XMLToolkit.loadXMLDocument(xml);
            context = new GenericNamespaceContext();
            addNamespaceToContext(document.getFirstChild(), context);
        } catch (NBCTestConfigurationException ex) {
            throw new WebServiceException("XML-Message invalid: could not parse input string to org.w3c.Document ("
                    + ex.getMessage() + ")", ex);
        }
    }

    /**
     * Set the XML-message to the processor.
     * 
     * @param xmlFile
     *            the XML-file
     * @throws WebServiceException
     *             thrown, if the XML message cannot be loaded or parsed
     */
    public void setDocument(File xmlFile) throws WebServiceException {
        try {
            document = XMLToolkit.loadXMLDocument(xmlFile);
            context = new GenericNamespaceContext();
            addNamespaceToContext(document.getFirstChild(), context);
        } catch (NBCTestConfigurationException ex) {
            throw new WebServiceException("Could not load input file " + xmlFile.getName(), ex);
        }
    }

    /**
     * Sets the given document to the processor.
     * 
     * @param document
     *            the XML-document
     */
    public void setDocument(Document document) {
        this.document = document;
        context = new GenericNamespaceContext();
        addNamespaceToContext(document.getFirstChild(), context);
    }

    /**
     * Gets the XML-document set to the processor.
     * 
     * @return the document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Gets the XML-document as string.
     * 
     * @return the XML-string * @throws WebServiceException thrown, if the XML-message cannot be
     *         parsed
     */
    public String getDocumentAsString() throws WebServiceException {
        try {
            StringWriter sw = new StringWriter();
            XMLToolkit.writeXMLDocument(document, sw);
            return sw.toString();
        } catch (NBCTestConfigurationException ex) {
            throw new WebServiceException("Could not parse org.w3c.dom.Document to String", ex);
        }
    }

    /**
     * Sets the given value to the element defined by the given XPath-expression.
     * 
     * @param xpathExpression
     *            the XPath-expression defining the target element or attribute
     * @param value
     *            the value to be set
     * @throws WebServiceException
     *             thrown, if the XPath-expression cannot be processed
     */
    public void setValue(String xpathExpression, String value) throws WebServiceException {
        try {
            xpath.setNamespaceContext(context);
            Node result = (Node) xpath.evaluate(xpathExpression, document, XPathConstants.NODE);

            if (result != null) {
                result.setTextContent(value);
            }
        } catch (XPathExpressionException ex) {
            throw new WebServiceException("Could not process XPath-expression: " + xpathExpression, ex);
        }
    }

    /**
     * Gets the value from an element or attribute specified by the given XPath-expression as
     * string.
     * 
     * @param xpathExpression
     *            the XPath-expression
     * @return the string value from the document set to this XPathProcessor
     */
    public String getSimpleValue(String xpathExpression) {
        try {
            xpath.setNamespaceContext(context);
            return xpath.evaluate(xpathExpression, document);
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the value from an element or attribute specified by the given XPath-expression as either
     * a PropertyList in case of a complex value, or a TextProperty in case of a simple value.
     * 
     * @param xpathExpression
     *            the XPath-expression
     * @return a PropertyList or TextProperty
     */
    public Property getValue(String xpathExpression, String id) {
        try {
            xpath.setNamespaceContext(context);
            NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);
            return getPropertyList(nodeList, id);
        } catch (XPathExpressionException ex) {
            String value = getSimpleValue(xpathExpression);
            return PropertyHelper.createTextProperty(id, value);
        }
    }

    /**
     * the given xpathExpression into a cound function in order to count the occurrence of the
     * element specified by the given xpath.
     * 
     * @param xpathExpression
     *            the xpath
     * @return the number of occurrence
     */
    public int count(String xpathExpression) {

        if (!xpathExpression.startsWith(COUNT)) {
            xpathExpression = COUNT + xpathExpression + ")";
        }
        try {
            xpath.setNamespaceContext(context);
            String count = xpath.evaluate(xpathExpression, document);
            if (count != null && !count.equals("")) {
                return Integer.parseInt(count);
            }
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 
     * @param xpathExpression
     * @return
     * @throws XPathExpressionException
     */
    public Element getFirstElement(String xpathExpression) throws XPathExpressionException {

        xpath.setNamespaceContext(context);
        NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);

        if (nodeList != null) {

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    return (Element) node;
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param xpathExpression
     * @return
     * @throws XPathExpressionException
     */
    public List<Element> getElements(String xpathExpression) throws XPathExpressionException {

        xpath.setNamespaceContext(context);
        NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression, document, XPathConstants.NODESET);
        List<Element> elementList = new ArrayList<Element>();

        if (nodeList != null) {

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    elementList.add((Element) node);
                }
            }
        }
        return elementList;
    }

    /**
     * This operation iterates through this node and all of its subnodes in order to find all
     * defined namespaces and adds them to the given namespace context.
     * 
     * @param node
     *            the node to start the search from
     * @param context
     *            the namespace context
     */
    private void addNamespaceToContext(Node node, GenericNamespaceContext context) {

        if (node == null) {
            return;
        }
        String uri = node.getNamespaceURI();
        String prefix = node.getPrefix();

        if (uri != null) {

            if (prefix == null) {
                prefix = this.defaultNSPrefix;
            }
            context.addNamespace(uri, prefix);
        }

        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                addNamespaceToContext(child, context);
            }
        }
    }

    /**
     * Maps the given {@link NodeList} into a PropertyList, or into a TextProperty, if the NodeList
     * contains only one element with no children.
     * 
     * @param nodeList
     *            the NodeList
     * @param id
     *            the id of the returned property
     * @return a PropertyList or TextProperty with the given id
     */
    private Property getPropertyList(NodeList nodeList, String id) {

        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        } else if (nodeList.getLength() == 1) {
            Node node = nodeList.item(0);

            if (node.getNodeType() == Node.ELEMENT_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE) {

                if (hasChildren(node)) {
                    NodeList children = node.getChildNodes();
                    PropertyList propertyList = PropertyHelper.createPropertyList(id);
                    PropertyHelper.add(getPropertyList(children, id), propertyList);
                    return propertyList;
                } else {
                    return PropertyHelper.createTextProperty(id, node.getTextContent());
                }
            }
            return null;
        } else {
            PropertyList propertyList = PropertyHelper.createPropertyList(id);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE) {

                    if (hasChildren(node)) {
                        NodeList children = node.getChildNodes();
                        PropertyHelper.add(getPropertyList(children, node.getNodeName()), propertyList);
                    } else {
                        PropertyHelper.add(
                                PropertyHelper.createTextProperty(node.getNodeName(), node.getTextContent()),
                                propertyList);
                    }
                }
            }
            return propertyList;
        }
    }

    /**
     * Determines, whether the given {@link Node} has child elements or not. Only ElementNodes or
     * Attributes node are considered to be child elements.
     * 
     * @param node
     *            the node to check
     * @return true, if the node has child element, otherwise false
     */
    private boolean hasChildren(Node node) {

        if (node == null || !node.hasChildNodes()) {
            return false;
        }

        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE || child.getNodeType() == Node.ATTRIBUTE_NODE) {
                return true;
            }
        }
        return false;
    }

}
