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
package org.nabucco.testautomation.engine.proxy.ws;

import org.nabucco.testautomation.engine.proxy.SubEngineOperationType;

/**
 * WebServiceEngineOperationType
 *
 * @author Steffen Schmidt, PRODYNA AG
 *
 */
public enum WebServiceEngineOperationType implements SubEngineOperationType {

    /**
     * OperationType to interact with WebServices based on the SOAP Version 1.1 standard.
     */
    SOAP_1_1_CLIENT,

    /**
     * OperationType to interact with WebServices based on the SOAP Version 1.2 standard.
     */
    SOAP_1_2_CLIENT,
    
    /**
     * OperationType to provide WebServices based on the SOAP Version 1.2 standard.
     */
    SOAP_SERVER,
    
    /**
     * OperationType to interact with WebServices based on REST.
     */
    REST_CLIENT,
    
    /**
     * OperationType to provide WebServices based on REST
     */
    REST_SERVER;
    
}
