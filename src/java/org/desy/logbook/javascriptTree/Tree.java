package org.desy.logbook.javascriptTree;
import org.desy.logbook.controller.LogController;
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.desy.logbook.settings.Settings;
import org.desy.logbook.types.DataComponent;


       
/**
 * class to handle different eLogbook instances
 * 
 * getData() can have parameters :
 * add      : add a path to the actual Tree sctructure e.g.: 2008/04
 * fill     : will switch an entry to "not empty" e.g.: 2008/04
 * actual   : sends the actual version-timestamp of the loaded XML-doc
 * p1 & p2  : first path is what information client has and second path
 *            is information what client wants. if both are empty the first
 *            level is returned.
 */
public class Tree extends DataComponent {

    private Document doc = null;
    private long lastChange = 0;
    private String logname = null;
    private String dataPath;
    private boolean addEmpty = false;

    String css = "<style type=\"text/css\">" +
            ".success{background-color:#BBFFAA;}" +
            ".error{background-color:#FF9999;}" +
            "</style>";

    /**
     * the logname is set and the treedata is loaded
     *
     * @param name of the logbook
     * @param dataPath path to the folder where the treeData.xml
     * is located
     * @return true if the data could be loaded
     */
    public boolean init(String name,String dataPath)
    {
        this.logname = name;
        this.dataPath = dataPath;
        this.ladeDaten();
        return (doc != null);
    }

    /**
     * this function can manipulate the loaded tree
     * it was made synchronized because there were some and
     * we expected a thread problem while reading/writing the
     * tree. Hopefully this doesn't cause performance problems
     * doGet can have parameters :
     * add      : add a path to the actual Tree sctructure e.g.: add=2008/04
     * fill     : make an empty entry appear filled
     * actual   : sends the actual version-timestamp of the loaded XML-doc
     * p1 & p2  : first path is what information client has and second path
     *            is information what client wants. if both are empty the first
     *            level is returned.
     * @param request 
     * @return mostly xml that can be represented by the tree javascript
     * of the client
     */
    synchronized public String getData(String request)
    {   
        if (request==null) return "";
        
        if (request.equals("getStatistics"))
        {
            return getStatistics();
        }
        
        if (request.equals("getShortStatistics"))
        {
            return getShortStatistics();
        }
        
        String result = "";

        if (getParameter("fill",request)!=null)
        {
            try
            {
                String path = getParameter("fill",request);
                if (path.startsWith("/")) path = path.replaceFirst("/", "");
                fillEntry(path);
            }
            catch(Exception ex)
            {
                LogController.getInstance().log(ex.toString());
                return "<e>entry '"+getParameter("fill",request)+"' could not be filled</e>";
            } 
            return "<ok>entry '"+getParameter("fill",request)+"' has been filled</ok>";
        }
        
        if (getParameter("add",request)!=null)
        {
            try
            {
                addEmpty = (getParameter("empty",request)!=null);
                String path = getParameter("add",request);
                if (path.startsWith("/")) path = path.replaceFirst("/", "");
                addEntry(path);
                // in last change wird die uhrzeit der letzten
                // aenderung gespeichert
                lastChange = System.currentTimeMillis();
                //out.println("<div class=\"success\">new entry '"+request.getParameter("add")+"' has been added successfully</div>");
                return "<ok>new entry '"+getParameter("add",request)+"' has been saved</ok>";
            }
            catch(Exception ex)
            {
                LogController.getInstance().log(ex.toString());
                return "<e>new entry '"+getParameter("add",request)+"' could not be saved</e>";
            }               
            //return "<ok>new entry '"+getParameter("add",request)+"' has been saved</ok>";
        }
        else
        if (getParameter("actual",request)!=null)
        {
            //response.setContentType("application/xml");
            result = "<?xml version="+'"'+"1.0"+'"'+" encoding="+'"'+"UTF-8"+'"'+"?>";
            result = "<R>";
            result += "<V>";
            result += lastChange;
            result += "</V>";
            result += "</R>";
            return result;
        }
        else
        if (getParameter("p1",request)!=null &&
            getParameter("p2",request)!=null)
        {
            String p1 = getParameter("p1",request);
            String p2 = getParameter("p2",request);
            try
            {
                String subfolders = getSubfolders(p1,p2);                                        
                result = "<R>";
                result += subfolders;
                result += "<V>";
                result += lastChange;
                result += "</V>";
                result += "</R>";
            }
            catch(Exception ex)
            {
                LogController.getInstance().log(ex.toString());
                result = "<E>"+ex.getMessage()+"</E>";
            }
            return result;
        }
        return "<e>no parameters were passed</e>";
    }

