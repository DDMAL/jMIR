/*
 * LastFMMiner.java
 * Version 1.0
 *
 * Last modified on August 4, 2010.
 * University of Waikato and McGill University
 */

package jsongminer;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.Event;
import net.roarsoftware.lastfm.Image;
import net.roarsoftware.lastfm.ImageSize;
import net.roarsoftware.lastfm.Tag;
import net.roarsoftware.lastfm.Track;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Vector;

/**
 * Objects of this class are used to extract information from the Last.FM API
 * with respect to a given song. Upon instantiation, an object of this class is
 * given identifying information about a song, and a Last.FM Track object is 
 * downloaded and stored from the Last.FM API. Further method calls to an object
 * of this LastFMMiner class can be used to extract and access further
 * information related to the song associated with this Track object. Each 
 * LastFMMiner object is associated with one and only one song.
 *
 * <p>Metadata is extracted from the Last.FM API, which is described at
 * http://www.last.fm/api. The queries are formatted and the results
 * interpereted using the last.fm-bindings library, which is available at
 * http://www.u-mass.de/lastfm.
 * 
 * @author Cory McKay
 */
public class LastFMMiner
{
	/* FIELDS *****************************************************************/


	/**
	 * The code used to indicate in MusicMetaData objects that a piece of data
	 * is from Last.FM.
	 */
	private String last_fm_source_identifier_code;

	/**
	 * The maximum number of items of any given field to include in reports.
	 */
	private static int max_to_report;

	/**
	 * The API key needed to access Last.FM web sevices.
	 */
	private String api_key;

	/**
	 * The Last.FM Track that is being analyzed.
	 */
	private Track track;


	/* CONSTRUCTOR ************************************************************/


	/**
	 * Initialize this object by setting up its fields. This includes accessing
	 * a Last.FM Track object from the Last.FM API based on the provided song
	 *title and artist name metadata.
	 *
	 * @param song_title		The title of the song. May not be null.
	 * @param artist_name		The "artist" of the song (typically refers
	 *							to performer/band, although it is sometimes
	 *							used to refer to composer, especially for
	 *							classical music). This may be null if this
	 *							information is unknown.
	 * @param api_key			The Last.FM API key.
	 * @throws Exception		An informative exception is thrown if invalid
	 *							parameters are provided or if a match could not
	 *							be found for the specified song title and
	 *							artist.
	 */
	public LastFMMiner(String song_title, String artist_name, String api_key)
			throws Exception
	{
		// Set basic fields
		last_fm_source_identifier_code = "Last.FM API";
		max_to_report = 10;
		this.api_key = api_key;

		// Retrieve Last.FM Track information
		setTrack(song_title, artist_name, false);
	}


	/* PUBLIC METHODS *********************************************************/


