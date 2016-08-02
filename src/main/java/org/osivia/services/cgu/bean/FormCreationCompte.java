package fr.toutatice.cartoun.portail.creationCompte.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import fr.toutatice.entity.Degre;
import fr.toutatice.entity.Enseignant;

public class FormCreationCompte {

	private Utilisateur utilisateur;
	private boolean compteExistant = false;

	/** code discipline > discipline (contient toutes les disciplines) */
	private final Map<String, Discipline> mapDisciplines = new HashMap<String, Discipline>();

	public boolean isBesoinDuChoixDisciplines() {
		assert utilisateur != null;
		if (utilisateur.getEnseignant().isPresent()) {
			final Enseignant personnelType = utilisateur.getEnseignant().get();
			if (Degre.SECOND == personnelType.getDegre()) {
				return true;
			}
		}
		return false;
	}

	public Collection<Discipline> getToutesLesDisciplines() {
		final TreeSet<Discipline> treeSet = new TreeSet<Discipline>(new Discipline.TextComparator());
		treeSet.addAll(mapDisciplines.values());
		return treeSet;
	}

	public Map<String, Discipline> getMapDisciplines() {
		return mapDisciplines;
	}

	public boolean isCompteExistant() {
		return compteExistant;
	}

	public void setCompteExistant(boolean compteExistant) {
		this.compteExistant = compteExistant;
	}

	public Utilisateur getUtilisateur() {
		return utilisateur;
	}

	public void setUtilisateur(Utilisateur utilisateur) {
		this.utilisateur = utilisateur;
	}

}
