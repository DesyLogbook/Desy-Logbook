package org.desy.logbook.contentItems;

import org.desy.logbook.controller.SchedulerController;
import org.desy.logbook.cronjobs.QuartzCronJob;
import org.desy.logbook.controller.LogController;
import org.desy.logbook.helper.XMLHelper;
import java.util.Date;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.desy.logbook.settings.Settings;
import org.desy.logbook.types.DataComponent;


/**
 * This class represents a menu-entry of the
 * elogbook manager-app. It's able to show the
 * menu-content over the getData() method.
 * The cronjobs of a logbook, that create the
 * new shift entries, are created here. They
 * can also be started and stopped.
 * @author Johannes Strampe
 */
public class CronJob extends DataComponent
{
    // variables to save the job state

     // /var/www/ELOGDATA/ABCelog
    private String _datapath = "";

    // ..tomcat/webapps/newElog/ABCelog/work.xml
    private String _workFile = "";

    // ..tomcat/webapps/newElog/build/web/ABCelog
    private String _logbookFolder = "";

    // ABCelog
    private String _logname = "";

    // http://localhost:8080/newElog/Manager
    private String _servletURL = "";
    
    // "Y", "M", etc.
    private String _shift = "";

    // "en", "de", etc.
    private String _langCode = "";

    private JobDetail _jobDetail = null;

    private CronTrigger _cronTrigger = null;
    
    
    /** Constructor. Creates and starts a new cronjob
     * @param datapath path were the new shift will be saved
     * @param shift can be Y M W D 3 or con-notation
     * @param workFile path to the work.xml
     * @param logbookFolder
     * @param logname
     * @param servletURL
     * @param langCode
     */
    public CronJob(String datapath, String workFile, String logbookFolder, String logname, String servletURL, String shift, String langCode/*, Scheduler scheduler*/)
    {
        this._datapath = datapath;
        this._langCode = langCode;
        this._logname = logname;
        this._logbookFolder = logbookFolder;
        this._servletURL = servletURL;
        this._shift = shift;
        this._workFile = workFile;
        
        // create and start the job
        try
        {
            createMainCronjob();
            startJob();
        }
        catch (Exception ex)
        {
            LogController.getInstance().log(ex.toString());
            // when a job is not createt, running job will always return false
        }
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
        
        // start the job
        if(request.equals(Settings.COMMAND_START_JOB))
        {
            try {startJob();}catch (Exception ex) {LogController.getInstance().log(ex.toString());};
        }
        
        // stop the job
        if(request.equals(Settings.COMMAND_STOP_JOB))
        {
            try {stopJob();}catch (Exception ex) {LogController.getInstance().log(ex.toString());};
        }
        
        String response = "";        
        response += XMLHelper.mkLabel("Cron job");
        boolean isRunning = isRunning();
        response += XMLHelper.mkStatus(isRunning);
        if (isRunning)
        {
            response += XMLHelper.mkCommand("pause job",Settings.COMMAND_STOP_JOB);
        }
        else
        {
            response += XMLHelper.mkCommand("start job",Settings.COMMAND_START_JOB);
        }
        String info="";
        if (_cronTrigger==null)
        {
            info = "No cronjob has been launched !\nPlease check these settings:\n\n";
        }
        else
        {
            info = "next launch:\n "+_cronTrigger.getFireTimeAfter(new Date())+"\n\n";
            info += "previous launch:\n "+SchedulerController.getInstance().getPrevLaunchTime(Settings.MAIN_CREATION_CRONJOB_NAME, _logname)+"\n\n";
        }
        
        //info += "previous launch:\n "+_cronTrigger.getPreviousFireTime()+"\n\n";
        
        info += "shift interval:\n "+_shift+"\n\n";
        info += "datapath :\n "+_datapath+"\n\n";
        info += "language:\n "+_langCode+"\n\n";
        info += "logbook folder:\n "+_logbookFolder+"\n\n";
        info += "workfile:\n "+_workFile+"\n\n";
        info += "servlet:\n "+_servletURL+"\n";        
        response += XMLHelper.mkInfo(info);
        return response;
    }

