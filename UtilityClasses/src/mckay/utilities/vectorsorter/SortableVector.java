/*
 * SortableVector.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.vectorsorter;
import java.util.*;


/**
 * A Vector that includes a generalized implementation of the Quicksort
 * algorithm. A SortableVector will sort its own contents when its sort method
 * is called.
 *
 * <p>An implementation of the QuicksortComparator interface is needed in order
 * to determine on what basis the vector is to be sorted. This
 * QuicksortComparator implementation is passed to the SortableVector during
 * construction.
 *
 * <p>The StringSortableVector class in this package provides an example of how
 * the this SortableVector class can be used in practice.
 *
 * <p>This approach to the Quicksort algorithm is derived from Bruce Eckel's
 * implementation, which can be found at: http://www.codeguru.com/java/tij/tij0091.shtml
 */
public class SortableVector<E>
     extends Vector<E>
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * Performs comparisons.
      */
     private QuicksortComparator comparator;
     
     /**
      * An identifier for use in serialization.
      */
     private static final long serialVersionUID = 53006L;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Stores the given QuicksortComparator object that will compare the
      * elements during ordering.
      *
      * @param comparator   Will evaluate relative values of elements during
      *                     sorting.
      */
     public SortableVector(QuicksortComparator comparator)
     {
          super();
          this.comparator = comparator;
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Iniates sorting of this Vector using the Quicksort algorithm.
      */
     public void sort()
     {
          quickSort();
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Sorts this Vector non-recursively using the Quicksort algorithm
      * (Sedgwick's implementation, which breaks it into sub-problems).
      */
     private void quickSort()
     {
          int[] problem_stack = new int[10000];
          
          int left = 0;
          int right = size() - 1;
          int problem = 2;
          
          while(problem != 0)
          {
               if(right > left)
               {
                    // Find pivot and move elements
                    int pivot = partition(left, right);
                    
                    // Now that there are two sub-problems, push the long one
                    if((pivot - left) > (right - pivot))
                    {
                         problem_stack[problem] = left;
                         problem_stack[problem + 1] = pivot - 1;
                         left = pivot + 1;
                    }
                    else
                    {
                         problem_stack[problem] = pivot + 1;
                         problem_stack[problem + 1] = right;
                         right = pivot - 1;
                    }
                    
                    // Move on to the next problem
                    problem += 2;
               }
               else
               {
                    // Finished, so do a sub-problem
                    problem -= 2;
                    left = problem_stack[problem];
                    right = problem_stack[problem + 1];
               }
          }
     }
     
     
     
     
     private int partition(int left, int right)
     {
          // Move the median element to the front
          int median = (left+right)/2;
          
          order(left, median);
          order(median, right);
          order(median, left);
          E pivot_element = get(left);
          
          int known_low = left;
          int unknown = left + 1;
          while(unknown <= right)
          {
               if(comparator.compare(get(unknown), pivot_element) < 0)
               {
                    known_low++;
                    swap(known_low, unknown);
               }
               unknown++;
          }
          
          // Put pivot_element in place
          swap(left, known_low);
          
          return known_low;
     }
     
     
     /**
      * Compares the element at the first index with the element at the second
      * index, and swaps their positions if they are out of order.
      *
      * @param first     The index in this vector of the first object to
      *                  check.
      * @param second    The index in this vector of the second object to
      *                  check.
      */
     private void order(int first, int second)
     {
          if (comparator.compare(get(first), get(second)) > 0)
               swap(first, second);
     }
     
     
     /**
      * Swaps the position of the two objects at the given indices in this
      * SortableVector.
      *
      * @param first     The index in this vector of the first object to
      *                  swap.
      * @param second    The index in this vector of the second object to
      *                  swap.
      */
     private void swap(int first, int second)
     {
          E temp = elementAt(first);
          setElementAt(elementAt(second), first);
          setElementAt(temp, second);
     }
}