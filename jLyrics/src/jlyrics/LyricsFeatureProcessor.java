/*
 * LyricsFeatureProcessor.java
 * Version 1.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jlyrics;

import java.io.File;
import java.io.FileFilter;
import java.util.Vector;
import ace.datatypes.DataSet;
import ace.datatypes.FeatureDefinition;
import jlyrics.datatypes.SongLyrics;
import jlyrics.features.*;

/**
 * A library of static methods that can be used to extract features from text files containing lyrics.
 *
 * @author Cory McKay
 */
public class LyricsFeatureProcessor
{
	/**
	 * Throws an exception if a file cannot be written to the specified file path. Possible causes could be
	 * that no path is specified or a file already exists at that path.
	 * 
	 * @param file_path			The file path to check.
	 * @param permit_overwrite	Whether or not existing files at the given path may be overwritten.
	 * @throws Exception		An informative exception is thrown if the specified path cannot be written to.
	 */
	public static void verifyCanSave(String file_path, boolean permit_overwrite)
		throws Exception
	{
		if (file_path == null) throw new Exception("No save path specified.");
		if (file_path.equals("")) throw new Exception("No save path specified.");

		if (!permit_overwrite)
		{
			File test_file = new File(file_path);
			if (test_file.exists()) throw new Exception("A file at the path " + file_path + " already exists.");
		}
	}


	/**
	 * Searches through the specified directory and its sub-directories in order to find all files with the
	 * ".txt" extension. All of these files are then parsed into SongLyrics objects. Files with other
	 * extensions are ignored. Each SongLyrics object is given an identifier equal to its filename (not its
	 * path).
	 *
	 * @param directory_path	The directory to search for files to extract lyrics from.
	 * @return					One SongLyrics object for each lyrics file found.
	 * @throws Exception		Throws an informative exception if a problem occurs during parsing or if
	 *							no appropriate lyrics files could be found.
	 */
	public static SongLyrics[] parseSongLyrics(String directory_path)
		throws Exception
	{
		// Define the extensions that files must have in order to be parsed
		String[] acceptable_extensions = {"txt"};
		FileFilter file_filter = new mckay.utilities.general.FileFilterImplementation(acceptable_extensions);

		// Parse the given directory recursively to find files to extract features from. An exception is
		// thrown if a problem is encountered.
		File directory = new File(directory_path);
		File[] files_to_parse = mckay.utilities.staticlibraries.FileMethods.getAllFilesInDirectory(directory,
				true, file_filter, null);

		// Throw an exception if no appropriate files were found
		if (files_to_parse == null)
			throw new Exception("No valid files to parse could be found in the " + directory_path + " directory.");

		// Create a SongLyrics object for each file.
		Vector<SongLyrics> all_songs = new Vector<SongLyrics>();
		for (int i = 0; i < files_to_parse.length; i++)
		{
			SongLyrics this_song = new SongLyrics(mckay.utilities.staticlibraries.StringMethods.removeExtension(files_to_parse[i].getName()),
					files_to_parse[i].getAbsolutePath());
			all_songs.add(this_song);
		}

		// Throw an exception if no appropriate files were found
		if (all_songs.isEmpty())
			throw new Exception("No valid files to parse could be found in the " + directory_path + " directory.");

		// Return all songs
		return all_songs.toArray(new SongLyrics[1]);
	}


	/**
	 * Return the set of features to extract from lyrics data.
	 *
	 * @return				The feature extractors to apply to lyrics data.
	 * @throws Exception	Throws an informative Exception if a problem occurs.
	 */
	public static LyricsFeatureExtractor[] populateFeatureExtractorsToApply()
		throws Exception
	{
		Vector<LyricsFeatureExtractor> extractors = new Vector<LyricsFeatureExtractor>();

		extractors.add(new ContainsWords());
		extractors.add(new NumberOfWords());
		extractors.add(new NumberOfUniqueWords());
		extractors.add(new WordVariety());
		extractors.add(new LettersPerWordAverage());
		extractors.add(new LettersPerWordVariance());
		extractors.add(new NumberOfLines());
		extractors.add(new WordsPerLineAverage());
		extractors.add(new WordsPerLineVariance());
		extractors.add(new NumberOfSegments());
		extractors.add(new LinesPerSegmentAverage());
		extractors.add(new LinesPerSegmentVariance());
		extractors.add(new AutomatedReadabilityIndex());

		String[] blues_traditional_keywords = {"ain't", "baby", "baby,", "blues", "boy", "child", "child,", "evil", "gonna", "hot", "lord,", "love", "mojo", "oh", "old", "red", "tamales", "working", "worried", "yeah"};
		extractors.add(new WordProfileMatch("Traditional Blues", blues_traditional_keywords));
		String[] blues_modern_keywords = {"'cause", "ain't", "baby", "baby,", "bad", "blues", "down", "feel", "gonna", "little", "lord", "love", "shame", "wanna", "woman", "wrong", "yeah", "yeah,"};
		extractors.add(new WordProfileMatch("Modern Blues", blues_modern_keywords));
		String[] classical_keywords = {"auf", "bist", "dein", "dich", "die", "dies", "du", "dum", "et", "finde", "gott", "herz", "ich", "ihn", "ist", "la", "le", "mein", "mi", "mir", "nach", "nicht", "thy", "und"};
		extractors.add(new WordProfileMatch("Classical", classical_keywords));
		String[] jazz_keywords = {"ain't", "baby", "blue", "cheek", "good", "heart", "love", "never", "please", "right"};
		extractors.add(new WordProfileMatch("Jazz", jazz_keywords));
		String[] rap_keywords = {"'em", "ain't", "ass", "bitch", "cuz", "fuck", "gonna", "gotta", "hit", "like", "money", "nigga", "niggas", "niggaz", "shit", "wanna", "wit", "y'all", "ya", "yeah", "yo"};
		extractors.add(new WordProfileMatch("Rap", rap_keywords));
		String[] metal_keywords = {"'cause", "ain't", "anger", "battery", "bleed", "blood", "dead", "death", "die", "end", "eyes", "fear", "fire", "god", "gonna", "hate", "heaven's", "human", "kill", "lie", "life", "night", "oh", "ooh", "shoot", "tick", "world", "yeah", "yeah!"};
		extractors.add(new WordProfileMatch("Metal", metal_keywords));

		return extractors.toArray(new LyricsFeatureExtractor[1]);
	}


