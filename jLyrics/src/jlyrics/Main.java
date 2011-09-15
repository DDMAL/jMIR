/*
 * Main.java
 * Version 1.1
 *
 * Last modified on July 26, 2010.
 * McGill University
 */
package jlyrics;

import java.io.File;
import ace.datatypes.DataBoard;
import ace.datatypes.DataSet;
import ace.datatypes.FeatureDefinition;
import jlyrics.datatypes.SongLyrics;
import jlyrics.features.LyricsFeatureExtractor;

/**
 * A command line tool for extracting features from musical lyrics. Functionality is also included for
 * extracting word profiles from sets of files, which consist of ordered lists of the words that occur the
 * most frequently. These profiles may be generated for all files, or based on class label filters.
 *
 * <p>The user specifies a directory containing .txt files that each contains the lyrics for one song. All of
 * these are then automatically parsed into jlyrics.datatypes.SongLyrics objects, where they are stored as a
 * raw string, as words, as lines (parsed based on line returns), as structural segments (parsed based on
 * separation by blank lines) and as frequency counts for each unique word.
 *
 * <p>The features in the jlyrics.features package are then all extracted for each song and saved as
 * ACE XML 1.1 Feature Value and Feature Definition files, each of which are saved at locations specified by
 * the user. Results may alternatively be saved as a Weka ARFF file if preferred.
 *
 * <p>Additional features may be added by extending the jlyrics.features.LyricsFeatureExtractor abstract
 * class. A reference to the new feature must also be added to the populateFeatureExtractorsToApply method
 * in the jlyrics.LyricsFeatureProcessor class.
 *
 * <p>The command line arguments may be as follows:
 *
 * <li>1) Directory path to parse for lyrics files
 * <li>2) File path to save ACE XML 1.1 Feature Values file to
 * <li>3) File path to save ACE XML 1.1 Feature Definitions file to
 *
 * <p>OR
 *
 * <li>1) Directory path to parse for lyrics files
 * <li>2) File path to save Weka ARFF file to
 *
 * <p>OR (for generating profiles)
 * 
 * <li>1) -profile
 * <li>2) Directory path to parse for lyrics files
 * <li>3) Path to save generated report to
 *
 * <p>OR (for generating profiles)
 * 
 * <li>1) -profile
 * <li>2) Directory path to parse for lyrics files
 * <li>3) Path of a file specifying unique classes
 * <li>4) Path of a file specifying instance classes
 * <li>5) Path of a file specifying instance identifiers
 * <li>6) Path to save generated reports to
 * 
 * @author Cory McKay
 */
public class Main
{
	/* FIELDS ***********************************************************************************************/


	/**
	 * Whether or not existing ACE XML files at the given path may be overwritten. Does not apply to Weka ARFF
	 * files or profiling reports.
	 */
	private static final boolean allow_output_file_overwrites = true;


	/* PUBLIC STATIC METHODS ********************************************************************************/


