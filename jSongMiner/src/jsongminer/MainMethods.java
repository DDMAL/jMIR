/*
 * Main.java
 * Version 1.0
 *
 * Last modified on August 4, 2010.
 * University of Waikato and McGill University
 */

package jsongminer;

import mckay.utilities.staticlibraries.MiscellaneousMethods;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Vector;
import mckay.utilities.staticlibraries.StringMethods;

/**
 * This class holds static methods that are called by the Main method of the
 * Main class. These outline each of the primary processing tasks performed by
 * jSongMiner.
 *
 * @author Cory McKay
 */
public class MainMethods
{
	/**
	 * Parses and validates the specified command line arguments. An informative
	 * error message is printed to standard error if the command line arguments
	 * are invalid. Also, an explanation of the acceptable command line
	 * arguments is printed to standard error if the command line arguments
	 * provided are incorrect. A lone flag of "-help" will print the valid
	 * command line arguments to standard out. Program execution is terminated
	 * if invalid command line arguments or "-help" are parsed. If the command
	 * line settings are valid, then the specified audio file path, song title,
	 * artist name and album title metadata are added to song_metadata if
	 * present.
	 *
	 * @param args			The command line arguments to parse and validate.
	 * @param song_metadata	A Vector of metadata already extracted for this
	 *						song. May not be null, but may be empty.
	 * @return				The parsed command line arguments. The keys indicate
	 *						the flags provided and the values indicate the
	 *						values for the flags specified. Only flags that were
	 *						present are included (i.e. optional flags are not
	 *						included.
	 */
	public static HashMap<String, String> parseAndTestCommandLineArgs( String[] args,
			Vector<MusicMetaData> song_metadata )
	{
		// The permitted command line arguments, and whether or not they're
		// obligatory
		HashMap<String, Boolean> permitted_flags = new HashMap<String, Boolean>();
		permitted_flags.put("-audio", new Boolean(false));
		permitted_flags.put("-title", new Boolean(false));
		permitted_flags.put("-artist", new Boolean(false));
		permitted_flags.put("-album", new Boolean(false));
		permitted_flags.put("-savedir", new Boolean(false));
		permitted_flags.put("-savefile", new Boolean(false));
		permitted_flags.put("-saveacexmlfile", new Boolean(false));
		permitted_flags.put("-savetxtfile", new Boolean(false));
		permitted_flags.put("-eraselogs", new Boolean(false));
		permitted_flags.put("-urldecode", new Boolean(false));
		permitted_flags.put("-acexmltotxt", new Boolean(false));
		permitted_flags.put("-acexmltotxtud", new Boolean(false));

		// Explanations of what each of the permissible flags mean
		Vector<String> explanations = new Vector<String>();
		explanations.add("The path of an audio file to identify and extract metadata for, if available.");
		explanations.add("The suspected title of the song to extract metadata for, if known.");
		explanations.add("The suspected name of the artist associated with the song to extract metadata for, if known.");
		explanations.add("The suspected title of the album most associated with the song to extract metadata for, if known.");
		explanations.add("The directory to save extracted metadata in. This overrides any and all output directories specified in the configuration file, and applies to both text and ACE XML output. Only a directory should be specified, as file names will be auto-generated based on content. May not be used in conjunction with any of the -savefile, -saveacexmlfile or -savetxtfile flags.");
		explanations.add("The file path to save extracted metadata to. This overrides configuration settings relating to output directories, and sets the package_song_artist_album setting to true so that song, artist and album metadata are combined into one file. If the value specified here has a .xml extension, then the output will be saved as ACE XML. If it has a .txt extension, then the output will be saved as text. May not be used in conjunction with any of the -savedir, -saveacexmlfile or -savetxtfile flags.");
		explanations.add("The file path of the ACE XML file to save extracted metadata to. This overrides configuration settings relating to output directories, sets the save_output_as_ace_xml configuration setting to true, and sets the package_song_artist_album setting to true so that song, artist and album metadata are combined into one file. This flag may be used either alone or in conjunction with the -savetxtfile flag.");
		explanations.add("The file path of the text file to save extracted metadata to. This overrides configuration settings relating to output directories, sets the save_output_as_txt configuration setting to true, and sets the package_song_artist_album setting to true so that song, artist and album metadata are combined into one file. This flag may be used either alone or in conjunction with the -saveacexmlfile flag.");
		explanations.add("Erases the logs of previously extracted artists and/or albums, if any. This does not alter the reextract_known_artist_metadata or reextract_known_album_metadata configuration settings, but it does erase any existing logs before the new processing begins. The value following this flag must be \"artist\", \"album\" or \"all\", and specifies which log(s) are erased.");
		explanations.add("URL decodes the text following this flag and prints the results to standard out. Uses UTF-8 decoding. This is a terminal utility function, so no other processing is performed other than this, and this flag cannot be used in combination with any other flags.");
		explanations.add("Converts the ACE XML Classifications 1.1 file whose path is specified after this flag into the type of text produced by jSongMiner, which is to say field names are printed to odd lines and field values are printed to the following even lines. This is output to standard out. URL decoding is not performed. This is a terminal utility function, so no other processing is performed other than this, and this flag cannot be used in combination with any other flags.");
		explanations.add("Converts the ACE XML Classifications 1.1 file whose path is specified after this flag into the type of text produced by jSongMiner, which is to say field names are printed to odd lines and field values are printed to the following even lines. This is output to standard out. URL decoding is performed (UTF-8). This is a terminal utility function, so no other processing is performed other than this, and this flag cannot be used in combination with any other flags.");

		// Keys for the explanations
		Vector<String> explanation_keys = new Vector<String>();
		explanation_keys.add("-audio");
		explanation_keys.add("-title");
		explanation_keys.add("-artist");
		explanation_keys.add("-album");
		explanation_keys.add("-savedir");
		explanation_keys.add("-savefile");
		explanation_keys.add("-saveacexmlfile");
		explanation_keys.add("-savetxtfile");
		explanation_keys.add("-eraselogs");
		explanation_keys.add("-urldecode");
		explanation_keys.add("-acexmltotxt");
		explanation_keys.add("-acexmltotxtud");

		// Parse and validate the command line arguments
		HashMap<String, String> parsed_args = MiscellaneousMethods.parseCommandLineParameters( args,
				permitted_flags,
				explanation_keys.toArray(new String[explanation_keys.size()]),
				explanations.toArray(new String[explanations.size()]),
				System.err );

		// To indicate if invalid command line arguments are used (beyond the
		// basic checking of the parseCommandLineParameters method
		boolean print_valid_and_quit = false;

		// Perform the special URL decoding utility operation and ends execution
		if (parsed_args.containsKey("-urldecode"))
		{
			if (parsed_args.size() > 1)
			{
				System.err.println("The \"-urldecode\" flag may not be used in combination with any other flags.");
				print_valid_and_quit = true;
			}
			else urlDecodAndQuit(parsed_args.get("-urldecode"));
		}

		// Perform the special ACE XML to text utility operation (without URL
		// decoding) and ends execution
		if (parsed_args.containsKey("-acexmltotxt"))
		{
			if (parsed_args.size() > 1)
			{
				System.err.println("The \"-acexmltotxt\" flag may not be used in combination with any other flags.");
				print_valid_and_quit = true;
			}
			else aceXMLToTextAndQuit(parsed_args.get("-acexmltotxt"), false);
		}

		// Perform the special ACE XML to text utility operation (with URL
		// decoding (and ends execution)
		if (parsed_args.containsKey("-acexmltotxtud"))
		{
			if (parsed_args.size() > 1)
			{
				System.err.println("The \"-acexmltotxtud\" flag may not be used in combination with any other flags.");
				print_valid_and_quit = true;
			}
			else aceXMLToTextAndQuit(parsed_args.get("-acexmltotxtud"), true);
		}

		// Perform additional validation using additional logic
		if (!parsed_args.containsKey("-audio") && !parsed_args.containsKey("-title"))
		{
			System.err.println("The command line arguments contain neither the \"-audio\" nor the \"-title\" flags. At least one of these two flags must be used (unless terminal utility flags are being used).");
			print_valid_and_quit = true;
		}
		if (parsed_args.containsKey("-savedir") && ( parsed_args.containsKey("-savefile") ||
		                                             parsed_args.containsKey("-saveacexmlfile") ||
		                                             parsed_args.containsKey("-savetxtfile") ) )
		{
			System.err.println("If the \"-savedir\" flag is used in the command line arguments, then none of the \"-savefile\", \"-saveacexmlfile\" or \"-savetxtfile\" flags flags may be used.");
			print_valid_and_quit = true;
		}
		else if (parsed_args.containsKey("-savefile") && ( parsed_args.containsKey("-savedir") ||
		                                                   parsed_args.containsKey("-saveacexmlfile") ||
		                                                   parsed_args.containsKey("-savetxtfile") ) )
		{
			System.err.println("If the \"-savefile\" flag is used in the command line arguments, then none of the \"-savedir\", \"-saveacexmlfile\" or \"-savetxtfile\" flags flags may be used.");
			print_valid_and_quit = true;
		}
		if (parsed_args.containsKey("-savefile"))
		{
			String extension = StringMethods.getExtension(parsed_args.get("-savefile"));
			if (extension == null || (!extension.equals(".txt") && !extension.equals(".xml")))
			{
				System.err.println("The file path following the -savefile command line switch must have an extension of either \".txt\" or \".xml\".");
				print_valid_and_quit = true;
			}
		}
		if (parsed_args.containsKey("-eraselogs"))
		{
			String this_value = parsed_args.get("-eraselogs");
			if ( !this_value.equals("artist") &&
			     !this_value.equals("album") &&
			     !this_value.equals("all") )
			{
				System.err.println("The value following the -eraselogs command line switch must have a value of \"artist\", \"album\" or \"all\".");
				print_valid_and_quit = true;
			}
		}
		if (print_valid_and_quit)
		{
			System.err.println("\nEXECUTION TERMINATED DUE TO INVALID COMMAND LINE ARGUMENTS");
			MiscellaneousMethods.parseCommandLineParameters( new String[] {"-help"},
				permitted_flags,
				explanation_keys.toArray(new String[explanation_keys.size()]),
				explanations.toArray(new String[explanations.size()]),
				System.err );
		}

		// Store significant information specified at the command line
		String source_identifier_code = "Command Line Argument";
		if (parsed_args.containsKey("-audio"))
			song_metadata.add(new MusicMetaData(source_identifier_code, "Audio Source File", parsed_args.get("-audio")));
		if (parsed_args.containsKey("-title"))
			song_metadata.add(new MusicMetaData(source_identifier_code, "Song Title", parsed_args.get("-title")));
		if (parsed_args.containsKey("-artist"))
			song_metadata.add(new MusicMetaData(source_identifier_code, "Artist Name", parsed_args.get("-artist")));
		if (parsed_args.containsKey("-album"))
			song_metadata.add(new MusicMetaData(source_identifier_code, "Album Title", parsed_args.get("-album")));

		// Return the parsed command line arguments
		return parsed_args;
	}


