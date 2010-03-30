package org.desy.logbook.contentItems;

import org.desy.logbook.controller.TreeController;
import org.desy.logbook.helper.XMLHelper;
import org.desy.logbook.settings.Settings;
import org.desy.logbook.types.DataComponent;

/**
 * This class represents a menu-entry of the
 * elogbook manager-app. It's able to show the
 * menu-content over the getData() method.
 * represents the tree info and client controller part
 * @author Johannes Strampe
 */
public class TreeInfo extends DataComponent{

    // name of the logbook to identify the tree
    private String _logname = "";

    // text which will be shown in the info part
    private String _reloadInfo = "";
    private String _createInfo = "";
    private String _statisticInfo = "";
    
    /**
     * Constructor which saves the logname
     * @param logname to identify the tree
     */
    public TreeInfo(String logname) 
    {
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
        
        String response = "";
        _statisticInfo = "";
        response = XMLHelper.mkCommand("reload",Settings.COMMAND_RELOAD_TREE);
        response += XMLHelper.mkCommand("create database",Settings.COMMAND_CREATE_TREE_DATABASE);

        // reloads the tree
        if (request.endsWith(Settings.COMMAND_RELOAD_TREE))
        {
            _reloadInfo = TreeController.getInstance().reload(_logname) + "\n";
            _statisticInfo = TreeController.getInstance().getStatistics(_logname);
            response += XMLHelper.mkInfo(_reloadInfo+_createInfo+_statisticInfo);
            return response;
        }

        // creates the treeData.xml but doesn't reload the tree
        if (request.endsWith(Settings.COMMAND_CREATE_TREE_DATABASE))
        {
            _createInfo = TreeController.getInstance().createTreeXML(_logname) + "\n";
            _statisticInfo = TreeController.getInstance().getStatistics(_logname);
            response += XMLHelper.mkInfo(_reloadInfo+_createInfo+_statisticInfo);
            return response;
        }


        _statisticInfo = TreeController.getInstance().getStatistics(_logname);
        response += XMLHelper.mkInfo(_reloadInfo+_createInfo+_statisticInfo);
        return XMLHelper.mkEntry(_logname,response);
    }

    /**
     * returns the id of this object
     * @return id of the element
     */
    public String getId() 
    {
        return "Tree";
    }
    
}//class end
