package org.desy.logbook.controller;

import org.desy.logbook.helper.ConfFileHelper;
import java.io.File;
import java.util.HashMap;
import org.desy.logbook.settings.Settings;
import org.desy.logbook.types.ConfLinkValues;
import org.desy.logbook.types.ConfValues;
import org.desy.logbook.types.ConfExecuteJobValues;

/**
 * The conf file values of all logbooks and the
 * whole logbook application are stored in this controller.
 * It is used by many other classes, so it has to load
 * correctly.
 * This class is a singleton class, the constructor
 * is private and the only object of the class can
 * be reached through the getInstance() method.
 * @author Johannes Strampe
 */
public class ConfValuesController {

    private static ConfValuesController _instance = null;

    /**
     * private constructor
     * reads all conf files
     */
    private ConfValuesController() {
        readAllConfs();
    }

    /**
     * static method to get the singleton object
     * @return the only instance of this class
     */
    public static synchronized ConfValuesController getInstance()
    {
        if (_instance==null) _instance = new ConfValuesController();
        return _instance;
    }

    /**
     * unique id of the conf.xml which belongs to the whole logbook app
     */
    public final String GENERAL_CONF = "__general_conf_file__";

    // map used to store the conf files
    private HashMap map = null;


    /**
     * removes all stored conf files
     */
    public void clear() {
        if (map==null) map = new HashMap();
        map.clear();
    }

    /**
     * Reads the general conf file in the context path. Then
     * obtains the logbook path and reads the conf files of
     * the logbooks. All existing conf files are cleared first.
     */
    synchronized public void readAllConfs()
    {
        // this is a vital variable. It must be set by the first Servlet hat
        // gets a call from a client.
        String contextPath = System.getProperty(Settings.GLOBAL_CONTEXT_PATH);
        if (!(contextPath==null))
        {
            // remove all existing conf files
            this.clear();

            // read the general conf
            File generConf = new File(contextPath+"/conf/conf.xml");
            if (generConf!=null && generConf.exists())
            {
                readConf(GENERAL_CONF,generConf);

                if (getGeneralConf()!=null &&
                    getGeneralConf().getContext_path().equals(""))
                {
                    getGeneralConf().setContext_path(contextPath);
                }
            }

            // get the logbook path from the general conf
            String logbookPath = "";
            if (getGeneralConf()!=null)
                logbookPath = getGeneralConf().getLogbook_path();

            // read all the logbooks
            File f = new File(logbookPath);
            if (f!=null && f.isDirectory())
            {
                File list[] = f.listFiles();
                for (int i = 0; i < list.length; i++)
                {
                    File confFile = new File(list[i].getAbsolutePath()+"/conf.xml");
                    if(confFile.exists())
                    {
                        readConf(list[i].getName(),confFile);
                    }
                }
            }
        }
    }// init end

    /**
     * returns the conf values of the logbook
     * @param name Name of the logbook
     * @return object holding the conf values
     */
    public ConfValues getConf(String name)
    {
       if (map!=null && map.containsKey(name))
           return (ConfValues)map.get(name);
       else
           return null;
    }

    /**
     * returns the conf values of the whole context
     * @return class holding the conf values
     */
    public ConfValues getGeneralConf()
    {
       return getConf(GENERAL_CONF);
    }

    /**
     * reloads a single conf file of a logbook
     * @param name of the logbook
     */
    public void reloadConf(String name)
    {
        ConfValues generalConf = getGeneralConf();
        if (generalConf!=null)
        {
            String logbookPath = generalConf.getLogbook_path();
            File confFile = new File(logbookPath+"/"+name+"/conf.xml");
            if (confFile!=null && confFile.exists())
            {
                readConf(name, confFile);
            }
        }
    }

    /**
     * gets the names of all the logbooks which
     * are stored in this controller. General
     * conf is not included.
     * @return Array with the names of all loaded logbooks
     */
    public String[] getAllConfNames() {
        if (map == null) return new String[0];

        Object keys[] = map.keySet().toArray();

        if (getGeneralConf()!=null && keys.length>0)
        {
            String result[] = new String[keys.length-1];
            int pos = 0;
            for (int i = 0; i < keys.length; i++) {
                if (!keys[i].equals(GENERAL_CONF))
                {
                    result[pos] = (String) keys[i];
                    pos = pos + 1;
                }
            }
            return result;
        }

        String stringKeys[] = new String[keys.length];
        for (int i = 0; i < stringKeys.length; i++) {
            stringKeys[i] = (String) keys[i];

        }
        
        return stringKeys;
    }

