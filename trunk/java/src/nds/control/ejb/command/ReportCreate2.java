package nds.control.ejb.command;

import java.rmi.RemoteException;
import java.util.ArrayList;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.util.NDSException;


/**
 * This is old one use use db to create txt file. 
 * 
 * Add support for C_Invoice export for hangtian golden tax interface
 * Will need client to input the query request object, if main table 
 * is C_INVOICE and used "taxifc" as seperator, then will call C_InvoiceExport method in oracle
 *
 */

public class ReportCreate2 extends Command {
	/**
	 * From 2.0, add two parameter in event: 
	 *  "request" - QueryRequest of the original query
	 * 
	 * If "taxifc" as serperator and table="C_Invoice", will call "C_InvoiceExport" in oracle
	 * Current implementation will not export by page for invoice, but the whole
	 * query result (by-page is not supported)
	 */
  public ValueHolder execute(DefaultWebEvent event) throws RemoteException, NDSException {
    try{


    /* -- Check directory permission on creation -- */
    //User commander = helper.getOperator(event);
    //helper.checkDirectoryWritePermission(event, commander);

    String sql = (String)event.getParameterValue("sql");
    logger.debug(sql);
    QueryRequest request=(QueryRequest) event.getParameterValue("request");
    String path = (String)event.getParameterValue("path");
    String fileName = (String)event.getParameterValue("filename");
    String header = (String)event.getParameterValue("header");

    //  html or txt, from 2.0 add taxifc for C_Invoice interface
    String separator = (String)event.getParameterValue("separator"); 
    boolean flink = ((String)event.getParameterValue("flink")).equalsIgnoreCase("true")?true:false;
    ArrayList params=new ArrayList();

    SPResult res = null;
    if(fileName.endsWith(".taxifc") &&
    		request !=null && 
    		"C_INVOICE".equalsIgnoreCase(request.getMainTable().getRealTableName())){
    	// for invoice export, to golden tax interface of aerospace corp.
    	params.add( request.toPKIDSQL(true));
    	params.add(path);
    	params.add(fileName);
    	res = helper.executeStoredProcedure("C_Invoice_Export", params, true);
    }else{
        params.add(sql);
        params.add(path);
        params.add(fileName);
        params.add(header);
    	
    if(separator.toLowerCase().equals("html")){
        if(flink){
            params.add(new Integer("1"));
            params.add((String)event.getParameterValue("flinkSql"));
            params.add((String)event.getParameterValue("flinkColumn"));
        }else{
            params.add(new Integer("0"));
            params.add("0");
            params.add("0");
        }
        res = helper.executeStoredProcedure("REPORTCREATEHTML", params, true);
    }else{
        params.add(separator);
        if(flink){
            params.add(new Integer("1"));
            params.add((String)event.getParameterValue("flinkSql"));
            params.add((String)event.getParameterValue("flinkColumn"));
        }else{
            params.add(new Integer("0"));
            params.add("0");
            params.add("0");
        }
        res = helper.executeStoredProcedure("REPORTCREATETXT", params, true);
    }
    }
        ValueHolder v=new ValueHolder();
        if( res.isSuccessful()){
            v.put("message", res.getMessage());
        }else{
            logger.debug(res.toString());
            throw new NDSEventException(res.getDebugMessage());
        }
        return v;
    }catch(Exception e){
      logger.error("", e);
      if(!(e instanceof NDSException ))throw new NDSEventException("@exception@", e);
      else throw (NDSException)e;
    }
  }
}