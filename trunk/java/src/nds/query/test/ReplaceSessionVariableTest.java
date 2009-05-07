/*
 * Agile Control Technologies Ltd,. CO.
 * http://www.agileControl.com
 */
package nds.query.test;

import nds.query.*;
import java.util.*;
import nds.util.*;
import junit.framework.TestCase;
import nds.log.Logger;
import nds.log.LoggerManager;


/**
 * 
 * @author yfzhu@agilecontrol.com
 */

public class ReplaceSessionVariableTest  extends TestCase {
	private QuerySession session;
	public ReplaceSessionVariableTest(String name) {
        super(name);
		session= new QuerySessionImpl();
		session.setAttribute("$AD_CLIENT_ID$", "10993");
		session.setAttribute("$AD_USER_ID$", "1001");
    }
	protected void setup(){
	}
	public void testReplace(){
		String sql="$$AD_CLIENT_$ID$$$AD_USER_ID$$";
		String expect="$$AD_CLIENT_$ID$$1001$";
		
		this.assertEquals(replaceVariables(sql), expect);
		sql= "$$AD_CLIENT_ID$$$AD_USER_ID$$";
		expect="$10993$1001$";
		this.assertEquals(replaceVariables(sql), expect);

		sql= "hello";
		expect="hello";
		this.assertEquals(replaceVariables(sql), expect);
}
	public static void main(String[] args) {
	}
	
	protected String replaceVariables(String sql){
		//method: search sqlWithVariables one by one, when found "$",
		//check to the next "$", and try to found attribute value of
		//that, if not found, take first $ as nothing, go to next.
		StringBuffer sb=new StringBuffer();
		int p= 0,p1,p2;
		while(p < sql.length()){
			p1= sql.indexOf("$", p);
			if(p1>-1){
				//found
				p2=sql.indexOf("$", p1+1);
				if(p2>-1){
					//found second
					String n= sql.substring(p, p2+1);
					Object v= session.getAttribute(n);
					if (v!=null) {
						//replace variable to attribute value
						sb.append(sql.substring(p, p1)).append(v);
						p=p2+1;
					}else{
						//remain the fake variable, not include last $
						sb.append(sql.substring(p, p2));
						p=p2;
					}
					
				}else{
					// not found the second $, so no variable any more
					sb.append(sql.substring(p));
					break;
				}
			}else{
				// not found the first $,so no variable any more
				sb.append(sql.substring(p));
				break;
			}
		}
		return sb.toString();
	}	
}
