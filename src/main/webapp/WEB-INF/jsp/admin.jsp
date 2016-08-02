<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>


<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0" %>


<portlet:defineObjects/>


<portlet:actionURL var="setAdminProperty" portletMode="admin"><portlet:param name="action" value="setAdminProperty" /></portlet:actionURL>
<portlet:actionURL var="annuler" portletMode="admin"><portlet:param name="action" value="annuler" /></portlet:actionURL>


<div>
	<form:form method="post" modelAttribute="formulaire"  action="${setAdminProperty}">  	
		
	
		<div class="ligne">
			Space Id des Dossiers disciplinaires :
			<form:input path="spaceId" name="spaceId"/>
			<font style="color: #C11B17;"><form:errors path="spaceId"/></font>
		</div>	
		
		
		
	
		<div class="ligne">
		
			<div class="bouton">
				<input type="submit" name="valider" value="Valider" />
				<input type="button" name="annuler" id="annuler" value="Annuler" onclick="updatePortletContent(this,'${annuler}');" />
			</div>
			
		</div>
	
	</form:form>
	
</div>