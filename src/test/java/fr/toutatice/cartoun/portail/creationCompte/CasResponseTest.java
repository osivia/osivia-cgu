package fr.toutatice.cartoun.portail.creationCompte;

import java.util.Collection;
import java.util.HashMap;
import org.apache.commons.collections.CollectionUtils;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.AssertionImpl;
import org.junit.Assert;
import org.junit.Test;
import org.osivia.services.cgu.CasResponseToUtilisateur.CasResponseException;
import org.osivia.services.cgu.bean.Utilisateur;

import com.google.common.collect.Lists;

public class CasResponseTest {


	@Test
	public void testRemplirUtilisateur() throws CasResponseException {
		final Utilisateur utilisateur = new Utilisateur();
		final HashMap<String, Object> attributs = new HashMap<String, Object>();

		final AttributePrincipalImpl attributePrincipalImpl = new AttributePrincipalImpl("toto", attributs);
		final AssertionImpl assertionImpl = new AssertionImpl(attributePrincipalImpl);
		attributs.put("sn", "nom");
		attributs.put("givenName", "prenom");
		CasResponseToUtilisateur.remplirUtilisateur(utilisateur, assertionImpl);

		Assert.assertEquals("prenom", utilisateur.getVecteurIdentite().getPrenom());
		Assert.assertEquals("nom", utilisateur.getVecteurIdentite().getNom());
	}

	@Test
	public void testFrEduRne() throws CasResponseException {
		final Utilisateur utilisateur = new Utilisateur();
		final HashMap<String, Object> attributs = new HashMap<String, Object>();

		final AttributePrincipalImpl attributePrincipalImpl = new AttributePrincipalImpl("toto", attributs);
		final AssertionImpl assertionImpl = new AssertionImpl(attributePrincipalImpl);
		attributs.put("FrEduRne", "0291101P$UAJ$PU$ENS$0291101P$T3$CLG$340;0291591X$UAJ$PU$ENS$0291591X$T3$CLG$340;0290072W$UAJ$PU$ENS$0290072W$T3$LP$320;0290070U$UAJ$PU$ENS$0290070U$T3$LYC$300");
		CasResponseToUtilisateur.remplirUtilisateur(utilisateur, assertionImpl);

		Assert.assertEquals(4, utilisateur.getVecteurIdentite().getListeRne().size());
		final Collection<String> expected = Lists.asList("0291101P", new String[] { "0291591X", "0290072W", "0290070U" });
		Assert.assertTrue(CollectionUtils.isEqualCollection(expected, utilisateur.getVecteurIdentite().getListeRne()));
	}


}
