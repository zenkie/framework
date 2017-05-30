package nds.transport;

import nds.query.ColumnLink;
import nds.query.QueryEngine;
import nds.query.QueryRequestImpl;
import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.xml.XmlMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;  
import java.io.InputStreamReader;  
import java.io.OutputStreamWriter;  
import java.net.URL;  
import java.net.URLConnection;  
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public final class tsmanger {
	private static final Log logger = LogFactory.getLog(tsmanger.class);
	
	private  transport ts;
	
	private HashMap tsval;
	
	public tsmanger() {
		ts=null;
		if(tsval!=null)tsval.clear();
	}
	
	public void newtmload(Object val) {
		
		//loadtsinfo(val);
		try {
			loadtsinfo(val);
		} catch (Exception e) {
			ts=null;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	private HashMap get_tsval(Object val) throws Exception{
		JSONObject jo=new JSONObject(val.toString());
		int objectid= jo.getInt("objid");
		int colid=jo.getInt("colid");
		int tbid=jo.getInt("tbid");
		TableManager manager= TableManager.getInstance();
		Table table=manager.getTable(tbid);
		Column col=table.getColumn(colid);
		JSONObject pc= col.getJSONProps();
		JSONObject jor=pc.getJSONObject("intbyid");
		
		//System.out.print(pc.toString());
		 //props.toString();
		QueryEngine engine=QueryEngine.getInstance();
    	QueryRequestImpl query= engine.createRequest(null);
    	//System.out.print(tbid);
		query.setMainTable(tbid);
		ColumnLink cl =new ColumnLink(jor.getString("cp"));
		
		query.addSelection(cl.getColumnIDs(),false,null);
		if(jor.has("cpname")){
		ColumnLink cpl =new ColumnLink(jor.getString("cpname"));
		query.addSelection(cpl.getColumnIDs(),false,null);
		}
		
		query.addSelection(table.getColumn(jor.getString("mailno")).getId());
	    query.addParam( table.getPrimaryKey().getId(), ""+ objectid );
	    List q=engine.doQueryList(query.toSQL());
	    if(q!=null && q.size()>0 ){
	    	//System.out.print(q);
	    	if(q.get(0) instanceof List ){
	    		List descs= (List)q.get(0);
	    		if(descs!=null&&!descs.isEmpty()){
	    		tsval=new HashMap();
	    		tsval.put("cp", descs.get(0)==null?"null":descs.get(0).toString());
	    		tsval.put("mailno", descs.get(2)==null?"null":descs.get(2).toString());
	    		}
	    	}
	    }
	    //System.out.print("aaaa");
		return tsval;
		
	}
	
	private  void loadtsinfo(Object val) throws Exception{
		HashMap tsval=get_tsval(val);
		String cp = (String)tsval.get("cp"),mailno =(String)tsval.get("mailno");
		logger.debug("cp :"+cp+"mailno"+ mailno);
		if(cp=="null"||mailno=="null") return;
//		URL url = new URL("http://api.ickd.cn/?id=EA08B368D6C199E50704D412EB3B5DEA&com="+cp+"&nu="+mailno+"&type=json");  
//        URLConnection connection = url.openConnection(); 
//        connection.setConnectTimeout(20000);
//        connection.setReadTimeout(20000);
//        //connection.connect(); 
//        InputStream l_urlStream;  
//        l_urlStream = connection.getInputStream();  
//        String sCurrentLine;  
//        String sTotalString;  
//        sCurrentLine = "";  
//        sTotalString = ""; 
//        BufferedReader l_reader = new BufferedReader(new InputStreamReader(  
//                l_urlStream));  
//        while ((sCurrentLine = l_reader.readLine()) != null) {  
//            sTotalString += sCurrentLine;
//  
//        }  
        //System.out.println(sTotalString); 
		String result = "";
		KdniaoTrackQueryAPI api = new KdniaoTrackQueryAPI();
		try {
			result = api.getOrderTracesByJson(cp, mailno);
			logger.debug(result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        try {
			JSONObject jo=new JSONObject(result);
			ts=new transport(jo.getString("LogisticCode"), jo.optInt("State"), jo.getString("Traces"),System.currentTimeMillis(),  jo.getString("ShipperCode"), 
					tsval.containsKey("cpname")?String.valueOf(tsval.get("cpname")):jo.getString("ShipperCode"));
			ts.setMessage(jo.optString("Reason"));
			ts.setHtml(ts.toHtml(ts).toString());
			ts.setFh_Info(ts.getStatus());
			//System.out.println(ts.toLine(ts));
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		
	}
	
	public transport getTransport() {
		return ts;
	}
	
//    public static void main(String[] args) throws Exception {  
//    	  
//    	newtmload();
//  
//    }  
	
}