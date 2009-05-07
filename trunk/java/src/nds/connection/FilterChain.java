//Source file: F:\\work\\sms\\src\\nds\\connection\\FilterChain.java

package nds.connection;

import java.util.List;

import nds.util.LazyList;

/**
 * Created by connection (handler) according to it's property named "filter", if
 * obmitted, using securty filter, Count filter etc
 */
public class FilterChain
{
   int pos=0;
   List filters;

   /**
    * @param filters List or null, when List, elements are FilterHolder
    *
    * @roseuid 404A97ED0375
    */
   public FilterChain(List filters)
   {
       this.filters=filters;
   }
   public FilterChain duplicate(){
       return new FilterChain(filters);
   }
   /**
    * @param request
    * @param response
    * @roseuid 4048CBBD0283
    */
   public Message doFilter(Message request)
   {
       // pass to next filter
       if (pos<LazyList.size(filters))
       {
           FilterHolder holder = (FilterHolder)LazyList.get(filters,pos++);
           MessageFilter filter = holder.getFilter();
           return filter.doFilter(request,this);
       }else
           return request;

       // Call servlet
     /*  if (_servletHolder!=null)
       {
           if (Code.verbose()) Code.debug("call servlet ",_servletHolder);
           _servletHolder.handle(request,response);
       }
       else // Not found
           notFound((Message)request,
                    (Message)response);
*/
   }
}
