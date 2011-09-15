/*
 * OptionsPanel.java
 * Version 1.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jMusicMetaManager.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import mckay.utilities.gui.progressbars.DoubleProgressBarDialog;
import jMusicMetaManager.*;


/**
 * A GUI panel allowing the user to select the parameters that will control
 * what data will be processed during the jMusicMetaManager run and what
 * analysis procedures will be used.
 *
 * <p>The top three text fields can be used to specify paths that will be used
 * during processing. The corresponding browse buttons allow the user to
 * select paths using a file chooser dialog box. Alternatively, paths may
 * be entered directly. The checkboxes control whether the corresponding fields
 * should be used during processing or not.
 *
 * <p>The iTunes XML file option allows the user to extract all available
 * relevant metadata from an iTunes XML file. The user must select an actual
 * file for this to work.
 *
 * <p>The MP3 directory option allows the user to select a directory that
 * contains MP3 files, either directly or in its subdirectories. The user must
 * therefore select a directory rather than a file when using this option.
 * The contents of this directory and its subdirectories are recursively
 * searched for MP3 files, and metadata is extracted from their ID3 tags.
 *
 * <p>Either or both of the iTunes XML or MP3 ID3 tag options may be used. The
 * iTunes option usually results in more reliable metadata, as available ID3
 * encoders and extractors can be buggy. If both options are used, the metadata
 * from both operations are used, and the metadata from the iTunes XML file is
 * used in the case of conflicting metadata.
 *
 * <p>The save path allows the user to specify a path to which a report will
 * be automatically saved after processing. This should correspond to an HTML
 * file. A directory will also be created with a similar file in order to
 * contain the linked HTML files.
 *
 * <p>The central part of this panel allows the user to specify what kinds of
 * operations to perform during processing.
 *
 * <p>The button at the bottom of the panel begins processing using the
 * metadata specified in the top two text boxes and the preferences specified
 * in the central window. A report is saved to the path given in the third
 * text box, and may also be viewed directly in the Report Panel once
 * processing is complete.
 *
 * <p>This class also includes a public method for exporting extracted metadata
 * to Weka ARFF or ACE XML files.
 *
 * @author Cory McKay
 */
