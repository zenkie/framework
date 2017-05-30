package nds.transport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nds.util.License;
import nds.util.Validator;

public final class transport {
	
	private String mailNo;
	private int status=0;//查询结果状态，0|1|2|3|4，0表示查询失败，1正常，2派送中，3已签收，4退回
	private String message;
	private String data;
	private String html;
	private long  update;
	private String expSpellName;	 
	private String expTextName;
	private  String Fh_Info=null;
	private  String Ps_Info=null;
	private  String Qr_Info=null;
	
	
	transport(String mailNo,int status,String data,
			long update,String expSpellName,String expTextName){
		this.mailNo=mailNo;
		this.status=status;
		this.data=data;
		this.update=update;
		this.expSpellName=expSpellName;
		this.expTextName=expTextName;

	}
	
	


	public StringBuffer toHtml(transport ts) throws Exception {
		//JSONArray jor=new JSONArray(ts.data);
		StringBuffer ret=new StringBuffer();
		String head="<table class=\"ickd_return\"><tbody>";
		ret.append(head);
		if(Validator.isNull((String)ts.data) ){
			
			JSONArray ja= new JSONArray();
		}else{
			JSONArray ja= new JSONArray((String)ts.data);
			String time=null;
			String context=null;
			for(int i=0;i< ja.length();i++){
			 String rowcon="";
			 JSONObject	jo= ja.getJSONObject(i);
			 time =jo.getString("AcceptTime");
			 context=jo.getString("AcceptStation");
			 rowcon=("<tr><td>"+time+"</td><td>"+context+"</td></tr>");
			 ret.append(rowcon);
			}
		}
		ret.append("<tr><td>信息来源:"+ts.expTextName+"</td><td>订单编号:"+ts.mailNo+"</td><tr></tbody></table>");
		return ret;	
	}
	
	
	public static StringBuffer toLine(transport ts) {
		//status 	int 	查询结果状态，0|1|2|3|4，0表示查询失败，1正常，2派送中，3已签收，4退回
		StringBuffer ret=new StringBuffer();
		
		int raid=ts!=null&&ts.status==3?44:0;
		raid=ts!=null&&ts.status==2?22:raid;
		String head="<div id=\"ts_line\"><span class=\"ts-item-show\"><a href=\"\"  data-title=\"\" >"
		 +"<img src=\"/html/nds/images/box.png\"></a></span><ul class=\"ts-item-flow\"><li class=\"ts-line\"></li><li class=\"rate-line\" style=\"width:"+raid+"%;\"></li>";
		ret.append(head);
		ret.append("<li class=\"fh\"><a id=\"line_fh\" title=\""+(ts!=null?ts.getFh_Info():"")+"\"></a><em>已发货</em></li>");
		ret.append("<li class=\"ps\"><a id=\"line_ps\" title=\""+(ts!=null?ts.getPs_Info():"")+"\"></a><em>配送中</em></li>");
		ret.append("<li class=\"qr\"><a id=\"line_qr\" title=\""+(ts!=null?ts.getQr_Info():"")+"\"></a><em>已签收</em></li>");
		ret.append("</ul></div>");
		if(ts!=null&&ts.status>=1){
		ret.append("<script>jQuery(\"#line_fh,#line_ps,#line_qr\").poshytip({className: 'tip-yellowsimple',showTimeout: 1,alignTo: 'target',alignX: 'center',offsetY: 5});</script>");
		ret.append("<div style=\"display:none;\" id=\"ts_html\" >"+ts.getHtml()+"</div>");
		}else if(ts!=null&&ts.status==0){
			ret.append("<script>jQuery(\"#line_fh\").poshytip({showOn:'none',className: 'tip-yellowsimple',showTimeout: 1,alignTo: 'target',alignX: 'center',offsetY: 5});</script>");
			ret.append("<script>jQuery(\"#line_fh\").poshytip('show');jQuery(\"#line_fh\").poshytip('hideDelayed',6000);</script>");
			ret.append("<div style=\"display:none;\" id=\"ts_html\" >"+ts.getHtml()+"</div>");
		}
		return ret;
	}
	
	
	
	public void setFh_Info(int status) throws JSONException {
		if(status>=1||status==0){
			//System.out.print("setFh_Info"+status);
			String context=null;//time =jo.getString("time");
			if(status==0){
				context=getMessage()+":\'"+getMailNo()+"\'<br><"+expTextName+">";
				 Fh_Info = context;
				 return;
			}
			JSONArray ja= new JSONArray((String)data);
			JSONObject	jo= ja.getJSONObject(0);
			 if(status>=1)context=jo.getString("AcceptTime")+"<br>"+jo.getString("AcceptTime")+"<"+expTextName+">";
			 Fh_Info = context;
		}
		if(status==2){
			JSONArray ja= new JSONArray((String)data);
			JSONObject jo=new JSONObject();
			if(ja.length()<2){
				jo= ja.getJSONObject(ja.length()-1);
			}else{
				jo= ja.getJSONObject(1);
			}
				
			 //String time =jo.getString("time");
			 String context=jo.getString("AcceptStation")+"<br>"+jo.getString("AcceptTime")+"<"+expTextName+">"+"<a href=javascript:void(0); onclick=art.artDialog(jQuery('#ts_html').html())>详细信息</a>";
			 Ps_Info = context;
		}
		if(status>=3){
			JSONArray ja= new JSONArray((String)data);
			JSONObject	jo= ja.getJSONObject(ja.length()-1);
			 //String time =jo.getString("time");
			 String context=jo.getString("AcceptStation")+"<br>"+jo.getString("AcceptTime")+"<"+expTextName+">"+"<a href=javascript:void(0); onclick=art.artDialog(jQuery('#ts_html').html())>详细信息</a>";
			 Qr_Info = context;
		}
	}
	
	public String setMessage(String msg) {
		return this.message=msg;
	}
	public String getMessage() {
		return this.message;
	}

	
	public    String getFh_Info() {
		return this.Fh_Info;
	}
	
	public    String getPs_Info() {
		return this.Ps_Info;
	}
	
	public    String getQr_Info() {
		return this.Qr_Info;
	}
	
	
	public void setMailNo(String mailNo) {
		this.mailNo = mailNo;
	}

	public String getMailNo() {
		return this.mailNo;
	}
	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return this.status;
	}
	public void setExpTextNamel(String expTextName) {
		this.expTextName = expTextName;
	}

	public String getExpTextName() {
		return this.expTextName;
	}
	
	public void setHtml(String html) {
		this.html = html;
	}

	public String getHtml() {
		return this.html;
	}

}
