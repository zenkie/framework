package nds.net;

import java.text.SimpleDateFormat;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Sequences;

public abstract class AbstractSessionListener implements SessionListener {
    protected Logger logger= LoggerManager.getInstance().getLogger(this.getClass().getName());
    private final static SimpleDateFormat expFileNameFormatter
            =new SimpleDateFormat("yyyyMMddHHmmss");
    private final static SimpleDateFormat dateFormatter
            =new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    protected SessionController controller;
    protected String id;

    public AbstractSessionListener() {
        id= getType() + "_" + Sequences.getNextID(getType());
    }
    public String getID(){
        return id;
    }
    public void setController( SessionController controller){
        this.controller= controller;
    }
    public String getType(){
        String shortName=this.getClass().getName();
        shortName=shortName.substring( shortName.lastIndexOf(".")+1);
        return shortName;
    }
    public void kill(){
        controller.removeListener(this);
    }
    /**
     * Start listener on SessionController, normal steps will include registering
     * in SessionController
     */
    public void start(){
        controller.addListener(this);
    }
    /**
     * Sednd msg to web, call program name posinfo.py
     */
    protected void notifyClientStatus(String jbUser, String name, String value){
        try{
            String tmpDir =controller.getAttribute("PosDB.TmpDir", "f:/act/posdb/tmp");
            String cmdRootPath= controller.getAttribute("cmd.root", "/");

            String expFileName= expFileNameFormatter.format(new java.util.Date())+".pylog";
            String outputFileName=tmpDir +"/" + expFileName +".out";
            //CommandExecuter exec= new CommandExecuter(outputFileName);
            String args="command=jabber_msg user="+ jbUser+" "+name+"="+ value;
            String cmd= "sh "+ cmdRootPath+ "/posinfo " +args ;//+" >>"+expFileName;
            Runtime.getRuntime().exec(cmd);
//            exec.run(cmd);
            logger.debug("Result in executing " +cmd);//+":" + Tools.getFileContent(outputFileName ));
        }catch(Exception e){
            logger.error("Could notify client status for "+ jbUser+","+name+"="+value+":", e);
        }
    }

}