	/**
	 * Parse the configurations file at the default location or, if this fails,
	 * generate a new one with  default configuration values. Any problems that
	 * occur are printed to standard error. Print a status update to standard
	 * out if appropriate.
	 *
	 * @return	The parsed (or auto-generated) configurations.
	 */
	public static ConfigDataAndParser parseConfigurationFile()
	{
		ConfigDataAndParser configs = new ConfigDataAndParser("./jSongMinerConfigs.xml", System.err );

		if (configs.print_current_status_to_terminal)
			System.out.println("\nLoading of configuration settings complete.\n");

		return configs;
	}


	/**
	 * Update the given configuration settings basedon command line arguments.
	 * Note that these configuration changes are temporary (i.e. they are not
	 * saved). Also delete existing artist and/or album log files, if specified
	 * in the command line arguments.
	 *
	 * @param command_line_args	The parsed command line arguments.
	 * @param configs			The parsed configuration settings.
	 * @param error_stream		To print error updates to. Nothing is printed
	 *							if this is null.
	 * @return					An array of size 2 holding the paths of output
	 *							files to save extracted metadata to based on
	 *							overrides specified in the command line
	 *							arguments. Element 0 corresponds to the ACE XML
	 *							file output and Element 1 corresponds to the
	 *							text file output. This array may not be null,
	 *							but one or both of its entries may be null if
	 *							the configuration settings and/or extracted data
	 *							itself are to be used whether and where the data
	 *							is to be saved.
	 */
	public static String[] updateConfigsBasedOnCommandLineArgs( HashMap<String, String> command_line_args,
			ConfigDataAndParser configs,
			PrintStream error_stream )
	{
		String[] save_file_path = {null, null};

		if (command_line_args.containsKey("-eraselogs"))
		{
			if ( command_line_args.get("-eraselogs").equals("artist") ||
			     command_line_args.get("-eraselogs").equals("all")	)
			{
				File to_erase = new File(configs.artists_already_accessed_file_path);
				if (to_erase.exists())
				{
					boolean deleted = to_erase.delete();
					if (!deleted && error_stream != null)
						error_stream.println("Could not delete the artist log at " + configs.artists_already_accessed_file_path + ".\n");
				}
			}
			if ( command_line_args.get("-eraselogs").equals("album") ||
			     command_line_args.get("-eraselogs").equals("all")	)
			{
				File to_erase = new File(configs.albums_already_accessed_file_path);
				if (to_erase.exists())
				{
					boolean deleted = to_erase.delete();
					if (!deleted && error_stream != null)
						error_stream.println("Could not delete the album log at " + configs.albums_already_accessed_file_path + ".\n");
				}
			}
		}

		if (command_line_args.containsKey("-savedir"))
		{
			configs.songs_save_directory = command_line_args.get("-savedir");
			configs.artists_save_directory = command_line_args.get("-savedir");
			configs.albums_save_directory = command_line_args.get("-savedir");
		}

		if (command_line_args.containsKey("-savefile"))
		{
			String path = command_line_args.get("-savefile");
			String extension = StringMethods.getExtension(path);

			configs.package_song_artist_album = true;

			if (extension.equals(".txt"))
			{
				configs.save_output_as_ace_xml = false;
				configs.save_output_as_txt = true;
				save_file_path[1] = path;
			}
			else if (extension.equals(".xml"))
			{
				configs.save_output_as_ace_xml = true;
				configs.save_output_as_txt = false;
				save_file_path[0] = path;
			}
		}

		if (command_line_args.containsKey("-saveacexmlfile"))
		{
			save_file_path[0] = command_line_args.get("-saveacexmlfile");
			configs.package_song_artist_album = true;
			configs.save_output_as_ace_xml = true;
		}

		if (command_line_args.containsKey("-savetxtfile"))
		{
			save_file_path[1] = command_line_args.get("-savetxtfile");
			configs.package_song_artist_album = true;
			configs.save_output_as_txt = true;
		}

		return save_file_path;
	}


