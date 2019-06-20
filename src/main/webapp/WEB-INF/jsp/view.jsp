<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" session="false"%>


<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>


<portlet:defineObjects />


<portlet:actionURL var="validateCgu" portletMode="view">
	<portlet:param name="action" value="validateCgu" />
</portlet:actionURL>

<portlet:actionURL var="rejectCgu" portletMode="view">
	<portlet:param name="action" value="rejectCgu" />
</portlet:actionURL>

<form:form method="post" modelAttribute="formulaire"
	action="${validateCgu}">

	<div style="height: 400px;" class="mb-3 p-3 overflow-auto border rounded">
		${cgus}</div>


	<div>
		<a class="btn btn-primary" href="${validateCgu}"> <i
			class="halflings halflings-ok"></i> <span>J'accepte les
				Conditions Générales d'Utilisation</span>
		</a>
	</div>

</form:form>

