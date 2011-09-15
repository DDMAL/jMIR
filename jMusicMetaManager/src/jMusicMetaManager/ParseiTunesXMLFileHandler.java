/*
 * ParseiTunesXMLFileHandler.java
 * Version 1.2.1
 *
 * Last modified on July 26, 2010.
 * McGill University
 */

package jMusicMetaManager;

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.Vector;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;


/**
 * An extension of the Xerces XML DefaultHandler class. The methods of this
 * class are called by an instance of an XMLReader while it is parsing an XML
 * document.
 *
 * <p>This particular implementation is used to parse iTunes XML files. After
 * parsing, the metadata for each recording can be obtained from the
 * extracted_metadata field.
 *
 * @author Cory McKay
 */
public class ParseiTunesXMLFileHandler
     extends DefaultHandler
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The metadata parsed from an iTunes XML file. Each entry corresponds
      * to the metadata from a different recording.
      */
     public RecordingMetaData[]         extracted_metadata = null;
     
     /**
      * The metadata of ll recordings parsed so far. Updated as parsing is
      * carried out.
      */
     private Vector<RecordingMetaData>  recordings_so_far = null;
     
     /**
      * The metadata of the recording currently being parsed.
      */
     private RecordingMetaData          current_recording = null;
     
     /**
      * The name of the start element that was most recently parsed.
      */
     private String                     last_start_element = null;
     
     /**
      * The contents of the last key tag that was parsed.
      */
     private String                     current_key = null;
     
     /**
      * The combined complete contents to be stored in a field.
      */
     private String                     complete_contents = null;
     
     /**
      * Used to determine if a meaningful tag is ready to be stored.
      */
     private int                        field_pending = 2;
     
     /**
      * Set to true when library is finished being parsed (playlists are
      * ignored)
      */
     private boolean                    done = false;
     
     
     /**
      * Will be set to the utf-8 coding for the + character.
      */
     private String                     plus = "+";
     
     
     /**
      * The element text parsed from the current XML element so far.
     */
     private StringBuffer				element_text_so_far;


     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * This method is called when the XML file starts being parsed. Performs
      * initialization steps.
      */
     public void startDocument()
     {
          recordings_so_far = new Vector<RecordingMetaData>();
          
          // Prepare the plus character
          try {plus = java.net.URLEncoder.encode("+", "utf-8");}
          catch (Exception e) {}
     }
     
     
     /**
      * This method is called when the start tag of an XML element is encounterd
      * during parsing. It causes the name of the start element to be stored.
      *
      * @param  namespace       Inherited from DefaultHandler.
      * @param	name		Name of the element that is encountered.
      * @param  qName           Inherited from DefaultHandler.
      * @param	atts		The attributes encountered.
      * @throws	SAXException	Exception thrown if is wrong type of XML file.
      */
     public void startElement(String namespace, String name, String qName,
          Attributes atts)
          throws SAXException
     {
          // Reset the string buffer
          element_text_so_far = new StringBuffer();

          if (!done)
               last_start_element = name;
     }
     
     
     /**
      * This method is called when the end tag of an XML element is encounterd
      * during parsing. It causes the value of a metadata field to be stored, if
      * appropriate.
      *
      * @param  namespace       Inherited from DefaultHandler.
      * @param	name		Name of the element that is encountered.
      * @param  qname           Inherited from DefaultHandler.
      */
     public void endElement(String namespace, String name, String qname)
     {
		  if (!done)
          {
               // Converts the parsed contents to a string
               String contents = element_text_so_far.toString();
               
               // If these contents are the contents of a key tag, then store
               // the type of key that it is. Also start a new recording if
               // the key contents are numerical (which indicates that the iTunes
               // XML file is storing data for a new recording). Also takes note
               // if a Compilation tag is encoutnered.
               if (last_start_element.equals("key"))
               {
                    // Note if finished acquiring needed metadata
                    if (contents.equals("Playlists"))
                         done = true;
                    
                    else
                    {
                         // Store the name of the key
                         current_key = contents;
                         
                         // Note that a key has recently been encountered
                         field_pending = 2;
                         complete_contents = new String();
                         
                         // Note that is a compilation if appropriate
                         if(contents.equals("Compilation"))
                              current_recording.compilation = 1;
                         
                         // Start a new recording if metadata for a new recording is
                         // found
                         if (mckay.utilities.staticlibraries.StringMethods.testIfStringIsAnInt(contents))
                              startNewRecording();
                    }
               }
               
               // Accumulate values for a field (necessary because ampersand
               // code breaks into multiple entries)
               else
               {
                    complete_contents += contents;
               }
          }

		 if (!done)
          {
               field_pending--;
               if (field_pending == 0)
               {
                    storeField();
               }
          }
     }
     
     
     /**
      * Called when the contents of an XML tag are parsed. 
      *
      * @param ch        Contains the characters parsed from the XML tag.
      * @param start     Where in ch to start looking for this tag's content.
      * @param length    Information on where in ch to stop looking for this
      *                  tag's content.
      */
     public void characters(char[] ch, int start, int length)
     {
          // The text stored in the element
          String text = new String(ch, start, length);

          // Store the text
          element_text_so_far.append(text);
     }
     
     
     /**
      * Called when the XML document comes to an end. Stores all information
      * that was parsed into the extracted_metadata field.
      */
     public void endDocument()
     {
          // Add the last recording
          commitCurrentRecording();
          
          // Commit the recordings to extracted_metadata
          extracted_metadata = recordings_so_far.toArray(new RecordingMetaData[1]);
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Store the value of a metadata field.
      */
     private void storeField()
     {
          // Decode the field. If there is an error during decoding, just use
          // the undecoded value for the field.
          if (!mckay.utilities.staticlibraries.StringMethods.testIfStringIsAnInt(complete_contents))
          {
               try
               {
                    // The plus sign is preencoded, as the iTunes format does
                    // not encode it
                    complete_contents = complete_contents.replace("+", plus);
                     
                    // Decode all characters in order to match their true
                    // values
                    complete_contents = java.net.URLDecoder.decode(complete_contents, "utf-8");
               }
               catch (Exception e)
               {}
          }
          
          // Store the file path
          if (current_key.equals("Location"))
          {
               current_recording.file_path = complete_contents;
               current_recording.file_path = current_recording.file_path.replaceFirst("file://localhost/", "");
          }
          
          // Store the title
          else if (current_key.equals("Name"))
               current_recording.title = complete_contents;
          
          // Store the artist
          else if (current_key.equals("Artist"))
               current_recording.artist = complete_contents;
          
          // Store the composer
          else if (current_key.equals("Composer"))
               current_recording.composer = complete_contents;
          
          // Store the album
          else if (current_key.equals("Album"))
               current_recording.album = complete_contents;
          
          // Store the disc number
          else if (current_key.equals("Disc Number"))
               current_recording.disc_number = (new Integer(complete_contents)).intValue();
          
          // Store the number of discs in the set
          else if (current_key.equals("Disc Count"))
               current_recording.discs_in_set = (new Integer(complete_contents)).intValue();
          
          // Store the track number
          else if (current_key.equals("Track Number"))
               current_recording.track_number = (new Integer(complete_contents)).intValue();
          
          // Store the totl number of tracks on the album
          else if (current_key.equals("Track Count"))
               current_recording.total_tracks = (new Integer(complete_contents)).intValue();
          
          // Store the year
          else if (current_key.equals("Year"))
               current_recording.year = (new Integer(complete_contents)).intValue();
          
          // Store the genres
          else if (current_key.equals("Genre"))
          {
               if (complete_contents != null)
               {
                    String temp = complete_contents;
                    current_recording.genres = temp.split(" \\+ ");
               }
          }
          
          // Store the duration
          else if (current_key.equals("Total Time"))
               current_recording.duration = (new Integer(complete_contents)).intValue() / 1000;
          
          // Store the comments
          else if (current_key.equals("Comments"))
               current_recording.comments = complete_contents;
          
          // Store the bit rate
          else if (current_key.equals("Bit Rate"))
               current_recording.bit_rate = (new Integer(complete_contents)).intValue();
          
          // Store the date that the recording was added to the
          // iTunes XML
          else if (current_key.equals("Date Added"))
               current_recording.date_added = parseiTunesDate(complete_contents);
     }
     
     
     /**
      * Store the last parsed recording in recordings_so_far and initialize
      * a new empty RecordingMetaData. Call this when all available metadata
      * has been extracted from a recording.
      */
     private void startNewRecording()
     {
          // Add the old recording
          commitCurrentRecording();
          
          // Start a new recording
          current_recording = new RecordingMetaData();
     }
     
     
     /**
      * Add the current recording to recordings_so_far and note if it does not
      * belong to a compilation album. Also assumes is disc 1 of a 1 disc set
      * if disc number is not specified. Mark recording as extracted from iTunes
      * XML.
      */
     private void commitCurrentRecording()
     {
          if (current_recording != null)
          {
               if (current_recording.compilation == -1)
                    current_recording.compilation = 0;
               
               if (current_recording.disc_number == -1)
                    current_recording.disc_number = 1;
               
               if (current_recording.discs_in_set == -1)
                    current_recording.discs_in_set = 1;
               
               current_recording.from_iTunes_XML = true;
               
               recordings_so_far.add(current_recording);
          }
     }
     
     
     /**
      * Parses a date string in the format used in iTunes XML.
      *
      * @param date_string    The string to parse.
      * @return               A Date object set to the date held in date_string.
      */
     private Date parseiTunesDate(String date_string)
     {
          int year = (new Integer(date_string.substring(0, 4))).intValue();
          int month = (new Integer(date_string.substring(5, 7))).intValue();
          int day = (new Integer(date_string.substring(8, 10))).intValue();
          int hour = (new Integer(date_string.substring(11, 13))).intValue();
          int minute = (new Integer(date_string.substring(14, 16))).intValue();
          int second = (new Integer(date_string.substring(17, 19))).intValue();
          
          Calendar calendar = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
          calendar.set(year, month - 1, day, hour, minute, second);
          
          return calendar.getTime();
     }
}
