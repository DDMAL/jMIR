/*
 * ParseACEProjectFileHandler.java
 * Version 2.2.1
 *
 * Last modified on July 26, 2010.
 * McGill University
 */

package ace.xmlparsers;

import org.xml.sax.*;
import java.util.LinkedList;


/**
 * An extension of the <i>Xerces</i> <code>XML DefaultHandler</code> class that
 * implements the <i>SAX</i> <code>ContentHandler</code>. The methods of this
 * class are called by an instance of an <code>XMLReader</code> while it is
 * parsing an XML document.
 *
 * <p>This particular implementation is custom designed to parse XML files of
 * the <i>ace_project_file</i> type used by the ACE classification system. A
 * custom exceptions is thrown if the file is not of this type. At the end of
 * parsing, the contents of the file elements are stored in the
 * <i>parsed_file_contents</i> field.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class ParseACEProjectFileHandler
     extends mckay.utilities.xml.ParseFileHandler
{
     /* FIELDS ****************************************************************/


        /* NOTE: LinkedList[] parsed_file_contents:
         * Holds the data extracted from the ACE project file XML file. The data
         * is stored in this order: comments, taxonomy_path,
         * feature_definitions_path, feature_vectors_path,
         * model_classifications_path, gui_preferences_path,
         * classifier_settings_path, trained_classifiers_path and
         * weka_arff_path. The entries corresponding to
         * feature_definitions_path, feature_vectors_path and
         * model_classifications_path are LinkedLists of strings, and the other
         * entrires are single strings.
         */


     /**
      * Counts how many elements have been encountered and read
      */
     private int					count;


     /**
      *The current element being processed
      */
     private	String				current_element;


     /**
      * Stores all of the paths for a given type of file.
      */
     private LinkedList<String>	paths;


     /**
      * The element text parsed from the current XML element so far.
     */
     private StringBuffer				element_text_so_far;


     /* PUBLIC METHODS ********************************************************/


     /**
      * This method is called when the start of the XML file to be parsed is
      * reached. Initializes the parsed_file_contents field to an array of empty
      * strings and initializes the count to 0.
      */
     public void startDocument()
     {
          parsed_file_contents = new LinkedList[9];
          count = 0;
     }


     /**
      * This method is called when the start of an XML element is encountered.
      * Increments the count when this occurs.
      *
      * @param	name		Name of the element that is encountered.
      * @throws	SAXException	Exception thrown if is wrong type of XML file.
      */
     public void startElement(String namespace, String name, String qName, Attributes atts)
     throws SAXException
     {
          // Reset the string buffer
          element_text_so_far = new StringBuffer();

          if (count == 0)
               if (!name.equals("ace_project_file"))
                    throw new SAXException("\n\nIt is in reality of the type " + name + ".");
          count++;

          if (!name.equals("path"))
          {
               current_element = name;
               paths = new LinkedList<String>();
          }
     }


     /**
      * This stores the contents of parsed text elements.
      */
     public void characters(char[] ch, int start, int length)
     {
          // The text stored in the element
          String text = new String(ch, start, length);

          // Store the text
          element_text_so_far.append(text);
     }


     /**
      * This method is called when the end tag of an XML element is encountered.
      *
      * @param	name	Name of the element that is encountered.
      */
     public void endElement(String namespace, String name, String qName)
     {
          // Store element text
          String parsed_text = element_text_so_far.toString();
		  if ( name.equals("comments") || 
				  name.equals("taxonomy_path") ||
				  name.equals("gui_preferences_path") ||
				  name.equals("classifier_settings_path") ||
				  name.equals("trained_classifiers_path") ||
				  name.equals("weka_arff_path") ||
				  name.equals("path") )
			  paths.add(element_text_so_far.toString());

		  // React depending on the type of tag
		  if (name.equals("comments"))
               parsed_file_contents[0] = paths;
          else if (name.equals("taxonomy_path"))
               parsed_file_contents[1] = paths;
          else if (name.equals("feature_definitions_path"))
               parsed_file_contents[2] = paths;
          else if (name.equals("feature_vectors_path"))
               parsed_file_contents[3] = paths;
          else if (name.equals("model_classifications_path"))
               parsed_file_contents[4] = paths;
          else if (name.equals("gui_preferences_path"))
               parsed_file_contents[5] = paths;
          else if (name.equals("classifier_settings_path"))
               parsed_file_contents[6] = paths;
          else if (name.equals("trained_classifiers_path"))
               parsed_file_contents[7] = paths;
          else if (name.equals("weka_arff_path"))
               parsed_file_contents[8] = paths;
     }
}