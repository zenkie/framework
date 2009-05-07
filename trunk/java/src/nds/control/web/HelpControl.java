package nds.control.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.help.HelpSet;
import javax.help.IndexView;
import javax.help.NavigatorView;
import javax.help.SearchHit;
import javax.help.SearchTOCItem;
import javax.help.SearchView;
import javax.help.TOCView;
import javax.help.search.MergingSearchEngine;
import javax.help.search.SearchEvent;
import javax.help.search.SearchItem;
import javax.help.search.SearchQuery;
import javax.servlet.ServletContext;
import javax.swing.tree.DefaultMutableTreeNode;

import nds.log.Logger;
import nds.log.LoggerManager;
import nds.util.Director;
import nds.util.ServletContextActor;


/**
    Controls over the help content
    Before request actions on this class, call initHelpSet() first, which will iniliaze
    HelpSet files.

    Note: we can not initialize the controller automatically in ServletContext, because
          we could not figure out what really url of the web app is, since ServletContext
          does not include such information, while HttpServletRequest has that.

          So there's two method to initialize the HelpSet file
            one is to implement the control as a Servlet, so each time we can figure out
                the HttpServletRequest
            two is to let jsp/servlet ones call initHelpSet from their place. We choose this one.
*/
public class HelpControl implements ServletContextActor,java.io.Serializable {

    private final static long TIME_WAIT=500L;// in millisecond
    //private final static String DEFAULT_HELPSET="/help/nds.hs";

    private Logger logger= LoggerManager.getInstance().getLogger(HelpControl.class.getName());

    private HelpSet helpSet;
    private DefaultMutableTreeNode tocNode, indexNode;
    private MergingSearchEngine engine;

