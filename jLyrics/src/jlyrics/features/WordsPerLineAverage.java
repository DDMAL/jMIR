/*
 * WordsPerLineAverage.java
 * Version 1.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jlyrics.features;

import ace.datatypes.FeatureDefinition;
import jlyrics.datatypes.SongLyrics;

/**
 * A feature extractor that extracts the average number of words per line. Blank lines are not included in
 * this calculation.
 *
 * @author Cory McKay
 */
public class WordsPerLineAverage
	extends LyricsFeatureExtractor
{
	/* CONSTRUCTOR ******************************************************************************************/

	/**
	 * Basic constructor that sets the class's inherited fields.
	 */
	public WordsPerLineAverage()
	{
		// Specify descriptive metadata about the function
		String name = "Words Per Line Average";
		String description = "The average number of words per line. Blank lines are not included in this " +
				"calculation.";
		boolean is_sequential = false;
		int dimensions = 1;

		// Specify features used in calculating this feature
		String[] features_used = {"Number of Words"};

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

		// Get the number of lines
		String[] lines = lyrics.getSongLines();

		// Calculate the feature value
		result[0] = (double) other_feature_values[0][0] / (double) lines.length;

		// Return the result
		return result;
	}
}