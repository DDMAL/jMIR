/*
 * FileSaver.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.general;

import java.io.*;


/**
 * Objects of this class are used by ResultDisplayPanels (or other classes) to
 * perform actual saving of data in a variety of formats. This implementation
 * only allows the saving of html files, but this class can be extended and the
 * getFileFormatExtension and saveContents methods overridden to allow a greater
 * variety of file formats.
 *
 * @author Cory McKay
 */
public class FileSaver
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The extensiosn of the file formats that may be saved by this class.
      */
     private final String[] available_formats = {"html"};
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Provides an array of the extensions of file formats that may be saved
      * with this object.
      *
      * @return  The available file extensions.
      */
     public String[] getFileFormatExtension()
     {
          return available_formats;
     }
     
     
     /**
      * Save the given text to the given location in the given format.
      *
      * @param chosen_file_extension The file extension (corresponding to one
      *                              of the extensions published by the
      *                              getFileFormatExtension method) to use when
      *                              saving data_to_save, and the corresponding
      *                              file format.
      * @param data_to_save          The content to save.
      * @param save_location         The file to save data_to_save to.
      * @throws Exception            Throws an Exception if the file cannot be
      *                              saved.
      */
     public void saveContents(String chosen_file_extension, String data_to_save,
          File save_location)
          throws Exception
     {
          if (chosen_file_extension.equals("html"))
          {
               DataOutputStream writer = mckay.utilities.staticlibraries.FileMethods.getDataOutputStream(save_location);
               writer.writeBytes(data_to_save);
               writer.close();
          }
     }
}