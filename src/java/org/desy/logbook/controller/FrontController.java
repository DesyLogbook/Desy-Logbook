package org.desy.logbook.controller;


import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.desy.logbook.types.ConfValues;

/**
 * This controller stores the security settings of
 * all logbooks. Every logbook request is checked
 * here and then directed to the correct route.
 * This class is a singleton class, the constructor
 * is private and the only object of the class can
 * be reached through the getInstance() method.
 * @author Johannes Strampe
 */
public class FrontController {

    private static FrontController _instance = null;

    /**
     * private constructor
     * loads all security settings for the logbooks
     */
    private FrontController(){
        init();
    }

    /**
     * static method to get the singleton object
     * @return the only instance of this class
     */
    public static synchronized FrontController getInstance(){
        if (_instance== null) _instance = new FrontController();
        return _instance;
    }

    // some staticvalues
    private static String NOTAVAILABLE = "/NotAvailable";
    private static String MAIN_JSP = "/jsp/elog.jsp";
    private static String SECURECONTROLLER = "/newElog/SecureController?name=";

    // array which stores the security settings of the logbooks
    private Logbook[] _logbookList = new Logbook[0];

    private static int IS_ALLOWED = 0;
    private static int IS_DENIED = 1;
    private static int IS_UNCERTAIN = 2;

    /**
     * Processes a normal http request, checks the security settings
     * like ip and role and then passes the request to the correct
     * route.
     * @param request normal http request
     * @param response normal http response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        // if logbook list is empty try to load the list
        if (getLogbookList().length==0)
            readAllLogbooks();

        Logbook[] logbookList = getLogbookList();
        
        // status if the request is allowed/denied/uncertain to pass
        int status = IS_UNCERTAIN;

        // only one redirect can happen or a exception will be caused
        boolean isRedirected = false;

        // the requested logbook
        Logbook actual = null;

        // get the name of the logbook from the request
        String logName = request.getRequestURI().replaceFirst(request.getContextPath()+"/", "");

        // check if requested logbook exists
        for (int i = 0; i < logbookList.length; i++) {
            if (logName.equals(logbookList[i].getName()))
            {
                actual = logbookList[i];
            }
        }
        if (actual==null)
        {
            // requested logbook does not exist, the not Available resource is shown
            forward(request, response, NOTAVAILABLE);
            status = IS_DENIED;
            isRedirected = true;
        }

        String senderIp = request.getRemoteAddr();

        // check if ip is allowed to enter
        if (status == IS_UNCERTAIN)
        {
            for (int i = 0; i < actual.getAllowIpList().length; i++)
            {
                String checkIp = actual.getAllowIpList()[i];
                if(checkIp.startsWith("*"))
                {
                    checkIp = checkIp.substring(1);
                    if (senderIp.endsWith(checkIp)) status = IS_ALLOWED;
                }
                if(checkIp.endsWith("*"))
                {
                    checkIp = checkIp.substring(0,checkIp.length()-1);
                    if (senderIp.startsWith(checkIp)) status = IS_ALLOWED;
                }
                if (senderIp.equals(checkIp)) status = IS_ALLOWED;
            }
        }

        // check if ip is on the denied list
        if (status == IS_UNCERTAIN)
        {
            for (int i = 0; i < actual.getRejectIpList().length; i++)
            {
                String checkIp = actual.getRejectIpList()[i];
                if(checkIp.startsWith("*"))
                {
                    checkIp = checkIp.substring(1);
                    if (senderIp.endsWith(checkIp)) status = IS_DENIED;
                }
                if(checkIp.endsWith("*"))
                {
                    checkIp = checkIp.substring(0,checkIp.length()-1);
                    if (senderIp.startsWith(checkIp)) status = IS_DENIED;
                }
                if (senderIp.equals(checkIp)) status = IS_DENIED;
            }
        }

        // check the role
        if (status == IS_UNCERTAIN)
        {
            // role is only needed when conf defines a needed role
            boolean is_role_needed = actual.getNeededRoleList().length>0;

            // get the actual username
            String senderRole = request.getRemoteUser();

            for (int i = 0; i < actual.getNeededRoleList().length; i++)
            {
                if (senderRole!=null && request.isUserInRole(actual.getNeededRoleList()[i])) status = IS_ALLOWED;
            }

            // a role is needed but not available
            if (is_role_needed && status == IS_UNCERTAIN)
            {
                // invalidate session for if wrong login has been used
                //request.getSession().invalidate();
                isRedirected = true;
                status = IS_DENIED;
                redirect(SECURECONTROLLER+logName, response);
            }
        }

        // check if request was not denied
        if(!(status == IS_DENIED))
        {
            // logName is set for the elog.jsp
            request.getSession().setAttribute("logName", logName);
            System.out.println("logname is "+logName);

            // the name of the logbook is also set to allow user to use
            // more than one logbook at one time
            request.getSession().setAttribute(logName, "allow");
            forward(request, response, MAIN_JSP);
        }
        else
        {
            // only redirect if no previous redirect or forward has happend (or exception will happen)
            if (!isRedirected) forward(request, response, NOTAVAILABLE);
        }
    }

    /**
     * Forward the client request to another source
     * @param request of the client
     * @param response to the client
     * @param path url where the client should be forwarded to
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    private void forward( HttpServletRequest request, HttpServletResponse response, String path ) throws ServletException, IOException
    {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        try {
            dispatcher.forward(request, response);
        } 
        catch (ServletException sE)
        {
            LogController.getInstance().log("Error in Frontcontroller while trying to forward request to "+path+""+
                                            sE.toString());
            
        }
    }

    /**
     * getter for the logbook list
     * @return list of logbooks and their security settings
     */
    private Logbook[] getLogbookList() {
        return this._logbookList;
    }

