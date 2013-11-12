package nds.schema;

import java.util.List;
import nds.util.Tools;

public class Client
{
  private int id;
  private String domain;
  private String name;
  private String description;
  private String logoURL;

  Client(List paramList)
  {
    this.id = Tools.getInt(paramList.get(0), -1);
    this.domain = ((String)paramList.get(1));
    this.name = ((String)paramList.get(2));
    this.description = ((String)paramList.get(3));
    this.logoURL = ((String)paramList.get(4));
  }

  public int getId() {
    return this.id;
  }

  public String getDomain()
  {
    return this.domain;
  }

  public String getName()
  {
    return this.name;
  }

  public String getDescription()
  {
    return this.description;
  }

  public String getLogoURL()
  {
    return this.logoURL;
  }
}