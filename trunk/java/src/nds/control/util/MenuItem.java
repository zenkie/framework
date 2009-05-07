package nds.control.util;

/**
 * Used for Menu
 */
public class MenuItem extends NavNode {
    protected int id;
    protected String eventType;
    protected int directoryId;
    protected boolean isNew;
    protected int parentId;

    public MenuItem() {
    }

    public MenuItem(int id, String name,String icon, String url, int parentId, String eventType, int directoryId, boolean isNew){
        this.id=id;
        this.setLabel(name);
        this.icon = icon;
        this.url=url;
        this.parentId = parentId;
        this.eventType=eventType;
        this.directoryId = directoryId;
        this.isNew = isNew;
    }
    public void setId(int d){ id=d;}
    public void setName(String name) {
        this.setLabel(name);
    }
    public void setParentId(int id){
        parentId=id;
    }
    public int getParentId(){
        return parentId;
    }
    public void setEventType(String e){ eventType=e;}
    public void setDirectoryId(int id){ directoryId=id;}
    public void setIsNew(boolean b){ isNew= b;}

    public int getId(){
        return id;
    }
    public String getName(){
        return getLabel();
    }
    public String getEventType(){
        return eventType;
    }
    public int getDirectoryId(){
        return directoryId;
    }
    public boolean isNew(){
        return isNew;
    }
    public void setParent(NavNode newParent) {
        super.setParent(newParent);
        if(newParent instanceof MenuItem)
        	setParentId(((MenuItem)newParent).getId());
    }

    public int hashCode(){
        return id;
    }
    
    public boolean equals(Object o){
        return (o instanceof MenuItem) && ((MenuItem)o).id ==this.id;
    }
}
