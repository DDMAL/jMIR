/*
 * StringSortableVector.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.vectorsorter;
import java.util.*;


/**
 * A simplistic example of how the SortableVector class might be used in
 * practice. This implementation assumes that the vector is storing and sorting
 * strings. An internal class is included that implements QuicksortComparator.
 *
 * <p>This class might be used as follows, for example:
 * <p>StringSortableVector sv = new StringSortableVector();
 * <br>sv.addElement("d");
 * <br>sv.addElement("A");
 * <br>sv.addElement("C");
 * <br>sv.addElement("c");
 * <br>sv.addElement("b");
 * <br>sv.addElement("B");
 * <br>sv.addElement("D");
 * <br>sv.addElement("a");
 * <br>Enumeration e = sv.elements();
 * <br>while(e.hasMoreElements()) System.out.println(e.nextElement());
 *
 * <p>This approach to the QuickSort algorithm is derived from Bruce Eckel's
 * implementation, which can be found at: http://www.codeguru.com/java/tij/tij0091.shtml
 */
public class StringSortableVector
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The vector that holds and sorts strings.
      */
     private SortableVector<String> sortable_vector;
     
     /**
      * Whether or not sortable_vector is fully sorted.
      */
     private boolean sorted;
     
     
     /* CONSTRUCTOR ***********************************************************/
     
     
     /**
      * Initializes the object.
      */
     public StringSortableVector()
     {
          sortable_vector = new SortableVector<String>(new StringCompare());
          sorted = false;
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Adds the given string to the SortableVector stored in this object.
      *
      * @param s    The string to add.
      */
     public void addElement(String s)
     {
          sortable_vector.addElement(s);
          sorted = false;
     }
     
     
     /**
      * Returns the string stored at the given index of the SortableVector
      * stored in this object. If the object has not already been sorted,
      * then it is sorted first.
      *
      * @param index     The post-sorting index of the object to return.
      * @return          The string stored at the given index.
      */
     public String elementAt(int index)
     {
          if(!sorted)
          {
               sortable_vector.sort();
               sorted = true;
          }
          return (String)sortable_vector.elementAt(index);
     }
     
     
     /**
      * Returns an Enumeration of all elements stored in the SortableVector
      * stored in this object. Sorting is performed if it has not already
      * been performed.
      *
      * @return     The post-sorted stored strings.
      */
     public Enumeration elements()
     {
          if(!sorted)
          {
               sortable_vector.sort();
               sorted = true;
          }
          return sortable_vector.elements();
     }
     
     
     /* INTERNAL CLASS ********************************************************/
     
     
     /**
      * An implementation of the QuicksortComparator class that compares
      * strings. All strings are converted to lower case in this comparison.
      */
     private class StringCompare
          implements QuicksortComparator
     {
          /**
           * Whether or not the lowercase version of first_string is less than
           * the lowercase version of second_string.
           *
           * @param first_string   The first string to compare. Must be a
           *                       string.
           * @param second_string  The second string to compare. Must be a
           *                       string.
           * @return               0 if first and second strings are equal, a
           *                       negative integer if the first string is less
           *                       than the second or a positive integer if the 
           *                       first string is greater than the second.
           */
          public int compare(Object first_string, Object second_string)
          {
               return ((String) first_string).toLowerCase().compareTo(((String) second_string).toLowerCase());
          }
     }
}