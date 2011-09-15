/*
 * DublinCoreInterpreter.java
 * Version 1.0
 *
 * Last modified on August 3, 2010.
 * University of Waikato and McGill University
 */

package jsongminer;

import java.util.HashMap;
import java.util.Vector;

/**
 * This is s utility class containing static methods for processing collections
 * of MusicMetaData objects into Dublin Core-formatted collections of
 * MusicMetaData.
 *
 * @author Cory McKay
 */
public class DublinCoreInterpreter
{
	/* PUBLIC METHODS *********************************************************/


	/**
	 * Produce new unqualified Dublin Core-formatted song metadata from the
	 * given set of song metadata mined using jSongMiner.
	 *
	 * @param song_metadata	The metadata to generate Dublin Core-formatted
	 *						metadata from.
	 * @return				A new vector of new MusicMetaData objects holding
	 *						Dublin Core-formatted metadata. A given Dublin Core
	 *						field may NOT have multiple MusicMetaData objects
	 *						produced for it Note that the source fields of the
	 *						returned MusicMetaData are set to "".
	 */
	public static Vector<MusicMetaData> getSongUnqualifiedDublinCore(Vector<MusicMetaData> song_metadata)
	{
		// Prepare the default mappings
		String[][] identifier_mappings =
		{
			{"dc.Title","Song Title"},
			{"dc.Creator","Artist Name"},
			{"dc.Subject","Song-Related Tag"},
			{"dc.Description","Last.FM Track Wiki Text"},
			{"dc.Publisher","Record Label"},
			{"dc.Contributor","Composer Name"},
			{"dc.Date","Release Year"},
			{"dc.Type","Genre"},
			{"dc.Format","Encoding Format"},
			{"dc.Identifier","Music Brainz Song ID"},
			{"dc.Source","Album Title"},
			{"dc.Language","Language"},
			{"dc.Relation","Similar Track"},
			{"dc.Coverage","Release Country"},
		};

		// Prepare the alternative mappings
		HashMap<String,Vector<String[]>> alternate_mappings = new HashMap<String,Vector<String[]>>();
		String[] alt_fields;
		alt_fields = new String[5];
		alt_fields[0] = "Music Brainz Artist ID";
		alt_fields[1] = "Echo Nest Artist ID";
		alt_fields[2] = "Discogs Artist Site";
		alt_fields[3] = "Artist Official URL";
		alt_fields[4] = "Artist Wikipedia URL";
		addToAlternateMappings("dc.Creator", alt_fields, alternate_mappings);
		alt_fields = new String[10];
		alt_fields[0] = "Last.FM Track Wiki Summary";
		alt_fields[1] = "Comment";
		alt_fields[2] = "Duration (seconds)";
		alt_fields[3] = "Tempo (BPM)";
		alt_fields[4] = "Time Signature";
		alt_fields[5] = "Key";
		alt_fields[6] = "Mode";
		alt_fields[7] = "Loudness (-100 to 100 dB)";
		alt_fields[8] = "Last.FM Track Play Count";
		alt_fields[9] = "Echo Nest Song Hottness (0 to 1)";
		addToAlternateMappings("dc.Description", alt_fields, alternate_mappings);
		alt_fields = new String[2];
		alt_fields[0] = "Lyricist Name";
		alt_fields[1] = "Remixer Name";
		addToAlternateMappings("dc.Contributor", alt_fields, alternate_mappings);
		alt_fields = new String[3];
		alt_fields[0] = "Grouping";
		alt_fields[1] = "Song-Related Tag";
		alt_fields[2] = "Mood";
		addToAlternateMappings("dc.Type", alt_fields, alternate_mappings);
		alt_fields = new String[7];
		alt_fields[0] = "Encoding Type";
		alt_fields[1] = "Encoder";
		alt_fields[2] = "Bit Rate (kilobits per second)";
		alt_fields[3] = "Sample Rate (Hz)";
		alt_fields[4] = "Variable Bit Rate";
		alt_fields[5] = "Number of Channels";
		alt_fields[6] = "Audio Source File";
		addToAlternateMappings("dc.Format", alt_fields, alternate_mappings);
		alt_fields = new String[7];
		alt_fields[0] = "Echo Nest Song ID";
		alt_fields[1] = "Last.FM Track URL";
		alt_fields[2] = "MusicIP Song ID";
		alt_fields[3] = "Amazon Song ID";
		alt_fields[4] = "Song Barcode";
		alt_fields[5] = "Audio Source File";
		alt_fields[6] = "ENMF Fingerprint Hash Output by the Echo Nest Fingerprinting Codegen Binary";
		addToAlternateMappings("dc.Identifier", alt_fields, alternate_mappings);
		alt_fields = new String[6];
		alt_fields[0] = "Music Brainz Album ID";
		alt_fields[1] = "Album Disc Number";
		alt_fields[2] = "Total Number of Discs";
		alt_fields[3] = "Album Track Number";
		alt_fields[4] = "Total Number of Album Tracks";
		alt_fields[5] = "Compilation Album";
		addToAlternateMappings("dc.Source", alt_fields, alternate_mappings);
		alt_fields = new String[3];
		alt_fields[0] = "Album Title";
		alt_fields[1] = "Lyrics";
		alt_fields[2] = "Lyrics URL";
		addToAlternateMappings("dc.Relation", alt_fields, alternate_mappings);
		alt_fields = new String[1];
		alt_fields[0] = "Release Year";
		addToAlternateMappings("dc.Coverage", alt_fields, alternate_mappings);

		// Prepare and return the results
		return parseOut(identifier_mappings, alternate_mappings, song_metadata, false);
	}



