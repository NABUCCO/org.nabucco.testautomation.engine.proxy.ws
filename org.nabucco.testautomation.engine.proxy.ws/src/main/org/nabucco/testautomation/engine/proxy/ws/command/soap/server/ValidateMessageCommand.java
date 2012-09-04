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
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.engine.proxy.ws.command.soap.AbstractSoapCommand;
import org.nabucco.testautomation.engine.proxy.ws.exception.WebServiceException;
import org.nabucco.testautomation.property.facade.datatype.BooleanProperty;
import org.nabucco.testautomation.property.facade.datatype.FileProperty;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.TextProperty;
import org.nabucco.testautomation.property.facade.datatype.XmlProperty;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyType;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;
import org.xml.sax.SAXException;

/**
 * ValidateRequestCommand
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class ValidateMessageCommand extends AbstractSoapCommand {

	/**
	 * {@inheritDoc}
	 */	
	@Override
	public PropertyList execute(Metadata metadata, PropertyList propertyList)
			throws WebServiceException {

		String url = getSchemaLocation(metadata, propertyList);
		FileProperty schemaFile = getSchemaFile(metadata, propertyList);
		XmlProperty xmlProperty = getXmlMessage(metadata, propertyList);
		BooleanProperty resultProperty = getResultProperty(propertyList);
		String xml = xmlProperty.getValue().getValue();
		Schema schema = null;
		
		try {
			this.setRequest(xml);
			this.start();
			
			// Lookup a factory for the W3C XML Schema language
			SchemaFactory factory = SchemaFactory
					.newInstance(SCHEMA_LANGUAGE);

			// Compile the schema for validation.
			if (url != null) {
				URL schemaLocation = new URL(url);
				schema = factory.newSchema(schemaLocation);
				this.info("Validating against SchemaLocation: " + url);
			} else if (schemaFile != null) {
				Source schemeSource = new StreamSource(new StringReader(
						schemaFile.getContent().getValue()));
				schema = factory.newSchema(schemeSource);
				this.info("Validating against SchemaFile: " + schemaFile);
			} else {
				throw new WebServiceException(
						"No Schema (XSD) found for validation");
			}
		} catch (SAXException ex) {
			this.setException(ex);
			throw new WebServiceException("Could not create Schema: " + ex.getMessage());
		} catch (MalformedURLException ex) {
			this.setException(ex);
			throw new WebServiceException("SchemaLocation is not a valid URL: "
					+ url);
		} finally {
			this.stop();
		}

		try {
			// Get a validator from the schema.
			Validator validator = schema.newValidator();

			// Parse the document you want to check.
			Source source = new StreamSource(new StringReader(xml));

			// Check the document
			validator.validate(source);
			this.stop();
			this.info(xmlProperty.getName().getValue() + " is valid");

			if (resultProperty != null) {
				resultProperty.setValue(Boolean.TRUE);
				PropertyList responseProperty = PropertyHelper
						.createPropertyList("ResponseProperties");
				PropertyHelper.add(resultProperty, responseProperty);
				return responseProperty;
			}
			return null;
		} catch (SAXException ex) {
			this.setException(ex);
			String msg = xmlProperty.getName().getValue() + " is NOT valid because: "
					+ ex.getMessage();
			this.setResponse(msg);
			this.info(msg);

			if (resultProperty != null) {
				resultProperty.setValue(Boolean.FALSE);
				PropertyList responseProperty = PropertyHelper
						.createPropertyList("ResponseProperties");
				PropertyHelper.add(resultProperty, responseProperty);
				return responseProperty;
			}
			return null;
		} catch (IOException ex) {
			this.setException(ex);
			throw new WebServiceException("Error reading XML: "
					+ ex.getMessage());
		} finally {
			this.stop();
		}
	}

	/**
	 * 
	 * @param properties
	 * @return
	 */
	private BooleanProperty getResultProperty(PropertyList properties) {

		BooleanProperty resultProperty = (BooleanProperty) PropertyHelper
				.getFromList(properties, PropertyType.BOOLEAN);

		if (resultProperty != null) {
			resultProperty.cloneObject();
		}
		return resultProperty;
	}

	/**
	 * 
	 * @param metadata
	 * @param properties
	 * @return
	 * @throws WebServiceException
	 */
	private XmlProperty getXmlMessage(Metadata metadata, PropertyList properties)
			throws WebServiceException {

		// First, check PropertyList from Action
		Property xmlProperty = PropertyHelper.getFromList(properties,
				PropertyType.XML);

		if (xmlProperty != null) {
			return (XmlProperty) xmlProperty;
		}

		// Second, check PropertyList from Metadata
		xmlProperty = PropertyHelper.getFromList(metadata.getPropertyList(),
				PropertyType.XML);

		if (xmlProperty != null) {
			return (XmlProperty) xmlProperty;
		}

		throw new WebServiceException("No XML-Message found for validation");
	}

	/**
	 * 
	 * @param metadata
	 * @param properties
	 * @return
	 * @throws WebServiceException
	 */
	private String getSchemaLocation(Metadata metadata, PropertyList properties)
			throws WebServiceException {

		// First, check PropertyList from Action
		Property schemaLocationProperty = PropertyHelper.getFromList(
				properties, PropertyType.TEXT, XSD);

		if (schemaLocationProperty != null) {
			return ((TextProperty) schemaLocationProperty).getValue()
					.getValue();
		}

		// Second, check PropertyList from Metadata
		schemaLocationProperty = PropertyHelper.getFromList(
				metadata.getPropertyList(), PropertyType.TEXT, XSD);

		if (schemaLocationProperty != null) {
			return ((TextProperty) schemaLocationProperty).getValue()
					.getValue();
		}

		return null;
	}

	/**
	 * 
	 * @param metadata
	 * @param properties
	 * @return
	 * @throws WebServiceException
	 */
	private FileProperty getSchemaFile(Metadata metadata,
			PropertyList properties) throws WebServiceException {

		// First, check PropertyList from Action
		Property schemaFileProperty = PropertyHelper.getFromList(properties,
				PropertyType.FILE);

		if (schemaFileProperty != null) {
			return (FileProperty) schemaFileProperty;
		}

		// Second, check PropertyList from Metadata
		schemaFileProperty = PropertyHelper.getFromList(
				metadata.getPropertyList(), PropertyType.FILE);

		if (schemaFileProperty != null) {
			return (FileProperty) schemaFileProperty;
		}

		return null;
	}

}
