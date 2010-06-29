package nds.rest;

import java.util.Map;
import java.util.HashMap;

/**
 * Desc:
 * <p/>
 */
public enum SipStatus
{
    ERROR( "error", 							"0000"),//服务请求失败
    SUCCESS( "success", 						"9999"),//服务请求成功
    SIGNATURE_INVALID( "signatureInvalid", 		"1001"),//签名无效
    REQ_TIMEOUT( "reqTimeout", 					"1002"),//请求过期
    BINDUSER_FAILD( "binduserFaild", 			"1003"),//用户绑定失败
    NEED_BINDUSER( "needBinduser", 				"1004"),//需要绑定用户
    NEED_APPKEY( "needAppKey", 					"1005"),//需要提供AppKey
    NEED_APINAME( "needApiName", 				"1006"),//需要提供服务名
    NEED_SIGN( "needSign", 						"1007"),//需要提供签名
    NEED_TIMESTAMP( "needTimeStamp", 			"1008"),//需要提供时间戳
    AUTH_FAILD( "authFaild", 					"1009"),//用户认证失败
    NORIGHT_CALLSERVICE( "noRightCallService", 	"1010"),//无权访问服务
    SERVICE_NOTEXIST( "service", 				"1011"),//服务不存在
    NEED_SESSIONID( "sessionid",				"1012"),//需要提供SessionId
    NEED_USERNAME( "username",					"1013");//需要提供用户名

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
            status.put("9999",SUCCESS);

            status.put("1001",SIGNATURE_INVALID);
            status.put("1002",REQ_TIMEOUT);
            status.put("1003",BINDUSER_FAILD);
            status.put("1004",NEED_BINDUSER);
            status.put("1005",NEED_APPKEY);
            status.put("1006",NEED_APINAME);
            status.put("1007",NEED_SIGN);
            status.put("1008",NEED_TIMESTAMP);
            status.put("1009",AUTH_FAILD);
            status.put("1010",NORIGHT_CALLSERVICE);
            status.put("1011",SERVICE_NOTEXIST);
            status.put("1012",NEED_SESSIONID);
            status.put("1013",NEED_USERNAME);
        }

        return status.get(code);
    }
}