	/**
	 * The main method. Parses command line arguments and performs requested actions
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		try
		{
			// Output an error message if an improper number of command line arguments are specified
			if (args.length != 3 && args.length != 2 && args.length != 6)
				throw new Exception("jLyrics must be run with eith two, three or six arguments, in one " +
						"of the following orders:\n\n" +
						"1) Directory path to parse for lyrics files\n" +
						"2) File path to save ACE XML 1.1 Feature Values file to\n" +
						"3) File path to save ACE XML 1.1 Feature Definitions file to\n" +
						"\nOR\n\n" +
						"1) Directory path to parse for lyrics files\n" +
						"2) File path to save Weka ARFF file to\n" +
						"\nOR\n\n" +
						"1) -profile\n" +
						"2) Directory path to parse for lyrics files\n" +
						"3) Path to save generated report to\n" +
						"\nOR\n\n" +
						"1) -profile\n" +
						"2) Directory path to parse for lyrics files\n" +
						"3) Path of a file specifying unique classes\n" +
						"4) Path of a file specifying instance classes\n" +
						"5) Path of a file specifying instance identifiers\n" +
						"6) Path to save generated reports to");

			// Generate profiling reports
			if (args[0].equals("-profile"))
			{
				if (args.length == 3)
				{
					System.out.println("Preparing profiling report...");

					String report = LyricsClassProfiler.buildWordFrequencyProfile(args[1]);
					LyricsClassProfiler.writeReport(args[2], report);

					System.out.println("Processing complete.");
				}
				else if (args.length == 6)
				{
					System.out.println("Preparing profiling reports...");
					String[] reports = LyricsClassProfiler.buildWordFrequencyProfile(args[1], args[2],
							args[3], args[4]);

					System.out.println("Writing profiling reports...");
					for (int i = 0; i < reports.length; i++)
					{
						String save_path = args[5];
						String extension = mckay.utilities.staticlibraries.StringMethods.getExtension(save_path);
						String without_extension = mckay.utilities.staticlibraries.StringMethods.removeExtension(save_path);
						String new_path = without_extension + "_" + (i+1) + extension;
						
						LyricsClassProfiler.writeReport(new_path, reports[i]);
					}

					System.out.println("Processing complete.");
				}
			}

			// Extract features
			else
			{
				// Store the command line arguments
				String directory_path_to_parse = args[0];
				String feature_values_path_to_save_to = args[1];
				String feature_definitions_path_to_save_to = null;
				if (args.length == 3)
					feature_definitions_path_to_save_to = args[2];

				// Verify the Feature Value and Feature Definition save paths are valid
				LyricsFeatureProcessor.verifyCanSave(feature_values_path_to_save_to, allow_output_file_overwrites);
				if (args.length == 3)
					LyricsFeatureProcessor.verifyCanSave(feature_definitions_path_to_save_to, allow_output_file_overwrites);

				// Parse lyrics files
				System.out.println("Parsing lyrics...");
				SongLyrics[] song_lyrics = LyricsFeatureProcessor.parseSongLyrics(directory_path_to_parse);

				/* DEBUGGING
				// Output all data parsed into the SongLyrics objects to standard out
				for (int i = 0; i < song_lyrics.length; i++)
					song_lyrics[i].printContents();
				*/

				// Get the features to extract in the order that they should be extracted
				LyricsFeatureExtractor[] features_to_extract = LyricsFeatureProcessor.populateFeatureExtractorsToApply();
				features_to_extract = LyricsFeatureProcessor.findOrderedFeatureExtractors(features_to_extract);

				// Extract feature values
				System.out.println("Extracting features...");
				double[][][] feature_values = LyricsFeatureProcessor.extractFeatures(song_lyrics, features_to_extract);

				// Save the results
				System.out.println("Writing extracted feature values...");
				FeatureDefinition[] feature_descriptions = LyricsFeatureProcessor.prepareFeatureDescriptions(features_to_extract);
				DataSet[] formatted_feature_values = LyricsFeatureProcessor.prepareFeatureValues(song_lyrics, features_to_extract, feature_values);
				if (args.length == 3) // save as ACE XML
				{
					FeatureDefinition.saveFeatureDefinitions(feature_descriptions, new File(feature_definitions_path_to_save_to), "Generated by jLyrics.");
					DataSet.saveDataSets(formatted_feature_values, feature_descriptions, new File(feature_values_path_to_save_to), "Generated by jLyrics.");
				}
				else if (args.length == 2) // save as Weka ARFF
				{
					DataBoard to_save = new DataBoard(null, feature_descriptions, formatted_feature_values, null);
					to_save.saveToARFF("Extracted lyric features", new File(feature_values_path_to_save_to), true, true);
				}

				// Note that processing is complete
				System.out.println("Processing complete.");
			}
		}

		// Output an informative error message to standard out
		catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			// e.printStackTrace();
		}
	}
}