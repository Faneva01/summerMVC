package mg.faneva.summermvc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import mg.faneva.summermvc.mapping.Mapping;
import mg.faneva.summermvc.mapping.UrlMethod;

public class FrontControllerServlet extends HttpServlet {


    private HashMap<UrlMethod, Mapping> routes;



    @Override
    public void init() throws ServletException {

        super.init();

        routes =
            (HashMap<UrlMethod, Mapping>)
            getServletContext()
            .getAttribute("routes");


        if(routes == null){
            throw new ServletException(
                "Routes non initialisées"
            );
        }

    }



    private void executeMethod(
            Mapping mapping)
            throws Exception {


        /*
         * Récupération des beans créés
         * par FrameworkContextListener
         */
        Map<Class<?>, Object> beans =
            (Map<Class<?>, Object>)
            getServletContext()
            .getAttribute("beans");



        /*
         * Récupération du contrôleur
         */
        Object controller =
                beans.get(
                    mapping.getController()
                );



        if(controller == null){

            throw new Exception(
                "Controller introuvable : "
                + mapping.getController()
            );

        }



        /*
         * Invocation de la méthode
         */
        mapping.getMethod()
               .invoke(controller);

    }





    protected void processRequest(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws ServletException, IOException {


        response.setContentType(
            "text/html;charset=UTF-8"
        );


        PrintWriter out =
                response.getWriter();



        String uri =
                request.getRequestURI();


        String context =
                request.getContextPath();


        String url =
                uri.substring(
                    context.length()
                );



        if(url.startsWith("/")){

            url =
                url.substring(1);

        }



        String httpMethod =
                request.getMethod();



        UrlMethod key =
                new UrlMethod(
                    url,
                    httpMethod
                );



        if(routes.containsKey(key)){


            Mapping mapping =
                    routes.get(key);



            try {

                executeMethod(mapping);


            } catch(Exception e){

                throw new ServletException(e);

            }



            out.println(
                "Route trouvee"
            );


            out.println("<br>");


            out.println(
                mapping.getController()
            );


            out.println("<br>");


            out.println(
                mapping.getMethod().getName()
            );


        }
        else{


            out.println(
                "Route inconnue"
            );

        }

    }





    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws ServletException, IOException {

        processRequest(
            request,
            response
        );

    }





    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws ServletException, IOException {


        processRequest(
            request,
            response
        );

    }

}