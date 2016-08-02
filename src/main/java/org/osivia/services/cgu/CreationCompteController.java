/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * Contributors:
 * aguihomat
 */
package org.osivia.services.cgu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.client.validation.Assertion;
import org.nuxeo.ecm.automation.client.model.Document;
import org.osivia.portal.api.Constants;
import org.osivia.portal.api.context.PortalControllerContext;
import org.osivia.portal.api.notifications.NotificationsType;
import org.osivia.portal.core.cms.CMSException;
import org.osivia.services.cgu.bean.Discipline;
import org.osivia.services.cgu.bean.FormCreationCompte;
import org.osivia.services.cgu.bean.Inspecteur;
import org.osivia.services.cgu.bean.Utilisateur;
import org.osivia.services.cgu.bean.VecteurIdentite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.context.PortletConfigAware;
import org.springframework.web.portlet.context.PortletContextAware;
import com.google.common.collect.Lists;
import fr.toutatice.entity.Degre;
import fr.toutatice.entity.Enseignant;
import fr.toutatice.entity.PriveOuPublic;
import fr.toutatice.outils.ldap.entity.Person;
import fr.toutatice.outils.ldap.entity.Profil;
import fr.toutatice.outils.ldap.exception.ToutaticeAnnuaireException;
import fr.toutatice.portail.cms.nuxeo.api.CMSPortlet;
import fr.toutatice.portail.cms.nuxeo.api.NuxeoController;

@Controller
@RequestMapping("VIEW")
@SessionAttributes("formCreationCompte")
public class CreationCompteController extends CMSPortlet implements PortletContextAware, PortletConfigAware {

	private static final String DISCIPLINES_TIMESTAMP = "mesDisciplinesTimestamp";
	private static final String FORM_NAME = "formCreationCompte";
	private static final String URL_NUXEO_CGUS = "/default-domain/documentation/juridique/conditions-generales-d";
	/** nom de domaine de la racine su le LDAP */
	private static final String LDAP_CHEMIN_PROFILS = ",ou=profils,ou=groupes,ou=cartoun,ou=education,o=gouv,c=fr";
	protected static final String PREFIX_PROFIL_LDAP = "cn=";
	protected static final String CAS_ATTRIBUTE = "edu.yale.its.tp.cas.client.filter.response";
	protected static final Logger logger = LoggerFactory.getLogger(CreationCompteController.class);
	protected static final Logger loggerPourDisciplineNonTrouvee = LoggerFactory.getLogger("loggerPourDisciplineNonTrouvee");
	private static final List<String> ADMIN_PROFILS_LIST = Lists.newArrayList("cn=powerusers,ou=profils,ou=groupes,ou=cartoun,ou=education,o=gouv,c=fr", "cn=Administrators,ou=profils,ou=groupes,ou=cartoun,ou=education,o=gouv,c=fr");

	public static final String CONST_CAS_ASSERTION = "_const_cas_assertion_";

	@Autowired
	private Person personneInstance;

	@Autowired
	private Profil profilInstance;

	private PortletContext portletContext;
	private PortletConfig portletConfig;

	/** pour parser la réponse de l'authentification du CAS */
	private final CasResponseToUtilisateur casResponseToUtilisateur;

	public CreationCompteController() throws JAXBException {
		super();
		casResponseToUtilisateur = new CasResponseToUtilisateur();
	}

	@PostConstruct
	public void initNuxeoService() throws Exception {
		super.init();
		if ((portletContext != null) && (portletContext.getAttribute("nuxeoService") == null)) {
			logger.info(" Start  nuxeo service ...");
			this.init(portletConfig);
			logger.info("Nuxeo service  started! ");
		}

	}

	@PreDestroy
	public void cleanUpNuxeoService() throws Exception {
		if ((portletContext != null) && (portletContext.getAttribute("nuxeoService") == null)) {
			logger.info(" Stop  nuxeo service ...");
			destroy();
			logger.info("Nuxeo service  stopped! ");
		}
	}

