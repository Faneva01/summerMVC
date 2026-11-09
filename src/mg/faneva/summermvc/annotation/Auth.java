package mg.faneva.summermvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auth {

    /*
     * SPRINT 10 BIS
     *
     * Rôles autorisés à exécuter la méthode.
     * Vide : tout utilisateur authentifié.
     */
    String[] roles() default {};
}
