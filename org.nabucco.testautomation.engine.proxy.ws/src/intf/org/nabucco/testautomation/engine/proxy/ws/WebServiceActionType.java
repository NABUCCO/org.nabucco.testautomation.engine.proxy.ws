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
package org.nabucco.testautomation.engine.proxy.ws;

import org.nabucco.testautomation.engine.proxy.SubEngineActionType;

/**
 * WebServiceActionType
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public enum WebServiceActionType implements SubEngineActionType {

    /**
     * The ActionType to call a webservice.
     */
    CALL("call"),
    
    /**
     * 
     */
    START("start"),
    
    /**
     * 
     */
    STOP("stop"),
    
    /**
     * The ActionType to receive a webservice-call.
     */
    RECEIVE("receive"),
    
    /**
     * The ActionType to receive and transform a webservice-call.
     */
    TRANSFORM("XSLT transformation"),
    
    /**
     * The ActionType to receive and validate a webservice-call.
     */
    VALIDATE("validate against a XSD"),
    
    /**
     * 
     */
    RESPOND("respond to the client");
    
    private String description;

    private WebServiceActionType(String desc) {
        this.description = desc;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
}