	/**
	 * Produce new unqualified Dublin Core-formatted artist metadata from the
	 * given set of artist metadata mined using jSongMiner.
	 *
	 * @param artist_metadata	The metadata to generate Dublin Core-formatted
	 *							metadata from.
	 * @return					A new vector of new MusicMetaData objects
	 *							holding Dublin Core-formatted metadata. A given
	 *							Dublin Core field may NOT have multiple
	 *							MusicMetaData objects produced for it Note that
	 *							the source fields of the returned MusicMetaData
	 *							are set to "".
	 */
	public static Vector<MusicMetaData> getArtistUnqualifiedDublinCore(Vector<MusicMetaData> artist_metadata)
	{
		// Prepare the default mappings
		String[][] identifier_mappings =
		{
			{"dc.Title","Artist Name"},
			{"dc.Subject","Artist-Related Tag"},
			{"dc.Description","Last.FM Artist Wiki Text"},
			{"dc.Type","Artist-Related Tag"},
			{"dc.Identifier","Music Brainz Artist ID"},
			{"dc.Relation","Top Tracks"},
			{"dc.Coverage","Artist Location"},
		};

		// Prepare the alternative mappings
		HashMap<String,Vector<String[]>> alternate_mappings = new HashMap<String,Vector<String[]>>();
		String[] alt_fields;
		alt_fields = new String[1];
		alt_fields[0] = "Artist-Related Term";
		addToAlternateMappings("dc.Subject", alt_fields, alternate_mappings);
		alt_fields = new String[9];
		alt_fields[0] = "Last.FM Artist Wiki Summary";
		alt_fields[1] = "Artist Biography";
		alt_fields[2] = "Artist-Related URL";
		alt_fields[3] = "Artist Review";
		alt_fields[4] = "Artist-Related News";
		alt_fields[5] = "Artist-Related Blog";
		alt_fields[6] = "Last.FM Artist Play Count";
		alt_fields[7] = "Echo Nest Artist Familiarity (0 to 1)";
		alt_fields[8] = "Echo Nest Artist Hotness (0 to 1)";
		addToAlternateMappings("dc.Description", alt_fields, alternate_mappings);
		alt_fields = new String[1];
		alt_fields[0] = "Artist-Related Term";
		addToAlternateMappings("dc.Type", alt_fields, alternate_mappings);
		alt_fields = new String[4];
		alt_fields[0] = "Echo Nest Artist ID";
		alt_fields[1] = "Last.FM Artist URL";
		alt_fields[2] = "7digital Artist ID";
		alt_fields[3] = "Play.me Artist ID";
		addToAlternateMappings("dc.Identifier", alt_fields, alternate_mappings);
		alt_fields = new String[5];
		alt_fields[0] = "Top Albums";
		alt_fields[1] = "Artist-Related Audio";
		alt_fields[2] = "Artist-Related Video";
		alt_fields[3] = "Artist-Related Image";
		alt_fields[4] = "Similar Artist";
		addToAlternateMappings("dc.Relation", alt_fields, alternate_mappings);
		alt_fields = new String[3];
		alt_fields[0] = "Artist Event";
		alt_fields[1] = "Artist-Related News";
		alt_fields[2] = "Artist-Related Blog";
		addToAlternateMappings("dc.Coverage", alt_fields, alternate_mappings);

		// Prepare and return the results
		return parseOut(identifier_mappings, alternate_mappings, artist_metadata, false);
	}


