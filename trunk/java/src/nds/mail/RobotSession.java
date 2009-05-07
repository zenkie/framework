//Source file: f:\\tmp\\mail\\RobotSession.java

package nds.mail;
import java.util.Properties;

public interface RobotSession
{
    public void init(Properties props);
    /**
    * Hanle object action accrording to actions
    * @param tableId
    * @param objectId
    * @param tableAction inculding "create, modify, delete, submit,rollback,permit"
    * @param briefMsg will be used as mail subject or sms body
    * @param detailMsg will be used as mail body if MailRobot is triggered.
    * @roseuid 3E68B54C0383
    */
   public void handleObject(int tableId, int objectId, String tableAction, StringBuffer briefMsg, StringBuffer detailMsg);
}