    /**
     * id is the name of the logbook
     * @return
     */
    public String getId() 
    {
        return this.logname;
    }

    
    /**
     * returns some statistics about the 
     * the separated with new line breaks
     */
    private String getShortStatistics() 
    {
        String result = this.logname+":\n";
        if(doc==null) return result.concat("no data is loaded\n");
        int anzSub = doc.getElementsByTagName("S").getLength();
        int anzData = doc.getElementsByTagName("D").getLength();
        result += "folders: "+anzSub +"\nentries: "+anzData+"\n";
        //result += "\npath: "+ this.dataPath +"\n";
        return result;
    }
    
    /**
     * returns some statistics about the 
     * the separated with new line breaks
     */
    private String getStatistics()
    {

        String result = getShortStatistics();
        if (doc!=null) result += wholeTreeToString(doc.getDocumentElement());
        return result;
    }

    /**
     * die baumstruktur wird hier aus einer
     * xml datei in den zwischenspeicher geladen
     */
    private void ladeDaten()
    {
        try
        {
            Document newDoc;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
            DocumentBuilder builder  = factory.newDocumentBuilder();
            String path = "";
            path = dataPath + "/treeData.xml";
            newDoc = builder.parse( new File(path));
            doc=newDoc;
            lastChange = System.currentTimeMillis();
        }
        catch(Exception ex)
        {
            LogController.getInstance().log(ex.toString());
            System.out.println(ex.getMessage());
        }
    }  

    /**
     * client fragt nach inhalt eines unterordners
     * der wird hier berechnet und als ergebnis
     * zurueckgeliefert
     */
    private String getSubfolders(String para, String para2)
    {        
        String result = "";

        if (doc!=null)
        {            
            String pathList[] = para.split("/");

            String enter = para2.split("/")[0];

            Node root = doc.getDocumentElement();
            NodeList l = getNodeListByPath(root,pathList);

            //result += "<hallo>"+root.getChildNodes().getLength()+" para1="+para+" para2="+para2+"</hallo>";
            if (l==null) {return "";}

            for (int i = 0; i < l.getLength(); i++) 
            {   
                String name;
                String paraAndName;
                if (isSubfolderNode(l.item(i)))
                {

                    name = l.item(i).getAttributes().getNamedItem("n").getNodeValue();
                    if (name.compareTo(enter)==0)
                    {

                        result += "<S n="+"'"+"" + name + ""+"'"+">";
                        if(para.compareTo("")!=0)
                        {
                            result += getSubfolders(para+"/"+enter,removePrefix(name,para2));                            
                        }
                        else
                        {                            
                            result += getSubfolders(enter,removePrefix(name,para2));
                        }

                        result += "</S>";

                    }
                    else
                    {
                        result += "<S n="+"'"+"" + name + ""+"'"+"></S>";
                    }
                }
                if (isDataNode(l.item(i)))
                {   
                    name = l.item(i).getAttributes().getNamedItem("n").getNodeValue();
                    if(l.item(i).getAttributes().getNamedItem("e")==null)
                    {
                        result += "<D n="+"'"+"" + name + ""+"'"+"></D>";
                    }
                    else
                    {
                        result += "<D e='' n="+"'"+"" + name + ""+"'"+"></D>";
                    }
                }
            }            

        }
        return result;
    }

