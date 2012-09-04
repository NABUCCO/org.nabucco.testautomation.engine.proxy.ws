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

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.Message;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.soap.MessageFactoryImpl;
import org.apache.axis.soap.SOAPConstants;

/**
 * SoapMessageFactory
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class SoapMessageFactory extends MessageFactoryImpl {

    private static SoapMessageFactory instance;

    private SoapMessageFactory() {
    }

    public static synchronized SoapMessageFactory getInstance() {

        if (instance == null) {
            instance = new SoapMessageFactory();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    public SOAPMessage createMessage(SOAPConstants constants) throws SOAPException {
        SOAPEnvelope env = new SOAPEnvelope(constants);
        env.setSAAJEncodingCompliance(true);
        env.removeNamespaceDeclaration("xsd");
        env.removeNamespaceDeclaration("xsi");
        Message message = new Message(env);
        message.setMessageType(Message.REQUEST);
        return message;
    }

}
