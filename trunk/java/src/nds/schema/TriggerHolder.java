package nds.schema;

import java.util.Properties;
import nds.util.*;
/**
 * For TableMapping to hold trigger
 * <p>Title: NDS Project</p>
 * <p>Description: DRP System</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Agile Control Technologies</p>
 * @author unascribed
 * @version 1.0
 */
public class TriggerHolder {
    Properties triggers=new Properties();
    public TriggerHolder() {
    }
    public void setAfterCreate(String t){
        if ( Validator.isNull(t)) t="";
        triggers.setProperty("AC", t);
   }
    public void setBeforeModify(String t){
        if ( Validator.isNull(t) ) t="";
        triggers.setProperty("BM", t);
    }
    public void setAfterModify(String t){
         if ( Validator.isNull(t)) t="";
         triggers.setProperty("AM", t);
    }
    public void setBeforeDelete(String t){
        if (Validator.isNull(t) ) t="";
        triggers.setProperty("BD", t);
   }
    public Properties getTriggers(){
        return triggers;
    }
}