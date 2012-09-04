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
package org.nabucco.testautomation.engine.proxy.ws.client.rest;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.soap.SOAPException;

import org.apache.axis.encoding.Base64;
import org.nabucco.testautomation.engine.base.exception.NBCTestConfigurationException;
import org.nabucco.testautomation.engine.base.xml.XMLToolkit;
import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceClient;
import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceListener;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.w3c.dom.Document;

/**
 * RestClientImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class RestClientImpl implements WebServiceClient {

    private final List<WebServiceListener> listeners = new ArrayList<WebServiceListener>();

    private Map<String, String> headerAttributes = new HashMap<String, String>();

    private String username;
    
    private String password;

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
        throw new UnsupportedOperationException("HeaderElements are not supported by REST");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendMessage(URL url, String message) throws SOAPException, WebServiceException {
        throw new WebServiceException("Operation not supported by RestClientImpl");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendMessage(URL url, String method, String message) throws SOAPException, WebServiceException {

        try {
            this.start();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            
            // Check Basic Authentication
            if (this.username != null && this.password != null) {
                String auth = this.username + ":" + this.password;
                connection.setRequestProperty(AUTHORIZATION, BASIC + Base64.encode(auth.getBytes()));
            }
            
            // Add customer header attributes
            addHeaderAttributes(connection);

            if (message != null) {
                connection.setDoOutput(true);
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(message);
                out.flush();
                out.close();
                this.messageSent(message);
            }

            final int responseCode = connection.getResponseCode();

            switch (responseCode) {
            case HttpURLConnection.HTTP_OK: {
                String responseMessage = receiveResponse(connection.getInputStream());
                this.finished();
                this.messageReceived(responseMessage);
                return responseMessage;
            }
            default:
                String responseMessage = receiveResponse(connection.getErrorStream());
                this.finished();
                this.messageReceived(responseMessage);
                throw new WebServiceException("Unexpected ResponseCode: " + responseCode);
            }
        } catch (IOException e) {
            throw new WebServiceException("Error during communication with URL '" + url.toString() + "'");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document sendMessage(URL url, Document message) throws SOAPException, WebServiceException {
        throw new WebServiceException("Operation not supported by RestClientImpl");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document sendMessage(URL url, String method, Document message) throws SOAPException, WebServiceException {

        try {
            this.start();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            
            // Check Basic Authentication
            if (this.username != null && this.password != null) {
                String auth = this.username + ":" + this.password;
                connection.setRequestProperty(AUTHORIZATION, BASIC + Base64.encode(auth.getBytes()));
            }
            
            // Add customer header attributes
            addHeaderAttributes(connection);

            StringWriter writer = new StringWriter();
            XMLToolkit.writeXMLDocument(message, writer);
            String messageString = writer.toString();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(messageString);
            out.flush();
            out.close();
            this.messageSent(messageString);

            final int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            this.finished();

            if (responseMessage != null) {
                this.messageReceived(responseMessage);
            }

            switch (responseCode) {
            case HttpURLConnection.HTTP_OK:
                return XMLToolkit.loadXMLDocument(responseMessage);
            default:
                throw new WebServiceException("Unexpected ResponseCode: " + responseCode);
            }
        } catch (IOException e) {
            throw new WebServiceException("Error during communication with URL '" + url.toString() + "'", e);
        } catch (NBCTestConfigurationException e) {
            throw new WebServiceException("Could parse ResponseMessage into XML", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document sendMessage(URL url, List<Document> messages) throws SOAPException, WebServiceException {
        throw new WebServiceException("Operation not supported by RestClientImpl");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document sendMessage(URL url, String method, List<Document> messages) throws SOAPException,
            WebServiceException {
        throw new WebServiceException("Operation not supported by RestClientImpl");
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

    private void messageSent(String request) {

        for (WebServiceListener listener : this.listeners) {
            listener.messageSent(request);
        }
    }

    private void messageReceived(String request) {

        for (WebServiceListener listener : this.listeners) {
            listener.messageReceived(request);
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
    
    private String receiveResponse(InputStream inputStream) throws IOException {

        StringBuffer responseMessage = new StringBuffer();
        BufferedInputStream in = new BufferedInputStream(inputStream);
        byte[] buf = new byte[1024];
        int c;

        while ((c = in.read(buf)) > 0) {
            responseMessage.append(new String(buf, 0, c));
        }

        return responseMessage.toString();
    }
    
    /**
     * @param connection
     */
    private void addHeaderAttributes(HttpURLConnection connection) {
        
        for (Entry<String, String> entry : this.headerAttributes.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

}
