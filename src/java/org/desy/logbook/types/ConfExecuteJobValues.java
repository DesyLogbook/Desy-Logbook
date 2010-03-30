package org.desy.logbook.types;

/**
 * This class holds an execute job
 * information as it is saved in the
 * logbook specific conf.xml
 * @author Johannes Strampe
 */
public class ConfExecuteJobValues {

    /**
     * sets the values
     * @param time launchtime in cron notation
     * @param target target path
     */
    public ConfExecuteJobValues(String time, String target) {
            this.target = target;
            this.time = time;
    }

    private String time = "";
    private String target = "";

    /**
     * getter for the execution target
     * @return execution target path
     */
    public String getTarget() {
        return target;
    }

    /**
     * getter for the launch time
     * @return launch time in cron notation
     */
    public String getTime() {
        return time;
    }


}
