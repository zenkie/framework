package nds.schema;
/**
 * Singleton
 */
public class SumMethodFactory {
    private static SumMethodFactory instance=null;
    private SumMethodFactory() {
    }
    /**
     * Create sum method according to name, currently only "sum" supported
     */
    public SumMethod createSumMethod(String name){
        if( ! "sum".equalsIgnoreCase(name)) throw new Error("Internal Error:"+ name +" is not supported.");
        return new Sum();
    }
    public static SumMethodFactory getInstance(){
        if(instance==null)instance= new SumMethodFactory();
        return instance;
    }
}