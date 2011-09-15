/*
 * EmbeddedTagMiner.java
 * Version 1.0
 *
 * Last modified on August 3, 2010.
 * University of Waikato and McGill University
 */

package jsongminer;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;

/**
 * An object of this class is used to extract embedded metadata from an audio
 * file. Upon instantiation, an object of this class is given a file path, which
 * it stores, and from which it then extracts and stores embedded metadata tags.
 * Certain individual field values can then be extracted using method calls.
 * Note that instantiation of an object of this class fails if the embedded
 * metadata cannot be extracted. Typically, the best way to access most or all
 * of the embedded information extracted is to call the
 * getAllAvailableNewMetaData method.
 *
 * <p>Metadata extraction is performed using the Jaudiotagger third-party
 * library (http://www.jthink.net/jaudiotagger/). Metadata can be extracted
 * only from file types supported by this library, namely MP3, MP4 (MP4, M4a and
 * M4p), Ogg Vorbis, FLAC and WMA, as well as limited support for WAV and Real
 * Audio.
 *
 * @author Cory McKay
 */
public class EmbeddedTagMiner
{
	/* FIELDS *****************************************************************/


	/**
	 * The code used to indicate in MusicMetaData objects that a piece of data
	 * has been extracted from embedded metadata.
	 */
	private String embedded_source_identifier_code;

	/**
	 * The audio file from which embedded metadata was extracted. This cannot be
	 * null, as an exception is thrown upon instantiation if it would have been.
	 */
	private String audio_file_path;

	/**
	 * Audio properties of the audio file that was parsed for metadata.
	 */
	private AudioHeader audio_header;

	/**
	 * The embedded metadata that was extracted from the audio file specified
	 * upon instantiation. This cannot be null, as an exception is thrown upon
	 * instantiation if it would have been.
	 */
	private Tag tags;


	/* CONSTRUCTOR ************************************************************/


	/**
	 * Instantiate this object and extract and store embedded metadata from the
	 * specified audio file in this object's fields.
	 *
	 * @param audio_file_path	The path of the audio file to extract metadata
	 *							from. It may be any type of audio file supported
	 *							by the Jaudiotagger library (see
	 *							http://www.jthink.net/jaudiotagger/)
	 * @throws Exception		An informative exception is thrown if the
	 *							specified file path is invalid, or if metadata
	 *							cannot be parsed from the file.
	 */
	public EmbeddedTagMiner(String audio_file_path)
			throws Exception
	{
		// Store the original error PrintStream. This is necessary because the
		// Jaudiotagger prints superfluous messages to the error stream, and
		// these must be suppressed.
		PrintStream original_err = System.err;

		try
		{
			// Temporarily suppress the standard error stream
			System.setErr(new PrintStream(new OutputStream(){public void write(int b) {}}));

			// Store the input file path
			this.audio_file_path = audio_file_path;

			// Set the identifier field
			embedded_source_identifier_code = "Data Embedded In File";

			// Parse the audio file and store the extracted tags
			AudioFile audio_file = AudioFileIO.read(new File(audio_file_path));
			audio_header = audio_file.getAudioHeader();
			tags = audio_file.getTag();

			// Throw an exception if tags could not be extracted
			if (tags == null)
				throw new Exception("Embedded metadata tags could not be parsed.");

			// Restore the standard error stream
			System.setErr(original_err);
		}
		catch (Exception e)
		{
			// Restore the standard error stream
			System.setErr(original_err);

			// Forward on the Exception
			throw e;
		}
	}


	/* PUBLIC METHODS *********************************************************/


	/**
	 * Return the path of the audio file from which this object extracted
	 * embedded metadata.
	 *
	 * @return	The file path.
	 */
	public String getAudioFilePath()
	{
		return audio_file_path;
	}


	/**
	 * Return the title tag that was extracted upon this object's instantiation.
	 *
	 * @return				The title tag. Note that this cannot be null or the
	 *						empty string, as an exception is thrown if it
	 *						would be.
	 * @throws Exception	An informative exception is thrown if the title tag
	 *						was not successfully extracted.
	 */
	public String getTitle()
			throws Exception
	{
		String this_tag = tags.getFirst(FieldKey.TITLE);
		if (this_tag == null)
			throw new Exception("Embedded title tag is missing or unspecified.");
		else if (this_tag.equals(""))
			throw new Exception("Embedded title tag is missing or unspecified.");
		else return this_tag;
	}


	/**
	 * Return the artist tag that was extracted upon this object's
	 * instantiation.
	 *
	 * @return				The artist tag. Note that this cannot be null or the
	 *						empty string, as an exception is thrown if it
	 *						would be.
	 * @throws Exception	An informative exception is thrown if the artist tag
	 *						was not successfully extracted.
	 */
	public String getArtist()
			throws Exception
	{
		String this_tag = tags.getFirst(FieldKey.ARTIST);
		if (this_tag == null)
			throw new Exception("Embedded artist tag is missing or unspecified.");
		else if (this_tag.equals(""))
			throw new Exception("Embedded artist tag is missing or unspecified.");
		else return this_tag;
	}