	/**
	 * Set up file and data structure to note artists or albums for which
	 * metadata has already been extracted. Attempt to parse the contents from
	 * the corresponding file, and create an empty hash map if this cannot be
	 * done. Print status and error reports if appropriate.
	 *
	 * @param already_accessed_file_path	The path of the Java serialized
	 *										object containing the names and
	 *										the metadata for artists albums that
	 *										have already had their metadata
	 *										extracted.
	 * @param should_reextract				Whether or not metadata for the
	 *										artist or album should be
	 *										reextracted even if it has already
	 *										been previously extracted.
	 * @param type_extracting				The kind of metadata that is being
	 *										extracted. Should be either "album"
	 *										or "artist". Used for reporting
	 *										purposes only.
	 * @param status_stream					To print status updates to. Nothing
	 *										printed if this is null.
	 * @param error_stream					To print error updates to. Nothing
	 *										printed if this is null.
	 * @return								A HashMap with keys that correspond
	 *										to artist or album names and values
	 *										that correspond to vectors of
	 *										extracted metadata. Is either empty
	 *										or contains the values parsed from
	 *										the specified file.
	 */
	public static HashMap<String, Vector<MusicMetaData>> parseAlreadyExtractedMetadata( String already_accessed_file_path,
			boolean should_reextract,
			String type_extracting,
			PrintStream status_stream,
			PrintStream error_stream )
	{
		HashMap<String, Vector<MusicMetaData>> metadata_so_far = null;

		if (!should_reextract)
		{
			if (status_stream != null)
				status_stream.println("Parsing " + type_extracting + "s for which metadata has already been extracted...\n");
			metadata_so_far = readArtistsOrAlbumsExtracted(already_accessed_file_path);
			if (metadata_so_far == null)
			{
				if (error_stream != null)
					error_stream.println("Unable to load previously extracted " + type_extracting + "s.\n");
			}
		}

		if (metadata_so_far == null)
		{
			if (error_stream != null)
				error_stream.println("Creating new data structures for logging " + type_extracting + "s that have already had their metadata extracted...\n");
			metadata_so_far = new HashMap< String, Vector<MusicMetaData> >();
		}

		return metadata_so_far;
	}


	/**
	 * Attempt to parse metadata from the specified audio file and store it.
	 *
	 * @param to_extract_audio_path	The audio file to extract metadata from.
	 *								Nothing will be done if this is null.
	 * @param status_stream			To print status updates to. Nothing printed
	 *								if this is null.
	 * @param error_stream			To print error updates to. Nothing printed
	 *								if this is null.
	 * @return						An object containing the parsed embedded
	 *								metadata. Null if no data could be parsed
	 *								or if the specified file path is invalid.
	 */
	public static EmbeddedTagMiner extractEmbeddedMetadata(String to_extract_audio_path,
			PrintStream status_stream,
			PrintStream error_stream )
	{
		if (to_extract_audio_path != null)
		{
			try
			{
				status_stream.println("Parsing metadata embedded in the specified audio file...\n");
				return new EmbeddedTagMiner(to_extract_audio_path);
			}
			catch (Exception e)
			{
				if (error_stream != null)
					error_stream.println("Could not extract metadata embedded in " + to_extract_audio_path + ".\n");
			}
		}
		return null;
	}


	/**
	 * Returns the embedded metadata associated with the specified file (or the
	 * value specified in the model_value parameter, if any). This is
	 * not based on fingerprinting (fingerprinting is done later). If the
	 * model_value parameter is non-null, then its value is returned. If it is
	 * null, then embedded metadata extraction is attempted on the audio file
	 * referred to by the audio_file_path parameter. If this is succesful, the
	 * requested type of metadata (as set by the metadata_type_identifier
	 * parameter) is returned. If this is unsuccesful, then null is returned and
	 * an explanatory message is printed to error_stream.
	 *
	 * @param emb_miner					An extractor for extracting embedded
	 *									metadata for a specified file. Null if
	 *									unavailable.
	 * @param audio_path				The path of the audio file from which
	 *									emb_miner extracted tags. Null if there
	 *									is no such file. Is only used to see
	 *									if there is such a file by testing if is
	 *									null or not.
	 * @param may_extract_embedded		Whether or not embedded metadata
	 *									extraction is permitted (based on user
	 *									settings).
	 * @param metadata_type_identifier	The type of metadata to extract. May be
	 *									either "artist" or "title".
	 * @param model_value				The model metadata value. If this is
	 *									non-null then it is considered to be
	 *									more reliable than any embedded
	 *									metadata, so it is returned.
	 * @param error_stream				To print error updates to. Nothing
	 *									printed if this is null.
	 * @return							The requested typ of metadata, or null
	 *									if it could not be found.
	 */
	public static String getCandidateMetadataValue( EmbeddedTagMiner emb_miner,
			String audio_path,
			boolean may_extract_embedded,
			String metadata_type_identifier,
			String model_value,
			PrintStream error_stream )
	{
		try
		{
			if (model_value != null)
				return model_value;
			else if (emb_miner == null)
			{
				if (may_extract_embedded && audio_path != null)
					throw new Exception();
				else return null;
			}
			else
			{
				if (metadata_type_identifier.equals("title"))
					return emb_miner.getTitle();
				else if (metadata_type_identifier.equals("artist"))
					return emb_miner.getArtist();
				else if (metadata_type_identifier.equals("album"))
					return emb_miner.getAlbum();
				else return null;
			}
		}
		catch (Exception e)
		{
			if (error_stream != null)
				error_stream.println("Could not extract embedded " + metadata_type_identifier + " metadata from the audio file.\n");
			return null;
		}
	}


	/**
	 * Attempt to extract the Echo Nest Song ID for the provided song using
	 * the most reliable methodology available. Three techniques are
	 * succesfively applied unitl one is succesful. The first is local
	 * fingerprinting, the second is remote fingerprinting and the third is
	 * embedded metadata-based identification. Print status or error messages as
	 * appropriate.
	 *
	 * <p>This method returns the Echo Nes Song ID as soon as one of these
	 * techniques is successful, and does not continue on to try other
	 * techniques unless the prior approaches did not work.
	 *
	 * @param music_file_path				The path of the audio file to
	 *										identify. Neither local nor remote
	 *										fingerprinting will be performed if
	 *										this is null.
	 * @param model_song_title_metadata		The title of the song.
	 *										Identification based on embedded
	 *										metadata will not be performed
	 *										if this is null.
	 * @param model_artist_metadata			The "artist" of the song (typically
	 *										refers to performer/band, although
	 *										it is sometimes used to refer to
	 *										composer, especially for classical
	 *										music). This may be null if this
	 *										information is unknown.
	 * @param en_miner						The object performing Echo Nest data
	 *										mining.
	 * @param en_codegen_run_path			The path of the  executable or
	 *										script used to run it. Local
	 *										fingerprinting will not be attempted
	 *										if this is null.
	 *										e.g. "./ENFP_Codegen.sh"
	 * @param en_codegen_directory			The path of the directory holding
	 *										the Echo Nest codegen fingerprinting
	 *										binary. Local fingerprinting will
	 *										not be attempted if this is null.
	 *										e.g. "/home/ENMF/"
	 * @param status_stream					To print status updates to. Nothing
	 *										printed if this is null.
	 * @param error_stream					To print error updates to. Nothing
	 *										printed if this is null.
	 * @return								The Echo Nest Song ID for the given
	 *										song.
	 */
	public static String extractEchoNestSongID( String music_file_path,
			String model_song_title_metadata,
			String model_artist_metadata,
			EchoNestMiner en_miner,
			String en_codegen_run_path,
			String en_codegen_directory,
			PrintStream status_stream,
			PrintStream error_stream )
	{
		String en_song_id = null;

		if (status_stream != null)
			status_stream.println("Attempting to identify the song using the best available Echo Nest method...\n");

		try
		{
			en_song_id = en_miner.getAndStoreEchoNestSongIDUsingBestAvailableMethod( music_file_path,
					model_song_title_metadata,
					model_artist_metadata,
					true,
					error_stream,
					en_codegen_run_path,
					en_codegen_directory );
		}
		catch (Exception e) {}

		if (en_song_id != null)
		{
			if (status_stream != null)
				status_stream.println("Song identification successfully completed: Echo Nest ID " + en_song_id + "\n");
		}
		else if (error_stream != null)
			error_stream.println("\nEcho Nest song identification FAILED!\n");

		return en_song_id;
	}


