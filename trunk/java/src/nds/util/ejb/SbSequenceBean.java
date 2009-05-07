/******************************************************************
*
*$RCSfile: SbSequenceBean.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: SbSequenceBean.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.4  2001/11/12 14:39:06  yfzhu
*no message
*
*Revision 1.3  2001/11/08 15:10:51  yfzhu
*First time compile OK
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/
package nds.util.ejb;
import java.rmi.RemoteException;

public  class SbSequenceBean implements  javax.ejb.SessionBean {

  private boolean doLog=true;
  private java.util.Hashtable _entries = new java.util.Hashtable();
  private int _blockSize;
  private int _retryCount;
  private SequenceRemoteHome _sequenceHome;
private void debug(String msg){
    if(doLog) System.out.println("[SbSequenceBean]"+msg);
}
public int getNextNumberInSequence(String name) throws RemoteException
{
    name= name.toLowerCase();
    try
    {
        Entry entry = (Entry) _entries.get(name);

        if (entry == null)
        {
            // add an entry to the sequence table
            entry = new Entry();
            try
            {
                entry.sequence = _sequenceHome.findByPrimaryKey(name);
            }
            catch (javax.ejb.FinderException e)
            {
                // if we couldn't find it, then create it...
                entry.sequence = _sequenceHome.create(name);
            }
            _entries.put(name, entry);
        }
        debug(" sequence for "+ name+ " is "+ entry.last+", with blockSize="+ _blockSize);
        if (entry.last % _blockSize == 0)
        {
            for (int retry = 0; true; retry++)
            {
                try
                {
                    debug(" trying to get sequence for "+ name+" with retry time:"+ retry);
                    entry.last = entry.sequence.getNextKeyAfterIncrementingBy(_blockSize);
                    debug(" get sequence for "+ name+" with retry time:"+ retry+" is:"+ entry.last);
                    break;
                }
                catch (javax.ejb.TransactionRolledbackLocalException e)
                {
                    if (retry < _retryCount)
                    {
                        // we hit a concurrency exception, so try again...
                        continue;
                    }
                    else
                    {
                        System.out.println("[sbSequenceBean] error  ");
 	                     // we tried too many times, so fail...
                        throw new javax.ejb.EJBException(e);
                    }
                }
            }
        }
        return entry.last ++ ;
    }
    catch (javax.ejb.CreateException e)
    {
        throw new javax.ejb.EJBException(e);
    }
}
  public void setSessionContext( javax.ejb.SessionContext sessionContext) {
    try {
      javax.naming.Context namingContext = new javax.naming.InitialContext();
      _blockSize = ((Integer) namingContext.lookup("java:comp/env/blockSize")).intValue();
      _retryCount = ((Integer) namingContext.lookup("java:comp/env/retryCount")).intValue();

      _sequenceHome = (SequenceRemoteHome) namingContext.lookup("nds/ejb/Sequence");
    }
    catch(javax.naming.NamingException e) {
      throw new javax.ejb.EJBException(e);
    }
  }
  public void ejbActivate() {}
  public void ejbCreate() {}
  public void ejbPassivate() {}
  public void ejbRemove() {}



}
  class Entry {
    SequenceRemote sequence;
    int last;
  };