	/**
	 * Return the album tag that was extracted upon this object's
	 * instantiation.
	 *
	 * @return				The album tag. Note that this cannot be null or the
	 *						empty string, as an exception is thrown if it
	 *						would be.
	 * @throws Exception	An informative exception is thrown if the album tag
	 *						was not successfully extracted.
	 */
	public String getAlbum()
			throws Exception
	{
		String this_tag = tags.getFirst(FieldKey.ALBUM);
		if (this_tag == null)
			throw new Exception("Embedded album tag is missing or unspecified.");
		else if (this_tag.equals(""))
			throw new Exception("Embedded album tag is missing or unspecified.");
		else return this_tag;
	}


	/**
	 * Return the genre tag that was extracted upon this object's instantiation.
	 *
	 * @return				The genre tag. Note that this cannot be null or the
	 *						empty string, as an exception is thrown if it
	 *						would be.
	 * @throws Exception	An informative exception is thrown if the genre tag
	 *						was not successfully extracted.
	 */
	public String getGenre()
			throws Exception
	{
		String this_tag = tags.getFirst(FieldKey.GENRE);
		if (this_tag == null)
			throw new Exception("Embedded genre tag is missing or unspecified.");
		else if (this_tag.equals(""))
			throw new Exception("Embedded genre tag is missing or unspecified.");
		else return this_tag;
	}


