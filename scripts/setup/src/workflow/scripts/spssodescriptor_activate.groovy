
import grails.plugins.nimble.core.*
import fedreg.core.*


workflowTaskService = ctx.getBean("workflowTaskService")
mailService = ctx.getBean("mailService")
invitationService = ctx.getBean("invitationService")
roleService = ctx.getBean("roleService")
permissionService = ctx.getBean("permissionService")
messageSource = ctx.getBean("messageSource")

def sp = SPSSODescriptor.get(env.serviceProvider.toLong())

if(sp) {

	log.info "Activating $sp. Workflow indicates it is valid and accepted for operation."
	
	sp.approved = true
	sp.active = true
	sp.save()
	
	if(sp.hasErrors()) {
		throw new RuntimeException("Attempt to process activate in script spssodescriptor_activate. Failed due to SP fault on save")
	}

	if(sp.entityDescriptor.approved == false || sp.entityDescriptor.active == false) {
		sp.entityDescriptor.approved = true
		sp.entityDescriptor.active = true
		sp.entityDescriptor.save()
		if(sp.entityDescriptor.hasErrors()) {
			throw new RuntimeException("Attempt to process activate in script spssodescriptor_activate. Failed due to SP entityDescriptor fault on save")
		}
	}
	
	// Create ED access control role
	def edRole = Role.findWhere(name:"descriptor-${sp.entityDescriptor.id}-administrators")
	if(!edRole){	// Generally expected state
		edRole = roleService.createRole("descriptor-${sp.entityDescriptor.id}-administrators", "Global administrators for ${sp.entityDescriptor}", false)
	
		LevelPermission permission = new LevelPermission()
	    permission.populate("descriptor", "${sp.entityDescriptor.id}", "*", null, null, null)
	    permission.managed = false
		permissionService.createPermission(permission, edRole)
	}
	
	// Create SP access control role
	def role = Role.findWhere(name:"descriptor-${sp.id}-administrators")
	if(!role){	// Expected state
		role = roleService.createRole("descriptor-${sp.id}-administrators", "Global administrators for $sp", false)
	}
	
	def permission = new LevelPermission()
	permission.populate("descriptor", "${sp.id}", "*", null, null, null)
	permission.managed = false
	permissionService.createPermission(permission, role)
	
	def invitation = invitationService.create(null, role.id, null, "SPSSODescriptor", "show", sp.id.toString())
	
	def creator = Contact.get(env.creator.toLong())
	mailService.sendMail {            
		to creator.email.uri
		from ctx.grailsApplication.config.nimble.messaging.mail.from
		subject messageSource.getMessage("fedreg.templates.mail.workflow.sp.activated.subject", null, "fedreg.templates.mail.workflow.sp.activated.subject", new Locale(env.locale))
		body view:"/templates/mail/workflows/default/_activated_sp", model:[serviceProvider:sp, locale:env.locale, invitation:invitation]
	}

	workflowTaskService.complete(env.taskInstanceID.toLong(), 'spssodescriptoractivated')
}
else {
	throw new RuntimeException("Attempt to process activate in script spssodescriptor_activate. Failed because referenced SP does not exist")
}