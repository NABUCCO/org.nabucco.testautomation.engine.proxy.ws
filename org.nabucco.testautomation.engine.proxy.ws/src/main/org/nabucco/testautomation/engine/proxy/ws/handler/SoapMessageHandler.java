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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPathExpressionException;

import org.nabucco.testautomation.engine.base.exception.NBCTestConfigurationException;
import org.nabucco.testautomation.engine.base.xml.XMLToolkit;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.xpath.XPathProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * SoapMessageHandler
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class SoapMessageHandler {

	public static final String NAMESPACE_1_1 = "http://schemas.xmlsoap.org/soap/envelope/";
	
	public static final String NAMESPACE_1_2 = "http://www.w3.org/2003/05/soap-envelope";
	
	private final String defaultNSPrefix;
	
	/**
     * Constructs a new SoapMessageHandler.
     * 
     * @param defaultNSPrefix
     *            the default namespace prefix
     */
	public SoapMessageHandler(String defaultNSPrefix) {
	    this.defaultNSPrefix = defaultNSPrefix;
	}
	
	public Document getSoapBody(String soapMessage) throws WebServiceException, XPathExpressionException, NBCTestConfigurationException {
		
		XPathProcessor processor = new XPathProcessor(this.defaultNSPrefix);
		processor.setDocument(soapMessage);
		
		Element rootElement = processor.getDocument().getDocumentElement();
		
		if (rootElement == null) {
			throw new WebServiceException("No root element found in message");
		}
		
		String ns = rootElement.getNamespaceURI();
		String prefix = rootElement.getPrefix();
		
		if (ns == null || (!ns.equals(NAMESPACE_1_1) && !ns.equals(NAMESPACE_1_2))) {
			throw new WebServiceException("Not a valid Soap-Message");
		}
		
		Element soapBody = processor.getFirstElement("//" + prefix + ":Body");
		
		if (soapBody != null) {
			Element e = XMLToolkit.getChildren(soapBody).get(0);
			Document doc = XMLToolkit.createDocument();
			Node importedNode = doc.importNode(e, true);
			doc.appendChild(importedNode);
			return doc;
		} else {
			throw new WebServiceException(prefix + ":Body not found in message");
		}
	}
	
	public SOAPMessage addToSoapMessage(Document message) throws SOAPException {
		
        MessageFactory mf = MessageFactory.newInstance(); //"SOAP 1.2 Protocol"
        SOAPMessage soapMessage = mf.createMessage();
        SOAPBody soapBody = soapMessage.getSOAPBody();
        soapBody.addDocument(message);
        return soapMessage;
	}
	
	public String serialize(SOAPMessage message) throws SOAPException, IOException {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		message.writeTo(out);
		return new String(out.toByteArray());
	}
}
