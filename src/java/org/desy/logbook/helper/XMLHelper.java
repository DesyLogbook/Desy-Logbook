package org.desy.logbook.helper;
import org.desy.logbook.settings.Settings;

/**
 * Is used to create xml tags that can be interpreted
 * by the javascript of the clientside manager app.
 * Only static mehtods are offered
 * @author Johannes Strampe
 */
public class XMLHelper {
    
    /**
     * create an entry xml element with no subdir structure
     * @param id id of the entry
     * @param content content of the entry
     * @return xml code that can be interpreted by javascript
     * of the manager app
     */
    public static String mkEntry(String id, String content)
    {
        String attr[] = {Settings.ATTRIBUT_ID,id};
        return mkXMLEntry(Settings.ELEMENT_ENTRY,attr,content);        
    }
    
    /**
     * create an entry xml element with or without
     * a subdir structure
     * @param id id of the entry
     * @param content content of the entry
     * @param hasSub indicates if the entry has more subelements
     * that can be opened
     * @return xml code that can be interpreted by javascript
     * of the manager app
     */
    public static String mkEntry(String id, String content,boolean hasSub)
    {
        if(!hasSub)
            return mkEntry(id,content);
        
        String attr[] = {Settings.ATTRIBUT_ID,id,
                         Settings.ATTRIBUT_HAS_SUB,Settings.VALUE_TRUE};
        
        return mkXMLEntry(Settings.ELEMENT_ENTRY,attr,content);
    }
    
    /**
     * create a label xml element with passed label text
     * @param txt label text
     * @return xml code that can be interpreted by javascript
     * of the manager app
     */
    public static String mkLabel(String txt)
    {
        return mkXMLEntry(Settings.ELEMENT_LABEL,txt);
    }

    /**
     * create a command xml element with passed label text and commando code
     * @param label label text shown to the user
     * @param code command code that will be send to the manager app
     * @return xml code that can be interpreted by javascript
     * of the manager app
     */
    public static String mkCommand(String label, String code)
    {
        String attr[] = {Settings.ATTRIBUT_CODE,code};
        return mkXMLEntry(Settings.ELEMENT_COMMAND,attr,label);
    }
    
    /**
     * create a status xml element with the passed running status
     * @param isRunning flag if object is running or not
     * @return xml code that can be interpreted by javascript
     * of the manager app
     */
    public static String mkStatus(boolean isRunning)
    {
        if (isRunning)
        {
            String attr[] = {Settings.ATTRIBUT_RUNNING,Settings.VALUE_TRUE};
            return mkXMLEntry(Settings.ELEMENT_STATUS,attr,"");
        }
        else
        {
            String attr[] = {Settings.ATTRIBUT_RUNNING,Settings.VALUE_FALSE};
            return mkXMLEntry(Settings.ELEMENT_STATUS,attr,"");
        }
    }

    /**
     * create an edit xml element with passed id and content
     * @param id id of the entry
     * @param content text the editbox is filles with
     * @return xml code that can be interpreted by javascript
     * of the manager app
     */
    public static String mkEdit(String id, String content)
    {
        String attr[] = {Settings.ATTRIBUT_ID,id};
        return mkXMLEntry(Settings.ELEMENT_EDIT,attr,content);
    }

    /**
     *
     * @param id id of the entry
     * @param content
     * @return xml code that can be interpreted by javascript
     * of the manager app
     */
    public static String mkTextArea(String id, String content)
    {
        String attr[] = {Settings.ATTRIBUT_ID,id};
        return mkXMLEntry(Settings.ELEMENT_TEXT_AREA,attr,content);
    }
    
    /**
     * create an info xml element with passed content
     * @param content infotext
     * @return xml code that can be interpreted by javascript
     * of the manager app
     */
    public static String mkInfo(String content)
    {
        //content = replaceAllSpecialCharacters(content);
        return mkXMLEntry(Settings.ELEMENT_INFO,content);
    }
    
    /**
     * create a link xml element with passed label text and link url
     * @param label text shown to the user
     * @param link url the link points to
     * @return xml code that can be interpreted by javascript
     * of the manager app
     */
    public static String mkLink(String label, String link)
    {
        String attr[] = {Settings.ATTRIBUT_LINK,link};
        return mkXMLEntry(Settings.ELEMENT_LINK,attr,label);
    }
    
    /**
     * build a xml entry with the passed parameters
     */
    private static String mkXMLEntry(String tag, String[] attr, String content)
    {
        String result = "<" + tag;
        // attributes are listed as paires: name/value
        for (int i = 0; i < attr.length/2; i++) 
        {
            int pos=i*2;
            if(attr.length>pos+1)
            {
                result += " "+attr[pos]+"=\""+attr[pos+1]+"\"";
            }
        }
        result += ">" + content + "</" + tag + ">";
        return result;
    }
    
    /**
     * build a xml entry with the passed parameters
     */
    private static String mkXMLEntry(String tag, String content)
    {
        return "<" + tag + ">" + content + "</" + tag + ">";
    }
    
}