    /**
     * client fragt nach dem vorgaenger im dom baum
     * der wird hier ermittelt und als ergebnis
     * zurueckgeliefert
     */    
    private String getPrev(String para)
    {
        String result = "";
        String pathList[] = para.split("/");
        Node n = getNodeByPath(doc.getDocumentElement(),pathList);        
        if (n==null)
        {
            result = "";
        }
        else
        {
            n = calcPrev(n,0);
            result = nodePathToString(n);
        }
        return result;
    }

    /**
     * client fragt nach dem nachfolger im dom baum
     * der wird hier ermittelt und als ergebnis
     * zurueckgeliefert
     */    
    private String getNext(String para)
    {
        String result = "";
        String pathList[] = para.split("/");
        Node n = getNodeByPath(doc.getDocumentElement(),pathList);        
        if (n==null)
        {
            result = "";
        }
        else
        {
            n = calcNext(n,0);
            result = nodePathToString(n);
        }
        return result;
    }

    /**
     * gets a path an searches for the given element.
     * When element is a data-element it is overwritten
     * without the "empty" element.
     * On error nothing is changed
     * @param path the path describing the data element
     * that should be filled
     */
    private void fillEntry(String path) 
    {
        Node parent = doc.getDocumentElement();
        String pathList[] = path.split("/");
        // loop for all elements in the path string
        for (int i = 0; i < pathList.length-1; i++) 
        {
            if (parent!=null) parent = getChild(parent, pathList[i]);
        }
        
        // see if parent was found
        if (parent!=null)
        {
            // see if parent has the searched child and if the child is a data node
            Node existingChild = getChild(parent,pathList[pathList.length-1]);
            if (existingChild != null && isDataNode(existingChild))
            {
                //create new child
                Element newExistingChild = doc.createElement("D");
                NamedNodeMap nnm =existingChild.getAttributes();
                Node atr = nnm.getNamedItem("n");
                newExistingChild.setAttribute("n", atr.getNodeValue());
                // replace the old element with the "filled" one
                parent.replaceChild(newExistingChild, existingChild);
                // touch the change flag so that changes will be shown automatically
                lastChange = System.currentTimeMillis();
            }
        }
    }
    
    /**
     * erhaelt einen pfad der in das geladene
     * document eingefuegt wird, das letzte
     * element ist ein data node
     */
    private void addEntry(String path) 
    {
        Node parent = doc.getDocumentElement();
        String pathList[] = path.split("/");

        // loop for all elements in the path string
        for (int i = 0; i < pathList.length-1; i++) 
        {
            Node child = getChild(parent,pathList[i]);
            // element does not exists and will be created
            if(child==null)
            {
                Element newChild = doc.createElement("S");
                newChild.setAttribute("n",pathList[i]);
                // insert @ the sorted location
                Node smallerElement = findSmallerElement(parent,newChild);
                if (smallerElement==null)
                {
                    parent.appendChild(newChild);
                }
                else
                {
                    parent.insertBefore(newChild,smallerElement);
                }
                child=newChild;
            }
            // special case when a structure deeper than 1 is added
            // to a data node, the data node is switched to subfolder
            else if(child.getNodeName().equals("D"))
            {
                Element newChild = doc.createElement("S");
                newChild.setAttribute("n",pathList[i]);
                parent.insertBefore(newChild,child);
                parent.removeChild(child);
                child = newChild;
            }
            parent = child;
        }                       
        // the path has been passed and a leaf-node is created
        // check if the node doesnt already exist
        if(getChild(parent,pathList[pathList.length-1])==null)
        {
            // when data node gets a child it becomes a subfolder node
            if (isDataNode(parent))
            {                    
                String name = "";
                Node attr = parent.getAttributes().getNamedItem("n");
                if (attr != null)
                {
                    name = attr.getNodeValue();
                }
                Element newParent = doc.createElement("S");
                newParent.setAttribute("n",name);
                Node grandpa = parent.getParentNode();
                grandpa.removeChild(parent);
                grandpa.appendChild(newParent);
                parent = grandpa.getLastChild();
            }

            Element newChild = doc.createElement("D");
            newChild.setAttribute("n",pathList[pathList.length-1]);
            // is the new data file empty ?
            if (addEmpty)
            {
                newChild.setAttribute("e","");
            }
            Node smallerElement = findSmallerElement(parent,newChild);
            if (smallerElement==null)
            {
                parent.appendChild(newChild);
            }
            else
            {
                parent.insertBefore(newChild,smallerElement);
            }
        }
        /* // when node exists a possible "e" tag is removed
        {
            // create a new element without the "e" attribute
            Node existingChild = getChild(parent,pathList[pathList.length-1]);
            Element newExistingChild = doc.createElement("D");
            NamedNodeMap nnm =existingChild.getAttributes();
            Node atr = nnm.getNamedItem("n");
            newExistingChild.setAttribute("n", atr.getNodeValue());
            parent.replaceChild(newExistingChild, existingChild);
        }*/
    }   

