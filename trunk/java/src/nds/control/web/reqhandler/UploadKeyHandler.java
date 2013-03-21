package nds.control.web.reqhandler;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;

import com.liferay.util.servlet.SessionErrors;

import nds.control.event.DefaultWebEvent;
import nds.control.event.NDSEvent;
import nds.control.event.NDSEventException;
import nds.control.web.SecurityManagerWebImpl;
import nds.control.web.ServletContextManager;
import nds.control.web.SessionInfo;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.schema.TableManager;
import nds.util.Configurations;
import nds.util.Tools;
import nds.util.WebKeys;
import java.sql.Connection; 
import java.sql.PreparedStatement; 
import java.sql.ResultSet; 



/**
 *  配合自定义上传keyfile  然后插入到数据库中 作为服务器的key证书校验码
 *  inited by jackrain 2013.03.19
 *  @since 4.0
 */
public class UploadKeyHandler extends RequestHandlerSupport {
	
	private static Logger logger=LoggerManager.getInstance().getLogger(UploadKeyHandler.class.getName());

	public UploadKeyHandler() {}

	public NDSEvent processRequest(HttpServletRequest request) throws NDSEventException {
		//long begintime=System.currentTimeMillis() ;
		try {
			Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
			if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
			DefaultWebEvent ei=new DefaultWebEvent("CommandEvent");

			ei.setParameter("command", "CheckmacKey");
			JSONObject jo=new JSONObject();


			Configurations conf=(Configurations)WebUtils.getServletContextManager().getActor(WebKeys.CONFIGURATIONS);


			//	          Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();

			//	             Set factory constraints
			factory.setSizeThreshold(1024*1024 * Tools.getInt( conf.getProperty("import.excel.maxsize", "1"), 1)); // 1MB
			factory.setRepository(new File(conf.getProperty("dir.tmp","/portal/act.nea/tmp")));

			//	             Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			//	             Set overall request size constraint
			upload.setSizeMax(1024*1024 * Tools.getInt( conf.getProperty("import.excel.maxsize", "1"), 1));

			//	             Parse the request
			List /* FileItem */ fileItems = upload.parseRequest(request);

			Iterator iter = fileItems.iterator();

			String fileName=null;
			FileItem fileItem=null;
			String va = null;
			Connection conn=null;
			PreparedStatement  pstmt=null;
			ResultSet rs = null;
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();

				if (item.isFormField()) {
					jo.put(item.getFieldName(), item.getString());
					// add to request attributes for later usage
					// since 2.0
					request.setAttribute(item.getFieldName(),item.getString() );
				} else {
					//如果是文件字段
					String value = item.getName();//会将完整路径名传过来  
					int start = value.lastIndexOf("\\"); 
					fileName = value.substring(start+1); 
					InputStream in = item.getInputStream(); 
					int index = fileName.lastIndexOf("."); 
					String realFileName = fileName.substring(0,index); 
					String type = fileName.substring(index+1); 
					// dao.insert(in, realFileName,type,va);//放入到数据库中  

					byte[] b = new byte[1024];
					String res = "";
					int bytesRead = 0;// 已经成功读取的字节的个数
					try {
						while (true) {
							bytesRead = in.read(b, 0, 1024); // return final read bytes counts
							if (bytesRead == -1) {// end of InputStream
								break;
							}
							res += new String(b, 0, bytesRead); // convert to string using bytes
						}
						logger.debug("read key file ："+res);
						in.close();
					} catch (Throwable t) {
						logger.error("keyfile not to read InputStream: " + t);
						throw new NDSEventException("@exception@",t);
					}
					try{
						conn= nds.query.QueryEngine.getInstance().getConnection();
						String sql = "update users  set mac=? where id=893";   
						pstmt= conn.prepareStatement(sql);
						pstmt.setString(1, res); 
						// 4.执行语句    
						pstmt.executeUpdate();   

					}catch(Throwable t){
						logger.debug("error update matchine code ",t);
						throw new NDSEventException("@exception@",t);
					}finally{
						try{if(rs!=null) rs.close();}catch(Throwable t){}
						try{if(pstmt!=null) pstmt.close();}catch(Throwable t){}
						try{if(conn!=null) conn.close();}catch(Throwable t){}
					}

				}
			}

			//jo.put("file", absFileName);
			jo.put("javax.servlet.http.HttpServletRequest",request);

			jo.put("objectid",0);// fake one, must set to 0, so webaction will take as can display(permission ok)

			//logger.debug("by user " + user.getUserName()+":"+jo.toString());
			ei.put("JSONOBJECT", jo);
			return ei;
		}
		catch (Exception ex) {
			logger.error("error handling import keyfile request", ex);
			if(ex instanceof NDSEventException) throw (NDSEventException)ex;
			else throw new NDSEventException("@eception@:"+ ex);
		}finally{

		}
	}




}
