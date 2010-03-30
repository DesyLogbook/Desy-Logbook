package org.desy.logbook.controller;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.desy.logbook.settings.Settings;

/**
 * Controlls access to the quartz scheduler which
 * is used to run cron and execute jobs
 * This class is a singleton class, the constructor
 * is private and the only object of the class can
 * be reached through the getInstance() method.
 * main controller to manage the quartz scheduler
 * @author Johannes Strampe
 */
public class SchedulerController implements JobListener, TriggerListener {
    
    private static SchedulerController _Instance = null;
    private static Scheduler _scheduler = null;
    
    /**
     * static method to get the singleton object
     * @return the only instance of this class
     */
    public static synchronized SchedulerController getInstance()
    {
        if (_Instance == null) _Instance = new SchedulerController();
        return _Instance;
    }
    
    /**
     * private constructor
     * starts the scheduler
     */
    private SchedulerController() 
    {
        startScheduler();
        if (Settings.LOG_ALL_SCHEDULER_ACTIONS)
        {
            try {

                _scheduler.addGlobalJobListener(this);
                _scheduler.addGlobalTriggerListener(this);

            } catch (SchedulerException ex) {
                LogController.getInstance().log("Could not add Listeners "+ex.toString());
            }
        }
    }

    /**
     * clears all jobs
     */
    public void clear() {
        stopAllJobs();
        _scheduler = null;
    }
    
    
    /**
     * stop all jobs and clean up resources
     */
    public void stopAllJobs()
    {
        Scheduler scheduler = getScheduler();
        try {
            String triggerGroups[] = scheduler.getTriggerGroupNames();
            boolean result;
            for (int i = 0; i < triggerGroups.length; i++)
            {
                String triggerNames[] = scheduler.getTriggerNames(triggerGroups[i]);
                for (int j = 0; j < triggerNames.length; j++)
                {
                    result = scheduler.unscheduleJob(triggerNames[j], triggerGroups[i]);
                    LogController.getInstance().log("unscheduling " + triggerNames[j]+ " " + triggerGroups[i] + " ("+result+")");
                }
            }
            // false means, dont wait for jobs to complete
            scheduler.shutdown(false);
            scheduler = null;
        } catch (Exception ex) {
            LogController.getInstance().log(ex.toString());
        }
    }
    
    
    
    /**
     * starts a new scheduler
     * does nothing when a scheduler is running
     */
    private static void startScheduler() 
    {
        try
        {            
            // start scheduler if this has not happened yet
            if(_scheduler==null) _scheduler = new StdSchedulerFactory().getScheduler();
            if(!_scheduler.isStarted()) _scheduler.start();
        } 
        catch (Exception ex)
        {
            LogController.getInstance().log(ex.toString());
        }        
    }

    /**
     * Add the given JobDetail to the Scheduler, and associate the given Trigger with it.
     * @param jobDetail details of the job
     * @param trigger contains the launchtime
     * @throws SchedulerException job with trigger can not be scheduled
     */
    public void scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException 
    {
        Scheduler scheduler = getScheduler();
        if (scheduler!=null)
        {
            scheduler.scheduleJob(jobDetail, trigger);
        }
    }
    
    /**
     * Remove the indicated Trigger from the scheduler.
     * @param triggerName to identify the trigger
     * @param triggerGroup to identify the trigger
     * @throws SchedulerException can not find or unschedule job
     */
    public void unscheduleJob(String triggerName, String triggerGroup) throws SchedulerException 
    {
        Scheduler scheduler = getScheduler();
        if (scheduler!=null)
        {
            scheduler.unscheduleJob( triggerName, triggerGroup);
        }
    }

    /**
     * Get the names of all the JobDetails in the given group. The two groupnames
     * should be indicated in the Settings.java
     * @param groupName 
     * @return names of all the jobs from the group
     * @throws SchedulerException
     */
    public String[] getJobNames(String groupName) throws SchedulerException 
    {
        Scheduler scheduler = getScheduler();
        if (scheduler!=null)
        {
            return scheduler.getJobNames(groupName);
        }
        return new String[0];
    }
    
