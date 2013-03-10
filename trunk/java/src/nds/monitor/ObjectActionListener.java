package nds.monitor;

public abstract interface ObjectActionListener
{
  public abstract void init(int paramInt)
    throws Exception;
  
  public abstract void setDebug(boolean debug);
  
  public abstract int getId();

  public abstract int getTableId();

  public abstract int getAdClientId();

  public abstract void onAction(ObjectActionEvent paramObjectActionEvent)
    throws Exception;
}