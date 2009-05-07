/******************************************************************
*
*$RCSfile: CommandMapping.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:14 $
*
*$Log: CommandMapping.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:14  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:34  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:46  yfzhu
*no message
*
*
********************************************************************/
package nds.control.ejb;

  /**
  * This class is for generating CommandMapping from a xml file
  *
  */
public class CommandMapping{


    private String command=null;
    private String entity=null;

    public CommandMapping(){
    }
    public void setCommand(String command){
        this.command=command;
    }
    public void setEntity(String entity){
        this.entity=entity;
    }
    public String getCommand(){
        return command;
    }
    public String getEntity(){
        return entity;
    }

}
