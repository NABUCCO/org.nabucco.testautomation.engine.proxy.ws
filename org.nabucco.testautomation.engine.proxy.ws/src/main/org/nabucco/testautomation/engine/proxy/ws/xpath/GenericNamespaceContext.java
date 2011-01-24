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
package org.nabucco.testautomation.engine.proxy.ws.xpath;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * GenericNamespaceContext
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class GenericNamespaceContext implements NamespaceContext {

	private final Map<String, String> prefixMap = new HashMap<String, String>();

	private final Map<String, String> namespaceMap = new HashMap<String, String>();

	/**
     * Adds a namespace to the context.
     * 
     * @param uri the namespace uri
     * @param prefix the namespace prefix
     */
	public void addNamespace(String uri, String prefix) {
		prefixMap.put(prefix, uri);
		namespaceMap.put(uri, prefix);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNamespaceURI(String prefix) {
		return prefixMap.get(prefix);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrefix(String uri) {
		return namespaceMap.get(uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<String> getPrefixes(String uri) {
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		String s = "[";
		for (String prefix : this.prefixMap.keySet()) {
			s += prefix + "=" + this.prefixMap.get(prefix) + ", ";
		}
		return s.substring(0, s.length() - 2) + "]";
	}

}
