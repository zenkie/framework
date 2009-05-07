package nds.query.web;

import java.util.Vector;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class PortalBean {

    private Vector tables;

    public PortalBean() {
        tables = null;
    }

    public void setTables(Vector tables){
        this.tables = tables;
    }

    public Vector getTables(){
        return tables;
    }
}