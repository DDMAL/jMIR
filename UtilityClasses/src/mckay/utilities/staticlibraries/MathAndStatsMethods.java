/*
 * MathAndStatsMethods.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.staticlibraries;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * A holder class for static methods relating to statistical and mathematical
 * analysis.
 *
 * @author Cory McKay
 */
public class MathAndStatsMethods
{
     /**
      * Returns the percentage of the given total that the given value
      * represents.
      *
      * @param value     The numerator in the percentage calculation.
      * @param total     The denominator in the percentage calculation.
      * @return          The percentage.
      */
     public static double getPercentage(int value, int total)
     {
          return 100.0 * ((double) value) / ((double) total);
     }
     
     
     /**
      * Returns the average of a set of doubles.
      * Returns 0 if the length of the data is 0.
      *
      * @param	data     The data to be averaged.
      * @return          The mean of the given data.
      */
     public static double getAverage(double[] data)
     {
          if (data.length < 1)
               return 0.0;
          double sum = 0.0;
          for	(int i = 0; i < data.length; i++)
               sum = sum + data[i];
          return (sum / ((double) data.length));
     }
     
     
     /**
      * Returns the average of a set of ints.
      * Returns 0 if the length of the data is 0.
      *
      * @param	data     The data to be averaged.
      * @return          The mean of the given data.
      */
     public static double getAverage(int[] data)
     {
          if (data.length < 1)
               return 0.0;
          double sum = 0.0;
          for	(int i = 0; i < data.length; i++)
               sum = sum + (double) data[i];
          return (sum / ((double) data.length));
     }
     
     
     /**
      * Returns the standard deviation of a set of doubles.
      * Returns 0 if there is only one piece of data.
      *
      * @param	data     The data for which the standard deviation is to be
      *                  found.
      * @return          The standard deviation of the given data.
      */
     public static double getStandardDeviation(double[] data)
     {
          if (data.length  < 2)
               return 0.0;
          double average = getAverage(data);
          double sum = 0.0;
          for (int i = 0; i < data.length; i++)
          {
               double diff = data[i] - average;
               sum = sum + diff * diff;
          }
          return Math.sqrt(sum / ((double) (data.length - 1)));
     }
     
     
     /**
      * Returns the standard deviation of a set of ints.
      * Returns 0 if there is only one piece of data.
      *
      * @param	data     The data for which the standard deviation is to be
      *                  found.
      * @return          The standard deviation of the given data.
      */
     public static double getStandardDeviation(int[] data)
     {
          if (data.length  < 2)
               return 0.0;
          double average = getAverage(data);
          double sum = 0.0;
          for (int i = 0; i < data.length; i++)
          {
               double diff = ((double) data[i]) - average;
               sum = sum + diff * diff;
          }
          return Math.sqrt(sum / ((double) (data.length - 1)));
     }
     
     
     /**
      * Calculates the factorial of the given value.
      *
      * @param      n         The number to find the factorial of.
      * @return               The factorial.
      * @throws     Exception Throws an informative exception if n is below 0
      *                       or greater than 20.
      */
     public static long getFactorial(long n)
     throws Exception
     {
          if (n < 0) throw new Exception("Factorial input of " + n + " invalid: Must be 0 or greater.");
          else if (n > 20) throw new Exception("Factorial input of " + n + " invalid: Must be 20 or less.");
          else if (n == 0) return 1;
          else return n * getFactorial(n - 1);
     }
     
     
     /**
      * Returns the index of the entry of an array of doubles with the smallest
      * value. The first occurence is returned in the case of a tie.
      */
     public static int getIndexOfSmallest(double[] values)
     {
          int min_index = 0;
          for (int i = 0; i < values.length; i++)
               if (values[i] < values[min_index])
                    min_index = i;
          return min_index;
     }
     
     
     /**
      * Returns the index of the entry of an array of doubles with the smallest
      * value. The first occurence is returned in the case of a tie.
      */
     public static int getIndexOfSmallest(int[] values)
     {
          int min_index = 0;
          for (int i = 0; i < values.length; i++)
               if (values[i] < values[min_index])
                    min_index = i;
          return min_index;
     }
     
     
     /**
      * Returns the index of the entry of an array of doubles that corresponds.
      * to the median entry. Returns -1 if a problem occurs.
      */
     public static int getIndexOfMedian(double[] values)
     {
          double[] copy = new double[values.length];
          for (int i = 0; i < values.length; i++)
               copy[i] = values[i];
          
          Arrays.sort(copy);
          
          int centre = copy.length / 2;
          
          double median = copy[centre];
          
          for (int i = 0; i < values.length; i++)
               if (values[i] == median)
                    return i;
          
          return -1;
     }
     
     
     /**
      * Returns the index of the entry of an array of floats with the largest
      * value. The first occurence is returned in the case of a tie.
      */
     public static int getIndexOfLargest(float[] values)
     {
          int max_index = 0;
          for (int i = 0; i < values.length; i++)
               if (values[i] > values[max_index])
                    max_index = i;
          return max_index;
     }
     
     
     /**
      * Returns the index of the entry of an array of doubles with the largest
      * value. The first occurence is returned in the case of a tie.
      */
     public static int getIndexOfLargest(double[] values)
     {
          int max_index = 0;
          for (int i = 0; i < values.length; i++)
               if (values[i] > values[max_index])
                    max_index = i;
          return max_index;
     }
     
     
     /**
      * Returns the index of the entry of an array of itegers with the largest
      * value. The first occurence is returned in the case of a tie.
      */
     public static int getIndexOfLargest(int[] values)
     {
          int max_index = 0;
          for (int i = 0; i < values.length; i++)
               if (values[i] > values[max_index])
                    max_index = i;
          return max_index;
     }
     
     
     /**
      * Returns a copy of the given array, but with any duplicate entries
      * removed (e.g. {1, 3, 5, 3, 4} would return {1, 3, 5, 4}).
      *
      * @param  to_check The array to remove double entries from.
      * @return          Returns the array with double entries removed.
      */
     public static int[] removeRedundantEntries(int[] to_check)
     {
          boolean[] is_double = new boolean[to_check.length];
          for (int i = 0; i < is_double.length; i++)
               is_double[i] = false;
          int doubles_found = 0;
          for (int i = 0; i < to_check.length - 1; i++)
               if (!is_double[i])
                    for (int j = i + 1; j < to_check.length; j++)
                         if (to_check[j] == to_check[i])
                         {
               doubles_found++;
               is_double[j] = true;
                         }
          
          if (doubles_found > 0)
          {
               int[] to_return = new int[to_check.length - doubles_found];
               int current = 0;
               for (int i = 0; i < to_check.length; i++)
               {
                    if (!is_double[i])
                    {
                         to_return[current] = to_check[i];
                         current++;
                    }
               }
               return to_return;
          }
          else return to_check;
     }
     
     
     /**
      * Returns the Euclidian distance between x and y. Throws an exception if x
      * and y have different sizes.
      */
     public static double calculateEuclideanDistance(double[] x, double[] y)
     throws Exception
     {
          if (x.length != y.length)
               throw new Exception("The two given arrays have different sizes.");
          
          double total = 0.0;
          for (int dim = 0; dim < x.length; dim++)
               total += Math.pow( (x[dim] - y[dim]), 2 );
          return Math.sqrt(total);
     }
     
     
     /**
      * Returns a random integer from 0 to max - 1, based on the uniform
      * distribution.
      */
     public static int generateRandomNumber(int max)
     {
          int random_number = (int) ( ((double) Integer.MAX_VALUE) * Math.random() );
          return (random_number % max);
     }
     
     
     /**
      * Returns an array <i>number_entries</i> arrays. Each entry has a value
      * between 0 and <i>number_entries</i> - 1, and no numbers are repeated.
      * Ordering of numbers is random.
      */
     public static int[] getRandomOrdering(int number_entries)
     {
          // Generate an array of random numbers
          double[] random_values = new double[number_entries];
          for (int i = 0; i < random_values.length; i++)
               random_values[i] = Math.random();
          
          // Fill in the array to return and return it
          int[] scrambled_values = new int[number_entries];
          for (int i = 0; i < scrambled_values.length; i++)
          {
               int largest_index = getIndexOfLargest(random_values);
               scrambled_values[i] = largest_index;
               random_values[largest_index] = -1.0; // to avoid double counting
          }
          return scrambled_values;
     }
     
     
     /**
      * Returns the sum of the contents of all of the entries of the given
      * array.
      */
     public static double getArraySum(double[] to_sum)
     {
          double sum = 0.0;
          for (int i = 0; i < to_sum.length; i++)
               sum += to_sum[i];
          return sum;
     }
     
     
     /**
      * Return a normalized copy of the the given array. The original array is
      * not altered.
      */
     public static double[] normalize(double[] to_normalize)
     {
          // Copy the to_normalize array
          double[] normalized = new double[to_normalize.length];
          for (int i = 0; i < normalized.length; i++)
               normalized[i] = to_normalize[i];
          
          // Perform the normalization
          double sum = getArraySum(normalized);
          for (int i = 0; i < normalized.length; i++)
          {
               if (sum == 0.0) normalized[i] = 0.0;
               else normalized[i] = normalized[i] / sum;
          }
          
          // Return the normalized results
          return normalized;
     }
     
     
     /**
      * Return a normalized copy of the the given array. Normalization is
      * performed by row (i.e. the sum of each row (first indice) is one after
      * normalization). Each row is independant. The original array is not
      * altered.
      */
     public static double[][] normalize(double[][] to_normalize)
     {
          // Copy the to_normalize array
          double[][] normalized = new double[to_normalize.length][];
          for (int i = 0; i < normalized.length; i++)
          {
               normalized[i] = new double[to_normalize[i].length];
               for (int j = 0; j < normalized[i].length; j++)
                    normalized[i][j] = to_normalize[i][j];
          }
          
          // Perform the normalization
          double[] totals = new double[normalized.length];
          for (int i = 0; i < normalized.length; i++)
          {
               totals[i] = 0.0;
               for (int j = 0; j < normalized[i].length; j++)
                    totals[i] += normalized[i][j];
          }
          for (int i = 0; i < normalized.length; i++)
               for (int j = 0; j < normalized[i].length; j++)
               {
                    if (totals[i] == 0.0) normalized[i][j] = 0.0;
                    else normalized[i][j] = normalized[i][j] / totals[i];
               }
          
          // Return the normalized results
          return normalized;
     }
     
     
     /**
      * Return a normalized copy of the the given array. Normalization is
      * performed overall so that the sum of all entries is 1.0. The original
      * array is not altered.
      */
     public static double[][] normalizeEntirely(double[][] to_normalize)
     {
          // Find the sum of all entries
          double sum = 0.0;
          for (int i = 0; i < to_normalize.length; i++)
               for (int j = 0; j < to_normalize[i].length; j++)
                    sum += to_normalize[i][j];
          
          // Make the normalized copy
          double[][] normalized = new double[to_normalize.length][];
          for (int i = 0; i < to_normalize.length; i++)
          {
               normalized[i] = new double[to_normalize[i].length];
               for (int j = 0; j < to_normalize[i].length; j++)
               {
                    if (sum == 0.0) normalized[i][j] = 0.0;
                    else normalized[i][j] = to_normalize[i][j] / sum;
               }
          }
          
          // Return the normalized results
          return normalized;
     }
     
     
     /**
      * Returns the given a raised to the power of the given b.
      *
      * <p><b>IMPORTANT:</b> b must be greater than zero.
      *
      * @param	a	The base.
      * @param	b	The exponent.
      */
     public static int pow(int a, int b)
     {
          int result = a;
          for (int i = 1; i < b; i++)
               result *= a;
          return result;
     }
     
     
     /**
      * Returns the logarithm of the specified base of the given number.
      *
      * <p><b>IMPORTANT:</b> Both x and n must be greater than zero.
      *
      * @param	x	The value to find the log of.
      * @param	n	The base of the logarithm.
      */
     public static double logBaseN(double x, double n)
     {
          return (Math.log10(x) / Math.log10(n));
     }
     
     
     
