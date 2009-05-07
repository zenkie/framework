/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.taglibs;

import java.io.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import javax.servlet.jsp.tagext.BodyTagSupport;

import nds.log.Logger;
import nds.log.LoggerManager;

import nds.util.*;

/**
 * Caches a fragment of a JSP page into file. File path is specified by name attribute
 * File not be invalidated if session is newer
 * @author yfzhu@agilecontrol.com
 */

public class FileCacheTag extends BodyTagSupport {
	private final static Logger logger=LoggerManager.getInstance().getLogger(FileCacheTag.class.getName());
    //*********************************************************************
    // Private state

    private String name;
	private boolean _test; // if true, will try to read cache

    private String cached;    // value from cache

    //*********************************************************************
    // Tag logic

    public int doStartTag() throws JspException {
    	if(_test){
    		logger.debug("Try cache");
	    	File f= new File(name);
	    	if(!f.exists() || f.lastModified()< this.pageContext.getSession().getCreationTime() ){
	    		this.cached=null;
	    	}else{
	    		try{
	    			this.cached =Tools.readFile(f.getAbsolutePath(),"UTF-8");
	    		}catch(Throwable t){
	    			t.printStackTrace();
	    			this.cached=null;
	    		}
	    	}
    	}else{
    		logger.debug("Not try cache");
    		this.cached=null;
    	}
        if (this.cached != null) {
            return SKIP_BODY;
        } else {
            return EVAL_BODY_BUFFERED;
        }
    }

    public int doEndTag() throws JspException {
        try {
        	String s;
            if(_test){
	             s= this.cached;
	            if (s == null ) {
	                if (bodyContent == null || bodyContent.getString() == null) {
	                    s = "";
	                } else {
	                    s = bodyContent.getString().trim();
	                }
		            File f= new File(name);
		            if(!f.getParentFile().exists())f.getParentFile().mkdirs();
		            Tools.writeFile(f.getAbsolutePath(), s, "UTF-8");
		            logger.debug("Write to cache "+ f.getAbsolutePath());
	            }else{
	            	logger.debug("Load from cache");
	            }
	            pageContext.getOut().write(s);
            }else{
                if (bodyContent == null || bodyContent.getString() == null) {
                    s = "";
                } else {
                    s = bodyContent.getString().trim();
                }
            	pageContext.getOut().write(s);
            }
        } catch (IOException ex) {
            throw new JspException(ex);
        }
        return EVAL_PAGE;
    }

    public void setName(String nameExpr) {
        this.name = nameExpr;
    }

	public void setTryCache(boolean test) {
		_test = test;
	}




    //*********************************************************************
    // Constructor and initialization

    public FileCacheTag() {
        super();
        init();
    }

    private void init() {
        this.name =  "";
        cached=null;
		_test = false;
    }
    
    public void release() {
        init();
        super.release();
    }



}
