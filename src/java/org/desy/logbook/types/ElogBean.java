package org.desy.logbook.types;

import org.desy.logbook.controller.ConfValuesController;
import org.desy.logbook.helper.ConfFileHelper;
import java.io.File;

/**
 * This class is used as a session-bean. The standard getter
 * setter methods can be easily accessed by using JSP objects.
 * The first request to a logbook will create a new session
 * @author Johannes Strampe
 */
public class ElogBean {

    private String logName = "";
    private ConfValues confValues = null;
    private long workXMLTimestamp = -1;
    private boolean isSecure = false;
    private String actualDir = "";
    private String showDir = "";
    private String showPos = "";

    // <editor-fold defaultstate="collapsed" desc="The Bean properties">

    /**
     * Get the name of the logbook
     * @return name of the logbook
     */
    public String getLogName() {
        return logName;
    }

    /**
     * Set the name of the logbook. This will initialize
     * the bean.
     *
     * @param logName name of the logbook
     */
    public void setLogName(String logName) {
        if (!logName.equals(this.logName) || confValues==null)
        {
            this.logName = logName;
            init();
        }
    }

    /**
     * set directory which the tree will show first
     * @param showDir
     */
    public void setShowDir(String showDir) {
        this.showDir = showDir;
    }

    /**
     * set anker which the content frame will show first
     * @param showPos anker which the content frame will show first
     */
    public void setShowPos(String showPos) {
        this.showPos = showPos;
    }

    /**
     * get directory which the tree will show first
     * @return directory which the tree will show first
     */
    public String getShowDir() {
        return showDir;
    }

    /**
     * get anker which the content frame will show first
     * @return anker which the content frame will show first
     */
    public String getShowPos() {
        return showPos;
    }

    /**
     * get the title from the conf file
     * @return titletext
     */
    public String getTitle() {
        if (confValues!=null)
            return confValues.getTitle();
        return "";
    }

    /**
     * get all links from the conf file
     * @return link list
     */
    public ConfLinkValues[] getLinks() {
        if (confValues!=null)
            return confValues.getLinkList();
        return new ConfLinkValues[0];
    }

    /**
     * get all dropbox links from the conf file
     * @return link list
     */
    public ConfLinkValues[] getDropBoxItems() {
        if (confValues!=null)
            return confValues.getDropBoxList();
        return new ConfLinkValues[0];
    }

    /**
     * get all css files from the conf file
     * @return list with css files
     */
    public String[] getCss() {
        if (confValues!=null)
            return confValues.getCss();
        return new String[0];
    }

    /**
     * get all javascript files from the conf file
     * @return list with javascript files
     */
    public String[] getJs() {
        if (confValues!=null)
            return confValues.getJs();
        return new String[0];
    }

    /**
     * get the logo url
     * @return logo url
     */
    public String getLogo() {
        if (confValues!=null)
            return confValues.getLogo();
        return "";
    }

    /**
     * calculates the init parameters. The first tree call will be
     * different if a showDir parameter is set
     * @return init parameters for the tree javascript file
     */
    public String getInitParams() {
        if (confValues!=null)
        {
            String context = getContextWithSecureCheck();
            if (!getShowDir().equals(""))
            {
                String saveShowDir = getShowDir();
                setShowDir("");
                return "\'"+context+"\',\'"+getLogName()+"\',\'"+saveShowDir+"\',\'"+getShowPos()+"\'";
            }
            else
            {
                return "\'"+context+"\',\'"+getLogName()+"\',\'"+getActualDir()+"\',\'"+getShowPos()+"\'";
            }
            
        }
        else
            return "";
    }

    /**
     * read the actual dir from the work.xml. Will only be refreshed
     * if work.xml changed or if no act_dir is loaded
     * @return actual dir e.g. /2009/12/24
     */
    public String getActualDir() {
        if (confValues!=null)
        {
            File workXML = new File(confValues.getLogbook_path()+"/"+logName+"/work.xml");
            if (workXML!=null && workXML.lastModified() != workXMLTimestamp)
            {
                ConfFileHelper cfh = new ConfFileHelper(workXML);
                setActualDir(cfh.getElementValue("act_dir"));
                workXMLTimestamp = workXML.lastModified();
            }
        }
        return actualDir;
    }

    /**
     * set the actual dir
     * @param actualDir
     */
    public void setActualDir(String actualDir) {
        this.actualDir = actualDir;
    }

    /**
     * will change http: to https: if secure flag is set
     * @return url context e.g. http://localhost:8080/newElog
     */
    public String getContextWithSecureCheck() {
        if (confValues!=null)
        {
            String context = confValues.getContext();
            if (isIsSecure() && context.toLowerCase().startsWith("http:"))
                return context.replaceFirst("http:", "https:" );
            return context;
        }
        return "";
    }

    /**
     * will change http: to https: if secure flag is set
     * @return url where the shift data can be reached
     */
    public String getHostDataWithSecureCheck() {
        if (confValues!=null)
        {
            String context = confValues.getHost_data();
            if (isIsSecure() && context.toLowerCase().startsWith("http:"))
                return context.replaceFirst("http:", "https:" );
            return context;
        }
        return "";
    }

    /**
     * flag if connection is secure
     * @return
     */
    public boolean isIsSecure() {
        return isSecure;
    }

    /**
     * set flag if connection is secure
     * @param isSecure
     */
    public void setIsSecure(boolean isSecure) {
        this.isSecure = isSecure;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Methods">

    private void init()
    {
        this.confValues = ConfValuesController.getInstance().getConf(logName);
    }



    // </editor-fold>
}
