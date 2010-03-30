package org.desy.logbook.cronjobs;

import org.desy.logbook.controller.TreeController;
import org.desy.logbook.helper.IOHelper;
import org.desy.logbook.controller.LogController;
import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * job that creates a new logbook shift and updates the 
 * javascript tree and the tree-data backup. It also creates
 * the init.xml files in the parent folders, with this
 * feature its possible to jump throu weeks in a daily shift
 * logbook
 * @author Johannes Strampe
 *
 */
public class QuartzCronJob implements Job {
       
    /**
     *
     */
    public QuartzCronJob() {}

    /** PARAMETER VALUES */

    // /var/www/ELOGDATA/ABCelog
    String datapath = "";
    // .../web/ABCelog/work.xml
    String workFile = "";
    // .../web/ABCelog
    String logbookFolder = "";
    // ABCelog
    String logname = "";
    //http://localhost:8080/newElog/Manager
    String servletURL = "";
    // Y|M|W|D|3
    String shift = "";
    // en de es
    String langCode = "";
    // time the thread sleeps before calling the reader script
    int sleepTime = 0;

    /** CALCULATED VALUES */

    // the isodate of the new shiftin month notation
    String monthtitle = "";
    // the isodate of the new shiftin week notation
    String weektitle = "";
    // the new date eg /2007/12/24
    String newShift = "";
    // the old date eg /2007/12/23
    String actualShift = "";

    
    
    String logText = "";
    
    /**
     * enter here when the job is launched ...
     * will create new shift and update tree and 
     * tree-data backup.
     * @param context
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext context) throws JobExecutionException 
    {
        try
        {
            readParameters(context.getJobDetail().getJobDataMap());

            log("cronjob for ".concat(logname));
            // thread waits to spread the starting time of many threads
            Thread.sleep(sleepTime);

            // get the actual shift from the work.xml
            readActualShift();

            calculateNewShift();

            // create only if the new shift doesn't exist already
            if (!existsNewShift())
            {
                writeNewShift();

                writeNewWorkfile();

                updateOldShift();

                // substring is used to remove the "/" prefix
                updateTree("name="+logname+"&add="+newShift.substring(1)+"&empty");

                // create a new treeData.xml
                TreeController.getInstance().createTreeXML(datapath,logbookFolder);
            }
            else
            {
                log("Shift already exists ! no creation.");
            }
        }
        catch (Exception ex)
        {
            log("Error occured while creating new shift. Error message ".concat(ex.toString()));
            log("Wait "+sleepTime+" ms");
            log("Datapath: ".concat(datapath));
            log("New shift: ".concat(newShift));
        }
        finally 
        {
            // always write info about the cronjob
            writeLogFile();
        }
        
    }

    /**
     * checks if a calculated shift already exists
     * @return true if the shift exists
     */
    private boolean existsNewShift()
    {
        File f = new File(datapath+newShift+"/init.xml");
        return f.exists();
    }

    /**
     * writes a log file to harddisk
     */
    private void writeLogFile()
    {
        LogController.getInstance().log(logText.concat("------------------"));
    }

    /**
     * saves a log message that can be viewed after execution
     * no need to add newlines.
     * @param message that will be saved. Newline is added after the message
     */
    private void log(String message)
    {
        logText += message.concat("\n");
    }
    
    /**
     * creates a new shift
     */
    private void writeNewShift() 
    {   
        //String content = "<metainfo>"+newShift+"</metainfo><pagetitle>"+title+"</pagetitle>";
        String content = "<metainfo>"+newShift+"</metainfo>";
        //content.concat("<pagetitle></pagetitle>");
        //if there is a month title
        if (!monthtitle.equals(""))
        {
            content = content.concat("<monthtitle>"+monthtitle+"</monthtitle>");
        }
        //if there is a week title
        if (!weektitle.equals(""))
        {
            content = content.concat("<weektitle>"+weektitle+"</weektitle>");
        }
        // actualShift does not exist when first entry is made
        if (actualShift.compareTo("")!=0) content += "<prev_shift>"+actualShift+"</prev_shift>";
        new File(datapath+newShift).mkdirs();
        if (!IOHelper.writeFile(datapath+newShift+"/init.xml", content))
        {
            log("Could not write new shift");
        }
        else
        {
            writeSubInitFiles(getParent(newShift),getParent(actualShift));
        }
    }
    
