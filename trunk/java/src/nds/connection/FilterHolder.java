package nds.connection;

import nds.util.Holder;
/**
 * Holding and initializing Filter Object
 */
public class FilterHolder extends Holder {
    private transient MessageFilter filter;

    //protected transient Class _class;

    public FilterHolder() {
    }
    public FilterHolder(String name, String classname){
        super(name,classname);
    }
    public MessageFilter getFilter(){
        return filter;
    }
    /* ------------------------------------------------------------ */
    public void stop()
    {
        if (filter!=null)
            filter.destroy();
        filter=null;
        super.stop();
    }

    /* ------------------------------------------------------------ */
    public void start()
        throws Exception
    {
        super.start();// will init _class

        if (!nds.connection.MessageFilter.class
            .isAssignableFrom(_class))
        {
            super.stop();
            throw new IllegalStateException(_class+" is not a nds.connection.MessageFilter");
        }
        filter=(MessageFilter)newInstance();
        filter.init(_initParams);
    }

    public static void main(String[] args) {
        FilterHolder filterHolder1 = new FilterHolder();
    }
}