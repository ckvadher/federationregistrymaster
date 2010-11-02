<%@ page import="fedreg.core.Organization" %>
<html>
	<head>	
		<meta name="layout" content="workflow" />
		<title><g:message code="fedreg.view.workflow.approval.title" /></title>
	</head>
	<body>
		<section>
			<h2><g:message code="fedreg.view.workflow.approval.heading" /></h2>

			<g:if test="${tasks}">
				<p><g:message code="fedreg.view.workflow.approval.descriptive" /></p>
				<table>
					<thead>
						<tr>
							<th><g:message code="label.name" /></th>
							<th><g:message code="label.description" /></th>
							<th><g:message code="label.processinstance" /></th>
							<th><g:message code="label.action" /></th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${tasks}" status="i" var="instance">
							<tr>
								<td>${fieldValue(bean: instance, field: "task.name")}</td>
								<td>${fieldValue(bean: instance, field: "task.description")}</td>
								<td>
									${fieldValue(bean: instance, field: "processInstance.description")}
									<br><br>
									<ul class="clean">
										<g:if test="${instance.processInstance.params.containsKey('identityProvider')}">
											<li><n:button href="${createLink(controller:'IDPSSODescriptor', action:'show', id:instance.processInstance.params.identityProvider)}" label="${message(code: 'label.view')} ${message(code: 'label.identityprovider')}" icon="arrowthick-1-ne"/></li>
										</g:if>
										<g:if test="${instance.processInstance.params.containsKey('serviceProvider')}">
											<li><n:button href="${createLink(controller:'SPSSODescriptor', action:'show', id:instance.processInstance.params.serviceProvider)}" label="${message(code: 'label.view')} ${message(code: 'label.serviceprovider')}" icon="arrowthick-1-ne"/></li>
										</g:if>
										<g:if test="${instance.processInstance.params.containsKey('organization') && instance.processInstance.params.organization.isNumber()}">
											<li><n:button href="${createLink(controller:'organization', action:'show', id:instance.processInstance.params.organization)}" label="${message(code: 'label.view')} ${message(code: 'label.organization')}" icon="arrowthick-1-ne"/></li>
										</g:if>
									</ul>
								</td>
								<td>
									<g:form action="approve" id="${instance.id}" name="submitapproval${i}"></g:form>
									<n:confirmaction action="\$('#submitapproval${i}').submit();" title="${message(code: 'fedreg.view.workflow.approval.approve.confirm.title')}" msg="${message(code: 'fedreg.view.workflow.approval.approve.confirm.descriptive', args:[instance.task.name, instance.processInstance.description])}" accept="${message(code: 'label.accept')}" cancel="${message(code: 'label.cancel')}" label="${message(code: 'label.approve')}" icon="check" />
									<h5>or reject due to:</h5>
									<ul class="clean">
										<g:each in="${instance.task.rejections}" var="rej">
											<li>
												<g:form action="reject" id="${instance.id}" name="submitrejection${i}">
													<g:hiddenField name="rejection" value="${rej.key}" />
												</g:form>
												<n:confirmaction action="\$('#submitrejection${i}').submit();" title="${message(code: 'fedreg.view.workflow.approval.rejection.confirm.title')}" msg="${message(code: 'fedreg.view.workflow.approval.rejection.confirm.descriptive', args:[instance.task.name, instance.processInstance.description, rej.value.description])}" accept="${message(code: 'label.accept')}" cancel="${message(code: 'label.cancel')}" label="${rej.value.name}" icon="close" />
											</li>
										</g:each>
									</ul>
								</td>
							</tr>
							<g:if test="${i+1 != tasks.size()}">
							<tr>
								<td colspan="4"><hr></td>
							</tr>
							</g:if>
						</g:each>
					</tbody>
				</table>
			</g:if>
			<g:else>
				<p><g:message code="fedreg.view.workflow.approval.nothing" /></p>
			</g:else>
			
		</section>
	</body>
</html>