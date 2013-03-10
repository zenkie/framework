package nds.monitor;

import nds.query.SPResult;

public abstract interface MonitorTrigger
{
  public abstract SPResult onAction(ObjectActionEvent paramObjectActionEvent)
    throws Exception;
}