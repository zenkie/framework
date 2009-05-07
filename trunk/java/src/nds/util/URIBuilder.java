package nds.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import nds.log.Logger;
import nds.log.LoggerManager;
/**
 Build new URI according to specified uri and query parameters
*/
public class URIBuilder {
    private static Logger logger= LoggerManager.getInstance().getLogger(URIBuilder.class.getName());

    private HashMap map;
    URI uri;
    String charset;
    public URIBuilder(String uriString, String charset)  throws URISyntaxException{
        map = new HashMap(6);
        uri = new URI(uriString);
        if ( charset==null) charset= StringUtils.ISO_8859_1;
        this.charset= charset;
        Tools.decodeURIQuery(uri.getRawQuery(), map, charset);
    }
    /**
     * Set one param according to input one
     * @param name String
     * @param value String
     */
    public void setQueryParam(String name, String value){
        map.put(name, value);
    }
    /**
     * merge query with the input one.
     * @param query String
     */
    public void setQuery(String query){
        Tools.decodeURIQuery(query, map, charset);
    }
    /**
     * merge query with the input one.
     * @param query String
     * @param namePrefix will be used as prefix of param name, for instance
     *   query="menu_root=root&menu_id=3", if prefix="_ncp1_", then the
     * final query will be "_ncp1_menu_root=root&_ncp1_menu_id=3"
     */
    public void setQuery(String query, String namePrefix){
        Tools.decodeURIQuery(query, map, charset, namePrefix);
    }
    public URI getURI() throws URISyntaxException,UnsupportedEncodingException{
            String query = Tools.encodeToURIQuery(map, charset);
//            System.out.println("URI:schema="+ uri.getScheme()+",auth="+ uri.getAuthority() +",path="+
//                   uri.getPath() +", query="+ query+",frag:"+ uri.getFragment() );
            URI n = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(),
                            query, uri.getFragment());
            return n;
    }
    public String toDebugString(){
        return "uri=["+ uri+"],map=["+ Tools.toString(map)+"],charset=["+charset+"]";
    }
}
