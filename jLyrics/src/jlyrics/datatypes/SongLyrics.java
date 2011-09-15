/*
 * SongLyrics.java
 * Version 1.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jlyrics.datatypes;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * A class for holding the lyrics of a song, as parsed from a source file or as specified directly as a
 * string. Automatically parses lyrics into words, into lines and into segments (e.g. verses, choruses, etc.),
 * where lines are separated by line breaks and segments are separated by blank lines. Also makes note of all
 * unique words in the song and how often they each occur.
 *
 * @author Cory McKay
 */
public class SongLyrics
{
	/* FIELDS ***********************************************************************************************/

	
	/**
	 * A name used internally to refer to the referenced song. Should be unique and may not be null.
	 */
	private String identifier;

	/**
	 * The path of a file associated with the lyrics contained in an object of this class. May be null if no
	 * such file is available. Otherwsie, should be unique.
	 */
	private String file_path;

	/**
	 * Whether or not lyrics are available for this song.  This could be because the song is instrumental, or
	 * it could be because they could not be found. This will be true even if only whitespace characters are
	 * present, however.
	 */
	private boolean are_lyrics_available;

	/**
	 * The complete unprocessed lyrics of a song. Null if there are no lyrics, but it will be non-null even if
	 * only whitespace characters are present, however.
	 */
	private String raw_lyrics;

	/**
	 * All raw lyrics separated out into words. Whitespace characters are removed. Repeated words are not
	 * filtered out. Each entry corresponds to an individual word. Null if there are no lyrics.
	 */
	private String[] individual_words;

	/**
	 * The lyrics of a song, broken into lines. Lines consisting only of line breaks are filtered out. Null if
	 * there are no lyrics.
	 */
	private String[] song_lines;

	/**
	 * The lyrics of a song, broken into segments (e.g. verses, choruses, etc.). Segments are assumed to be
	 * segmented by blank lines, and lines are segmented by line breaks. Lines consisting only of line breaks
	 * are filtered out. The first indice refers to the segment and the second to the line. Null if there are
	 * no lyrics.
	 */
	private String[][] song_segments;

	/**
	 * Mappings between all unique words of a song and how often each of these words occur in the song. Null
	 * if there are no lyrics or if there are no words consisting of non-whitespace characters.
	 */
	private HashMap<String,Integer> unique_word_counts;


	/* CONSTRUCTOR ******************************************************************************************/


	/**
	 * Manual constructor. Parses the specified raw_lyrics into song lines and song segments.
	 *
	 * @param identifier	A name used internally to refer to the referenced song. Should be unique.
	 * @param file_path     The path to some file of interest in relation to the lyrics contained in the
	 *						raw_lyrics parameter. This file is not actually parsed by this constructor,
	 *						however. May be null if no such file is available. Otherwise, should be unique.
	 * @param raw_lyrics	The complete lyrics of a song. A value of null indicates that no lyrics are
	 *						available.
	 * @throws Exception	Throws an informative exception if invalid input is specified.
	 */
	public SongLyrics(String identifier, String file_path, String raw_lyrics)
		throws Exception
	{
		if (identifier == null) throw new Exception("A valid lyrics identifier must be specified.");

		this.identifier = identifier;
		this.raw_lyrics = raw_lyrics;

		file_path = null;

		if (raw_lyrics == null)
		{
			are_lyrics_available = false;
			individual_words = null;
			song_lines = null;
			song_segments = null;
			unique_word_counts = null;
		}
		else
		{
			are_lyrics_available = true;
			individual_words = parseWords(raw_lyrics);
			song_lines = parseSongLines(raw_lyrics);
			song_segments = parseSongSegments(raw_lyrics);
			unique_word_counts = parseUniqueWords(raw_lyrics);
		}
	}


