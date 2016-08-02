package fr.toutatice.cartoun.portail.creationCompte.bean;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Un discipline possède un code de discipline unique qui est trouvé dans le Ldap et l'intitulé de la discipline à afficher à l'utilisateur.
 *
 * @author vcaron
 */
public class Discipline {
	/** clé unique identifiant la discipline */
	private final String key;
	/** intitulé de la discipline à afficher à l'utilisateur */
	private final String text;

	/**
	 * Constructeur
	 *
	 * @param key
	 *            clé unique identifiant la discipline
	 * @param text
	 *            intitulé de la discipline à afficher à l'utilisateur
	 */
	public Discipline(String key, String text) {
		super();
		this.key = key;
		this.text = text;
	}

	/**
	 * Permet de comparer une Discipline suivant son text
	 */
	public static class TextComparator implements Comparator<Discipline> {
		/** pour faire un tri en fonction de la langue (et classer dans l'ordre suivant : 'e','é','f') */
		private final Collator collator = Collator.getInstance(Locale.FRENCH);

		public int compare(Discipline o1, Discipline o2) {
			return collator.compare(o1.text, o2.text);
		}
	}

	public String getKey() {
		return key;
	}

	public String getText() {
		return text;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((key == null) ? 0 : key.hashCode());
		result = (prime * result) + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Discipline other = (Discipline) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		if (text == null) {
			if (other.text != null) {
				return false;
			}
		} else if (!text.equals(other.text)) {
			return false;
		}
		return true;
	}

}
