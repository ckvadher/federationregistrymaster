package fedreg.compliance

import fedreg.core.Attribute
import fedreg.core.AttributeCategory
import fedreg.core.IDPSSODescriptor
import fedreg.compliance.CategorySupportStatus

class IdpAttributeComplianceController {
	
	def index = {
		redirect action:summary
	}
	
	def summary = {
		def idpInstanceList = IDPSSODescriptor.list()
		
		def categorySupportSummaries = []
		idpInstanceList.each { idp ->			
			def categories = AttributeCategory.listOrderByName()
			categories.each {
				def total = Attribute.countByCategory(it)
				def supported = idp.attributes.findAll{a ->	a.category == it }
				def summary = new CategorySupportStatus(totalCount:total, supportedCount:supported.size(), name:it.name, idp: idp)
				categorySupportSummaries.add(summary)
			}
		}
		
		[idpInstanceList:idpInstanceList, categorySupportSummaries:categorySupportSummaries]
	}
	
	def comprehensive = {
		def idp = IDPSSODescriptor.get(params.id)
        if (!idp) {
			flash.type="error"
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'fedreg.label.identityprovider'), params.id])}"
            redirect(action: "summary")
			return
        }

		def categorySupport = []
		def categories = AttributeCategory.list()
		categories.each {
			def total = Attribute.countByCategory(it)
			def supported = idp.attributes.findAll{att -> att.category == it }
			def currentStatus = new CategorySupportStatus(totalCount:total, supportedCount:supported.size(), available:Attribute.findAllByCategory(it), supported:supported, name:it.name)
			categorySupport.add(currentStatus)
		}
        [idp:idp, categorySupport: categorySupport]
	}
	
	def federationwide = {
		def attribute = Attribute.get(params.id)
		if (!attribute) {
			flash.type="error"
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'fedreg.label.attribute'), params.id])}"
            redirect(action: "summary")
			return
        }

		def idpInstanceList = IDPSSODescriptor.list()
		def supportingIdpInstanceList = idpInstanceList.findAll{idp -> attribute in idp.attributes}
		[idpInstanceList:idpInstanceList, supportingIdpInstanceList: supportingIdpInstanceList, attribute: attribute]
	}
	
}