	@ModelAttribute(FORM_NAME)
	public FormCreationCompte insertFormInSession(final PortletRequest request) throws Exception {
		final FormCreationCompte formulaire = new FormCreationCompte();
		final Utilisateur utilisateur = new Utilisateur();
		formulaire.setUtilisateur(utilisateur);
		utilisateur.setUid(request.getUserPrincipal().getName());

		final HttpServletRequest httpRequest = (HttpServletRequest) request.getAttribute(Constants.PORTLET_ATTR_HTTP_REQUEST);
		final Assertion assertion = (Assertion) httpRequest.getSession().getAttribute(CONST_CAS_ASSERTION);
		CasResponseToUtilisateur.remplirUtilisateur(utilisateur, assertion);

		final Person user = personneInstance.findUtilisateur(utilisateur.getUid());


		if (user == null) {
			fillDisciplines(formulaire);
			formulaire.setCompteExistant(false);
		} else {
			formulaire.setCompteExistant(true);
			utilisateur.setAdmin(CollectionUtils.containsAny(ADMIN_PROFILS_LIST, user.getListeProfils()));
			if (!utilisateur.isAdmin()) {
				fillDisciplines(formulaire);
				// Récupération des matières :
				final Set<String> matieresTrouvees = extraireDisciplines(user, formulaire);
				utilisateur.setListeDisciplines(matieresTrouvees);
			}
		}

		return formulaire;
	}


	/**
	 * Récupère les disciplines qui font parties du profil de l'utilisateur
	 *
	 * @param user
	 *            utilisateur courant
	 * @param form
	 *            contient les matières existantes.
	 * @return Set des codes des disciplines sélectionnées par l'utilisateur
	 */
	private Set<String> extraireDisciplines(final Person user, FormCreationCompte form) {
		final Set<String> matieresTrouvees = new HashSet<String>();
		for (final String profil : user.getListeProfils()) {
			if (profil.startsWith(PREFIX_PROFIL_LDAP + user.getCodeAcademie())) {
				final String codeMatiereSupposee = extraireDisciplineDepuisLdap(profil);

				// Est-ce que la matière est dans la liste des matières connues ?
				if (form.getMapDisciplines().containsKey(codeMatiereSupposee)) {
					matieresTrouvees.add(codeMatiereSupposee);
				}
			}
		}
		return matieresTrouvees;
	}

	/**
	 * Extrait le code de la discpline depuis le champ LDAP identifiant du profil
	 *
	 * @param nomProfilLdap
	 *            de la forme cn=014_art-plastique_ensprive
	 * @return la discipline
	 */
	private String extraireDisciplineDepuisLdap(final String nomProfilLdap) {
		final String[] splitted = StringUtils.split(nomProfilLdap, "_", 3);
		final String codeMatiereSupposee = splitted[1];
		return codeMatiereSupposee;
	}

	@RenderMapping
	public String accueil(@ModelAttribute(FORM_NAME) FormCreationCompte formulaire, final RenderRequest request, final RenderResponse response) {
		if (formulaire.isCompteExistant()) {
			// user déjà dans ldap. MAJ des infos si besoin, et possibilité de modifier les choix de discipline
			if (formulaire.isBesoinDuChoixDisciplines()) {
				return "modification/01_choixDisciplines";
			} else {
				return "modification/01_culDeSac";
			}
		} else {
			// interface de création du compte
			return "01_accueil";
		}
	}

	@RenderMapping(params = "action=montrerChoixDisciplines")
	public String montrerChoixDisciplines() {
		return "02_choixDisciplines";

	}

	@RenderMapping(params = "action=montrerChoixDisciplinesEnModification")
	public String montrerChoixDisciplinesEnModification() {
		return "modification/01_choixDisciplines";
	}

	@ActionMapping(value = "suiteAccueil")
	public void suiteAccueil(@ModelAttribute(FORM_NAME) FormCreationCompte formulaire, final ActionRequest request, final ActionResponse response)
			throws CMSException {
		if (formulaire.isBesoinDuChoixDisciplines()) {
			response.setRenderParameter("action", "montrerChoixDisciplines");
		} else {
			response.setRenderParameter("action", "montrerCgus");
		}
	}