    /**
     * findet heraus ob ein knoten ein ordner ist oder nicht
     */
    private boolean isSubfolderNode(Node n)
    {
        if (n != null)
        {
            return (n.getNodeType()==1 && n.getNodeName().equals("S"));        
        }
        else
        {
            return false;
        }
    }  

    /**
     * findet heraus ob ein knoten ein Datum ist oder nicht
     */
    private boolean isDataNode(Node n)
    {
       if (n != null)
        {
            return (n.getNodeType()==1 && n.getNodeName().equals("D"));
        }
        else
        {
            return false;
        }
    }

    /**
     * guckt ob der knoten n ein attribut name mit dem wert compare hat
     *
     */
    private boolean isCorrectElement(Node n, String compare)
    {
        try
        {
            return (n.getAttributes().getNamedItem("n").getNodeValue().compareTo(compare)==0);
        }
        catch(Exception ex)
        {
            return false;
        }
    }

    /**
     * der pfad in pathList wird in dem baum l durchlaufen
     * wenn pathList leer ist wird das element zurueckgeliefert
     */     
    private Node getNodeByPath(Node n, String[] pathList)
    {
        // exception vermeiden
        if (pathList.length == 0 || n == null)
        {
            return null;
        }

        // hier muss das element gesucht werden
        if (pathList.length==1)
        {
            NodeList l = n.getChildNodes();
            for (int i = 0; i < l.getLength(); i++)
            {
                Node testnode = l.item(i);
                if (isCorrectElement(testnode,pathList[0]))
                {
                    return testnode;
                }
            }
        }

        // hier wird der baum weiter rekursiv durchlaufen
        if (pathList.length>=1)
        {
            NodeList l = n.getChildNodes();            
            for (int i = 0; i < l.getLength(); i++) 
            {
                Node testnode = l.item(i);
                if (isSubfolderNode(testnode) && isCorrectElement(testnode,pathList[0]))
                {
                    String newList[] = new String[pathList.length-1];
                    for (int j = 0; j < newList.length; j++) 
                    {
                        newList[j]=pathList[j+1];                        
                    }
                    return getNodeByPath(testnode,newList);
                }
            }
            return null;
        }        

        // sollte nicht passieren
        return null;
    }

    /**
     * der pfad in pathList wird in dem baum l durchlaufen
     * wenn pathList leer ist wird der baum zurueckgeliefert
     */     
    private NodeList getNodeListByPath(Node n,String[] pathList)
    {

        if (pathList.length == 0 || pathList[0].compareTo("")==0)
        {
            return n.getChildNodes();
        }
        else
        {
            NodeList l = n.getChildNodes();
            for (int i = 0; i < l.getLength(); i++) 
            {
                Node testnode = l.item(i);                
                if (isSubfolderNode(testnode) && isCorrectElement(testnode,pathList[0]))
                {
                    String newList[] = new String[pathList.length-1];
                    for (int j = 0; j < newList.length; j++) 
                    {
                        newList[j]=pathList[j+1];                        
                    }
                    return getNodeListByPath(testnode,newList);
                }
            }
            return null;
        }       
    }