	/**
	 * Returns an array containing the same feature extractors as those provided, but reordered such that
	 * all features that require other features to be extracted will be extracted after those features.
	 *
	 * @param unordered_feature_extractors	The unordered feature extractors to order. This is not changed
	 *										during processing.
	 * @return								The properly ordered feature extractors. The feature extractors
	 *										that appear first should be extracted frist.
	 */
	public static LyricsFeatureExtractor[] findOrderedFeatureExtractors(LyricsFeatureExtractor[] unordered_feature_extractors)
	{
		// Find the names of all features
		String[] all_feature_names = new String[unordered_feature_extractors.length];
		for (int feat = 0; feat < unordered_feature_extractors.length; feat++)
			all_feature_names[feat] = unordered_feature_extractors[feat].getFeatureDescription().name;

		// Find the dependencies of all features marked to be extracted
		String[][] dependencies = new String[unordered_feature_extractors.length][];
		for (int feat = 0; feat < unordered_feature_extractors.length; feat++)
			dependencies[feat] = unordered_feature_extractors[feat].getFeatureDepenedencies();

		// Find the correct order to extract features in by filling the
		// ordered_feature_extractors field
		LyricsFeatureExtractor[] ordered_feature_extractors = new LyricsFeatureExtractor[unordered_feature_extractors.length];
		boolean[] feature_added = new boolean[unordered_feature_extractors.length];
		for (int i = 0; i < feature_added.length; i++)
			feature_added[i] = false;
		int current_position = 0;
		boolean done = false;
		while (!done)
		{
			done = true;

			// Add all features that have no remaining dependencies and remove
			// their dependencies from all unadded features
			for (int feat = 0; feat < dependencies.length; feat++)
			{
				if (!feature_added[feat] && dependencies[feat] == null) // add feature if it has no dependencies
				{
					feature_added[feat] = true;
					ordered_feature_extractors[current_position] = unordered_feature_extractors[feat];
					current_position++;
					done = false;

					// Remove this dependency from all features that have
					// it as a dependency and are marked to be extracted
					for (int i = 0; i < dependencies.length; i++)
					{
						if (dependencies[i] != null)
						{
							int num_defs = dependencies[i].length;
							for (int j = 0; j < num_defs; j++)
							{
								if (dependencies[i][j].equals(all_feature_names[feat]))
								{
									if (dependencies[i].length == 1)
									{
										dependencies[i] = null;
										j = num_defs;
									}
									else
									{
										String[] temp = new String[dependencies[i].length - 1];
										int m = 0;
										for (int k = 0; k < dependencies[i].length; k++)
										{
											if (k != j)
											{
												temp[m] = dependencies[i][k];
												m++;
											}
										}
										dependencies[i] = temp;
										j--;
										num_defs--;
									}
								}
							}
						}
					}
				}
			}
		}

		// Return the correctly ordered feature extractors
		return ordered_feature_extractors;
	}


