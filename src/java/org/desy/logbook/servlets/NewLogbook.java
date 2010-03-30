package org.desy.logbook.servlets;

import org.desy.logbook.controller.ConfValuesController;
import org.desy.logbook.helper.StartupHelper;
import org.desy.logbook.helper.IOHelper;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.desy.logbook.settings.Settings;
import org.desy.logbook.types.ConfValues;

/**
 * Servlet which gets form data from the user
 * and tries to create a new logbook
 * @author Johannes Strampe
 */
public class NewLogbook extends HttpServlet {

    private static String OK = "Everything worked fine !!!";

    /**
     * Reads the form data and tries to create a new logbook.
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        setGlobalContextPath(getServletContext().getRealPath(""));

        // first check if user has logged into the manager
        if(request.getSession().getAttribute("manager")==null ||
          !request.getSession().getAttribute("manager").equals("manager"))
        {
            response.sendRedirect("/NotAvailable");
        }
        else
        {
            // read the form data
            NewLogbookData data = new NewLogbookData();
            data.setName(request.getParameter("name"));
            data.setTitle(request.getParameter("title"));
            data.setNewShift(request.getParameter("newShift"));

            // create a new logbook
            String result = createNewLogbook(data);

            if (result.equals(OK))
            {
                // go to the manager app if everything worked
                response.sendRedirect("");
            }
            else
            {
                Cookie cookie = new Cookie("ERROR", result);
                response.addCookie(cookie);
                response.sendRedirect("jsp/newLogbook.jsp");
            }
        }

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
     * try to create a new logbook with the passed data
     * @param data with the data for the new logbook
     * @return string which indicates if everything worked
     */
    private String createNewLogbook(NewLogbookData data) {

        // first check the input
        if (!isInputOK(data.getName()))
            return "Please enter a legal name !";
        if (!isInputOK(data.getTitle()))
            return "Please enter a legal title !";
        if (!isInputOK(data.getNewShift()))
            return "Please enter a legal shift !";

        ConfValues cv = ConfValuesController.getInstance().getGeneralConf();

        if (cv==null) return "The manager has not been initialized, login to the manager application";

        String logbookPath = cv.getLogbook_path();
        String dataPath = cv.getDatapath();

        File dataFolder = new File(dataPath+"/"+data.getName());
        File logbookFolder = new File(logbookPath+"/"+data.getName());

        if (dataFolder.exists() || logbookFolder.exists())
            return "Please choose another logbook name<br></br>a folder with the given name already exists";

        if (!dataFolder.mkdirs()) return "Could not create folder "+dataFolder.getAbsolutePath();

        if (!logbookFolder.mkdirs()) return "Could not create folder "+logbookFolder.getAbsolutePath();

        String confPath = logbookFolder.getAbsolutePath()+"/conf.xml";

        if (!writeConfXML(confPath, data))
        {
            return "could not write the file "+confPath;
        }

        reloadEverything();

        return OK;
    }

    /**
     * check the user input
     * @param input user input
     * @return true if input is ok
     */
    private boolean isInputOK(String input)
    {
        return (input!=null &&
                !input.equals("") &&
                input.length()<75);
    }

    /**
     * reload the data stored in the controllers
     */
    private void reloadEverything() {

        ConfValues cv = ConfValuesController.getInstance().getGeneralConf();
        if (cv!=null)
        {
            StartupHelper.restartAllControllers();
            StartupHelper.updateWebXML();
        }
    }

    /**
     * Sets the contextPath as a System global
     * variable which is vital for the loading of the conf
     * files in the ConfValuesController.
     * @throws javax.servlet.ServletException
     */
    public void init() throws ServletException {
        setGlobalContextPath(getServletContext().getRealPath(""));
    }


    /**
     * writes a new standard conf.xml with the passed parameters
     * @param confPath file path of the conf.xml
     * @param data data of the new logbook
     * @return true if the writing was successful
     */
    private boolean writeConfXML(String confPath, NewLogbookData data) {

        String content = "";
        content += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        content += "\n";
        content += "<logbook>\n";
        content += "<!--allowIp>127*</allowIp-->\n";
        content += "<!--rejectIp>123.456.789*</rejectIp-->\n";
        content += "<!--neededRole>myRole</neededRole-->\n";
        content += "\n";
        content += "<title>"+data.getTitle()+"</title>\n";
        content += "<name>"+data.name+"</name>\n";
        content += "<new_shift>"+data.getNewShift()+"</new_shift>\n";
        content += "\n";
        content += "<linkList>\n";
        content += "<link target=\"http://www.google.de\" label=\"Google\"/>\n";
        content += "<link target=\"http://tesla.desy.de/doocs/\" label=\"Doocs\"/>\n";
        content += "<link target=\"http://ttfinfo.desy.de/TTFelog/\" label=\"TTFelog\"/>\n";
        content += "<link target=\"http://www.desy.de\" label=\"Desy\"/>\n";
        content += "</linkList>\n";
        content += "\n";
        content += "<dropBoxList>\n";
        content += "<item target=\"\" label=\"--Logbook--\"/>\n";
        content += "</dropBoxList>\n";
        content += "\n";
        content += "\n";
        content += "<location_list enabled=\"true\">\n";
        content += "<location>eins</location>\n";
        content += "<location>zwo</location>\n";
        content += "<location>drei</location>\n";
        content += "</location_list>\n";
        content += "</logbook>\n";
        
        return IOHelper.writeFile(confPath, content);
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


    /**
     * class which stores the data needed for
     * the creation of a new logbook
     */
    private class NewLogbookData{

        /**
         * empty constructor
         */
        public NewLogbookData() {
        }

        // values for a new logbook
        private String name = "";
        private String title = "";
        private String newShift = "";

        // setter

        public void setName(String name) {
            this.name = name;
        }

        public void setNewShift(String newShift) {
            this.newShift = newShift;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        // getter

        public String getName() {
            return name;
        }

        public String getNewShift() {
            return newShift;
        }

        public String getTitle() {
            return title;
        }
    }

}
