/*
 *  A Grails/Hibernate compatible environment for SAML2 metadata types with application specific 
 *  data extensions as appropriate.
 * 
 *  Copyright (C) 2010 Australian Access Federation
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

package fedreg.core

/**
 * While not marked explicitly due to GORM issues RoleDescriptor is considered 'Abstract'
 *
 * @author Bradley Beddoes
 */
class RoleDescriptor extends Descriptor {
	static auditable = true
	
	def cryptoService
	
	Organization organization
	UrlURI errorURL
	
	String displayName
	String description
	String extensions
	
	boolean active
	boolean archived = false
	boolean approved = false
	boolean reporting = true

	Date dateCreated
	Date lastUpdated

	static hasMany = [
		contacts: ContactPerson,
		protocolSupportEnumerations: SamlURI,
		keyDescriptors: KeyDescriptor,
		monitors: ServiceMonitor
	]

	static mapping = {
		tablePerHierarchy false
		sort "displayName"
	}

	static constraints = {
		displayName(nullable:false, blank:false)
		description(nullable:false, blank: false, maxSize:2000)
		
		organization(nullable: false)
		extensions(nullable: true, maxSize:2000)
		errorURL(nullable:true)
		protocolSupportEnumerations(nullable: false, minSize:1)
		contacts(nullable: true)
		keyDescriptors(nullable: true)
		dateCreated(nullable:true)
		lastUpdated(nullable:true)
	}
	
	public String toString() { "roledescriptor:[id:$id, displayName: $displayName]" }
	
	// RoleDescriptor is considered abstract but can't be marked as such due to GORM issues
	// This method should be overlaoded by all subclasses
	public boolean functioning() {
		false
	}
}
