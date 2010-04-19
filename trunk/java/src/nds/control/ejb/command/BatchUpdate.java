package nds.control.ejb.command;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;
import java.sql.*;

import nds.control.ejb.Command;
import nds.control.ejb.command.pub.Pub;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.SecurityUtils;
import nds.control.util.ValueHolder;
import nds.query.*;
import nds.schema.*;
import nds.security.Directory;
import nds.security.User;
import nds.util.*;
/**
 	Batch update selected records.
 	
 	The screen should allow user to specify filter to construct where clause, and 
 	input new value for modifiable columns.
 	
 	For new value input, if wrapped by ${} tag, then the content inside will passed to
 	database, so can include formula or function in it. Use input format like  
 	OLD.<column name| column description> to utilize old column value of the same line,
 	use OLD without dot will mean current value of the same column.
 	
 	The implementation does not support multiple table update. So you can not refer to
 	columns of other table as new value part.
 	
 	Sample input:
 	${sysdate+10} to get date of next 10 days
 	${OLD*2} to double the current value
 	${OLD.高度/2} to divide the value of the "height" column 
 	
 	Will call _AM method for each row updated. 
 	
 	If table has special command handler other than ObjectModify, this method will failed. 
 	  
*/
public class BatchUpdate  extends Command{
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(){
    	return true;
    }
    /**
     * If command exists other than ObjectModify for this table, will throw exception
     * @throws NDSException
     */
    private void checkCommand(Table table, String tableDesc) throws NDSException{
    	try{
    		Class.forName("nds.control.ejb.command."+ table.getName()+"Modify");
    		throw new NDSException("@table-not-support-batchupdate@:"+ tableDesc);
    	}catch(ClassNotFoundException e){
    		
    	}
    	
    }
    /**
     */
    public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
    	
    	long beginTime= System.currentTimeMillis();
        logger.debug(event.toDetailString());
    	TableManager manager = helper.getTableManager();
        int tableId = Tools.getInt(event.getParameterValue("table",true),-1 ) ;

        User user=helper.getOperator(event);
        Integer userId= user.id;
        Table table = manager.getTable(tableId) ;
        // check no special command class for this table's AM method
        checkCommand(table, table.getDescription(event.getLocale()));

