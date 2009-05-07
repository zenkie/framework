package nds.schema;

import java.util.ArrayList;
public class Sum implements SumMethod{

    public Sum() {
    }
    /**
     * 计算汇总值，注意data中可能包含无效值（如null), 作0处理
     * @param data 将列值进行汇总, data element can be string
     */
    public double calculate(ArrayList data){
        double d=0;
        if( data ==null) return d;
        double v;
        for( int i=0;i< data.size();i++){
            try{
                v=(new Double(data.get(i)+"")).doubleValue();
            }catch(Exception e){
                v= 0;
            }
            d +=v;
        }
        return d;
    }

}