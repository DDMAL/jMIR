/*
 * EchoNestMiner.java
 * Version 1.0
 *
 * Last modified on August 4, 2010.
 * University of Waikato and McGill University
 */
package jsongminer;

import com.echonest.api.v4.Artist;
import com.echonest.api.v4.Audio;
import com.echonest.api.v4.Biography;
import com.echonest.api.v4.Blog;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.Image;
import com.echonest.api.v4.News;
import com.echonest.api.v4.Params;
import com.echonest.api.v4.Review;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.Term;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.Video;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import mckay.utilities.staticlibraries.MiscellaneousMethods;
import mckay.utilities.staticlibraries.NetworkMethods;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Objects of this class are used to identify songs and extract information
 * about them from the Echo Nest API. This class inculdes methods for
 * identifying unknown audio files using the Echo Nest's fingerprinting codegen
 * binary and/or the Echo Nest's remote fingerprinting service. There is also a
 * method for identifying audio files using known metadata (e.g. data extracted
 * from ID3 fields). Methods are also included for extracting additional kinds
 * of information from the Echo Nest API.
 *
 * <p>Calling the getAndStoreEchoNestSongIDUsingBestAvailableMethod method is
 * generally the best approach to use for identifying an unknown song. Once this
 * is done, other methods can be called to extract additional information about
 * the song, such as getAndStoreSongMetaData and getAndStoreArtistMetaData.
 * Better yet, the getAllAvailableNewMetaData method can be called to get all
 * appropriate and available metadata once the Echo Nest Song ID has been
 * acquired for a song.
 *
 * <p>Each object of this class maintains a private music_meta_data field that
 * holds a vector of MusicMetaData objects. Each method call to an EchoNestMiner
 * object gives the user the option of having the data extracted by the given
 * method be stored in this vector. If this is done, then all of the extracted
 * data to date can be accessed from this vector at a later time. Information
 * for multiple songs could be stored in this way, but in practice it is best to
 * store only information for a single song in each EchoNestMiner object. Echo
 * Nest song IDs are used as unique identifiers in objects of this class.
 *
 * <p>Local fingerprinting is done using the Echo Nest codegen binary, which may
 * be downloaded (with permission) from http://groups.google.com/group/enmfp.
 * Metadata extraction is done using the Echo Nest web services API, which is
 * outlined http://developer.echonest.com/docs/v4/. The jEN-api library,
 * available http://code.google.com/p/jen-api/, is used to send requests and
 * interperet returned results from the Echo Nest API. Also, the JSON.simple
 * library, described at http://code.google.com/p/json-simple/, is used to parse
 * JSON-formatted data.
 *
 * @author Cory McKay
 */
public class EchoNestMiner
{
	/* FIELDS *****************************************************************/


	/**
	 * The data that has been extracted from the Echo Nest so far.
	 */
	private Vector<MusicMetaData> music_meta_data;

	/**
	 * The code used to indicate in MusicMetaData objects that a piece of data
	 * is from the Echo Nest. Note that the code will append 
	 * " (Local Fingerprinter)", " (Remote Fingerprinter)" or
	 * " (Embedded Metadata Query)" to this when identifying a song (as opposed
	 * to other kinds of extraction, where this is left unaltered.
	 */
	private String echo_nest_source_identifier_code;

	/**
	 * The maximum number of items of any given field to include in reports.
	 */
	private static int max_to_report;

	/**
	 * The API key needed to access Echo Nest web sevices.
	 */
	private String api_key;

	/**
	 * Used to query the Echo Nest API.
	 */
	private EchoNestAPI echo_nest_api;


	/* CONSTRUCTOR ************************************************************/


	/**
	 * Initialize this object and store needed data in fields.
	 *
	 * @param api_key			The Echo Nest API key.
	 * @throws Exception		An exception is thrown if a problem occurs.
	 */
	public EchoNestMiner(String api_key)
			throws Exception
	{
		echo_nest_source_identifier_code = "Echo Nest API";

		max_to_report = 10;

		this.api_key = api_key;

		music_meta_data = new Vector<MusicMetaData>();

		echo_nest_api = new EchoNestAPI(api_key);
		echo_nest_api.setTraceSends(false);
		echo_nest_api.setTraceRecvs(false);
	}


	/* PUBLIC METHODS *********************************************************/


	/**
	 * Return an array holding all of the metadata that has been extracted and
	 * stored internally by this object so far.
	 * 
	 * @return	The extracted metadata so far. Null if none is available.
	 */
	public MusicMetaData[] getExtractedMetadata()
	{
		if (music_meta_data.isEmpty()) return null;
		else return music_meta_data.toArray(new MusicMetaData[music_meta_data.size()]);
	}
	
	
	/**
	 * Extract and return the full JSON dictionary output by the the Echo Nest
	 * fingerprinting codegen for the specified file. This dictionary is also
	 * stored internally in this object's music_meta_data field if the  
	 * store_result parameter is true. Default values are used for the
	 * audio offset and duration to process.
	 *
	 * @param music_file_path		The path of the audio file to find a
	 *								fingerprint for.
	 * @param store_result			Whether or not to add a MusicMetaData object
	 *								holding the JSON dictionary generated to the
	 *								internal music_metadata Vector field
	 *								(assuming succesful extraction).
	 * @param run_script_path		The path of the Echo Nest codegen binary, or
	 *								a script to run it.
	 *								e.g. "./ENFP_Codegen.sh"
	 * @param en_codegen_directory	The path of the directory holding the
	 *								Echo Nest codegen fingerprinting binary.
	 *								e.g. "/home/ENMF/"
	 * @return						The JSON dictionary output by the the Echo
	 *								Nest fingerprinting codegen. This includes
	 *								both metadata extracted from annotations
	 *								embedded in the file, as well as the 
	 *								fingerprint resulting from audio analysis
	 *								itself. Null is returned if no results were
	 *								generated.
	 * @throws Exception			An exception is thrown if a problem occurs.
	 */
	public String getAndStoreFullFingerpintingCodegenOutput(String music_file_path,
			boolean store_result,
			String run_script_path,
			String en_codegen_directory )
			throws Exception
	{
		// Set defaults
		String offset_from_beginning = "10";
		String duration_of_audio = "30";

		// Perform the processing
		return getAndStoreFullFingerpintingCodegenOutput( music_file_path,
			offset_from_beginning,
			duration_of_audio,
			store_result,
			run_script_path,
			en_codegen_directory );
	}


