/*
 * StringMethods.java
 * Version 3.0.1
 *
 * Last modified on July 1, 2010.
 * McGill University and University of Waikato
 */

package mckay.utilities.staticlibraries;

import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Vector;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


/**
 * A holder class for static methods relating to strings.
 *
 * @author Cory McKay
 */
public class StringMethods
{
     /**
      * Prints each outer entry of the given string array to standard out on a
      * different line and each inner entry on the same line. Each inner entry
      * is marked on the left with open_marker and on the right by close_marker.
      * Each line begins with number_tabs tabs. NULL is printed if test_strings
      * is null. The first thing printed is the number of outer elements.
      *
      * @param test_strings   The array to print.
      * @param number_tabs    The number of tabs to start the line with.
      * @param open_marker    The marker denoting the start of an element.
      * @param close_marker   The marker denoting the end of an element.
      */
     public static void printDoubleArray(String[][] test_strings,
          int number_tabs, String open_marker, String close_marker)
     {
          if (test_strings != null)
          {
               System.out.println("== " + test_strings.length + " OUTER ELEMENTS ==");
               for (int i = 0; i < test_strings.length; i++)
                    printStringArrayContentsOnOneLine(test_strings[i], number_tabs, open_marker, close_marker);
          }
          else System.out.println("NULL");
          System.out.println("");
     }
     
     
     /**
      * Prints each entry of the given string array to standard out on the same
      * line. Each entry is marked on the left with open_marker and on the right
      * by close_marker. Each line begins with number_tabs tabs. NULL is printed
      * if test_strings is null.
      *
      * @param test_strings   The array to print.
      * @param number_tabs    The number of tabs to start the line with.
      * @param open_marker    The marker denoting the start of an element.
      * @param close_marker   The marker denoting the end of an element.
      */
     public static void printStringArrayContentsOnOneLine(String[] test_strings,
          int number_tabs, String open_marker, String close_marker)
     {
          for (int i = 0; i < number_tabs; i++)
               System.out.print("\t");
          
          if (test_strings == null)
               System.out.println("NULL");
          else for (int i = 0; i < test_strings.length; i++)
               System.out.print(open_marker + test_strings[i] + close_marker);
          System.out.println("");
     }
     
     
     /**
      * Prints each entry of the given string array to standard out on a
      * different line. Each line is numbered by its corresponding array
      * index, and is contained in >< markers.
      *
      * @param test_strings   The array of strings to pring.
      */
     public static void printStringArrayContents(String[] test_strings)
     {
          if (test_strings == null)
               System.out.println("The given string array is null.");
          else
          {
               System.out.println("The string array has " + test_strings.length + " entries:");
               for (int i = 0; i < test_strings.length; i++)
                    System.out.println(i + ": >" + test_strings[i] + "<");
          }
     }
     
     
     /**
      * Breaks the given input string into tokens based on the given delimiter,
      * and returns an array where each entry is a token.
      *
      * @param      input     The string to break into tokens.
      * @param      delimiter The string that must occur wherever tokens are to
      *                       be separated.
      * @return               The tokens. Null is returned if either input or
      *                       delimiter is null.
      */
     public static String[] breakIntoTokens(String input, String delimiter)
     {
          if (input == null || delimiter == null) return null;
          
          StringTokenizer tokenizer = new StringTokenizer(input, delimiter);
          String[] tokens = new String[tokenizer.countTokens()];
          int count = 0;
          while (tokenizer.hasMoreTokens())
          {
               tokens[count] = tokenizer.nextToken();
               count++;
          }
          return tokens;
     }
     
     
     /**
      * Checks to see if a given string is the same as an entry in the given
      * array.
      *
      * @param	test_string	The string to search the array_to_check array
      *                         for.
      * @param	array_to_check	The array to search to see if test_string is an
      *				entry.
      * @return			True if test_string is the same of at least one
      *				entry of array_to_check, false otherwise.
      */
     public static boolean isStringInArray(String test_string, String[] array_to_check)
     {
          for (int i = 0; i < array_to_check.length; i++)
               if (test_string.equals(array_to_check[i]))
                    return true;
          return false;
     }
     
     
     /**
      * Checks the given array of strings to see if all of the contents are
      * identical. If they are, then true is returned. If the are not, then
      * false is returned. True is returned if the array is of size one. The
      * given array may contain some or all entries with values of null.
      *
      * @param test_array     The array of strings to test.
      * @return               Whether or not all entries are identical.
      */
     public static boolean areAllEntriesIdentical(String[] test_array)
     {
          if (test_array.length == 1)
               return true;
          String first = test_array[0];
          for (int i = 1; i < test_array.length; i++)
          {
               if (test_array[i] == null)
               {
                    if (first != null)
                         return false;
               }
               else if (first == null)
                    return false;
               else if (!first.equals(test_array[i]))
                    return false;
          }
          return true;
     }
     
     
     /**
      * Returns the index in the possible_names array where the given_name
      * parameter occurs. Throws an exception if it is not there.
      *
      * @param	given_name	The string to search for in possible_names.
      * @param	possible_names	The array to search for given_names in.
      * @return                 The index in possible_names that contains
      *                         given_name.
      * @throws Exception       Throws an exception if if given_name is not
      *                         in the possible_names array.
      */
     public static int getIndexOfString(String given_name,
          String[] possible_names)
          throws Exception
     {
          for (int i = 0; i < possible_names.length; i++)
               if (given_name.equals(possible_names[i]))
                    return i;
          throw new Exception("Unable to find " + given_name + ".");
     }
     
     
     /**
      * Return the index of the longest string in the given array. If multiple
      * strings have the same length, then the fist one is returned.
      *
      * @param  to_check The strings to check.
      * @return          The index in to_check of the longest string.
      */
     public static int getIndexOfLongestString(String[] to_check)
     {
          int[] lengths = new int[to_check.length];
          for (int i = 0; i < lengths.length; i++)
               lengths[i] = to_check[i].length();
          return MathAndStatsMethods.getIndexOfLargest(lengths);
     }
     
     
     /**
      * Returns the length of the longer of the two given strings.
      *
      * @param  first    One string to compare.
      * @param  second   The other string to compare.
      * @return          The length of the longer string. Returns -1 if either
      *                  string is null.
      */
     public static int getLengthOfLongerString(String first, String second)
     {
          if (first == null || second == null)
               return -1;
          int first_length = first.length();
          int second_length = second.length();
          if (first_length > second_length)
               return first_length;
          else return second_length;
     }
     
     
     /**
      * Returns the length of the shorter of the two given strings.
      *
      * @param  first    One string to compare.
      * @param  second   The other string to compare.
      * @return          The length of the shorter string. Returns -1 if either
      *                  string is null.
      */
     public static int getLengthOfShorterString(String first, String second)
     {
          if (first == null || second == null)
               return -1;
          int first_length = first.length();
          int second_length = second.length();
          if (first_length < second_length)
               return first_length;
          else return second_length;
     }
     

