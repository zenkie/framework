/******************************************************************
*
*$RCSfile: PathMatch.java,v $ $Revision: 1.1.1.1 $ $Author: Administrator $ $Date: 2005/03/15 11:23:27 $
*
*$Log: PathMatch.java,v $
*Revision 1.1.1.1  2005/03/15 11:23:27  Administrator
*init
*
*Revision 1.1.1.1  2002/11/29 02:50:35  yfzhu
*Active POS
*
*Revision 1.2  2001/11/07 20:58:48  yfzhu
*no message
*
*
********************************************************************/
package nds.util.xml;

import java.util.StringTokenizer;

/** micro-XPath match - match a path
 */
class PathMatch implements XmlMatch {
    String names[]=new String[6];
    int pos=0;

    public PathMatch( String tagName ) {
	StringTokenizer st=new StringTokenizer( tagName, "/" );
	while( st.hasMoreTokens() ) {
	    names[pos]=st.nextToken();
	    pos++;
	}
    }

    public boolean match( SaxContext ctx ) {
	int depth=ctx.getTagCount();

	for( int j=pos-1; j>=0; j--) {
	    if( depth<1) {
		//		System.out.println("Pattern too long ");
		return false;
	    }
	    String tag=ctx.getTag(depth-1);
	    if( ! names[j].equals( tag ) ) {
		//		System.out.println("XXX" + names[j] + " " + tag);
		return false;
	    }
	    depth--;
	}


	return true;
    }

    public String toString() {
	return "Tag("+names+")";
    }

}