	/**
	 * Parses the specified file in order to acquire raw lyrics, song lines and song segments.
	 *
	 * @param identifier	A name used internally to refer to the referenced song. Should be unique and may
	 *						not be null.
	 * @param file_path     The path of the file from which the lyrics contained in an object of this class
	 *						are to be parsed. Should be unique. May not be null.
	 * @throws Exception	Throws an informative exception if invalid input is specified.
	 */
	public SongLyrics(String identifier, String file_path)
		throws Exception
	{
		if (identifier == null) throw new Exception("A valid lyrics identifier must be specified.");
		if (file_path == null) throw new Exception("A valid file path must be specified.");

		this.identifier = identifier;
		this.file_path = file_path;

		raw_lyrics = parseTextFile(file_path);

		if (raw_lyrics == null)
		{
			are_lyrics_available = false;
			individual_words = null;
			song_lines = null;
			song_segments = null;
			unique_word_counts = null;
		}
		else
		{
			are_lyrics_available = true;
			individual_words = parseWords(raw_lyrics);
			song_lines = parseSongLines(raw_lyrics);
			song_segments = parseSongSegments(raw_lyrics);
			unique_word_counts = parseUniqueWords(raw_lyrics);
		}
	}


	/* PUBLIC METHODS ***************************************************************************************/


	/**
	 * Returns the unique name used internally to refer to the referenced song.
	 *
	 * @return	The unique identifier.
	 */
	public String getIdentifier()
	{
		return identifier;
	}


	/**
	 * Returns the unique path of a file associated with the lyrics contained in this object. May be null if
	 * no such file is available.
	 *
	 * @return	The unique file path.
	 */
	public String getFilePath()
	{
		return file_path;
	}


	/**
	 * Returns whether or not the lyrics are available for this song. This could be because the song is
	 * instrumental, or it could be because they could not be found. This will be true even if only whitespace
	 * characters are present, however.
	 *
	 * @return	Whether or not lyrics are available.
	 */
	public boolean isLyricsAvailable()
	{
		return are_lyrics_available;
	}


	/**
	 * Returns the complete unprocessed lyrics of this song. Will be null if there are no lyrics to return,
	 * although it will be non-null even if only whitespace characters are present.
	 *
	 * @return	The raw lyrics of this song.
	 */
	public String getRawLyrics()
	{
		return raw_lyrics;
	}


	/**
	 * Returns the raw lyrics separated out into words. Whitespace characters are removed. Repeated words are
	 * not filtered out. Each entry corresponds to an individual word. Null is returned if there are no
	 * lyrics.

	 * @return	The individual words of this song.
	 */
	public String[] getWords()
	{
		return individual_words;
	}


	/**
	 * Returns the lyrics of this song, broken into lines. Lines consisting only of line breaks are filtered
	 * out. Will be null if there are no lyrics to return.
	 *
	 * @return	The lines of this song.
	 */
	public String[] getSongLines()
	{
		return song_lines;
	}


	/**
	 * Returns the lyrics of this song, broken into segments (e.g. verses, choruses, etc.). The first indice
	 * refers to the segment and the second to the line. Lines consisting only of line breaks are filtered
	 * out. Will be null if there are no lyrics to return.
	 *
	 * @return	The segments of this song.
	 */
	public String[][] getSongSegments()
	{
		return song_segments;
	}


	/**
	 * Returns an array containing all unique words contained in this song (i.e. without duplicates). The
	 * order of the words corresponds to the order of the word counts returned by the getUniqueWordCounts
	 * method. Null is returned if there are no non-whitespace words in this song.
	 *
	 * @return	The unique words.
	 */
	public String[] getUniqueWords()
	{
		if (unique_word_counts == null) return null;

		String[] unique_words = new String[unique_word_counts.size()];
		Iterator<String> iterator = unique_word_counts.keySet().iterator();
		for (int i = 0; i < unique_words.length; i++)
			unique_words[i] = iterator.next();
		return unique_words;
	}


	/**
	 * Returns an array containing how many times each word occured in this song. The order of the counts
	 * corresponds to the order of the words returned by the getUniqueWords method. Null is returned if there
	 * are no non-whitespace words in this song.
	 *
	 * @return	The unique words.
	 */
	public int[] getUniqueWordCounts()
	{
		if (unique_word_counts == null) return null;

		int[] word_counts = new int[unique_word_counts.size()];
		Iterator<Integer> iterator = unique_word_counts.values().iterator();
		for (int i = 0; i < word_counts.length; i++)
			word_counts[i] = iterator.next().intValue();
		return word_counts;
	}


