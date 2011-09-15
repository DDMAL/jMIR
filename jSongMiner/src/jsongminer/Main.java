/*
 * Main.java
 * Version 1.0
 *
 * Last modified on August 4, 2010.
 * University of Waikato and McGill University
 */

package jsongminer;

import mckay.utilities.webservices.ProxyServerAccessor;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Vector;

/**
 * jSongMiner is a system for identifying music using fingerprinting of audio
 * and/or the use of embedded metadata. This identifying information is then
 * used to extract metadata from various web services. See the jSongMiner
 * documentations files for more detailed information.
 *
 * <p>This is the Main class for the jSongMiner project. It contains the
 * executable Main method, which outlines the jSongMiner chain of processing
 * from a high-level perspective. This primarily calls methods from the
 * MainMethods class, which provides and intermediate-level perspective on the
 * processing. More detailed processing and data structures are implemented in
 * the other classes of this project.
 *
 * @author Cory McKay
 */
public class Main
{
	/* MAIN METHOD ************************************************************/


	/**
	 * Performs basic processing for identifying a track and extracting metadata
	 * from web services that is associated with this track.
	 *
	 * @param args The command line arguments.
	 */
	public static void main(String[] args)
	{
		try
		{
			// Initialize variables
			HashMap<String, String> command_line_args = null;
			ConfigDataAndParser configs = null;
			PrintStream status_stream = null;
			PrintStream error_stream = null;
			HashMap< String, Vector<MusicMetaData> > artist_metadata_so_far = null;
			HashMap< String, Vector<MusicMetaData> > album_metadata_so_far = null;
			EmbeddedTagMiner emb_miner = null;
			EchoNestMiner en_miner = null;
			LastFMMiner lfm_miner = null;
			String en_song_id = null;
			String to_extract_audio_path = null;
			String identifying_title = null;
			String identifying_artist = null;
			String identifying_album = null;
			Vector<MusicMetaData> song_metadata = new Vector<MusicMetaData>();
			Vector<MusicMetaData> artist_metadata = new Vector<MusicMetaData>();
			Vector<MusicMetaData> album_metadata = new Vector<MusicMetaData>();
			String ace_xml_save_file_path = null; // this is only used if config overrides are specified at the command line
			String text_save_file_path = null; // this is only used if config overrides are specified at the command line

			// Parse and validate the command line arguments. Also store
			// important information that is found in the command line arguments
			// in song_metadata
			command_line_args = MainMethods.parseAndTestCommandLineArgs(args, song_metadata);

			// Parse the configurations file or, if this fails, generate a new
			// one with default configuration values
			configs = MainMethods.parseConfigurationFile();

			// Prepare output streams to print status updates and errors to
			if (configs.print_current_status_to_terminal)
				status_stream = System.out;
			if (configs.print_errors_to_terminal)
				error_stream = System.err;

			// Update configuration settings based on command line arguments.
			// These changes are temporary (i.e. they are not saved). Also
			// delete artist and/or album log files if instructed to do so
			// in command line arguments.
			String[] temp_save_array = MainMethods.updateConfigsBasedOnCommandLineArgs( command_line_args,
					configs,
					error_stream );
			ace_xml_save_file_path = temp_save_array[0];
			text_save_file_path = temp_save_array[1];

			// Verify if proxy settings are needed, and set them up if they are
			if ( configs.enable_echo_nest_fingerprinting ||
			     configs.enable_last_fm_fingerprinting ||
				 configs.save_echo_nest_metadata ||
				 configs.save_last_fm_metadata )
			{
				if (status_stream != null)
					status_stream.println("Testing if proxy server is needed and configuring it if so...\n");
				new ProxyServerAccessor("./proxysettings.cfg", null);
			}

			// Parse / set up file and data structures to note the artists and
			// albums for which metadata has already been extracted
			artist_metadata_so_far = MainMethods.parseAlreadyExtractedMetadata( configs.artists_already_accessed_file_path,
					configs.reextract_known_artist_metadata,
					"artist",
					status_stream,
					error_stream );
			album_metadata_so_far = MainMethods.parseAlreadyExtractedMetadata( configs.albums_already_accessed_file_path,
					configs.reextract_known_album_metadata,
					"album",
					status_stream,
					error_stream );

			// Set the file path to base extraction on
			to_extract_audio_path = command_line_args.get("-audio");

			// Extract metadata embedded in the audio file
			if ( configs.enable_embedded_metadata_track_identification ||
			     configs.save_embedded_metadata )
			{
				emb_miner = MainMethods.extractEmbeddedMetadata( to_extract_audio_path,
					status_stream,
					error_stream );
			}

			// Set the candidate identifying title and artist fields to the
			// values specified at the command line. If values were not set at
			// the command line, attempt to set them to the values in the files'
			// embedded metadata. If this is not possible, they are set to null.
			identifying_title = MainMethods.getCandidateMetadataValue( emb_miner,
					to_extract_audio_path,
					configs.enable_embedded_metadata_track_identification,
					"title",
					command_line_args.get("-title"),
					error_stream );
			identifying_artist = MainMethods.getCandidateMetadataValue( emb_miner,
					to_extract_audio_path,
					configs.enable_embedded_metadata_track_identification,
					"artist",
					command_line_args.get("-artist"),
					error_stream );
			identifying_album = MainMethods.getCandidateMetadataValue( emb_miner,
					to_extract_audio_path,
					configs.enable_embedded_metadata_track_identification,
					"album",
					command_line_args.get("-album"),
					error_stream );

			// If Echo Nest usage is enabled, initialize en_miner with the
			// Echo Nest API key. Then, if Echo Nest fingerprinting is
			// enabled, use the Echo Nest to identify the song using local
			// fingerprinting, remote fingerprinting or metadata query,
			// whichever is succesful first. If fingerprinting is enabled and
			// performed succesfully then the identifier is stored in
			// en_song_id, and identifying_title and identifying_artist are
			// reset based on the results.
			if ( configs.enable_echo_nest_fingerprinting ||
			     configs.save_echo_nest_metadata )
			{
				// Initialize the Echo Nest miner
				if (status_stream != null)
					status_stream.println("Initializing the Echo Nest API...\n");
				en_miner = new EchoNestMiner(configs.echo_nest_api_key);

				// Identify the song using the Echo Nest
				if (configs.enable_echo_nest_fingerprinting)
				{
					en_song_id = MainMethods.extractEchoNestSongID( to_extract_audio_path,
							identifying_title,
							identifying_artist,
							en_miner,
							configs.echo_nest_fingerprinting_codegen_run_path,
							configs.echo_nest_codegen_directory,
							status_stream,
							error_stream );
					if (en_song_id != null)
					{
						String[] temp_identifiers = en_miner.getIdentifyingMetadataForSong(en_song_id);
						if (temp_identifiers != null)
						{
							if (temp_identifiers[0] != null)
								identifying_title = temp_identifiers[0];
							if (temp_identifiers[1] != null)
								identifying_artist = temp_identifiers[1];
							identifying_album = null; // in case embedded or model metadata was wrong
							                          // Echo Nest does not currently specify the album
						}
					}
				}
			}

			// If Last.FM usage is enabled, initialize lfm_miner with the
			// Last.FM API key and identifying information for the song it is
			// to be associated with. If Last.FM fingerprinting is
			// enabled and Echo Nest track identification was not performed
			// succesfully, then the identifying_title, identifying_artist and
			// identifying_album variables are reset based on the results.
			if ( configs.save_last_fm_metadata ||
			     (configs.enable_last_fm_fingerprinting && en_song_id == null) )
			{
				lfm_miner = MainMethods.matchToLastFMTrack( identifying_title,
					identifying_artist,
					configs.last_fm_api_key,
					status_stream,
					error_stream );
				if ( lfm_miner != null &&
				     configs.enable_last_fm_fingerprinting &&
					 en_song_id == null )
				{
					String[] temp_identifiers = lfm_miner.getIdentifyingMetadataForSong();
					if (temp_identifiers != null)
					{
						if (temp_identifiers[0] != null)
							identifying_title = temp_identifiers[0];
						if (temp_identifiers[1] != null)
							identifying_artist = temp_identifiers[1];
						if (temp_identifiers[2] != null)
							identifying_album = temp_identifiers[2];
					}
				}
				else if ( lfm_miner != null &&
				          configs.enable_last_fm_fingerprinting )
				{
					String[] temp_identifiers = lfm_miner.getIdentifyingMetadataForSong();
					if (temp_identifiers != null)
					{
						if (temp_identifiers[2] != null)
							identifying_album = temp_identifiers[2]; // because Echo Nest can't currently extract this
					}
				}
			}

			// If Echo Nest fingerprinting is not permitted, but saving of Echo
			// Nest data is, and identifying information is now available from
			// some other source (e.g. Last.FM), then attempt a
			// non-fingerprinting-based Echo Nest ID extraction.
			if ( en_song_id == null &&
			     identifying_title != null &&
				 configs.save_echo_nest_metadata &&
			     !configs.enable_echo_nest_fingerprinting )
			{
				en_song_id = MainMethods.extractEchoNestSongID( null,
						identifying_title,
						identifying_artist,
						en_miner,
						null,
						null,
						status_stream,
						error_stream );
			}

			// If the song was succesfully identified...
			if (identifying_title != null)
			{
				// Store identifying metadata if this is all that is desired
				if (configs.identify_only)
				{
					MainMethods.storeIdentifyingMetadataOnly( song_metadata,
							to_extract_audio_path,
							identifying_title,
							identifying_artist,
							identifying_album,
							en_song_id,
							lfm_miner,
							configs.store_fails );
				}

				// Extract additional metadata
				else
				{
					// Extract Echo Nest metadata (song and, if appropriate, artist)
					// for the newly identified song
					if (configs.save_echo_nest_metadata)
					{
						MainMethods.extractEchoNestMetadata( en_song_id,
							song_metadata,
							artist_metadata,
							artist_metadata_so_far,
							configs.store_fails,
							en_miner,
							status_stream,
							error_stream );
					}

					// Extract Last.FM metadata (song and, if appropriate, artist
					// and album) for the newly matched song
					if (configs.save_last_fm_metadata)
					{
						MainMethods.extractLastFMMetadata( lfm_miner,
							song_metadata,
							artist_metadata,
							album_metadata,
							artist_metadata_so_far,
							album_metadata_so_far,
							configs.store_fails,
							status_stream,
							error_stream );
					}

					// Parse song metadata embedded in the audio file, if any
					if (configs.save_embedded_metadata)
					{
						MainMethods.storeEmbeddedMetadata( emb_miner,
							song_metadata,
							configs.store_fails,
							status_stream,
							error_stream );
					}
				}
		
				// Do a final check to remove metadata that was returned null or
				// empty string by the various APIs
				if (!configs.store_fails)
				{
					if (status_stream != null)
						status_stream.println("Removing empty metadata...\n");
					MusicMetaData.cleanMusicMetaData(song_metadata);
					MusicMetaData.cleanMusicMetaData(artist_metadata);
					MusicMetaData.cleanMusicMetaData(album_metadata);
				}

				// Interperet the results into Dublin Core formatted data and
				// add them to the metadata vectors if either the
				// include_unqualified_dublin_core or the
				// include_unqualified_dublin_core configuration settings are
				// set to true. Delete non-Dublin Core metadata from these
				// vectors if Dublin Core data is to be extracted and the
				// include_other_metadata_with_dublin_core configuration setting
				// is false.
				MainMethods.prepareDublinCore( configs.include_unqualified_dublin_core,
					configs.include_qualified_dublin_core,
					configs.include_other_metadata_with_dublin_core,
					configs.package_song_artist_album,
					song_metadata,
					artist_metadata,
					album_metadata,
					status_stream );

				// Save all of the just-extracted metadata
				// for the song and, if apprpropriate, artist and album, as 
				// ACE XML 1.1 data
				if (configs.save_output_as_ace_xml)
				{
					MainMethods.saveData( true,
						song_metadata,
						artist_metadata,
						album_metadata,
						configs.package_song_artist_album,
						configs.songs_save_directory,
						configs.artists_save_directory, 
						configs.albums_save_directory,
						configs.url_encode_output,
						ace_xml_save_file_path,
						status_stream,
						error_stream );
				}
						
				// Save all of the just-extracted metadata
				// for the song and, if apprpropriate, artist and album, as
				// txt data
				if (configs.save_output_as_txt)
				{
					MainMethods.saveData( false,
						song_metadata,
						artist_metadata,
						album_metadata,
						configs.package_song_artist_album,
						configs.songs_save_directory,
						configs.artists_save_directory,
						configs.albums_save_directory,
						configs.url_encode_output,
						text_save_file_path,
						status_stream,
						error_stream );
				}

				// Update and save the artists and albums already extracted 
				// logs.
				MainMethods.updateAlreadyAccessedLog( configs.artists_already_accessed_file_path,
					artist_metadata_so_far,
					identifying_artist,
					null,
					status_stream,
					error_stream );
				MainMethods.updateAlreadyAccessedLog( configs.albums_already_accessed_file_path,
					album_metadata_so_far,
					null,
					identifying_album,
					status_stream,
					error_stream );

				// Output extracted metadata to standard out
				if (configs.print_extracted_metadata_to_terminal)
				{
					MainMethods.printMetadata( song_metadata,
						artist_metadata,
						album_metadata,
						configs.package_song_artist_album );
				}
			}
		}
 		catch (Exception e)
		{
			System.err.println("ERROR: " + e.getMessage() + "\n");
			// e.printStackTrace();
		}
	}
}