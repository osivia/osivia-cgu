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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.locator.Locator;
import org.osivia.portal.api.urls.IPortalUrlFactory;
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


    /** Portal URL factory. */
    private IPortalUrlFactory portalUrlFactory;



   
    
    
    @PostConstruct
    public void initNuxeoService() throws Exception {
        super.init();
        if ((this.portletContext != null) && (this.portletContext.getAttribute("nuxeoService") == null)) {

            this.init(this.portletConfig);
        }
        
        // Portal URL factory
        this.portalUrlFactory = Locator.findMBean(IPortalUrlFactory.class, IPortalUrlFactory.MBEAN_NAME);
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
    public void validateCgu(@ModelAttribute final FormAdmin formulaire, final BindingResult result, final ActionRequest request,
            final ActionResponse response, final ModelMap modelMap, final PortletSession session, final ModelMap model) throws Exception {

        final NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
        final PortalWindow window = WindowFactory.getWindow(request);

        int level = 1;
        String cguLevel = window.getProperty("osivia.services.cgu.level");
        try {
            level = Integer.parseInt(cguLevel);
        } catch(Exception e)    {
        }
        nuxeoController.executeNuxeoCommand(new UpdateProfileCommand(request.getUserPrincipal().getName(), level));
        
        
         // Level Marked as checked
        HttpServletRequest servletRequest = (HttpServletRequest) request.getAttribute(Constants.PORTLET_ATTR_HTTP_REQUEST);
        servletRequest.getSession().setAttribute("osivia.services.cgu.level", level);
        
        String redirectUrl = (String) servletRequest.getSession().getAttribute("osivia.services.cgu.pathToRedirect");
        if( redirectUrl != null)    {
            String adaptedUrl = portalUrlFactory.adaptPortalUrlToNavigation(new PortalControllerContext(getPortletContext(), request, response), redirectUrl);
            response.sendRedirect(adaptedUrl);
        }
    }
    
    
    @ActionMapping(params = "action=rejectCgu")
    public void rejectCgu(@ModelAttribute final FormAdmin formulaire, final BindingResult result, final ActionRequest request,
            final ActionResponse response, final ModelMap modelMap, final PortletSession session, final ModelMap model) throws Exception {

        final NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());

        int level = 0;
        nuxeoController.executeNuxeoCommand(new UpdateProfileCommand(request.getUserPrincipal().getName(), level));
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