	/**
	 * Prints the contents of this object to standard out. Intended for debugging.
	 */
	public void printContents()
	{
		System.out.println("==================");

		System.out.println("IDENTIFIER: >" + identifier + "<");
		System.out.println("FILE PATH: >" + file_path + "<");
		System.out.println("ARE LYRICS AVAILABLE: >" + are_lyrics_available + "<\n");

		System.out.println("RAW LYRICS:\n>" + raw_lyrics + "<\n");

		if (individual_words == null)
			System.out.println("INDIVIDUAL WORDS: >" + individual_words + "<\n");
		else
		{
			System.out.println("INDIVIDUAL WORDS:");
			for (int i = 0; i < individual_words.length; i++)
				System.out.println("WORD " + (i + 1) + ": >" + individual_words[i] + "<");
			System.out.println();
		}

		if (song_lines == null)
			System.out.println("SONG LINES: >" + song_lines + "<\n");
		else
		{
			System.out.println("SONG LINES:");
			for (int i = 0; i < song_lines.length; i++)
				System.out.println("LINE " + (i + 1) + ": >" + song_lines[i] + "<");
			System.out.println();
		}

		if (song_segments == null)
			System.out.println("SONG SEGMENTS: >" + song_segments + "<\n");
		else
		{
			System.out.println("SONG SEGMENTS:");
			for (int i = 0; i < song_segments.length; i++)
			{
				System.out.println("\nSEGMENT " + (i + 1) + ":");
				for (int j = 0; j < song_segments[i].length; j++)
					System.out.println("LINE " + (j + 1) + ": >" + song_segments[i][j] + "<");
			}
			System.out.println();
		}

		if (unique_word_counts == null)
			System.out.println("UNIQUE WORD COUNTS: >" + song_segments + "<\n");
		else
		{
			String[] words = getUniqueWords();
			int[] counts = getUniqueWordCounts();

			System.out.println("UNIQUE WORD COUNTS:");
			for (int i = 0; i < words.length; i++)
				System.out.println("WORD " + (i + 1) + ": >" + words[i] + "<   COUNT: " + counts[i]);
			System.out.println();
		}

		System.out.println("==================");
	}


	/* PUBLIC STATIC METHODS ********************************************************************************/


	/**
	 * Parses a text file in order to extract lyrics from it. Simply returns the contents of the file as
	 * a string. Returns null if the file is empty, and throws an exception if the file cannot be found or
	 * parsed.
	 *
	 * @param file_path_to_parse	The path of the text file to parse.
	 * @return						The contents of the text file. Null if it is empty.
	 * @throws Exception			Throws an informative exception if the specified file cannot be found or
	 *								parsed.
	 */
	public static String parseTextFile(String file_path_to_parse)
		throws Exception
	{
		File file_to_parse = new File(file_path_to_parse);
		String results = mckay.utilities.staticlibraries.FileMethods.parseTextFile(file_to_parse);

		if (results.equals("")) return null;
		else return results;
	}


	/**
	 * Finds all of the words in the specified lyrics and returns them separated out into an array with one
	 * entry per word. "Words" consisting only of white space are not counted. Returns null if the given
	 * string is null or if no words containing content other than whitespace are contained in it.
	 *
	 * @param lyrics_string	The complete unprocessed lyrics of a song.
	 * @return				An array with the words in lyrics_string separated out.
	 */
	public static String[] parseWords(String lyrics_string)
	{
		// Return null if no string is provided
		if (lyrics_string == null) return null;

		// Break into words
		String[] words = lyrics_string.split("\\s");

		// Filter out "words" consisting only of line breaks
		Vector<String> filtered_words = new Vector<String>();
		for (int i = 0; i < words.length; i++)
			if (!words[i].equals(""))
				filtered_words.add(words[i]);

		// Return null if there are no words containing content other than white space
		if (filtered_words.isEmpty()) return null;

		// Reset words so that there are no longer any "words" consisting only of line breaks
		words = filtered_words.toArray(new String[1]);

		// Return the words found
		return words;
	}


