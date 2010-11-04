
import org.codehaus.groovy.runtime.InvokerHelper

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.ApplicationAttributes

import grails.util.GrailsUtil
import grails.plugins.nimble.core.Role

import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ThreadContext
import org.apache.shiro.SecurityUtils

import fedreg.core.Organization

import fedreg.workflow.Process
import fedreg.workflow.WorkflowScript

import fedreg.host.User
import fedreg.host.Profile

class BootStrap {
	
	def grailsApplication
	def dataImporterService
	def workflowProcessService
	def sessionFactory
	def nimbleService
	def roleService

     def init = { servletContext ->
		def applicationContext = servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT) 
		
		nimbleService.init()
		
		// Populate default administrative account if required
		if(User.count() == 0) {
			def profile = new Profile(email:'internaladministrator@not.valid')
			def user = new User(username:'internaladministrator', enabled: false, external:false, federated: false, profile: profile)
			user.save(flush: true)
		}
		
		// Provide security manager as none yet exists
		def suMetaClass = new ExpandoMetaClass(SecurityUtils)
		suMetaClass.'static'.getSubject = {[getPrincipal:{User.findByUsername('internaladministrator').id}] as Subject}
		suMetaClass.initialize()
		SecurityUtils.metaClass = suMetaClass
		
		// Populate Workflow Scripts on initial deployment name is set as filename <name>.groovy
		if(WorkflowScript.count() == 0) {	
			def scripts = new File("${System.getenv('FEDREG_CONFIG')}/workflow/scripts")
			scripts.eachFile { script ->
				def name = script.name =~ /(.+?)(\.[^.]*$|$)/
				def s = new WorkflowScript(name: name[0][1], definition: script.getText(), creator:User.findByUsername('internaladministrator'))
				if(!s.save()) {
					log.error "Unable to correctly process workflow script $script during bootstrap"
					s.errors.each {
						log.error it
					}
					throw new RuntimeException("Unable to import $script")
				}
				else {
					log.info "Loaded valid workflow script $script"
				}
			}
		}
		
		// Create federation-administrators role, used in workflows etc
		def fedAdminRole = Role.findWhere(name:"federation-administrators")
		if(!fedAdminRole)
			roleService.createRole("federation-administrators", "Role representing federation level administrators who can make decisions onbehalf of the entire federation, particuarly in workflows", false)
		
		// Populate WorkFlows on initial deployment
		if(Process.count() == 0) {	
			def processes = new File("${System.getenv('FEDREG_CONFIG')}/workflow/processes")
			processes.eachFile { process ->
				workflowProcessService.create(process.getText())
			}
		}
		
		SecurityUtils.metaClass = null
		
		if(GrailsUtil.environment == "development") {
			if(Organization.count() == 0) {
				def base = new File("../scripts/setup/samlBase.groovy")
				def attrBase = new File("../scripts/setup/aafAttributePopulation.groovy")
				def rrData = new File("../scripts/setup/importRRContent.groovy")
			
				def shell = new GroovyShell(grailsApplication.classLoader, new Binding(
				            'application': grailsApplication,
				            'context': applicationContext, 'ctx': applicationContext))
				shell.'shell' = shell
			
				shell.evaluate(base.text)
				shell.evaluate(attrBase.text)
				shell.evaluate(rrData.text)
			}
		}
     }

     def destroy = {
     }
} 