	/**
	 * Prepare a LastFMMiner object to mine Last.FM. Set it to be associated
	 * with the song with the specified title and artist name. Print status or
	 * error messages as appropriate.
	 *
	 * @param model_song_title_metadata	The believed title of the song.
	 *									Identification will not be performed
	 *									if this is null.
	 * @param model_artist_metadata		The believed "artist" of the song
	 *									(typically refers to performer/band,
	 *									although it is sometimes used to refer
	 *									to composer, especially for classical
	 *									music). This may be null if this
	 *									information is unknown.
	 * @param last_fm_api_key			The Last.FM API key used to access
	 *									Last.FM web services.
	 * @param status_stream				To print status updates to. Nothing
	 *									printed if this is null.
	 * @param error_stream				To print error updates to. Nothing
	 *									printed if this is null.
	 * @return							A LastFMMiner associated with the
	 *									specified song. Null if no association
	 *									could be made.
	 */
	public static LastFMMiner matchToLastFMTrack( String model_song_title_metadata,
			String model_artist_metadata,
			String last_fm_api_key,
			PrintStream status_stream,
			PrintStream error_stream )
	{
		LastFMMiner lfm_miner = null;

		if (status_stream != null)
			status_stream.println("Attempting to match the song to a Last.FM track...\n");

		try
		{
			if (model_song_title_metadata == null)
				throw new Exception("No model song title to base identification upon available.");
			lfm_miner = new LastFMMiner( model_song_title_metadata,
					model_artist_metadata,
					last_fm_api_key );
			if (status_stream != null)
				status_stream.println("Last.FM successfully matched song: " + model_song_title_metadata + ", by " + model_artist_metadata + "\n");
		}
		catch (Exception e)
		{
			if (error_stream != null)
				error_stream.println("Last.FM song identification FAILED!: " + e.getMessage() + "\n");
		}

		return lfm_miner;
	}


	/**
	 * Store identifying information in the given Vector of MusicMetaData. Any
	 * information previously in this Vector is erased. The information stored
	 * is the song title, artist name, album title, audio file path, Echo Nest
	 * Song ID and Last.FM Track URL.
	 *
	 * @param song_metadata			The vector to clear and store identifying
	 *								metadata in.
	 * @param to_extract_audio_path	The file path of the audio file that is
	 *								being identified.
	 * @param identifying_title		The title of the song.
	 * @param identifying_artist	The name of the artist.
	 * @param identifying_album		The title of the album.
	 * @param en_song_id			The Echo Nest Song ID.
	 * @param lfm_miner				The object storing metadata for a track.
	 * @param store_fails			If this is true, then for each individual
	 *								piece of metadata that was not extracted
	 *								(i.e. for each of the above parameters that
	 *								is null) an indication is added to
	 *								song_metadata. If this is false then fields
	 *								could not be extracted are simply ignored.
	 */
	public static void storeIdentifyingMetadataOnly( Vector<MusicMetaData> song_metadata,
			String to_extract_audio_path,
			String identifying_title,
			String identifying_artist,
			String identifying_album,
			String en_song_id,
			LastFMMiner lfm_miner,
			boolean store_fails )
	{
		// Erase any existing contents in song_metadata
		song_metadata.clear();

		if (to_extract_audio_path != null)
			song_metadata.add(new MusicMetaData("jSongMiner", "Audio Source File", to_extract_audio_path));
		else if (store_fails)
			song_metadata.add(new MusicMetaData("jSongMiner", "Audio Source File", "Could not extract this information."));

		if (identifying_title != null)
			song_metadata.add(new MusicMetaData("jSongMiner", "Song Title", identifying_title));
		else if (store_fails)
			song_metadata.add(new MusicMetaData("jSongMiner", "Song Title", "Could not extract this information."));

		if (identifying_artist != null)
			song_metadata.add(new MusicMetaData("jSongMiner", "Artist Name", identifying_artist));
		else if (store_fails)
			song_metadata.add(new MusicMetaData("jSongMiner", "Artist Name", "Could not extract this information."));

		if (identifying_album != null)
			song_metadata.add(new MusicMetaData("jSongMiner", "Album Title", identifying_album));
		else if (store_fails)
			song_metadata.add(new MusicMetaData("jSongMiner", "Album Title", "Could not extract this information."));

		if (en_song_id != null)
			song_metadata.add(new MusicMetaData("jSongMiner", "Echo Nest Song ID", en_song_id));
		else if (store_fails)
			song_metadata.add(new MusicMetaData("jSongMiner", "Echo Nest Song ID", "Could not extract this information."));

		String track_url = null;
		if (lfm_miner != null)
		{
			track_url = lfm_miner.getLastFMTrackURL();
			if (track_url != null)
				song_metadata.add(new MusicMetaData("jSongMiner", "Last.FM Track URL", track_url));
		}
		if (track_url == null && store_fails)
			song_metadata.add(new MusicMetaData("jSongMiner", "Last.FM Track URL", "Could not extract this information."));
	}


