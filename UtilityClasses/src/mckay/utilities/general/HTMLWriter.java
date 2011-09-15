/*
 * HTMLWriter.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.general;

import java.io.*;
import java.lang.StringBuffer;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import mckay.utilities.staticlibraries.FileMethods;
import mckay.utilities.staticlibraries.StringMethods;


/**
 * Static methods for generating HTML files, either via a DataOutputStream
 * or a StringBuffer.
 *
 * <p>An HTML file is typically created using the startNewHTMLFile method, which
 * returns a DataOutputStream file or a StringBuffer which can be used by
 * various methods to write to the file/buffer. When all contents have been
 * added, then the endHTMLFile method should be used to end the file.
 *
 * <p>A frames base interface consisiting of multiple files can be generated
 * using the startNewFramesPage and the addFrame methods.
 *
 * <p>It can be appropriate to pre-process text passed to the methods of this
 * class with the convertSpecialCharacters method, as this replaces special
 * characters such as ampersands with the appropriate HTML tags.
 *
 * @author Cory McKay
 */
public class HTMLWriter
{
     /**
      * Converts HTML escape characters in the given text to HTML encoding in
      * order to make it HTML consistent.
      *
      * <p>The escape characters converted are:
      *
      * <li> <
      * <li> >
      * <li> "
      * <li> '
      * <li> \
      * <li> &
      *
      * @param to_convert     The text to convert. <b>IMPORTANT:</b> This text
      *                       must not include HTML tags, as they will be
      *                       converted out of HTML.
      * @return               The encoded text.
      */
     public static String convertSpecialCharacters(String to_convert)
     {
          final StringBuffer result = new StringBuffer();
          final StringCharacterIterator iterator = new StringCharacterIterator(to_convert);
          char character =  iterator.current();
          while (character != CharacterIterator.DONE )
          {
               if (character == '<')
                    result.append("&lt;");
               else if (character == '>')
                    result.append("&gt;");
               else if (character == '\"')
                    result.append("&quot;");
               else if (character == '\'')
                    result.append("&#039;");
               else if (character == '\\')
                    result.append("&#092;");
               else if (character == '&')
                    result.append("&amp;");
               else
                    result.append(character);
               
               character = iterator.next();
          }
          return result.toString();
     }
     
     
     /**
      * Converts HTML escape characters in the entries of the given array to
      * HTML encoding in order to make it HTML consistent.
      *
      * <p>The escape characters converted are:
      *
      * <li> <
      * <li> >
      * <li> "
      * <li> '
      * <li> \
      * <li> &
      *
      * @param to_convert     The text to convert. <b>IMPORTANT:</b> This text
      *                       must not include HTML tags, as they will be
      *                       converted out of HTML.
      * @return               The array of encoded text.
      */
     public static String[] convertSpecialCharacters(String[] to_convert)
     {
          String[] temp_array = new String[to_convert.length];
          for (int i = 0; i < to_convert.length; i++)
               temp_array[i] = convertSpecialCharacters(to_convert[i]);
          return temp_array;
     }
     
     
     /**
      * Creates a new HTML file at the given path and gives it a header that
      * includes the given document title and a body tag that sets the text and
      * background to the specified colours. If the include_text parameter is
      * true, then an anchor at the top of the page named "Top" and a large blue
      * title at the top of the page that corresponds to the given
      * document_title parameter are also added. Returns, in either case, a
      * DataOutputStream that can be used to write further code to the HTML file.
      *
      * <p>If the file at the given path already exists and the can_erase
      * parameter is false, then the user is presented with a dialog box
      * asking if it should be overwritten.
      *
      * <p>If the file cannot be written to, then the user is given an error
      * message saying so, and null is returned.
      *
      * @param	path               The path at which the HTML file is to be
      *                            saved.
      * @param  background_colour  The hex code for the background colour.
      *                            (e.g. #e4e4e4 for grey, #000000 for black
      *                            and #FFFFFF for white).
      * @param  text_colour        The hex code for the text colour.
      *                            (e.g. #e4e4e4 for grey, #000000 for black
      *                            and #FFFFFF for white).
      * @param	can_erase          Whether or not the file should be
      *                            automatically overwritten if it already
      *                            exists.
      * @param  document_title     The title to assign to the document in the
      *                            HTML header and to write at the top of the
      *                            page.
      * @param  include_text       Whether or not to include the anchor and the
      *                            title at the top of the page.
      * @return                    Returns a DataOutputStream that can be used
      *                            to write to the file at the given path. Null
      *                            is returned if there is a problem.
      */
     public static DataOutputStream startNewHTMLFile(String path,
          String background_colour,
          String text_colour,
          boolean can_erase,
          String document_title,
          boolean include_text)
     {
          DataOutputStream writer = null;
          File html_file = FileMethods.getNewFileForWriting(path, can_erase);
          if (html_file != null)
          {
               try
               {
                    writer = FileMethods.getDataOutputStream(html_file);
                    
                    String header = "<html>\n<head>\n\t<title>" + document_title + "</title>\n</head>\n\n";
                    String body = "<body text=\"" + text_colour + "\" bgcolor=\"" + background_colour + "\">\n\n";
                    String anchor = "<a name=\"Top\"></a>\n\n";
                    String title = "<h1><font color=\"#0000FF\">" + document_title + "</font></h1>\n\n";
                    
                    writer.writeBytes(header);
                    writer.writeBytes(body);
                    if (include_text)
                    {
                         writer.writeBytes(anchor);
                         writer.writeBytes(title);
                    }
               }
               catch (Exception e)
               {
                    html_file = null;
               }
          }
          
          return writer;
     }
     
     
     /**
      * Creates the beginning of the contents for a new HTML file. Gives it a
      * header that includes the given document title and a body tag that sets
      * the text and background to the specified colours. If the
      * include_text parameter is true, then an anchor at the top of the page
      * named "Top" and a large blue title at the top of the page that
      * corresponds to the given document_title parameter are also added.
      * Returns, in either case, a StringBuffer to which further HTML code
      * can then be written.
      *
      * @param  background_colour  The hex code for the background colour.
      *                            (e.g. #e4e4e4 for grey, #000000 for black
      *                            and #FFFFFF for white).
      * @param  text_colour        The hex code for the text colour.
      *                            (e.g. #e4e4e4 for grey, #000000 for black
      *                            and #FFFFFF for white).
      * @param  document_title     The title to assign to the document in the
      *                            HTML header and to write at the top of the
      *                            page.
      * @param  include_text       Whether or not to include the anchor and the
      *                            title at the top of the page.
      * @return                    Returns a StringBuffer that contains the
      *                            generated HTML and to which further HTML
      *                            may be written.
      */
     public static StringBuffer startNewHTMLFile(String background_colour,
          String text_colour,
          String document_title,
          boolean include_text)
     {
          StringBuffer html_buffer = new StringBuffer("");
          
          String header = "<html>\n<head>\n\t<title>" + document_title + "</title>\n</head>\n\n";
          String body = "<body text=\"" + text_colour + "\" bgcolor=\"" + background_colour + "\">\n\n";
          String anchor = "<a name=\"Top\"></a>\n\n";
          String title = "<h1><font color=\"#0000FF\">" + document_title + "</font></h1>\n\n";
          
          html_buffer.append(header);
          html_buffer.append(body);
          if (include_text)
          {
               html_buffer.append(anchor);
               html_buffer.append(title);
          }
          
          return html_buffer;
     }
     
     
     /**
      * Ends the HTML file referred to by the given DataOutputStream by
      * writing a horizontal rule followed by a link to the anchor at the top
      * of the page followed by the final HTML closing tags.
      *
      * <p>The writer DataOutputStream is also closed.
      *
      * @param      writer         Refers to the HTML file to write to.
      * @param      include_text   Whether or not to include the anchor link and
      *                            the horizontal rule.
      * @throws     IOException    An exception is thrown if a problem occurs
      *                            during writing.
      */
     public static void endHTMLFile(DataOutputStream writer, boolean include_text)
     throws IOException
     {
          String section_end_rule = "<br>\n<hr>\n\n";
          String anchor_link = "<p><tt><a href=\"#Top\">-top of page-</a></tt></p>\n\n";
          String end_tags = "</body>\n</html>\n";
          
          if (include_text)
          {
               writer.writeBytes(section_end_rule);
               writer.writeBytes(anchor_link);
          }
          writer.writeBytes(end_tags);
          
          writer.close();
     }
     
     
     /**
      * Ends the HTML file referred to by the given StringBuffer by
      * writing a horizontal rule followed by a link to the anchor at the top
      * of the page followed by the final HTML closing tags.
      *
      * @param      writer         Refers to the HTML file to write to.
      * @param      include_text   Whether or not to include the anchor link and
      *                            the horizontal rule.
      */
     public static void endHTMLFile(StringBuffer writer, boolean include_text)
     {
          String section_end_rule = "<br>\n<hr>\n\n";
          String anchor_link = "<p><tt><a href=\"#Top\">-top of page-</a></tt></p>\n\n";
          String end_tags = "</body>\n</html>\n";
          
          if (include_text)
          {
               writer.append(section_end_rule);
               writer.append(anchor_link);
          }
          writer.append(end_tags);
     }
     
     
     /**
      * Writes the given paragraph as formatted HTML to the given file.
      *
      * @param  paragraph     The paragraph to wrap in HTML tags and write.
      * @param  writer        Refers to the HTML file to write to.
      * @throws IOException   Throws an exception if cannot write.
      */
     public static void addParagraph(String paragraph, DataOutputStream writer)
     throws IOException
     {
          writer.writeBytes("<p>" + paragraph + "</p>\n\n");
     }
     
     
     /**
      * Writes the given paragraph as formatted HTML to the given StringBuffer.
      *
      * @param  paragraph     The paragraph to wrap in HTML tags and write.
      * @param  writer        Refers to the StringBuffer to write to.
      */
     public static void addParagraph(String paragraph, StringBuffer writer)
     {
          writer.append("<p>" + paragraph + "</p>\n\n");
     }
     
     
     /**
      * Writes a a horizontal rule to the given file.
      *
      * @param  writer        Refers to the HTML file to write to.
      * @throws IOException   Throws an exception if cannot write.
      */
     public static void addHorizontalRule(DataOutputStream writer)
     throws IOException
     {
          writer.writeBytes("<p><hr></p>\n\n");
     }
     
     
     /**
      * Writes a a horizontal rule to the given StringBuffer.
      *
      * @param  writer        Refers to the StringBuffer to write to.
      */
     public static void addHorizontalRule(StringBuffer writer)
     {
          writer.append("<p><hr></p>\n\n");
     }
     
     
     /**
      * Writes the given list as a formatted HTML lsit to the given file.
      *
      * @param  list          The list to write. Each entry corresponds to a
      *                       different item in the list.
      * @param  numbered      Whether or not the list should be numbered.
      * @param  writer        Refers to the HTML file to write to.
      * @throws IOException   Throws an exception if cannot write.
      */
     public static void addList(String[] list, boolean numbered,
          DataOutputStream writer)
          throws IOException
     {
          if (numbered)
               writer.writeBytes("<ol>\n");
          else
               writer.writeBytes("<ul>\n");
          
          for (int i = 0; i < list.length; i++)
               writer.writeBytes("\t<li>" + list[i] + "\n");
          
          if (numbered)
               writer.writeBytes("</ol>\n\n");
          else
               writer.writeBytes("</ul>\n\n");
     }
     
     
     /**
      * Writes the given list as a formatted HTML lsit to the given
      * StringBuffer.
      *
      * @param  list          The list to write. Each entry corresponds to a
      *                       different item in the list.
      * @param  numbered      Whether or not the list should be numbered.
      * @param  writer        Refers to the StringBuffer to write to.
      */
     public static void addList(String[] list, boolean numbered,
          StringBuffer writer)
     {
          if (numbered)
               writer.append("<ol>\n");
          else
               writer.append("<ul>\n");
          
          for (int i = 0; i < list.length; i++)
               writer.append("\t<li>" + list[i] + "\n");
          
          if (numbered)
               writer.append("</ol>\n\n");
          else
               writer.append("</ul>\n\n");
     }
     
     
     /**
      * Writes the given table as formatted HTML to the given file.
      *
      * @param  table              The table to wrap in HTML tags and write. The
      *                            first dimension indicates rows and the second
      *                            indicates columns.
      * @param  column_headings    An optional row of headings to place in the
      *                            first row. Ignored if null.
      * @param  writer             Refers to the HTML file to write to.
      * @throws IOException        Throws an exception if cannot write.
      */
     public static void addTable(String[][] table,
          String[] column_headings,
          DataOutputStream writer)
          throws IOException
     {
          // Begin the table
          writer.writeBytes("<table border=1>\n");
          
          // Write the column headings
          if (column_headings != null)
          {
               writer.writeBytes("\t<tr>\n");
               for (int column = 0; column < column_headings.length; column++)
               {
                    writer.writeBytes("\t\t<td>");
                    writer.writeBytes("<b>" + column_headings[column] + "</b>");
                    writer.writeBytes("</td>\n");
               }
               writer.writeBytes("\t</tr>\n");
          }
          // Write the rows
          for (int row = 0; row < table.length; row++)
          {
               if (table[row] != null)
               {
                    writer.writeBytes("\t<tr>\n");
                    
                    // Write the column entries
                    for (int column = 0; column < table[row].length; column++)
                    {
                         writer.writeBytes("\t\t<td>");
                         if (table[row][column] != null)
                              writer.writeBytes(table[row][column]);
                         writer.writeBytes("</td>\n");
                    }
                    
                    writer.writeBytes("\t</tr>\n");
               }
          }
          
          // End the table
          writer.writeBytes("</table>\n\n");
     }
     
     
     /**
      * Writes the given table as formatted HTML to the given StringBuffer.
      *
      * @param  table              The table to wrap in HTML tags and write. The
      *                            first dimension indicates rows and the second
      *                            indicates columns.
      * @param  column_headings    An optional row of headings to place in the
      *                            first row. Ignored if null.
      * @param  bold_first_column  Whether or not the entries of the first
      *                            column should be in bold.
      * @param  writer             Refers to the StringBuffer to write to.
      */
     public static void addTable(String[][] table,
          String[] column_headings,
          boolean bold_first_column,
          StringBuffer writer)
     {
          // Begin the table
          writer.append("<table border=1>\n");
          
          // Write the column headings
          if (column_headings != null)
          {
               writer.append("\t<tr>\n");
               for (int column = 0; column < column_headings.length; column++)
               {
                    writer.append("\t\t<td>");
                    writer.append("<b>" + column_headings[column] + "</b>");
                    writer.append("</td>\n");
               }
               writer.append("\t</tr>\n");
          }
          // Write the rows
          for (int row = 0; row < table.length; row++)
          {
               if (table[row] != null)
               {
                    writer.append("\t<tr>\n");
                    
                    // Write the column entries
                    for (int column = 0; column < table[row].length; column++)
                    {
                         writer.append("\t\t<td>");
                         if (table[row][column] != null)
                         {
                              if (bold_first_column && column == 0)
                                   writer.append("<b>");
                              writer.append(table[row][column]);
                              if (bold_first_column && column == 0)
                                   writer.append("</b>");
                         }
                         writer.append("</td>\n");
                    }
                    
                    writer.append("\t</tr>\n");
               }
          }
          
          // End the table
          writer.append("</table>\n\n");
     }
     
     
     /**
      * Writes the given table as formatted HTML to the given StringBuffer.
      * 
      * <p>Italicizes and bolds  the highest value in each row. <b>IMPORTANT:</b>
      * Note that this assumes that each entry in the table is a number (except
      * for those entries in the first column, if bold_first_column is set to 
      * true). Rows not containing any numbers are written without any 
      * italicizations/bolds.
      *
      * @param  table              The table to wrap in HTML tags and write. The
      *                            first dimension indicates rows and the second
      *                            indicates columns.
      * @param  column_headings    An optional row of headings to place in the
      *                            first row. Ignored if null.
      * @param  bold_first_column  Whether or not the entries of the first
      *                            column should be in bold. If this is true
      *                            then the first column is considered to hold
      *                            labels and is not considered as a candidate
      *                            for holding the highest value in the row.
      * @param  writer             Refers to the StringBuffer to write to.
      */
     public static void addTableHighlightingHighestInRow(String[][] table,
          String[] column_headings,
          boolean bold_first_column,
          StringBuffer writer)
     {
          // Begin the table
          writer.append("<table border=1>\n");
          
          // Write the column headings
          if (column_headings != null)
          {
               writer.append("\t<tr>\n");
               for (int column = 0; column < column_headings.length; column++)
               {
                    writer.append("\t\t<td>");
                    writer.append("<b>" + column_headings[column] + "</b>");
                    writer.append("</td>\n");
               }
               writer.append("\t</tr>\n");
          }
          // Write the rows
          for (int row = 0; row < table.length; row++)
          {
               if (table[row] != null)
               {
                    writer.append("\t<tr>\n");
                    
                    // Find the column with the highest value
                    int column_to_highlight = -1;
                    if (table[row].length > 1)
                    {
                         // Find the index of the first column to examine
                         int first_column = 0;
                         if (bold_first_column) first_column = 1;
                         
                         // Process entries into array of doubles
                         double[] column_contents = new double[table[row].length - first_column];
                         boolean no_numbers = true;
                         for (int i = 0; i < column_contents.length; i++)
                         {
                              try
                              {
                                   column_contents[i] = (Double.valueOf(table[row][i + first_column])).doubleValue();
                                   no_numbers = false;
                              }
                              catch (NumberFormatException e)
                              {
                                   column_contents[i] = Double.NEGATIVE_INFINITY;
                              }
                         }
                              
                         // Store the index of the highest value
                         if (!no_numbers)
                              column_to_highlight = first_column + mckay.utilities.staticlibraries.MathAndStatsMethods.getIndexOfLargest(column_contents);
                    }
                    
                    // Write the column entries
                    for (int column = 0; column < table[row].length; column++)
                    {
                         writer.append("\t\t<td>");
                         if (table[row][column] != null)
                         {
                              if (bold_first_column && column == 0)
                                   writer.append("<b>");
                              if (column == column_to_highlight)
                                   writer.append("<i><b>");
                              writer.append(table[row][column]);
                              if (column == column_to_highlight)
                                   writer.append("<b></i>");
                              if (bold_first_column && column == 0)
                                   writer.append("</b>");
                         }
                         writer.append("</td>\n");
                    }
                    
                    writer.append("\t</tr>\n");
               }
          }
          
          // End the table
          writer.append("</table>\n\n");
     }     
     
     
     /**
      * Prepares the creation of a frame-based interface consisting of one
      * frame on the left and one on the right (named, respectively, "left" and
      * "right"). The left frame holds a list of links that can be loaded into
      * the right frame.
      *
      * <p>This method automatically creates a root file that defines the frame
      * settings. This file is saved at the path specified in the root_path
      * parameter.
      *
      * <p>A sub-directory is also created with the same name as the root file,
      * but with no extension and "_documents" appended to the name. This
      * sub-directory is meant to eventually hold each of the HTML pages
      * that will be referred to in the left frame. <b>IMPORTANT:</b> If this
      * sub-directory already exists, then it and all of its contents are erased
      * and/or overwritten. A pointer to the new sub-directory is stored in the
      * first entry of the given sub_directory_dummy parameter.
      *
      * <p>A contents file is created in this sub-directory. This file will
      * eventually hold links to the right frame. For now, it is initialized
      * as an HTML file using the startNewHTMLFile method, and a
      * DataOutputStream to it is returned in the first entry of the returned
      * array. A heading is also created for this contents file that is the same
      * asthe root_title parameter, and a link named "Home" is created for the
      * default page that is loaded in the right frame.
      *
      * <p>A reference is also created in the root file to this default pages so
      * that it will be loaded into the right frame. This reference points to a
      * file created in the sub-directory with a file name provided by the
      * title_page_title parameter (with the extension ".html" added). This
      * method initializes this file as an HTML file using the startNewHTMLFile
      * method and returns DataOutputStream for it in the second entry of the
      * returned array.
      *
      * <p>Once this method is called, further pages that can be loaded into
      * the right frame can be added (along with auto-updates to the contents
      * links) using the addFrame method. The default page that is generated
      * here can be updated using the miscellaneous methods of this class.
      *
      * @param	root_path           The path at which the root HTML file for the
      *                             the frames is to be saved.
      * @param  root_title          The title to assign to the root file in the
      *                             HTML header. Also used as the heading of the
      *                             list of contents in the left frame.
      * @param  left_frame_size     The percentage of the frames (1 to 99) to be
      *                             assigned to the left frame.
      * @param  title_page_title    The name to assign to the default page
      *                             that will load in the right frame.
      * @param  sub_directory_dummy A dummy File[] of size 1. The contents of
      *                             this array are ignored, and the first entry
      *                             will be overwritten with a link to the
      *                             sub-directory that will be created.
      * @return                     Returns two DataOutputStreams that can be
      *                             used to write to. The one in entry 0 is the
      *                             contents file that is displayed in the left
      *                             frame, and the one in entry 1 is the
      *                             page that loads by default in the right
      *                             frame. Null is returned if there is a
      *                             problem.
      * @throws Exception           An exception is thrown if any errors occur.
      */
     public static DataOutputStream[] startNewFramesPage(String root_path,
          String root_title,
          int left_frame_size,
          String title_page_title,
          File[] sub_directory_dummy)
          throws Exception
     {
          // Prepare the array to return
          DataOutputStream array_to_return[] = new DataOutputStream[2];
          
          // Create the sub-directory, erasing any existing contents if necessary
          String subdirectory_path = getFrameSubdirectoryPath(root_path);
          boolean success = FileMethods.createEmptyDirectory(subdirectory_path);
          if (!success)
               throw new Exception("Could not create sub-directory to hold HTML files.\nThis may be because this path is currently protected.\n\nTry saving using a different name.");
          sub_directory_dummy[0] = new File(subdirectory_path);
          
          // Create the title page file
          String title_page_path = getDefaultPageFramePath(root_path, title_page_title);
          array_to_return[1] = startNewHTMLFile(title_page_path, "#e4e4e4", "#000000", true, title_page_title, true);
          if (array_to_return[1] == null)
               throw new Exception("Could not save the default HTML file.");
          
          // Create the contents file
          String contents_file_path = getContentsFramePath(root_path);
          File contents_file = FileMethods.getNewFileForWriting(contents_file_path, true);
          if (contents_file == null)
               throw new Exception("Could not save the contents HTML page.");
          array_to_return[0] = FileMethods.getDataOutputStream(contents_file);
          array_to_return[0].writeBytes("<html>\n<head>\n\t<title>Contents Directory</title>\n</head>\n\n");
          array_to_return[0].writeBytes("<body bgcolor=\"#000000\" text=\"#FFFFFF\" link=\"#FFFFFF\" vlink=\"#FFFFFF\" alink=\"#FFFFFF\">\n\n");
          array_to_return[0].writeBytes("<font size=+1>Contents</font><br>\n");
          array_to_return[0].writeBytes("<font size=+1>Directory</font>\n\n");
          array_to_return[0].writeBytes("<p><font size=-1><font color=\"#FFFFFF\"><a href=\"" + title_page_title + ".html\" target=\"right\">Home</a></font></font></p>\n\n");
          
          // Write the root file
          File root_file = FileMethods.getNewFileForWriting(root_path, true);
          if (root_file == null)
               throw new Exception("Could not save the root HTML page.");
          DataOutputStream root_writer = FileMethods.getDataOutputStream(root_file);
          String header = "<html>\n<head>\n\t<title>" + root_title + "</title>\n</head>\n\n";
          String frameset_top = "<frameset cols=\"" + left_frame_size + "%, *\", border=no>\n";
          String contents_frame = "\t<frame src=\"" + sub_directory_dummy[0].getName() + "/Contents.html\", name=left>\n";
          String title_page_frame = "\t<frame src=\"" + sub_directory_dummy[0].getName() + "/" + title_page_title + ".html\", name=right>\n";
          String frameset_bottom = "</frameset>\n\n";
          String ender = "</html>\n";
          root_writer.writeBytes(header);
          root_writer.writeBytes(frameset_top);
          root_writer.writeBytes(contents_frame);
          root_writer.writeBytes(title_page_frame);
          root_writer.writeBytes(frameset_bottom);
          root_writer.writeBytes(ender);
          root_writer.close();
          
          // Return the results
          return array_to_return;
     }
     
     
     /**
      * Adds a new HTML page to a frames-based interface, as prepared with the
      * startNewFramesPage method. This method adds a page that can be loaded
      * into the right frame (se up using the startNewHTMLFile method), and adds
      * a link to it on the contents page in the left frame.
      *
      * @param  new_file_title          The title of the new file. This is
      *                                 stored in the HTML header and is written
      *                                 in large blue type at the top of the
      *                                 page.
      * @param  directory               The directory where the new file is to
      *                                 be saved. Is typically the same
      *                                 directory that the sub_directory_dummy
      *                                 parameter of the startNewHTMLFile method
      *                                 is filled with.
      * @param  contents_file_stream    A stream to the contents frame. A link
      *                                 will be added to this frame that links
      *                                 to the new file prepared by this method.
      * @return                         A stream to the new file that has been
      *                                 created.
      * @throws Exception               Throws an exception if a problem occurs.
      */
     public static DataOutputStream addFrame(String new_file_title,
          File directory,
          DataOutputStream contents_file_stream)
          throws Exception
     {
          // Create the new file
          String file_path = directory.getAbsoluteFile() + "/" + new_file_title + ".html";
          DataOutputStream writer = startNewHTMLFile(file_path, "#e4e4e4", "#000000", true, new_file_title, true);
          if (writer == null)
               throw new Exception("Could not generate HTML file:\n" + file_path);
          
          // Generate the link to this new file
          contents_file_stream.writeBytes("<p><font size=-1><font color=\"#FFFFFF\"><a href=\"" + new_file_title + ".html\" target=\"right\">" + new_file_title + "</a></font></font></p>\n\n");
          
          // Return the writer for the new file
          return writer;
     }
     
     
     /**
      * Get the path of the subdirectory that is created by the
      * startNewFramesPage method.
      *
      * @param  root_path     The path of the root frame file.
      * @return               The path of the subdirectory.
      * @throws Exception     Throws an exception if the root path is invalid.
      */
     public static String getFrameSubdirectoryPath(String root_path)
     throws Exception
     {
          String root_path_no_extension = StringMethods.removeExtension(root_path);
          if (root_path_no_extension == null)
               throw new Exception("Root file must have an extension.");
          String subdirectory_path = root_path_no_extension + "_documents";
          return subdirectory_path;
     }
     
     
     /**
      * Get the path of the left frame holding links to the frames that is
      * created by the startNewFramesPage method.
      *
      * @param  root_path     The path of the root frame file.
      * @return               The path of the contents file.
      * @throws Exception     Throws an exception if the root path is invalid.
      */
     public static String getContentsFramePath(String root_path)
     throws Exception
     {
          String subdirectory_path = getFrameSubdirectoryPath(root_path);
          String contents_path = subdirectory_path + "/" + "Contents.html";
          return contents_path;
     }
     
     
     /**
      * Get the path of the default page for the right frame that is created by
      * the startNewFramesPage method.
      *
      * @param  root_path          The path of the root frame file.
      * @param  title_page_title   The title of the default page.
      * @return                    The path of the subdirectory.
      * @throws Exception          Throws an exception if the root path is
      *                            invalid.
      */
     public static String getDefaultPageFramePath(String root_path,
          String title_page_title)
          throws Exception
     {
          String subdirectory_path = getFrameSubdirectoryPath(root_path);
          String default_page_path = subdirectory_path + "/" + title_page_title + ".html";
          return default_page_path;
     }
     
     
     /**
      * Returns an array consisting of the given array but with the second
      * dimension projected into one entry separated by <br> tags. The
      * convertSpecialCharacters method is also called on each element.
      *
      * @param to_process     The array to project to [n][1] size with line
      *                       breaks.
      * @return               The new array.
      */
     public static String[][] addLineBreaksToArray(String[][] to_process)
     {
          String[][] list = new String[to_process.length][1];
          for (int i = 0; i < list.length; i++)
          {
               String list_so_far = "";
               for (int j = 0; j < to_process[i].length; j++)
               {
                    if (j != 0) list_so_far += "<br>";
                    list_so_far += convertSpecialCharacters(to_process[i][j]);
               }
               list[i][0] = list_so_far;
          }
          return list;
     }
}
