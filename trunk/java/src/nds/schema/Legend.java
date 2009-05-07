package nds.schema;

import java.util.*;
/**
 * Legend for table records in list page
 * 
 * @author yfzhu
 * @version 5 
 */
public class Legend {
	public class Item{
		private String style;
		private String description;
		public Item(String styl, String desc){
			style= styl;
			description= desc;
		}
		public String getStyle(){
			return style;
		}
		public String getDescription(Locale locale){
			return nds.util.MessagesHolder.getInstance().getMessage(locale,  description);
		}
	}
	public final static Legend EMPTY_LEGEND=new Legend();
	
	ArrayList items;
	public Legend(){
		items=new ArrayList();
	}
	public int size(){
		return items.size();
	}
	public Legend.Item getItem(int i){
		return (Legend.Item)items.get(i);
	}
	public Legend.Item addItem(String style, String description){
		Legend.Item item=new Legend.Item(style, description);
		items.add(item);
		return item;
		
	}
}
