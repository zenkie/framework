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
 * 实现向银联接口的扣款申请，见 《附件4：公共支付系统商户接口V1.0.doc》7.7.2、	批量代收类信息文件
 * 
 * 批量代扣类信息文件为文本格式，一条记录占一行，第一条记录为总计记录，后跟代扣款信息记录等。
 * 
 * 具体实现
 * 准备数据：
 * 	将所有已提交，但未扣款的“已付款” 单据按付款方合并生成 B_PAY_SUM 单据，单据格式：
 * 		单据编号，付款医院，总金额
 * 	B_PAY_SUM 信息回写到所有B_PAY 中
 * 
 *  对于扣款成功的记录，所有的B_PAY 也都被设置上扣款成功，并且设置为未支付到供应商。然后有付款程序完成向供应商的付款
 *  对于扣款不成功的记录，所有的B_PAY也都被设置上扣款不成功，下次再付
 *  B_PAY_SUM 用于记录不在前端显示
 *  
 */
public class AHYY_Payment extends SvrProcess
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
		// 	load query into cache file directly
		//int userId= this.getAD_User_ID();
		//User user= SecurityUtils.getUser(userId); 
		
		Connection conn= null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		QueryEngine engine= QueryEngine.getInstance();
		conn= engine.getConnection();

	    Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
	    String folder= conf.getProperty("ahyy.payment.folder.upload", "e:/act/ahyy/upload");
	    File fd= new File(folder);
	    if(!fd.exists()) fd.mkdirs();
	    ahyyCode= conf.getProperty("ahyy.payment.code");
	    if(ahyyCode==null || ahyyCode.length()!=8) throw new NDSException("Wrong code for bank payment interface");
	    
    	SimpleDateFormat df=new SimpleDateFormat("yyMMdd");
	    String fileWithoutFolder="MERFEE"+ ahyyCode+ df.format(new java.util.Date());
	    String file=folder+"/"+fileWithoutFolder;
		
		try{
			/**
			 * 准备付款总单
			 */
			int cnt=0;
			
			nds.query.SPResult sr=engine.executeStoredProcedure("b_pay_sum_generate", new ArrayList(),true, conn);
			cnt=sr.getCode(); 
			if (cnt>0){
			
		        FileOutputStream fos=new FileOutputStream(new File(file));
		        OutputStreamWriter w= new OutputStreamWriter(fos, "UTF-8");
				String lineSep="\n";
				//总计记录数
		/*
		?	代扣费信息总计记录格式
		序号	数据域	长度	描述
		1	记录标识	3	必填“FEE”
		2	商户平台机构代码	11	左靠，右补空格；必须与文件名的商户标志码相同
		3	文件生成日期	4	必须与文件名的日期相同
		4	总计笔数	10	右靠，左补0；如，笔数为34条记录，则为“0000000034”。
		5	应缴金额总数	16	右靠，左补0；不带小数点；如，金额为3489.45元，则为“0000000000348945”。
		6	回车符	1	0x00或0x0A
		 */		
				// 从未扣款,或扣款失败的扣款汇总单，汇总单每天只允许生成一次
				String s=(String) engine.doQueryOne("select 'FEE"+ formatSpace(this.ahyyCode,11)+"' || to_char(sysdate,'YYMMDD') || "+
						"trim(to_char(count(*),'0000000009')) || "+ 
						"trim(to_char( sum(amt)*100 ,'0000000000000009')) from b_pay_sum where state in ('N','P')",conn);
				w.write(s);
				w.write(lineSep);
	/*
	1	帐单类型	2	01：手机话费；02：固话费；03：水费；04：电费；05：煤气；06：社保；07：小灵通话费；08：信用卡还款，等。
	2	帐单号码	20	左靠，右补空格 (付款方银行卡卡号，假设卡号为12位）
	3	个人缴费金额	12	右靠，左补0；不带小数点；如，金额为124.20元，则为“000000012420”。
	4	附加数据域长度	3	000：表示没有附加数据域
		实际长度：表示有附加数据域；右靠，左补0；如，021为21的位附加数据域
	5	附加数据域	200	
	6	回车符	1	0x00或0x0A
	 */			 
				// RE0807260001 12位，加8个0 ，帐单号码
				pstmt=conn.prepareStatement("SELECT '"+BILL_TYPE+
						"'|| billno || trim(to_char( amt*100 ,'000000000009'))||'000'  from b_pay_sum where state in ('N','P')");
				rs= pstmt.executeQuery();
				while(rs.next()){
					w.write( rs.getString(1));
					w.write(lineSep);
					cnt++;
				}
				w.close();
				
				// 设置总单为扣款中
				conn.createStatement().executeUpdate("update b_pay_sum set state='R' where state in ('N','P')");
	
				CommandExecuter cmd= new CommandExecuter(folder+"/log/"+fileWithoutFolder+".log");
				String exec=conf.getProperty("ahyy.payment.upload", "e:/act/bin/upload.cmd");
				int err=cmd.run(exec +" "+ file);
			}

			this.addLog("共有 "+ (cnt)+ " 行");
			return "完成";
		}finally{
			if(rs!=null)try{rs.close();}catch(Throwable t){}
			if(pstmt!=null)try{pstmt.close();}catch(Throwable t){}
			if(conn!=null)try{conn.close();}catch(Throwable t){}
		}			
		
		
	}
	private final static String ZERO="0000000000000000000000000000000000000000";
	private final static String SPACE="                                       ";
	/***
	 * 右补空格，总长度达到len 要求
	 * @param s
	 * @param len
	 * @return
	 * @throws NDSException
	 */
	private String formatSpace(String s, int len)throws NDSException{
		if(s==null) return SPACE.substring(0, len); 
		int l= s.length();
		if(l>len) throw new NDSException(s+" is too long (max="+ len+")");
		if( l==len ) return s;
		return s+SPACE.substring(0, len-l);
	}
	/**
	 * 如果 s 的长度< len，将通过设定0来补足
	 * @param s
	 * @param len 不得大约40
	 * @return
	 */
	private String format(String s, int len) throws NDSException{
		if(s==null) return ZERO.substring(0, len); 
		int l= s.length();
		if(l>len) throw new NDSException(s+" is too long (max="+ len+")");
		if( l==len ) return s;
		return ZERO.substring(0, len-l)+s;
	}
}