public class OptionsPanel
     extends JPanel
     implements ActionListener
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The GUI element that holds this panel.
      */
     private OuterFrame       parent;
     
     /**
      * The button that brings up the dialog box to browse for an MP3 holding
      * directory.
      */
     private JButton          browse_mp3s_button;
     
     /**
      * The button that brings up the dialog box to browse for an iTunes XML
      * file.
      */
     private JButton          browse_iTunes_button;
     
     /**
      * The button that brings up the dialog box to browse for a save path.
      */
     private JButton          browse_save_path_button;
     
     /**
      * The text field that shows the currently selected path to an iTunes
      * XML file.
      */
     private JTextField       iTunes_path_field;
     
     /**
      * The text field that shows the currently selected path to a directory
      * holding MP3 files.
      */
     private JTextField       mp3_path_field;
     
     /**
      * The text field that shows the currently selected path to which a report
      * file should be saved.
      */
     private JTextField       save_path_field;
     
     /**
      * A checkbox controlling whether an iTunes XML file is to be used.
      */
     private JCheckBox        use_iTunes_checkbox;
     
     /**
      * A checkbox controlling whether ID3 tags of MP3s are to be used.
      */
     private JCheckBox        use_mp3_checkbox;
     
     /**
      * The button that begins processing.
      */
     private JButton          execute_button;
     
     /**
      * A dialog box allowing the user to select a directory holding MP3 files
      * to be selected.
      */
     private JFileChooser     mp3_browser;
     
     /**
      * A dialog box allowing the user to select an iTunes XML file.
      */
     private JFileChooser     iTunes_browser;
     
     /**
      * A dialog box allowing the user to select a path to which a report file
      * will be saved.
      */
     private JFileChooser     save_path_browser;
     
     /**
      * A gloabal control. The ignore_case_in_edit_distances,
      * convert_ands_to_ampersands, convert_ings, convert_title_abbreviations,
      * convert_title_abbreviations, remove_thes, remove_periods, remove_commas,
      * remove_hyphens and remove_spaces operations are only performed if this
      * is true.
      */
     private JCheckBox        perform_find_replace_operations_checkbox;
     
     /**
      * Whether or not edit distances are calculated with letters belonging to
      * different cases treated as identical.
      */
     private JCheckBox        ignore_case_in_edit_distances_checkbox;
     
     /**
      * Whether or not all numbers and spaces at the very beginning of recording
      * titles are removed.
      */
     private JCheckBox        remove_numbers_at_title_beginnings_checkbox;
     
     /**
      * Whether or not all incidences of "in'" are converted to "ing".
      */
     private JCheckBox        convert_ings_checkbox;
     
     /**
      * Whether or not the following conversions are made: "Mister " to "Mr. ",
      * "Doctor " to "Dr. " and "Professor " to "Prof. ".
      */
     private JCheckBox        convert_title_abbreviations_checkbox;
     
     /**
      * Whether or not all periods are removed.
      */
     private JCheckBox        remove_periods_checkbox;
     
     /**
      * Whether or not all commas are removed.
      */
     private JCheckBox        remove_commas_checkbox;
     
     /**
      * Whether or not all hyphens are removed.
      */
     private JCheckBox        remove_hyphens_checkbox;
     
     /**
      * Whether or not all colons are removed.
      */
     private JCheckBox        remove_colons_checkbox;
     
     /**
      * Whether or not all semicolons are removed.
      */
     private JCheckBox        remove_seimcolons_checkbox;
     
     /**
      * Whether or not all quotation marks are removed.
      */
     private JCheckBox        remove_quotation_marks_checkbox;
     
     /**
      * Whether or not all apostrophes and single quotes are removed.
      */
     private JCheckBox        remove_single_quotes_checkbox;
     
     /**
      * Whether or not all parentheses, square brackets and curly braces
      * are removed.
      */
     private JCheckBox        remove_brackets_checkbox;
     
     /**
      * Whether or not all incidences of " and " are converted to " & ".
      */
     private JCheckBox        convert_ands_to_ampersands_checkbox;
     
     /**
      * Whether or not all occurences of "the " are removed.
      */
     private JCheckBox        remove_thes_checkbox;
     
     /**
      * Whether or not all spaces are removed.
      */
     private JCheckBox        remove_spaces_checkbox;
     
     /**
      * Whether or not a check is performed to detect possible errors in fields
      * due to different word orderings for otherwise identical field values.
      */
     private JCheckBox        check_word_ordering_checkbox;
     
     /**
      * The minimum percent (1 to 100) of words that must match in a
      * check_word_ordering test in order for two fields to be reported as
      * intended to be identical.
      */
     private JTextField       word_ordering_percent_match_textfield;
     
     /**
      * Whether or not a check is performed to detect possible errors in fields
      * due to the words in one field's value being a subset of the words in
      * another field's value.
      */
     private JCheckBox        check_word_subset_checkbox;
     
     /**
      * The minimum percent (1 to 100) of words that must match in a
      * word_subset_fraction_match test in order for two fields to be
      * reported as intended to be identical.
      */
     private JTextField       word_subset_percent_match_textfield;
     
     /**
      * Whether or not the edit distance is calculated between pairs of
      * differing values found for a field, and a threshold based on the
      * absolute edit distance between each pair is used to determine if the
      * values should likely in fact be the same.
      */
     private JCheckBox        calculate_absolute_ED_checkbox;
     
     /**
      * The maximum absolute edit distance permissible for two strings to be
      * considered likely to be identical. Related to the
      * calculate_absolute_ED_checkbox field.
      */
     private JTextField       absolute_ED_threshold_textfield;
     
     /**
      * Whether or not the edit distance is calculated between pairs of
      * differing values found for a field, and a threshold based on the
      * absolute edit distance between each pair divided by the length of the
      * longer field is used to determine if the values should likely in fact be
      * the same.
      */
     private JCheckBox        calculate_proportional_ED_checkbox;
     
     /**
      * The maximum proportional edit distance permissible for two strings to be
      * considered likely to be identical (calculated by dividing the absolute
      * distance by the length of the longer field). Related to the
      * calculate_proportional_ED_checkbox field.
      */
     private JTextField       proportional_ED_threshold_textfield;
     
     /**
      * Whether or not the edit distance is calculated between pairs of
      * differing values found for a field, and a threshold based on the
      * absolute edit distance between each pair minus the difference in lengths
      * of the fields, all divided by the length of the shorter field, is used
      * to determine if the values should likely in fact be the same.
      */
     private JCheckBox        calculate_subset_ED_checkbox;
     
     /**
      * The maximum subset edit distance permissible for two strings to be
      * considered likely to be identical (calculated by subtracting the
      * difference in length from the absolute edit distance of the two strings
      * and dividing the result by the length of the shorter field). Related to
      * the calculate_proportional_ED_checkbox calculate_subset_ED_checkbox.
      */
     private JTextField       subset_ED_threshold_textfield;
     
     /**
      * Whether or not the durations of pairs of recordings are considered when
      * determining whether they are duplicates of each other.
      */
     private JCheckBox        filter_duplicates_by_duration_checkbox;
     
     /**
      * The maximum percentage difference between the durations of two
      * recordings for them to be considered to be duplicates of each other.
      * Must be from 0 to 100.
      */
     private JTextField       duration_filter_percent_max_textfield;
     
     /**
      * Whether or not pairs of recordings must have the same values in their
      * artist fields for them to be considered to be duplicates of each other.
      */
     private JCheckBox        filter_duplicates_by_artist_checkbox;
     
     /**
      * Whether or not pairs of recordings must have the same values in their
      * composer fields for them to be considered to be duplicates of each
      * other.
      */
     private JCheckBox        filter_duplicates_by_composer_checkbox;
     
     /**
      * Whether or not pairs of recordings must have at least one identical
      * value in their genre fields for them to be considered to be duplicates
      * of each other.
      */
     private JCheckBox        filter_duplicates_by_genre_checkbox;
     
     /**
      * Whether or not pairs of recordings must have the same values in their
      * album fields for them to be considered to be duplicates of each other.
      */
     private JCheckBox        filter_duplicates_by_album_checkbox;
     
     /**
      * Whether or not a report is generated listing all MP3 files that were
      * found but could not be parsed, and why they could not be parsed.
      */
     private JCheckBox        list_mp3_files_could_not_parse_checkbox;
     
     /**
      * Whether or not the paths of all files found in the mp3_directory that
      * do not end with the .mp3 or .MP3 extension are reported.
      */
     private JCheckBox        list_all_non_mp3s_in_directory_checkbox;
     
     /**
      * Whether or not the path, title and artist of all recordings for which
      * metadata is extracted is reported. Two separate lists are produced, one
      * for the recordings extracted from ID3 tags and one for recordings
      * extracted from an iTunes XML file.
      */
     private JCheckBox        list_all_recordings_found_checkbox;
     
     /**
      * Whether or not the path of each recording parsed from an iTunes XML file
      * is checked, and a report is generated listing such files that are
      * missing.
      */
     private JCheckBox        list_files_in_XML_but_missing_checkbox;
     
     /**
      * Whether or not the recordings that are referenced in an iTunes XML file
      * but are not present on disk (and vice versa) are reported. This is only
      * meaningful if both an iTunes XML file and an MP3 directory are specified
      * by the user.
      */
     private JCheckBox        list_noncorresponding_recordings_checkbox;
     
     /**
      * Whether or not fields that differ between each MP3 file and its
      * corresponding iTunes XML entry are reported. This is only
      * meaningful if both an iTunes XML file and an MP3 directory are specified
      * by the user.
      */
     private JCheckBox        list_noncorresponding_fields_checkbox;
     
     /**
      * Whether or not all available metadata is reported for every recording
      * after the metadata from the iTunes XML file and MP3 ID3 tags have been
      * merged. This is generally used only for testing, as it is largely
      * redundant if the list_sorted_recording_metadata option is selected.
      */
     private JCheckBox        list_all_postmerge_metadata_checkbox;
     
     /**
      * Whether or not all available metadata is reported for every recording
      * after the recordings have been sorted by title.
      */
     private JCheckBox        list_sorted_recording_metadata_checkbox;
     
     /**
      * Whether or not a report is generated that names all unique artists and
      * statistics about their entries in the music database.
      */
     private JCheckBox        resport_artist_breakdown_checkbox;
     
     /**
      * Whether or not a report is generated that names all unique composers and
      * statistics about their entries in the music database.
      */
     private JCheckBox        resport_composer_breakdown_checkbox;
     
     /**
      * Whether or not a report is generated that names all unique genres and
      * statistics about their entries in the music database.
      */
     private JCheckBox        report_genre_breakdown_checkbox;
     
     /**
      * Whether or not a report is generated that names all unique comments and
      * statistics about their entries in the music database.
      */
     private JCheckBox        report_comment_breakdown_checkbox;
     
     /**
      * Whether or not a report is generated alphabetically listing each genre
      * present. All artists present that have at least one recording in a
      * given genre are listed under that genre's entry as well as the number
      * of recordings by the artist belonging to the given genre and the
      * percentage of the recordings in that genre that they represent.
      */
     private JCheckBox        list_artists_by_genre;
     
     /**
      * Whether or not a report is generated alphabetically listing each genre
      * present. All composers present that have at least one recording in a
      * given genre are listed under that genre's entry as well as the number
      * of recordings by the composer belonging to the given genre and the
      * percentage of the recordings in that genre that they represent.
      */
     private JCheckBox        list_composers_by_genre;
     
     /**
      * Whether or not all recordings should be explicity reported that have
      * empty title, artist, composer, album and/or genre fields. A separate
      * table is produced for each of these.
      */
     private JCheckBox        report_missing_metadata_checkbox;
     
     /**
      * Whether or not a list of artists with only a few recordings is
      * generated.
      */
     private JCheckBox        list_artists_with_few_recordings_checkbox;
     
     /**
      * The number of recordings cutoff to use for the list of artists with only
      * a few recordings.
      */
     private JTextField       cutoff_for_artists_few_recs_textfield;
     
     /**
      * Whether or not a list of composers with only a few recordings is
      * generated.
      */
     private JCheckBox        list_composers_with_few_recordings_checkbox;
     
     /**
      * The number of recordings cutoff to use for the list of composers with
      * only a few recordings.
      */
     private JTextField       cutoff_for_composers_few_recs_textfield;
     
     /**
      * Whether or not a list of all titles that are identical is generated.
      */
     private JCheckBox        report_identical_titles_checkbox;
     
     /**
      * Whether or not a list of titles, artists, composers, albums and genres
      * that start with spaces is generated.
      */
     private JCheckBox        report_starting_with_spaces_checkbox;
     
     /**
      * Whether or not a report is generated alphabetically listing each artist
      * present. All albums present that have at least one recording by a given
      * artist are listed under that artist's entry. Note is also made of
      * whether each album is a compilation, how many tracks in each album are
      * by the corresponding artist and how many tracks belonging to each album
      * are present.
      */
     private JCheckBox        list_albums_by_artist_checkbox;
     
     /**
      * Whether or not a report is generated alphabetically listing each
      * composer present. All albums present that have at least one recording by
      * a given composer are listed under that composer's entry. Note is also
      * made of whether each album is a compilation, how many tracks in each
      * album are by the corresponding composer and how many tracks belonging to
      * each album are present.
      */
     private JCheckBox        list_albums_by_composer_checkbox;
     
     /**
      * Whether or not a report is generated listing all albums that are missing
      * tracks, or that have an unknown number of tracks.
      */
     private JCheckBox        list_incomplete_albums_checkbox;
     
     /**
      * The percentage of tracks present in an album below which an album will
      * be listed as bold in the Incomplete Albums report.
      */
     private JTextField       incoplete_albums_threshold_textfield;
     
     /**
      * Whether or not a list is generated of all albums with multiple tracks
      * with the same number as well as of all albums containing one or
      * more recordings that do not have a track number specified.
      */
     private JCheckBox        list_albums_with_duplicate_tracks_checkbox;
     
     /**
      * Whether or not a list is generated of albums containing one or more
      * recordings that do not have year metadata specified.
      */
     private JCheckBox        list_albums_missing_year_checkbox;
     
     /**
      * Whether or not a list of all albums marked as compilations is generated,
      * as well as a list of albums that are not marked as compilations but
      * contain multiple artists and a list of albums that are marked as
      * compilations but contain only one artist.
      */
     private JCheckBox        report_on_compilation_albums_checkbox;
     
     /**
      * Whether or not to report entries where otherwise identical title,
      * artist, composer, genre and album fields differ in case.
      */
     private JCheckBox        report_differing_only_in_case_checkbox;
     
     /**
      * Whether or not a report is generated detailing every change made during
      * the find and replace operations.
      */
     private JCheckBox        report_all_find_replace_changes_checkbox;
     
     /**
      * Whether or not a report is generated describing all newly identical
      * entries after the find and replace operations have been performed.
      */
     private JCheckBox        report_new_identicals_after_fr_checkbox;
     
     /**
      * Whether or not a report is generated describing describing the results
      * of the word_ordering_fraction_match and word_subset_fraction_match
      * reports for artists, composers and genres.
      */
     private JCheckBox        report_word_ordering_tests_checkbox;
     
     /**
      * Whether or not a report is generated listing a table of edit distances
      * for each pair of values for each selected field.
      */
     private JCheckBox        report_edit_distances_checkbox;
     
     /**
      * Whether or not a report is generated listing all recordings that were
      * filtered out of the probable duplicates report.
      */
     private JCheckBox        list_titles_rejected_as_equivalent_checkbox;
     
     /**
      * Whether or not a report should be generated indicating recordings that
      * are likely multiple occurences of the same recording under the same
      * or different names.
      */
     private JCheckBox        report_probable_duplicates_checkbox;
     
     /**
      * Whether or not a report should be generated indicating all clusters of
      * title fields whose members differ from one another, but nonetheless
      * likely should, in fact, be identical.
      */
     private JCheckBox        report_wrongly_differing_titles_checkbox;
     
     /**
      * Whether or not a report should be generated indicating all clusters of
      * artist fields whose members differ from one another, but nonetheless
      * likely should, in fact, be identical.
      */
     private JCheckBox        report_wrongly_differing_artists_checkbox;
     
     /**
      * Whether or not a report should be generated indicating all clusters of
      * composer fields whose members differ from one another, but nonetheless
      * likely should, in fact, be identical.
      */
     private JCheckBox        report_wrongly_differing_composers_checkbox;
     
     /**
      * Whether or not a report should be generated indicating all clusters of
      * album fields whose members differ from one another, but nonetheless
      * likely should, in fact, be identical.
      */
     private JCheckBox        report_wrongly_differing_albums_checkbox;
     
     /**
      * Whether or not a report should be generated indicating all clusters of
      * genre fields whose members differ from one another, but nonetheless
      * likely should, in fact, be identical.
      */
     private JCheckBox        report_wrongly_differing_genres_checkbox;
     
     /**
      * Whether or not a list of all options selected for the run of processing
      * and reporting is generated.
      */
     private JCheckBox        report_options_selected_checkbox;
     
     /**
      * Whether or not the processing times needed for each portion of the
      * analysis should be reported.
      */
     private JCheckBox        report_processing_times_checkbox;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates and displays a new instance of PreferencesPanel.
      *
      * @param parent    The OuterFrame that this panel is part of.
      */
     public OptionsPanel(OuterFrame parent)
     {
          // Store reference to parent window
          this.parent = parent;
          
          // Initialize layout settings
          int horizontal_gap = 4;
          int vertical_gap = 4;
          setLayout(new BorderLayout(horizontal_gap, vertical_gap));
          
          // Set up the browse_button_panel
          JPanel browse_button_panel = new JPanel(new GridLayout(3, 1, horizontal_gap, vertical_gap));
          browse_iTunes_button = new JButton("Browse iTunes XML File:");
          browse_iTunes_button.addActionListener(this);
          browse_button_panel.add(browse_iTunes_button);
          browse_mp3s_button = new JButton("Browse MP3 Directory:");
          browse_mp3s_button.addActionListener(this);
          browse_button_panel.add(browse_mp3s_button);
          browse_save_path_button = new JButton("Browse Report Save Path:");
          browse_save_path_button.addActionListener(this);
          browse_button_panel.add(browse_save_path_button);
          
          // Set up the file_text_box_panel
          JPanel file_text_box_panel = new JPanel(new GridLayout(3, 1, horizontal_gap, vertical_gap));
          try
          {
               iTunes_path_field = new JTextField(new File(".").getCanonicalPath() + File.separator + "SampleFiles" + File.separator + "Tutorial_iTunes.xml");
               mp3_path_field = new JTextField(new File(".").getCanonicalPath() + File.separator + "SampleFiles");
               save_path_field = new JTextField(new File(".").getCanonicalPath() + File.separator + "WorkingDirectory" + File.separator + "anaylysis_output_01.html");
          }
          catch (Exception e)
          {;}
          file_text_box_panel.add(iTunes_path_field);
          file_text_box_panel.add(mp3_path_field);
          file_text_box_panel.add(save_path_field);
          
          // Set up the check_box_panel
          JPanel check_box_panel = new JPanel(new GridLayout(3, 1, horizontal_gap, vertical_gap));
          use_iTunes_checkbox = new JCheckBox("Use iTunes");
          use_iTunes_checkbox.addActionListener(this);
          check_box_panel.add(use_iTunes_checkbox);
          use_mp3_checkbox = new JCheckBox("Use MP3s");
          use_mp3_checkbox.addActionListener(this);
          check_box_panel.add(use_mp3_checkbox);
          
          // Set up file_selection_panel
          JPanel file_selection_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          file_selection_panel.add(browse_button_panel, BorderLayout.WEST);
          file_selection_panel.add(file_text_box_panel, BorderLayout.CENTER);
          file_selection_panel.add(check_box_panel, BorderLayout.EAST);
          
          // Set up preferences objects
          perform_find_replace_operations_checkbox = new JCheckBox("Perform find/replace operations");
          perform_find_replace_operations_checkbox.addActionListener(this);
          ignore_case_in_edit_distances_checkbox = new JCheckBox("Treat upper/lower case as identical");
          ignore_case_in_edit_distances_checkbox.addActionListener(this);
          remove_numbers_at_title_beginnings_checkbox = new JCheckBox("Remove numbers and spaces at beginning of titles");
          convert_ings_checkbox = new JCheckBox("Convert \"in'\" to \"ing\"");
          convert_title_abbreviations_checkbox = new JCheckBox("Convert personal titles to abbreviations");
          remove_periods_checkbox = new JCheckBox("Remove all periods");
          remove_commas_checkbox = new JCheckBox("Remove all commas");
          remove_hyphens_checkbox = new JCheckBox("Remove all hyphens");
          remove_colons_checkbox = new JCheckBox("Remove all colons");
          remove_seimcolons_checkbox = new JCheckBox("Remove all semicolons");
          remove_quotation_marks_checkbox = new JCheckBox("Remove all quotation marks");
          remove_single_quotes_checkbox = new JCheckBox("Remove all single quotes and apostrophes");
          remove_brackets_checkbox = new JCheckBox("Remove all brackets");
          convert_ands_to_ampersands_checkbox = new JCheckBox("Convert \" and \" to \" & \"");
          remove_thes_checkbox = new JCheckBox("Remove all occurrences  of \"the \"");
          remove_spaces_checkbox = new JCheckBox("Remove all spaces");
          check_word_ordering_checkbox = new JCheckBox("Check word ordering");
          word_ordering_percent_match_textfield = new JTextField(4);
          check_word_subset_checkbox = new JCheckBox("Check word subsets");
          word_subset_percent_match_textfield = new JTextField(4);
          calculate_absolute_ED_checkbox = new JCheckBox("Use absolute threshold");
          absolute_ED_threshold_textfield = new JTextField(4);
          calculate_proportional_ED_checkbox = new JCheckBox("Use proportional threshold");
          proportional_ED_threshold_textfield = new JTextField(4);
          calculate_subset_ED_checkbox = new JCheckBox("Use subset threshold");
          subset_ED_threshold_textfield = new JTextField(4);
          filter_duplicates_by_duration_checkbox = new JCheckBox("Filter by duration");
          duration_filter_percent_max_textfield = new JTextField(4);
          filter_duplicates_by_artist_checkbox = new JCheckBox("Filter by artist");
          filter_duplicates_by_composer_checkbox = new JCheckBox("Filter by composer");
          filter_duplicates_by_genre_checkbox = new JCheckBox("Filter by genre (at least one common)");
          filter_duplicates_by_album_checkbox = new JCheckBox("Filter by album");
          list_mp3_files_could_not_parse_checkbox = new JCheckBox("List MP3 files found that could not be parsed");
          list_all_non_mp3s_in_directory_checkbox = new JCheckBox("List all non-MP3 files found");
          list_all_recordings_found_checkbox = new JCheckBox("List all recordings parsed (before merge)");
          list_files_in_XML_but_missing_checkbox = new JCheckBox("List files in iTunes XML but not at specified path");
          list_noncorresponding_recordings_checkbox = new JCheckBox("List recordings in one source but not the other");
          list_noncorresponding_fields_checkbox = new JCheckBox("Report fields that do not correspond between sources");
          list_all_postmerge_metadata_checkbox = new JCheckBox("List all post-iTunes and ID3 merge metadata");
          list_sorted_recording_metadata_checkbox = new JCheckBox("List all recordings post-sorting");
          resport_artist_breakdown_checkbox = new JCheckBox("Report artist breakdown");
          resport_composer_breakdown_checkbox = new JCheckBox("Report composer breakdown");
          report_genre_breakdown_checkbox = new JCheckBox("Report genre breakdown");
          report_comment_breakdown_checkbox = new JCheckBox("Report comment statistics");
          list_artists_by_genre = new JCheckBox("List artists by genre");
          list_composers_by_genre = new JCheckBox("List composers by genre");
          report_missing_metadata_checkbox = new JCheckBox("Report recordings missing key metadata");
          list_artists_with_few_recordings_checkbox = new JCheckBox("List artists with few recordings");
          cutoff_for_artists_few_recs_textfield = new JTextField(4);
          list_composers_with_few_recordings_checkbox = new JCheckBox("List composers with few recordings");
          cutoff_for_composers_few_recs_textfield = new JTextField(4);
          report_identical_titles_checkbox = new JCheckBox("Report exactly identical recording titles");
          report_starting_with_spaces_checkbox = new JCheckBox("Report fields starting with a space");
          list_albums_by_artist_checkbox = new JCheckBox("List albums by artist");
          list_albums_by_composer_checkbox = new JCheckBox("List albums by composer");
          list_incomplete_albums_checkbox = new JCheckBox("List incomplete albums");
          incoplete_albums_threshold_textfield = new JTextField(4);
          list_albums_with_duplicate_tracks_checkbox = new JCheckBox("List albums with duplicate or unknown track numbers");
          list_albums_missing_year_checkbox = new JCheckBox("List albums with unspecified year");
          report_on_compilation_albums_checkbox = new JCheckBox("Report on compilation albums");
          report_differing_only_in_case_checkbox = new JCheckBox("Report fields differing only in case");
          report_all_find_replace_changes_checkbox = new JCheckBox("Report detailed replacements made");
          report_new_identicals_after_fr_checkbox = new JCheckBox("Report newly identical fields after find/replace");
          report_word_ordering_tests_checkbox = new JCheckBox("Report fields whose words are scrambled or subsets");
          report_edit_distances_checkbox = new JCheckBox("Report edit distance values");
          list_titles_rejected_as_equivalent_checkbox = new JCheckBox("List filtered candidate duplicate recordings");
          report_probable_duplicates_checkbox = new JCheckBox("Report probable duplicates of the same recording");
          report_probable_duplicates_checkbox.addActionListener(this);
          report_wrongly_differing_titles_checkbox = new JCheckBox("Report probable errors in title field");
          report_wrongly_differing_artists_checkbox = new JCheckBox("Report probable errors in artist field");
          report_wrongly_differing_composers_checkbox = new JCheckBox("Report probable errors in composer field");
          report_wrongly_differing_albums_checkbox = new JCheckBox("Report probable errors in album field");
          report_wrongly_differing_genres_checkbox = new JCheckBox("Report probable errors in genre field");
          report_options_selected_checkbox = new JCheckBox("Report options selected");
          report_processing_times_checkbox = new JCheckBox("Report processing times");
          
          // Set up default checkbox and text field selections
          restoreDefaults();
          
          // Set up left and right panels
          JPanel left_options_display = new JPanel(new GridLayout(52, 1, horizontal_gap, vertical_gap));
          JPanel right_options_display = new JPanel(new GridLayout(52, 1, horizontal_gap, vertical_gap));
          left_options_display.add(new ColouredJLabel("FIND/REPLACE SETTINGS:"));
          left_options_display.add(perform_find_replace_operations_checkbox);
          left_options_display.add(ignore_case_in_edit_distances_checkbox);
          left_options_display.add(remove_numbers_at_title_beginnings_checkbox);
          left_options_display.add(convert_ings_checkbox);
          left_options_display.add(convert_title_abbreviations_checkbox);
          left_options_display.add(remove_periods_checkbox);
          left_options_display.add(remove_commas_checkbox);
          left_options_display.add(remove_hyphens_checkbox);
          left_options_display.add(remove_colons_checkbox);
          left_options_display.add(remove_seimcolons_checkbox);
          left_options_display.add(remove_quotation_marks_checkbox);
          left_options_display.add(remove_single_quotes_checkbox);
          left_options_display.add(remove_brackets_checkbox);
          left_options_display.add(convert_ands_to_ampersands_checkbox);
          left_options_display.add(remove_thes_checkbox);
          left_options_display.add(remove_spaces_checkbox);
          left_options_display.add(new JLabel(""));
          left_options_display.add(new ColouredJLabel("REORDERED WORD SUBSET SETTINGS:"));
          left_options_display.add(getCheckboxTextfieldCombo(check_word_ordering_checkbox, "Min % Matches (1-100):", word_ordering_percent_match_textfield, horizontal_gap, vertical_gap));
          left_options_display.add(getCheckboxTextfieldCombo(check_word_subset_checkbox, "Min % Matches (1-100):", word_subset_percent_match_textfield, horizontal_gap, vertical_gap));
          left_options_display.add(new JLabel(""));
          left_options_display.add(new ColouredJLabel("EDIT DISTANCE SETTINGS:"));
          left_options_display.add(getCheckboxTextfieldCombo(calculate_absolute_ED_checkbox, "Max (1-75):", absolute_ED_threshold_textfield, horizontal_gap, vertical_gap));
          left_options_display.add(getCheckboxTextfieldCombo(calculate_proportional_ED_checkbox, "Max % (1-99):", proportional_ED_threshold_textfield, horizontal_gap, vertical_gap));
          left_options_display.add(getCheckboxTextfieldCombo(calculate_subset_ED_checkbox, "Max % (1-199):", subset_ED_threshold_textfield, horizontal_gap, vertical_gap));
          left_options_display.add(new JLabel(""));
          left_options_display.add(new ColouredJLabel("DUPLICATE RECORDING DETECTION SETTINGS:"));
          left_options_display.add(getCheckboxTextfieldCombo(filter_duplicates_by_duration_checkbox, "Max % Difference (0-100):", duration_filter_percent_max_textfield, horizontal_gap, vertical_gap));
          left_options_display.add(filter_duplicates_by_artist_checkbox);
          left_options_display.add(filter_duplicates_by_composer_checkbox);
          left_options_display.add(filter_duplicates_by_genre_checkbox);
          left_options_display.add(filter_duplicates_by_album_checkbox);
          right_options_display.add(new ColouredJLabel("REPORTS RELATING TO ITUNES/ID3 PARSING AND MERGING:"));
          right_options_display.add(list_mp3_files_could_not_parse_checkbox);
          right_options_display.add(list_all_non_mp3s_in_directory_checkbox);
          right_options_display.add(list_all_recordings_found_checkbox);
          right_options_display.add(list_files_in_XML_but_missing_checkbox);
          right_options_display.add(list_noncorresponding_recordings_checkbox);
          right_options_display.add(list_noncorresponding_fields_checkbox);
          right_options_display.add(list_all_postmerge_metadata_checkbox);
          right_options_display.add(new JLabel(""));
          right_options_display.add(new ColouredJLabel("GENERAL PROFILES OF MUSIC COLLECTION:"));
          right_options_display.add(list_sorted_recording_metadata_checkbox);
          right_options_display.add(resport_artist_breakdown_checkbox);
          right_options_display.add(resport_composer_breakdown_checkbox);
          right_options_display.add(report_genre_breakdown_checkbox);
          right_options_display.add(report_comment_breakdown_checkbox);
          right_options_display.add(list_artists_by_genre);
          right_options_display.add(list_composers_by_genre);
          right_options_display.add(report_missing_metadata_checkbox);
          right_options_display.add(getCheckboxTextfieldCombo(list_artists_with_few_recordings_checkbox, "Cutoff (2-90):", cutoff_for_artists_few_recs_textfield, horizontal_gap, vertical_gap));
          right_options_display.add(getCheckboxTextfieldCombo(list_composers_with_few_recordings_checkbox, "Cutoff (2-90):", cutoff_for_composers_few_recs_textfield, horizontal_gap, vertical_gap));
          right_options_display.add(new JLabel(""));
          right_options_display.add(new ColouredJLabel("MISCELLANEOUS REPORTS:"));
          right_options_display.add(report_identical_titles_checkbox);
          right_options_display.add(report_starting_with_spaces_checkbox);
          right_options_display.add(new JLabel(""));
          right_options_display.add(new ColouredJLabel("PROFILES OF ALBUMS IN MUSIC COLLECTION:"));
          right_options_display.add(list_albums_by_artist_checkbox);
          right_options_display.add(list_albums_by_composer_checkbox);
          right_options_display.add(getCheckboxTextfieldCombo(list_incomplete_albums_checkbox, "% Tracks Present (1-100):", incoplete_albums_threshold_textfield, horizontal_gap, vertical_gap));
          right_options_display.add(list_albums_with_duplicate_tracks_checkbox);
          right_options_display.add(list_albums_missing_year_checkbox);
          right_options_display.add(report_on_compilation_albums_checkbox);
          right_options_display.add(new JLabel(""));
          right_options_display.add(new ColouredJLabel("REPORTS DESCRIBING DETAILED PROCESSING RESULTS:"));
          right_options_display.add(report_differing_only_in_case_checkbox);
          right_options_display.add(report_all_find_replace_changes_checkbox);
          right_options_display.add(report_new_identicals_after_fr_checkbox);
          right_options_display.add(report_word_ordering_tests_checkbox);
          right_options_display.add(report_edit_distances_checkbox);
          right_options_display.add(list_titles_rejected_as_equivalent_checkbox);
          right_options_display.add(new JLabel(""));
          right_options_display.add(new ColouredJLabel("SUMMARIES OF PROBABLE ERRORS IN METADATA:"));
          right_options_display.add(report_probable_duplicates_checkbox);
          right_options_display.add(report_wrongly_differing_titles_checkbox);
          right_options_display.add(report_wrongly_differing_artists_checkbox);
          right_options_display.add(report_wrongly_differing_composers_checkbox);
          right_options_display.add(report_wrongly_differing_albums_checkbox);
          right_options_display.add(report_wrongly_differing_genres_checkbox);
          right_options_display.add(new JLabel(""));
          right_options_display.add(new ColouredJLabel("TECHNICAL REPORTS:"));
          right_options_display.add(report_options_selected_checkbox);
          right_options_display.add(report_processing_times_checkbox);
          
          // Set up the combined options display
          JPanel options_display = new JPanel(new GridLayout(1, 2, horizontal_gap, vertical_gap));
          JScrollPane options_display_scroll_pane = new JScrollPane(options_display);
          options_display_scroll_pane.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
          JPanel left_options_display_outer = new JPanel();
          left_options_display_outer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
          left_options_display.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
          left_options_display_outer.add(left_options_display);
          JPanel right_options_display_outer = new JPanel();
          right_options_display_outer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
          right_options_display.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
          right_options_display_outer.add(right_options_display);
          options_display.add(left_options_display_outer);
          options_display.add(right_options_display_outer);
          
          // Set up the execute_button
          execute_button = new JButton("BEGIN METADATA ANALYSIS");
          execute_button.addActionListener(this);
          
          // Add items to the main panel
          add(file_selection_panel, BorderLayout.NORTH);
          add(options_display_scroll_pane, BorderLayout.CENTER);
          add(execute_button, BorderLayout.SOUTH);
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event    The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the browse_iTunes_button
          if (event.getSource().equals(browse_iTunes_button))
               browse("iTunes XML");
          
          // React to the browse_mp3s_button
          else if (event.getSource().equals(browse_mp3s_button))
               browse("MP3 Directory");
          
          // React to the browse_save_path_button
          else if (event.getSource().equals(browse_save_path_button))
               browse("Save Path");
          
          // React to the execute_button
          else if (event.getSource().equals(execute_button))
               execute();
          
          // React to the use_iTunes_checkbox
          else if (event.getSource().equals(use_iTunes_checkbox))
               updateBasedOnCheckBoxes(use_iTunes_checkbox);
          
          // React to the use_mp3_checkbox
          else if (event.getSource().equals(use_mp3_checkbox))
               updateBasedOnCheckBoxes(use_mp3_checkbox);
          
          // React to the perform_find_replace_operations_checkbox checkbox
          else if (event.getSource().equals(perform_find_replace_operations_checkbox))
               updateBasedOnCheckBoxes(perform_find_replace_operations_checkbox);
          
          // React to the ignore_case_in_edit_distances_checkbox checkbox
          else if (event.getSource().equals(ignore_case_in_edit_distances_checkbox))
               updateBasedOnCheckBoxes(ignore_case_in_edit_distances_checkbox);
          
          // React to the check_word_ordering_checkbox checkbox
          else if (event.getSource().equals(check_word_ordering_checkbox))
               updateBasedOnCheckBoxes(check_word_ordering_checkbox);
          
          // React to the check_word_subset_checkbox checkbox
          else if (event.getSource().equals(check_word_subset_checkbox))
               updateBasedOnCheckBoxes(check_word_subset_checkbox);
          
          // React to the calculate_absolute_ED_checkbox checkbox
          else if (event.getSource().equals(calculate_absolute_ED_checkbox))
               updateBasedOnCheckBoxes(calculate_absolute_ED_checkbox);
          
          // React to the calculate_proportional_ED_checkbox checkbox
          else if (event.getSource().equals(calculate_proportional_ED_checkbox))
               updateBasedOnCheckBoxes(calculate_proportional_ED_checkbox);
          
          // React to the calculate_subset_ED_checkbox checkbox
          else if (event.getSource().equals(calculate_subset_ED_checkbox))
               updateBasedOnCheckBoxes(calculate_subset_ED_checkbox);
          
          // React to the filter_duplicates_by_duration_checkbox checkbox
          else if (event.getSource().equals(filter_duplicates_by_duration_checkbox))
               updateBasedOnCheckBoxes(filter_duplicates_by_duration_checkbox);
          
          // React to the list_artists_with_few_recordings_checkbox checkbox
          else if (event.getSource().equals(list_artists_with_few_recordings_checkbox))
               updateBasedOnCheckBoxes(list_artists_with_few_recordings_checkbox);
          
          // React to the list_composers_with_few_recordings_checkbox checkbox
          else if (event.getSource().equals(list_composers_with_few_recordings_checkbox))
               updateBasedOnCheckBoxes(list_composers_with_few_recordings_checkbox);
          
          // React to the list_incomplete_albums_checkbox checkbox
          else if (event.getSource().equals(list_incomplete_albums_checkbox))
               updateBasedOnCheckBoxes(list_incomplete_albums_checkbox);
          
          // React to the report_probable_duplicates_checkbox checkbox
          else if (event.getSource().equals(report_probable_duplicates_checkbox))
               updateBasedOnCheckBoxes(report_probable_duplicates_checkbox);
     }
     
     
     /**
      * Restores user preferences to startup defaults. The contents of the three
      * path text fields are not changed, however.
      */
     public void restoreDefaults()
     {
          // Set defaults
          use_iTunes_checkbox.setSelected(true);
          use_mp3_checkbox.setSelected(false);
          perform_find_replace_operations_checkbox.setSelected(true);
          ignore_case_in_edit_distances_checkbox.setSelected(true);
          remove_numbers_at_title_beginnings_checkbox.setSelected(true);
          convert_ings_checkbox.setSelected(true);
          convert_title_abbreviations_checkbox.setSelected(true);
          remove_periods_checkbox.setSelected(true);
          remove_commas_checkbox.setSelected(true);
          remove_hyphens_checkbox.setSelected(true);
          remove_colons_checkbox.setSelected(true);
          remove_seimcolons_checkbox.setSelected(true);
          remove_quotation_marks_checkbox.setSelected(true);
          remove_single_quotes_checkbox.setSelected(true);
          remove_brackets_checkbox.setSelected(true);
          convert_ands_to_ampersands_checkbox.setSelected(true);
          remove_thes_checkbox.setSelected(true);
          remove_spaces_checkbox.setSelected(true);
          check_word_ordering_checkbox.setSelected(true);
          word_ordering_percent_match_textfield.setText("70");
          check_word_subset_checkbox.setSelected(true);
          word_subset_percent_match_textfield.setText("80");
          calculate_absolute_ED_checkbox.setSelected(true);
          absolute_ED_threshold_textfield.setText("1");
          calculate_proportional_ED_checkbox.setSelected(true);
          proportional_ED_threshold_textfield.setText("20");
          calculate_subset_ED_checkbox.setSelected(false);
          subset_ED_threshold_textfield.setText("20");
          filter_duplicates_by_duration_checkbox.setSelected(true);
          duration_filter_percent_max_textfield.setText("5");
          filter_duplicates_by_artist_checkbox.setSelected(true);
          filter_duplicates_by_composer_checkbox.setSelected(false);
          filter_duplicates_by_genre_checkbox.setSelected(false);
          filter_duplicates_by_album_checkbox.setSelected(false);
          list_mp3_files_could_not_parse_checkbox.setSelected(true);
          list_all_non_mp3s_in_directory_checkbox.setSelected(true);
          list_all_recordings_found_checkbox.setSelected(false);
          list_files_in_XML_but_missing_checkbox.setSelected(true);
          list_noncorresponding_recordings_checkbox.setSelected(true);
          list_noncorresponding_fields_checkbox.setSelected(false);
          list_all_postmerge_metadata_checkbox.setSelected(false);
          list_sorted_recording_metadata_checkbox.setSelected(true);
          resport_artist_breakdown_checkbox.setSelected(true);
          resport_composer_breakdown_checkbox.setSelected(true);
          report_genre_breakdown_checkbox.setSelected(true);
          report_comment_breakdown_checkbox.setSelected(true);
          list_artists_by_genre.setSelected(true);
          list_composers_by_genre.setSelected(true);
          report_missing_metadata_checkbox.setSelected(true);
          list_artists_with_few_recordings_checkbox.setSelected(true);
          cutoff_for_artists_few_recs_textfield.setText("6");
          list_composers_with_few_recordings_checkbox.setSelected(true);
          cutoff_for_composers_few_recs_textfield.setText("6");
          report_identical_titles_checkbox.setSelected(true);
          report_starting_with_spaces_checkbox.setSelected(true);
          list_albums_by_artist_checkbox.setSelected(true);
          list_albums_by_composer_checkbox.setSelected(true);
          list_incomplete_albums_checkbox.setSelected(true);
          incoplete_albums_threshold_textfield.setText("66");
          list_albums_with_duplicate_tracks_checkbox.setSelected(true);
          list_albums_missing_year_checkbox.setSelected(true);
          report_on_compilation_albums_checkbox.setSelected(true);
          report_differing_only_in_case_checkbox.setSelected(false);
          report_all_find_replace_changes_checkbox.setSelected(false);
          report_new_identicals_after_fr_checkbox.setSelected(false);
          report_word_ordering_tests_checkbox.setSelected(false);
          report_edit_distances_checkbox.setSelected(false);
          list_titles_rejected_as_equivalent_checkbox.setSelected(false);
          report_probable_duplicates_checkbox.setSelected(true);
          report_wrongly_differing_titles_checkbox.setSelected(true);
          report_wrongly_differing_artists_checkbox.setSelected(true);
          report_wrongly_differing_composers_checkbox .setSelected(true);
          report_wrongly_differing_albums_checkbox.setSelected(true);
          report_wrongly_differing_genres_checkbox.setSelected(true);
          report_options_selected_checkbox.setSelected(true);
          report_processing_times_checkbox.setSelected(false);
          
          // Grey out appropriate check boxes
          updateBasedOnCheckBoxes(use_iTunes_checkbox);
          updateBasedOnCheckBoxes(use_mp3_checkbox);
          updateBasedOnCheckBoxes(perform_find_replace_operations_checkbox);
          updateBasedOnCheckBoxes(ignore_case_in_edit_distances_checkbox);
          updateBasedOnCheckBoxes(calculate_absolute_ED_checkbox);
          updateBasedOnCheckBoxes(check_word_ordering_checkbox);
          updateBasedOnCheckBoxes(check_word_subset_checkbox);
          updateBasedOnCheckBoxes(calculate_absolute_ED_checkbox);
          updateBasedOnCheckBoxes(calculate_proportional_ED_checkbox);
          updateBasedOnCheckBoxes(calculate_subset_ED_checkbox);
          updateBasedOnCheckBoxes(filter_duplicates_by_duration_checkbox);
          updateBasedOnCheckBoxes(list_artists_with_few_recordings_checkbox);
          updateBasedOnCheckBoxes(list_composers_with_few_recordings_checkbox);
          updateBasedOnCheckBoxes(list_incomplete_albums_checkbox);
          updateBasedOnCheckBoxes(report_probable_duplicates_checkbox);
     }
     
     
     /**
      * Load the HTML file detailed in the save field into the Report Panel
      * of the parent OuterFrame. The Report Panel report_panel is enbaled and
      * displayed.
      *
      * @param contents_path  The path to the report index file to which a
      *                       directory of reports created has been written.
      *                       This will be displayed in the left frame of the
      *                       report screen.
      * @param summary_path   The path to the summary report that will be
      *                       displayed by default in the right frame.
      */
     public void reportResults(String contents_path, String summary_path)
     {
          parent.loadReportIntoReportPanel(contents_path, summary_path);
     }
     
     
     /**
      * Extract metadata from the GUI specified iTunes XML and/or MP3 files
      * and export this information as a Weka ARFF or ACE XML file. No
      * processing is applied to the metadata other than possibly the merging of
      * metadata from the two possible sources if both are specified. An error
      * dialog box is displayed if a problem occurs.
      *
      * <p>The user is presented with a dialog box asking where the data
      * should be exported to.
      *
      * @param to_arff   Whether or not to export to a Weka ARFF file.
      * @param to_ace    Whether or not to export to an ACE XML file.
      */
     public void export(boolean to_arff, boolean to_ace)
     {
          // Disable the diplay panel
          parent.enableReportPanel(false);
          
          try
          {
               // Set the iTunes and MP3 source information based on their
               // respective user entries
               File iTunes_file = null;
               if (use_iTunes_checkbox.isSelected())
                    iTunes_file = new File(iTunes_path_field.getText());
               File mp3_directory = null;
               if (use_mp3_checkbox.isSelected())
                    mp3_directory = new File(mp3_path_field.getText());
               
               // Get the save path
               File save_file = null;
               JFileChooser save_path_browser = new JFileChooser();
               int browse_return = save_path_browser.showSaveDialog(this);
               if (browse_return == JFileChooser.APPROVE_OPTION)
               {
                    String selection = save_path_browser.getSelectedFile().getAbsolutePath();
                    boolean go_ahead = true;
                    if ((new File(selection)).exists())
                    {
                         int response = JOptionPane.showConfirmDialog(null, "A file " +
                              "with the specified path already exists.\nDo you wish to overwrite it?", "Warning",
                              JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                         if (response != JOptionPane.YES_OPTION) go_ahead = false;
                    }
                    if (go_ahead)
                         save_file = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(selection, true);
               }
               
               // Perform the export
               if (save_file != null)
                    RecordingMetaData.exportRecordingMetaData(to_arff, to_ace, iTunes_file, mp3_directory, save_file);
          }
          catch (Throwable t)
          {
               if (t.toString().equals("java.lang.OutOfMemoryError"))
                    JOptionPane.showMessageDialog(null, "The Java Runtime ran out of memory. Please rerun this program with a higher amount of memory assigned to the Java Runtime heap.\n\nAnalysis cancelled.", "ERROR", JOptionPane.ERROR_MESSAGE);
               else
                    JOptionPane.showMessageDialog(null, t.getMessage() + "\n\nExport cancelled.", "ERROR", JOptionPane.ERROR_MESSAGE);
          }
          
          // Enable the diplay panel
          parent.enableReportPanel(true);
     }
     
     
     /**
      * Begin processing based on user selected preferences. Display an error
      * message if a problem occurs.
      */
     public void execute()
     {
          // Verify if the user has chosen to display edit distances
          if (report_edit_distances_checkbox.isSelected())
          {
               int response = JOptionPane.showConfirmDialog(null, "Explicitly printing error distances requires a large amount of memory.\nThis report is generally unnesecary.\nAre you sure that you wish to generate this report?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
               if (response == JOptionPane.NO_OPTION)
                    report_edit_distances_checkbox.setSelected(false);
          }
          
          // Execute
          try
          {
               // Disable the diplay panel
               parent.enableReportPanel(false);
               
               // Set the iTunes and MP3 directories based on their respective
               // check boxes
               String iTunes;
               if (use_iTunes_checkbox.isSelected())
                    iTunes = iTunes_path_field.getText();
               else
                    iTunes = "";
               String mp3 = null;
               if (use_mp3_checkbox.isSelected())
                    mp3 = mp3_path_field.getText();
               else
                    mp3 = "";
               
               // Ensure that the save path has a .html extension
               String save_path = save_path_field.getText();
               String extension = mckay.utilities.staticlibraries.StringMethods.getExtension(save_path);
               boolean problem = false;
               if (extension == null) problem = true;
               else if (!extension.equals(".html")) problem = true;
               if (problem)
               {
                    boolean ok = false;
                    int response = JOptionPane.showConfirmDialog(null, "The report save path " +
                         save_path + " does not end with a .html extension.\nDo you wish to replace the " +
                         "existing extension with a .html extension?", "Warning",
                         JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (response == JOptionPane.OK_OPTION)
                    {
                         String new_path = mckay.utilities.staticlibraries.StringMethods.replaceExtension(save_path, "html");
                         if (new_path != null)
                         {
                              save_path_field.setText(new_path);
                              ok = true;
                         }
                    }
                    if (!ok) throw new Exception("Report save path does not have a .html extension.");
               }
               
               // Store and validate the preferences
               AnalysisPreferences preferences = new AnalysisPreferences(
                    iTunes,
                    mp3,
                    save_path_field.getText(),
                    perform_find_replace_operations_checkbox.isSelected(),
                    ignore_case_in_edit_distances_checkbox.isSelected(),
                    remove_numbers_at_title_beginnings_checkbox.isSelected(),
                    convert_ings_checkbox.isSelected(),
                    convert_title_abbreviations_checkbox.isSelected(),
                    remove_periods_checkbox.isSelected(),
                    remove_commas_checkbox.isSelected(),
                    remove_hyphens_checkbox.isSelected(),
                    remove_colons_checkbox.isSelected(),
                    remove_seimcolons_checkbox.isSelected(),
                    remove_quotation_marks_checkbox.isSelected(),
                    remove_single_quotes_checkbox.isSelected(),
                    remove_brackets_checkbox.isSelected(),
                    convert_ands_to_ampersands_checkbox.isSelected(),
                    remove_thes_checkbox.isSelected(),
                    remove_spaces_checkbox.isSelected(),
                    check_word_ordering_checkbox.isSelected(),
                    ((double) mckay.utilities.staticlibraries.StringMethods.getIntInLimits(word_ordering_percent_match_textfield.getText(), 1, 100)) / 100.0,
                    check_word_subset_checkbox.isSelected(),
                    ((double) mckay.utilities.staticlibraries.StringMethods.getIntInLimits(word_subset_percent_match_textfield.getText(), 1, 100)) / 100.0,
                    calculate_absolute_ED_checkbox.isSelected(),
                    mckay.utilities.staticlibraries.StringMethods.getIntInLimits(absolute_ED_threshold_textfield.getText(), 1, 75),
                    calculate_proportional_ED_checkbox.isSelected(),
                    mckay.utilities.staticlibraries.StringMethods.getIntInLimits(proportional_ED_threshold_textfield.getText(), 1, 99),
                    calculate_subset_ED_checkbox.isSelected(),
                    mckay.utilities.staticlibraries.StringMethods.getIntInLimits(subset_ED_threshold_textfield.getText(), 1, 99),
                    filter_duplicates_by_duration_checkbox.isSelected(),
                    mckay.utilities.staticlibraries.StringMethods.getIntInLimits(duration_filter_percent_max_textfield.getText(), 0, 100),
                    filter_duplicates_by_artist_checkbox.isSelected(),
                    filter_duplicates_by_composer_checkbox.isSelected(),
                    filter_duplicates_by_genre_checkbox.isSelected(),
                    filter_duplicates_by_album_checkbox.isSelected(),
                    list_mp3_files_could_not_parse_checkbox.isSelected(),
                    list_all_non_mp3s_in_directory_checkbox.isSelected(),
                    list_all_recordings_found_checkbox.isSelected(),
                    list_files_in_XML_but_missing_checkbox.isSelected(),
                    list_noncorresponding_recordings_checkbox.isSelected(),
                    list_noncorresponding_fields_checkbox.isSelected(),
                    list_all_postmerge_metadata_checkbox.isSelected(),
                    list_sorted_recording_metadata_checkbox.isSelected(),
                    resport_artist_breakdown_checkbox.isSelected(),
                    resport_composer_breakdown_checkbox.isSelected(),
                    report_genre_breakdown_checkbox.isSelected(),
                    report_comment_breakdown_checkbox.isSelected(),
                    list_artists_by_genre.isSelected(),
                    list_composers_by_genre.isSelected(),
                    report_missing_metadata_checkbox.isSelected(),
                    list_artists_with_few_recordings_checkbox.isSelected(),
                    mckay.utilities.staticlibraries.StringMethods.getIntInLimits(cutoff_for_artists_few_recs_textfield.getText(), 2, 90),
                    list_composers_with_few_recordings_checkbox.isSelected(),
                    mckay.utilities.staticlibraries.StringMethods.getIntInLimits(cutoff_for_composers_few_recs_textfield.getText(), 2, 90),
                    report_identical_titles_checkbox.isSelected(),
                    report_starting_with_spaces_checkbox.isSelected(),
                    list_albums_by_artist_checkbox.isSelected(),
                    list_albums_by_composer_checkbox.isSelected(),
                    list_incomplete_albums_checkbox.isSelected(),
                    mckay.utilities.staticlibraries.StringMethods.getIntInLimits(incoplete_albums_threshold_textfield.getText(), 1, 100),
                    list_albums_with_duplicate_tracks_checkbox.isSelected(),
                    list_albums_missing_year_checkbox.isSelected(),
                    report_on_compilation_albums_checkbox.isSelected(),
                    report_differing_only_in_case_checkbox.isSelected(),
                    report_all_find_replace_changes_checkbox.isSelected(),
                    report_new_identicals_after_fr_checkbox.isSelected(),
                    report_word_ordering_tests_checkbox.isSelected(),
                    report_edit_distances_checkbox.isSelected(),
                    list_titles_rejected_as_equivalent_checkbox.isSelected(),
                    report_probable_duplicates_checkbox.isSelected(),
                    report_wrongly_differing_titles_checkbox.isSelected(),
                    report_wrongly_differing_artists_checkbox.isSelected(),
                    report_wrongly_differing_composers_checkbox.isSelected(),
                    report_wrongly_differing_albums_checkbox.isSelected(),
                    report_wrongly_differing_genres_checkbox.isSelected(),
                    report_options_selected_checkbox.isSelected(),
                    report_processing_times_checkbox.isSelected());
               
               // Begin processing with a progress bar
               int progress_bar_polling_interval = 333; // in milliseconds
               ProcessingProgressBar processor = new ProcessingProgressBar(this, preferences);
               DoubleProgressBarDialog progress_bar = new DoubleProgressBarDialog(parent,
                    processor,
                    progress_bar_polling_interval);
               processor.setProgressBarDialog(progress_bar);
               processor.go();
          }
          catch (Throwable t) // note that this only catches exceptions before threads spawned
          {
               // t.printStackTrace();
               
               // React to the Java Runtime running out of memory
               if (t.toString().equals("java.lang.OutOfMemoryError"))
                    JOptionPane.showMessageDialog(null, "The Java Runtime ran out of memory. Please rerun this program with a higher amount of memory assigned to the Java Runtime heap.\n\nAnalysis cancelled.", "ERROR", JOptionPane.ERROR_MESSAGE);
               else
                    JOptionPane.showMessageDialog(null, t.getMessage() + "\n\nAnalysis cancelled.", "ERROR", JOptionPane.ERROR_MESSAGE);
          }
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Display the appropriate file browsing dialog box and store the selection
      * in the appropriate text box.
      *
      * @param what_to_browse A code identifying which dialog box to display.
      *                       The options are "iTunes XML", "MP3 Directory"
      *                       or "Save Path".
      * @return               Whether or not a path was succesfully chosen.
      *                       False is returned if an invalid what_to_browse
      *                       code is given.
      */
     private boolean browse(String what_to_browse)
     {
          // Use the iTunes XML file browser
          if (what_to_browse.equals("iTunes XML"))
          {
               if (iTunes_browser == null)
               {
                    iTunes_browser = new JFileChooser();
                    String[] accepted_extensions = {"xml"};
                    iTunes_browser.setFileFilter(new mckay.utilities.general.FileFilterImplementation(accepted_extensions));
               }
               int browse_return = iTunes_browser.showOpenDialog(this);
               if (browse_return == JFileChooser.APPROVE_OPTION)
               {
                    iTunes_path_field.setText(iTunes_browser.getSelectedFile().getAbsolutePath());
                    return true;
               }
          }
          
          // Use the MP3 directory file browser. A directory, not a file, must
          // be selected
          else if (what_to_browse.equals("MP3 Directory"))
          {
               if (mp3_browser == null)
               {
                    mp3_browser = new JFileChooser();
                    mp3_browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
               }
               int browse_return = mp3_browser.showOpenDialog(this);
               if (browse_return == JFileChooser.APPROVE_OPTION)
               {
                    mp3_path_field.setText(mp3_browser.getSelectedFile().getAbsolutePath());
                    return true;
               }
          }
          
          // Use the report save path browser
          else if (what_to_browse.equals("Save Path"))
          {
               if (save_path_browser == null)
                    save_path_browser = new JFileChooser();
               int browse_return = save_path_browser.showSaveDialog(this);
               if (browse_return == JFileChooser.APPROVE_OPTION)
               {
                    String selection = save_path_browser.getSelectedFile().getAbsolutePath();
                    boolean go_ahead = true;
                    if ((new File(selection)).exists())
                    {
                         int response = JOptionPane.showConfirmDialog(null, "A file " +
                              "with the specified path already exists.\nDo you wish to overwrite it?", "Warning",
                              JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                         if (response != JOptionPane.YES_OPTION)
                              go_ahead = false;
                    }
                    if (go_ahead)
                         save_path_field.setText(selection);
                    return go_ahead;
               }
          }
          
          // Return false if an invalid what_to_browse code was given
          return false;
     }
     
     
     /**
      * Returns a JPanel with check_box on the left and, a JLabel filled with
      * label in the middle and text_field on the right. The latter two are
      * right justified.
      *
      * <p>An ActionListener is also attached to the given check_box.
      *
      * @param check_box      The JCheckBox to add to the panel.
      * @param label          The label to give text_field.
      * @param text_field     The JTextField to add to the panel.
      * @param horizontal_gap The horizontal gap between components.
      * @param vertical_gap   The vertical gap between components.
      * @return               The constructed JPanel.
      */
     private JPanel getCheckboxTextfieldCombo(JCheckBox check_box, String label,
          JTextField text_field, int horizontal_gap, int vertical_gap)
     {
          check_box.addActionListener(this);
          
          JPanel panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          panel.add(check_box, BorderLayout.CENTER);
          JPanel right_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          right_panel.add(new JLabel(label), BorderLayout.WEST);
          right_panel.add(text_field, BorderLayout.CENTER);
          panel.add(right_panel, BorderLayout.EAST);
          return panel;
     }
     
     
     /**
      * Enable or disable components based on the value of a check box.
      *
      * @param check_box The check box to base the enabling on.
      */
     private void updateBasedOnCheckBoxes(JCheckBox check_box)
     {
          if (check_box == use_mp3_checkbox)
          {
               if (use_mp3_checkbox.isSelected())
               {
                    browse_mp3s_button.setEnabled(true);
                    mp3_path_field.setEnabled(true);
                    list_mp3_files_could_not_parse_checkbox.setEnabled(true);
                    list_all_non_mp3s_in_directory_checkbox.setEnabled(true);
                    
                    if (use_iTunes_checkbox.isSelected())
                    {
                         list_noncorresponding_recordings_checkbox.setEnabled(true);
                         list_noncorresponding_fields_checkbox.setEnabled(true);
                    }
                    else
                    {
                         list_noncorresponding_recordings_checkbox.setEnabled(false);
                         list_noncorresponding_fields_checkbox.setEnabled(false);
                    }
               }
               else
               {
                    browse_mp3s_button.setEnabled(false);
                    mp3_path_field.setEnabled(false);
                    list_mp3_files_could_not_parse_checkbox.setEnabled(false);
                    list_all_non_mp3s_in_directory_checkbox.setEnabled(false);
                    
                    list_noncorresponding_recordings_checkbox.setEnabled(false);
                    list_noncorresponding_fields_checkbox.setEnabled(false);
               }
          }
          else if (check_box == use_iTunes_checkbox)
          {
               if (use_iTunes_checkbox.isSelected())
               {
                    browse_iTunes_button.setEnabled(true);
                    iTunes_path_field.setEnabled(true);
                    list_files_in_XML_but_missing_checkbox.setEnabled(true);
                    
                    if (use_mp3_checkbox.isSelected())
                    {
                         list_noncorresponding_recordings_checkbox.setEnabled(true);
                         list_noncorresponding_fields_checkbox.setEnabled(true);
                    }
                    else
                    {
                         list_noncorresponding_recordings_checkbox.setEnabled(false);
                         list_noncorresponding_fields_checkbox.setEnabled(false);
                    }
               }
               else
               {
                    browse_iTunes_button.setEnabled(false);
                    iTunes_path_field.setEnabled(false);
                    list_files_in_XML_but_missing_checkbox.setEnabled(false);
                    
                    list_noncorresponding_recordings_checkbox.setEnabled(false);
                    list_noncorresponding_fields_checkbox.setEnabled(false);
               }
          }
          else if(check_box == perform_find_replace_operations_checkbox)
          {
               if (perform_find_replace_operations_checkbox.isSelected())
               {
                    ignore_case_in_edit_distances_checkbox.setEnabled(true);
                    remove_numbers_at_title_beginnings_checkbox.setEnabled(true);
                    convert_ands_to_ampersands_checkbox.setEnabled(true);
                    convert_ings_checkbox.setEnabled(true);
                    convert_title_abbreviations_checkbox.setEnabled(true);
                    remove_thes_checkbox.setEnabled(true);
                    remove_periods_checkbox.setEnabled(true);
                    remove_commas_checkbox.setEnabled(true);
                    remove_hyphens_checkbox.setEnabled(true);
                    remove_colons_checkbox.setEnabled(true);
                    remove_seimcolons_checkbox.setEnabled(true);
                    remove_quotation_marks_checkbox.setEnabled(true);
                    remove_single_quotes_checkbox.setEnabled(true);
                    remove_brackets_checkbox.setEnabled(true);
                    remove_spaces_checkbox.setEnabled(true);
                    if (ignore_case_in_edit_distances_checkbox.isSelected())
                         report_differing_only_in_case_checkbox.setEnabled(true);
                    report_all_find_replace_changes_checkbox.setEnabled(true);
                    report_new_identicals_after_fr_checkbox.setEnabled(true);
               }
               else
               {
                    ignore_case_in_edit_distances_checkbox.setEnabled(false);
                    remove_numbers_at_title_beginnings_checkbox.setEnabled(false);
                    convert_ands_to_ampersands_checkbox.setEnabled(false);
                    convert_ings_checkbox.setEnabled(false);
                    convert_title_abbreviations_checkbox.setEnabled(false);
                    remove_thes_checkbox.setEnabled(false);
                    remove_periods_checkbox.setEnabled(false);
                    remove_commas_checkbox.setEnabled(false);
                    remove_hyphens_checkbox.setEnabled(false);
                    remove_colons_checkbox.setEnabled(false);
                    remove_seimcolons_checkbox.setEnabled(false);
                    remove_quotation_marks_checkbox.setEnabled(false);
                    remove_single_quotes_checkbox.setEnabled(false);
                    remove_brackets_checkbox.setEnabled(false);
                    remove_spaces_checkbox.setEnabled(false);
                    report_differing_only_in_case_checkbox.setEnabled(false);
                    report_all_find_replace_changes_checkbox.setEnabled(false);
                    report_new_identicals_after_fr_checkbox.setEnabled(false);
               }
          }
          else if (check_box == ignore_case_in_edit_distances_checkbox)
          {
               if (ignore_case_in_edit_distances_checkbox.isSelected() && perform_find_replace_operations_checkbox.isSelected())
                    report_differing_only_in_case_checkbox.setEnabled(true);
               else if(!ignore_case_in_edit_distances_checkbox.isSelected())
                    report_differing_only_in_case_checkbox.setEnabled(false);
          }
          else if (check_box == check_word_ordering_checkbox)
          {
               if (check_word_ordering_checkbox.isSelected())
                    word_ordering_percent_match_textfield.setEnabled(true);
               else
                    word_ordering_percent_match_textfield.setEnabled(false);
               
               if (!check_word_ordering_checkbox.isSelected() && !check_word_subset_checkbox.isSelected())
                    report_word_ordering_tests_checkbox.setEnabled(false);
               else report_word_ordering_tests_checkbox.setEnabled(true);
          }
          else if (check_box == check_word_subset_checkbox)
          {
               if (check_word_subset_checkbox.isSelected())
                    word_subset_percent_match_textfield.setEnabled(true);
               else
                    word_subset_percent_match_textfield.setEnabled(false);
               
               if (!check_word_ordering_checkbox.isSelected() && !check_word_subset_checkbox.isSelected())
                    report_word_ordering_tests_checkbox.setEnabled(false);
               else report_word_ordering_tests_checkbox.setEnabled(true);
          }
          else if (check_box == calculate_absolute_ED_checkbox)
          {
               if (calculate_absolute_ED_checkbox.isSelected())
               {
                    absolute_ED_threshold_textfield.setEnabled(true);
                    report_edit_distances_checkbox.setEnabled(true);
               }
               else
               {
                    absolute_ED_threshold_textfield.setEnabled(false);
                    if (!calculate_proportional_ED_checkbox.isSelected() && !calculate_subset_ED_checkbox.isSelected())
                         report_edit_distances_checkbox.setEnabled(false);
               }
          }
          else if (check_box == calculate_proportional_ED_checkbox)
          {
               if (calculate_proportional_ED_checkbox.isSelected())
               {
                    proportional_ED_threshold_textfield.setEnabled(true);
                    report_edit_distances_checkbox.setEnabled(true);
               }
               else
               {
                    proportional_ED_threshold_textfield.setEnabled(false);
                    if (!calculate_absolute_ED_checkbox.isSelected() && !calculate_subset_ED_checkbox.isSelected())
                         report_edit_distances_checkbox.setEnabled(false);
               }
          }
          else if (check_box == calculate_subset_ED_checkbox)
          {
               if (calculate_subset_ED_checkbox.isSelected())
               {
                    subset_ED_threshold_textfield.setEnabled(true);
                    report_edit_distances_checkbox.setEnabled(true);
               }
               else
               {
                    subset_ED_threshold_textfield.setEnabled(false);
                    if (!calculate_absolute_ED_checkbox.isSelected() && !calculate_proportional_ED_checkbox.isSelected())
                         report_edit_distances_checkbox.setEnabled(false);
               }
          }
          else if (check_box == filter_duplicates_by_duration_checkbox)
          {
               if (filter_duplicates_by_duration_checkbox.isSelected())
                    duration_filter_percent_max_textfield.setEnabled(true);
               else duration_filter_percent_max_textfield.setEnabled(false);
          }
          else if (check_box == list_artists_with_few_recordings_checkbox)
          {
               if (list_artists_with_few_recordings_checkbox.isSelected())
                    cutoff_for_artists_few_recs_textfield.setEnabled(true);
               else
                    cutoff_for_artists_few_recs_textfield.setEnabled(false);
          }
          else if (check_box == list_composers_with_few_recordings_checkbox)
          {
               if (list_composers_with_few_recordings_checkbox.isSelected())
                    cutoff_for_composers_few_recs_textfield.setEnabled(true);
               else
                    cutoff_for_composers_few_recs_textfield.setEnabled(false);
          }
          else if (check_box == list_incomplete_albums_checkbox)
          {
               if (list_incomplete_albums_checkbox.isSelected())
                    incoplete_albums_threshold_textfield.setEnabled(true);
               else
                    incoplete_albums_threshold_textfield.setEnabled(false);
          }
          else if (check_box == report_probable_duplicates_checkbox)
          {
               if (report_probable_duplicates_checkbox.isSelected())
               {
                    filter_duplicates_by_duration_checkbox.setEnabled(true);
                    duration_filter_percent_max_textfield.setEnabled(true);
                    filter_duplicates_by_artist_checkbox.setEnabled(true);
                    filter_duplicates_by_composer_checkbox.setEnabled(true);
                    filter_duplicates_by_genre_checkbox.setEnabled(true);
                    filter_duplicates_by_album_checkbox.setEnabled(true);
                    list_titles_rejected_as_equivalent_checkbox.setEnabled(true);
               }
               else
               {
                    filter_duplicates_by_duration_checkbox.setEnabled(false);
                    duration_filter_percent_max_textfield.setEnabled(false);
                    filter_duplicates_by_artist_checkbox.setEnabled(false);
                    filter_duplicates_by_composer_checkbox.setEnabled(false);
                    filter_duplicates_by_genre_checkbox.setEnabled(false);
                    filter_duplicates_by_album_checkbox.setEnabled(false);
                    list_titles_rejected_as_equivalent_checkbox.setEnabled(false);
               }
          }
     }
     
     
     /* INTERNAL CLASS ********************************************************/
     
     
     /**
      * A JLabel that is coloured coloured blue.
      */
     private class ColouredJLabel
          extends JLabel
     {
          /**
           * Same as superclass' constructor, but sets colour to blue.
           */
          public ColouredJLabel(String text)
          {
               super(text);
               setForeground(new Color(0, 0, 255));
          }
     }
}
