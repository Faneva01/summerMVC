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



    /**
     * Exécute la méthode du Controller
     * et retourne son résultat.
     */
    private Object executeMethod(
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
         * Récupération du Controller
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
         *
         * Avant Sprint 6 :
         *
         * mapping.getMethod().invoke(controller);
         *
         * Le résultat était perdu.
         *
         * Maintenant :
         * on récupère le résultat.
         */
        Object result =
                mapping.getMethod()
                       .invoke(controller);



        return result;

    }





    protected void processRequest(
            HttpServletRequest request,
            HttpServletResponse response
    )
            throws ServletException, IOException {


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

                /*
                 * Exécution du Controller
                 */
                Object result =
                        executeMethod(mapping);



                /*
                 * SPRINT 6
                 *
                 * Si la méthode possède
                 * l'annotation @Json,
                 * on retourne directement
                 * le résultat sous forme JSON.
                 */
                if(mapping.isJson()){


                    response.setContentType(
                        "application/json;charset=UTF-8"
                    );


                    PrintWriter out =
                            response.getWriter();



                    /*
                     * Pour l'instant,
                     * on transforme simplement
                     * le résultat en String.
                     *
                     * Cette partie sera remplacée
                     * par Jackson pour produire
                     * un vrai JSON.
                     */
                    out.println(
                        result
                    );



                    return;

                }



                /*
                 * Sinon :
                 *
                 * comportement MVC classique
                 * du Sprint 5 bis.
                 *
                 * Ici on récupérera le ModelView
                 * et on fera le RequestDispatcher.
                 */
                response.setContentType(
                    "text/html;charset=UTF-8"
                );


                PrintWriter out =
                        response.getWriter();



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
            catch(Exception e){

                throw new ServletException(e);

            }

        }
        else{


            response.setContentType(
                "text/html;charset=UTF-8"
            );


            PrintWriter out =
                    response.getWriter();


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