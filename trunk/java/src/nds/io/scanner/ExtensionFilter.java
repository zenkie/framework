/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package nds.io.scanner;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import nds.io.protocol.URLLister;

/**
 * <p>A simple filter to for the URLDeploymentScanner.  Three arrays are
 * maintained for checking: a prefix, suffix, and match array.  If the
 * filename starts with any of the prefixes, ends with any of the
 * suffixes, or exactly matches any of the matches, then the accepts
 * method will return false.
 */
public class ExtensionFilter implements FileFilter, URLLister.URLFilter
{

    /**
     * Compare the strings backwards.  This assists in suffix comparisons.
     */
    private static final Comparator reverseComparator = new Comparator()
    {
       public int compare(Object o1, Object o2)
       {
          int idx1 = ((String) o1).length();
          int idx2 = ((String) o2).length();
          int comp = 0;

          while (comp == 0 && idx1 > 0 && idx2 > 0)
             comp = ((String) o1).charAt(--idx1) - ((String) o2).charAt(--idx2);

          return (comp == 0) ? (idx1 - idx2) : comp;
       }
    };

   /** The sorted list of disallowed values */
   private ArrayList matches;

   /** @param match specified extension such as ".txt", ".log", contains "." */
   public ExtensionFilter(String match)
   {
      this(new String[]{match});
   }

   /**
    * Create using a custom set of matches, prefixes, and suffixes.  If any of
    * these arrays are null, then the corresponding default will be
    * substituted.
    */
   public ExtensionFilter(String[] matches)
   {
      this.matches = new ArrayList(Arrays.asList(matches));
   }

   public void addExtension(String ext)
   {
      this.matches.add(ext);
      Collections.sort(this.matches);
   }
   public void addExtensions(String[] exts)
   {
      this.matches.add(Arrays.asList(exts));
      Collections.sort(this.matches);
   }

   /**
    * If the filename matches any string in the prefix, suffix, or matches
    * array, return false.  Perhaps a bit of overkill, but this method
    * operates in log(n) time, where n is the size of the arrays.
    *
    * @param  file  The file to be tested
    * @return  <code>false</code> if the filename matches any of the prefixes,
    *          suffixes, or matches.
    */
   public boolean accept(File file)
   {
      return accept(file.getName());
   }

   public boolean accept(URL baseURL, String memberName)
   {
      return accept(memberName);
   }

   private boolean accept(String name)
   {
      // check exact match
      int index = Collections.binarySearch(matches, name);
      if (index >= 0)
         return true;

      // check suffix
      index = Collections.binarySearch(matches, name, reverseComparator);
      if (index >= 0)
         return true;
      if (index < -1)
      {
         // The < 0 index gives the first index greater than name
         int firstLessIndex = -2 - index;
         String suffix = (String) matches.get(firstLessIndex);
         // If name ends with an ingored suffix ignore name
         if( name.endsWith(suffix) )
            return true;
      }

      // everything checks out.
      return false;
   }
}