     /**
      * Returns a new array whose first part consists of the elements of
      * array_1 and whose second part consists of the elements of array_2.
      *
      * @param	array_1  The first array to concatenate.
      * @param	array_2  The second array to concatenate.
      * @return          array_1 and array_2 combined into 1 array.
      */
     public static String[] concatenateStringArrays(String[] array_1, String[] array_2)
     {
          int length_1 = array_1.length;
          int length_2 = array_2.length;
          String[] new_array = new String[length_1 + length_2];
          for (int i = 0; i < length_1; i++)
               new_array[i] = array_1[i];
          for (int j = 0; j < length_2; j++)
               new_array[length_1 + j] = array_2[j];
          return new_array;
     }


	 /**
      * Returns true if the two given arrays contain exactly the same strings,
      * but in any order in the array. Returns false if this is not the case
      * (i.e. if either of the arrays contains a string that the other does
      * not).
      *
      * @param array1    The array to compare with array2.
      * @param array2    The array to compare with array1.
      * @return          Whether or not the two arrays have matching strings.
      */
     public static boolean doStringArraysMatch(String[] array1, String[] array2)
     {
          if (array1.length != array2.length)
               return false;
          
          for (int i = 0; i < array1.length; i++)
          {
               boolean found = false;
               for (int j = 0; j < array2.length; j++)
                    if (array1[i].equals(array2[j]))
                    {
                    found = true;
                    j = array2.length;
                    }
               if (!found)
                    return false;
          }
          
          return true;
     }
     
     
     /**
      * Returns a shortened copy of the given array of strings with all
      * duplicate entries removed. The original array of strings is not changed.
      *
      * @param	strings	The array of strings to remove duplicate entries from.
      * @return		A shortened copy of the given strings with duplicates
      *			removed.
      */
     public static String[] removeDoubles(String[] strings)
     {
          String[] editable_strings = new String[strings.length];
          for (int i = 0; i < editable_strings.length; i++)
               editable_strings[i] = strings[i];
          
          for (int i = 0; i < editable_strings.length - 1; i++)
               for (int j = i + 1; j < editable_strings.length; j++)
                    if (editable_strings[i] != null && editable_strings[j] != null)
                         if (editable_strings[i].equals(editable_strings[j]))
                              editable_strings[j] = null;
          
          Object[] cleaned_obj = ArrayMethods.removeNullEntriesFromArray(editable_strings);
          String[] cleaned_strings = new String[cleaned_obj.length];
          for (int i = 0; i < cleaned_strings.length; i++)
               cleaned_strings[i] = (String) cleaned_obj[i];
          
          return cleaned_strings;
     }


