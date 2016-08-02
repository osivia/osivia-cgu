<%@ page contentType="text/plain; charset=UTF-8"%>
<%@ include file="/WEB-INF/jsp/include.jsp"%>

<portlet:defineObjects />


</portlet:renderURL>
<portlet:renderURL var="actionValidation">
	<portlet:param name="action" value="validationCgus" />
</portlet:renderURL>

<div class="container-fluid">
	<div class="row">
		<h2>Conditions Générales d'Utilisation</h2>
	</div>
	<div class="row">


		<div class="col-md-12">
			<h3>Prenez connaissance des Conditions Générales d'Utilisation</h3>
			<div class="row">

				<div class="col-md-12"
					style="margin-bottom: 10px; height: 400px; overflow-x: hidden; overflow-y: scroll; border: 1px solid #bbbbbb;">
					${cgus}</div>

			</div>

			<div class="row">

				<div class="col-md-12">
					<a class="btn btn-primary" href="${actionValidation}"> <i
						class="glyphicons halflings ok"></i> <span>J'accepte les
							Conditions Générales d'Utilisation</span>
					</a>
				</div>
			</div>

		</div>

	</div>


</div>
