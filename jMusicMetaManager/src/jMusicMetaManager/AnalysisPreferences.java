/*
 * AnalysisPreferences.java
 * Version 1.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jMusicMetaManager;

import java.io.File;


/**
 * An object of this class holds the settings to be used when jMusicMetaManager
 * analyzes recording metadata.
 *
 * @author Cory McKay
 */
public class AnalysisPreferences
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The path of the iTunes XML file to be analyzed. A value of null
      * indicates that no iTunes XML file is to be analyzed.
      */
     public File         iTunes_file;
     
     /**
      * The path of a directory containing MP3 files (and/or subdirectories
      * containing MP3s) whose ID3 tags are to be analyzed. A value of null
      * indicates that no ID3 tags are to be analyzed.
      */
     public File         mp3_directory;
     
     /**
      * The path to which a report file should be saved. A value of null is not
      * permitted.
      */
     public File         save_file;
     
     /**
      * A gloabal control. The ignore_case_in_edit_distances,
      * convert_ands_to_ampersands, convert_ings, convert_title_abbreviations,
      * convert_title_abbreviations, remove_thes, remove_periods, remove_commas,
      * remove_hyphens and remove_spaces operations are only performed if this
      * is true.
      */
     public boolean      perform_find_replace_operations;
     
     /**
      * Whether or not edit distances are calculated with letters belonging to
      * different cases treated as identical.
      */
     public boolean      ignore_case_in_edit_distances;
     
     /**
      * Whether or not all numbers and spaces at the very beginning of recording
      * titles are removed.
      */
     public boolean      remove_numbers_at_title_beginnings;
     
     /**
      * Whether or not all incidences of "in'" are converted to "ing".
      */
     public boolean      convert_ings;
     
     /**
      * Whether or not the following conversions are made: "Mister " to "Mr. ",
      * "Doctor " to "Dr. " and "Professor " to "Prof. ".
      */
     public boolean      convert_title_abbreviations;
     
     /**
      * Whether or not all periods are removed.
      */
     public boolean      remove_periods;
     
     /**
      * Whether or not all commas are removed.
      */
     public boolean      remove_commas;
     
     /**
      * Whether or not all hyphens are removed.
      */
     public boolean      remove_hyphens;
     
     /**
      * Whether or not all colons are removed.
      */
     public boolean      remove_colons;
     
     /**
      * Whether or not all semicolons are removed.
      */
     public boolean      remove_semicolons;
     
     /**
      * Whether or not all quotation marks are removed.
      */
     public boolean      remove_quotation_marks;
     
     /**
      * Whether or not all apostrophes and single quotes are removed.
      */
     public boolean      remove_single_quotes;
     
     /**
      * Whether or not all parentheses, square brackets and curly braces
      * are removed.
      */
     public boolean      remove_brackets;
     
     /**
      *
      * Whether or not all incidences of " and " are converted to " & ".
      */
     public boolean      convert_ands_to_ampersands;
     
     /**
      * Whether or not all occurences of "the " are removed.
      */
     public boolean      remove_thes;
     
     /**
      * Whether or not all spaces are removed.
      */
     public boolean      remove_spaces;
     
     /**
      * Whether or not a check is performed to detect possible errors in fields
      * due to different word orderings for otherwise identical field values.
      */
     public boolean      check_word_ordering;
     
     /**
      * The minimum fraction (0.0 to 1.0) of words that must match in a
      * check_word_ordering test in order for two fields to be reported as
      * intended to be identical.
      */
     public double       word_ordering_fraction_match;
     
     /**
      * Whether or not a check is performed to detect possible errors in fields
      * due to the words in one field's value being a subset of the words in
      * another field's value.
      */
     public boolean      check_word_subset;
     
     /**
      * The minimum fraction (0.0 to 1.0) of words that must match in a
      * word_subset_fraction_match test in order for two fields to be
      * reported as intended to be identical.
      */
     public double       word_subset_fraction_match;
     
     /**
      * Whether or not the edit distance is calculated between pairs of
      * differing values found for a field, and a threshold based on the
      * absolute edit distance between each pair is used to determine if the
      * values should likely in fact be the same.
      */
     public boolean      calculate_absolute_ED;
     
     /**
      * The maximum absolute edit distance permissible for two strings to be
      * considered likely to be identical. Related to the calculate_absolute_ED
      * field.
      */
     public int          absolute_ED_threshold;
     
     /**
      * Whether or not the edit distance is calculated between pairs of
      * differing values found for a field, and a threshold based on the
      * absolute edit distance between each pair divided by the length of the
      * longer field is used to determine if the values should likely in fact be
      * the same.
      */
     public boolean      calculate_proportional_ED;
     
     /**
      * The maximum proportional edit distance permissible for two strings to be
      * considered likely to be identical (calculated by dividing the absolute
      * distance by the length of the longer field). Related to the
      * calculate_proportional_ED field.
      */
     public int          proportional_ED_threshold;
     
     /**
      * Whether or not the edit distance is calculated between pairs of
      * differing values found for a field, and a threshold based on the
      * absolute edit distance between each pair minus the difference in lengths
      * of the fields, all divided by the length of the shorter field, is used
      * to determine if the values should likely in fact be the same.
      */
     public boolean      calculate_subset_ED;
     
     /**
      * The maximum subset edit distance permissible for two strings to be
      * considered likely to be identical (calculated by subtracting the
      * difference in length from the absolute edit distance of the two strings
      * and dividing the result by the length of the shorter field). Related to
      * the calculate_proportional_ED calculate_subset_ED.
      */
     public int          subset_ED_threshold;
     
     /**
      * Whether or not the durations of pairs of recordings are considered when
      * determining whether they are duplicates of each other.
      */
     public boolean      filter_duplicates_by_duration;
     
     /**
      * The maximum percentage difference between the durations of two
      * recordings for them to be considered to be duplicates of each other.
      * Must be from 0 to 100.
      */
     public int          duration_filter_percent_max = 0;
     
     /**
      * Whether or not pairs of recordings must have the same values in their
      * artist fields for them to be considered to be duplicates of each other.
      */
     public boolean      filter_duplicates_by_artist;
     
     /**
      * Whether or not pairs of recordings must have the same values in their
      * composer fields for them to be considered to be duplicates of each
      * other.
      */
     public boolean      filter_duplicates_by_composer;
     
     /**
      * Whether or not pairs of recordings must have at least one identical
      * value in their genre fields for them to be considered to be duplicates
      * of each other.
      */
     public boolean      filter_duplicates_by_genre;
     
     /**
      * Whether or not pairs of recordings must have the same values in their
      * album fields for them to be considered to be duplicates of each other.
      */
     public boolean      filter_duplicates_by_album;
     
     /**
      * Whether or not a report is generated listing all MP3 files that were
      * found but could not be parsed, and why they could not be parsed.
      */
     public boolean      list_mp3_files_could_not_parse;
     
     /**
      * Whether or not the paths of all files found in the mp3_directory that
      * do not end with the .mp3 or .MP3 extension are reported.
      */
     public boolean      list_all_non_mp3s_in_directory;
     
     /**
      * Whether or not the path, title and artist of all recordings for which
      * metadata is extracted is reported. Two separate lists are produced, one
      * for the recordings extracted from ID3 tags and one for recordings
      * extracted from an iTunes XML file.
      */
     public boolean      list_all_recordings_found;
     
     /**
      * Whether or not the path of each recording parsed from an iTunes XML file
      * is checked, and a report is generated listing such files that are
      * missing.
      */
     public boolean      list_files_in_XML_but_missing;
     
     /**
      * Whether or not the recordings that are referenced in an iTunes XML file
      * but are not present on disk (and vice versa) are reported. This is only
      * meaningful if both an iTunes XML file and an MP3 directory are specified
      * by the user.
      */
     public boolean      list_noncorresponding_recordings;
     
     /**
      * Whether or not fields that differ between each MP3 file and its
      * corresponding iTunes XML entry are reported. This is only
      * meaningful if both an iTunes XML file and an MP3 directory are specified
      * by the user.
      */
     public boolean      list_noncorresponding_fields;
     
     /**
      * Whether or not all available metadata is reported for every recording
      * after the metadata from the iTunes XML file and MP3 ID3 tags have been
      * merged. This is generally used only for testing, as it is largely
      * redundant if the list_sorted_recording_metadata option is selected.
      */
     public boolean      list_all_postmerge_metadata;
     
     /**
      * Whether or not all available metadata is reported for every recording
      * after the recordings have been sorted by title.
      */
     public boolean      list_sorted_recording_metadata;
     
     /**
      * Whether or not a report is generated that names all unique artists and
      * statistics about their entries in the music database.
      */
     public boolean      report_artist_breakdown;
     
     /**
      * Whether or not a report is generated that names all unique composers and
      * statistics about their entries in the music database.
      */
     public boolean      report_composer_breakdown;
     
     /**
      * Whether or not a report is generated that names all unique genres and
      * statistics about their entries in the music database.
      */
     public boolean      report_genre_breakdown;
     
     /**
      * Whether or not a report is generated that names all unique comments and
      * statistics about their entries in the music database.
      */
     public boolean      report_comment_breakdown;
     
     /**
      * Whether or not a report is generated alphabetically listing each genre
      * present. All artists present that have at least one recording in a
      * given genre are listed under that genre's entry as well as the number
      * of recordings by the artist belonging to the given genre and the
      * percentage of the recordings in that genre that they represent.
      */
     public boolean      list_artists_by_genre;
     
     /**
      * Whether or not a report is generated alphabetically listing each genre
      * present. All composers present that have at least one recording in a
      * given genre are listed under that genre's entry as well as the number
      * of recordings by the composer belonging to the given genre and the
      * percentage of the recordings in that genre that they represent.
      */
     public boolean      list_composers_by_genre;
     
     /**
      * Whether or not all recordings should be explicity reported that have
      * empty title, artist, composer, album and/or genre fields. A separate
      * table is produced for each of these.
      */
     public boolean      report_missing_metadata;
     
     /**
      * Whether or not a list of artists with only a few recordings is
      * generated.
      */
     public boolean      list_artists_with_few_recordings;
     
     /**
      * The number of recordings cutoff to use for the list of artists with only
      * a few recordings.
      */
     public int          cutoff_for_artists_few_recs;
     
     /**
      * Whether or not a list of composers with only a few recordings is
      * generated.
      */
     public boolean      list_composers_with_few_recordings;
     
     /**
      * The number of recordings cutoff to use for the list of artists with only
      * a few recordings.
      */
     public int          cutoff_for_composers_few_recs;
     
     /**
      * Whether or not a list of all titles that are identical is generated.
      */
     public boolean      report_identical_titles;
     
     /**
      * Whether or not a list of titles, artists, composers, albums and genres
      * that start with spaces is generated.
      */
     public boolean      report_starting_with_spaces;
     
     /**
      * Whether or not a report is generated alphabetically listing each artist
      * present. All albums present that have at least one recording by a given
      * artist are listed under that artist's entry. Note is also made of
      * whether each album is a compilation, how many tracks in each album are
      * by the corresponding artist and how many tracks belonging to each album
      * are present.
      */
     public boolean      list_albums_by_artist;
     
     /**
      * Whether or not a report is generated alphabetically listing each
      * composer present. All albums present that have at least one recording by
      * a given composer are listed under that composer's entry. Note is also
      * made of whether each album is a compilation, how many tracks in each
      * album are by the corresponding composer and how many tracks belonging to
      * each album are present.
      */
     public boolean      list_albums_by_composer;
     
     /**
      * Whether or not a report is generated listing all albums that are missing
      * tracks, or that have an unknown number of tracks.
      */
     public boolean      list_incomplete_albums;
     
     /**
      * The percentage of tracks present in an album below which an album will
      * be listed as bold in the Incomplete Albums report.
      */
     public int          incoplete_albums_threshold;
     
     /**
      * Whether or not a list is generated of all albums with multiple tracks
      * with the same number as well as of albums containing one or
      * more recordings that do not have a track number specified.
      */
     public boolean      list_albums_with_duplicate_tracks;
     
     /**
      * Whether or not a list is generated of albums containing one or more
      * recordings that do not have year metadata specified.
      */
     public boolean      list_albums_missing_year;
     
     /**
      * Whether or not a list of all albums marked as compilations is generated,
      * as well as a list of albums that are not marked as compilations but
      * contain multiple artists and a list of albums that are marked as
      * compilations but contain only one artist.
      */
     public boolean      report_on_compilation_albums;
     
     /**
      * Whether or not to report entries where otherwise identical title,
      * artist, composer, genre and album fields differ in case.
      */
     public boolean      report_differing_only_in_case;
     
     /**
      * Whether or not a report is generated detailing every change made during
      * the find and replace operations.
      */
     public boolean      report_all_find_replace_changes;
     
     /**
      * Whether or not a report is generated describing all newly identical
      * entries after the find and replace operations have been performed.
      */
     public boolean      report_new_identicals_after_fr;
     
     /**
      * Whether or not a report is generated describing describing the results
      * of the word_ordering_fraction_match and word_subset_fraction_match
      * reports for artists, composers and genres.
      */
     public boolean      report_word_ordering_tests;
     
     /**
      * Whether or not a report is generated listing a table of edit distances
      * for each pair of values for each selected field.
      */
     public boolean      report_edit_distances;
     
     /**
      * Whether or not a report is generated listing all recordings that were
      * filtered out of the probable duplicates report.
      */
     public boolean      list_titles_rejected_as_equivalent;
     
     /**
      * Whether or not a report should be generated indicating recordings that
      * are likely multiple occurences of the same recording under the same
      * or different names.
      */
     public boolean      report_probable_duplicates;
     
     /**
      * Whether or not a report should be generated indicating all clusters of
      * title fields whose members differ from one another, but nonetheless
      * likely should, in fact, be identical.
      */
     public boolean      report_wrongly_differing_titles;
     
     /**
      * Whether or not a report should be generated indicating all clusters of
      * artist fields whose members differ from one another, but nonetheless
      * likely should, in fact, be identical.
      */
     public boolean      report_wrongly_differing_artists;
     
     /**
      * Whether or not a report should be generated indicating all clusters of
      * composer fields whose members differ from one another, but nonetheless
      * likely should, in fact, be identical.
      */
     public boolean      report_wrongly_differing_composers;
     
     /**
      * Whether or not a report should be generated indicating all clusters of
      * album fields whose members differ from one another, but nonetheless
      * likely should, in fact, be identical.
      */
     public boolean      report_wrongly_differing_albums;
     
     /**
      * Whether or not a report should be generated indicating all clusters of
      * genre fields whose members differ from one another, but nonetheless
      * likely should, in fact, be identical.
      */
     public boolean      report_wrongly_differing_genres;
     
     /**
      * Whether or not a list of all options selected for the run of processing
      * and reporting is generated.
      */
     public boolean      report_options_selected;
     
     /**
      * Whether or not the processing times needed for each portion of the
      * analysis should be reported.
      */
     public boolean      report_processing_times;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new instance of AnalysisPreferences with the given
      * preferences and validates the supplied paramters.
      *
      *
      * @param iTunes_path                        Used to set the iTunes_file
      *                                            field. A value of ""
      *                                            indicates that no iTunes file
      *                                            is to be analyzed.
      * @param mp3_path                           Used to set the mp3_directory
      *                                            field. A value of ""
      *                                            indicates that no MP3 files
      *                                            are to be analyzed.
      * @param save_path                          Used to set the save_file
      *                                            field. A value of "" is not
      *                                            permitted.
      * @param perform_find_replace_operations    Sets the corresponding field.
      * @param ignore_case_in_edit_distances      Sets the corresponding field.
      * @param remove_numbers_at_title_beginnings Wets the corresponding field.
      * @param convert_ings                       Sets the corresponding field.
      * @param convert_title_abbreviations        Sets the corresponding field.
      * @param remove_periods                     Sets the corresponding field.
      * @param remove_commas                      Sets the corresponding field.
      * @param remove_hyphens                     Sets the corresponding field.
      * @param remove_colons                      Sets the corresponding field.
      * @param remove_semicolons                  Sets the corresponding field.
      * @param remove_quotation_marks             Sets the corresponding field.
      * @param remove_single_quotes               Sets the corresponding field.
      * @param remove_brackets                    Sets the corresponding field.
      * @param convert_ands_to_ampersands         Sets the corresponding field.
      * @param remove_thes                        Sets the corresponding field.
      * @param remove_spaces                      Sets the corresponding field.
      * @param check_word_ordering                Sets the corresponding field.
      * @param word_ordering_fraction_match       Sets the corresponding field.
      * @param check_word_subset                  Sets the corresponding field.
      * @param word_subset_fraction_match         Sets the corresponding field.
      * @param calculate_absolute_ED              Sets the corresponding field.
      * @param absolute_ED_threshold              Sets the corresponding field.
      * @param calculate_proportional_ED          Sets the corresponding field.
      * @param proportional_ED_threshold          Sets the corresponding field.
      * @param calculate_subset_ED                Sets the corresponding field.
      * @param subset_ED_threshold                Sets the corresponding field.
      * @param filter_duplicates_by_duration      Sets the corresponding field.
      * @param duration_filter_percent_max        Sets the corresponding field.
      * @param filter_duplicates_by_artist        Sets the corresponding field.
      * @param filter_duplicates_by_composer      Sets the corresponding field.
      * @param filter_duplicates_by_genre         Sets the corresponding field.
      * @param filter_duplicates_by_album         Sets the corresponding field.
      * @param list_mp3_files_could_not_parse     Sets the corresponding field.
      * @param list_all_non_mp3s_in_directory     Sets the corresponding field.
      * @param list_all_recordings_found          Sets the corresponding field.
      * @param list_files_in_XML_but_missing      Sets the corresponding field.
      * @param list_noncorresponding_recordings   Sets the corresponding field.
      * @param list_noncorresponding_fields       Sets the corresponding field.
      * @param list_all_postmerge_metadata        Sets the corresponding field.
      * @param list_sorted_recording_metadata     Sets the corresponding field.
      * @param report_artist_breakdown            Sets the corresponding field.
      * @param report_composer_breakdown          Sets the corresponding field.
      * @param report_genre_breakdown             Sets the corresponding field.
      * @param report_comment_breakdown           Sets the corresponding field.
      * @param list_artists_by_genre              Sets the corresponding field.
      * @param list_composers_by_genre            Sets the corresponding field.
      * @param report_missing_metadata            Sets the corresponding field.
      * @param list_artists_with_few_recordings   Sets the corresponding field.
      * @param cutoff_for_artists_few_recs        Sets the corresponding field.
      * @param list_composers_with_few_recordings Sets the corresponding field.
      * @param cutoff_for_composers_few_recs      Sets the corresponding field.
      * @param report_identical_titles            Sets the corresponding field.
      * @param report_starting_with_spaces        Sets the corresponding field.
      * @param list_albums_by_artist              Sets the corresponding field.
      * @param list_albums_by_composer            Sets the corresponding field.
      * @param list_incomplete_albums             Sets the corresponding field.
      * @param incoplete_albums_threshold         Sets the corresponding field.
      * @param list_albums_with_duplicate_tracks  Sets the corresponding field.
      * @param list_albums_missing_year           Sets the corresponding field.
      * @param report_on_compilation_albums       Sets the corresponding field.
      * @param report_differing_only_in_case      Sets the corresponding field.
      * @param report_all_find_replace_changes    Sets the corresponding field.
      * @param report_new_identicals_after_fr     Sets the corresponding field.
      * @param report_word_ordering_tests         Sets the corresponding field.
      * @param report_edit_distances              Sets the corresponding field.
      * @param list_titles_rejected_as_equivalent Sets the corresponding field.
      * @param report_probable_duplicates         Sets the corresponding field.
      * @param report_wrongly_differing_titles    Sets the corresponding field.
      * @param report_wrongly_differing_artists   Sets the corresponding field.
      * @param report_wrongly_differing_composers Sets the corresponding field.
      * @param report_wrongly_differing_albums    Sets the corresponding field.
      * @param report_wrongly_differing_genres    Sets the corresponding field.
      * @param report_options_selected            Sets the corresponding field.
      * @param report_processing_times            Sets the corresponding field.
      * @throws Exception                          An informative exception is
      *                                            thrown if logically invalid
      *                                            parameters are supplied.
      */
     public AnalysisPreferences(
          String iTunes_path,
          String mp3_path,
          String save_path,
          boolean perform_find_replace_operations,
          boolean ignore_case_in_edit_distances,
          boolean remove_numbers_at_title_beginnings,
          boolean convert_ings,
          boolean convert_title_abbreviations,
          boolean remove_periods,
          boolean remove_commas,
          boolean remove_hyphens,
          boolean remove_colons,
          boolean remove_semicolons,
          boolean remove_quotation_marks,
          boolean remove_single_quotes,
          boolean remove_brackets,
          boolean convert_ands_to_ampersands,
          boolean remove_thes,
          boolean remove_spaces,
          boolean check_word_ordering,
          double word_ordering_fraction_match,
          boolean check_word_subset,
          double word_subset_fraction_match,
          boolean calculate_absolute_ED,
          int absolute_ED_threshold,
          boolean calculate_proportional_ED,
          int proportional_ED_threshold,
          boolean calculate_subset_ED,
          int subset_ED_threshold,
          boolean filter_duplicates_by_duration,
          int duration_filter_percent_max,
          boolean filter_duplicates_by_artist,
          boolean filter_duplicates_by_composer,
          boolean filter_duplicates_by_genre,
          boolean filter_duplicates_by_album,
          boolean list_mp3_files_could_not_parse,
          boolean list_all_non_mp3s_in_directory,
          boolean list_all_recordings_found,
          boolean list_files_in_XML_but_missing,
          boolean list_noncorresponding_recordings,
          boolean list_noncorresponding_fields,
          boolean list_all_postmerge_metadata,
          boolean list_sorted_recording_metadata,
          boolean report_artist_breakdown,
          boolean report_composer_breakdown,
          boolean report_genre_breakdown,
          boolean report_comment_breakdown,
          boolean list_artists_by_genre,
          boolean list_composers_by_genre,
          boolean report_missing_metadata,
          boolean list_artists_with_few_recordings,
          int cutoff_for_artists_few_recs,
          boolean list_composers_with_few_recordings,
          int cutoff_for_composers_few_recs,
          boolean report_identical_titles,
          boolean report_starting_with_spaces,
          boolean list_albums_by_artist,
          boolean list_albums_by_composer,
          boolean list_incomplete_albums,
          int incoplete_albums_threshold,
          boolean list_albums_with_duplicate_tracks,
          boolean list_albums_missing_year,
          boolean report_on_compilation_albums,
          boolean report_differing_only_in_case,
          boolean report_all_find_replace_changes,
          boolean report_new_identicals_after_fr,
          boolean report_word_ordering_tests,
          boolean report_edit_distances,
          boolean list_titles_rejected_as_equivalent,
          boolean report_probable_duplicates,
          boolean report_wrongly_differing_titles,
          boolean report_wrongly_differing_artists,
          boolean report_wrongly_differing_composers,
          boolean report_wrongly_differing_albums,
          boolean report_wrongly_differing_genres,
          boolean report_options_selected,
          boolean report_processing_times )
          throws Exception
     {
          // Ensure that parameters are logically valid
          if (iTunes_path == null)
               throw new Exception("Path to iTunes file is null.");
          if (mp3_path == null)
               throw new Exception("Path to MP3 directory is null.");
          if (save_path == null)
               throw new Exception("Save path is null.");
          if (save_path.equals(""))
               throw new Exception("No save path specified.");
          if (iTunes_path.equals("") && mp3_path.equals(""))
               throw new Exception("No iTunes path specified and no MP3 directory specified.\n" +
                    "One or both must be specified.");
          
          // Validate and store the file references
          if (iTunes_path.equals(""))
               iTunes_file = null;
          else
          {
               iTunes_file = new File(iTunes_path);
               mckay.utilities.staticlibraries.FileMethods.validateFile(iTunes_file, true, false);
          }
          if (mp3_path.equals(""))
               mp3_directory = null;
          else
          {
               mp3_directory = new File(mp3_path);
               if (!mp3_directory.exists())
                    throw new Exception("The specified MP3 directory does not exist.");
               if (!mp3_directory.isDirectory() || !mp3_directory.canRead())
                    throw new Exception("The specified MP3 directory is not a directory\n" +
                         "that can be read.");
          }
          save_file = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(save_path, false);
          if (save_file == null)
               throw new Exception("Cannot write to specified save path.");
          
          // Set the preference fields
          this.perform_find_replace_operations = perform_find_replace_operations;
          this.ignore_case_in_edit_distances = ignore_case_in_edit_distances;
          this.remove_numbers_at_title_beginnings = remove_numbers_at_title_beginnings;
          this.convert_ings = convert_ings;
          this.convert_title_abbreviations = convert_title_abbreviations;
          this.remove_periods = remove_periods;
          this.remove_commas = remove_commas;
          this.remove_hyphens = remove_hyphens;
          this.remove_colons = remove_colons;
          this.remove_semicolons = remove_semicolons;
          this.remove_quotation_marks = remove_quotation_marks;
          this.remove_single_quotes = remove_single_quotes;
          this.remove_brackets = remove_brackets;
          this.convert_ands_to_ampersands = convert_ands_to_ampersands;
          this.remove_thes = remove_thes;
          this.remove_spaces = remove_spaces;
          this.check_word_ordering = check_word_ordering;
          this.word_ordering_fraction_match = word_ordering_fraction_match;
          this.check_word_subset = check_word_subset;
          this.word_subset_fraction_match = word_subset_fraction_match;
          this.calculate_absolute_ED = calculate_absolute_ED;
          this.absolute_ED_threshold = absolute_ED_threshold;
          this.calculate_proportional_ED = calculate_proportional_ED;
          this.proportional_ED_threshold = proportional_ED_threshold;
          this.calculate_subset_ED = calculate_subset_ED;
          this.subset_ED_threshold = subset_ED_threshold;
          this.filter_duplicates_by_duration = filter_duplicates_by_duration;
          this.duration_filter_percent_max = duration_filter_percent_max;
          this.filter_duplicates_by_artist = filter_duplicates_by_artist;
          this.filter_duplicates_by_composer = filter_duplicates_by_composer;
          this.filter_duplicates_by_genre = filter_duplicates_by_genre;
          this.filter_duplicates_by_album = filter_duplicates_by_album;
          this.list_mp3_files_could_not_parse = list_mp3_files_could_not_parse;
          this.list_all_non_mp3s_in_directory = list_all_non_mp3s_in_directory;
          this.list_all_recordings_found = list_all_recordings_found;
          this.list_files_in_XML_but_missing = list_files_in_XML_but_missing;
          this.list_noncorresponding_recordings = list_noncorresponding_recordings;
          this.list_noncorresponding_fields = list_noncorresponding_fields;
          this.list_all_postmerge_metadata = list_all_postmerge_metadata;
          this.list_sorted_recording_metadata = list_sorted_recording_metadata;
          this.report_artist_breakdown = report_artist_breakdown;
          this.report_composer_breakdown = report_composer_breakdown;
          this.report_genre_breakdown = report_genre_breakdown;
          this.report_comment_breakdown = report_comment_breakdown;
          this.list_artists_by_genre = list_artists_by_genre;
          this.list_composers_by_genre = list_composers_by_genre;
          this.report_missing_metadata = report_missing_metadata;
          this.list_artists_with_few_recordings = list_artists_with_few_recordings;
          this.cutoff_for_artists_few_recs = cutoff_for_artists_few_recs;
          this.list_composers_with_few_recordings = list_composers_with_few_recordings;
          this.cutoff_for_composers_few_recs = cutoff_for_composers_few_recs;
          this.report_identical_titles = report_identical_titles;
          this.report_starting_with_spaces = report_starting_with_spaces;
          this.list_albums_by_artist = list_albums_by_artist;
          this.list_albums_by_composer = list_albums_by_composer;
          this.list_incomplete_albums = list_incomplete_albums;
          this.incoplete_albums_threshold = incoplete_albums_threshold;
          this.list_albums_with_duplicate_tracks = list_albums_with_duplicate_tracks;
          this.list_albums_missing_year = list_albums_missing_year;
          this.report_on_compilation_albums = report_on_compilation_albums;
          this.report_differing_only_in_case = report_differing_only_in_case;
          this.report_all_find_replace_changes = report_all_find_replace_changes;
          this.report_new_identicals_after_fr = report_new_identicals_after_fr;
          this.report_word_ordering_tests = report_word_ordering_tests;
          this.report_edit_distances = report_edit_distances;
          this.list_titles_rejected_as_equivalent = list_titles_rejected_as_equivalent;
          this.report_probable_duplicates = report_probable_duplicates;
          this.report_wrongly_differing_titles = report_wrongly_differing_titles;
          this.report_wrongly_differing_artists = report_wrongly_differing_artists;
          this.report_wrongly_differing_composers = report_wrongly_differing_composers;
          this.report_wrongly_differing_albums = report_wrongly_differing_albums;
          this.report_wrongly_differing_genres = report_wrongly_differing_genres;
          this.report_options_selected = report_options_selected;
          this.report_processing_times = report_processing_times;
     }
}