	/**
	 * Extract all available song and, if appropriate, artist metadata for the
	 * specified Echo Nest Song ID from the Echo Nest API. Adds the extracted
	 * metadata to the two Vector parameters of this method, respectively.
	 * Print status or error messages as appropriate.
	 *
	 * <p>Song metadata is always extracted. Artist metadata is only extracted
	 * if it has not already been extracted for the given artist during the
	 * processing of another song, as indicated by the presence of the artist's
	 * name (in lower case) in the artist_metadata_so_far parameter. Note
	 * that this method does <b>not</b> update this hash map in any way, and it
	 * is left to the calling object to do this.
	 *
	 * @param en_song_id				The Echo Nest Song ID of the song to
	 *									extract metadata for. Null if no
	 *									association could be made with a
	 *									particular song, in which case no
	 *									extraction is attempted.
	 * @param song_metadata				A Vector of metadata already
	 *									extracted for this song. May not
	 *									be null, but may be empty.
	 * @param artist_metadata			A Vector of metadata already
	 *									extracted for the artist associated
	 *									with this song. May not be null, but
	 *									may be empty.
	 * @param artist_metadata_so_far	A hash map whose keys correspond to
	 *									artist names (converted to lower
	 *									case), and whose values correspond
	 *									to vectors of corresponding artist
	 *									metadata. The contents are based on
	 *									the processing of previous songs.
	 *									This parameter may be null, in which
	 *									case it is ignored	and artist
	 *									extraction happens automatically.
	 *									Also, the values may be null, in
	 *									order to save space and processing
	 *									for the calling object, but this has
	 *									no effect on this method, as only
	 *									the keys are used.
	 * @param store_fails				If this is true, then for each
	 *									individual piece of metadata that
	 *									cannot be extracted from the Echo
	 *									Nest API an indication is added to
	 *									the stored MusicMetaData
	 *									highlighting the failure. If this is
	 *									false then fields that cannot
	 *									be extracted are simply ignored.
	 *									Note that this parameter has no
	 *									effect on song identification
	 *									specifically.
	 * @param en_miner					The object that performs the actual
	 *									mining of the Echo Nest.
	 * @param status_stream				To print status updates to. Nothing
	 *									printed if this is null.
	 * @param error_stream				To print error updates to. Nothing
	 *									printed if this is null.
	 */
	public static void extractEchoNestMetadata( String en_song_id,
			Vector<MusicMetaData> song_metadata,
			Vector<MusicMetaData> artist_metadata,
			HashMap< String, Vector<MusicMetaData> > artist_metadata_so_far,
			boolean store_fails,
			EchoNestMiner en_miner,
			PrintStream status_stream,
			PrintStream error_stream )
	{
		if (en_song_id != null)
		{
			if (status_stream != null)
				status_stream.println("Attempting to extract Echo Nest metadata...\n");
			boolean echo_nest_found_metadata = false;
			try
			{
				echo_nest_found_metadata = en_miner.getAllAvailableNewMetaData( en_song_id,
						song_metadata,
						artist_metadata,
						artist_metadata_so_far,
						true,
						store_fails );

			}
			catch (Exception e) {}
			if (echo_nest_found_metadata)
			{
				if (status_stream != null)
					status_stream.println("Echo Nest metadata successfully extracted.\n");
			}
			else if (error_stream != null)
				error_stream.println("Echo Nest metadata extraction FAILED!\n");
		}
	}


	/**
	 * Extracts all available song and, if appropriate, artist and album
	 * metadata from the Last.FM API for the song stored in it. Adds
	 * the extracted metadata to the Vector parameters of this method. Print
	 * status or error messages as appropriate.
	 *
	 * <p>Song metadata is always extracted. Artist and album metadata are only
	 * extracted if they have not already been extracted for the given artist
	 * or album (respectively) during the processing of another song, as
	 * indicated by the presence of the artist's name (in lower case) in the
	 * artist_metadata_so_far parameter, or the album's title (in lower case)
	 * in the album_metadata_so_far parameter, respectively. Note that
	 * this method does <b>not</b> update either of these hash maps in any way,
	 * as it is left to the calling object to do this.
	 *
	 * @param lfm_miner					A LastFMMiner associated with a
	 *									particular specified song. Null if no
	 *									association could be made with a
	 *									particular song, in which case no
	 *									extraction is attempted.
	 * @param song_metadata				A Vector of metadata already
	 *									extracted for this song. May not
	 *									be null, but may be empty.
	 * @param artist_metadata			A Vector of metadata already
	 *									extracted for the artist associated
	 *									with this song. May not be null, but
	 *									may be empty.
	 * @param album_metadata			A Vector of metadata already
	 *									extracted for the album associated
	 *									with this song. May not be null, but
	 *									may be empty.
	 * @param artist_metadata_so_far	A hash map whose keys correspond to
	 *									artist names (converted to lower
	 *									case), and whose values correspond
	 *									to vectors of corresponding artist
	 *									metadata. The contents are based on
	 *									the processing of previous songs.
	 *									This parameter may be null, in which
	 *									case it is ignored	and artist
	 *									extraction happens automatically.
	 *									Also, the values may be null, in
	 *									order to save space and processing
	 *									for the calling object, but this has
	 *									no effect on this method, as only
	 *									the keys are used.
	 * @param album_metadata_so_far		A hash map whose keys correspond to
	 *									artist names (converted to lower
	 *									case), and whose values correspond
	 *									to vectors of corresponding artist
	 *									metadata. The contents are based on
	 *									the processing of previous songs.
	 *									This parameter may be null, in which
	 *									case it is ignored	and artist
	 *									extraction happens automatically.
	 *									Also, the values may be null, in
	 *									order to save space and processing
	 *									for the calling object, but this has
	 *									no effect on this method, as only
	 *									the keys are used.
	 * @param store_fails				If this is true, then for each
	 *									individual piece of metadata that
	 *									cannot be extracted from the Last.FM
	 *									API an indication is added tothe stored
	 *									MusicMetaData data highlighting the
	 *									failure. If this is false then fields
	 *									that cannot	be extracted are simply
	 *									ignored. Note that this parameter has no
	 *									effect on song identification
	 *									specifically.
	 * @param status_stream				To print status updates to. Nothing
	 *									printed if this is null.
	 * @param error_stream				To print error updates to. Nothing
	 *									printed if this is null.
	 * @throws Exception				An informative exception is thrown if a
	 *									problem occurs.
	 */
	public static void extractLastFMMetadata( LastFMMiner lfm_miner,
			Vector<MusicMetaData> song_metadata,
			Vector<MusicMetaData> artist_metadata,
			Vector<MusicMetaData> album_metadata,
			HashMap< String, Vector<MusicMetaData> > artist_metadata_so_far,
			HashMap< String, Vector<MusicMetaData> > album_metadata_so_far,
			boolean store_fails,
			PrintStream status_stream,
			PrintStream error_stream )
			throws Exception
	{
		if (lfm_miner != null)
		{
			if (status_stream != null)
				status_stream.println("Attempting to extract Last.FM metadata...\n");
			boolean last_fm_found_metadata = false;
			last_fm_found_metadata = lfm_miner.getAllAvailableNewMetaData( song_metadata,
					artist_metadata,
					album_metadata,
					artist_metadata_so_far,
					album_metadata_so_far,
					store_fails );
			if (last_fm_found_metadata)
			{
				if (status_stream != null)
					status_stream.println("Last.FM metadata successfully extracted.\n");
			}
			else if (error_stream != null)
				error_stream.println("Last.FM metadata extraction FAILED!\n");
		}
	}