    /**
     * writes a new init file in the newshift and updates the actualshift.
     * These init files are used to get the previous month in a daily
     * shiftet logbook.
     * @param newDir new shift
     * @param actualDir actual shift
     */
    private void writeSubInitFiles(String newDir, String actualDir) 
    {
        // just start when init.xml doesn't exist already
        // do not create init file directly in /data folder !
        File newInitFile = new File(datapath+newDir+"/init.xml");
        if (!newInitFile.exists() && !newDir.equals(""))
        {
            // create an isodate title that will be shown in logbook
            String newMonthtitle = createMonthtitle(newDir, this.shift);
            String newWeektitle = createWeektitle(newDir, this.shift);

            String content = "<metainfo>"+newDir+"</metainfo>";
            //content.concat("<pagetitle></pagetitle>");
            //if there is a month title
            if (!newMonthtitle.equals(""))
            {
                content = content.concat("<monthtitle>"+newMonthtitle+"</monthtitle>");
            }
            //if there is a week title
            if (!newWeektitle.equals(""))
            {
                content = content.concat("<weektitle>"+newWeektitle+"</weektitle>");
            }
            
            // in some cases newDir and actualDir are be the same
            if (!newDir.equals(actualDir) && !actualDir.equals("")) 
            {
                content += "<prev_shift>"+actualDir+"</prev_shift>";
            }
            IOHelper.writeFile(datapath+newDir+"/init.xml", content);
            // update the previous init.xml
            // but only if actual is not newDir and not empty
            if (!actualDir.equals("")&&!newDir.equals(actualDir))
            {
                content = IOHelper.readFile(datapath+actualDir+"/init.xml");
                if (!content.equals(""))
                {
                    content += "<next_shift>"+newDir+"</next_shift>";
                    IOHelper.writeFile(datapath+actualDir+"/init.xml", content);
                }
            }
            // continue recursivly with parent folder
            writeSubInitFiles(getParent(newDir), getParent(actualDir));
        }
    }
    
    
    /**
     * creates a new workfile
     */
    private void writeNewWorkfile() 
    {
        String content = "<?xml version='1.0' encoding='ISO-8859-1' ?>" +
                "<work><act_dir>"+
                newShift+
                "</act_dir>";
        if (!actualShift.equals(""))
        {
            content += "<prev_dir>"+actualShift+"</prev_dir>";
        }

        // some special stuff, is possibly needed by some script
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(4);
        content +="<act_week_dir>/"
                + cal.get(Calendar.YEAR)
                + "/"
                + cal.get(Calendar.WEEK_OF_YEAR)
                + "</act_week_dir>";
        long oneWeekAgo = cal.getTimeInMillis()-604800000;
        cal.setTimeInMillis(oneWeekAgo);
        content +="<prev_week_dir>/"
                + cal.get(Calendar.YEAR)
                + "/"
                + cal.get(Calendar.WEEK_OF_YEAR)
                + "</prev_week_dir>";
        // special stuff end

        content += "</work>";
        if (!IOHelper.writeFile(workFile, content))
        {
            log("Could not write the new work.xml file");
        }
        
    }
    
    /**
     * adds the new "next_shift" to the last shift
     */
    private void updateOldShift() 
    {
        if(!actualShift.equals(""))
        {
            String content = IOHelper.readFile(datapath+"/"+actualShift+"/init.xml");
            content += "<next_shift>"+newShift+"</next_shift>";
            if (!IOHelper.writeFile(datapath+"/"+actualShift+"/init.xml", content))
            {
                log("Could not update the old init.xml of shift: "+actualShift);
            }
        }
    }
    
    /**
     * gets the actual shift out of the workfile
     */
    private void readActualShift()
    {
        String content = IOHelper.readFile(workFile);
        int begin = content.toLowerCase().indexOf("<act_dir>")+9;
        int end = content.toLowerCase().indexOf("</act_dir>");
        if (begin > 8 && end > -1)
        {
            actualShift = content.substring(begin,end);
        }
        else
        {
            log("Could not read act_dir");
        }
    }
    
