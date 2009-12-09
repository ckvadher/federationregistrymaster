/*
 *  saml2orm, A Grails/Hibernate compatible environment for SAML2 metadata types
 *  Copyright (C) 2009 Intient Pty Ltd
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package aaf.fedreg.saml2.metadata.orm

/**
 * @author Bradley Beddoes
 */
class AttributeConsumingService {

	boolean isDefault
	int index

	static hasMany = [
    	serviceNames: LocalizedName,
	    serviceDescriptions: LocalizedName,
	    requestedAttributes: RequestedAttribute
	]

	static constraints = {
		serviceDescriptions(nullable: true)
	}
	
	static mapping = {
		index column: "mdindex"		
	}

}