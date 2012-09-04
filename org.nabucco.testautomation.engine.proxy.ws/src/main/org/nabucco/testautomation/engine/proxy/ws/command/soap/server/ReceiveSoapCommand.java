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
package org.nabucco.testautomation.engine.proxy.ws.command.soap.server;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.nabucco.testautomation.engine.base.exception.InterruptionException;
import org.nabucco.testautomation.engine.base.exception.NBCTestConfigurationException;
import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.engine.proxy.ws.WsConstants;
import org.nabucco.testautomation.engine.proxy.ws.command.soap.AbstractSoapCommand;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.handler.SoapMessageHandler;
import org.nabucco.testautomation.engine.proxy.ws.handler.XmlMessageHandler;
import org.nabucco.testautomation.engine.proxy.ws.server.http.BlockingHttpServer;
import org.nabucco.testautomation.engine.proxy.ws.server.http.HttpException;
import org.nabucco.testautomation.engine.proxy.ws.server.http.HttpRequest;
import org.nabucco.testautomation.property.facade.datatype.NumericProperty;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyType;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;
import org.w3c.dom.Document;

/**
 * ReceiveSoapCommand
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class ReceiveSoapCommand extends AbstractSoapCommand {

    private BlockingHttpServer httpServer;

    private HttpRequest httpRequest;

    private final String defaultNSPrefix;

    /**
     * Creates a new ReceiveSoapCommand
     * 
     * @param httpServer
     *            the hhtp server to use
     * @param defaultNSPrefix
     *            the default namespace prefix
     * @throws WebServiceException
     *             thrown, if the server is not started
     */
    public ReceiveSoapCommand(BlockingHttpServer httpServer, String defaultNSPrefix) throws WebServiceException {

        if (httpServer == null) {
            throw new WebServiceException("Server is not started");
        }
        this.httpServer = httpServer;
        this.defaultNSPrefix = defaultNSPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyList execute(Metadata metadata, PropertyList propertyList) throws WebServiceException {

        long timeout = getTimeout(metadata, propertyList);

        // Get XPath for extraction
        PropertyList extractProps = (PropertyList) PropertyHelper.getFromList(metadata.getPropertyList(),
                PropertyType.LIST, WsConstants.REQUEST_ID);

        try {
            this.info("Waiting to receive SOAP-Request ...");
            this.start();
            this.httpRequest = httpServer.receive(timeout);
            this.stop();
            this.info("Received Http-Request from client: " + this.httpRequest.getClientAddress());

            String soapMessage = this.httpRequest.getRequestBody();
            this.setRequest(soapMessage);

            // Extract XML-Message (payload) from SOAPMessage
            SoapMessageHandler handler = new SoapMessageHandler(this.defaultNSPrefix);
            Document message = handler.getSoapBody(soapMessage);

            // Extract Properties from XML-Message
            XmlMessageHandler messageHandler = new XmlMessageHandler(message, this.defaultNSPrefix);
            messageHandler.setExtractionProperties(extractProps);
            return messageHandler.extract();
        } catch (InterruptedException ex) {
            this.setException(ex);
            throw new InterruptionException("Receiving SOAP-Message aborted");
        } catch (HttpException ex) {
            this.error(ex.getMessage());
            this.setException(ex);
            throw new WebServiceException(ex.getMessage());
        } catch (IOException ex) {
            this.setException(ex);
            throw new WebServiceException(ex.getMessage());
        } catch (XPathExpressionException ex) {
            this.setException(ex);
            throw new WebServiceException(ex.getMessage());
        } catch (NBCTestConfigurationException ex) {
            this.setException(ex);
            throw new WebServiceException(ex.getMessage());
        }
    }

    /**
     * 
     * @return
     */
    public HttpRequest getHttpRequest() {
        return this.httpRequest;
    }

    /**
     * 
     * @param metadata
     * @param properties
     * @return
     * @throws WebServiceException
     */
    private long getTimeout(Metadata metadata, PropertyList properties) throws WebServiceException {

        // First, check PropertyList from Action
        Property timeoutProperty = PropertyHelper.getFromList(properties, TIMEOUT);

        if (timeoutProperty != null && timeoutProperty.getType() == PropertyType.NUMERIC) {
            return ((NumericProperty) timeoutProperty).getValue().getValue().longValue();
        }

        // Second, check PropertyList from Metadata
        timeoutProperty = PropertyHelper.getFromList(metadata.getPropertyList(), TIMEOUT);

        if (timeoutProperty != null && timeoutProperty.getType() == PropertyType.NUMERIC) {
            return ((NumericProperty) timeoutProperty).getValue().getValue().longValue();
        }

        throw new WebServiceException("TIMEOUT not defined");
    }

}
