package nds.fckeditor;

import nds.util.*;
import nds.control.web.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import net.fckeditor.handlers.CommandHandler;
import net.fckeditor.handlers.ConnectorHandler;
import net.fckeditor.handlers.ExtensionsHandler;
import net.fckeditor.handlers.RequestCycleHandler;
import net.fckeditor.handlers.ResourceTypeHandler;
import net.fckeditor.response.UploadResponse;
import net.fckeditor.response.XmlResponse;
import net.fckeditor.tool.Utils;
import net.fckeditor.tool.UtilsFile;
import net.fckeditor.tool.UtilsResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Different with net.fckeditor.connector.ConnectorServlet, the root folder can based out of
 * context.
 * 
 * User has only permission to view/upload files within the folder of his company
 * 
 * Servlet to upload and browse files.<br>
 * 
 * This servlet accepts 4 commands used to retrieve and create files and folders from a server
 * directory. The allowed commands are:
 * <ul>
 * <li>GetFolders: Retrieve the list of directory under the current folder
 * <li>GetFoldersAndFiles: Retrive the list of files and directory under the current folder
 * <li>CreateFolder: Create a new directory under the current folder
 * <li>FileUpload: Send a new file to the server (must be sent with a POST)
 * </ul>
 * 
 */
public class FCKEditorServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(FCKEditorServlet.class);
	private String clientWebRoot;
	/**
	 * Initialize the servlet.<br>
	 * The default directory for user files will be constructed.
	 */
	public void init() throws ServletException, IllegalArgumentException {
	    Configurations conf=(Configurations)nds.control.web.WebUtils.getServletContextManager().getActor(nds.util.WebKeys.CONFIGURATIONS);	    
	    clientWebRoot=conf.getProperty("client.webroot","/act/webroot");
		
		/*
		// check, if 'baseDir' exists
		String realDefaultUserFilesPath = getServletContext().getRealPath(
		        ConnectorHandler.getDefaultUserFilesPath());

		File defaultUserFilesDir = new File(realDefaultUserFilesPath);
		UtilsFile.checkDirAndCreate(defaultUserFilesDir);

		logger.info("ConnectorServlet successful initialized!");
		*/
	}

	/**
	 * Manage the Get requests (GetFolders, GetFoldersAndFiles, CreateFolder).<br>
	 * 
	 * The servlet accepts commands sent in the following format:<br>
	 * connector?Command=CommandName&Type=ResourceType&CurrentFolder=FolderPath<br>
	 * <br>
	 * It executes the commands and then return the results to the client in XML format.
	 * 
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/xml; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();

		String commandStr = request.getParameter("Command");
		String typeStr = request.getParameter("Type");
		String currentFolderStr = request.getParameter("CurrentFolder");

		/*logger.debug("Parameter Command: {}", commandStr);
		logger.debug("Parameter Type: {}", typeStr);
		logger.debug("Parameter CurrentFolder: {}", currentFolderStr);
*/
		UserWebImpl userWeb =null;
		java.util.Locale locale=null;//  nds.schema.TableManager.getInstance().getDefaultLocale();
    	try{
    		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));	
    	}catch(Throwable userWebException){
    		
    	}
    	if(userWeb!=null && userWeb.getUserId()!=UserWebImpl.GUEST_ID ){
    		locale= userWeb.getLocale();
    	} 
		
		XmlResponse xr;
		
		if (!RequestCycleHandler.isEnabledForFileBrowsing(request))
			xr = new XmlResponse(XmlResponse.EN_ERROR,nds.util.MessagesHolder.getInstance().getMessage(locale, "not-authorized-for-browsing"));
		else if (!CommandHandler.isValidForGet(commandStr))
			xr = new XmlResponse(XmlResponse.EN_ERROR,nds.util.MessagesHolder.getInstance().getMessage(locale, "invalid-command"));
		else if (typeStr != null && !ResourceTypeHandler.isValid(typeStr))
			xr = new XmlResponse(XmlResponse.EN_ERROR, nds.util.MessagesHolder.getInstance().getMessage(locale, "invalid-resouce-type"));
		else if (!UtilsFile.isValidPath(currentFolderStr))
			xr = new XmlResponse(XmlResponse.EN_ERROR, nds.util.MessagesHolder.getInstance().getMessage(locale, "invalid-current-folder"));
		else {
			CommandHandler command = CommandHandler.getCommand(commandStr);
			ResourceTypeHandler resourceType = ResourceTypeHandler.getDefaultResourceType(typeStr);

			// userWeb must exists
	    	String domain= userWeb.getClientDomain();
	    	
			String typeDirPath = clientWebRoot+"/"+domain+"/"+resourceType.getPath()+currentFolderStr;
			//Following is from ConnectorServlet, now replaced with client.webroot+client.domain
			//String typePath = UtilsResponse.constructResponseUrl(request, resourceType,currentFolderStr, false, false);
			//String typeDirPath = getServletContext().getRealPath(typePath);

			File typeDir = new File(typeDirPath);
			UtilsFile.checkDirAndCreate(typeDir);

			File currentDir =typeDir;// new File(typeDir, currentFolderStr);
			
			if (!currentDir.exists()){
				logger.debug(currentDir.getAbsolutePath()+ " not exists");
				xr = new XmlResponse(XmlResponse.EN_INVALID_FOLDER_NAME);
			}else {
				/**
				 * set constructedUrl to /servlets/userfolder
				 */
				xr = new XmlResponse(command, resourceType, currentFolderStr,
						WebClientUserFileServlet.USER_FOLDER_PATH+ resourceType.getPath()+currentFolderStr);				
				/*
				xr = new XmlResponse(command, resourceType, currentFolderStr,
						 UtilsResponse.constructResponseUrl(request, resourceType, currentFolderStr, true,ConnectorHandler.isFullUrl()));
				*/
				if (command.equals(CommandHandler.GET_FOLDERS))
					xr.setFolders(currentDir);
				else if (command.equals(CommandHandler.GET_FOLDERS_AND_FILES))
					xr.setFoldersAndFiles(currentDir);
				else if (command.equals(CommandHandler.CREATE_FOLDER)) {
					String newFolderStr = UtilsFile.sanitizeFolderName(request
					        .getParameter("NewFolderName"));

					File newFolder = new File(currentDir, newFolderStr);
					int errorNumber = XmlResponse.EN_UKNOWN;

					if (newFolder.exists())
						errorNumber = XmlResponse.EN_ALREADY_EXISTS;
					else {
						try {
							errorNumber = (newFolder.mkdir()) ? XmlResponse.EN_OK
							        : XmlResponse.EN_INVALID_FOLDER_NAME;
						} catch (SecurityException e) {
							errorNumber = XmlResponse.EN_SECURITY_ERROR;
						}
					}
					xr.setError(errorNumber);
				}
			}
		}

		out.print(xr);
		out.flush();
		out.close();
	}

	/**
	 * Manage the Post requests (FileUpload).<br>
	 * 
	 * The servlet accepts commands sent in the following format:<br>
	 * connector?Command=FileUpload&Type=ResourceType&CurrentFolder=FolderPath<br>
	 * <br>
	 * It store the file (renaming it in case a file with the same name exists) and then return an
	 * HTML file with a javascript command in it.
	 */
	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();

		String commandStr = request.getParameter("Command");
		String typeStr = request.getParameter("Type");
		String currentFolderStr = request.getParameter("CurrentFolder");

		UploadResponse ur;

		// if this is a QuickUpload-Request, 'commandStr' and 'currentFolderStr' are empty
		if (Utils.isEmpty(commandStr) && Utils.isEmpty(currentFolderStr)) {
			commandStr = "QuickUpload";
			currentFolderStr = "/";
		}

		UserWebImpl userWeb =null;
		java.util.Locale locale=  nds.schema.TableManager.getInstance().getDefaultLocale();
    	try{
    		userWeb= ((UserWebImpl)WebUtils.getSessionContextManager(request.getSession()).getActor(nds.util.WebKeys.USER));	
    	}catch(Throwable userWebException){
    		
    	}
    	if(userWeb!=null && userWeb.getUserId()!=UserWebImpl.GUEST_ID ){
    		locale= userWeb.getLocale();
    	}

		
		if (!RequestCycleHandler.isEnabledForFileUpload(request))
			ur = new UploadResponse(new Object[]{UploadResponse.EN_SECURITY_ERROR, null, null,
					nds.util.MessagesHolder.getInstance().getMessage(locale, "not-authorized-for-upload")});
		else if (!CommandHandler.isValidForPost(commandStr))
			ur = new UploadResponse(new Object[]{UploadResponse.EN_ERROR, null, null, nds.util.MessagesHolder.getInstance().getMessage(locale, "invalid-command")});
		else if (typeStr != null && !ResourceTypeHandler.isValid(typeStr))
			ur = new UploadResponse(new Object[]{UploadResponse.EN_ERROR, null, null,nds.util.MessagesHolder.getInstance().getMessage(locale, "invalid-resouce-type")});
		else if (!UtilsFile.isValidPath(currentFolderStr))
			ur = UploadResponse.UR_INVALID_CURRENT_FOLDER;
		else {
			ResourceTypeHandler resourceType = ResourceTypeHandler.getDefaultResourceType(typeStr);

			// userWeb must exists
	    	String domain= userWeb.getClientDomain();
	    	
			String typeDirPath = clientWebRoot+"/"+domain+resourceType.getPath()+currentFolderStr;

			//String typePath = UtilsResponse.constructResponseUrl(request, resourceType,currentFolderStr, false, false);
			//String typeDirPath = getServletContext().getRealPath(typePath);

			File typeDir = new File(typeDirPath);
			UtilsFile.checkDirAndCreate(typeDir);

			File currentDir =typeDir;// new File(typeDir);

			if (!currentDir.exists())
				ur = UploadResponse.UR_INVALID_CURRENT_FOLDER;
			else {

				String newFilename = null;
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);

				try {

					List<FileItem> items = upload.parseRequest(request);

					// We upload only one file at the same time
					FileItem uplFile = items.get(0);
					String rawName = UtilsFile.sanitizeFileName(uplFile.getName());
					String filename = FilenameUtils.getName(rawName);
					String baseName = FilenameUtils.removeExtension(filename);
					String extension = FilenameUtils.getExtension(filename);

					if (!ExtensionsHandler.isAllowed(resourceType, extension))
						ur = new UploadResponse(new Object[]{UploadResponse.EN_INVALID_EXTENSION});
					else {

						// construct an unique file name
						File pathToSave = new File(currentDir, filename);
						int counter = 1;
						while (pathToSave.exists()) {
							newFilename = baseName.concat("(").concat(String.valueOf(counter))
							        .concat(")").concat(".").concat(extension);
							pathToSave = new File(currentDir, newFilename);
							counter++;
						}

						if (Utils.isEmpty(newFilename))
							ur = new UploadResponse(new Object[]{UploadResponse.EN_OK,  
									WebClientUserFileServlet.USER_FOLDER_PATH+ resourceType.getPath()+currentFolderStr+filename
									/*UtilsResponse.constructResponseUrl(request, resourceType, currentFolderStr,true, ConnectorHandler.isFullUrl()).concat(filename)*/
							});
						else
							ur = new UploadResponse(new Object[]{UploadResponse.EN_RENAMED,
									WebClientUserFileServlet.USER_FOLDER_PATH+ resourceType.getPath()+currentFolderStr+filename
							        /*UtilsResponse.constructResponseUrl(request, resourceType,
							                currentFolderStr, true, ConnectorHandler.isFullUrl())
							                .concat(newFilename)*/
							                , newFilename});

						// secure image check
						if (resourceType.equals(ResourceTypeHandler.IMAGE)
						        && ConnectorHandler.isSecureImageUploads()) {
							if (UtilsFile.isImage(uplFile.getInputStream()))
								uplFile.write(pathToSave);
							else {
								uplFile.delete();
								ur = new UploadResponse(new Object[]{UploadResponse.EN_INVALID_EXTENSION});
							}
						} else
							uplFile.write(pathToSave);

					}
				} catch (Exception e) {
					ur = new UploadResponse(new Object[]{UploadResponse.EN_SECURITY_ERROR});
				}
			}

		}

		out.print(ur);
		out.flush();
		out.close();

	}

}
