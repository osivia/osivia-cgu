package fr.toutatice.cartoun.portail.creationCompte.bean;

import java.util.HashSet;
import java.util.Set;
import com.google.common.base.Optional;
import fr.toutatice.entity.Enseignant;

public class Utilisateur {

	private String uid = "";
	private boolean admin = false;

	private Set<String> listeDisciplines = new HashSet<String>();
	private Optional<? extends Enseignant> enseignant;
	private VecteurIdentite vecteurIdentite;

	public String getUid() {
		return uid;
	}

	public void setUid(final String uidCartoun) {
		uid = uidCartoun;
	}


	public Optional<? extends Enseignant> getEnseignant() {
		return enseignant;
	}

	public void setEnseignant(Optional<? extends Enseignant> personnelType) {
		enseignant = personnelType;
	}


	public Set<String> getListeDisciplines() {
		return listeDisciplines;
	}

	public void setListeDisciplines(Set<String> listeDisciplines) {
		this.listeDisciplines = listeDisciplines;
	}

	public VecteurIdentite getVecteurIdentite() {
		return vecteurIdentite;
	}

	public void setVecteurIdentite(VecteurIdentite vecteurIdentite) {
		this.vecteurIdentite = vecteurIdentite;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

}
