/*
 * ParseTaxonomyFileHandler.java
 * Version 2.2.1
 *
 * Last modified on July 26, 2010.
 * McGill University
 */

package ace.xmlparsers;

import org.xml.sax.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


/**
 * An extension of the Xerces XML DefaultHandler class that implements the
 * SAX ContentHandler. The methods of this class are called by an instance of
 * an XMLReaderwhile it is parsing an XML document.
 *
 * <p>This particular implementation is custom designed to parse XML files of
 * the taxonomy_file type used by the ACE classification system. A custom
 * exception is thrown if the file is not of this type. At the end of parsing,
 * the contents of the file elements are stored in the parsed_file_contents
 * field as a one element array holding a DefaultTreeModel.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class ParseTaxonomyFileHandler
     extends mckay.utilities.xml.ParseFileHandler
{
     /* FIELDS ****************************************************************/


     /*
      * DefaultTreeModel[] parsed_file_contents
      * Holds the data extracted from the XML file.
      */


     /**
      * Whether or not the element currently being parsed is the first in the
      * file.
      */
     private boolean			is_first_element;


     /**
      * Used to store the parsed contents of the file.
      */
     private DefaultMutableTreeNode	current_node;


     /**
      * The element text parsed from the current XML element so far.
     */
     private StringBuffer				element_text_so_far;


     /* PUBLIC METHODS ********************************************************/


     /**
      * This method is called when the start of the XML file to be
      * parsed is reached. Initializes the fields.
      */
     public void startDocument()
     {
          current_node = new DefaultMutableTreeNode("Taxonomy");
          is_first_element = true;
     }


     /**
      * This method is called when the end of the XML file being
      * parsed is reached. Puts the parsed file contents into the
      * parsed_file_contents array in the form of a DefaultTreeModel.
      */
     public void endDocument()
     {
          parsed_file_contents = new DefaultTreeModel[1];
          parsed_file_contents[0] = new DefaultTreeModel(current_node.getRoot());
     }


     /**
      * This method is called when the start of an XML element
      * is encountered. Throws an exception if the XML document
      * is the wrong type of XML document.
      *
      * @param	name		Name of the element that is encountered.
      * @throws	SAXException	Exception thrown if is wrong type of XML file.
      */
     public void startElement(String namespace, String name, String qName, Attributes atts)
     throws SAXException
     {
          // Reset the string buffer
          element_text_so_far = new StringBuffer();

          if (is_first_element == true)
          {
               if (!name.equals("taxonomy_file"))
                    throw new SAXException("\n\nIt is in reality of the type " + name + ".");
               is_first_element = false;
          }
     }


     /**
      * This method is called when the end tag of an XML element
      * is encountered. Moves the tree pointer to the root of the
      * tree if this is the last element. Moves the tree pointer
      * to the parent node of where it is currently pointing if this
      * is not the end tag of a category_name element.
      *
      * @param	name	Name of the element that is encountered.
      */
     public void endElement(String namespace, String name, String qName)
     {
          // Store element text
          String parsed_text = element_text_so_far.toString();

		  // React depending on the type of tag
          if (name.equals("class_name"))
          {
               DefaultMutableTreeNode new_node = new DefaultMutableTreeNode(parsed_text);
               current_node.add(new_node);
               current_node = new_node;
          }
          else if (name.equals("comments"))
               comments = parsed_text;
		  else if (name.equals("taxonomy_file"))
               current_node = (DefaultMutableTreeNode) current_node.getRoot();
          else if (name.equals("sub_class") || name.equals("parent_class"))
               current_node = (DefaultMutableTreeNode) current_node.getParent();
     }


     /**
      * This method creates a new node of the tree and places the
      * textual content of the element in the new node. The tree
      * pointer is then moved to this new child.
      */
     public void characters(char[] ch, int start, int length)
     {
          // The text stored in the element
          String text = new String(ch, start, length);

          // Store the text
          element_text_so_far.append(text);
     }
}