package nds.control.ejb.command;
import java.util.*;
import java.io.PrintStream;
import java.rmi.RemoteException;

import nds.control.ejb.Command;
import nds.control.event.DefaultWebEvent;
import nds.control.util.ValueHolder;
import nds.schema.TableManager;
import nds.util.NDSException;
import bsh.EvalError;
import bsh.Interpreter;

/**
 *  ETL I_Invoice
 */
public class I_InvoiceETL extends Command{

    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
    	// check permission
    	event.setParameter("directory","I_INVOICE_ETL");
    	helper.checkDirectoryReadPermission(event, helper.getOperator(event));
    	long t= System.currentTimeMillis();
    	// reload from db
    	helper.executeStoredProcedure("I_INVOICE_ETL", new ArrayList(), false);
    	
    	ValueHolder v = new ValueHolder();
    	v.put("message","命令执行成功，耗时"+ (System.currentTimeMillis()-t)/1000.0 +" 秒!") ;
    	return v;
    }
}