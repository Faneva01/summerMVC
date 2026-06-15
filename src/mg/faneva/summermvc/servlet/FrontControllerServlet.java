package mg.faneva.summermvc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class FrontControllerServlet extends HttpServlet{
    protected void processRequest(
        HttpServletRequest request, HttpServletResponse response
    ) throws ServletException , IOException {
        response.setContentType("text/html;charset=UTF-8");

        try(PrintWriter out = response.getWriter()){
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"en\">");
            out.println("<head>");
            out.println("<title>FrontControllerServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Welcome into FrontcontrollerServlet</h1>");
            out.println("<p>URL: "+ request.getRequestURI() +"</p>" );
            out.println("<p>Methode: "+ request.getMethod() + "</p>");
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
}