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
import mg.faneva.summermvc.mapping.Mapping;
import mg.faneva.summermvc.mapping.UrlMethod;

public class FrontControllerServlet extends HttpServlet{
    private List<String> listController = new ArrayList<>();
    private HashMap<UrlMethod, Mapping> routes;

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
    
    private void executeMethod(Mapping mapping)
            throws Exception {

        Class<?> clazz =
                Class.forName(
                    mapping.getClassName()
                );

        Object controller =
                clazz.getDeclaredConstructor()
                    .newInstance();

        java.lang.reflect.Method method =
                clazz.getDeclaredMethod(
                    mapping.getMethodName()
                );

        method.invoke(controller);
    }


    protected void processRequest(
        HttpServletRequest request, HttpServletResponse response
    ) throws ServletException , IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String uri = request.getRequestURI();
        String context = request.getContextPath();

        String url = uri.substring(context.length());

        if(url.startsWith("/")){
            url = url.substring(1);
        }

        String httpMethod = request.getMethod();

        UrlMethod key =
                new UrlMethod(
                    url,
                    httpMethod
                );

        if(routes.containsKey(key)){

            Mapping mapping = routes.get(key);

            try {
                executeMethod(mapping);
            } catch (Exception e) {
                throw new ServletException(e);
            }

            out.println("Route trouvee");

            out.println("<br>");

            out.println(mapping.getClassName());

            out.println("<br>");

            out.println(mapping.getMethodName());

        }
        else{

            out.println("Route inconnue");
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

    private void scanMethods(String className) throws Exception {

        Class<?> clazz = Class.forName(className);

        java.lang.reflect.Method[] methods =
                clazz.getDeclaredMethods();

        for(java.lang.reflect.Method method : methods){

            if(method.isAnnotationPresent(
                    mg.faneva.summermvc.annotation.RequestMapping.class)){

                mg.faneva.summermvc.annotation.RequestMapping rm =
                        method.getAnnotation(
                            mg.faneva.summermvc.annotation.RequestMapping.class);

                String url = rm.value();

                String httpMethod = rm.method();

                UrlMethod key =
                        new UrlMethod(url, httpMethod);

                if(routes.containsKey(key)){
                    throw new Exception(
                        "Route deja utilisee : "
                        + httpMethod
                        + " "
                        + url
                    );
                }

                Mapping mapping =
                        new Mapping(
                            className,
                            method.getName()
                        );

                routes.put(key, mapping);
            }
        }
    }
}