package mg.faneva.summermvc.listener;

import java.util.HashMap;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import mg.faneva.summermvc.mapping.Mapping;
import mg.faneva.summermvc.mapping.UrlMethod;
import mg.faneva.summermvc.scanner.ControllerScanner;

public class FrameworkContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {

            ServletContext context = sce.getServletContext();

            String basePackage =
                    context.getInitParameter("base-package");

            ControllerScanner scanner =
                    new ControllerScanner();

            List<String> controllers =
                    scanner.scanControllers(basePackage);

            HashMap<UrlMethod, Mapping> routes =
                    scanner.scanMethods(controllers);

            context.setAttribute("routes", routes);

            System.out.println("===== SummerMVC Started =====");

        } catch (Exception e) {

            throw new RuntimeException(e);

        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}