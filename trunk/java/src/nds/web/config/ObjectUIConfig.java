package nds.web.config;

public class ObjectUIConfig implements PortletConfig {
	public final static int ACTION_VIEW=1;
	public final static int ACTION_EDIT=2;
	private int id;
	private String name;
	private String tableParamName;
	private String idParamName;
	private String cssClass;
	private int colsPerRow;
	private int defaultAction;// ObjectUIConfig.ACTION_XXX
	private boolean isPublic=false;
	private boolean isShowRefTables=false;
	private boolean isShowComments=false;
	
	private static ObjectUIConfig defaultConfig=null;
	private static ObjectUIConfig defaultTableUIConfig=null;
	private static ObjectUIConfig defaultInlineTableUIConfig=null;
	/**
	 * Inline table config, for /html/nds/object/inlineobj.jsp
	 * @return
	 */
	public static ObjectUIConfig getDefaultInlineTableUIConfig(){
		if(defaultInlineTableUIConfig==null){
			defaultInlineTableUIConfig=new ObjectUIConfig();
			defaultInlineTableUIConfig.id=-2;
			defaultInlineTableUIConfig.name= "default-inline-table-ui-config";
			defaultInlineTableUIConfig.tableParamName="table";
			defaultInlineTableUIConfig.idParamName="id";
			defaultInlineTableUIConfig.cssClass="inlineobjtb";
			defaultInlineTableUIConfig.colsPerRow=3;
			defaultInlineTableUIConfig.defaultAction= ObjectUIConfig.ACTION_VIEW; // this is for QUERY_LIST, not for OBJECT_VIEW type
			defaultInlineTableUIConfig.isPublic=false;
    		defaultInlineTableUIConfig.isShowRefTables=false;
    		defaultInlineTableUIConfig.isShowComments=false;
    	}
    	return defaultInlineTableUIConfig;
	}
	/**
     * Get default table ui config, this is for table object page in /html/nds/object/object.jsp
     * 
     * @param type
     * @return
     * @throws Exception
     */
    public static ObjectUIConfig getDefaultTableUIConfig() {
    	if(defaultTableUIConfig==null){
    		defaultTableUIConfig=new ObjectUIConfig();
    		defaultTableUIConfig.id=-2;
    		defaultTableUIConfig.name= "default-table-ui-config";
    		defaultTableUIConfig.tableParamName="table";
    		defaultTableUIConfig.idParamName="id";
    		defaultTableUIConfig.cssClass="objtb";
    		defaultTableUIConfig.colsPerRow=3;
    		defaultTableUIConfig.defaultAction= ObjectUIConfig.ACTION_VIEW; // this is for QUERY_LIST, not for OBJECT_VIEW type
    		defaultTableUIConfig.isPublic=false;
    		defaultTableUIConfig.isShowRefTables=true;
    		defaultTableUIConfig.isShowComments=false;
    	}
    	return defaultTableUIConfig;
    }
	/**
     * Get default config, this is for object page 
     * 
     * @param type
     * @return
     * @throws Exception
     */
    public static ObjectUIConfig getDefaultConfig() {
    	if(defaultConfig==null){
    		defaultConfig=new ObjectUIConfig();
    		defaultConfig.id=-1;
    		defaultConfig.name= "default-obj-ui-config";
    		defaultConfig.tableParamName="table";
    		defaultConfig.idParamName="id";
    		defaultConfig.cssClass="obj-table";
    		defaultConfig.colsPerRow=2;
    		defaultConfig.defaultAction= ObjectUIConfig.ACTION_EDIT;
    		defaultConfig.isPublic=false;
    		defaultConfig.isShowRefTables=true;
    		defaultConfig.isShowComments=true;
    	}
    	return defaultConfig;
    }
    
	public int getColsPerRow() {
		return colsPerRow;
	}
	public void setColsPerRow(int colsPerRow) {
		this.colsPerRow = colsPerRow;
	}
	public String getCssClass() {
		return cssClass;
	}
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}
	public int getDefaultAction() {
		return defaultAction;
	}
	public void setDefaultAction(int defaultAction) {
		this.defaultAction = defaultAction;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIdParamName() {
		return idParamName;
	}
	public void setIdParamName(String idParamName) {
		this.idParamName = idParamName;
	}
	public boolean isPublic() {
		return isPublic;
	}
	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTableParamName() {
		return tableParamName;
	}
	public void setTableParamName(String tableParamName) {
		this.tableParamName = tableParamName;
	}

	public boolean isShowRefTables() {
		return isShowRefTables;
	}

	public void setShowRefTables(boolean isShowRefTables) {
		this.isShowRefTables = isShowRefTables;
	}

	public boolean isShowComments() {
		return isShowComments;
	}

	public void setShowComments(boolean isShowComments) {
		this.isShowComments = isShowComments;
	}
	
}