    /**
     * returns the id of this object
     * @return id of the element
     */
    public String getId() 
    {
        return "cronjob";
    }
    
    /**
     * create the main cronjob
     */
    private void createMainCronjob() throws Exception
    {
        // save the job parameters
        _jobDetail = new JobDetail(Settings.MAIN_CREATION_CRONJOB_NAME,_logname,QuartzCronJob.class);
        _jobDetail.getJobDataMap().put("datapath",_datapath);
        _jobDetail.getJobDataMap().put("workFile",_workFile);
        _jobDetail.getJobDataMap().put("logbookFolder",_logbookFolder);
        _jobDetail.getJobDataMap().put("logname",_logname);
        _jobDetail.getJobDataMap().put("servletURL",_servletURL);
        _jobDetail.getJobDataMap().put("shift",_shift);
        _jobDetail.getJobDataMap().put("langCode",_langCode);
        _jobDetail.getJobDataMap().put("sleepTime",Settings.getSleepTime());

        String triggerInterval = null;

        // used to check if there was a missed shift
        // is only possible with Y,M,W or D settings
        boolean singleTrigger = true;

        // convert the normal shifts to cronnotation
        switch(_shift.toUpperCase().charAt(0))
        {
            //s m h dm m dw (y)
            case 'Y' : triggerInterval = "1 0 0 1 1 ?"; break;
            case 'M' : triggerInterval = "1 0 0 1 * ?"; break;
            case 'W' : triggerInterval = "1 0 0 ? * MON"; break;
            case 'D' : triggerInterval = "1 0 0 * * ?"; break;
            case '3' : 
                if (_shift.length()==1)
                {
                    triggerInterval = "0 0 7,15,23 * * ?"; 
                    break;
                }
                else
                {
                    triggerInterval = _shift; 
                    singleTrigger=false;
                    break;
                }                
            default : triggerInterval = _shift; singleTrigger=false; break;
        }

        // create trigger
        _cronTrigger = new CronTrigger(Settings.MAIN_CREATION_CRONJOB_NAME,_logname,triggerInterval);

        // single trigger is launched if shift is Y M W D or 3
        // then a missed shift can be created
        if (singleTrigger)
        {
            JobDetail sJobDetail = new JobDetail(Settings.SIMPLE_JOB_NAME,_logname,QuartzCronJob.class);
            sJobDetail.getJobDataMap().put("datapath",_datapath);
            sJobDetail.getJobDataMap().put("workFile",_workFile);
            sJobDetail.getJobDataMap().put("logbookFolder",_logbookFolder);
            sJobDetail.getJobDataMap().put("logname",_logname);
            sJobDetail.getJobDataMap().put("servletURL",_servletURL);
            sJobDetail.getJobDataMap().put("shift",_shift);
            sJobDetail.getJobDataMap().put("langCode",_langCode);
            sJobDetail.getJobDataMap().put("sleepTime",Settings.getSleepTime());
            long startTime = System.currentTimeMillis() + 3000L;                
            SimpleTrigger sTrigger = new SimpleTrigger(Settings.SIMPLE_JOB_NAME, _logname, new java.util.Date(startTime),null,0,0L);
            // is launched to get the last shift right
            SchedulerController.getInstance().scheduleJob(sJobDetail, sTrigger);        
        }
    }

    /**
     * start the job
     */
    private void startJob() throws SchedulerException 
    {
        SchedulerController.getInstance().scheduleJob(_jobDetail,_cronTrigger);
    }
    
    /**
     * stop the job
     */
    private void stopJob() throws SchedulerException
    {
        SchedulerController.getInstance().unscheduleJob(Settings.MAIN_CREATION_CRONJOB_NAME,_logname);
    }

    /**
     * returns true if the job is running
     * else returns false
     */
    private boolean isRunning()
    {
        try
        {
            String jobNames[] = SchedulerController.getInstance().getJobNames(_logname);
            for (int i = 0; i < jobNames.length; i++) 
            {
                if (jobNames[i].equals(Settings.MAIN_CREATION_CRONJOB_NAME)) 
                {
                    return true;
                }
            }        
            return false;
        }
        catch (SchedulerException ex)
        {
            return false;
        }
    }


    
}// class end
