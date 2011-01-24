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
package org.nabucco.testautomation.engine.proxy.ws.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.nabucco.testautomation.engine.base.util.PropertyHelper;
import org.nabucco.testautomation.engine.proxy.ws.WsConstants;

import org.nabucco.testautomation.facade.datatype.property.BooleanProperty;
import org.nabucco.testautomation.facade.datatype.property.DateProperty;
import org.nabucco.testautomation.facade.datatype.property.base.Property;

/**
 * PropertyValueMapper
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public abstract class PropertyValueMapper {

    private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ssZ");

    /**
     * Evaluates the type of the given property and returns its value in a webservice compatible
     * format.
     * 
     * @param prop
     *            the property to get value from
     * @return the value from the property in webservice (e.g. SOAP) compatible format
     */
    public static String getValue(Property prop) {

    	if (prop == null) {
    		return WsConstants.EMPTY_STRING;
    	}
    	
        switch (prop.getType()) {
        case BOOLEAN:
            return mapBoolean((BooleanProperty) prop);
        case DATE:
            return mapDate((DateProperty) prop);
        default:
            return PropertyHelper.toString(prop);
        }
    }
    
    /**
     * Gets the date value in the xsd:DateTime format: 
     * YYYY-MM-DDTHH:MM:SSZ
     * 
     * @param prop the property
     * @return the formatted date string
     */
    private static String mapDate(DateProperty prop) {
        
    	if (prop == null) {
    		return WsConstants.EMPTY_STRING;
    	}
    	
    	// xs:DateTime-format: YYYY-MM-DDTHH:MM:SSZ
    	Date date = prop.getValue().getValue();

        if (date == null) {
            return WsConstants.EMPTY_STRING;
        }
        return dateTimeFormatter.format(date);
    }

    /**
     * Gets the boolean value in the format: 
     * "true" or "false"
     * 
     * @param prop the property
     * @return the formatted date string
     */
    private static String mapBoolean(BooleanProperty prop) {
        
    	if (prop == null) {
    		return WsConstants.EMPTY_STRING;
    	}
    	Boolean b = prop.getValue().getValue();

        if (b == null || !b.booleanValue()) {
            return "false";
        } else {
            return "true";
        }
    }

}