    /**
     * setter for the logbook list
     * @param logbookList list of logbooks and their security settings
     */
    private void setLogbookList(Logbook[] logbookList) {
        this._logbookList = logbookList;
    }


    /**
     * sends a redirect as response to the client
     * @param path where the client is redirected to
     * @param response the response to the client
     * @throws java.io.IOException
     */
    private void redirect( String path, HttpServletResponse response) throws IOException
    {
        String urlWithSessionID = response.encodeRedirectURL(path);
        response.sendRedirect( urlWithSessionID );
    }

    /**
     * cleares a logbook security settings
     */
    public void clear() {
        this._logbookList = new Logbook[0];
    }

    /**
     * reads the security settings from all logbooks
     */
    public void init() {
        readAllLogbooks();
    }

    /**
     * reads the security settings from all logbooks
     */
    synchronized private void readAllLogbooks() {
        ConfValues generalConf = ConfValuesController.getInstance().getGeneralConf();
        if (generalConf!=null)
        {
            String logbookNames[] = ConfValuesController.getInstance().getAllConfNames();
            Logbook result[] = new Logbook[logbookNames.length];

            for (int i = 0; i < logbookNames.length; i++) {
                result[i] = new Logbook(ConfValuesController.getInstance().getConf(logbookNames[i]));
                result[i].concatLogbooks(generalConf);
            }
            setLogbookList(result);
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
     * class to store the security information from a
     * logbook conf file
     */
    private class Logbook{

        // variables to store the logname and the security settings
        private String name;
        private String[] allowIpList;
        private String[] rejectIpList;
        private String[] neededRoleList;

        /**
         * empty constructor
         */
        public Logbook(){
            
        }

        /**
         * constructor which sets all class attributes
         * @param conf which contains a logbook conf file
         */
        public Logbook(ConfValues conf){
            if (conf!=null)
            {
                this.name = conf.getName();
                this.allowIpList = conf.getAllowIp();
                this.rejectIpList = conf.getRejectIp();
                this.neededRoleList = conf.getNeededRole();
            }
        }

        /**
         * the security settings of the passed conffile
         * is added to the existing values
         * @param conf contains the security settings that should be added
         */
        public void concatLogbooks(ConfValues conf)
        {
            if (conf!=null)
            {
                allowIpList = concatStringArrays(allowIpList, conf.getAllowIp());
                rejectIpList = concatStringArrays(rejectIpList, conf.getRejectIp());
                neededRoleList = concatStringArrays(neededRoleList, conf.getNeededRole());
            }
        }

        /**
         * Get the name of the logbook
         * @return name of the logbook
         */
        public String getName() {
            return name;
        }

        /**
         * List of ips that are allowed to pass
         * @return list of ip's
         */
        public String[] getAllowIpList() {
            if (allowIpList==null) return new String[0];
            return allowIpList;
        }


        /**
         * List of roles, from which one is needed to pass
         * @return list of roles
         */
        public String[] getNeededRoleList() {
            if (neededRoleList==null) return new String[0];
            return neededRoleList;
        }

        /**
         * List of ips that are not allowed to pass
         * @return list of ip's
         */
        public String[] getRejectIpList() {
            if (rejectIpList==null) return new String[0];
            return rejectIpList;
        }

        /**
         * Sets the name of the logbook
         * @param name name of the logbook
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Sets the list of ip's that are allowed to pass
         * @param allowIpList list of ip's
         */
        public void setAllowIpList(String[] allowIpList) {
            this.allowIpList = allowIpList;
        }

        /**
         * Sets the list of roles from which one is needed to pass
         * @param neededRoleList list of roles
         */
        public void setNeededRoleList(String[] neededRoleList) {
            this.neededRoleList = neededRoleList;
        }

        /**
         * Sets the list of ip's that are not allowed to pass
         * @param allowIpList list of ip's
         */
        public void setRejectIpList(String[] rejectIpList) {
            this.rejectIpList = rejectIpList;
        }

        /**
         * merges two arrays to one array
         * @param arr1 first part of thhe new array
         * @param arr2 second part of the new array
         * @return the new array or an empty array if
         * one of the parameters is null
         */
        private String[] concatStringArrays(String[] arr1, String []arr2)
        {
            if (arr1==null || arr2==null) return new String[0];

            String result[] = new String[arr1.length + arr2.length];
            for (int i = 0; i < arr1.length; i++) {
                result[i]=arr1[i];
            }

            int difference = arr1.length;

            for (int i = 0; i < arr2.length; i++) {
                result[i+difference] = arr2[i];
            }

            return result;
        }
        
    }// logbook class end

}// frontcontroller class end