	/**
	 * Produce new unqualified Dublin Core-formatted album metadata from the
	 * given set of album metadata mined using jSongMiner.
	 *
	 * @param album_metadata	The metadata to generate Dublin Core-formatted
	 *							metadata from.
	 * @return					A new vector of new MusicMetaData objects
	 *							holding Dublin Core-formatted metadata. A given
	 *							Dublin Core field may NOT have multiple
	 *							MusicMetaData objects produced for it Note that
	 *							the source fields of the returned MusicMetaData
	 *							are set to "".
	 */
	public static Vector<MusicMetaData> getAlbumUnqualifiedDublinCore(Vector<MusicMetaData> album_metadata)
	{
		// Prepare the default mappings
		String[][] identifier_mappings =
		{
			{"dc.Title","Album Title"},
			{"dc.Creator","Artist Name"},
			{"dc.Subject","Album-Related Tag"},
			{"dc.Description","Last.FM Album Wiki Text"},
			{"dc.Date","Release Date"},
			{"dc.Type","Album-Related Tag"},
			{"dc.Identifier","Music Brainz Album ID"},
			{"dc.Coverage","Release Date"}
		};

		// Prepare the alternative mappings
		HashMap<String,Vector<String[]>> alternate_mappings = new HashMap<String,Vector<String[]>>();
		String[] alt_fields;
		alt_fields = new String[1];
		alt_fields[0] = "Music Brainz Artist ID";
		addToAlternateMappings("dc.Creator", alt_fields, alternate_mappings);
		alt_fields = new String[2];
		alt_fields[0] = "Last.FM Album Wiki Summary";
		alt_fields[1] = "Last.FM Album Play Count";
		addToAlternateMappings("dc.Description", alt_fields, alternate_mappings);
		alt_fields = new String[1];
		alt_fields[0] = "Last.FM Album URL";
		addToAlternateMappings("dc.Identifier", alt_fields, alternate_mappings);


		// Prepare and return the results
		return parseOut(identifier_mappings, alternate_mappings, album_metadata, false);
	}


