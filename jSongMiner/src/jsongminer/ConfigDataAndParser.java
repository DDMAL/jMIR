/*
 * ConfigDataAndParser.java
 * Version 1.0
 *
 * Last modified on August 3, 2010.
 * University of Waikato and McGill University
 */

package jsongminer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import mckay.utilities.staticlibraries.FileMethods;
import mckay.utilities.xml.ParsingXMLErrorHandler;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.HashMap;

/**
 * Objects of this class hold basic configuration settings that are accessible
 * via their public fields. Objects of this class also include functionality for
 * parsing or saving these settings from or to an XML configuration file.
 *
 * <p>The Xereces library, described at http://xerces.apache.org, is used to
 * parse the XML.
 *
 * @author Cory McKay
 */
public class ConfigDataAndParser
		extends DefaultHandler
{
	/* PUBLIC FIELDS **********************************************************/


	/**
	 * If this is set to true, then the only metadata that is saved for each
	 * song is the song title, artist name, album title, unique identifier (e.g.
	 * from the Echo Nest or from Last.FM) and, if it is a file being
	 * identified, the path of the file. This overrides other configuration
	 * settings. If this  setting is set to false, then all metadata that can be
	 * accessed (in thise case under the constraints of the other configuration
	 * settings) is saved.
	 */
	public boolean identify_only;

	/**
	 * Whether or not to package extracted song, artist and album metadata into
	 * a single file. If this is true, then a single ACE XML and/or text file
	 * will be output for each song, and it will contain all extracted metadata
	 * associated with the song, the artist and the album. If this is set to
	 * false, then a separate ACE XML and/or text file will be output each for
	 * the song, artist and album metadata corresponding to the song under
	 * consideration. Note, however, that artist and album metadata may not be
	 * extracted for any given song, depending on the
	 * reextract_known_artist_metadata and reextract_known_album_metadata
	 * configuration settings and on the extraction history. If artist and/or
	 * album metadata is not extracted for a song, then a corresponding file
	 * will not be generated if this package_song_artist_album setting is false,
	 * and the single generated file will not contain the corresponding metadata
	 * if this package_song_artist_album setting is true.  Note that this
	 * setting may be overridden by certain command line settings.
	 */
	public boolean package_song_artist_album;

	/**
	 * Whether or not to reextract metadata for the artist associated with the
	 * song being processed even if metadata has already been extracted for this
	 * artist during the processing of a previous song. If this configuration
	 * setting is set to false, then the artists already accessed log file is
	 * checked before extracting artist metadata for the song currently being
	 * processed, and metadata for the artist is only extracted if it has not
	 * already been extracted during the processing of a previous song. If this
	 * configuration setting is set to true, then any existing artist log file
	 * is deleted and metadata for the artist associated with the given song is
	 * extracted, regardless of whether it has already been extracted
	 * previously.
	 */
	public boolean reextract_known_artist_metadata;

	/**
	 * Whether or not to reextract metadata for the album associated with the
	 * song being processed even if metadata has already been extracted for this
	 * album during the processing of a previous song. If this configuration
	 * setting is set to false, then the albums already accessed log file is
	 * checked before extracting album metadata for the song currently being
	 * processed, and metadata for the album is only extracted if it has not
	 * already been extracted during the processing of a previous song. If this
	 * configuration setting is set to true, then any existing album log file is
	 * deleted and metadata for the album associated with the given song is
	 * extracted, regardless of whether it has already been extracted
	 * previously.
	 */
	public boolean reextract_known_album_metadata;

	/**
	 * Whether or not to produce metadata labeled with unqualified Dublin Core
	 * tags by interpreting extracted metadata. Note that this will cause
	 * metadata to be stored redundantly if qualified Dublin Core or other
	 * metadata are saved as well. If the package_song_artist_album
	 * configuration setting is set to true, then unqualified Dublin Core tags
	 * will only be generated for the song metadata, and not for the artist or
	 * album metadata. If the package_song_artist_album configuration setting is
	 * set to false, then unqualified Dublin Core fields will be added to each
	 * of the song, artist and album files, as appropriate. Note that, as is
	 * often the case when dealing with the unqualified Dublin Core standard,
	 * the precise meaning of each unqualified Dublin Core tag will vary based
	 * on the metadata that is available for each given resource.
	 */
	public boolean include_unqualified_dublin_core;

	/**
	 * Whether or not to produce metadata labeled with qualified Dublin Core
	 * tags by interpreting extracted metadata. Note that this will cause
	 * metadata to be stored redundantly if unqualified Dublin Core or other
	 * metadata are saved as well.
	 */
	public boolean include_qualified_dublin_core;

	/**
	 *  Whether or not to include all extracted metadata in the saved metadata
	 * when Dublin Core tags are being saved. If either the
	 * include_unqualified_dublin_core or include_qualified_dublin_core
	 * configuration settings are set to true, then all metadata that is not
	 * stored in a Dublin Core tag is not saved unless this
	 * include_other_metadata_with_dublin_core setting is set to true. If it is
	 * set to true, then all of the available metadata will be saved in its
	 * original form, in addition to in the Dublin Core tagged data, which will
	 * result in some redundancy. This setting has no effect if both the
	 * include_unqualified_dublin_core and include_qualified_dublin_core
	 * settings are set to false.
	 */
	public boolean include_other_metadata_with_dublin_core;

	/**
	 * Whether or not Echo Nest fingerprinting is to be performed in order to
	 * derive the Echo Nest identifier for an unknown song. Note that, based on
	 * the save_echo_nest_metadata configuration setting, other Echo Nest web
	 * services may still be used even if fingerprinting is deactivated here.
	 */
	public boolean enable_echo_nest_fingerprinting;

	/**
	 * Whether or not to extract and save metadata from Echo Nest web services.
	 * Note that this setting does not affect whether or not Echo Nest web
	 * services are used for the purpose of identifying a track, it only affects
	 * whether or not other metadata is extracted from the Echo Nest once a
	 * track has been identified.
	 */
	public boolean save_echo_nest_metadata;

	/**
	 * Whether or not Last.FM fingerprinting is to be performed in order to
	 * derive the Last.FM identifier for an unknown song. Note that, based on
	 * the save_last_fm_metadata configuration setting, other Last.FM web
	 * services may still be used even if fingerprinting is deactivated here.
	 * <b>IMPORTANT:</b> Note that Last.FM fingerprinting is not included in
	 * this software yet, so this setting is only a placeholder for the moment.
	 */
	public boolean enable_last_fm_fingerprinting;

	/**
	 * Whether or not to extract and save metadata from Last.FM web services.
	 * Note that this setting does not affect whether or not Last.FM web
	 * services are used for the purpose of identifying a track, it only affects
	 * whether or not other metadata is extracted from Last.FM once a track has
	 * been identified.
	 */
	public boolean save_last_fm_metadata;

	/**
	 * Whether or not metadata embedded in an audio file is extracted in order
	 * to help identify it. Note that embedded metadata will not be used if
	 * better sources of identifying information are available, such as
	 * fingerprinting results or model metadata specified by the user at the
	 * command line. Note also that this setting does not affect whether or not
	 * embedded metadata in general is saved and/or output to standard out.
	 */
	public boolean enable_embedded_metadata_track_identification;

	/**
	 * Whether or not to extract and save metadata embedded in audio files. Note
	 * that this does not affect whether or not embedded metadata is extracted
	 * for the purpose of identifying a song, it only affects whether metadata
	 * extracted from audio files is saved and/or output to standard out.
	 */
	public boolean save_embedded_metadata;

	/**
	 * Whether or not to keep a record in the extracted metadata of those
	 * metadata fields for which values could not be successfully extracted. If
	 * this setting is set to true, then the output metadata will include a
	 * record for each field that could not be extracted indicating that it
	 * could not be extracted. If this setting is set to false, then fields that
	 * could not be extracted for each song are simply omitted from the the
	 * metadata that is output for that song.
	 */
	public boolean store_fails;

	/**
	 * Whether or not to URL-encode all saved metadata using UTF-8. It is
	 * strongly suggested that this be set to true, as this avoids problems due
	 * to non-ASCII characters returned by the APIs accessed, as well as
	 * potentially invalid XML output. This setting applies to both the text
	 * files and ACE XML files saved by jWebMiner, but does not apply to output
	 * sent to standard out or standard error, which are never URL encoded by
	 * the software.
	 */
	public boolean url_encode_output;

	/**
	 * Whether or not to save extracted metadata in the form of ACE XML 1.1
	 * Classification files. This has no effect on whether text output files are
	 * also generated. This setting may be overridden by certain command line
	 * flags.
	 */
	public boolean save_output_as_ace_xml;

	/**
	 * Whether or not to save extracted metadata as new line delimitted text
	 * files. Lines with odd line numbers will contain metadata field labels,
	 * and lines with even line numbers will contain the metadata values for the
	 * fields on the preceding lines. This setting has no effect on whether ACE
	 * XML output files are also generated. This setting may be overridden by
	 * certain command line flags.
	 */
	public boolean save_output_as_txt;

	/**
	 * Whether or not to print extracted metadata to standard out after
	 * extraction has completed. This setting has no effect on the output files
	 * generated.
	 */
	public boolean print_extracted_metadata_to_terminal;

	/**
	 * Whether or not to print status updates indicating the principal
	 * processing operations of jSongMiner as they are performed. These updates
	 * are printed to standard out.
	 */
	public boolean print_current_status_to_terminal;

	/**
	 * Whether or not to print status updates indicating non-terminal errors or
	 * failures that occur during jSongMiner processing. Such updates are
	 * printed to standard error. Note that terminal errors are printed to
	 * standard error regardless of this setting.
	 */
	public boolean print_errors_to_terminal;

	/**
	 * The directory to save files holding extracted song metadata to, if this
	 * is not explicitly specified at runtime. This applies to output files in
	 * both ACE XML and text formats. If the package_song_artist_album setting
	 * is set to true, then all of the metadata for each song will be saved in a
	 * single ACE XML and/or text file in this directory, including artist
	 * and/or album metadata, if available. This setting may be overridden by
	 * certain command line settings.
	 */
	public String songs_save_directory;

	/**
	 * The directory to save files holding extracted artist metadata to, if this
	 * information is not explicitly specified at runtime. This applies to
	 * output files in both ACE XML and text formats. This directory is not used
	 * if the package_song_artist_album setting is set to true. This setting may
	 * be overridden by certain command line settings.
	 */
	public String artists_save_directory;

	/**
	 * The directory to save files holding extracted album metadata to, if this
	 * information is not explicitly specified at runtime. This applies to
	 * output files in both ACE XML and text formats. This directory is not used
	 * if the package_song_artist_album setting is set to true. This setting may
	 * be overridden by certain command line settings.
	 */
	public String albums_save_directory;

	/**
	 * The key needed by jSongMiner in order to access the Echo Nest web
	 * services. An application for an API key may be made to the Echo Nest at
	 * http://developer.echonest.com/account/register. This setting must be set
	 * to a valid value if any of the Echo Nest web services are to be used.
	 */
	public String echo_nest_api_key;

	/**
	 * The local path of the Echo Nest fingerprinting codegen binary (or shell
	 * that runs it). This must be set to a valid path if local Echo Nest
	 * fingerprinting is to be used.
	 */
	public String echo_nest_fingerprinting_codegen_run_path;

	/**
	 * The directory holding the Echo Nest fingerprinting codegen binary. This
	 * may be needed if the codegen is being run via a script, but otherwise it
	 * is ignored.
	 */
	public String echo_nest_codegen_directory;

	/**
	 * The key needed by jSongMiner in order to access the Last.FM web services.
	 * An application for a key may be made to Last.FM at
	 * http://www.last.fm/api/account. In theory this must be set to a valid
	 * value if any of the Last.FM web services are to be used, although in
	 * practice service is still sometimes provided even if a valid Last.FM API
	 * key is lacking.
	 */
	public String last_fm_api_key;

	/**
	 * The save path of the log file that is used to keep track of artists for
	 * whom metadata has already been extracted. This can be helpful in avoiding
	 * the wasteful process of repetetively reextracting the same artist
	 * metadata for different songs by the same artist. This setting may be
	 * overridden by certain command line settings.
	 */
	public String artists_already_accessed_file_path;

	/**
	 * The save path of the log file that is used to keep track of albums for
	 * which metadata has already been extracted. This is helpful in avoiding
	 * the wasteful process of repetetively reextracting the same album metadata
	 * for different songs on the same album. This setting may be overridden by
	 * certain command line settings.
	 */
	public String albums_already_accessed_file_path;


	/* PRIVATE FIELDS *********************************************************/


	/**
	 * Parsing fields noting whether parsing is currently inside the given
	 * XML element. The keys indicate element names and the values indicate
	 * whether the system is currently in the given state.
	 */
	private HashMap<String,Boolean> parsing_state;

	/**
	 * The element text parsed from the current XML element so far.
	 */
	private StringBuffer element_text_so_far;

	/**
	 * Indicates whether parsing of an XML file has begun.
	 */
	private boolean parsing_begun;


	/* CONSTRUCTOR METHODS ****************************************************/


	/**
	 * Set up the public fields of this object to have the values contained in
	 * the specified jSongMiner configuration XML file. If the config_file_path
	 * parameter is null, or if the file cannot be parsed, then default field
	 * values are used. If config_file_path is specified, and it cannot be
	 * successfully parsed, then these default values are also saved to
	 * config_file_path, overwriting anything already there.
	 *
	 * @param config_file_path	The path of the configurations file to parse
	 *							settings from. May be null, in which case
	 *							default settings are used.
	 * @param warning_stream	The stream to write warning messages to if
	 *							problems occur. There will be no feedback
	 *							provided if this is null.
	 */
	public ConfigDataAndParser(String config_file_path,
			PrintStream warning_stream)
	{
		// Set public fields to their default values
		setPublicFieldsToDefaults();

		// Load configuration settings into public fields from the
		// configuration file
		if (config_file_path != null)
		{
			try { parsejSongMinerConfigurationFile(config_file_path); }
			catch (Exception e)
			{
				if (warning_stream != null)
				{
					warning_stream.println("WARNING: Could not parse the jSongMiner configuration file that should be at " + config_file_path);
					warning_stream.println("DETAILS: " + e.getMessage());
					warning_stream.println("This is normal the first time that this program is run.\n");
					warning_stream.println("Reverting to default configuration settings...\n");
					warning_stream.println("Saving default configuration settings to " + config_file_path + "...\n");
				}

				// If loading did not work, revert to default settings and
				// save them
				try
				{
					setPublicFieldsToDefaults();
					saveConfigurationFile(config_file_path);
					if (warning_stream != null)
					{
						warning_stream.println("Saving of configuration settings complete.");
						warning_stream.println("It is strongly recommended that you change these settings now that they are saved, either by editing the file manually or by using command line options.\n");
					}
				}
				catch (Exception f)
				{
					if (warning_stream != null)
					{
						warning_stream.println("WARNING: Could not save the jSongMinerConfiguration file to " + config_file_path);
						warning_stream.println("DETAILS: " + f.getMessage());
						warning_stream.println("Please be sure that this directory exists with read/write permissions, otherwise it will not be possible to save a configuration file.");
						warning_stream.println("Proceeding with default configuration settings...\n");
					}
				}
			}
		}

		// Check for particularly problematic field values
		if (warning_stream != null)
			testPublicFieldValues(warning_stream);
	}


	/* PUBLIC METHODS *********************************************************/


	/**
	 * Parse the contents of a jSongMiner configuration XML file and store its
	 * contents in the public fields of this object.
	 *
	 * @param path_of_file_to_parse		The path of an file to parse.
	 * @throws Exception				An informative exception is thrown if
	 *									an invalid file path or an invalid file
	 *									is given to this method.
	 */
	public void parsejSongMinerConfigurationFile(String path_of_file_to_parse)
			throws Exception
	{
		// Initialize parsing fields
		refreshParsingFields();

		// Verify that the file can be read from
		FileMethods.validateFile(new File(path_of_file_to_parse), true, false);

		// Prepare the XML parser
		DefaultHandler handler = this;
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		parser.setFeature("http://xml.org/sax/features/validation", true);
		parser.setErrorHandler(new ParsingXMLErrorHandler());
		parser.setContentHandler(handler);

		// Parse the file_to_parse
		try { parser.parse(path_of_file_to_parse); }
		catch (SAXParseException e) // throw an exception if the file is not a valid XML file
		{
			throw new Exception("The " + path_of_file_to_parse + " file is not a valid XML file.\n\n" +
					"Details of the problem: " + e.getMessage() +
					"This error is likely in the region of line " + e.getLineNumber() + ".");
		}
		catch (SAXException e) // throw an exception if the file is not an XML file of the correct type
		{
			throw new Exception("The " + path_of_file_to_parse + " file must be a valid jSongMiner configuration file.\n" +
					"Details of the problem: " + e.getMessage());
		}
		catch (Exception e) // throw an exception if the file is not formatted properly
		{
			throw new Exception("The " + path_of_file_to_parse + " file is not formatted properly.\n" +
					"Details of the problem: " + e.getMessage());
		}
	}


	/**
	 * Saves a configuration file to the specified path. The values stored in
	 * the configuration file are the same as those currently stored in this
	 * object.
	 *
	 * @param save_path		The path to save the configuration file to.
	 * @throws Exception	An exception is thrown if the save could not be
	 *						performed.
	 */
	public void saveConfigurationFile(String save_path)
			throws Exception
	{
		try
		{
			// Prepare the file writer
			FileWriter writer = new FileWriter(new File (save_path));

			// Write the DTD
			writer.write("<?xml version=\"1.0\"?>\n");
			writer.write("<!DOCTYPE jsongminer_config_file [\n");
			writer.write("\t<!ELEMENT jsongminer_config_file (identify_only, package_song_artist_album, reextract_known_artist_metadata, reextract_known_album_metadata, include_unqualified_dublin_core, include_qualified_dublin_core, include_other_metadata_with_dublin_core, enable_echo_nest_fingerprinting, save_echo_nest_metadata, enable_last_fm_fingerprinting, save_last_fm_metadata, enable_embedded_metadata_track_identification, save_embedded_metadata, store_fails, url_encode_output, save_output_as_ace_xml, save_output_as_txt, print_extracted_metadata_to_terminal, print_current_status_to_terminal, print_errors_to_terminal, songs_save_directory, artists_save_directory, albums_save_directory, echo_nest_api_key, echo_nest_fingerprinting_codegen_run_path, echo_nest_codegen_directory, last_fm_api_key, artists_already_accessed_file_path, albums_already_accessed_file_path)>\n");
			writer.write("\t<!ELEMENT identify_only (#PCDATA)>\n");
			writer.write("\t<!ELEMENT package_song_artist_album (#PCDATA)>\n");
			writer.write("\t<!ELEMENT reextract_known_artist_metadata (#PCDATA)>\n");
			writer.write("\t<!ELEMENT reextract_known_album_metadata (#PCDATA)>\n");
			writer.write("\t<!ELEMENT include_unqualified_dublin_core (#PCDATA)>\n");
			writer.write("\t<!ELEMENT include_qualified_dublin_core (#PCDATA)>\n");
			writer.write("\t<!ELEMENT include_other_metadata_with_dublin_core (#PCDATA)>\n");
			writer.write("\t<!ELEMENT enable_echo_nest_fingerprinting (#PCDATA)>\n");
			writer.write("\t<!ELEMENT enable_last_fm_fingerprinting (#PCDATA)>\n");
			writer.write("\t<!ELEMENT save_echo_nest_metadata (#PCDATA)>\n");
			writer.write("\t<!ELEMENT enable_embedded_metadata_track_identification (#PCDATA)>\n");
			writer.write("\t<!ELEMENT save_last_fm_metadata (#PCDATA)>\n");
			writer.write("\t<!ELEMENT save_embedded_metadata (#PCDATA)>\n");
			writer.write("\t<!ELEMENT store_fails (#PCDATA)>\n");
			writer.write("\t<!ELEMENT url_encode_output (#PCDATA)>\n");
			writer.write("\t<!ELEMENT save_output_as_ace_xml (#PCDATA)>\n");
			writer.write("\t<!ELEMENT save_output_as_txt (#PCDATA)>\n");
			writer.write("\t<!ELEMENT print_extracted_metadata_to_terminal (#PCDATA)>\n");
			writer.write("\t<!ELEMENT print_current_status_to_terminal (#PCDATA)>\n");
			writer.write("\t<!ELEMENT print_errors_to_terminal (#PCDATA)>\n");
			writer.write("\t<!ELEMENT songs_save_directory (#PCDATA)>\n");
			writer.write("\t<!ELEMENT artists_save_directory (#PCDATA)>\n");
			writer.write("\t<!ELEMENT albums_save_directory (#PCDATA)>\n");
			writer.write("\t<!ELEMENT echo_nest_api_key (#PCDATA)>\n");
			writer.write("\t<!ELEMENT echo_nest_fingerprinting_codegen_run_path (#PCDATA)>\n");
			writer.write("\t<!ELEMENT echo_nest_codegen_directory (#PCDATA)>\n");
			writer.write("\t<!ELEMENT last_fm_api_key (#PCDATA)>\n");
			writer.write("\t<!ELEMENT artists_already_accessed_file_path (#PCDATA)>\n");
			writer.write("\t<!ELEMENT albums_already_accessed_file_path (#PCDATA)>\n");
			writer.write("]>\n");
			writer.write("\n");

			// Write the content
			writer.write("<jsongminer_config_file>\n");
			writer.write("\t<identify_only>" + identify_only + "</identify_only>\n");
			writer.write("\t<package_song_artist_album>" + package_song_artist_album + "</package_song_artist_album>\n");
			writer.write("\t<reextract_known_artist_metadata>" + reextract_known_artist_metadata + "</reextract_known_artist_metadata>\n");
			writer.write("\t<reextract_known_album_metadata>" + reextract_known_album_metadata + "</reextract_known_album_metadata>\n");
			writer.write("\t<include_unqualified_dublin_core>" + include_unqualified_dublin_core + "</include_unqualified_dublin_core>\n");
			writer.write("\t<include_qualified_dublin_core>" + include_qualified_dublin_core + "</include_qualified_dublin_core>\n");
			writer.write("\t<include_other_metadata_with_dublin_core>" + include_other_metadata_with_dublin_core + "</include_other_metadata_with_dublin_core>\n");
			writer.write("\t<enable_echo_nest_fingerprinting>" + enable_echo_nest_fingerprinting + "</enable_echo_nest_fingerprinting>\n");
			writer.write("\t<save_echo_nest_metadata>" + save_echo_nest_metadata + "</save_echo_nest_metadata>\n");
			writer.write("\t<enable_last_fm_fingerprinting>" + enable_last_fm_fingerprinting + "</enable_last_fm_fingerprinting>\n");
			writer.write("\t<save_last_fm_metadata>" + save_last_fm_metadata + "</save_last_fm_metadata>\n");
			writer.write("\t<enable_embedded_metadata_track_identification>" + enable_embedded_metadata_track_identification + "</enable_embedded_metadata_track_identification>\n");
			writer.write("\t<save_embedded_metadata>" + save_embedded_metadata + "</save_embedded_metadata>\n");
			writer.write("\t<store_fails>" + store_fails + "</store_fails>\n");
			writer.write("\t<url_encode_output>" + url_encode_output + "</url_encode_output>\n");
			writer.write("\t<save_output_as_ace_xml>" + save_output_as_ace_xml + "</save_output_as_ace_xml>\n");
			writer.write("\t<save_output_as_txt>" + save_output_as_txt + "</save_output_as_txt>\n");
			writer.write("\t<print_extracted_metadata_to_terminal>" + print_extracted_metadata_to_terminal + "</print_extracted_metadata_to_terminal>\n");
			writer.write("\t<print_current_status_to_terminal>" + print_current_status_to_terminal + "</print_current_status_to_terminal>\n");
			writer.write("\t<print_errors_to_terminal>" + print_errors_to_terminal + "</print_errors_to_terminal>\n");
			writer.write("\t<songs_save_directory>" + songs_save_directory + "</songs_save_directory>\n");
			writer.write("\t<artists_save_directory>" + artists_save_directory + "</artists_save_directory>\n");
			writer.write("\t<albums_save_directory>" + albums_save_directory + "</albums_save_directory>\n");
			writer.write("\t<echo_nest_api_key>" + echo_nest_api_key + "</echo_nest_api_key>\n");
			writer.write("\t<echo_nest_fingerprinting_codegen_run_path>" + echo_nest_fingerprinting_codegen_run_path + "</echo_nest_fingerprinting_codegen_run_path>\n");
			writer.write("\t<echo_nest_codegen_directory>" + echo_nest_codegen_directory + "</echo_nest_codegen_directory>\n");
			writer.write("\t<last_fm_api_key>" + last_fm_api_key + "</last_fm_api_key>\n");
			writer.write("\t<artists_already_accessed_file_path>" + artists_already_accessed_file_path + "</artists_already_accessed_file_path>\n");
			writer.write("\t<albums_already_accessed_file_path>" + albums_already_accessed_file_path + "</albums_already_accessed_file_path>\n");
			writer.write("</jsongminer_config_file>");

			// Close the writer
			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			throw new Exception("Given file " + save_path + " may not be written to. Details: " + e.getMessage());
		}
	}


	/**
	 * The values of the public fields of this object are examined for
	 * particularly problematic settings. If a problematic setting is
	 * encountered, then non-terminal warnings are printed to the provided
	 * stream.
	 *
	 * @param to_write	The stream to print warnings to. May not be null.
	 */
	public void testPublicFieldValues(PrintStream to_write)
	{
		if (echo_nest_api_key == null)
			to_write.println("WARNING: No Echo Nest API key is specified in the configuration file. It is strongly suggested that one be acquired from http://developer.echonest.com/account/register and entered in the configuration file for this software, as the Echo Nest web services may not return results otherwise.\n");

		if (last_fm_api_key == null)
			to_write.println("WARNING: No Last.FM API key is specified in the configuration file. It is strongly suggested that one be acquired from http://www.last.fm/api/account and entered in the configuration file for this software, as the Last.FM web services may not return results otherwise.\n");

		if (!print_extracted_metadata_to_terminal && !save_output_as_ace_xml && !save_output_as_txt)
			to_write.println("WARNING: Under the current configuration settings, none of the options (ACE XML, text or printing to standard out) are selected for outputting extracted metadata. It is strongly suggested that one or more of them be activated on the configurations file for this software so that extracted metadata may be examined, unless overrides are specified at the command line.\n");

		if (!url_encode_output)
			to_write.println("WARNING: Under the current configuration settings, output will be generated whose content is not URL encoded. This could result in the generation of invalid XML or text files that do not parse properly, so it is strongly suggested that URL encoding be turned on in the configurations file, unless all that is wanted is output results to standard out.\n");

		if (!save_echo_nest_metadata && !save_last_fm_metadata && !save_embedded_metadata)
			to_write.println("WARNING: Under the current configuration settings, no extracted metadata will be saved. It is therefore strongly suggested that at least one of the desired metadata sources be activated in the configuration settings file.\n");

		if (!enable_echo_nest_fingerprinting && !enable_last_fm_fingerprinting && !enable_embedded_metadata_track_identification)
			to_write.println("WARNING: Under the current configuration settings, all of the song identification methods are disabled, other than using tags manually specified at the command line. It is suggested that other means of song identification be activated in the configuration settings file.\n");
	}


	/* PRIVATE METHODS ********************************************************/


	/**
	 * Set the public fields of this object to default values.
	 */
	private void setPublicFieldsToDefaults()
	{
		identify_only = false;
		package_song_artist_album = true;
		reextract_known_artist_metadata = true;
		reextract_known_album_metadata = true;
		include_unqualified_dublin_core = false;
		include_qualified_dublin_core = false;
		include_other_metadata_with_dublin_core = true;
		enable_echo_nest_fingerprinting = true;
		save_echo_nest_metadata = true;
		enable_last_fm_fingerprinting = true;
		save_last_fm_metadata = true;
		enable_embedded_metadata_track_identification = true;
		save_embedded_metadata = true;
		store_fails = false;
		url_encode_output = true;
		save_output_as_ace_xml = true;
		save_output_as_txt = false;
		print_extracted_metadata_to_terminal = true;
		print_current_status_to_terminal = true;
		print_errors_to_terminal = true;
		songs_save_directory = System.getProperty("user.home");
		artists_save_directory = System.getProperty("user.home");
		albums_save_directory = System.getProperty("user.home");
		echo_nest_api_key = null;
		echo_nest_fingerprinting_codegen_run_path = null;
		echo_nest_codegen_directory = null;
		last_fm_api_key = null;
		artists_already_accessed_file_path = "./artists_so_far.ser";
		albums_already_accessed_file_path = "./albums_so_far.ser";
	}


	/**
	 * Resets the pasrsing_state and parsing_begun fields to indicate that
	 * processing is not currently in any XML elelements and that parsing has
	 * not yet begun.
	 */
	private void refreshParsingFields()
	{
		parsing_begun = false;

		parsing_state = new HashMap<String,Boolean>(16);
		parsing_state.put("jsongminer_config_file", new Boolean(false));
		parsing_state.put("identify_only", new Boolean(false));
		parsing_state.put("package_song_artist_album", new Boolean(false));
		parsing_state.put("reextract_known_artist_metadata", new Boolean(false));
		parsing_state.put("reextract_known_album_metadata", new Boolean(false));
		parsing_state.put("include_unqualified_dublin_core", new Boolean(false));
		parsing_state.put("include_qualified_dublin_core", new Boolean(false));
		parsing_state.put("include_other_metadata_with_dublin_core", new Boolean(false));
		parsing_state.put("enable_echo_nest_fingerprinting", new Boolean(false));
		parsing_state.put("save_echo_nest_metadata", new Boolean(false));
		parsing_state.put("enable_last_fm_fingerprinting", new Boolean(false));
		parsing_state.put("save_last_fm_metadata", new Boolean(false));
		parsing_state.put("enable_embedded_metadata_track_identification", new Boolean(false));
		parsing_state.put("save_embedded_metadata", new Boolean(false));
		parsing_state.put("store_fails", new Boolean(false));
		parsing_state.put("url_encode_output", new Boolean(false));
		parsing_state.put("save_output_as_ace_xml", new Boolean(false));
		parsing_state.put("save_output_as_txt", new Boolean(false));
		parsing_state.put("print_extracted_metadata_to_terminal", new Boolean(false));
		parsing_state.put("print_current_status_to_terminal", new Boolean(false));
		parsing_state.put("print_errors_to_terminal", new Boolean(false));
		parsing_state.put("songs_save_directory", new Boolean(false));
		parsing_state.put("artists_save_directory", new Boolean(false));
		parsing_state.put("albums_save_directory", new Boolean(false));
		parsing_state.put("echo_nest_api_key", new Boolean(false));
		parsing_state.put("echo_nest_fingerprinting_codegen_run_path", new Boolean(false));
		parsing_state.put("echo_nest_codegen_directory", new Boolean(false));
		parsing_state.put("last_fm_api_key", new Boolean(false));
		parsing_state.put("artists_already_accessed_file_path", new Boolean(false));
		parsing_state.put("albums_already_accessed_file_path", new Boolean(false));
	}


	/* XML PARSING METHODS ****************************************************/


	/**
	 * This method is called when the start of an XML element is encountered
	 * during parsing.
	 *
	 * @param	namespace		The Namespace URI, or the empty string if the
	 *							element has no Namespace URI or if Namespace
	 *							processing is not being performed.
	 * @param	element_name	The name of the element that was encountered.
	 * @param	qual_name		The qualified name (with prefix), or the empty
	 *							string if qualified names are not available.
	 * @param	attributes		The attributes attached to the element. If there
	 *							are no attributes, this shall be an empty
	 *							Attributes object.
     * @throws	SAXException	An informative exception is thrown if an error
	 *							occurs during parsing.
	 */
	@Override
	public void startElement( String namespace,
			String element_name,
			String qual_name,
			Attributes attributes )
			throws SAXException
	{
		// Verify that this is the correct XML file type and note that parsing
		// has begun
		if (!parsing_begun)
		{
			if (!element_name.equals("jsongminer_config_file"))
				throw new SAXException("This is a " + element_name + " type XML file. It should be a jsongminer_config_file type file.");
			parsing_begun = true;
		}

		// Reset the string buffer
		element_text_so_far = new StringBuffer();

		// Note what kind of element this is
		parsing_state.put(element_name, true);
	}


	/**
	 * This method is called when the character data inside an element is
	 * parsed.
	 *
	 * @param	characters		The characters that are parsed.
	 * @param	start_position	The start position in the character array.
	 * @param	length			The number of characters to use from the
	 *							character array.
     * @throws	SAXException	An informative exception is thrown if an error
	 *							occurs during parsing.
	 */
	@Override
	public void characters (char[] characters, int start_position, int length)
			throws SAXException
	{
		// The text stored in the element
		String text = new String(characters, start_position, length);

		// Store the text
		element_text_so_far.append(text);
	}


	/**
	 * This method is called when the ending of an XML element is encountered
	 * during parsing.
	 *
	 * @param	namespace		The Namespace URI, or the empty string if the
	 *							element has no Namespace URI or if Namespace
	 *							processing is not being performed.
	 * @param	element_name	The name of the element that was encountered.
	 * @param	qual_name		The qualified name (with prefix), or the empty
	 *							string if qualified names are not available.
     * @throws	SAXException	An informative exception is thrown if an error
	 *							occurs during parsing.
	 */
	@Override
	public void endElement( String namespace,
			String element_name,
			String qual_name )
			throws SAXException
	{
		// Finalize the element text
		String text = element_text_so_far.toString();

		// Store the element text in the appropriate field
		if (parsing_state.get("identify_only"))
			identify_only = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("package_song_artist_album"))
			package_song_artist_album = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("reextract_known_artist_metadata"))
			reextract_known_artist_metadata = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("reextract_known_album_metadata"))
			reextract_known_album_metadata = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("include_unqualified_dublin_core"))
			include_unqualified_dublin_core = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("include_qualified_dublin_core"))
			include_qualified_dublin_core = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("include_other_metadata_with_dublin_core"))
			include_other_metadata_with_dublin_core = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("enable_echo_nest_fingerprinting"))
			enable_echo_nest_fingerprinting = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("save_echo_nest_metadata"))
			save_echo_nest_metadata = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("enable_last_fm_fingerprinting"))
			enable_last_fm_fingerprinting = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("save_last_fm_metadata"))
			save_last_fm_metadata = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("enable_embedded_metadata_track_identification"))
			enable_embedded_metadata_track_identification = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("save_embedded_metadata"))
			save_embedded_metadata = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("store_fails"))
			store_fails = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("url_encode_output"))
			url_encode_output = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("save_output_as_ace_xml"))
			save_output_as_ace_xml = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("save_output_as_txt"))
			save_output_as_txt = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("print_extracted_metadata_to_terminal"))
			print_extracted_metadata_to_terminal = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("print_current_status_to_terminal"))
			print_current_status_to_terminal = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("print_errors_to_terminal"))
			print_errors_to_terminal = (new Boolean(text)).booleanValue();
		else if (parsing_state.get("songs_save_directory"))
			songs_save_directory = text;
		else if (parsing_state.get("artists_save_directory"))
			artists_save_directory = text;
		else if (parsing_state.get("albums_save_directory"))
			albums_save_directory = text;
		else if (parsing_state.get("echo_nest_api_key"))
			echo_nest_api_key = text;
		else if (parsing_state.get("echo_nest_fingerprinting_codegen_run_path"))
			echo_nest_fingerprinting_codegen_run_path = text;
		else if (parsing_state.get("echo_nest_codegen_directory"))
			echo_nest_codegen_directory = text;
		else if (parsing_state.get("last_fm_api_key"))
			last_fm_api_key = text;
		else if (parsing_state.get("artists_already_accessed_file_path"))
			artists_already_accessed_file_path = text;
		else if (parsing_state.get("albums_already_accessed_file_path"))
			albums_already_accessed_file_path = text;

		// Note what kind of element this is
		parsing_state.put(element_name, false);
	}
}