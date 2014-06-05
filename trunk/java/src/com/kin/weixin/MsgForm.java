package com.kin.weixin;/**
 * Created with IntelliJ IDEA.
 * User: Kone
 * Date: 13-5-21
 * Time: 上午12:17
 * To change this template use File | Settings | File Templates.
 */



/**
 * 群发消息表单
 */
public class MsgForm {
    /**
     * fid | appmsgid
     */
    private String type = "1";
    private String content = "";
    private String error = "false";
    private String needcomment = "0";
    private String groupid = "-1";
    private String sex = "0";
    private String country = "";
    private String province = "";
    private String city = "";
    private String token = "";
    private String ajax = "1";

    public String getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type.getType() + "";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getNeedcomment() {
        return needcomment;
    }

    public void setNeedcomment(String needcomment) {
        this.needcomment = needcomment;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAjax() {
        return ajax;
    }

    public void setAjax(String ajax) {
        this.ajax = ajax;
    }

}

