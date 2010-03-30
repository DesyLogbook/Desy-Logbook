package org.desy.logbook.contentItems;

import org.desy.logbook.controller.ConfValuesController;
import org.desy.logbook.helper.XMLHelper;
import org.desy.logbook.types.ConfValues;
import org.desy.logbook.types.ConfExecuteJobValues;
import org.desy.logbook.types.DataComponent;

/**
 * This class represents a menu-entry of the
 * elogbook manager-app. It's able to show the
 * menu-content over the getData() method.
 * represents all jobs of a logbook
 * @author Johannes Strampe
 */
public class Jobs extends DataComponent{
    
    
    /** Creates a new instance of Jobs
     * @param logname
     */
    public Jobs(String logname)
    {        
        // create all jobs (cron and execute jobs)
        getAllAvailableJobs(logname);
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
        if (request == null) return "";

        // return all elements this menu entry contains
        if (request.equals(""))
        {
            String response = "";
            
            for (int i = 0; i < getSubelementList().length(); i++)
            {
                String lName = getSubelementList().itemAt(i).getId();
                response += XMLHelper.mkEntry(lName,getSubelementList().itemAt(i).getData(""));
            }
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
        return "Jobs";
    }
    
    
    /**
     * reads all cron and execute jobs from conf file
     * and creates subelements
     */
    private void getAllAvailableJobs(String logname)
    {
        
        ConfValues cv = ConfValuesController.getInstance().getConf(logname);

        // Y, M, 3 etc.
        String shift        = cv.getNew_shift();
        // /var/www/ELOGDATA
        String datapath     = cv.getDatapath();
        // "en" is default
        String langCode     = cv.getLang_code();
        // http://localhost:8080/newElog
        String context      = cv.getContext();
        // /home/jstrampe/newElog/build/web
        String logbookPath  = cv.getLogbook_path();
        
        String elogFolder = logbookPath + "/"+logname;
        datapath += "/"+logname;
        String workFile = elogFolder+"/work.xml";
        String treeServlet = context + "/Manager";

        // create the cronjob and the cronjob menu entry
        getSubelementList().addItem(new CronJob(datapath,
            workFile,
            elogFolder,
            logname,
            treeServlet,
            shift,
            langCode));


        // get all execute jobs, create them and their menu entries
        ConfExecuteJobValues[] ejl = cv.getExecuteJobsList();

        for (int i = 0; i < ejl.length; i++) {
            getSubelementList().addItem(new ExecuteJob(logname,
                                         ejl[i].getTarget(),
                                         ejl[i].getTime()
                                         ));
        }
    }

}//class end
