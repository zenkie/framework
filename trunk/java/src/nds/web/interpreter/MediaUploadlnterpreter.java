package nds.web.interpreter;

import java.util.Locale;

import nds.schema.Column;
import nds.schema.Table;
import nds.schema.TableManager;
import nds.util.ColumnInterpretException;
import nds.util.ColumnInterpreter;

import org.json.JSONArray;
import org.json.JSONObject;

public class MediaUploadlnterpreter implements ColumnInterpreter,java.io.Serializable  {

	public  MediaUploadlnterpreter(){}
//	private String value;
//	private Locale locale;
	private int objid;
  	private int colid;
 	private Table table;
  	
  	@Override
  	public String parseValue(Object value, Locale locale) {

  		if(value==null) return "";
		String[] vals=((String) value).split("@");
		if(vals.length==2){
    		this.objid=Integer.parseInt(vals[0]);
    		this.colid=Integer.parseInt(vals[1]);
    		Column col=TableManager.getInstance().getColumn(this.colid);
    		table=col.getTable();
    		JSONObject jor=col.getJSONProps();
    		String medcol = jor.optString("mediacolumn"); 
    		int mediamax = jor.optInt("mediamax");
    		String htmlli = null;
    		for(int i=0;i<mediamax;i++)
    		{
    			if(i==0)
    			{
    				htmlli= "<li ><div class='media-upload'><img class='imgc' id='p"+(i+1)+"' src='' width='100' height='100'/>"+
			      					"<span class='float-label'>×</span>"+
			      					"</div></li>";
    			}else{
    				
    				htmlli+="<li class='cli' style='float:left;'><div id='media-upload' class='media-upload'><img class='imgc' id='p"+(i+1)+"' src='' width='100' height='100'/>"+
  					"<span class='float-label'>×</span>"+
  					"</div></li>";
    			}
    		}
    		String html ="<script type='text/javascript' src='/html/nds/oto/js/mediaupload.js'></script>"+
	    				 "<div  class='uploadmedia-body'>" +
		    				"<div id='button_attention'>"+
		    					"<div id='upload_44444' class='uploadifive-button'></div>"+
		    				"</div>"+
		    				"<div id='media_position'>"+
		    				    "<ul  id='imgList'>"+htmlli+"</ul>"+
		    				"</div>"+
		    				"<div id='media_attention'><span>提示：</span><br><br>" +
		    						"<span>可以上传<strong class='bright' style='color:red;'>"+mediamax+"</strong>张图片，还能上传1张</span><br><br>" +
		    						"<span>图片建议尺寸100*100</span>" +
		    				"</div>"+
	    				"</div>";
    		
    		return html;
		}
  		return "";
  	}
  	@Override
  	public Object getValue(String str, Locale locale)
  			throws ColumnInterpretException {
  		// TODO Auto-generated method stub
  		return null;
  	}
  	@Override
  	public String changeValue(String str, Locale locale)
  			throws ColumnInterpretException {
  		// TODO Auto-generated method stub
  		return null;
  	}

}
