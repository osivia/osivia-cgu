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
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.directory.v2.model.preferences.UserPreferences;
import org.osivia.directory.v2.service.preferences.UserPreferencesService;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.PortalException;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.directory.v2.DirServiceFactory;
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

import java.io.IOException;

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
    /** User preferences service. */
    private final UserPreferencesService userPreferencesService;


    /**
     * Constructor.
     */
    public ViewController() {
        super();

        // Portal URL factory
        this.portalUrlFactory = Locator.findMBean(IPortalUrlFactory.class, IPortalUrlFactory.MBEAN_NAME);
        // User preferences service
        this.userPreferencesService = DirServiceFactory.getService(UserPreferencesService.class);
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
    public void validateCgu(ActionRequest request, ActionResponse response, @ModelAttribute FormAdmin formulaire) throws PortletException, IOException {
        // Portal controller context
        PortalControllerContext portalControllerContext = new PortalControllerContext(this.getPortletContext(), request, response);
        // Nuxeo controller
        NuxeoController nuxeoController = new NuxeoController(portalControllerContext);
        
        // Window
        PortalWindow window = WindowFactory.getWindow(request);

        // Servlet request
        HttpServletRequest httpServletRequest = portalControllerContext.getHttpServletRequest();
        // HTTP session
        HttpSession session = httpServletRequest.getSession();


        // Terms of service
        String termsOfService = window.getProperty("osivia.services.cgu.level");
        
        // User preferences
        UserPreferences userPreferences;
        try {
            userPreferences = this.userPreferencesService.getUserPreferences(portalControllerContext);
        } catch (PortalException e) {
            throw new PortletException(e);
        }
        
        // Update user preferences
        userPreferences.setTermsOfService(termsOfService);
        userPreferences.setUpdated(true);
        
        
        // Get redirect url from session
        String redirectUrl = (String) session.getAttribute("osivia.services.cgu.pathToRedirect");

        if( redirectUrl != null)    {
            // Close URL
            String closeURL;
            try {
                closeURL = portalUrlFactory.getDestroyCurrentPageUrl(portalControllerContext, redirectUrl);
            } catch (PortalException e) {
                throw new PortletException(e);
            }
            response.sendRedirect(closeURL);
        }
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
