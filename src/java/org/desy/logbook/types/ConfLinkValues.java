package org.desy.logbook.types;

/**
 * class which stores the information
 * to create a html link
 * @author Johannes Strampe
 */
public class ConfLinkValues {

    private String target = "";
    private String label = "";

    /**
     * constructor which stores the parameters
     * @param target url where the link points
     * @param label text of the link
     */
    public ConfLinkValues(String target, String label) {
        this.label = label;
        this.target = target;
    }

    /**
     * label getter
     * @return label text
     */
    public String getLabel() {
        return label;
    }

    /**
     * target getter
     * @return target url
     */
    public String getTarget() {
        return target;
    }


}
