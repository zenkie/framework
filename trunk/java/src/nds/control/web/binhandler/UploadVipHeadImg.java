package nds.control.web.binhandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nds.control.web.ServletContextManager;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.query.QueryEngine;
import nds.util.Configurations;
import nds.util.Tools;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

public class UploadVipHeadImg
  implements BinaryHandler
{
  private Logger logger = LoggerManager.getInstance().getLogger(UploadVipHeadImg.class.getName());
  private static final String CONTENT_TYPE = "image/png";

  public void init(ServletContext context)
  {
  }

  public void process(HttpServletRequest request, HttpServletResponse response)
    throws Exception
  {
    boolean isOK = false;
    String fileName = "";
    HashMap map = new HashMap();
    try
    {
      DiskFileUpload fu = new DiskFileUpload();

      fu.setSizeMax(1073741824L);

      List fileItems = fu.parseRequest(request);
      Iterator iter = fileItems.iterator();
      InputStream in = null;

      while (iter.hasNext()) {
        FileItem item = (FileItem)iter.next();
        if (!item.isFormField()) {
          in = item.getInputStream();
          fileName = item.getName();

          if (fileName != null) {
            int pos = fileName.lastIndexOf("\\");
            if (pos > 0)
              fileName = fileName.substring(pos + 1);
            pos = fileName.lastIndexOf("/");
            if (pos > 0)
              fileName = fileName.substring(pos + 1);
          }
        } else {
          map.put(item.getFieldName(), item.getString());
        }
      }
      if (in != null)
      {
        Configurations conf = (Configurations)WebUtils.getServletContextManager().getActor("nds.web.configs");
        String webRoot = conf.getProperty("web.root", "E:/portal422/server/default/deploy/nds.war/html/nds");

        File f = new File(webRoot).getParentFile().getParentFile();
        webRoot = f.getPath();
        QueryEngine engine = QueryEngine.getInstance();
        String vipImageUrl = String.valueOf(engine.doQueryOne("select value from ad_param where name='portal.vipimg.url'"));
        String filePath = webRoot + vipImageUrl + fileName;

        File file = new File(filePath);
        OutputStream out = null;
        out = new FileOutputStream(file);
        copyContents(in, out);
        out.flush();
        out.close();
        isOK = true;
        this.logger.debug("save to " + file.getAbsolutePath() + " (" + Tools.formatSize(file.length()) + ")");
      }
    } catch (Throwable t) {
      this.logger.error("fail to do upload ", t);
    }

    response.setContentType("image/png");
    PrintWriter out = response.getWriter();
    out.print("[{\"message\":\"Íê³É\",\"id\":0,\"count\":1,\"code\":0,\"rows\":[[" + fileName + "]]}]");
  }

	/**
	 *  Just copies all characters from <I>in</I> to <I>out</I>.
	 *
	 *  @since 1.9.31
	 */
	private void copyContents( InputStream in, OutputStream out )
	    throws IOException
	{
	    byte[] b = new byte[1024*16]; // 16k cache
	    int bInt;
	    while((bInt = in.read(b,0,b.length)) != -1)
	    {
	        out.write(b,0,bInt);
	    }
	    out.flush();
	}
}