    /**
     * entfernt den Teilstring 'del' im String 'target'
     * und ein trennzeichen '/' falls ein uebrigbleibt
     * Beispiel :   del = 2008/12/5
     *              target = 2008
     *              return = 12/5
     */
    private String removePrefix(String del,String target)
    {
        if (del.compareTo(target)==0)
        {
            return "";
        }
        target = target.replaceFirst(del,"");
        if(target.charAt(0)=='/')
        {
            target = target.replaceFirst("/","");
        }
        return target;       
    }

    /**
     * verwandelt den pfad im Dom baum zum element n in 
     * eine Stringsequenz die durch "/" getrennt ist
     * und liefert diese als ergebnis zurueck
     */
    private String nodePathToString(Node n)
    {
        String result = "";
        if (n == null)
        {
            return result;
        }
        else
        {            
            if (isSubfolderNode(n)||isDataNode(n))
            {               
                result = n.getAttributes().getNamedItem("n").getNodeValue();
                n = n.getParentNode();
                while (isSubfolderNode(n)||isDataNode(n))
                {
                    result = n.getAttributes().getNamedItem("n").getNodeValue() + "/"+result;
                    n = n.getParentNode();
                }
            }
            return result;
        } 
    }

    /**
     * ermittelt die anzahl and element kindern eines
     * knotens
     */
    private int getNumOfElements(Node n)
    {
        int result = 0;
        NodeList children = n.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) 
        {
            if (children.item(i).getNodeType()==Node.ELEMENT_NODE)
            {
                result++;
            }
        }
        return result;
    }

    /**
     * liefert den letzten knoten in mit der tiefe "depth"
     */
    private Node getLastLeaf(Node n,int depth)
    {
        for (int i=0; i<depth;i++)
        {
            n=getChildElement(n,getNumOfElements(n)-1);
            if (n==null) return null;            
        }
        return n;
    }

    private Node getFirstLeaf(Node n,int depth)
    {
        for (int i=0; i<depth;i++)
        {
            n=getChildElement(n,0);
            if (n==null) return null;            
        }
        return n;
    }

    /**
     * ermittelt den unterknoten vom type element 
     * mit dem gegebenen index
     */
    private Node getChildElement(Node parent, int index)
    {
        NodeList children = parent.getChildNodes();        
        int pos = 0;
        for (int i=0; i<children.getLength();i++)
        {           
            if (children.item(i).getNodeType()==Node.ELEMENT_NODE)
            {                
                if (pos == index)
                {    
                    return children.item(i);
                }
                pos++;
            }
        }        
        return null;
    }

    /**
     * berechnet den vorgaenger im baum auf
     * der gleichen ebene
     */
    private Node calcPrev(Node n,int depth)
    {    
        // erster aufruf von calcPrev
        if (depth==0)
        {        
            int index = getIndex(n);        
            if (index==-1) return null;
            if (index!=0) return getChildElement(n.getParentNode(), index-1);//n.getParentNode().getChildNodes().item(index-1);
            return calcPrev(n.getParentNode(),1);
        }
        else
        {
            int index = getIndex(n);        
            if (index==-1) return null;
            if(index==0) return calcPrev(n.getParentNode(),depth+1);
            // es wird der letzte unterknoten des vaters ermittelt
            //Node result = getLastLeaf(n.getParentNode().getChildNodes().item(index-1),depth);
            Node result = getLastLeaf(getChildElement(n.getParentNode(),index-1),depth);
            if (result==null)
            {
                result = calcPrev(n.getParentNode(),depth+1);
            }
            return result;
        }       
    }

    /**
     * berechnet den nachfolger im baum auf
     * der gleichen ebene
     */
    private Node calcNext(Node n,int depth)
    {    
        // erster aufruf von calcPrev
        int max = getNumOfChilds(n.getParentNode())-1;        
        if (depth==0)
        {        
            int index = getIndex(n);        
            if (index==-1) return null;
            if (index<max) return getChildElement(n.getParentNode(), index+1);//n.getParentNode().getChildNodes().item(index-1);
            return calcNext(n.getParentNode(),1);
        }
        else
        {
            int index = getIndex(n);        
            if (index==-1) return null;
            if(index==max) return calcNext(n.getParentNode(),depth+1);
            // es wird der letzte unterknoten des vaters ermittelt
            //Node result = getLastLeaf(n.getParentNode().getChildNodes().item(index-1),depth);
            Node result = getFirstLeaf(getChildElement(n.getParentNode(),index+1),depth);
            if (result==null)
            {
                result = calcNext(n.getParentNode(),depth+1);
            }
            return result;
        }        
    }

    /**
     * berechnet den index des Knotens n in relation zum vater
     * liefert -1 bei fehler
     */
    private int getIndex(Node n)
    {
        //problem children.getLength() liefert nicht nur die element nodes sondern alle

        if (n == null || n.getParentNode() == null )
            return -1;
        NodeList children = n.getParentNode().getChildNodes();

        // root has parents ? it seems so
        if (!n.hasAttributes())
        {
            return -1;
        }

        String compare = n.getAttributes().getNamedItem("n").getNodeValue();
        int pos = 0;
        for (int i=0; i<children.getLength();i++)
        {            
            // es sollen nur die elemente ohne die attribute gezaehlt werden
            if (children.item(i).getNodeType()==Node.ELEMENT_NODE)
            {                
                if (isCorrectElement(children.item(i),compare))
                {    
                    return pos;
                }
                pos++;                    
            }
        }
        return -1;
    }

    /**
     * berechnet die anzahl an elementknoten
     * unter den kindelementen von n
     */
    private int getNumOfChilds(Node n)
    {
        if (n == null)
            return -1;
        NodeList children = n.getChildNodes();
        int result = 0;
        for (int i = 0; i < children.getLength(); i++) 
        {
            if (children.item(i).getNodeType()==Node.ELEMENT_NODE)
            {
                result++;
            }            
        }
        return result;
    }

    /**
     * durchlaeuft die kinder von n und sucht
     * nach einem element mit dem attribute 
     * 'name' == name
     */
    private Node getChild(Node n,String name)
    {
        if(n.hasChildNodes())
        {
            n=n.getFirstChild();
            while (n!=null)
            {
                if(this.isCorrectElement(n,name))
                {
                    return n;
                }
                n = n.getNextSibling();
            }
        }
        return null;
    }

    /**
     * durchlaeuft alle kinder von parent und liefert das erste
     * element was kleiner ist als newElement, oder null
     * dabei sind subfolder groesser als data elemente
     */
    private Node findSmallerElement(Node parent,Node newElement)
    {        
        NodeList children = parent.getChildNodes();
        boolean isSubFolderNew = isSubfolderNode(newElement);
        for (int i = 0; i < children.getLength(); i++) 
        {            

            Node item = children.item(i);            
            boolean isSubFolderCompare = (isSubfolderNode(item));
            // item could be a textnode or an attribute
            if (isSubFolderCompare || isDataNode(item))
            {
                // a folder is bigger that a file
                if (isSubFolderNew  && !isSubFolderCompare) return item;
                // if the 2 nodes have the same type compare them
                if (isSubFolderNew == isSubFolderCompare)
                {
                    String newVal = newElement.getAttributes().getNamedItem("n").getNodeValue();
                    String compareVal = item.getAttributes().getNamedItem("n").getNodeValue();
                    // if compare item is smaller return it
                    if (isBigger(newVal,compareVal)) return item;

                    //if (newVal.compareTo(compareVal)>0) return item;
                }
            }
        }
        return null;
    }

    /**
     * in TTF 01.03 is bigger than 28.02
     * 28.02 is bigger than 27.02
     * 28.02_n is bigger than 28.02_a
     * 28.02_a is bigger than 28.02_M
     * @param first first file
     * @param second second file
     * @return true if the first file is bigger than the second
     */
    private boolean isBigger(String first, String second) {
        if (first==null || second==null) return false;
        // when string is too short just do string comparison
        if (first.length()<5 || second.length()<5)
            return first.compareTo(second)>0;
        // compare month
        if(first.charAt(3)>second.charAt(3)) return true;
        if(first.charAt(3)<second.charAt(3)) return false;
        if(first.charAt(4)>second.charAt(4)) return true;
        if(first.charAt(4)<second.charAt(4)) return false;
        // compare day
        if(first.charAt(0)>second.charAt(0)) return true;
        if(first.charAt(0)<second.charAt(0)) return false;
        if(first.charAt(1)>second.charAt(1)) return true;
        if(first.charAt(1)<second.charAt(1)) return false;
        // when string is too short just do string comparison
        if (first.length()<7 || second.length()<7)
            return first.compareTo(second)>0;
        // compare shift
        if(first.charAt(6)=='n' && second.charAt(6)!='n') return true;
        if(first.charAt(6)!='n' && second.charAt(6)=='n') return false;
        if(first.charAt(6)=='a' && second.charAt(6)=='M') return true;
        if(first.charAt(6)=='M' && second.charAt(6)=='a') return false;
        return first.compareTo(second)>0;
    }

    /**
     * can be used for debug
     * prints the passed note, all
     * following notes and all
     * children
     */
    private String printNode(Node n)
    {
        String result = "";
        while(n!=null)
        {	
            result += n.getNodeType()+"\n";
            if(n.getNodeType() != 3)
            {			
                result += "knoten name : "+n.getNodeName()+"\n";
            }
            if(n.hasChildNodes())
            {
                result += "kind \n"+printNode(n.getFirstChild());
            }
            n=n.getNextSibling();
        }
        return result;
    }
    
    /**
     * Gets a parameter string like
     * para1=val1&para2=val2
     * and a parameter name (para1)
     * and returns the parameter value (val1)
     */
    private String getParameter(String paraName, String request) 
    {
        String arr[] = request.replaceAll("&","=").split("=");
        for (int i = 0; i < arr.length; i++) 
        {
            if (i%2==0)
            {
                if (arr[i].equals(paraName))
                {
                    if ((i+1)<arr.length)
                    {
                        return arr[i+1];
                    }
                    else
                    {
                        return "";
                    }
                    
                }
            }
        }
        return null;
    }
    
    /**
     * Converts the first part of the actual loaded tree into
     * a string.
     * (with new line breaks)
     * (masked tags < = &lt;)
     */
    private String wholeTreeToString(Node n)
    {
        int TREE_MAX_SIZE = Settings.MAX_TREE_TO_STRING_LENGTH;
        String result = "";
        if (doc==null) return "no Tree loaded";
        //Node root = doc.getDocumentElement();        
        NodeList l = n.getChildNodes();
        if (l==null) {return "Tree is empty";}

        for (int i = 0; i < l.getLength(); i++) 
        {
            if (result.length()>TREE_MAX_SIZE)
            {
                return result+ " ... (rest of tree was cut out)";
            }
            if (isSubfolderNode(l.item(i)))
            {

                String name = l.item(i).getAttributes().getNamedItem("n").getNodeValue();
                result += "&lt;S n="+"'"+"" + name + ""+"'"+"&gt;\n";
                result += wholeTreeToString(l.item(i));
                if (result.endsWith("... (rest of tree was cut out)"))
                {
                    return result;
                }
                result += "&lt;/S&gt;\n";

            }
            if (isDataNode(l.item(i)))
            {   
                String name = l.item(i).getAttributes().getNamedItem("n").getNodeValue();
                if(l.item(i).getAttributes().getNamedItem("e")==null)
                {
                    result += "&lt;D n="+"'"+"" + name + ""+"'"+"&lt;/D&gt;\n";
                }
                else
                {
                    result += "&lt;D e='' n="+"'"+"" + name + ""+"'"+"&lt;/D&gt;\n";
                }
            }
        }        
        return result;
    }

    

}// treeSubServlet class end

