package org.desy.logbook.types;


/**
 * This class should be extended by other classes.
 * Its interface offers a getData method to send
 * data to the objects of this class. Every object
 * has a list of subelements which can be filled
 * with other objects of this class.
 * @author Johannes Strampe
 */
public abstract class DataComponent {

    private DataIterator subelements = new DataIterator();

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
    public abstract String getData(String request);


    /**
     * returns the id of this object
     * @return id of the element
     */
    public abstract String getId();

    /**
     * method which is used to pass a request to a
     * subelement. Request should start with the
     * id of a subelement. E.g. there are two sub-
     * elements "sub1" and "sub2". If the request
     * is "sub1/do something", then the getData()
     * of sub1 will be called with request "do
     * something".
     * @param request
     * @return the result of the subelemnts
     * getData() or empty string if element
     * doesn't exist
     */
    public String sendToSubElements(String request)
    {
        String reqId = request.split("/")[0];
        DataComponent item = subelements.getItemById(reqId);
        if(item!= null)
        {
            String newReq = request.replaceFirst(reqId,"");
            if(newReq.startsWith("/")) newReq = newReq.replaceFirst("/","");
            return item.getData(newReq);
        }
        return "";
    }

    /**
     * get the list of exisiting subelements
     * @return list of subelements, should never
     * be null
     */
    public DataIterator getSubelementList()
    {
        return subelements;
    }

}//class end