    /**
     * reads a conf.xml file and stores it. The general
     * conf will always provide default settings for the
     * non existent values.
     * @param logname to identify the logbook
     * @param confFile where the information is saved
     */
    private void readConf(String logname, File confFile)
    {
        ConfFileHelper cfh = new ConfFileHelper(confFile);
        ConfValues cv = null;

        // if the conf already exists values will be overwritten
        if (map.containsKey(logname))
        {
            cv = (ConfValues) map.get(logname);
            map.remove(logname);
        }
        // if conf doesnt exist and a general conf is loaded
        // the values of the general conf are set as default
        // and are the overwritten
        else if (map.containsKey(GENERAL_CONF))
            cv = ((ConfValues) map.get(GENERAL_CONF)).makeCopy();
        // else this must be the general conf file
        else
            cv = new ConfValues();

        /* read the simple values and simple lists */
        if (cfh.getElementsByTag("allowIp").length>0)
            cv.setAllowIp(cfh.getElementsByTag("allowIp"));
        if (cfh.getElementsByTag("css").length>0)
            cv.setCss(cfh.getElementsByTag("css"));
        if (cfh.getElementsByTag("js").length>0)
            cv.setJs(cfh.getElementsByTag("js"));
        if (cfh.getElementsByTag("keyword_list").length>0)
            cv.setKeyword_list(cfh.getElementsByTag("keyword"));
        if (cfh.getElementsByTag("location_list").length>0)
            cv.setLocation_list(cfh.getElementsByTag("location"));
        if (cfh.getElementsByTag("neededRole").length>0)
            cv.setNeededRole(cfh.getElementsByTag("neededRole"));
        if (cfh.getElementsByTag("rejectIp").length>0)
            cv.setRejectIp(cfh.getElementsByTag("rejectIp"));
        if (!cfh.getElementValue("logo").equals(""))
            cv.setLogo(cfh.getElementValue("logo"));
        if (!cfh.getElementValue("context").equals(""))
            cv.setContext(cfh.getElementValue("context"));
        if (!cfh.getElementValue("context_path").equals(""))
            cv.setContext_path(cfh.getElementValue("context_path"));
        if (!cfh.getElementValue("logbook_path").equals(""))
            cv.setLogbook_path(cfh.getElementValue("logbook_path"));
        if (!cfh.getElementValue("datapath").equals(""))
            cv.setDatapath(cfh.getElementValue("datapath"));
        if (!cfh.getElementValue("edit_servlet").equals(""))
            cv.setEdit_servlet(cfh.getElementValue("edit_servlet"));
        if (!cfh.getElementValue("edit_xsl").equals(""))
            cv.setEdit_xsl(cfh.getElementValue("edit_xsl"));
        if (!cfh.getElementValue("host_data").equals(""))
            cv.setHost_data(cfh.getElementValue("host_data"));
        if (!cfh.getElementValue("lang_code").equals(""))
            cv.setLang_code(cfh.getElementValue("lang_code"));
        if (!cfh.getElementValue("name").equals(""))
            cv.setName(cfh.getElementValue("name"));
        if (!cfh.getElementValue("new_shift").equals(""))
            cv.setNew_shift(cfh.getElementValue("new_shift"));
        if (!cfh.getElementValue("pdf_xsl").equals(""))
            cv.setPdf_xsl(cfh.getElementValue("pdf_xsl"));
        if (!cfh.getElementValue("search2_xsl").equals(""))
            cv.setSearch2_xsl(cfh.getElementValue("search2_xsl"));
        if (!cfh.getElementValue("search_xsl").equals(""))
            cv.setSearch_xsl(cfh.getElementValue("search_xsl"));
        if (!cfh.getElementValue("title").equals(""))
            cv.setTitle(cfh.getElementValue("title"));
        if (!cfh.getElementValue("tree_servlet").equals(""))
            cv.setTree_servlet(cfh.getElementValue("tree_servlet"));
        if (!cfh.getElementValue("view_servlet").equals(""))
            cv.setView_servlet(cfh.getElementValue("view_servlet"));
        if (!cfh.getElementValue("view_xsl").equals(""))
            cv.setView_xsl(cfh.getElementValue("view_xsl"));

        /* read the complex list-values */
        cv.setExecuteJobsList(readExecuteJobs(cfh));
        cv.setLinkList(readLinkList(cfh));
        cv.setDropBoxList(readDropBoxList(cfh));

        /* read the boolean values */
        if (cfh.getAttributesByTag("location_list", "enabled").length>0 &&
            cfh.getAttributesByTag("location_list", "enabled")[0].toLowerCase().equals("true"))
            cv.setIsLocaltionListEnabled(true);

        if (cfh.getAttributesByTag("spellchecker", "enabled").length>0 &&
            cfh.getAttributesByTag("spellchecker", "enabled")[0].toLowerCase().equals("true"))
            cv.setIsSpellcheckerEnabled(true);

        if (cfh.getAttributesByTag("mail2expert", "enabled").length>0 &&
            cfh.getAttributesByTag("mail2expert", "enabled")[0].toLowerCase().equals("true"))
            cv.setIsMailToExpertEnabled(true);

        if (cfh.getAttributesByTag("shiftsummary", "enabled").length>0 &&
            cfh.getAttributesByTag("shiftsummary", "enabled")[0].toLowerCase().equals("true"))
            cv.setIsShiftSummaryEnabled(true);

        if (cfh.getAttributesByTag("view_history", "enabled").length>0 &&
            cfh.getAttributesByTag("view_history", "enabled")[0].toLowerCase().equals("true"))
            cv.setIsViewHistoryEnabled(true);

        if (cfh.getAttributesByTag("edit_enable", "enabled").length>0 &&
            cfh.getAttributesByTag("edit_enable", "enabled")[0].toLowerCase().equals("true"))
            cv.setIsEditEnableEnabled(true);

        // save the conf
        map.put(logname, cv);
    }

