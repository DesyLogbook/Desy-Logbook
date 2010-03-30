package org.desy.logbook.servlets;


import org.desy.logbook.controller.*;
import org.desy.logbook.controller.LogController;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.desy.logbook.settings.Settings;

/**
 * This servlets manages requests to the treeController the
 * manager app. First the s
 *
 * @author Johannes Strampe
 */
public class Manager extends HttpServlet {
    
    String debugText = "";
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException
     * @throws IOException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        setGlobalContextPath(getServletContext().getRealPath(""));

        PrintWriter out = response.getWriter();
        Cookie cList[] = request.getCookies();
        
        
        // when servlet is called with a query string, the query is passed 
        // to the tree controller
        String queryString = request.getQueryString();
        
        // query string which is not the special query string for internet explorer, to 
        // stop ie from caching ajax requests to same adresses. This is because info is transfered
        // throu cookies and not html url parameters.
        // this request needs some treedata result for the javascript tree.
        if (queryString!=null && !queryString.startsWith("IE_Sux"))
        {
            response.setContentType("application/xml");

            // first check if session-variable is set
            // ATTENTION it is not checked if the logbook of the request
            // and of the authentication are the same. It is possible to request
            // ABCelog treedata when you logged in to the XYZelog.
            if(request.getSession().getAttribute("logName")==null)
            {
                out.print("<E>No authentication!</E>");
            }
            else
            {
                response.setContentType("application/xml");
                out.print(TreeController.getInstance().getData(request.getQueryString()));
                out.flush();
                out.close();
            }
        }
        else // no query string or special ie query string
        {
            
            String cookieContent = getCookie(cList,Settings.REQUEST_COOKIE);

            // nor query string neither cookie was sent
            // sending initial page
            if ( cookieContent == null)
            {
                response.setContentType("text/html;charset=UTF-8");
                sendInitialPage(out);
                out.flush();
                out.close();
            }
            else // a cookie was sent, its content is passed to the data controller
            {
                // first check if session-variable is set
                if(request.getSession().getAttribute("manager")==null ||
                  !request.getSession().getAttribute("manager").equals("manager"))
                {
                    response.sendRedirect("/NotAvailable");
                }
                else
                {
                    response.setContentType("application/xml");
                    out.print(Settings.RESPONSE_OPEN);
                    String result = DataController.getInstance().getData(cookieContent);
                    out.print(result);
                    out.print(Settings.RESPONSE_CLOSE);
                    out.flush();
                    out.close();
                }
            }
        }// else end
    }

    /**
     * when servlet is stopped
     * all quartz jobs should
     * be stopped too
     */
    public void destroy()
    {
        SchedulerController.getInstance().stopAllJobs();
        LogController.getInstance().log("System shutting down");
        LogController.getInstance().writeLogFile(true);
    }
    

    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     * @return
     */
    public String getServletInfo() {
        return "eLogbook Manager Servlet";
    }

    // </editor-fold>

    /**
     * On servlet startup, context path is set as a
     * system global variable which is vital for
     * the loading of the conf files in the
     * ConfValuesController.
     * @throws ServletException
     */
    public void init() throws ServletException 
    {
        // path = "/home/jstrampe/newElog/build/web"
        setGlobalContextPath(getServletContext().getRealPath(""));
    }

    
    /**
     * gets a cookie list and returns the content
     * of the passed cookie name
     * null is returned if cookie does not exist
     * empty string is returned if cookie is
     * empty or the content equals "empty"
     */
    private String getCookie(Cookie[] cookieList, String cName)
    {
        if (cookieList==null) return null;
        for (int i = 0; i < cookieList.length; i++) 
        {
            if (cookieList[i].getName().equals(cName))
            {
                if (cookieList[i].getValue().equals("empty")) return "";
                return cookieList[i].getValue();
            }            
        }
        return null;
    }
    
       
    /**
     * error message when servlet is manually called, and not through the
     * ajax obejct of the index.html
     */
    private void sendInitialPage(PrintWriter out) 
    {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Manager</title>");
        out.println("</head>");
        out.println("<h2>Error</h2>");
        out.println("<div>The Manager Servlet should not be manually called !</div><br></br>");
        out.println("</html>");
    }

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
