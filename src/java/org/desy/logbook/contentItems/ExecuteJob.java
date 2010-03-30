package org.desy.logbook.contentItems;

import org.desy.logbook.controller.SchedulerController;
import org.desy.logbook.cronjobs.QuartzExecuteJob;
import org.desy.logbook.controller.LogController;
import org.desy.logbook.helper.XMLHelper;
import java.text.ParseException;
import java.util.Date;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.desy.logbook.settings.Settings;
import org.desy.logbook.types.DataComponent;

/**
 * This class represents a menu-entry of the
 * elogbook manager-app. It's able to show the
 * menu-content over the getData() method.
 * The execute jobs, which can be declared in
 * the conf.xml files, are created here. They
 * also can be started and stopped.
 * @author Johannes Strampe
 */
public class ExecuteJob extends DataComponent{
    
    // variables to save the job state
    private String _logname = "";
    private String _target = "";
    private String _time = "";
    private String _jobName = "";
    // the filename that is executed e.g. "doSomething.sh"
    private String _executeFile = "";
    private JobDetail _jobDetail = null;
    private CronTrigger _cronTrigger = null;
    
    /**
     * Constructor. Will create and start an execute job
     * @param logname name of the logbook also used to generate a unique name
     * @param target command that will be executed
     * @param time contime when the job will launch
     */
    public ExecuteJob(String logname, String target, String time)
    {
        
        this._logname = logname;
        this._target = target;
        this._time = time;

        // try to calculate the filename of the execute command
        try {
            _executeFile = target.split(" ")[0];
            int last = _executeFile.split("/").length-1;
            _executeFile = _executeFile.split("/")[last];
        }
        catch (Exception ex)
        {
            if (_executeFile.equals("") )
                _executeFile = target;
        }
        
        // create and start job
        try
        {
            createJobName();
            createExcecutejob();
            startJob();
        }
        catch (Exception ex)
        {
            // when a job is not createt, running job will always return false
            LogController.getInstance().log(ex.toString());
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
        
        // start job
        if(request.equals(Settings.COMMAND_START_JOB))
        {
            try 
            {
                // must recreate job, elsewise it would force a launch on restart
                createExcecutejob();
                startJob();
            }
            catch (Exception ex) {LogController.getInstance().log(ex.toString());};
        }
        
        // stop job
        if(request.equals(Settings.COMMAND_STOP_JOB))
        {
            try {stopJob();}catch (Exception ex) {LogController.getInstance().log(ex.toString());};
        }
        
        String response = "";
        response += XMLHelper.mkLabel("Exec job");
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
        String info = "next launch:\n "+_cronTrigger.getFireTimeAfter(new Date())+"\n\n";
        info += "previous launch:\n "+SchedulerController.getInstance().getPrevLaunchTime(_jobName, _logname)+"\n\n";
        info += "target:\n "+_target+"\n\n";
        info += "crontime:\n "+_time+"\n\n";
        info += "jobname:\n "+_jobName+"\n\n";
        response += XMLHelper.mkInfo(info);
        response += XMLHelper.mkLabel("( "+_executeFile+" )");
        return response;
    }

    /**
     * returns the id of this object
     * @return id of the element
     */
    public String getId() 
    {
        return _jobName;
    }

    /**
     * create a unique job name
     * remove special characters, they are bad for the
     * html communication
     */
    private void createJobName()
    {
        _jobName = _logname+ _time+ _target;
        _jobName = _jobName.replace("/","");
        _jobName = _jobName.replace(" ", "");
        _jobName = _jobName.replace("?", "x");
        _jobName = _jobName.replace("*", "x");
        _jobName = _jobName.replace(",", "x");
        _jobName = _jobName.replace(".", "x");
    }
    
    /**
     * create an execute job
     */
    private void createExcecutejob() throws ParseException 
    {       
        _cronTrigger = new CronTrigger(_jobName,_logname,_time);
        _jobDetail = new JobDetail(_jobName,_logname,QuartzExecuteJob.class);
        // add the execute job parameter
        _jobDetail.getJobDataMap().put("targetPath",_target);
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
        SchedulerController.getInstance().unscheduleJob(_jobName,_logname);
    }
    
    /**
     * return true if the job is running
     * else it returns false
     */
    private boolean isRunning()
    {
        try
        {
            String jobNames[] = SchedulerController.getInstance().getJobNames(_logname);
            for (int i = 0; i < jobNames.length; i++) 
            {
                if (jobNames[i].equals(_jobName)) 
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