        String tableName = table.getName();          // 得到表的名字
        java.sql.Connection conn=null;
        java.sql.Connection conn2=null;
        try{
        	conn= QueryEngine.getInstance().getConnection();
        	conn2= QueryEngine.getInstance().getConnection();
        	conn2.setAutoCommit(false);
        // construct query
        String filterExpr=(String) event.getParameterValue("param_expr");
        Expression expr=null;
        if(Validator.isNotNull(filterExpr)){
        	expr= new Expression(filterExpr);
        }
        QueryRequestImpl query= QueryEngine.getInstance().createRequest(event.getQuerySession());
        query.setMainTable(table.getId());
        if(expr!=null)query.addParam(expr);
        
        StringBuffer sb=new StringBuffer("UPDATE "+ table.getRealTableName()+ " " + table.getName()+ " SET ");
        ArrayList columns= table.getModifiableColumns(Column.MODIFY);
        boolean appendComma=false;
        
        for(int i=0;i<columns.size();i++){
        	Column column= (Column)columns.get(i);
        	String name = column.getName().toLowerCase() ;
        	if( column.getReferenceTable()!=null){
        		name= name +"__"+ column.getReferenceTable().getAlternateKey().getName().toLowerCase();
        	}
        	String value=(String)event.getParameterValue(name);
        	if(value!=null && column.getReferenceTable()!=null && column.getReferenceTable().getAlternateKey().isUpperCase())
        		value= value.toUpperCase();
        	if(value!=null && column.getReferenceTable()==null && column.isUpperCase())
        		value= value.toUpperCase();
        	if(value!=null)if (column.isValueLimited() && "0".equals(value)) value=null;
        	
        	if(value!=null && value.length()>0){
        		// is value wrappered by ${} ?
        		if(value.startsWith("${") && value.endsWith("}")){
        			value= parseValue(value, column);
        		}else{
        			// if value =null, then set that as update clause, so the clause will be
        			// <column>=null
        			if(!"NULL".equalsIgnoreCase(value)){
	        			// for fk column, will try to get id
	        			if("object".equals(column.getObtainManner())){
	        				ObjectColumnObtain oco=new ObjectColumnObtain();
	        				oco.setConnection(conn);
							value =((Object[]) oco.getColumnValue(event,table,column,1 /* only 1 line*/).elementAt(0))[0].toString();
	        			}
	        			int colType= column.getType();
	                    if( colType == column.DATE ){
	                    	java.util.Date date= QueryUtils.parseInputDate(value,column.isNullable(), column.getSQLType() );
	                        value= "TO_DATE('"+ ((java.text.SimpleDateFormat)QueryUtils.dateTimeSecondsFormatter.get()).format(date)+"','YYYY/MM/DD HH24:MI:SS')";
		                }else if( colType == column.STRING ){
		                    value="'"+Pub.getDoubleQuote(value)+"'";
		                }
        			}
        			
        		}
        		if(appendComma) sb.append(",");
        		sb.append(column.getName()).append("=").append(value);
				appendComma=true;
        	}
        	
        }
        int totalRowsFound=0, totalRowsPermitted=0,totalRowsUpdated=0, rowsFailed=0;
        nds.util.PairTable pt=new nds.util.PairTable();
        if(!appendComma){
        	logger.debug("not column update found");
        }else{
        	// modified date and user
        	if( table.getColumn("MODIFIEDDATE")!=null) sb.append(",MODIFIEDDATE=sysdate");
        	if( table.getColumn("MODIFIERID")!=null) sb.append(",MODIFIERID="+ userId);
        	sb.append(" WHERE ID=");
	        //handle records one by one, after check user write permission on that object 
        	 
        	ResultSet rs= conn.createStatement().executeQuery(query.toPKIDSQL(true));
        	Savepoint sp=null;
        	StringBuffer sql=null;
            Statement stmt=conn2.createStatement();
        	while(rs.next()){
        		int objectId=rs.getInt(1);
        		totalRowsFound++;
        		// check user write permission on this object
        		if(nds.control.util.SecurityUtils.hasObjectPermission(conn,userId, user.name, table.getName(), objectId, nds.security.Directory.WRITE, event.getQuerySession())){
        			totalRowsPermitted++;
        			//do update
            		sql=new StringBuffer(sb.toString());
            		sql.append(objectId);
            		try{
	            		sp=conn2.setSavepoint();
	            		stmt.executeUpdate(sql.toString());
	            		//do AM
	           	        helper.doTrigger("AM", table, objectId, conn2);
	           	        sp=null;
	           	        totalRowsUpdated++;
            		}catch(Throwable t){
            			logger.error("Fail to update "+ table.getName()+"(id="+ objectId+"):", t);

            			rowsFailed++;
            			pt.put(new Integer(objectId), nds.util.StringUtils.getRootCause(t).getMessage());
            			if(sp!=null){
            				try{
            					conn2.rollback(sp);
            				}catch(Throwable rt){
            					logger.error("Could not rollback to save point:", rt);
            				}
            			}
            		}
        		}
        		
        	}
        	
        }
        //int totalRowsFound=0, totalRowsPermitted=0,totalRowsUpdated=0, rowsFailed=0;
        sb=new StringBuffer();
        sb.append("<pre>### "+ (new java.util.Date())+ ":@complete@(@consumed-to@ "+ (System.currentTimeMillis() -beginTime)/1000 + " @seconds@) ###");
        sb.append("\r\n");
        sb.append("@operate-table@："+table.getDescription(event.getLocale())+"\r\n");
        sb.append("@total-rows-found@:"+ totalRowsFound +", @rows-permitted@:"+ totalRowsPermitted +
        		", @update-success@:"+totalRowsUpdated+", @update-fail@:"+ rowsFailed + "\r\n");
        
        for(int i=0;i< pt.size();i++){
            sb.append("id="+ pt.getKey(i)+":"+ pt.getValue(i) + "\r\n");
        }
        sb.append("</pre>");

        ValueHolder vh=new ValueHolder();
        vh.put("message", sb.toString());
        return vh;
        }catch(SQLException sqle){
        	throw new NDSException(sqle.getLocalizedMessage(), sqle);
        }finally{
        	try{conn.close();}catch(Throwable t){}
        	try{
        		conn2.commit();
        	}catch(Throwable t2){}
        	try{conn2.close();}catch(Throwable t){}
        }

    }
    /**
     * Parse value to db acceptable format
     * @param value starts with "${" and ends with "}", may contains OLD/OLD.<column> type 
     * @param column
     * @return value that will be used for update clause construction
     */
    private String parseValue(String value, Column column) throws NDSException{
    	
    	value= value.substring(2,value.length()-1);
    	StringBuffer sb=new StringBuffer();
    	int p= value.indexOf("OLD");
    	String str;
    	int pre=0,i;
    	boolean found;
    	
    	while(p> -1){
    		sb.append(value.substring(pre, p));
    		// handle old replacement
    		if( (value.length()> p+3) && value.charAt(p+3)=='.'){
    			// maybe column definition
    			for(i=p+4;i< value.length();i++){
    				if("+-*/ ,;()[]{}'\"|\\<>:".indexOf(value.charAt(i))>-1){
    					found=true;
    					break;
    				}
    			}
    				str= value.substring(p+4, i);
    				ArrayList cols=column.getTable().getAllColumns();
        			found=false;
    				for(int j=0;j< cols.size();j++){
    					Column col= (Column)cols.get(j);
    					if( col.getName().equalsIgnoreCase(str)|| col.getDescription(Locale.CHINA).equalsIgnoreCase(str)){
    						sb.append(col.getName());
    						found=true;
    						break;
    					}
    				}
        			if(found){
        				p=i; 
	    			}else{
	    				//there's no column matches the name, take old. as normal string
	    				sb.append("OLD.");
	    				p+=4;
	    			}
    		}else{
    			// just current column
    			sb.append( column.getName());
    			p+=3;
    		}
    		pre=p;
			p= value.indexOf("OLD", p);
    	}
    	if(pre< value.length())
    		sb.append(value.substring(pre,  value.length()));
    	return sb.toString();
    }
    
}