	/**
	 * Extract and return the full JSON dictionary output by the the Echo Nest
	 * fingerprinting codegen for the specified file. This dictionary is also
	 * stored internally in this object's music_meta_data field if the
	 * store_result parameter is true.
	 *
	 * @param music_file_path		The path of the audio file to find a
	 *								fingerprint for.
	 * @param offset_from_beginning	The offset in seconds from the beginning of
	 *								the file indicating where to begin
	 *								fingerprinting. e.g. "10"
	 * @param duration_of_audio		The duration of audio to process after the
	 *								offset_from_beginning. e.g. "50"
	 * @param store_result			Whether or not to add a MusicMetaData object
	 *								holding the JSON dictionary generated to the
	 *								internal music_metadata Vector field
	 *								(assuming succesful extraction).
	 * @param run_script_path		The path of the Echo Nest codegen binary, or
	 *								a script to run it.
	 *								e.g. "./ENFP_Codegen.sh"
	 * @param en_codegen_directory	The path of the directory holding the
	 *								Echo Nest codegen fingerprinting binary.
	 *								e.g. "/home/ENMF/"
	 * @return						The JSON dictionary output by the the Echo
	 *								Nest fingerprinting codegen. This includes
	 *								both metadata extracted from annotations
	 *								embedded in the file, as well as the 
	 *								fingerprint resulting from audio analysis
	 *								itself. Null is returned if no results were
	 *								generated.
	 * @throws Exception			An exception is thrown if a problem occurs.
	 */
	public String getAndStoreFullFingerpintingCodegenOutput(String music_file_path,
			String offset_from_beginning,
			String duration_of_audio,
			boolean store_result,
			String run_script_path,
			String en_codegen_directory )
			throws Exception
	{
		// Throw an exception for invalid input
		if (run_script_path == null)
			throw new Exception("The path to the script for running the Echo Nest fingerprinting codegen is not specified.");
		if (en_codegen_directory == null)
			throw new Exception("The path to the directory holding the Echo Nest fingerprinting codegen is not specified.");

		// Prepare the exection string needed to run the Echo Nest codegen 
		// binary. If run_script_path ends with the filename of an Echo Nest
		// codegen executable, then the exection string is set to run the
		// codegen binary directly. Otherwise, the execution string is set to
		// run a script whose first argument is the directory holding the 
		// codegen binary, and whose remaining arguments are the arguments to be
		// used by the codegen binary.
		String execution_string;
		if ( run_script_path.endsWith("codegen.windows.exe") ||
		     run_script_path.endsWith("codegen.Linux-i686") ||
			 run_script_path.endsWith("codegen.Linux-x86_64") )
		{
			execution_string = run_script_path + " " +
					music_file_path + " " +
					offset_from_beginning + " " +
					duration_of_audio;
		}
		else
		{
			execution_string = run_script_path + " " +
					en_codegen_directory + " " +
					music_file_path + " " +
					offset_from_beginning + " " +
					duration_of_audio;
		}

		// Run the script that runs the Echo Nest codegen binary
		Runtime run_time = Runtime.getRuntime();
		String[] script_output = MiscellaneousMethods.runCommand(execution_string, run_time, null, null);

		// Parse the output to hold only the JSON output of the Echo Nest
		// codegen binary
		if (script_output != null)
		{
			for (int i = 0; i < script_output.length; i++)
			{
				if (script_output[i].startsWith("{"))
				{
					// Store the result in the music_meta_data field if appropriate
					if (store_result)
					{
						MusicMetaData result = new MusicMetaData(echo_nest_source_identifier_code + " (Local Fingerprinter)",
							"Full JSON Dictionary Output by the Echo Nest Fingerprinting Codegen Binary",
							script_output[i]);
						music_meta_data.add(result);
					}

					// Return the JSON dictionary
					return script_output[i];
				}
			}
		}
		return null;
	}


	/**
	 * Given the JSON dictionary output of the Echo Nest fingerprinting codegen,
	 * this method extracts and returns the ENMF fingerprint hash from it. This
	 * hash is also stored internally in this object's music_meta_data field if
	 * the store_result parameter is true.
	 *
	 * @param codegen_json_output	The JSON dictionary that is output by the
	 *								Echo Nest fingerprinting codegen.
	 * @param store_result			Whether or not to add a MusicMetaData object
	 *								holding the ENMF hash extracted by this
	 *								method to the internal music_metadata Vector
	 *								field (assuming succesful extraction).
	 * @return						The ENMF fingerprint hash contained in the
	 *								given JSON dictionary. Null if it cannot be
	 *								extracted.
	 */
	public String getAndStoreENMFHash( String codegen_json_output,
			boolean store_result )
	{
		// Parse out the fingerprint hash and store it, if appropriate
		MusicMetaData enmf_md = extractFieldValueFromEchoNestJSON( codegen_json_output,
				new String[]{"code"},
				"ENMF Fingerprint Hash Output by the Echo Nest Fingerprinting Codegen Binary",
				store_result );

		// Return null if nothing was found
		if (enmf_md == null)
			return null;

		// Return the fingerprint hash
		return enmf_md.getValue();
	}


