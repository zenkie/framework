package nds.control.ejb.command;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.json.JSONObject;

import com.liferay.util.Validator;

import nds.control.check.ColumnCheckImpl;
import nds.control.ejb.DefaultWebEventHelper;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.query.*;
import nds.schema.*;
import nds.util.*;

/**
 * 增加对aliassupport table 的支持
 * 从界面上来的内容，可能是fk对应的reference table的alias table的ak, 如
 * 订单上的物料，可能输入的内容是条码，而条码来自于m_product_alias 表，然后通过
 * m_product_alias 表上的m_product_id 和 m_attributesetinstance_id 来设置
 * c_orderitem上的m_product_id 和 m_attributesetinstance_id
 * 
 * 这一功能跟随 AliasSupportTable而补充
 * 
 * 支持AK2 (2009-11-20 yfzhu)
 * 发现BUG，如果没有关联表的查询权限，无论填什么都报错 (2009-12-13 yfzhu)
 * @author yfzhu@agilecontrol.com
 */
public class ObjectColumnObtain extends ColumnObtain{
//  int length;
	private Locale locale;
  public ObjectColumnObtain() {
//      this.length = length;
  }

  public Vector getColumnValue(DefaultWebEvent event,Table table,Column col,int length) throws NDSException,RemoteException{
    DefaultWebEventHelper helper= new DefaultWebEventHelper();
    locale= event.getLocale();
    Boolean unionfk=(Boolean) (event.getParameterValue("unionfk")!=null?event.getParameterValue("unionfk"):false);
    logger.debug("unionfk for column " + col.getName() + ":"+ unionfk);
    nds.security.User user= helper.getOperator(event);
    //logger.debug(" for column " + col.getName() + ":"+ event.toDetailString());
  	TableManager tm= helper.getTableManager();
    QueryEngine engine = QueryEngine.getInstance();

  	Table refTable = col.getReferenceTable();
      if(refTable==null){
          refTable = col.getObjectTable();
      }/* 得到关联表的名字 */
      boolean hasReadPermission=
    	 "root".equals(user.getName()) || 
    	 (nds.control.util.SecurityUtils.getPermission(refTable.getSecurityDirectory(), user.getId().intValue()) & 1)==1;
    	  
      
      
      String refTableName = refTable.getName();
      String refTablleDesc  = refTable.getDescription(Locale.CHINA) ;

      String tableColumnName = col.getTableColumnName();
      if((tableColumnName==null)||("".equals(tableColumnName))){
           tableColumnName = col.getReferenceColumnName() ;
      }
      Column akColumn = refTable.getAlternateKey();
      Column ak2Column= refTable.getAlternateKey2();
      String akNo=akColumn.getName();

      //Column akCol = refTable.getAlternateKey();
      String colName = col.getName();
      int lastIndex = -100;
      String newStr = null;
      String[] objectStr = null;
      /**
      * Using column1__column2__... as input name from web, the seperator will be double "_"
      * @since 2.0
      */
      String eventValueName=(colName+"__"+akNo).toUpperCase();
      objectStr=event.getParameterValues(eventValueName);
      		
      /**
       * Will also fetch objectid directly from event, which may be set by fetching from alias table
       * for instance, m_attributesetinstance_id may be set when handling m_product_id column, which is
       * processed before m_attributesetinstance_id
       * @since 3.0
       */
      
      String[] objectIds= event.getParameterValues(colName.toUpperCase()); // may be null
      //logger.debug((colName).toUpperCase() +":" + (objectIds==null?"isnull":"size:"+objectIds.length)+":" + Tools.toString(objectIds));
      
      boolean isAliasSupportTable =(refTable instanceof AliasSupportTable);
      PreparedStatement aliasPstmt=null;
      PairTable assocColumns=null;
      

      /*
      if(colName.lastIndexOf("ID")!=-1){
          lastIndex = colName.lastIndexOf("ID");
          newStr = colName.substring(0,lastIndex) ;
          objectStr = event.getParameterValues(newStr+"_"+akNo);

      }else{
          objectStr = event.getParameterValues(refTableName+"_"+akNo);
      }*/
      BigDecimal[] resultInt = new BigDecimal[length];
   	  /**
   	   * yfzhu 2005-2-3 try fetch data from default value
   	   * @since 2.0
   	   */
   	  if(objectStr==null) fetchFromDefault(resultInt, event, col);
      
      Vector vec = new Vector();

      if(objectStr==null ){
    	  if(col.isNullable()){
              vec.add(resultInt);
              return vec;
    	  }else{
    		  throw new NDSException("Configuration error: column "+ col+" is not nullable while value not found in UI");
    	  }
       }
       /*---------above added by yfzhu for nmdemo -----*/
      ColumnCheckImpl checkImpl = new ColumnCheckImpl();

      //get read permission filter on ref table
      Expression filter=null;// helper.getSecurityFilter(refTableName, 1,user.getId().intValue() );
      if(!hasReadPermission){
    	  filter=(new Expression(null,"1<>1","NoPermission"));
      }else{
    	  filter=helper.getSecurityFilter(refTableName,1, user.getId().intValue(), event.getQuerySession());
      }
      
      int refTableId= tm.getTable(refTableName).getId();
      String filterSql=null;
      
      //suppot fkid and fkak
      // refTableName must not be real table, but column filter may use the alias table as reference   
      StringBuffer sqlStr=new StringBuffer( "SELECT "+tableColumnName+" FROM "+(refTable.getRealTableName())+
	    " "+refTable.getName()+" WHERE "+(refTable.isAcitveFilterEnabled()?"ISACTIVE='Y' AND (":"(")+(unionfk?refTable.getPrimaryKey().getName():akNo)+" = ?");

      
      if(ak2Column!=null){
    	  sqlStr.append(" OR ").append(ak2Column.getName()).append("=?");
      }
      sqlStr.append(")");
      // add refTable filter
      if(refTable.getFilter()!=null){
      	sqlStr.append(" AND "+ refTable.getFilter());
      }
      
      if(filter !=null && ! filter.isEmpty()){
          QueryRequestImpl req= engine.createRequest(event.getQuerySession());
          req.setMainTable(refTableId);
          req.addParam(filter);
          filterSql= req.toPKIDSQL(true);
          sqlStr.append(" AND ID IN (" + filterSql + ")");
          
      }
      // add ad_client_id control, see Table.isClientIsolated for detail
      if (refTable.isAdClientIsolated()){
      	 int ad_client_id= helper.getAdClientId(event.getQuerySession(), true);
      	 sqlStr.append(" and AD_CLIENT_ID="+ ad_client_id);
      }
      /**
       * yfzhu 2005-2-2 add support for column filter, which is defined in
       * table definition xml, means the column data must obey that filter.
       * 
       * Filter can has $ or @ variables, $ is from QuerySession attribute,
       * @ is from page context @means some other column's input data, such like:
       * C_BPARTNER_LOCATION_ID is a column of table C_Invoice, it has filter
       * @where C_BPARTNER_id=@C_BPARTNER_id@
       *  means the reference object of C_BPARTNER_LOCATION must have C_BPARTNER_id=
       * @C_BPARTNER_id@, the variable @C_BPARTNER_id@ is input value of another column
       * in C_Invoice screen, also should exists in DefaultWebEvent/QuerySession
       * 
       * @since 2.0
       
         考虑一个简单实现：filter 里的内容将被添加到 
         select id from reftable where ak=? and id in (security sql) and ad_client_id=id
         and (column.filter) 中，当前仅允许直接在关联表上设置过滤条件，可以是exists 语句等
         可以引用reftable中的任何数据库存在的字段。
         注意在QueryInputHandler 里也有相同的实现，也便在用户查找时过滤掉不该显示的记录
       */
      Column parentFkColumn =null;
      if( col.getFilter()!=null){
    	  if(!col.isFilteredByWildcard()){
      		sqlStr.append(" AND ("+ col.getFilter()+")");
    	  }else{
    		  /*
    		   * 以下模式必须支持：
    		   * （来自于主表的数据的获取）
    		   * 1)在移库单中，明细输入的移出库位必须属于移出仓库
    		   * 2)在药品采购单中，明细的药品必须和头上的项目设置一致
    		   * 
    		   * (来自同表的数据的获取，要求字段必须靠前）
    		   * 3）在运单中，运输地址必须属于运输客户
    		   * 
    		   * 这些模式都要求在明细上的字段的过滤器来自于单头的信息，明细字段配置WCF:
    		   * 1)m_locator.m_warehouse_id=@m_v_inout.m_whsout_id@
    		   * 2)m_product.c_project_id=@c_order.c_project_id@
    		   * 3)c_bpartner_location.c_bpartner_id=@l_order.s_bpartner_id@
    		   */ 
    		  sqlStr.append(" AND ("+ constructWildcardFilter(col)+")");
    		  parentFkColumn=tm.getParentFKColumn(col.getTable());
    	  }
      }      
      PreparedStatement fsPstmt=null;
      boolean isFastSave=(refTable.getJSONProps()!=null && refTable.getJSONProps().optBoolean("fast_save"));
      PreparedStatement pstmt=null;
      ResultSet result=null;
      boolean isUpperCase = akColumn.isUpperCase();
      Table aliasTable=null;
      BigDecimal tmpValue;
      try{
      	  logger.debug("object obtain sql= "+ sqlStr);
          pstmt=conn.prepareStatement(sqlStr.toString());
      	  if(isFastSave){
      		  String fsSQL=getFastSaveSQL(refTable,col,event,user);
      		  logger.debug("fast save sql= "+ fsSQL);
      		  fsPstmt= conn.prepareStatement(fsSQL);
      	  }
          if(isAliasSupportTable){
        	 aliasTable=tm.getTable(((AliasSupportTable)refTable).getAliasTable());// m_product_alias
          	String pkAssocColumn= ((AliasSupportTable)refTable).getAssociatedColumnInAliasTable(); // m_product_id, this is in m_product_alias
          	assocColumns=((AliasSupportTable)refTable).getOtherAssociatedColumnsInAliasTable();// m_attributesetinstance_id
          	
          	String aliasTableFilterSql=prepareSQLForAliasTableQuery((AliasSupportTable)refTable,helper, event);
            logger.debug("alias sql= "+ aliasTableFilterSql);
            
          	aliasPstmt=conn.prepareStatement(aliasTableFilterSql);
          }
          logger.debug("test is uphere!!!!");
          int tableId= col.getTable().getId();
          List rcwf=col.getReferenceColumnsInWildcardFilter();
          Column rc;
		  HashMap valuesHashMap=(HashMap)event.getParameterValue("ColumnValueHashMap");
		  Vector v=null;
          for(int i = 0;i<length;i++){
              try{
                  checkImpl.isColumnValid(col,objectStr[i]);
                  // if objectId is set, we will not check ui input then
                  if(objectIds!=null && Validator.isNotNull(objectIds[i])){
              		resultInt[i] =new BigDecimal( objectIds[i]);
              	  }else{
	                  if(col.isNullable() && Validator.isNull( objectStr[i])){
	                  	// commented 2007-04-16, check alias support table instead of skip
	                  	// @since 3.0
	                  	//continue;
	                  }else{
	                  	// find data from event
		                  if(isUpperCase) objectStr[i]= objectStr[i].toUpperCase();
		                  //logger.debug(objectStr[i]);
		                  pstmt.setString(1,objectStr[i] );
		                  int cidx=1;
		                  if(ak2Column!=null)pstmt.setString(++cidx,objectStr[i] );
		                  
		                  if(col.isFilteredByWildcard()){
		                	  for(int j=0;j<rcwf.size();j++ ){
		                		  rc= (Column) rcwf.get(j);
		                		  if(tableId!= rc.getTable().getId()){
		                			  //parent table, find parent column id
		                			  v=  (Vector)valuesHashMap.get(parentFkColumn.getName() );
		                			  if(v==null || v.size()==0) throw new NDSException("Internal error:"+ col+" need values from "+ rc+" while not found, check parentFKcolumn setting");
		                		  }else{
		                			  //same table
		                			  System.out.print("column name ->"+rc.getName());
		                	         if( rc.getReferenceTable()!=null){
		                	        		String name= rc.getName() +"__"+ rc.getReferenceTable().getAlternateKey().getName().toUpperCase();
		                	        		//System.out.print(event.getParameterValue("ColumnValueHashMap"));
		                	        		//if(Validator.isNull((String) event.getParameterValue("ColumnValueHashMap"))){
		                	        		if(event.getParameterValue("ColumnValueHashMap")==null){
		                	        			String value=(String)event.getParameterValue(name);
		                	        			v=new Vector(); 
		                						QueryRequestImpl query = QueryEngine.getInstance().createRequest(null);
		                						query.setMainTable(rc.getReferenceTable().getId());
		                						query.addSelection(rc.getReferenceColumn().getId());
		                						query.addParam(rc.getReferenceTable().getAlternateKey().getId(), "="+ value);
		                						QueryResult rs = QueryEngine.getInstance().doQuery(query);
		                						if (rs != null && rs.getTotalRowCount() > 0) {
		                							while (rs.next()) {
		                								logger.debug(rs.getObject(1).toString());
		                								BigDecimal[] itemId = new BigDecimal[1];
		                								itemId[0] =new BigDecimal( Tools.getInt(rs.getObject(1),-1) );
		                								v.addElement(itemId);
		                							}
		                						}
		                	        			
		                	        				//	
		                	        			
		                	        			//System.out.print("new value ->"+value);
		                	        		}else{
		                	        		 //System.out.print("new name ->"+name);
		                	        		 System.out.print("new value ->"+(Vector)valuesHashMap.get(rc.getName()));
		                	        		 v= (Vector)valuesHashMap.get(rc.getName());
		                	        		}
		                	         }else{
		                	        	 if(event.getParameterValue("ColumnValueHashMap")!=null)
		                	        		 System.out.print("new value ->"+(Vector)valuesHashMap.get(rc.getName()));
		                	        		 v= (Vector)valuesHashMap.get(rc.getName());
		                	         }
		                	         
		                			  
		                			  if( v==null || v.size()==0) throw new NDSException("Internal error:"+ col+" need values from "+ rc+" while not found ("+ rc+" should locate before "+ col);
		                			  
		                		  }
		                		  // rc may be fk column, or limit value
		                		  if(v.elementAt(0) instanceof BigDecimal[]){
		                			  BigDecimal[] d=(BigDecimal[]) v.elementAt(0);
		                			  logger.debug(" value for "+ rc + ":"+d[i]);
		                			  pstmt.setBigDecimal(++cidx,d[i]);
		                		  }else if (v.elementAt(0) instanceof String[]){
		                			  String[] d=(String[]) v.elementAt(0);
			                		  logger.debug(" value for "+ rc + ":"+d[i]);
			                		  pstmt.setString(++cidx,d[i]);
		                		  }else{
		                			  logger.debug("Take as object since not supported as type:"+v.elementAt(0).getClass());
		                			  Object[] d=(Object[]) v.elementAt(0);
			                		  logger.debug(" value for "+ rc + ":"+d[i]);
			                		  pstmt.setObject(++cidx,d[i]);
		                		  }
		                	  }
		                  }
		                  
		                  //logger.debug("str:"+ objectStr[i]);
		                  //按纤丝鸟要求，条码优先，而不是款号优先
		                  tmpValue =null;
		                  if(isAliasSupportTable){
			                  // try alias table if supported, 目前未考虑在别名表上支持wcf 类型的字段
			                  // since 3.0
			                  
		                  		/*look for associated columns in alias table, and find matched one for this column,
		                  		  will also setup other columns in associated column list*/
	                  		  tmpValue = findInAliasTable(objectStr[i], col, i, event, aliasPstmt,assocColumns,eventValueName,aliasTable);
		                  }
                  		  if(tmpValue!=null){
                  			  resultInt[i] =tmpValue;
                  		  }else{
                  			  result= pstmt.executeQuery();
                  			  if(result.next() ){
                  				  resultInt[i] = result.getBigDecimal(1);
                  			  }else{
                  				  //add support for ad_table( fast_save), will save if not found 2010.2.9 yfzhu
                  				  
                  				  if(isFastSave){
                  					  resultInt[i]=new BigDecimal( fastSaveNewValue(refTable,objectStr[i], i,fsPstmt));
                  					  
                  				  }else{
	                  				  if(this.isBestEffort ){
			                              this.setRowInvalid(i,objectStr[i]+" ("+refTablleDesc+")@not-exists-or-invalid@" );
			                              resultInt[i]=new BigDecimal(-1);
			                          }else 
			                          	throw new NDSEventException("@line@ "+(i+1)+": "+objectStr[i]+"("+refTablleDesc+")@not-exists-or-invalid@");
                  				  }
                  			  }
                  		  }
		                  
		                  /*result= pstmt.executeQuery();
		                  if(result.next() ){
		                      resultInt[i] = result.getBigDecimal(1);
		                  }else{
		                  	// try alias table if supported, 目前未考虑在别名表上支持wcf 类型的字段
		                  	// since 3.0
		                  	tmpValue =null;
		                  	if(isAliasSupportTable){
		                  		tmpValue = findInAliasTable(objectStr[i], col, i, event, aliasPstmt,assocColumns,eventValueName);
		                  	}
	                  		if(tmpValue!=null){
	                  			resultInt[i] =tmpValue;
	                  		}else{
		                  	
		                    // commented 2006-7-1  
		                  	//if(!col.isNullable() ){
		                          if(this.isBestEffort ){
		                              this.setRowInvalid(i,objectStr[i]+" ("+refTablleDesc+")@not-exists-or-invalid@" );
		                              resultInt[i]=new BigDecimal(-1);
		                          }else 
		                          	throw new NDSEventException("@line@ "+(i+1)+": "+objectStr[i]+"("+refTablleDesc+")@not-exists-or-invalid@");
		                    //} 
	                  		}
		                  }*/
	                  }
              	  }
              }catch(Exception e){
                  if(this.isBestEffort ){
                      this.setRowInvalid(i, col.getDescription(Locale.CHINA) + ": "+ e.getLocalizedMessage()  );
                      resultInt[i]=new BigDecimal(-1);
                  }else{
	                  if(e instanceof NDSEventException){
	                        throw e;//throw new NDSEventException(e.getMessage());
	                  }
	                  logger.debug("error found",e);
	                  throw new NDSEventException("@exception@",e);
                  }
              }finally{
                  if( result !=null) try{ result.close() ;}catch(Exception e2){}
              }
            }
      }catch(Exception e21){
          throw new NDSEventException(e21.getMessage(), e21);
      }finally{
          if( pstmt !=null) try{ pstmt.close();}catch(Exception e3){}
          if( aliasPstmt !=null) try{ aliasPstmt.close();}catch(Exception e3){}
          if(fsPstmt!=null)try{ fsPstmt.close();}catch(Exception e3){}
      }
      vec.add(resultInt);
      return vec;
   }
  /**
   针对以下形式构造sql过滤语句
   		   * （来自于主表的数据的获取）
    		   * 1)在移库单中，明细输入的移出库位必须属于移出仓库
    		   * 2)在药品采购单中，明细的药品必须和头上的项目设置一致
    		   * 
    		   * (来自同表的数据的获取，要求字段必须靠前）
    		   * 3）在运单中，运输地址必须属于运输客户
    		   * 
    		   * 这些模式都要求在明细上的字段的过滤器来自于单头的信息，明细字段配置WCF:
    		   * 1)m_locator.m_warehouse_id=@m_v_inout.m_whsout_id@
    		   * 2)m_product.c_project_id=@c_order.c_project_id@
    		   * 3)c_bpartner_location.c_bpartner_id=@l_order.s_bpartner_id@
   *  
   * @param column
   * @return for "m_locator.m_warehouse_id=@m_v_inout.m_whsout_id@" 
   * 	return "m_locator.m_warehouse_id=(select m_whsout_id from m_inout m_v_inout where m_v_inout.id=?)"
   * 	and in event, set value for @m_v_inout.m_whsout_id@ of each row
   * @throws Excpetion
   */
  private String constructWildcardFilter(Column column){
	  int tableId= column.getTable().getId();
	  List al=column.getReferenceColumnsInWildcardFilter();
	  String sql= column.getFilter();
	  String sql2;
	  for(int i=0;i< al.size();i++){
		  Column col= (Column) al.get(i);
		  if(col.getTable().getId()!=tableId){
			  // parent table column
			  sql2="(select " + col.getName()+ " from "+ col.getTable().getRealTableName()+" "+
			  		col.getTable().getName()+" where id=?)"; 
		  }else{
			  // same table
			  sql2="?";
		  }
		  sql= StringUtils.replace(sql, "@"+ col.getTable().getName()+"."+ col.getName()+"@",sql2); 
	  }
	  return sql;
  }
  /**
   * Prepare sql for alias table query 
   * @param refTable
   * @param helper
   * @param event
   * @return
   * @throws Exception
   */
  private String prepareSQLForAliasTableQuery(AliasSupportTable refTable, DefaultWebEventHelper helper, DefaultWebEvent event) throws Exception{
  	TableManager tm=TableManager.getInstance();
  	Table aliasTable=tm.getTable(((AliasSupportTable)refTable).getAliasTable());// m_product_alias
  	String pkAssocColumn= ((AliasSupportTable)refTable).getAssociatedColumnInAliasTable(); // m_product_id, this is in m_product_alias
    PairTable assocColumns=((AliasSupportTable)refTable).getOtherAssociatedColumnsInAliasTable();// m_attributesetinstance_id
    //get read permission filter on alias table
    Expression aliasTableFilter= helper.getSecurityFilter(aliasTable.getName(), 1,helper.getInt((String)event.getParameterValue("operatorid"), -1), event.getQuerySession() );
    
    String aliasTableFilterSql=null;
    // refTableName must not be real table, but column filter may use the alias table as reference   
    StringBuffer aliasTableFilterSqlStr=new StringBuffer( "SELECT ");
    aliasTableFilterSqlStr.append(pkAssocColumn);
    if(assocColumns!=null){
    	for(int i=0;i< assocColumns.size();i++){
    		aliasTableFilterSqlStr.append(",").append(assocColumns.getKey(i));
    	}
    }
    aliasTableFilterSqlStr.append(" FROM ").append(aliasTable.getRealTableName()+
	    " "+aliasTable.getName()+" WHERE "+(aliasTable.isAcitveFilterEnabled()?"ISACTIVE='Y' AND (":"(")+aliasTable.getAlternateKey().getName()+" = ?");
    if(aliasTable.getAlternateKey2()!=null){
    	aliasTableFilterSqlStr.append(" OR ").append(aliasTable.getAlternateKey2().getName()).append("=?");
    }
    aliasTableFilterSqlStr.append(")");
    // add refTable filter
    if(aliasTable.getFilter()!=null){
    	aliasTableFilterSqlStr.append(" AND "+ aliasTable.getFilter());
    }
    // add ad_client_id control, see Table.isClientIsolated for detail
    if (aliasTable.isAdClientIsolated()){
    	 int ad_client_id= helper.getAdClientId(event.getQuerySession(), true);
    	 aliasTableFilterSqlStr.append(" AND AD_CLIENT_ID="+ ad_client_id);
    }   		
    
    if(aliasTableFilter !=null && ! aliasTableFilter.isEmpty()){
        QueryRequestImpl req= QueryEngine.getInstance().createRequest(event.getQuerySession());
        req.setMainTable(aliasTable.getId());
        req.addParam(aliasTableFilter);
        aliasTableFilterSql= req.toPKIDSQL(true);
        aliasTableFilterSqlStr.append(" AND ID IN (" + aliasTableFilterSql + ")");
    }
    return aliasTableFilterSqlStr.toString();
  }
  /**
   * Find column data in its alias table for matching records. Will also try to fill
   * all other associated columns in event object for later reference.
   * Current does not write to event value named "ColumnValueHashMap", which will be used by wfc column (may be bugs)
   * since it's difficult to do update(yfzhu 2009-10-24)
   *  
   * @param value AK value in alias table
   * @param column FK column
   * @param index current index in event 
   * @param event
   * @param eventValueName 
   * @return null if no record found
   * @throws Exception
   */
  private BigDecimal findInAliasTable(String value, Column column, int index, 
		  DefaultWebEvent event, PreparedStatement aliasPstmt,
		  PairTable assocColumns, String eventValueName, Table aliasTable) throws Exception{
    ResultSet res= null;
    aliasPstmt.setString(1,value );
    if(aliasTable.getAlternateKey2()!=null) aliasPstmt.setString(2,value );
    res= aliasPstmt.executeQuery();
    BigDecimal aId=null;
    if(res.next()){
    	aId= res.getBigDecimal(1);
    	if(assocColumns!=null){
        	for(int i=0;i< assocColumns.size();i++){
        		Object assValue= res.getObject(i+2);
        		event.setValue((String)assocColumns.get(assocColumns.getKey(i)), assValue,eventValueName,index);
        		// should write to ColumnValueHashMap?
        	}
        }
    }
    return aId;
    
  }
  /**
   * SQL for fast save, only ak is input from ui
   * @param table reference table, has "fast_save" property set
   * @param rcol the column to work on, has reference table refers to <param>table</param>
   * @return sql 
   */
  private String getFastSaveSQL(Table table,Column rcol, DefaultWebEvent event,nds.security.User user ) throws Exception{
	  StringBuffer sb=new StringBuffer();
	  sb.append("insert into ").append(table.getRealTableName()).append(" ").
	  	append(table.getName()).append("(ID,").append(table.getAlternateKey().getName());

	  StringBuffer s2=new StringBuffer("?,?");
	  HashMap m=new HashMap();
	  if(rcol.getJSONProps()!=null ){
		  JSONObject jo= rcol.getJSONProps().optJSONObject("fast_save_dv");
		  if(jo!=null){
			  logger.debug("fast_save_dv:"+ jo);
			  //fetch defaul value for that column
			  for(Iterator it=jo.keys();it.hasNext();){
				  String key= ((String)it.next());
				  m.put(key.toUpperCase(),key);
				  Object value= jo.get(key);
				  sb.append(",").append(key);
				  logger.debug("fast_save_dv:"+key+","+value );
				  if(value instanceof String)
					  s2.append(",").append(QueryUtils.TO_STRING((String)value));
				  else
					  s2.append(",").append(value);
			  }
		  }
	  }	  
	  //columns that has default value, and AK will be saved
	  ArrayList cols=table.getAllColumns();
	  for(int i=0;i<cols.size();i++){
		  Column col= (Column) cols.get(i);
		  
		  if(col.isAlternateKey()|| col.isVirtual() || m.get(col.getName())!=null)continue;
		  
		  if("operate".equals(col.getObtainManner())){
			  sb.append(",").append(col.getName());
			  s2.append(",").append(user.id);
			  
		  }else if("sysdate".equals(col.getObtainManner())){
			  sb.append(",").append(col.getName());
			  s2.append(",sysdate");
		  }else{
		  
			  String dv= col.getDefaultValue(true);
			  if (dv==null) continue;
			  dv=event.getStringWithNoVariables(dv);
			  /*if(col.getType()!=Column.STRING){
				  throw new NDSRuntimeException("found default value for none-string column:"+ col);
			  }*/
			  if(nds.util.Validator.isNotNull(dv)){
				  sb.append(",").append(col.getName());
				  s2.append(",");
				  if( col.getType()== Column.STRING){
					  s2.append(QueryUtils.TO_STRING(dv));
				  }else{
					  s2.append(dv);
				  }
			  }
		  }
	  }
	  sb.append(") values (").append(s2).append(")");
	  return sb.toString();
  }
  /**
   * 
   * @param table the table that will be added new record
   * @param value record ak value 
   * @param userId 
   * @param rowIdx row index in array, will notify user the row index if error found
   * @return record id created
   * @throws Exception
   */
  private int fastSaveNewValue(Table table, String value,int rowIdx,PreparedStatement pstmt)throws Exception{
	  try{
		  int id= QueryEngine.getInstance().getSequence(table.getName(), conn);
		  pstmt.setInt(1,id);
		  pstmt.setString(2, value);
		  pstmt.executeUpdate();
		  return id;
	  }catch(Exception e){
		  if(this.isBestEffort ){
              this.setRowInvalid(rowIdx,value+":"+ e.getMessage() );
              return -1;
          }else 
          	throw new NDSEventException("@line@ "+(rowIdx+1)+": "+
          			value+"("+table.getDescription(locale)+")@exception@:"+ e.getMessage());
	  }
  }
  /**
   * Fetch default value from <default-value> in definition xml.
   * The default value may be a variable, such like $AD_Client_ID
   * Only when column is not modifiable, will this proceeded. (Otherwise
   * The value should be retrieved from screen UI.
   * @param resultInt
   * @param event
   * @param column 
   * @throws NDSException if default value is not valid in definition file
   */
   private void fetchFromDefault(BigDecimal[] resultInt, DefaultWebEvent event, Column column) throws NDSException{
   		String df=column.getDefaultValue();
   		int v;
   		BigDecimal bd=null;
   		if (df==null || df.length()==0) return; 
   		if (df.startsWith("$")){
   			// is a variable name, fetch value from event's session object
   			QuerySession session=(QuerySession)event.getParameterValue("nds.query.querysession");
   			if(session ==null ){
   				logger.warning("Found no session object in event");
   				return;
   			}
   			Object att=session.getAttribute(df);
   			if( att==null) throw new NDSException("Could not found " + df+ " in session attributes"); 
   			bd =new BigDecimal( att.toString());
   		}else{
   			// parse as  integer
   			v=Tools.getInt(df,-1);
   			if ( v==-1) return;
   			bd= new BigDecimal(v) ;
   			
   		}
   		for( int i=0;i< resultInt.length;i++) resultInt[i]=bd;
   }

}