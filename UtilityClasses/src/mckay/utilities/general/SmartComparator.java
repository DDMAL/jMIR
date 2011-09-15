/*
 * SmartComparator.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.general;

import java.text.Collator;
import java.util.Comparator;


/**
 * A Comparator that can be used for making comparisons in tasks such as
 * sorting. Performs numerical comparisons for integers, shorts, longs, doubles
 * and floats and performs string comparisons for all other types of objects,
 * where case is ignored and a Collator for the default location is used.
 *
 * @author Cory McKay
 */
public class SmartComparator
     implements Comparator
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * Performs actual comparisons for strings.
      */
     private	Collator	string_comparer;
     
     
     /* CONSTRUCTOR ***********************************************************/
     
     
     /**
      * Basic constructor that stores the settings for the default locale.
      */
     public SmartComparator()
     {
          string_comparer = Collator.getInstance();
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Compares the two arguments for order. Returns a negative integer, zero,
      * or a positive integer as the first argument is less than, equal to, or
      * greater than the second argument. If the arguments are both Integers,
      * Longs, Shorts, Floats, Doubles or Booleans, then they are compared based
      * on the wrapped baseic data type. Any other objects are compared to as
      * strings, with the order determined using a Collator with the default
      * Locale. Case is not considered in the comparison, however, as is
      * normally the case with Collators.
      *
      * @param  a   The first item to compare.
      * @param  b   The second item to compare.
      * @return     The comparison result.
      */
     public int compare(Object a, Object b)
     {
          if (a instanceof Integer && b instanceof Integer)
               return compare(((Integer) a).intValue(), ((Integer) b).intValue());
          else if (a instanceof Long && b instanceof Long)
               return compare(((Long) a).longValue(), ((Long) b).longValue());
          else if (a instanceof Short && b instanceof Short)
               return compare(((Short) a).shortValue(), ((Short) b).shortValue());
          else if (a instanceof Double && b instanceof Double)
               return compare(((Double) a).doubleValue(), ((Double) b).doubleValue());
          else if (a instanceof Float && b instanceof Float)
               return compare(((Float) a).floatValue(), ((Float) b).floatValue());
          else
               return compare(a.toString(), b.toString());
     }
     
     
     /**
      * Compares the two arguments for order. Returns a negative integer, zero,
      * or a positive integer as the first argument is less than, equal to, or
      * greater than the second argument. The order is determined using a
      * Collator with the default Locale. Case is not considered in the
      * comparison, however, as is normally the case with Collators.
      *
      * @param  a   The first item to compare.
      * @param  b   The second item to compare.
      * @return     The comparison result.
      */
     public int compare(String a, String b)
     {
          String a_lower_case = (new String(a)).toLowerCase();
          String b_lower_case = (new String(b)).toLowerCase();
          return string_comparer.compare(a_lower_case, b_lower_case);
     }
     
     
     /**
      * Compares the two arguments for order. Returns a negative integer, zero,
      * or a positive integer as the first argument is less than, equal to, or
      * greater than the second argument.
      *
      * @param  a   The first item to compare.
      * @param  b   The second item to compare.
      * @return     The comparison result.
      */
     public int compare(int a, int b)
     {
          if (a < b)
               return -1;
          else if (a > b)
               return 1;
          else return 0;
     }
     
     
     /**
      * Compares the two arguments for order. Returns a negative integer, zero,
      * or a positive integer as the first argument is less than, equal to, or
      * greater than the second argument.
      *
      * @param  a   The first item to compare.
      * @param  b   The second item to compare.
      * @return     The comparison result.
      */
     public int compare(long a, long b)
     {
          if (a < b)
               return -1;
          else if (a > b)
               return 1;
          else return 0;
     }
     
     
     /**
      * Compares the two arguments for order. Returns a negative integer, zero,
      * or a positive integer as the first argument is less than, equal to, or
      * greater than the second argument.
      *
      * @param  a   The first item to compare.
      * @param  b   The second item to compare.
      * @return     The comparison result.
      */
     public int compare(short a, short b)
     {
          if (a < b)
               return -1;
          else if (a > b)
               return 1;
          else return 0;
     }
     
     
     /**
      * Compares the two arguments for order. Returns a negative integer, zero,
      * or a positive integer as the first argument is less than, equal to, or
      * greater than the second argument.
      *
      * @param  a   The first item to compare.
      * @param  b   The second item to compare.
      * @return     The comparison result.
      */
     public int compare(double a, float b)
     {
          if (a < b)
               return -1;
          else if (a > b)
               return 1;
          else return 0;
     }
     
     
     /**
      * Compares the two arguments for order. Returns a negative integer, zero,
      * or a positive integer as the first argument is less than, equal to, or
      * greater than the second argument.
      *
      * @param  a   The first item to compare.
      * @param  b   The second item to compare.
      * @return     The comparison result.
      */
     public int compare(float a, float b)
     {
          if (a < b)
               return -1;
          else if (a > b)
               return 1;
          else return 0;
     }
     
     
     /**
      * Compares the two arguments for order. Returns a negative integer, zero,
      * or a positive integer as the first argument is less than, equal to, or
      * greater than the second argument. False is considered to be before True.
      *
      * @param  a   The first item to compare.
      * @param  b   The second item to compare.
      * @return     The comparison result.
      */
     public int compare(boolean a, boolean b)
     {
          if (a == b)
               return 0;
          else if (b)
               return 1;
          else return -1;
     }
     
     
     /**
      * Indicates whether the given object is "equal to" this Comparator. It is
      * considered equal if it is a SmartComparator and it has an equal
      * Collator for use with strings.
      *
      * @param  object   The object to check for equality.
      * @return          Whether there is equality.
      */
     public boolean equals(Object object)
     {
          if (!(object instanceof SmartComparator))
               return false;
          return ((SmartComparator) object).getCollator().equals(string_comparer);
     }
     
     
     /**
      * Get the Collator used by this object.
      *
      * @return The Collator used by this object.
      */
     public Collator getCollator()
     {
          return string_comparer;
     }
}
