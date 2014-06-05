package com.kin.weixin;

/**
 * Created with IntelliJ IDEA.
 * User: Kone
 * Date: 13-5-21
 * Time: 上午12:17
 * To change this template use File | Settings | File Templates.
 */


/**
 * 群发消息返回json对象
 *
 * @author Kone
 */
public class MsgJson {
    private int ret;
    private String msg;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
