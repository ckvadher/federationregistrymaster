package fedreg.core

import fedreg.workflow.ProcessPriority

class SPSSODescriptorService {

	def cryptoService
	def workflowProcessService

	def create(def params) {
		// Organization
		def organization = Organization.get(params.organization?.id)

		// Contact
		def contact = Contact.get(params.contact?.id)
		if(!contact) {
			if(params.contact?.email)
				contact = MailURI.findByUri(params.contact?.email)?.contact		// We may already have them referenced by email address and user doesn't realize
			if(!contact)
				contact = new Contact(givenName: params.contact?.givenName, surname: params.contact?.surname, email: new MailURI(uri:params.contact?.email), organization:organization)
				contact.save()
		}
		def ct = params.contact?.type ?: 'administrative'
		
		// Entity Descriptor
		def entityDescriptor = EntityDescriptor.get(params.entity.id)
		if(!entityDescriptor) {
			entityDescriptor = new EntityDescriptor(active: params.active, entityID: params.entity?.identifier, organization: organization)
			def entContactPerson = new ContactPerson(contact:contact, type:ContactType.findByName(ct))
			entityDescriptor.addToContacts(entContactPerson)
		}
		
		// SP
		def samlNamespace = SamlURI.findByUri('urn:oasis:names:tc:SAML:2.0:protocol')
		def serviceProvider = new SPSSODescriptor(active:params.active, displayName: params.sp?.displayName, description: params.sp?.description, organization: organization, authnRequestsSigned:true, wantAssertionsSigned: true)
		serviceProvider.addToProtocolSupportEnumerations(samlNamespace)
		
		def acs = new AttributeConsumingService(lang:params.lang ?:'en')
		acs.addToServiceNames(params.sp?.displayName ?: '')
		acs.addToServiceDescriptions(params.sp?.description ?: '')
		params.sp.attributes.each { a -> 
			if(a.value.requested == "on") {
				def attr = AttributeBase.get(a.key)
				if(attr)
					acs.addToRequestedAttributes(new RequestedAttribute(base:attr, reasoning: a.value.reasoning, isRequired: (a.value.required == 'on') ))
			}
		}
		serviceProvider.addToAttributeConsumingServices(acs)
		
		params.sp.nameidformats.each { nameFormatID -> 
			if(nameFormatID.value == "on") {
				def nameid = SamlURI.get(nameFormatID.key)
				if(nameid)
					serviceProvider.addToNameIDFormats(nameid)
			}
		}
		def spContactPerson = new ContactPerson(contact:contact, type:ContactType.findByName(ct))
		serviceProvider.addToContacts(spContactPerson)
		
		// Initial endpoints
		// Assertion Consumer Services
		def postBinding = SamlURI.findByUri(SamlConstants.httpPost)
		def postLocation = new UrlURI(uri: params.sp?.acs?.post?.uri)
		def httpPostACS = new AssertionConsumerService(binding: postBinding, location:postLocation, active:params.active)
		serviceProvider.addToAssertionConsumerServices(httpPostACS)
		httpPostACS.validate()

		def artifactBinding = SamlURI.findByUri(SamlConstants.soap)
		def artifactLocation = new UrlURI(uri: params.sp?.acs?.artifact?.uri)
		def soapArtifactACS = new AssertionConsumerService(binding: artifactBinding, location:artifactLocation, active:params.active)
		serviceProvider.addToAssertionConsumerServices(soapArtifactACS)
		soapArtifactACS.validate()
		
		// Single Logout Services
		def sloArtifact, sloRedirect, sloSOAP, sloPost
		if(params.sp?.slo?.artifact?.uri){
			def sloArtifactBinding = SamlURI.findByUri(SamlConstants.httpArtifact)
			def sloArtifactLocation = new UrlURI(uri: params.sp?.slo?.artifact?.uri)
			sloArtifact = new SingleLogoutService(binding: sloArtifactBinding, location:sloArtifactLocation, active:params.active)
			serviceProvider.addToSingleLogoutServices(sloArtifact)
			sloArtifact.validate()
		}
		if(params.sp?.slo?.redirect?.uri){
			def sloRedirectBinding = SamlURI.findByUri(SamlConstants.httpRedirect)
			def sloRedirectLocation = new UrlURI(uri: params.sp?.slo?.redirect?.uri)
			sloRedirect	= new SingleLogoutService(binding: sloRedirectBinding, location:sloRedirectLocation, active:params.active)
			serviceProvider.addToSingleLogoutServices(sloRedirect)
			sloRedirect.validate()
		}
		if(params.sp?.slo?.soap?.uri){
			def sloSOAPBinding = SamlURI.findByUri(SamlConstants.soap)
			def sloSOAPLocation = new UrlURI(uri: params.sp?.slo?.soap?.uri)
			sloSOAP = new SingleLogoutService(binding: sloSOAPBinding, location:sloSOAPLocation, active:params.active)
			serviceProvider.addToSingleLogoutServices(sloSOAP)
			sloSOAP.validate()
		}
		if(params.sp?.slo?.post?.uri){
			def sloPostBinding = SamlURI.findByUri(SamlConstants.httpPost)
			def sloPostLocation = new UrlURI(uri: params.sp?.slo?.post?.uri)
			sloPost = new SingleLogoutService(binding: sloPostBinding, location:sloPostLocation, active:params.active)
			serviceProvider.addToSingleLogoutServices(sloPost)
			sloPost.validate()
		}
		
		// Cryptography
		// Signing
		if(params.sp?.crypto?.sig) {
			def cert = cryptoService.createCertificate(params.sp?.crypto?.sigdata)
			cryptoService.validateCertificate(cert)
			def keyInfo = new KeyInfo(certificate: cert)
			def keyDescriptor = new KeyDescriptor(keyInfo:keyInfo, keyType:KeyTypes.signing, roleDescriptor:serviceProvider)
			serviceProvider.addToKeyDescriptors(keyDescriptor)
		}
		
		// Encryption
		if(params.sp?.crypto?.enc) {
			def certEnc = cryptoService.createCertificate(params.sp?.crypto?.encdata)
			cryptoService.validateCertificate(certEnc)
			def keyInfoEnc = new KeyInfo(certificate:certEnc)
			def keyDescriptorEnc = new KeyDescriptor(keyInfo:keyInfoEnc, keyType:KeyTypes.encryption, roleDescriptor:serviceProvider)
			serviceProvider.addToKeyDescriptors(keyDescriptorEnc)
		}
		
		// Service Description
		def serviceDescription = new ServiceDescription(connectURL: params.sp?.servicedescription?.connecturl, logo: params.sp?.servicedescription?.logo, furtherInfo: params.sp?.servicedescription?.furtherInfo, 
			provides: params.sp?.servicedescription?.provides, benefits: params.sp?.servicedescription?.benefits, audience: params.sp?.servicedescription?.audience, restrictions: params.sp?.servicedescription?.restrictions, 
			accessing: params.sp?.servicedescription?.accessing, support: params.sp?.servicedescription?.support, maintenance: params.sp?.servicedescription?.maintenance)
		serviceProvider.serviceDescription = serviceDescription
		
		// Submission validation
		if(!entityDescriptor.save()) {
			entityDescriptor?.errors.each { log.error it }
			return [false, organization, entityDescriptor, serviceProvider, httpPostACS, soapArtifactACS, sloArtifact, sloRedirect, sloSOAP, sloPost, Organization.list(), AttributeBase.list(), SamlURI.findAllWhere(type:SamlURIType.ProtocolBinding), contact]
		}
		serviceProvider.entityDescriptor = entityDescriptor
		entityDescriptor.addToSpDescriptors(serviceProvider)
		
		if(!serviceProvider.validate()) {			
			serviceProvider.errors.each { log.warn it }
			return [false, organization, entityDescriptor, serviceProvider, httpPostACS, soapArtifactACS, sloArtifact, sloRedirect, sloSOAP, sloPost, Organization.list(), AttributeBase.list(), SamlURI.findAllWhere(type:SamlURIType.ProtocolBinding), contact]
		}
		
		if(!serviceProvider.save()) {			
			serviceProvider.errors.each {log.warn it}
			return [false, organization, entityDescriptor, serviceProvider, httpPostACS, soapArtifactACS, sloArtifact, sloRedirect, sloSOAP, sloPost, Organization.list(), AttributeBase.list(), SamlURI.findAllWhere(type:SamlURIType.ProtocolBinding), contact]
		}
		
		def workflowParams = [ creator:contact?.id, serviceProvider:serviceProvider?.id, organization:organization.name ]
		def processInstance = workflowProcessService.initiate( "spssodescriptor_create", "Approval for creation of ${serviceProvider}", ProcessPriority.MEDIUM, workflowParams)
		workflowProcessService.run(processInstance)
		
		return [true, organization, entityDescriptor, serviceProvider, httpPostACS, soapArtifactACS, sloArtifact, sloRedirect, sloSOAP, sloPost, Organization.list(), AttributeBase.list(), SamlURI.findAllWhere(type:SamlURIType.ProtocolBinding), contact]
	}
}