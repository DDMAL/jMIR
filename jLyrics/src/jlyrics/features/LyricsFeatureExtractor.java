/*
 * LyricsFeatureExtractor.java
 * Version 1.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jlyrics.features;

import ace.datatypes.FeatureDefinition;
import jlyrics.datatypes.SongLyrics;

/**
 * The feature extractor prototype. Each class that extends this class will extract a particular feature from
 * the lyrics of a song.
 *
 * <p>Classes that extend this class should have a constructor that sets the three protected fields of this
 * class.
 *
 * @author Cory McKay
 */
public abstract class LyricsFeatureExtractor
{
	/* FIELDS ***********************************************************************************************/


	/**
	 * Meta-data describing a feature.
	 */
	protected FeatureDefinition feature_description;

	/**
	 * The names of other features that are needed in order for a feature to be calculated. Will be null if
	 * there are no dependencies.
	 */
	protected String[] feature_dependencies;

	/**
	 * Additional data of some kind that can be used to calculate a feature. Will be null if no such data is
	 * needed.
	 */
	protected String[] external_dependencies;


	/* PUBLIC METHODS ***************************************************************************************/


	/**
	 * Returns metadata describing this feature.
	 *
	 * <p><b>IMPORTANT:</b> Note that a value of 0 in the returned dimensions of the FeatureDefinition implies
	 * that the feature dimensions are variable, and depend on the analyzed data.
	 */
	public FeatureDefinition getFeatureDescription()
	{
		return feature_description;
	}


	/**
	 * Returns the names of other features that are needed in order to extract this feature. Will return null
	 * if no other features are needed.
	 */
	public String[] getFeatureDepenedencies()
	{
		return feature_dependencies;
	}


	/**
	 * Returns identifiers for external data of some kind thet is needed to extract this feature. Will return
	 * null if no other features are needed.
	 */
	public String[] getExternalDepenedencies()
	{
		return external_dependencies;
	}


	/**
	 * The prototype function that classes extending this class will override in order to extract their
	 * feature from song lyrics.
	 *
	 * @param lyrics				The lyrics to extract the feature from.
	 * @param other_feature_values	The values of other features that are needed to calculate this value. The
	 *								order of these features must be the same as those returned by this class's
	 *								getFeatureDepenedencies method. The first indice indicates the feature and
	 *								the second indicates the value (in order to allow for multi-dimensional
	 *								features).
	 * @param external_information	Additional data of some kind that can be used to calculate a feature. The
	 *								order of this data must be the same as that returned by this class's
	 *								getExternalDependencies method. The first indice indicates the dependency
	 *								and the second indicates the value.
	 * @return						The extracted feature value. Null if the feature cannot be extracted
	 *								from the given data.
	 * @throws Exception			Throws an informative exception if invalid data is provided.
	 */
	public abstract double[] extractFeature(SongLyrics lyrics,
			double[][] other_feature_values,
			Object[][] external_information)
		throws Exception;
}