<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="compliance" />
		<g:set var="entityName" value="${message(code: 'identityProviderAttributeCompliance.label')}" />
		<title><g:message code="fedreg.view.compliance.attribute.title"/></title>
	</head>
	<body>
		<h2><g:message code="fedreg.view.compliance.attribute.heading" args="${[attribute?.friendlyName]}" /></h2>
		<div class="attributesummary">
			<div class="attribute">
				<h3><g:message code="fedreg.view.compliance.attribute.graph.heading" /></h3>
				<div class="numeric">
					<strong>${supportingIdpInstanceList.size().encodeAsHTML()}<span class="total"> / ${idpInstanceList.size().encodeAsHTML()}</span></strong>
				</div>
				<div id="graphic${i}" style="width: 150px; height: 125px;"></div>
				<script type="text/javascript">
					line${i} = [['supported',${supportingIdpInstanceList.size()}], ['unsupported',${(idpInstanceList.size() - supportingIdpInstanceList.size())}] ];
					plot${i} = $.jqplot('graphic${i}', [line${i}], {
						title: '',
						seriesColors: [ "#30A800", "#D44226" ],
						grid: { background: '#fff', borderColor: '#fff', shadow: false },
						seriesDefaults:{renderer:$.jqplot.PieRenderer, rendererOptions:{sliceMargin:0, diameter: 80}}
					});
				</script>
			</div>
		</div>
		<div class="attributedetail">
			<table class="enhancedtabledata">
				<thead>
					<tr>
						<th><g:message code="fedreg.label.identityprovider" /></th>
						<th><g:message code="fedreg.label.status" /></th>
						<th/>
					</tr>
				</thead>
				<tbody>
				<g:each in="${idpInstanceList}" status="i" var="idp">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td>${idp?.displayName?.encodeAsHTML()}</td>
						<td>
						<g:if test="${supportingIdpInstanceList.contains(idp)}">
							<span class="icon icon_tick"><g:message code="fedreg.label.supported"/></span>
						</g:if>
						<g:else>
							<span class="icon icon_cross"><g:message code="fedreg.label.notsupported"/></span>
						</g:else>
						</td>
						<td><g:link action="comprehensive" id="${idp.id}" class="button icon icon_magnifier"><g:message code="fedreg.link.idpattravail" /></g:link></td>
					</tr>
				</g:each>
			   </tbody>
		   </table>
		</div>
	</body>
</html>