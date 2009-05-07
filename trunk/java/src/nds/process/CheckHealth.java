/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import nds.control.util.SecurityUtils;
import nds.control.web.WebUtils;
//import nds.olap.OLAPUtils;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.security.User;
import nds.util.*;
import java.io.*;

import java.sql.*;

/**
 * Healthy check of the whole system, such as remove obsolete file
 * 
 * @author yfzhu@agilecontrol.com
 */

public class CheckHealth extends SvrProcess
{
	
	/**
	 *  Parameters:
	 *    
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameters();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else 
				log.error("prepare - Unknown Parameter: " + name);			
		}
	}	//	prepare	
	/**
	 *  Perrform process.
	 *  @return Message that would be set to process infor summary (no use currently)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception{
		removeTmpFiles();
		
		log.debug("Finished");
		
		return null;
	}
	/**
	 * Remove obsolete file in directory of "dir.tmp"
	 * 
	 */
	private void removeTmpFiles(){
		String path= ((Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS)).getProperty("dir.tmp","/act/tmp");
		try{
		File f= new File(path);
		if(f.exists()){
			String[] list = f.list();
			for (int i = 0; i < list.length; i++) {
				removeDir(new File(f, list[i]));
			}
		}
		}catch(Throwable t){
			log.error("Fail to remove temp files in "+ path, t);
		}
	}
	protected void removeDir(File d) {
        String[] list = d.list();
        if (list == null) {
            list = new String[0];
        }
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            File f = new File(d, s);
            if (f.isDirectory()) {
                removeDir(f);
            } else {
                if (!f.delete()) {
                    String message = "Unable to delete file " 
                        + f.getAbsolutePath();
                    log.debug(message);
                }
            }
        }
        if (!d.delete()) {
            String message = "Unable to delete directory " 
                + d.getAbsolutePath();
            log.debug(message);
        }
    }	
}
