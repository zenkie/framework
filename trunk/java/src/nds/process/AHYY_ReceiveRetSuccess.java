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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
/**
 * 通过 固定账户 向经销商账户的支付，必须是已经完成扣款的医院的付款单才能实现向经销商账户的支付
 * 即:
 * select * from b_pay where state_kou='Y' and state_fu in ('N','P') 
 *  
 *  对于支付成功的行，将修改state_fu='P', 并为 收款方自动创建 已收款单 
 *  
 */
public class AHYY_ReceiveRetSuccess extends SvrProcess
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

		Connection conn= null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		QueryEngine engine= QueryEngine.getInstance();
		conn= engine.getConnection();

	    Configurations conf= (Configurations)WebUtils.getServletContextManager().getActor( nds.util.WebKeys.CONFIGURATIONS);
	    String folder= conf.getProperty("ahyy.payment.folder.download", "e:/act/ahyy/download");
	    File fd= new File(folder);
	    if(!fd.exists()) fd.mkdirs();
	    //“yz--yymmdd.txt”上传，名称yymmdd--ylcg.xls;失败文件yymmdd--ylbs.xls
    	SimpleDateFormat df=new SimpleDateFormat("yyMMdd");
	    String ftpFile=df.format(new java.util.Date())+"--ylcg.xls";
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
			//BANKACCOUNT ||'|' || BANKOWNER || '|' || BANKNAME
			String[][] data= this.convertExcelToArray(file,log);
			pstmt=conn.prepareStatement("update b_pay set state_fu=?, msg_fu =? where docno=? "+
					"and TOT_AMT_ACTUAL=? and state_kou='Y' and state_fu ='R' and status=2 and "+
					"exists (select 1 from c_bpartner c where c.id=b_pay.C_BPARTNER2_ID and "+
					"c.BANKACCOUNT=? and c.BANKOWNER=? and c.BANKNAME=?)");
			for(int i=0;i<data.length;i++){
				/*
	 * 收款账号	总金额	收款人	汇入地址	开户行	用途	备注	汇款方式	收款行号	处理结果
4904720010200060789	79.2	李放		中国银行池州市分行	08-21,898340272980028,0.80		普通	104362004010	成功
				 * 
				 */
				String billNo= data[i][6];// 备注 即我们的单号
				if(Validator.isNull(billNo)) continue;
				double amt= Double.valueOf(data[i][1]).doubleValue();
				String BANKACCOUNT=data[i][0];
				String BANKOWNER=data[i][2];
				String BANKNAME=data[i][4];
				
				log.debug("docno="+billNo+", TOT_AMT_ACTUAL="+amt+",BANKACCOUNT="+BANKACCOUNT+",BANKOWNER="+BANKOWNER+",BANKNAME="+BANKNAME);	

				java.math.BigDecimal bd=new java.math.BigDecimal(amt);
				pstmt.setString(1,"Y");
				pstmt.setNull(2, java.sql.Types.VARCHAR);
				pstmt.setString(3, billNo.trim());
				
				pstmt.setDouble(4, amt);
				pstmt.setString(5,BANKACCOUNT.trim() );
				pstmt.setString(6,BANKOWNER.trim());
				pstmt.setString(7,BANKNAME.trim());
				
				int ret= pstmt.executeUpdate();
				if(ret!=1){
					sb.append("line "+(i+1)+" updated "+ ret+" lines").append(lineSep);
				}else{
					// 已收款单自动创建，并提交
					// in trigger
				}
			}
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
	/**
	 * 
	 * @param xlsFile
	 * 收款账号	总金额	收款人	汇入地址	开户行	用途	备注	汇款方式	收款行号	处理结果
4904720010200060789	79.2	李放		中国银行池州市分行	08-21,898340272980028,0.80		普通	104362004010	成功

	 * @return null if no rows found
	 * @throws Exception
	 */
	static String[][] convertExcelToArray(String xlsFile, nds.log.Logger logger) throws Exception{
        POIFSFileSystem fs ;
        fs=new POIFSFileSystem(new FileInputStream(xlsFile));

        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        logger.debug("Last row num:"+  sheet.getLastRowNum());
        if(sheet.getLastRowNum()<1){
        	return null;
        }
        String[][] colData= new String[sheet.getLastRowNum()][10];
        logger.debug("Create array["+(sheet.getLastRowNum())+"][10]" );
        for ( int i= 1; i<=sheet.getLastRowNum();i++){
            HSSFRow row = sheet.getRow(i);
            if( row==null) continue;
            for( int j=0;j< 10;j++){
                HSSFCell cell = row.getCell((short)j);
                if(cell==null){
                	if( j==0)
                    	break;
                	//log.info("fond null cell in line:"+ i+", j="+j);
                }else{
	                try{
	                	HSSFRichTextString o=cell.getRichStringCellValue();
	                	if(o!=null)
	                		colData[i-1][j]=o.getString();
	                }catch(NumberFormatException e2){
	                    // cell is a number format, so try using numeric
	                	colData[i-1][j]= String.valueOf(cell.getNumericCellValue());
	                }
                }
            }
        }
        
        return colData;
	}
}
