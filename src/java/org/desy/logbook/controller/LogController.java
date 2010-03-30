package org.desy.logbook.controller;

import org.desy.logbook.helper.*;
import java.util.Calendar;
import java.util.Date;
import org.desy.logbook.settings.Settings;

/**
 * This controller stores the logmessages that can
 * be placed from anywhere in the logbook app. Logtext
 * can also be stored in a .log file on the filesystem
 * This class is a singleton class, the constructor
 * is private and the only object of the class can
 * be reached through the getInstance() method.
 * @author Johannes Strampe
 */
public class LogController {
    
    /**
     * above this number the log will be cut to the cut length
     */
    private static int MAX_LENGTH = 3800;
    /**
     * when max length is reached log is shortened to this length
     */
    public static int CUT_LENGTH = 2000;
    private static int MAX_SHOW_LENGTH = 3800;
    private static LogController _instance = null;
    private String _logContent = "\nbeginning log "+new Date().toString()+"\n";
    private String _timerMessage = "";
    private long _startTime = 0;
    private long _differenceTime = 0;

    /**
     * private constructor
     */
    private LogController() {
        
    }
    
    /**
     * static method to get the singleton object
     * @return the only instance of this class
     */
    public static synchronized LogController getInstance() {
        if (_instance == null) {
            _instance = new LogController();
        }
        return _instance;
    }
    
    
    /**
     * Saves a log message that can be viewed in the Manager Servlet
     * @param message should be passed without a linebreaker
     */
    public void log(String message) {
        message = replaceAllSpecialCharacters(message);
        Calendar cal = Calendar.getInstance();
        String dateString = "";
        if (cal.get(Calendar.HOUR_OF_DAY)<10) dateString+="0"+cal.get(Calendar.HOUR_OF_DAY);
        else dateString += cal.get(Calendar.HOUR_OF_DAY);
        if (cal.get(Calendar.MINUTE)<10) dateString+=":0"+cal.get(Calendar.MINUTE);
        else dateString += ":"+cal.get(Calendar.MINUTE);
        dateString+= " "+cal.get(Calendar.DAY_OF_MONTH)+'.'
                   + (cal.get(Calendar.MONTH)+1)+'.'
                   + cal.get(Calendar.YEAR);
        
        
            
        _logContent = dateString+' '+message+"\n"+_logContent;
        if (_logContent.length()>MAX_LENGTH)
        {
            writeLogFile(false);
        }
        
    }
    
    /**
     * starts the timer and stores the message. Be
     * sure that only 1 thread uses this method
     * at a time
     * @param message asoziated with this timer
     */
    public void timerStart(String message)
    {
        _timerMessage=replaceAllSpecialCharacters(message);
        _startTime = System.currentTimeMillis();
    }
    
    /**
     * timer stops and message is saved with the timing result
     * to the log
     */
    public void timerStop()
    {
        _differenceTime = System.currentTimeMillis()-_startTime;
        log(_timerMessage+" "+_differenceTime+"ms");
    }
    
    /**
     * gets the logged messages
     * @return String representing the logged messages
     */
    public String getLog() 
    {
        if (_logContent.length()<=MAX_SHOW_LENGTH) return _logContent;
        return _logContent.substring(0, MAX_SHOW_LENGTH);
    }
    
    /**
     * replaces all problematic html characters with its mask
     * @param content String with characters that should be masked or null
     * @return String with the masked characters
     */
    private String replaceAllSpecialCharacters(String content) 
    {
        if (content==null) return "";
        content = content.replace("&", "&amp;");
        content = content.replace("<", "&lt;");
        content = content.replace(">", "&gt;");
        return content;
    }
    
    /**
     * saves the current logtext into a local log file
     * @param completely indicates if the complete 
     * logtext is saved or just a part of it
     */
    public void writeLogFile(boolean completely)
    {
        
        String messageToSave = "";
        String messageToKeep = "";
        if (completely)
        {
            messageToKeep = "beginning log "+new Date().toString();
            messageToSave = _logContent;
        }
        else
        {
            if (_logContent.length()>CUT_LENGTH)
            {
                messageToKeep = _logContent.substring(0, CUT_LENGTH);
                messageToSave = _logContent.substring(CUT_LENGTH);
            }
            else
            {
                messageToKeep = _logContent;
            }
        }
        
        _logContent = messageToKeep;
        String content = IOHelper.readFile(Settings.LOGFILE);
        // the last message is first on the log
        content = messageToSave.concat(content);
        
        if (content.length()>Settings.LOGFILE_MAXSIZE)
        {
            IOHelper.moveFile(Settings.LOGFILE,Settings.LOGFILE+System.currentTimeMillis()+".bak");
            content = messageToSave;
        }
        IOHelper.writeFile(Settings.LOGFILE, content);
    }
}
