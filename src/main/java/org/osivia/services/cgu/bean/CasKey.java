package fr.toutatice.cartoun.portail.creationCompte.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sert à marquer un membre comme étant une clé à retrouver dans la Map fournie par CAS.
 *
 * @author vcaron
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CasKey {
	public String value();
}