	@RenderMapping(params = "action=montrerCgus")
	public String montrerCgus(@ModelAttribute(FORM_NAME) FormCreationCompte formulaire, final RenderRequest request, final RenderResponse response)
			throws CMSException {
		// Charger Cgus

		final NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
		nuxeoController.setScope("anonymous");
		final Document document = nuxeoController.fetchDocument(URL_NUXEO_CGUS);
		nuxeoController.setCurrentDoc(document);
		final String note = nuxeoController.transformHTMLContent((String) document.getProperties().get("note:note"));
		request.setAttribute("cgus", note);

		return "03_cgus";
	}

	@RenderMapping(params = "action=validationCgus")
	public String showValidationCreationCompte(@ModelAttribute(FORM_NAME) final FormCreationCompte form, final RenderRequest request, final RenderResponse response) {
		final List<String> intitulesDisciplinesChoisies = new ArrayList<String>();
		for (final String codeDiscipline : form.getUtilisateur().getListeDisciplines()) {
			intitulesDisciplinesChoisies.add(form.getMapDisciplines().get(codeDiscipline).getText());
		}
		request.setAttribute("intitulesDisciplinesChoisies", intitulesDisciplinesChoisies);
		if (form.isCompteExistant()) {
			return "modification/02_recapitulatif";
		}
		return "04_recapitulatif";
	}

	@RenderMapping(params = "action=compteCree")
	public String showConfirmationCreationCompte(final RenderRequest request, final RenderResponse response) {
		// Génération de l'URL de redirection

		final NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
		final PortalControllerContext portalControllerContext = nuxeoController.getPortalCtx();
		String redirectionURL = getPortalUrlFactory().getBasePortalUrl(portalControllerContext) + "/portail";
		redirectionURL = redirectionURL + "/signout?location=" + redirectionURL;

		request.setAttribute("redirectUrl", redirectionURL);

		return "05_compteCree";
	}

	@ActionMapping(value = "validationChoixDisciplines")
	public void validationChoixDisciplines(@ModelAttribute(FORM_NAME) final FormCreationCompte form, final BindingResult bindingResult,
			final ActionResponse response,
			final ActionRequest request)
			throws CMSException {
		if (CollectionUtils.isEmpty(form.getUtilisateur().getListeDisciplines())) {
			bindingResult.rejectValue("utilisateur.listeDisciplines", "erreur.disciplines.vide");
			if (form.isCompteExistant()) {
				response.setRenderParameter("action", "montrerChoixDisciplinesEnModification");
			} else {
				response.setRenderParameter("action", "montrerChoixDisciplines");
			}
		} else {
			if (form.isCompteExistant()) {
				response.setRenderParameter("action", "validationCgus");
			} else {
				response.setRenderParameter("action", "montrerCgus");
			}
		}

	}

	@RenderMapping(params = "action=afficherCgus")
	public String afficherCgus(@ModelAttribute(FORM_NAME) final FormCreationCompte form, final BindingResult bindingResult, final ActionResponse response,
			final ActionRequest request)
			throws CMSException {
		return "03_cgus";

	}

	@RenderMapping(params = "action=succesModification")
	public String succesModification() {
		return "modification/03_succes";
	}

