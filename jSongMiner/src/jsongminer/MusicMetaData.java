/*
 * MusicMetaData.java
 * Version 1.0
 *
 * Last modified on August 3, 2010.
 * University of Waikato and McGill University
 */

package jsongminer;

import ace.datatypes.SegmentedClassification;
import mckay.utilities.staticlibraries.StringMethods;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Vector;

/**
 * Each object of this class holds a piece of metadata, including the field
 * identifer for the metadata, the value itself of the metadata and the source
 * from which the metadata value was derived.
 *
 * <p>Static methods are also included for processing collections of
 * MusicMetaData objects in various ways.
 *
 * @author Cory McKay
 */
public class MusicMetaData
		implements java.io.Serializable
{
	/* FIELDS *****************************************************************/


	/**
	 * The source of a piece of metadata.
	 */
	private String source;
	
	/**
	 * The field identifier for the piece of metadata.
	 */
	private String field_identifier;
	
	/**
	 * The value for the field of the piece of metadata.
	 */
	private String value;
	
	/**
	 * The serialization code.
	 */
	private static final long serialVersionUID = 5454673585687677L;


	/* CONSTRUCTOR ************************************************************/


	/**
	 * Store a piece of metadata consisting of the specified information.
	 * 
	 * @param source			The source of a piece of metadata.
	 * @param field_identifier	The field identifier for the piece of metadata.
	 * @param value				The value for this field of the piece of
	 *							metadata.
	 */
	public MusicMetaData(String source, String field_identifier, String value)
	{
		this.source = source;
		this.field_identifier = field_identifier;
		this.value = value;
	}
	
	
	/* PUBLIC METHODS *********************************************************/


	/**
	 * Returns the source of this piece of metadata.
	 * 
	 * @return	The source of a piece of metadata.
	 */
	public String getSource()
	{
		return source;
	}


	/**
	 * Returns the field identifier of this piece of metadata.
	 *
	 * @return	The field identifier that the piece of metadata corresponds to.
	 */
	public String getFieldIdentifier()
	{
		return field_identifier;
	}


	/**
	 * Returns the value of this piece of metadata.
	 *
	 * @return	The value for this field of the piece of metadata.
	 */
	public String getValue()
	{
		return value;
	}


	/**
	 * A debugging method that prints the contents of this object to standard
	 * out.
	 */
	public void printValues()
	{
		System.out.println("SOURCE: " + source);
		System.out.println("FIELD IDENTIFIER: " + field_identifier);
		System.out.println("VALUE: " + value);
		System.out.println();
	}


	/* PUBLIC STATIC METHODS **************************************************/


	/**
	 * Searches through the provided vector for the first MusicMetaData object
	 * with the given field identifier that has a non-null and non-empty string
	 * value.
	 *
	 * @param field_id_to_search_for	The field identifier to search for.
	 * @param to_search					The MusicMetaData objects to search
	 *									through.
	 * @return							The value of the first MusicMetaData
	 *									object found meeting the required
	 *									conditions. Null if none can be found.
	 */
	public static String findFirstValidValueForFieldID( String field_id_to_search_for,
			Vector<MusicMetaData> to_search)
	{
		for (int i = 0; i < to_search.size(); i++)
		{
			String field_identifier = to_search.get(i).getFieldIdentifier();
			if (field_identifier.equals(field_id_to_search_for))
			{
				String value = to_search.get(i).getValue();
				if (value != null)
					if (!value.equals(""))
						return value;
			}
		}
		return null;
	}


	/**
	 * Remove all entries in the given data that have values of null or empty
	 * string.
	 *
	 * @param to_clean	The MusicMetaData to clean.
	 */
	public static void cleanMusicMetaData(Vector<MusicMetaData> to_clean)
	{
		// Prepare a vector listing all of the entries to erase
		Vector<MusicMetaData> to_erase = new Vector<MusicMetaData>();
		for (int i = 0; i < to_clean.size(); i++)
		{
			MusicMetaData this_one = to_clean.get(i);
			if (this_one.getValue() == null)
				to_erase.add(this_one);
			else if (this_one.getValue().equals(""))
				to_erase.add(this_one);
		}

		// Erase the entries
		to_clean.removeAll(to_erase);
	}


	/**
	 * Remove all duplicate entries in the given data. A duplicate entry is one
	 * that has the same values for all three public fields (source,
	 * field_identifier and value).
	 *
	 * @param to_clean	The MusicMetaData to clean.
	 */
	public static void removeDuplicateEntries(Vector<MusicMetaData> to_clean)
	{
		// Prepare a vector listing all of the entries to erase
		Vector<MusicMetaData> to_erase = new Vector<MusicMetaData>();
		for (int i = 0; i < to_clean.size(); i++)
		{
			MusicMetaData first_one = to_clean.get(i);

			for (int j = i + 1; j < to_clean.size(); j++)
			{
				MusicMetaData second_one = to_clean.get(j);

				if ( (first_one.field_identifier).equals(second_one.field_identifier) &&
					 (first_one.value).equals(second_one.value) &&
					 (first_one.source).equals(second_one.source) )
				{
					to_erase.add(second_one);
				}
			}
		}

		// Erase the entries
		to_clean.removeAll(to_erase);
	}


	/**
	 * Return the given vector of metadata as a single ACE
	 * SegmentedClassification object with the metadata placed in a
	 * corresponding way in the misc_info_key and misc_info fields. The
	 * specified identifier is used as the instance identifier in this new
	 * SegmentedClassification object.
	 *
	 * @param identifier	An identifier identifying what the given to_convert
	 *						metadata refers to as a whole.
	 * @param to_convert	The converted data. Null is returned if identifier
	 *						is null or an empty string.
	 * @param url_encode	Whether or not to URL encode (UTF-8) the data
	 *						(in both misc_info_key and misc_info).
	 * @return				An ACE SegmentedClassification object containing
	 *						the data in to_convert.
	 * @throws Exception	An exception is thrown if the data could not be
	 *						properly URL encoded.
	 */
	public static SegmentedClassification convertToJMIRData( String identifier,
			Vector<MusicMetaData> to_convert,
			boolean url_encode )
			throws Exception
	{
		if (identifier == null)
			return null;
		else if (identifier.equals(""))
			return null;

		String[] misc_info_key;
		String[] misc_info;
		if (to_convert.isEmpty())
		{
			misc_info_key = null;
			misc_info = null;
		}
		else
		{
			misc_info_key = new String[to_convert.size()];
			misc_info = new String[to_convert.size()];
			for (int i = 0; i < to_convert.size(); i++)
			{
				misc_info_key[i] = generateSingleKey(to_convert.get(i));
				misc_info[i] = to_convert.get(i).getValue();

				if (url_encode)
				{
					misc_info_key[i] = StringMethods.URLEncodeWithNulls(misc_info_key[i]);
					misc_info[i] = StringMethods.URLEncodeWithNulls(misc_info[i]);
				}
			}
		}

		return new SegmentedClassification( identifier,
				Double.NaN,
				Double.NaN,
				null,
				misc_info,
				misc_info_key,
				null );
	}


	/**
	 * Save the provided MusicMetaData vector as an ACE XML 1.1 Classifications
	 * File containing a single instance with all of the MusicMetaData objects
	 * saved as misc_info. The instance identifier is found based on the the
	 * provided identifier_keys. The data_to_save parameter is searched for a
	 * MusicMetaData object with one of the identifier_keys (from start to
	 * finish of the array) as its field identifier. The value of the first
	 * matching MusicMetaData object is used as the identifier. This value is
	 * also used as the file name for the saved ACE XML file (with the .xml
	 * extension appended). If a file with the selected name already exists,
	 * then an underscore followed by a numerical value is appended to the end
	 * of the file name such that the name is unique.
	 *
	 * @param identifier_keys	The candidate field identifiers to look for
	 *							in the data_to_save in order to acquire the
	 *							identifier string and filename. These are ranked
	 *							from best to worse.
	 * @param data_to_save		The data to save as an ACE XML file.
	 * @param url_encode		Whether or not to URL encode (UTF-8) the saved
	 *							data (in both misc_info_key and misc_info). If
	 *							this is true, the file name will also be URL
	 *							encoded.
	 * @param save_directory	The directory to save the ACE XML file in.
	 * @param save_file_path	The path of the file to save to. Ignored
	 *							if null. If it is non-null, then it	overrides
	 *							all other settings.
	 * @throws Exception		An informative exception is thrown if the input
	 *							parameters are invalid or if the file could not
	 *							be saved.
	 */
	public static void saveInACEXMLClassificationsFile( String[] identifier_keys,
			Vector<MusicMetaData> data_to_save,
			boolean url_encode,
			String save_directory,
			String save_file_path )
			throws Exception
	{
		// Get an abstract file with an appropriate file name
		File save_file;
		if (save_file_path == null)
			save_file = getSaveFile( identifier_keys,
				data_to_save,
				true,
				url_encode,
				save_directory );
		else save_file = new File(save_file_path);

		// Search through the provided identifier keys
		String identifier = null;
		for (int i = 0; i < identifier_keys.length; i++)
		{
			identifier = findFirstValidValueForFieldID( identifier_keys[i], data_to_save);
			if (identifier != null) i = identifier_keys.length; // exit loop
		}
		if (identifier == null)
			throw new Exception("No valid identifier could be found.");

		// Parse the data into an ACE object
		SegmentedClassification[] to_save = {convertToJMIRData(identifier, data_to_save, url_encode)};

		// Save the file
		SegmentedClassification.saveClassifications( to_save,
			save_file,
			"Generated by jMIR jSongMiner" );
	}


	/**
	 * Save the provided MusicMetaData vector as a text file where each odd line
	 * is a field identifier and each even line is the value for the field
	 * named on the preceding line.
	 *
	 * <p>The file name is found based on the the provided identifier_keys. The
	 * data_to_save parameter is searched for a MusicMetaData object with one of
	 * the identifier_keys (from start to finish of the array) as its field
	 * identifier. The value of the first matching MusicMetaData object is used
	 * as the file name (with the .txt extension appended). If a file with the
	 * selected name already exists, then an underscore followed by a numerical
	 * value is appended to the end of the file name such that the name is
	 * unique.
	 *
	 * @param identifier_keys	The candidate field identifiers to look for
	 *							in the data_to_save in order to acquire the
	 *							filename. These are ranked from best to worse.
	 * @param data_to_save		The data to save as a txt file.
	 * @param url_encode		Whether or not to URL encode (UTF-8) the saved
	 *							data. If this is true, the file name will also
	 *							be URL encoded.
	 * @param save_directory	The directory to save the file in.
	 * @param save_file_path	The path of the file to save to. Ignored
	 *							if null. If it is non-null, then it	overrides
	 *							all other settings.
	 * @throws Exception		An informative exception is thrown if the input
	 *							parameters are invalid or if the file could not
	 *							be saved.
	 */
	public static void saveInTextFile( String[] identifier_keys,
			Vector<MusicMetaData> data_to_save,
			boolean url_encode,
			String save_directory,
			String save_file_path )
			throws Exception
	{
		// Get an abstract file with an appropriate file name
		File save_file;
		if (save_file_path == null)
			save_file = getSaveFile( identifier_keys,
				data_to_save,
				false,
				url_encode,
				save_directory );
		else save_file = new File(save_file_path);

		// Prepare stream writer
		FileOutputStream to = new FileOutputStream(save_file);
		DataOutputStream writer = new DataOutputStream(to);

		// Go through and save the entries one by one
		for (int i = 0; i < data_to_save.size(); i++)
		{

			// Extract the key and data
			String key = generateSingleKey(data_to_save.get(i));
			String data = data_to_save.get(i).getValue();

			// URL encode
			if (url_encode)
			{
				key = StringMethods.URLEncodeWithNulls(key);
				data = StringMethods.URLEncodeWithNulls(data);
			}

			// Save the entry
			 writer.writeBytes(key);
			 writer.writeBytes("\n");
			 writer.writeBytes(data);
			 writer.writeBytes("\n");
		}

		// Close the output stream
		writer.close();
	}


	/**
	 * Convert the given ACE XML 1.1 Classifications file to the type of text
	 * file output by jSongMiner, which is to say field names 
	 * (misc_info info_type attribute values) are put on odd lines and field 
	 * values (misc_info element values) are put on the following even
	 * lines. The resultant string, including line breaks, is returned. URL
	 * decoding is not performed.
	 *
	 * @param ace_xml_path	The path of the ACE XML 1.1 Classifications file to
	 *						transalte.
	 * @return				The text conversion of the contents of the ACE XML
	 *						file.
	 * @throws Exception	An informative exceptino is thrown if a problem 
	 *						occurs.
	 */
	public static String convertACEXMLToText(String ace_xml_path)
			throws Exception
	{
		// Parse the ACE XML file
		SegmentedClassification[] ace_instances = SegmentedClassification.parseClassificationsFile(ace_xml_path);

		// Validate the parsed ACE XML contents
		if (ace_instances == null)
			throw new Exception("Could not read valid metadata from the " + ace_xml_path + " file.");
		if (ace_instances.length != 1)
			throw new Exception("The given " + ace_xml_path + " file contains " +
					ace_instances.length + " instances. It should only contain one.");

		// Store the relevant information from the ACE XML
		String[] field_keys = ace_instances[0].misc_info_key;
		String[] field_values = ace_instances[0].misc_info_info;

		// Validate the parsed ACE XML contents some more
		if (field_keys == null || field_values == null)
			throw new Exception("The given " + ace_xml_path + " file does not contain any metadata for its instance.");
		if (field_keys.length == 0 || field_values.length == 0)
			throw new Exception("The given " + ace_xml_path + " file does not contain any metadata for its instance.");

		// Prepare the text
		StringBuffer results = new StringBuffer();
		for (int i = 0; i < field_keys.length; i++)
		{
			results.append(field_keys[i]);
			results.append("\n");
			results.append(field_values[i]);
			results.append("\n");
		}

		// Return the results
		return results.toString();
	}


	/* PRIVATE METHODS ********************************************************/


	/**
	 * Find an abstract File with an appropriate file path. The file name is
	 * found based on the the provided identifier_keys. The
	 * data_to_save parameter is searched for a MusicMetaData object with one of
	 * the identifier_keys (from start to finish of the array) as its field
	 * identifier. The value of the first matching MusicMetaData object is used
	 * as the file name (with either the or .xml .txt extension appended). If a
	 * file with the selected name already exists, then an underscore followed
	 * by a numerical value is appended to the end of the file name such that
	 * the name is unique.
	 *
	 * @param identifier_keys	The candidate field identifiers to look for
	 *							in the data_to_save in order to acquire the
	 *							filename. These are ranked from best to worse.
	 * @param data_to_save		The data that will eventually be saved
	 * @param url_encode		Whether or not to URL encode (UTF-8) file name.
	 * @param save_directory	The directory to save the file in.
	 * @return					An abstract File where the data may actually
	 *							be saved.
	 * @throws Exception		An informative exception is thrown if the input
	 *							parameters are invalid or if the file could not
	 *							be saved.
	 */
	private static File getSaveFile(String[] identifier_keys,
			Vector<MusicMetaData> data_to_save,
			boolean is_xml,
			boolean url_encode,
			String save_directory)
			throws Exception
	{
		// Throw an exception for invalid input
		if (identifier_keys == null)
			throw new Exception ("Valid identifier keys must be provided (it is null).");
		if (data_to_save == null)
			throw new Exception ("Data to save must be provided (it is null).");
		if (data_to_save.size() == 0)
			throw new Exception ("Data to save must be provided (it is of size 0).");

		// Search through the provided identifier keys and find an identifier to
		// use in naming the file
		String identifier = null;
		for (int i = 0; i < identifier_keys.length; i++)
		{
			identifier = findFirstValidValueForFieldID( identifier_keys[i], data_to_save);
			if (identifier != null) i = identifier_keys.length; // exit loop
		}
		if (identifier == null)
			throw new Exception("No valid identifier could be found.");

		// Update the file name if necessary in order to avoid overwriting and
		// save the file
		File save_file = null;
		int suffix = 0;
		boolean found = false;
		while (!found)
		{
			// Prepare a numerical file name suffix, if necessary
			String file_suffix = "";
			if (suffix != 0)
				file_suffix = "_" + (new Integer(suffix)).toString();

			// Prepare a candidate abstract file
			String file_name = identifier + file_suffix;
			if (is_xml) file_name += ".xml";
			else file_name += ".txt";
			if (url_encode)
				file_name = StringMethods.URLEncodeWithNulls(file_name);
			String save_path = save_directory + System.getProperty("file.separator") + file_name;
			save_file = new File(save_path);

			// Update the file name if a file with the given name already exists
			// and save the file if it doesn't
			if (save_file.exists())
				suffix++;
			else found = true;
		}

		// Return the file
		return save_file;
	}


	/**
	 * Return a single identifer that combines both the source and the field of
	 * the give MusicMetaData object.
	 *
	 * @param source_data	The data to find the single identifier for.
	 * @return				The single identifier. Null is returned if both
	 *						the source and field identifier are null. If only
	 *						one of them is null, then the non-null one is
	 *						returned.
	 */
	private static String generateSingleKey(MusicMetaData source_data)
	{
		String single_key;

		if (source_data.getSource() != null && source_data.getFieldIdentifier() != null)
		{
			if (!source_data.getSource().equals(""))
				single_key = source_data.getSource() + ": " + source_data.getFieldIdentifier();
			else
				single_key = source_data.getFieldIdentifier();
		}
		else if(source_data.getSource() == null && source_data.getFieldIdentifier() == null)
			return null;
		else if (source_data.getFieldIdentifier() == null)
			single_key = source_data.getSource();
		else
			single_key = source_data.getFieldIdentifier();

		return single_key;
	}
}