     /**
      * Removes duplicate entries from the given String array.
      *
      * @param array_to_check	The array to check for duplicates.
      * @return					A copy of <i>array_to_check</i> with the the duplicate entries removed. All
	  *							entries of the returned array will be unique.
      * @throws Exception		An informative exception is thrown if <i>array_to_check</i> is null or empty.
      */
     public static String[] removeDuplicateEntries(String[] array_to_check)
             throws Exception
     {
         // Check that given array is not empty or null
         if (array_to_check == null)
             throw new Exception ("Cannot remove duplicate entries from empty array.");
         if (array_to_check.length < 1)
             throw new Exception ("Cannot remove duplicate entries from empty array.");

         // Store unique entries in a Linked list
         LinkedList<String> unique_entries = new LinkedList<String>();
         for (int i=0; i < array_to_check.length; i++)
             if(!unique_entries.contains(array_to_check[i]))
                 unique_entries.add(array_to_check[i]);

		 // Return the results
         return unique_entries.toArray(new String[1]);
     }

     
     /**
      * Returns the number of times that the given substring occurs in the
      * given_string. Returns 0 if either of these strings are empty.
      *
      * @param given_string   The string to search.
      * @param substring      The string to search given_string for.
      * @return               The number of times that substring occurs in
      *                       given_string.
      */
     public static int getNumberOccurencesOfSubString(String given_string,
          String substring)
     {
          // Return null is either of given strings are empty
          if (given_string == null)
               return 0;
          if (given_string.equals(""))
               return 0;
          if (substring == null)
               return 0;
          if (substring.equals(""))
               return 0;
          
          // Count the number of times that the substring occurs
          int count = 0;
          int index = 0;
          while (index < given_string.length() && index != -1)
          {
               index = given_string.indexOf(substring, index);
               if (index != -1)
               {
                    count++;
                    index++;
               }
          }
          
          // Return the results
          return count;
     }
     
     
     /**
      * Combines all the Strings of the given array into one String. Each of the
      * Strings that were previously stored in separate cells of the array are
      * separated by a comma and a space in the new String.
      *
      * @param words    The Strings to be concatenated.
      * @return         A single String containing all the Strings from the
      *                 array separated by a comma and a space.
      */
     public static String concatenateArrayOfStrings(String[] words)
     {
          StringBuffer concatenated = new StringBuffer();
          for(int i = 0; i < words.length; i++)
          {
               concatenated.append(words[i]);
               if(i < words.length - 1)
                    concatenated.append(", ");
          }
          return concatenated.toString();
     }
     
     
     /**
      * Given an array of Strings, this method removes empty cells from the
      * array. Empty is defined as either being null or having a length of
      * zero.
      *
      * @param array     Array from which to remove empty cells.
      * @return          New array containing only the cells of the given array
      *                  that were not empty. Null is returned if the given array
      *                  is null or if all entries are empty.
      */
     public static String[] removeEmptyStringsFromArray(String[] array)
     {
          // return null if given array is null
          if (array == null) return null;
          
          // Linked list to contain non empty entries
          LinkedList<String> not_empty = new LinkedList<String>();
          
          // Go through array checking for empty cells
          for (int i = 0; i < array.length; i++)
               if (array[i] != null && array[i].length()!= 0)
                    not_empty.add(array[i]);
          
          // If all entries were empty, return null
          if(not_empty.size() == 0) return null;
          
          // Otherwise return new array of all non-empty entries
          else return not_empty.toArray(new String[not_empty.size()]);
     }
     
     
     /**
      * Calculates the Levenshtein Distance (edit distance) between the two
      * given strings. This distance is defined as the minimum number of
      * operations needed to transform one string into the other, where a
      * single operation consists of an insertion, deletion or substitution of
      * a single character. All three types of operations are assigned equal
      * weights.
      *
      * @param  first_string  A string to compare to second_string.
      * @param  second_string A string to compare to first_string.
      * @return               The edit distance between first_string and
      *                       second_string. If either string is null, then -1
      *                       is returned.
      */
     public static int calculateLevenshteinDistance(String first_string,
          String second_string)
     {
          // Check if either string is null
          if (first_string == null) return -1;
          if (second_string == null) return -1;
          
          // Convert to charachters array
          char[] first_chars = first_string.toCharArray();
          char[] second_chars = second_string.toCharArray();
          
          // Prepare the distances array
          int[][] distances = new int[first_chars.length + 1][second_chars.length + 1];
          for(int i = 0; i <= first_chars.length; i++)
               distances[i][0] = i;
          for(int i = 0; i < second_chars.length + 1; i++)
               distances[0][i] = i;
          
          // Calculate distances
          for(int i = 1; i <= first_chars.length; i++)
          {
               for(int j = 1; j <= second_chars.length; j++)
               {
                    // Find the cost
                    int cost;
                    if (first_chars[i-1] == second_chars[j-1])
                         cost = 0;
                    else cost = 1;
                    
                    // Find three possibilities
                    int a = distances[i-1][j] + 1;
                    int b = distances[i][j-1] + 1;
                    int c = distances[i-1][j-1] + cost;
                    
                    // Store the minimum of the three possibilities
                    distances[i][j] = a;
                    if (b < distances[i][j])
                         distances[i][j] = b;
                    if (c < distances[i][j])
                         distances[i][j] = c;
               }
          }
          
          // Return the edit distance
          return distances[first_chars.length][second_chars.length];
     }
     
     
     /**
      * Calculates the Levenshtein Distance (edit distance) between each pair
      * of strings in the given array. This distance is defined as the minimum
      * number of operations needed to transform one string into the other,
      * where a single operation consists of an insertion, deletion or
      * substitution of a single character. All three types of operations are
      * assigned equal weights.
      *
      * @param  strings  The strings to compare.
      * @return          The distances between each pair of strings in the given
      *                  array. Both dimensions correspond to the strings in the
      *                  same order as they are found in the given array. If
      *                  either string being compared is null, then -1 its entry
      *                  in the matrix is set to -1.
      */
     public static int[][] calculateLevenshteinDistances(String[] strings)
     {
          int[][] distances = new int[strings.length][strings.length];
          for (int i = 0; i < distances.length; i++)
          {
               if (strings[i] == null)
                    distances[i][i] = -1;
               else distances[i][i] = 0;
               
               for (int j = 0; j < i; j++)
               {
                    distances[i][j] = calculateLevenshteinDistance(strings[i], strings[j]);
                    distances[j][i] = distances[i][j];
               }
          }
          return distances;
     }
     
     
     /**
      * Compares two strings to see if they are the same, taking into account
      * the possibility that they may be null. Updates the provided to_update
      * Vector with the difference if they are different.
      *
      * @param key       A description of the strings being compared.
      * @param first     The first string to comare.
      * @param second    The second string to compare.
      * @param to_update The Vector to add the difference report to. Nothing
      *                  is added if first and second are the same. If they
      *                  are different, then a String array of size 3 is
      *                  added to to_update. The first entry is key, the second
      *                  is first and the third is second.
      */
     public static void reportStringDifferences(String key, String first,
          String second, Vector<String[]> to_update)
     {
          boolean shold_update = false;
          
          if ( (first == null && second != null) ||
               (first != null && second == null) )
               shold_update = true;
          else if (first == null && second == null)
               shold_update = false;
          else if (!first.equals(second))
               shold_update = true;
          
          if (shold_update)
          {
               String[] update = {key, first, second};
               to_update.add(update);
          }
     }
     
     
     /**
      * Returns a copy of the given string with all but the first
      * number_characters eliminated. If the given string is shorter than
      * number_characters, then blank spaces are added to the end of the string
      * in order to make it the full length.
      *
      * @param	string_to_shorten  The string to be shortened or have spaces
      *                            added to its end.
      * @param	number_characters  Number of characters in the new string.
      * @return                    The shortened string.
      */
     public static String getBeginningOfString(String string_to_shorten,
          int number_characters)
     {
          String copy = new String(string_to_shorten);
          if (string_to_shorten.length() < number_characters)
          {
               int difference = number_characters - string_to_shorten.length();
               for (int i = 0; i < difference; i++)
                    copy += " ";
               return copy;
          }
          else if (string_to_shorten.length() > number_characters)
               return string_to_shorten.substring(0, number_characters);
          else return copy;
     }
     
     
     /**
      * Returns a copy of the given string with all but the first
      * number_characters eliminated. If the given string is shorter than
      * number_characters, then hyphens are are added to the end of the string
      * in order to make it the full length (with two blank spaces on each side.
      *
      * @param	string_to_shorten  The string to be shortened or have spaces
      *                            added to its end.
      * @param	number_characters  Number of characters in the new string.
      * @return                    The shortened string.
      */
     public static String getBeginningOfStringWithHyphenFiller(String string_to_shorten,
          int number_characters)
     {
          String copy = new String(string_to_shorten);
          if (string_to_shorten.length() < number_characters)
          {
               int difference = number_characters - string_to_shorten.length();
               for (int i = 0; i < difference; i++)
               {
                    if (i == 0 || i == 1 || i == (difference - 2) || i == (difference - 1))
                         copy += " ";
                    else
                         copy += "-";
               }
               return copy;
          }
          else if (string_to_shorten.length() > number_characters)
               return string_to_shorten.substring(0, number_characters);
          else return copy;
     }
     
     
     /**
      * Returns null if the given string is empty and returns the original
      * string if it is not empty.
      *
      * @param given_string   The string to examine.
      * @return               The original string or null.
      */
     public static String nullifyStringIfEmpty(String given_string)
     {
          if (given_string == null)
               return null;
          else if (given_string.matches(""))
               return null;
          else
               return given_string;
     }
     
     
     /**
      * Returns the name of the file referred to by the given path.
      *
      * @param	file_path The file path from which the file name is to be
      *                   extracted.
      * @return           The name of the file referred to in the parameter.
      */
     public static String convertFilePathToFileName(String file_path)
     {
          return file_path.substring(file_path.lastIndexOf(File.separator) + 1, file_path.length());
     }
     
     
     /**
      * Returns the name of the directory that the given filename is found in.
      * Throws an exception if no valid directory separator is present.
      *
      * @param file_path      The path to extract the directory from.
      * @return               The directory name.
      * @throws Exception     Throws an exception if no valid directory
      *                       separator is present.
      */
     public static String getDirectoryName(String file_path)
     throws Exception
     {
          int index_of_last_separator = file_path.lastIndexOf(File.separatorChar);
          if (index_of_last_separator == -1)
               throw new Exception(file_path + " does not contain a valid directory separator.");
          return new String(file_path.substring(0, file_path.lastIndexOf(File.separator)) + File.separator);
     }
     
     
     /**
      * Returns a copy of the given string with the extension removed. Returns
      * null if there is no extension or if there are less than five characters
      * in the string.
      *
      * <p><b>IMPORTANT:</b> <i>filename</i> should consist of at least four
      * characters.
      *
      * @param	filename The name of the file from which the extension is to be
      *                  removed.
      * @return          The name of the file with the extension removed.
      */
     public static String removeExtension(String filename)
     {
          if (filename.length() < 5)
               return null;
          if (filename.charAt(filename.length() - 4) != '.')
          {
               if (filename.charAt(filename.length() - 5) == '.')
                    return filename.substring(0, (filename.length() - 5));
               else if (filename.charAt(filename.length() - 3) == '.')
                    return filename.substring(0, (filename.length() - 3));
               else return null;
          }
          return filename.substring(0, (filename.length() - 4));
     }
     
     
     /**
      * Returns the 2, 3 or 4 letter extension of the given file name. Returns
      * null if there is no extension or if there are less than 4 characters in
      * the string.
      *
      * <p><b>IMPORTANT:</b> <i>filename</i> should consist of at least four
      * characters.
      *
      * @param	filename The name of the file from which the extension is to be
      *                  returned.
      * @return          The extension of the file (including the period).
      */
     public static String getExtension(String filename)
     {
          if (filename.length() < 5)
               return null;
          if (filename.charAt(filename.length() - 4) != '.')
          {
               if (filename.charAt(filename.length() - 5) == '.')
                    return filename.substring((filename.length() - 5), filename.length());
               else if (filename.charAt(filename.length() - 3) == '.')
                    return filename.substring((filename.length() - 3), filename.length());
               else return null;
          }
          return filename.substring((filename.length() - 4), filename.length());
     }
     
     
     /**
      * Returns a copy of the given file name with its original extension
      * stripped away and replaced by the new specified extension.
      *
      * @param filename       The name of the file that is to have its extension
      *                       replaced.
      * @param new_extension  The replacement extension, NOT including the
      *                       period.
      * @return               The file name with the new extension.
      */
     public static String replaceExtension(String filename, String new_extension)
     {
          String new_string = filename;
          if (getExtension(filename) != null)
               new_string = removeExtension(filename);
          return new_string + "." + new_extension;
     }
     
     
     /**
      * Returns a formatted version of number_to_round. It will always
      * show a 0 before numbers less than one, and will only show up to
      * decimal_places decimal places.
      *
      * <p>Values of not a number, negative infinity and positive infinity will
      * be returned as NaN, -Infinity and Infinity respectively.
      *
      * @param	number_to_round	The number that is to be rounded.
      * @param	decimal_places	The maximum number of decimal places that will
      *                         be displayed.
      * @return                 The formatted number.
      */
     public static String getRoundedDouble(double number_to_round,
          int decimal_places)
     {
          if (number_to_round == Double.NaN)
               return new String("NaN");
          if (number_to_round == Double.NEGATIVE_INFINITY)
               return new String("-Infinity");
          if (number_to_round == Double.POSITIVE_INFINITY)
               return new String("Infinity");
          
          String format_pattern = "#0.";
          for (int i = 0; i < decimal_places; i++)
               format_pattern += "#";
          DecimalFormat formatter = new DecimalFormat(format_pattern);
          
          return formatter.format(number_to_round);
     }
     
     
     /**
      * Returns a formatted version of number_to_round. It will only show up to
      * decimal_places decimal places. Every three places to the left of the
      * decimal will be separated by a comma.
      *
      * <p>Values of not a number, negative infinity and positive infinity will
      * be returned as NaN, -Infinity and Infinity respectively.
      *
      * @param	number_to_round	The number that is to be rounded.
      * @param	decimal_places	The maximum number of decimal places that will
      *                         be displayed.
      * @return                 The formatted number.
      */
     public static String getRoundedDoubleWithCommas(double number_to_round,
          int decimal_places)
     {
          if (number_to_round == Double.NaN)
               return new String("NaN");
          if (number_to_round == Double.NEGATIVE_INFINITY)
               return new String("-Infinity");
          if (number_to_round == Double.POSITIVE_INFINITY)
               return new String("Infinity");
          
          String format_pattern = "###,###.";
          for (int i = 0; i < decimal_places; i++)
               format_pattern += "#";
          DecimalFormat formatter = new DecimalFormat(format_pattern);
          
          return formatter.format(number_to_round);
     }
     
     
     /**
      * Returns a formatted version of <i>number_to_round</i> that has been
      * converted to scientific notation and includes the given number
      * of <i>significant_digits</i>.
      *
      * <p>Values of not a number, negative infinity and positive infinity will
      * be returned as NaN, -Infinity and Infinity respectively.
      *
      * @param	number_to_round		The number that is to be formatted.
      * @param	significant_digits	The number of significant digits to use.
      * @return                         The formatted string.
      */
     public static String getDoubleInScientificNotation(double number_to_round,
          int significant_digits)
     {
          if (number_to_round == Double.NaN)
               return new String("NaN");
          if (number_to_round == Double.NEGATIVE_INFINITY)
               return new String("-Infinity");
          if (number_to_round == Double.POSITIVE_INFINITY)
               return new String("Infinity");
          
          String format_pattern = "0.";
          for (int i = 0; i < significant_digits - 1; i++)
               format_pattern += "#";
          format_pattern += "E0";
          DecimalFormat formatter = new DecimalFormat(format_pattern);
          
          return formatter.format(number_to_round);
     }
     
     
     /**
      * Formats the given number with a comma every three digits.
      *
      * @param number    The number to format.
      * @return          The formatted number.
      */
     public static String getNumberFormattedWithCommas(int number)
     {
          String format_pattern = "###,###";
          DecimalFormat formatter = new DecimalFormat(format_pattern);
          return formatter.format(number);
     }
     
     
     /**
      * Formats the given number with a comma every three digits.
      *
      * @param number    The number to format.
      * @return          The formatted number.
      */
     public static String getNumberFormattedWithCommas(long number)
     {
          String format_pattern = "###,###";
          DecimalFormat formatter = new DecimalFormat(format_pattern);
          return formatter.format(number);
     }
     
     
     /**
      * Tests if the given string is an integer (e.g. "42" but not "I am 42.").
      *
      * @param test_string    The string to test.
      * @return               Whether or not the string is an integer.
      */
     public static boolean testIfStringIsAnInt(String test_string)
     {
          boolean is_int = true;
          try
          {
               int dummy = Integer.parseInt(test_string);
          }
          catch (NumberFormatException e)
          {
               is_int = false;
          }
          return is_int;
     }
     
     
     /**
      * Tests if the given string is an integer within the given bounds, and
      * returns it as an integer if it is. If it is not, then throws an
      * exception indicating the problem.
      *
      * @param  test_string   The string to convert to an integer.
      * @param  min           The minimum acceptable integer value.
      * @param  max           The maximum acceptable integer value.
      * @return               The value of test_strig as an int.
      * @throws Exception     An informative exception is thrown if test_string
      *                       is not an integer or does not fall between min
      *                       and max.
      */
     public static int getIntInLimits(String test_string, int min, int max)
     throws Exception
     {
          try
          {
               int number = Integer.parseInt(test_string);
               if (number < min)
                    throw new Exception(test_string + " is less than the acceptable minimum of " + min);
               if (number > max)
                    throw new Exception(test_string + " is more than the acceptable maximum of " + max);
               return number;
          }
          catch (NumberFormatException e)
          {
               throw new Exception(test_string + " is not an integer.");
          }
     }


