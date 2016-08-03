package org.osivia.services.cgu;

import javax.annotation.PostConstruct;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.windows.PortalWindow;
import org.osivia.portal.api.windows.WindowFactory;
import org.osivia.services.cgu.bean.FormAdmin;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.context.PortletConfigAware;
import org.springframework.web.portlet.context.PortletContextAware;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

@Controller
@RequestMapping("VIEW")
public class ViewController extends CMSPortlet implements PortletContextAware, PortletConfigAware {


    private PortletContext portletContext;
    private PortletConfig portletConfig;

    @PostConstruct
    public void initNuxeoService() throws Exception {
        super.init();
        if ((this.portletContext != null) && (this.portletContext.getAttribute("nuxeoService") == null)) {

            this.init(this.portletConfig);
        }

    }

    @RequestMapping
    public String showView(final ModelMap model, final RenderRequest request, final RenderResponse response, final PortletSession session) {

        final NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
        final PortalWindow window = WindowFactory.getWindow(request);


        String cguPath = window.getProperty("osivia.services.cgu.path");

        if (cguPath != null) {
            final Document document = nuxeoController.fetchDocument(cguPath);
            nuxeoController.setCurrentDoc(document);
            final String note = nuxeoController.transformHTMLContent((String) document.getProperties().get("note:note"));
            request.setAttribute("cgus", note);
        }


        return "view";
    }


    @ActionMapping(params = "action=validateCgu")
    public void setAdminProperty(@ModelAttribute final FormAdmin formulaire, final BindingResult result, final ActionRequest request,
            final ActionResponse response, final ModelMap modelMap, final PortletSession session, final ModelMap model) throws Exception {

        final NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
        final PortalWindow window = WindowFactory.getWindow(request);

        String cguLevel = window.getProperty("osivia.services.cgu.level");
        int level = Integer.parseInt(cguLevel);
        nuxeoController.executeNuxeoCommand(new UpdateProfilCommand(request.getUserPrincipal().getName(), level));
    }


    @Override
    public void setPortletContext(PortletContext ctx) {
        this.portletContext = ctx;

    }

    @Override
    public void setPortletConfig(PortletConfig portletConfig) {
        this.portletConfig = portletConfig;

    }

}