    /**
     * Gives a description of all scheduled jobs
     * @return description of all scheduled jobs
     */
    public String toString()
    {
        Scheduler scheduler = getScheduler();
        if (scheduler==null) return "scheduler is null";
        try 
        {
            if (!scheduler.isStarted()) return "the scheduler is not started";
            String result ="";        
            result += "context: " + _scheduler.getContext().toString();
            result += "\nscheduler name: "+_scheduler.getSchedulerName();
            result += "\nscheduler metadata : "+_scheduler.getMetaData().toString();
            
            /*String triggerGroups[] = _scheduler.getTriggerGroupNames();
            for (int i = 0; i < triggerGroups.length; i++)
            {
                String triggerNames[] = _scheduler.getTriggerNames(triggerGroups[i]);
                for (int j = 0; j < triggerNames.length; j++)
                {
                    result += "\nTrigger: "+triggerNames[j]+" "+triggerGroups[i];
                }
                
            }*/
            result += "\n\nCurrently running jobs:\n" +
                    "Jobname - Groupname\n" +
                    "-------------------";
            String groups[] = scheduler.getJobGroupNames();
            for (int i = 0; i < groups.length; i++)
            {
                String jobs[] = scheduler.getJobNames(groups[i]);
                for (int j = 0; j < jobs.length; j++)
                {
                    result += "\n"+jobs[j]+ " - "+ groups[i];
                }
            }
            return result;
        }
        catch (Exception ex) 
        {
            LogController.getInstance().log(ex.toString());
            return "error while getting scheduler information";
        }
    }
    
    /**
     * stops all jobs from one logbook
     * @param logname Should be the groupname of the job
     */
    public void stopJobsFromLogbook(String logname)
    {
        Scheduler scheduler = getScheduler();
        try
        {
            String jobNames[] = scheduler.getJobNames(logname);
            for (int i = 0; i < jobNames.length; i++)
            {
                scheduler.unscheduleJob(jobNames[i],logname);
            }
        }
        catch (Exception ex)
        {
            LogController.getInstance().log(ex.toString());
        }
    }

    /**
     * gets a message when the last launch happend
     * @param job to identify the job
     * @param group to identify the job
     * @return message when the last launch happend
     */
    public String getPrevLaunchTime(String job, String group)
    {
        Scheduler scheduler = getScheduler();
        try {
            Trigger[] t = scheduler.getTriggersOfJob(job, group);
            if (t.length>0)
            if (t[0].getPreviousFireTime()==null)
                return "Not launched since server startup";
            else return t[0].getPreviousFireTime().toString();
        } catch (SchedulerException ex) {
            LogController.getInstance().log(ex.toString()+" SchedulerController.java 190");
        }
        return "Error while computing previous launch-time";
    }

    /**
     * getter for the scheduler, if scheduler is null a new one
     * will be created
     * @return scheduler instance
     */
    private Scheduler getScheduler()
    {
        if (_scheduler==null) startScheduler();
        return _scheduler;
    }

    //@Override
    public String getName() {
        return "MyListener";
    }

    //@Override
    public void jobToBeExecuted(JobExecutionContext arg0) {
        String result = "jobToBeExecuted: "
               + arg0.getJobDetail().getName()+" "+arg0.getFireTime().toString()
               + " - "+arg0.getScheduledFireTime().toString()
               + " recovering?"+arg0.isRecovering()
               + " RC:"+arg0.getRefireCount()
               + " dur:"+arg0.getJobRunTime();
        LogController.getInstance().log(result);
    }

    //@Override
    public void jobExecutionVetoed(JobExecutionContext arg0) {
        LogController.getInstance().log("jobExecutionVetoed: "+arg0.toString());
    }

    //@Override
    public void jobWasExecuted(JobExecutionContext arg0, JobExecutionException arg1) {
        String result = "jobWasExecuted: "
               + arg0.getJobDetail().getName()+" "+arg0.getFireTime().toString()
               + " "+arg0.getScheduledFireTime().toString()
               + " recovering?"+arg0.isRecovering()
               + " RC:"+arg0.getRefireCount()
               + " dur:"+arg0.getJobRunTime()
               + arg1.toString();
        LogController.getInstance().log(result);
    }

    //@Override
    public void triggerFired(Trigger arg0, JobExecutionContext arg1) {
        String result = "triggerFired: "
               + arg0.getName()+" "+arg0.getMisfireInstruction()
               + " "+arg1.getFireTime().toString()+" - "+arg1.getScheduledFireTime().toString()
               + " recovering?"+arg1.isRecovering()
               + " RC:"+arg1.getRefireCount()
               + " dur:"+arg1.getJobRunTime();
        LogController.getInstance().log(result);
    }

    //@Override
    public boolean vetoJobExecution(Trigger arg0, JobExecutionContext arg1) {
        // we dont want to veto (prevent execution) any jobs
        return false;
    }

    //@Override
    public void triggerMisfired(Trigger arg0) {
        LogController.getInstance().log("triggerMisfired: "+arg0.toString());
    }

    //@Override
    public void triggerComplete(Trigger arg0, JobExecutionContext arg1, int arg2) {
        String result = "triggerComplete: "    
               + arg0.getName()+" "+arg0.getMisfireInstruction()
               + " "+arg1.getFireTime().toString()+" - "+arg1.getScheduledFireTime().toString()
               + " recovering?"+arg1.isRecovering()
               + " RC:"+arg1.getRefireCount()
               + " dur:"+arg1.getJobRunTime()
               + " arg2:"+arg2;
        LogController.getInstance().log(result);
   }

}
