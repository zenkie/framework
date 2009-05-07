package nds.control.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
/*
 Hold MenuItems
*/
public class Menu {
    private int id;
    private String name;
    private boolean isRoot;
    private boolean isModified;
    private String remark;
    private Date creationDate;
    private Date modifiedDate;
    private int ownerId;
    private ArrayList roots= new ArrayList(); //MenuItems
    public Menu() {

    }
    public Menu(int id, String name, boolean isRoot, boolean isModified, String remark, Date creationDate,
                Date modifiedDate, int ownerId){
        this.id=id;
        this.name=name;
        this.isRoot=isRoot;
        this.isModified = isModified;
        this.remark = remark;
        this.creationDate = creationDate;
        this.modifiedDate = modifiedDate;
        this.ownerId= ownerId;

    }
    public int getOwnerId(){
        return ownerId;
    }
    public void setOwnerId(int d){
        ownerId=d;
    }
    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public boolean isRoot(){
        return isRoot;
    }
    public boolean isModified(){
        return isModified;
    }
    public String getRemark(){
        return remark;
    }
    public Date getCreationDate(){
        return creationDate;
    }
    public Date getModifiedDate(){
        return modifiedDate;
    }
    public void setId(int id){
        this.id=id;
    }
    public void setName(String n){
        name= n;
    }
    public void setIsRoot(boolean b){
        isRoot=b;
    }
    public void setIsModified(boolean b){
        isModified=b;
    }
    public void setRemark(String re){
        remark= re;
    }
    public void setCreationDate(Date d){
        creationDate =d;
    }
    public void setModifiedDate(Date d){
        modifiedDate=d;
    }
    public void addRootItem(MenuItem root){
        roots.add(root);
    }
    public void removeRootItem(MenuItem root){
        roots.remove(root);
    }
    public void clearRoots(){
        roots.clear();
    }
    public Collection getRootItems(){
        return roots;
    }
    public String toString(){
        return id+"";
    }
}
