/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.web.button;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.security.LoginFailedException;
import nds.util.*;
import nds.control.event.NDSEventException;
import nds.schema.*;
import nds.control.web.*;
import nds.query.*;
/**
 * AHYY_signContract
 */
public class Button_AHYY_signContract extends ButtonCommandUI_Impl{
	/**
	 * This is to get the UI handle page, if special page should be directed and get interaction
	 * between user and machine, this should be extended. 
	 * @param request
	 * @param column
	 * @param objectId
	 * @return
	 */
	protected String getHandleURL(HttpServletRequest request, Column column, int objectId){
		
		StringBuffer sb = new StringBuffer();
		sb.append(WebKeys.WEB_CONTEXT_ROOT).
		append(WebKeys.NDS_URI+ "/ahyy/signcontract.jsp?objectid=").append(objectId);
		return sb.toString();
	}
	/**
	 * Popup type
	 * @return
	 */
	protected String getPopupType( HttpServletRequest request, Column column, int objectId){
		return POPUP_TARGET_BLANK;
	}
	/**
	 * Button caption
	 * @return
	 */
	protected String getCaption(HttpServletRequest request, Column column, int objectId ){
		try{
			UserWebImpl userWeb=(UserWebImpl) WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER);
//			 a party or b party?
			boolean aParty=false;
			QueryEngine engine= QueryEngine.getInstance();
			int bpartnerId=Tools.getInt(engine.doQueryOne("select c_bpartner_id from users where id="+ userWeb.getUserId()),-1);
			List al= (List) engine.doQueryList("select HOSPITAL,RETAILER,a_signer_id,b_signer_id,status from b_contract where id="+ objectId).get(0);
			int aPartyId =Tools.getInt( al.get(0),-2);
			int bPartyId =Tools.getInt( al.get(1),-2);

			boolean isQuit=false;
			if(bpartnerId==aPartyId){
				aParty=true;
				if( Tools.getInt(al.get(2),-1)!=-1)isQuit=true;
			}else{
				if(bpartnerId==bPartyId){
					aParty=false;
					if( Tools.getInt(al.get(3),-1)!=-1)isQuit=true;
				}else{
					logger.debug(" not any party of the contract:"+ objectId+" for user "+ userWeb.getUserId());
					return "N/A";
				}
			}
			Locale locale = (Locale)request.getSession(true).getAttribute(org.apache.struts.Globals.LOCALE_KEY);
			if(locale==null)locale= TableManager.getInstance().getDefaultLocale();
			String key=(isQuit? "contract-quit":"contract-sign");
			
			return nds.util.MessagesHolder.getInstance().getMessage(locale, key); 
		}catch(Exception e){
			logger.debug("Erro check permission for directory 'user_list':" + e );
			return "N/A";
		}		
	}
	
	/**
	 * Should display the button or just blank
	 * @return
	 */
	protected boolean isValid(HttpServletRequest request, Column column, int objectId ){
		try{
			UserWebImpl userWeb=(UserWebImpl) WebUtils.getSessionContextManager(request.getSession(true)).getActor(nds.util.WebKeys.USER);
			if(!userWeb.hasObjectPermission("B_CONTRACT", objectId, 3)){
				return false;
			}
//			 a party or b party?
			boolean aParty=false;
			QueryEngine engine= QueryEngine.getInstance();
			int bpartnerId=Tools.getInt(engine.doQueryOne("select c_bpartner_id from users where id="+ userWeb.getUserId()),-1);
			List al= (List) engine.doQueryList("select HOSPITAL,RETAILER,a_signer_id,b_signer_id,status from b_contract where id="+ objectId).get(0);
			int aPartyId =Tools.getInt( al.get(0),-2);
			int bPartyId =Tools.getInt( al.get(1),-2);
			int status=Tools.getInt( al.get(4),-1);
			if(status==2) return false;
			else
				return bpartnerId==aPartyId || bpartnerId==bPartyId;
			/*
			
			boolean isQuit=false;
			
			if(bpartnerId==aPartyId){
				aParty=true;
				if( Tools.getInt(al.get(2),-1)!=-1)isQuit=true;
			}else{
				if(bpartnerId==bPartyId){
					aParty=false;
					if( Tools.getInt(al.get(3),-1)!=-1)isQuit=true;
				}else{
					logger.debug(" not any party of the contract:"+ objectId+" for user "+ userWeb.getUserId());
					return false;
				}
			}
			return (isQuit == false);*/ 
		}catch(Exception e){
			logger.debug("Erro check permission for directory 'user_list':" + e );
			return false;
		}
	}	
}