	/**
	 * Goes through the given array of feature extractors and determines which features are dependent on
	 * which other features in terms of the indices of this given feature_extractors array.
	 *
	 * @param feature_extractors	The feature extractors to extract dependencies from.
	 * @return						The dependencies of the features in the given feature_extractors. The
	 *								first indice corresponds to the feature_extractors indice and the second
	 *								identifies the number of the dependent feature. The entry identifies the
	 *								indice of the feature in feature_extractors that corresponds to a
	 *								dependant feature. The first dimension is set to null if there are no
	 *								dependent features.
	 */
	public static int[][] findFeatureExtractorDependencies(LyricsFeatureExtractor[] feature_extractors)
	{
		int[][] dependencies = new int[feature_extractors.length][];
		String[] feature_names = new String[feature_extractors.length];
		for (int feat = 0; feat < feature_names.length; feat++)
			feature_names[feat] = feature_extractors[feat].getFeatureDescription().name;
		String[][] feature_dependencies_str = new String[feature_extractors.length][];
		for (int feat = 0; feat < feature_dependencies_str.length; feat++)
			feature_dependencies_str[feat] = feature_extractors[feat].getFeatureDepenedencies();
		for (int i = 0; i < feature_dependencies_str.length; i++)
		{
			if (feature_dependencies_str[i] != null)
			{
				dependencies[i] = new int[feature_dependencies_str[i].length];
				for (int j = 0; j < feature_dependencies_str[i].length; j++)
				{
					for (int k = 0; k < feature_names.length; k++)
					{
						if (feature_dependencies_str[i][j].equals(feature_names[k]))
							dependencies[i][j] = k;
					}
				}
			}
		}
		return dependencies;
	}


	/**
	 * Extract all of the specified features from all of the specified songs.
	 *
	 * @param song_lyrics			The lyrics from which to extract features.
	 * @param features_to_extract	The particular features to extract. They will be extracted in the order
	 *								in which they appear.
	 * @return						The extracted feature values. The first indice identifies the song, the
	 *								second identifies the feature and the third identifies the feature value.
	 *								The third dimension will be null if the given feature could not be
	 *								extracted for the given window.
	 * @throws Exception
	 */
	public static double[][][] extractFeatures(SongLyrics[] song_lyrics,
			LyricsFeatureExtractor[] features_to_extract)
		throws Exception
	{
		// Determine feature dependencies
		int[][] feature_dependencies = LyricsFeatureProcessor.findFeatureExtractorDependencies(features_to_extract);

		// Extract the feature values
		double[][][] feature_values = new double[song_lyrics.length][features_to_extract.length][];
		for (int song = 0; song < song_lyrics.length; song++)
		{
			for (int feat = 0; feat < features_to_extract.length; feat++)
			{
				// Find previously extracted feature values that this feature needs, if any
				double[][] other_feature_values = null;
				if (feature_dependencies[feat] != null)
				{
					other_feature_values = new double[feature_dependencies[feat].length][];
					for (int i = 0; i < feature_dependencies[feat].length; i++)
					{
						int feature_indice = feature_dependencies[feat][i];
						other_feature_values[i] = feature_values[song][feature_indice];
					}
				}

				// Extract the feat feature for song
				feature_values[song][feat] = features_to_extract[feat].extractFeature(song_lyrics[song], other_feature_values, null);
			}
		}

		// Return the feature values
		return feature_values;
	}


	/**
	 * Return ACE DataSet objects for all of the extracted features.
	 *
	 * @param song_lyrics			The songs from which features were extracted.
	 * @param features_extracted	The features that were extracted.
	 * @param feature_values		The extracted feature values.  The first indice identifies the song, the
	 *								second identifies the feature and the third identifies the feature value.
	 *								The third dimension will be null if the given feature could not be
	 *								extracted for the given window.
	 * @return						DataSet objects representing the extracted features.
	 */
	public static DataSet[] prepareFeatureValues(SongLyrics[] song_lyrics,
			LyricsFeatureExtractor[] features_extracted,
			double[][][] feature_values)
	{
		// Prepare the feature names
		String[] feature_names = new String[features_extracted.length];
		for (int i = 0; i < features_extracted.length; i++)
			feature_names[i] = features_extracted[i].getFeatureDescription().name;

		// Store data in the DataSet objects
		DataSet[] results = new DataSet[song_lyrics.length];
		for (int i = 0; i < song_lyrics.length; i++)
		{
			String identifier = song_lyrics[i].getIdentifier();
			DataSet[] sub_sets = null;
			Double start = java.lang.Double.NaN;
			Double stop = java.lang.Double.NaN;
			double[][] values = feature_values[i];
			DataSet parent = null;

			results[i] = new DataSet(identifier, sub_sets, start, stop, values, feature_names, parent);
		}

		// Return the ACE objects
		return results;
	}


	/**
	 * Return ACE FeatureDefinition objects for each of the features to be extracted.
	 *
	 * @param features	The features to be extracted.
	 * @return			FeatureDefinitions for the features.
	 */
	public static FeatureDefinition[] prepareFeatureDescriptions(LyricsFeatureExtractor[] features)
	{
		FeatureDefinition[] feature_descriptions = new FeatureDefinition[features.length];
		for (int i = 0; i < feature_descriptions.length; i++)
			feature_descriptions[i] = features[i].getFeatureDescription();
		return feature_descriptions;
	}
}