package nds.mail.test;
import junit.framework.TestCase;
import nds.mail.NotificationManager;
import nds.schema.Table;
import nds.schema.TableManager;
public class MailRobotSessionTest extends TestCase {
//    DbConnectionDefaultPool pool;
    public MailRobotSessionTest(String name) {
          super(name);
    }
    public static void main(String[] args)throws Exception{
    }
    /*       * @param tableId
            * @param objectId
            * @param tableAction
            * @param briefMsg
    * @param detailMsg   */
    public void testMail(){
        TableManager tm= TableManager.getInstance();
        Table table=tm.getTable("Department");
        NotificationManager.getInstance().handleObject(table.getId(),110035, "create",
                new StringBuffer("单据被创建"),new StringBuffer("着是您的单据"),null);
    }
}