	/**
	 * Extract all available metadata on Last.FM for the Track stored in this
	 * object. This excludes metadata about the artist, however, which
	 * can be obtained using the getArtistMetaData method.
	 *
	 * @param store_fails	If this is true, then for each individual piece of
	 *						metadata that cannot be extracted from the Last.FM
	 *						API an indication is added to the returned
	 *						MusicMetaData array highlighting the failure. If
	 *						this is false then fields that cannot be extracted
	 *						are simply ignored.
	 * @return				An array holding references to all of the extracted
	 *						track metadata. Null is returned if no data could be
	 *						found.
	 */
	public MusicMetaData[] getSongMetaData(boolean store_fails)
	{
		// The metadata extracted to date by this method
		Vector<MusicMetaData> results = new Vector<MusicMetaData>();

		// Extract the song title
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Song Title", track.getName()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Song Title", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM Track URL
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Track URL", track.getUrl()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Track URL", "Could not extract this information: " + e.getMessage()));}

		// Extract the song Music Brainz ID
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Music Brainz Song ID", track.getMbid()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Song Music Brainz ID", "Could not extract this information: " + e.getMessage()));}

		// Extract the artist name
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist Name", track.getArtist()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist Name", "Could not extract this information: " + e.getMessage()));}

		// Extract the Artist Music Brainz ID
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Music Brainz Artist ID", track.getArtistMbid()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Music Brainz Artist ID", "Could not extract this information: " + e.getMessage()));}

		// Extract the album title
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Album Title", track.getAlbum()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Album Title", "Could not extract this information: " + e.getMessage()));}

		// Extract the Album Music Brainz ID
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Music Brainz Album ID", track.getAlbumMbid()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Music Brainz Album ID", "Could not extract this information: " + e.getMessage()));}

		// Extract the album track number
		try
		{
			int track_number_int = track.getPosition();
			String track_number = "Unknown";
			if (track_number_int != -1)
				track_number = (new Integer(track_number_int)).toString();
			results.add(new MusicMetaData(last_fm_source_identifier_code, "Album Track Number", track_number));
		}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Album Track Number", "Could not extract this information: " + e.getMessage()));}

		// Extract the song duration
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Duration (seconds)", (new Integer(track.getDuration())).toString()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Duration (seconds)", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM wiki summary
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Track Wiki Summary", track.getWikiSummary()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Track Wiki Summary", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM wiki text
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Track Wiki Text", track.getWikiText()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Track Wiki Text", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM song play count
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Track Play Count", (new Integer(track.getPlaycount())).toString()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Play Count", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM song-related tags
		try
		{
			List<Tag> tag_collection = Track.getTopTags(track.getArtist(), track.getName(), api_key);
			Tag[] tags = tag_collection.toArray(new Tag[tag_collection.size()]);
			int this_max = tags.length;
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
				results.add(new MusicMetaData(last_fm_source_identifier_code, "Song-Related Tag (Tag " + (i + 1) + ")", tags[i].getName() + " (Count: " + tags[i].getCount() + ") (Last.FM Tag URL: " + tags[i].getUrl() + ")"));
		}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Song-Related Tags", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM similar tracks
		try
		{
			Collection<Track> track_collection = Track.getSimilar(track.getArtist(), track.getName(), null, api_key);
			Track[] tracks = track_collection.toArray(new Track[track_collection.size()]);
			int this_max = tracks.length;
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
				results.add(new MusicMetaData(last_fm_source_identifier_code, "Similar Track (Track " + (i + 1) + ")", tracks[i].getName() + " (Last.FM TracK URL: " + tracks[i].getUrl() + ")"));
		}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Similar Tracks", "Could not extract this information: " + e.getMessage()));}

		// Return the extracted metadata
		if (results.isEmpty()) return null;
		else return results.toArray(new MusicMetaData[results.size()]);
	}


	/**
	 * Extract all available on Last.FM metadata for the artist associated with
	 * the Track stored in this object.
	 *
	 * @param store_fails	If this is true, then for each individual piece of
	 *						metadata that cannot be extracted from the Last.FM
	 *						API an indication is added to the returned
	 *						MusicMetaData array highlighting the failure. If
	 *						this is false then fields that cannot be extracted
	 *						are simply ignored.
	 * @return				An array holding references to all of the extracted
	 *						artist metadata. Null is returned if no data could
	 *						be found.
	 */
	public MusicMetaData[] getArtistMetaData(boolean store_fails)
	{
		// The metadata extracted to date by this method
		Vector<MusicMetaData> results = new Vector<MusicMetaData>();

		// Prepare a Last.FM Artist object
		Artist artist = Artist.getInfo(track.getArtist(), api_key);

		// Extract the artist name
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist Name", artist.getName()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist Name", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM Artist URL
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Artist URL", artist.getUrl()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Artist URL", "Could not extract this information: " + e.getMessage()));}

		// Extract the Artist Music Brainz ID
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Music Brainz Artist ID", artist.getMbid()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Music Brainz Artist ID", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM artist wiki summary
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Artist Wiki Summary", artist.getWikiSummary()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Artist Wiki Summary", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM artist wiki text
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Artist Wiki Text", artist.getWikiText()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Artist Wiki Text", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM artist-related image files
		try
		{
			Collection<Image> image_collection = (Artist.getImages(artist.getName(), api_key)).getPageResults();
			Image[] images = image_collection.toArray(new Image[image_collection.size()]);
			int this_max = images.length;
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
				results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist-Related Image (" + images[i].getUrl() + ")", images[i].getImageURL(ImageSize.valueOf("LARGE"))));
		}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist-Related Images", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM artist play count
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Artist Play Count", (new Integer(artist.getPlaycount())).toString()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Aritst Play Count", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM top tracks
		try
		{
			Collection<Track> track_collection = Artist.getTopTracks(artist.getName(), api_key);
			Track[] tracks = track_collection.toArray(new Track[track_collection.size()]);
			int this_max = tracks.length;
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
				results.add(new MusicMetaData(last_fm_source_identifier_code, "Top Tracks (Track " + (i + 1) + ")", tracks[i].getName() + " (Last.FM TracK URL: " + tracks[i].getUrl() + ")"));
		}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Top Tracks", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM top albums
		try
		{
			Collection<Album> album_collection = Artist.getTopAlbums(artist.getName(), api_key);
			Album[] albums = album_collection.toArray(new Album[album_collection.size()]);
			int this_max = albums.length;
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
				results.add(new MusicMetaData(last_fm_source_identifier_code, "Top Albums (Album " + (i + 1) + ")", albums[i].getName() + " (Last.FM Album URL: " + albums[i].getUrl() + ")"));
		}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Top Albums", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM artist-related tags
		try
		{
			Collection<String> tag_collection = Artist.getTopTags(artist.getName(), api_key);
			String[] tags = tag_collection.toArray(new String[tag_collection.size()]);
			int this_max = tags.length;
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
				results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist-Related Tag (Tag " + (i + 1) + ")", tags[i]));
		}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist-Related Tags", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM similar artists
		try
		{
			Collection<Artist> artist_collection = Artist.getSimilar(artist.getName(), max_to_report, api_key);
			Artist[] artists = artist_collection.toArray(new Artist[artist_collection.size()]);
			for (int i = 0; i < artists.length; i++)
				results.add(new MusicMetaData(last_fm_source_identifier_code, "Similar Artist (Artist " + (i + 1) + ")", artists[i].getName() + " (Last.FM Artist URL: " + artists[i].getUrl() + ")"));
		}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Similar Artists", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM artist upcoming events
		try
		{
			Collection<Event> event_collection = Artist.getEvents(artist.getName(), api_key);
			Event[] events = event_collection.toArray(new Event[event_collection.size()]);
			int this_max = events.length;
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
				results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist Event (" + events[i].getUrl() + ")", events[i].getTitle() + " (Date: " + events[i].getStartDate() + ")" + " (Venue: " + events[i].getVenue().getName() + ", " + events[i].getVenue().getCity() + ", " + events[i].getVenue().getCountry() + ")"));
		}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist Events", "Could not extract this information: " + e.getMessage()));}

		// Return the extracted metadata
		if (results.isEmpty()) return null;
		else return results.toArray(new MusicMetaData[results.size()]);
	}


	/**
	 * Extract all available on Last.FM metadata for the album associated with
	 * the Track stored in this object.
	 *
	 * @param store_fails	If this is true, then for each individual piece of
	 *						metadata that cannot be extracted from the Last.FM
	 *						API an indication is added to the returned
	 *						MusicMetaData array highlighting the failure. If
	 *						this is false then fields that cannot be extracted
	 *						are simply ignored.
	 * @return				An array holding references to all of the extracted
	 *						album metadata. Null is returned if no data could
	 *						be found.
	 */
	public MusicMetaData[] getAlbumMetaData(boolean store_fails)
	{
		// The metadata extracted to date by this method
		Vector<MusicMetaData> results = new Vector<MusicMetaData>();

		// Prepare a Last.FM Artist object
		Album album = Album.getInfo(track.getArtist(), track.getAlbum(), api_key);

		// Extract the album name
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Album Title", album.getName()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Album Title", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM Album URL
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Album URL", album.getUrl()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Album URL", "Could not extract this information: " + e.getMessage()));}

		// Extract the Album Music Brainz ID
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Music Brainz Album ID", album.getMbid()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Album Album Brainz ID", "Could not extract this information: " + e.getMessage()));}

		// Extract the artist name
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist Name", album.getArtist()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Artist Name", "Could not extract this information: " + e.getMessage()));}

		// Extract the Artist Music Brainz ID
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Music Brainz Artist ID", track.getArtistMbid()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Music Brainz Artist ID", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM album release date
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Release Date", (album.getReleaseDate()).toString()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Release Date", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM album wiki summary
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Album Wiki Summary", album.getWikiSummary()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Album Wiki Summary", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM albumb wiki text
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Album Wiki Text", album.getWikiText()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Album Wiki Text", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM album play count
		try {results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Album Play Count", (new Integer(album.getPlaycount())).toString()));}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Last.FM Album Play Count", "Could not extract this information: " + e.getMessage()));}

		// Extract the Last.FM album-related tags
		try
		{
			Collection<String> tag_collection = album.getTags();
			String[] tags = tag_collection.toArray(new String[tag_collection.size()]);
			int this_max = tags.length;
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
				results.add(new MusicMetaData(last_fm_source_identifier_code, "Album-Related Tag (Tag " + (i + 1) + ")", tags[i]));
		}
		catch (Exception e) {if (store_fails) results.add(new MusicMetaData(last_fm_source_identifier_code, "Album-Related Tags", "Could not extract this information: " + e.getMessage()));}

		// Return the extracted metadata
		if (results.isEmpty()) return null;
		else return results.toArray(new MusicMetaData[results.size()]);
	}


	/**
	 * Extracts all available song and, if appropriate, artist and album
	 * metadata from the Last.FM API for the Track stored in this object. Adds
	 * the extracted metadata to the Vector parameters of this method.
	 *
	 * <p>Song metadata is always extracted. Artist and album metadata are only
	 * extracted if they have not already been extracted for the given artist
	 * or album (respectively) during the processing of another song, as
	 * indicated by the presence of the artist's name (in lower case) in the
	 * artists_already_extracted parameter, or the album's title (in lower case)
	 * in the albums_already_extracted parameter, respectively. Note that
	 * this method does <b>not</b> update either of these hash maps in any way,
	 * as it is left to the calling object to do this.
	 *
	 * @param song_metadata					A Vector of metadata already
	 *										extracted for this song. May not
	 *										be null, but may be empty.
	 * @param artist_metadata				A Vector of metadata already
	 *										extracted for the artist associated
	 *										with this song. May not be null, but
	 *										may be empty.
	 * @param album_metadata				A Vector of metadata already
	 *										extracted for the album associated
	 *										with this song. May not be null, but
	 *										may be empty.
	 * @param artists_already_extracted		A hash map whose keys correspond to
	 *										artist names (converted to lower
	 *										case), and whose values correspond
	 *										to vectors of corresponding artist
	 *										metadata. The contents are based on
	 *										the processing of previous songs.
	 *										This parameter may be null, in which
	 *										case it is ignored	and artist
	 *										extraction happens automatically.
	 *										Also, the values may be null, in
	 *										order to save space and processing
	 *										for the calling object, but this has
	 *										no effect on this method, as only
	 *										the keys are used.
	 * @param albums_already_extracted		A hash map whose keys correspond to
	 *										album titles (converted to lower
	 *										case), and whose values correspond
	 *										to vectors of corresponding album
	 *										metadata. The contents are based on
	 *										the processing of previous songs.
	 *										This parameter may be null, in which
	 *										case it is ignored	and album
	 *										extraction happens automatically.
	 *										Also, the values may be null, in
	 *										order to save space and processing
	 *										for the calling object, but this has
	 *										no effect on this method, as only
	 *										the keys are used.
	 * @param store_fails					If this is true, then for each
	 *										individual piece of metadata that
	 *										cannot be extracted from the Last.FM
	 *										API an indication is added to
	 *										the stored MusicMetaData
	 *										highlighting the failure. If this is
	 *										false then fields that cannot
	 *										be extracted are simply ignored.
	 *										Note that this parameter has no
	 *										effect on song identification
	 *										specifically.
	 * @throws Exception					An exception is thrown if invalid
	 *										parameters are passed.
	 * @return								True is returned if a change was
	 *										made to song_metadata,
	 *										artist_metadata or both.
	 */
	public boolean getAllAvailableNewMetaData( Vector<MusicMetaData> song_metadata,
			Vector<MusicMetaData> artist_metadata,
			Vector<MusicMetaData> album_metadata,
			HashMap< String, Vector<MusicMetaData> > artists_already_extracted,
			HashMap< String, Vector<MusicMetaData> > albums_already_extracted,
			boolean store_fails )
			throws Exception
	{
		// Verify input parameters
		if (song_metadata == null || artist_metadata == null || album_metadata == null)
			throw new Exception ("Song, artist and album vectors must be provided (although they may be empty).");

		// Whether or not a change has been made
		boolean some_data_successfully_extracted = false;

		// Extract song metadata
		MusicMetaData[] new_song_metadata = getSongMetaData(store_fails);

		// Add all song metadata extracted so far to the provided Vector
		if (new_song_metadata != null)
		{
			for (int i = 0; i < new_song_metadata.length; i++)
				song_metadata.add(new_song_metadata[i]);
			some_data_successfully_extracted = true;
		}

		// Test if should extract artist metadata
		boolean extract_artist_data = true;
		if (artists_already_extracted != null)
		{
			String artist_name = track.getArtist();
			if (artist_name == null)
			{
				extract_artist_data = false;
			}
			else
			{
				artist_name = artist_name.toLowerCase();
				if (artists_already_extracted.containsKey(artist_name))
					extract_artist_data = false;
			}
		}

		// Extract artist metadata and store it, if appropriate
		if (extract_artist_data)
		{
			// Extract artist metadat for the specified song
			MusicMetaData[] new_artist_data = getArtistMetaData(store_fails);

			if (new_artist_data != null)
			{
				// Add artist metadata to the artist_metadata Vector
				for (int i = 0; i < new_artist_data.length; i++)
					artist_metadata.add(new_artist_data[i]);

				// Note that a change has been made
				some_data_successfully_extracted = true;
			}
		}

		// Test if should extract album metadata
		boolean extract_album_data = true;
		if (albums_already_extracted != null)
		{
			String album_name = track.getAlbum();
			if (album_name == null)
			{
				extract_album_data = false;
			}
			else
			{
				album_name = album_name.toLowerCase();
				if (albums_already_extracted.containsKey(album_name))
					extract_album_data = false;
			}
		}

		// Extract album metadata and store it, if appropriate
		if (extract_album_data)
		{
			// Extract artist metadat for the specified song
			MusicMetaData[] new_album_data = getAlbumMetaData(store_fails);

			if (new_album_data != null)
			{
				// Add album metadata to the album_metadata Vector
				for (int i = 0; i < new_album_data.length; i++)
					album_metadata.add(new_album_data[i]);

				// Note that a change has been made
				some_data_successfully_extracted = true;
			}
		}		

		// Return whether some data was successfully extracted
		return some_data_successfully_extracted;
	}


	/**
	 * Return the Last.FM Track URL for the song referred to by this object.
	 *
	 * @return	The Last.FM Track URL for this song. Null is returned if this
	 *			information could not be provided.
	 */
	public String getLastFMTrackURL()
	{
		try {return track.getUrl();}
		catch (Exception e) {return null;}
	}


	/**
	 * Return the song title, artist name and album title for the song referred
	 * to by this object.
	 *
	 * @return	The song title, artist name and album title (in that order).
	 *			Null is returned if this information could not be provided.
	 */
	public String[] getIdentifyingMetadataForSong()
	{
		try
		{
			String[] results = {track.getName(), track.getArtist(), track.getAlbum()};
			return results;
		}
		catch (Exception e) {return null;}
	}


	/**
	 * Get the album name for the album associated with the song held in this
	 * object
	 *
	 * @return			The name of the album, or null if it could not be
	 *					found or a problem occured. Note that the album's name
	 *					is converted to lower case in the returned string.
	 */
	public String getAlbumNameLowerCase()
	{
		try
		{
			String result = track.getAlbum();
			result = result.toLowerCase();
			return result;
		}
		catch (Exception e) {return null;}
	}



	/* PRIVATE METHODS ********************************************************/


	/**
	 * Retrieve a Last.FM Track with the given song title and, optionally,
	 * artist name, and store it in this object's track field.
	 *
	 * @param song_title		The title of the song. May not be null.
	 * @param artist_name		The "artist" of the song (typically refers
	 *							to performer/band, although it is sometimes
	 *							used to refer to composer, especially for
	 *							classical music). This may be null if this
	 *							information is unknown.
	 * @param called_already	Whether or not this method has already been
	 *							called. Needed to preven an infinite loop.
	 * @param api_key			The Last.FM API key.
	 * @throws Exception		An informative exception is thrown if invalid
	 *							parameters are provided or if a match could not
	 *							be found for the specified song title and
	 *							artist.
	 */
	private void setTrack( String song_title,
			String artist_name,
			boolean called_already )
			throws Exception
	{
		// Verify that a song title is specified
		if (song_title == null)
			throw new Exception("A song title must be specified in order to attempt to identify it.");
		if (song_title.equals(""))
			throw new Exception("A song title must be specified in order to attempt to identify it.");

		// Retrieve the Track if an artist is specified
		if (artist_name != null)
		{
			track = Track.getInfo(artist_name, song_title, api_key);
			if (track == null)
				throw new Exception("Could not find a Last.FM track matching " + song_title + " by " + artist_name);
		}

		// Retrieve the Track recursively if an artist is not specified. This
		// is done because the Track.getInfo method typically returns more
		// information than the Track.search method
		else
		{
			Collection<Track> tracks = Track.search(song_title, api_key);
			if (tracks.isEmpty())
				throw new Exception("Could not find a Last.FM track matching " + song_title);
			Iterator<Track> tracks_iterator = tracks.iterator();
			Track temp_track = tracks_iterator.next();

			if (!called_already)
				setTrack(temp_track.getName(), temp_track.getArtist(), true);
			else
				track = temp_track;
		}
	}
}