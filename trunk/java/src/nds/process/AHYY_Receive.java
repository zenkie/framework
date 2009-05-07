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
 * 通过 固定账户 向经销商账户的支付，必须是已经完成扣款的医院的付款单才能实现向经销商账户的支付
 * 即:
 * select * from b_pay where state_kou='Y' and state_fu in ('N','P') 
 *  
 *  对于支付成功的行，将修改state_fu='P', 并为 收款方自动创建 已收款单 
 *  
 */
public class AHYY_Receive extends SvrProcess
{
	
	/**
	 *  Parameters:
	 *    no 
	 */
	protected void prepare()
	{
		
		/*ProcessInfoParameter[] para = getParameters();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
		}*/
	}	//	prepare	
	


	/**
	 *  Perrform process.
	 *  @return Message that would be set to process infor summary (no use currently)
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception{

		/**
		 * 通过 固定账户 向经销商账户的支付，必须是已经完成扣款的医院的付款单才能实现向经销商账户的支付
		 * 即:
		 * select * from b_pay where state_kou='Y' and state_fu in ('N','P') and status=2
		 *  
		 *  对于支付成功的行，将修改state_fu='P', 并为 收款方自动创建 已收款单 
		 *  “yz--yymmdd.txt”上传，“yymmdd--ahyl.xls”下载
		 * 
2|0.02
117010152500003115|是|117010152500002797|兴业银行网上银行中心|兴业银行||0.01|转账||
117010152500003115|是|117010152500002797|兴业银行网上银行中心|兴业银行||0.01|转账||

第一行：笔数|金额
第二行开始：付款账号|是否兴业银行|收款账号|收款单位名称|收款银行|汇入地点|金额|用途|备注|（大小额标志、填空即可）|收款行号|

 */ 
	
		Connection conn= null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		QueryEngine engine= QueryEngine.getInstance();
		conn= engine.getConnection();

	    Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
	    String folder= conf.getProperty("ahyy.payment.folder.upload", "e:/act/ahyy/upload");
	    File fd= new File(folder);
	    if(!fd.exists()) fd.mkdirs();
	    
    	SimpleDateFormat df=new SimpleDateFormat("yyMMdd");
	    String fileWithoutFolder="yz--"+ df.format(new java.util.Date())+".txt";
	    String file=folder+"/"+fileWithoutFolder;
		
		try{
			
	        FileOutputStream fos=new FileOutputStream(new File(file));
	        OutputStreamWriter w= new OutputStreamWriter(fos, "UTF-8");
			String lineSep="\r\n";

			String s=(String) engine.doQueryOne("select trim(to_char(count(*))) || '|' || "+ 
					"trim(to_char( sum(TOT_AMT_ACTUAL) )) from b_pay where state_kou='Y' and state_fu in ('N','P') and status=2",conn);
			w.write(s);
			w.write(lineSep);
			
			String accountNo=conf.getProperty("ahyy.payment.account.no");
			String isXinYe="Y".equals(conf.getProperty("ahyy.payment.account.xinye"))?"是":"否";//trim(to_char( amt*100 ,'000000000009'))
			String sql="SELECT '"+accountNo+"|"+ isXinYe+
			"|'|| BANKACCOUNT ||'|' || BANKOWNER || '|' || BANKNAME || '||' || (trim(to_char(TOT_AMT_ACTUAL,'99999999999990.99')))  ||'|转帐|'|| docno||'|||' from b_pay b, c_bpartner c where b.state_kou='Y' and b.state_fu in ('N','P') and b.status=2 and c.id=b.c_bpartner2_id";
			pstmt=conn.prepareStatement(sql);
			rs= pstmt.executeQuery();
			int lineCount=0;
			while(rs.next()){
				w.write( rs.getString(1));
				w.write(lineSep);
				lineCount++;
			}
			w.flush();
			w.close();
			fos.close();
			
			// 设置单据为扣款中
			conn.createStatement().executeUpdate("update b_pay set state_fu='R', msg_fu=null where state_kou='Y' and state_fu in ('N','P') and status=2");

			CommandExecuter cmd= new CommandExecuter(folder+"/log/"+fileWithoutFolder+".log");
			String exec=conf.getProperty("ahyy.payment.upload", "cmd /c e:\\act\\bin\\upload.cmd");
			int err=cmd.run(exec +" "+ file);
			
			this.addLog("共有 "+ (lineCount)+ " 行, 上传命令返回码:"+ err);
			return "完成";
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}			
		
		
	}
}
