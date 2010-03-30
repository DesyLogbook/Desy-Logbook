package org.desy.logbook.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User that didn't log in are routed to this protected
 * resource. When they log in they are redirected back to
 * the front controller
 * @author Johannes Strampe
 */
public class Secure extends HttpServlet {

    private static String FRONTCONTROLLER = "/newElog/";
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        /* der user hat sich ueber den security-constraint des
         * servlet containers eingeloggt und wird automatisch
         * zum front-controller weitergeschickt
         */
        String name = request.getParameter("name");
        redirect(FRONTCONTROLLER+name, response);

    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    /**
     * macht einen redirect
     * @param path wohin der redirect geht
     * @param response response wohin der redirect geschickt wird
     * @throws java.io.IOException
     */
    private void redirect( String path, HttpServletResponse response) throws IOException
    {
        String urlWithSessionID = response.encodeRedirectURL(path);
        response.sendRedirect( urlWithSessionID );
    }

}
