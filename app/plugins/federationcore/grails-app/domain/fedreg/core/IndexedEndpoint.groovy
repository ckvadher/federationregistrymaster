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
 * While not marked explicitly due to GORM issues both Endpoint and IndexedEndpoint are considered 'Abstract'
 *
 * @author Bradley Beddoes
 */
class IndexedEndpoint extends Endpoint  {
	static auditable = true

	boolean isDefault = false
	int index

  	static mapping = {
		tablePerHierarchy false
		index column: "samlmd_index"
  	}

	static constraints = {
		isDefault(nullable:false)
		index(nullable:false)
	}

	public String toString() { "indexedendpoint:[id:$id, location: $location]" }
}
