/*
 * MergeReport.java
 * Version 1.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jMusicMetaManager.processingstructures;

import java.util.Vector;


/**
 * An object of this class contains a report of a merge of two Entry objects
 * that was carried out via the mergeIdenticalEntries and/or
 * mergeEntriesIgnoringWordOrder methods of the Entries class. Each MergeReport
 * contains one or more merge keys, which can give a description of why the
 * merge was performed. Each MergeReport also contains two or more merge
 * indexes, which indicate the combined contents of the indexes fields of the
 * two merged Entry objects.
 *
 * <p>The static mergeSubsetsIntoSupersets method can be used to take an array
 * of MergeReport objects and cause all those MergeReport objects whose merge
 * keys are a subset of those merge keys of another MergeReport object in the
 * array to be subsumed into their superset MergeReport objects, which will have
 * the combined merge keys of themselves and all MergeReport objects that they
 * subsume.
 *
 * @author Cory McKay
 */
public class MergeReport
{
     /* FIELDS ****************************************************************/
     
     /**
      * Information describing why a merge of Entry objects was performed.
      * Individual entries may be null.
      */
     private   Vector<String>    merge_keys;
     
     /**
      * The combined contents of the index fields of the two merged Entry
      * objects.
      */
     private   Vector<Integer>   merge_indexes;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new MergeReport instance describing a merge of Entry objects.
      *
      * @param merge_key The reason why this merge of Entry objects occured.
      *                  May be null.
      * @param indexes   The combined indexes of the two merged entry objects.
      */
     public MergeReport(String merge_key, int[] indexes)
     {
          merge_keys = new Vector<String>();
          merge_keys.add(merge_key);
          merge_indexes = new Vector<Integer>();
          addAdditionalIndexes(indexes);
     }
     
     
     /**
      * Creates a MergeReport that contains copies of all information in the
      * given MergeReport.
      *
      * @param to_copy   The MergeReport to copy.
      */
     public MergeReport(MergeReport to_copy)
     {
          merge_keys = new Vector<String>();
          String[] original_keys = to_copy.getMergeKeys();
          for (int i = 0; i < original_keys.length; i++)
               merge_keys.add(original_keys[i]);
          
          merge_indexes = new Vector<Integer>();
          int[] original_indexes = to_copy.getMergeIndexes();
          for (int i = 0; i < original_indexes.length; i++)
               merge_indexes.add(original_indexes[i]);
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Get the merge keys stored in this MergeReport expressed as an array.
      *
      * @return     The merge keys stored in this MergeReport. Returns null if
      *             none have been added or the first is null.
      */
     public String[] getMergeKeys()
     {
          String[] to_return = merge_keys.toArray(new String[1]);
          if (to_return[0] == null)
               return null;
          else return to_return;
     }
     
     
     /**
      * Get the merge keys stored in this MergeReport in their original Vector
      * form.
      *
      * @return     The merge keys stored in this MergeReport. Returns null if
      *             none have been added or the first is null.
      */
     public Vector<String> getMergeKeysVector()
     {
          return merge_keys;
     }
     
     
     /**
      * Get the number of individual merge indexes stored in this MergeReport.
      *
      * @return     The number of individual merge indexes stored in this
      *             MergeReport.
      */
     public int getNumberIndexes()
     {
          return merge_indexes.size();
     }
     
     
     /**
      * Get the merge indexes stored in this MergeReport.
      *
      * @return     The merge indexes stored in this MergeReport.
      */
     public int[] getMergeIndexes()
     {
          Integer[] temp = merge_indexes.toArray(new Integer[1]);
          int[] to_return = new int[temp.length];
          for (int i = 0; i < to_return.length; i++)
               to_return[i] = temp[i].intValue();
          return to_return;
     }
     
     
     /**
      * Add additional indexes to those stored in this MergeReport.
      *
      * @param indexes_to_add Additional indexes to store in this MergeReport.
      */
     public void addAdditionalIndexes(int[] indexes_to_add)
     {
          for (int i = 0; i < indexes_to_add.length; i++)
               merge_indexes.add(new Integer(indexes_to_add[i]));
     }
     
     
     /**
      * Add additional merge keys to those stored in this MergeReport.
      *
      * @param new_merge_keys The new merge keys to store in this MergeReport.
      */
     public void addAdditionalMergeKeys(String[] new_merge_keys)
     {
          for (int i = 0; i < new_merge_keys.length; i++)
               merge_keys.add(new_merge_keys[i]);
     }
     
     
     /* STATIC METHODS ********************************************************/
     
     
     /**
      * Independently remove any duplicate strings in each merge_keys field of
      * the given MergeReport objects.
      *
      * @param given_reports  The MergeReports to each remove duplicates from.
      */
     public static void removeDuplicateMergeKeys(MergeReport[] given_reports)
     {
          for (int i = 0; i < given_reports.length; i++)
          {
               Vector<String> these_reports = given_reports[i].getMergeKeysVector();
               for (int j = 0; j < these_reports.size() - 1; j++)
               {
                    String current = these_reports.get(j);
                    for (int k = j + 1; k < these_reports.size(); k++)
                         if (these_reports.get(k).equals(these_reports.get(j)))
                         {
                         these_reports.remove(k);
                         k--;
                         }
               }
          }
     }
     
     
     /**
      * Take the given MergeReports and search through them for any MergeReport
      * whose merge_indexes are a subset of another MergeReport's merge_indexes.
      * The returned array is built of all of the passed MergeReports, with such
      * subsets removed and the merge_keys of the subsets added to the
      * supersets which subsumed them.
      *
      * @param given_reports  The MergeReports to search for subsets.
      * @return               The given_reports, but with subsets removed and
      *                       merge_keys of subsets added to supersets.
      */
     public static MergeReport[] mergeSubsetsIntoSupersets(MergeReport[] given_reports)
     {
          // Copy given_reports
          MergeReport[] reports = new MergeReport[given_reports.length];
          for (int i = 0; i < reports.length; i++)
               reports[i] = new MergeReport(given_reports[i]);
          
          // Set all entries of reports whose indexes are a subset of another
          // entry of reports to null, and add the merge keys of the subset
          // entry to the merge keys of the superset entry
          boolean a_change_made = false;
          int reports_length = reports.length;
          for (int i = 0; i < reports_length; i++)
          {
               for (int j = 0; j < reports_length; j++)
               {
                    if (i != j && reports[j] != null && reports[i] != null)
                    {
                         // Assign the report with the larger number of indexes
                         // to bigger, and the one with the smaller number to
                         // smaller
                         MergeReport bigger = null;
                         MergeReport smaller = null;
                         if (reports[i].getNumberIndexes() > reports[j].getNumberIndexes())
                         {
                              bigger = reports[i];
                              smaller = reports[j];
                         }
                         else
                         {
                              bigger = reports[j];
                              smaller = reports[i];
                         }
                         
                         // Find whether smaller is a subset of larger
                         boolean is_subset = true;
                         int[] bigger_indexes = bigger.getMergeIndexes();
                         int[] smaller_indexes = smaller.getMergeIndexes();
                         for (int m = 0; m < smaller_indexes.length; m++)
                         {
                              boolean found = false;
                              for (int n = 0; n < bigger_indexes.length; n++)
                              {
                                   if (smaller_indexes[m] == bigger_indexes[n])
                                   {
                                        found = true;
                                        n = bigger_indexes.length;
                                   }
                              }
                              if (!found)
                              {
                                   is_subset = false;
                                   m = smaller_indexes.length;
                              }
                         }
                         
                         // Add the keys of bigger to smaller and set the
                         // entry in reports corresponding to smaller to null
                         if (is_subset)
                         {
                              a_change_made = true;
                              bigger.addAdditionalMergeKeys(smaller.getMergeKeys());
                              if (reports[i].getNumberIndexes() > reports[j].getNumberIndexes())
                                   reports[j] = null;
                              else
                                   reports[i] = null;
                         }
                    }
               }
          }
          
          // Remove all null entries of report if a change was made. Return
          // results
          if (a_change_made)
          {
               Vector<MergeReport> report_vector = new Vector<MergeReport>();
               for (int i = 0; i < reports.length; i++)
                    report_vector.add(reports[i]);
               int current = 0;
               while (current < report_vector.size() - 1)
               {
                    if (report_vector.get(current) == null)
                         report_vector.remove(current);
                    else
                         current++;
               }
               return report_vector.toArray(new MergeReport[1]);
          }
          else return given_reports;
     }
}