	/**
	 * Produce new qualified Dublin Core-formatted song metadata from the given
	 * set of song metadata mined using jSongMiner.
	 *
	 * @param song_metadata	The metadata to generate Dublin Core-formatted
	 *						metadata from.
	 * @return				A new vector of new MusicMetaData objects holding
	 *						Dublin Core-formatted metadata. A given Dublin Core
	 *						field may have multiple MusicMetaData objects
	 *						produced for it, but none of them will have the same
	 *						value. Note that the source fields of the returned
	 *						MusicMetaData are set to "".
	 */
	public static Vector<MusicMetaData> getSongQualifiedDublinCore(Vector<MusicMetaData> song_metadata)
	{
		String[][] identifier_mappings =
		{
			{"dc.Title^songtitle","Song Title"},
			{"dc.Creator^artistname","Artist Name"},
			{"dc.Creator^musicbrainzartistid","Music Brainz Artist ID"},
			{"dc.Creator^echonestartistid","Echo Nest Artist ID"},
			{"dc.Creator^discogsartistsite","Discogs Artist Site"},
			{"dc.Creator^artistofficialurl","Artist Official URL"},
			{"dc.Creator^artistwikipediaurl","Artist Wikipedia URL"},
			{"dc.Subject^songrelatedtag","Song-Related Tag"},
			{"dc.Description^lastfmtrackwikitext","Last.FM Track Wiki Text"},
			{"dc.Description^lastfmtrackwikisummary","Last.FM Track Wiki Summary"},
			{"dc.Description^comment","Comment"},
			{"dc.Description^duration","Duration (seconds)"},
			{"dc.Description^tempo","Tempo (BPM)"},
			{"dc.Description^timesignature","Time Signature"},
			{"dc.Description^key","Key"},
			{"dc.Description^mode","Mode"},
			{"dc.Description^loudness","Loudness (-100 to 100 dB)"},
			{"dc.Description^lastfmtrackplaycount","Last.FM Track Play Count"},
			{"dc.Description^echonestsonghotness","Echo Nest Song Hottness (0 to 1)"},
			{"dc.Publisher^recordlabel","Record Label"},
			{"dc.Contributor^composername","Composer Name"},
			{"dc.Contributor^lyricistname","Lyricist Name"},
			{"dc.Contributor^remixername","Remixer Name"},
			{"dc.Date^releaseyear","Release Year"},
			{"dc.Type^genre","Genre"},
			{"dc.Type^grouping","Grouping"},
			{"dc.Type^songrelatedtag","Song-Related Tag"},
			{"dc.Type^mood","Mood"},
			{"dc.Format^encodingformat","Encoding Format"},
			{"dc.Format^encodingtype","Encoding Type"},
			{"dc.Format^encoder","Encoder"},
			{"dc.Format^bitrate","Bit Rate (kilobits per second)"},
			{"dc.Format^samplerate","Sample Rate (Hz)"},
			{"dc.Format^variablebitrate","Variable Bit Rate"},
			{"dc.Format^numberofchannels","Number of Channels"},
			{"dc.Format^filepath","Audio Source File"},
			{"dc.Identifier^musicbrainzsongid","Music Brainz Song ID"},
			{"dc.Identifier^echonestsongid","Echo Nest Song ID"},
			{"dc.Identifier^lastfmtrackurl","Last.FM Track URL"},
			{"dc.Identifier^musicipsongid","MusicIP Song ID"},
			{"dc.Identifier^amazonsongid","Amazon Song ID"},
			{"dc.Identifier^songbarcode","Song Barcode"},
			{"dc.Identifier^filepath","Audio Source File"},
			{"dc.Identifier^echonestfingerprintinghash","ENMF Fingerprint Hash Output by the Echo Nest Fingerprinting Codegen Binary"},
			{"dc.Source^albumtitle","Album Title"},
			{"dc.Source^musicbrainzalbumid","Music Brainz Album ID"},
			{"dc.Source^albumdiscnumber","Album Disc Number"},
			{"dc.Source^totalnumberofdiscs","Total Number of Discs"},
			{"dc.Source^albumtracknumber","Album Track Number"},
			{"dc.Source^totalnumberofalbumtracks","Total Number of Album Tracks"},
			{"dc.Source^compilationalbum","Compilation Album"},
			{"dc.Language^tracklanguage","Language"},
			{"dc.Relation^similartrack","Similar Track"},
			{"dc.Relation^albumtitle","Album Title"},
			{"dc.Relation^lyrics","Lyrics"},
			{"dc.Relation^lyricsurl","Lyrics URL"},
			{"dc.Coverage^releasecountry","Release Country"},
			{"dc.Coverage^releaseyear","Release Year"}
		};
		
		return parseOut(identifier_mappings, null, song_metadata, true);
	}


	/**
	 * Produce new qualified Dublin Core-formatted artist metadata from the given
	 * set of artist metadata mined using jSongMiner.
	 *
	 * @param artist_metadata	The metadata to generate Dublin Core-formatted
	 *							metadata from.
	 * @return					A new vector of new MusicMetaData objects
	 *							holding Dublin Core-formatted metadata. A given
	 *							Dublin Core field may have multiple
	 *							MusicMetaData objects produced for it, but none
	 *							of them will have the same value. Note that the
	 *							source fields of the returned MusicMetaData are
	 *							set to "".
	 */
	public static Vector<MusicMetaData> getArtistQualifiedDublinCore(Vector<MusicMetaData> artist_metadata)
	{
		String[][] identifier_mappings =
		{
			{"dc.Title^artistname","Artist Name"},
			{"dc.Subject^artistrelatedtag","Artist-Related Tag"},
			{"dc.Subject^artistrelatedterm","Artist-Related Term"},
			{"dc.Description^lastfmartistwikitext","Last.FM Artist Wiki Text"},
			{"dc.Description^lastfmartistwikisummary","Last.FM Artist Wiki Summary"},
			{"dc.Description^artistbiography","Artist Biography"},
			{"dc.Description^artistrelatedurl","Artist-Related URL"},
			{"dc.Description^artistreview","Artist Review"},
			{"dc.Description^artistrelatednews","Artist-Related News"},
			{"dc.Description^artistrelatedblog","Artist-Related Blog"},
			{"dc.Description^lastfmartistplaycount","Last.FM Artist Play Count"},
			{"dc.Description^echonestartistfamiliarity","Echo Nest Artist Familiarity (0 to 1)"},
			{"dc.Description^echonestartisthotness","Echo Nest Artist Hotness (0 to 1)"},
			{"dc.Type^artistrelatedtag","Artist-Related Tag"},
			{"dc.Type^artistrelatedterm","Artist-Related Term"},
			{"dc.Identifier^musicbrainzartistid","Music Brainz Artist ID"},
			{"dc.Identifier^echonestartistid","Echo Nest Artist ID"},
			{"dc.Identifier^lastfmartisturl","Last.FM Artist URL"},
			{"dc.Identifier^7digitalartistid","7digital Artist ID"},
			{"dc.Identifier^playmeartistid","Play.me Artist ID"},
			{"dc.Relation^toptracks","Top Tracks"},
			{"dc.Relation^topalbums","Top Albums"},
			{"dc.Relation^artistrelatedaudio","Artist-Related Audio"},
			{"dc.Relation^artistrelatedvideo","Artist-Related Video"},
			{"dc.Relation^artistrelatedimage","Artist-Related Image"},
			{"dc.Relation^similarartist","Similar Artist"},
			{"dc.Coverage^artistlocation","Artist Location"},
			{"dc.Coverage^artistevent","Artist Event"},
			{"dc.Coverage^artistrelatednews","Artist-Related News"},
			{"dc.Coverage^artistrelatedblog","Artist-Related Blog"}
		};

		return parseOut(identifier_mappings, null, artist_metadata, true);
	}


