/******************************************************************
* Used for menu portlet
*
********************************************************************/
package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.Menu;
import nds.control.util.MenuItem;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.security.User;
import nds.util.IntHashtable;
import nds.util.NDSException;
import nds.util.SQLUtils;

/**
* Used by menu portlet, will fetch menu according to param "menuName"
 */
public class HandleChain_Menu extends Command {
    private static String GET_MENU =
    "select id, isroot, ismodified, remark,creationdate, modifieddate from prfmnu001 where name=? and ownerid=?";
    private static String GET_MENUITEMS=
    "select id, name,parentid, url, eventtype, directoryid, isnew from prfmnu002 where moduleid =? and ownerid=? order by id";

    // name: file name of tree.xml to be retrieved
    // value: Vector of tree nodes
    private static Hashtable trees=new Hashtable();
    /**
     * @param event - can has following parameters:
     *     1. menu_root - string, menu root name
     *
     * @return valueholder contains a nds.control.util.Menu named "menu" if found, or null if not found.
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {
        logger.debug("execute "+ event.toDetailString());
        ValueHolder v=new ValueHolder();
        User user=helper.getOperator(event);
        String menuName= (String)event.getParameterValue("menu_root");
        if( menuName ==null)menuName="root";   //throw new NDSEventException("menu_root must be set");

        try {
            Menu menu= constructMenu_test(menuName); // createMenu(menuName, user.getId().intValue() );
            if(menu !=null){
                v.put("menu", menu);
                v.put("event",event );
            }
            v.put("forward", "portlet.chain.view.menu.iframe" );
            return v;
        }
        catch (Exception e) {
            logger.error("", e);
            throw new NDSEventException("Error found.", e);
        }
    }
    private Menu createMenu(String menuName, int userId) throws Exception{
        Connection con= QueryEngine.getInstance().getConnection();
        PreparedStatement pstmt=null;
        ResultSet rs= null;
        try{
            pstmt.setString(1, menuName);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                Menu menu= new Menu();
                menu.setId(rs.getInt(1));
                menu.setIsRoot(SQLUtils.getBoolean(rs, 2, false)) ;
                menu.setIsModified(SQLUtils.getBoolean(rs,3, false));
                menu.setRemark(rs.getString(4));
                menu.setCreationDate(rs.getDate(5));
                menu.setModifiedDate(rs.getDate(6));
                menu.setOwnerId(userId);
                menu.setName(menuName);
                insertItems(menu, con);
                return menu;
            }

        }finally{
            if(rs!=null){try{rs.close();}catch(Exception e){}}
            if(pstmt!=null){try{pstmt.close();}catch(Exception e){}}
            try{con.close();}catch(Exception e){}
        }
        return null;
    }

    private void insertItems(Menu menu, Connection con) throws Exception{
        PreparedStatement pstmt=null;
        ResultSet rs= null;
        ArrayList al;
        // use IntHashtable instead of Hashtable so the order of menu item will not change
        IntHashtable ht=new IntHashtable();// key : item id, value MenuItem
        try{
            pstmt.setInt(1, menu.getId());
            pstmt.setInt(2, menu.getOwnerId());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                MenuItem item= new MenuItem();
//select id, name,parentid, url, eventtype, directoryid, isnew, creationdate, modifieddate from prfmnu002 where moduleid =? and ownerid=?";
                item.setId(rs.getInt(1));
                item.setName(rs.getString(2));
                item.setParentId(rs.getInt(3));
                item.setURL(rs.getString(4));
                item.setEventType(rs.getString(5));
                item.setDirectoryId(SQLUtils.getInt(rs, 6, -1)); // -1 means root
                item.setIsNew( SQLUtils.getBoolean(rs,7, false));
                ht.put(item.getId(), item);
            }
        }finally{
            if(rs!=null){try{rs.close();}catch(Exception e){}}
            if(pstmt!=null){try{pstmt.close();}catch(Exception e){}}
            try{con.close();}catch(Exception e){}
        }
        // compose the tree
        al= arrangeItems(ht,menu);
        for (int i=0;i< al.size();i++) menu.addRootItem((MenuItem)al.get(i));
    }
    // use IntHashtable instead of Hashtable so the order of menu item will not change
    private ArrayList arrangeItems(IntHashtable items, Menu menu){
        ArrayList al=new ArrayList();
        MenuItem item,parent;
        for (Iterator it= items.values().iterator();it.hasNext() ;){
            item= (MenuItem) it.next();
            if ( item.getParentId()==-1) al.add(item);
            else{
                // add it to parent
                parent=(MenuItem) items.get(item.getParentId());
                if(parent!=null){
                    parent.addChild(item);
                    item.setParent(parent);
                }else{
                    logger.error("Found parent not in the tree of menu " + menu);
                }
            }
        }
        return al;
    }
    private nds.control.util.Menu constructMenu_test(String root) throws     Exception {
    nds.control.util.Menu menu = new nds.control.util.Menu(1, root, true, true, "menu1", new java.util.Date(),
                         new java.util.Date(), 0);
//(int id, String name,String icon, String url, int parentId, String eventType, int directoryId, boolean isNew){
    MenuItem item1 = new MenuItem(1, "item1", "icon1", "menu_root=item1", 1,
                                  "Menu", 1, true);
    MenuItem item2 = new MenuItem(2, "item2", "icon2", "menu_root=item2", 2,
                                  "ObjectList", 2, false);
    menu.addRootItem(item1);
    menu.addRootItem(item2);
    item2 = new MenuItem(3, "item1.1", "icon2", "menu_root=item1.1", 3,
                         "Menu", 3, true);
    item1.addChild(item2);
    item1.addChild(new MenuItem(4, "item1.2", "icon2", "menu_root=item1.2",
                                4, "Menu", 4, true));
    return menu;
}

}
