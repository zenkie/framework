/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.*;
import java.util.Collections;
import nds.web.bean.*;
import nds.util.PairTable;
/**
 * m_product 表的实现类，主要是为了支持m_alias 别名表
 * m_alias 存储了条码和 物料及属性的对应关系
 * 对于不使用m_alias的应用系统，可以不将此类关联到m_product
 * @since 3.0 
 */
public class ProductTableImpl extends TableImpl implements AliasSupportTable{
	private final static String ALIAS_TABLE="M_PRODUCT_ALIAS";
	private final static String ASSOCIATED_COLUMN="M_PRODUCT_ID";
	private static PairTable assocColumns;
	public ProductTableImpl(){
		assocColumns=new PairTable();
		assocColumns.put("M_ATTRIBUTESETINSTANCE_ID", "M_ATTRIBUTESETINSTANCE_ID");
	}
	/**
	 * Get the alias table name, e.g, for m_product table, it should return m_alias
	 * @return table name
	 */
	public String getAliasTable(){
		return ALIAS_TABLE;
	}
	/**
	 * Return assoicated column name in alias table for the pk of this table
	 * @return
	 */
	public String getAssociatedColumnInAliasTable(){
		return ASSOCIATED_COLUMN;
	}
	
	public PairTable getOtherAssociatedColumnsInAliasTable(){
		return assocColumns;
	}


}
