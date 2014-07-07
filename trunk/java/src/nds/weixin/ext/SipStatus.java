package nds.weixin.ext;

import java.util.Map;
import java.util.HashMap;

/**
 * Desc:
 * <p/>
 */
public enum SipStatus
{

    ERROR( "error", 			"0000"),//服务请求失败
    SUCCESS( "success", 						"0"),//服务请求成功
    INERFACEERROR( "inerfaceerror", 		"1000"),//回调接口失败的默认错误
    USER_INVALID( "userinvalid", 		"1001"),//与用户所填写用户名相关的错误
    PHONE_INVALID( "phoneinvalid", 					"1002"),//与用户所填写手机号相关的错误
    EMAIL_INVALID( "emailinvalid", 			"1003"),//与用户所填写邮箱相关的错误
    USERID_INVALID( "useridinvalid", 				"1004"),//与用户所填写身份证相关的错误
    PWD_INVALID( "pwdinvalid", 					"1005"),//绑卡实体卡号密码相关的错误
    VCODE_INVALID( "vcodeinvalid", 				"1006"),//绑卡实体卡验证码相关的错误
    BRTH_INVALID( "brthinvalid", 						"1007"),//与用户所填写生日相关的错误
    SEX_INVALID( "sexinvalid", 			"1008"),//与用户所填写性别相关的错误
    CARD_FAILD( "cardfaild", 					"1009"),//实体卡未找到或已被绑定
  
    PARAM_ERROR( "paramerror", 	"1"),//必需参数未传递
    SERVICE_NOTEXIST( "servicenot", 				"2"),//方法内部执行错误,如系统繁忙
    PARAM_NONEED( "paramnoneed",				"3"),//参数无效,请求方法的参数不合要求
    SIGN_ERROR( "signerror",					"4"),//sig 校验出错
    AUTH_FAILD( "authfaild",					"5"),//没有权限操作
    PROCESS_ERROR( "processerror",					"6"),//逻辑性错误
    REPAIT_ERROR( "repaiterror",					"7");//重复操作

    /*
{"0000":"服务请求失败","9999":"服务请求成功","1001":"签名无效","1002":"请求过期","1003":"用户绑定失败","1004":"需要绑定用户","1005":"/需要提供AppKey","1006":"需要提供服务名","1007":"需要提供签名","1008":"需要提供时间戳","1009":"用户认证失败","1010":"无权访问服务","1011":"服务不存在","1012":"需要提供SessionId","1013":"需要提供用户名"}
     */
    
    private String v;
    private String c;

    private static Map<String,SipStatus> status ;

    SipStatus(String value, String code)
    {
        v = value;
        c = code;
    }

    @Override
    public String toString() {
        return v;
    }

    public String getCode() {
        return c;
    }

    public static SipStatus getStatus(String code) {
        if(status == null) {
            status = new HashMap<String,SipStatus>();
            status.put("0000",ERROR);
            status.put("0",SUCCESS);
            status.put("1000",INERFACEERROR);
            status.put("1001",USER_INVALID);
            status.put("1002",PHONE_INVALID);
            status.put("1003",EMAIL_INVALID);
            status.put("1004",USERID_INVALID);
            status.put("1005",PWD_INVALID);
            status.put("1006",VCODE_INVALID);
            status.put("1007",BRTH_INVALID);
            status.put("1008",SEX_INVALID);
            status.put("1009",CARD_FAILD);
            status.put("1",PARAM_ERROR);
            status.put("2",SERVICE_NOTEXIST);
            status.put("3",PARAM_NONEED);
            status.put("4",SIGN_ERROR);
            status.put("5",AUTH_FAILD);
            status.put("6",PROCESS_ERROR);
            status.put("7",REPAIT_ERROR);
        }

        return status.get(code);
    }
}
