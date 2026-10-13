package mg.faneva.summermvc.scanner;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import mg.faneva.summermvc.annotation.Controller;
import mg.faneva.summermvc.annotation.Json;
import mg.faneva.summermvc.annotation.RequestMapping;
import mg.faneva.summermvc.mapping.Mapping;
import mg.faneva.summermvc.mapping.UrlMethod;

public class ControllerScanner {

    public List<Class<?>> scanControllers(String basePackage)
            throws Exception {

        if (basePackage == null || basePackage.trim().isEmpty()) {
            throw new Exception(
                "Init-param 'base-package' manquant dans web.xml. "
                + "Ajoutez : <context-param><param-name>base-package</param-name>"
                + "<param-value>mg.faneva.test.controller</param-value></context-param>");
        }

        List<Class<?>> controllers = new ArrayList<>();

        String path =
                basePackage.replace(".", "/");

        ClassLoader classLoader =
                Thread.currentThread()
                .getContextClassLoader();

        Enumeration<URL> resources =
                classLoader.getResources(path);

        while(resources.hasMoreElements()){

            URL resource =
                    resources.nextElement();

            File directory =
                    new File(resource.toURI());

            scanDirectory(directory, basePackage, controllers);
        }

        return controllers;
    }


    private void scanDirectory(
            File directory,
            String packageName,
            List<Class<?>> controllers)
            throws ClassNotFoundException {

        File[] files = directory.listFiles();

        if (files == null)
            return;

        for (File file : files) {

            if (file.isDirectory()) {
                scanDirectory(
                    file,
                    packageName + "." + file.getName(),
                    controllers);
            }
            else if (file.getName().endsWith(".class")) {

                String className =
                        packageName
                        + "."
                        + file.getName()
                        .replace(".class", "");

                Class<?> clazz =
                        Class.forName(className);

                if (clazz.isAnnotationPresent(
                        Controller.class)) {

                    controllers.add(clazz);
                }
            }
        }
    }

    public HashMap<UrlMethod, Mapping> scanMethods(
            List<Class<?>> controllers)
            throws Exception {

        HashMap<UrlMethod, Mapping> routes =
                new HashMap<>();


        for(Class<?> clazz : controllers){

            Method[] methods =
                    clazz.getDeclaredMethods();


            for(Method method : methods){

                /*
                 * Vérifie si la méthode possède
                 * @RequestMapping
                 */
                if(method.isAnnotationPresent(
                        RequestMapping.class)){

                    RequestMapping rm =
                            method.getAnnotation(
                                    RequestMapping.class);


                    /*
                     * Construction de la clé
                     * URL + méthode HTTP
                     */
                    UrlMethod key =
                            new UrlMethod(
                                    rm.value(),
                                    rm.method()
                            );


                    /*
                     * Vérification des doublons
                     */
                    if(routes.containsKey(key)){

                        throw new Exception(
                                "Route déjà utilisée : "
                                + rm.value()
                        );
                    }


                    /*
                     * Vérifie si la méthode possède
                     * @Json
                     */
                    boolean json =
                            method.isAnnotationPresent(
                                    Json.class
                            );


                    /*
                     * Création du Mapping
                     *
                     * Le Mapping contient maintenant :
                     * - le Controller
                     * - la méthode
                     * - si elle retourne du JSON
                     */
                    routes.put(
                            key,
                            new Mapping(
                                    clazz,
                                    method,
                                    json
                            )
                    );
                }
            }
        }

        return routes;
    }

}