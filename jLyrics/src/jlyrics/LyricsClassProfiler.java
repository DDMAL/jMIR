/*
 * LyricsClassProfiler.java
 * Version 1.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jlyrics;

import jlyrics.datatypes.SongLyrics;
import java.io.DataOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * A library of static methods that can be used to build profiles about the relative frequencies of words in
 * different categorization classes.
 *
 * @author Cory McKay
 */
public class LyricsClassProfiler
{
	/**
	 * Parses all .txt files in the given directory and returns a report containing, among other things, a
	 * list of all words found in the text files sorted by their relative frequencies.
	 *
	 * @param lyrics_directory_path	The directory to search for .txt files.
	 * @return						A report listing the the directory searched, the total number of .txt
	 *								files found, the number of songs with lyrics, the number of songs without
	 *								lyrics, the total number or words, the total number of unique words, and a
	 *								list of each unique word followed by a tab and how many times the word
	 *								occurs, followed by another tab and	what percentage of the total number of
	 *								words that this represents. This list is sorted from the most common word
	 *								to the least common word.
	 * @throws Exception			An informative exception is thrown if a prolem occurs.
	 */
	public static String buildWordFrequencyProfile(String lyrics_directory_path)
		throws Exception
	{
		// Parse all lyrics files in the lyrics_directory_path directory
		SongLyrics[] all_lyrics = LyricsFeatureProcessor.parseSongLyrics(lyrics_directory_path);

		// Prepare the report
		StringBuffer report = calculateCounts(all_lyrics);

		// Prepare the report header
		StringBuffer report_header = new StringBuffer();
		report_header.append("DIRECTORY SEARCHED: " + lyrics_directory_path + "\n");
		report_header.append("TOTAL NUMBER OF .TXT FILES FOUND: " + all_lyrics.length + "\n\n");

		// Return the report
		return report_header.append(report).toString();
	}