	/**
	 * Stores all available song metadata that has been extracted from an audio
	 * file during the  construction of this object and adds it to the given
	 * song_metadata.
	 *
	 * @param song_metadata		A Vector of metadata already extracted for this
	 *							song. May not be null, but may be empty.
	 * @param store_fails		If this is true, then for each individual piece
	 *							of metadata that cannot be extracted an
	 *							indication is added to the stored MusicMetaData
	 *							highlighting the failure. If this is false then
	 *							fields that cannot be extracted are simply
	 *							ignored. Note that this parameter has no
	 *							effect on song identification specifically.
	 * @throws Exception		An exception is thrown if invalid parameters are
	 *							passed.
	 * @return					True is returned if a change was made to
	 *							song_metadata, false otherwise.
	 */
	public boolean getAllAvailableNewMetaData(Vector<MusicMetaData> song_metadata,
			boolean store_fails)
			throws Exception
	{
		// Verify input parameters
		if (song_metadata == null)
			throw new Exception ("Song vector must be provided (although it may be empty).");

		// Whether or not a change has been made
		boolean some_data_successfully_extracted = false;

		// The metadata extracted by this method
		Vector<MusicMetaData> results = new Vector<MusicMetaData>();

		// Extract the song title
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Song Title", tags.getFirst(FieldKey.TITLE)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Song Title", "Could not extract this information: " + e.getMessage()));}

		// Extract the Music Brainz Song ID
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Music Brainz Song ID", tags.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Music Brainz Song ID", "Could not extract this information: " + e.getMessage()));}

		// Extract the MusicIP Song ID
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "MusicIP Song ID", tags.getFirst(FieldKey.MUSICIP_ID)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "MusicIP Song ID", "Could not extract this information: " + e.getMessage()));}

		// Extract the Amazon ID
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Amazon Song ID", tags.getFirst(FieldKey.AMAZON_ID)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Amazon Song ID", "Could not extract this information: " + e.getMessage()));}

		// Extract the song barcode
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Song Barcode", tags.getFirst(FieldKey.BARCODE)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Song Barcode", "Could not extract this information: " + e.getMessage()));}

		// Extract the artist name
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Artist Name", tags.getFirst(FieldKey.ARTIST)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Artist Name", "Could not extract this information: " + e.getMessage()));}

		// Extract the Artist Music Brainz ID
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Music Brainz Artist ID", tags.getFirst(FieldKey.MUSICBRAINZ_ARTISTID)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Music Brainz Artist ID", "Could not extract this information: " + e.getMessage()));}

		// Extract the Discogs Artist Site
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Discogs Artist Site", tags.getFirst(FieldKey.URL_DISCOGS_ARTIST_SITE)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Discogs Artist Site", "Could not extract this information: " + e.getMessage()));}

		// Extract the official artist URL
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Artist Official URL", tags.getFirst(FieldKey.URL_OFFICIAL_ARTIST_SITE)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Artist Official URL", "Could not extract this information: " + e.getMessage()));}

		// Extract the artist Wikipedia URL
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Artist Wikipedia URL", tags.getFirst(FieldKey.URL_WIKIPEDIA_ARTIST_SITE)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Artist Wikipedia URL", "Could not extract this information: " + e.getMessage()));}

		// Extract the composer name
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Composer Name", tags.getFirst(FieldKey.COMPOSER)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Composer Name", "Could not extract this information: " + e.getMessage()));}

		// Extract the lyricist name
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Lyricist Name", tags.getFirst(FieldKey.LYRICIST)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Lyricist Name", "Could not extract this information: " + e.getMessage()));}

		// Extract the remixer name
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Remixer Name", tags.getFirst(FieldKey.REMIXER)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Remixer Name", "Could not extract this information: " + e.getMessage()));}

		// Extract the album title
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Album Title", tags.getFirst(FieldKey.ALBUM)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Album Title", "Could not extract this information: " + e.getMessage()));}

		// Extract the Album Music Brainz ID
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Music Brainz Album ID", tags.getFirst(FieldKey.MUSICBRAINZ_DISC_ID)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Music Brainz Album ID", "Could not extract this information: " + e.getMessage()));}

		// Extract whether or not this is a track on a compilation album
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Compilation Album", tags.getFirst(FieldKey.IS_COMPILATION)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Compilation Album", "Could not extract this information: " + e.getMessage()));}

		// Extract the album disc number
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Album Disc Number", tags.getFirst(FieldKey.DISC_NO)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Album Disc Number", "Could not extract this information: " + e.getMessage()));}

		// Extract the total number of discs
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Total Number of Discs", tags.getFirst(FieldKey.DISC_TOTAL)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Total Number of Discs", "Could not extract this information: " + e.getMessage()));}

		// Extract the album track number
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Album Track Number", tags.getFirst(FieldKey.TRACK)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Album Track Number", "Could not extract this information: " + e.getMessage()));}

		// Extract the total number of album tracks
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Total Number of Album Tracks", tags.getFirst(FieldKey.TRACK_TOTAL)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Total Number of Album Tracks", "Could not extract this information: " + e.getMessage()));}

		// Extract the record label
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Record Label", tags.getFirst(FieldKey.RECORD_LABEL)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Record Label", "Could not extract this information: " + e.getMessage()));}

		// Extract the release country
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Release Country", tags.getFirst(FieldKey.MUSICBRAINZ_RELEASE_COUNTRY)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Release Country", "Could not extract this information: " + e.getMessage()));}

		// Extract the release year
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Release Year", tags.getFirst(FieldKey.YEAR)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Release Year", "Could not extract this information: " + e.getMessage()));}

		// Extract the language
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Language", tags.getFirst(FieldKey.LANGUAGE)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Language", "Could not extract this information: " + e.getMessage()));}

		// Extract the lyrics
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Lyrics", tags.getFirst(FieldKey.LYRICS)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Lyrics", "Could not extract this information: " + e.getMessage()));}

		// Extract the lyrics URL
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Lyrics URL", tags.getFirst(FieldKey.URL_LYRICS_SITE)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Lyrics URL", "Could not extract this information: " + e.getMessage()));}

		// Extract the genre label
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Genre", tags.getFirst(FieldKey.GENRE)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Genre", "Could not extract this information: " + e.getMessage()));}

		// Extract the mood label
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Mood", tags.getFirst(FieldKey.MOOD)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Mood", "Could not extract this information: " + e.getMessage()));}

		// Extract the grouping
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Grouping", tags.getFirst(FieldKey.GROUPING)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Grouping", "Could not extract this information: " + e.getMessage()));}

		// Extract the comment label
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Comment", tags.getFirst(FieldKey.COMMENT)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Comment", "Could not extract this information: " + e.getMessage()));}

		// Extract the tempo marking
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Tempo (BPM)", tags.getFirst(FieldKey.BPM)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Tempo (BPM)", "Could not extract this information: " + e.getMessage()));}

		// Extract the key
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Key", tags.getFirst(FieldKey.KEY)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Key)", "Could not extract this information: " + e.getMessage()));}

		// Extract the track duration
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Duration (seconds)", (new Integer(audio_header.getTrackLength())).toString()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Duration (seconds)", "Could not extract this information: " + e.getMessage()));}

		// Extract the encoder
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Encoder", tags.getFirst(FieldKey.ENCODER)));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Encoder)", "Could not extract this information: " + e.getMessage()));}

		// Extract the encoding type
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Encoding Type", audio_header.getEncodingType()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Encoding Type", "Could not extract this information: " + e.getMessage()));}

		// Extract the format
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Encoding Format", audio_header.getFormat()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Encoding Format", "Could not extract this information: " + e.getMessage()));}

		// Extract the number of channels
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Number of Channels", audio_header.getChannels()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Number of Channels", "Could not extract this information: " + e.getMessage()));}

		// Extract the sample rate
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Sample Rate (Hz)", audio_header.getSampleRate()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Sample Rate (Hz)", "Could not extract this information: " + e.getMessage()));}

		// Extract the bit rate
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Bit Rate (kilobits per second)", audio_header.getBitRate()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Bit Rate (kilobits per second)", "Could not extract this information: " + e.getMessage()));}

		// Extract whether the bit rate is variable
		try {results.add(new MusicMetaData(embedded_source_identifier_code, "Variable Bit Rate", (new Boolean(audio_header.isVariableBitRate())).toString()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(embedded_source_identifier_code, "Variable Bit Rate", "Could not extract this information: " + e.getMessage()));}

		// Add all song metadata extracted so far to the provided Vector
		if (!results.isEmpty())
		{
			for (int i = 0; i < results.size(); i++)
				song_metadata.add(results.get(i));
			some_data_successfully_extracted = true;
		}

		// Return whether some data was successfully extracted
		return some_data_successfully_extracted;
	}
}