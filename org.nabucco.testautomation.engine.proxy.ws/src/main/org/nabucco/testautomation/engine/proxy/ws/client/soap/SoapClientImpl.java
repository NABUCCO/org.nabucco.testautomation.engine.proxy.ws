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
package org.nabucco.testautomation.engine.proxy.ws.client.soap;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.client.Call;
import org.apache.axis.soap.SOAP11Constants;
import org.apache.axis.soap.SOAP12Constants;
import org.apache.axis.soap.SOAPConstants;
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

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String SOAP_ACTION = "SOAPAction";

    private final List<WebServiceListener> listeners = new ArrayList<WebServiceListener>();

    private final SoapVersion version;

    private Map<String, String> headerAttributes = new HashMap<String, String>();

    private Map<String, String> headerElements = new HashMap<String, String>();
    
    private String username;
    
    private String password;

    /**
     * Constructs a new SOAP based instance of a {@link WebServiceClient}
     * 
     * @throws WebServiceException
     *             thrown, if an error occurs
     */
    public SoapClientImpl(SoapVersion version) throws WebServiceException {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHeaderAttribute(String name, String value) throws WebServiceException {
        this.headerAttributes.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHeaderElement(String name, String value) throws WebServiceException {
        this.headerElements.put(name, value);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SOAPException
     * @throws WebServiceException
     */
    @Override
    public Document sendMessage(URL url, Document message) throws SOAPException, WebServiceException {

        SOAPMessage soapRequest = createSoapMessage(url, null);

        // set message as payload
        SOAPBody soapBody = soapRequest.getSOAPBody();
        soapBody.addDocument(message);

        // create connection and send soap message
        messageSent(soapRequest);
        start();
        SOAPMessage soapResponse = call(url, null, soapRequest);
        finished();
        messageReceived(soapResponse);

        soapBody = soapResponse.getSOAPBody();
        Document responseMessage = getDocument(soapBody);
        return responseMessage;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SOAPException
     * @throws WebServiceException
     */
    @Override
    public Document sendMessage(URL url, String method, Document message) throws SOAPException, WebServiceException {

        SOAPMessage soapRequest = createSoapMessage(url, method);

        // set message as payload
        SOAPBody soapBody = soapRequest.getSOAPBody();
        soapBody.addDocument(message);

        // create connection and send soap message
        messageSent(soapRequest);
        start();
        SOAPMessage soapResponse = call(url, method, soapRequest);
        finished();
        messageReceived(soapResponse);

        soapBody = soapResponse.getSOAPBody();
        Document responseMessage = getDocument(soapBody);
        return responseMessage;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SOAPException
     * @throws WebServiceException
     */
    @Override
    public Document sendMessage(URL url, List<Document> messages) throws SOAPException, WebServiceException {

        SOAPMessage soapRequest = createSoapMessage(url, null);
        SOAPBody soapBody = soapRequest.getSOAPBody();

        // add messages to soap body
        for (Document document : messages) {
            soapBody.addDocument(document);
        }

        // create connection and send soap message
        messageSent(soapRequest);
        start();
        SOAPMessage soapResponse = call(url, null, soapRequest);
        finished();
        messageReceived(soapResponse);

        soapBody = soapResponse.getSOAPBody();
        Document responseMessage = getDocument(soapBody);
        return responseMessage;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SOAPException
     * @throws WebServiceException
     */
    @Override
    public Document sendMessage(URL url, String method, List<Document> messages) throws SOAPException,
            WebServiceException {

        SOAPMessage soapRequest = createSoapMessage(url, method);
        SOAPBody soapBody = soapRequest.getSOAPBody();

        // add messages to soap body
        for (Document document : messages) {
            soapBody.addDocument(document);
        }

        // create connection and send soap message
        messageSent(soapRequest);
        start();
        SOAPMessage soapResponse = call(url, method, soapRequest);
        finished();
        messageReceived(soapResponse);

        soapBody = soapResponse.getSOAPBody();
        Document responseMessage = getDocument(soapBody);
        return responseMessage;
    }

    /**
     * @param url
     * @param method
     * @param soapRequest
     * @return
     */
    private SOAPMessage call(URL url, String method, SOAPMessage soapRequest) {
        SOAPMessage soapResponse = null;

        try {
            Call call = new Call(url);
            ((org.apache.axis.Message) soapRequest).setMessageContext(call.getMessageContext());
            String soapActionURI = checkForSOAPActionHeader(soapRequest);

            if (soapActionURI != null) {
                call.setSOAPActionURI(soapActionURI);
            }

            call.setProperty(Call.CHECK_MUST_UNDERSTAND, Boolean.FALSE);
            call.setProperty(Call.CHARACTER_SET_ENCODING, "UTF-8");
            call.setReturnClass(SOAPMessage.class);
            call.setTimeout(10000);
            call.setUsername(this.username);
            call.setPassword(this.password);

            if (method != null) {
                call.setOperation(method);
            }

            call.invoke((org.apache.axis.Message) soapRequest);
            soapResponse = call.getResponseMessage();
        } catch (org.apache.axis.AxisFault af) {
            soapResponse = new org.apache.axis.Message(af);
        }
        return soapResponse;
    }

    /**
     * Creates an empty {@link SoapMessage}.
     * 
     * @return the empty SOAP message
     * @throws WebServiceException
     *             thrown, if the message could not be created
     */
    private SOAPMessage createSoapMessage(URL url, String method) throws WebServiceException {

        try {
            SOAPConstants soapConstants;
            SOAPMessage soapMessage;

            switch (version) {
            case V_1_1:
                soapConstants = new SOAP11Constants();
                break;
            case V_1_2:
                soapConstants = new SOAP12Constants();
                break;
            default:
                throw new WebServiceException("Invalid SoapVersion defined: " + version);
            }

            // Create SOAP 1.1 or SOAP 1.2 message
            soapMessage = SoapMessageFactory.getInstance().createMessage(soapConstants);

            // Manipulate HTTP header attributes
            MimeHeaders hd = soapMessage.getMimeHeaders();
            String contentType = soapConstants.getContentType();
            contentType += ";charset=UTF-8";

            if (method != null) {
                hd.addHeader(SOAP_ACTION, url + "/" + method);
                contentType += ";action=" + method;
            }

            hd.setHeader(CONTENT_TYPE, contentType);

            // Add SOAP-Header attributes and elements
            for (String name : this.headerAttributes.keySet()) {
                soapMessage.getSOAPHeader().addAttribute(SOAPFactory.newInstance().createName(name),
                        this.headerAttributes.get(name));
            }

            for (String name : this.headerElements.keySet()) {
                soapMessage.getSOAPHeader().addHeaderElement(SOAPFactory.newInstance().createName(name))
                        .setValue(this.headerElements.get(name));
            }

            return soapMessage;
        } catch (SOAPException ex) {
            throw new WebServiceException("Could not create SOAPMessage", ex);
        }
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

        String trace = trace(request);
        
        for (WebServiceListener listener : this.listeners) {
            listener.messageSent(trace);
        }
    }

    private void messageReceived(SOAPMessage response) {

        String trace = trace(response);
        
        for (WebServiceListener listener : this.listeners) {
            listener.messageReceived(trace);
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

    private String checkForSOAPActionHeader(SOAPMessage request) {
        MimeHeaders hdrs = request.getMimeHeaders();

        if (hdrs != null) {
            String[] saHdrs = hdrs.getHeader(SOAP_ACTION);

            if (saHdrs != null && saHdrs.length > 0) {
                return saHdrs[0];
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SOAPException
     * @throws WebServiceException
     */
    @Override
    public String sendMessage(URL url, String message) throws SOAPException, WebServiceException {
        throw new WebServiceException("Operation not supported by SoapClientImpl");
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SOAPException
     * @throws WebServiceException
     */
    @Override
    public String sendMessage(URL url, String method, String message) throws SOAPException, WebServiceException {
        throw new WebServiceException("Operation not supported by SoapClientImpl");
    }
    
    private String trace(SOAPMessage msg) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            msg.writeTo(out);
            return new String(out.toByteArray());
        } catch (Exception e) {
            return "n/a";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

}
