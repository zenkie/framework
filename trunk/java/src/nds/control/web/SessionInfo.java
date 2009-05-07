package nds.control.web;
/**
 * Title:        NDS Project
 * Description:  San gao shui yuan, mu xiang ren jia
 * Copyright:    Copyright (c) 2001
 * Company:      aic
 * @author yfzhu
 * @version 1.0
 */

import nds.control.web.*;

public class SessionInfo implements java.io.Serializable{
    private int userId;
    private String hostIP;
    private String sessionId;
    private long creationTime;
    private long lastActiveTime;
    private String userName;
    private transient UserWebImpl user;
    public SessionInfo( int userId, String userName,String sessionId, long time, String hostIP, UserWebImpl user) {
        this.userName=userName;
        this.userId=userId;
        this.hostIP=hostIP;
        this.sessionId=sessionId;
        this.creationTime=time;
        lastActiveTime=time;
        this.user=user;
    }
    public void setLastActiveTime(long time){
        lastActiveTime=time;
    }
    public String getUserName(){
        return userName;
    }
    public int getUserId(){
        return userId;
    }
    public String getHostIP(){
        return hostIP;
    }
    public String getSessionId(){
        return sessionId;
    }
    public long getCreationTime(){
        return creationTime;
    }
    public long getLastActiveTime(){
        return lastActiveTime;
    }
    public SessionInfo duplicate(){
        SessionInfo si=new SessionInfo(userId, userName, sessionId, creationTime, hostIP,null);
        si.setLastActiveTime(lastActiveTime);
        return si;
    }
    public void copy(SessionInfo si){
        this.creationTime= si.creationTime;
        this.lastActiveTime= si.lastActiveTime;
        this.hostIP = si.hostIP;
        this.sessionId=si.sessionId;
        this.userId= si.userId;
        this.userName= si.userName;
    }
    public UserWebImpl getUserWebImpl(){
    	return user;
    }
    public String toString(){
        return userName;
    }


}
