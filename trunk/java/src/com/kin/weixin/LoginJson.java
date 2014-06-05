package com.kin.weixin;

/**
 * 登录返回信息Json对象
 *
 * @author Kone
 */
public class LoginJson {
    public int Ret;
    public String ErrMsg;
    public int ShowVerifyCode;
    public int ErrCode;

    public int getRet() {
        return Ret;
    }

    public void setRet(int ret) {
        Ret = ret;
    }

    public String getErrMsg() {
        return ErrMsg;
    }

    public void setErrMsg(String ErrMsg) {
        this.ErrMsg = ErrMsg;
    }

    public int getShowVerifyCode() {
        return ShowVerifyCode;
    }

    public void setShowVerifyCode(int showVerifyCode) {
        ShowVerifyCode = showVerifyCode;
    }

    public int getErrCode() {
        return ErrCode;
    }

    public void setErrCode(int errCode) {
        ErrCode = errCode;
    }

}
