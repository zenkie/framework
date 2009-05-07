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
 * 凡设置了此表作为实现类的表定义，在明细表被调用nds.control.ejb.command.CheckProductAttribute时，
 * 将由系统自动调用对应主表的对应存储过程主表名＋"_CHKPDT"(主表记录id, 物料id)，进行是否允许插入指定产品的检验。
 * 
 * 例如，在入库单明细扫描输入时，如果发现有非入库单上已经设置的产品，应立刻禁止输入
 * @since 4.0 
 */
public class ProductCheckTableImpl extends TableImpl {

}