	/**
	 * Use the provided ENMF fingerprint hash to identify a song. The full JSON
	 * dictionary response to a Echo Nest Song Identify query is returned. It is
	 * also stored internally in this object's music_meta_data field if the
	 * store_result parameter is true.
	 *
	 * @param enmf_hash		An ENMF fingerprint hash that is embedded in the
	 *						JSON dictionary produced by processin a piece of
	 *						audio with the Echo Nest fingerprinting codegen.
	 * @param store_result	Whether or not to add a MusicMetaData object
	 *						holding the response to the query sumbitted by this
	 *						method to the internal music_metadata Vector
	 *						field (assuming succesful extraction).
	 * @return				The full JSON dictionrary response by the Echo Nest
	 *						API to the Song Identify query. Null is returned if
	 *						a problem occurs (however, null is NOT, returned if
	 *						the Echo Nest simply does not get a match, as this
	 *						results in a JSON response).
	 */
	public String getAndStoreFullSongIdentifyResponse( String enmf_hash,
			boolean store_result )
	{
		// The Echonest URL to query
		String api_url = "http://developer.echonest.com/api/v4/";
		String api_command = "song/identify";
		String url_to_access = api_url + api_command;

		// The query to submit to the EchoNest
		String api_prefix = "api_key=";
		String enfp_prefix = "&code=";
		String data_to_send = api_prefix + api_key + enfp_prefix + enmf_hash;

		try
		{
			// Query the Echo Nest API
			String response = NetworkMethods.sendGetRequest(url_to_access, data_to_send);

			// Store the result if appropriate
			if (store_result)
			{
				MusicMetaData result = new MusicMetaData( echo_nest_source_identifier_code + " (Local Fingerprinter)",
					"Full Echo Nest Song Identify JSON Dictionary Response",
					response );
				music_meta_data.add(result);
			}
			
			// Return the result
			return response;
		}

		// Return null if a problem occured
		catch (Exception e)
		{
			// e.printStackTrace();
			return null;
		}
	}


	/**
	 * Given the JSON dictionary output of the Echo Nest Song Idenify API,
	 * this method extracts and returns the Echo Nest Song ID code from it. This
	 * ID is also stored internally in this object's music_meta_data field if
	 * the store_result parameter is true.
	 *
	 * @param song_identify_json_output	The JSON dictionary that is output by
	 *									the Echo Nest Song Identify API.
	 * @param store_result				Whether or not to add a MusicMetaData
	 *									object holding the Song ID extracted by
	 *									this method to the internal
	 *									music_metadata Vector field (assuming
	 *									succesful extraction).
	 * @return							The Echo Nest Song ID contained in the
	 *									given JSON dictionary. Null if it
	 *									cannot be extracted.
	 */
	public String getAndStoreEchoNestSongIDFromFingerPrint( String song_identify_json_output,
			boolean store_result )
	{
		// Parse out the song ID and store it, if appropriate
		MusicMetaData result = extractFieldValueFromEchoNestJSON( song_identify_json_output,
				new String[]{"response", "songs", "0", "id"},
				"Echo Nest Song ID",
				store_result );

		// Return null if nothing was found
		if (result == null)
			return null;

		// Return the fingerprint hash
		return result.getValue();
	}


	/**
	 * Given the path to an audio file, extract its ENMF fingerprint hash using
	 * the locally accessible Echo Nest's binary codegen, and then use the Echo
	 * Nest API to extract the Echo Nest Song ID for the specified audio using
	 * this fingerprint hash. Depending on the vaule of the store_results field,
	 * the fingerprint hash and the Song ID may be stored internally in this
	 * object's music_meta_data field.
	 *
	 * @param music_file_path		The path of the audio file to identify.
	 * @param store_results			Whether or not to add a MusicMetaData object
	 *								holding the ENMF hash and the Echo Nest
	 *								Song ID to the internal music_metadata
	 *								Vector field (assuming succesful
	 *								extraction). Note that there are other types
	 *								of intermediate data that can be stored,
	 *								but are not regardless of the value of this
	 *								parameter, namely the raw JSON strings used
	 *								by the Echo Nest. If one wishes to store
	 *								these, they may be extracted using other
	 *								methods of this class.
	 * @param run_script_path		The path of the Echo Nest codegen binary, or
	 *								a script to run it.
	 *								e.g. "./ENFP_Codegen.sh"
	 * @param en_codegen_directory	The path of the directory holding the
	 *								Echo Nest codegen fingerprinting binary.
	 *								e.g. "/home/ENMF/"
	 * @return						The Echo Nest Song ID of the specified
	 *								audio file. Null if it cannot be extracted.
	 */
	public String getAndStoreEchoNestSongIDFromLocalFingerPrint( String music_file_path,
			boolean store_results,
			String run_script_path,
			String en_codegen_directory)
	{
		try
		{
			String codegen_json_output = getAndStoreFullFingerpintingCodegenOutput(music_file_path,
				false,
				run_script_path,
				en_codegen_directory );
			// System.out.println("CODEGEN JSON OUTPUT: " + codegen_json_output + "\n");

			String enmf_hash = getAndStoreENMFHash(codegen_json_output, store_results);
			// System.out.println("ENMF HASH: " + enmf_hash + "\n");

			String song_identify_json_response = getAndStoreFullSongIdentifyResponse(enmf_hash, false);
			// System.out.println("SONG IDENTIFY JSON RESPONSE: " + song_identify_json_response + "\n");

			String en_song_id = getAndStoreEchoNestSongIDFromFingerPrint(song_identify_json_response, store_results);
			// System.out.println("EN SONG ID: " + en_song_id + "\n");

			return en_song_id;
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			return null;
		}
	}


	/**
	 * Given the path to an audio file, upload this file to the Echo Nest and
	 * have it dentify the song remotely using fingerprinting. The song title
	 * and artist name are extracted from this, and the Echo Nest Song ID is
	 * then extracted based on these two metadata fields. Depending on the vaule
	 * of the store_result field, the Song ID may be stored internally in this
	 * object's music_meta_data field.
	 *
	 * <p>Note that since it is only possible to base the Song ID on the song
	 * title and artist name metadata (even though the Echo Nest Track from
	 * which they are derived does not itself have this limitation) there may
	 * be confusion as to the particular version of the song if there are
	 * multiple recordings available for the same artist.
	 *
	 * <p>This method is thus less reliable than the
	 * getAndStoreEchoNestSongIDFromLocalFingerPrint method, but more reliable
	 * than the getAndStoreEchoNestSongIDFromMetadata method, since the latter
	 * makes use of potentially very noisy embedded metadata.
	 *
	 * @param music_file_path	The path of the audio file to identify.
	 * @param store_result		Whether or not to add a MusicMetaData object
	 *							holding the Echo Nest Song ID to the internal
	 *							music_metadata Vector field (assuming succesful
	 *							extraction).
	 * @return					The Echo Nest Song ID of the specified
	 *							audio file. Null if it cannot be extracted.
	 */
	public String getAndStoreEchoNestSongIDFromRemoteFingerPrint( String music_file_path,
			boolean store_result )
	{
		try
		{
			// Upload the track
			Track track = echo_nest_api.uploadTrack(new File(music_file_path), true);

			// Get the song title and artist name
			String song_title = track.getTitle();
			String artist_name = track.getArtistName();

			// Get the Song ID
			String song_id = getAndStoreEchoNestSongIDFromMetadata(song_title,
					artist_name,
					false);

			// Store the result if appropriate
			if (store_result)
			{
				MusicMetaData result = new MusicMetaData( echo_nest_source_identifier_code + " (Remote Fingerprinter)",
					"Echo Nest Song ID",
					song_id );
				music_meta_data.add(result);
			}

			// Return the result
			return song_id;
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			return null;
		}
	}