	/**
	 * Stores all available song metadata extracted from the metadata embedded
	 * in an audio file. Adds the extracted metadata to the Vector parameter of
	 * this method. Prints status or error messages as appropriate.
	 *
	 * @param emb_miner			An object associated with a particular audio
	 *							file containing metadata that has already been
	 *							parsed from it. Null if embedded metadata could
	 *							not be parased from the audio file, in which
	 *							case no extraction is performed.
	 * @param song_metadata		A Vector of metadata already extracted for this
	 *							song. May not be null, but may be empty.
	 * @param store_fails		If this is true, then for each individual piece
	 *							of metadata that cannot be extracted an
	 *							indication is added tothe stored MusicMetaData
	 *							data highlighting the failure. If this is false
	 *							then fields that cannot be extracted are simply
	 *							ignored. Note that this parameter has no effect
	 *							on song identification specifically.
	 * @param status_stream		To print status updates to. Nothing printed if
	 *							this is null.
	 * @param error_stream		To print error updates to. Nothing printed if
	 *							this is null.
	 * @throws Exception		An informative exception is thrown if a	problem
	 *							occurs.
	 */
	public static void storeEmbeddedMetadata( EmbeddedTagMiner emb_miner,
			Vector<MusicMetaData> song_metadata,
			boolean store_fails,
			PrintStream status_stream,
			PrintStream error_stream )
			throws Exception
	{
		if (emb_miner != null)
		{
			if (status_stream != null)
				status_stream.println("Storing embedded metadata parsed from the audio file...\n");
			boolean embedded_metadata_found_metadata = false;
			embedded_metadata_found_metadata = emb_miner.getAllAvailableNewMetaData( song_metadata, store_fails );
			if (embedded_metadata_found_metadata)
			{
				if (status_stream != null)
					status_stream.println("Embedded metadata parsed from the audio file successfully stored.\n");
			}
			else if (error_stream != null)
				error_stream.println("Could not store any embedded metadata parsed from the audio file.\n");
		}
	}


	/**
	 * Process the provided song, artist and album metadata in order to produce
	 * versions of the metadata formatted using the Dublin Core standard,
	 * including unqualified and/or qualified Dublin Core. The user may also
	 * opt to delete the original metadata after this is done, based on the
	 * include_other_metadata_with_dublin_core configuration setting. The
	 * three provided Vectors are altered to hold the results. Note that the
	 * Dublin Core MusicMetaData objects will have "" for in their source
	 * fields. Note also that if the repress_unqualified_artist_and_album
	 * parameter is true that unqualified artist and album Dublin Core data
	 * will not be produced even if doing so is possible (this has no effect
	 * on qualified Dublin Core).
	 *
	 * @param include_unqualified_dublin_core			Whether or not to
	 *													interperet the metadata
	 *													into unqualified DC.
	 * @param include_qualified_dublin_core				Whether or not to
	 *													interperet the metadata
	 *													into qualified DC.
	 * @param include_other_metadata_with_dublin_core	Whether or not to delete
	 *													the original contents of
	 *													the three provided
	 *													vectors after the DC
	 *													data has been produced.
	 * @param repress_unqualified_artist_and_album		Whether or not to
	 *													produce unqualified
	 *													DC metadata for artists
	 *													and albums (even if the
	 *													data is available).
	 * @param song_metadata								A Vector of metadata
	 *													already extracted for
	 *													this song. May not be
	 *													null, but may be empty.
	 * @param artist_metadata							A Vector of metadata
	 *													already extracted for
	 *													the artist associated
	 *													with this song. May not
	 *													be null, but may be
	 *													empty.
	 * @param album_metadata							A Vector of metadata
	 *													already extracted for
	 *													the album associated
	 *													with this song. May not
	 *													be null, but may be
	 *													empty.
	 * @param status_stream								To print status updates
	 *													to. Nothing printed if
	 *													this is null.
	 */
	public static void prepareDublinCore( boolean include_unqualified_dublin_core,
			boolean include_qualified_dublin_core,
			boolean include_other_metadata_with_dublin_core,
			boolean repress_unqualified_artist_and_album,
			Vector<MusicMetaData> song_metadata,
			Vector<MusicMetaData> artist_metadata,
			Vector<MusicMetaData> album_metadata,
			PrintStream status_stream )
	{
		if (include_unqualified_dublin_core || include_qualified_dublin_core)
		{
			// To store Dublin Core metadata
			Vector<MusicMetaData> dc_song_metadata = new Vector<MusicMetaData>();
			Vector<MusicMetaData> dc_artist_metadata = new Vector<MusicMetaData>();
			Vector<MusicMetaData> dc_album_metadata = new Vector<MusicMetaData>();

			// Parse out Dublin Core unqualified data
			if (include_unqualified_dublin_core)
			{
				if (status_stream != null)
					status_stream.println("Formatting extracted metadata into unqualified Dublin Core...\n");
				dc_song_metadata.addAll(DublinCoreInterpreter.getSongUnqualifiedDublinCore(song_metadata));
				if (!repress_unqualified_artist_and_album)
				{
					dc_artist_metadata.addAll(DublinCoreInterpreter.getArtistUnqualifiedDublinCore(artist_metadata));
					dc_album_metadata.addAll(DublinCoreInterpreter.getAlbumUnqualifiedDublinCore(album_metadata));
				}
			}

			// Parse out Dublin Core qualified data
			if (include_qualified_dublin_core)
			{
				if (status_stream != null)
					status_stream.println("Formatting extracted metadata into qualified Dublin Core...\n");
				dc_song_metadata.addAll(DublinCoreInterpreter.getSongQualifiedDublinCore(song_metadata));
				dc_artist_metadata.addAll(DublinCoreInterpreter.getArtistQualifiedDublinCore(artist_metadata));
				dc_album_metadata.addAll(DublinCoreInterpreter.getAlbumQualifiedDublinCore(album_metadata));
			}

			// Remove all non-Dublin Core-formatted data
			if (!include_other_metadata_with_dublin_core)
			{
				if (status_stream != null)
					status_stream.println("Removing non-Dublin Core formatted metadata...\n");
				song_metadata.clear();
				artist_metadata.clear();
				album_metadata.clear();
			}

			// Add the Dublin Core-formatted data
			song_metadata.addAll(dc_song_metadata);
			artist_metadata.addAll(dc_artist_metadata);
			album_metadata.addAll(dc_album_metadata);
		}
	}


