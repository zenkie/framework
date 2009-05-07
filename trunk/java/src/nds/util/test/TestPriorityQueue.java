  package nds.util.test;
  import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import nds.util.PriorityQueue;
  /**
  *  java TestPriorityQueue one two three four
  */
  public class TestPriorityQueue {
    public static void main (String args[]) {
      //vif( args==null)
      args=new String[]{"one","two","three","four"}  ;
      List list = Arrays.asList(args);
      PriorityQueue queue = new PriorityQueue(list);
      System.out.println(queue);
      queue = new PriorityQueue(10);
      try {
        System.out.println(queue.removeFirst());
      } catch (NoSuchElementException e) {
        System.out.println(
                        "Received expected exception");
      }
      queue.insert("Joy", 8);
      queue.insert("Scott", 9);
      queue.insert("Sueltz", 5);
      queue.insert("Bill", 8);
      queue.insert("McNealy", 9);
      queue.insert("Patricia", 5);
      queue.insert("C.", 5);
      queue.insert("Papadopoulos", 4);
      queue.insert("Greg", 4);
      System.out.println(queue);
      queue.addAll(list);
      System.out.println(queue);
      while (queue.size() != 0) {
        System.out.println(queue.removeFirst());
      }
    }
  }

