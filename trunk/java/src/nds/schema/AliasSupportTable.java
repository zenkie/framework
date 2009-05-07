/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.schema;

import java.util.*;
import java.util.Collections;
import nds.util.PairTable;
/**
 * 
 * 物料表m_product拥有一张支持物料别名的表 m_product_alias, 当界面上输入的内容为条码的时候，
 * 系统将更加m_product_alias表找到相应记录，并获取其中的m_product_id作为单据对应于m_product表的fk
 * 字段（通常也是m_product_id名称)的值进行设置。
 * 
 * 同时，通过m_product上指明的关联字段(m_attributesetinstance_id）,系统还将m_product_alias记录上的
 * m_attributesetinstance_id的值传递给单据的对应字段，这个设置是通过 AliasSupportTable#getAssociatedColumnsInAliasTable
 * 来完成的.
 * @since 3.0
 */
public interface AliasSupportTable extends Table {
	/**
	 * Get the alias table name, e.g, for m_product table, it should return m_product_alias
	 * @return table name
	 */
	public String getAliasTable();
	
	/**
	 * Return assoicated column name in alias table for the pk of this table
	 * @return
	 */
	public String getAssociatedColumnInAliasTable(); 
	/**
	 * Return array of columns in reference table, which contains one aliassupporttable column, 
	 * and the correlation column in alias table.
	 * For instance, for m_product table, it should return 
	 * 	{
	 * 		{"m_product_alias.m_attributesetinstance_id","m_attributesetinstance_id"}
	 *	}
	 *@return PairTable null if no other columns
	 *	key: String the column name in alias table 
	 *	value: String the column name in reference table
	 */
	public PairTable getOtherAssociatedColumnsInAliasTable();


}