	/**
	 * Dans le cas de la création ou de la mise à jour du compte
	 */
	@ActionMapping(value = "creationCompte")
	public void creationCompte(@ModelAttribute(FORM_NAME) final FormCreationCompte form, final PortletSession session,
			final ModelMap model,
			final ActionResponse response, final ActionRequest request) {

		try {
			if (!form.isCompteExistant()) {
				creationDuCompte(form);
			}
			final Person user = personneInstance.findUtilisateur(form.getUtilisateur().getUid());
			if (user == null) {
				// Le compte n'a pas été créé
				response.setRenderParameter("action", "validationCreationCompte");
				final PortalControllerContext pcc = new PortalControllerContext(portletContext, request, response);
				addNotification(pcc, "label.erreurCreation", NotificationsType.ERROR);
				logger.error("l'utilisateur " + form.getUtilisateur().getUid() + " n'a pas été créé dans l'annuaire.");
			} else {
				logger.info("l'utilisateur " + user.getUid() + " a été récupéré depuis le LDAP de cartounN.");
				mettreAJourEtRecuperProfils(form, user);

				user.update();

				// Tout s'est bien passé
				session.removeAttribute(FORM_NAME);
				model.remove(FORM_NAME);
				request.getPortletSession().setAttribute(DISCIPLINES_TIMESTAMP, new Date().getTime());
			}
			if (form.isCompteExistant()) {
				// Invalidation du cache Nuxeo :
				final NuxeoController nuxeoController = new NuxeoController(request, response, getPortletContext());
				nuxeoController.executeNuxeoCommand(new RefreshCommandNuxeo());

				response.setRenderParameter("action", "succesModification");
			} else {
				response.setRenderParameter("action", "compteCree");
			}

		} catch (final Exception e) {
			logger.info(e.toString(), e);
			final PortalControllerContext pcc = new PortalControllerContext(portletContext, request, response);
			addNotification(pcc, "label.erreurCreation", NotificationsType.ERROR);
			response.setRenderParameter("action", "validationCreationCompte");
			return;
		}

	}

	private void mettreAJourEtRecuperProfils(final FormCreationCompte form, final Person user) {
		// title de la forme : ENS/IPR/IAN
		String title = form.getUtilisateur().getVecteurIdentite().getTitle();
		final String codeAcademie = form.getUtilisateur().getVecteurIdentite().getCodeAcademie();
		if (form.getUtilisateur().getEnseignant().isPresent()) {
			final Enseignant enseignant = form.getUtilisateur().getEnseignant().get();
			if (!(enseignant instanceof Inspecteur)) {
				// pas un inspecteur > c'est un enseignant
				// on remplace le Title pour les enseignants(Ens) public/privé :
				title = enseignant.getPriveOuPublic() == PriveOuPublic.PRIVE ? "EnsPrive" : "EnsPublic";
				ajoutMacroProfil(user, codeAcademie, "_Ens");

			} else if (enseignant instanceof Inspecteur) {
				if (enseignant.getDegre() == Degre.SECOND) {
					// Uniquement pour les inspecteurs du 2nd degrés :
					title = "ipr";
					ajoutMacroProfil(user, codeAcademie, "_IPR");
				} else {
					// 1er degré
					ajoutMacroProfil(user, codeAcademie, "_IEN");
				}
			}
			if (enseignant.getDegre() == Degre.SECOND) {
				// Mise à jour des profils, uniquement pour le Second degré :
				logger.info("Récupération des profils de l'utilisateur...");
				final Set<String> disciplinesQuiSerontSupprimees = extraireDisciplines(user, form);
				// Ajout des profils :
				for (final String discipline : form.getUtilisateur().getListeDisciplines()) {
					disciplinesQuiSerontSupprimees.remove(discipline);
					final String cnProfil = construireNomProfilLdap(discipline, codeAcademie, title);
					logger.info("Ajout du profil: " + cnProfil + " pour l'utilisateur: " + form.getUtilisateur().getUid());
					user.addProfil(cnProfil);
				}

				// Suppression des disciplines :
				for (final String discipline : disciplinesQuiSerontSupprimees) {
					final String cnProfil = construireNomProfilLdap(discipline, codeAcademie, title);
					logger.info("Suppression du profil: " + cnProfil + " pour l'utilisateur: " + form.getUtilisateur().getUid());
					user.removeProfil(cnProfil);
				}
			}
		} else {
			// TODO : Que faire si la personne n'est pas reconnue comme étant un Personnel ?
		}
	}

