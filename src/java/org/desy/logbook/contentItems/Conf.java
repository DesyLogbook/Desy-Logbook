package org.desy.logbook.contentItems;

import org.desy.logbook.controller.ConfValuesController;
import org.desy.logbook.helper.ConfFileHelper;
import org.desy.logbook.helper.IOHelper;
import org.desy.logbook.helper.XMLHelper;
import org.desy.logbook.settings.Settings;
import org.desy.logbook.types.DataComponent;


/**
 * This class represents a menu-entry of the
 * elogbook manager-app. It's able to show the
 * menu-content over the getData() method.
 * represents an editable conf file
 * @author Johannes Strampe
 */
public class Conf extends DataComponent{
    
    // saves the logbook name
    private String _logname = null;
    
    /**
     * Constructor which saves the logbook name
     * @param logname to identify the conf file
     */
    public Conf(String logname) {
        _logname = logname;
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

        String confPath = ConfValuesController.getInstance().getConf(_logname).getLogbook_path()+"/"+_logname+"/conf.xml";

        // return all elements this menu entry contains
        if (request.equals(""))
        {
            String content = IOHelper.readFile(confPath);
            String result = "";

            //check if conf has been read correctly
            if (content==null) result = "could not read XML ("+confPath+")";
            if (content.length()>4000) result = "XML is too big, edit it at "+confPath;

            if (!result.equals(""))
            {
                return XMLHelper.mkEntry("error",XMLHelper.mkLabel(result));
            }

            // mask some characters
            content = content.replaceAll(">", "&gt;");
            content = content.replaceAll("<", "&lt;");
            content = content.replaceAll("\"", "&quot;");

            content = XMLHelper.mkTextArea("conf",content);
            content += XMLHelper.mkCommand("Save conf", Settings.COMMAND_SAVE_WHOLE_CONF);
            result += XMLHelper.mkEntry(Settings.NEW_CONF_FILE_ELEMENT_ID,content);
            return result;
        }

        // here the request contains the xml file that should be saved
        // first remove the command-prefix from the request
        request = request.replaceFirst(Settings.NEW_CONF_FILE_ELEMENT_ID+"/", "");
        request = request.replaceFirst(Settings.COMMAND_SAVE_WHOLE_CONF+"/", "");
        // demask all characters that were masked by javascript
        request = request.replace("&gt", ">");
        request = request.replace("&lt", "<");
        request = request.replace("&s", " ");
        request = request.replace("&que", "?");
        request = request.replace("&e", "=");
        request = request.replace("&quo", "\"");
        request = request.replace("&d", ":");
        request = request.replace("&n", "\n");
        
        // check if xml is correct
        if (!ConfFileHelper.isCorrectXML(request))
        {
            return XMLHelper.mkLabel("Logbook was not saved, please enter correct xml");
        }
        else
        {
            // save the xml file
            IOHelper.writeFile(confPath, request);

            // reload the file we just saved and recreate
            // the elemts for the manager app menu
            String content = IOHelper.readFile(confPath);

            String result = "";
            if (content==null) result = "could not read XML ("+confPath+")";
            if (content.length()>4000) result = "XML is too big, edit it at "+confPath;

            if (!result.equals(""))
            {
                return XMLHelper.mkLabel(result);
            }

            // mask some characters
            content = content.replaceAll(">", "&gt;");
            content = content.replaceAll("<", "&lt;");
            content = content.replaceAll("\"", "&quot;");

            content = XMLHelper.mkTextArea("conf",content);
            content += XMLHelper.mkCommand("Save conf", Settings.COMMAND_SAVE_WHOLE_CONF);
            return content;
        }
    }

    /**
     * returns the id of this object
     * @return id of the element
     */
    public String getId() 
    {
        return "Conf";
    }

        
}//class end