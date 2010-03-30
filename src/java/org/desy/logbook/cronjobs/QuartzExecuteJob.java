package org.desy.logbook.cronjobs;

import org.desy.logbook.controller.LogController;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author Johannes Strampe
 *
 * Job that makes an execute call
 *
 */
public class QuartzExecuteJob implements Job{
    
    String targetPath;

    /**
     * empty constructor
     */
    public QuartzExecuteJob()
    {
        
    }
    
    /**
     * enter here when the job is launched ...
     * parameters to the job are read and target path
     * is executed
     * @param context
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext context) throws JobExecutionException 
    {
        readParameters(context.getJobDetail().getJobDataMap());
        executeTarget();        
    }

    /**
     * reads parameters that are passed to this job
     * @param jdm where the parameters are hidden
     */
    private void readParameters(JobDataMap jdm)
    {
        try
        {            
            targetPath = (String)jdm.get("targetPath");            
        }
        catch(Exception ex)
        {
            LogController.getInstance().log(ex.toString());
        }  
    }

    /**
     * executes the targetPath
     */
    private void executeTarget() 
    {
        
        try 
        {
            Process p;
            p = Runtime.getRuntime().exec(targetPath);
            p.waitFor();
        } 
        catch (Exception ex) 
        {
            LogController.getInstance().log(ex.toString());
        }
       
    }
    
}