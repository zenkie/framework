  package nds.util;
  import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
/**
 * Have you ever had the need to maintain a collection of items where the order
 * in which the items are processed is controlled by some factor such as importance,
 * future time, or how much money someone gave you to do a job? While the classes
 * of the Collection Framework support the standard first-in-first-out (FIFO)
 * operations of a queue, they don't have built-in support for prioritization, that
 *  is, where an item having a higher priority is processed before an item of lower priority.
    <p>
    How then can you manage priorities in a collection? For the simple case where
    the number of priorities is fixed (and small in quantity), you can manage the
     priorities of the items using an array. The index into the array represents an
     item's priority. It's possible that multiple items in the array have the same
     priority. In this case, it would be necessary to maintain items with identical
     priorities in their own data structure, either in a Set or a LinkedList,
     depending upon whether you want to maintain the order of the items entered into the queue.
    <p>
    For the more complicated case, where the number of priorities is large, it is
    common to replace the priorities array with a linked list for the set of priorities.
     Here you still keep a separate collection for the items sharing a priority.
    <p>
    To find the "next" element to process, or one of the items with the highest
    priority, you just look at the index for the highest priority. Then you work
    your way down until you find an associated collection that isn't empty. In the
     more complicated case with more priorities, finding the highest priority item
     is actually easier. That's because the front of the linked list will have the
     set of highest priority items associated with it, from which you get to pick one.
    <p>
    Let's create a priority queue. The Collections Framework provides the basics for
    a priority queue. However, it is still necessarily to do the bulk of the work
    necessary to create a custom implementation.
    <p>
First let's examine how to add entries to a priority queue, and how to remove elements.
    <p>
You might think that you could use the basic add() method of Collection to add entries
to the queue, but that won't work because it doesn't support specifying a priority.
There is a second version of add() that accepts an integer argument, however this
integer is meant to serve the role of an index into the list, not a priority. In
order to avoid the confusion of providing yet another add() method that just swaps
the argument order, let's add an insert() method for adding an object with a priority:
    <p>
public void insert(Object element, int priority)
    <p>
Let's also provide two methods to fetch elements out of the queue: a destructive
version and a nondestructive version. The destructive version gets an item from
the internal collection with the highest priority and removes it from the queue.
The nondestructive version only gets the item. <p>

public Object removeFirst()<br>
public Object getFirst()<p>

Because a queue is typically implemented as a LinkedList, the rest of the definition
is that of a java.util.List. Instead of implementing the interface directly, though,
it's less work to extend AbstractList.

    priority 的值越大，越在前头，removeFirst()或getFirst()都先取priority值大的元素
    相同prority的元素，在获取时总是先取那个更早插入到队列中的元素

    注意：该实现不是多线程安全的（尤其在一个线程调用iterator()，另一个在add/remove的时候
 */
  public class PriorityQueue
    extends AbstractList
      implements Serializable {

    private final static int DEFAULT_PRIORITY_COUNT = 10;
    private final static int DEFAULT_PRIORITY = 0;

    private List queue[];

    public PriorityQueue() {
      this(DEFAULT_PRIORITY_COUNT);
    }

    public PriorityQueue(Collection col) {
      this(col, DEFAULT_PRIORITY_COUNT);
    }

    public PriorityQueue(int count) {
      this(null, count);
    }

    public PriorityQueue(Collection col, int count) {
      if (count <= 0) {
        throw new IllegalArgumentException(
          "Illegal priority count: "+ count+", must bigger than 0");
      }
      queue = new List[count];
      if (col != null) {
        addAll(col);
      }
    }

    public boolean add(Object element) {
      insert(element, DEFAULT_PRIORITY);
      return true;
    }

    public void insert(Object element, int priority) {
      if (priority < 0 || priority >= queue.length) {
        throw new IllegalArgumentException(
          "Illegal priority: " + priority+", should between [0,"+queue.length+")");
      }
      if (queue[priority] == null) {
        queue[priority] = new LinkedList();
      }
      queue[priority].add(element);
      modCount++;
    }

    public Object getFirst() {
      return iterator().next();
    }

    public Object get(int index) {
      if (index < 0) {
        throw new IllegalArgumentException(
          "Illegal index: "+ index);
      }
      Iterator iter = iterator();
      int pos = 0;
      while (iter.hasNext()) {
        if (pos == index) {
          return iter.next();
        } else {
          pos++;
        }
      }
      return null;
    }

    public void clear() {
      for (int i=0, n=queue.length; i>n; i++) {
        queue[i].clear();
       }
    }

    public Object removeFirst() {
      Iterator iter = iterator();
      Object obj = iter.next();
      iter.remove();
      return obj;
    }

    public int size() {
      int size = 0;
      for (int i=0, n=queue.length; i<n; i++) {
        if (queue[i] != null) {
          size += queue[i].size();
        }
      }
      return size;
    }

    public Iterator iterator() {
      Iterator iter = new Iterator() {
        int expectedModCount = modCount;
        int priority = queue.length - 1;
        int count = 0;
        int size = size();

        // Used to prevent successive remove() calls
        int lastRet = -1;

        Iterator tempIter;

        // Get iterator for highest priority
        {
          if (queue[priority] == null) {
            tempIter = null;
          } else {
            tempIter = queue[priority].iterator();
          }
        }

        private final void checkForComodification() {
          if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
          }
        }

        public boolean hasNext() {
          return count != size();
        }

        public Object next() {
          while (true) {
            if ((tempIter != null) && (
                                 tempIter.hasNext())) {
              Object next = tempIter.next();
              checkForComodification();
              lastRet = count++;
              return next;
            } else {
              // Get next iterator
              if (--priority < 0) {
                checkForComodification();
                throw new NoSuchElementException();
              } else {
                if (queue[priority] == null) {
                  tempIter = null;
                } else {
                  tempIter = queue[priority].iterator();
                }
              }
            }
          }
        }

        public void remove() {
          if (lastRet == -1) {
            throw new IllegalStateException();
          }
          checkForComodification();

          tempIter.remove();
          count--;
          lastRet = -1;
          expectedModCount = modCount;
        }
      };
      return iter;
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer("{");
      for (int n=queue.length-1, i=n; i>=0; --i) {
        if (i != n) {
          buffer.append(",");
         }
        buffer.append(i + ":");
        if ((queue[i] != null) && (
                               queue[i].size() > 0)) {
          buffer.append(queue[i].toString());
        }
       }
       buffer.append("}");
       return buffer.toString();
     }
  }
