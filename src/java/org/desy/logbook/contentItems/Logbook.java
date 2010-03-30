package org.desy.logbook.contentItems;

import org.desy.logbook.controller.ConfValuesController;
import org.desy.logbook.controller.SchedulerController;
import org.desy.logbook.controller.TreeController;
import org.desy.logbook.helper.XMLHelper;
import org.desy.logbook.settings.Settings;
import org.desy.logbook.types.DataComponent;


/**
 * This class represents a menu-entry of the
 * elogbook manager-app. It's able to show the
 * menu-content over the getData() method.
 * represents a logbook
 * @author Johannes Strampe
 */
public class Logbook extends DataComponent {
    
    // logbook name
    private String _name = "";

    // url to the logbook. Is calculated during init()
    private String _logbookLink = "";
    
    
    
    /**
     * Constructor which initializes the whole logbook
     * @param name of the logbook
     */
    public Logbook(String name)
    {
        init(name);
    }
    
    /**
     * Initializes the logbook and saves the logname,
     * which is important to identify the conf file
     * @param logname to identify the logbook
     */
    private void init(String logname)
    {
        // save the logbook name e.g. ABCelog
        this._name = logname;

        ConfValuesController.getInstance().reloadConf(logname);
        String context = ConfValuesController.getInstance().getConf(logname).getContext();
        this._logbookLink = context +"/"+ logname;

        // clear the list of logbooks
        getSubelementList().clear();
        
        addJobs(logname);
        addConf(logname);
        addTree(logname);
    }
    
    /**
     * add job subelements to the logbook
     * @param logname to identify the logbook
     */
    private void addJobs(String logname)
    {
        getSubelementList().addItem(new Jobs(logname));
    }
    
    /**
     * add conf file editor to the logbook
     * @param logname to identify the logbook
     */
    private void addConf(String logname)
    {
        getSubelementList().addItem(new Conf(logname));
    }
    
    /**
     * add tree child to the logbook
     * @param logname to identify the logbook
     */
    private void addTree(String logname)
    {
        getSubelementList().addItem(new TreeInfo(logname));
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

        // command to reload the logbook
        if (request.equals("extra/"+Settings.COMMAND_RELOAD_LOGBOOK))
        {
            // delete all subelements
            getSubelementList().clear();
            // stop all jobs
            SchedulerController.getInstance().stopJobsFromLogbook(_name);
            // reload the tree
            TreeController.getInstance().reload(_name);
            // recreate all subelements
            init(_name);
            // request is changed to "" to show all data again
            request = "";
        }

        // with no subelemnts something went wrong
        if (getSubelementList().length()==0)
        {
            return XMLHelper.mkEntry("",XMLHelper.mkLabel("This logbook is currently not managed. It may be outdated."));
        }
        
        // return all elements this menu entry contains
        if (request.equals(""))
        {
            String response = "";
            for (int i = 0; i < getSubelementList().length(); i++)
            {                
                String lName = getSubelementList().itemAt(i).getId();
                response += XMLHelper.mkEntry(lName,XMLHelper.mkLabel(lName),true);
            }
            String extraContent = "";
            extraContent += XMLHelper.mkCommand("reload logbook",Settings.COMMAND_RELOAD_LOGBOOK);
            extraContent += XMLHelper.mkLink("visit", _logbookLink);
            response += XMLHelper.mkEntry("extra",extraContent);
            return response;
        }

        // pass the request to the specific subelement
        return sendToSubElements(request);
    }
    
    /**
     * returns the id of this object
     * @return id of the element
     */
    public String getId() 
    {
        return _name;
    }


}//class end
