package com.kin.weixin;

/**
 * Created with IntelliJ IDEA.
 * User: Kone
 * Date: 13-5-21
 * Time: 上午12:18
 * To change this template use File | Settings | File Templates.
 */

/**
 * 消息类型
 *
 * @author Kone
 */
public enum MsgType {
    TEXT(0), VOICE(3), IMAGE(2), VIDEO(4), IMAGE_TEXT(10);
    private int type;

    private MsgType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
