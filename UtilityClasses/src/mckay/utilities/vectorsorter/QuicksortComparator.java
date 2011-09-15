/*
 * QuicksortComparator.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.vectorsorter;


/**
 * An interface intended to be implemented for use with the SortableVector
 * Quicksort implementation. A class that implements this interface will
 * perform the coparisons that are made during the sorting process.
 *
 * <p>This approach to the Quicksort algorithm is derived from Bruce Eckel's
 * implementation, which can be found at: http://www.codeguru.com/java/tij/tij0091.shtml
 *
 * @author Cory McKay
 */
public interface QuicksortComparator
{
     /**
      * Tests two objects to see whether the are "equal" and, if not, which is
      * "greater" than the other (in whatever sense is relevant).
      *
      * @param first     The object to compare to second.
      * @param second    The object to compare to first.
      * @return          0 if first and second are equal, a negative integer if
      *                  first is less than second or a positive integer if
      *                  first is greater than second.
      */
     public int compare(Object first, Object second);
}
