package org.desy.logbook.types;

/**
 * This class represents a list of DataComponent
 * object and provides methods to work with
 * those objects
 * @author Johannes Strampe
 */
public class DataIterator {

    // list of DataComponents
    private DataComponent[] _list = null;
    
    
    /**
     * adds an item to the list
     * if item with existing id is added,
     * the old element is overwritten.
     * A null element will not be added
     * @param e item that will be added
     */
    public void addItem(DataComponent e)
    {
        if (e!=null)
        {
            if(getItemById(e.getId())!=null)
            {
                for (int i = 0; i < length(); i++)
                {
                    if (_list[i].getId().equals(e.getId()))
                    {
                        _list[i] = e;
                    }
                }
            }
            else // add a new element
            {
                DataComponent[] newList = new DataComponent[length()+1];
                for (int i = 0; i < length(); i++)
                {
                    newList[i] = _list[i];
                }
                newList[newList.length-1] = e;
                _list = newList;
            }
        }
    }
    
    
    /**
     * gets the number of items in the list
     * @return length of the list
     */
    public int length()
    {
        if (_list==null) return 0;
        return _list.length;
    }
    
    /**
     * not implemented yet
     * @param item
     */
    public void removeItem(DataComponent item)
    {
        // implement when needed
    }
    
    /**
     * removes all items from the list
     */
    public void clear()
    {
        _list = null;
    }
    
    /**
     * gets the item at the position "pos"
     * @param pos
     * @return the element or null if
     * index is out of bounds
     */
    public DataComponent itemAt(int pos)
    {
        if (pos<length() && pos>=0)
        {
            return _list[pos];
        }
        else return null;
    }
    
    /**
     * gets the item with the passed id
     * @param id of the item
     * @return item with the id or null if no
     * such item exists
     */
    public DataComponent getItemById(String id)
    {
        
        for (int i = 0; i < length(); i++) 
        {
            if (_list[i].getId().equals(id)) return _list[i];            
        }
        return null;        
    }
    
}//class end