	/**
	 * Parses all .txt files in the given directory and returns a report for each candidate class in the
	 * provided unique_classes_path file. Each report includes, among other information, sorted word
	 * frequencies across all files in the directory with identifiers matching those referred to in the
	 * identifiers_path file and with matching entries in the classes_path file that match the given
	 * unique classs.
	 *
	 * @param lyrics_directory_path	The directory to search for .txt files.
	 * @param unique_classes_path	A path of a .txt file containing a unique class name on each line. A
	 *								report is generated for each class listed in this file, where each report
	 *								only contains results from songs associated with the given class.
	 * @param classes_path			The path of a .txt file containing the name of a class on each line. Each
	 *								line should specify the class of the instance referred to on the matching
	 *								line on the indentifiers_path file.
	 * @param identifiers_path		The path of a .txt file containing an instance identifier on each line.
	 *								Each instance should have a class corresponding to that listed on the
	 *								corresponding line of the classes_path file.
	 * @return						A report for each unique class listing, along with additional
	 *								miscellaneous data, a list of each unique word followed by a tab and how
		*							many times the word	occurs, followed by another tab and	what percentage of
	 *								the total number of	words that this represents. This list is sorted from
	 *								the most common word to the least common word.
	 * @throws Exception			An informative exception is thrown if a problem occurs.
	 */
	public static String[] buildWordFrequencyProfile(String lyrics_directory_path,
			String unique_classes_path,
			String classes_path,
			String identifiers_path)
		throws Exception
	{
		// Parse the unique classes file
		SongLyrics temp = new SongLyrics("Unique Classes", unique_classes_path);
		String[] unique_classes = temp.getSongLines();

		// Parse the classes file
		temp = new SongLyrics("Classes", classes_path);
		String[] classes = temp.getSongLines();

		// Parse the identifiers file
		temp = new SongLyrics("Identifiers", identifiers_path);
		String[] identifiers = temp.getSongLines();

		// Verify that there are an equal number of classes and identifiers
		if (classes.length != identifiers.length)
			throw new Exception("List of classes and list of identifiers are of a different length.");

		// Append ".txt" to all of the identifiers
		for (int i = 0; i < identifiers.length; i++)
			identifiers[i] = identifiers[i] + ".txt";

		// Parse all lyrics files in the lyrics_directory_path directory
		SongLyrics[] all_lyrics = LyricsFeatureProcessor.parseSongLyrics(lyrics_directory_path);

		// Prepare a HashMap for finding lyrics based on their identifiers
		HashMap<String,SongLyrics> lyrics_map = new HashMap<String,SongLyrics>(all_lyrics.length);
		for (int i = 0; i < all_lyrics.length; i++)
			lyrics_map.put(all_lyrics[i].getIdentifier(), all_lyrics[i]);

		// Generate reports
		String[] reports = new String[unique_classes.length];
		for (int unique_class = 0; unique_class < unique_classes.length; unique_class++)
		{
			// Information to maintain for a given report
			Vector<SongLyrics> matched_songs = new Vector<SongLyrics>();
			Vector<String> songs_matched = new Vector<String>();
			Vector<String> songs_could_not_find = new Vector<String>();

			// Find the songs with the given class
			for (int song = 0; song < identifiers.length; song++)
			{
				if (classes[song].equals(unique_classes[unique_class]))
				{
					SongLyrics this_song = lyrics_map.get(identifiers[song]);

					if (this_song == null)
						songs_could_not_find.add(identifiers[song]);
					else
					{
						matched_songs.add(this_song);
						songs_matched.add(identifiers[song]);
					}
				}
			}

			// Prepare the report header
			StringBuffer report_header = new StringBuffer();
			report_header.append("FILTER CLASS: " + unique_classes[unique_class] + "\n");
			report_header.append("DIRECTORY SEARCHED: " + lyrics_directory_path + "\n");
			report_header.append("TOTAL NUMBER OF .TXT FILES FOUND IN DIRECTORY: " + all_lyrics.length + "\n\n");
			if (!songs_matched.isEmpty())
			{
				report_header.append("MATCHING FILES PROCESSED:" + matched_songs.size() + "\n");
				String[] matched_songs_array = songs_matched.toArray(new String[1]);
				for (int i = 0; i < matched_songs_array.length; i++)
					report_header.append("\t" + matched_songs_array[i] + "\n");
			}
			else report_header.append("NO MATCHING FILES FOUND\n");
			if (!songs_could_not_find.isEmpty())
			{
				report_header.append("FILES MATCHING THIS CLASS THAT COULD NOT BE LOCATED IN THE DIRECTORY:" + songs_could_not_find.size() + "\n");
				String[] songs_could_not_find_array = songs_could_not_find.toArray(new String[1]);
				for (int i = 0; i < songs_could_not_find_array.length; i++)
					report_header.append("\t" + songs_could_not_find_array[i] + "\n");
			}

			// Prepare the main report
			StringBuffer report = new StringBuffer();
			if (!songs_matched.isEmpty())
			{
				SongLyrics[] these_songs = matched_songs.toArray(new SongLyrics[1]);
				report.append(calculateCounts(these_songs));
			}

			// Store the report
			reports[unique_class] = report_header.append(report).toString();
		}

		// Return the resutls
		return reports;
	}


	/**
	 * Saves the given string to the given path.
	 *
	 * @param save_path		The path to save to.
	 * @param report		The information to save.
	 * @throws Exception	Throws an exception if a problem occurs.
	 */
	public static void writeReport(String save_path, String report)
		throws Exception
	{
		File save_file = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(save_path, true);
		if (save_file != null)
		{
			DataOutputStream writer = mckay.utilities.staticlibraries.FileMethods.getDataOutputStream(save_file);
			writer.writeBytes(report);
			writer.close();
		}
	}


