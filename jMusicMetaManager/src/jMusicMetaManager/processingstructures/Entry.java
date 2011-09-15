/*
 * Entry.java
 * Version 1.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jMusicMetaManager.processingstructures;

import java.util.Vector;


/**
 * A data structure to hold the name of a key and the indexes in some external
 * data structure that correspond to this key. An Entry is constructed with
 * only one index, but an Entry can later be merged (using the merge method)
 * with another entry that has the same key, and their indexes will be combined.
 *
 * <p>Note that it is possible that the value field may be set to null by some
 * calling methods for various reasons.
 *
 * @author Cory McKay
 */
public class Entry
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The key value stored by this Entry. Typically, a value of null
      * corresponds to unknown.
      */
     private   String              value;
     
     /**
      * The indexes to which value corresponds to in some external data
      * structure.
      */
     private   Vector<Integer>     indexes;
     
     
     /* CONSTRUCTOR ***********************************************************/
     
     
     /**
      * Creates a new instance of Entry.
      *
      * @param value     The key to store in this entry.
      * @param index     The original index that corresponds to value in some
      *                  external data structure.
      */
     public Entry(String value, int index)
     {
          this.value = value;
          indexes = new Vector<Integer>();
          indexes.add(new Integer(index));
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Returns the stored key value for this Entry.
      *
      * @return     The stored value.
      */
     public String getValue()
     {
          return value;
     }
     
     
     /**
      * Returns the stored index values for this Entry as an array of ints.
      *
      * @return     The stored index values.
      */
     public int[] getIndexes()
     {
          Integer[] temp = getIntegerIndexes();
          int[] to_return = new int[temp.length];
          for (int i = 0; i < to_return.length; i++)
               to_return[i] = temp[i].intValue();
          return to_return;
     }
     
     
     /**
      * Returns the stored index values for this Entry as an array of Integers.
      *
      * @return     The stored index values.
      */
     public Integer[] getIntegerIndexes()
     {
          return indexes.toArray(new Integer[1]);
     }
     
     
     /**
      * Sets the value field to the given new_value.
      *
      * @param new_value The new value for the value field.
      */
     public void setValue(String new_value)
     {
          value = new_value;
     }
     
     
     /**
      * Adds the indexes of the given Entry object to this Entry object. If
      * the use_given value parameter is true, then the value field of this
      * Entry object is replaced by the value field of the given Entry object.
      *
      * @param given_entry         The Entry object whose fields are to be
      *                            merged with this one's.
      * @param use_given_value     Whether or not the given Entry object's
      *                            value field should replace this one's.
      */
     public void merge(Entry given_entry, boolean use_given_value)
     {
          Integer[] new_indexes = given_entry.getIntegerIndexes();
          for (int i = 0; i < new_indexes.length; i++)
               indexes.add(new_indexes[i]);
          if (use_given_value)
               value = given_entry.value;
     }
     
     
     /**
      * Checks the given Entry to see if it has the same value field as this
      * Entry. If it does not, then nothing is done. If both Entry objects
      * have the same value field, then the indexes field of the passed entry
      * are added to the indexes field of this array.
      *
      * <p>False is also returned and no change is made if one Entry object
      * has a null value field. A merge is performed and true is returned,
      * however, if both Entry objects have null value fields.
      *
      * @param entry     The Entry to compare with this Entry, whose indexes
      *                  may be added to this Entry's indexes if a merge occurs.
      * @return          True if both Entry objects have the same value field,
      *                  false if they differ or if one but not both value
      *                  fields are null.
      */
     public boolean mergeIfIdentical(Entry entry)
     {
          if (value == null || entry.getValue() == null)
          {
               if (value == null && entry.getValue() == null)
               {
                    merge(entry, false);
                    return true;
               }
               else return false;
          }
          else if (!value.equals(entry.getValue()))
               return false;
          else
          {
               merge(entry, false);
               return true;
          }
     }
}