    /**
     * reads the execute jobs elements
     * @param cfh object with has access to the conf data
     * @return list of all execute jobs
     */
    private ConfExecuteJobValues[] readExecuteJobs(ConfFileHelper cfh) {
        String times[] = cfh.getElementsByTag("time");
        String targets[] = cfh.getElementsByTag("target");
        if (times.length == targets.length)
        {
            ConfExecuteJobValues ejv[] = new ConfExecuteJobValues[times.length];
            for (int i = 0; i < targets.length; i++) {
                ejv[i] = new ConfExecuteJobValues(times[i], targets[i]);
            }
            return ejv;
        }
        else
        {
            return new ConfExecuteJobValues[0];
        }
    }

    /**
     * reads the links, that are show on the logbook startpage
     * @param cfh object with has access to the conf data
     * @return list of all links
     */
    private ConfLinkValues[] readLinkList(ConfFileHelper cfh) {
        String targets[] = cfh.getAttributesByTag("link","target");
        String labels[] = cfh.getAttributesByTag("link","label");
        if (labels.length == targets.length)
        {
            ConfLinkValues ejv[] = new ConfLinkValues[labels.length];
            for (int i = 0; i < targets.length; i++) {
                ejv[i] = new ConfLinkValues(targets[i],labels[i]);
            }
            return ejv;
        }
        else
        {
            return new ConfLinkValues[0];
        }
    }

    /**
     * reads the links, that are show as a dropbox
     * on the logbook startpage
     * @param cfh object with has access to the conf data
     * @return list of all links
     */
    private ConfLinkValues[] readDropBoxList(ConfFileHelper cfh) {
        String targets[] = cfh.getAttributesByTag("item","target");
        String labels[] = cfh.getAttributesByTag("item","label");
        if (labels.length == targets.length)
        {
            ConfLinkValues ejv[] = new ConfLinkValues[labels.length];
            for (int i = 0; i < targets.length; i++) {
                ejv[i] = new ConfLinkValues(targets[i],labels[i]);
            }
            return ejv;
        }
        else
        {
            return new ConfLinkValues[0];
        }
    }

}//class end
