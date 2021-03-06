package fedreg.core

import org.apache.shiro.SecurityUtils

import grails.plugins.nimble.core.UserBase
import grails.plugins.nimble.core.ProfileBase
import grails.plugins.nimble.core.Role

/**
 * Provides administration management views for Descriptors.
 *
 * @author Bradley Beddoes
 */
class DescriptorAdministrationController {
	def allowedMethods = [grantFullAdministration: 'POST', revokeFullAdministration: 'DELETE']
	
	def roleService
	
	def listFullAdministration = {
		def descriptor = Descriptor.get(params.id)
		if (!descriptor) {
			flash.type="error"
			flash.message = message(code: 'fedreg.core.descriptor.nonexistant')
			redirect(action: "list")
			return
		}
		
		def adminRole = Role.findByName("descriptor-${descriptor.id}-administrators")
		render template: '/templates/descriptor/listfulladministration', contextPath: pluginContextPath, model:[descriptor:descriptor, administrators: adminRole?.users]
	}
	
	def searchFullAdministration = {
		def descriptor = Descriptor.get(params.id)
		if (!descriptor) {
			flash.type="error"
			flash.message = message(code: 'fedreg.core.descriptor.nonexistant')
			redirect(action: "list")
			return
		}
		
		def q = "%" + params.q + "%"
	    log.debug("Performing search for users matching $q")

	    def users = UserBase.findAllByUsernameIlike(q)
	    ProfileBase.findAllByFullNameIlikeOrEmailIlike(q, q)?.each {
			if(!users.contains(it.owner))
				users.add(it.owner)
	    }

		def adminRole = Role.findByName("descriptor-${descriptor.id}-administrators")
		if(adminRole) {
			users.removeAll(adminRole.users)
			render template: '/templates/descriptor/searchresultsfulladministration', contextPath: pluginContextPath, model:[descriptor:descriptor, users: users]
		} else {
			log.warn("Attempt to search for administrative control for $descriptor to $user by $authenticatedUser was denied. Administrative role doesn't exist")
			response.sendError(403)
		}
	}
	
	def grantFullAdministration = {
		def descriptor = Descriptor.get(params.id)
		if (!descriptor) {
			flash.type="error"
			flash.message = message(code: 'fedreg.core.descriptor.nonexistant')
			redirect(action: "list")
			return
		}	
		
		def user = UserBase.get(params.userID)
		if (!descriptor) {
			flash.type="error"
			flash.message = message(code: 'fedreg.core.user.nonexistant')
			redirect(action: "list")
			return
		}
		
		if(SecurityUtils.subject.isPermitted("descriptor:${descriptor.id}:manage:administrators")) {
			def adminRole = Role.findByName("descriptor-${descriptor.id}-administrators")
			
			if(adminRole) {
				roleService.addMember(user, adminRole)
			
				log.info "$authenticatedUser successfully added $user to $adminRole"
				render message(code: 'fedreg.descriptor.administration.grant.success')
			} else {
				log.warn("Attempt to grant administrative control for $descriptor to $user by $authenticatedUser was denied. Administrative role doesn't exist")
				response.sendError(403)
			}
		}
		else {
			log.warn("Attempt to assign complete administrative control for $descriptor to $user by $authenticatedUser was denied, incorrect permission set")
			response.sendError(403)
		}
	}
	
	def revokeFullAdministration = {
		def descriptor = Descriptor.get(params.id)
		if (!descriptor) {
			flash.type="error"
			flash.message = message(code: 'fedreg.core.descriptor.nonexistant')
			redirect(action: "list")
			return
		}	
		
		def user = UserBase.get(params.userID)
		if (!descriptor) {
			flash.type="error"
			flash.message = message(code: 'fedreg.core.user.nonexistant')
			redirect(action: "list")
			return
		}
		
		if(SecurityUtils.subject.isPermitted("descriptor:${descriptor.id}:manage:administrators")) {
			def adminRole = Role.findByName("descriptor-${descriptor.id}-administrators")
			roleService.deleteMember(user, adminRole)
			
			log.info "$authenticatedUser successfully removed $user from $adminRole"
			render message(code: 'fedreg.descriptor.administration.revoke.success')
		}
		else {
			log.warn("Attempt to remove complete administrative control for $descriptor from $user by $authenticatedUser was denied, incorrect permission set")
			response.sendError(403)
		}
	}
}