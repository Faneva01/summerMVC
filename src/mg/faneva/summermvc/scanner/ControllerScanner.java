package mg.faneva.summermvc.scanner;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import mg.faneva.summermvc.annotation.Controller;
import mg.faneva.summermvc.annotation.RequestMapping;
import mg.faneva.summermvc.mapping.Mapping;
import mg.faneva.summermvc.mapping.UrlMethod;

public class ControllerScanner {

    public List<String> scanControllers(String basePackage)
            throws Exception {

        List<String> controllers = new ArrayList<>();

        String path = basePackage.replace(".", "/");

        ClassLoader cl =
                Thread.currentThread().getContextClassLoader();

        Enumeration<URL> resources =
                cl.getResources(path);

        while (resources.hasMoreElements()) {

            URL resource = resources.nextElement();

            File dir = new File(resource.toURI());

            String[] files = dir.list();

            if(files == null)
                continue;

            for(String file : files){

                if(file.endsWith(".class")){

                    String className =
                            basePackage + "."
                            + file.replace(".class","");

                    Class<?> clazz =
                            Class.forName(className);

                    if(clazz.isAnnotationPresent(
                            Controller.class)){

                        controllers.add(className);

                    }

                }

            }

        }

        return controllers;

    }

    public HashMap<UrlMethod, Mapping> scanMethods(
            List<String> controllers)
            throws Exception {

        HashMap<UrlMethod, Mapping> routes =
                new HashMap<>();

        for(String className : controllers){

            Class<?> clazz =
                    Class.forName(className);

            Method[] methods =
                    clazz.getDeclaredMethods();

            for(Method method : methods){

                if(method.isAnnotationPresent(
                        RequestMapping.class)){

                    RequestMapping rm =
                            method.getAnnotation(
                                    RequestMapping.class);

                    UrlMethod key =
                            new UrlMethod(
                                    rm.value(),
                                    rm.method());

                    if(routes.containsKey(key))
                        throw new Exception(
                                "Route déjà utilisée");

                    routes.put(
                            key,
                            new Mapping(
                                    className,
                                    method.getName()
                            )
                    );

                }

            }

        }

        return routes;

    }

}