     /**
      * If the given x is a power of the given n, then x is returned.
      * If not, then the next value above the given x that is a power
      * of n is returned.
      *
      * <p><b>IMPORTANT:</b> Both x and n must be greater than zero.
      *
      * @param	x	The value to ensure is a power of n.
      * @param	n	The power to base x's validation on.
      */
     public static int ensureIsPowerOfN(int x, int n)
     {
          double log_value = logBaseN((double) x, (double) n);
          int log_int = (int) log_value;
          int valid_size = pow(n, log_int);
          if (valid_size != x)
               valid_size = pow(n, log_int + 1);
          return valid_size;
     }
     
     
     /**
      * Calculates the number of permutations of the given parameters without
      * replacement.
      *
      * <p>For example, if one wishes to find the number of ordered ways that
      * the letters C, A and T can be combined into a set of size 2, then the
      * set_size paramter would be 3 (because there are three letters in the
      * alphabet being used) and the permutation_size parameter would be 2.
      *
      * @param      set_size            The number of entries in the alphabet
      *                                 that can be used to construct
      *                                 permutations.
      * @param      permutation_size    The size of the permutation sets to
      *                                 considered.
      * @return                         The number of possible permutations
      * @throws     Exception           An informative Exception is thrown if
      *                                 set_size is greater than 20, as this
      *                                 would necessitate the calculation of an
      *                                 overly large factorial. An Exception is
      *                                 also thrown if permutation_size is
      *                                 greater than set_size.
      */
     public static int getNumberPerumutations(int set_size, int permutation_size)
     throws Exception
     {
          if (permutation_size > set_size)
               throw new Exception("Permutation set of " + permutation_size + " is larger than set size of " + set_size + ". Replacement is not permitted.");
          long numerator = getFactorial((long) set_size);
          long denominator = getFactorial((long) (set_size - permutation_size));
          return (int) (numerator / denominator);
     }
     
     
     /**
      * Returns whether or not x is either a factor or a multiple of y.
      * z denotes the possible multipliers to check for. True is returned
      * if x is either a factor of a multiple of y (and vice versa), and false
      * otherwise.
      */
     public static boolean isFactorOrMultiple(int x, int y, int[] z)
     {
          boolean is_factor_or_multiple = false;
          
          if (y > x)
          {
               for (int i = 0; i < z.length; i++)
                    if ((x * z[i]) == y)
                    {
                    is_factor_or_multiple = true;
                    i = z.length + 1; // exit loop
                    }
          }
          else
          {
               for (int i = 0; i < z.length; i++)
                    if ((y * z[i]) == x)
                    {
                    is_factor_or_multiple = true;
                    i = z.length + 1; // exit loop
                    }
          }
          
          return is_factor_or_multiple;
     }
}
