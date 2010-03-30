package org.desy.logbook.contentItems;

import org.desy.logbook.controller.SchedulerController;
import org.desy.logbook.controller.TreeController;
import org.desy.logbook.helper.XMLHelper;
import org.desy.logbook.controller.LogController;
import org.desy.logbook.helper.StartupHelper;
import org.desy.logbook.settings.Settings;
import org.desy.logbook.types.DataComponent;

/**
 * This class represents a menu-entry of the
 * elogbook manager-app. It's able to show the
 * menu-content over the getData() method.
 * Here some information and options for the
 * whole logbook-app are shown:
 * -latest Logfile entries
 * -info about running cron and execute jobs
 * -info about loaded trees
 * -action to reload the whole manager
 * -link to create a new logbook
 * @author Johannes Strampe
 */
public class Extra extends DataComponent{

    // suffix, when a message is too long (>4kb)
    private String cutMessage = " ...The log has been cut !\nSee logfile for whole log: ";

    // info that explains what steps a reload consists of
    private String reloadInfo = "By reloading the Manager you will:\n\n" +
                                "reload conf.xml files\n"+
                                "reload security settings of the front controller\n"+
                                "reload manager elements\n"+
                                "reload cronjobs\n"+
                                "reload treedata\n"+
                                "check and write the routing of the web.xml";


    /**
     * Empty constructor
     */
    public Extra() {
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
    public String getData(String request) {

        // a request should never be null
        if (request==null) return "";

        if (request.equals("reload/"+Settings.COMMAND_RELOAD_MANAGER))
        {
            StartupHelper.restartAllControllers();
            StartupHelper.updateWebXML();
            return XMLHelper.mkEntry("", XMLHelper.mkLabel("Please refresh this page to complete the reload !"));
        }

        // return all elements this menu entry contains
        if (request.equals(""))
        {
            String result = "";
            
            String log = "";
            log += XMLHelper.mkLabel("Log");
            log += XMLHelper.mkInfo(getLogMessage());
            result += XMLHelper.mkEntry("log", log);
            
            String cronjobs = "";
            cronjobs += XMLHelper.mkLabel("Cronjobs");
            cronjobs += XMLHelper.mkInfo(SchedulerController.getInstance().toString());
            result += XMLHelper.mkEntry("cronjobs", cronjobs);
            
            String trees = "";
            trees += XMLHelper.mkLabel("Trees");
            trees += XMLHelper.mkInfo(TreeController.getInstance().getStatistics());
            result += XMLHelper.mkEntry("trees", trees);

            String newLogbook = "";
            newLogbook += XMLHelper.mkLink("Create a new Logbook", "jsp/newLogbook.jsp");
            result += XMLHelper.mkEntry("newLogbook", newLogbook );

            String reloadManager = "";
            reloadManager = XMLHelper.mkCommand("Reload Manager", Settings.COMMAND_RELOAD_MANAGER);
            reloadManager += XMLHelper.mkInfo(this.reloadInfo);
            result += XMLHelper.mkEntry("reload", reloadManager);

            return result;
        }
        return "";
    }

    /**
     * returns the id of this object
     * @return id of the element
     */
    public String getId() {
        return "Extra";
    }
    
    /**
     * Generate a logmessage that will be shown
     * in the manager. If message is too long
     * it will be cut.
     * @return the generated logmessage.
     */
    private String getLogMessage() {
        String result = LogController.getInstance().getLog();
        // long messages get a note that the message was cut
        if (LogController.CUT_LENGTH < result.length())
        {
            result = result.concat(cutMessage).concat(Settings.LOGFILE);
        }
        return result;
    }
    

}