	/**
	 * Save all of the just-extracted Echo Nest and Last.FM metadata for the
	 * song and, if apprpropriate, artist and album, in either a text file where
	 * each odd line is a field identifier and each even line is the value for
	 * the field named on the preceding line, or as an ACE XML 1.1
	 * Classifications file. Song, artist and album data may each saved be in
	 * its own auto-named file, or it may be saved in separate files, depending
	 * on the package_song_artist_album parameter. Print status or error
	 * messages as appropriate.
	 *
	 * <p>Each ACE XML 1.1 Classifications File is saved to contain a single
	 * instance with all of the MusicMetaData objects saved as misc_info. The
	 * instance identifier is found based on the the provided identifier_keys
	 * (see code listing), and this identifier is used to auto-generate the file
	 * name. If a file with the selected name already exists, then an underscore
	 * followed by a numerical value is appended to the end of the file name
	 * such that the name is unique.
	 *
	 * @param as_ace_xml				If this is true, then the data is saved
	 *									as ACE XML. If it is false, then it is
	 *									saved as txt.
	 * @param song_metadata				A Vector of metadata already
	 *									extracted for this song. May not
	 *									be null, but may be empty.
	 * @param artist_metadata			A Vector of metadata already
	 *									extracted for the artist associated
	 *									with this song. May not be null, but
	 *									may be empty.
	 * @param album_metadata			A Vector of metadata already
	 *									extracted for the album associated
	 *									with this song. May not be null, but
	 *									may be empty.
	 * @param package_song_artist_album Whether or not to package the song,
	 *									artist and album metadata in the above
	 *									three parameters into a single file.
	 *									If this is true, then all of this
	 *									metadata is saved in a single file in
	 *									the songs_save_directory. If it is false
	 *									then files may also be saved in the
	 *									artists_save_directory and
	 *									albums_save_directory directories.
	 * @param songs_save_directory		The directory to save the ACE XML song
	 *									file in. The file name itself will be
	 *									auto-generated based on the file's
	 *									contents.
	 * @param artists_save_directory	The directory to save the ACE XML artist
	 *									file in. The file name itself will be
	 *									auto-generated based on the file's
	 *									contents. Ignored if
	 *									package_song_artist_album is true.
	 * @param albums_save_directory		The directory to save the ACE XML album
	 *									file in. The file name itself will be
	 *									auto-generated based on the file's
	 *									contents. Ignored if
	 *									package_song_artist_album is true.
	 * @param url_encode_output			Whether or not to URL encode (UTF-8) the
	 *									saved data. If this is true, the file
	 *									name will also be URL encoded.
	 * @param save_file_path			The path of the single file to save
	 *									metadata extraction results to. Ignored
	 *									if null. If it is non-null, then it
	 *									overrides all other settings.
	 * @param status_stream				To print status updates to. Nothing
	 *									printed if this is null.
	 * @param error_stream				To print error updates to. Nothing
	 *									printed if this is null.
	 */
	public static void saveData( boolean as_ace_xml,
			Vector<MusicMetaData> song_metadata,
			Vector<MusicMetaData> artist_metadata,
			Vector<MusicMetaData> album_metadata,
			boolean package_song_artist_album,
			String songs_save_directory,
			String artists_save_directory,
			String albums_save_directory,
			boolean url_encode_output,
			String save_file_path,
			PrintStream status_stream,
			PrintStream error_stream )
	{
		// Save merged metadata
		if (package_song_artist_album)
		{
			// Get a single collection of metadata
			Vector<MusicMetaData> merged_metadata = new Vector<MusicMetaData>(song_metadata.size() + artist_metadata.size() + album_metadata.size());
			merged_metadata.addAll(song_metadata);
			merged_metadata.addAll(artist_metadata);
			merged_metadata.addAll(album_metadata);

			if (merged_metadata.size() != 0)
			{
				try
				{
					// Remove duplicates from the merged metadata
					MusicMetaData.removeDuplicateEntries(merged_metadata);

					// Save the data
					if (status_stream != null)
					{
						if (!as_ace_xml)
							status_stream.println("Saving extracted metadata as text...\n");
						else
							status_stream.println("Saving extracted metadata as ACE XML...\n");
					}
					String[] identifier_keys = { "Echo Nest Song ID",
							"Music Brainz Song ID",
							"Last.FM Track URL",
							"Song Title",
							"Audio Source File",
							"dc.Identifier^echonestsongid",
							"dc.Identifier^musicbrainzsongid",
							"dc.Identifier^lastfmtrackurl",
							"dc.Title^songtitle",
							"dc.Identifier^filepath",
							"dc.Identifier",
							"dc.Title" };
					if (as_ace_xml)
						MusicMetaData.saveInACEXMLClassificationsFile( identifier_keys,
								merged_metadata,
								url_encode_output,
								songs_save_directory,
								save_file_path );
					else
						MusicMetaData.saveInTextFile( identifier_keys,
								merged_metadata,
								url_encode_output,
								songs_save_directory,
								save_file_path );
				}
				catch (Exception e)
				{
					if (error_stream != null)
						error_stream.println("Saving of metadata FAILED!: " + e.getMessage());
				}
			}
		}

		// Save song data
		if (song_metadata.size() != 0 && !package_song_artist_album)
		{
			try
			{
				if (status_stream != null)
				{
					if (!as_ace_xml)
						status_stream.println("Saving extracted song data as text...\n");
					else
						status_stream.println("Saving extracted song data as ACE XML...\n");
				}
				String[] identifier_keys = { "Echo Nest Song ID",
							"Music Brainz Song ID",
							"Last.FM Track URL",
							"Song Title",
							"Audio Source File",
							"dc.Identifier^echonestsongid",
							"dc.Identifier^musicbrainzsongid",
							"dc.Identifier^lastfmtrackurl",
							"dc.Title^songtitle",
							"dc.Identifier^filepath",
							"dc.Identifier",
							"dc.Title" };
				if (as_ace_xml)
					MusicMetaData.saveInACEXMLClassificationsFile( identifier_keys,
							song_metadata,
							url_encode_output,
							songs_save_directory,
							save_file_path );
				else
					MusicMetaData.saveInTextFile( identifier_keys,
							song_metadata,
							url_encode_output,
							songs_save_directory,
							save_file_path );
			}
			catch (Exception e)
			{
				if (error_stream != null)
					error_stream.println("Saving of song data FAILED!: " + e.getMessage());
			}
		}

		// Save the artist data
		if (artist_metadata.size() != 0 && !package_song_artist_album)
		{
			try
			{
				if (status_stream != null)
					status_stream.println("Saving extracted artist data in ACE XML...\n");
				String[] identifier_keys = { "Echo Nest Artist ID",
						"Music Brainz Artist ID",
						"Last.FM Artist URL",
						"Artist Name",
						"dc.Identifier^echonestartistid",
						"dc.Identifier^musicbrainzartistid",
						"dc.Identifier^lastfmartisturl",
						"dc.Title^artistname",
						"dc.Identifier",
						"dc.Title" };
				if (as_ace_xml)
					MusicMetaData.saveInACEXMLClassificationsFile( identifier_keys,
							artist_metadata,
							url_encode_output,
							artists_save_directory,
							save_file_path );
				else
					MusicMetaData.saveInTextFile( identifier_keys,
							artist_metadata,
							url_encode_output,
							artists_save_directory,
							save_file_path );
			}
			catch (Exception e)
			{
				if (error_stream != null)
					error_stream.println("Saving of artist data FAILED!: " + e.getMessage());
			}
		}

		// Save the album data
		if (album_metadata.size() != 0 && !package_song_artist_album)
		{
			try
			{
				if (status_stream != null)
					status_stream.println("Saving extracted album data in ACE XML...\n");
				String[] identifier_keys = { "Music Brainz Album ID",
						"Last.FM Album URL",
						"Album Title",
						"dc.Identifier^musicbrainzalbumid",
						"dc.Identifier^lastfmalbumurl",
						"dc.Title^albumtitle",
						"dc.Identifier",
						"dc.Title" };
				if (as_ace_xml)
					MusicMetaData.saveInACEXMLClassificationsFile( identifier_keys,
							album_metadata,
							url_encode_output,
							albums_save_directory,
							save_file_path );
				else
					MusicMetaData.saveInTextFile( identifier_keys,
							album_metadata,
							url_encode_output,
							albums_save_directory,
							save_file_path );
			}
			catch (Exception e)
			{
				if (error_stream != null)
					error_stream.println("Saving of album data FAILED!: " + e.getMessage());
			}
		}
	}


