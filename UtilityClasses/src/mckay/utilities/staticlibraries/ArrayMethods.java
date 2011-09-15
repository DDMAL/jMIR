/*
 * ArrayMethods.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.staticlibraries;

import java.util.LinkedList;


/**
 * A holder class for general static methods that can be used for processing
 * arrays.
 *
 * @author Cory McKay
 */
public class ArrayMethods
{
     /**
      * Returns a shortened array, with all entries that were set to null
      * removed. Returns null if the resulting array has no valid entries
      * or if the given array has no valid entries.
      *
      * @param	array	The array to remove null entries from.
      * @return		A shortened array with all null entries removed,
      *			or null.
      */
     public static Object[] removeNullEntriesFromArray(Object[] array)
     {
          if (array == null)
               return null;
          
          int number_null_entries = 0;
          for (int i = 0; i < array.length; i++)
               if (array[i] == null)
                    number_null_entries++;
          
          int number_valid_entries = array.length - number_null_entries;
          if (number_valid_entries == 0)
               return null;
          
          Object[] new_array = new Object[number_valid_entries];
          int current_index = 0;
          for (int i = 0; i < array.length; i++)
               if (array[i] != null)
               {
               new_array[current_index] = array[i];
               current_index++;
               }
          
          return new_array;
     }
     
     
     /**
      * Returns a copy of the given array. Note that the entries are copied by
      * reference.
      *
      * @param	given_array   The array to copy
      * @return               A copy of the given array.
      */
     public static Object[] getCopyOfArray(Object[] given_array)
     {
          Object[] new_array = new Object[given_array.length];
          for (int i = 0; i < new_array.length; i++)
               new_array[i] = given_array[i];
          return new_array;
     }
     
     
     /**
      * Returns a new array whose first part consists of the elements of
      * array_1 and whose second part consists of the elements of array_2.
      *
      * @param	array_1  The first array to concatenate.
      * @param	array_2  The second array to concatenate.
      * @return          array_1 and array_2 combined into 1 array.
      */
     public static Object[] concatenateArray(Object[] array_1, Object[] array_2)
     {
          int length_1 = array_1.length;
          int length_2 = array_2.length;
          Object[] new_array = new Object[length_1 + length_2];
          for (int i = 0; i < length_1; i++)
               new_array[i] = array_1[i];
          for (int j = 0; j < length_2; j++)
               new_array[length_1 + j] = array_2[j];
          return new_array;
     }
     
     
     /**
      * Take the given original array and return a copy of it with the elements
      * of to_insert inserted at the index_to_insert_at index of the original
      * array. The element originally present at original[index_to_insert_at] is
      * deleted if delete_index_inserted_at is true. The returned array thus
      * has size original.length + index_to_insert_at.length (- 1 if
      * delete_index_inserted_at is true).
      *
      * @param original                      The array to insert elements into.
      * @param to_insert                     The array whose elements are to be
      *                                      inserted.
      * @param index_to_insert_at            The index of original to begin
      *                                      inserting the elements of to_insert
      *                                      at.
      * @param delete_index_inserted_at      Whether to delete the element
      *                                      originally at the index_to_insert_at
      *                                      index of original.
      * @return                              A copy of original of a larger
      *                                      size with the elements inserted.
      */
     public static Object[] insertIntoArray(Object[] original,
          Object[] to_insert,
          int index_to_insert_at,
          boolean delete_index_inserted_at )
     {
          LinkedList<Object> result = new LinkedList<Object>();
          for (int i = 0; i < original.length; i++)
          {
               if (i != index_to_insert_at) result.add(original[i]);
               else
               {
                    for (int j = 0; j < to_insert.length; j++)
                         result.add(to_insert[j]);
                    if (!delete_index_inserted_at)
                         result.add(original[i]);
               }
          }
          return result.toArray(new Object[1]);
     }
     
     
     /**
      * Returns the given array of Objects as an array of Strings.
      *
      * @param to_cast The array to cast.
      * @return        The String[] version of to_cast.
      */
     public static String[] castArrayAsStrings(Object[] to_cast)
     {
          if (to_cast == null) return null;
          
          String[] results = new String[to_cast.length];
          for (int i = 0; i < to_cast.length; i++)
               results[i] = (String) to_cast[i];
          return results;
     }
}