    private boolean hasInitialized=false;// set true after doing initHelpSet()
    public HelpControl() {
    }
    public boolean isInitialized(){
        return hasInitialized;
    }
    /**
     * Init help set
     * @param url the url of the HelpSet file(.hs)
     */
    public void initHelpSet(URL url){
        if( hasInitialized) return;
        logger.debug("Help Set file url:"+ url);
        try{
            helpSet= new HelpSet(this.getClass().getClassLoader(),url);
        }catch(Exception e2){
            logger.error("Error creating HelpSet", e2);
        }

        // create TOC items
        NavigatorView view= helpSet.getNavigatorView("TOC");
        if( view ==null) throw new Error("TOC view in helpset not found, note the name of view must be \"TOC\".");
        if(!( view instanceof TOCView))
            throw new Error("TOC view type must be 'javax.help.TOCView'");
        tocNode=((TOCView)view).getDataAsTree();

        // create Index items
        view= helpSet.getNavigatorView("Index");
        if( view ==null) throw new Error("Index view in helpset not found, note the name of view must be \"Index\".");
        if(!( view instanceof IndexView))
            throw new Error("Index view type must be 'javax.help.IndexView'");
        indexNode=((IndexView)view).getDataAsTree();

        // get search engine
        view= helpSet.getNavigatorView("Search");
        if( view ==null) throw new Error("Search view in helpset not found, note the name of view must be \"Search\".");
        if(!( view instanceof SearchView))
            throw new Error("TOC view type must be 'javax.help.SearchView'");
        engine= new MergingSearchEngine(view);

        this.hasInitialized=true;
    }
    public void init(Director director) {}
    public void init(ServletContext context) {
/*        // find help path according to context root path, it will be at "$root/help" directory
        URL url=null;
        try{
            url=context.getResource(DEFAULT_HELPSET);
        }catch(Exception e){
            logger.error("Error getting resource 'help/help.hs'",e);
        }
        initHelpSet(url);*/
        logger.debug("HelpControl initialized.");
    }
    // implements ServletContextActor
    public void destroy() {
        logger.debug("HelpControl destroied.");
    }
    /**
     * Search help set with params
     * Call initHelpSet() before doing this
     * @return nodes elements of DefaultMutableTreeNode whose userObject is javax.help.search.SearchItem
     */
    public Vector doSearch(String params){
        SearchQuery query=engine.createQuery();
        SearchObserver listener=new SearchObserver();
        Object lock=new Object();
        query.addSearchListener(listener);
        query.start(params, Locale.CHINA);
        while( query.isActive()){
            try{
                lock.wait(TIME_WAIT);// cause current thread to wait
            }catch(Exception e){}
        }
        query.removeSearchListener(listener);
        return listener.getNodes();
    }
    /**
     * Get table of content of the help system, just like catalog of a book.
     * Call initHelpSet() before doing this
     * @return A TreeNode that represents the TOC. the node's userObject is javax.help.TOCItem
     *      Returns null if parsing errors were encountered
     */
    public DefaultMutableTreeNode getTOCItems(){
        return tocNode;
    }
    /**
     * Get index items of help system. You can take index items as index page at the end of one book
     * Call initHelpSet() before doing this
     * @return DefaultMutableTreeNode whose userObject is javax.help.IndexItem
     */
    public DefaultMutableTreeNode getIndexItems(){
        return indexNode;
    }
    /**
     * Get help file url according to help id. This is usually used by help button in specific page
     * Call initHelpSet() before doing this
     * @param id the id of the help file
     * @return URL of the help file, or null if not found
     * @throws MalformedURLException if errors found
     */
    public URL getHelpURL(String id) throws java.net.MalformedURLException{
        javax.help.Map map=helpSet.getCombinedMap();
        if( !map.isValidID(id, helpSet))
            return map.getURLFromID(helpSet.getHomeID());
        return map.getURLFromID(javax.help.Map.ID.create(id, helpSet));
    }
    /**
     * Save all search result
     */
  class SearchObserver implements javax.help.search.SearchListener{
    Vector nodes ;// contains javax.help.search.SearchItem
    long elapseTime;
    SearchObserver(){
        nodes = new Vector();
    }
    public void itemsFound(SearchEvent e){
        logger.debug("Items matching '"+ e.getParams()+"' found");
	SearchTOCItem tocitem;
        Enumeration itemEnum = e.getSearchItems();
	while (itemEnum.hasMoreElements()) {
	    SearchItem item = (SearchItem) itemEnum.nextElement();
	    URL url;
	    try {
		url = new URL(item.getBase(), item.getFilename());
	    } catch (MalformedURLException me) {
		logger.error("Failed to create URL from " + item.getBase() + "|" +
		       item.getFilename());
		continue;
	    }
	    boolean foundNode = false;
	    DefaultMutableTreeNode node = null;
	    Enumeration nodesEnum = nodes.elements();
	    while (nodesEnum.hasMoreElements()) {
		node = (DefaultMutableTreeNode)nodesEnum.nextElement();
		tocitem = (SearchTOCItem) node.getUserObject();
		URL testURL = tocitem.getURL();
		if (testURL != null && url != null && url.sameFile(testURL)) {
		    tocitem = (SearchTOCItem) node.getUserObject();
		    tocitem.addSearchHit(new SearchHit(item.getConfidence(),
						       item.getBegin(),
						       item.getEnd()));
		    foundNode = true;
		    break;
		}
	    }
	    if (!foundNode) {
		tocitem = new SearchTOCItem(item);
		node = new DefaultMutableTreeNode(tocitem);
		nodes.addElement(node);
	    }
	}
	reorder(nodes);

    }
    // reorder the nodes
    private void reorder (Vector nodes) {
	// Create an array of the elements for sorting & copy the elements
	// into the array.
	DefaultMutableTreeNode[] array = new DefaultMutableTreeNode[nodes.size()];
	nodes.copyInto(array);

	// Sort the array (Quick Sort)
	quickSort(array, 0, array.length - 1);
    }
    /** This is a version of C.A.R Hoare's Quick Sort
    * algorithm.  This will handle arrays that are already
    * sorted, and arrays with duplicate keys.<BR>
    *
    * If you think of a one dimensional array as going from
    * the lowest index on the left to the highest index on the right
    * then the parameters to this function are lowest index or
    * left and highest index or right.  The first time you call
    * this function it will be with the parameters 0, a.length - 1.
    *
    * @param a       a DefaultMutableTreeNode array
    * @param lo0     left boundary of array partition
    * @param hi0     right boundary of array partition
    */
    void quickSort(DefaultMutableTreeNode a[], int lo0, int hi0) {
	int lo = lo0;
	int hi = hi0;
	int mid;

	if ( hi0 > lo0)
	    {

		/* Arbitrarily establishing partition element as the midpoint of
		 * the array.
		 */
		mid = ( lo0 + hi0 ) / 2;

		// loop through the array until indices cross
		while( lo <= hi )
		    {
			/* find the first element that is greater than or equal to
			 * the partition element starting from the left Index.
			 */

			while( ( lo < hi0 ) && ( compare(a[lo],a[mid]) > 0 ))
			    ++lo;

			/* find an element that is smaller than or equal to
			 * the partition element starting from the right Index.
			 */
			while( ( hi > lo0 ) && ( compare(a[hi],a[mid]) < 0 ))
			    --hi;

			// if the indexes have not crossed, swap
			if( lo <= hi )
			    {
				swap(a, lo, hi);
				++lo;
				--hi;
			    }
		    }

		/* If the right index has not reached the left side of array
		 * must now sort the left partition.
		 */
		if( lo0 < hi )
		    quickSort( a, lo0, hi );

		/* If the left index has not reached the right side of array
		 * must now sort the right partition.
		 */
		if( lo < hi0 )
		    quickSort( a, lo, hi0 );

	    }
    }

    private void swap(DefaultMutableTreeNode a[], int i, int j)
    {
	DefaultMutableTreeNode T;
	T = a[i];
	a[i] = a[j];
	a[j] = T;

    }

    private int compare (DefaultMutableTreeNode node1,
			 DefaultMutableTreeNode node2) {
	SearchTOCItem item1, item2;
	double confidence1, confidence2;
	int hits1, hits2;

	item1 = (SearchTOCItem) node1.getUserObject();
	confidence1 = item1.getConfidence();
	hits1 = item1.hitCount();

	item2 = (SearchTOCItem) node2.getUserObject();
	confidence2 = item2.getConfidence();
	hits2 = item2.hitCount();

	// confidence is a penality. The lower the better
	if (confidence1 > confidence2) {
	    // node1 is less than node2
	    return -1;
	} else if (confidence1 < confidence2) {
	    // node1 is greater than node2
	    return 1;
	} else {
	    // confidences are the same check the hits
	    if (hits1 < hits2) {
		// node1 is less than node2
		return -1;
	    } else if (hits1 > hits2) {
		// node2 is greater than node2
		return 1;
	    }
	}
	// nodes1 and nodes2 are equivalent
	return 0;

    }


    public void searchStarted(SearchEvent e){
        elapseTime= System.currentTimeMillis();
        logger.debug("Begine search for '"+ e.getParams()+"'");
    }
     public void searchFinished(SearchEvent e){
        logger.debug("End search for '"+ e.getParams()+"', time elapse "+
        ((System.currentTimeMillis()- elapseTime)/1000)+" s");
    }
    public Vector getNodes(){
        return nodes;
    }

  }
}
