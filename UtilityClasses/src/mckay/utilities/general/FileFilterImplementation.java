/*
 * FileFilter.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.general;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * A file filter for the JFileChooser class. Implements the two methods of the
 * FileFilter abstract class.
 *
 * <p>Filters all files except directories and files that end with the given 
 * extension. Case is ignored.
 *
 * @author Cory McKay
 */
public class FileFilterImplementation
     extends FileFilter
     implements java.io.FileFilter
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The extensions that are allowed to pass through the filter.
      */
     private String[]    extensions;
     
     
     /**
      * A list of the file types that are allowed to pass through the filter.
      */
     private String      description;
     
     
     /* CONSTRUCTOR ***********************************************************/
     
     
     /**
      * Instantiate the FileFilterImplementation object to let files with the
      * specified extension(s) pass.
      *
      * @param extensions The extension(s) that this filter will allow to pass.
      *                   Case is irrelevant, and the period should be omitted.
      *                   For example, {"jpg", "jpeg"} should be passed if this
      *                   is to apply to JPEG files. None of these may be null.
      */
     public FileFilterImplementation(String[] extensions)
     {
          this.extensions = new String[extensions.length];
          for (int i = 0; i < extensions.length; i++)
          {
               this.extensions[i] = "." + extensions[i].toLowerCase();
               
               if (i == 0)
                    description = new String(this.extensions[0]);
               else if (i == extensions.length - 1)
                    description += " and " + this.extensions[i];
               else
                    description += ", " + this.extensions[i];
          }
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Returns whether the given file should pass through the filter.
      *
      * @param f    The File to be tested.
      * @return     True if the File is a directory or has an acceptable
      *             extension, false otherwise.
      */
     public boolean accept(File f)
     {
          if (f.isDirectory())
               return true;
          
          for (int i = 0; i < extensions.length; i++)
               if (f.getName().toLowerCase().endsWith(extensions[i]))
                    return true;
          
          return false;
     }
     
     
     /**
      * Returns a formatted list describing the file extensions that can
      * pass the filter.
      *
      * @return     The formatted list.
      */
     public String getDescription()
     {
          return description;
     }
}
