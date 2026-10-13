package mg.faneva.summermvc.binding;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

public class DataBinder {

    /**
     * Prépare les arguments attendus par une méthode.
     */
    public Object[] bind(
            HttpServletRequest request,
            Method method) throws Exception {

        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {

            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();

            // Cas 1 : type simple
            if (isSimpleType(type)) {

                if (!parameter.isNamePresent()) {
                    throw new IllegalArgumentException(
                        "Nom du paramètre Java indisponible : "
                        + parameter.getName()
                        + ". Compilez avec l'option -parameters."
                    );
                }

                String name = parameter.getName();
                String value = request.getParameter(name);

                arguments[i] = convert(value, type);
            }

            // Cas 2 : objet Java
            else {
                arguments[i] = bindObject(request, type, "");
            }
        }

        return arguments;
    }

    /**
     * Détermine si un type peut être converti
     * directement depuis une valeur HTTP.
     */
    private boolean isSimpleType(Class<?> type) {

        return type == String.class
                || type == int.class
                || type == Integer.class
                || type == long.class
                || type == Long.class
                || type == double.class
                || type == Double.class
                || type == float.class
                || type == Float.class
                || type == short.class
                || type == Short.class
                || type == byte.class
                || type == Byte.class
                || type == boolean.class
                || type == Boolean.class
                || type == char.class
                || type == Character.class
                || type == BigDecimal.class
                || type == BigInteger.class
                || type.isEnum();
    }

    /**
     * Convertit une valeur HTTP vers le type Java attendu.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convert(
            String value,
            Class<?> type) {

        if (value == null || value.isEmpty()) {

            if (type.isPrimitive()) {
                throw new IllegalArgumentException(
                    "Paramètre obligatoire manquant pour le type : "
                    + type.getName()
                );
            }

            return null;
        }

        if (type == String.class) {
            return value;
        }

        if (type == int.class || type == Integer.class) {
            return Integer.valueOf(value);
        }

        if (type == long.class || type == Long.class) {
            return Long.valueOf(value);
        }

        if (type == double.class || type == Double.class) {
            return Double.valueOf(value);
        }

        if (type == float.class || type == Float.class) {
            return Float.valueOf(value);
        }

        if (type == short.class || type == Short.class) {
            return Short.valueOf(value);
        }

        if (type == byte.class || type == Byte.class) {
            return Byte.valueOf(value);
        }

        if (type == boolean.class || type == Boolean.class) {

            if (!value.equalsIgnoreCase("true")
                    && !value.equalsIgnoreCase("false")) {

                throw new IllegalArgumentException(
                    "Valeur booléenne invalide : " + value
                );
            }

            return Boolean.valueOf(value);
        }

        if (type == char.class || type == Character.class) {

            if (value.length() != 1) {
                throw new IllegalArgumentException(
                    "Un seul caractère est attendu : " + value
                );
            }

            return value.charAt(0);
        }

        if (type == BigDecimal.class) {
            return new BigDecimal(value);
        }

        if (type == BigInteger.class) {
            return new BigInteger(value);
        }

        if (type.isEnum()) {
            return Enum.valueOf(
                (Class<? extends Enum>) type,
                value
            );
        }

        throw new IllegalArgumentException(
            "Type simple non pris en charge : " + type.getName()
        );
    }

    /**
     * Construit un objet et remplit ses propriétés
     * à partir des paramètres de la requête.
     *
     * Exemple :
     * nom=Rakoto -> setNom("Rakoto")
     */
    private Object bindObject(
            HttpServletRequest request,
            Class<?> type,
            String prefix) throws Exception {

        Object object;

        try {
            object = type.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException e) {
            throw new IllegalArgumentException(
                "L'objet " + type.getName()
                + " doit avoir un constructeur sans argument accessible",
                e
            );
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(
                "Impossible de construire l'objet "
                + type.getName(),
                e.getCause()
            );
        }

        for (Method setter : type.getMethods()) {

            // Un setter doit commencer par set
            // et accepter exactement un argument.
            if (!setter.getName().startsWith("set")
                    || setter.getName().length() <= 3
                    || setter.getParameterCount() != 1) {
                continue;
            }

            String property = Character.toLowerCase(
                    setter.getName().charAt(3))
                    + setter.getName().substring(4);

            String propertyPath = prefix.isEmpty()
                    ? property
                    : prefix + "." + property;
            String value = request.getParameter(propertyPath);

            // Si le champ n'a pas été envoyé,
            // on conserve la valeur par défaut.
            Class<?> propertyType =
                    setter.getParameterTypes()[0];

            if (isSimpleType(propertyType)) {
                // Un champ absent conserve la valeur par défaut du modèle.
                if (value == null) {
                    continue;
                }

                Object convertedValue;
                try {
                    convertedValue = convert(value, propertyType);
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException(
                        "Valeur invalide pour la propriété '"
                        + propertyPath + "' : " + value,
                        e
                    );
                }

                try {
                    setter.invoke(object, convertedValue);
                } catch (InvocationTargetException e) {
                    throw new IllegalArgumentException(
                        "Erreur lors du remplissage de la propriété : "
                        + propertyPath,
                        e.getCause()
                    );
                }
                continue;
            }

            // Un objet imbriqué reste null si aucun de ses champs n'est
            // présent. Les préfixes pointés permettent plusieurs niveaux.
            if (hasNestedParameters(request, propertyPath)) {
                Object nestedObject = bindObject(
                        request,
                        propertyType,
                        propertyPath
                );
                try {
                    setter.invoke(object, nestedObject);
                } catch (InvocationTargetException e) {
                    throw new IllegalArgumentException(
                        "Erreur lors du remplissage de la propriété : "
                        + propertyPath,
                        e.getCause()
                    );
                }
            }
        }

        return object;
    }

    private boolean hasNestedParameters(
            HttpServletRequest request,
            String prefix) {
        String nestedPrefix = prefix + ".";
        for (Map.Entry<String, String[]> parameter
                : request.getParameterMap().entrySet()) {
            if (parameter.getKey().startsWith(nestedPrefix)) {
                return true;
            }
        }
        return false;
    }
}
