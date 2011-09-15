/*
 * NumberOfUniqueWords.java
 * Version 1.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jlyrics.features;

import ace.datatypes.FeatureDefinition;
import jlyrics.datatypes.SongLyrics;

/**
 * A feature extractor that extracts the total number of unique words in a text. This means that words that
 * occur more than once are not counted twice. Whitespace characters are not counted as words.
 *
 * @author Cory McKay
 */
public class NumberOfUniqueWords
	extends LyricsFeatureExtractor
{
	/* CONSTRUCTOR ******************************************************************************************/

	/**
	 * Basic constructor that sets the class's inherited fields.
	 */
	public NumberOfUniqueWords()
	{
		// Specify descriptive metadata about the function
		String name = "Number of Unique Words";
		String description = "The total number unique of words in the text. This means that words that " +
				"occur more than once are not counted twice. Whitespace characters are not counted as words.";
		boolean is_sequential = false;
		int dimensions = 1;

		// Set the superclass's fields appropriately
		feature_description = new FeatureDefinition(name, description, is_sequential, dimensions);
		feature_dependencies = null;
		external_dependencies = null;
	}


	/* PUBLIC METHODS ***************************************************************************************/


	/**
	 * Extract this feature from given song lyrics.
	 *
	 * @param lyrics				The lyrics to extract the feature from.
	 * @param other_feature_values	Ignored (not needed to calculate this feature).
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

		// Get the counts for each unique word
		int[] word_counts = lyrics.getUniqueWordCounts();

		// Count the number of unique words
		result[0] = word_counts.length;

		// Return the result
		return result;
	}
}