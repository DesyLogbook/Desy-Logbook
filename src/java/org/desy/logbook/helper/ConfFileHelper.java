package org.desy.logbook.helper;

import org.desy.logbook.controller.LogController;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Helper class to work with a conf.xml file.
 * Helps reading conf values.
 * @author Johannes Strampe
 */
public class ConfFileHelper {

    // XML document
    private Document doc = null;
    private String _confPath = null;
    
    /** Creates a new instance of ConfFileHelper
     * @param confFile
     */
    public ConfFileHelper(File confFile) 
    {
        try
        {
            // load the xml to the doc variable
            _confPath = confFile.getAbsolutePath();
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();            
            DocumentBuilder builder = factory.newDocumentBuilder();            
            doc = builder.parse( confFile );
        }
        catch (Exception ex)
        {
            LogController.getInstance().log(ex.toString());
        }        
    }

    /**
     * Get the path of the loaded xml file.
     * @return path of the xml file or null
     * if no file was loaded
     */
    public String getPath()
    {
        return this._confPath;
    }

    /**
     * gets all elements with the passed tagname
     * and returns the content values as a 
     * string list
     * @param tagName xml tag
     * @return content of each tag as an array of strings
     */
    public String[] getElementsByTag(String tagName)
    {
        try
        {
            NodeList l = doc.getElementsByTagName(tagName);
            String result[] = new String[l.getLength()];
            for (int i = 0; i < l.getLength(); i++) 
            {
                result[i]=l.item(i).getFirstChild().getNodeValue();
            }
            return result;
        }
        catch(Exception ex)
        {
            return new String[0];
        }
    }

    /**
     * get all elements with the passed tagname
     * and return the content of the attribute 
     * if such an attribute exist as a string list.
     * e.g. <someTag someAttribute="some value"></someTag>
     * now you can get "some value" with the call
     * getAttributesByTag("someTag","someAttribute")
     * @param tagName xml tag
     * @param attributeName name of the tags attribute
     * @return all attribute values as a list
     */
    public String[] getAttributesByTag(String tagName,String attributeName)
    {
        try
        {
            NodeList l = doc.getElementsByTagName(tagName);
            int resultCount = 0;
            for (int i = 0; i < l.getLength(); i++)
            {
                if (l.item(i).getAttributes().getNamedItem(attributeName)!=null)
                    resultCount++;
            }
            String result[] = new String[resultCount];
            int actual_pos = 0;
            for (int i = 0; i < resultCount; i++)
            {
                if (l.item(i).getAttributes().getNamedItem(attributeName)!=null)
                {
                    result[actual_pos] = l.item(i).getAttributes().getNamedItem(attributeName).getNodeValue();
                    actual_pos++;
                }
            }

            return result;
        }
        catch(Exception ex)
        {
            return new String[0];
        }
    }



