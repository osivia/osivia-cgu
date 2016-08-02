package fr.toutatice.cartoun.portail.creationCompte.bean;

import java.util.HashSet;
import java.util.Set;

/**
 * Pour l'instant ne sert qu'à stocker les informations telles qu'elles devraient arriver depuis le vecteur d'identité
 */
public class VecteurIdentite {

	@CasKey("sn")
	private String nom;

	@CasKey("givenName")
	private String prenom;

	@CasKey("ENTCodeAcademie")
	private String codeAcademie;

	@CasKey("mail")
	private String email;

	@CasKey("title")
	private String title;

	@CasKey("personalTitle")
	private String codeCivilite;

	/** avec ce code on peut en déduire si c'est un membre du privé ou du public */
	@CasKey("typensi")
	private String typeNsi;

	/** permet de savoir si c'est un inspecteur du 1er degré ou 2eme degré : [X=pour un inspecteur du 1er degré], [IEN1D= inspecteur du 1er degré] */
	@CasKey("FrEduFonctAdm")
	private String frEduFonctAdm;

	/** C'est le code de la discipline principale de l'utilisateur. Le code devra ensuite être retrouvé dans les disciplines qui sont dans le LDAP */
	@CasKey("ENTAuxDisciplinePoste")
	private String codeDisciplineParDefaut;

	/** liste des établissements de rattachement */
	private Set<String> listeRne = new HashSet<String>();

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getCodeAcademie() {
		return codeAcademie;
	}

	public void setCodeAcademie(String codeAcademie) {
		this.codeAcademie = codeAcademie;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCodeCivilite() {
		return codeCivilite;
	}

	public void setCodeCivilite(String codeCivilite) {
		this.codeCivilite = codeCivilite;
	}

	public String getTypeNsi() {
		return typeNsi;
	}

	public void setTypeNsi(String codensi) {
		typeNsi = codensi;
	}

	public String getFrEduFonctAdm() {
		return frEduFonctAdm;
	}

	public void setFrEduFonctAdm(String frEduFonctAdm) {
		this.frEduFonctAdm = frEduFonctAdm;
	}

	public Set<String> getListeRne() {
		return listeRne;
	}

	public void setListeRne(Set<String> listeRne) {
		this.listeRne = listeRne;
	}

	@Override
	public String toString() {
		return "VecteurIdentite [ nom=" + nom + ", prenom=" + prenom + ", codeAcademie=" + codeAcademie + ", email=" + email + ", title=" + title + ", codeCivilite=" + codeCivilite + ", codensi=" + typeNsi + ", frEduFonctAdm=" + frEduFonctAdm + ", listeRne=" + listeRne + "]";
	}

	public String getCodeDisciplineParDefaut() {
		return codeDisciplineParDefaut;
	}

	public void setCodeDisciplineParDefaut(String codeDisciplineParDefaut) {
		this.codeDisciplineParDefaut = codeDisciplineParDefaut;
	}

}