	/**
	 * Prepares a report of how often each word appears in the given collection of SongLyrics objects. Note
	 * that all words are converted to lowercase to maintain case independence.
	 *
	 * @param lyrics		The texts to search through. At least one of these must contain lyrics.
	 * @return				A report listing the the number of songs with lyrics, the number of songs without
	 *						lyrics, the total number or words, the total number of unique words, and
	 *						a list of each unique word followed by a tab and how many times the word occurs,
	 *						followed by another tab and what percentage of the total number of words that this
	 *						represents. This list is sorted from the most common word to the least common
	 *						word.
	 * @throws Exception	An informative exception if none of the provided SongLyrics contains any text.
	 */
	public static StringBuffer calculateCounts(SongLyrics[] lyrics)
		throws Exception
	{
		// Verify that at leat one of the lyrics has text
		boolean are_lyrics = false;
		for (int i = 0; i < lyrics.length; i++)
			if (lyrics[i].isLyricsAvailable())
				are_lyrics = true;
		if (!are_lyrics)
			return new StringBuffer("\nNO TEXT FOUND IN ANY OF THE PROVIDED FILES\n");

		// Calculate the word frequencies and total words
		HashMap<String,Integer> word_frequency_map = new HashMap<String,Integer>();
		long total_words = 0;
		for (int file = 0; file < lyrics.length; file++)
		{
			String[] song_words = lyrics[file].getWords();
			if (song_words != null)
			{
				// Convert the words to lower case
				for (int word_i = 0; word_i < song_words.length; word_i++)
					song_words[word_i] = song_words[word_i].toLowerCase();

				// Add the words to the word_frequency_map, keeping the word counts updated
				for (int word_i = 0; word_i < song_words.length; word_i++)
				{
					Integer new_count = new Integer(1);
					Integer word_count = word_frequency_map.get(song_words[word_i]);

					if (word_count != null)
						new_count = new_count.intValue() + word_count.intValue();

					word_frequency_map.put(song_words[word_i], new_count);

					total_words++;
				}
			}
		}

		// Extract the words in the word_frequency_map
		String[] words = new String[word_frequency_map.size()];
		Iterator<String> iterator = word_frequency_map.keySet().iterator();
		int unique_words = 0;
		while (iterator.hasNext())
		{
			words[unique_words] = iterator.next();
			unique_words++;
		}

		// Extract the word counts
		int[] counts = new int[words.length];
		for (int i = 0; i < counts.length; i++)
			counts[i] = word_frequency_map.get(words[i]).intValue();

		// Sort the words and word counts
		String[] sorted_words = new String[words.length];
		int[] sorted_counts = new int[counts.length];
		while (unique_words > 0)
		{
			unique_words--;

			int highest_count = 0;
			int current_index = -1;
			for (int i = 0; i < counts.length; i++)
			{
				if (words[i] != null)
				{
					if (counts[i] > highest_count)
					{
						highest_count = counts[i];
						current_index = i;
					}
				}
			}

			sorted_words[unique_words] = words[current_index];
			sorted_counts[unique_words] = counts[current_index];

			words[current_index] = null;
			counts[current_index] = -1;
		}

		// Calculate percentages
		float[] sorted_word_percentages = new float[sorted_counts.length];
		for (int i = 0; i < sorted_word_percentages.length; i++)
			sorted_word_percentages[i] = (float) 100 * (float) sorted_counts[i] / (float) total_words;

		// Calculate the number of non-empty lyrics files
		int non_empty_lyrics = 0;
		for (int i = 0; i < lyrics.length; i++)
			if (lyrics[i].isLyricsAvailable())
				non_empty_lyrics++;

		// Prepare the report
		StringBuffer report = new StringBuffer();
		report.append("NUMBER OF SONGS WITH LYRICS: " + non_empty_lyrics + "\n");
		report.append("NUMBER OF SONGS WITHOUT LYRICS: " + (lyrics.length - non_empty_lyrics) + "\n\n");
		report.append("TOTAL WORDS: " + total_words + "\n");
		report.append("UNIQUE WORDS: " + sorted_words.length + "\n\n");
		for (int i = sorted_words.length - 1; i >=0; i--)
		{
			report.append(sorted_words[i]);
			report.append("\t");
			report.append(sorted_counts[i]);
			report.append("\t");
			report.append(sorted_word_percentages[i]);
			report.append("\n");
		}
		
		// Return the report
		return report;
	}
}