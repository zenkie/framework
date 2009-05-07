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
 * 如出入库单等支持属性矩阵输入，矩阵的信息存储在m_attributedetail 表中
 * originaly created at 2007-05-30
 * @since 3.0
 */
public class AttributeDetailSupportTableImpl extends TableImpl {
	public boolean supportAttributeDetail(){
		return true;
	}
	
	
}
