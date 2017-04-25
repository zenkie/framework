package nds.control.web.binhandler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nds.query.QueryEngine;
import nds.schema.TableManager;
import nds.util.ParamUtils;
import nds.util.Tools;
import nds.util.WebKeys;

public class ptdimgshow implements BinaryHandler{

	@Override
	public void init(ServletContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// TODO Auto-generated method stub
		
		TableManager tableManager=TableManager.getInstance();
		//int tableId= ParamUtils.getIntAttributeOrParameter(request, "table", -1);
		//int columnId =ParamUtils.getIntAttributeOrParameter(request, "column", -1);
		QueryEngine engine= QueryEngine.getInstance();
		//Connection conn= engine.getConnection();
		String pdtno= ParamUtils.getAttributeOrParameter(request, "pdtno");
		
		String imgurl=(String) engine.doQueryOne("select g.IMAGEURL from m_product g where g.name='"+pdtno+"'");
		
		if(imgurl!=null){
			request.getRequestDispatcher(imgurl).forward(request, response);
		}else{
			request.getRequestDispatcher("/images/nopic.png").forward(request, response);
		}
 
	}
	

}
