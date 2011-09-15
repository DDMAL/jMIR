/*
 * RecordingMetaData.java
 * Version 1.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jMusicMetaManager;

import java.awt.Component;
import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.ProgressMonitor;
import mckay.utilities.xml.*;
import de.vdheide.mp3.*;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import jMusicMetaManager.processingstructures.Entries;


/**
 * An object to hold the metadata associated with an MP3 recording that has
 * been added to a music collection. Includes methods for extracting this
 * information from an Apple iTunes XML file or from an MP3's ID3 tags, as well
 * as performing additional processing.
 *
 * <p>String and Date entries are set to null if the information is not
 * available and empty entries in the genre array are remeoved. Numerical entris
 * are set to -1 if the information corresponding to the tag is unknown.
 *
 * @author Cory McKay
 */
public class RecordingMetaData
     implements Serializable
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The file path of the recording referred to.
      */
     public    String    file_path = null;
     
     /**
      * The title of the recording.
      */
     public    String    title = null;
     
     /**
      * The performer(s) of the recording.
      */
     public    String    artist = null;
     
     /**
      * The composer(s) of the recording.
      */
     public    String    composer = null;
     
     /**
      * The name of the album that the recording belongs to.
      */
     public    String    album = null;
     
     /**
      * The disc number of the album that the recording belongs to. Only
      * relevant for multi-disc albums. Set to 1 by default if information is
      * not specified.
      */
     public    int       disc_number = -1;
     
     /**
      * The total number of discs in a mult-disc album. Set to 1 by default if
      * information is not specified.
      */
     public    int       discs_in_set = -1;
     
     /**
      * The track number of the recording on the album that it belongs to.
      */
     public    int       track_number = -1;
     
     /**
      * The total number of tracks in the album that the recording belongs to.
      */
     public    int       total_tracks = -1;
     
     /**
      * Whether or not the album is a compilation album. A value of +1
      * corresponds to true, 0 to false and -1 to unknown. If this is acquired
      * from parsing the ID3 tags, of an MP3, it is set to true if the file
      * path of the file includes the substring "Compilations".
      */
     public    int       compilation = -1;
     
     /**
      * The year that the recording was recorded.
      */
     public    int       year = -1;
     
     /**
      * The genre(s) of the recording. The genres field of ID3 or iTunes XML
      * is interpereted by assuming that any occurences of " + " are meant to
      * separate multiple genre, and these are parsed into separate entries in
      * the genres array.
      */
     public    String[]  genres = null;
     
     /**
      * The length in seconds of the recording.
      */
     public    long      duration = -1;
     
     /**
      * Any comments associated with the recording.
      */
     public    String    comments = null;
     
     /**
      * The bit rate of the recording.
      */
     public    int       bit_rate = -1;
     
     /**
      * Set to 1 if the recording is copy protected, 0 if it is not and
      * -1 if this informatino is not known.
      */
     public    int       is_copy_protected = -1;
     
     /**
      * True if metadata was acquired from iTunes XML.
      */
     public    boolean   from_iTunes_XML = false;
     
     /**
      * True if metadata was acquired from the ID3 tag of an MP3.
      */
     public    boolean   from_id3 = false;
     
     /**
      * The date that the recording was added to the database. Set to the date
      * when an object of this class is constructed by default, but is set
      * to the date noted in an iTunes XML file if such information is
      * available.
      */
     public    Date      date_added = null;
     
     
     /* STATIC FINAL FIELDS ***************************************************/
     
     
     /**
      * An identifier used to signify the title field.
      */
     public    static final int  TITLE_IDENTIFIER = 111;
     
     /**
      * An identifier used to signify the title field.
      */
     public    static final int  ARTIST_IDENTIFIER = 222;
     
     /**
      * An identifier used to signify the album field.
      */
     public    static final int  COMPOSER_IDENTIFIER = 333;
     
     /**
      * An identifier used to signify the album field.
      */
     public    static final int  ALBUM_IDENTIFIER = 444;
     
     /**
      * An identifier used to signify the title field.
      */
     public    static final int  GENRES_IDENTIFIER = 555;
     
     /**
      * An identifier used to signify the comment field.
      */
     public    static final int  COMMENTS_IDENTIFIER = 777;
     
     /**
      * An identifier for use in serialization.
      */
     private   static final long serialVersionUID = 42806L;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new RecordingMetaData instance with fields initialized
      * based on the specified parameters. Automatically sets any given empty
      * strings to null.
      *
      * @param file_path           The corresponding field is set to this value.
      * @param title               The corresponding field is set to this value.
      * @param artist              The corresponding field is set to this value.
      * @param composer            The corresponding field is set to this value.
      * @param album               The corresponding field is set to this value.
      * @param disc_number         The corresponding field is set to this value.
      * @param discs_in_set        The corresponding field is set to this value.
      * @param track_number        The corresponding field is set to this value.
      * @param total_tracks        The corresponding field is set to this value.
      * @param compilation         The corresponding field is set to this value.
      * @param year                The corresponding field is set to this value.
      * @param genres              The corresponding field is set to this value.
      * @param duration            The corresponding field is set to this value.
      * @param comments            The corresponding field is set to this value.
      * @param bit_rate            The corresponding field is set to this value.
      * @param is_copy_protected   The corresponding field is set to this value.
      * @param from_iTunes_XML     The corresponding field is set to this value.
      * @param from_id3            The corresponding field is set to this value.
      * @param date_added          The corresponding field is set to this value.
      *
      */
     public RecordingMetaData(String file_path, String title, String artist,
          String composer, String album, int disc_number, int discs_in_set,
          int track_number, int total_tracks, int compilation, int year,
          String[] genres, long duration, String comments, int bit_rate,
          int is_copy_protected, boolean from_iTunes_XML, boolean from_id3,
          Date date_added)
     {
          // Set fields
          this.file_path = file_path;
          this.title = title;
          this.artist = artist;
          this.composer = composer;
          this.album = album;
          this.disc_number = disc_number;
          this.discs_in_set = discs_in_set;
          this.track_number = track_number;
          this.total_tracks = total_tracks;
          this.compilation = compilation;
          this.year = year;
          this.genres = genres;
          this.duration = duration;
          this.comments = comments;
          this.bit_rate = bit_rate;
          this.is_copy_protected = is_copy_protected;
          this.from_iTunes_XML = from_iTunes_XML;
          this.from_id3 = from_id3;
          this.date_added = date_added;
          
          // Set empty strings to null
          setEmptyStringsToNull();
     }
     
     
     /**
      * Creates a new RecordingMetaData instance with fields initialized
      * based on the ID3 tags of the given MP3 file.
      *
      * @param mp3_file       An MP3 file to extract metadata from.
      * @throws Exception     An informative exception is thrown if there is a
      *                       problem with the file.
      */
     public RecordingMetaData(File mp3_file)
     throws Exception
     {
          // Ensure that the given file is a valid file
          mckay.utilities.staticlibraries.FileMethods.validateFile(mp3_file, true, false);
          
          // Ensure that the given file is a valid MP3 file
          MP3File parsed_file = null;
          try
          {
               parsed_file = new MP3File(mp3_file.getAbsolutePath());
          }
          catch (Exception e)
          {
               if (e instanceof ID3v2IllegalVersionException)
                    throw new Exception("File " + mp3_file.getPath() + " has uses an incompatible ID3 version.");
               else
                    throw new Exception("File " + mp3_file.getPath() + " is not a valid MP3 file.");
          }
          
          // Store the file path
          file_path = mp3_file.getPath();
          
          // Store the title meta-data extracted from the ID3 tag
          title = parsed_file.getTitle().getTextContent();
          
          // Store the artist meta-data extracted from the ID3 tag
          artist = parsed_file.getArtist().getTextContent();
          
          // Store the composer meta-data extracted from the ID3 tag
          composer = parsed_file.getComposer().getTextContent();
          
          // Store the album title meta-data extracted from the ID3 tag
          album = parsed_file.getAlbum().getTextContent();
          
          // Store the disc numbering meta-data extracted from the ID3 tag
          String temp_disc_info = parsed_file.getPartOfSet().getTextContent();
          if (temp_disc_info != null)
          {
               String[] halves = temp_disc_info.split("/");
               disc_number = new Integer(halves[0]).intValue();
               discs_in_set = new Integer(halves[1]).intValue();
          }
          else
          {
               disc_number = 1;
               discs_in_set = 1;
          }
          
          // Store the track numbering meta-data extracted from the ID3 tag
          String temp_track_info = parsed_file.getTrack().getTextContent();
          if (temp_track_info != null)
          {
               String[] halves = temp_track_info.split("/");
               if (halves.length == 2)
               {
                    track_number = new Integer(halves[0]).intValue();
                    total_tracks = new Integer(halves[1]).intValue();
               }
          }
          
          // Determine whether recording is on a compilation album base on
          // its file path
          compilation = file_path.indexOf("Compilations");
          if (compilation != -1)
               compilation = 1;
          else
               compilation = 0;
          
          // Store the year meta-data extracted from the ID3 tag
          if (parsed_file.getYear().getTextContent() != null)
               year = (new Integer(parsed_file.getYear().getTextContent())).intValue();
          
          // Store the genre meta-data extracted from the ID3 tag
          String all_genres = parsed_file.getGenre().getTextContent();
          if (all_genres != null)
               genres = all_genres.split(" \\+ ");
          
          // Store the music duration
          duration = parsed_file.getLength();
          
          // Store the comments meta-data extracted from the ID3 tag
          comments = parsed_file.getComments().getTextContent();
          
          // Store the bit rate
          bit_rate = parsed_file.getBitrate();
          
          // Store whether is copy protected
          if (parsed_file.getCopyright()) // maybe should be getProtection method instead?
               is_copy_protected = 1;
          else
               is_copy_protected = 0;
          
          // Note that this data was extracted from the ID3 tags of an MP3 file
          from_id3 = true;
          
          // Store the date that the file was last modified as the date added
          date_added = new Date(mp3_file.lastModified());
          
          // Set empty strings to null
          setEmptyStringsToNull();
     }
     
     
     /**
      * Initializes all fields to unknown.
      */
     public RecordingMetaData()
     {
     }
     
     
     /* STATIC METHODS ********************************************************/
     
     
     /**
      * Extracts the ID3 metadata from the given array of MP3 files. Invalid
      * files are noted and skipped.
      *
      * @param files               The files to parse.
      * @param unparsable_files    An empty array of outer size 2. Used to store
      *                            information about MP3 files that could not
      *                            be parsed. The first dimension is filled
      *                            with the paths of such files, and the second
      *                            is filled with information on why they could
      *                            not be parsed. Any information already stored
      *                            in the passed array is overwritten. If all
      *                            files could be parsed, then the two entries
      *                            in the first dimension are set to null. This
      *                            paramter may be null if this information is
      *                            not desired.
      * @return                    The metadata corresponding to each of the
      *                            files. The original order is maintained. Null
      *                            is returned if no files are given.
      * @throws Exception          An informative exception is thrown if no
      *                            valid MP3 files could be parsed.
      */
     public static RecordingMetaData[] extractMetaDataFromID3s(File[] files,
          String[][] unparsable_files)
          throws Exception
     {
          // Return null if no files are provided
          if (files == null)
               return null;
          if (files.length == 0)
               return null;
          
          // Extract metadata and note files that cannot be parsed
          Vector<RecordingMetaData> succesfully_parsed = new Vector<RecordingMetaData>();
          Vector<String> could_not_parse_path = null;
          Vector<String> could_not_parse_reason = null;
          for (int i = 0; i < files.length; i++)
          {
               try
               {
                    RecordingMetaData this_one = new RecordingMetaData(files[i]);
                    succesfully_parsed.add(this_one);
               }
               catch (Exception e)
               {
                    if (could_not_parse_path == null)
                    {
                         could_not_parse_path = new Vector<String>();
                         could_not_parse_reason = new Vector<String>();
                    }
                    could_not_parse_path.add(files[i].getAbsolutePath());
                    could_not_parse_reason.add(e.getMessage());
               }
          }
          
          // Record files that could not be parsed
          if (unparsable_files != null)
          {
               if (could_not_parse_path != null)
               {
                    unparsable_files[0] = could_not_parse_path.toArray(new String[1]);
                    unparsable_files[1] = could_not_parse_reason.toArray(new String[1]);
               }
               else
               {
                    unparsable_files[0] = null;
                    unparsable_files[1] = null;
               }
          }
          
          // Record extracted metadata
          RecordingMetaData[] results = succesfully_parsed.toArray(new RecordingMetaData[1]);
          if (results[0] == null)
               throw new Exception("No valid MP3 files could be parsed.");
          
          // Return extracted metadata
          return results;
     }
     
     
     /**
      * Extracts the metadata from the given iTunes XML file.
      *
      * @param file           The file to parse.
      * @param mp3s_only      If this is true, then metadata corresponding to
      *                       files that do not end in ".mp3" (case insensitive)
      *                       are excluded from the returned results.
      * @return               The metadata extracted from the XML file. There is
      *                       one entry for each recording referred to by the
      *                       XML file. Null is returned if no valid recordings
      *                       are found.
      * @throws Exception     An informative exception is thrown if there is a
      *                       problem with the XML file.
      */
     public static RecordingMetaData[] extractMetaDataFromiTunesXML(File file,
          boolean mp3s_only)
          throws Exception
     {
          // Ensure that the given file is a valid file
          mckay.utilities.staticlibraries.FileMethods.validateFile(file, true, false);

		  // Check if a proxy server connection is needed
		   new mckay.utilities.webservices.ProxyServerAccessor(null, null);

          // Prepare the XML parser with the validation feature on and the error
          // handler set to throw exceptions on all warnings and errors
          XMLReader reader = new SAXParser();
          reader.setFeature("http://xml.org/sax/features/validation", true);
          reader.setErrorHandler(new ParsingXMLErrorHandler());
          ParseiTunesXMLFileHandler handler = new ParseiTunesXMLFileHandler();
          reader.setContentHandler(handler);
          
          // Parse the XML file and throw an excpetion if necessary
          try
          {reader.parse(file.getAbsolutePath());}
          catch (SAXParseException e) // throw an exception if the file is not a valid XML file
          {
               throw new Exception("The " + file.getAbsolutePath() + " file is not a valid XML file.\n\n" +
                    "Details of the problem: " + e.getMessage() + "\n\n" +
                    "This error is likely in the region of line " + e.getLineNumber() + ".");
          }
          catch (SAXException e) // throw an exception if the file is not an XML file of the correct type
          {
               throw new Exception("The " + file.getAbsolutePath() + " file must be of type plist.\n\n" +
                    "Details of the problem: " + e.getMessage());
          }
          catch (Exception e) // throw a miscellaneous exception
          {
               // Case where cannot access internet to get Apple's DTD
 
               if(e.getMessage().equals("www.apple.com"))
                    throw new Exception("The specified Apple iTunes file could not be parsed.\n\n" +
                                        "This is because jMusicMetaManager could not access\n" +
                                        "the internet, and it is necessary for the software\n" +
                                        "to contact www.apple.com in order to access the DTD\n" +
                                        "for Apple iTunes XML files.\n\n" +
                                        "In order to solve this problem, connect your\n" +
                                        "computer to the internet and/or prevent your\n" +
                                        "firewall, if any, from blocking Java.");
               
               // Mixcellaneous exception
               throw new Exception("The " + file.getAbsolutePath() + " file is not formatted properly.\n\n" +
                    "Details of the problem: " + e.getMessage());
          }
          
          // Remove the entries for recordings that are not MP3s if this option
          // is selected
          if (mp3s_only)
          {
               Vector<RecordingMetaData> filtered = new Vector<RecordingMetaData>();
               for (int i = 0; i < handler.extracted_metadata.length; i++)
               {
                    String path = handler.extracted_metadata[i].file_path;
                    if (path != null)
                    {
                         if (path.length() > 4)
                         {
                              String extension = path.substring(path.length() - 4);
                              extension = extension.toLowerCase();
                              if (extension.equals(".mp3"))
                                   filtered.add(handler.extracted_metadata[i]);
                         }
                    }
               }
               handler.extracted_metadata = filtered.toArray(new RecordingMetaData[1]);
          }
          
          // Return the metada parsed from the file
          return handler.extracted_metadata;
     }
     
     
     /**
      * Extract metadata from the specified iTunes XML and/or MP3 files
      * and export this information as a Weka ARFF or ACE XML file. No
      * processing is applied to the metadata other than possibly the merging of
      * metadata from the two possible sources if both are specified.
      *
      * @param  to_arff       Whether or not to export to a Weka ARFF file.
      * @param  to_ace        Whether or not to export to an ACE XML file.
      * @param  iTunes_file   A reference to an iTunes XML file. Ignored if this
      *                       parameter is null.
      * @param  mp3_directory A reference to a directory holding MP3 files
      *                       and/or subdirectories holding MP3 files. Ignored
      *                       if this parameter is null.
      * @param  save_file     The file to export to.
      * @throws Exception     Throws an informative exception if there is a
      *                       problem with the parameters or with parsing.
      */
     public static void exportRecordingMetaData(boolean to_arff, boolean to_ace,
          File iTunes_file, File mp3_directory, File save_file)
          throws Exception
     {
          // Validate
          if (iTunes_file == null && mp3_directory == null)
               throw new Exception("No iTunes path specified and no MP3 directory specified.\n" +
                    "One or both must be specified.");
          if (to_arff && to_ace)
               throw new Exception("Must select to export either to ACE XML or to Weka ARFF, not both.");
          if (save_file == null) throw new Exception("No valid save path specified.");
          
          // Extract data from iTunes file
          RecordingMetaData[] recordings_xml = null;
          if (iTunes_file != null)
          {
               mckay.utilities.staticlibraries.FileMethods.validateFile(iTunes_file, true, false);
               recordings_xml = RecordingMetaData.extractMetaDataFromiTunesXML(iTunes_file, true);
               if (recordings_xml == null)
                    throw new Exception("No metadata found in " + iTunes_file.getAbsolutePath() + ".");
          }
          
          // Extract metadata from ID3 tags
          RecordingMetaData[] recordings_id3 = null;
          if (mp3_directory != null)
          {
               if (!mp3_directory.exists())
                    throw new Exception("The specified MP3 directory does not exist.");
               if (!mp3_directory.isDirectory() || !mp3_directory.canRead())
                    throw new Exception("The specified MP3 directory is not a directory\n" +
                         "that can be read.");
               String[] extensions = {"mp3"};
               mckay.utilities.general.FileFilterImplementation filter = new mckay.utilities.general.FileFilterImplementation(extensions);
               File[] mp3_files = mckay.utilities.staticlibraries.FileMethods.getAllFilesInDirectory(mp3_directory, true, filter, null);
               if (mp3_files == null)
                    throw new Exception("No MP3 files found in " + mp3_directory.getAbsolutePath() + ".");
               String[][] could_not_parse = new String[2][];
               recordings_id3 = RecordingMetaData.extractMetaDataFromID3s(mp3_files, null);
          }
          
          // Merge the metadata from the two sources and store them in the
          // recordings variable
          RecordingMetaData[] recordings;
          if (recordings_id3 == null)
               recordings = recordings_xml;
          else if (recordings_xml == null)
               recordings = recordings_id3;
          else
          {
               // Initialize the arrays to hold the mappings. Set to -1 to note
               // that hasn't been found
               int[] files_to_xml_mappings = new int[recordings_id3.length];
               for (int i = 0; i < recordings_id3.length; i++)
                    files_to_xml_mappings[i] = -1;
               int[] xml_to_files_mappings = new int[recordings_xml.length];
               for (int j = 0; j < recordings_xml.length; j++)
                    xml_to_files_mappings[j] = -1;
               
               // Set up dummy arrays
               RecordingMetaData[] temp_files = new RecordingMetaData[recordings_id3.length];
               for (int i = 0; i < temp_files.length; i++)
                    temp_files[i] = recordings_id3[i];
               RecordingMetaData[] temp_xml = new RecordingMetaData[recordings_xml.length];
               for (int j = 0; j < temp_xml.length; j++)
                    temp_xml[j] = recordings_xml[j];
               
               // Find mappings
               for (int i = 0; i < temp_files.length; i++)
               {
                    if (temp_files[i] != null)
                    {
                         for (int j = 0; j < temp_xml.length; j++)
                         {
                              if (temp_xml[j] != null)
                              {
                                   if (temp_files[i].refersToSameFile(temp_xml[j]))
                                   {
                                        // Store mappings
                                        files_to_xml_mappings[i] = j;
                                        xml_to_files_mappings[j] = i;
                                        
                                        // Note that already found for these
                                        // entries to avoid researching
                                        temp_files[i] = null;
                                        temp_xml[j] = null;
                                        
                                        // End this loop
                                        j = temp_xml.length;
                                   }
                              }
                         }
                    }
               }
               
               // Fill the recordings field with the complete set of merged
               // recordings
               Vector<RecordingMetaData> merged_recordings = new Vector<RecordingMetaData>();
               for (int i = 0; i < xml_to_files_mappings.length; i++)
               {
                    if (xml_to_files_mappings[i] != -1)
                         recordings_xml[i].merge(recordings_id3[xml_to_files_mappings[i]]);
                    merged_recordings.add(recordings_xml[i]);
               }
               for (int i = 0; i < files_to_xml_mappings.length; i++)
                    if (files_to_xml_mappings[i] == -1)
                         merged_recordings.add(recordings_id3[i]);
               recordings = merged_recordings.toArray(new RecordingMetaData[1]);
          }
          
          // Make sure that at least one recording is present
          if (recordings == null)
               throw new Exception("No metadata to export available in the provided source(s).");
          if (recordings[0] == null)
               throw new Exception("No metadata to export available in the provided source(s).");
          
          // Prepare to write
          DataOutputStream export_stream = mckay.utilities.staticlibraries.FileMethods.getDataOutputStream(save_file);
          
          // Export to Weka ARFF
          if (to_arff)
          {
               // Write introductory comments
               export_stream.writeBytes("% File: " + save_file.getName() + "\n");
               export_stream.writeBytes("% Generated by jMusicMetaManager on " + DateFormat.getDateInstance().format(new Date()) + "\n");
               if(iTunes_file != null)
                    export_stream.writeBytes("% Generated from iTunes file: " + iTunes_file.getName() + "\n");
               if(mp3_directory != null)
                    export_stream.writeBytes("% Generated from MP3 directory: " + mp3_directory.getName() + "\n");
               
               // Write header
               export_stream.writeBytes("\n@RELATION \'" + save_file.getName() + "\'\n\n");
               export_stream.writeBytes("@ATTRIBUTE file_path         STRING\n");
               export_stream.writeBytes("@ATTRIBUTE title             STRING\n");
               export_stream.writeBytes("@ATTRIBUTE artist            STRING\n");
               export_stream.writeBytes("@ATTRIBUTE composer          STRING\n");
               export_stream.writeBytes("@ATTRIBUTE album             STRING\n");
               export_stream.writeBytes("@ATTRIBUTE disc_number       INTEGER\n");
               export_stream.writeBytes("@ATTRIBUTE discs_in_set      INTEGER\n");
               export_stream.writeBytes("@ATTRIBUTE track_number      INTEGER\n");
               export_stream.writeBytes("@ATTRIBUTE total_tracks      INTEGER\n");
               export_stream.writeBytes("@ATTRIBUTE compilation       INTEGER\n");
               export_stream.writeBytes("@ATTRIBUTE year              INTEGER\n");
               export_stream.writeBytes("@ATTRIBUTE duration          NUMERIC\n");
               export_stream.writeBytes("@ATTRIBUTE comments          STRING\n");
               export_stream.writeBytes("@ATTRIBUTE bit_rate          INTEGER\n");
               export_stream.writeBytes("@ATTRIBUTE is_copy_protected INTEGER\n");
               export_stream.writeBytes("@ATTRIBUTE from_iTunes_XML   STRING\n");
               export_stream.writeBytes("@ATTRIBUTE from_id3          STRING\n");
               export_stream.writeBytes("@ATTRIBUTE date_added        DATE\n");
               export_stream.writeBytes("@ATTRIBUTE genres            STRING\n");
               
               // Write data
               export_stream.writeBytes("\n@DATA\n");
               for (int i = 0; i < recordings.length; i++)
               {
                    if (recordings[i].file_path == null) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes("\'" + recordings[i].file_path.replace("\'", "\\'") + "\', ");
                    
                    if (recordings[i].title == null) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes("\'" + recordings[i].title.replace("\'", "\\'") + "\', ");
                    
                    if (recordings[i].artist == null) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes("\'" + recordings[i].artist.replace("\'", "\\'") + "\', ");
                    
                    if (recordings[i].composer == null) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes("\'" + recordings[i].composer.replace("\'", "\\'") + "\', ");
                    
                    if (recordings[i].album == null) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes("\'" + recordings[i].album.replace("\'", "\\'") + "\', ");
                    
                    if (recordings[i].disc_number == -1) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes(recordings[i].disc_number + ", ");
                    
                    if (recordings[i].bit_rate == -1) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes(recordings[i].discs_in_set + ", ");
                    
                    if (recordings[i].discs_in_set == -1) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes(recordings[i].track_number + ", ");
                    
                    if (recordings[i].total_tracks == -1) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes(recordings[i].total_tracks + ", ");
                    
                    if (recordings[i].compilation == -1) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes(recordings[i].compilation + ", ");
                    
                    if (recordings[i].year == -1) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes(recordings[i].year + ", ");
                    
                    if (recordings[i].duration == -1) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes(recordings[i].duration + ", ");
                    
                    if (recordings[i].comments == null) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes("\'" + recordings[i].comments.replace("\'", "\\'") + "\', ");
                    
                    if (recordings[i].bit_rate == -1) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes(recordings[i].bit_rate + ", ");
                    
                    if (recordings[i].is_copy_protected == -1) export_stream.writeBytes("?, ");
                    else export_stream.writeBytes(recordings[i].is_copy_protected + ", ");
                    
                    export_stream.writeBytes(recordings[i].from_iTunes_XML + ", ");
                    
                    export_stream.writeBytes(recordings[i].from_id3 + ", ");
                    
                    if (recordings[i].date_added == null) export_stream.writeBytes("?, ");
                    else
                    {
                         SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getInstance();
                         sdf.applyPattern("yyyy-MM-dd'T'HH:mm:ss");
                         export_stream.writeBytes(sdf.format(recordings[i].date_added) + ", ");
                    }
                    
                    if (recordings[i].genres == null) export_stream.writeBytes("?\n");
                    else
                    {
                         export_stream.writeBytes("\'");
                         for (int j = 0; j < recordings[i].genres.length; j++)
                         {
                              export_stream.writeBytes(recordings[i].genres[j].replace("\'", "\\'"));
                              if (j < recordings[i].genres.length - 1)
                                   export_stream.writeBytes(" + ");
                         }
                         export_stream.writeBytes("\'\n");
                    }
               }
          }
          
          // Export to ACE XML
          if (to_ace)
          {
               // Write header
               String comments = "File " + save_file.getName() + " generated by jMusicMetaManager on " + DateFormat.getDateInstance().format(new Date());
               if (iTunes_file != null)
                    comments += " from iTunes file " + iTunes_file.getName();
               if (mp3_directory != null)
               {
                    if (iTunes_file != null)
                         comments += " and ";
                    comments += " from MP3 directory " + mp3_directory.getName();
               }
               export_stream.writeBytes(
                    "<?xml version=\"1.0\"?>\n" +
                    "<!DOCTYPE classifications_file [\n" +
                    "   <!ELEMENT classifications_file (comments, data_set+)>\n" +
                    "   <!ELEMENT comments (#PCDATA)>\n" +
                    "   <!ELEMENT data_set (data_set_id, misc_info*, role?, classification)>\n" +
                    "   <!ELEMENT data_set_id (#PCDATA)>\n" +
                    "   <!ELEMENT misc_info (#PCDATA)>\n" +
                    "   <!ATTLIST misc_info info_type CDATA \"\">\n" +
                    "   <!ELEMENT role (#PCDATA)>\n" +
                    "   <!ELEMENT classification (section*, class*)>\n" +
                    "   <!ELEMENT section (start, stop, class+)>\n" +
                    "   <!ELEMENT class (#PCDATA)>\n" +
                    "   <!ELEMENT start (#PCDATA)>\n" +
                    "   <!ELEMENT stop (#PCDATA)>\n" +
                    "]>\n\n" +
                    "<classifications_file>\n\n" +
                    "\t<comments>" + comments + "</comments>\n\n");
               
               // Write the data
               for (int i = 0; i < recordings.length; i++)
               {
                    export_stream.writeBytes("\t<data_set>\n");
                    export_stream.writeBytes("\t\t<data_set_id>" + URLEncoder.encode(recordings[i].file_path, "utf-8") + "</data_set_id>\n");
                    
                    if (recordings[i].title != null)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"title\">" + URLEncoder.encode(recordings[i].title, "utf-8") + "</misc_info>\n");
                    
                    if (recordings[i].artist != null)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"artist\">" + URLEncoder.encode(recordings[i].artist, "utf-8") + "</misc_info>\n");
                    
                    if (recordings[i].composer != null)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"composer\">" + URLEncoder.encode(recordings[i].composer, "utf-8") + "</misc_info>\n");
                    
                    if (recordings[i].album != null)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"album\">" + URLEncoder.encode(recordings[i].album, "utf-8") + "</misc_info>\n");
                    
                    if (recordings[i].disc_number != -1)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"disc_number\">" + recordings[i].disc_number + "</misc_info>\n");
                    
                    if (recordings[i].discs_in_set != -1)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"discs_in_set\">" + recordings[i].discs_in_set + "</misc_info>\n");
                    
                    if (recordings[i].track_number != -1)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"track_number\">" + recordings[i].track_number + "</misc_info>\n");
                    
                    if (recordings[i].total_tracks != -1)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"total_tracks\">" + recordings[i].total_tracks + "</misc_info>\n");
                    
                    if (recordings[i].compilation != -1)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"compilation\">" + recordings[i].compilation + "</misc_info>\n");
                    
                    if (recordings[i].year != -1)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"year\">" + recordings[i].year + "</misc_info>\n");
                    
                    if (recordings[i].genres != null)
                    {
                         export_stream.writeBytes("\t\t<misc_info info_type=\"genres\">");
                         for (int j = 0; j < recordings[i].genres.length; j++)
                         {
                              export_stream.writeBytes(URLEncoder.encode(recordings[i].genres[j], "utf-8"));
                              if (j < recordings[i].genres.length - 1)
                                   export_stream.writeBytes(" + ");
                         }
                         export_stream.writeBytes("</misc_info>\n");
                    }
                    
                    if (recordings[i].duration != -1)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"duration\">" + recordings[i].duration + "</misc_info>\n");
                    
                    if (recordings[i].comments != null)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"comments\">" + URLEncoder.encode(recordings[i].comments, "utf-8") + "</misc_info>\n");
                    
                    if (recordings[i].bit_rate != -1)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"bit_rate\">" + recordings[i].bit_rate + "</misc_info>\n");
                    
                    if (recordings[i].is_copy_protected != -1)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"is_copy_protected\">" + recordings[i].is_copy_protected + "</misc_info>\n");
                    
                    export_stream.writeBytes("\t\t<misc_info info_type=\"from_iTunes_XML\">" + recordings[i].from_iTunes_XML + "</misc_info>\n");
                    
                    export_stream.writeBytes("\t\t<misc_info info_type=\"from_id3\">" + recordings[i].from_id3 + "</misc_info>\n");
                    
                    if (recordings[i].date_added != null)
                         export_stream.writeBytes("\t\t<misc_info info_type=\"date_added\">" + recordings[i].date_added + "</misc_info>\n");
                    
                    export_stream.writeBytes("\n\t\t<classification></classification>\n");
                    export_stream.writeBytes("\t</data_set>\n\n");
               }
               
               // Finalize
               export_stream.writeBytes("</classifications_file>");
          }
          
          // Close the output stream
          export_stream.close();
     }
     
     
     /**
      * Returns the subset of the given recordings specified in the given array
      * of indexes.
      *
      * @param recordings     The recordings to draw the subset from.
      * @param indexes        The inexes of the recordings in the recordings
      *                       parameter that are to make up the subset.
      * @return               The subset.
      */
     public static RecordingMetaData[] filterRecordings(RecordingMetaData[] recordings,
          int[] indexes)
     {
          RecordingMetaData[] new_recordings = new RecordingMetaData[indexes.length];
          for (int i = 0; i < new_recordings.length; i++)
               new_recordings[i] = recordings[indexes[i]];
          return new_recordings;
     }
     
     
     /**
      * Checks whether all of the given recordings all have the same value for
      * the specified field.
      *
      * @param recordings          The recordigns to check.
      * @param field_identifier    Identifies the field to check. Must
      *                            correspond to either TITLE_IDENTIFIER,
      *                            ARTIST_IDENTIFIER, COMPOSER_IDENTIFIER
      *                            or ALBUM_IDENTIFIER. GENRES_IDENTIFIER is
      *                            NOT acceptable. Any value other than one
      *                            of the acceptable codes will result in true
      *                            being returned.
      * @return                    Whether or not all given recordings have
      *                            the same value for the specified field.
      */
     public static boolean haveIdenticalFields(RecordingMetaData[] recordings,
          int field_identifier)
     {
          String[] fields = new String[recordings.length];
          for (int i = 0; i < recordings.length; i++)
          {
               if (field_identifier == TITLE_IDENTIFIER)
                    fields[i] = recordings[i].title;
               else if (field_identifier == ARTIST_IDENTIFIER)
                    fields[i] = recordings[i].artist;
               else if (field_identifier == COMPOSER_IDENTIFIER)
                    fields[i] = recordings[i].composer;
               else if (field_identifier == ALBUM_IDENTIFIER)
                    fields[i] = recordings[i].album;
          }
          return mckay.utilities.staticlibraries.StringMethods.areAllEntriesIdentical(fields);
     }
     
     
     /**
      * Takes the given set of recording metadata, considers those recordings
      * specified in the given these_indexes parameter (which should all
      * correspond to recordings in the same album), and returns the total
      * number of tracks in the album, or -1 if unknown, based on the metadata.
      *
      * <p>Note that if multi-volume albums are encountered, the cumulative
      * total is returned.
      *
      * @param these_recordings    Metada on a set or recordings.
      * @param these_indexes       Indexes of recordings in these_recordings
      *                            that correspond to recordings belonging to
      *                            the same album.
      * @return                    The total number of tracks in the album,
      *                            based on the total_tracks field(s) of
      *                            these_recordings.
      */
     public static int getTotalNumberOfTracks(RecordingMetaData[] these_recordings,
          int[] these_indexes)
     {
          int total_tracks = these_recordings[these_indexes[0]].total_tracks;
          if (total_tracks == -1)
               return -1;
          else
          {
               // Take into account possibility of multi-disc sets
               if (these_recordings[these_indexes[0]].discs_in_set > 1)
               {
                    int[][] tracks_and_disc_numbers = new int[these_indexes.length][2];
                    for (int k = 0; k < tracks_and_disc_numbers.length; k++)
                    {
                         tracks_and_disc_numbers[k][0] = these_recordings[these_indexes[k]].total_tracks;
                         tracks_and_disc_numbers[k][1] = these_recordings[these_indexes[k]].disc_number;
                    }
                    int count_so_far = 0;
                    for (int k = 0; k < tracks_and_disc_numbers.length; k++)
                    {
                         if (tracks_and_disc_numbers[k][0] > 0)
                         {
                              if (count_so_far == 0)
                                   count_so_far += tracks_and_disc_numbers[k][0];
                              else
                              {
                                   boolean found = false;
                                   int current_disc = tracks_and_disc_numbers[k][1];
                                   for (int m = 0; m < k; m++)
                                   {
                                        if (tracks_and_disc_numbers[m][1] == current_disc)
                                        {
                                             found = true;
                                             m = k;
                                        }
                                   }
                                   if (!found)
                                        count_so_far += tracks_and_disc_numbers[k][0];
                              }
                         }
                    }
                    total_tracks = count_so_far;
               }
               
               return total_tracks;
          }
     }
     
     
     /**
      * Finds the number of <i>unique</i> items (titles, composers, genres,
      * artists or albums) in the subset of the given metadata specified by the
      * given indexes.
      *
      * <p>Field values of null are not counted in the tallies. Also,
      * any find/replace operations that have been performed are not reflected
      * here for the purpose of determining uniqueness.
      *
      * @param indexes        The indexes of metadata to consider.
      * @param type           A code detailing what field of metadata  to
      *                       consider. May be "titles", "composers", "genres",
      *                       "artists", "albums" or "comments".
      * @param metadata       The metadata to search.
      * @return               How many unique items are present in the specified
      *                       metadata subset.
      * @throws Exception     An exception is thrown if an invalid type is
      *                       specified.
      */
     public static int findNumberUniqueItems(int[] indexes, String type,
          RecordingMetaData[] metadata)
          throws Exception
     {
          // Return the result for titles
          if (type.equals("titles"))
               return indexes.length;
          
          // Determine if dealing with artists, composers, albums or genres
          boolean storing_artists = false;
          boolean storing_composers = false;
          boolean storing_albums = false;
          boolean storing_genres = false;
          boolean storing_comments = false;
          
          if (type.equals("composers"))
               storing_composers = true;
          else if (type.equals("genres"))
               storing_genres = true;
          else if (type.equals("artists"))
               storing_artists = true;
          else if (type.equals("albums"))
               storing_albums = true;
          else if (type.equals("comments"))
               storing_comments = true;
          else
               throw new Exception("Invalid type code.");
          
          // Store all field values
          Entries processing_entries = new Entries();
          for (int i = 0; i < indexes.length; i ++)
          {
               if (storing_artists && metadata[indexes[i]].artist != null)
                    processing_entries.addEntry(metadata[indexes[i]].artist, indexes[i]);
               else if (storing_composers && metadata[indexes[i]].composer != null)
                    processing_entries.addEntry(metadata[indexes[i]].composer, indexes[i]);
               else if (storing_albums && metadata[indexes[i]].album != null)
                    processing_entries.addEntry(metadata[indexes[i]].album, indexes[i]);
               else if (storing_comments && metadata[indexes[i]].comments != null)
                    processing_entries.addEntry(metadata[indexes[i]].comments, indexes[i]);
               else if (storing_genres && metadata[indexes[i]].genres != null)
               {
                    for (int j = 0; j < metadata[indexes[i]].genres.length; j++)
                         processing_entries.addEntry(metadata[indexes[i]].genres[j], indexes[i]);
               }
          }
          
          // Discount non-unique field items
          processing_entries.mergeIdenticalEntries(false, null);
          
          // Return the tally
          return processing_entries.getNumberEntries();
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Notes whether the the given RecordingMetaData refers to the same file
      * (in its file_path field) as this one.
      *
      * @param other_metadata The RecordingMetaData to check to see if it
      *                       refers to the same file.
      * @return               True if they both refer to the same file and false
      *                       if they refer to different files or if one or
      *                       both of the files don't have paths provided.
      */
     public boolean refersToSameFile(RecordingMetaData other_metadata)
     {
          if (other_metadata.file_path == null || file_path == null)
               return false;
          
          File this_one = new File(file_path);
          File other_one = new File(other_metadata.file_path);
          
          int comparison = this_one.compareTo(other_one);
          if (comparison == 0)
               return true;
          else
               return false;
     }
     
     
     /**
      * Update the fields of this RecordingMetaData object with the metadata
      * for the given RecordingMetaData object. The fields of this
      * RecordingMetaData object are left unchanged unless they are unknown
      * (i.e. have a vaule of null or -1). If a field is unknown, it is
      * updated with the corresponding field of other_metadata.
      *
      * <p>If either RecordingMetaData object has a value of true for its
      * from_iTunes_XML field, then this RecordingMetaData object will also
      * be assigned a value of true for this field. The same is true for the
      * from_id3 field.
      *
      * <p>With the exception of these two fields, all fields of this
      * RecordingMetaData object default to their original values, even if they
      * differ from the contents of other_metadata's fields.
      *
      * @param other_metadata The RecordingMetaData to update this
      *                       RecordingMetaData's fields with
      */
     public void merge(RecordingMetaData other_metadata)
     {
          if (file_path == null)
               file_path = other_metadata.file_path;
          if (title == null)
               title = other_metadata.title;
          if (artist == null)
               artist = other_metadata.artist;
          if (composer == null)
               composer = other_metadata.composer;
          if (album == null)
               album = other_metadata.album;
          if (disc_number == -1)
               disc_number = other_metadata.disc_number;
          if (discs_in_set == -1)
               discs_in_set = other_metadata.discs_in_set;
          if (track_number == -1)
               track_number = other_metadata.track_number;
          if (total_tracks == -1)
               total_tracks = other_metadata.total_tracks;
          if (compilation == -1)
               compilation = other_metadata.compilation;
          if (year == -1)
               year = other_metadata.year;
          if (genres == null)
               genres = other_metadata.genres;
          if (duration == -1)
               duration = other_metadata.duration;
          if (comments == null)
               comments = other_metadata.comments;
          if (bit_rate == -1)
               bit_rate = other_metadata.bit_rate;
          if (is_copy_protected == -1)
               is_copy_protected = other_metadata.is_copy_protected;
          if (other_metadata.from_iTunes_XML)
               from_iTunes_XML = true;
          if (other_metadata.from_id3)
               from_id3 = true;
          if (date_added == null)
               date_added = other_metadata.date_added;
     }
     
     
     /**
      * Compares this RecordingMetaData object with another and resturns a
      * detailed list of the differences between the fields of the two objects.
      *
      * <p>Note that this method does not report diffreences in the following
      * fields: date_added, from_iTunes_xml, from_id3 and is_copy_protected.
      * Code is written to find these differences, but it is currently commented
      * out because this method is typically used to compare an entry from
      * an iTunes XML file with an MP3 file, and these fields will in general
      * always differ.
      *
      * @param other_metadata The RecordingMetaData object whose fields are
      *                       to be compared to this one's.
      * @param translatecodes If this is true, then the entries in the returned
      *                       array that are null or "-1" are translated to
      *                       "unknown", entries that are "0" are translated to
      *                       "false" and entries of "1" are translated to
      *                       "true".
      * @return               The field be field differences between the two
      *                       RecordingMetaData objectes. The dimensionality
      *                       of the first index is equal to the number of
      *                       fields that differ. If two fields are the same,
      *                       then no entry is creaded. The second index
      *                       always has a size of three. The first entry
      *                       names the field that differs, the second shows
      *                       the value of this object's fields and the
      *                       third shows the value of the corresponding field
      *                       in other_metadata. Null is returned if there are
      *                       no differences.
      */
     public String[][] getDifferences(RecordingMetaData other_metadata,
          boolean translatecodes)
     {
          // Prepare the list of found differences
          Vector<String[]> differences = new Vector<String[]>();
          
          // Test if the same file is referred to
          if (!refersToSameFile(other_metadata))
          {
               String[] problem = {"File Path", file_path, other_metadata.file_path};
               differences.add(problem);
          }
          
          // Test if the same title is referred to
          mckay.utilities.staticlibraries.StringMethods.reportStringDifferences("Title", title, other_metadata.title, differences);
          
          // Test if the same artist is referred to
          mckay.utilities.staticlibraries.StringMethods.reportStringDifferences("Artist", artist, other_metadata.artist, differences);
          
          // Test if the same composer is referred to
          mckay.utilities.staticlibraries.StringMethods.reportStringDifferences("Composer", composer, other_metadata.composer, differences);
          
          // Test if the same album is referred to
          mckay.utilities.staticlibraries.StringMethods.reportStringDifferences("Album", album, other_metadata.album, differences);
          
          // Test if the same disc number is referred to
          if (disc_number != other_metadata.disc_number)
          {
               String[] problem = {"Disc Number", String.valueOf(disc_number), String.valueOf(other_metadata.disc_number)};
               differences.add(problem);
          }
          
          // Test if the same discs in set are referred to
          if (discs_in_set != other_metadata.discs_in_set)
          {
               String[] problem = {"Discs In Set", String.valueOf(discs_in_set), String.valueOf(other_metadata.discs_in_set)};
               differences.add(problem);
          }
          
          // Test if the same track number is referred to
          if (track_number != other_metadata.track_number)
          {
               String[] problem = {"Track Number", String.valueOf(track_number), String.valueOf(other_metadata.track_number)};
               differences.add(problem);
          }
          
          // Test if the same total tracks are referred to
          if (total_tracks != other_metadata.total_tracks)
          {
               String[] problem = {"Total Tracks", String.valueOf(total_tracks), String.valueOf(other_metadata.total_tracks)};
               differences.add(problem);
          }
          
          // Test if the same compilation status is referred to
          if (compilation != other_metadata.compilation)
          {
               String[] problem = {"Compilation", String.valueOf(compilation), String.valueOf(other_metadata.compilation)};
               differences.add(problem);
          }
          
          // Test if the same year is referred to
          if (year != other_metadata.year)
          {
               String[] problem = {"Year", String.valueOf(year), String.valueOf(other_metadata.year)};
               differences.add(problem);
          }
          
          // Test if the same genres referred to
          if (genres == null && other_metadata.genres != null)
          {
               for (int i = 0; i < other_metadata.genres.length; i++)
               {
                    String[] problem = {"Genre", null, other_metadata.genres[i]};
                    differences.add(problem);
               }
          }
          else if (genres != null && other_metadata.genres == null)
          {
               for (int i = 0; i < genres.length; i++)
               {
                    String[] problem = {"Genre", genres[i], null};
                    differences.add(problem);
               }
          }
          else if (genres != null && other_metadata.genres != null)
          {
               for (int i = 0; i < genres.length; i++)
               {
                    boolean found = false;
                    for (int j = 0; j < other_metadata.genres.length; j++)
                    {
                         if (genres[i].equals(other_metadata.genres[j]))
                         {
                              found = true;
                              j = other_metadata.genres.length;
                         }
                    }
                    if (!found)
                    {
                         String[] problem = {"Genre", genres[i], ""};
                         differences.add(problem);
                    }
               }
               for (int i = 0; i < other_metadata.genres.length; i++)
               {
                    boolean found = false;
                    for (int j = 0; j < genres.length; j++)
                    {
                         if (other_metadata.genres[i].equals(genres[j]))
                         {
                              found = true;
                              j = genres.length;
                         }
                    }
                    if (!found)
                    {
                         String[] problem = {"Genre", "", other_metadata.genres[i]};
                         differences.add(problem);
                    }
               }
          }
          
          // Test if the same duration is referred to
          if (duration != other_metadata.duration)
          {
               String[] problem = {"Duration", String.valueOf(duration), String.valueOf(other_metadata.duration)};
               differences.add(problem);
          }
          
          // Test if the same comments is referred to
          mckay.utilities.staticlibraries.StringMethods.reportStringDifferences("Comments", comments, other_metadata.comments, differences);
          
          // Test if the same bit rate is referred to
          if (bit_rate != other_metadata.bit_rate)
          {
               String[] problem = {"Bit Rate", String.valueOf(bit_rate), String.valueOf(other_metadata.bit_rate)};
               differences.add(problem);
          }
          
          // Test if the same copy protection status is referred to
          /*
          if (is_copy_protected != other_metadata.is_copy_protected)
          {
               String[] problem = {"Copy Protected", String.valueOf(is_copy_protected), String.valueOf(other_metadata.is_copy_protected)};
               differences.add(problem);
          }
           */
          
          // Test if the same iTunes status is referred to
          /*
          if (from_iTunes_XML != other_metadata.from_iTunes_XML)
          {
               String[] problem = {"From iTunes XML", String.valueOf(from_iTunes_XML), String.valueOf(other_metadata.from_iTunes_XML)};
               differences.add(problem);
          }
           */
          
          // Test if the same ID3 status is referred to
          /*
          if (from_id3 != other_metadata.from_id3)
          {
               String[] problem = {"From MP3 ID3", String.valueOf(from_id3), String.valueOf(other_metadata.from_id3)};
               differences.add(problem);
          }
           */
          
          // Test if the same date added is referred to
          /*
          DateFormat date_format = DateFormat.getDateInstance();
          if (date_added == null && other_metadata.date_added != null)
          {
               String[] problem = {"Date Added", null, date_format.format(other_metadata.date_added)};
               differences.add(problem);
          }
          else if (date_added != null && other_metadata.date_added == null)
          {
               String[] problem = {"Date Added", date_format.format(date_added), null};
               differences.add(problem);
          }
          else if (date_added != null && other_metadata.date_added != null)
          {
               if (!date_added.equals(other_metadata.date_added))
               {
                    String[] problem = {"Date Added", date_format.format(date_added), date_format.format(other_metadata.date_added)};
                    differences.add(problem);
               }
          }
           */
          
          // Return null if there are no differences
          if (differences.size() == 0)
               return null;
          
          // Reformat the results
          String[][] results = differences.toArray(new String[1][1]);
          
          // Replace null and -1 entries with formatted entries if requested
          if (translatecodes)
          {
               for (int i = 0; i < results.length; i++)
               {
                    for (int j = 1; j < 3; j++)
                    {
                         if (results[i][j] == null)
                              results[i][j]= "unknown";
                         else if (results[i][j].equals("-1"))
                              results[i][j] = "unknown";
                         else if (results[i][j].equals("1"))
                         {
                              if (!results[i][0].equals("Disc Number") &&
                                   !results[i][0].equals("Discs In Set") &&
                                   !results[i][0].equals("Track Number") &&
                                   !results[i][0].equals("Total Tracks") &&
                                   !results[i][0].equals("Year") &&
                                   !results[i][0].equals("Duration") &&
                                   !results[i][0].equals("Bit Rate") )
                                   results[i][j] = "true";
                         }
                         else if (results[i][j].equals("0"))
                         {
                              if (!results[i][0].equals("Disc Number") &&
                                   !results[i][0].equals("Discs In Set") &&
                                   !results[i][0].equals("Track Number") &&
                                   !results[i][0].equals("Total Tracks") &&
                                   !results[i][0].equals("Year") &&
                                   !results[i][0].equals("Duration") &&
                                   !results[i][0].equals("Bit Rate") )
                                   results[i][j] = "false";
                         }
                    }
               }
          }
          
          // Return the differences
          return results;
     }
     
     
     /**
      * Returns a string array whose entries contain the values for each field
      * of this RecordingMetaData object. The contents are rephrased in some
      * cases in order to make them more meaningful to viewers.
      *
      * @param index     A number to fill the first entry of the returned array
      *                  with.
      * @param headings  A dummy parameter that allows the calling method
      *                  to access the heading names of each of the fields
      *                  returned. An empty String[1][] should be passed
      *                  to this array. The first entry of this array will
      *                  be filled by this method with the corresponding title
      *                  of each entry of the returned array.
      * @return          The formatted and interpereted values of this object's
      *                  fields.
      */
     public String[] getFormattedFieldContents(int index, String[][] headings)
     {
          // Fill in the report
          String[] report = new String[20];
          report[0] = "" + index;
          report[1] = title;
          report[2] = artist;
          report[3] = album;
          report[4] = "" + track_number;
          report[5] = "" + total_tracks;
          report[6] = "" + disc_number;
          report[7] = "" + discs_in_set;
          report[8] = file_path;
          report[9] = "" + from_iTunes_XML;
          report[10] = "" + from_id3;
          if (genres != null)
          {
               report[11] = new String("");
               for (int i = 0; i < genres.length; i++)
               {
                    report[11] += genres[i];
                    if (i < genres.length - 1)
                         report[11] += " + ";
               }
          }
          report[12] = comments;
          report[13] = composer;
          report[14] = "" + year;
          report[15] = "" + compilation;
          report[16] = "" + duration;
          report[17] = "" + is_copy_protected;
          report[18] = "" + date_added;
          report[19] = "" + bit_rate;
          
          // Format the colum headings
          String[] dummy_headings = {"Number", "Title", "Artist", "Album",
          "Track Number", "Total Tracks", "Disc Number", "Discs In Set",
          "File Path", "From iTunes XML", "From MP3 ID3", "Genres", "Comments",
          "Composer", "Year", "Compilation", "Duration", "Copy Protected",
          "Date Added", "Bit Rate"};
          headings[0] = dummy_headings;
          
          // Format the report
          for (int i = 0; i < report.length; i++)
          {
               if (report[i] == null)
                    report[i] = "UNKNOWN";
               else if (report[i].equals("-1"))
                    report[i] = "UNKNOWN";
               else if (report[i].equals("1"))
               {
                    if ( !dummy_headings[i].equals("Number") &&
                         !dummy_headings[i].equals("Disc Number") &&
                         !dummy_headings[i].equals("Discs In Set") &&
                         !dummy_headings[i].equals("Track Number") &&
                         !dummy_headings[i].equals("Total Tracks") &&
                         !dummy_headings[i].equals("Year") &&
                         !dummy_headings[i].equals("Duration") &&
                         !dummy_headings[i].equals("Bit Rate") )
                         report[i] = "true";
               }
               else if (report[i].equals("0"))
               {
                    if ( !dummy_headings[i].equals("Number") &&
                         !dummy_headings[i].equals("Disc Number") &&
                         !dummy_headings[i].equals("Discs In Set") &&
                         !dummy_headings[i].equals("Track Number") &&
                         !dummy_headings[i].equals("Total Tracks") &&
                         !dummy_headings[i].equals("Year") &&
                         !dummy_headings[i].equals("Duration") &&
                         !dummy_headings[i].equals("Bit Rate") )
                         report[i] = "false";
               }
          }
          
          // Return the results
          return report;
     }
     
     
     /**
      * Returns a string array whose entries contain the values for selected
      * fields of this RecordingMetaData object. The contents are rephrased in
      * some cases in order to make them more meaningful to viewers.
      *
      * @param index     A number to fill the first entry of the returned array
      *                  with.
      * @param headings  A dummy parameter that allows the calling method
      *                  to access the heading names of each of the fields
      *                  returned. An empty String[1][] should be passed
      *                  to this array. The first entry of this array will
      *                  be filled by this method with the corresponding title
      *                  of each entry of the returned array.
      * @return          The formatted and interpereted values of selected
      *                  fields from this object.
      */
     public String[] getPartialFormattedFieldContents(int index,
          String[][] headings)
     {
          // Format the colum headings
          String[] dummy_headings = {"Number", "Title", "Artist", "Album",
          "Track Number", "Disc Number", "Genres", "Composer", "Duration"};
          headings[0] = dummy_headings;
          
          // Get the full report
          String[] full_report = getFormattedFieldContents(index, new String[1][]);
          
          // Choose the fields interested in
          String[] to_return = new String[9];
          to_return[0] = full_report[0];
          to_return[1] = full_report[1];
          to_return[2] = full_report[2];
          to_return[3] = full_report[3];
          to_return[4] = full_report[4];
          to_return[5] = full_report[6];
          to_return[6] = full_report[11];
          to_return[7] = full_report[13];
          to_return[8] = full_report[16];
          
          // Return results
          return to_return;
     }
     
     
     /**
      * A debugging method that prints the metadata held by an object of this
      * class to standard out.
      */
     public void printContents()
     {
          System.out.println("\n");
          System.out.println("FILE PATH:\t" + file_path);
          System.out.println("TITLE:\t\t" + title);
          System.out.println("ARTIST:\t\t" + artist);
          System.out.println("COMPOSER:\t" + composer);
          System.out.println("ALBUM:\t\t" + album);
          System.out.println("DISC NUMBER:\t" + disc_number);
          System.out.println("DISCS IN SET:\t" + discs_in_set);
          System.out.println("TRACK NUMBER:\t" + track_number);
          System.out.println("TOTAL TRACKS:\t" + total_tracks);
          System.out.println("COMPILATION:\t" + compilation);
          System.out.println("YEAR:\t\t" + year);
          System.out.print("GENRES:\t\t");
          if (genres == null)
               System.out.print(genres);
          else for (int i = 0; i < genres.length; i++)
          {
               System.out.print(genres[i]);
               if (i < genres.length - 1)
                    System.out.print(" + ");
          }
          System.out.print("\n");
          System.out.println("DURATION:\t" + duration);
          System.out.println("COMMENTS:\t" + comments);
          System.out.println("BIT RATE:\t" + bit_rate);
          System.out.println("COPY PROTECTED:\t" + is_copy_protected);
          System.out.println("FROM ITUNES:\t" + from_iTunes_XML);
          System.out.println("FROM MP3 ID3:\t" + from_id3);
          System.out.println("DATE ADDED:\t" + date_added);
          System.out.println("\n");
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Sets all string fields that contain an empty string to null. Also
      * deletes any entris in the genre array that would be null as a result
      * and resets the size of the array to eliminate these null entries.
      */
     private void setEmptyStringsToNull()
     {
          // Set any empty Strings to null
          file_path = mckay.utilities.staticlibraries.StringMethods.nullifyStringIfEmpty(file_path);
          title = mckay.utilities.staticlibraries.StringMethods.nullifyStringIfEmpty(title);
          artist = mckay.utilities.staticlibraries.StringMethods.nullifyStringIfEmpty(artist);
          composer = mckay.utilities.staticlibraries.StringMethods.nullifyStringIfEmpty(composer);
          album = mckay.utilities.staticlibraries.StringMethods.nullifyStringIfEmpty(album);
          comments = mckay.utilities.staticlibraries.StringMethods.nullifyStringIfEmpty(comments);
          
          // Remove empty entries in genres array
          if (genres != null)
          {
               if (genres.length == 0)
                    genres = null;
               else
               {
                    for (int i = 0; i < genres.length; i++)
                         genres[i] = mckay.utilities.staticlibraries.StringMethods.nullifyStringIfEmpty(genres[i]);
                    Object[] temp = mckay.utilities.staticlibraries.ArrayMethods.removeNullEntriesFromArray(genres);
                    if (temp == null)
                         genres = null;
                    else
                    {
                         genres = new String[temp.length];
                         for (int i = 0; i < genres.length; i++)
                              genres[i] = (String) temp[i];
                    }
               }
          }
     }
}
