/*
 * FileMethods.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.staticlibraries;

import java.io.*;
import java.lang.StringBuilder;
import java.util.Vector;
import javax.swing.JOptionPane;


/**
 * A holder class for static methods relating to files.
 *
 * @author Cory McKay
 */
public class FileMethods
{
     /**
      * Gets a new File object to write to based on the given path. If the
      * can_erase parameter is false, then the user is given a warning message
      * through the GUI asking him/er if s/he wishes to overwrite the file.
      * Returns null if the choice is to not overwrite file. Attempts to write
      * an empty string and displays an error message if this cannot be done
      * (also returns null in this case).
      *
      * @param	path          The path to which the file is to be saved.
      * @param	can_erase     Whether or not the file should be automatically
      *                       overwritten if it already exists.
      * @return               Returns the requested file, or null if a fie
      *                       cannot be written to.
      */
     public static File getNewFileForWriting( String path,
          boolean can_erase )
     {
          // Check to see if should overwrite a file
          boolean go_ahead = true;
          File to_file = new File(path);
          if (to_file.exists() && can_erase == false)
          {
               int response = JOptionPane.showConfirmDialog(null, "A file " +
                    "with the path " + path + " already exists.\nDo you wish to overwrite it?", "Warning",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
               if (response != JOptionPane.YES_OPTION)
                    go_ahead = false;
          }

          // Check that can write to file and return appropriate value
          if (go_ahead == true)
          {
               try
               {
                    DataOutputStream writer = getDataOutputStream(to_file);
                    writer.writeBytes("");
                    writer.close();
                    return to_file;
               }
               catch (Exception e)
               {
                    JOptionPane.showMessageDialog(null, "Unable to write file.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    // e.printStackTrace();
                    return null;
               }
          }

          // Return indication that can or should not write to file
          return null;
     }


     /**
      * Prepares a DataOutputStream that can be used to write a file. The useer
      * must remember to close this DataOutputStream after writing is complete.
      *
      * @param      file                     The File that will be written to.
      * @return                              A new DataOutputStream for the
      *                                      given file.
      * @throws     FileNotFoundException    This exception is thrown if the
      *                                      given file cannot be found.
      */
     public static DataOutputStream getDataOutputStream(File file)
     throws FileNotFoundException
     {
          FileOutputStream to = new FileOutputStream(file);
          return new DataOutputStream(to);
     }


     /**
      * Tests the given file to see if it is a valid file.
      *
      * @param file           The file to test.
      * @param need_read      If this is set to true, then an exception is
      *                       sent if the file cannot be read from.
      * @param need_write     If this is set to true, then an exception is
      *                       sent if the file cannot be written to.
      * @return               Value of true if no problems occured during
      *                       file validation.
      * @throws Exception     An informative exception is thrown if there is
      *                       a problem with the file.
      */
     public static boolean validateFile(File file, boolean need_read,
          boolean need_write)
          throws Exception
     {
          if (file == null)
               throw new Exception("Empty file reference provided.");
          if (!file.exists())
               throw new Exception("File " + file.getPath() + " does not exist.");
          if (file.isDirectory())
               throw new Exception("Reference to a directory instead of a file: " + file.getPath() + ".");
          if (!file.isFile())
               throw new Exception("Reference to " + file.getPath() + "is not a valid file.");
          if (need_read && !file.canRead())
               throw new Exception("Cannot read from file " + file.getPath() + ".");
          if (need_write && !file.canWrite())
               throw new Exception("File " + file.getPath() + " cannot be written to.");

          return true;
     }


     /**
      * Returns all files meeting the requirements of the given filter in the
      * given directory and, if requested, its subdirectories.
      *
      * @param directory                The directory to explore.
      * @param explore_subdirectories   Whether or not the subdirectories of
      *                                 directory should be explored.
      * @param filter                   A filter controlling what files are
      *                                 elligible to be returned. A value of
      *                                 null means all files found will be
      *                                 returned.
      * @param results                  Used internally for recursive calls.
      *                                 Pass null when calling this method
      *                                 externally.
      * @return                         An array containing all files meeting
      *                                 the requirements of the other
      *                                 parameters. Null is returned if no
      *                                 files can be found or if the given
      *                                 directory is not a valid directory.
      */
     public static File[] getAllFilesInDirectory(File directory, boolean explore_subdirectories,
          FileFilter filter, Vector<File> results)
     {
          // Ensure that the passed parameter is a valid directory
          if (!directory.isDirectory())
               return null;

          // Initialize the vector that will store found files
          if (results == null)
               results = new Vector<File>();

          // Find elligible files recursively
          File[] in_this_directory = directory.listFiles(filter);
          for (int i = 0; i < in_this_directory.length; i++)
          {
               if (in_this_directory[i].isDirectory())
               {
                    if (explore_subdirectories)
                         getAllFilesInDirectory(in_this_directory[i], explore_subdirectories,
                              filter, results);
               }
               else
                    results.add(in_this_directory[i]);
          }

          // Convert the results to a file array
          File[] results_array = results.toArray(new File[1]);

          // Return null if no elligible files were found
          if (results_array[0] == null)
               return null;

          // Return the results
          return results_array;
     }


     /**
      * Creates a directory at the given path. If a file already exists at the
      * given path, it is deleted. All contents of a directory that already
      * exists at the given path are deleted.
      *
      * <p>If a deletion fails, then the method stops attempting to delete and
      * returns false.
      *
      * @param path      The path at which a directory is to be created.
      * @return          True if the directory was succesfully created. False
      *                   if the directory could not be created or a pre-existing
      *                   item could not be deleted.
      */
     public static boolean createEmptyDirectory(String path)
     {
          File directory = new File(path);

          if (directory.exists())
          {
               boolean success = deleteDirectoryRecursively(directory);
               if (!success)
                    return false;
          }

          return directory.mkdir();
     }


     /**
      * Deletes all files and sub-directories in the given directory. If the
      * given directory is actually a file, then it is just deleted.
      *
      * <p>If a deletion fails, then the method stops attempting to delete and
      * returns false.
      *
      * @param directory The directory to delete.
      * @return          True if the directory and its contents were succesfully
      *                  deleted, false if a failure to delete occured.
      */
     public static boolean deleteDirectoryRecursively(File directory)
     {
          // Delete directory contents recursively
          if (directory.isDirectory())
          {
               String[] children = directory.list();
               for (int i=0; i<children.length; i++)
               {
                    boolean success = deleteDirectoryRecursively(new File(directory, children[i]));
                    if (!success)
                         return false;
               }
          }

          // Delete empty directory or file
          return directory.delete();
     }


     /**
      * Copies the contents of one file to another.
      * Throws an exception if the destination file already exists
      * or if the original file does not exist.
      *
      * @param	original	The name of the file to be copied.
      * @param	destination	The name of the file to be copied to.
      * @throws Exception       An exception is thrown if a problem occurs
      *                         during copying.
      */
     public static void copyFile(String original, String destination)
     throws Exception
     {
          File original_file = new File(original);
          File destination_file = new File(destination);
          if (!original_file.exists())
               throw new Exception("File with path " + original + " does not exist.");
          if (destination_file.exists())
               throw new Exception("File with path " + destination + " already exists.");
          FileReader in = new FileReader(original_file);
          FileWriter out = new FileWriter(destination_file);
          int c;
          while ((c = in.read()) != -1)
               out.write(c);
          in.close();
          out.close();
     }


	 /**
	  * Parses the specified text file into a String. New line characters are
	  * changed into the current system's line separators. The text is otherwise
	  * left unchanged.
	  *
	  * @param to_parse		The file to parse.
	  * @return				The parsed contents of the file
	  * @throws Exception	An informative exception is thrown if a problem occurs.
	  */
	 public static String parseTextFile(File to_parse)
	 	throws Exception
	 {
         // Ensure that the file can be read
         validateFile(to_parse, true, false);

		 // Prepare to read the file
		 StringBuilder contents = new StringBuilder();
		 BufferedReader buffered_input =  new BufferedReader(new FileReader(to_parse));

		 // Read the file
		 try
		 {
			 String line = null; //not declared within while loop
			 while (( line = buffered_input.readLine()) != null)
			 {
				 contents.append(line);
				 contents.append(System.getProperty("line.separator"));
			 }
		 }
		 finally {buffered_input.close();}

		 // Return the parsed results
		 return contents.toString();
	 }


     /**
      * Parses the given text file. The parsed file is considered to comprise a
      * list. Each line is counted as a separate item in the list. Blank lines
      * are treated as an item in the list consisting of "". An array of strings
      * is returned with one entry for each item in the list (i.e. each line).
      * This array is not sorted or otherwise processed, but no entries may be
      * null. A descriptive exception is thrown if a problem occurs during
      * paring.
      *
      * @param      to_parse  The file to parse.
      * @return               The parsed contents of the file.
      * @throws     Exception An informative description of any problem that
      *                       occurs during parsing.
      */
     public static String[] parseTextFileLines(File to_parse)
     throws Exception
     {
          // Ensure that the file can be read
          validateFile(to_parse, true, false);

          // Prepare file reader
          FileReader reader = new FileReader(to_parse);

          // Prepare the file parser
          BufferedReader parser = new BufferedReader(reader);

          // Read lines one by one
          Vector<String> parsed_lines = new Vector<String>();
          String this_line = "";
          while ((this_line = parser.readLine()) != null)
               parsed_lines.add(this_line);

          // Return the parsed results
          return parsed_lines.toArray(new String[1]);
     }
}
