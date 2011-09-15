/*
 * UnknownFileHandler.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.xmlparsers;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A class for parsing unidentified XML files.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class UnknownFileHandler
        extends DefaultHandler
{
    int count;

    public void startDocument()
    {
        count = 0;
    }

    public void startElement(String namespace, String name, String qName, Attributes atts)
            throws SAXException
    {
        if (count == 0)
        {
            if (name.equals("taxonomy_file"))
                throw new SAXException("taxonomy_file");
            else if (name.equals("feature_key_file"))
                throw new SAXException("feature_key_file");
            else if (name.equals("feature_vector_file"))
                throw new SAXException("feature_vector_file");
            else if (name.equals("classifications_file"))
                throw new SAXException("classifications_file");
            else if (name.equals("ace_project_file"))
                throw new SAXException("project_file");
            else
                throw new SAXException("unknown_file");
        }
        count++;
    }
}
