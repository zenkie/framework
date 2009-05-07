/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.process;

import nds.control.util.SecurityUtils;
import nds.control.web.WebUtils;
import nds.query.QueryEngine;
import nds.query.QueryUtils;
import nds.security.User;
import nds.util.*;
import nds.schema.*;
import java.text.*;
import java.sql.*;
import java.util.*;
import java.io.*;
/**
 * 实现向银联接口的扣款回复，见 《附件4：公共支付系统商户接口V1.0.doc》7.7.2、	批量代收类信息文件
 * 
 *  
 */
public class AHYY_PaymentRet extends SvrProcess
{
	/**
	 * ?	代扣费信息记录帐单类型
	 */
	private final static String BILL_TYPE="00";
	/**
	 * ?	商户标志码（8位，见商户基本信息表）
	 */
	private String ahyyCode;
	/**
	 *  Parameters:
	 *    no 
	 */
	protected void prepare()
	{
	}		
	

	/**
	 *  Perrform process.
	 *  @return Message that would be set to process infor summary (no use currently)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception{
		// 	load query into cache file directly
		//int userId= this.getAD_User_ID();
		//User user= SecurityUtils.getUser(userId); 
		
		Connection conn= null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		QueryEngine engine= QueryEngine.getInstance();
		conn= engine.getConnection();

	    Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
	    String folder= conf.getProperty("ahyy.payment.folder.download", "e:/act/ahyy/download");
	    File fd= new File(folder);
	    if(!fd.exists()) fd.mkdirs();
	    ahyyCode= conf.getProperty("ahyy.payment.code");
	    if(ahyyCode==null || ahyyCode.length()!=8) throw new NDSException("Wrong code for bank payment interface");
	    
    	SimpleDateFormat df=new SimpleDateFormat("yyMMdd");
	    String ftpFile="MPAYREP"+ ahyyCode+ df.format(new java.util.Date());
	    String file=folder+"/"+ftpFile;
		StringBuffer sb=new StringBuffer();
		String lineSep =Tools.LINE_SEPARATOR;
		try{
			//下载文件
			CommandExecuter cmd= new CommandExecuter(folder+"/log/"+ftpFile+".log");
			String exec=conf.getProperty("ahyy.payment.download", "e:/act/bin/download.cmd");
			int err=cmd.run(exec +" "+ ftpFile);
			if(err!=0){
				throw new NDSException("Error(code="+ err+") when doing "+exec +" "+ ftpFile);
			}
			if(!(new File(file)).exists()){
				throw new NDSException("File not downloaded when doing "+exec +" "+ ftpFile);
			}
			BufferedReader in
			   = new BufferedReader(new FileReader(file));
			in.readLine(); // skip first line, which is summary
			String line= in.readLine();
			pstmt=conn.prepareStatement("update b_pay_sum set state=?, err_code =? where billno=? and state='R'");
			while(line!=null){
				log.debug(line);
				if(line.length()<34){
					log.debug("line not has length: 34");
				}
				
				/* 
				String billType= line.substring(0,2);
				String billNo= line.substring(0, 22);// billno
				String amt=line.substring(22,34);
				String ack=line.substring(34,36);*/
				String billNo= line.substring(0, 20);// billno
				String amt=line.substring(20,32);
				String ack=line.substring(32,34);
				if("00".equals(ack)){
					java.math.BigDecimal bd=new java.math.BigDecimal(amt);
					pstmt.setString(1,"Y");
					pstmt.setNull(2, java.sql.Types.VARCHAR);
					pstmt.setString(3, billNo);
					int ret= pstmt.executeUpdate();
					if(ret!=1){
						sb.append(line+"(updated "+ ret+" lines)").append(lineSep);
					}
				}else{
					pstmt.setString(1,"P");
					pstmt.setString(2, ack);
					pstmt.setString(3, billNo);
					int ret= pstmt.executeUpdate();
					if(ret!=1){
						sb.append(line+"(updated "+ ret+" lines)").append(lineSep);
					}
				}
				line= in.readLine();
			}
			in.close();
			try{
				log.debug("Move "+ file +" to "+ folder+"/log/"+ftpFile);
			(new File(file)).renameTo(new File(folder+"/log/"+ftpFile));
			}catch(Throwable t){
				log.error("Fail to move "+ file +" to "+ folder+"/log/"+ftpFile, t);
			}
			Vector vet=new Vector();
			String log=sb.toString();
			this.log.debug(log);
			this.addLog(log);
			return "完成";
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}			
		
		
	}
	
}