	/**
	 * Use the provided metadata to extract the Echo Nest Song ID from the
	 * Echo Nest API. Note that this is not done with reference to any audio
	 * samples that may be available locally (this fingerprinting methods of
	 * this class should be used if one wishes to identify a song based on its
	 * audio). This method is best used when fingerprinting is unavailable or
	 * is unsuccesful. Depending on the vaule of the store_results field, the
	 * Song ID may be stored internally in this object's music_meta_data field.
	 *
	 * <p>Note that since it is only possible to specify song title and artist
	 * name that there may be confusion as to the particular version of the
	 * song if there are multiple recordings available for the same artist.
	 *
	 * @param song_title	The title of the song. This may not be null.
	 * @param artist_name	The "artist" of the song (typically refers to
	 *						performer/band, although it is sometimes used to
	 *						refer to composer, especially for classical music).
	 *						This may be null if this information is unknown.
	 * @param store_result	Whether or not to add a MusicMetaData object
	 *						holding the Echo Nest Song ID to the internal
	 *						music_metadata Vector field (assuming succesful
	 *						extraction).
	 * @return				The Echo Nest Song ID of the song that the Echo
	 *						Nest API matches to the specified metadata. Null if
	 *						there are no matches.
	 */
	public String getAndStoreEchoNestSongIDFromMetadata( String song_title,
			String artist_name,
			boolean store_result )
	{
		try
		{
			// Verify that a song is specified
			if (song_title == null)
				throw new Exception("A song title must be specified in order to attempt to identify it by embedded metadata.");
			if (song_title.equals(""))
				throw new Exception("A song title must be specified in order to attempt to identify it by embedded metadata.");

			// Prepare the search parameters
			Params search_parameters = new Params();
			search_parameters.add("title", song_title);
			if (artist_name != null)
				search_parameters.add("artist", artist_name);
			
			// Only return one result
			search_parameters.add("results", 1);

			// Perform the query
			Song response = (echo_nest_api.searchSongs(search_parameters)).get(0);

			// Store the result if appropriate
			if (store_result)
			{
				MusicMetaData result = new MusicMetaData( echo_nest_source_identifier_code + " (Embedded Metadata Query)",
					"Echo Nest Song ID",
					response.getID() );
				music_meta_data.add(result);
			}

			// Return the result
			return response.getID();
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			return null;
		}
	}


	/**
	 * Attempts to identify a song by finding its Echo Nest Song ID using the
	 * Echo Nest API. Three techniques are succesfively applied unitl one is
	 * succesful. The first is local fingerprinting, the second is remote
	 * fingerprinting and the third is embedded metadata-based identification.
	 *
	 * <p>This method returns the Song ID as soon as one of these techniques is
	 * successful, and does not continue on to try other techniques unless the
	 * prior approaches did not work. The calling object may elect not to
	 * attempt certain approaches by passing null to certain parameters, as
	 * described below.
	 *
	 * <p>Depending on the vaule of the store_results field, the Song ID may be
	 * stored internally in this object's music_meta_data field.
	 *
	 * @param music_file_path		The path of the audio file to identify.
	 *								Neither local nor remote fingerprinting will
	 *								be performed if this is null.
	 * @param song_title			The title of the song. Identification based
	 *								on embedded metadata will not be performed
	 *								if this is null.
	 * @param artist_name			The "artist" of the song (typically refers
	 *								to performer/band, although it is sometimes
	 *								used to refer to composer, especially for
	 *								classical music). This may be null if this
	 *								information is unknown.
	 * @param store_results			Whether or not to add a MusicMetaData object
	 *								holding the Echo Nest Song ID to the
	 *								internal music_metadata Vector field
	 *								(assuming succesful extraction).
	 * @param report_failures_stream Where to report failures or errors that
	 *								occur. Nothing is reported if this is null.
	 * @param run_script_path		The path of the executable or script used to
	 *								run the Echo Nest codegen. Local
	 *								fingerprinting will not be attempted
	 *								if this is null. e.g. "./ENFP_Codegen.sh"
	 * @param en_codegen_directory	The path of the directory holding the
	 *								Echo Nest codegen fingerprinting binary.
	 *								Local fingerprinting will not be attempted
	 *								if this is null. e.g. "/home/ENMF/"
	 * @return						The Echo Nest Song ID of the song that the
	 *								Echo Nest API matches to the specified
	 *								music. Null if there are no matches.
	 */
	public String getAndStoreEchoNestSongIDUsingBestAvailableMethod( String music_file_path,
			String song_title,
			String artist_name,
			boolean store_results,
			PrintStream report_failures_stream,
			String run_script_path,
			String en_codegen_directory )
	{
		// The Echo Nest Song ID 
		String song_id = null;

		// Attempt to identify the song using local fingerprinting
		if (music_file_path != null && run_script_path != null && en_codegen_directory != null)
		{
			song_id = getAndStoreEchoNestSongIDFromLocalFingerPrint(music_file_path,
					store_results,
					run_script_path,
					en_codegen_directory);
			if (song_id == null && report_failures_stream != null)
				report_failures_stream.println("Local fingerprinting of " + music_file_path + " was unsuccessful. Attempting another methodology...\n");
		}

		// If local fingerprinting does not work, then attempt to identify the
		// song using remote fingerprinting
		if (song_id == null && music_file_path != null)
		{
			song_id = getAndStoreEchoNestSongIDFromRemoteFingerPrint(music_file_path,
					store_results);
			if (song_id == null && report_failures_stream != null)
				report_failures_stream.println("Remote fingerprinting of " + music_file_path + " was unsuccessful. Attempting another methodology...\n");
		}

		// If neither type of fingerprinting worked, attempt to identify the
		// song from embedded metadata
		if (song_id == null && song_title != null)
		{
			song_id = getAndStoreEchoNestSongIDFromMetadata(song_title,
					artist_name,
					store_results);
			if (song_id == null && report_failures_stream != null)
				report_failures_stream.println("Metadata-based identification of " + music_file_path + " was unsuccessful.\n");
		}

		// Return the Echo Nest Song ID
		return song_id;
	}


