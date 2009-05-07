package nds.schema;

import java.util.ArrayList;
/**
 * 对字段列的统计方法。在.table文件的<column> <sum-method></sum-method> </column> 中
 * 定义。
 */
public interface SumMethod {

    /**
     * 计算汇总值，注意data中可能包含无效值（如null)
     * @param data 将列值进行汇总
     */
    public double calculate(ArrayList data);

}