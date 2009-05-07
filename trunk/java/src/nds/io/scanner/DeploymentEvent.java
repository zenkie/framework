package nds.io.scanner;

import java.io.File;
import java.net.URL;
import java.util.Date;

public class DeploymentEvent extends java.util.EventObject{

   /**
    * file created and deployed
    */
   public static int DEPLOYMENT_DEPLOYED = 1;
   /**
    * file updated
    */
   public static int DEPLOYMENT_MODIFIED = 2;
   /**
    * file removed
    */
   public static int DEPLOYMENT_REMOVED = 3;

   private URL url;
   private long deployedLastModified;
   private boolean isModified;
   private boolean isRemoved;
   private int type;
   /**
    */
   public DeploymentEvent(DeployedURL du )
   {
       super(du);
       url=du.url;
       deployedLastModified=du.getLastModified();
       isModified= du.isModified();
       isRemoved= du.isRemoved();
       type= isRemoved?DEPLOYMENT_REMOVED: (isModified?DEPLOYMENT_MODIFIED:DEPLOYMENT_DEPLOYED);
   }
   public static String getTypeDescription(int t){
       return t==DEPLOYMENT_DEPLOYED?"deployed": (t==DEPLOYMENT_MODIFIED?"modified":"removed");
   }
   /**
    * @return DEPLOYMENT_DEPLOYED,DEPLOYMENT_MODIFIED,DEPLOYMENT_REMOVED
    */
   public int getType(){
       return type;
   }
   public boolean isFile()
   {
      return url.getProtocol().equals("file");
   }

   public File getFile()
   {
      return new File(url.getFile());
   }

   public boolean isRemoved()
   {
       return this.isRemoved;
   }

   public long getLastModified()
   {
      return this.deployedLastModified;
   }

   public boolean isModified()
   {
      return isModified;
   }

   public int hashCode()
   {
      return url.hashCode();
   }
   public String toString(){
       return url+ ",status="+ getTypeDescription(type)+ ",date="+ (new Date(this.deployedLastModified));
   }

}