	/**
	 * Extract all available metadata for the song with the specified Echo Nest
	 * Song ID from the Echo Nest API. Depending on the vaule of the
	 * store_results field, these metadata results may be stored internally in
	 * this object's music_meta_data field.
	 *
	 * @param song_id		The Echo Nest Song ID of the song to extract
	 *						metadata for.
	 * @param store_result	Whether or not to add a MusicMetaData object
	 *						holding each piece of extracted metadata to the
	 *						internal music_metadata Vector field (assuming
	 *						succesful extraction).
	 * @param store_fails	If this is true, then for each individual piece of
	 *						metadata that cannot be extracted from the Echo Nest
	 *						API an indication is added to the returned (and, if
	 *						appropriate, stored) MusicMetaData highlighting the
	 *						failure. If this is false then fields that cannot
	 *						be extracted are simply ignored.
	 * @return				An array holding references to all of the extracted
	 *						song metadata for the specified song. Null is
	 *						returned if no data could be found.
	 * @throws Exception	An exception is thrown <b>only</b> if invalid song
	 *						identification information is provided. An exception
	 *						is <b>not</b> thrown if a given piece of metadata
	 *						cannot be extracted for a valid song.
	 */
	public MusicMetaData[] getAndStoreSongMetaData( String song_id,
			boolean store_result,
			boolean store_fails )
			throws Exception
	{
		// Verify that a song is specified
		if (song_id == null)
			throw new Exception("A song ID must be specified in order to attempt to extract song metadata.");
		if (song_id.equals(""))
			throw new Exception("A song ID must be specified in order to attempt to extract song metadata.");

		// The song corresponding to the specified song_id
		Song song = new Song(echo_nest_api, song_id);

		// The metadata extracted to date by this method
		Vector<MusicMetaData> results = new Vector<MusicMetaData>();

		// Extract the song title
		try {results.add(createAndStoreMusicMetaDataObject("Song Title", song.getTitle(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Song Title", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the artist name
		try {results.add(createAndStoreMusicMetaDataObject("Artist Name", song.getArtistName(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist Name", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest Artist ID
		try {results.add(createAndStoreMusicMetaDataObject("Echo Nest Artist ID", song.getArtistID(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Echo Nest Artist ID", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the duration
		try {results.add(createAndStoreMusicMetaDataObject("Duration (seconds)", (new Double(song.getDuration())).toString(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Duration (seconds)", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the tempo
		try {results.add(createAndStoreMusicMetaDataObject("Tempo (BPM)", (new Double(song.getTempo())).toString(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Tempo (BPM)", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the time signature
		try
		{
			int meter = song.getTimeSignature();
			String meter_id = null;
			if (meter == -1) meter_id = "No time signature";
			else if (meter == 0) meter_id = "Complex or changing meter";
			else if (meter == 1) meter_id = "Unknown";
			else if (meter == 3) meter_id = "3/4";
			else if (meter == 4) meter_id = "4/4";
			else if (meter == 5) meter_id = "5/4";
			else if (meter == 6) meter_id = "6/4";
			else if (meter == 7) meter_id = "7/4";
			results.add(createAndStoreMusicMetaDataObject("Time Signature", meter_id, store_result));
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Time Signature", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the key
		try
		{
			int key = song.getKey();
			String key_id = null;
			if (key == -1) key_id = "Unidentified";
			else if (key == 0) key_id = "C";
			else if (key == 1) key_id = "C#/Db";
			else if (key == 2) key_id = "D";
			else if (key == 3) key_id = "D#/Eb";
			else if (key == 4) key_id = "E";
			else if (key == 5) key_id = "F";
			else if (key == 6) key_id = "F#/Gb";
			else if (key == 7) key_id = "G";
			else if (key == 8) key_id = "G#/Ab";
			else if (key == 9) key_id = "A";
			else if (key == 10) key_id = "A#/Bb";
			else if (key == 11) key_id = "B";
			results.add(createAndStoreMusicMetaDataObject("Key", key_id, store_result));
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Key", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the modality (major/minor)
		try
		{
			int mode = song.getMode();
			String mode_id = null;
			if (mode == 0) mode_id = "Major";
			else if (mode == 1) mode_id = "Minor";
			results.add(createAndStoreMusicMetaDataObject("Mode", mode_id, store_result));
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Mode", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the loudness
		try {results.add(createAndStoreMusicMetaDataObject("Loudness (-100 to 100 dB)", (new Double(song.getLoudness())).toString(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Loudness (-100 to 100 dB)", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest song hottnesss
		try {results.add(createAndStoreMusicMetaDataObject("Echo Nest Song Hottness (0 to 1)", (new Double(song.getSongHotttnesss())).toString(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Echo Nest Song Hottness (0 to 1)", "Could not extract this information: " + e.getMessage(), store_result));}

		// Return the extracted metadata
		if (results.isEmpty()) return null;
		else return results.toArray(new MusicMetaData[results.size()]);
	}


	/**
	 * Extract all available metadata for the artist associated with the song 
	 * with the specified Echo Nest Song ID from the Echo Nest API (other than
	 * artist name and Echo Nest ID, which are extracted by the
	 * getAndStoreSongMetaData method). Depending on the vaule of the
	 * store_results field, these metadata results may be stored internally in
	 * this object's music_meta_data field.
	 *
	 * @param song_id		The Echo Nest Song ID of the song to extract
	 *						metadata for.
	 * @param store_result	Whether or not to add a MusicMetaData object
	 *						holding each piece of extracted metadata to the
	 *						internal music_metadata Vector field (assuming
	 *						succesful extraction).
	 * @param store_fails	If this is true, then for each individual piece of
	 *						metadata that cannot be extracted from the Echo Nest
	 *						API an indication is added to the returned (and, if
	 *						appropriate, stored) MusicMetaData highlighting the
	 *						failure. If this is false then fields that cannot
	 *						be extracted are simply ignored.
	 * @return				An array holding references to all of the extracted
	 *						artist metadata for the specified song. Null is
	 *						returned if no data could be found.
	 * @throws Exception	An exception is thrown <b>only</b> if invalid song
	 *						identification information is provided. An exception
	 *						is <b>not</b> thrown if a given piece of metadata
	 *						cannot be extracted for a valid song.
	 */
	public MusicMetaData[] getAndStoreArtistMetaData( String song_id,
			boolean store_result,
			boolean store_fails )
			throws Exception
	{
		// Verify that a song is specified
		if (song_id == null)
			throw new Exception("A song ID must be specified in order to attempt to extract artist metadata.");
		if (song_id.equals(""))
			throw new Exception("A song ID must be specified in order to attempt to extract artist metadata.");

		// The metadata extracted to date by this method
		Vector<MusicMetaData> results = new Vector<MusicMetaData>();

		// The song corresponding to the specified song_id
		Artist artist = echo_nest_api.newArtistByID((new Song(echo_nest_api, song_id)).getArtistID());

		// Extract the artist name
		try {results.add(createAndStoreMusicMetaDataObject("Artist Name", artist.getName(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist Name", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest Artist ID
		try {results.add(createAndStoreMusicMetaDataObject("Echo Nest Artist ID", artist.getID(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Echo Nest Artist ID", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Music Brainz Artist ID
		try 
		{
			String full_id = artist.getForeignID("musicbrainz");
			String stripped_id = cleanForeignID(full_id, "musicbrainz:artist:");
			results.add(createAndStoreMusicMetaDataObject("Music Brainz Artist ID", stripped_id, store_result));
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("MusicBrainz Artist ID", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract 7digital Artist ID
		try
		{
			String full_id = artist.getForeignID("7digital");
			String stripped_id = cleanForeignID(full_id, "7digital:artist:");
			results.add(createAndStoreMusicMetaDataObject("7digital Artist ID", stripped_id, store_result));
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("7digital Artist ID", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract Play.me Artist ID
		try
		{
			String full_id = artist.getForeignID("playme");
			String stripped_id = cleanForeignID(full_id, "playme:artist:");
			results.add(createAndStoreMusicMetaDataObject("Play.me Artist ID", stripped_id, store_result));
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Play.me Artist ID", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the artist's location
		try {results.add(createAndStoreMusicMetaDataObject("Artist Location", ((new Song(echo_nest_api, song_id)).getArtistLocation()).getPlaceName(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist Location", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist familiarity
		try {results.add(createAndStoreMusicMetaDataObject("Echo Nest Artist Familiarity (0 to 1)", (new Double(artist.getFamiliarity())).toString(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Echo Nest Artist Familiarity (0 to 1)", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist hotness
		try {results.add(createAndStoreMusicMetaDataObject("Echo Nest Artist Hotness (0 to 1)", (new Double(artist.getHotttnesss())).toString(), store_result));}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Echo Nest Artist Hotness (0 to 1)", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist-related terms
		try
		{
			List<Term> terms = artist.getTerms();
			int this_max = terms.size();
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
			{
				Term term = terms.get(i);
				results.add(createAndStoreMusicMetaDataObject("Artist-Related Term (Term " + (i + 1) + ")", term.getName() + " (Weight: " + term.getWeight() + " Frequency: " + term.getFrequency() +")", store_result));
			}
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist-Related Terms", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest similar artists
		try
		{
			List<Artist> similar_artists = artist.getSimilar(max_to_report);
			int this_max = similar_artists.size();
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
			{
				Artist similar_artist = similar_artists.get(i);
				results.add(createAndStoreMusicMetaDataObject("Similar Artist (Artist " + (i + 1) + ")", similar_artist.getName() + " (Echo Nest Artist ID: " + similar_artist.getID() + ")", store_result));
			}
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Similar Artists", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist-related audio files
		try
		{
			List<Audio> audio_list = artist.getAudio();
			int this_max = audio_list.size();
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
			{
				Audio audio = audio_list.get(i);
				results.add(createAndStoreMusicMetaDataObject("Artist-Related Audio (" + audio.getTitle() + ")", audio.getURL(), store_result));
			}
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist-Related Audio", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist-related image files
		try
		{
			List<Image> images = artist.getImages();
			int this_max = images.size();
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
			{
				Image image = images.get(i);
				results.add(createAndStoreMusicMetaDataObject("Artist-Related Image (Image " + (i + 1) + ")", image.getURL(), store_result));
			}
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist-Related Images", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist-related video files
		try
		{
			List<Video> videos = artist.getVideos();
			int this_max = videos.size();
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
			{
				Video video = videos.get(i);
				results.add(createAndStoreMusicMetaDataObject("Artist-Related Video (" + video.getTitle() + ")", video.getURL(), store_result));
			}
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist-Related Videos", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist related URLs
		try
		{
			Map<String,String> url_map = artist.getUrls();
			Set<String> key_set = url_map.keySet();
			String[] keys = key_set.toArray(new String[key_set.size()]);
			int this_max = keys.length;
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
				results.add(createAndStoreMusicMetaDataObject("Artist-Related URL (" + keys[i] + ")", url_map.get(keys[i]), store_result));
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist-Related URLs", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist biographies
		try
		{
			List<Biography> bios = artist.getBiographies();
			int this_max = bios.size();
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
			{
				Biography bio = bios.get(i);
				results.add(createAndStoreMusicMetaDataObject("Artist Biography (" + bio.getSite() + ")", bio.getURL(), store_result));
			}
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist Biographies", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist-related news
		try
		{
			List<News> news_list = artist.getNews();
			int this_max = news_list.size();
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
			{
				News news = news_list.get(i);
				results.add(createAndStoreMusicMetaDataObject("Artist-Related News (" + news.getName() + ")", news.getURL(), store_result));
			}
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist-Related News", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist reviews
		try
		{
			List<Review> reviews = artist.getReviews();
			int this_max = reviews.size();
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
			{
				Review review = reviews.get(i);
				results.add(createAndStoreMusicMetaDataObject("Artist Review (" + review.getName() + ")", review.getURL(), store_result));
			}
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist Reviews", "Could not extract this information: " + e.getMessage(), store_result));}

		// Extract the Echo Nest artist-related blogs
		try
		{
			List<Blog> blogs = artist.getBlogs();
			int this_max = blogs.size();
			if (max_to_report < this_max) this_max = max_to_report;
			for (int i = 0; i < this_max; i++)
			{
				Blog blog = blogs.get(i);
				results.add(createAndStoreMusicMetaDataObject("Artist-Related Blog (" + blog.getName() + ")", blog.getURL(), store_result));
			}
		}
		catch (Exception e) {if (store_fails) results.add(createAndStoreMusicMetaDataObject("Artist-Related Blogs", "Could not extract this information: " + e.getMessage(), store_result));}

		// Return the extracted metadata
		if (results.isEmpty()) return null;
		else return results.toArray(new MusicMetaData[results.size()]);
	}


	/**
	 * Extracts all available song and, if appropriate, artist metadata for the
	 * specified Echo Nest Song ID from the Echo Nest API. Adds the extracted
	 * metadata to the two Vector parameters of this method, respectively. Note
	 * that the extracted metadata is <b>not</b> stored in this object.
	 *
	 * <p>Song metadata is always extracted. Artist metadata is only extracted
	 * if it has not already been extracted for the given artist during the
	 * processing of another song, as indicated by the presence of the artist's
	 * name (in lower case) in the artists_already_extracted parameter. Note
	 * that this method does <b>not</b> update this hash map in any way, and it
	 * is left to the calling object to do this.
	 *
	 * @param song_id						The Echo Nest Song ID of the song to
	 *										extract metadata for.
	 * @param song_metadata					A Vector of metadata already
	 *										extracted for this song. May not
	 *										be null, but may be empty.
	 * @param artist_metadata				A Vector of metadata already
	 *										extracted for the artist associated
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
	 * @param store_identification_metadata	Whether or not to store metadata
	 *										previously extracted by this object
	 *										(typically song identification data)
	 *										to the song_metadata Vector.
	 * @param store_fails					If this is true, then for each
	 *										individual piece of metadata that
	 *										cannot be extracted from the Echo
	 *										Nest API an indication is added to
	 *										the stored MusicMetaData
	 *										highlighting the failure. If this is
	 *										false then fields that cannot
	 *										be extracted are simply ignored.
	 *										Note that this parameter has no
	 *										effect on song identification
	 *										specifically.
	 * @return								True is returned if a change was
	 *										made to song_metadata,
	 *										artist_metadata or both.
	 * @throws Exception					An exception is thrown if invalid
	 *										parameters are passed.
	 */
	public boolean getAllAvailableNewMetaData( String song_id,
			Vector<MusicMetaData> song_metadata,
			Vector<MusicMetaData> artist_metadata,
			HashMap< String, Vector<MusicMetaData> > artists_already_extracted,
			boolean store_identification_metadata,
			boolean store_fails )
			throws Exception
	{
		// Verify input parameters
		if (song_id == null)
			throw new Exception("A song ID must be specified in order to attempt to extract metadata");
		if (song_id.equals(""))
			throw new Exception("A song ID must be specified in order to attempt to extract metadata");
		if (song_metadata == null || artist_metadata == null)
			throw new Exception ("A song Vector and an artist Vector must be provided (although they may be empty).");

		// Whether or not a change has been made
		boolean some_data_successfully_extracted = false;

		// Store previously extracted metadata
		if (store_identification_metadata && !music_meta_data.isEmpty())
		{
			song_metadata.addAll(music_meta_data);
			some_data_successfully_extracted = true;
		}

		// Extract song metadata
		MusicMetaData[] new_song_metadata = getAndStoreSongMetaData( song_id,
				false,
				store_fails );

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
			String artist_name = (new Song(echo_nest_api, song_id)).getArtistName();
			if (artist_name == null)
				extract_artist_data = false;
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
			MusicMetaData[] new_artist_data = getAndStoreArtistMetaData( song_id,
					false,
					store_fails );

			if (new_artist_data != null)
			{
				// Add artist metadata to the artist_metadata Vector
				for (int i = 0; i < new_artist_data.length; i++)
					artist_metadata.add(new_artist_data[i]);

				// Note that a change has been made
				some_data_successfully_extracted = true;
			}
		}

		// Return whether some data was successfully extracted
		return some_data_successfully_extracted;
	}


	/**
	 * Return the song title and artist name for the specifried Echo Nest
	 * Song ID.
	 *
	 * @param song_id	The Echo Nest Song ID of the song to extract metadata
	 *					for.
	 * @return			The song title and artist name (in that order) of the
	 *					specified song. Null is returned if no song could be
	 *					found for the specified song_id.
	 */
	public String[] getIdentifyingMetadataForSong(String song_id)
	{
		try
		{
			// The song corresponding to the specified song_id
			Song song = new Song(echo_nest_api, song_id);

			// Get and return the identifying metadata
			String[] results = {song.getTitle(), song.getArtistName()};
			return results;
		}
		catch (Exception e) {return null;}
	}


	/**
	 * Returns the Echo Nest Artist ID for the specified artist.
	 *
	 * @param artist_name	The name of the artist to identify.
	 * @return				The Echo Nest Artist ID, or null if it could not be
	 *						found or a problem occured.
	 */
	public String getEchoNestArtistID(String artist_name)
	{
		try
		{
			Artist artist = echo_nest_api.newArtistByName(artist_name);
			return artist.getID();
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			return null;
		}
	}


	/**
	 * Get the artist name for the artist associated with the specified Echo
	 * Nest Song ID.
	 *
	 * @param song_id	The Echo Nest Song ID of the song for which the artist
	 *					name is being queried.
	 * @return			The name of the artist, or null if it could not be
	 *					found or a problem occured. Note that the artist's name
	 *					is converted to lower case in the returned string.
	 */
	public String getArtistNameLowerCase(String song_id)
	{
		try
		{
			String result = (new Song(echo_nest_api, song_id)).getArtistName();
			result = result.toLowerCase();
			return result;
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			return null;
		}
	}


	/**
	 * Get the Music Brainz Artist ID for the artist associated with the
	 * specified Echo Nest Song ID.
	 *
	 * @param song_id	The Echo Nest Song ID of the song for which the Music
	 *					Brainz Aritt ID is being queried.
	 * @return			The Music Brainz Artist ID, or null if it could not be
	 *					found or a problem occured. Note that the prefix of
	 *					"musicbrainz:artist:" added by the Echo Nest is stripped
	 *					away.
	 */
	public String getMusicBrainzArtistID(String song_id)
	{
		try
		{
			Artist artist = echo_nest_api.newArtistByID((new Song(echo_nest_api, song_id)).getArtistID());
			String mb_artist_id = artist.getForeignID("musicbrainz");
			mb_artist_id = cleanForeignID(mb_artist_id, "musicbrainz:artist:");
			return mb_artist_id;
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			return null;
		}
	}


	/* PRIVATE METHODS ********************************************************/


	/**
	 * Parses through the provided JSON dictionary and returns a MusicMetaData
	 * object holding the provided int_field_id, the value corresponding to the
	 * provided chain of dictionary field identifiers and a code identifying The
	 * Echo Nest as the source of the data. The returned MusicMetaData object is
	 * also added to the music_metadata Vector field if the store_result
	 * parameter is true (assuming succesful extraction).
	 *
	 * @param json_dict			A JSON dictionary to parse. This is presumably
	 *							extracted from an EchoNest service.
	 * @param dict_fields		The chain of metadata field identifiers to parse
	 *							through. e.g. {"metadata", "artist"}. Note that
	 *							in the case of JSON arrays, index values must be
	 *							specified, instead of field identifers, as
	 *							Strings (e.g. "0").
	 * @param field_id_to_store	The field identifier to store in the
	 *							MusicMetaData object produced. Note that this is
	 *							not used in the JSON parsing itself. This is
	 *							ignored if the store_result is false.
	 * @param store_result		Whether or not to add the produced MusicMetaData
	 *							object to the music_metadata Vector field
	 *							(assuming succesful extraction).
	 * @return					The parsed result. Null is returned if a problem
	 *							occured, such as the given chain of dictionary
	 *							fields being invalid for the given JSON
	 *							dictionary.
	 */
	private MusicMetaData extractFieldValueFromEchoNestJSON( String json_dict,
			String[] dict_fields,
			String field_id_to_store,
			boolean store_result )
	{
		try
		{
			String field_value = null;

			Object current_json = JSONValue.parse(json_dict);

			for (int i = 0; i < dict_fields.length; i++)
			{
				// For the last one
				if (i == dict_fields.length - 1)
				{
					if (current_json instanceof JSONObject)
					{
						field_value = (String) ((JSONObject) current_json).get(dict_fields[i]);
					}
					else if (current_json instanceof JSONArray)
					{
						int index = (new Integer(dict_fields[i])).intValue();
						field_value = (String) ((JSONArray) current_json).get( index );
					}
					else
						return null;
				}

				// For all but the last one
				else
				{
					if (current_json instanceof JSONObject)
					{
						current_json = ((JSONObject) current_json).get(dict_fields[i]);
					}
					else if (current_json instanceof JSONArray)
					{
						int index = (new Integer(dict_fields[i])).intValue();
						current_json = ((JSONArray) current_json).get( index );
					}
					else
						return null;
				}
			}

			// Prepare the MusicMetaData object
			MusicMetaData result = new MusicMetaData(echo_nest_source_identifier_code + " (Local Fingerprinter)",
					field_id_to_store,
					field_value);

			// Store the MusicMetaData object if appropriate
			if (store_result) music_meta_data.add(result);

			// Return the MusicMetaData object
			return result;
		}
		catch (Exception e)
		{
			// Return null if a problem occured
			return null;
		}
	}


	/**
	 * Generate a MusicMetaData object with the specified field identifier and
	 * field value. The source is set based on the value of this object's
	 * echo_nest_source_identifier_code field. This new MusicMetaData object is
	 * also stored in this object's music_metadata Vector field if the
	 * store_result parameter is true.
	 *
	 * @param field_identifier	The field identifier to store in the new
	 *							MusicMetaData object.
	 * @param field_value		The value for the field to store in the new
	 *							MusicMetaData object.
	 * @param store_result		Whether or not to add the new MusicMetaData
	 *							object holding the specified to the internal
	 *							music_metadata Vector field.
	 * @return					The new MusicMetaData object.
	 */
	private MusicMetaData createAndStoreMusicMetaDataObject( String field_identifier,
			String field_value,
			boolean store_result )
	{
		// Prepare the MusicMetaData object
		MusicMetaData result = new MusicMetaData(echo_nest_source_identifier_code,
				field_identifier,
				field_value);

		// Store the MusicMetaData object if appropriate
		if (store_result) music_meta_data.add(result);

		// Return the MusicMetaData object
		return result;
	}


	/**
	 * The Echo Nest returns foreign IDs with a prefix identifying them. This
	 * method strips away this prefix.
	 *
	 * @param full_foreign_id	The full foreign ID, including the prefix.
	 * @param prefix_to_strip	The prefix to strip away.
	 * @return					The foreign ID with the prefix stripped away.
	 *							The original full_echo_nest_id string is
	 *							returned if the prefix was not present.
	 */
	private static String cleanForeignID( String full_foreign_id,
			String prefix_to_strip )
	{
		return full_foreign_id.replaceFirst(prefix_to_strip, "");
	}
}