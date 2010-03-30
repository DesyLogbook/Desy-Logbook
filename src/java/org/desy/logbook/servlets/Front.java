package org.desy.logbook.servlets;

import org.desy.logbook.controller.FrontController;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.desy.logbook.settings.Settings;

/**
 * passes all requests to the FrontController class
 * but first sets the contextPath as a System global
 * variable which is vital for the loading of the conf
 * files in the ConfValuesController.
 *
 * @author Johannes Strampe
 */
public class Front extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        setGlobalContextPath(getServletContext().getRealPath(""));
        FrontController.getInstance().processRequest(request, response);
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
     *
     */
    public void init(){

        // path = "/home/jstrampe/newElog/build/web"
        setGlobalContextPath(getServletContext().getRealPath(""));
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    /**
     * Sets the contextPath as a System global
     * variable which is vital for the loading of the conf
     * files in the ConfValuesController.
     * @param contextPath
     */
    private void setGlobalContextPath(String contextPath)
    {
        if (System.getProperty(Settings.GLOBAL_CONTEXT_PATH)==null)
            System.setProperty(Settings.GLOBAL_CONTEXT_PATH, contextPath);
    }
    

}
