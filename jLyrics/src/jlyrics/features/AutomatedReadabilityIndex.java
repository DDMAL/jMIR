/*
 * AutomatedReadabilityIndex.java
 * Version 1.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jlyrics.features;

import ace.datatypes.FeatureDefinition;
import jlyrics.datatypes.SongLyrics;

/**
 * A feature extractor that extracts the Automated Readability Index. This index is designed to provide a
 * measure of the understandability of a text, and provides a result that is roughly equivalent to a U.S.
 * grade level. More information is available at en.wikipedia.org/wiki/Automated_Readability_Index.
 *
 * @author Cory McKay
 */
public class AutomatedReadabilityIndex
	extends LyricsFeatureExtractor
{
	/* CONSTRUCTOR ******************************************************************************************/

	/**
	 * Basic constructor that sets the class's inherited fields.
	 */
	public AutomatedReadabilityIndex()
	{
		// Specify descriptive metadata about the function
		String name = "Automated Readability Index";
		String description = "This index is designed to provide a measure of the understandability of a text," +
				" and provides a result that is roughly equivalent to a U.S. grade level. More information" +
				" is available at en.wikipedia.org/wiki/Automated_Readability_Index";
		boolean is_sequential = false;
		int dimensions = 1;

		// Specify features used in calculating this feature
		String[] features_used = {"Number of Words", "Number of Lines"};

		// Set the superclass's fields appropriately
		feature_description = new FeatureDefinition(name, description, is_sequential, dimensions);
		feature_dependencies = features_used;
		external_dependencies = null;
	}


	/* PUBLIC METHODS ***************************************************************************************/


	/**
	 * Extract this feature from given song lyrics.
	 *
	 * @param lyrics				The lyrics to extract the feature from.
	 * @param other_feature_values	The values of other features that are needed to calculate this value. The
	 *								order of these features must be the same as those returned by this class's
	 *								getFeatureDepenedencies method. The first indice indicates the feature and
	 *								the second indicates the value (in order to allow for multi-dimensional
	 *								features).
	 * @param external_information	Ignored (not needed to calculate this feature).
	 * @return						The extracted feature value. Null if the feature cannot be extracted
	 *								from the given data.
	 * @throws Exception			Throws an informative exception if invalid data is provided.
	 */
	public double[] extractFeature(SongLyrics lyrics,
			double[][] other_feature_values,
			Object[][] external_information)
		throws Exception
	{
		// Throw an exception if no lyrics data is provided
		if (lyrics == null) throw new Exception("No lyrics data provided.");

		// Prepare the result and default it to 0
		double[] result = new double[1];
		result[0] = 0.0;

		// Return 0 if there are no words
		if (!lyrics.isLyricsAvailable())
			return result;

		// Prepare the numbers of words and lines
		double number_of_words = other_feature_values[0][0];
		double number_of_lines = other_feature_values[1][0];

		// Calculate the number of non-whitespace characters
		int number_of_characters = 0;
		String[] words = lyrics.getWords();
		for (int i = 0; i < words.length; i++)
			number_of_characters += words[i].length();

		// Calculate the ARI
		result[0] = 4.71 * (number_of_characters / number_of_words) + 0.5 * (number_of_words / number_of_lines) - 21.43;

		// Return the result
		return result;
	}
}