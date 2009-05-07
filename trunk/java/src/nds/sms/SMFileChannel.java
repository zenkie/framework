//Source file: F:\\work\\sms\\src\\nds\\sms\\SMFileChannel.java

package nds.sms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nds.connection.ConnectionFailedException;
import nds.connection.Message;
import nds.connection.MessageBundle;
import nds.io.scanner.ExtensionFilter;
import nds.util.NDSException;
import nds.util.Sequences;
import nds.util.Tools;
/**
 * Will supervise on the directory of recieved sms file, scan at interval, if files found,
 * will construct the sms object and move the file to backup directory
 *
 * The file format will be
# Receiving
# -----------------------------------------------------------------------------
# The filename will be
#
# IN<date>_<time>_<serialno>_<phone number>_<sequence>.txt
#
# eg. IN20021130_021531_00_+45409000931640979_00.txt
# where
# <date> is yyyymmdd
# <time> is hhmmss
# <serialno> is nn (in case of more SMSes sent at the same time to the phone)
# <sequence> is nn (for multi-SMSes)
#
# In case of 8 bit SMS coding the SMS is stored as binary data with the file
# extension .bin
Pattern is "^IN(\\d{8})_(\\d{6})_(\\d{2})_[+-]??(\\d{1,})_(\\d{2}).txt"
File content contains receiver code and the command , the receiver and command
are seperated by delimiter specified by "sms.file.delimiter", default to space char
if delimiter not found, whole body will be taken as receiver code.
#
# Transmitting
# -----------------------------------------------------------------------------
# The filename should be one of the following formats
#
# OUT<phone number>.txt
# OUT<priority>_<phone number>_<serialno>.txt
# OUT<priority><date>_<time>_<serialno>_<phone number>_<anything>.txt
# where
# <priority> is an alphabetic character (A-Z) A = highest priority
# Other fields as above.
# SMSes will be transmitted sequentially based on the file name
#
# The contents of the file is the SMS to be transmitted (in Unicode or standard
# character set)
#

 *
 */
public class SMFileChannel extends ConnectionChannel
{
   private File inbox;
   private Pattern inboxFileNamePattern= Pattern.compile("^IN(\\d{8})_(\\d{6})_(\\d{2})_[+-]??(\\d{1,})_(\\d{2}).txt");
   public final static DateFormat dateTimeFormatter =new SimpleDateFormat("yyyyMMdd HHmmss");
   public final static DateFormat dateFormatter =new SimpleDateFormat("yyyyMMdd");
   public final static DateFormat timeFormatter =new SimpleDateFormat("HHmmss");
   
   /**
    *
    */
   public SMFileChannel()
   {
   }

