package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import nds.control.ejb.Command;
import nds.control.ejb.command.tree.TreeNodeHolder;
import nds.control.ejb.command.tree.TreeNodeManager;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.NavNode;
import nds.control.util.ValueHolder;
import nds.query.QueryEngine;
import nds.security.Directory;
import nds.security.User;
import nds.util.NDSException;
import nds.util.NDSRuntimeException;
import nds.util.Tools;
import nds.model.*;
import nds.model.dao.*;
import org.hibernate.*;


/**
 * Clob object modify
 */
public class U_ClobModify extends Command {
	
    public ValueHolder execute(DefaultWebEvent event) throws NDSException, RemoteException {
    	// check permission
    	Session session= null;
    	UClobDAO clobDAO=UClobDAO.getInstance();
    	try{
    	int id = Tools.getInt(event.getParameterValue("id"),-1);
    	session=clobDAO.createSession();
    	UClob clob=clobDAO.load(new Integer(id), session);
    	
    	String refTableName= clob.getRefTable();
    	int refObjId=clob.getRefObjectId().intValue();
    	
    	clob.setModifieddate(new java.util.Date());
    	clob.setModifierid( helper.getOperator(event).getId());
		clob.setContent( Hibernate.createClob(" ") );
		clobDAO.saveOrUpdate(clob, session);
		session.flush();
    	session.refresh(clob, LockMode.UPGRADE);
    	oracle.sql.CLOB c=(oracle.sql.CLOB) ( (org.hibernate.lob.SerializableClob) clob.getContent()).getWrappedClob();

    	/*oracle.sql.CLOB c = (oracle.sql.CLOB) clob.getContent();
    	if(c==null){
    		clob.setContent( Hibernate.createClob(" ") );
    		clobDAO.saveOrUpdate(clob, session);
    		session.flush();
        	session.refresh(clob, LockMode.UPGRADE);
    		c=(oracle.sql.CLOB) clob.getContent();
    	}else{
        	session.refresh(clob, LockMode.UPGRADE);
    	}*/
    	java.io.Writer pw = c.getCharacterOutputStream();
    	//c.truncate(c.getLength());
    	String rawContent= (String)event.getParameterValue("clob_content");
    	String content=com.liferay.util.JS.decodeURIComponent( rawContent);
    	//logger.debug("content:" + content);
    	pw.write(content);
    	pw.close();
    	clobDAO.saveOrUpdate(clob,session);
    	session.flush();
    	
        ValueHolder v = new ValueHolder();
        v.put("message","ÐÞ¸Ä³É¹¦!") ;
        return v;
    	}catch(Exception e){
    		logger.error("Could not save clob:", e); 
    		throw new NDSEventException(e.getMessage());    		
    	}finally{
    		if(session!=null){
    			try{ clobDAO.closeSession();}catch(Exception ex){}
    		}
    	}
    }

}