    /**
     * Calculates the name and the path of the new shift
     */    
    private void calculateNewShift() 
    {        
        Calendar cal = Calendar.getInstance();
        // in us first day is sunday
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // to get the 52/53 week problem right
        cal.setMinimalDaysInFirstWeek(4);

        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH)+1;
        int w = cal.get(Calendar.WEEK_OF_YEAR);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        int dw = calendarDayToInt(cal.get(Calendar.DAY_OF_WEEK));
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int s = cal.get(Calendar.SECOND);
        String year = ""+y;
        String month = ""+m;
        String week = ""+w;
        String day = ""+d;
        String dayWeek = ""+dw;
        String hour = ""+h;
        String minute = ""+min;
        String second = ""+s;
        if (m<10) month = "0"+m;
        if (w<10) week = "0"+w;
        if (d<10) day = "0"+d;
        if (h<10) hour = "0"+h;
        if (min<10) minute = "0"+min;
        if (s<10) second = "0"+s;
        
        // shift is Y M W D 3 or a cronnotation
        char cShift = shift.toUpperCase().charAt(0);
        switch (cShift)
        {            
            case 'M' : 
            {
                newShift = "/"+year+"/"+month;
                monthtitle = year+"-"+month;
                break;
            }
            case 'W' : 
            {
                newShift = "/"+year+"/"+week;
                weektitle = year+"-W"+week;
                break;
            }
            case 'D' : 
            {
                newShift = "/"+year+"/"+week+"/"+day+"."+month;
                weektitle = year+"-W"+week+"-"+dayWeek;
                monthtitle = year+"-"+month+"-"+day;
                break;
            }
            case 'Y' : 
            {
                newShift = "/"+year;
                monthtitle = ""+year;
                break;
            }
            case '3' :
            {
                if (shift.length()==1)
                {
                    if(h>=7 && h<15)
                    {
                        newShift = "/"+year+"/"+week+"/"+day+"."+month+"_M";
                        weektitle = year+"-W"+week+"-"+dayWeek+"T07";
                        monthtitle = year+"-"+month+"-"+day+"T07";
                    }
                    else if(h>=15 && h<23)
                    {
                        newShift = "/"+year+"/"+week+"/"+day+"."+month+"_a";
                        weektitle = year+"-W"+week+"-"+dayWeek+"T15";
                        monthtitle = year+"-"+month+"-"+day+"T15";
                    }
                    else
                    {
                        // special case when manager is relaunched betwenn 0:00
                        // and 7:00 am. Then the shift belongs to the previous day
                        if (h<7)
                        {
                            cal.setTimeInMillis(cal.getTimeInMillis()-86400000);
                            y = cal.get(Calendar.YEAR);
                            m = cal.get(Calendar.MONTH)+1;
                            w = cal.get(Calendar.WEEK_OF_YEAR);
                            d = cal.get(Calendar.DAY_OF_MONTH);
                            dw = calendarDayToInt(cal.get(Calendar.DAY_OF_WEEK));
                            year = ""+y;
                            month = ""+m;
                            week = ""+w;
                            day = ""+d;
                            dayWeek = ""+dw;
                            if (m<10) month = "0"+m;
                            if (w<10) week = "0"+w;
                            if (d<10) day = "0"+d;
                        }

                        newShift = "/"+year+"/"+week+"/"+day+"."+month+"_n";
                        weektitle = year+"-W"+week+"-"+dayWeek+"T23";
                        monthtitle = year+"-"+month+"-"+day+"T23";
                    }
                    break;
                }
            }
            // default is a cron - notation
            default :
            {
                String cronVals[] = shift.split(" ");

                newShift = "/"+year;
                monthtitle = year;
                if(cronVals[4].charAt(0)!='1')
                {
                    newShift = "/"+year+"/"+month;
                    monthtitle = year+"-"+month;
                }
                if((cronVals[3].charAt(0)!='?' && cronVals[3].charAt(0)!='1') ||
                   cronVals[5].charAt(0)!='?')
                {
                    newShift = "/"+year+"/"+month+"/"+day;
                    monthtitle = year+"-"+month+"-"+day;
                    weektitle = year+"-W"+week+"-"+dayWeek;
                }
                if(cronVals[2].charAt(0)!='0')
                {
                    newShift = "/"+year+"/"+month+"/"+day+"/"+hour+minute;
                    monthtitle = year+"-"+month+"-"+day+"T"+hour+":"+minute;
                    weektitle = year+"-W"+week+"-"+dayWeek+"T"+hour+":"+minute;
                }
                if(cronVals[1].charAt(0)!='0')
                {
                    newShift = "/"+year+"/"+month+"/"+day+"/"+hour+minute;
                    monthtitle = year+"-"+month+"-"+day+"T"+hour+":"+minute;
                    weektitle = year+"-W"+week+"-"+dayWeek+"T"+hour+":"+minute;
                }
                if(cronVals[0].charAt(0)!='0')
                {
                    newShift = "/"+year+"/"+month+"/"+day+"/"+hour+minute+second;
                    monthtitle = year+"-"+month+"-"+day+"T"+hour+":"+minute+":"+second;
                    weektitle = year+"-W"+week+"-"+dayWeek+"T"+hour+":"+minute+":"+second;
                }               
            }            
        }
    }
        

    /**
     * Reads all the parameters that were passed to this job
     * @param jdm object where the parameters are hidden
     */
    private void readParameters(JobDataMap jdm)
    {
        try
        {            
            datapath = (String)jdm.get("datapath");
            workFile = (String)jdm.get("workFile");
            logbookFolder = (String)jdm.get("logbookFolder");
            logname = (String)jdm.get("logname");
            servletURL = (String)jdm.get("servletURL");
            shift = (String)jdm.get("shift");
            langCode = (String)jdm.get("langCode");
            sleepTime = Integer.parseInt(jdm.get("sleepTime").toString());
        }
        catch(Exception ex)
        {
            log("Error while reading parameters. Error was "+ex.toString());
        }        
    }

    /**
     * directly update the tree with a passed parameter
     * @param parameter describes the update to the tree
     * e.g. "name=ABCelog&add=2009/12/24&empty"
     */
    private void updateTree(String parameter) {
        TreeController.getInstance().getData(parameter);
    }

    /**
     * cuts the last folder of an directory path
     * @param path the path that should be cut
     * @return the cut path if exists or "" (empty string)
     */
    private String getParent(String path)
    {
        if (path==null) return "";
        int index = path.lastIndexOf("/");
        if (index>0 && index<path.length()-1) return path.substring(0, index);
        return "";
    }

    /**
     * Creates a week title. Result is empty
     * if the date cannot be displayed in
     * Isodate-Week notation. A year is not
     * displayed as weektitle.
     * @param path path of the created shift e.g. /2008/12/31
     * @param shift shift parameter e.g. 3 D W M Y
     * @return the title string
     */
    private String createWeektitle(String path, String shift) {

        if (path==null || shift==null)return "";

        // remove the first slash
        if (path.startsWith("/")) path = path.substring(1);

        // the year is always a monthtitle
        if (path.length()==4)
            return "";

        // shift 3 and d create week isodates
        if (shift.toLowerCase().equals("d") || shift.equals("3"))
        {
            if (path.length()==7)
                return path.substring(0, 4)+"-W"+path.substring(5);
            
        }
        else // other shifts create month isodates
        {
            // the path is like 2009/01/12
            // must be converted to 2009-Wxy-z
            // weekdate must be 2009-Wxy-zT... use it
            if (path.length()==10)
            {
                if (weektitle.length()>=10)
                    return weektitle.substring(0, 10);
            }
        }
        return "";
    }

    /**
     * Creates a month title. Result is empty
     * if the date cannot be displayed in
     * Isodate-Month notation. A year is displayed
     * as a monthtitle
     * @param path path of the created shift e.g. /2008/12/31
     * @param shift shift parameter e.g. 3 D W M Y
     * @return the title string
     */
    private String createMonthtitle(String path, String shift) {

        if (path==null || shift==null)return "";

        // remove the first slash
        if (path.startsWith("/")) path = path.substring(1);

        // the year is always a monthtitle
        if (path.length()==4)
            return path;

        // shift 3 and d create week isodates, they dont return months
        if (!shift.toLowerCase().equals("d") && !shift.equals("3"))
        {
            if (path.length()==10)
                return path.substring(0, 4)+"-"+path.substring(5,7)+"-"+path.substring(8);

            if (path.length()==7)
                return path.substring(0, 4)+"-"+path.substring(5);

            if (path.length()==4)
                return path;
        }
        return "";
    }

    /**
     * Converts the day constants from the Calendar class
     * into day of week numbers
     * e.g. Calendar.Monday will be 1
     * @param calendarDay
     * @return the number in the week
     */
    private int calendarDayToInt(int calendarDay)
    {
        switch (calendarDay)
        {
            case Calendar.MONDAY : return 1;
            case Calendar.TUESDAY : return 2;
            case Calendar.WEDNESDAY : return 3;
            case Calendar.THURSDAY : return 4;
            case Calendar.FRIDAY : return 5;
            case Calendar.SATURDAY : return 6;
            case Calendar.SUNDAY : return 7;
        }
        return 0;
    }

}