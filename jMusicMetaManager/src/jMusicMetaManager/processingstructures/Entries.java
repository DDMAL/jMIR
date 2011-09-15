/*
 * Entries.java
 * Version 1.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jMusicMetaManager.processingstructures;

import java.util.Vector;
import mckay.utilities.vectorsorter.QuicksortComparator;
import mckay.utilities.vectorsorter.SortableVector;
import jMusicMetaManager.RecordingMetaData;


/**
 * Stores a sortable vector of Entry objects and includes methods for combining
 * and processing them.
 *
 * <p>Note that it is possible that the value field of stored Entry objects may
 * be set to null by some calling methods for various reasons.
 *
 * <p>A cumulative report of all merges of Entry object that occur (using the
 * mergeIdenticalEntries method) is stored, and may be accessed via the
 * getMergeReport method.
 *
 * @author Cory McKay
 */
public class Entries
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * A vector of sortable Entry objects.
      */
     private   SortableVector<Entry>    entries;
     
     /**
      * Whether or not the entries have been sorted.
      */
     private   boolean                  sorted;
     
     /**
      * A cumulative report of all important merges of entries that have occured
      * using the mergeIdenticalEntries or mergeEntriesIgnoringWordOrder methods
      * with the report_in_merge_report parameter set to true.
      */
     private   Vector<MergeReport>      merge_report;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new empty instance of Entries.
      */
     public Entries()
     {
          entries = new SortableVector<Entry>(new EntryCompare());
          sorted = false;
          merge_report = new Vector<MergeReport>();
     }
     
     
     /**
      * Creates a new Entires object with the specified fields.
      *
      * @param      entries        The entries to fill this Entries object with.
      *                            May not be null.
      * @param      sorted         Whether or not the sorted entries are sorted.
      * @param      merge_report   The merge_report to store in this Entries
      *                            object. May not be null.
      * @throws     Exception      Throws an Exception if the entries or
      *                            merge_report parameters are null.
      */
     public Entries(SortableVector<Entry> entries, boolean sorted,
          Vector<MergeReport> merge_report)
          throws Exception
     {
          if (entries == null)
               throw new Exception("Entries parameter may not be null.");
          if (merge_report == null)
               throw new Exception("Merge report parameter may not be null.");
          
          this.entries = entries;
          this.sorted = sorted;
          this.merge_report = merge_report;
     }
     
     
     /**
      * Creates a new instance of Entries that is initialized to store each
      * of the values of the given array as a key and its index in the given
      * array as the stored index. The entries are assumed to be unsorted,
      * and are marked as such.
      *
      * @param contents  The data to store as Entries.
      */
     public Entries(String[] contents)
     {
          entries = new SortableVector<Entry>(new EntryCompare());
          for (int i = 0; i < contents.length; i++)
               entries.add(new Entry(contents[i], i));
          sorted = false;
          merge_report = new Vector<MergeReport>();
     }
     
     
     /**
      * Creates a new instance of Entries that is initialized based on the
      * data in an array of RecordingMetaData objects. The value fields of the
      * Entry objects stored in the entries field will store either the titles,
      * artists, composers, album titles or genres of the given recordings,
      * depending on the value of the field_key parameter. The entries are
      * assumed to be unsorted, and are marked as such.
      *
      * <p>Note that it is possible that the value field of stored Entry objects
      * may be set to null by some calling methods for various reasons.
      *
      * @param  recordings    The recordings whose contents will be used to
      *                       populate the entries field.
      * @param  field_key     A code indicating the field of the recordings
      *                       objects to store. Must correspond to one of the
      *                       static final codes of the RecordingMetaData class.
      * @throws Exception     An exception is thrown if an invalid field_key
      *                       is used.
      */
     public Entries(RecordingMetaData[] recordings, int field_key)
     throws Exception
     {
          entries = new SortableVector<Entry>(new EntryCompare());
          
          if (field_key == RecordingMetaData.ALBUM_IDENTIFIER)
               for (int i = 0; i < recordings.length; i++)
                    entries.add(new Entry(recordings[i].album, i));
          else if (field_key == RecordingMetaData.ARTIST_IDENTIFIER)
               for (int i = 0; i < recordings.length; i++)
                    entries.add(new Entry(recordings[i].artist, i));
          else if (field_key == RecordingMetaData.TITLE_IDENTIFIER)
               for (int i = 0; i < recordings.length; i++)
                    entries.add(new Entry(recordings[i].title, i));
          else if (field_key == RecordingMetaData.COMPOSER_IDENTIFIER)
               for (int i = 0; i < recordings.length; i++)
                    entries.add(new Entry(recordings[i].composer, i));
          else if (field_key == RecordingMetaData.COMMENTS_IDENTIFIER)
               for (int i = 0; i < recordings.length; i++)
                    entries.add(new Entry(recordings[i].comments, i));
          else if (field_key == RecordingMetaData.GENRES_IDENTIFIER)
          {
               for (int i = 0; i < recordings.length; i++)
               {
                    if (recordings[i].genres == null)
                         entries.add(new Entry(null, i));
                    else
                    {
                         for (int j = 0; j < recordings[i].genres.length; j++)
                              entries.add(new Entry(recordings[i].genres[j], i));
                    }
               }
          }
          else throw new Exception("Invalid field key.");
          sorted = false;
          merge_report = new Vector<MergeReport>();
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Create and add a new entry. The entries are marked as unsorted.
      *
      * @param value     The value to store in the new entry.
      * @param index     The index to store in the new Entry.
      */
     public void addEntry(String value, int index)
     {
          entries.add(new Entry(value, index));
          sorted = false;
     }
     
     
     /**
      *  Returns the number of stored entries.
      *
      * @return     The number of stored entries.
      */
     public int getNumberEntries()
     {
          return entries.size();
     }
     
     
     /**
      * Returns the values stored in each entry in the same order that they
      * are stored in this Entries object.
      *
      * @return     The value stored in each Entry.
      */
     public String[] getValues()
     {
          String[] to_return = new String[entries.size()];
          for (int i = 0; i < to_return.length; i++)
               to_return[i] = ((Entry) entries.get(i)).getValue();
          return to_return;
     }
     
     
     /**
      * Returns the indexes stored in each entry in the same order that they
      * are stored in this Entries object.
      *
      * @return     The indexes stored in each Entry. The first index identifies
      *             the Entry and the second identifies the index stored in
      *             that Entry.
      */
     public int[][] getIndexes()
     {
          int[][] to_return = new int[entries.size()][];
          for (int i = 0; i < to_return.length; i++)
               to_return[i] = ((Entry) entries.get(i)).getIndexes();
          return to_return;
     }
     
     
     /**
      * Returns the combined data stored in the indexes field for all entries
      * that have null value fields.
      *
      * @return     The combined indexes for all entries with null values.
      *             Null is returned if there are no entries with null values.
      */
     public int[] getIndexesOfNullEntries()
     {
          Vector<Integer> indexes_of_null_entries = new Vector<Integer>();
          int number_entries = entries.size();
          for (int i = 0; i < number_entries; i++)
               if (((Entry) entries.get(i)).getValue() == null)
               {
               int[] these_indexes = ((Entry) entries.get(i)).getIndexes();
               for (int j = 0; j < these_indexes.length; j++)
                    indexes_of_null_entries.add(new Integer(these_indexes[j]));
               }
          
          if (indexes_of_null_entries.size() == 0)
               return null;
          int[] to_return = new int[indexes_of_null_entries.size()];
          for (int i = 0; i < to_return.length; i++)
               to_return[i] = ((Integer) indexes_of_null_entries.get(i)).intValue();
          return to_return;
     }
     
     
     /**
      * Get the MergeReport objects stored in this Entries object. These merge
      * reports cumulatively describe every merge operation that occured trough
      * all calls to this Entries object's mergeIdenticalEntries method (when
      * the mergeIdenticalEntries method's report_in_merge_report parameter
      * was set to true). There is a separate MergeReport object in the returned
      * array for each time a <i>new</i> value was merged.
      *
      * <p>Note that the MergeReport array returned may contain MergeReport
      * objects that may be subsets of others. Subsets may be subsumed into
      * supersets by subsequently using the MergeReport class' static
      * mergeSubsetsIntoSupersets method.
      *
      * @return     A cumulative array of all merges performed by calls to the
      *             mergeIdenticalEntries method with the report_in_merge_report
      *             parameter set to true. Null is returned if no merges were
      *             performed.
      */
     public MergeReport[] getMergeReport()
     {
          MergeReport[] report = merge_report.toArray(new MergeReport[1]);
          if (report[0] == null)
               return null;
          else return report;
     }
     
     
     /**
      * Returns a new Entries object containing all Entry objects that are
      * contained in this Entries object and have at least two indexes stored
      * in them. This original Entries object is not changed. This objects
      * merge_report parameter is NOT preserved.
      *
      * @return               Returns all Entry objects with at least two
      *                       indexes. Returns null if there are no such Entry
      *                       objects.
      * @throws     Exception Throws an Exception if a problem occurs.
      */
     public Entries getMultipleEntries()
     throws Exception
     {
          if (entries.size() == 0) return null;
          
          SortableVector<Entry> new_entries = new SortableVector<Entry>(new EntryCompare());
          int number = entries.size();
          for (int i = 0; i < number; i++)
               if (entries.get(i).getIndexes().length > 1)
                    new_entries.add(entries.get(i));
          
          return new Entries(new_entries, sorted, new Vector<MergeReport>());
     }
     
     
     /**
      * Converts the value field of each stored entry to lower case. Marks the
      * entries as unsorted.
      */
     public void convertValuesToLowerCase()
     {
          int size = entries.size();
          for (int i = 0; i < size; i++)
          {
               if (entries.get(i).getValue() != null)
                    entries.get(i).setValue(entries.get(i).getValue().toLowerCase());
          }
          sorted = false;
     }
     
     
     /**
      * Removes all spaces and numerical digits from the beginnings of all
      * value fields of all stored Entry objects in the entries field. This
      * means that all spaces and numerical digits are removed until a non-space
      * and non-numerical digit character is encountered, at which point
      * no more changes are made.
      *
      * <p>No changes are made at all to a value if all of this would result in
      * all of the contents of a value being deleted (i.e. if only numerical
      * digits and/or spaces are present).
      *
      * @return     Null is returned if no changes are made. If changes are
      *             made, then the before and after values corresponding to each
      *             change are returned. The first dimension of the returned
      *             array represents a different Entry that was changed, and
      *             the second dimension has a size of 2. The value at index 0
      *             is the before value of the Entry field, and the value at
      *             index 1 is the after value.
      */
     public String[][] removeNumbersAndSpacesAtBeginningOfValues()
     {
          // Cumulative replacements made
          Vector<String[]> changes = new Vector<String[]>();
          
          // Perform changes
          int number_entries = entries.size();
          for (int i = 0; i < number_entries; i++)
          {
               Entry this_entry = entries.get(i);
               String this_value = this_entry.getValue();
               String original = this_entry.getValue();
               boolean done = false;
               boolean changed = false;
               while (!done)
               {
                    if (this_value == null)
                         done = true;
                    else if (this_value.length() == 0)
                         done = true;
                    else
                    {
                         char this_char = this_value.charAt(0);
                         if ( this_char == '0' || this_char == '1' || this_char == '2' ||
                              this_char == '3' || this_char == '4' || this_char == '5' ||
                              this_char == '6' || this_char == '7' || this_char == '8' ||
                              this_char == '9' || this_char == ' ')
                         {
                              changed = true;
                              if (this_value.length() == 1)
                              {
                                   changed = false;
                                   done = true;
                              }
                              else
                                   this_value = this_value.substring(1);
                         }
                         else
                              done = true;
                    }
               }
               
               if (changed)
               {
                    // Store the new value post-changes
                    this_entry.setValue(this_value);
                    
                    // Store a report of the change
                    String[] change = {original, this_value};
                    changes.add(change);
               }
          }
          
          // Return a report of the changes made
          if (changes.size() == 0)
               return null;
          else
          {
               sorted = false;
               return changes.toArray(new String[1][1]);
          }
     }
     
     
     /**
      * Replaces each occurence of the given regular expression in any value
      * field of any of the stored Entry objects with the given replacement
      * parameter. If at least one change is made, the stored entries are marked
      * as unsorted.
      *
      * <p>Note that no change is made to a given Entry's value field if the
      * change would result in a string of length 0.
      *
      * @param regex               The string to search for.
      * @param replacement         The stirng to replace all occurences of
      *                            to_find with.
      * @return                    If no occurences of to_find are found, then
      *                            null is returned. If changes are made, then
      *                            the before and after values are returned.
      *                            The first dimension represents a different
      *                            Entry that was changed, and the second
      *                            dimension has a size of 2. The value at
      *                            index 0 is the before value of the Entry
      *                            field, and the value at index 1 is the
      *                            after value.
      */
     public String[][] findAndReplaceValues(String regex, String replacement)
     {
          // Convert searches for periods so that they work in a regular
          // expression sense
          if (regex.equals("."))
               regex = "\\.";
          
          Vector<String[]> changes = new Vector<String[]>();
          
          int size = entries.size();
          for (int i = 0; i < size; i++)
          {
               String original = entries.get(i).getValue();
               if (original != null)
               {
                    String result = original.replaceAll(regex, replacement);
                    if (result.length() != 0)
                    {
                         if (!result.equals(original))
                         {
                              String[] change = {original, result};
                              changes.add(change);
                              entries.get(i).setValue(result);
                         }
                    }
               }
          }
          
          if (changes.size() == 0)
               return null;
          else
          {
               sorted = false;
               return changes.toArray(new String[1][1]);
          }
     }
     
     
     /**
      * Processes the stored entries and merges all <i>adjacent</i> entries that
      * have the same value field. Entries with duplicate value fields are
      * merged into a single entry, and the contents of the indexes field of
      * each such entry are all combined and stored in the resulting entry.
      *
      * <p>In order for this to be effective, the entries must first be sorted.
      * This method therefore automatically sorts them if they have not already
      * been sorted.
      *
      * <p>Entries that both have null values ARE merged.
      *
      * <p>This method returns a string array that contains one entry for each
      * value field that was merged. If multiple entries with the same value
      * are merged, then the value merged is only reported once. The total
      * number of changes can be determined by calling the the getNumberEntries
      * method before and after this mergeIdenticalEntries method.
      *
      * <p>If the report_in_merge_report parameter is true, then a report of
      * each merge that occurs is also stored in this Entries object's merge
      * report. A separate MergeReport object is generated and added to the
      * report each time that a new value is merged.
      *
      * @param  report_in_merge_report  Whether or not merges performed in this
      *                                 call to mergeIdenticalEntries are to be
      *                                 reported in the merge report stored
      *                                 by this Entries object.
      * @param  merge_reason            A description of the reason that the
      *                                 merge is being performed. Is only
      *                                 relevant if report_in_merge_report is
      *                                 true, since it's only relevance is to
      *                                 be stored in the merge report. May be
      *                                 null.
      * @return                         A unique list of all field values that
      *                                 were merged once or more. Returns null
      *                                 if no merges were performed.
      */
     public String[] mergeIdenticalEntries(boolean report_in_merge_report,
          String merge_reason)
     {
          // Sort entires if they have not already been sorted
          if (!sorted)
               sortEntries();
          
          // The changes to report
          Vector<String> changes = new Vector<String>();
          
          // Variables relating to the merge report
          MergeReport current_merge_report = null;
          String value_for_current_merge_report = null;
          
          // Perorm merging and generate reports
          int current = 0;
          while (current < entries.size() - 1)
          {
               Entry this_entry = (Entry) entries.get(current);
               Entry next_entry = (Entry) entries.get(current + 1);
               
               boolean merged = this_entry.mergeIfIdentical(next_entry);
               
               if (merged)
               {
                    // Update changes report
                    String merged_value = next_entry.getValue();
                    if (changes.size() == 0)
                    {
                         if (merged_value == null)
                              changes.add("UNKNOWN");
                         else
                              changes.add(merged_value);
                    }
                    else if (merged_value == null)
                    {
                         if (!changes.lastElement().equals("UNKNOWN"))
                              changes.add("UNKNOWN");
                    }
                    else if (!merged_value.equals((String) changes.lastElement()))
                         changes.add(merged_value);
                    
                    // Update merge report
                    if (report_in_merge_report && merged_value != null)
                    {
                         int[] this_indexes = this_entry.getIndexes(); // because merged, also contains next_indexes found below
                         int[] next_indexes = next_entry.getIndexes();
                         
                         if (current_merge_report == null)
                         {
                              current_merge_report = new MergeReport(merge_reason, this_indexes);
                              value_for_current_merge_report = merged_value;
                         }
                         else if (value_for_current_merge_report.equals(merged_value))
                              current_merge_report.addAdditionalIndexes(next_indexes);
                         else
                         {
                              merge_report.add(current_merge_report);
                              current_merge_report = new MergeReport(merge_reason, this_indexes);
                              value_for_current_merge_report = merged_value;
                         }
                    }
                    
                    // Remove merged entry
                    entries.remove(current + 1);
               }
               else
                    current++;
          }
          
          // Add the last report to the merge report
          if (current_merge_report != null)
               merge_report.add(current_merge_report);
          
          // Return the results
          if (changes.size() == 0)
               return null;
          else
               return changes.toArray(new String[1]);
     }
     
     
     /**
      * Processes the stored Entry objects and merges all Entry objects whose
      * value fields contain a fraction of matching words in any order equal to
      * or greater than the fraction_match parameter. Such Entry objects are
      * merged into a single Entry, and the contents of the indexes field of
      * each such entry are all combined and stored in the resulting entry. The
      * resulting Entry object is assigned whichever value field is longer.
      *
      * <p>This method returns a string array that contains one entry for each
      * merge that occured. If multiple Entry objects with sufficiently matching
      * values are merged, then the value merged is only reported once. The
      * total number of changes can be determined by calling the the
      * getNumberEntries method before and after this mergeIdenticalEntries
      * method.
      *
      * <p>If the report_in_merge_report parameter is true, then a report of
      * each merge that occurs is also stored in this Entries object's merge
      * report. A separate MergeReport object is generated and added to the
      * report each time that a new merge occurs.
      *
      * <p>Whether or not two given Entry objects are merged is determined by
      * first breaking their value fields into tokens delimited by spaces (i.e.
      * into words). The fraction of words that are identical between the two
      * Entry objects is then calculated. Ordering is not considered. If this
      * fraction is greater or equal to the given fraction_match parameter, then
      * a merge occurs.
      *
      * <p>The is_subset_calculation parameter is used to determine how the
      * fraction of identical tokens is calculated. If this parameter is false,
      * then the denominator in the fraction is the number of words in the
      * Entry with the larger number of words. If this parameter is true, then
      * the denominator is the Entry with the fewer number of words. In
      * practice, the is_subset_calculation parameter should be true when
      * testing to see if one value is a subset of the other, and it should
      * be false when one is simply considering differences in ordering.
      *
      * <p>If the do_not_merge_identicals parameter is true, then no merge
      * occurs if the words in the value fields of the two Entry objects are
      * identical and occur in the same order, as this sort of merge should be
      * performed elsewhere.
      *
      * <p><i>NO</i> merge occurs between two Entry objects if either of their
      * value fields are null.
      *
      * @param  fraction_match          The minimum fraction of words that must
      *                                 match, in any order,  in order for a
      *                                 merge to be performed. Must be between
      *                                 0.0 and 1.0.
      * @param  do_not_merge_identicals If this is true, then Entry objects
      *                                 whose words in their value fields are
      *                                 identical AND occur in the same order
      *                                 are not merged.
      * @param  is_subset_calculation   Whether or not merge calculations are
      *                                 testing whether the words in one Entry's
      *                                 value field are a subset of the other's
      *                                 (see overall description above).
      * @param  report_in_merge_report  Whether or not merges performed in this
      *                                 call to mergeEntriesIgnoringWordOrder
      *                                 are to be reported in the merge report
      *                                 stored by this Entries object.
      * @return                         A unique list of all field values that
      *                                 were merged once or more. Returns null
      *                                 if no merges were performed.
      */
     public String[] mergeEntriesIgnoringWordOrder(double fraction_match,
          boolean do_not_merge_identicals, boolean is_subset_calculation,
          boolean report_in_merge_report)
     {
          // Find which ones should be merged
          int number_entries = entries.size();
          boolean[][] ones_to_merge = new boolean[number_entries][number_entries];
          for (int i = 0; i < number_entries; i++)
          {
               for (int j = 0; j <= i; j++)
               {
                    ones_to_merge[i][j] = shouldMergeIgnoringWordOrder(entries.get(i), entries.get(j), fraction_match, do_not_merge_identicals, is_subset_calculation);
                    ones_to_merge[j][i] = ones_to_merge[i][j];
               }
          }
          
          // To store merges that should occur
          Vector<int[]> merges_found = new Vector<int[]>();
          
          // The list of changes to return
          Vector<String> changes = new Vector<String>();
          
          // Go through rows of ones_to_merge one by one and fill in
          // merges_found with one entry each for each row that has at least one
          // merge
          for (int i = 0; i < number_entries; i++)
          {
               // To store results for this row
               boolean found_merge_for_this_i = false;
               boolean[] merges_for_this_index = new boolean[number_entries];
               for (int j = 0; j < number_entries; j++)
                    merges_for_this_index[j] = false;
               
               // Go through entries in this row one by one
               for (int j = 0; j < number_entries; j++)
               {
                    // Do not merge on diagonal
                    if (j != i)
                    {
                         // Mark it if a merge should be performed for this entry
                         if (ones_to_merge[i][j])
                         {
                              found_merge_for_this_i = true;
                              merges_for_this_index[i] = true;
                              merges_for_this_index[j] = true;
                              
                              // Set this and the symetrically corresponding entry to
                              // false to avoid a redundant merge
                              ones_to_merge[i][j] = false;
                              ones_to_merge[j][i] = false;
                         }
                    }
               }
               
               // Store merges for this row, if any
               if (found_merge_for_this_i)
               {
                    // Store indexes of merge
                    Vector<Integer> found_indexes = new Vector<Integer>();
                    for (int j = 0; j < number_entries; j++)
                         if (merges_for_this_index[j])
                              found_indexes.add(new Integer(j));
                    int[] found_indexes_array = new int[found_indexes.size()];
                    for (int j = 0; j < found_indexes_array.length; j++)
                         found_indexes_array[j] = found_indexes.get(j).intValue();
                    merges_found.add(found_indexes_array);
               }
          }
          
          // Go through each set of merges indicated by merges_found and
          // merge_reasons_found and combine every set that has at least one
          // entry in common
          int set_1 = 0;
          while (set_1 < merges_found.size() - 1)
          {
               // Keep processing this set until no more common entries found
               // with any other set
               boolean made_a_change = true;
               while (made_a_change)
               {
                    // Default to no change made
                    made_a_change = false;
                    
                    // Compare with each other set
                    for (int set_2 = set_1 + 1; set_2 < merges_found.size(); set_2++)
                    {
                         // Compare entries one by one between two sets
                         int[] first = merges_found.get(set_1);
                         int[] second = merges_found.get(set_2);
                         for (int i = 0; i < first.length; i++)
                         {
                              for (int j = 0; j < second.length; j++)
                              {
                                   // If the two sets contain an entry in common
                                   if (first[i] == second[j])
                                   {
                                        // Note that a change was made
                                        made_a_change = true;
                                        
                                        // Combine the two sets
                                        int[] new_merges_found = new int[first.length + second.length];
                                        for (int k = 0; k < first.length; k++)
                                             new_merges_found[k] = first[k];
                                        for (int k = 0; k < second.length; k++)
                                             new_merges_found[k + first.length] = second[k];
                                        
                                        // Remove redundancies from the combined sets
                                        new_merges_found = mckay.utilities.staticlibraries.MathAndStatsMethods.removeRedundantEntries(new_merges_found);
                                        
                                        // Store the new set
                                        merges_found.set(set_1, new_merges_found);
                                        
                                        // Remove the old set
                                        merges_found.remove(set_2);
                                        set_2--;
                                        
                                        // Exit loop
                                        i = first.length;
                                        j = second.length;
                                   }
                              }
                         }
                    }
               }
               set_1++;
          }
          
          // Find the longest value for each set of Entry objects to be merged,
          // which will be used as the new value
          String[] merge_values = new String[merges_found.size()];
          for (int i = 0; i < merge_values.length; i++)
          {
               String[] pre_merge_values = new String[merges_found.get(i).length];
               for (int j = 0; j < pre_merge_values.length; j++)
                    pre_merge_values[j] = entries.get(merges_found.get(i)[j]).getValue();
               int index_of_longest = mckay.utilities.staticlibraries.StringMethods.getIndexOfLongestString(pre_merge_values);
               merge_values[i] = pre_merge_values[index_of_longest];
          }
          
          // Use merges_found to perform merges. Keep track of ones that are
          // being subsumed.
          Vector<Integer> being_subsumed = new Vector<Integer>();
          for (int i = 0; i < merges_found.size(); i++)
          {
               // Perform merges
               int[] these_merges = merges_found.get(i);
               int recipient_entriy_index = these_merges[0];
               for (int j = 1; j < these_merges.length; j++)
               {
                    entries.get(recipient_entriy_index).merge(entries.get(these_merges[j]), false);
                    being_subsumed.add(new Integer(these_merges[j]));
               }
               
               // Set the new value field
               entries.get(recipient_entriy_index).setValue(merge_values[i]);
               
               // Report the change
               changes.add(merge_values[i]);
               
               // Generate the merge report
               if(report_in_merge_report)
               {
                    String reason_report = null;
                    if (is_subset_calculation) reason_report = "Word subset, neglecting order";
                    else reason_report = "Word ordering, possible subset";
                    merge_report.add(new MergeReport(reason_report, entries.get(recipient_entriy_index).getIndexes()));
               }
          }
          
          // Remove Entry objects that have been merged into other Entry objects
          int[] to_delete = new int[being_subsumed.size()];
          for (int i = 0; i < to_delete.length; i++)
               to_delete[i] = being_subsumed.get(i).intValue();
          for (int i = 0; i < to_delete.length; i++)
          {
               entries.remove(to_delete[i]);
               for (int j = i + 1; j < to_delete.length; j++)
                    if (to_delete[j] > to_delete[i])
                         to_delete[j]--;
          }
          
          // Return the results and note if became unsorted
          if (changes.size() == 0)
               return null;
          else
          {
               sorted = false;
               return changes.toArray(new String[1]);
          }
     }
     
     
     /**
      * Merges all Entry objects stored in the entries field of this Entries
      * object that have at least one corresponding value of true in the given
      * ones_to_merge array. Also collects into one all merges that have any
      * one entry in common (i.e. if the ones_to_merge paramter specifies that
      * Entry A should merge with Entry B, and that Entry B should merge with
      * Entry C, then Entry objects A, B and C will all be merged together, even
      * if ones_to_merge does not specify that A and C should merge.
      *
      * <p><b>IMPORTANT:</b> The given ones_to_merge array will be altered, so
      * it is important that the calling method does not need to resuse it.
      *
      * <p>A merge means that multiple Entry objects are joined together into
      * one which replaces all those Entry objects involved in the merge. The
      * contents of the new Entry object's indexes field consist of the combined
      * indexes of the parent merged Entry objects. The new value field is
      * the value field of the parent with the longest value field.
      *
      * <p>If the report_in_merge_report parameter is true, then a report of
      * each merge that occurs is also stored in this Entries object's merge
      * report. A separate MergeReport object is generated and added to the
      * report each time that a new merge occurs.
      *
      * @param ones_to_merge            A set of 2-D matrixes that each specify
      *                                 which Entry objects stored in the
      *                                 entries field should be merged. The first
      *                                 dimension specifies the set and the
      *                                 other two dimensions are the dimensions
      *                                 of each matrix. Each matrix must be
      *                                 symetrical and each of its two
      *                                 dimensions must correspond in size and
      *                                 order to the Entry objects stored in the
      *                                 entries field. Only one entry of one
      *                                 of the matrixes must be true for a merge
      *                                 to occur. Each matrix corresponds to a
      *                                 different reason as to why a merge could
      *                                 occur.
      * @param merge_reasons            The reasons for merging. Corresponds to
      *                                 the first dimension of the ones_to_merge
      *                                 parameter.
      * @param  report_in_merge_report  Whether or not merges performed in this
      *                                 call to mergeSpecifiedEntries
      *                                 are to be reported in the merge report
      *                                 stored by this Entries object.
      */
     public void mergeSpecifiedEntries(boolean[][][] ones_to_merge,
          String[] merge_reasons, boolean report_in_merge_report)
     {
          // Save time by pre-calculating constants
          int number_entries = ones_to_merge[0].length;
          int number_sets = ones_to_merge.length;
          
          // To store merges that should occur
          Vector<int[]> merges_found = new Vector<int[]>();
          Vector<boolean[]> merge_reasons_found = new Vector<boolean[]>();
          
          // Go through rows of ones_to_merge one by one and fill in
          // merges_found and merge_reasons found with one entry each for each
          // row that has at least one merge
          for (int i = 0; i < number_entries; i++)
          {
               // To store results for this row
               boolean found_merge_for_this_i = false;
               boolean[] merges_for_this_index = new boolean[number_entries];
               for (int j = 0; j < number_entries; j++)
                    merges_for_this_index[j] = false;
               boolean[] merge_reasons_for_this_index = new boolean[number_sets];
               for (int j = 0; j < number_sets; j++)
                    merge_reasons_for_this_index[j] = false;
               
               // Go through entries in this row one by one
               for (int j = 0; j < number_entries; j++)
               {
                    // Do not merge on diagonal
                    if (j != i)
                    {
                         // See if a merge should be performed for this entry
                         boolean one_is_true = false;
                         for (int set = 0; set < number_sets; set++)
                         {
                              if (ones_to_merge[set][i][j])
                              {
                                   merge_reasons_for_this_index[set] = true;
                                   one_is_true= true;
                              }
                         }
                         
                         // Mark it if a merge should be performed for this entry
                         if (one_is_true)
                         {
                              found_merge_for_this_i = true;
                              merges_for_this_index[i] = true;
                              merges_for_this_index[j] = true;
                              
                              // Set this and the symetrically corresponding entry to
                              // false to avoid a redundant merge
                              for (int set = 0; set < number_sets; set++)
                              {
                                   ones_to_merge[set][i][j] = false;
                                   ones_to_merge[set][j][i] = false;
                              }
                         }
                    }
               }
               
               // Store merges for this row, if any
               if (found_merge_for_this_i)
               {
                    // Store indexes of merge
                    Vector<Integer> found_indexes = new Vector<Integer>();
                    for (int j = 0; j < number_entries; j++)
                         if (merges_for_this_index[j])
                              found_indexes.add(new Integer(j));
                    int[] found_indexes_array = new int[found_indexes.size()];
                    for (int j = 0; j < found_indexes_array.length; j++)
                         found_indexes_array[j] = found_indexes.get(j).intValue();
                    merges_found.add(found_indexes_array);
                    
                    // Note the reasons for the merge
                    merge_reasons_found.add(merge_reasons_for_this_index);
               }
          }
          
          // Go through each set of merges indicated by merges_found and
          // merge_reasons_found and combine every set that has at least one
          // entry in common
          int set_1 = 0;
          while (set_1 < merges_found.size() - 1)
          {
               // Keep processing this set until no more common entries found
               // with any other set
               boolean made_a_change = true;
               while (made_a_change)
               {
                    // Default to no change made
                    made_a_change = false;
                    
                    // Compare with each other set
                    for (int set_2 = set_1 + 1; set_2 < merges_found.size(); set_2++)
                    {
                         // Compare entries one by one between two sets
                         int[] first = merges_found.get(set_1);
                         int[] second = merges_found.get(set_2);
                         for (int i = 0; i < first.length; i++)
                         {
                              for (int j = 0; j < second.length; j++)
                              {
                                   // If the two sets contain an entry in common
                                   if (first[i] == second[j])
                                   {
                                        // Note that a change was made
                                        made_a_change = true;
                                        
                                        // Combine the two sets
                                        int[] new_merges_found = new int[first.length + second.length];
                                        for (int k = 0; k < first.length; k++)
                                             new_merges_found[k] = first[k];
                                        for (int k = 0; k < second.length; k++)
                                             new_merges_found[k + first.length] = second[k];
                                        
                                        // Remove redundancies from the combined sets
                                        new_merges_found = mckay.utilities.staticlibraries.MathAndStatsMethods.removeRedundantEntries(new_merges_found);
                                        
                                        // Store the new set
                                        merges_found.set(set_1, new_merges_found);
                                        
                                        // Combine the merge reasons
                                        for (int k = 0; k < number_sets; k++)
                                             if (merge_reasons_found.get(set_2)[k])
                                                  merge_reasons_found.get(set_1)[k] = true;
                                        
                                        // Remove the old set
                                        merges_found.remove(set_2);
                                        merge_reasons_found.remove(set_2);
                                        set_2--;
                                        
                                        // Exit loop
                                        i = first.length;
                                        j = second.length;
                                   }
                              }
                         }
                    }
               }
               set_1++;
          }
          
          // Find the longest value for each set of Entry objects to be merged,
          // which will be used as the new value
          String[] merge_values = new String[merges_found.size()];
          for (int i = 0; i < merge_values.length; i++)
          {
               String[] pre_merge_values = new String[merges_found.get(i).length];
               for (int j = 0; j < pre_merge_values.length; j++)
                    pre_merge_values[j] = entries.get(merges_found.get(i)[j]).getValue();
               int index_of_longest = mckay.utilities.staticlibraries.StringMethods.getIndexOfLongestString(pre_merge_values);
               merge_values[i] = pre_merge_values[index_of_longest];
          }
          
          // Use merges_found to perform merges. Keep track of ones that are
          // being subsumed.
          Vector<Integer> being_subsumed = new Vector<Integer>();
          for (int i = 0; i < merges_found.size(); i++)
          {
               // Perform merges
               int[] these_merges = merges_found.get(i);
               int recipient_entriy_index = these_merges[0];
               for (int j = 1; j < these_merges.length; j++)
               {
                    entries.get(recipient_entriy_index).merge(entries.get(these_merges[j]), false);
                    being_subsumed.add(new Integer(these_merges[j]));
               }
               
               // Set the new value field
               entries.get(recipient_entriy_index).setValue(merge_values[i]);
               
               // Generate the merge report
               if(report_in_merge_report)
               {
                    String reason_report = null;
                    boolean[] these_merge_reasons_found = merge_reasons_found.get(i);
                    for (int j = 0; j < these_merge_reasons_found.length; j++)
                    {
                         if (these_merge_reasons_found[j])
                         {
                              if (reason_report == null) reason_report = new String();
                              else reason_report += " + ";
                              reason_report += merge_reasons[j];
                         }
                    }
                    
                    merge_report.add(new MergeReport(reason_report, entries.get(recipient_entriy_index).getIndexes()));
               }
          }
          
          // Remove Entry objects that have been merged into other Entry objects
          int[] to_delete = new int[being_subsumed.size()];
          for (int i = 0; i < to_delete.length; i++)
               to_delete[i] = being_subsumed.get(i).intValue();
          for (int i = 0; i < to_delete.length; i++)
          {
               entries.remove(to_delete[i]);
               for (int j = i + 1; j < to_delete.length; j++)
                    if (to_delete[j] > to_delete[i])
                         to_delete[j]--;
          }
          
          // Note if array now unsorted
          if (merges_found.size() != 0)
               sorted = false;
     }
     
     
     /**
      * Sort the stored Entry objects alphabeticall objects based on their value
      * fields. The sort is performed using the String compareTo method, without
      * any special processing such as change of case. After sorting is
      * complete, the entries are marked as sorted.
      *
      * <p>Sorting only occurs if the entries are marked as unsorted. If they
      * are already marked as sorted, then no processing is performed.
      */
     public void sortEntries()
     {
          if (!sorted)
          {
               entries.sort();
               sorted = true;
          }
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     private static boolean shouldMergeIgnoringWordOrder(Entry entry_1,
          Entry entry_2, double fraction_match, boolean do_not_merge_identicals,
          boolean is_subset_calculation)
     {
          // Do not perform merge if either value is null
          if (entry_1.getValue() == null || entry_2.getValue() == null)
               return false;
          
          // Break both values into tokens
          String[] tokens_1 = mckay.utilities.staticlibraries.StringMethods.breakIntoTokens(entry_1.getValue(), " ");
          String[] tokens_2 = mckay.utilities.staticlibraries.StringMethods.breakIntoTokens(entry_2.getValue(), " ");
          
          // Do not perform merge is number of tokens is too far apart. This
          // is done in order to save processing only.
          if (!is_subset_calculation)
          {
               double size_ratio = ((double) tokens_1.length) / ((double) tokens_2.length);
               double inverse_size_ratio = ((double) tokens_2.length) / ((double) tokens_1.length);
               if (size_ratio < fraction_match || inverse_size_ratio < fraction_match) return false;
          }
          
          // Return false if the tokens are identical
          if (do_not_merge_identicals)
          {
               if (tokens_1.length == tokens_2.length)
               {
                    boolean a_difference = false;
                    for (int i = 0; i < tokens_1.length; i++)
                    {
                         if (!tokens_1[i].equals(tokens_2[i]))
                         {
                              a_difference = true;
                              i = tokens_1.length;
                         }
                    }
                    if(!a_difference) return false;
               }
          }
          
          // Find the number of tokens that match
          int matching = 0;
          for (int i = 0; i < tokens_1.length; i++)
               for (int j = 0; j < tokens_2.length; j++)
                    if (tokens_1[i].equals(tokens_2[j]))
                    {
               matching++;
               tokens_2[j] = "";
               j = tokens_2.length;
                    }
          
          // Determine the number of tokens to use when testing fraction
          double number_tokens;
          if (is_subset_calculation)
          {
               if (tokens_1.length < tokens_2.length)
                    number_tokens = (double) tokens_1.length;
               else number_tokens = (double) tokens_2.length;
          }
          else
          {
               if (tokens_1.length > tokens_2.length)
                    number_tokens = (double) tokens_1.length;
               else number_tokens = (double) tokens_2.length;
          }
          
          // Determine whether or not a sufficient number match
          double matching_fraction = ((double) matching) / number_tokens;
          if (matching_fraction < fraction_match) return false;
          
          // Merge should be performed
          else return true;
     }
     
     
     /* INTERNAL CLASSES ******************************************************/
     
     
     /**
      * An implementation of the QuicksortComparator class that compares
      * the value fields of Entry objects. Since these are strings, comparison
      * results are alphabetical. No conversion to upper or lower case is made,
      * so upper and lower case values are NOT considered equivalent.
      *
      * <p>Null string values are considered greater than non-null string
      * contents.
      */
     private class EntryCompare
          implements QuicksortComparator
     {
          /**
           * Compares the values of the value fields of the two given Entry
           * objects. No conversion to upper or lower case is made, so upper and
           * lower case values are NOT considered equivalent.
           *
           * <p>Null string values are considered greater than non-null
           * string contents.
           *
           * @param first_entry    The first Entry to compare.
           * @param second_entry   The second Entry to compare.
           * @return               0 if first and second values are equal, a
           *                       negative integer if the first value is less
           *                       than the second or a positive integer if the
           *                       first value is greater than the second.
           */
          public int compare(Object first_entry, Object second_entry)
          {
               String first_string = ((Entry) first_entry).getValue();
               String second_string = ((Entry) second_entry).getValue();
               
               if (first_string == null && second_string == null)
                    return 0;
               else if (first_string == null || second_string == null)
               {
                    if (first_string == null)
                         return 1;
                    else if (second_string == null)
                         return -1;
               }
               
               return first_string.compareTo(second_string);
          }
     }
}
