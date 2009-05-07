
package nds.mail;



/**
 * 存放消息通知的参数设置
 */
public class NotifyParams
{

   /**
    * 在哪张表上操作，表名一率大写
    */
   private String tableName;

   /**
    * SQL 条件，即满足此条件的单据或对象将触发通知发布
    * 假设存储的sqlCondition为
    * sqlCondtion= select id from <table> , <table2> where <table>.<xxx> like
    * '$Operator' or <table2>.<yyy> = sysdate
    * 则下列语句可以进行条件判断
    * select count(*) from table where id= xxxx and id in (sqlCondition)
    * 如果大于0 表示当前操作满足
    */
   private String sqlCondition;

   /**
    * 在当前完成的动作，如create, modify, delete, submit,rollback,permit,all, 具为小写
    */
   private String tableAction;

   /**
    * 自动机将完成的动作，目前仅支持mail
    */
   private String robotAction;

   /**
    * 参数，将在自动机动作中展现，如 MailRobotSession.robotParam="$me"
    * 表示邮件将发给我
    */
   private String robotParam;

   /**
    * 本条记录创建的所有者
    */
   private int ownerId;

   /**
    * @roseuid 3E698FF003BA
    */
   public NotifyParams()
   {

   }
}
