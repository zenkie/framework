package nds.io.scanner;

import java.util.EventListener;

   /** listener that should be implemented to listen for any connection events */
public interface DeploymentListener extends EventListener
   {

       /**
        */
       public void urlDeployed(DeploymentEvent det);

       /**
        */
       public void urlModified(DeploymentEvent de);

       public void urlRemoved(DeploymentEvent de);
   }