	/**
	 * Encodes the given string in exactly the same way as the <i>java.net.URLEncoder encode</i> method
	 * using UTF-8 character encoding, with the one difference that an empty string is returned if the
	 * <i>to_encode</i> string is null.
	 *
	 * @param to_encode	The string to encode.
	 * @return				The results of the encoding, or "" if the input to the method is null.
	 * @throws Exception	An exception is thrown if a problem occurs.
	 */
	public static String URLEncodeWithNulls(String to_encode)
			throws Exception
	{
		if (to_encode == null) return "";
		else return URLEncoder.encode(to_encode, "utf-8");
	}


	/**
	 * Returns a string that is identical to the result of the <i>java.net.URLDecoder decode</i> method
	 * applied using the UTF-8 character encoding, with the one difference that null is returned if the
	 * input string is null.
	 *
	 * @param to_encode		The string to decode.
	 * @return				The results of the decoding, or null if the input to the method is null.
	 * @throws Exception	An exception is thrown if a problem occurs.
	 */
	public static String URLDecodeNullCompatible(String to_encode)
		throws Exception
	{
		if (to_encode == null) return null;
		else return URLDecoder.decode(to_encode, "utf-8");
	}



	/**
	 * Encrypt the given String using the specified password. The resultant
	 * array of encrypted bytes can be decrypted using the same password with
	 * with the passwordBasedDecrypt method.
	 *
	 * @param text_to_encrypt	The text to encrypt.
	 * @param password			The password key to use for encryption
	 * @return					The encrypted text.
	 * @throws Exception		An exception is thrown if a problem occurs.
	 */
	public static byte[] passwordBasedEncrypt(String text_to_encrypt, String password)
			throws Exception
	{
		// Set the salt and iteration count
		byte[] salt = { (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99 };
		int count = 20;

		// Create PBE parameter set
		PBEParameterSpec pbe_param_spec = new PBEParameterSpec(salt, count);

		// Convert the password into a SecretKey object
		PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory key_factory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey pbe_key = key_factory.generateSecret(pbeKeySpec);

		// Create and initialize a Cipher
		Cipher pbe_cipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbe_cipher.init(Cipher.ENCRYPT_MODE, pbe_key, pbe_param_spec);

		// Perform the encryption
		return pbe_cipher.doFinal(text_to_encrypt.getBytes());
	}


	/**
	 * Decrypt the given bytes into text using the specified password. It is
	 * assumed that the bytes were generated with the passwordBasedEncrypt
	 * method.
	 *
	 * @param encrypted_text	The encrypted data.
	 * @param password			The password to use to perform the decryption.
	 * @return					The decrypted text.
	 * @throws Exception		An exception is thrown if a problem occurs.
	 */
	public static String passwordBasedDecrypt(byte[] encrypted_text, String password)
			throws Exception
	{
		// Set the salt and iteration count
		byte[] salt = { (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99 };
		int count = 20;

		// Create PBE parameter set
		PBEParameterSpec pbe_param_spec = new PBEParameterSpec(salt, count);

		// Convert the password into a SecretKey object
		PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
		SecretKeyFactory key_factory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		SecretKey pbe_key = key_factory.generateSecret(pbeKeySpec);

		// Create and initialize a Cipher
		Cipher pbe_cipher = Cipher.getInstance("PBEWithMD5AndDES");
		pbe_cipher.init(Cipher.DECRYPT_MODE, pbe_key, pbe_param_spec);

		// Decrypt the ciphertext
		return new String(pbe_cipher.doFinal(encrypted_text));
	}
}