package mg.faneva.summermvc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.faneva.summermvc.mapping.MappingInfo;

public class FrontControllerServlet extends HttpServlet{
    private List<String> listController = new ArrayList<>();
    private HashMap<String , MappingInfo> routes;

    @Override
    public void init() throws ServletException{
        super.init();
        String basePackage = getServletContext().getInitParameter("base-package");
        routes = new HashMap<>();

        try {
            listController = scanControllers(basePackage);
            for (String controller : listController) {
                scanMethods(controller);
            }
        } catch (Exception e) {
            throw new ServletException();
        }
    }
    
    protected void processRequest(
        HttpServletRequest request, HttpServletResponse response
    ) throws ServletException , IOException {
        response.setContentType("text/html;charset=UTF-8");
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String url = uri.substring(contextPath.length());
        if (url.startsWith("/")) {
            url.substring(1);
        }
        
        
        
        try(PrintWriter out = response.getWriter()){
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\">");
            out.println("<head>");
            out.println("<title>SummerMVC</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>SummerMVC ok!</h1>");
            if (routes.containsKey(url)) {
                MappingInfo mapping = routes.get(url);
                out.println("<h2>URL reconnue</h2>");
                out.println("<p>URL : " + url + "</p>");
                out.println("<p>Classe : " + mapping.getClassName() + "</p>");
                out.println("<p>Méthode : " + mapping.getMethodName() + "</p>");
            } else {
                out.println("<h2>URL inconnue : " + url + "</h2>");

                out.println("<h3>Les URL connues :</h3>");

                out.println("<ul>");

                for(String key : routes.keySet()){
                    out.println("<li>" + key + "</li>");
                }

                out.println("</ul>");
            }
            out.println("</body>");
            out.println("</html>");
        }
    }
    @Override
    protected void doGet(
        HttpServletRequest request, HttpServletResponse response
    ) throws ServletException, IOException{
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
        processRequest(request, response);
    }

    private List<String> scanControllers(String basePackage) throws Exception {

        List<String> controllers = new ArrayList<>();

        // Convert package -> path
        String path = basePackage.replace(".", "/");

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        Enumeration<java.net.URL> resources = cl.getResources(path);

        while (resources.hasMoreElements()) {

            java.net.URL resource = resources.nextElement();

            java.io.File dir = new java.io.File(resource.toURI());

            for (String file : dir.list()) {

                if (file.endsWith(".class")) {

                    String className =
                        basePackage + "." + file.replace(".class", "");

                    Class<?> clazz = Class.forName(className);

                    if (clazz.isAnnotationPresent(
                            mg.faneva.summermvc.annotation.Controller.class)) {

                        controllers.add(className);
                    }
                }
            }
        }
        return controllers;
    }

    private void scanMethods(String className) throws Exception{
        Class<?> clazz = Class.forName(className);
        java.lang.reflect.Method[] methodes = clazz.getDeclaredMethods();

        for (java.lang.reflect.Method method : methodes) {
            
            if (method.isAnnotationPresent(mg.faneva.summermvc.annotation.Mapping.class)) {
                mg.faneva.summermvc.annotation.Mapping annotation = method.getAnnotation(mg.faneva.summermvc.annotation.Mapping.class);
                String url = annotation.value();

                if (routes.containsKey(url)) {
                    throw new Exception("URL déjà utilisé!" + url);
                }
                MappingInfo mapping = new MappingInfo(
                    className,
                    method.getName()
                );

                routes.put(url, mapping);
            }
        }
    }
}