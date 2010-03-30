package org.desy.logbook.controller;

import org.desy.logbook.contentItems.Extra;
import org.desy.logbook.contentItems.Logbook;
import org.desy.logbook.helper.XMLHelper;
import java.io.File;
import org.desy.logbook.types.DataComponent;

/**
 * This class is a singleton class, the constructor
 * is private and the only object of the class can
 * be reached through the getInstance() method.
 * This class represents a root menu-entry of the
 * elogbook manager-app. It's able to show the
 * menu-content over the getData() method.
 * Among others its elements are the logbooks
 * 
 * @author Johannes Strampe
 */
public class DataController extends DataComponent{
    
    private static DataController _Instance = null;
    
    /**
     * static method to get the singleton object
     * @return the only instance of this class
     */
    public static synchronized DataController getInstance()
    {
        if (_Instance == null)
            _Instance =  new DataController();
        return _Instance;
    }
    
    /**
     * private constructor
     * reads prepares all logbooks and initializes their
     * crons
     */
    private DataController() {
        getAllLogbooks();
    }

    /**
     * removes all menu elements
     */
    public void clear() {
        
        getSubelementList().clear();
    }
    
    /**
     * get the path to the servlet location
     * like "/var/lib/tomcat5/webapps/elogbookManager"
     * then the webapps folder is searched for
     * folders containing a conf.xml file
     * every conf.xml file represents a logbook
     * which will be created as a child element
     */
    synchronized public void getAllLogbooks()
    {
        String path = ConfValuesController.getInstance().getGeneralConf().getLogbook_path();
        File f = new File(path);        
        // the "extra" menu should be first
        getSubelementList().addItem(new Extra());
        if (f!=null)
        {
            // f should be the webapps folder
            if (f.isDirectory())
            {   
                // logbooks are sorted
                File list[] = sort(f.listFiles());
                for (int i = 0; i < list.length; i++) 
                {                    
                    File confFile = new File(list[i].getAbsolutePath()+"/conf.xml");
                    if(confFile.exists() && !confFile.getAbsolutePath().endsWith("/conf/conf.xml"))
                    {
                        LogController.getInstance().timerStart("loading logbook "+confFile.getAbsolutePath());
                        getSubelementList().addItem(new Logbook(list[i].getName()));
                        LogController.getInstance().timerStop();
                    }
                }
            }
        }

    }// function end
    
    /**
     * sorts a list of files and folders by name and folders
     * are bigger than files
     * @param unsorted an unsorted list of files and folders
     * @return a sorted list of files and folders
     */
    private File[] sort(File[] unsorted)
    {
        if (unsorted==null || unsorted.length==0)
            return unsorted;
        
        File sorted[] = new File[unsorted.length];
        File runner;
        int count = 0;
        int index;
        boolean isSorted=false;
        while(!isSorted)
        {       
            runner = unsorted[0];
            index = 0;
            for (int i = 0; i < unsorted.length; i++) 
            {
                if (!isBigger(runner,unsorted[i]))
                {
                    runner = unsorted[i];
                    index=i;
                }                
            }
            if (runner==null)
            {
                isSorted=true;
            }
            else
            {
                sorted[count]=runner;
                unsorted[index]=null;
                count++;
            }
        }        
        return sorted;
    }
    
    /**
     * Compares both filenames and indicates which one is bigger.
     * @param first file that is compared to the second
     * @param second file that is compared to the first
     * @return true if the first file is bigger ot equal than the second
     */
    private boolean isBigger(File first, File second)
    {
        if (second == null) return true;
        if (first == null)  return false;
        return (first.getName().compareTo(second.getName())<0);
    }
    
 
    
    /**
     * Gets a request string and return xml-data
     * that can be interpreted by the client
     * javascript. An empty string will return
     * all elementst this menu-element contains.
     * The request can also contain commands and
     * routes to subelemnts.
     * @param request if is empty string all menu
     * elements will be returned. Can also contains
     * commands and routes to subelements
     * @return an xml like string that can be
     * interpreted by the client javascript
     */
    public String getData(String request)
    {
        // a request should never be null
        if (request==null) return "";

        // return all elements this menu entry contains
        if (request.equals(""))
        {
            String response = "";
            
            for (int i = 0; i < getSubelementList().length(); i++)
            {                
                String lName = getSubelementList().itemAt(i).getId();
                response += XMLHelper.mkEntry(lName,XMLHelper.mkLabel(lName),true);
            }
            return response;
        }

        // pass the request to the specific subelement
        return sendToSubElements(request);
    }

    /**
     * returns the id of this object
     * @return
     */
    public String getId() 
    {
        return "Manager";
    }
}
