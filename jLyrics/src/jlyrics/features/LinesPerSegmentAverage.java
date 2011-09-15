/*
 * LinesPerSegmentAverage.java
 * Version 1.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jlyrics.features;

import ace.datatypes.FeatureDefinition;
import jlyrics.datatypes.SongLyrics;

/**
 * A feature extractor that extracts the average number of lines per segment (e.g. verses, choruses, etc.) in
 * the text. Segments are assumed to be segmented by blank lines, and lines are segmented by line breaks.
 * Lines consisting only of line breaks are filtered out. This calculation does not include lines consisting
 * only of line breaks.
 *
 * @author Cory McKay
 */
public class LinesPerSegmentAverage
	extends LyricsFeatureExtractor
{
	/* CONSTRUCTOR ******************************************************************************************/

	/**
	 * Basic constructor that sets the class's inherited fields.
	 */
	public LinesPerSegmentAverage()
	{
		// Specify descriptive metadata about the function
		String name = "Lines Per Segment Average";
		String description = "The average number of lines per segments (e.g. verses, choruses, etc.) in the" +
				" text. Segments are assumed to be segmented by blank lines, and lines are segmented by line" +
				" breaks. Lines consisting only of line breaks are filtered out. This count does not include" +
				" lines consisting only of line breaks.";
		boolean is_sequential = false;
		int dimensions = 1;

		// Specify features used in calculating this feature
		String[] features_used = {"Number of Lines", "Number of Segments"};

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

		// Calculate the results
		result[0] = other_feature_values[0][0] / other_feature_values[1][0];

		// Return the result
		return result;
	}
}