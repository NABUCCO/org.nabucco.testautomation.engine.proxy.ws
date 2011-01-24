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
package org.nabucco.testautomation.engine.proxy.ws.client.soap;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceClient;
import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceListener;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * SoapClientImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class SoapClientImpl implements WebServiceClient {

	private final List<WebServiceListener> listeners = new ArrayList<WebServiceListener>();

	private SOAPMessage soapRequest;

    /**
     * Constructs a new SOAP based instance of a {@link WebServiceClient}
     * 
     * @throws WebServiceException thrown, if an error occurs
     */
    public SoapClientImpl() throws WebServiceException {
        try {
            MessageFactory mf = MessageFactory.newInstance(); //"SOAP 1.2 Protocol"
            soapRequest = mf.createMessage();
        } catch (SOAPException ex) {
            throw new WebServiceException("Could not create SOAPMessage", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addHeaderAttribute(String name, String value) throws WebServiceException {
        try {
            soapRequest.getSOAPHeader().addAttribute(SOAPFactory.newInstance().createName(name),
                    value);
        } catch (SOAPException ex) {
            throw new WebServiceException("Could not add header attribute " + name + "=" + value,
                    ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHeaderElement(String name, String value) throws WebServiceException {
        try {
            soapRequest.getSOAPHeader()
                    .addHeaderElement(SOAPFactory.newInstance().createName(name)).setValue(value);
        } catch (SOAPException ex) {
            throw new WebServiceException("Could not add header element " + name + "=" + value, ex);
        }
    }

    /**
     * {@inheritDoc}
     * @throws SOAPException 
     * @throws WebServiceException 
     */
    @Override
    public Document sendMessage(URL url, Document message) throws SOAPException, WebServiceException {

        // set message as payload
        SOAPBody soapBody = soapRequest.getSOAPBody();
        soapBody.addDocument(message);

        // create connection and send soap message
        messageSent(soapRequest);
        start();
        SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
        SOAPMessage soapResponse = soapConnection.call(soapRequest, url);
        soapConnection.close();
        finished();
        messagereceived(soapResponse);
        
        soapBody = soapResponse.getSOAPBody();
        Document responseMessage = getDocument(soapBody);
        return responseMessage;
    }

    /**
     * Gets the payload from the given SOAPBody.
     * 
     * @param soapBody
     *            the SOAPBody to get the payload from
     * @return the payload
     */
    private Document getDocument(SOAPBody soapBody) throws WebServiceException {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = null;
            NodeList childNodes = soapBody.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);

                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    root = (Element) child;
                    break;
                }
            }

            if (root != null) {
                doc.appendChild(doc.importNode(root, true));
            }
            return doc;
        } catch (ParserConfigurationException ex) {
            throw new WebServiceException("Could not read response document from SOAPBody", ex);
        }
    }
    
    private void messageSent(SOAPMessage request) {
    	
    	for (WebServiceListener listener : this.listeners) {
    		listener.messageSent(request);
    	}
    }
    
    private void messagereceived(SOAPMessage request) {
    	
    	for (WebServiceListener listener : this.listeners) {
    		listener.messagereceived(request);
    	}
    }
    
    private void start() {
    	
    	for (WebServiceListener listener : this.listeners) {
    		listener.start();
    	}
    }
    
    private void finished() {
    	
    	for (WebServiceListener listener : this.listeners) {
    		listener.finished();
    	}
    }

	@Override
	public void addWebServiceListener(WebServiceListener listener) {
		
		if (listener != null) {
			this.listeners.add(listener);
		}
	}

	@Override
	public void removeWebServiceListener(WebServiceListener listener) {

		if (listener != null) {
			this.listeners.remove(listener);
		}		
	}

}
