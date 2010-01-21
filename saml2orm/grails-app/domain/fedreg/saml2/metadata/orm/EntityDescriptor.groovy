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

package fedreg.saml2.metadata.orm

/**
 * @author Bradley Beddoes
 */
class EntityDescriptor {

  String entityID
  Organization organization
  String extensions

  static hasMany = [
          idpDescriptors: IDPSSODescriptor,
          spDescriptors: SPSSODescriptor,
          attributeAuthorityDescriptors: AttributeAuthorityDescriptor,
          pdpDescriptors: PDPDescriptor,
          additionalMetadataLocations: AdditionalMetadataLocation,
          contactPersons: ContactPerson
  ]

  static constraints = {
    idpDescriptors(nullable: true)
    spDescriptors(nullable: true)
    attributeAuthorityDescriptors(nullable: true)
    pdpDescriptors(nullable: true)
    organization(nullable: true)
    contactPersons(nullable: true)
    extensions(nullable: true)
  }

	static mapping = {
    	tablePerHierarchy false
  	}

}
