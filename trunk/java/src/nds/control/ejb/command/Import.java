package nds.control.ejb.command;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.*;

import nds.control.ejb.Command;
import nds.control.ejb.DefaultWebEventHelper;
import nds.control.ejb.MySQLObjectCreateImpl;
import nds.control.ejb.ObjectCreateImpl;
import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEventException;
import nds.control.util.ValueHolder;
import nds.mail.NotificationManager;
import nds.query.*;
import nds.schema.Column;
import nds.schema.RefByTable;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.NDSException;
import nds.util.PairTable;
import nds.util.Tools;
import nds.util.Validator;

/**
 * batch object creation for REST
 */
public class Import extends Command{
	/**
     * Whether this command use internal transaction control. For normal command, transaction is controled by
     * caller, yet for some special ones, the command will control transaction seperativly, that is, the command
     * will new transaction and commit that one explicitly
     * @return false if use transaction from caller
     */
    public boolean internalTransaction(){
    	return true;
    }	
  /**
   * Redirect to object creation command with batch mode set 
   */	
  public ValueHolder execute(DefaultWebEvent event) throws NDSException ,RemoteException{
  	  event.setParameter("command", "ObjectCreate");
  	  event.setParameter("best_effort", "true");
  	  //event.setParameter("nds.control.ejb.UserTransaction", "N");// let create do its own transaction
  	  event.setParameter("output_json","y");
  	  return helper.handleEvent(event);
  }
 
}

