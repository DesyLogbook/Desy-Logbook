package org.desy.logbook.types;

/**
 * This class holds all values that can be placed
 * in conf.xml files. There are two different types
 * of conf files:
 * - a general one located in /jsp/conf.xml
 * - several logbook specific ones in /XXXelog/conf.xml
 * where XXX is the name of the logbook
 * specific values should override the general ones
 * @author Johannes Strampe
 */
public class ConfValues {



    /**
     * return a new object with the same values
     * as this one (clone)
     * @return clone
     */
    public ConfValues makeCopy()
    {
        ConfValues result = new ConfValues();
        result.setAllowIp(allowIp);
        result.setContext(context);
        result.setContext_path(context_path);
        result.setLogbook_path(logbook_path);
        result.setCss(css);
        result.setDatapath(datapath);
        result.setEdit_servlet(edit_servlet);
        result.setEdit_xsl(edit_xsl);
        result.setHost_data(host_data);
        result.setJs(js);
        result.setKeyword_list(keyword_list);
        result.setLang_code(lang_code);
        result.setLinkList(linkList);
        result.setDropBoxList(dropBoxList);
        result.setLocation_list(location_list);
        result.setName(name);
        result.setNeededRole(neededRole);
        result.setNew_shift(new_shift);
        result.setLogo(logo);
        result.setPdf_xsl(pdf_xsl);
        result.setRejectIp(rejectIp);
        result.setSearch2_xsl(search2_xsl);
        result.setSearch_xsl(search_xsl);
        result.setTitle(title);
        result.setTree_servlet(tree_servlet);
        result.setView_servlet(view_servlet);
        result.setView_xsl(view_xsl);
        result.setExecuteJobsList(executeJobsList);
        result.setIsLocaltionListEnabled(isLocaltionListEnabled);
        result.setIsMailToExpertEnabled(isMailToExpertEnabled);
        result.setIsShiftSummaryEnabled(isShiftSummaryEnabled);
        result.setIsSpellcheckerEnabled(isSpellcheckerEnabled);
        result.setIsViewHistoryEnabled(isViewHistoryEnabled);
        result.setIsEditEnableEnabled(isEditEnableEnabled);
        
        return result;
    }

    /* more generall conf values */
    private String[] css = new String[0];
    private String[] js = new String[0];
    private String context = "";
    private String context_path = "";
    private String logbook_path = "";
    private String host_data = "";
    private String datapath = "";
    private String lang_code = "";
    private String tree_servlet = "";
    private String view_servlet = "";
    private String edit_servlet = "";
    private String view_xsl = "";
    private String edit_xsl = "";
    private String search_xsl = "";
    private String search2_xsl = "";
    private String pdf_xsl = "";
    /* more logbook specific conf values */
    private String[] allowIp = new String[0];
    private String[] rejectIp = new String[0];
    private String[] neededRole = new String[0];
    private String title = "";
    private String name = "";
    private String new_shift = "";
    private String logo = "";
    private String[] keyword_list = new String[0];
    private String[] location_list = new String[0];
    private ConfExecuteJobValues[] executeJobsList = new ConfExecuteJobValues[0];
    private ConfLinkValues[] linkList = new ConfLinkValues[0];
    private ConfLinkValues[] dropBoxList = new ConfLinkValues[0];


    /* values of the old logbook which are needed to use the
       xsl-files. Are those values really needed the way they
       are declared ??? */
    private boolean isLocaltionListEnabled = false;
    private boolean isSpellcheckerEnabled = false;
    private boolean isMailToExpertEnabled = false;
    private boolean isViewHistoryEnabled = false;
    private boolean isShiftSummaryEnabled = false;
    private boolean isEditEnableEnabled = false;

    // <editor-fold defaultstate="collapsed" desc="setter">

    /**
     * standard setter
     * @param allowIp
     */
    public void setAllowIp(String[] allowIp) {
        this.allowIp = allowIp;
    }

    /**
     * standard setter
     * @param context
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * standard setter
     * @param context_path
     */
    public void setContext_path(String context_path) {
        this.context_path = context_path;
    }

    /**
     * standard setter
     * @param logbook_path
     */
    public void setLogbook_path(String logbook_path) {
        this.logbook_path = logbook_path;
    }

    /**
     * standard setter
     * @param css
     */
    public void setCss(String[] css) {
        this.css = css;
    }