	/**
	 * Produce new qualified Dublin Core-formatted album metadata from the given
	 * set of album metadata mined using jSongMiner.
	 *
	 * @param album_metadata	The metadata to generate Dublin Core-formatted
	 *							metadata from.
	 * @return					A new vector of new MusicMetaData objects
	 *							holding Dublin Core-formatted metadata. A given
	 *							Dublin Core field may have multiple
	 *							MusicMetaData objects produced for it, but none
	 *							of them will have the same value. Note that the
	 *							source fields of the returned MusicMetaData are
	 *							set to "".
	 */
	public static Vector<MusicMetaData> getAlbumQualifiedDublinCore(Vector<MusicMetaData> album_metadata)
	{
		String[][] identifier_mappings =
		{
			{"dc.Title^albumtitle","Album Title"},
			{"dc.Creator^artistname","Artist Name"},
			{"dc.Creator^musicbrainzartistid","Music Brainz Artist ID"},
			{"dc.Subject^albumrelatedtag","Album-Related Tag"},
			{"dc.Description^lastfmalbumwikitext","Last.FM Album Wiki Text"},
			{"dc.Description^lastfmalbumwikisummary","Last.FM Album Wiki Summary"},
			{"dc.Description^lastfmalbumplaycount","Last.FM Album Play Count"},
			{"dc.Date^releasedate","Release Date"},
			{"dc.Type^albumrelatedtag","Album-Related Tag"},
			{"dc.Identifier^musicbrainzalbumid","Music Brainz Album ID"},
			{"dc.Identifier^lastfmalbumurl","Last.FM Album URL"},
			{"dc.Coverage^releasedate","Release Date"},
		};
		
		return parseOut(identifier_mappings, null, album_metadata, true);
	}


	/* PRIVATE METHODS ********************************************************/