	/**
	 * Update and save either the artists or albums already extracted log. Note
	 * that the metadata saved for each artist or album here is null, as at this
	 * point only the keys (i.e. artist names or album titles in lower case) are
	 * needed in the hash map to check if metadata has already been extracted
	 * for an artist or album, and the artist or album metadata itself is saved
	 * elsewhere. Print status or error messages as appropriate.
	 *
	 * @param already_accessed_file_path	The path to save the updated hash
	 *										map to.
	 * @param metadata_so_far				A hash map whose keys correspond to
	 *										artist or album names, and whose
	 *										values correspond to vectors of
	 *										corresponding extracted metadata.
	 *										The contents are based on the
	 *										processing of previous songs.
	 *										This may not be null.
	 * @param identifying_artist			The name of the artist that is being
	 *										added to the log. Null if this
	 *										update is to albums instead of
	 *										artists. If this is not null, then
	 *										identifying_album must be.
	 * @param identifying_album				The name of the album that is being
	 *										added to the log. Null if this
	 *										update is to artists instead of
	 *										albums. If this is not null, then
	 *										identifying_artist must be.
	 * @param status_stream					To print status updates to. Nothing
	 *										printed if this is null.
	 * @param error_stream					To print error updates to. Nothing
	 *										printed if this is null.
	 */
	public static void updateAlreadyAccessedLog( String already_accessed_file_path,
			HashMap< String, Vector<MusicMetaData> > metadata_so_far,
			String identifying_artist,
			String identifying_album,
			PrintStream status_stream,
			PrintStream error_stream )
	{
		if (status_stream != null && identifying_artist != null)
			status_stream.println("Saving update to log of artists already accessed...\n");
		else if (status_stream != null && identifying_album != null)
			status_stream.println("Saving update to log of albums already accessed...\n");
		try
		{
			String key = null;
			if (identifying_artist !=null)
				key = identifying_artist.toLowerCase();
			else if (identifying_album != null)
				key = identifying_album.toLowerCase();
			writeArtistsOrAlbumsExtracted( already_accessed_file_path,
					metadata_so_far,
					key,
					null );
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			if (error_stream != null)
				error_stream.println("Updating of already accessed log file FAILED!\n");
		}
	}


	/**
	 * Print all song, artist and album metadata just extracted to standard out.
	 *
	 * @param song_metadata				The song metadata just extracted. May be
	 *									empty, but not null.
	 * @param artist_metadata			The artist metadata just extracted. May
	 *									be empty, but not null.
	 * @param album_metadata			The album metadata just extracted. May
	 *									be empty, but not null.
	 * @param package_song_artist_album	If this is true, then song, artist and
	 *									album metadata are output as a single
	 *									list. If it is false, then they are
	 *									each output as a separate list.
	 */
	public static void printMetadata( Vector<MusicMetaData> song_metadata,
			Vector<MusicMetaData> artist_metadata,
			Vector<MusicMetaData> album_metadata,
			boolean package_song_artist_album )
	{
		// Output the identifier if metadata is to be combined
		if (package_song_artist_album)
			System.out.println("\nEXTRACTED COMBINED METADATA:\n");

		// Output all extracted song metadata
		if (!package_song_artist_album)
			System.out.println("\nEXTRACTED SONG METADATA:\n");
		if (!song_metadata.isEmpty())
			for (int i = 0; i < song_metadata.size(); i++)
				song_metadata.get(i).printValues();

		// Output all extracted artist metadata
		if (!package_song_artist_album)
			System.out.println("\nEXTRACTED ARTIST METADATA:\n");
		if (!artist_metadata.isEmpty())
			for (int i = 0; i < artist_metadata.size(); i++)
				artist_metadata.get(i).printValues();

		// Output all extracted album metadata
		if (!package_song_artist_album)
			System.out.println("\nEXTRACTED ALBUM METADATA:\n");
		if (!album_metadata.isEmpty())
			for (int i = 0; i < album_metadata.size(); i++)
				album_metadata.get(i).printValues();

		// Final blank line
		System.out.println();
	}


	/**
	 * Parses a HashMap< String, Vector<MusicMetaData> > from the specified
	 * Java serialized object. This hash map has keys that correspond to artist
	 * or album names and values that correspond to vectors of corresponding
	 * extracted metadata. The contents are based on the processing of previous
	 * songs.
	 *
	 * @param file_path		The file path to read the hash map from.
	 * @return				The parsed hash map. Null is returned if it could
	 *						not be parsed.
	 */
	public static HashMap<String, Vector<MusicMetaData>> readArtistsOrAlbumsExtracted(String file_path)
	{
		try
		{
			FileInputStream fis = new FileInputStream(file_path);
			ObjectInputStream ois = new ObjectInputStream(fis);
			HashMap< String, Vector<MusicMetaData> > metadata_so_far = (HashMap< String, Vector<MusicMetaData> >) ois.readObject();
			ois.close();
			return metadata_so_far;
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			return null;
		}
	}


	/**
	 * Saves given metadata_so_far as a serialized Java object to
	 * file_path, after having added a new_metadata entry for
	 * new_key_id to it.
	 *
	 * @param file_path					The path to save the updated hash map
	 *									to.
	 * @param metadata_so_far			A hash map whose keys correspond to
	 *									artist or album names, and whose
	 *									values correspond to vectors of
	 *									corresponding extracted metadata. The
	 *									contents are based on the processing of
	 *									previous songs.	This may not be null.
	 * @param new_key_id				The artist or album name of a new artist
	 *									of album to add to metadata_so_far.
	 *									Nothing will be added to the hash map
	 *									if this is null.
	 * @param new_metadata				The metadata for the artist artist or
	 *									album referred to by new_key_id. This
	 *									may be null, in which case a new entry
	 *									will still be added to the hash map for
	 *									new_key_id, but the value for this key
	 *									will be null.
	 * @throws Exception				An exception is thrown if the file
	 *									cannot be written.
	 */
	public static void writeArtistsOrAlbumsExtracted( String file_path,
			HashMap< String, Vector<MusicMetaData> > metadata_so_far,
			String new_key_id,
			Vector<MusicMetaData> new_metadata )
			throws Exception
	{
		// Add the new key and its metadata to the hash map
		if (new_key_id != null)
			metadata_so_far.put(new_key_id, new_metadata);

		// Save the updated hash map
		try
		{
			FileOutputStream fos = new FileOutputStream(file_path);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(metadata_so_far);
			oos.close();
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			throw new Exception("Could not save the updated file.");
		}
	}


	/**
	 * URL-decode (using UTF-8) the given string and print it to standard out.
	 * Then terminate execution. If a problem occurs, a notification is printed
	 * to standard error.
	 *
	 * @param to_decode	The string to decode.
	 */
	public static void urlDecodAndQuit(String to_decode)
	{
		// URL decode to_decode and print it to standard out
		try {System.out.print(StringMethods.URLDecodeNullCompatible(to_decode));}
		catch (Exception e) {System.err.println("ERROR: " + e.getMessage() + "\n");}

		// Terminate execution
		System.exit(0);
	}


	/**
	 * Convert the given ACE XML 1.1 Classifications file to the type of text
	 * file output by jSongMiner, which is to say field names
	 * (misc_info info_type attribute values) are printed to odd lines and field
	 * values (misc_info element values) are printed to the following even
	 * lines. This is output to standard out. URL decoding may or may not be
	 * performed, depending on the url_decode parameter. Executino ends after
	 * this is complete. If a problem occurs, a notification is printed to
	 * standard error.
	 *
	 * @param file_path		The path of the ACE XML 1.1 Classifications file to
	 *						translate.
	 * @param url_decode	Whether or not to URL decode the results (UTF-8)
	 */
	public static void aceXMLToTextAndQuit( String file_path,
			boolean url_decode)
	{
		// Translate the file and print it to standard out
		try
		{
			String parsed_results = MusicMetaData.convertACEXMLToText(file_path);
			if (url_decode)
				parsed_results = StringMethods.URLDecodeNullCompatible(parsed_results);
			System.out.print(parsed_results);
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			System.err.println("ERROR: " + e.getMessage() + "\n");
		}

		// Terminate execution
		System.exit(0);
	}
}