    /**
     * gets the tag names of all first level
     * children of the root element. Useful
     * to get all first level elements of the
     * conf.xml file. Result is a string list
     * @return name of all children of the
     * root tag
     */
    public String[] getAllElementTags()
    {
        
        try {
            NodeList children = doc.getFirstChild().getChildNodes();
            int pos = 0;
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    pos++;
                }
            }
            String result[] = new String[pos];
            pos = 0;
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    result[pos] = children.item(i).getNodeName();
                    pos++;
                }
            }
            return result;
        }
        catch (Exception e)
        {
            LogController.getInstance().log(e.toString());
            return new String[0];
        }
    }
    
    

    
    /**
     * converts the first element with the
     * passed tag into a string. Open and 
     * close tags are masked
     * children are part of the result.
     * return "" when elem does not exist.
     * @param elem xml tag of the wanted element
     * @return the element and all subelements and
     * values with masked tag symbols as a string
     */
    public String getElementAsString(String elem)
    {
        try {
            NodeList l = doc.getElementsByTagName(elem);
            if (l != null && l.getLength() > 0) {
                return nodeToString(l.item(0), true);
            } else {
                return "";
            }
        }
        catch (Exception e)
        {
            LogController.getInstance().log(e.toString());
            return "";
        }
    }
    
    /**
     * gets a node and converts it to a string.
     * open and close tags can be masked.
     * All children are also part of the result
     * with recursive calls
     * @param n node that should be converted
     * @param maskTags true if tag symbols should be masked
     * @return string representation of the xml element
     */
    private String nodeToString(Node n,boolean maskTags)
    {        
        String openTag;
        String closeTag;
        // set the mask values
        if(maskTags)
        {
            openTag = "&lt;";
            closeTag = "&gt;";
        }
        else
        {
            openTag = "<";
            closeTag = ">";
        }
        // comment nodes
        if(n.getNodeType() == Node.COMMENT_NODE)
        {
            return openTag + "!--" +
                    n.getNodeValue() +
                    "--" + closeTag;
                    
        }

        // text nodes
        if (n.getNodeType() == Node.TEXT_NODE)
        {
            return n.getTextContent();
        }

        // complex nodes with possible recursion
        if (n.getNodeType() == Node.ELEMENT_NODE)
        {
            String result = openTag + n.getNodeName();
            NamedNodeMap attr = n.getAttributes();
            for (int i = 0; i < attr.getLength(); i++) 
            {
                result += " "+attr.item(i).getNodeName() 
                        + "=\"" 
                        + attr.item(i).getNodeValue()+"\"";
            }
            result += closeTag;
            NodeList children = n.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) 
            {
                result += nodeToString(children.item(i),maskTags);
            }
            result += openTag+"/"+n.getNodeName()+closeTag;
            return result;
        }

        //unsupported node type
        return "";
    }
    
    /**
     * gets the node value of the first
     * element with the passed tag
     * returns "" when elem does not exist
     * @param elemName xml tag of the searched element
     * @return content of the element or "" if element
     * doesn't exist
     */
    public String getElementValue(String elemName)
    {
        try
        {
            NodeList l = doc.getElementsByTagName(elemName);
            Node n = l.item(0);
            n = n.getFirstChild();
            String s = n.getNodeValue();
            if (s==null) return "";
            return s;
        }
        catch(Exception ex)
        {
            return "";
        }
    }

    /**
     * if one or more elements with the given name exist, the text
     * content of the first one is set to elemValue.
     * if no element with the given name exists yet a new element
     * below the root element is created
     * @param elemName
     * @param elemValue
     */
    public void updateOrCreateTextElement(String elemName, String elemValue)
    {
        NodeList l = doc.getElementsByTagName(elemName);
        if (l.getLength()>0)
        {
            l.item(0).setTextContent(elemValue);
        }
        else
        {
            Element newChild = doc.createElement(elemName);
            newChild.setTextContent(elemValue);
            doc.getDocumentElement().appendChild(newChild);
        }
    }

    /**
     * static mehtod to check if a string contains
     * a 'valid' xml file
     * @param xml string to be checked
     * @return true if the string contains 'valid' xml
     */
    public static boolean isCorrectXML(String xml)
    {
        Document xmlDoc = null;
        try
        {
            // load the xml in the doc variable
            DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            byte xmlBytes[] = xml.getBytes();
            ByteArrayInputStream stream = new ByteArrayInputStream(xmlBytes);
            xmlDoc = builder.parse(stream);
        }
        catch (Exception ex)
        {
            return false;
        }
        return xmlDoc!=null;
    }

    /**
     * saves all changes to the current conf file. Creates
     * a backup file (conf.xml.bak)
     * @return status if saving was successfull
     */
    public boolean save()
    {
        try {
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(new DOMSource(doc.getDocumentElement()), streamResult);

            String oldConf = IOHelper.readFile(_confPath);
            IOHelper.writeFile(_confPath+".bak", oldConf);

            return IOHelper.writeFile(_confPath, stringWriter.toString());
        } catch (Exception e) {
            LogController.getInstance().log(e.toString());
            return false;
        }
    }


}