	/**
	 * Parses the specified lyrics into lines, where lines are assumed to be segmented by line breaks. Lines
	 * consisting only of line breaks are filtered out. Null is returned if there are no non line break
	 * characters in the given lyrics_string or if it is null.
	 *
	 * @param lyrics_string	The complete unprocessed lyrics of a song.
	 * @return				The parsed lyrics, with each line in a separate element of the array.
	 */
	public static String[] parseSongLines(String lyrics_string)
	{
		// Return null if no string is provided
		if (lyrics_string == null) return null;

		// Split the lyrics_string into lines
		String[] all_lines = lyrics_string.split("\r\n|\r|\n");

		// Filter out lines consisting only of line breaks
		Vector<String> filtered_lines = new Vector<String>();
		for (int i = 0; i < all_lines.length; i++)
			if (!all_lines[i].equals(""))
				filtered_lines.add(all_lines[i]);

		// Return the results
		if (filtered_lines.isEmpty()) return null;
		return filtered_lines.toArray(new String[1]);
	}


	/**
	 * Parses the specified lyrics into segments (e.g. verses, choruses, etc.), where segments are assumed
	 * to be segmented by blank lines and lines are segmented by line breaks. Lines consisting only of line
	 * breaks are filtered out. Null is returned if there are no non line break characters in the given
	 * lyrics_string or if it is null.
	 *
	 * @param lyrics_string	The complete unprocessed lyrics of a song.
	 * @return				The parsed lyrices, with the first element of the array corresponding to segments
	 *						and the second corresponding to lines for that segment.
	 */
	public static String[][] parseSongSegments(String lyrics_string)
	{
		// Return null if no string is provided
		if (lyrics_string == null) return null;
		
		// Split the lyrics_string into lines
		String[] all_lines = lyrics_string.split("\r\n|\r|\n");

		// Go through each line, creating a verse when a blank line is encountered
		Vector<String[]> verses_found = new Vector<String[]>();
		Vector<String> lines_found = new Vector<String>();
		for (int i = 0; i < all_lines.length; i++)
		{
			if(!all_lines[i].equals("")) // i.e. if not a blank line
				lines_found.add(all_lines[i]);
			else if (!lines_found.isEmpty()) // i.e. if at least one non-blank line has been found
			{
				verses_found.add(lines_found.toArray(new String[1]));
				lines_found = new Vector<String>();
			}
		}

		// Add a final verse that has not yet been added
		if (!lines_found.isEmpty())
			verses_found.add(lines_found.toArray(new String[1]));

		// Return the results
		if (verses_found.isEmpty()) return null;
		else return verses_found.toArray(new String[1][1]);
	}


	/**
	 * Finds all of the unique words in the specified lyrics and counts how often they occur. "Words"
	 * consisting only of white space are not counted. Returns null if the given string is null or if no words
	 * containing content other than whitespace are contained in it.
	 * 
	 * @param lyrics_string	The complete unprocessed lyrics of a song.
	 * @return				A HashMap that maps all unique words found to the number of times that they
	 *						occured in the lyrics_string.
	 */
	public static HashMap<String,Integer> parseUniqueWords(String lyrics_string)
	{
		// Return null if no string is provided
		if (lyrics_string == null) return null;

		// Break into words
		String[] words = parseWords(lyrics_string);

		// Fill the hash map with word counts
		HashMap<String,Integer> word_counts = new HashMap<String,Integer>(75);
		for (int i = 0; i < words.length; i++)
		{
			int count_so_far = 1;
			if (word_counts.containsKey(words[i]))
				count_so_far += word_counts.get(words[i]).intValue();
			word_counts.put(words[i], count_so_far);
		}

		// Return the results
		return word_counts;
	}
}