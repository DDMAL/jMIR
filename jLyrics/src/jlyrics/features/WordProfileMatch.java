/*
 * WordProfileMatch.java
 * Version 1.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jlyrics.features;

import ace.datatypes.FeatureDefinition;
import jlyrics.datatypes.SongLyrics;
import java.util.HashMap;

/**
 * A feature extractor that calculates the percentage of words in the given text that are found in an
 * externally provided list of words. This percentage is based on all words present in the text, not just the
 * unique words. Case is ignored.
 *
 * <p>The externally defined list of words is provided in a special constructor that is not found in other
 * LyricsFeatureExtractors classes. This specialized constructor must be used for instantiating
 * WordProfileMatch rather than the standard LyricsFeatureExtractors-style constructor.
 *
 * @author Cory McKay
 */
public class WordProfileMatch
	extends LyricsFeatureExtractor
{
	/* FIELDS ***********************************************************************************************/


	/**
	 * The set of words that are used to calculate feature values. These keywords specify the profile that
	 * texts are compared to. This is not null, and may not be empty.
	 */
	private String[] keywords;


	/* CONSTRUCTORS *****************************************************************************************/


	/**
	 * A disallowed constructor that throws an Exception if it is called.
	 *
	 * @throws Exception	Notes that this constructor should not be used.
	 */
	public WordProfileMatch()
		throws Exception
	{
		throw new Exception("WordProfileMatch object instantiated with a disallowed constructor.");
	}


	/**
	 * Basic constructor that sets the class's inherited fields and stores the provided keywords.
	 *
	 * @param name_addendum An addendum that is added to the feature name to identify it with the profile
	 *						associated with the specified keywords. This must not be null.
	 * @param keywords		The set of words that will be used to calculate feature values. These keywords
	 *						specify	the profile that texts are compared to. This must not be null, and may
	 *						not be empty.
	 * @throws Exception	Throws an Exception if invalid input is provided.
	 */
	public WordProfileMatch(String name_addendum, String[] keywords)
		throws Exception
	{
		// Throw an exception if a null name_addendum is provided
		if (name_addendum == null) throw new Exception("No feature name addendum provided.");

		// Throw an exception if no or invalid keyword data is provided
		if (keywords == null) throw new Exception("No keywords provided.");
		if (keywords.length == 0) throw new Exception("No keywords provided.");
		for (int i = 0; i < keywords.length; i++)
		{
			if (keywords[i] == null) throw new Exception("An empty keyword provided.");
			if (keywords[i].equals("")) throw new Exception("An empty keyword provided.");
		}

		// Specify descriptive metadata about the function
		String name = "Word Profile Match " + name_addendum;
		boolean is_sequential = false;
		int dimensions = 1;

		// Specify the feature description
		String description = "The percentage of words in the text that are found in a list of keywords. This" +
				" percentage is based on all words present in the text, not just the unique words. Case is" +
				" ignored. The keywords are:";
		StringBuffer description_addendum = new StringBuffer();
		for (int i = 0; i < keywords.length; i++)
		{
			description_addendum.append(" ");
			description_addendum.append(keywords[i]);
		}
		description_addendum.append(".");
		description += description_addendum.toString();

		// Specify features used in calculating this feature
		String[] features_used = {"Number of Words"};

		// Set the superclass's fields appropriately
		feature_description = new FeatureDefinition(name, description, is_sequential, dimensions);
		feature_dependencies = features_used;
		external_dependencies = null;

		// Store the keywords
		this.keywords = keywords;
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

		// Note the total number of words
		double total_number_words = other_feature_values[0][0];

		// Prepare the data structure to hold mappings between keywords and how often they occur in the lyrics
		HashMap<String,Integer> matches = new HashMap(keywords.length);
		for (int i = 0; i < keywords.length; i++)
			matches.put(keywords[i].toLowerCase(), new Integer(0));

		// Count how many times a keword occurs in the lyrics
		int count = 0;
		String[] words_in_lyrics = lyrics.getWords();
		for (int i = 0; i < words_in_lyrics.length; i++)
			if (matches.containsKey(words_in_lyrics[i].toLowerCase()))
				count++;

		// Calculate the percentage of words that match a keyword
		double percentage_match = 100.0 * (double) count / total_number_words;

		// Prepare the results
		result[0] = percentage_match;

		// Return the result
		return result;
	}
}