/*
 * AnalysisProcessor.java
 * Version 1.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jMusicMetaManager;

import java.io.*;
import java.util.Vector;
import mckay.utilities.general.HTMLWriter;
import mckay.utilities.gui.progressbars.DoubleProgressBarTaskMonitor;
import jMusicMetaManager.gui.OptionsPanel;
import jMusicMetaManager.processingstructures.*;


/**
 * This class performs the actual processing to detect inconsistencies and
 * redundancies in recording metadata. It has two constructors, one of which
 * is used if a command line version of jMusicMetaManager is being used, and
 * the other of which is used if the GUI is being used. In either case, the
 * constructor provides the AnalysisPreferences that specify the data to be
 * analyzed and the types of analysis to be performed.
 *
 * <p>Actual processing is performed when the performProcessing method is
 * called. The results of the analyses are saved to HTML files as specified
 * in the AnalysisPreferences passed to the constructor.
 *
 * @author Cory McKay
 */
public class AnalysisProcessor
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The preferences to base analysis on.
      */
     private AnalysisPreferences             preferences;
     
     /**
      * The merged metadata for all recordings extracted from both an iTunes XML
      * file and/or the ID3 tags of MP3 recordings. If there is a conflict
      * for a field between the metadata from an iTunes file and an ID3 tag,
      * then the iTunes metadata is used.
      */
     private RecordingMetaData[]             recordings;
     
     /**
      * All extracted recording titles and links to the indexes in the
      * recordings field to which they each correspond. Entries are typically
      * kept sorted alphabetically.
      */
     private Entries                         titles;
     
     /**
      * All extracted recording artists and links to the indexes in the
      * recordings field to which they each correspond. Entries are typically
      * kept sorted alphabetically. Identical entries are also merged.
      */
     private Entries                         artists;
     
     /**
      * All extracted recording artists and links to the indexes in the
      * recordings field to which they each correspond. Entries are typically
      * kept sorted alphabetically. Identical entries are also merged.
      */
     private Entries                         composers;
     
     /**
      * All extracted recording artists and links to the indexes in the
      * recordings field to which they each correspond. Entries are typically
      * kept sorted alphabetically. Identical entries are also merged.
      */
     private Entries                         albums;
     
     /**
      * All extracted recording artists and links to the indexes in the
      * recordings field to which they each correspond. Entries are typically
      * kept sorted alphabetically. Identical entries are also merged.
      */
     private Entries                         genres;
     
     /**
      * The sub-directory that reports are being saved in.
      */
     private File                            subdirectory;
     
     /**
      * The stream used to save links to each of the generated report files.
      */
     private DataOutputStream                contents_stream;
     
     /**
      * An optional progress bar to monitor processing. Is set to null if is
      * not to be used.
      */
     private DoubleProgressBarTaskMonitor    progress_bar;
     
     /**
      * The GUI generating this object. Will be null if no GUI is present.
      */
     private OptionsPanel                    calling_gui_panel;
     
     /**
      * The total number of subtasks that need to be performed. Used by the
      * progress bar.
      */
     private int                             number_tasks;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new instance of AnalysisProcessor that stores the settings
      * to base processing upon. This constructor should be used if no GUI
      * is present.
      *
      * @param preferences    The settings to base processing upon.
      */
     public AnalysisProcessor(AnalysisPreferences preferences)
     {
          this.preferences = preferences;
          progress_bar = null;
          calling_gui_panel = null;
     }
     
     
     /**
      * Creates a new instance of AnalysisProcessor that stores the settings
      * to base processing upon and a progress bar that will be used to monitor
      * the progress of the analysis. This constructor should be used if a GUI
      * is present.
      *
      * @param preferences         The settings to base processing upon.
      * @param calling_gui_panel   The GUI panel instantiating this object.
      * @param progress_bar        The progress bar that will monitor processing.
      */
     public AnalysisProcessor(AnalysisPreferences preferences,
          OptionsPanel calling_gui_panel,
          DoubleProgressBarTaskMonitor progress_bar)
     {
          this.preferences = preferences;
          this.calling_gui_panel = calling_gui_panel;
          this.progress_bar = progress_bar;
          
          // Calculate the number of sub-tasks
          number_tasks = 0;
          number_tasks++; // Read the input data
          if (preferences.list_all_non_mp3s_in_directory && preferences.mp3_directory != null)
               number_tasks++; // List all non-MP3 files in the directory
          if (preferences.list_all_recordings_found)
               number_tasks++; // Report all recordings found
          if (preferences.list_files_in_XML_but_missing && preferences.iTunes_file != null)
               number_tasks++; // Report files listed in XML but not at specified paths
          if (preferences.iTunes_file != null && preferences.mp3_directory != null)
          {
               number_tasks++; // Merge iTunes and ID3 data
               if (preferences.list_noncorresponding_recordings)
                    number_tasks++; // Report recordings that are in one source but not the other
               if (preferences.list_noncorresponding_fields)
                    number_tasks++; // Report fields that differ from one recording to another
          }
          if (preferences.list_all_postmerge_metadata)
               number_tasks++; // Report all metadata available after the merge
          number_tasks++; // Extract, sort and merge metadata
          if (preferences.list_sorted_recording_metadata)
               number_tasks++; // List the metadata of all recordings after sorting by title
          if (preferences.report_artist_breakdown)
               number_tasks++; // Report information about artists
          if (preferences.report_composer_breakdown)
               number_tasks++; // Report information about composers
          if (preferences.report_genre_breakdown)
               number_tasks++; // Report information about genres
          if (preferences.report_comment_breakdown)
               number_tasks++; // Report information about comments
          if ( preferences.list_artists_by_genre ||
               preferences.list_composers_by_genre )
               number_tasks++; // List albums and/or composers by genre
          if (preferences.report_missing_metadata)
               number_tasks++; // List all recordings missing title, artist, composer, album and/or genre metadata
          if ( preferences.list_artists_with_few_recordings ||
               preferences.list_composers_with_few_recordings )
               number_tasks++; // Report artists and/or composers with few recordings
          if ( preferences.report_wrongly_differing_titles ||
               preferences.report_identical_titles ||
               preferences.report_probable_duplicates )
               number_tasks++; // Merge titles
          if ( preferences.report_starting_with_spaces)
               number_tasks++; // Report all fields starting with spaces
          if ( preferences.list_albums_by_artist ||
               preferences.list_albums_by_composer ||
               preferences.list_incomplete_albums ||
               preferences.list_albums_with_duplicate_tracks ||
               preferences.list_albums_missing_year ||
               preferences.report_on_compilation_albums )
               number_tasks++; // List albums in various ways
          if (preferences.ignore_case_in_edit_distances && preferences.perform_find_replace_operations)
               number_tasks++; // Convert all relevant text fields to lower case
          if (preferences.perform_find_replace_operations)
               number_tasks++; // Perform find and replace operations
          if ( !preferences.perform_find_replace_operations &&
               (preferences.check_word_ordering || preferences.check_word_subset) )
               number_tasks++; // Tokenize words and test equality if word order rearranged
          if ( preferences.calculate_absolute_ED ||
               preferences.calculate_proportional_ED ||
               preferences.calculate_subset_ED )
               number_tasks++; // // Perform edit distance operations
          if ( preferences.report_probable_duplicates ||
               preferences.report_wrongly_differing_titles )
               number_tasks++; // Report summary report(s) for titles
          if ( preferences.report_wrongly_differing_artists ||
               preferences.report_wrongly_differing_composers ||
               preferences.report_wrongly_differing_albums ||
               preferences.report_wrongly_differing_genres )
               number_tasks++; // Report all summaries of probable errors (except for titles)
          if (preferences.report_options_selected)
               number_tasks++; // Report options selected to generate reports
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Perform actual analysis.
      *
      * @throws     Exception An informative report of any problem that occurs.
      */
     public void performProcessing()
     throws Exception
     {
          DataOutputStream summary_stream = null;
          try
          {
               // Initialize the HTML reports
               summary_stream = initializeReportFiles();
               
               // Read the input data and generate appropriate report of files
               // that could not be parsed, if appropriate
               RecordingMetaData[][] temp_id3 = new RecordingMetaData[1][];
               RecordingMetaData[][] temp_xml = new RecordingMetaData[1][];
               readInputData(summary_stream, temp_id3, temp_xml);
               
               // Report files found that do not end with an MP3 extension
               if (preferences.list_all_non_mp3s_in_directory && preferences.mp3_directory != null)
                    listNonMP3FilesFound();
               
               // Report all recordings found (separate list for each of the two
               // potential sources)
               if (preferences.list_all_recordings_found)
                    listAllRecordingsFound(temp_id3[0], temp_xml[0]);
               
               // Report files in iTunes XML file that are not present on disk
               // at the specified path
               if (preferences.list_files_in_XML_but_missing && preferences.iTunes_file != null)
                    listFilesInXMLButMissing(temp_xml[0]);
               
               // Merge recording metadata by matching entries from the iTunes file
               // and MP3 files on disk. The results are stored in the recordings
               // field. If only one of the input sources (i.e. just XML file or just
               // ID3 tags) is being used, then the source recordings are simply
               // copied into the recordings field. Also, if the
               // list_noncorresponding_recordings preference is set, then the
               // differences are also reported. Also, if the
               // list_noncorresponding_fields_checkbox preference is true then all
               // fields whose corresponding entries between an ID3 tag and its
               // corresponding MP3 ID3 tag differ are reported.
               mergeRecordings(temp_id3[0], temp_xml[0], summary_stream);
               temp_id3 = null;
               temp_xml = null;
               
               // Report all metadata on all recordings after the ID3 and iTunes
               // data has been merged
               if (preferences.list_all_postmerge_metadata)
                    listAllPostMergeMetadata();
               
               // Extract all titles, artists, composers, albums and genres
               // and alphebetically sort the results. Identical artist, composer,
               // album and genres are merged (but identical titles are not
               // merged, only sorted). The results are stored in the titles,
               // artists, composers, artists and genres fields.
               organizeMetadata();
               
               // List all metadata for all recordings after sorting by title
               if (preferences.list_sorted_recording_metadata)
                    listSortedRecordings(summary_stream);
               
               // Report all unique artists and stats on them
               if (preferences.report_artist_breakdown)
                    reportArtistBreakdown(summary_stream);
               
               // Report all unique composers and stats on them
               if (preferences.report_composer_breakdown)
                    reportComposerBreakdown(summary_stream);
               
               // Report all unique genres and stats on them
               if (preferences.report_genre_breakdown)
                    reportGenreBreakdown(summary_stream);
               
               // Report all unique comments and stats on them
               if (preferences.report_comment_breakdown)
                    reportCommentBreakdown();
               
               // List albums and/or composers by genre
               if ( preferences.list_artists_by_genre ||
                    preferences.list_composers_by_genre )
                    listByGenre();
               
               // List all recordings missing title, artist, composer, album
               // and/or genre metadata
               if (preferences.report_missing_metadata)
                    reportMissingMetadata();
               
               // Report artists and/or composers with few recordings
               if ( preferences.list_artists_with_few_recordings ||
                    preferences.list_composers_with_few_recordings )
                    listFewRecordings();
               
               // Merge all titles with completely identical names. Report
               // such titles if the corresponding preference is set.
               if ( preferences.report_wrongly_differing_titles ||
                    preferences.report_identical_titles ||
                    preferences.report_probable_duplicates )
                    mergeTitles();
               
               // Report all fields starting with spaces
               if ( preferences.report_starting_with_spaces)
                    detectFieldsStartingWithSpaces();
               
               // Report the total number of albums
               HTMLWriter.addParagraph(albums.getNumberEntries() + " unique albums present", summary_stream);
               
               // List albums in various ways
               if ( preferences.list_albums_by_artist ||
                    preferences.list_albums_by_composer ||
                    preferences.list_incomplete_albums ||
                    preferences.list_albums_with_duplicate_tracks ||
                    preferences.list_albums_missing_year ||
                    preferences.report_on_compilation_albums )
                    listAlbums();
               
               // Convert all titles, artists, composers, albums and genres to
               // lowercase and resort them. A report is generated detailing
               // which titles, artists, composers, albums and genres are
               // identical, except for case.
               if (preferences.ignore_case_in_edit_distances && preferences.perform_find_replace_operations)
                    convertCase();
               
               // Search through all titles, artists, composers, albums and
               // genres and perform the required replacements and tokenized
               // tests specified by the preferences
               if (preferences.perform_find_replace_operations)
                    performFindReplaceTokenizeOperations();
               
               // Perform word ordering and subset oprerations
               if ( !preferences.perform_find_replace_operations &&
                    (preferences.check_word_ordering || preferences.check_word_subset) )
                    performAllTokenizeOperations();
               
               // Perform edit distance operations
               if ( preferences.calculate_absolute_ED ||
                    preferences.calculate_proportional_ED ||
                    preferences.calculate_subset_ED )
                    performEditDistanceCalculations();
               
               // Update summary title page
               HTMLWriter.addParagraph("<h3>Probable Errors:</h3>", summary_stream);
               
               // Report probable errors for titles
               if ( preferences.report_probable_duplicates ||
                    preferences.report_wrongly_differing_titles )
                    reportProbableErrorsInvolvingTitles(summary_stream);
               
               // Report probable errors in the metadata (except for titles)
               if ( preferences.report_wrongly_differing_artists ||
                    preferences.report_wrongly_differing_composers ||
                    preferences.report_wrongly_differing_albums ||
                    preferences.report_wrongly_differing_genres )
                    reportProbableErrorsInMetadata(summary_stream);
               
               // Report options selected to generate reports
               if (preferences.report_options_selected)
                    reportOptionsSelected();
               
               // End the summary report
               HTMLWriter.endHTMLFile(summary_stream, true);
               
               // Stop the progress bar when processing is complete
               progress_bar.markTaskComplete();
               
               // Report the time logs
               if (preferences.report_processing_times && progress_bar != null)
                    reportProcessingTimes();
               
               // End the contents report
               HTMLWriter.endHTMLFile(contents_stream, false);
               
               // Display the generated reports in the generating GUI, if present
               if (calling_gui_panel != null)
               {
                    String contents_path = HTMLWriter.getContentsFramePath(preferences.save_file.getAbsolutePath());
                    String summary_path = HTMLWriter.getDefaultPageFramePath(preferences.save_file.getAbsolutePath(), "Summary");
                    calling_gui_panel.reportResults(contents_path, summary_path);
               }
          }
          catch (Exception e)
          {
               // Close outstanding streams
               if (summary_stream != null)
                    summary_stream.close();
               if (contents_stream != null)
                    contents_stream.close();
               
               // e.printStackTrace();
               throw(e);
          }
     }
     
     
     /**
      * Find the total number of subtasks that need to be performed.
      *
      * @return     The number of subtasks.
      */
     public int findNumberOfTasks()
     {
          return number_tasks;
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     

     /**
      * Prepares the report files by creating the frame root file, preparing
      * its sub-directory and creating the contents file and the default
      * summary HTML file to be shown in the right frame.
      *
      * @return     A stream to the summary HTML tile that is shown in the right
      *             frame by default.
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private DataOutputStream initializeReportFiles()
     throws Exception
     {
          File[] sub_directory_dummy = new File[1];
          DataOutputStream[] results_dummy = HTMLWriter.startNewFramesPage
               (
               preferences.save_file.getAbsolutePath(),
               mckay.utilities.staticlibraries.StringMethods.removeExtension(preferences.save_file.getName()),
               20,
               "Summary",
               sub_directory_dummy
               );
          
          HTMLWriter.addParagraph("<h3>Music Collection Overview:</h3>", results_dummy[1]);
          
          subdirectory = sub_directory_dummy[0];
          contents_stream = results_dummy[0];
          return results_dummy[1];
     }
     
     
     /**
      * Parses the MP3s and iTunes XML file refered to in the preferences
      * field. Fills recordings_id3[0] and recordings_xml[0] parameters based on
      * the results. Left as they are (which should be null) if data is not
      * to be extracted from them.
      *
      * <p>Writes the number of files parsed each of these ways to the summary
      * writer provided. Also generates a report listing MP3 files whose ID3
      * tags could not be parsed, if indicated by preferences.
      *
      * @param      writer         A writer to the summary file.
      * @param      recordings_id3 A 2-D array that will be filled by this
      *                            method with the metadata extracted from the
      *                            ID3 tags of MP3 files. The value passed to
      *                            this parameter should be:
      *                            RecordingMetaData[][] temp_id3 = new RecordingMetaData[1][];
      * @param      recordings_xml A 2-D array that will be filled by this
      *                            method with the metadata extracted from an
      *                            iTunes XML file. The value passed to
      *                            this parameter should be:
      *                            RecordingMetaData[][] temp_xml = new RecordingMetaData[1][];
      * @throws     Exception      Throws an exception if a problem occurs.
      */
     private void readInputData(DataOutputStream writer,
          RecordingMetaData[][] recordings_id3,
          RecordingMetaData[][] recordings_xml)
          throws Exception
     {
          // Update the progress bar
          if (progress_bar != null)
               progress_bar.startNewSubTask(2, "Reading input data");
          
          // Parse MP3 files and generate report of files that could not be
          // parsed.
          if (preferences.mp3_directory != null)
          {
               String[] extensions = {"mp3"};
               mckay.utilities.general.FileFilterImplementation filter = new mckay.utilities.general.FileFilterImplementation(extensions);
               File[] files = mckay.utilities.staticlibraries.FileMethods.getAllFilesInDirectory(preferences.mp3_directory, true, filter, null);
               if (files == null)
                    throw new Exception("No MP3 files found in " + preferences.mp3_directory.getAbsolutePath() + ".");
               String[][] could_not_parse = new String[2][];
               recordings_id3[0] = RecordingMetaData.extractMetaDataFromID3s(files, could_not_parse);
               
               // Generate report of files that could not be parsed
               if (preferences.list_mp3_files_could_not_parse)
               {
                    DataOutputStream report_stream = HTMLWriter.addFrame("MP3 files found that could not be parsed", subdirectory, contents_stream);
                    HTMLWriter.addParagraph("<i>This report lists all files with an MP3 extension that were found in the specified MP3 directory (" + preferences.mp3_directory.getAbsolutePath() + ") or its subdirectories whose ID3 tags could not be parsed. All processing that was performed by this software skipped these files.</i>", report_stream);
                    HTMLWriter.addHorizontalRule(report_stream);
                    
                    if (could_not_parse[0] == null)
                         HTMLWriter.addParagraph("<h2>No MP3 files were found whose ID3 tags could not be parsed.</h2>", report_stream);
                    else
                    {
                         HTMLWriter.addParagraph("<h2>" + could_not_parse[0].length + " files with MP3 extensions whose ID3 tags could not be parsed:</h2>", report_stream);
                         String[] column_headings = {"Paths", "Reason"};
                         String[][] table = new String[could_not_parse[0].length][could_not_parse.length];
                         for (int i = 0; i < table.length; i++)
                              for (int j = 0; j < table[i].length; j++)
                                   table[i][j] = could_not_parse[j][i];
                         HTMLWriter.addTable(table, column_headings, report_stream);
                    }
                    HTMLWriter.endHTMLFile(report_stream, true);
               }
          }
          
          // Report on MP3 files
          int number_recordings = 0;
          String addendum = "";
          if (recordings_id3[0] != null)
          {
               number_recordings = recordings_id3[0].length;
               addendum += " from " + preferences.mp3_directory.getAbsolutePath() + " directory and its sub-directories";
          }
          HTMLWriter.addParagraph("" + number_recordings + " MP3 files parsed" + addendum, writer);
          
          // Update the progress bar
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(1);
          
          // Parse the iTunes XML file
          if (preferences.iTunes_file != null)
          {
               recordings_xml[0] = RecordingMetaData.extractMetaDataFromiTunesXML(preferences.iTunes_file, true);
               if (recordings_xml[0] == null)
                    System.out.println("No metadata found in " + preferences.iTunes_file.getAbsolutePath() + ".");
          }
          
          // Report on the XML file
          number_recordings = 0;
          addendum = "";
          if (recordings_xml[0] != null)
          {
               number_recordings = recordings_xml[0].length;
               addendum += " " + preferences.iTunes_file.getAbsolutePath();
          }
          HTMLWriter.addParagraph(number_recordings + " recordings parsed from iTunes XML file" + addendum, writer);
          
          // Update the progress bar
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(2);
     }
     
     
     /**
      * Writes a report of all files found in the preferences.mp3_directory
      * directory that do not end with an mp3 extension. Only the extensions
      * of files are considered, not the actual file content. Case is ignored.
      *
      * @throws Exception          Throws an exception if a problem occurs.
      */
     private void listNonMP3FilesFound()
     throws Exception
     {
          // Update the progress bar
          if (progress_bar != null)
               progress_bar.startNewSubTask(1, "Searching for non-MP3 files");
          
          // Prepare the report file
          DataOutputStream report_stream = HTMLWriter.addFrame("Non-MP3 files found", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists all files in the specified MP3 directory (" + preferences.mp3_directory.getAbsolutePath() + ") that do not have an MP3 extension. The case of the extension is ignored. It should be noted that these files do not have any effect on processing, as they are ignored other than in this report. This report is only for informational purposes. Only the extension of the file is considered when generating this report, not actual file content.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Examine all files
          File[] files = mckay.utilities.staticlibraries.FileMethods.getAllFilesInDirectory(preferences.mp3_directory, true, null, null);
          boolean[] not_mp3s = null;
          int number_found = 0;
          if (files != null)
          {
               not_mp3s = new boolean[files.length];
               for (int i = 0; i < not_mp3s.length; i++)
               {
                    String path = files[i].getPath();
                    String extension = mckay.utilities.staticlibraries.StringMethods.getExtension(path);
                    if (extension == null)
                    {
                         not_mp3s[i] = true;
                         number_found++;
                    }
                    else
                    {
                         extension = extension.toLowerCase();
                         if (!extension.equals(".mp3"))
                         {
                              not_mp3s[i] = true;
                              number_found++;
                         }
                         else
                              not_mp3s[i] = false;
                    }
               }
          }
          
          // Prepare report
          if (number_found == 0)
               HTMLWriter.addParagraph("<h2>No non-MP3 files found.</h2>", report_stream);
          else
          {
               Vector<String> found = new Vector<String>();
               for (int i = 0; i < not_mp3s.length; i++)
                    if (not_mp3s[i])
                         found.add(files[i].getPath());
               HTMLWriter.addParagraph("<h2>" + number_found + " non-MP3 files found:</h2>", report_stream);
               HTMLWriter.addList((String[]) found.toArray(new String[1]), true, report_stream);
          }
          
          // Finalize processing
          HTMLWriter.endHTMLFile(report_stream, true);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(1);
     }
     
     
     /**
      * Writes a report file giving the path, title and artist of all recordings
      * for which metadata was extracted. Two separate lists are produced on the
      * same report, one for the recordings extracted from ID3 tags and one for
      * recordings extracted from an iTunes XML file.
      *
      * @param  recordings_id3     The recordings extracted from MP3 files.
      * @param  recordings_xml     The recordings extracted from an iTunes file.
      * @throws Exception          Throws an exception if a problem occurs.
      */
     private void listAllRecordingsFound(RecordingMetaData[] recordings_id3,
          RecordingMetaData[] recordings_xml)
          throws Exception
     {
          // Update the progress bar
          if (progress_bar != null)
               progress_bar.startNewSubTask(2, "Reporting all recordings found");
          
          // Prepare the report file
          String[] column_headings = {"Number", "Title", "Artist", "Path"};
          DataOutputStream report_stream = HTMLWriter.addFrame("All recordings parsed (before merge)", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists the path, title and artist of all recordings for which metadata was extracted, before any processing is performed. Two separate lists are produced, one for the recordings extracted from ID3 tags and one for recordings extracted from an iTunes XML file.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Report the recordings parsed from MP3 files
          if (recordings_id3 == null)
               HTMLWriter.addParagraph("<h2>No MP3s parsed.</h2>", report_stream);
          else
          {
               HTMLWriter.addParagraph("<h2>" + recordings_id3.length + " MP3s parsed directly via ID3 tags:</h2>", report_stream);
               String[][] mp3_recordings_found = new String[recordings_id3.length][column_headings.length];
               for (int i = 0; i < recordings_id3.length; i++)
               {
                    String[] temp = {"" + i, recordings_id3[i].title, recordings_id3[i].artist, recordings_id3[i].file_path};
                    mp3_recordings_found[i] = temp;
               }
               HTMLWriter.addTable(mp3_recordings_found, column_headings, report_stream);
          }
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(1);
          
          // Report the recordings parsed from the iTunes XML file
          HTMLWriter.addHorizontalRule(report_stream);
          if (recordings_xml == null)
               HTMLWriter.addParagraph("<h2>No recordings parsed from iTunes XML file.</h2>", report_stream);
          else
          {
               HTMLWriter.addParagraph("<h2>" + recordings_xml.length + " recordings parsed from iTunes XML file:</h2>", report_stream);
               String[][] xml_recordings_found = new String[recordings_xml.length][column_headings.length];
               for (int i = 0; i < recordings_xml.length; i++)
               {
                    String[] temp = {"" + i, recordings_xml[i].title, recordings_xml[i].artist, recordings_xml[i].file_path};
                    xml_recordings_found[i] = temp;
               }
               HTMLWriter.addTable(xml_recordings_found, column_headings, report_stream);
          }
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(1);
          
          // Finalize processing
          HTMLWriter.endHTMLFile(report_stream, true);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(2);
     }
     
     
     /**
      * Write a report listing recordings found in the iTunes XML file that are
      * not present at the specified path.
      *
      * @param xml_recording_metadata   The recordings whose paths are to be
      *                                 checked.
      * @throws Exception               Throws an exception if a problem occurs.
      */
     private void listFilesInXMLButMissing(RecordingMetaData[] xml_recording_metadata)
     throws Exception
     {
          // Update the progress bar
          if (progress_bar != null)
               progress_bar.startNewSubTask(xml_recording_metadata.length, "Reporting files in iTunes XML but not at specified path");
          
          // Find any missing files
          Vector<String[]> table_vector = null;
          for (int i = 0; i < xml_recording_metadata.length; i++)
          {
               File file_to_test = new File(xml_recording_metadata[i].file_path);
               try
               {mckay.utilities.staticlibraries.FileMethods.validateFile(file_to_test, true, false);}
               catch (Exception e)
               {
                    if (table_vector == null)
                         table_vector = new Vector<String[]>();
                    String[] row = {xml_recording_metadata[i].title, xml_recording_metadata[i].file_path, e.getMessage()};
                    table_vector.add(row);
               }
               
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(i);
          }
          
          // Generate the report
          DataOutputStream report_stream = HTMLWriter.addFrame("Files in iTunes XML but not at specified path", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists each recording that is parsed from the iTunes XML file for which there is no valid readable file at the specified path. No verification is performed during the generation of this particular report of whether files that are present are in fact valid MP3 files, as this information is available in other reports. Unlike the Recordings in one source but not the other report, this report may be generated even if the Use MP3s option is not selected.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          if (table_vector == null)
               HTMLWriter.addParagraph("<h2>No recordings were found for which a corresponding file was unavailable.</h2>", report_stream);
          else
          {
               String[] column_headings = {"Title", "Path", "Problem"};
               String[][] table = table_vector.toArray(new String[1][1]);
               HTMLWriter.addParagraph("<h2>" + table.length + " recordings did not have valid readable files present at the specifified path:</h2>", report_stream);
               HTMLWriter.addTable(table, column_headings, report_stream);
          }
          
          // Finalize processing
          HTMLWriter.endHTMLFile(report_stream, true);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(xml_recording_metadata.length);
     }
     
     
     /**
      * Merge recording metadata by matching entries from the iTunes file
      * and MP3 files on disk. The results are stored in the recordings
      * field. If the metadata differs from the two sources for a given
      * field, the field from the iTunes file is used. If only one of the
      * input sources (i.e. just an XML file or just ID3 tags) is being used,
      * then the source recordings are simply copied into the recordings
      * field.
      *
      * <p>If the list_noncorresponding_recordings preference is set,
      * then the differences are also reported.
      *
      * <p>If the list_noncorresponding_fields_checkbox preference is true
      * then all fields whose corresponding entries between an ID3 tag and its
      * corresponding MP3 ID3 tag differ are reported.
      *
      * <p>The total number of unique recordings is also written to the
      * summary_stream.
      *
      * @param  recordings_id3     The recordings extracted from MP3 files.
      * @param  recordings_xml     The recordings extracted from an iTunes file.
      * @param  summary_stream     A stream to write the total number of uniqu
      *                            recordings to.
      * @throws Exception          Throws an exception if a problem occurs.
      */
     private void mergeRecordings(RecordingMetaData[] recordings_id3,
          RecordingMetaData[] recordings_xml, DataOutputStream summary_stream)
          throws Exception
     {
          if (preferences.iTunes_file == null && preferences.mp3_directory == null)
               throw new Exception("No recording sources available.");
          else if (preferences.iTunes_file == null)
               recordings = recordings_id3;
          else if (preferences.mp3_directory == null)
               recordings = recordings_xml;
          else
          {
               // Initialize the arrays to hold the mappings. Set to -1 to note
               // that hasn't been found
               int[] files_to_xml_mappings = new int[recordings_id3.length];
               for (int i = 0; i < recordings_id3.length; i++)
                    files_to_xml_mappings[i] = -1;
               int[] xml_to_files_mappings = new int[recordings_xml.length];
               for (int j = 0; j < recordings_xml.length; j++)
                    xml_to_files_mappings[j] = -1;
               
               // Set up dummy arrays
               RecordingMetaData[] temp_files = new RecordingMetaData[recordings_id3.length];
               for (int i = 0; i < temp_files.length; i++)
                    temp_files[i] = recordings_id3[i];
               RecordingMetaData[] temp_xml = new RecordingMetaData[recordings_xml.length];
               for (int j = 0; j < temp_xml.length; j++)
                    temp_xml[j] = recordings_xml[j];
               
               // Find mappings
               if (progress_bar != null)
                    progress_bar.startNewSubTask(temp_files.length, "Merging iTunes XML data to MP3 ID3 tags");
               for (int i = 0; i < temp_files.length; i++)
               {
                    if (temp_files[i] != null)
                    {
                         if (progress_bar != null)
                              progress_bar.setSubTaskProgressValue(i);
                         
                         for (int j = 0; j < temp_xml.length; j++)
                         {
                              if (temp_xml[j] != null)
                              {
                                   if (temp_files[i].refersToSameFile(temp_xml[j]))
                                   {
                                        // Store mappings
                                        files_to_xml_mappings[i] = j;
                                        xml_to_files_mappings[j] = i;
                                        
                                        // Note that already found for these
                                        // entries to avoid researching
                                        temp_files[i] = null;
                                        temp_xml[j] = null;
                                        
                                        // End this loop
                                        j = temp_xml.length;
                                   }
                              }
                         }
                    }
               }
               
               // Find files in one source but not the other
               if (preferences.list_noncorresponding_recordings)
               {
                    // Prepare progress bar
                    if (progress_bar != null)
                         progress_bar.startNewSubTask(files_to_xml_mappings.length + xml_to_files_mappings.length, "Finding recordings in one source but not the other");
                    
                    // Find recordings in files but not XML
                    Vector<String[]> in_file_but_not_xml = new Vector<String[]>();
                    for (int i = 0; i < files_to_xml_mappings.length; i++)
                    {
                         if (files_to_xml_mappings[i] == -1)
                         {
                              String[] report = {recordings_id3[i].title, recordings_id3[i].artist, recordings_id3[i].file_path};
                              in_file_but_not_xml.add(report);
                         }
                         if (progress_bar != null)
                              progress_bar.setSubTaskProgressValue(i);
                    }
                    String[][] in_file_but_not_xml_table = in_file_but_not_xml.toArray(new String[1][1]);
                    
                    // Find recordings in XML but not files
                    Vector<String[]> in_xml_but_not_file = new Vector<String[]>();
                    for (int i = 0; i < xml_to_files_mappings.length; i++)
                    {
                         if (xml_to_files_mappings[i] == -1)
                         {
                              String report[] = {recordings_xml[i].title, recordings_xml[i].artist, recordings_xml[i].file_path};
                              in_xml_but_not_file.add(report);
                         }
                         if (progress_bar != null)
                              progress_bar.setSubTaskProgressValue(i + files_to_xml_mappings.length);
                    }
                    String[][] in_xml_but_not_file_table = in_xml_but_not_file.toArray(new String[1][1]);
                    
                    // Report the results
                    DataOutputStream report_stream = HTMLWriter.addFrame("Recordings in one source but not the other", subdirectory, contents_stream);
                    HTMLWriter.addParagraph("<i>This report indicates any recordings that were found in the iTunes XML file but for which corresponding MP3 files were not found, and vice versa. It may be appropriate to review these two sources in order to ensure that they are consistent.</i>", report_stream);
                    HTMLWriter.addHorizontalRule(report_stream);
                    String[] column_headings = {"Title", "Artist", "Path"};
                    if (in_file_but_not_xml_table[0] == null)
                         HTMLWriter.addParagraph("<h2>All MP3 files have a correspondig entry in the iTunes file.</h2>", report_stream);
                    else
                    {
                         HTMLWriter.addParagraph("<h2>" + in_file_but_not_xml_table.length + " MP3 files exist for which there is no matching entry in the iTunes file:</h2>", report_stream);
                         HTMLWriter.addTable(in_file_but_not_xml_table, column_headings, report_stream);
                    }
                    HTMLWriter.addHorizontalRule(report_stream);
                    if (in_xml_but_not_file_table[0] == null)
                         HTMLWriter.addParagraph("<h2>All recordings in the iTunes file have a correspondig MP3 file.</h2>", report_stream);
                    else
                    {
                         HTMLWriter.addParagraph("<h2>" + in_xml_but_not_file_table.length + " entries exist in the iTunes file for which there is no matching MP3 file:</h2>", report_stream);
                         HTMLWriter.addTable(in_xml_but_not_file_table, column_headings, report_stream);
                    }
                    HTMLWriter.endHTMLFile(report_stream, true);
               }
               
               // Find fields that differ between the iTunes entry and the
               // corresponding MP3 file
               if (preferences.list_noncorresponding_fields)
               {
                    // Prepare progress bar
                    if (progress_bar != null)
                         progress_bar.startNewSubTask(files_to_xml_mappings.length, "Finding inconsistent fields");
                    
                    // Initialize the report
                    DataOutputStream report_stream = HTMLWriter.addFrame("Fields that do not correspond between sources", subdirectory, contents_stream);
                    HTMLWriter.addParagraph("<i>This report shows any differences in significant fields in the metadata for each recording parsed from the iTunes XML file and its corresponding MP3 file. It may be appropriate to review this metadata to ensure that it is consistent. jMusicMetaManager  uses the metadata extracted from the iTunes file by default when there is such a disagreement.</i>", report_stream);
                    HTMLWriter.addHorizontalRule(report_stream);
                    int corresponding_recordings = 0;
                    int non_matching_recordings = 0;
                    
                    // Find and report differences
                    String[] column_headings = {"Field", "iTunes XML Entry", "MP3's ID3 Entry"};
                    for (int i = 0; i < xml_to_files_mappings.length; i++)
                    {
                         if (xml_to_files_mappings[i] != -1)
                         {
                              corresponding_recordings++;
                              String[][] differences = recordings_xml[i].getDifferences(recordings_id3[xml_to_files_mappings[i]], true);
                              if (differences != null)
                              {
                                   non_matching_recordings++;
                                   HTMLWriter.addParagraph("<h2>" + differences.length + " inconsistencies between iTunes XML entry and ID3 tag for the file: " + recordings_xml[i].file_path + ":</h2>", report_stream);
                                   HTMLWriter.addTable(differences, column_headings, report_stream);
                              }
                         }
                         
                         // Update the progress bar
                         if (progress_bar != null)
                              progress_bar.setSubTaskProgressValue(i);
                    }
                    
                    // Finalize the report
                    HTMLWriter.addHorizontalRule(report_stream);
                    HTMLWriter.addParagraph("<b>Summary: </b>" + non_matching_recordings + " entries out of " + corresponding_recordings + " corresponding entries do not match.", report_stream);
                    HTMLWriter.endHTMLFile(report_stream, true);
               }
               
               // Fill the recordings field with the complete set of merged
               // recordings
               Vector<RecordingMetaData> merged_recordings = new Vector<RecordingMetaData>();
               for (int i = 0; i < xml_to_files_mappings.length; i++)
               {
                    if (xml_to_files_mappings[i] != -1)
                         recordings_xml[i].merge(recordings_id3[xml_to_files_mappings[i]]);
                    merged_recordings.add(recordings_xml[i]);
               }
               for (int i = 0; i < files_to_xml_mappings.length; i++)
                    if (files_to_xml_mappings[i] == -1)
                         merged_recordings.add(recordings_id3[i]);
               recordings = merged_recordings.toArray(new RecordingMetaData[1]);
               
               // Write the number of unique recordings to the summary stream
               HTMLWriter.addParagraph(recordings_id3.length + " unique recordings found and merged", summary_stream);
          }
     }
     
     
     /**
      * Writes a report giving all available metadata for the recordings in the
      * recordings field. Should be called after the iTunex and ID3 metadata
      * has been merged.
      *
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private void listAllPostMergeMetadata()
     throws Exception
     {
          // Prepare progress bar
          if (progress_bar != null)
               progress_bar.startNewSubTask(recordings.length, "Reporting all post-iTunes/ID3 merge metadata");
          
          // Initialize the report
          DataOutputStream report_stream = HTMLWriter.addFrame("All post-iTunes and ID3 merge metadata", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report gives all available metadata for each recording immediately after the metadata extracted from the iTunes file and/or the MP3 files has been combined. If an entry in the iTunes file contains a reference to a file with a path that is also found during ID3 parsing, then the metadata from these two sources is combined into a single recording. jMusicMetaManager uses the metadata extracted from the iTunes file by default when there is a disagreement between the fields of any two recordings from different sources with the same path.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Write report
          String[][] table_to_report = new String[recordings.length][];
          String[][] column_headings = new String[1][];
          for (int i = 0; i < recordings.length; i++)
          {
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(i);
               
               table_to_report[i] = recordings[i].getFormattedFieldContents(i, column_headings);
          }
          HTMLWriter.addTable(table_to_report, column_headings[0], report_stream);
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Extract all titles, artists, composers, albums and genres from the
      * recordings field and store them in the fields of the same names. The
      * results are each alphebetically sorted. Identical artist, composer,
      * album and genres are merged (but identical titles are not merged).
      *
      * @throws     Exception An exception is thrown if a problem occurs.
      */
     private void organizeMetadata()
     throws Exception
     {
          if (progress_bar != null)
               progress_bar.startNewSubTask(5, "Sorting and merging metadata");
          titles = new Entries(recordings, RecordingMetaData.TITLE_IDENTIFIER);
          titles.sortEntries();
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(1);
          
          artists = new Entries(recordings, RecordingMetaData.ARTIST_IDENTIFIER);
          artists.mergeIdenticalEntries(false, null);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(2);
          
          composers = new Entries(recordings, RecordingMetaData.COMPOSER_IDENTIFIER);
          composers.mergeIdenticalEntries(false, null);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(3);
          
          albums = new Entries(recordings, RecordingMetaData.ALBUM_IDENTIFIER);
          albums.mergeIdenticalEntries(false, null);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(5);
          
          genres = new Entries(recordings, RecordingMetaData.GENRES_IDENTIFIER);
          genres.mergeIdenticalEntries(false, null);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(4);
     }
     
     
     /**
      * Writes a report giving all available metadata for all recordings after
      * sorting. Also writes an overview string to summary_stream.
      *
      * @param  summary_stream     A stream to write the total number of
      *                            recordings to.
      * @throws Exception          Throws an exception if a problem occurs.
      */
     private void listSortedRecordings(DataOutputStream summary_stream)
     throws Exception
     {
          if (progress_bar != null)
               progress_bar.startNewSubTask(titles.getNumberEntries(), "Listing sorted recording metadata");
          String[] title_entries = titles.getValues();
          int[][] title_indexes = titles.getIndexes();
          String[][] title_column_headings = new String[1][];
          String[][] title_table = new String[title_entries.length][];
          for (int i = 0; i < title_entries.length; i++)
          {
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(i);
               title_table[i] = recordings[title_indexes[i][0]].getFormattedFieldContents(i, title_column_headings);
          }
          String title_overview_string;
          int unknown_titles = 0;
          for (int i = 0; i < title_entries.length; i++)
               if (title_entries[i] == null)
                    unknown_titles++;
          if (unknown_titles > 0)
               title_overview_string = (title_entries.length - unknown_titles) + " titled recordings present, and " +
                    unknown_titles + " recordings present with unknown titles";
          else
               title_overview_string = title_entries.length + " titled recordings present";
          HTMLWriter.addParagraph(title_overview_string, summary_stream);
          DataOutputStream title_report_stream = HTMLWriter.addFrame("All recordings post-sorting", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report gives all available metadata for each recording. This list is sorted by title. Recordings with identical or similar titles have not been merged in this report, and changes made via find and replace, word reordering subset and edit distance operations are not reflected here.</i>", title_report_stream);
          HTMLWriter.addHorizontalRule(title_report_stream);
          HTMLWriter.addParagraph(title_overview_string, title_report_stream);
          HTMLWriter.addParagraph("", title_report_stream);
          HTMLWriter.addTable(title_table, title_column_headings[0], title_report_stream);
          HTMLWriter.endHTMLFile(title_report_stream, true);
     }
     
     
     /**
      * Writes a report naming all unique artists and statistics corresponding
      * to each. Also writes a summary report to summary_stream.
      *
      * @param  summary_stream     A stream to write the total number of
      *                            unique artists to.
      * @throws Exception          Throws an exception if a problem occurs.
      */
     private void reportArtistBreakdown(DataOutputStream summary_stream)
     throws Exception
     {
          // Prepare progress bar
          if (progress_bar != null)
               progress_bar.startNewSubTask(artists.getNumberEntries(), "Reporting artist breakdown");
          
          // Prepare variables
          String[] artist_entries = artists.getValues();
          int[][] artist_indexes = artists.getIndexes();
          String[] artist_column_headings = {"Artist", "Recordings", "% Total Recordings", "Albums", "Genres", "Composers"};
          String[][] artist_table = new String[artist_entries.length + 2][artist_column_headings.length];
          double[][] counts = new double[artist_column_headings.length - 1][artist_entries.length];
          int total_recordings = recordings.length;
          
          // Add each artist to table and find counts
          for (int i = 0; i < artist_entries.length; i++)
          {
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(i);
               
               artist_table[i][0] = recordings[artist_indexes[i][0]].artist;
               if (artist_table[i][0] == null)
                    artist_table[i][0] = "UNKNOWN";
               
               int number_recordings = RecordingMetaData.findNumberUniqueItems(artist_indexes[i], "titles", recordings);
               counts[0][i] = (new Integer(number_recordings)).doubleValue();
               artist_table[i][1] = "" + number_recordings;
               
               double percentage = mckay.utilities.staticlibraries.MathAndStatsMethods.getPercentage(number_recordings, total_recordings);
               counts[1][i] = percentage;
               artist_table[i][2] = "" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(percentage, 1);
               
               int number_albums = RecordingMetaData.findNumberUniqueItems(artist_indexes[i], "albums", recordings);
               counts[2][i] = (new Integer(number_albums)).doubleValue();
               artist_table[i][3] = "" + number_albums;
               
               int number_genres = RecordingMetaData.findNumberUniqueItems(artist_indexes[i], "genres", recordings);
               counts[3][i] = (new Integer(number_genres)).doubleValue();
               artist_table[i][4] = "" + number_genres;
               
               int number_composers = RecordingMetaData.findNumberUniqueItems(artist_indexes[i], "composers", recordings);
               counts[4][i] = (new Integer(number_composers)).doubleValue();
               artist_table[i][5] = "" + number_composers;
          }
          
          // Add means and standard deviations to table
          artist_table[artist_table.length - 2][0] = "<i>MEAN</i>";
          artist_table[artist_table.length - 1][0] = "<i>SDEV</i>";
          for (int i = 1; i < artist_column_headings.length; i++)
          {
               artist_table[artist_table.length - 2][i] = "<i>" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(mckay.utilities.staticlibraries.MathAndStatsMethods.getAverage(counts[i-1]), 1) + "</i>";
               artist_table[artist_table.length - 1][i] = "<i>" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(mckay.utilities.staticlibraries.MathAndStatsMethods.getStandardDeviation(counts[i-1]), 1) + "</i>";
          }
          
          // Prepare overview information
          String artist_overview_string;
          if (artist_entries[artist_entries.length - 1] == null)
               artist_overview_string = (artist_entries.length - 1) + " unique artists present, and " +
                    artist_indexes[artist_indexes.length - 1].length + " recordings present with unknown artists";
          else
               artist_overview_string = artist_entries.length + " unique artists present";
          
          // Write information to reports
          HTMLWriter.addParagraph(artist_overview_string, summary_stream);
          DataOutputStream artist_report_stream = HTMLWriter.addFrame("Artist breakdown", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists the unique names of all artists found, as well as the number of recordings and the percentage of total recordings corresponding to each artist. The number of unique composers, albums and genres that have at least one recording corresponding to each artist is also reported.</i>", artist_report_stream);
          HTMLWriter.addParagraph("<i>The tallies listed do not include counts for unknown composers, genres and albums. </i>" , artist_report_stream);
          HTMLWriter.addParagraph("<i>The determination of artist, composer, genre and album uniqueness does not incorporate any processing due to find and replace, word reordering subset and edit distance operations.</i>", artist_report_stream);
          HTMLWriter.addHorizontalRule(artist_report_stream);
          HTMLWriter.addParagraph(artist_overview_string, artist_report_stream);
          HTMLWriter.addParagraph("", artist_report_stream);
          HTMLWriter.addTable(artist_table, artist_column_headings, artist_report_stream);
          HTMLWriter.endHTMLFile(artist_report_stream, true);
          
          // Finalize progress bar
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(artist_entries.length);
     }
     
     
     /**
      * Writes a report naming all unique composers and statistics corresponding
      * to each. Also writes a summary report to summary_stream.
      *
      * @param  summary_stream     A stream to write the total number of
      *                            unique composers to.
      * @throws Exception          Throws an exception if a problem occurs.
      */
     private void reportComposerBreakdown(DataOutputStream summary_stream)
     throws Exception
     {
          // Prepare progress bar
          if (progress_bar != null)
               progress_bar.startNewSubTask(composers.getNumberEntries(), "Reporting composer breakdown");
          
          // Prepare variables
          String[] composer_entries = composers.getValues();
          int[][] composer_indexes = composers.getIndexes();
          String[] composer_column_headings = {"Composer", "Recordings", "% Total Recordings", "Artists", "Albums", "Genres"};
          String[][] composer_table = new String[composer_entries.length + 2][composer_column_headings.length];
          double[][] counts = new double[composer_column_headings.length - 1][composer_entries.length];
          int total_recordings = recordings.length;
          
          // Add each composer to table and find counts
          for (int i = 0; i < composer_entries.length; i++)
          {
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(i);
               
               composer_table[i][0] = recordings[composer_indexes[i][0]].composer;
               if (composer_table[i][0] == null)
                    composer_table[i][0] = "UNKNOWN";
               
               int number_recordings = RecordingMetaData.findNumberUniqueItems(composer_indexes[i], "titles", recordings);
               counts[0][i] = (new Integer(number_recordings)).doubleValue();
               composer_table[i][1] = "" + number_recordings;
               
               double percentage = mckay.utilities.staticlibraries.MathAndStatsMethods.getPercentage(number_recordings, total_recordings);
               counts[1][i] = percentage;
               composer_table[i][2] = "" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(percentage, 1);
               
               int number_artists = RecordingMetaData.findNumberUniqueItems(composer_indexes[i], "artists", recordings);
               counts[2][i] = (new Integer(number_artists)).doubleValue();
               composer_table[i][3] = "" + number_artists;
               
               int number_albums = RecordingMetaData.findNumberUniqueItems(composer_indexes[i], "albums", recordings);
               counts[3][i] = (new Integer(number_albums)).doubleValue();
               composer_table[i][4] = "" + number_albums;
               
               int number_genres = RecordingMetaData.findNumberUniqueItems(composer_indexes[i], "genres", recordings);
               counts[4][i] = (new Integer(number_genres)).doubleValue();
               composer_table[i][5] = "" + number_genres;
          }
          
          // Add means and standard deviations to table
          composer_table[composer_table.length - 2][0] = "<i>MEAN</i>";
          composer_table[composer_table.length - 1][0] = "<i>SDEV</i>";
          for (int i = 1; i < composer_column_headings.length; i++)
          {
               composer_table[composer_table.length - 2][i] = "<i>" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(mckay.utilities.staticlibraries.MathAndStatsMethods.getAverage(counts[i-1]), 1) + "</i>";
               composer_table[composer_table.length - 1][i] = "<i>" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(mckay.utilities.staticlibraries.MathAndStatsMethods.getStandardDeviation(counts[i-1]), 1) + "</i>";
          }
          
          // Prepare overview information
          String composer_overview_string;
          if (composer_entries[composer_entries.length - 1] == null)
               composer_overview_string = (composer_entries.length - 1) + " unique composers present, and " +
                    composer_indexes[composer_indexes.length - 1].length + " recordings present with unknown composers";
          else
               composer_overview_string = composer_entries.length + " unique composers present";
          
          // Write information to reports
          HTMLWriter.addParagraph(composer_overview_string, summary_stream);
          DataOutputStream composer_report_stream = HTMLWriter.addFrame("Composer breakdown", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists the unique names of all composers found, as well as the number of recordings and the percentage of total recordings corresponding to each composer. The number of unique artists, albums and genres that have at least one recording corresponding to each composer is also reported.</i>", composer_report_stream);
          HTMLWriter.addParagraph("<i>The tallies listed do not include counts for unknown artists, genres and albums.</i>" , composer_report_stream);
          HTMLWriter.addParagraph("<i>The determination of artist, composer, genre and album uniqueness does not incorporate any processing due to find and replace, word reordering subset and edit distance operations.</i>", composer_report_stream);
          HTMLWriter.addHorizontalRule(composer_report_stream);
          HTMLWriter.addParagraph(composer_overview_string, composer_report_stream);
          HTMLWriter.addParagraph("", composer_report_stream);
          HTMLWriter.addTable(composer_table, composer_column_headings, composer_report_stream);
          HTMLWriter.endHTMLFile(composer_report_stream, true);
          
          // Finalize progress bar
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(composer_entries.length);
     }
     
     
     /**
      * Writes a report giving the total number of genres and a table showing
      * the name of each genre, how many recordings have that genre, how many
      * unique artists match that genre and how many unique composers match
      * that genre.
      *
      * <p>A summary line is also written to the overall summary report.
      *
      * @param  summary_stream     A stream to write the total number of unique
      *                            recordings to.
      * @throws Exception          Throws an exception if a problem occurs.
      */
     private void reportGenreBreakdown(DataOutputStream summary_stream)
     throws Exception
     {
          // Prepare progress bar
          if (progress_bar != null)
               progress_bar.startNewSubTask(genres.getNumberEntries(), "Reporting genre breakdown");
          
          // Prepare variables
          String[] genre_entries = genres.getValues();
          int[][] genre_indexes = genres.getIndexes();
          String[] genre_column_headings = {"Genre", "Recordings", "% Total Recordings", "Artists", "Composers", "Albums"};
          String[][] genre_table = new String[genre_entries.length + 2][genre_column_headings.length];
          double[][] counts = new double[genre_column_headings.length - 1][genre_entries.length];
          int total_recordings = recordings.length;
          
          // Add each genre to table and find counts
          for (int i = 0; i < genre_entries.length; i++)
          {
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(i);
               
               genre_table[i][0] = genre_entries[i];
               if (genre_table[i][0] == null)
                    genre_table[i][0] = "UNKNOWN";
               
               int number_recordings = RecordingMetaData.findNumberUniqueItems(genre_indexes[i], "titles", recordings);
               counts[0][i] = (new Integer(number_recordings)).doubleValue();
               genre_table[i][1] = "" + number_recordings;
               
               double percentage = mckay.utilities.staticlibraries.MathAndStatsMethods.getPercentage(number_recordings, total_recordings);
               counts[1][i] = percentage;
               genre_table[i][2] = "" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(percentage, 1);
               
               int number_artists = RecordingMetaData.findNumberUniqueItems(genre_indexes[i], "artists", recordings);
               counts[2][i] = (new Integer(number_artists)).doubleValue();
               genre_table[i][3] = "" + number_artists;
               
               int number_composers = RecordingMetaData.findNumberUniqueItems(genre_indexes[i], "composers", recordings);
               counts[3][i] = (new Integer(number_composers)).doubleValue();
               genre_table[i][4] = "" + number_composers;
               
               int number_albums = RecordingMetaData.findNumberUniqueItems(genre_indexes[i], "albums", recordings);
               counts[4][i] = (new Integer(number_albums)).doubleValue();
               genre_table[i][5] = "" + number_albums;
          }
          
          // Add means and standard deviations to table
          genre_table[genre_table.length - 2][0] = "<i>MEAN</i>";
          genre_table[genre_table.length - 1][0] = "<i>SDEV</i>";
          for (int i = 1; i < genre_column_headings.length; i++)
          {
               genre_table[genre_table.length - 2][i] = "<i>" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(mckay.utilities.staticlibraries.MathAndStatsMethods.getAverage(counts[i-1]), 1) + "</i>";
               genre_table[genre_table.length - 1][i] = "<i>" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(mckay.utilities.staticlibraries.MathAndStatsMethods.getStandardDeviation(counts[i-1]), 1) + "</i>";
          }
          
          // Prepare overview information
          String overview_string;
          if (genre_entries[genre_entries.length - 1] == null)
               overview_string = (genre_entries.length - 1) + " unique genres present, and " +
                    genre_indexes[genre_indexes.length - 1].length + " recordings present with unknown genres";
          else
               overview_string = genre_entries.length + " unique genres present";
          
          // Write report
          HTMLWriter.addParagraph(overview_string, summary_stream);
          DataOutputStream report_stream = HTMLWriter.addFrame("Genre breakdown", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists the unique names of all genres found, as well as the number of recordings and the percentage of total recordings belonging to each genre. The number of unique artists, composers and albums that have at least one recording belonging to each genre is also reported.</i>", report_stream);
          HTMLWriter.addParagraph("<i>The tallies listed do not include counts for unknown artists, composers and albums.</i>" , report_stream);
          HTMLWriter.addParagraph("<i>The determination of artist, composer, genre and album uniqueness does not incorporate any processing due to find and replace, word reordering subset and edit distance operations.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          HTMLWriter.addParagraph(overview_string, report_stream);
          HTMLWriter.addParagraph("", report_stream);
          HTMLWriter.addTable(genre_table, genre_column_headings, report_stream);
          HTMLWriter.endHTMLFile(report_stream, true);
          
          // Finalize progress bar
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(genre_entries.length);
     }
     
     
     /**
      * Writes a report naming all unique comments and statistics corresponding
      * to each.
      *
      * @throws Exception          Throws an exception if a problem occurs.
      */
     private void reportCommentBreakdown()
     throws Exception
     {
          // Prepare progress bar
          if (progress_bar != null)
               progress_bar.startNewSubTask(2, "Reporting comment statistics");
          
          // Find and merge all unique comments
          Entries comments = new Entries(recordings, RecordingMetaData.COMMENTS_IDENTIFIER);
          comments.mergeIdenticalEntries(false, null);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(1);
          
          
          // Prepare variables
          String[] comments_entries = comments.getValues();
          int[][] comments_indexes = comments.getIndexes();
          String[] comments_column_headings = {"Comment", "Recordings", "% Total Recordings", "Artists", "Albums", "Genres", "Composers"};
          String[][] comments_table = new String[comments_entries.length + 2][comments_column_headings.length];
          double[][] counts = new double[comments_column_headings.length - 1][comments_entries.length];
          int total_recordings = recordings.length;
          
          // Add each artist to table and find counts
          for (int i = 0; i < comments_entries.length; i++)
          {
               comments_table[i][0] = recordings[comments_indexes[i][0]].comments;
               if (comments_table[i][0] == null)
                    comments_table[i][0] = "UNKNOWN";
               
               int number_recordings = RecordingMetaData.findNumberUniqueItems(comments_indexes[i], "titles", recordings);
               counts[0][i] = (new Integer(number_recordings)).doubleValue();
               comments_table[i][1] = "" + number_recordings;
               
               double percentage = mckay.utilities.staticlibraries.MathAndStatsMethods.getPercentage(number_recordings, total_recordings);
               counts[1][i] = percentage;
               comments_table[i][2] = "" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(percentage, 1);
               
               int number_artists = RecordingMetaData.findNumberUniqueItems(comments_indexes[i], "artists", recordings);
               counts[2][i] = (new Integer(number_artists)).doubleValue();
               comments_table[i][3] = "" + number_artists;
               
               int number_albums = RecordingMetaData.findNumberUniqueItems(comments_indexes[i], "albums", recordings);
               counts[3][i] = (new Integer(number_albums)).doubleValue();
               comments_table[i][4] = "" + number_albums;
               
               int number_genres = RecordingMetaData.findNumberUniqueItems(comments_indexes[i], "genres", recordings);
               counts[4][i] = (new Integer(number_genres)).doubleValue();
               comments_table[i][5] = "" + number_genres;
               
               int number_composers = RecordingMetaData.findNumberUniqueItems(comments_indexes[i], "composers", recordings);
               counts[5][i] = (new Integer(number_composers)).doubleValue();
               comments_table[i][6] = "" + number_composers;
          }
          
          // Add means and standard deviations to table
          comments_table[comments_table.length - 2][0] = "<i>MEAN</i>";
          comments_table[comments_table.length - 1][0] = "<i>SDEV</i>";
          for (int i = 1; i < comments_column_headings.length; i++)
          {
               comments_table[comments_table.length - 2][i] = "<i>" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(mckay.utilities.staticlibraries.MathAndStatsMethods.getAverage(counts[i-1]), 1) + "</i>";
               comments_table[comments_table.length - 1][i] = "<i>" + mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(mckay.utilities.staticlibraries.MathAndStatsMethods.getStandardDeviation(counts[i-1]), 1) + "</i>";
          }
          
          // Prepare overview information
          String comments_overview_string;
          if (comments_entries[comments_entries.length - 1] == null)
               comments_overview_string = (comments_entries.length - 1) + " unique comments present, and " +
                    comments_indexes[comments_indexes.length - 1].length + " recordings present with unknown comments";
          else
               comments_overview_string = comments_entries.length + " unique comments present";
          
          // Write information to reports
          DataOutputStream comment_report_stream = HTMLWriter.addFrame("Comment statistics", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists all unique file comments found, as well as the number of recordings and the percentage of total recordings corresponding to each comment. The number of unique artists, composers, albums and genres that have at least one recording corresponding to each comment is also reported.</i>", comment_report_stream);
          HTMLWriter.addParagraph("<i>The tallies listed do not include counts for unknown artists, composers, genres and albums. </i>" , comment_report_stream);
          HTMLWriter.addParagraph("<i>The determination of comment, artist, composer, genre and album uniqueness does not incorporate any processing due to find and replace, word reordering subset and edit distance operations.</i>", comment_report_stream);
          HTMLWriter.addHorizontalRule(comment_report_stream);
          HTMLWriter.addParagraph(comments_overview_string, comment_report_stream);
          HTMLWriter.addParagraph("", comment_report_stream);
          HTMLWriter.addTable(comments_table, comments_column_headings, comment_report_stream);
          HTMLWriter.endHTMLFile(comment_report_stream, true);
          
          // Finalize progress bar
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(2);
     }
     
     
     /**
      * Generates a list of all genres. Artists and/or composers are listed
      * under a given genre if they have at least one recording belonging to the
      * genre. Artists and/or composers also have the number of recordings they
      * have belonging to the given genre listed beside their entries, along
      * with the percentage of the total number of recordings in the genre that
      * they represent.
      *
      * @throws     Exception An exception is thrown if a problem occurs.
      */
     private void listByGenre()
     throws Exception
     {
          // Prepare sub-task progress bar
          int number_sub_tasks = 0;
          if (preferences.list_artists_by_genre)
               number_sub_tasks++;
          if (preferences.list_composers_by_genre)
               number_sub_tasks++;
          if (progress_bar != null)
               progress_bar.startNewSubTask(number_sub_tasks, "Listing artists and/or composers by genre");
          number_sub_tasks = 0;
          
          // Generate report on artists
          if (preferences.list_artists_by_genre)
          {
               fillInListingByGenre(artists, "Artist", RecordingMetaData.ARTIST_IDENTIFIER);
               number_sub_tasks++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks);
          }
          
          // Generate report on composers
          if (preferences.list_composers_by_genre)
          {
               fillInListingByGenre(composers, "Composer", RecordingMetaData.COMPOSER_IDENTIFIER);
               number_sub_tasks++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks);
          }
     }
     
     
     /**
      * Used to write one of the two possible reports (artist or composer) for
      * the listByGenre method.
      *
      * @param      basis          The Entries to analyze.
      * @param      key_string     Identifies the type of Entries being
      *                            analyzed.
      * @param      identifier     Identifies the type of data being analyzed.
      *                            Either RecordingMetaData.ARTIST_IDENTIFIER or
      *                            RecordingMetaData.COMPOSER_IDENTIFIER.
      * @throws     Exception      An exception is thrown if a problem occurs.
      */
     private void fillInListingByGenre(Entries basis, String key_string,
          int identifier)
          throws Exception
     {
          // Prepare the report header
          DataOutputStream report_stream = HTMLWriter.addFrame(key_string + "s listed by genre", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report alphabetically lists all unique genres. " + key_string + "s are listed under a given genre if they have at least one recording belonging to the genre. " + key_string + "s also have the number of recordings they have belonging to the given genre listed beside their entries, along with the percentage of the total number of recordings in the genre that they represent. Uniqueness for all fields is determined before any find and replace, word reordering subset or edit distance operations are performed.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Write the report
          int[][] genre_indexes = genres.getIndexes();
          String[] column_headings = {(key_string + " name"), "Recordings in this genre", "% of total recordings in this genre"};
          for (int i = 0; i < genre_indexes.length; i++)
          {
               // Process data
               RecordingMetaData[] recordings_in_this_genre = RecordingMetaData.filterRecordings(recordings, genre_indexes[i]);
               Entries artists_or_composers_in_this_genre = new Entries(recordings_in_this_genre, identifier);
               artists_or_composers_in_this_genre.mergeIdenticalEntries(false, null);
               int[][] artist_or_composer_indexes = artists_or_composers_in_this_genre.getIndexes();
               
               // Write report
               HTMLWriter.addParagraph("<h2>" + genres.getValues()[i] + ": <i>" + artist_or_composer_indexes.length + " " + key_string.toLowerCase() + "s and " + genre_indexes[i].length + " recordings</i></h2>", report_stream);
               String[][] table = new String[artist_or_composer_indexes.length][column_headings.length];
               for (int j = 0; j < table.length; j++)
               {
                    String title = null;
                    if (identifier == RecordingMetaData.ARTIST_IDENTIFIER)
                         title = recordings_in_this_genre[artist_or_composer_indexes[j][0]].artist;
                    else title = recordings_in_this_genre[artist_or_composer_indexes[j][0]].composer;
                    if (title == null)
                         table[j][0] = "UNKNOWN";
                    else table[j][0] = title;
                    
                    table[j][1] = "" + artist_or_composer_indexes[j].length;
                    
                    table[j][2] = "" + (100 * artist_or_composer_indexes[j].length / genre_indexes[i].length);
               }
               HTMLWriter.addTable(table, column_headings, report_stream);
          }
          
          // End the report
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Writes a report file listing the metadata for each recording that
      * has an unknown title, artist, composer, album and/or genre. A separate
      * table is generated in a single report for each of these fields.
      *
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private void reportMissingMetadata()
     throws Exception
     {
          // Perform initializations
          if (progress_bar != null)
               progress_bar.startNewSubTask(5, "Reporting missing artists, composers and genres");
          DataOutputStream report_stream = HTMLWriter.addFrame("Recordings missing key metadata", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists all metadata for those recordings that have empty title, artist, composer, album and/or genre fields. A separate table is produced for each of these fields.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          String[][] title_column_headings = new String[1][];
          int[] indexes_of_recordings_missing_entries = null;
          String[][] report_table = null;
          
          // Report recordings that are missing genre metadata
          indexes_of_recordings_missing_entries = titles.getIndexesOfNullEntries();
          if (indexes_of_recordings_missing_entries == null)
               HTMLWriter.addParagraph("<h2>All recordings have titles specified</h2>", report_stream);
          else
          {
               report_table = new String[indexes_of_recordings_missing_entries.length][];
               HTMLWriter.addParagraph("<h2>" + report_table.length + " recordings lack title labels:</h2>", report_stream);
               for (int i = 0; i < report_table.length; i++)
                    report_table[i] = recordings[indexes_of_recordings_missing_entries[i]].getFormattedFieldContents(i, title_column_headings);
               HTMLWriter.addTable(report_table, title_column_headings[0], report_stream);
          }
          HTMLWriter.addHorizontalRule(report_stream);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(1);
          
          // Report recordings that are missing genre metadata
          indexes_of_recordings_missing_entries = genres.getIndexesOfNullEntries();
          if (indexes_of_recordings_missing_entries == null)
               HTMLWriter.addParagraph("<h2>All recordings have genres specified</h2>", report_stream);
          else
          {
               report_table = new String[indexes_of_recordings_missing_entries.length][];
               HTMLWriter.addParagraph("<h2>" + report_table.length + " recordings lack genre labels:</h2>", report_stream);
               for (int i = 0; i < report_table.length; i++)
                    report_table[i] = recordings[indexes_of_recordings_missing_entries[i]].getFormattedFieldContents(i, title_column_headings);
               HTMLWriter.addTable(report_table, title_column_headings[0], report_stream);
          }
          HTMLWriter.addHorizontalRule(report_stream);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(2);
          
          // Report recordings that are missing artist metadata
          indexes_of_recordings_missing_entries = artists.getIndexesOfNullEntries();
          if (indexes_of_recordings_missing_entries == null)
               HTMLWriter.addParagraph("<h2>All recordings have artists specified</h2>", report_stream);
          else
          {
               report_table = new String[indexes_of_recordings_missing_entries.length][];
               HTMLWriter.addParagraph("<h2>" + report_table.length + " recordings lack artist labels:</h2>", report_stream);
               for (int i = 0; i < report_table.length; i++)
                    report_table[i] = recordings[indexes_of_recordings_missing_entries[i]].getFormattedFieldContents(i, title_column_headings);
               HTMLWriter.addTable(report_table, title_column_headings[0], report_stream);
          }
          HTMLWriter.addHorizontalRule(report_stream);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(3);
          
          // Report recordings that are missing composer metadata
          indexes_of_recordings_missing_entries = composers.getIndexesOfNullEntries();
          if (indexes_of_recordings_missing_entries == null)
               HTMLWriter.addParagraph("<h2>All recordings have composers specified</h2>", report_stream);
          else
          {
               report_table = new String[indexes_of_recordings_missing_entries.length][];
               HTMLWriter.addParagraph("<h2>" + report_table.length + " recordings lack composers labels:</h2>", report_stream);
               for (int i = 0; i < report_table.length; i++)
                    report_table[i] = recordings[indexes_of_recordings_missing_entries[i]].getFormattedFieldContents(i, title_column_headings);
               HTMLWriter.addTable(report_table, title_column_headings[0], report_stream);
          }
          HTMLWriter.addHorizontalRule(report_stream);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(4);
          
          // Report recordings that are missing album metadata
          indexes_of_recordings_missing_entries = albums.getIndexesOfNullEntries();
          if (indexes_of_recordings_missing_entries == null)
               HTMLWriter.addParagraph("<h2>All recordings have albums specified</h2>", report_stream);
          else
          {
               report_table = new String[indexes_of_recordings_missing_entries.length][];
               HTMLWriter.addParagraph("<h2>" + report_table.length + " recordings lack album labels:</h2>", report_stream);
               for (int i = 0; i < report_table.length; i++)
                    report_table[i] = recordings[indexes_of_recordings_missing_entries[i]].getFormattedFieldContents(i, title_column_headings);
               HTMLWriter.addTable(report_table, title_column_headings[0], report_stream);
          }
          
          // Finalize processing
          HTMLWriter.endHTMLFile(report_stream, true);
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(5);
     }
     
     
     /**
      * Generates a report listing artists and/or recordings with only a few
      * recordings present in the database.
      *
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private void listFewRecordings()
     throws Exception
     {
          // Decide on cutoff for inclusion
          int artist_cutoff = preferences.cutoff_for_artists_few_recs;
          int composer_cutoff = preferences.cutoff_for_composers_few_recs;
          
          // Prepare sub-task progress bar
          int number_sub_tasks = 0;
          if (preferences.list_artists_with_few_recordings)
               number_sub_tasks++;
          if (preferences.list_composers_with_few_recordings)
               number_sub_tasks++;
          if (progress_bar != null)
               progress_bar.startNewSubTask(number_sub_tasks, "Listing artists and/or composers with few recordings");
          number_sub_tasks = 0;
          
          // Prepare the report header
          DataOutputStream report_stream = HTMLWriter.addFrame("Artists and composers with few recordings", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report alphabetically lists artists with fewer than " + artist_cutoff + " recordings and/or composers with fewer than " + composer_cutoff + " recordings in the music collection. Artists and/or composers with fewer than half these numbers of recordings are listed in bold. Field uniqueness is determined before any find and replace, word reordering subset or edit distance operations have been performed.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Generate report on artists
          if (preferences.list_artists_with_few_recordings)
          {
               fillInFewRecordings(artists, artist_cutoff, report_stream, "Artist");
               
               if (preferences.list_composers_with_few_recordings)
                    HTMLWriter.addHorizontalRule(report_stream);
               
               number_sub_tasks++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks);
          }
          
          // Generate report on composers
          if (preferences.list_composers_with_few_recordings)
          {
               fillInFewRecordings(composers, composer_cutoff, report_stream, "Composer");
               
               number_sub_tasks++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks);
          }
          
          // End the report
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Reports all entries in to_check with fewer than cutoff recordings.
      *
      * @param      to_check            The Entries to check.
      * @param      cutoff              The minimum number of recordings that
      *                                 may be had to avoid being reported by
      *                                 this method.
      * @param      recording_stream    Where to write the report.
      * @param      key_string          Identifies what is being reported.
      * @throws     Exception           Throws an exception if a problem occurs.
      */
     private void fillInFewRecordings(Entries to_check, int cutoff,
          DataOutputStream report_stream, String key_string)
          throws Exception
     {
          // Prepare variables
          int[][] indexes = to_check.getIndexes();
          Vector<int[]> to_report = new Vector<int[]>();
          int half_cutoff = cutoff / 2;
          boolean reporting_artist = false;
          if (key_string.equals("Artist"))
               reporting_artist = true;
          
          // Find ones to report
          for (int i = 0; i < indexes.length; i++)
               if (indexes[i].length < cutoff)
                    to_report.add(indexes[i]);
          
          // Write report
          HTMLWriter.addParagraph("<h2>" + key_string + "s with fewer than " + cutoff + " recordings: " + to_report.size(), report_stream);
          if (to_report.size() != 0)
          {
               String[] column_headings = {key_string, "Recordings"};
               String[][] table = new String[to_report.size()][column_headings.length];
               for (int i = 0; i < table.length; i++)
               {
                    // Bold the row if appropriate
                    String prefix = "";
                    String suffix = "";
                    if (to_report.get(i).length <= half_cutoff)
                    {
                         prefix = "<b>";
                         suffix = "</b>";
                    }
                    
                    // Add the row
                    if (reporting_artist)
                    {
                         if (recordings[to_report.get(i)[0]].artist == null)
                              table[i][0] = prefix + "UNKNOWN" + suffix;
                         else table[i][0] = prefix + recordings[to_report.get(i)[0]].artist + suffix;
                    }
                    else
                    {
                         if (recordings[to_report.get(i)[0]].composer == null)
                              table[i][0] = prefix + "UNKNOWN" + suffix;
                         else table[i][0] = prefix + recordings[to_report.get(i)[0]].composer + suffix;
                    }
                    table[i][1] = prefix + to_report.get(i).length + suffix;
               }
               HTMLWriter.addTable(table, column_headings, report_stream);
          }
     }
     
     
     /**
      * Merges all titles that are identical. If the preference is set, then
      * a report is also generated listing all recordings with identical titles.
      *
      * @throws     Exception An Exception is thrown if a problem occurs.
      */
     private void mergeTitles()
     throws Exception
     {
          // Prepare progress bar
          if (progress_bar != null)
          {
               int number_sub_tasks = 1;
               if (preferences.report_identical_titles)
                    number_sub_tasks++;
               progress_bar.startNewSubTask(number_sub_tasks, "Finding identical titles");
          }
          
          // Merge titles with identical entries
          titles.mergeIdenticalEntries(true, "Identical titles");
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(1);
          
          // Report titles with identical enttries
          if (preferences.report_identical_titles)
          {
               // Find the indexes of recordings after the merge
               int[][] original_indexes = titles.getIndexes();
               
               // Prepare report header
               DataOutputStream report_stream = HTMLWriter.addFrame("Exactly identical recording titles", subdirectory, contents_stream);
               HTMLWriter.addParagraph("<i>This report lists all recordings that have exactly the same title (before any processing). This is useful in seeing how many versions of the same piece exist in the music database. These duplicates may be identical copies of the same recording occurring in different places, or they may be different versions of the same piece. The \"Probable duplicates of the same recording\" report can be consulted to detect which of these recordings are likely multiple copies of the same recording.</i>", report_stream);
               HTMLWriter.addParagraph("<i>The report begins with several overall statistics, followed by a list of all titles for which there is more than one recording with the same name. Finally, the details of all such recordings in each identical title cluster are reported.</i>", report_stream);
               HTMLWriter.addHorizontalRule(report_stream);
               
               // Calculate statistics
               int[] number_versions = new int[original_indexes.length];
               for (int i = 0; i < number_versions.length; i++)
                    number_versions[i] = original_indexes[i].length;
               String average_number_versions = mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(mckay.utilities.staticlibraries.MathAndStatsMethods.getAverage(number_versions), 2);
               String standard_deviation = mckay.utilities.staticlibraries.StringMethods.getRoundedDouble(mckay.utilities.staticlibraries.MathAndStatsMethods.getStandardDeviation(number_versions), 2);
               
               // Find number of identical title clusters
               int number_identicals = 0;
               int number_affected_recordings = 0;
               int[][] indexes = new int[original_indexes.length][];
               for (int i = 0; i < indexes.length; i++)
               {
                    if (original_indexes[i].length > 1)
                    {
                         indexes[i] = original_indexes[i];
                         number_identicals++;
                         number_affected_recordings += indexes[i].length;
                    }
                    else indexes[i] = null;
               }
               
               // Write body of report
               if (number_identicals == 0)
                    HTMLWriter.addParagraph("<h2>No titles are present with exactly the same title.</h2>", report_stream);
               else
               {
                    // Add overall information
                    HTMLWriter.addParagraph("<h3>" + number_identicals + " groups of are titles are present with exactly the same title. A total of " + number_affected_recordings + " recordings are involved.</h3>", report_stream);
                    HTMLWriter.addParagraph("<h3>There is an average of " + average_number_versions + " recordings per unique title in the music collection as a whole, with a standard deviation of " + standard_deviation + ".</h3>", report_stream);
                    
                    // Add summary
                    HTMLWriter.addHorizontalRule(report_stream);
                    HTMLWriter.addParagraph("<h3>Summary:</h3>", report_stream);
                    Vector<String> summary_of_titles = new Vector<String>();
                    Vector<Integer> summary_counts = new Vector<Integer>();
                    for (int i = 0; i < indexes.length; i++)
                         if (indexes[i] != null)
                         {
                         summary_of_titles.add(recordings[indexes[i][0]].title);
                         summary_counts.add(new Integer(indexes[i].length));
                         }
                    String[] summary_of_titles_array = new String[summary_of_titles.size()];
                    for (int i = 0; i < summary_of_titles_array.length; i++)
                         summary_of_titles_array[i] = summary_of_titles.get(i) + " <i>(occurs " + summary_counts.get(i).toString() + " times)</i>";
                    HTMLWriter.addList(summary_of_titles_array, true, report_stream);
                    
                    // Add details
                    for (int i = 0; i < indexes.length; i++)
                    {
                         if (indexes[i] != null)
                         {
                              HTMLWriter.addHorizontalRule(report_stream);
                              HTMLWriter.addParagraph("<h3>\"" + recordings[indexes[i][0]].title + "\" occurs " + indexes[i].length + " times:</h3>", report_stream);
                              String[][] table = new String[indexes[i].length][];
                              String[][] sub_table_column_headings = new String[1][];
                              for (int j = 0; j < table.length; j++)
                                   table[j] = recordings[indexes[i][j]].getPartialFormattedFieldContents(j + 1, sub_table_column_headings);
                              HTMLWriter.addTable(table, sub_table_column_headings[0], report_stream);
                         }
                    }
               }
               
               // Update progress bar
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(2);
          }
     }
     
     
     /**
      * Reports all clusters of fields that start with a space.
      *
      * @throws Exception     Throws an Exception if a problem occurs.
      */
     private void detectFieldsStartingWithSpaces()
     throws Exception
     {
          // Prepare the progress bar
          if (progress_bar != null)
               progress_bar.startNewSubTask(5, "Finding fields starting with a space");
          
          // Prepare the report header
          DataOutputStream report_stream = HTMLWriter.addFrame("Fields starting with a space", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report separately lists all titles, artists, composers, albums and genres that start with a space. Each such field is only listed once when multiple fields are identical. This analysis is done before any requested find and replace, word reordering subset or edit distance operations have been performed. This report is useful in immediately highlighting this particularly common error.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Generate the reports
          reportFielsStartingWithSpace(titles, report_stream, 1, "Title");
          reportFielsStartingWithSpace(artists, report_stream, 2, "Artist");
          reportFielsStartingWithSpace(composers, report_stream, 3, "Composer");
          reportFielsStartingWithSpace(albums, report_stream, 4, "Album");
          reportFielsStartingWithSpace(genres, report_stream, 5, "Genre");
          
          // End the report
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Reports a cluster of fields that start with a space.
      *
      * @param  to_check      The fields to check.
      * @param  report_stream Where to write the report.
      * @param  progress      The amount of progress to mark on the progress
      *                       bar when done.
      * @param  key           An identifying key used in the report.
      * @throws Exception     Throws an Exception if a problem occurs.
      */
     private void reportFielsStartingWithSpace(Entries to_check,
          DataOutputStream report_stream, int progress, String key)
          throws Exception
     {
          Vector<String> start_with_space = new Vector<String>();
          String[] values = to_check.getValues();
          for (int i = 0; i < values.length; i++)
               if (values[i] != null)
                    if (values[i].length() > 0)
                         if (values[i].charAt(0) == ' ')
                              start_with_space.add(values[i]);
          
          if (progress != 1)
               HTMLWriter.addHorizontalRule(report_stream);
          HTMLWriter.addParagraph("<h2>" + key + " clusters present that start with a space: " + start_with_space.size() + "</h2>", report_stream);
          if (start_with_space.size() > 0)
          {
               String[] results = start_with_space.toArray(new String[1]);
               HTMLWriter.addList(results, true, report_stream);
          }
          
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(progress);
     }
     
     
     /**
      * Generate a report listing albums and information about them in various
      * ways, as set by the preferences.
      *
      * @throws  Exception    Throws an exception if a problem occurs.
      */
     private void listAlbums()
     throws Exception
     {
          // Prepare sub-task progress bar
          int number_sub_tasks = 0;
          if (preferences.list_albums_by_artist)
               number_sub_tasks++;
          if (preferences.list_albums_by_composer)
               number_sub_tasks++;
          if (preferences.list_incomplete_albums)
               number_sub_tasks++;
          if (preferences.list_albums_with_duplicate_tracks)
               number_sub_tasks++;
          if (preferences.list_albums_missing_year)
               number_sub_tasks++;
          if (preferences.report_on_compilation_albums)
               number_sub_tasks++;
          if (progress_bar != null)
               progress_bar.startNewSubTask(number_sub_tasks, "Analyzing album information");
          number_sub_tasks = 0;
          
          // List albums by artist
          if (preferences.list_albums_by_artist)
          {
               generateAlbumBreakdown(RecordingMetaData.ARTIST_IDENTIFIER, artists);
               
               number_sub_tasks++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks);
          }
          
          // List albums by composer
          if (preferences.list_albums_by_composer)
          {
               generateAlbumBreakdown(RecordingMetaData.COMPOSER_IDENTIFIER, composers);
               
               number_sub_tasks++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks);
          }
          
          // List incomplete albums
          if (preferences.list_incomplete_albums)
          {
               listIncompleteAlbums();
               
               number_sub_tasks++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks);
          }
          
          // List albums with duplicate or missing track numbers
          if (preferences.list_albums_with_duplicate_tracks)
          {
               reportAlbumsWithDuplicateTracks();
               
               number_sub_tasks++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks);
          }
          
          // List albums with duplicate track numbers
          if (preferences.list_albums_missing_year)
          {
               reportMissingYear();
               
               number_sub_tasks++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks);
          }
          
          // Report on compilations
          if (preferences.report_on_compilation_albums)
          {
               reportOnCompilations();
               
               number_sub_tasks++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks);
          }
     }
     
     
     /**
      * Generate a report listing all albums that correspond to each artist or
      * composer (as defined by the field_key parameter), as well as other
      * statistics on each album.
      *
      * @param   field_key    Either RecordingMetaData.ARTIST_IDENTIFIER or
      *                       RecordingMetaData.COMPOSER_IDENTIFIER.
      * @param   basis        Either artists or composers. This is what albums
      *                       are organized around.
      * @throws  Exception    Throws an exception if a problem occurs.
      */
     private void generateAlbumBreakdown(int field_key, Entries basis)
     throws Exception
     {
          // Determine whether albums are to be listed by artist or composer
          String key = "unknown field key";
          if (field_key == RecordingMetaData.ARTIST_IDENTIFIER)
               key = "artist";
          else if (field_key == RecordingMetaData.COMPOSER_IDENTIFIER)
               key = "composer";
          
          // Prepare the column headings
          String[] column_headings = {"Album name", "Tracks available in dataset", "Total tracks in album",  "% of total tracks present and by this " + key, "Marked as compilation"};
          
          // Prepare the report header
          DataOutputStream report_stream = HTMLWriter.addFrame("Albums listed by " + key, subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report alphabetically lists each unique " + key + " present. Uniqueness of " + key + " is determined before any requested find and replace, word reordering subset or edit distance operations have been performed. All albums present that have at least one recording by each given " + key + " are listed under that " + key + "'s entry. Note is also made of how many tracks by each given " + key + " belonging to the given album are present in the music collection, how many total tracks the album is marked as containing (cumulative for mult-disc sets, if annotated), what percentage of the total tracks are by the given " + key + " (marked in bold if not equal to 100%) and whether each album is marked as a compilation.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Sort the artists or composers if not already sorted
          basis.sortEntries();
          
          // Generate the report
          int number_entries = basis.getNumberEntries();
          int[][] indexes = basis.getIndexes();
          for (int i = 0; i < number_entries; i++)
          {
               // Find all recordings by this artist or composer
               RecordingMetaData[] recordings_belonging_to_this_one = new RecordingMetaData[indexes[i].length];
               for (int j = 0; j < recordings_belonging_to_this_one.length; j++)
                    recordings_belonging_to_this_one[j] = recordings[indexes[i][j]];
               
               // Merge album titles by this artist or composer that have the
               // same album title
               Entries these_albums = new Entries(recordings_belonging_to_this_one, RecordingMetaData.ALBUM_IDENTIFIER);
               these_albums.mergeIdenticalEntries(false, null);
               String[] titles_of_these_albums = these_albums.getValues();
               int[][] indexes_of_recordings_in_these_albums = these_albums.getIndexes();
               
               // Add the name of the current artist or composer to the report
               String heading = "";
               if (field_key == RecordingMetaData.ARTIST_IDENTIFIER)
                    heading = recordings[indexes[i][0]].artist;
               else if (field_key == RecordingMetaData.COMPOSER_IDENTIFIER)
                    heading = recordings[indexes[i][0]].composer;
               if (heading == null)
                    heading = "UNKNOWN";
               HTMLWriter.addParagraph("<h2>" + heading + ": <i>" + these_albums.getNumberEntries() + " albums</i></h2>", report_stream);
               
               // Fill in the table for the current artist or composer
               String[][] table = new String[these_albums.getNumberEntries()][column_headings.length];
               for (int j = 0; j < table.length; j++)
               {
                    // Add the title of the album
                    if (titles_of_these_albums[j] == null)
                         table[j][0] = "UNKNOWN";
                    else
                         table[j][0] = titles_of_these_albums[j];
                    
                    // Add the total number of tracks present in the current
                    // album by the current artist or composer
                    int number_tracks_present_by_this_one = indexes_of_recordings_in_these_albums[j].length;
                    table[j][1] = "" + number_tracks_present_by_this_one;
                    
                    // Add the total number of tracks in this album by any
                    // artist or composer
                    int total_tracks = RecordingMetaData.getTotalNumberOfTracks(recordings_belonging_to_this_one, indexes_of_recordings_in_these_albums[j]);
                    if (total_tracks == -1)
                         table[j][2] = "UNKNOWN";
                    else table[j][2] = "" + total_tracks;
                    
                    // Add the percentage of total tracks in the album that are
                    // both present and by the current artist or composer
                    if (total_tracks == -1)
                         table[j][3] = "UNKNOWN";
                    else if (number_tracks_present_by_this_one == total_tracks)
                         table[j][3] = "100%";
                    else
                         table[j][3] = "<b>" + (100 * number_tracks_present_by_this_one / total_tracks) + "%</b>";
                    
                    // Add whether or not this album is marked as a compilation
                    int compilation_status = recordings_belonging_to_this_one[indexes_of_recordings_in_these_albums[j][0]].compilation;
                    if (compilation_status == 1)
                         table[j][4] = "<b>Yes</b>";
                    else if (compilation_status == 0)
                         table[j][4] = "No";
                    else if (compilation_status == -1)
                         table[j][4] = "UNKNOWN";
               }
               HTMLWriter.addTable(table, column_headings, report_stream);
          }
          
          // End the report
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Generates a report consisting of an alphabetical list of all albums that
      * are missing tracks followed by a list of all albums with an unknown
      * number of tracks. In the case of multi-album sets, the total number of
      * tracks includes all tracks from all component albums, as long as they
      * all have the same name.
      *
      * <p>Albums where less than the of the total tracks are in the database
      * specified in the preferences are listed in bold.
      *
      * <p>This report is generated before andy find and replace, word
      * reordering subset or edit distance find/replace operations have been
      * performed.
      *
      * @throws Exception     Throws an exception if a problem occurs.
      */
     private void listIncompleteAlbums()
     throws Exception
     {
          // Prepare the report header
          DataOutputStream report_stream = HTMLWriter.addFrame("Incomplete albums", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report consists of an alphabetical list of all albums that are missing tracks, followed by a list of all albums with an unknown number of tracks. In the case of multi-album sets, the total number of tracks includes all tracks from all component albums, as long as they all have the same name.</i><p><i>Albums that include less than " + preferences.incoplete_albums_threshold + "% of the total number of tracks that could be on the album are listed in bold.</i><p><i>This report is generated before any find and replace, word reordering subset or edit distance operations have been performed.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Sort the albums if they are not already
          albums.sortEntries();
          
          // Prepare information about the albums
          int number_albums = albums.getNumberEntries();
          int[][] album_indexes = albums.getIndexes();
          
          // Find all incomplete albums and albums with an unknown number of
          // tracks
          Vector<int[]> incompletes = new Vector<int[]>();
          Vector<int[]> unknowns = new Vector<int[]>();
          for (int i = 0; i < number_albums; i++)
          {
               // Find the total number of tracks in this album
               int total_tracks = RecordingMetaData.getTotalNumberOfTracks(recordings, album_indexes[i]);
               
               // Store the album if the number of tracks are unknown
               if (total_tracks == -1)
                    unknowns.add(album_indexes[i]);
               
               // Store the album if it is incomplete
               else if (total_tracks != album_indexes[i].length)
                    incompletes.add(album_indexes[i]);
          }
          
          // Report all incomplete albums
          fillInAlbumIncompletesOrMissings(incompletes, report_stream, "incomplete");
          
          // Report all albums with an unknown number of tracks
          HTMLWriter.addHorizontalRule(report_stream);
          fillInAlbumIncompletesOrMissings(unknowns, report_stream, "unknown");
          
          // End the report
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Takes the given indexes of entries in the recordings field and generates
      * report tables based on them for the calling listIncompleteAlbums method.
      *
      * @param indexes        A vector of index arrays, where each array
      *                       corresponds to a set of recordings in the
      *                       recordings field all belonging to the same album.
      * @param report_string  Where to write the report
      * @param note_string    A key string to include in the report identifying
      *                       the generated table.
      */
     private void fillInAlbumIncompletesOrMissings(Vector<int[]> indexes,
          DataOutputStream report_stream, String note_string)
          throws Exception
     {
          if (indexes.size() == 0)
               HTMLWriter.addParagraph("<h2>No albums with an " + note_string + " number of tracks found.</h2>", report_stream);
          else
          {
               // Initialize settings
               String[] column_headings = {"Album Title", "Tracks available in dataset", "Total tracks in album", "% of Total Tracks Present", "Marked as compilation", "Artist"};
               int acceptable_percentage = preferences.incoplete_albums_threshold;
               int amount = indexes.size();
               
               // Prepare the report
               HTMLWriter.addParagraph("<h2>" + amount + " albums found whose total number of tracks are " + note_string + ".</h2>", report_stream);
               String[][] table = new String[amount][column_headings.length];
               for (int i = 0; i < amount; i++)
               {
                    // Find the percentage of tracks present in the album
                    int number_tracks = indexes.get(i).length;
                    int total_tracks = RecordingMetaData.getTotalNumberOfTracks(recordings, indexes.get(i));
                    int percentage_present = -1;
                    if (total_tracks != -1)
                         percentage_present = 100 * number_tracks / total_tracks;
                    
                    // Bold the row if appropriate
                    String prefix = "";
                    String suffix = "";
                    if (percentage_present < acceptable_percentage && total_tracks != -1)
                    {
                         prefix = "<b>";
                         suffix = "</b>";
                    }
                    
                    if (recordings[((int[]) indexes.get(i))[0]].album != null)
                         table[i][0] = prefix + recordings[((int[]) indexes.get(i))[0]].album + suffix;
                    else table[i][0] = prefix + "UNKNOWN" + suffix;
                    
                    table[i][1] = prefix + number_tracks + suffix;
                    
                    if (total_tracks != -1)
                         table[i][2] = prefix + total_tracks + suffix;
                    else table[i][2] = prefix + "UNKNOWN" + suffix;
                    
                    if (percentage_present != -1)
                         table[i][3] = prefix + percentage_present + "%" + suffix;
                    else table[i][3] = prefix + "UNKNOWN" + suffix;
                    
                    int compilation_status = recordings[((int[]) indexes.get(i))[0]].compilation;
                    if (compilation_status == 1)
                         table[i][4] = prefix + "Yes" + suffix;
                    else if (compilation_status == 0)
                         table[i][4] = prefix + "No" + suffix;
                    else if (compilation_status == -1)
                         table[i][4] = prefix + prefix + "UNKNOWN" + suffix;
                    
                    RecordingMetaData[] subset = RecordingMetaData.filterRecordings(recordings, indexes.get(i));
                    if (RecordingMetaData.haveIdenticalFields(subset, RecordingMetaData.ARTIST_IDENTIFIER))
                    {
                         if (recordings[((int[]) indexes.get(i))[0]].artist != null)
                              table[i][5] = prefix + recordings[((int[]) indexes.get(i))[0]].artist + suffix;
                         else table[i][5] = prefix + "UNKNOWN" + suffix;
                    }
                    else
                         table[i][5] = prefix + "MULTIPLE ARTISTS" + suffix;
               }
               
               // Write the report
               HTMLWriter.addTable(table, column_headings, report_stream);
          }
     }
     
     
     /**
      * Generates a list of all albums with more than one recording with the
      * same track number or that have at least one recording with an unknown
      * track number.
      *
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private void reportAlbumsWithDuplicateTracks()
     throws Exception
     {
          // Sort the albums if they are not already
          albums.sortEntries();
          
          // Find the track numbers available for each album
          int[][] recording_indexes = albums.getIndexes();
          int[][] track_numbers = new int[recording_indexes.length][];
          for (int i = 0; i < track_numbers.length; i++)
          {
               track_numbers[i] = new int[recording_indexes[i].length];
               for (int j = 0; j < track_numbers[i].length; j++)
                    track_numbers[i][j] = recordings[recording_indexes[i][j]].track_number;
          }
          
          // Find the number of unknown track numbers per album
          int[] number_unknowns = new int[track_numbers.length];
          for (int i = 0; i < number_unknowns.length; i++)
          {
               number_unknowns[i] = 0;
               for (int j = 0; j < track_numbers[i].length; j++)
                    if (track_numbers[i][j] == -1)
                         number_unknowns[i]++;
          }
          
          // Find duplicates (marking with null if there are no duplicates)
          int[][] duplicate_track_numbers = new int[track_numbers.length][];
          int[][] number_copies = new int[track_numbers.length][];
          for (int i = 0; i < track_numbers.length; i++)
          {
               duplicate_track_numbers[i] = null;
               number_copies[i] = null;
               Vector<Integer> these_duplicate_track_numbers = new Vector<Integer>();
               Vector<Integer> these_number_copies = new Vector<Integer>();
               for (int j = 0; j < track_numbers[i].length; j++)
               {
                    int count = 0;
                    int this_track_number = track_numbers[i][j];
                    if (this_track_number != -1)
                    {
                         for (int k = j + 1; k < track_numbers[i].length; k++)
                         {
                              if (track_numbers[i][k] == this_track_number)
                              {
                                   if (recordings[recording_indexes[i][j]].disc_number == recordings[recording_indexes[i][k]].disc_number)
                                   {
                                        track_numbers[i][k] = -1;
                                        count++;
                                   }
                              }
                         }
                    }
                    if (count > 0)
                    {
                         these_duplicate_track_numbers.add(track_numbers[i][j]);
                         these_number_copies.add(count + 1);
                    }
               }
               if (these_duplicate_track_numbers.size() != 0)
               {
                    Integer[] these_duplicate_track_numbers_I = these_duplicate_track_numbers.toArray(new Integer[1]);
                    duplicate_track_numbers[i] = new int[these_duplicate_track_numbers_I.length];
                    Integer[] these_number_copies_I = these_number_copies.toArray(new Integer[1]);
                    number_copies[i] = new int[these_number_copies_I.length];
                    for (int j = 0; j < these_duplicate_track_numbers_I.length; j++)
                    {
                         duplicate_track_numbers[i][j] = these_duplicate_track_numbers_I[j].intValue();
                         number_copies[i][j] = these_number_copies_I[j].intValue();
                    }
               }
          }
          
          // Write the report header
          DataOutputStream report_stream = HTMLWriter.addFrame("Albums with duplicate or unknown track numbers", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report consists of an alphabetical list of all albums with the same name that contain more than one recording with the same track number or that contain one or more recordings that do not have a track number specified. This report is generated before any find and replace, word reordering subset or edit distance operations have been performed. Note that some albums may be inappropriately listed here in cases where two different albums have exactly the same name.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Prepare the table
          String[] column_headings = {"Album Title", "Number of tracks with an unknown track number", "Number of tracks that have duplicates", "Tracks that are duplicates", "Copies of each track"};
          Vector<String[]> rows = new Vector<String[]>();
          for (int i = 0; i < track_numbers.length; i++)
          {
               if (duplicate_track_numbers[i] != null || number_unknowns[i] != 0)
               {
                    String number_duplicate_track_numbers = "0";
                    String duplicates = "";
                    String copies = "";
                    if (duplicate_track_numbers[i] != null)
                    {
                         number_duplicate_track_numbers = "<b>" + String.valueOf(duplicate_track_numbers[i].length) + "</b>";
                         for (int j = 0; j < duplicate_track_numbers[i].length; j++)
                         {
                              if (j != 0)
                              {
                                   duplicates += "<br>";
                                   copies += "<br>";
                              }
                              duplicates += duplicate_track_numbers[i][j];
                              copies += number_copies[i][j];
                         }
                    }
                    String[] album_names = albums.getValues();
                    String report_number_of_unknowns = "0";
                    if (number_unknowns[i] != 0)
                         report_number_of_unknowns = "<b>" + String.valueOf(number_unknowns[i]) + "</b>";
                    String[] row = {album_names[i], report_number_of_unknowns, number_duplicate_track_numbers, duplicates, copies};
                    rows.add(row);
               }
          }
          
          // Write report
          if (rows.size() == 0)
               HTMLWriter.addParagraph("<h2>No albums found with duplicates of the same track number or with tracks that have an unknown track number.</h2>", report_stream);
          else
          {
               HTMLWriter.addParagraph("<h2>" + rows.size() + " albums found containing multiple tracks with the same track number and/or with at least one track that has an unknown track number.</h2>", report_stream);
               String[][] table = rows.toArray(new String[1][1]);
               HTMLWriter.addTable(table, column_headings, report_stream);
          }
          
          // End the report
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Generates a list of albums containing one or more recordings that do not
      * have year metadata specified.
      *
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private void reportMissingYear()
     throws Exception
     {
          // Sort the albums if they are not already
          albums.sortEntries();
          
          // Find the number of recordings missing year metadata per album
          int[] number_missing = new int[albums.getNumberEntries()];
          int[] tracks_present_per_album = new int[albums.getNumberEntries()];
          int[] percent_missing = new int[albums.getNumberEntries()];
          int[][] indexes = albums.getIndexes();
          for (int i = 0; i < indexes.length; i++)
          {
               number_missing[i] = 0;
               for (int j = 0; j < indexes[i].length; j++)
                    if (recordings[indexes[i][j]].year == -1)
                         number_missing[i]++;
               tracks_present_per_album[i] = indexes[i].length;
               percent_missing[i] = 100 * number_missing[i] / tracks_present_per_album[i];
          }
          
          // Perform report
          DataOutputStream report_stream = HTMLWriter.addFrame("Albums with unspecified year", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists all albums that have one or more recordings missing year metadata.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Prepare table
          String[] column_headings = {"Album title", "Number tracks missing year", "Total tracks present", "% Tracks missing year"};
          Vector<String[]> rows = new Vector<String[]>();
          String[] album_titles = albums.getValues();
          for (int i = 0; i < number_missing.length; i++)
               if (number_missing[i] > 0)
               {
               String[] row = {album_titles[i], String.valueOf(number_missing[i]), String.valueOf(tracks_present_per_album[i]), String.valueOf(percent_missing[i])};
               rows.add(row);
               }
          
          // Write report
          if (rows.size() == 0)
               HTMLWriter.addParagraph("<h2>No albums found that have one or more tracks missing year metadata.</h2>", report_stream);
          else
          {
               HTMLWriter.addParagraph("<h2>" + rows.size() + " albums found containing containing one or more tracks missing year metadata.</h2>", report_stream);
               String[][] table = rows.toArray(new String[1][1]);
               HTMLWriter.addTable(table, column_headings, report_stream);
          }
          
          // Finalize report
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Generates a report alphabetically listing all unique albums that contain
      * at least one recording marked as a compilation or with an unknown
      * compilation status. Albums where some recordings are marked as
      * compilations and others are not are written in bold, since all tracks in
      * an album should have the same compilation marking. Uniqueness of album
      * name is determined before any requested find and replace, word
      * reordering subset or edit distance operations have been  made.
      *
      * <p>>An album should be marked as a compilation if and only if not all of
      * its component recordings have the same value for their artist fields. A
      * separate report is therefore also included of all albums that are not
      * marked as compilations but contain multiple artists, as is another
      * report of of albums that are marked as compilations but contain only one
      * artist.
      *
      * @throws     Exception An exception is thrown if a problem occurs.
      */
     private void reportOnCompilations()
     throws Exception
     {
          // Prepare the report header
          DataOutputStream report_stream = HTMLWriter.addFrame("Report on compilation albums", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report alphabetically lists all unique albums that contain at least one recording marked as a compilation or with an unknown compilation status. Albums where some recordings are marked as compilations and others are not are written in bold, since all tracks in an album should have the same compilation marking. Uniqueness of album name is determined before any requested find and replace, word reordering subset or edit distance operations have been performed.</i><p><i>An album should be marked as a compilation if and only if not all of its component recordings have the same value for their artist fields. A separate report is therefore also included of all albums that are incorrectly not marked as compilations but contain multiple artists (note that some entirely different albums sometimes have an identical name, which will result in incorrect inclusion here). Another report of albums that are incorrectly marked as compilations but contain only one artist is also generated (some of these may in fact be compilation albums that are missing all tracks by all but one artists).</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Sort the albums if they're not already sorted
          albums.sortEntries();
          
          // To store results
          Vector<int[]> marked_as_compilation_album_indices = new Vector<int[]>();
          Vector<int[]> marked_as_compilation_stats = new Vector<int[]>();
          Vector<int[]> should_be_compilation_album_indices = new Vector<int[]>();
          Vector<int[]> should_be_compilation_stats = new Vector<int[]>();
          Vector<int[]> should_not_be_compilation_album_indices = new Vector<int[]>();
          Vector<int[]> should_not_be_compilation_stats = new Vector<int[]>();
          
          // Examine all albums and their tracks
          int[][] album_indices = albums.getIndexes();
          for (int i = 0; i < album_indices.length; i++)
          {
               // Find number of recordings in this album marked as compilations
               int compilation_count = 0;
               int non_compilation_count = 0;
               int unknown_compilation_count = 0;
               for (int j = 0; j < album_indices[i].length; j++)
               {
                    if (recordings[album_indices[i][j]].compilation == 0)
                         non_compilation_count++;
                    else if (recordings[album_indices[i][j]].compilation == 1)
                         compilation_count++;
                    else if (recordings[album_indices[i][j]].compilation == -1)
                         unknown_compilation_count++;
               }
               
               // Find number of artists per album
               RecordingMetaData[] recordings_in_this_album = RecordingMetaData.filterRecordings(recordings, album_indices[i]);
               Entries artists_in_album = new Entries(recordings_in_this_album, RecordingMetaData.ARTIST_IDENTIFIER);
               artists_in_album.mergeIdenticalEntries(false, null);
               int artist_count = artists_in_album.getNumberEntries();
               
               // Prepare stats for this album
               int[] stats = {artist_count, compilation_count, non_compilation_count, unknown_compilation_count};
               
               // Prepare reports
               if (compilation_count != 0 || unknown_compilation_count != 0)
               {
                    // Note albums marked as compilations
                    marked_as_compilation_album_indices.add(album_indices[i]);
                    marked_as_compilation_stats.add(stats);
                    
                    // Note albums that should not be marked as compilations
                    if (artist_count == 1)
                    {
                         should_not_be_compilation_album_indices.add(album_indices[i]);
                         should_not_be_compilation_stats.add(stats);
                    }
               }
               // Note albums that should be marked as compilations
               else if (artist_count != 1)
               {
                    should_be_compilation_album_indices.add(album_indices[i]);
                    should_be_compilation_stats.add(stats);
               }
          }
          
          // Write reports
          fillInCompilationTable(marked_as_compilation_album_indices, marked_as_compilation_stats, report_stream, "are marked as compilations");
          HTMLWriter.addHorizontalRule(report_stream);
          fillInCompilationTable(should_be_compilation_album_indices, should_be_compilation_stats, report_stream, "should be marked as compilations but are not");
          HTMLWriter.addHorizontalRule(report_stream);
          fillInCompilationTable(should_not_be_compilation_album_indices, should_not_be_compilation_stats, report_stream, "should not be marked as compilations but are");
          
          // End the report
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Writes a table to the given stream regarding compilation albums.
      *
      * @param      recording_indexes   The indexes of recordings in the
      *                                 recordings field. Each entry in the
      *                                 vector corresponds to an album, and
      *                                 each entry in the held int[] is a
      *                                 recording in the album.
      * @param      statistics          Statistics on each album. Indexes match
      *                                 recording_indexes.
      * @param      report_stream       Where to write the data.
      * @param      note_string         An identifying string.
      * @throws     Exception           Throws an exception if a problem occurs.
      */
     private void fillInCompilationTable(Vector<int[]> recording_indexes,
          Vector<int[]> statistics, DataOutputStream report_stream,
          String note_string)
          throws Exception
     {
          // Initialize
          String[] column_headings = {"Album title", "Number artists", "Number tracks available", "Compilation tracks", "Non-compilation tracks", "Unknown status tracks"};
          int number_albums = recording_indexes.size();
          boolean is_compilation_summary = false;
          if (note_string.equals("are marked as compilations"))
               is_compilation_summary = true;
          
          // Write the header
          HTMLWriter.addParagraph("<h2>" + number_albums + " albums found that " + note_string + ".</h2>", report_stream);
          
          // Write the table
          if (number_albums != 0)
          {
               String[][] table = new String[number_albums][column_headings.length];
               for (int i = 0; i < table.length; i++)
               {
                    String prefix = "";
                    String suffix = "";
                    if (is_compilation_summary)
                    {
                         if (statistics.get(i)[2] != 0 || statistics.get(i)[3] != 0)
                         {
                              prefix = "<b>";
                              suffix = "</b>";
                         }
                    }
                    
                    if(recordings[recording_indexes.get(i)[0]].album == null)
                         table[i][0] = prefix + "UNKNOWN" + suffix;
                    else table[i][0] = prefix + recordings[recording_indexes.get(i)[0]].album + suffix;
                    
                    table[i][1] = prefix + statistics.get(i)[0] + suffix;
                    
                    table[i][2] = prefix + recording_indexes.get(i).length + suffix;
                    
                    table[i][3] = prefix + statistics.get(i)[1] + suffix;
                    
                    table[i][4] = prefix + statistics.get(i)[2] + suffix;
                    
                    table[i][5] = prefix + statistics.get(i)[3] + suffix;
               }
               HTMLWriter.addTable(table, column_headings, report_stream);
          }
     }
     
     
     /**
      * Convert all titles, artists, composers, albums and genres to lowercase
      * and merges them. A report is generated detailing which titles, artists,
      * composers, albums and genres were identical except for case.
      */
     private void convertCase()
     throws Exception
     {
          // Prepare progress bar
          int number_sub_tasks = 0;
          int current_task = 0;
          if (preferences.report_probable_duplicates || preferences.report_wrongly_differing_titles)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_artists)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_composers)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_albums)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_genres)
               number_sub_tasks++;
          if (preferences.report_differing_only_in_case)
               number_sub_tasks++;
          if (progress_bar != null)
               progress_bar.startNewSubTask(number_sub_tasks, "Converting to lower case and reorganizing");
          
          // Convert case and reorganize
          String[] titles_merged = null;
          if (preferences.report_probable_duplicates || preferences.report_wrongly_differing_titles)
          {
               titles.convertValuesToLowerCase();
               titles_merged = titles.mergeIdenticalEntries(true, "Letter capitalization");
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          String[] artists_merged = null;
          if (preferences.report_wrongly_differing_artists)
          {
               artists.convertValuesToLowerCase();
               artists_merged = artists.mergeIdenticalEntries(true, "Letter capitalization");
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          String[] composers_merged = null;
          if (preferences.report_wrongly_differing_composers)
          {
               composers.convertValuesToLowerCase();
               composers_merged = composers.mergeIdenticalEntries(true, "Letter capitalization");
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          String[] albums_merged = null;
          if (preferences.report_wrongly_differing_albums)
          {
               albums.convertValuesToLowerCase();
               albums_merged = albums.mergeIdenticalEntries(true, "Letter capitalization");
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          String[] genres_merged = null;
          if (preferences.report_wrongly_differing_genres)
          {
               genres.convertValuesToLowerCase();
               genres_merged = genres.mergeIdenticalEntries(true, "Letter capitalization");
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          
          // Generate report
          if (preferences.report_differing_only_in_case)
          {
               DataOutputStream report_stream = HTMLWriter.addFrame("Fields differing only in case", subdirectory, contents_stream);
               HTMLWriter.addParagraph("<i>Entries listed below indicate field values where otherwise identical (before any other find/replace, word subset reordering or edit distance processing has been performed) title, artist, composer, genre and album fields differ in case. One list is produced for each of these fields. It will likely be appropriate to correct metadata so that all fields that are otherwise identical also have the same case.</i>", report_stream);
               HTMLWriter.addParagraph("<i>Only one sample value is listed for each cluster of results, and it reflects all selected processing performed up until the time that this report was generated. For example, field values of \"Reggae\" and \"reggae\" will result in a singe value of \"reggae\" reported here.</i>", report_stream);
               HTMLWriter.addParagraph("<i>Processing for titles, artists, composers, genres or albums are each only performed and reported if each corresponding Probable Error Report is set to be generated.</i>", report_stream);
               if (preferences.report_probable_duplicates || preferences.report_wrongly_differing_titles)
               {
                    HTMLWriter.addHorizontalRule(report_stream);
                    if (titles_merged != null)
                    {
                         HTMLWriter.addParagraph("<h2>Titles with entries that are identical in every way except for case:</h2>", report_stream);
                         HTMLWriter.addList(titles_merged, false, report_stream);
                    }
                    else HTMLWriter.addParagraph("<h2>No title entries are present which differ only in case.</h2>", report_stream);
               }
               if (preferences.report_wrongly_differing_artists)
               {
                    HTMLWriter.addHorizontalRule(report_stream);
                    if (artists_merged != null)
                    {
                         HTMLWriter.addParagraph("<h2>Artists with entries that are identical in every way except for case:</h2>", report_stream);
                         HTMLWriter.addList(artists_merged, false, report_stream);
                    }
                    else HTMLWriter.addParagraph("<h2>No artist entries are present which differ only in case.</h2>", report_stream);
               }
               if (preferences.report_wrongly_differing_composers)
               {
                    HTMLWriter.addHorizontalRule(report_stream);
                    if (composers_merged != null)
                    {
                         HTMLWriter.addParagraph("<h2>Composers with entries that are identical in every way except for case:</h2>", report_stream);
                         HTMLWriter.addList(composers_merged, false, report_stream);
                    }
                    else HTMLWriter.addParagraph("<h2>No composer entries are present which differ only in case.</h2>", report_stream);
               }
               if (preferences.report_wrongly_differing_albums)
               {
                    HTMLWriter.addHorizontalRule(report_stream);
                    if (albums_merged != null)
                    {
                         HTMLWriter.addParagraph("<h2>Albums with entries that are identical in every way except for case:</h2>", report_stream);
                         HTMLWriter.addList(albums_merged, false, report_stream);
                    }
                    else HTMLWriter.addParagraph("<h2>No album entries are present which differ only in case.</h2>", report_stream);
               }
               if (preferences.report_wrongly_differing_genres)
               {
                    HTMLWriter.addHorizontalRule(report_stream);
                    if (genres_merged != null)
                    {
                         HTMLWriter.addParagraph("<h2>Genres with entries that are identical in every way except for case:</h2>", report_stream);
                         HTMLWriter.addList(genres_merged, false, report_stream);
                    }
                    else HTMLWriter.addParagraph("<h2>No genre entries are present which differ only in case.</h2>", report_stream);
               }
               
               HTMLWriter.endHTMLFile(report_stream, true);
               
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
     }
     
     
     /**
      * Search through all titles, artists, composers, genres and albums and
      * perform the required replacements denoted in the preferences. The
      * entries are then merged for the artists, composers, albums and genres,
      * but not for the titles.
      *
      * <p>Word tokenization and tests for reordering are also performed if the
      * preferences.check_word_ordering ore preferences.check_word_subset
      * fields are true. Corresponding reports are also generated if the
      * preferences are set appropriately.
      *
      * <p>Two reports are generated, if the proper preferences are set. The
      * first details every change that was made, and the second shows which
      * fields were made newly identical after the merges.
      */
     private void performFindReplaceTokenizeOperations()
     throws Exception
     {
          // Prepare progress bar
          int number_sub_tasks = 0;
          int current_task = 0;
          if (preferences.report_probable_duplicates || preferences.report_wrongly_differing_titles)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_artists)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_composers)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_albums)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_genres)
               number_sub_tasks++;
          if (progress_bar != null)
               progress_bar.startNewSubTask(number_sub_tasks, "Performing find and replace operations, tokenizing and reorganizing");
          
          // Prepare the changes made report
          DataOutputStream write_changes_to = null;
          if (preferences.report_all_find_replace_changes)
          {
               write_changes_to = HTMLWriter.addFrame("Detailed replacements made", subdirectory, contents_stream);
               HTMLWriter.addParagraph("<i>Entries listed below indicate all changes that were made during all find/replace operations.</i>", write_changes_to);
               HTMLWriter.addParagraph("<i>These operations were performed after lowercase conversion (if requested) and before edit distance operations. The reported changes are cumulative. These find/replace operations were also performed before any requested word subset reordering operations, except for space removals, which were performed after.</i>", write_changes_to);
               HTMLWriter.addParagraph("<i>Processing for titles, artists, composers, albums and genres are each only performed and reported if each corresponding Probable Error Report is set to be generated.</i>", write_changes_to);
               HTMLWriter.addHorizontalRule(write_changes_to);
          }
          
          // Prepare the merge report
          DataOutputStream write_merges_to = null;
          if (preferences.report_new_identicals_after_fr)
          {
               write_merges_to = HTMLWriter.addFrame("Newly identical fields after find and replace", subdirectory, contents_stream);
               HTMLWriter.addParagraph("<i>Entries listed below indicate all title, artist, composer, album and genre fields that were made newly identical after the find/replace operations were performed. Any such occurrences are probably due to errors in the metadata and should be corrected. The Detailed Replacements Made report can be consulted for more information.</i>", write_merges_to);
               HTMLWriter.addParagraph("<i>These operations were performed after lowercase conversion (if requested) and before edit distance operations. These find/replace operations were also performed before any requested word subset reordering operations, except for space removals, which were performed after.</i>", write_merges_to);
               HTMLWriter.addParagraph("<i>Only one sample value is listed for each cluster of results, and it reflects all selected processing performed up until the time that this report was generated. For example, field values of \"Dr. John\" and \"Dr John\" will result in a singe value of \"dr john\" reported here.</i>", write_merges_to);
               HTMLWriter.addParagraph("<i>Processing for titles, artists, composers, albums and genres are each only performed and reported if each corresponding Probable Error Report is set to be generated.</i>", write_merges_to);
          }
          
          // Determine whether or not to find/replace spaces on first pass
          boolean no_remove_spaces = false;
          if (preferences.check_word_ordering || preferences.check_word_subset)
               no_remove_spaces = true;
          
          // Prepare the report streams for tokenize operations (note that may
          // be null based on preferences)
          DataOutputStream scrambled_report_stream = getScrambledWordOrderReportStream();
          DataOutputStream subset_report_stream = getWordSubsetReportStream();
          
          // Perform find/replace on titles
          if (preferences.report_probable_duplicates || preferences.report_wrongly_differing_titles)
          {
               findReplaceOnOneEntries(titles, "titles", true, write_changes_to, write_merges_to, no_remove_spaces, false);
               if (preferences.check_word_ordering || preferences.check_word_subset)
               {
                    performOneTokenizeOperation(titles, scrambled_report_stream, subset_report_stream, "titles");
                    findReplaceOnOneEntries(titles, "titles", true, write_changes_to, write_merges_to, false, true);
               }
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          
          // Perform find/replace and tokenize check on artists
          if (preferences.report_wrongly_differing_artists)
          {
               findReplaceOnOneEntries(artists, "artists", true, write_changes_to, write_merges_to, no_remove_spaces, false);
               if (preferences.check_word_ordering || preferences.check_word_subset)
               {
                    performOneTokenizeOperation(artists, scrambled_report_stream, subset_report_stream, "artists");
                    findReplaceOnOneEntries(artists, "artists", true, write_changes_to, write_merges_to, false, true);
               }
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          
          // Perform find/replace and tokenize check on composers
          if (preferences.report_wrongly_differing_composers)
          {
               findReplaceOnOneEntries(composers, "composers", true, write_changes_to, write_merges_to, no_remove_spaces, false);
               if (preferences.check_word_ordering || preferences.check_word_subset)
               {
                    performOneTokenizeOperation(composers, scrambled_report_stream, subset_report_stream, "composers");
                    findReplaceOnOneEntries(composers, "composers", true, write_changes_to, write_merges_to, false, true);
               }
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          
          // Perform find/replace and tokenize check on albums
          if (preferences.report_wrongly_differing_albums)
          {
               findReplaceOnOneEntries(albums, "albums", true, write_changes_to, write_merges_to, no_remove_spaces, false);
               if (preferences.check_word_ordering || preferences.check_word_subset)
               {
                    performOneTokenizeOperation(albums, scrambled_report_stream, subset_report_stream, "albums");
                    findReplaceOnOneEntries(albums, "albums", true, write_changes_to, write_merges_to, false, true);
               }
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          
          // Perform find/replace and tokenize check on genres
          if (preferences.report_wrongly_differing_genres)
          {
               findReplaceOnOneEntries(genres, "genres", true, write_changes_to, write_merges_to, no_remove_spaces, false);
               if (preferences.check_word_ordering || preferences.check_word_subset)
               {
                    performOneTokenizeOperation(genres, scrambled_report_stream, subset_report_stream, "genres");
                    findReplaceOnOneEntries(genres, "genres", true, write_changes_to, write_merges_to, false, true);
               }
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          
          // Close reports if appropriate
          if (preferences.report_all_find_replace_changes)
               HTMLWriter.endHTMLFile(write_changes_to, true);
          if (preferences.report_new_identicals_after_fr)
               HTMLWriter.endHTMLFile(write_merges_to, true);
          if (scrambled_report_stream != null)
               HTMLWriter.endHTMLFile(scrambled_report_stream, true);
          if (subset_report_stream != null)
               HTMLWriter.endHTMLFile(subset_report_stream, true);
     }
     
     
     /**
      * Performs the find and replace operations denoted by the preferences
      * field on the given Entries. Writes a report to the given
      * write_changes_to detailing all changes made (unless write_changes_to is
      * null, in which case no changes are reported).
      *
      * <p>If the perform_merge parameter is true, then a full merge is
      * performed, and the results are written to write_merges_to detailing all
      * changes made (if write_merges_to is not null).
      *
      * <p>Note that all regular expressions and replacements are converted to
      * lowevercase if the ignore_case_in_edit_distances parameter of the
      * preferences field is set.
      *
      * @param  to_perform_on      The Entries object to apply the find and
      *                            replace operation to. The operation will be
      *                            applied to the value field of each Entry
      *                            object of the to_perform_on Entries.
      * @param  name               The name to give to_perform_on in generated
      *                            reports.
      * @param  perform_merge      Whether or not a merge is to be performed.
      * @param  write_changes_to   The DataOutputStream to report all changes
      *                            made. Nothing is written if this is null.
      * @param  write_merges_to    The DataOutputStream to write all resulting
      *                            merges to. If this is null, then nothing is
      *                            written.
      * @param  no_remove_spaces   If this is true, then spaces will not be
      *                            removed, even if preferences.remove_spaces
      *                            is true. If it is false, then whether or not
      *                            spaces are removed depends only on
      *                            preferences.remove_spaces.
      * @param  only_remove_spaces If this is true, then the only kind of action
      *                            that will take place will be removal of
      *                            spaces, and even this only if
      *                            preferences.remove_spaces is true. If this
      *                            parameter is false, then which actions take
      *                            place depends on preferences.
      * @return                    True is returned if at least one change was
      *                            made.
      * @throws Exception          An exception is thrown if a problem occurs.
      */
     private boolean findReplaceOnOneEntries(Entries to_perform_on, String name,
          boolean perform_merge, DataOutputStream write_changes_to,
          DataOutputStream write_merges_to, boolean no_remove_spaces,
          boolean only_remove_spaces)
          throws Exception
     {
          // Initialize the reports
          boolean made_change = false;
          if (!only_remove_spaces)
          {
               // Add horizontal rules
               if (!name.equals("titles"))
               {
                    if (write_changes_to != null)
                         HTMLWriter.addHorizontalRule(write_changes_to);
                    if (write_merges_to != null && !name.equals("titles"))
                         HTMLWriter.addHorizontalRule(write_merges_to);
               }
               
               // Add keys
               if (write_changes_to != null)
                    HTMLWriter.addParagraph("<h2>Changes made to " + name + ":</h2>", write_changes_to);
               if (write_merges_to != null && perform_merge)
                    HTMLWriter.addParagraph("<h2>Newly identical " + name + ":</h2>", write_merges_to);
          }
          
          // Remove numbers and spaces at beginnings of titles
          if (preferences.remove_numbers_at_title_beginnings && to_perform_on == titles && !only_remove_spaces)
          {
               boolean changed = removeNumbersAndSpacesFromBeginningsOfTitles(write_changes_to, write_merges_to, perform_merge);
               if (changed) made_change = true;
          }
          
          // Convert all instances of "in'" to "ing"
          if (preferences.convert_ings && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace("in'", "ing", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Convert all instances of "Mister " to "Mr. ", "Doctor " to "Dr. "
          // and "Professor " to "Prof. "
          if (preferences.convert_title_abbreviations && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace("Mister ", "Mr. ", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
               changed = performOneFindReplace("Doctor ", "Dr. ", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
               changed = performOneFindReplace("Professor ", "Prof. ", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Remove all periods
          if (preferences.remove_periods && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace(".", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Remove all commas
          if (preferences.remove_commas && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace(",", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Remove all hyphens
          if (preferences.remove_hyphens && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace("-", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Remove all colons
          if (preferences.remove_colons && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace(":", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Remove all semicolons
          if (preferences.remove_semicolons && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace(";", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Remove all quotation marks
          if (preferences.remove_quotation_marks && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace("\"", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Remove all single quotes and apostrophes
          if (preferences.remove_single_quotes && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace("'", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Remove all brackets
          if (preferences.remove_brackets && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace("\\(", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
               changed = performOneFindReplace("\\)", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
               changed = performOneFindReplace("\\[", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
               changed = performOneFindReplace("\\]", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
               changed = performOneFindReplace("\\{", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
               changed = performOneFindReplace("\\}", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Convert all instances of " and " to " & "
          if (preferences.convert_ands_to_ampersands && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace(" and ", " & ", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Remove all instances of "the "
          if (preferences.remove_thes && !only_remove_spaces)
          {
               boolean changed = performOneFindReplace("the ", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Remove all spaces
          if (preferences.remove_spaces && !no_remove_spaces)
          {
               boolean changed = performOneFindReplace(" ", "", perform_merge, to_perform_on, write_changes_to, write_merges_to);
               if (changed) made_change = true;
          }
          
          // Return whether a change was made
          return made_change;
     }
     
     
     /**
      * Removes all spaces and numerical digits from the beginnings of all
      * value fields of all stored Entry objects in the titles field. This
      * means that all spaces and numerical digits are removed until a non-space
      * and non-numerical digit character is encountered, at which point
      * no more changes are made.
      *
      * <p>No changes are made at all to a value if all of this would result in
      * all of the contents of a value being deleted (i.e. if only numerical
      * digits and/or spaces are present).
      *
      * <p>A merge of titles is performed if the perform_merge parameter is
      * true.
      *
      * @param  write_changes_to   The DataOutputStream to report all changes
      *                            made. Nothing is written if this is null.
      * @param  write_merges_to    The DataOutputStream to write all resulting
      *                            merges to. If this is null, then nothing is
      *                            written.
      * @param  perform_merge      Whether or not to merge any newly identical
      *                            titles after the merge has been performed.
      * @return                    True is returned if at least one change was
      *                            made.
      * @throws Exception          An exception is thrown if a problem occurs.
      */
     private boolean removeNumbersAndSpacesFromBeginningsOfTitles(DataOutputStream write_changes_to,
          DataOutputStream write_merges_to, boolean perform_merge)
          throws Exception
     {
          // Perform changes
          String[][] results = titles.removeNumbersAndSpacesAtBeginningOfValues();
          
          // Report all changes made, if appropriate
          if (write_changes_to != null && results != null)
          {
               HTMLWriter.addParagraph("Removing numbers and spaces from beginnings of titles:", write_changes_to);
               String[] column_headings = {"Original", "Changed To"};
               HTMLWriter.addTable(results, column_headings, write_changes_to);
          }
          
          // Perform the merge and report the results, if appropriate
          if (results != null && perform_merge)
          {
               String[] merge_results = titles.mergeIdenticalEntries(true, "Nmbers and spaces removed from beginnings");
               if (write_merges_to != null && merge_results != null)
               {
                    HTMLWriter.addParagraph("Newly identical entries after removing numbers and spaces from beginnings:", write_merges_to);
                    HTMLWriter.addList(merge_results, true, write_merges_to);
               }
          }
          
          // Return whether or not changes were made
          if (results == null) return false;
          else return true;
     }
     
     
     /**
      * Performs a find and replace on the given to_perform_on Entries. All
      * occurrences of regex are converted to replacement. Writes a report to
      * the given write_changes_to detailing all changes made (unless
      * write_changes_to is null, in which case no changes are reported).
      *
      * <p>Note that regex and replacement are converted to lowercase if the
      * ignore_case_in_edit_distances parameter of the preferences field is set.
      *
      * <p>If the perform_merge parameter is true, then a full merge is
      * performed, and the results are written to write_merges_to detailing all
      * changes made (if write_merges_to is not null).
      *
      * @param  regex              The regular expression to search for
      * @param  replacement        The string to replace all occurrences of
      *                            regext with.
      * @param  perform_merge      Whether or not a merge is to be performed.
      * @param  to_perform_on      The Entries object to apply the find and
      *                            replace operation to. The operation will be
      *                            applied to the value field of each Entry
      *                            object of the to_perform_on Entries.
      * @param  write_changes_to   The DataOutputStream to report all changes
      *                            made. Nothing is written if this is null.
      * @param  write_merges_to    The DataOutputStream to write all resulting
      *                            merges to. If this is null, then nothing is
      *                            written.
      * @return                    True is returned if at least one change was
      *                            made.
      * @throws Exception          An exception is thrown if a problem occurs.
      */
     private boolean performOneFindReplace(String regex, String replacement,
          boolean perform_merge, Entries to_perform_on,
          DataOutputStream write_changes_to, DataOutputStream write_merges_to)
          throws Exception
     {
          // Convert regex and replacement to lowercase if appropriate
          if (preferences.ignore_case_in_edit_distances)
          {
               regex = regex.toLowerCase();
               replacement = replacement.toLowerCase();
          }
          
          // Perform the find and replace operations
          String[][] results = to_perform_on.findAndReplaceValues(regex, replacement);
          
          // Report all changes made, if appropriate
          if (write_changes_to != null && results != null)
          {
               HTMLWriter.addParagraph("Changing \"" + regex + "\" to \"" + replacement + "\":", write_changes_to);
               String[] column_headings = {"Original", "Changed To"};
               HTMLWriter.addTable(results, column_headings, write_changes_to);
          }
          
          // Perform the merge and report the results, if appropriate
          if (results != null && perform_merge)
          {
               String[] merge_results = to_perform_on.mergeIdenticalEntries(true, "Replace \"" + regex + "\" with \"" + replacement + "\"");
               if (write_merges_to != null && merge_results != null)
               {
                    HTMLWriter.addParagraph("Changing \"" + regex + "\" to \"" + replacement + "\":", write_merges_to);
                    HTMLWriter.addList(merge_results, true, write_merges_to);
               }
          }
          
          // Return whether or not changes were made
          if (results == null)
               return false;
          else
               return true;
     }
     
     
     /**
      * Merges titles, artists, composers, albums and genres whose word order is
      * scrambled and/or are subsets of each other. If the preferences are set
      * appropriately, then a scrambled word order report and/or a word subset
      * report are also generated.
      *
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private void performAllTokenizeOperations()
     throws Exception
     {
          // Prepare progress bar
          int number_sub_tasks = 0;
          int current_task = 0;
          if (preferences.report_probable_duplicates || preferences.report_wrongly_differing_titles)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_artists)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_composers)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_albums)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_genres)
               number_sub_tasks++;
          if (progress_bar != null)
               progress_bar.startNewSubTask(number_sub_tasks, "Performing reordered word subset tests");
          
          // Prepare the report streams (note that may be null based on
          // preferences)
          DataOutputStream scrambled_report_stream = getScrambledWordOrderReportStream();
          DataOutputStream subset_report_stream = getWordSubsetReportStream();
          
          // Perform operations
          if (preferences.report_probable_duplicates || preferences.report_wrongly_differing_titles)
          {
               performOneTokenizeOperation(titles, scrambled_report_stream, subset_report_stream, "titles");
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          if (preferences.report_wrongly_differing_artists)
          {
               performOneTokenizeOperation(artists, scrambled_report_stream, subset_report_stream, "artists");
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          if (preferences.report_wrongly_differing_composers)
          {
               performOneTokenizeOperation(composers, scrambled_report_stream, subset_report_stream, "composers");
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          if (preferences.report_wrongly_differing_albums)
          {
               performOneTokenizeOperation(albums, scrambled_report_stream, subset_report_stream, "albums");
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          if (preferences.report_wrongly_differing_genres)
          {
               performOneTokenizeOperation(genres, scrambled_report_stream, subset_report_stream, "genres");
               current_task++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(current_task);
          }
          
          // Close report streams
          if (scrambled_report_stream != null)
               HTMLWriter.endHTMLFile(scrambled_report_stream, true);
          if (subset_report_stream != null)
               HTMLWriter.endHTMLFile(subset_report_stream, true);
     }
     
     
     /**
      * Searches for pairs of fields in the given Entries which have a
      * a sufficiently matching number of fields, as set by the
      * preferences.word_ordering_fraction_match and
      * preferences.word_subset_fraction_match fields. Merges those entries
      * using the non-subset and/or subset evaluations, as set by the
      * preferences. If the scrambled_word_order_report_stream
      * or word_subset_report_stream streams are non-null, then reports are
      * written to them as well.
      *
      * @param  to_tokenize                            The Entries object to
      *                                                perform merges on.
      * @param  scrambled_word_order_report_stream     A stream to write a
      *                                                report to. No such report
      *                                                is written if this
      *                                                parameter is null.
      * @param  word_subset_report_stream              A stream to write a
      *                                                report to. No such report
      *                                                is written if this
      *                                                parameter is null.
      * @throws Exception                              An Exception is thrown if
      *                                                a problem occurs.
      */
     private void performOneTokenizeOperation(Entries to_tokenize,
          DataOutputStream scrambled_word_order_report_stream,
          DataOutputStream word_subset_report_stream, String key)
          throws Exception
     {
          // Perform the word ordering merges, and also generate a report, if
          // specified by the preferences
          if (preferences.check_word_ordering)
          {
               String[] non_subset_merge_results = to_tokenize.mergeEntriesIgnoringWordOrder(preferences.word_ordering_fraction_match, true, false, true);
               if (scrambled_word_order_report_stream != null)
               {
                    if (!key.equals("titles"))
                         HTMLWriter.addHorizontalRule(scrambled_word_order_report_stream);
                    if (non_subset_merge_results == null)
                         HTMLWriter.addParagraph("<h2>No " + key + " found with a sufficient number of matching words.</h2>", scrambled_word_order_report_stream);
                    else
                    {
                         HTMLWriter.addParagraph("<h2>Scrambled " + key + ":</h2>", scrambled_word_order_report_stream);
                         HTMLWriter.addList(non_subset_merge_results, true, scrambled_word_order_report_stream);
                    }
               }
          }
          
          // Perform the word subset merges, and also generate a report, if
          // specified by the preferences
          if (preferences.check_word_subset)
          {
               String[] subset_merge_results = to_tokenize.mergeEntriesIgnoringWordOrder(preferences.word_subset_fraction_match, true, true, true);
               if (word_subset_report_stream != null)
               {
                    if (!key.equals("titles"))
                         HTMLWriter.addHorizontalRule(word_subset_report_stream);
                    if (subset_merge_results == null)
                         HTMLWriter.addParagraph("<h2>No " + key + " found with a sufficient number of matching words.</h2>", word_subset_report_stream);
                    else
                    {
                         HTMLWriter.addParagraph("<h2>Part of " + key + " subset:</h2>", word_subset_report_stream);
                         HTMLWriter.addList(subset_merge_results, true, word_subset_report_stream);
                    }
               }
          }
     }
     
     
     /**
      * Returns a stream with initialized content to report the results of
      * the ordering word processing. Returns null if the preferences preclude
      * the generation of such a report.
      *
      * @return               Either an initialized stream or null.
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private DataOutputStream getScrambledWordOrderReportStream()
     throws Exception
     {
          if (preferences.report_word_ordering_tests && preferences.check_word_ordering)
          {
               DataOutputStream report_stream = HTMLWriter.addFrame("Fields with scrambled word orderings", subdirectory, contents_stream);
               HTMLWriter.addParagraph("<i>This report lists all title, artist, composer, album and genre fields that contain similar words, but in any order, and where sometimes the words in one field are a subset of the words in the other. For example, occurence of both \"Duke Ellington\" and \"Ellington Duke\" might be reported here. In order to be listed here, two field values must have at least " + (int) (100.0 * preferences.word_ordering_fraction_match) + "% identical words (the words may occur in any order). The percentage is calculated based on the field with the higher number of words, and is calculated after all selected find/replace operations have been performed (except the removal of spaces), but before any edit distance calculations have been performed.</i>", report_stream);
               HTMLWriter.addParagraph("<i>Only one sample value is listed for each cluster of results, and it reflects all selected processing performed up until the time that this report was generated. For example, field values of \"Jackson, Mahalia\" and \"Mahalia Jackson\" will result in a singe value of \"mahalia jackson\" reported here.</i>", report_stream);
               HTMLWriter.addParagraph("<i>Processing for titles, artists, composers, albums and genres are each only performed and reported if each corresponding Probable Errors report is set to be generated.</i>", report_stream);
               HTMLWriter.addHorizontalRule(report_stream);
               return report_stream;
          }
          else return null;
     }
     
     
     /**
      * Returns a stream with initialized content to report the results of
      * the word subset processing. Returns null if the preferences preclude
      * the generation of such a report.
      *
      * @return               Either an initialized stream or null.
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private DataOutputStream getWordSubsetReportStream()
     throws Exception
     {
          if (preferences.report_word_ordering_tests && preferences.check_word_subset)
          {
               DataOutputStream report_stream = HTMLWriter.addFrame("Fields whose words are subsets of another", subdirectory, contents_stream);
               HTMLWriter.addParagraph("<i>This report lists all title, artist, composer, album and genre fields that contain similar words, usually where the words in one are a subset of the words in the other, and where the words may be in any order. For example, occurence of both \"The Royal Philharmonic Orchestra\" and \"The Royal Philharmonic\" might be reported here. In order to be listed here, the two fields must have at least " + (int) (100.0 * preferences.word_subset_fraction_match) + "% of the same words (the words may occur in any order). The percentage is calculated based on the field with the lower number of words, and is calculated after all selected find/replace operations have been performed (except the removal of spaces), but before any edit distance calculations have been performed. Note that items that might have otherwise been listed here may be included in the Fields With Scrambled Word Orderings report instead if this report is also set to be generated.</i>", report_stream);
               HTMLWriter.addParagraph("<i>Only one sample value is listed for each cluster of results, and it reflects all selected processing performed up until the time that this report was generated. For example, field values of \"Duke Ellington and His Orchestra\" and \"Duke Ellington\" will result in a singe value of \"duke ellington\" reported here.</i>", report_stream);
               HTMLWriter.addParagraph("<i>Processing for titles, artists, composers, albums and genres are each only performed and reported if each corresponding Probable Errors report is set to be generated.</i>", report_stream);
               HTMLWriter.addHorizontalRule(report_stream);
               return report_stream;
          }
          else return null;
     }
     
     
     /**
      * Calculate the absolute, proportional and subset edit distances, as set
      * in the preferences, for the titles, artists, composers, albums and
      * genres, as set in the preferences.. Merge those pairs of Entry objects
      * that have at least one distance below the threshold  specified in the
      * preferences. Generate a report showing all calculated edit distances if
      * the appropriate preference is set.
      *
      * @throws Exception     Throws an exception if a problem occurs.
      */
     private void performEditDistanceCalculations()
     throws Exception
     {
          // Prepare the progress bar
          int number_sub_tasks = 0;
          int number_sub_tasks_complete = 0;
          if (preferences.report_probable_duplicates || preferences.report_wrongly_differing_titles)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_artists)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_composers)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_albums)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_genres)
               number_sub_tasks++;
          if (progress_bar != null)
               progress_bar.startNewSubTask(number_sub_tasks, "Performing edit distance calculations");
          
          // Prepare to report distances
          DataOutputStream report_stream = null;
          if (preferences.report_edit_distances)
          {
               report_stream = HTMLWriter.addFrame("Edit distances", subdirectory, contents_stream);
               HTMLWriter.addParagraph("<i>This report lists the edit distances between all pairs of values in each of the title, artist, composer, album and genre fields. One table is generated for each type of field. Each cell in a table contains up to three values, depending on the preferences selected by the user. These are (in the following order): absolute edit distance, proportional edit distance and subset edit distance. Absolute edit distance is the basic Levenshtein distance (see below). Proportional edit distance is the absolute edit distance divided by the length of the longer string, expressed as a percentage. Subset edit distance is the absolute edit distance minus the difference in lengths of the strings, all divided by the length of the shorter string and expressed as a percentage.</i>", report_stream);
               HTMLWriter.addParagraph("<i>Distances in bold on the table fall below the thresholds set by the user in the preferences. Entries consisting of a hyphen indicate one or both of the field values are unknown, so the edit distance cannot be calculated.</i>", report_stream);
               HTMLWriter.addParagraph("<i>All edit distance calculations are performed on fields after any selected find/replace and/or word subset reordering operations have been performed and any resulting merging of fields has occurred. Processing for titles, artists, composers, albums and genres are each only performed and reported if each corresponding Probable Errors report is set to be generated.</i>", report_stream);
               HTMLWriter.addParagraph("<i>The edit distance (also called Levenshtein Distance) between two strings is defined as the minimum number of operations needed to transform one of the strings into the other, where a single operation consists of an insertion, deletion or substitution of a single character. All three types of operations are assigned equal weights in this case.</i>", report_stream);
               HTMLWriter.addHorizontalRule(report_stream);
          }
          
          // Calculate edit distances and thresholds
          if (preferences.report_probable_duplicates || preferences.report_wrongly_differing_titles)
          {
               processAnEditDistanceSet(titles, report_stream, "title");
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
          if (preferences.report_wrongly_differing_artists)
          {
               processAnEditDistanceSet(artists, report_stream, "artist");
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
          if (preferences.report_wrongly_differing_composers)
          {
               processAnEditDistanceSet(composers, report_stream, "composer");
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
          if (preferences.report_wrongly_differing_albums)
          {
               processAnEditDistanceSet(albums, report_stream, "album");
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
          if (preferences.report_wrongly_differing_genres)
          {
               processAnEditDistanceSet(genres, report_stream, "genre");
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
          
          // End the report
          if (preferences.report_edit_distances)
               HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Take the given Entries object and calculate the absolute, proportional
      * and subset edit distances, as set in the preferences. Merge those pairs
      * of Entry objects that have at least one distance below the threshold
      * specified in the preferences. Generate a report showing all calculated
      * edit distances if the appropriate preference is set.
      *
      * @param  entries       The Entries object holding the strings to compare.
      * @param  report_stream The stream to write the table to.
      * @param  name          The field name corresponding to the strings.
      * @throws Exception     Throws an exception if a problem occurs.
      */
     private void processAnEditDistanceSet(Entries entries,
          DataOutputStream report_stream, String name)
          throws Exception
     {
          // Get the strings to compare
          String[] to_compare = entries.getValues();
          int number_strings = to_compare.length;
          
          // Perform edit distance calculations
          int[][] absolute_distances = mckay.utilities.staticlibraries.StringMethods.calculateLevenshteinDistances(to_compare);
          
          // Apply absolute threshold
          boolean[][] aboslute_close_enough = null;
          if (preferences.calculate_absolute_ED)
          {
               aboslute_close_enough = new boolean[number_strings][number_strings];
               for (int i = 0; i < number_strings; i++)
               {
                    for (int j = 0; j <= i; j++)
                    {
                         if (absolute_distances[i][j] == -1)
                              aboslute_close_enough[i][j] = false;
                         else if(absolute_distances[i][j] <= preferences.absolute_ED_threshold)
                              aboslute_close_enough[i][j] = true;
                         else aboslute_close_enough[i][j] = false;
                         
                         aboslute_close_enough[j][i] = aboslute_close_enough[i][j];
                    }
               }
          }
          
          // Apply proportional threshold
          int[][] proportional_percentages = null;
          boolean[][] proportional_close_enough = null;
          if (preferences.calculate_proportional_ED)
          {
               proportional_percentages = new int[number_strings][number_strings];
               proportional_close_enough = new boolean[number_strings][number_strings];
               for (int i = 0; i < number_strings; i++)
               {
                    for (int j = 0; j <= i; j++)
                    {
                         if (absolute_distances[i][j] == -1)
                         {
                              proportional_percentages[i][j] = -1;
                              proportional_close_enough[i][j] = false;
                         }
                         else
                         {
                              int longer_length = mckay.utilities.staticlibraries.StringMethods.getLengthOfLongerString(to_compare[i], to_compare[j]);
                              if (longer_length == -1)
                                   proportional_percentages[i][j] = -1;
                              else proportional_percentages[i][j] = 100 * absolute_distances[i][j] / longer_length;
                              
                              if(proportional_percentages[i][j] <= preferences.proportional_ED_threshold)
                                   proportional_close_enough[i][j] = true;
                              else proportional_close_enough[i][j] = false;
                         }
                         
                         proportional_percentages[j][i] = proportional_percentages[i][j];
                         proportional_close_enough[j][i] = proportional_close_enough[i][j];
                    }
               }
          }
          
          // Apply subset threshold
          int[][] subset_percentages = null;
          boolean[][] subset_close_enough = null;
          if (preferences.calculate_subset_ED)
          {
               subset_percentages = new int[number_strings][number_strings];
               subset_close_enough = new boolean[number_strings][number_strings];
               for (int i = 0; i < number_strings; i++)
               {
                    for (int j = 0; j <= i; j++)
                    {
                         if (absolute_distances[i][j] == -1)
                         {
                              subset_percentages[i][j] = -1;
                              subset_close_enough[i][j] = false;
                         }
                         else
                         {
                              int shorter_length = mckay.utilities.staticlibraries.StringMethods.getLengthOfShorterString(to_compare[i], to_compare[j]);
                              int longer_length = mckay.utilities.staticlibraries.StringMethods.getLengthOfLongerString(to_compare[i], to_compare[j]);
                              int length_difference = longer_length - shorter_length;
                              subset_percentages[i][j] = 100 * (absolute_distances[i][j] - length_difference) / shorter_length;
                              
                              if(subset_percentages[i][j] <= preferences.subset_ED_threshold)
                                   subset_close_enough[i][j] = true;
                              else subset_close_enough[i][j] = false;
                         }
                         
                         subset_percentages[j][i] = subset_percentages[i][j];
                         subset_close_enough[j][i] = subset_close_enough[i][j];
                    }
               }
          }
          
          // Report distances
          if (preferences.report_edit_distances)
          {
               // Generate the column headings
               String[] column_headings = new String[number_strings + 1];
               column_headings[0] = "";
               for (int i = 1; i < column_headings.length; i++)
               {
                    if (to_compare[i-1] == null)
                         column_headings[i] = "UNKNOWN";
                    else column_headings[i] = to_compare[i-1];
               }
               
               // Generate the rest of the table
               String[][] table = new String[number_strings][number_strings + 1];
               for (int i = 0; i < table.length; i++)
               {
                    // Fill in the row headings
                    if (to_compare[i] == null)
                         table[i][0] = "UNKNOWN";
                    else table[i][0] = to_compare[i];
                    
                    // Fill in the entries
                    for (int j = 1; j < table[i].length; j++)
                    {
                         String table_entry = "";
                         
                         if (preferences.calculate_absolute_ED)
                         {
                              String this_part = null;
                              if (absolute_distances[i][j-1] == -1)
                                   this_part = "-";
                              else this_part = String.valueOf(absolute_distances[i][j-1]);
                              
                              if (aboslute_close_enough[i][j-1])
                                   table_entry += "<b>" + this_part + "</b>";
                              else table_entry += this_part;
                         }
                         
                         if (preferences.calculate_proportional_ED)
                         {
                              String this_part = "";
                              if (preferences.calculate_absolute_ED)
                                   this_part = "<br>";
                              if (proportional_percentages[i][j-1] == -1)
                                   this_part += "-";
                              else this_part += String.valueOf(proportional_percentages[i][j-1]) + "%";
                              
                              if (proportional_close_enough[i][j-1])
                                   table_entry += "<b>" + this_part + "</b>";
                              else table_entry += this_part;
                         }
                         
                         if (preferences.calculate_subset_ED)
                         {
                              String this_part = "";
                              if (preferences.calculate_absolute_ED || preferences.calculate_proportional_ED)
                                   this_part = "<br>";
                              if (subset_percentages[i][j-1] == -1)
                                   this_part += "-";
                              else this_part += String.valueOf(subset_percentages[i][j-1]) + "%";
                              
                              if (subset_close_enough[i][j-1])
                                   table_entry += "<b>" + this_part + "</b>";
                              else table_entry += this_part;
                         }
                         
                         table[i][j] = table_entry;
                    }
               }
               
               // Write the table
               HTMLWriter.addParagraph("<h2>Edit distances for the " + name + " field:</h2>", report_stream);
               HTMLWriter.addTable(table, column_headings, report_stream);
               table = null;
               if (!name.equals("genre")) HTMLWriter.addHorizontalRule(report_stream);
          }
          
          // Perform merge
          Vector<boolean[][]> ones_to_merge = new Vector<boolean[][]>();
          Vector<String> merge_reasons = new Vector<String>();
          if (preferences.calculate_absolute_ED)
          {
               ones_to_merge.add(aboslute_close_enough);
               merge_reasons.add("Absolute edit distance");
          }
          if (preferences.calculate_proportional_ED)
          {
               ones_to_merge.add(proportional_close_enough);
               merge_reasons.add("Proportional edit distance");
          }
          if (preferences.calculate_subset_ED)
          {
               ones_to_merge.add(subset_close_enough);
               merge_reasons.add("Subset edit distance");
          }
          boolean[][][] ones_to_merge_array = ones_to_merge.toArray(new boolean[1][1][1]);
          String[] merge_reasons_array = merge_reasons.toArray(new String[1]);
          entries.mergeSpecifiedEntries(ones_to_merge_array, merge_reasons_array, true);
     }
     
     
     /**
      * Generate a separate report listing likely duplicates of the same
      * recording and/or probable errors in title fields.
      *
      * <p>A summary of the errors found is also added to the summary report.
      *
      * @param  summary_stream     A stream to write overall totals to.
      * @throws Exception          An Exception is thrown if a problem occurs
      *                            while writing the report file
      */
     private void reportProbableErrorsInvolvingTitles(DataOutputStream summary_stream)
     throws Exception
     {
          // Prepare the progress bar
          int number_sub_tasks = 0;
          int number_sub_tasks_complete = 0;
          if (preferences.report_probable_duplicates)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_titles)
               number_sub_tasks++;
          if (progress_bar != null)
               progress_bar.startNewSubTask(number_sub_tasks, "Reporting metadata related to titles likely to be erroneous");
          
          // Generate the reports
          if (preferences.report_probable_duplicates)
          {
               reportProbableDuplicateRecordings(summary_stream);
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
          if (preferences.report_wrongly_differing_titles)
          {
               reportProbableErrorsIntTitles(summary_stream);
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
     }
     
     
     /**
      * Finds recordings likely to be duplicates of each other. If selected,
      * writes the Filtered Candidate Duplicate Recordings and Probable
      * Duplicates of the Same Recording reports, and updates the summary
      * report.
      *
      * @param  summary_stream A stream to write overall totals to.
      * @throws Exception      An Exception is thrown if a problem occurs while
      *                        writing the report file.
      */
     private void reportProbableDuplicateRecordings(DataOutputStream summary_stream)
     throws Exception
     {
          // Assemble a list of all recordings with similar post-processing titles
          Entries duplicate_titles = titles.getMultipleEntries();
          duplicate_titles.sortEntries();
          int[][] indexes = duplicate_titles.getIndexes();
          
          // Filter probable duplicate recordings based on selected requirements
          Vector<int[]> accepted_duplicates = new Vector<int[]>();
          Vector<int[]> rejected_duplicates = new Vector<int[]>();
          Vector<int[]> accepted_while_others_rejected = new Vector<int[]>();
          int total_number_duplicates = 0;
          long duration_percent = (long) preferences.duration_filter_percent_max;
          for (int i = 0; i < indexes.length; i++)
          {
               // Prepare table detailing which recordings are the same in this
               // cluster
               boolean[][][] same = new boolean[5][indexes[i].length][indexes[i].length];
               
               // Filter recordings by duration
               if (preferences.filter_duplicates_by_duration)
               {
                    for (int j = 0; j < indexes[i].length; j++)
                    {
                         for (int k = 0; k <= j; k++)
                         {
                              if (recordings[indexes[i][j]].duration > recordings[indexes[i][k]].duration)
                              {
                                   long min = recordings[indexes[i][j]].duration - (duration_percent * recordings[indexes[i][j]].duration / (long) 100);
                                   long max = recordings[indexes[i][j]].duration + (duration_percent * recordings[indexes[i][j]].duration / (long) 100);
                                   if(recordings[indexes[i][k]].duration < min || recordings[indexes[i][k]].duration > max)
                                        same[0][j][k] = false;
                                   else same[0][j][k] = true;
                              }
                              else
                              {
                                   long min = recordings[indexes[i][k]].duration - (duration_percent * recordings[indexes[i][k]].duration / (long) 100);
                                   long max = recordings[indexes[i][k]].duration + (duration_percent * recordings[indexes[i][k]].duration / (long) 100);
                                   if(recordings[indexes[i][j]].duration < min || recordings[indexes[i][j]].duration > max)
                                        same[0][j][k] = false;
                                   else same[0][j][k] = true;
                              }
                              same[0][k][j] = same[0][j][k];
                         }
                    }
               }
               
               // Filter recordings by artist
               if (preferences.filter_duplicates_by_artist)
               {
                    for (int j = 0; j < indexes[i].length; j++)
                    {
                         String this_one = recordings[indexes[i][j]].artist;
                         for (int k = 0; k <= j; k++)
                         {
                              if (this_one == null || recordings[indexes[i][k]].artist == null)
                              {
                                   if (this_one == null && recordings[indexes[i][k]].artist == null)
                                        same[1][j][k] = true;
                                   else same[1][j][k] = false;
                              }
                              else if (this_one.equals(recordings[indexes[i][k]].artist))
                                   same[1][j][k] = true;
                              else same[1][j][k] = false;
                              same[1][k][j] = same[1][j][k];
                         }
                    }
               }
               
               // Filter recordings by composer
               if (preferences.filter_duplicates_by_composer)
               {
                    for (int j = 0; j < indexes[i].length; j++)
                    {
                         String this_one = recordings[indexes[i][j]].composer;
                         for (int k = 0; k <= j; k++)
                         {
                              if (this_one == null || recordings[indexes[i][k]].composer == null)
                              {
                                   if (this_one == null && recordings[indexes[i][k]].composer == null)
                                        same[2][j][k] = true;
                                   else same[2][j][k] = false;
                              }
                              else if (this_one.equals(recordings[indexes[i][k]].composer))
                                   same[2][j][k] = true;
                              else same[2][j][k] = false;
                              same[2][k][j] = same[2][j][k];
                         }
                    }
               }
               
               // Filter recordings by genre
               if (preferences.filter_duplicates_by_genre)
               {
                    for (int j = 0; j < indexes[i].length; j++)
                    {
                         String[] first_one = recordings[indexes[i][j]].genres;
                         for (int k = 0; k <= j; k++)
                         {
                              String[] second_one = recordings[indexes[i][k]].genres;
                              if (first_one == null || second_one == null)
                              {
                                   if (first_one == null && second_one == null)
                                        same[3][j][k] = true;
                                   else same[3][j][k] = false;
                              }
                              else
                              {
                                   boolean shared = false;
                                   for (int m = 0; m < first_one.length; m++)
                                        for (int n = 0; n < second_one.length; n++)
                                             if (first_one[m].equals(second_one[n]))
                                             {
                                        shared = true;
                                        m = first_one.length;
                                        n = second_one.length;
                                             }
                                   if (shared) same[3][j][k] = true;
                                   else same[3][j][k] = false;
                              }
                              same[3][k][j] = same[3][j][k];
                         }
                    }
               }
               
               // Filter recordings by album
               if (preferences.filter_duplicates_by_album)
               {
                    for (int j = 0; j < indexes[i].length; j++)
                    {
                         String this_one = recordings[indexes[i][j]].album;
                         for (int k = 0; k <= j; k++)
                         {
                              if (this_one == null || recordings[indexes[i][k]].album == null)
                              {
                                   if (this_one == null && recordings[indexes[i][k]].album == null)
                                        same[4][j][k] = true;
                                   else same[4][j][k] = false;
                              }
                              else if (this_one.equals(recordings[indexes[i][k]].album))
                                   same[4][j][k] = true;
                              else same[4][j][k] = false;
                              same[4][k][j] = same[4][j][k];
                         }
                    }
               }
               
               // Combine results
               Vector<int[]> these_accepted_duplicates = new Vector<int[]>();
               boolean[] has_been_added_to_an_accepted_group = new boolean[indexes[i].length];
               for (int m = 0; m < has_been_added_to_an_accepted_group.length; m++)
                    has_been_added_to_an_accepted_group[m] = false;
               for (int j = 0; j < indexes[i].length; j++)
               {
                    // Note which have been filtered out
                    boolean[] accepted = new boolean[indexes[i].length];
                    for (int m = 0; m < accepted.length; m++)
                         accepted[m] = true;
                    for (int k = 0; k < indexes[i].length; k++)
                    {
                         if (preferences.filter_duplicates_by_duration && !same[0][j][k])
                              accepted[k] = false;
                         else if (preferences.filter_duplicates_by_artist && !same[1][j][k])
                              accepted[k] = false;
                         else if (preferences.filter_duplicates_by_composer && !same[2][j][k])
                              accepted[k] = false;
                         else if (preferences.filter_duplicates_by_genre && !same[3][j][k])
                              accepted[k] = false;
                         else if (preferences.filter_duplicates_by_album && same[4][j][k])
                              accepted[k] = false;
                    }
                    
                    // Note how many remain after filtering
                    int number_accepted = 0;
                    for (int k = 0; k < accepted.length; k++)
                         if (accepted[k] && !has_been_added_to_an_accepted_group[k])
                              number_accepted++;
                    
                    // Accept a group if appropriate
                    if (number_accepted > 1) // 1 because always equals itself
                    {
                         int[] an_accepted_group = new int[number_accepted];
                         int current = 0;
                         for (int k = 0; k < accepted.length; k++)
                              if (accepted[k] && !has_been_added_to_an_accepted_group[k])
                              {
                              an_accepted_group[current] = indexes[i][k];
                              has_been_added_to_an_accepted_group[k] = true;
                              current++;
                              }
                         these_accepted_duplicates.add(an_accepted_group);
                    }
                    
                    // Note that done if all added
                    if (number_accepted == accepted.length) j = indexes[i].length;
               }
               
               // Note rejects (recordings not added to any group)
               Vector<Integer> these_rejected_duplicates = new Vector<Integer>();
               Vector<Integer> these_accepted_while_others_rejected = new Vector<Integer>();
               for (int j = 0; j < has_been_added_to_an_accepted_group.length; j++)
                    if (!has_been_added_to_an_accepted_group[j])
                         these_rejected_duplicates.add(new Integer(indexes[i][j]));
               if (these_rejected_duplicates.size() != 0)
                    for (int j = 0; j < has_been_added_to_an_accepted_group.length; j++)
                         if (has_been_added_to_an_accepted_group[j])
                              these_accepted_while_others_rejected.add(new Integer(indexes[i][j]));
               
               // Store processing results for this cluster
               if (these_accepted_duplicates.size() != 0)
                    for (int j = 0; j < these_accepted_duplicates.size(); j++)
                    {
                    total_number_duplicates += these_accepted_duplicates.get(j).length;
                    accepted_duplicates.add(these_accepted_duplicates.get(j));
                    }
               if (these_rejected_duplicates.size() != 0)
               {
                    int[] int_rejected = new int[these_rejected_duplicates.size()];
                    for (int j = 0; j < int_rejected.length; j++)
                         int_rejected[j] = these_rejected_duplicates.get(j).intValue();
                    rejected_duplicates.add(int_rejected);
                    
                    if (these_accepted_while_others_rejected.size() == 0)
                         accepted_while_others_rejected.add(null);
                    else
                    {
                         int[] int_awor = new int[these_accepted_while_others_rejected.size()];
                         for (int j = 0; j < int_awor.length; j++)
                              int_awor[j] = these_accepted_while_others_rejected.get(j).intValue();
                         accepted_while_others_rejected.add(int_awor);
                    }
               }
          }
          
          // Generate the filtered candidate duplicate recordings report
          if (preferences.list_titles_rejected_as_equivalent)
          {
               // Prepare the report
               DataOutputStream rejections_stream = HTMLWriter.addFrame("Filtered candidate duplicate recordings", subdirectory, contents_stream);
               HTMLWriter.addParagraph("<i>This report lists all recordings that were found to have similar titles, but were rejected as being duplicates of other recordings because of the selected filters (similar duration, identical artist, identical composer, one or more identical genres and/or non-identical album). The similarity of titles was assessed after all selected find/replace, word subset reordering and/or edit distance operations were performed, but no such operations were applied to any other fields for the purposes of this report.</i>", rejections_stream);
               HTMLWriter.addParagraph("<i>Each cluster of similar titles where one or more candidate duplicates were filtered out is listed, and each such listing includes either one or two tables. The first table, which is always present, lists recordings that were not found to be duplicates of any other recordings in the cluster. The second table lists all recordings in the cluster, if any, that were in fact found to be duplicates of one or more other recordings in the cluster.</i>", rejections_stream);
               HTMLWriter.addHorizontalRule(rejections_stream);
               
               // Write the report
               if (rejected_duplicates.size() == 0)
                    HTMLWriter.addParagraph("<h2>No recordings were found with similar titles that were rejected as potential duplicates of the same recording.</h2>", rejections_stream);
               else
               {
                    for (int i = 0; i < rejected_duplicates.size(); i++)
                    {
                         HTMLWriter.addParagraph("<h2>Cluster " + (i + 1) + ":</h2>", rejections_stream);
                         
                         int[] rejects = rejected_duplicates.get(i);
                         String[][] rejected_table = new String[rejects.length][];
                         String[][] rejected_table_column_headings = new String[1][];
                         for (int j = 0; j < rejected_table.length; j++)
                              rejected_table[j] = recordings[rejects[j]].getPartialFormattedFieldContents(j+1, rejected_table_column_headings);
                         HTMLWriter.addParagraph("<h3>These recordings were found not be duplicates of any others in their cluster:</h3>", rejections_stream);
                         HTMLWriter.addTable(rejected_table, rejected_table_column_headings[0], rejections_stream);
                         
                         int[] accepts = accepted_while_others_rejected.get(i);
                         if (accepts != null)
                         {
                              String[][] accept_table = new String[accepts.length][];
                              String[][] accept_table_column_headings = new String[1][];
                              for (int j = 0; j < accept_table.length; j++)
                                   accept_table[j] = recordings[accepts[j]].getPartialFormattedFieldContents(j+1, accept_table_column_headings);
                              HTMLWriter.addParagraph("<h3>These recordings were in the same cluster, and were found to be duplicates of each other (in one or more sub-clusters):</h3>", rejections_stream);
                              HTMLWriter.addTable(accept_table, accept_table_column_headings[0], rejections_stream);
                         }
                         
                         if (i != rejected_duplicates.size() - 1)
                              HTMLWriter.addHorizontalRule(rejections_stream);
                    }
               }
               HTMLWriter.endHTMLFile(rejections_stream, true);
          }
          
          // Prepare the probable duplicates report
          DataOutputStream duplicates_stream = HTMLWriter.addFrame("Probable duplicates of the same recording", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists recordings that are likely to be redundant duplicates of the same recording. First, all recordings whose title fields are judged to be similar after any selected find/replace, word subset reordering and edit distance processing are grouped together into clusters. Then the filters selected in the Duplicate Recording Detection Settings section of the Options Panel are applied to each such cluster. Specifically, recordings are filtered out of each cluster unless they have similar durations, have the same value for their artist fields, have the same value for their composer fields, share at least one genre and/or have different values for their album fields. Note that no processing is performed on artist, composer, genre or album fields as far as the generation of this report itself is concerned. Each cluster may be broken into further clusters during this processing, if appropriate. Each cluster with more than one surviving recording is then reported. It may be appropriate to delete some of these multiples for the sake of removing redundancy in the music collection.</i>", duplicates_stream);
          HTMLWriter.addParagraph("<i>Note that some false errors may be reported here, as the system is biased under the default settings towards catching all true errors. Large clusters of false errors sometimes form when processing extremely large collections. Such clusters may be ignored by the user, or he or she may wish to change the extraction parameters in the Options Panel.</i>", duplicates_stream);
          HTMLWriter.addHorizontalRule(duplicates_stream);
          if (accepted_duplicates.size() == 0)
               HTMLWriter.addParagraph("<h2>No recordings were found that are likely to be duplicates of each other.</h2>", duplicates_stream);
          else
          {
               for (int i = 0; i < accepted_duplicates.size(); i++)
               {
                    HTMLWriter.addParagraph("<h2>Cluster " + (i + 1) + ":</h2>", duplicates_stream);
                    
                    int[] accepts = accepted_duplicates.get(i);
                    String[][] accepted_table = new String[accepts.length][];
                    String[][] accepted_table_column_headings = new String[1][];
                    for (int j = 0; j < accepted_table.length; j++)
                         accepted_table[j] = recordings[accepts[j]].getPartialFormattedFieldContents(j+1, accepted_table_column_headings);
                    HTMLWriter.addTable(accepted_table, accepted_table_column_headings[0], duplicates_stream);
                    
                    if (i != accepted_duplicates.size() - 1)
                         HTMLWriter.addHorizontalRule(duplicates_stream);
               }
          }
          HTMLWriter.endHTMLFile(duplicates_stream, true);
          
          // Update the summary report
          if (accepted_duplicates.size() == 0)
               HTMLWriter.addParagraph("0 probable duplicate recordings were found", summary_stream);
          else HTMLWriter.addParagraph(accepted_duplicates.size() + " clusters of probable duplicate recordings were found, affecting a total of " + total_number_duplicates + " recordings", summary_stream);
     }
     
     
     /**
      * Analyzes the MergeReport objects stored in the titles Entries object
      * and generate a report detailing the clusters of differing title values
      * that shouuld likely in fact be the same. Cases where the ONLY reason
      * that a cluster of titles were merged is that they were identical are
      * not included here, since this report is only meant to report cases where
      * their are likely errors in titles that should otherwise be identical,
      * not to report likely duplicate recordings.
      *
      * <p>A summary of the errors found is also added to the summary report.
      *
      * @param  summary_stream A stream to write overall totals to.
      * @throws Exception      An Exception is thrown if a problem occurs while
      *                        writing the report file.
      */
     private void reportProbableErrorsIntTitles(DataOutputStream summary_stream)
     throws Exception
     {
          // Prepare the report
          DataOutputStream report_stream = HTMLWriter.addFrame("Probable errors in title field", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists probable errors in the title field of the input music database's metadata. Each cluster listed below indicates a set of several differing yet similar values for the title field that are found in the music database. The values found in each cluster should likely, in reality, be the same. These possible errors should be reviewed and, if appropriate, the metadata should be corrected accordingly. Some clusters may have one or more entries that should not in fact be the same as the others. This is a result of processing that generally errs on the side of reporting false errors rather than missing true errors.</i>", report_stream);
          HTMLWriter.addParagraph("<i>Some of the possible transformations that could make them the same are also listed. Only a subset of the necessary find/replace operations may be listed. Additional metadata for each recording in each cluster is also listed.</i>", report_stream);
          HTMLWriter.addParagraph("<i>Probable errors reported here were all discovered as a result of the find/replace, word subset reordering and/or edit distance processing that was selected and performed.</i>", report_stream);
          HTMLWriter.addParagraph("<i>Note that some false errors may be reported here, as the system is biased under the default settings towards catching all true errors. Large clusters of false errors sometimes form when processing extremely large collections. Such clusters may be ignored by the user, or he or she may wish to change the extraction parameters in the Options Panel.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          String[] column_headings = {"Fields that should likely be the same", "Some operation(s) needed to achieve equality"};
          
          // Collect the merge reports that indicate probable errors
          MergeReport[] merge_reports = titles.getMergeReport();
          
          if (merge_reports != null)
          {
               // Merge the reports that all apply to a single likely value
               // for the field and remove redundant listings of reasons
               merge_reports = MergeReport.mergeSubsetsIntoSupersets(merge_reports);
               MergeReport.removeDuplicateMergeKeys(merge_reports);
               
               // Filter out any cases where the entries are all identical
               Vector<MergeReport> list_of_valid_merge_reports = new Vector<MergeReport>();
               for (int i = 0; i < merge_reports.length; i++)
               {
                    int[] indexes = merge_reports[i].getMergeIndexes();
                    boolean not_identical = false;
                    String title = recordings[indexes[0]].title;
                    for (int j = 1; j < indexes.length; j++)
                    {
                         if(!recordings[indexes[j]].title.equals(title))
                         {
                              not_identical = true;
                              j = indexes.length;
                         }
                    }
                    if (not_identical)
                         list_of_valid_merge_reports.add(merge_reports[i]);
               }
               if (list_of_valid_merge_reports.size() == 0)
                    merge_reports = null;
               else
                    merge_reports = list_of_valid_merge_reports.toArray(new MergeReport[1]);
          }
          
          // Indicate if no errors were discovered
          if (merge_reports == null)
          {
               HTMLWriter.addParagraph("<h2>No probable errors in the title field values were detected.</h2>", report_stream);
               HTMLWriter.addParagraph("0 probable errors were found in the title field", summary_stream);
          }
          
          // Report probable errors
          else
          {
               // The total number of recordings affected
               int total_recordings_affected = 0;
               
               // Report each probable error cluster one by one
               for (int i = 0; i < merge_reports.length; i++)
               {
                    // Collect each value that should likely be made identical
                    int[] these_recording_indexes = merge_reports[i].getMergeIndexes();
                    String[] unique_values = new String[these_recording_indexes.length];
                    for (int j = 0; j < unique_values.length; j++)
                         unique_values[j] = recordings[these_recording_indexes[j]].title;
                    
                    // Update the number of recordings affected
                    total_recordings_affected += unique_values.length;
                    
                    // Remove identical values for the field so that all those
                    // that are reported in this cluster are unique. Note that
                    // individual entries can still be accessed from the
                    // these_recording_indexes variable if desired.
                    unique_values = mckay.utilities.staticlibraries.StringMethods.removeDoubles(unique_values);
                    
                    // Prepare data structure to hold an error cluster report
                    String[][] equivalency_report = new String[1][2];
                    equivalency_report[0][0] = new String("");
                    equivalency_report[0][1] = new String("");
                    for (int j = 0; j < unique_values.length; j++)
                         equivalency_report[0][0] += (j + 1) + ") " + unique_values[j] + "<br>";
                    String[] these_recording_keys = merge_reports[i].getMergeKeys();
                    for (int j = 0; j < these_recording_keys.length; j++)
                         equivalency_report[0][1] += these_recording_keys[j] +"<br>";
                    
                    // Report this error cluster
                    HTMLWriter.addParagraph("<h2>Cluster " + (i + 1) + ":</h2>", report_stream);
                    HTMLWriter.addTable(equivalency_report, column_headings, report_stream);
                    
                    // List all recordings that likely belong to this cluster
                    String[][] table_to_report = new String[these_recording_indexes.length][];
                    String[][] sub_table_column_headings = new String[1][];
                    for (int j = 0; j < these_recording_indexes.length; j++)
                         table_to_report[j] = recordings[these_recording_indexes[j]].getPartialFormattedFieldContents(j+1, sub_table_column_headings);
                    HTMLWriter.addParagraph("<h3>All recordings with these titles:</h3>", report_stream);
                    HTMLWriter.addTable(table_to_report, sub_table_column_headings[0], report_stream);
                    
                    // End the cluster
                    if (i != merge_reports.length - 1)
                         HTMLWriter.addHorizontalRule(report_stream);
               }
               
               // Add information to the overall summary report
               HTMLWriter.addParagraph(merge_reports.length + " clusters of probable errors found in the title field, affecting a total of " + total_recordings_affected + " recordings", summary_stream);
          }
          
          // Finalize the report
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Generate a separate report for each of the artists, composers,
      * albums and genres fields. Each such report analyzes the MergeReport
      * objects stored in the appropriate one of these Entries and details the
      * clusters of differing field values that shouuld likely in fact be the
      * same.
      *
      * <p>A summary of the errors found is also added to the summary report.
      *
      * @param  summary_stream     A stream to write overall totals to.
      * @throws Exception          An Exception is thrown if a problem occurs
      *                            while writing the report file
      */
     private void reportProbableErrorsInMetadata(DataOutputStream summary_stream)
     throws Exception
     {
          // Prepare the progress bar
          int number_sub_tasks = 0;
          int number_sub_tasks_complete = 0;
          if (preferences.report_wrongly_differing_artists)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_composers)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_albums)
               number_sub_tasks++;
          if (preferences.report_wrongly_differing_genres)
               number_sub_tasks++;
          if (progress_bar != null)
               progress_bar.startNewSubTask(number_sub_tasks, "Reporting metadata likely to be erroneous");
          
          // Generate the reports one by one
          if (preferences.report_wrongly_differing_artists)
          {
               reportProbableErrorsInATypeOfMetadata(artists, "artist", summary_stream);
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
          if (preferences.report_wrongly_differing_composers)
          {
               reportProbableErrorsInATypeOfMetadata(composers, "composer", summary_stream);
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
          if (preferences.report_wrongly_differing_albums)
          {
               reportProbableErrorsInATypeOfMetadata(albums, "album", summary_stream);
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
          if (preferences.report_wrongly_differing_genres)
          {
               reportProbableErrorsInATypeOfMetadata(genres, "genre", summary_stream);
               number_sub_tasks_complete++;
               if (progress_bar != null)
                    progress_bar.setSubTaskProgressValue(number_sub_tasks_complete);
          }
     }
     
     
     /**
      * Analyzes the MergeReport objects stored in the given Entries object
      * and generate a report detailing the clusters of differing field values
      * that shouuld likely in fact be the same.
      *
      * <p>A summary of the errors found is also added to the summary report.
      *
      * @param  to_analyze     The Entries object whose MergeReport objects
      *                        are to be analyzed.
      * @param  field_name     A code denoting the particular type of metadata
      *                        being analyzed. Must be one of the following:
      *                        "artist", "composer", "genre" or "album".
      * @param  summary_stream A stream to write overall totals to.
      * @throws Exception      An Exception is thrown if a problem occurs while
      *                        writing the report file
      */
     private void reportProbableErrorsInATypeOfMetadata(Entries to_analyze,
          String field_name, DataOutputStream summary_stream)
          throws Exception
     {
          // Prepare the report
          DataOutputStream report_stream = HTMLWriter.addFrame("Probable errors in " + field_name + " field", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report lists probable errors in the " + field_name + " field of the input music database's metadata. Each table listed below indicates a cluster of several differing yet similar values for the " + field_name + " field that are found in the music database. The values found in each cluster should likely, in reality, be the same. These possible errors should be reviewed and, if appropriate, the metadata should be corrected accordingly. Some clusters may have one or more entries that should not in fact be the same as the others. This is a result of processing that generally errs on the side of reporting false errors rather than missing true errors.</i>", report_stream);
          if (field_name.equals("genre"))
               HTMLWriter.addParagraph("<i>Some of the reported clusters may include some genres that may in fact not be part of the cluster. This is due to a minor processing anomaly when there are multiple genres.</i>", report_stream);
          HTMLWriter.addParagraph("<i>Some of the possible transformations that could make them the same are also listed. Only a subset of the necessary find/replace operations may be listed.</i>", report_stream);
          HTMLWriter.addParagraph("<i>Probable errors reported here were all discovered as a result of the find/replace, word subset reordering and/or edit distance processing that was selected and performed.</i>", report_stream);
          HTMLWriter.addParagraph("<i>Note that some false errors may be reported here, as the system is biased under the default settings towards catching all true errors. Large clusters of false errors sometimes form when processing extremely large collections. Such clusters may be ignored by the user, or he or she may wish to change the extraction parameters in the Options Panel.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          String[] column_headings = {"Fields that should likely be the same", "Some operation(s) needed to achieve equality"};
          
          // Collect the merge reports that indicate probable errors
          MergeReport[] merge_reports = to_analyze.getMergeReport();
          
          // Indicate if no errors were discovered
          if (merge_reports == null)
          {
               HTMLWriter.addParagraph("<h2>No probable errors in the " + field_name + " field values were detected.</h2>", report_stream);
               HTMLWriter.addParagraph("0 probable errors were found in the " + field_name + " field", summary_stream);
          }
          
          // Report probable errors
          else
          {
               // Merge the reports that all apply to a single likely value
               // for the field and remove redundant merge reasons
               merge_reports = MergeReport.mergeSubsetsIntoSupersets(merge_reports);
               MergeReport.removeDuplicateMergeKeys(merge_reports);
               
               // The total number of recordings affected
               int total_recordings_affected = 0;
               
               // Report each probable error cluster one by one
               for (int i = 0; i < merge_reports.length; i++)
               {
                    // Collect each value that should likely be made identical
                    int[] these_recording_indexes = merge_reports[i].getMergeIndexes();
                    String[] unique_values = new String[these_recording_indexes.length];
                    if (field_name.equals("artist"))
                    {
                         for (int j = 0; j < unique_values.length; j++)
                              unique_values[j] = recordings[these_recording_indexes[j]].artist;
                    }
                    else if (field_name.equals("composer"))
                    {
                         for (int j = 0; j < unique_values.length; j++)
                              unique_values[j] = recordings[these_recording_indexes[j]].composer;
                    }
                    else if (field_name.equals("genre"))
                    {
                         Vector<String> temp = new Vector<String>();
                         for (int j = 0; j < unique_values.length; j++)
                              for (int k = 0; k < recordings[these_recording_indexes[j]].genres.length; k++)
                                   temp.add(recordings[these_recording_indexes[j]].genres[k]);
                         unique_values = temp.toArray(new String[1]);
                    }
                    else if (field_name.equals("album"))
                    {
                         for (int j = 0; j < unique_values.length; j++)
                              unique_values[j] = recordings[these_recording_indexes[j]].album;
                    }
                    
                    // Update the number of recordings affected
                    total_recordings_affected += unique_values.length;
                    
                    // Remove identical values for the field so that all those
                    // that are reported in this cluster are unique. Note that
                    // individual entries can still be accessed from the
                    // these_recording_indexes variable if desired.
                    unique_values = mckay.utilities.staticlibraries.StringMethods.removeDoubles(unique_values);
                    
                    // Prepare data structure to hold an error cluster report
                    String[][] equivalency_report = new String[1][2];
                    equivalency_report[0][0] = new String("");
                    equivalency_report[0][1] = new String("");
                    for (int j = 0; j < unique_values.length; j++)
                         equivalency_report[0][0] += (j + 1) + ") " + unique_values[j] + "<br>";
                    String[] these_recording_keys = merge_reports[i].getMergeKeys();
                    for (int j = 0; j < these_recording_keys.length; j++)
                         equivalency_report[0][1] += these_recording_keys[j] +"<br>";
                    
                    // Report this error cluster
                    HTMLWriter.addParagraph("<h2>Cluster " + (i + 1) + ":</h2>", report_stream);
                    HTMLWriter.addTable(equivalency_report, column_headings, report_stream);
                    
                    // End the cluster
                    if (i != merge_reports.length - 1)
                         HTMLWriter.addHorizontalRule(report_stream);
               }
               
               // Add information to the overall summary report
               HTMLWriter.addParagraph(merge_reports.length + " clusters of probable errors found in the " + field_name + " field, affecting a total of " + total_recordings_affected + " recordings", summary_stream);
          }
          
          // Finalize the report
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Writes a report of all options selected by the user and stored in the
      * preferences field.
      *
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private void reportOptionsSelected()
     throws Exception
     {
          // Initializing
          if (progress_bar != null)
               progress_bar.startNewSubTask(1, "Reporting selected options");
          DataOutputStream report_stream = HTMLWriter.addFrame("Options selected", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report indicates the preferences set by the user in the Options Panel. These preferences determined which reports were generated and controlled the parameters of the processing that affected the contents of the reports generated.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // To store informations for each section
          Vector<String> reports = new Vector<String>();
          String current = new String();
          
          // Report on input and output data
          HTMLWriter.addParagraph("<h2>I/O SETTINGS</h2>", report_stream);
          current += "<b>iTunes File Path:</b> ";
          if (preferences.iTunes_file == null) current += "None used<br>";
          else current += preferences.iTunes_file + "<br>";
          current += "<b>MP3 Root Directory:</b> ";
          if (preferences.mp3_directory == null) current += "None used<br>";
          else current += preferences.mp3_directory + "<br>";
          current += "<b>Saved Report Path:</b> " + preferences.save_file.getAbsoluteFile();
          HTMLWriter.addParagraph(current, report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Report processing that occurred
          reports = new Vector<String>();
          HTMLWriter.addParagraph("<h2>PROCESSING SETTINGS</h2>", report_stream);
          HTMLWriter.addParagraph("<b>Find and Replace Operations</b>", report_stream);
          if (preferences.perform_find_replace_operations)
          {
               if (preferences.ignore_case_in_edit_distances)
                    reports.add("Ignored difference between uppder and lower case");
               if (preferences.remove_numbers_at_title_beginnings)
                    reports.add("Removed numbers and spaces at beginnings of titles");
               if (preferences.convert_ings)
                    reports.add("Converted all incidences of \"in'\" \to \"ing\"");
               if (preferences.convert_title_abbreviations)
                    reports.add("Converted all incidences of \"Mister \" to \"Mr. \", \"Doctor \" to \"Dr. \" and \"Professor \" to \"Prof. \"");
               if (preferences.remove_periods)
                    reports.add("Removed all periods");
               if (preferences.remove_commas)
                    reports.add("Removed all commas");
               if (preferences.remove_hyphens)
                    reports.add("Removed all hyphens");
               if (preferences.remove_colons)
                    reports.add("Removed all colons");
               if (preferences.remove_semicolons)
                    reports.add("Removed all semiconols");
               if (preferences.remove_quotation_marks)
                    reports.add("Removed all quotation marks");
               if (preferences.remove_single_quotes)
                    reports.add("Removed all single quotes and apostrophes");
               if (preferences.remove_brackets)
                    reports.add("Removed all parentheses, square brackets and curly braces");
               if (preferences.convert_ands_to_ampersands)
                    reports.add("Converted all incidences of \" and \" to \" & \"");
               if (preferences.remove_thes)
                    reports.add("Removed all occurrences of \"the \"");
               if (preferences.remove_spaces)
                    reports.add("Removed all spaces");
          }
          if (reports.size() == 0)
               HTMLWriter.addParagraph("No find and replace operations performed", report_stream);
          else HTMLWriter.addList(reports.toArray(new String[1]), false, report_stream);
          reports = new Vector<String>();
          HTMLWriter.addParagraph("<b>Reordered Word Subset Operations</b>", report_stream);
          if (preferences.check_word_ordering)
               reports.add("Checked altered word ordering with a threshold of a minimum of " + (int) (100 * preferences.word_ordering_fraction_match) + "% matching words");
          if (preferences.check_word_subset)
               reports.add("Checked for reordered word subsets with a threshold of a minimum of " + (int) (100 * preferences.word_subset_fraction_match) + "% matching words");
          if (reports.size() == 0)
               HTMLWriter.addParagraph("No reordered word subset operations performed", report_stream);
          else HTMLWriter.addList(reports.toArray(new String[1]), false, report_stream);
          reports = new Vector<String>();
          HTMLWriter.addParagraph("<b>Edit Distance Operations</b>", report_stream);
          if (preferences.calculate_absolute_ED)
               reports.add("Used absolute edit distance with a maximum acceptable distance of " + preferences.absolute_ED_threshold);
          if (preferences.calculate_proportional_ED)
               reports.add("Used prportional edit distance with a maximum acceptable distance of " + preferences.proportional_ED_threshold + "%");
          if (preferences.calculate_subset_ED)
               reports.add("Used subset edit distance with a maximum acceptable distance of " + preferences.subset_ED_threshold + "%");
          if (reports.size() == 0)
               HTMLWriter.addParagraph("No edit distance operations performed", report_stream);
          else HTMLWriter.addList(reports.toArray(new String[1]), false, report_stream);
          reports = new Vector<String>();
          HTMLWriter.addParagraph("<b>Duplicate Recording Filters Used</b>", report_stream);
          if (preferences.report_probable_duplicates)
          {
               if (preferences.filter_duplicates_by_duration)
                    reports.add("Required similar duration (maximum difference of " + preferences.duration_filter_percent_max + "% allowed)");
               if (preferences.filter_duplicates_by_artist)
                    reports.add("Required identical artist");
               if (preferences.filter_duplicates_by_composer)
                    reports.add("Required identical composer");
               if (preferences.filter_duplicates_by_genre)
                    reports.add("Required one or more identical genres");
               if (preferences.filter_duplicates_by_album)
                    reports.add("Required identical album");
          }
          if (reports.size() == 0)
               HTMLWriter.addParagraph("No duplicate recording filters used", report_stream);
          else HTMLWriter.addList(reports.toArray(new String[1]), false, report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          
          // Indicate reports generated
          reports = new Vector<String>();
          HTMLWriter.addParagraph("<h2>REPORTS GENERATED</h2>", report_stream);
          HTMLWriter.addParagraph("<b>Reports Relating to iTunes/ID3 Parsing and Merging</b>", report_stream);
          if (preferences.list_mp3_files_could_not_parse && preferences.mp3_directory != null)
               reports.add("MP3s found that could not parse");
          if (preferences.list_all_non_mp3s_in_directory && preferences.mp3_directory != null)
               reports.add("Non-MP3 files found");
          if (preferences.list_all_recordings_found)
               reports.add("Recordings parsed (before merge)");
          if (preferences.list_files_in_XML_but_missing && preferences.iTunes_file != null)
               reports.add("Files in iTunes XML but not at specified path");
          if (preferences.iTunes_file != null && preferences.mp3_directory != null)
          {
               if (preferences.list_noncorresponding_recordings)
                    reports.add("Recordings in one source but not the other");
               if (preferences.list_noncorresponding_fields)
                    reports.add("Fields that do not correspond between sourses");
          }
          if (preferences.list_all_postmerge_metadata)
               reports.add("All post-iTunes and ID3 merge metadata");
          if (reports.size() == 0)
               HTMLWriter.addParagraph("No parsing or source merging reports generated", report_stream);
          else HTMLWriter.addList(reports.toArray(new String[1]), false, report_stream);
          reports = new Vector<String>();
          HTMLWriter.addParagraph("<b>General Profiles of Music Collection</b>", report_stream);
          if (preferences.list_sorted_recording_metadata)
               reports.add("All recordings post-sorting");
          if (preferences.report_artist_breakdown)
               reports.add("Artist breakdown");
          if (preferences.report_composer_breakdown)
               reports.add("Composer breakdown");
          if (preferences.report_genre_breakdown)
               reports.add("Genre breakdown");
          if (preferences.report_comment_breakdown)
               reports.add("Comment statistics");
          if (preferences.list_artists_by_genre)
               reports.add("Artists listed by genre");
          if (preferences.list_composers_by_genre)
               reports.add("Composers listed by genre");
          if (preferences.report_missing_metadata)
               reports.add("Recordings missing key metadata");
          if (preferences.list_artists_with_few_recordings)
               reports.add("Artists with fewer than " + preferences.cutoff_for_artists_few_recs + " recordings");
          if (preferences.list_composers_with_few_recordings)
               reports.add("Artists with fewer than " + preferences.cutoff_for_composers_few_recs + " recordings");
          if (preferences.report_identical_titles)
               reports.add("Exactly identical recording titles");
          if (reports.size() == 0)
               HTMLWriter.addParagraph("No general profile reports generated", report_stream);
          else HTMLWriter.addList(reports.toArray(new String[1]), false, report_stream);
          reports = new Vector<String>();
          HTMLWriter.addParagraph("<b>Profiles of Albums in Music Collection</b>", report_stream);
          if (preferences.list_albums_by_artist)
               reports.add("Albums listed by artist");
          if (preferences.list_albums_by_composer)
               reports.add("Albums listed by composer");
          if (preferences.list_incomplete_albums)
               reports.add("Incomplete albums (with albums containing less that " + preferences.incoplete_albums_threshold + "% of their total tracks bolded)");
          if (preferences.list_albums_with_duplicate_tracks)
               reports.add("Albums with duplicate or unknown track numbers");
          if (preferences.list_albums_missing_year)
               reports.add("Albums with unspecified year");
          if (preferences.report_on_compilation_albums)
               reports.add("Report on compilation albums");
          if (reports.size() == 0)
               HTMLWriter.addParagraph("No album profile reports generated", report_stream);
          else HTMLWriter.addList(reports.toArray(new String[1]), false, report_stream);
          reports = new Vector<String>();
          HTMLWriter.addParagraph("<b>Detailed Processing Results</b>", report_stream);
          if (preferences.report_differing_only_in_case && preferences.perform_find_replace_operations && preferences.ignore_case_in_edit_distances)
               reports.add("Fields differing only in case");
          if (preferences.report_all_find_replace_changes && preferences.perform_find_replace_operations)
               reports.add("Detailed replacements made");
          if (preferences.report_new_identicals_after_fr && preferences.perform_find_replace_operations)
               reports.add("Newly identical fields after find and replace");
          if (preferences.report_word_ordering_tests && preferences.check_word_ordering)
               reports.add("Fields with scrambled word orderings");
          if (preferences.report_word_ordering_tests && preferences.check_word_subset)
               reports.add("Fields whose words are subsets of another");
          if (preferences.report_edit_distances && (preferences.calculate_absolute_ED || preferences.calculate_proportional_ED || preferences.calculate_subset_ED))
               reports.add("Edit distances");
          if (preferences.list_titles_rejected_as_equivalent && preferences.report_probable_duplicates)
               reports.add("Filtered candidate duplicate recordings");
          if (reports.size() == 0)
               HTMLWriter.addParagraph("No detailed processing reports generated", report_stream);
          else HTMLWriter.addList(reports.toArray(new String[1]), false, report_stream);
          reports = new Vector<String>();
          HTMLWriter.addParagraph("<b>Summaries of Probable Errors in Metadata</b>", report_stream);
          if (preferences.report_probable_duplicates)
               reports.add("Probable duplicates of the same recording");
          if (preferences.report_wrongly_differing_titles)
               reports.add("Probable errors in title fields");
          if (preferences.report_wrongly_differing_artists)
               reports.add("Probable errors in artist fields");
          if (preferences.report_wrongly_differing_composers)
               reports.add("Probable errors in composer fields");
          if (preferences.report_wrongly_differing_albums)
               reports.add("Probable errors in album fields");
          if (preferences.report_wrongly_differing_genres)
               reports.add("Probable errors in genre fields");
          if (reports.size() == 0)
               HTMLWriter.addParagraph("No error summary reports generated", report_stream);
          else HTMLWriter.addList(reports.toArray(new String[1]), false, report_stream);
          reports = new Vector<String>();
          HTMLWriter.addParagraph("<b>Technical Reports</b>", report_stream);
          if (preferences.report_options_selected)
               reports.add("Options selected");
          if (preferences.report_processing_times)
               reports.add("Processing time log");
          if (reports.size() == 0)
               HTMLWriter.addParagraph("No technical reports generated", report_stream);
          else HTMLWriter.addList(reports.toArray(new String[1]), false, report_stream);
          
          // Finalizing
          if (progress_bar != null)
               progress_bar.setSubTaskProgressValue(1);
          HTMLWriter.endHTMLFile(report_stream, true);
     }
     
     
     /**
      * Writes a report file giving the logged processing times.
      *
      * @throws     Exception Throws an exception if a problem occurs.
      */
     private void reportProcessingTimes()
     throws Exception
     {
          String[] column_headings = {"Task", "Time (ms)"};
          String[][] time_logs = progress_bar.getProcessingTimeLogs(false);
          DataOutputStream report_stream = HTMLWriter.addFrame("Processing time log", subdirectory, contents_stream);
          HTMLWriter.addParagraph("<i>This report indicates how long each processing task took.</i>", report_stream);
          HTMLWriter.addHorizontalRule(report_stream);
          HTMLWriter.addTable(time_logs, column_headings, report_stream);
          HTMLWriter.endHTMLFile(report_stream, true);
     }
}
