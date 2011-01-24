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
package org.nabucco.testautomation.engine.proxy.ws.client;

import java.net.URL;

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
     * @param trace The MessageTrace to be filed with tracing information. Null, if no tracing is request
     * @return the response message
     * @throws WebServiceException thrown, if an error occurs
     */
    public Document sendMessage(URL url, Document message) throws SOAPException, WebServiceException;

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
    
}