	/**
	 * Parse through the given original_metadata and return a new vector of
	 * MusicMetaData consisting of this data reformatted into Dublin Core.
	 * The identifier_mappings is used to generate the new Dublin Core
	 * field_identifiers based on the old ones, and to find the appropriate
	 * values. This method may be used for both qualified and unqualified
	 * Dublin Core. A given Dublin Core field may have multiple MusicMetaData
	 * objects produced for it if the field_identifier parameter is set to true,
	 * but none of them will have the same value.
	 *
	 * @param identifier_mappings	An array of size [x][2], where each [x][0]
	 *								indicates a Dublin Core field identifier
	 *								for metadata type x, and each [x][1]
	 *								indicates a SUBstring that will be found
	 *								in the field_identifier field of
	 *								MusicMetaData objects corresponding to
	 *								metadata of type x.
	 * @param alternate_mappings	Alternative mappings to use for Dublin Core
	 *								fields specified in the identifier_mappings
	 *								parameter if an [x][1] value is not found
	 *								for a given x. The keys of this hash map
	 *								are Dublin Core field identifies (i.e.
	 *								identifier_mappings [x][0] values). The
	 *								values are vectors of alternative
	 *								[x][0]/[x][1] pairs for the given x that
	 *								can be used if no matches were found for the
	 *								x in the original identifier_mappings. These
	 *								are ordered from best to worst. This
	 *								parameter may be null, in which case it is
	 *								ignored. This parameter is useful for
	 *								unqualified Dublin Core, but is typically
	 *								not used for qualified Dublin Core.
	 * @param original_metadata		The metadata to parse Dublin Core-formatted
	 *								metadata out of using the
	 *								identifier_mappings.
	 * @param field_identifier		If this is true, then each Dublin Core
	 *								metadata field is permitted to have multiple
	 *								values (i.e. there will be multiple
	 *								corresponding MusicMetaData objects in the
	 *								returned results). If it is false, then
	 *								only the first entry in original_metadata
	 *								that matches identifier_mappings [x][1]
	 *								will be stored in the returned results.
	 *								Typically this will be true for qualified
	 *								Dublin Core, and false for unqualified
	 *								Dublin Core.
	 * @return						A new vector holding Dublin Core-formatted
	 *								metadtata only. Note that the source fields
	 *								of the returned MusicMetaData are set to "".
	 */
	private static Vector<MusicMetaData> parseOut( String[][] identifier_mappings,
			HashMap<String,Vector<String[]>> alternate_mappings,
			Vector<MusicMetaData> original_metadata,
			boolean allow_multiple_values )
	{
		// The results to return
		Vector<MusicMetaData> results = new Vector<MusicMetaData>();

		// Return an empty array if there is no metadata to return
		if (original_metadata == null) return results;
		if (original_metadata.size() == 0) return results;

		// Parse out the original_metadata using the identifier_mappings,
		// removing duplicates
		for (int key = 0; key < identifier_mappings.length; key++)
		{
			// To hold the results for a single entry in identifier_mappings
			Vector<MusicMetaData> temp_results = new Vector<MusicMetaData>();

			// Parse out the metadata
			boolean found_one = false;
			for (int data = 0; data < original_metadata.size(); data++)
			{
				String this_field_identifier = original_metadata.get(data).getFieldIdentifier();
				if (this_field_identifier.indexOf(identifier_mappings[key][1]) != -1)
				{
					String this_value = original_metadata.get(data).getValue();
					MusicMetaData dcentry = new MusicMetaData("", identifier_mappings[key][0], this_value);
					temp_results.add(dcentry);
					found_one = true;

					if (!allow_multiple_values)
						data = original_metadata.size(); // exit the loop
				}

				// If there nothing was found for this DC field and there is an
				// alternative mapping for the field, then use it
				if (alternate_mappings != null)
				{
					if ( data == original_metadata.size() - 1 &&
					     !found_one &&
						 alternate_mappings.containsKey(identifier_mappings[key][0]) )
					{
						if (alternate_mappings.get(identifier_mappings[key][0]) != null)
						{
							if (alternate_mappings.get(identifier_mappings[key][0]).size() != 0)
							{
								String[] new_mapping_pair = alternate_mappings.get(identifier_mappings[key][0]).remove(0);
								identifier_mappings[key] = new_mapping_pair;
								data = -1;
							}
						}
					}
				}
			}

			// Remove duplicate identical values for the field
			for (int first = 0; first < temp_results.size(); first++)
			{
				for (int second = first + 1; second < temp_results.size(); second++)
				{
					String first_value = temp_results.get(first).getValue();
					String second_value = temp_results.get(second).getValue();
					if (first != second && first_value.equals(second_value))
					{
						temp_results.remove(second);
						second--;
					}
				}
			}

			// Store the remainder
			results.addAll(temp_results);
		}

		// Return the results
		return results;
	}


	/**
	 * Adds an entry to alternate_mappings with a key corresponding to
	 * dublin_core_field_name and a value corresponding to a new vector of
	 * [x,y] String pairs, where x is dublin_core_field_nam and y is each
	 * successive element of alternate_jsongminer_field_names.
	 *
	 * @param dublin_core_field_name			The Dublin Core field name.
	 * @param alternate_jsongminer_field_names	A list of alternative field
	 *											names that can correspond to
	 *											dublin_core_field_name, ranked
	 *											from best to worst.
	 * @param alternate_mappings				The hash map to add the new
	 *											vector to.
	 */
	private static void addToAlternateMappings( String dublin_core_field_name,
			String[] alternate_jsongminer_field_names,
			HashMap<String,Vector<String[]>> alternate_mappings )
	{
		Vector<String[]> alt_vector = new Vector(alternate_jsongminer_field_names.length);
		for (int i = 0; i < alternate_jsongminer_field_names.length; i++)
		{
			String[] alt_array = new String[2];
			alt_array[0] = dublin_core_field_name;
			alt_array[1] = alternate_jsongminer_field_names[i];
			alt_vector.add(alt_array);
		}
		alternate_mappings.put(dublin_core_field_name, alt_vector);
	}
}