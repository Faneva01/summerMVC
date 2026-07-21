package mg.faneva.summermvc.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;


import mg.faneva.summermvc.mapping.Mapping;
import mg.faneva.summermvc.mapping.UrlMethod;
import mg.faneva.summermvc.scanner.ControllerScanner;

public class FrameworkContextListener implements ServletContextListener {


    private Map<Class<?>, Object> beans =
            new HashMap<>();


    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {

            ServletContext context =
                    sce.getServletContext();


            String basePackage =
                    context.getInitParameter(
                            "base-package"
                    );


            ControllerScanner scanner =
                    new ControllerScanner();


            // Scanner les contrôleurs
            List<Class<?>> controllers =
                    scanner.scanControllers(
                            basePackage
                    );


            // Scanner les routes
            HashMap<UrlMethod, Mapping> routes =
                    scanner.scanMethods(
                            controllers
                    );



            /*
             * Récupération du contexte Spring
             */
            WebApplicationContext springContext =
                    WebApplicationContextUtils
                            .getRequiredWebApplicationContext(
                                    context
                            );



            /*
             * Récupération du BeanFactory Spring
             */
            AutowireCapableBeanFactory beanFactory =
                    springContext
                            .getAutowireCapableBeanFactory();



            /*
             * Création des contrôleurs
             * et injection des dépendances Spring
             */
            for(Class<?> clazz : controllers){


                Object controller =
                        clazz.getDeclaredConstructor()
                             .newInstance();


                beanFactory.autowireBean(
                        controller
                );


                beans.put(
                        clazz,
                        controller
                );

            }



            /*
             * Stockage dans le ServletContext
             */
            context.setAttribute(
                    "routes",
                    routes
            );


            context.setAttribute(
                    "beans",
                    beans
            );


            System.out.println(
                    "===== SummerMVC Started ====="
            );


        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }



    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}