    /**
     * standard setter
     * @param datapath
     */
    public void setDatapath(String datapath) {
        this.datapath = datapath;
    }

    /**
     * standard setter
     * @param edit_servlet
     */
    public void setEdit_servlet(String edit_servlet) {
        this.edit_servlet = edit_servlet;
    }

    /**
     * standard setter
     * @param edit_xsl
     */
    public void setEdit_xsl(String edit_xsl) {
        this.edit_xsl = edit_xsl;
    }

    /**
     * standard setter
     * @param host_data
     */
    public void setHost_data(String host_data) {
        this.host_data = host_data;
    }

    /**
     * standard setter
     * @param js
     */
    public void setJs(String[] js) {
        this.js = js;
    }

    /**
     * standard setter
     * @param keyword_list
     */
    public void setKeyword_list(String[] keyword_list) {
        this.keyword_list = keyword_list;
    }

    /**
     * standard setter
     * @param lang_code
     */
    public void setLang_code(String lang_code) {
        this.lang_code = lang_code;
    }

    /**
     * standard setter
     * @param location_list
     */
    public void setLocation_list(String[] location_list) {
        this.location_list = location_list;
    }

    /**
     * standard setter
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * standard setter
     * @param neededRole
     */
    public void setNeededRole(String[] neededRole) {
        this.neededRole = neededRole;
    }

    /**
     * standard setter
     * @param new_shift
     */
    public void setNew_shift(String new_shift) {
        this.new_shift = new_shift;
    }

    /**
     * standard setter
     * @param logo
     */
    public void setLogo(String logo) {
        this.logo = logo;
    }

    /**
     * standard setter
     * @param pdf_xsl
     */
    public void setPdf_xsl(String pdf_xsl) {
        this.pdf_xsl = pdf_xsl;
    }

    /**
     * standard setter
     * @param rejectIp
     */
    public void setRejectIp(String[] rejectIp) {
        this.rejectIp = rejectIp;
    }

    /**
     * standard setter
     * @param search2_xsl
     */
    public void setSearch2_xsl(String search2_xsl) {
        this.search2_xsl = search2_xsl;
    }

    /**
     * standard setter
     * @param search_xsl
     */
    public void setSearch_xsl(String search_xsl) {
        this.search_xsl = search_xsl;
    }

    /**
     * standard setter
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * standard setter
     * @param tree_servlet
     */
    public void setTree_servlet(String tree_servlet) {
        this.tree_servlet = tree_servlet;
    }

    /**
     * standard setter
     * @param view_servlet
     */
    public void setView_servlet(String view_servlet) {
        this.view_servlet = view_servlet;
    }

    /**
     * standard setter
     * @param view_xsl
     */
    public void setView_xsl(String view_xsl) {
        this.view_xsl = view_xsl;
    }

    /**
     * standard setter
     * @param executeJobsList
     */
    public void setExecuteJobsList(ConfExecuteJobValues[] executeJobsList) {
        this.executeJobsList = executeJobsList;
    }

    /**
     * standard setter
     * @param linkList
     */
    public void setLinkList(ConfLinkValues[] linkList) {
        this.linkList = linkList;
    }

    /**
     * standard setter
     * @param dropBoxList
     */
    public void setDropBoxList(ConfLinkValues[] dropBoxList) {
        this.dropBoxList = dropBoxList;
    }

    /**
     * standard setter
     * @param isLocaltionListEnabled
     */
    public void setIsLocaltionListEnabled(boolean isLocaltionListEnabled) {
        this.isLocaltionListEnabled = isLocaltionListEnabled;
    }

    /**
     * standard setter
     * @param isMailToExpertEnabled
     */
    public void setIsMailToExpertEnabled(boolean isMailToExpertEnabled) {
        this.isMailToExpertEnabled = isMailToExpertEnabled;
    }

    /**
     * standard setter
     * @param isShiftSummaryEnabled
     */
    public void setIsShiftSummaryEnabled(boolean isShiftSummaryEnabled) {
        this.isShiftSummaryEnabled = isShiftSummaryEnabled;
    }

    /**
     * standard setter
     * @param isSpellcheckerEnabled
     */
    public void setIsSpellcheckerEnabled(boolean isSpellcheckerEnabled) {
        this.isSpellcheckerEnabled = isSpellcheckerEnabled;
    }

