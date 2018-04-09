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

<div>


	<div class="container-fluid">

		<div class="row">
			<form:form method="post" modelAttribute="formulaire"
				action="${validateCgu}">

				<div class="col-md-12">
					<div class="row">

						<div class="col-md-12"
							style="margin-bottom: 10px; height: 400px; overflow-x: hidden; overflow-y: scroll; border: 1px solid #bbbbbb;">
							${cgus}</div>

					</div>

					<div class="row">

						<div class="col-md-6">
							<a class="btn btn-primary" href="${validateCgu}"> <i
								class="halflings halflings-ok"></i> <span>J'accepte les
									Conditions Générales d'Utilisation</span>
							</a>
						</div>

					</div>

				</div>
			</form:form>
		</div>


	</div>