   /**
    * Periodically this method will be called
    * @return MessageBundle
    */
   protected synchronized MessageBundle readFromChannel()
   {
       MessageBundle mb=new MessageBundle();
       File[] files=inbox.listFiles(new ExtensionFilter(props.getProperty("sms.file.ext",".txt")));
       for (int i=0;i<files.length;i++){
           try {
       			Message msg=createMessage(files[i]);
       			mb.addMessage(msg);
       			logger.debug("created message from file "+  files[i].getAbsolutePath());
           }
           catch (Throwable ex) {
               logger.error("Could not load message from file "+ files[i].getAbsolutePath(), ex);
           }
           moveFile(files[i]);
       }
       return mb;
   }
   /**
    * Attempts to rename a file from a source to a destination.
    * If overwrite is set to true, this method overwrites existing file
    * even if the destination file is newer.  Otherwise, the source file is
    * renamed only if the destination file is older than it.
    * Method then checks if token filtering is used.  If it is, this method
    * returns false assuming it is the responsibility to the copyFile method.
    *
    * @throws IOException
    */
   protected boolean renameFile(File sourceFile, File destFile,boolean overwrite)
       throws IOException {

       boolean renamed = true;
       // ensure that parent dir of dest file exists!
       // not using getParentFile method to stay 1.1 compat
       String parentPath = destFile.getParent();
       if (parentPath != null) {
           File parent = new File(parentPath);
           if (!parent.exists()) {
               parent.mkdirs();
           }
       }

       if (destFile.exists() && destFile.isFile()) {
           if (!destFile.delete()) {
               throw new IOException("Unable to remove existing "
                                        + "file " + destFile);
           }
       }
       renamed = sourceFile.renameTo(destFile);
       return renamed;
    }
   /**
    * Move file to backup directory specified by property "sms.dir.bak", value not end with "/"
    * such as "f:/sms/bak"
    */
   private void moveFile(File file){
       try{
           File dest= new File( props.getProperty("sms.dir.bak", "./bak")+ "/"+ file.getName());
           renameFile(file, dest, true);
       }catch(Exception e){
           logger.error(e.getMessage(), e );
       }
   }
   /**
    * create message according to file content
# The filename will be
#
# IN<date>_<time>_<serialno>_<phone number>_<sequence>.txt
#
# eg. IN20021130_021531_00_+45409000931640979_00.txt
# where
# <date> is yyyymmdd
# <time> is hhmmss
# <serialno> is nn (in case of more SMSes sent at the same time to the phone)
# <sequence> is nn (for multi-SMSes)
#
# In case of 8 bit SMS coding the SMS is stored as binary data with the file
# extension .
Pattern is "^IN(\\d{8})_(\\d{6})_(\\d{2})_[+-]??(\\d{1,})_(\\d{2}).txt"

File content should contain receiver and boby information, seperated by property "sms.file.delimiter"
if seperator not found, will set receiver to empty (the default memory handler whose user code is -1);
    *
    */
   private Message createMessage(File file) throws Exception {
       String fileName= file.getName();
       Matcher m=inboxFileNamePattern.matcher(fileName);
       if (!m.matches() || m.groupCount() != 5){
           throw new NDSException("File name not valid for income message:"+ file.getAbsolutePath());
       }
       ShortMessage sm=new ShortMessage();
       String d= m.group(1)+ " " + m.group(2);
       Date date= dateTimeFormatter.parse(d);
       sm.setCreationDate(date); //creation date
       sm.setSender(m.group(4)); // sender
       sm.setStatus(sm.PROCESSING); // status
       sm.setProperty("file", fileName); // file

       String body= Tools.readFile(file.getAbsolutePath());
       String receiver,content, seperator;
       seperator=props.getProperty("sms.file.delimiter"," ");
       if(seperator.length()==0) seperator=" "; // in property file, space char will be cut off
       // body contains receiver and command, seperated by first "sms.file.delimiter"
       int idx= body.indexOf(seperator);
       if ( idx > -1){
           receiver= body.substring(0, idx);
           content= body.substring(idx+1);
       }else{
           receiver=""; // whose user code is -1
           content=body;
       }

       sm.setContent(content); // content
       sm.setReceiver(receiver);


       return sm;
   }
   /**
    * 
    * @param p if numeric, 1 and lower for Z and 26 and higher for A, if string, get first char to judge 
    * @return is an alphabetic character (A-Z) A = highest priority
    */
   private String translatePriority(String p){
 	  String d="Z"; //default;
   	  if (p==null|| p.length()<1) return d;
   	  int i=Tools.getInt(p, -1);
   	  if(i==-1){
   	  	// not int
   	  	char c=p.charAt(0);
   	  	if (c<'Z' && c>='A') d= String.valueOf(c);
   	  }else{
   	  	if(i>1 && i<26 ) d= String.valueOf((char)('A'+i) );
   	  	if(i>26) d="A"; 
   	  }
   	  return d;
   }
   /**
    * Write to directory specified by "sms.dir.outbox"
    *  The filename should be one of the following formats
#
# OUT<priority><date>_<time>_<serialno>_<phone number>_<anything>.txt

# SMSes will be transmitted sequentially based on the file name
#
# The contents of the file is the SMS to be transmitted (in Unicode or standard
# character set)
    */
   protected  void write(Message msg){
       try{
       //@todo split content to multiple file when sending

       String fileName=getNextFileName(props.getProperty("sms.dir.outbox", "./outbox")+ "/OUT"+
       		translatePriority(msg.getProperty("priority",""))+
                       dateFormatter.format(new Date())+"_"+ timeFormatter.format(new Date())+"_01_"+
                       msg.getReceiver()) ;
       FileOutputStream fos=new FileOutputStream( fileName);
       StringBuffer sb=new StringBuffer(msg.getContent());
       // append my sender code
       sb.append("["+ msg.getSender()+"]");
       fos.write(sb.toString().getBytes());
       fos.close();
       }catch(Exception e){
           // change status to DISCARD
           logger.error("Could not write msg to disk", e);
           msg.setStatus(Message.DISCARD);
           msg.setProperty("info", "Could not write msg to disk:"+e.getMessage());
           return;
       }
       // change status to OK
       msg.setStatus(Message.SENT);
   }
   /**
    * Append distinct marker on <param> namePart</param> to make file name unique
    * in the directory

    * @param namePart in format like
    * OUT<priority><date>_<time>_<serialno>_<phone number>
    *
    * @return file name in format like
    * directory+"/OUT<priority><date>_<time>_<serialno>_<phone number>_<anything>.txt
    */
   private String getNextFileName( String namePart){
       int c= 0;
       String name;
       File f;
       while( true){

           name= namePart+ "_"+ c+".txt";
           f=new File( name);
           if ( f.exists()){
               c ++;
           }else break;
       }
       return name;
   }
   /**
    * Will be called before open channel reader during #connect
    * you can override this to establish connection here.
    */
   protected void setupConnection() throws ConnectionFailedException{
       //logger.debug("setupConnection");
       inbox = new File(props.getProperty("sms.dir.inbox", "./inbox"));
       if (!inbox.isDirectory()) throw new ConnectionFailedException(
               "sms.dir.inbox " + props.getProperty("sms.dir.inbox", "./inbox")+ " is not a directory.");


   }
   /**
    * Will be called after channel reader stopped
    * You can override this to logout or close folder watcher here.
    */
   protected void tearDownConnection(){
       logger.debug("tearDownConnection");
   }
}
