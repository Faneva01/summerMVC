package mg.faneva.summermvc.listener;import java.util.HashMap;
import java.util.List;
import java.util.Map;import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;import jakarta.servlet.ServletContext;
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
                ServletContext context = sce.getServletContext();
                String basePackage = context.getInitParameter( "base-package" );

                ControllerScanner scanner = new ControllerScanner();
                
                // Scanner les contrôleurs
                List<Class<?>> controllers = scanner.scanControllers( basePackage );
                
                // Scanner les routes
                HashMap<UrlMethod, Mapping> routes = scanner.scanMethods( controllers  );            
            
                // SPRINT 10
                // Nom de la variable de session qui prouve l'authentification
                String authVariable = context.getInitParameter( "auth-variable" );
                if(authVariable != null){
                        authVariable = authVariable.trim();
                }

                // Une route @Auth sans variable configurée ne peut pas être protégée
                for(Mapping mapping : routes.values()){
                        if(mapping.isAuth()
                                && (authVariable == null || authVariable.isEmpty())){
                                throw new Exception(
                                        "Init-param 'auth-variable' manquant dans web.xml. "
                                        + "Il est obligatoire pour la méthode @Auth : "
                                        + mapping.getController().getName()
                                        + "." + mapping.getMethod().getName());
                        }
                }

                // SPRINT 10 BIS
                // Nom de la variable de session qui contient le rôle de l'utilisateur
                String roleVariable = context.getInitParameter( "role-variable" );
                if(roleVariable != null){
                        roleVariable = roleVariable.trim();
                }

                // Une route @Auth(roles) sans variable configurée ne peut pas être contrôlée
                for(Mapping mapping : routes.values()){
                        if(mapping.getRoles().length > 0
                                && (roleVariable == null || roleVariable.isEmpty())){
                                throw new Exception(
                                        "Init-param 'role-variable' manquant dans web.xml. "
                                        + "Il est obligatoire pour la méthode @Auth(roles) : "
                                        + mapping.getController().getName()
                                        + "." + mapping.getMethod().getName());
                        }
                }

                //Récupération du contexte Spring
             
                WebApplicationContext springContext = WebApplicationContextUtils.getRequiredWebApplicationContext( context );
                // Récupération du BeanFactory Spring

                AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();       
                
                // Création des contrôleurr
                // et injection des dépendances Spring
                for(Class<?> clazz : controllers){
                        Object controller =
                                clazz.getDeclaredConstructor()
                                .newInstance();
                        beanFactory.autowireBean(controller);
                        beans.put( clazz, controller);
                }
                
                //Stockage dans le ServletContext
                context.setAttribute( "routes",routes);
                context.setAttribute( "beans", beans);
                context.setAttribute( "authVariable", authVariable);
                context.setAttribute( "roleVariable", roleVariable);
            
                System.out.println("===== SummerMVC Started =====");

        } catch (Exception e) {            
             throw new RuntimeException(e);        
        }    
    }    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}