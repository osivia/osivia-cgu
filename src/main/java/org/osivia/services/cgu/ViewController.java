package org.osivia.services.cgu;

import javax.annotation.PostConstruct;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
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
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.context.PortletConfigAware;
import org.springframework.web.portlet.context.PortletContextAware;

import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;
import fr.toutatice.portail.cms.nuxeo.api.cms.NuxeoDocumentContext;

/**
 * CGU portlet controller.
 * 
 * @see CMSPortlet
 * @see PortletConfigAware
 * @see PortletContextAware
 */
@Controller
@RequestMapping("VIEW")
public class ViewController extends CMSPortlet implements PortletConfigAware, PortletContextAware {

    /** Portlet config. */
    private PortletConfig portletConfig;
    /** Portlet context. */
    private PortletContext portletContext;


    /** Portal URL factory. */
    private final IPortalUrlFactory portalUrlFactory;


    /**
     * Constructor.
     */
    public ViewController() {
        super();

        // Portal URL factory
        this.portalUrlFactory = Locator.findMBean(IPortalUrlFactory.class, IPortalUrlFactory.MBEAN_NAME);
    }


    /**
     * Post-construct.
     *
     * @throws PortletException
     */
    @PostConstruct
    public void postConstruct() throws PortletException {
        super.init(this.portletConfig);
    }
   

    /**
     * View render mapping.
     * 
     * @param request render request
     * @param response render response
     * @return view path
     */
    @RenderMapping
    public String view(RenderRequest request, RenderResponse response) {
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(request, response, this.portletContext);
        // Portal window
        PortalWindow window = WindowFactory.getWindow(request);

        // CGU path
        String path = window.getProperty("osivia.services.cgu.path");

        if (StringUtils.isNotEmpty(path)) {
            if (!path.startsWith("/")) {
                // WebId
                path = NuxeoController.webIdToFetchPath(path);
            }

            // Nuxeo document context
            NuxeoDocumentContext documentContext = nuxeoController.getDocumentContext(path);
            // Nuxeo document
            Document document = documentContext.getDocument();

            // Note content
            String note = nuxeoController.transformHTMLContent(document.getString("note:note"));
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
        
        PortalControllerContext portalControllerContext = new PortalControllerContext( getPortletContext(), request, response);

         
        
        // Get redirect url from session
        String redirectUrl = (String) servletRequest.getSession().getAttribute("osivia.services.cgu.pathToRedirect");
        
        
        if( redirectUrl != null)    {
            String closeURL = portalUrlFactory.getDestroyCurrentPageUrl(portalControllerContext, redirectUrl);
            response.sendRedirect(closeURL);
        }
    }
    
    
    @ActionMapping(params = "action=rejectCgu")
    public void rejectCgu(@ModelAttribute final FormAdmin formulaire, final BindingResult result, final ActionRequest request,
            final ActionResponse response, final ModelMap modelMap, final PortletSession session, final ModelMap model) throws Exception {

        final NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());

        int level = 0;
        nuxeoController.executeNuxeoCommand(new UpdateProfileCommand(request.getUserPrincipal().getName(), level));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setPortletConfig(PortletConfig portletConfig) {
        this.portletConfig = portletConfig;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setPortletContext(PortletContext portletContext) {
        this.portletContext = portletContext;
    }

}
