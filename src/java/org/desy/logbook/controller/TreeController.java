package org.desy.logbook.controller;


import java.util.Date;
import org.desy.logbook.javascriptTree.Tree;
import org.desy.logbook.javascriptTree.MakeTreeXML;
import org.desy.logbook.types.ConfValues;
import org.desy.logbook.types.DataComponent;

/**
 * This class is a singleton class, the constructor
 * is private and the only object of the class can
 * be reached through the getInstance() method.
 * This extends DataComponent and has a getData()
 * method but is not used as a menu element for
 * the manager app.
 * @author Johannes Strampe
 */
public class TreeController extends DataComponent{
    
    private static TreeController _Instance = null;
    
    
    /**
     * static method to get the singleton object
     * @return the only instance of this class
     */
    public static synchronized TreeController getInstance()
    {
        if (_Instance == null)
            _Instance =  new TreeController();
        return _Instance;
    }
    
    /**
     * private constructor
     * sets the logbook path
     */
    private TreeController() 
    {
        init();
    }
    
    // ..tomcat/webapps/web
    private String _logbookPath = "";

    /**
     *
     */
    public void clear() {
        getSubelementList().clear();
    }
          
    /***
     * for initialization the servlet path must be passed
     * eg /var/lib/tomcat5/webapps/elogbookManager/web
     */
    public void init(/*String logbookPath*/)
    {
        // save the context path (webapps folder)
        //File context = new File(servletPath);
        //_logbookPath = logbookPath;//context.getParent()+"/";//servletPath.substring(0,servletPath.length()-4);//.replace("/elog","");
        _logbookPath = ConfValuesController.getInstance().getGeneralConf().getLogbook_path();
        
    }
    
    /**
     * Processes a request from a client. Mostly the
     * data for the javascript tree
     * @param request form a client
     * @return response can be plain text or xml like
     */
    public String getData(String request) 
    {
        // create value is the name of the logbook
        String create = getParameter("create",request);
        
        // create parameter was passed so create new tree
        if (create!=null && create.compareTo("")!=0)
        {
            Tree tree = new Tree();
            String path = _logbookPath +"/"+ create;
            
            long nachher;
            long vorher = System.currentTimeMillis();
            // check if subServlet is initialized
            if ((tree!=null) && (tree.init(create,path)))
            {                
                // if key already exists it is overwritten
                getSubelementList().addItem(tree);
                //map.put(create,tree);
                nachher = System.currentTimeMillis();
                return "logbook created in: "+(nachher-vorher)+ "ms on date: "+(new Date()).toString();                
            }
            return "tree could not be initialized";            
        }
        else // no create parameter
        {
            // get name of the logbook
            String name = getParameter("name",request);
            DataComponent tree = getSubelementList().getItemById(name);
            //Tree tree = (Tree)map.get(name);
            if (tree!=null)
            {                
                return tree.getData(request);
            }
            else
            {
                String path = _logbookPath +"/"+ name;
                Tree newTree = new Tree();
                if ((newTree!=null) && (newTree.init(name,path)))
                {
                    // if key already exists it is overwritten
                    getSubelementList().addItem(newTree);
                    //map.put(name,newTree);
                    return newTree.getData(request);
                }
                else
                {
                    return("<E>error with tree init. name:"+name+" path:"+path+"</E>");                    
                }                
            }
        }
    }

    /**
     * returns the id of this object
     * @return
     */
    public String getId() 
    {
        return "Tree Controller";
    }


    /**
     * Gets a parameter string like
     * para1=val1&para2=val2 and gets the
     * searched parameter
     * @param paraName name of the searched parameter
     * @param request whole parameter string
     * @return values of the parameter or null if it
     * doesn't exist
     */
    private String getParameter(String paraName, String request) 
    {
        if (request==null) return null;
        String arr[] = request.replaceAll("&","=").split("=");
        for (int i = 0; i < arr.length; i++) 
        {
            if (i%2==0)
            {
                if (arr[i].equals(paraName))
                {
                    if ((i+1)<arr.length)
                    {
                        return arr[i+1];
                    }
                    else
                    {
                        return "";
                    }
                    
                }
            }
        }
        return null;
    }

    /**
     * Get some statistics about the loaded trees.
     * @return Some statistics about the loaded trees
     */
    public String getStatistics()
    {
        String result = "Number of trees: "+getSubelementList().length()+"\n\n";
        for (int i = 0; i < getSubelementList().length(); i++)
        {
            result +=getSubelementList().itemAt(i).getData("getShortStatistics")+"\n";
        }
        return result;
    }
    
    /**
     * get some statistics for the loaded tree data
     * of the logbook with the passed logname
     * @param logname to identify the tree
     * @return statistics about the single tree
     */
    public String getStatistics(String logname)
    {
        //Tree tree = (Tree)map.get(logname);
        DataComponent tree = getSubelementList().getItemById(logname);
        if (tree!=null)
        {            
            return tree.getData("getStatistics");//.getStatistics();
        }
        else
        {
            return "the tree for "+logname+" is not loaded.";
        }
    }
    
    /**
     * Is used when treeData is created manually.
     * gets the path to the conf file and creates
     * a new treeData.xml file with the fix parameters
     * -d -n -de
     * first data then folder
     * first number then letters
     * decreasing
     * response is some statusinfo of the 
     * creation process
     * @param logname 
     * @return
     */
    public String createTreeXML(String logname)
    {
        ConfValues cv = ConfValuesController.getInstance().getConf(logname);

        String elogpath = cv.getLogbook_path()+"/"+logname;
        
        String datapath = cv.getDatapath()+"/"+logname;

        // create the treeData.xml
        createTreeXML(datapath, elogpath);
        
        String response = "tree XML manually created on: "+(new Date()).toString()+"\n";
        LogController.getInstance().log("write treeData.xml for "+logname);
        return response;
    }
    
    /**
     * Is used 
     * gets the path to the conf file and creates
     * a new treeData.xml file with the fix parameters
     * -d -n -de
     * first data then folder
     * first number then letters
     * decreasing
     * response is some statusinfo of the 
     * creation process
     * @param datapath 
     * @param jspFolder
     */
    public void createTreeXML(String datapath, String jspFolder)
    {
        // /var/www/TESTelog/data /var/www/TESTelog/jsp -d -n -de
        String args[] = new String[5];
        args[0] = datapath;
        args[1] = jspFolder;
        args[2] = "-d";
        args[3] = "-n";
        args[4] = "-de";
        new MakeTreeXML(args);
        
    }

    /**
     * reload the treeData.xml file of
     * the logbook with the passed name
     * @param logname
     * @return message about the reload success
     */
    public String reload(String logname)
    {      
        Tree tree = new Tree();

        
        String path = ConfValuesController.getInstance().getConf(logname).getLogbook_path()+"/"+logname;
     
        long nachher;
        long vorher = System.currentTimeMillis();
        // check if subServlet is initialized
        if ((tree!=null) && (tree.init(logname,path)))
        {                
            // if key already exists it is overwritten
            getSubelementList().addItem(tree);
            //map.put(logname,tree);
            nachher = System.currentTimeMillis();
            return "logbook created in: "+(nachher-vorher)+ "ms on date: "+(new Date()).toString()+"\n";
        }
        return "tree could not be initialized";
    }
    
}//class end
