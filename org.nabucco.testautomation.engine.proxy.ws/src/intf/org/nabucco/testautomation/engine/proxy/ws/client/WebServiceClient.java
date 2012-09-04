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
package org.nabucco.testautomation.engine.proxy.ws.client;

import java.net.URL;
import java.util.List;

import javax.xml.soap.SOAPException;

import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.w3c.dom.Document;


/**
 * WebServiceClient
 *
 * @author Steffen Schmidt, PRODYNA AG
 *
 */
public interface WebServiceClient {

    public static final String BASIC = " Basic ";

    public static final String AUTHORIZATION = "Authorization";
    
    /**
     * Adds an custom attribute to the header of the webservice message.
     * 
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @throws WebServiceException thrown, if an error occurs
     */
    public void addHeaderAttribute(String name, String value) throws WebServiceException;

    /**
     * Adds an custom element to the header of the webservice message.
     * 
     * @param name the name of the element
     * @param value the value of the element
     * @throws WebServiceException thrown, if an error occurs
     */
    public void addHeaderElement(String name, String value) throws WebServiceException;

    /**
     * Sends the given message to the given URL.
     * 
     * @param url the target URL
     * @param message the payload to be send
     * @return the response message
     * @throws WebServiceException thrown, if an error occurs
     */
    public String sendMessage(URL url, String message) throws SOAPException, WebServiceException;
    
    /**
     * Sends the given message to the given URL.
     * 
     * @param url the target URL
     * @param method the webservice method to call
     * @param message the payload to be send
     * @return the response message
     * @throws WebServiceException thrown, if an error occurs
     */
    public String sendMessage(URL url, String method, String message) throws SOAPException, WebServiceException;
    
    /**
     * Sends the given message to the given URL.
     * 
     * @param url the target URL
     * @param message the payload to be send
     * @return the response message
     * @throws WebServiceException thrown, if an error occurs
     */
    public Document sendMessage(URL url, Document message) throws SOAPException, WebServiceException;

    /**
     * Sends the given message to the given URL.
     * 
     * @param url the target URL
     * @param method the webservice method to call
     * @param message the payload to be send
     * @return the response message
     * @throws WebServiceException thrown, if an error occurs
     */
    public Document sendMessage(URL url, String method, Document message) throws SOAPException, WebServiceException;

    /**
     * Sends the given message to the given URL.
     * 
     * @param url the target URL
     * @param message the payload to be send
     * @return the response message
     * @throws WebServiceException thrown, if an error occurs
     */
    public Document sendMessage(URL url, List<Document> messages) throws SOAPException, WebServiceException;
    
    /**
     * Sends the given message to the given URL.
     * 
     * @param url the target URL
     * @param method the webservice method to call
     * @param message the payload to be send
     * @return the response message
     * @throws WebServiceException thrown, if an error occurs
     */
    public Document sendMessage(URL url, String method, List<Document> messages) throws SOAPException, WebServiceException;

    /**
     * Adds a {@link WebServiceListener} to the client.
     * 
     * @param listener the listener to add
     */
    public void addWebServiceListener(WebServiceListener listener);
    
    /**
     * Removes a {@link WebServiceListener} from the client.
     * 
     * @param listener the listener to remove
     */
    public void removeWebServiceListener(WebServiceListener listener);
    
    /**
     * Sets the username for basic authentication.
     * 
     * @param username the username for authentication
     */
    public void setUsername(String username);
    
    /**
     * Sets the password for basic authentication.
     * 
     * @param password the username for authentication
     */
    public void setPassword(String password);
    
}
