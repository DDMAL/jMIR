/*
 * XMLDocumentParser.java
 * Version 2.2
 *
 * Last modified on July 26, 2010.
 * McGill University
 */

package ace.xmlparsers;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.File;


/**
 * A holder class for the XMLDocumentParser</code> method. This method is a
 * general purpose method for loading an XML file, testing that the file exists,
 * validating it as a valid XML file, ensuring that it is of the correct type,
 * parsing it and extracting its data into the required form. Informative error
 * exceptions are thrown in a format that can be displayed directly to users.
 *
 * <p>Custom handlers can be written to properly extract information from
 * arbitrary XML files. The types of files currently implemented are:
 * feature_vector_file, feature_key_file, taxonomy_file and
 * classifications_file. See the file handlers for each of these file types for
 * more information on the kind of data returned.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class XMLDocumentParser
{
     /**
      * This method is a general purpose method for loading an XML
      * file, testing that the file exists, validating it as a valid
      * XML file, ensuring that it is of the correct type, parsing
      * it and extracting its data into the required form. Informative
      * error exceptions are thrown in a format that can be displayed
      * directly to users.
      *
      * <p>Custom handlers can be written to properly extract
      * information from arbitrary XML files. The types of files
      * currently implemented are: feature_vector_file, feature_key_file,
      * taxonomy_file and classifications_file. See the file handlers for
      * each of these file types for more information on the kind of data
      * returned.
      *
      * @param	file_path	The path of an XML file that will be parsed.
      * @param	document_type	The type of XML file. Defined by the name
      *				of the first element in the file.
      * @return			An array of objects containing information
      *				extracted from the XML file. Object types
      *				depend on type of document parsed.
      * @throws	Exception	Informative exceptions are thrown if an
      *				invalid file path is specified.
      */
     public static Object parseXMLDocument(String file_path, String document_type)
     throws Exception
     {
          // Verify that the file referred to in file_path exists and is not a
          // directory

          File test_file = new File(file_path);
          if (!test_file.exists())
               throw new Exception("The specified path " + file_path + " does not refer to an existing file.");
          if (test_file.isDirectory())
               throw new Exception("The specified path " + file_path + " refers to a directory, not to a file.");

          // Prepare the XML parser with the validation feature on and the error
          // handler set to throw exceptions on all warnings and errors
          XMLReader reader = new SAXParser();
          reader.setFeature("http://xml.org/sax/features/validation", true);
          reader.setErrorHandler(new mckay.utilities.xml.ParsingXMLErrorHandler());
          mckay.utilities.xml.ParseFileHandler handler;

          // Choose the correct type handler based on the type of XML file
          if (document_type.equals("feature_vector_file"))
               handler = new ParseDataSetFileHandler();
          else if (document_type.equals("feature_key_file"))
               handler = new ParseFeatureDefinitionsFileHandler();
          else if (document_type.equals("taxonomy_file"))
               handler = new ParseTaxonomyFileHandler();
          else if (document_type.equals("classifications_file"))
               handler = new ParseClassificationsFileHandler();
          else if (document_type.equals("ace_project_file"))
               handler = new ParseACEProjectFileHandler();

          // Throw an exception if an unknown type of XML file is specified
          else throw new Exception(new String("Invalid type of XML file specified. The XML file type " + document_type + " is not known."));

          // Parse the file so that the contents are available in the
          // parsed_file_contents field of the handler
          reader.setContentHandler(handler);
          try
          {reader.parse(file_path);}
          catch (SAXParseException e) // throw an exception if the file is not a valid XML file
          {
               throw new Exception("The " + file_path + " file is not a valid XML file.\n\nDetails of the problem: " + e.getMessage() +
                    "\n\nThis error is likely in the region of line " + e.getLineNumber() + ".");
          }
          catch (SAXException e) // throw an exception if the file is not an XML file of the correct type
          {
               throw new Exception("The " + file_path + " file must be of type " + document_type + "." + e.getMessage());
          }
          catch (Exception e) // throw an exception if the file is not formatted properly
          {
               throw new Exception("The " + file_path + " file is not formatted properly.\n\nDetails of the problem: " + e.getMessage());
          }

          // Return the contents of the parsed file
          return handler.parsed_file_contents;
     }
     /**
      * Returns the file type of the given file.
      * <p> Recogonized file types include:
      * <ul>
      * <li>ACE XML taxonomy file - "taxonomy_file" is returned
      * <li>ACE XML feature definitions file - "feature_key_file" is returned
      * <li>ACE XML feature vector file - "feature_vector_file" is returned
      * <li>ACE XML model classifications file - "classifications_file" is returned
      * <li>ACE project file - (This includes accompanying project.sp file) "project_file" is returned
      * </ul>
      *
      * @param file_path        The file whose type we wish to discover.
      * @return                 The type of the give file. If the file is not found
      *                         to be one of the file types listed above, "unknown"
      *                         will be returned.
      * @throws Exception       If an error occurs while parsing file.
      */
    public static String getFileType(String file_path)
            throws Exception
    {
        String type = "unknown";
        // Check extension, only attempt to parse if XML file
        String ext = mckay.utilities.staticlibraries.StringMethods.getExtension(file_path);

        if (file_path.endsWith(".sp"))
        {
            return "special_file";
        }
        else if (ext == null || !ext.equals(".xml"))
            return type;


        // Prepare the XML parser with the validation feature on and the error
        // handler set to throw exceptions on all warnings and errors
        XMLReader reader = new SAXParser();
        reader.setFeature("http://xml.org/sax/features/validation", true);
        reader.setErrorHandler(new mckay.utilities.xml.ParsingXMLErrorHandler());
        UnknownFileHandler handler = new UnknownFileHandler();

        // Parse the file so that the contents are available in the
        // parsed_file_contents field of the handler
        reader.setContentHandler(handler);

        try
        {
            reader.parse(file_path);
        }
        catch (SAXException saxe)
        {
            // Check for each file type
            String message = saxe.getMessage();
            if (!(message.equals("taxonomy_file")) && !(message.equals("feature_key_file"))
                    && !(message.equals("feature_vector_file")) && !(message.equals("classifications_file"))
                    && !(message.equals("project_file")) && !(message.equals("unknown_file"))&& !(message.equals("special_file")))
                throw saxe;
            else
                return message;
        }
        return type;
    }
}
