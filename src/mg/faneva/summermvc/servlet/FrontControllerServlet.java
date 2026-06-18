package mg.faneva.summermvc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class FrontControllerServlet extends HttpServlet{
    private List<String> listController = new ArrayList<>();

    @Override
    public void init() throws ServletException{
        super.init();
        String basePackage = getServletContext().getInitParameter("base-package");

        try {
            listController = scanControllers(basePackage);
            for(String controller : listController){
                System.out.println(controller);
            }

        } catch (Exception e) {
            throw new ServletException();
        }
    }
    
    protected void processRequest(
        HttpServletRequest request, HttpServletResponse response
    ) throws ServletException , IOException {
        response.setContentType("text/html;charset=UTF-8");

        try(PrintWriter out = response.getWriter()){
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\">");
            out.println("<head>");
            out.println("<title>SummerMVC</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>SummerMVC is now active</h1>");
            out.println("<p>URL: "+ request.getRequestURI() +"</p>" );
            out.println("<p>Methode: "+ request.getMethod() + "</p>");
            out.println("<p>Controllers: "+ listController);
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
}