package nds.control.web.binhandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nds.control.web.ServletContextManager;
import nds.control.web.UserWebImpl;
import nds.control.web.WebUtils;
import nds.log.Logger;
import nds.log.LoggerManager;
import nds.monitor.MonitorManager;
import nds.report.ReportUtils;
import nds.util.Configurations;

public class MFile
implements BinaryHandler
{
	private Logger logger = LoggerManager.getInstance().getLogger(GetFile.class.getName());
	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";
	private static final String[] TEXT_TYPE=new String[]{"html","htm","csv","txt","log"};
	private static final String DOWNLOAD_TYPE = "application/octetstream; charset=GBK";
	private static final String[] EXT=new String[]{
		"xls","doc","pdf","zip"
	};
	private static final String[] EXT_CONTENT_TYPE=new String[]{
		"application/vnd.ms-excel","application/vnd.ms-word","application/pdf","application/zip"
	};

	private static boolean isChildren(File paramFile1, File paramFile2)
	{
		boolean bool = paramFile1.getParentFile().equals(paramFile2);
		while (!bool)
		{
			if ((
					paramFile1 = paramFile1.getParentFile()) == null)
				break;
			bool = paramFile1.getParentFile().equals(paramFile2);
		}
		return bool;
	}

	public void process(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String str;
		Object localObject;
		String filePath = request.getParameter("f");
		if (filePath == null) {
			String pathInfo = request.getPathInfo();
			int p = pathInfo.indexOf('/', 1);
			if (p > 0) {
				filePath = pathInfo.substring(p + 1);

			}
		}
		if (filePath != null && !filePath.trim().equals("")) {
			filePath = filePath.trim();
			ReportUtils ru = new ReportUtils(request);
			String name = ru.getUserName();
			Configurations conf = (Configurations) nds.control.web.WebUtils
					.getServletContextManager().getActor(
							nds.util.WebKeys.CONFIGURATIONS);

			String webRoot = MonitorManager.getInstance().getMonitorRootFolder(
					ru.getUser().getClientDomain());
			File file = new File(webRoot + File.separator + filePath);

			if (file.exists() && file.isFile()) {
				logger.debug("Downloading "
						+ file.getAbsolutePath());
				response.setContentType(DOWNLOAD_TYPE);
				response.setHeader(
						"Content-Disposition",
						"attachment;"
								+ WebUtils.getContentDispositionFileName(
										filePath, request));

				FileInputStream is = new FileInputStream(file);
				ServletOutputStream os = response.getOutputStream();
				byte[] b = new byte[8192];
				int bInt;
				while ((bInt = is.read(b, 0, b.length)) != -1) {
					os.write(b, 0, bInt);
				}
				// out.write(new Byte(file.toString()));
				os.close();
				is.close();
				return;
			}
			logger.warning("Could not load file:"
					+ file.getAbsolutePath());
		}

		response.setContentType(CONTENT_TYPE);
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title>GetFile</title></head>");
		out.println("<body>");
		out.println("<p>文件不存在,或者文件不可读，或者没有指定文件名</p>");
		out.println("</body></html>");
	}

	public void init(ServletContext context) {
	}
}

/* Location:           E:\portal5\portal422\server\default\deploy\nds.war\WEB-INF\classes\
 * Qualified Name:     nds.control.web.binhandler.MFile
 * JD-Core Version:    0.6.2
 */