	private void fillDisciplines(FormCreationCompte formulaire) {
		Assert.hasLength(formulaire.getUtilisateur().getVecteurIdentite().getCodeAcademie(), "le code académie est absent !");
		final String codeDisciplineParDefaut = formulaire.getUtilisateur().getVecteurIdentite().getCodeDisciplineParDefaut();

		// Récupération de la liste des disciplines de l'académie concernée :
		final List<Profil> profils = profilInstance.findProfilByFiltre(PREFIX_PROFIL_LDAP + formulaire.getUtilisateur().getVecteurIdentite().getCodeAcademie() + "_*");
		for (final Profil profil : profils) {
			if (profil.isMacroProfil()) {
				continue;
			}
			final String disciplineCode = extraireDisciplineDepuisLdap(profil.getCn());
			final Discipline discipline = new Discipline(disciplineCode, profil.getDisplayName());
			formulaire.getMapDisciplines().put(disciplineCode, discipline);
			if (profil.getCartounCodesDiscipline().contains(codeDisciplineParDefaut)) {
				formulaire.getUtilisateur().getListeDisciplines().add(disciplineCode);
			}
		}
		logger.debug("Nb disciplines retrouvées : " + formulaire.getMapDisciplines().size());
		if (formulaire.getUtilisateur().getListeDisciplines().isEmpty()) {
			loggerPourDisciplineNonTrouvee.warn("Code discipline non trouvé : " + codeDisciplineParDefaut + " - Utilisateur:" + formulaire.getUtilisateur().getUid() + " title:" + formulaire.getUtilisateur().getVecteurIdentite().getTitle());
		}
	}

	/***
	 * Ajout si besoin le macro profil sur l'utilisateur
	 *
	 * @param user
	 *            utilisateur
	 * @param codeAcademie
	 *            code de l'académie
	 * @param enseignantOuIpr
	 *            "_ens" ou "_ipr"
	 */
	private void ajoutMacroProfil(Person user, String codeAcademie, String enseignantOuIpr) {
		final String macroProfil = PREFIX_PROFIL_LDAP + codeAcademie + enseignantOuIpr + LDAP_CHEMIN_PROFILS;
		// Ajout du macro profil :
		if (!user.hasProfil(macroProfil)) {
			user.addProfil(macroProfil);
		}
	}

	private void creationDuCompte(final FormCreationCompte form) throws ToutaticeAnnuaireException {
		final Utilisateur utilisateur = form.getUtilisateur();
		final VecteurIdentite attrs = utilisateur.getVecteurIdentite();
		final Person person = personneInstance.getNewInstance();
		person.setSn(attrs.getNom());
		person.setAlias(attrs.getPrenom());
		person.setDisplayName(attrs.getPrenom()+ " "+ attrs.getNom());
		person.setGivenName(attrs.getPrenom());
		person.setUid(utilisateur.getUid());
		person.setCn(attrs.getNom() + " " + attrs.getPrenom());
		person.setEmail(attrs.getEmail());
		person.setPersonalTitle(attrs.getCodeCivilite());
		person.setTitle(attrs.getTitle());
		person.setTypeNsi(attrs.getTypeNsi());
		person.setCodeAcademie(attrs.getCodeAcademie());

		person.setListeRnes(Lists.newArrayList(attrs.getListeRne()));
		person.setListeDisciplinePoste(Arrays.asList(attrs.getCodeDisciplineParDefaut()));

		person.createEnseignant();

		// Après la création du compte on supprime l'entrée du cache afin de forcer une nouvelle interrogation de l'annuaire
		person.evictPersonnFromCache(utilisateur.getUid());
	}

	private String construireNomProfilLdap(final String discipline, final String codeAcademie, String title) {
		// Le code académie est forcément sur 3 caractères
		final String codeAca = StringUtils.leftPad(codeAcademie, 3, '0');
		return PREFIX_PROFIL_LDAP + codeAca + '_' + discipline + '_' + title.toLowerCase() + LDAP_CHEMIN_PROFILS;
	}

	public void setPortletConfig(final PortletConfig portletConfig) {
		this.portletConfig = portletConfig;
	}

	public void setPortletContext(final PortletContext portletContext) {
		this.portletContext = portletContext;
	}

	public void setPersonneInstance(Person personneInstance) {
		this.personneInstance = personneInstance;
	}

	public Profil getProfilInstance() {
		return profilInstance;
	}

	public void setProfilInstance(Profil profilInstance) {
		this.profilInstance = profilInstance;
	}
}
