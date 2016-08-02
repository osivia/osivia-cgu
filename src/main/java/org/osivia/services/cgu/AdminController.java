package org.osivia.services.cgu;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.services.cgu.bean.FormAdmin;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.ActionMapping;

@Controller
@RequestMapping("ADMIN")

public class AdminController {

	@RequestMapping
	public String showAdmin(final ModelMap model, final RenderRequest request, final PortletSession session) {
		final PortalWindow window = WindowFactory.getWindow(request);
		final FormAdmin formulaire = new FormAdmin();

		if (window.getProperty("osivia.services.cgu.path") != null) {
			formulaire.setCguPath(window.getProperty("osivia.services.cgu.path"));
		}

		model.addAttribute("formulaire", formulaire);

		return "admin";
	}

	@ActionMapping(params = "action=setAdminProperty")
	public void setAdminProperty(@ModelAttribute final FormAdmin formulaire, final BindingResult result, final ActionRequest request, final ActionResponse response,
			final ModelMap modelMap, final PortletSession session, final ModelMap model) throws Exception {

		final PortalWindow window = WindowFactory.getWindow(request);
		window.setProperty("osivia.services.cgu.path", formulaire.getCguPath());
		response.setPortletMode(PortletMode.VIEW);
		response.setRenderParameter("action", "");
	}

	@ActionMapping(params = "action=annuler")
	public void annuler(final ActionRequest request, final ActionResponse response, final PortletSession session, final ModelMap modelMap) throws Exception {

		response.setPortletMode(PortletMode.VIEW);
		response.setRenderParameter("action", "");

	}

}
