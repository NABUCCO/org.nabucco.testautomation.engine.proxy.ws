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

import org.nabucco.testautomation.engine.proxy.ws.client.rest.RestClientImpl;
import org.nabucco.testautomation.engine.proxy.ws.client.soap.SoapClientImpl;
import org.nabucco.testautomation.engine.proxy.ws.client.soap.SoapVersion;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;


/**
 * WebServiceClientFactory
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class WebServiceClientFactory {

    private static WebServiceClientFactory instance;

    /**
     * Constructs a new factory instance.
     */
    private WebServiceClientFactory() {
    }

    /**
     * Gets an instance of the factory.
     * 
     * @return the factory instance
     */
    public static final synchronized WebServiceClientFactory getInstance() {

        if (instance == null) {
            instance = new WebServiceClientFactory();
        }
        return instance;
    }

    /**
     * Gets an instance of a {@link WebServiceClient} for SOAP-Calls.
     * 
     * @return the WebServiceClient instance
     * @param the required SOAP-Version
     * @throws WebServiceException
     *             thrown, if the instance could not be created
     */
    public WebServiceClient getSoapWebServiceClient(SoapVersion version) throws WebServiceException {
        return new SoapClientImpl(version);
    }

    /**
     * Gets an instance of a {@link WebServiceClient} for REST-Calls.
     * 
     * @return the WebServiceClient instance
     * @throws WebServiceException
     *             thrown, if the instance could not be created
     */
    public WebServiceClient getRestWebServiceClient() throws WebServiceException {
        return new RestClientImpl();
    }

}
