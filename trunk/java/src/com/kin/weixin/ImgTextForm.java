package com.kin.weixin;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Kone
 * Date: 13-5-21
 * Time: 上午12:01
 * To change this template use File | Settings | File Templates.
 */
public class ImgTextForm {
    private String error = "false";
    private String count;
    private String AppMsgId = "";
    private String token;
    private String ajax = "1";
    private String lang = "zh_CN";
    private String t = "ajax-response";
    private String sub = "create";
    private List<Piece> pieces;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getAjax() {
        return ajax;
    }

    public void setAjax(String ajax) {
        this.ajax = ajax;
    }

    public String getAppMsgId() {
        return AppMsgId;
    }

    public void setAppMsgId(String appMsgId) {
        AppMsgId = appMsgId;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void setPieces(List<Piece> pieces) {
        this.pieces = pieces;
        if (null != pieces)
            this.count = pieces.size() + "";
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class Piece {
        private String title;
        private String digest;
        private String content;
        private String fileid;
        private ImgFileForm img;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getDigest() {
            return digest;
        }

        public void setDigest(String digest) {
            this.digest = digest;
        }

        public String getFileid() {
            return fileid;
        }

        public void setFileid(String fileid) {
            this.fileid = fileid;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public ImgFileForm getImg() {
            return img;
        }

        public void setImg(ImgFileForm img) {
            this.img = img;
        }
    }
}
