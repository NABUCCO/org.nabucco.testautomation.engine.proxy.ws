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
package org.nabucco.testautomation.engine.proxy.ws.command.soap.client;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.nabucco.testautomation.engine.base.util.PropertyHelper;
import org.nabucco.testautomation.engine.proxy.ws.WsConstants;
import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceClient;
import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceClientFactory;
import org.nabucco.testautomation.engine.proxy.ws.client.WebServiceListener;
import org.nabucco.testautomation.engine.proxy.ws.command.soap.AbstractSoapCommand;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.engine.proxy.ws.handler.XmlMessageHandler;
import org.w3c.dom.Document;

import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.facade.datatype.property.XmlProperty;
import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyContainer;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyType;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * SoapCallCommand
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class SoapCallCommand extends AbstractSoapCommand implements
		WebServiceListener {

	@Override
	public PropertyList execute(Metadata metadata, PropertyList propertyList)
			throws WebServiceException {
		
		// get the target URL
		URL url = getURL(metadata);

		// get the XML-Message to send
		String message = getXmlMessage(metadata, propertyList);

		// Get XPath for transformation
		PropertyList transformProps = (PropertyList) PropertyHelper
				.getFromList(metadata.getPropertyList(), PropertyType.LIST,
						WsConstants.REQUEST_ID);

		// Get XPath for extraction
		PropertyList extractProps = (PropertyList) PropertyHelper.getFromList(
				metadata.getPropertyList(), PropertyType.LIST,
				WsConstants.RESPONSE_ID);

		// Initialize XmlMessageHandler
		XmlMessageHandler messageHandler = new XmlMessageHandler(message);
		messageHandler.setTransformationProperties(transformProps);
		messageHandler.setExtractionProperties(extractProps);

		try {
			Document xml = messageHandler.transform(propertyList);
			WebServiceClient soapExecutor = WebServiceClientFactory
					.getInstance().getWebServiceClient();
			soapExecutor.addWebServiceListener(this);
			Document rs = soapExecutor.sendMessage(url, xml);
			soapExecutor.removeWebServiceListener(this);
			messageHandler.setMessage(rs);
			PropertyList responseProperties = messageHandler.extract();
			return responseProperties;
		} catch (SOAPException ex) {
			setException(ex);
			throw new WebServiceException(ex);
		}
	}

	/**
	 * Gets the target URL specified in the given Metadata object.
	 * 
	 * @param metadata
	 *            the metadata
	 * @return the URL specified by the given metadata
	 * @throws WebServiceException
	 *             thrown, if an error occurs
	 */
	private URL getURL(Metadata metadata) throws WebServiceException {
		for (PropertyContainer container : metadata.getPropertyList()
				.getPropertyList()) {
			Property property = container.getProperty();

			if (property.getName().getValue().equalsIgnoreCase(WsConstants.URL)) {
				try {
					return new URL(PropertyHelper.toString(property));
				} catch (MalformedURLException e) {
					throw new WebServiceException("Invalid URL: "
							+ e.toString());
				}
			} else if (property.getName().getValue()
					.equalsIgnoreCase(WsConstants.REQUEST_ID)
					&& property.getType() == PropertyType.LIST) {
				for (PropertyContainer container2 : ((PropertyList) property)
						.getPropertyList()) {
					Property prop = container2.getProperty();

					if (prop.getName().getValue()
							.equalsIgnoreCase(WsConstants.URL)) {
						try {
							return new URL(PropertyHelper.toString(prop));
						} catch (MalformedURLException e) {
							throw new WebServiceException("Invalid URL: "
									+ e.toString());
						}
					}
				}
			}
		}
		throw new WebServiceException("URL not defined ");
	}

	/**
	 * Tries to find the XML-Message for the request within the properties of
	 * the given metadata. If the message is not found, an exception is thrown.
	 * 
	 * @param metadata
	 *            the metadata to get the XML-message from
	 * @throws WebServiceException
	 *             thrown, if no message was found
	 */
	private String getXmlMessage(Metadata metadata, PropertyList propertyList)
			throws WebServiceException {

		if (propertyList != null) {

			// First, check PropertyList from Action
			XmlProperty messageProperty = (XmlProperty) PropertyHelper
					.getFromList(propertyList, PropertyType.XML);

			if (messageProperty != null) {
				return messageProperty.getValue().getValue();
			}
		}

		// Second, check PropertyList of Metadata
		XmlProperty messageProperty = (XmlProperty) PropertyHelper.getFromList(
				metadata.getPropertyList(), PropertyType.XML);

		if (messageProperty != null) {
			return messageProperty.getValue().getValue();
		}

		throw new WebServiceException("No XML-Message defined");
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

	@Override
	public void start() {
		super.start();
	}

	@Override
	public void finished() {
		stop();
	}

	@Override
	public void messageSent(SOAPMessage request) {
		this.setRequest(trace(request));
	}

	@Override
	public void messagereceived(SOAPMessage response) {
		this.setResponse(trace(response));
	}

}
