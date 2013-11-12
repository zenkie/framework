package nds.schema;

import java.util.Hashtable;
import java.util.List;
import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.query.QueryException;
import nds.util.NDSRuntimeException;
import nds.util.ObjectNotFoundException;
import nds.util.Tools;
import nds.web.shell.ShellCmd;

public class ClientManager
{
  private static ClientManager instance = null;
  private Hashtable<Integer, Client> clientIds;
  private Hashtable<String, Client> clientDomains;
  private Hashtable<String, Client> clientNames;
  private int defaultClientId;
  private boolean isSingleClient;

  private ClientManager()
  {
    this.clientIds = new Hashtable<Integer, Client>();
    this.clientDomains = new Hashtable<String,Client>();
    this.clientNames = new Hashtable<String,Client>();
    this.defaultClientId = Tools.getInt(WebUtils.getProperty("client.default.clientid"), 37);
    try {
      List localList = QueryEngine.getInstance().doQueryList("select id,domain,name,description,logourl from ad_client");

      Client localclient = null;
      for (int i = 0; i < localList.size(); i++) {
    	  localclient = new Client((List)localList.get(i));  
    	  this.clientIds.put(Integer.valueOf(localclient.getId()), localclient);
    	  this.clientDomains.put(localclient.getDomain(), localclient); 
    	  this.clientNames.put(localclient.getName(), localclient);
      }

      if (localList.size() == 1) {
        this.defaultClientId = localclient.getId();
        this.isSingleClient = true;
      }return;
    } catch (Throwable localThrowable) {
      throw new NDSRuntimeException("Fail to load clients", localThrowable);
    }
  }

  public boolean isSingleClient()
  {
    return this.isSingleClient;
  }

  public Client getClient(int id)
    throws ObjectNotFoundException, QueryException
  {
	  Client cl=(Client)this.clientIds.get(Integer.valueOf(id));
	  if(cl==null)
    {
      throw new ObjectNotFoundException("client id=" + id + " not found");
    }
    return cl;
  }

  public Client getClientByDomain(String domin)
    throws ObjectNotFoundException, QueryException
  {
	  Client cl=(Client)this.clientIds.get(domin);
	  if(cl==null)
    {
      throw new ObjectNotFoundException("client domain=" + domin + " not found");
    }
    return cl;
  }

  public Client getClientByName(String name)
    throws ObjectNotFoundException, QueryException
  {
	  Client cl=(Client)this.clientIds.get(name);
	  if(cl==null)
    {
      throw new ObjectNotFoundException("client name=" + name + " not found");
    }
    return cl;
  }
  public int getDefaultClientId() {
    return this.defaultClientId;
  }
  public Client getDefaultClient() {
    try {
      return getClient(this.defaultClientId); } catch (Throwable localThrowable) {
    }
    throw new NDSRuntimeException("default client not found");
  }

  public void reload(int id)
  {
	  Client cl=null;
    if ((
    	 cl = (Client)this.clientIds.remove(Integer.valueOf(id))) != null)
    {
      this.clientDomains.remove(cl.getDomain());
      this.clientNames.remove(cl.getDomain());
    }
  }

  public static ClientManager getInstance() {
    if (instance == null) {
    	instance = new ClientManager();
    }
    return instance;
  }
}