    /**
     * standard setter
     * @param isViewHistoryEnabled
     */
    public void setIsViewHistoryEnabled(boolean isViewHistoryEnabled) {
        this.isViewHistoryEnabled = isViewHistoryEnabled;
    }

    /**
     * standard setter
     * @param isEditEnableEnabled
     */
    public void setIsEditEnableEnabled(boolean isEditEnableEnabled) {
        this.isEditEnableEnabled = isEditEnableEnabled;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="getter">

    /**
     * standard getter
     * @return
     */
    public String[] getAllowIp() {
        return allowIp;
    }

    /**
     * standard getter
     * @return
     */
    public String getContext() {
        return context;
    }

    /**
     * standard getter
     * @return
     */
    public String getContext_path() {
        return context_path;
    }

    /**
     * standard getter
     * @return
     */
    public String getLogbook_path() {
        return logbook_path;
    }

    /**
     * standard getter
     * @return
     */
    public String[] getCss() {
        return css;
    }

    /**
     * standard getter
     * @return
     */
    public String getDatapath() {
        return datapath;
    }

    /**
     * standard getter
     * @return
     */
    public String getEdit_servlet() {
        return edit_servlet;
    }

    /**
     * standard getter
     * @return
     */
    public String getEdit_xsl() {
        return edit_xsl;
    }

    /**
     * standard getter
     * @return
     */
    public String getHost_data() {
        return host_data;
    }

    /**
     * standard getter
     * @return
     */
    public String[] getJs() {
        return js;
    }

    /**
     *
     * @retu standard getterrn
     */
    public String[] getKeyword_list() {
        return keyword_list;
    }

    /**
     * standard getter
     * @return
     */
    public String getLang_code() {
        return lang_code;
    }

    /**
     * standard getter
     * @return
     */
    public String[] getLocation_list() {
        return location_list;
    }

    /**
     * standard getter
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * standard getter
     * @return
     */
    public String[] getNeededRole() {
        return neededRole;
    }

    /**
     * standard getter
     * @return
     */
    public String getNew_shift() {
        return new_shift;
    }

    /**
     * standard getter
     * @return
     */
    public String getLogo() {
        return logo;
    }

    /**
     * standard getter
     * @return
     */
    public String getPdf_xsl() {
        return pdf_xsl;
    }

    /**
     * standard getter
     * @return
     */
    public String[] getRejectIp() {
        return rejectIp;
    }

    /**
     * standard getter
     * @return
     */
    public String getSearch2_xsl() {
        return search2_xsl;
    }

    /**
     * standard getter
     * @return
     */
    public String getSearch_xsl() {
        return search_xsl;
    }

    /**
     * standard getter
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * standard getter
     * @return
     */
    public String getTree_servlet() {
        return tree_servlet;
    }

    /**
     * standard getter
     * @return
     */
    public String getView_servlet() {
        return view_servlet;
    }

    /**
     * standard getter
     * @return
     */
    public String getView_xsl() {
        return view_xsl;
    }

    /**
     * standard getter
     * @return
     */
    public ConfExecuteJobValues[] getExecuteJobsList() {
        return executeJobsList;
    }

    /**
     * standard getter
     * @return
     */
    public ConfLinkValues[] getLinkList() {
        return linkList;
    }

    /**
     * standard getter
     * @return
     */
    public ConfLinkValues[] getDropBoxList() {
        return dropBoxList;
    }

    /**
     * standard getter
     * @return
     */
    public boolean isIsLocaltionListEnabled() {
        return isLocaltionListEnabled;
    }

    /**
     * standard getter
     * @return
     */
    public boolean isIsMailToExpertEnabled() {
        return isMailToExpertEnabled;
    }

    /**
     * standard getter
     * @return
     */
    public boolean isIsShiftSummaryEnabled() {
        return isShiftSummaryEnabled;
    }

    /**
     * standard getter
     * @return
     */
    public boolean isIsSpellcheckerEnabled() {
        return isSpellcheckerEnabled;
    }

    /**
     * standard getter
     * @return
     */
    public boolean isIsViewHistoryEnabled() {
        return isViewHistoryEnabled;
    }

    /**
     * standard getter
     * @return
     */
    public boolean isIsEditEnableEnabled() {
        return isEditEnableEnabled;
    }

    // </editor-fold>

}//class end
