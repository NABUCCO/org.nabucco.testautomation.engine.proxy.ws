/*
 * Copyright 2012 PRODYNA AG
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
package org.nabucco.testautomation.engine.proxy.ws.json;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jetty.util.ajax.JSON;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyContainer;
import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;

/**
 * JsonMapper
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class JsonMapper {

    /**
     * Maps a given PropertyList into a JSON-String.
     * 
     * @param property the PropertyList to map
     * @return the JSON-String
     */
    public static String mapToString(PropertyList property) {

        Map<String, Object> map = new HashMap<String, Object>();

        for (PropertyContainer container : property.getPropertyList()) {
            Property prop = container.getProperty();
            map.put(prop.getName().getValue(), mapProperty(prop));
        }
        return JSON.toString(map);
    }

    /**
     * Maps the given JSON-String into a PropertyList.
     * 
     * @param json the JSON-String to be parsed and mapped
     * @return the mapped PropertyList
     */
    @SuppressWarnings("unchecked")
    public static PropertyList mapFromString(String json) {

        PropertyList propertyList = PropertyHelper.createPropertyList("JSON");
        Map<String, Object> map = (Map<String, Object>) JSON.getDefault().fromJSON(json);
        Iterator<Entry<String, Object>> it = map.entrySet().iterator();

        while (it.hasNext()) {
            Entry<String, Object> entry = it.next();
            PropertyHelper.add(mapProperty(entry.getKey(), entry.getValue()), propertyList);
        }

        return propertyList;
    }

    @SuppressWarnings("unchecked")
    private static Property mapProperty(String name, Object obj) {

        if (obj instanceof String) {
            return PropertyHelper.createTextProperty(name, (String) obj);
        } else if (obj instanceof Boolean) {
            return PropertyHelper.createBooleanProperty(name, (Boolean) obj);
        } else if (obj instanceof Integer) {
            return PropertyHelper.createNumericProperty(name, (Integer) obj);
        } else if (obj instanceof BigDecimal) {
            return PropertyHelper.createNumericProperty(name, (BigDecimal) obj);
        } else if (obj instanceof Map) {
            PropertyList list = PropertyHelper.createPropertyList(name);
            Map<String, Object> map = (Map<String, Object>) obj;
            Iterator<Entry<String, Object>> it = map.entrySet().iterator();

            while (it.hasNext()) {
                Entry<String, Object> entry = it.next();
                PropertyHelper.add(mapProperty(entry.getKey(), entry.getValue()), list);
            }

            return list;
        }
        return null;
    }

    private static Object mapProperty(Property property) {

        switch (property.getType()) {
        case LIST:
            Map<String, Object> map = new HashMap<String, Object>();

            for (PropertyContainer container : ((PropertyList) property).getPropertyList()) {
                Property prop = container.getProperty();
                map.put(prop.getName().getValue(), mapProperty(prop));
            }
            return map;
        default:
            return PropertyHelper.toString(property);
        }
    }

}
