/*
 * ParseACEZipFile.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */
package ace.xmlparsers;

import ace.datatypes.Project;
import java.io.*;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;

/**
 * Contains methods to create, edit, parse, and manage ACE zip files. ACE zip files
 * contain the componant XML files of an ACE project.
 *
 * <p>This class uses the java.util.zip package to compress and decompress the componant files of an ACE project.
 * A special file called project.sp is used internally to specify the path to the
 * ACE XML project file and both are included in the zip file.
 * The ACE XML project file will specify the pathnames to the ACE XML files that were compressed in the zip file.
 * It is with the ACE XML project file that one is able to associate the different files of the extracted zip file
 * with the different types of ACE datatypes/XML files (Taxonomy, Feature Key, Feature vectors, and etc).
 * This ACE XML project file will be written automatically.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class ParseACEZipFile {


    /*PUBLIC METHODS *************************************/

    /**
     * Coordinates the the extraction of an ACE zip file.
     * <p> After the zip file is extracted, a new ACE XML project file is written
     * specifying the paths to the newly extracted files.
     *
     *
     * @param zipfile              Specifies the path to the zip file that the user
     *                             wishes to extract
     * @param destination          Specifies the path to the folder to which the
     *                             contents of the zip file will be extracted
     * @return project_file_path   Specifies the path to the ACE XML project file
     *                             after extraction (not absolute).
     * @throws Exception           If an error occurs.
     */
    public static String parseZip(String zipfile, String destination)throws Exception
    {
        // Extracts the specified zip file into the specified directory
        boolean can_erase = false;
        //File dest =  mckay.utilities.staticlibraries.FileMethods.getNewDirectory(destination, can_erase);
        File zip = new File (zipfile);
        //dest.mkdir();
        //String fullpath = dest.getAbsolutePath();
//System.out.println("fullpath: " + fullpath);
        String zippath = zip.getAbsolutePath();
        decompress(zippath, destination, can_erase);
        String project_file_path = parseSp(destination);

        /* Creates a new Project object and changes the file paths specified in the
        ACE XML project file to represent the extracted files in their new directory.*/
        Project newpaths = new Project();
        newpaths.parseProjectFile(project_file_path);
        newpaths.saveUnzippedProjectFile(destination, project_file_path);

        return project_file_path;
    }

    /**Saves an ACE project in a zip file
     *
     * <p> Given the paths to all componant ACE XML files, this method combines
     * all files into a compressed Zip file. An ACE XML project file will written
     * to specify the given file paths and will be included in the zip file. All
     * files to be included in the zip file are combined into one array before being
     * passed to method <code>compress</code>.
     *
     * @param taxonomy_path         Specifies the path to the taxonomy file for
     *                              the ACE project.
     * @param classification_paths  Specifies the path(s) to the classification
     *                              file(s) for the ACE project.
     * @param feature_defs_paths    Specifies the path(s) to the feature definitions/key
     *                              file(s) for the ACE project.
     * @param data_set_paths        Specifies the path(s) to the data set/feature
     *                              vectors file(s) for the ACE project.
     * @param other_paths           Specifies the path(s) to any other (non ACE
     *                              XML) files that the user wishes to compress.
     * @param zipfile               The name of the ACE zip file to be created.
     * @param project_file          The name of the ACE project file to be contained
     *                              within the ACE zip file to be created. This
     *                              will be the name of the ACE project file that
     *                              was given in the list of files to be zipped,
     *                              or, if no project file was given, the new
     *                              project file will have the same name as the
     *                              zipfile except with ".xml" as the extension.
     * @throws Exception            If no files are specified to be zipped.
     */
    public static void saveZip(String taxonomy_path, String feature_defs_paths[], String data_set_paths[],
                        String classification_paths[], String[] other_paths, String zipfile, String project_file)
                        throws Exception
    {
        //The project file and the zip file will both have the same name but with different extensions

        /*if (mckay.utilities.staticlibraries.StringMethods.getExtension(project_name) != null)
        {
            // remove extension if present
            project_name = mckay.utilities.staticlibraries.StringMethods.removeExtension(project_name);
        }
        zipfile = project_name + ".zip";
        project_file = project_name + ".xml";*/
        if(project_file == null)
            project_file = mckay.utilities.staticlibraries.StringMethods.removeExtension(zipfile) + ".xml";
        else
        {
            // Remove directory structure if present. Only name of project file will remain
            int sep = project_file.lastIndexOf(File.separator);
            if(sep > -1 && sep < project_file.length())
                project_file = project_file.substring(sep+1);
        }

        // will be used later to specify how many files are to be zipped
        int array_length = 2;
        boolean ace_xml = true;

        /* If each are not null, add length of each individual array to array_length
           reserve 2 spots for the .sp file and the ACE XML project file ONLY when
           ACE XML files are being saved
         */

        if(taxonomy_path != null)
            array_length++;
        if(classification_paths != null)
            array_length += classification_paths.length;

        if(feature_defs_paths != null)
            array_length += feature_defs_paths.length;
        if(data_set_paths != null)
            array_length += data_set_paths.length;

        if(other_paths != null)
            array_length += other_paths.length;

        //if all are null, throw exception
        //if array_length is still 2, the user hasn't specified any files to zip, throw exception
        if(array_length == 2)throw new Exception ("No files were specified to be zipped!");

        // Decrease array size by 2 because there is no need for project file or .sp file
        if (other_paths != null && taxonomy_path == null && classification_paths == null && feature_defs_paths == null && data_set_paths == null)
        {
            array_length -= 2;
            ace_xml = false;
        }

        //fill filesToZip with the path names of all files to be zipped
        String[] filesToZip = new String[array_length];
        int current = 0;
        if(taxonomy_path != null)
        {
            filesToZip[0] = taxonomy_path;
            current++;
        }
        if(classification_paths != null)
        {
            System.arraycopy(classification_paths, 0, filesToZip, current, classification_paths.length);
            current += classification_paths.length;
        }
        if(feature_defs_paths != null)
        {
            System.arraycopy(feature_defs_paths, 0, filesToZip, current, feature_defs_paths.length);
            current += feature_defs_paths.length;
        }
        if(data_set_paths != null)
        {
            System.arraycopy(data_set_paths, 0, filesToZip, current, data_set_paths.length);
            current += data_set_paths.length;
        }
        if (other_paths != null)
        {
            System.arraycopy(other_paths, 0, filesToZip, current, other_paths.length);
        }

        // Only create project file and .sp file if ACE XML are present.
        File projfile = null;
        File special = null;
        if (ace_xml)
        {

            /* Create new project file and delete previous project file.
            Put it's pathname in filesToZip and then delete file after compression.*/
            Project proj = new Project(project_file, null, taxonomy_path, feature_defs_paths, data_set_paths,
                    classification_paths, null,
                    null, null, null);
            String project_path = proj.saveProjectFile(project_file, true);
            filesToZip[filesToZip.length - 2] = project_path;
            projfile = new File(project_path);// Create File object so that it can be deleted


            // Create project.sp file and add its path to fileToZip, then delete after compression
            filesToZip[filesToZip.length - 1] = writeSp(project_file);

            // Test to make sure first line of .sp file is correct
            /*File test = new File(filesToZip[2]);
            BufferedReader read = new BufferedReader(new FileReader (test));
            System.out.println("first line: " + read.readLine());*/

        }

        /* Copy all files to current directory to avoid saving directory structure in zip file.
         These are temporary files and will be deleted after compression. Keep track
         of whether or not the files specified to be zipped are already in the current
         directory. Only files that are copied to the current directory should be
         deleted after compression, not the files that were there to begin with.*/
        // Array to keep track of location of files, one cell per file, true if
        // file is located in current directory, false otherwise
        boolean[] pwd = new boolean[filesToZip.length];
        String sep = File.separator;
        for(int i = 0; i<(filesToZip.length)&&filesToZip[i]!=null; i++)
        {
            pwd[i] = true;
            if (filesToZip[i].contains(sep))
            {
                int index = filesToZip[i].lastIndexOf(sep);
                if (index > 0)
                {
                    String outfile = filesToZip[i].substring(index);
                    File outFile = new File(outfile);
                    if (!outFile.exists())
                    {
                        pwd[i] = false;
                        mckay.utilities.staticlibraries.FileMethods.copyFile(filesToZip[i], outfile);

                    }
                    else
                        pwd[i] = true;
                    filesToZip[i] = outfile;
                }
            }
        }
        //compress all files.
        compress(filesToZip, zipfile);

        //delete all temp files
        if (ace_xml)
        {
            special = new File("project.sp"); // Create File object so that it can be deleted
            projfile.delete();
            special.delete();
        }
        for(int i = 0; i<(filesToZip.length)&&filesToZip[i]!=null; i++)
        {
            if(!pwd[i])
            {
                File temp = new File(filesToZip[i]);
                temp.delete();
            }
        }
    }

    /**
     * Given the path to an ACE XML project file and the name of this ACE project,
     * this method retrieves the paths to the componant ACE XML files contained in
     * the project file and then calls the previous saveZip method to save them all
     * in a zip file.
     *
     * @param project_path          Specifies the path to the ACE XML Project file
     * @param zip_path				Specifies the path of the zip file.
     * @throws java.lang.Exception  If an error occurs.
     */
    public static void saveZip (String project_path, String zip_path)throws Exception
    {
        //create project
        String[] other_paths = null;
        Project save = new Project();
        save.parseProjectFile(project_path);
        saveZip(save.taxonomy_path, save.feature_settings_paths, save.feature_vectors_paths, save.classification_paths, other_paths, zip_path, project_path);

    }

    /**
     * Saves all of the given files into a compressed zipfile. In this version of
     * saveZip, the file types of the files to be compressed are originally unknown,
     * so the file type of each specified file must be discovered before the ACE
     * XML project file can be written and the files can be compressed into an
     * ACE zip file.
     * <p>Concerning project files: If a single project file is specified to be
     * compressed into an ACE zip file, all the ACE XML files that are referenced
     * in the specified ACE project file will also be included in the zip file.
     * On the other hand, if an ACE project file is included in a list of files
     * to be compressed into an ACE zip file, it will be assumed that this ACE
     * zip file contains references to the other ACE XML files that were specified
     * to be compressed, in which case the original ACE project file will be deleted
     * as a new one will be written and included in the ACE zip file. If an ACE
     * project file is specified to be zipped along with any other file, it will
     * be discarded and the ACE XML files that it refrences will nott be accessed.
     *
     * @param files2        The files to be compressed. This may contain only one
     *                      single ACE project file in which case the ACE XML files
     *                      associated with that ACE project file will be compressed
     *                      in the ACE zip file.
     * @param name          The name of the zipfile into which the files will be saved.
     * @throws Exception    If more than one ACE XML taxonmy files are specified
     *                      to be zipped in the same file or if only one non-project
     *                      file was specified to be included in the zip file.
     */
    public static void saveZip(String[] files2, String name)
            throws Exception
    {
        // Get any files that were contained in given directories
        String[] files = getFiles(files2);

        // Initialize variables
        String project = null;
        String taxonomy_path = null;
        LinkedList <String> feature_definitions_paths = new LinkedList<String>();
        LinkedList <String> feature_vectors_paths = new LinkedList<String>();
        LinkedList <String> classifications_paths = new LinkedList<String>();
        LinkedList <String> other_paths = new LinkedList<String>();

        for (int i = 0; i < files.length; i++)
        {
            // For each file, determine its filetype
            String type = XMLDocumentParser.getFileType(files[i]);
            if(type.equals("taxonomy_file"))
            {
                if(taxonomy_path != null)
                    throw new Exception ("Only one ACE XML taxonomy file may be included in an ACE zip file.");
                else
                    taxonomy_path = files[i];
            }
            else if (type.equals("feature_vector_file"))
            {
                feature_vectors_paths.add(files[i]);
            }
            else if (type.equals("feature_key_file"))
            {
                feature_definitions_paths.add(files[i]);
            }
            else if (type.equals("classifications_file"))
            {
                classifications_paths.add(files[i]);
            }
            else if (type.equals("project_file"))
            {
                project = files[i];
            }
            else
            {
                other_paths.add(files[i]);
            }
        }

        // If project file was the only file specified to be saved, save that project
        if(project != null && files.length == 1)
            saveZip(project, name);

        // Convert to arrays
        String[] fdefs_paths = feature_definitions_paths.toArray(new String[0]);
        String[] fvec_paths = feature_vectors_paths.toArray(new String[0]);
        String[] class_paths = classifications_paths.toArray(new String[0]);
        String[] oth_paths = other_paths.toArray(new String[0]);

        // Set to null if no files of that type were specified
        if (fdefs_paths.length == 0)
            fdefs_paths = null;
        if (fvec_paths.length == 0)
            fvec_paths = null;
        if (class_paths.length == 0)
            class_paths = null;
        if (oth_paths.length == 0)
            oth_paths = null;

        if(files.length > 1 || project == null)
            saveZip(taxonomy_path, fdefs_paths, fvec_paths, class_paths, oth_paths, name, project);
        else
            throw new Exception ("Please specify more than one file or an ACE project file to include in the ACE zip file.");
    }

    /**
     * Extracts the specified file(s) from the given zip file.
     * <p>If file_type is not null, all files of that file type contained in the
     * zip file will be extracted. Otherwise, the single file specified by file_name
     * will be extracted.
     *
     * @param file_name     If file_type is null, this specifies the name of the
     *                      single file to be extracted from the zip_file.

     * @param zip_file      Specifies the path to the zip file containing the file(s) to be extracted
     * @param file_type     Can be either "project_file", "taxonomy_file", "feature_key_file", "feature_vector_file",
     *                      or "classifications_file". If filetype is not one of these
     *                      Strings, an exception will be thrown.
     * @param destpath      Specifies the directory into which the file(s) should
     *                      be extracted.
     * @return              The array of paths to the specified filetype.
     *                      example: if file type is "feature_vectors_file", this method will return the array
     *                      of paths to all the feature vectors specified in this ACE project zip file.
     *                      If filetype is "taxonomy_file" (a project may only have one taxonomy)
     *                      or if only a single file was specified to be extracted,
     *                      an array of size 1 will be returned.
     * @throws Exception    If incorrect filetype is specified
     */
    public static String[] extract(String file_name, String zip_file, String file_type, String destpath)
            throws Exception
    {
        String[] files_to_extract = null;
        try
        {
            File zipfile = new File (zip_file);
            File unzipDestinationDirectory = new File (destpath);
            unzipDestinationDirectory.mkdir();

            //Open zip file for reading
            ZipFile zipFile = new ZipFile(zipfile, ZipFile.OPEN_READ);
            if (file_type != null)
            {
                if (file_type.equals("project_file"))
                {
                    String project_file;
                    project_file = getProjectFile(zipFile);
                    files_to_extract = new String[1];
                    files_to_extract[0] = project_file;
                }
                else if (file_type.equals("taxonomy_file")||file_type.equals("feature_key_file")
                        ||file_type.equals("feature_vector_file")||file_type.equals("classifications_file"))
                {
                    // get all files of that file type
                    // extract and parse project file
                    String[] extracted = extract(null, zip_file, "project_file", destpath);
                    String proj_path = null;
                    if(extracted != null && extracted.length > 0 && extracted[0] != null )
                        proj_path = destpath + File.separator + extracted[0];
                    else
                        throw new Exception ("Unable extract ACE Project file.");
                    Project project = new Project();
                    project.parseProjectFile(proj_path);
                    project.saveUnzippedProjectFile(destpath, proj_path);
                    if(file_type.equals("taxonomy_file"))
                    {
                        files_to_extract = new String[1];
                        files_to_extract[0] = project.taxonomy_path;
                    }
                    else if(file_type.equals("feature_key_file"))
                    {
                        files_to_extract = project.feature_settings_paths;
                    }
                    else if(file_type.equals("feature_vector_file"))
                        files_to_extract = project.feature_vectors_paths;
                    else if (file_type.equals("classifications_file"))
                        files_to_extract = project.classification_paths;
                    project.delete();
                }
                else if (file_type.equals("unknown"))
                {
                    files_to_extract = new String[1];
                }
                else
                    throw new Exception (file_type + " is not a valid file type. ");
            }
            else
            {
                files_to_extract = new String[1];
                files_to_extract[0] = file_name;
            }
            return  decompress(destpath, files_to_extract, zipFile);

        }
        catch(IOException ioe)
        {
            throw new Exception ("IO Exception: An error occured while trying to extract from " + zip_file + ". ");
        }
    }


            /*//Process each entry
            while(zipFileEntries.hasMoreElements())
            {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

                String currentEntry = entry.getName();

                File destFile = new File(unzipDestinationDirectory, currentEntry);

                BufferedInputStream is =
                        new BufferedInputStream(zipFile.getInputStream(entry));
                int currentByte;
                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);

                BufferedOutputStream dest =
                        new BufferedOutputStream(fos, BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1)
                {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
            zipFile.close();
        }
        catch (IOException ioe)
        {
            throw new IOException("I/O error" + ioe.getMessage());
        }
        /*
        //unzip the entire zip file
        String[] paths = null;
        String project = parseZip(zip_file, "temp");
        Project proj = new Project();
        proj.parseProjectFile(project);

        //find the specific file to extract,
        if(filetype.equals("taxonomy"))
        {
            paths = new String[1];
            paths[0] = proj.taxonomy_path;
        }
        else if(filetype.equals("feature_settings"))
            paths = proj.feature_settings_paths;
        else if(filetype.equals("feature_vectors"))
            paths = proj.feature_vectors_paths;
        else if (filetype.equals("classifications"))
            paths = proj.classification_paths;
        else
            throw new Exception (filetype + " is an invalid filetype. Must be taxonomy, feature_settings, feature_vectors, or classifications");
        if (paths == null || paths.length == 0)
            throw new Exception ("The specified filetype is not present in the zip file.");

        //copy files whose paths are specified in paths to a new directory
        String sep = File.separator;
        File extracted = new File("EXTRACTED");
        extracted.mkdir();
        for(int i=0; i<paths.length&&paths[i]!=null; i++ )
        {
            String outfile = (paths[i].substring(0, paths[i].indexOf("temp"))) + "EXTRACTED" + (paths[i].substring(paths[i].lastIndexOf(sep)));
            mckay.utilities.staticlibraries.FileMethods.copyFile(paths[i], outfile);
            paths[i] = outfile;
        }

        //delete directory "temp"
        File temp = new File ("temp");
        mckay.utilities.staticlibraries.FileMethods.deleteDirectoryRecursively(temp);

        return paths;*/
    //}

    /**
     * Adds one or many files or directories to an existing ACE zip file.
     * This method extracts the contents of the given zipfile and re-compresses
     * it to include the new files. A new ACE XML project file will be written
     * to include references to any new ACE XML files that were added.
     *
     *
     * @param zip_file      Specifies the path to the zip file to which the file
     *                      specified in file_path is to be added.
     * @param files         Array of files or directories to be added to the zip
     *                      file. Any type of file is permitted but only references
     *                      to ACE XML files will be added to the ACE XML project file.
     * @throws Exception    If an error occurs.
     *
     *
     */
    public static void add(String zip_file, String[] files) throws Exception
    {

        // Decompress zip file
        String dest_path = "tempzip";
        boolean can_erase = false;
        decompress(zip_file, dest_path, can_erase);
        // Don't need to remove project files because saveZip will not include them, so smart that saveZip

        // Create string array containing files to be added and files previously stored in zip minus project files
        String[] files_to_zip = new String[files.length + 1];
        System.arraycopy(files, 0, files_to_zip, 0, files.length);
        files_to_zip[files_to_zip.length -1] = dest_path;

        // Pass that array to saveZip, it will check the type of all files and create new project file.
        saveZip(files_to_zip, zip_file);

        // Delete temporary directory
        File temp = new File("tempzip");
        mckay.utilities.staticlibraries.FileMethods.deleteDirectoryRecursively(temp);






        // Unused code that was part of an unsuccessful attempt to add individual files to an
        // existing zip file without unzipping the contents first:

        /*String dest_path = "tempzip";
        int current = 0;

        // If a directory was specified to be added, get all nested files
        //String[] files = {file_path};
        String[] contained = getFiles(files);
        int num_files = contained.length;

        // Will contain the filetype of each file in contained
        String[] file_types = new String[num_files];

        // Whether or not any ACE XML files were specifed to be added to zip file
        boolean ace_xml = false;

        // Determine file type of all files to be added
        // Take note if any ACE XML files were specified
        for (int i = 0; i < num_files; i++)
        {
            file_types[i] = XMLDocumentParser.getFileType(contained[i]);
            if (!file_types[i].equals("unknown"))
                ace_xml = true;
        }


        ZipFile zipFile = new ZipFile(new File(zip_file), ZipFile.OPEN_READ);
        String project_path = getProjectFile(zipFile);
System.out.println("project_path: " + project_path);
        decompress(zip_file, dest_path);
        String[] dest = {dest_path};
        String[] unzipped = getFiles(dest);
        String[] files_to_zip = new String[contained.length + unzipped.length];

        // If any ACE XML files were specified to be added, extract project file
        // so that paths can be added.
        String new_proj = null;
        if(ace_xml)
        {
            /*Extract and parse project file
            String[] temp = extract(" ", zip_file, "project_file", "TEMP");
            if(temp == null || temp[0] == null)
                throw new Exception (" Unable to extract project file.");

            String project_path = "TEMP" + File.separator + temp[0];*/

            /*Project project = new Project();
            project.parseProjectFile("tempzip" + File.separator + project_path);


            // check file type
            // add to the appropriate filetype array
            for (int i = 0; i < num_files; i++)
            {
                if(file_types[i].equals("taxonomy_file"))
                    project.addTaxonomy(contained[i]);
                else if(file_types[i].equals("feature_key_file"))
                    project.addFeatureDefinition(contained[i]);
                else if(file_types[i].equals("feature_vector_file"))
                    project.addDataSet(contained[i]);
                else if(file_types[i].equals("classifications_file"))
                    project.addClassification(contained[i]);
            }
            new_proj = project.saveProjectFile(project_path, true);
            files_to_zip[files_to_zip.length-1] = new_proj;
        }

        // go through each file to be added (only 1 unless directory was given)
        // create File and add to array to be passed

        System.arraycopy(unzipped, 0, files_to_zip, 0, unzipped.length);
        System.arraycopy(contained, 0, files_to_zip, unzipped.length-1, contained.length);

for (int i = 0; i < files_to_zip.length; i++)
{
    System.out.println("files_to_zip[" + i + "]: " + files_to_zip[i]);
}


        // Compress into zipfile
        //addFilesToExistingZip(zipFile, files_to_add);
        compress(files_to_zip, zip_file);

        // Delete temporary directory
        File temp = new File("tempzip");
        mckay.utilities.staticlibraries.FileMethods.deleteDirectoryRecursively(temp);*/
        //if(new_proj != null)
           // new_proj.delete();


        /*
        //unzip the entire zip file into a temporary directory
        String project = parseZip(zip_file, "temp");
        Project proj = new Project();
        proj.parseProjectFile(project);

        if(file_type.equals("taxonomy"))
        {
            proj.taxonomy_path = file_path;
        }
        else if(file_type.equals("feature_settings"))
        {
            //store in temporary array
            String[] temp = proj.feature_settings_paths;
            //increase size of array to hold new entry
            proj.feature_settings_paths = new String[proj.feature_settings_paths.length+1];
            //copy old array into new one
            System.arraycopy(temp, 0, proj.feature_settings_paths, 0, temp.length);
            //add new entry to array
            proj.feature_settings_paths[proj.feature_settings_paths.length-1] = file_path;
        }
        else if(file_type.equals("feature_vectors"))
        {
            //store in temporary array
            String[] temp = proj.feature_vectors_paths;
            //increase size of array to hold new entry
            proj.feature_vectors_paths = new String[proj.feature_vectors_paths.length+1];
            //copy old array into new one
            System.arraycopy(temp, 0, proj.feature_vectors_paths, 0, temp.length);
            //add new entry to array
            proj.feature_vectors_paths[proj.feature_vectors_paths.length-1] = file_path;
        }
        else if (file_type.equals("classifications"))
        {
            //store in temporary array
            String[] temp = proj.classification_paths;
            //increase size of array to hold new entry
            proj.classification_paths = new String[proj.classification_paths.length+1];
            //copy old array into new one
            System.arraycopy(temp, 0, proj.classification_paths, 0, temp.length);
            //add new entry to array
            proj.classification_paths[proj.classification_paths.length-1] = file_path;
        }
        else
            throw new Exception (file_type + " is an invalid filetype. Must be taxonomy, feature_settings, feature_vectors, or classifications");

        //save back into zipfile
        saveZip(proj.taxonomy_path, proj.feature_settings_paths, proj.feature_vectors_paths, proj.classification_paths, zip_file.substring(0,zip_file.indexOf(".")));
        //delete directory "temp"
        File temp = new File ("temp");
        mckay.utilities.staticlibraries.FileMethods.deleteDirectoryRecursively(temp);*/
    }

    /*PRIVATE METHODS************************************/

    /**
     * Parses a special file called project.sp that contains the path to the ACE
     * project file in a single line of text.
     * The first and only line of the project.sp file will be the path to the ACE
     * XML project file for this project.
     *
     * @param zipdirectory              The directory into which the contents of
     *                                  the ACE zip file were extracted.
     * @throws Exception, IOException   If an error occurs.
     * @returns project_path            The path to the ACE XML project file, as
     *                                  read from the special project.sp file.
     */
    private static String parseSp(String zipdirectory) throws Exception, IOException
    {
        String project_path = null;
        BufferedReader in;
        String sep = File.separator;
        in = new BufferedReader(new FileReader(zipdirectory + sep + "project.sp"));
        project_path = zipdirectory + sep + in.readLine();
        in.close();

        return project_path;
    }

    /**
     * Writes the path to the ACE project file in a special file called project.sp
     *
     * @param projpath  specifies the path to the ACE project file
     * @return          returns the path to the special project.sp file containing
     *                  the path name to the ACE project file
     */
    private static String writeSp(String projpath)throws Exception
    {
        String sp_path = null;
        try
        {
            File sp = new File ("project.sp");
            sp.delete();
            sp.createNewFile();
            FileWriter out = new FileWriter(sp);
            out.write(projpath);
            //sp_path = sp.getAbsolutePath();
            out.flush();
            out.close();
        }
        catch(IOException ioe)
        {
            throw new IOException ("I/O error "+ ioe.getMessage());
        }
        return "project.sp";//sp_path;
    }

    /**
     * Compresses all the specified files into a zip file using the java.util.zip package
     *
     * @param toZip     array of paths to files to be zipped
     * @param nameofZip specifies name of the .zip file to be created
     */
    private static void compress(String[] toZip, String nameofZip) throws Exception
    {

        try
        {
            // If file already exists, we want to overwrite it, so delete file.
            File zip = new File (nameofZip);
            zip.delete();
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(nameofZip));

            byte[] buffer = new byte[18024];

            // Set the compression ratio
            out.setLevel(Deflater.DEFAULT_COMPRESSION);

            // iterate through the array of files, adding each to the zip file
            for (int i = 0; i<(toZip.length)&&toZip[i]!=null; i++)
            {
                // Associate a file input stream for the current file
                FileInputStream in = new FileInputStream(toZip[i]);

                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(toZip[i]));

                // Transfer bytes from the current file to the ZIP file
                //out.write(buffer, 0, in.read(buffer));

                int len;
                while ((len = in.read(buffer)) > 0)
                {
                    out.write(buffer, 0, len);
                }

                // Close the current entry
                out.closeEntry();

                // Close the current file input stream
                in.close();

            }
            // Close the ZipOutPutStream
            out.close();
        }
        catch (IllegalArgumentException iae)
        {
            throw new Exception("Illegal Arguments encountered" + iae.getMessage());

        }
        catch (FileNotFoundException fnfe)
        {
            throw new Exception("file was not found" + fnfe.getMessage());
        }
        catch (IOException ioe)
        {
            throw new Exception("I/O error" + ioe.getMessage());
        }
    }

    /**
     * Unzipps the contents of a specified zip file into a specified directory.
     *
     * @param zippath                The path to the .zip file that the user wishes to extract.
     * @param destpath               The directory into which the zip file will be extracted.
     * @return destination_directory The directory into which the zip file has been extracted.
     */
    private static void decompress(String zippath, String destpath, boolean can_erase) throws Exception
    {
        // specify buffer size for extraction
        final int BUFFER = 2048;

        try
        {
            File zipfile = new File (zippath);
            //File unzipDestinationDirectory = mckay.utilities.staticlibraries.FileMethods.getNewDirectory(destpath, can_erase);
            File unzipDestinationDirectory = new File (destpath);
            unzipDestinationDirectory.mkdir();

            //Open zip file for reading
            ZipFile zipFile = new ZipFile(zipfile, ZipFile.OPEN_READ);

            //Create enumeration of the entries in the zip file
            Enumeration zipFileEntries = zipFile.entries();

            //Process each entry
            while(zipFileEntries.hasMoreElements())
            {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(unzipDestinationDirectory, currentEntry);
                BufferedInputStream is =
                        new BufferedInputStream(zipFile.getInputStream(entry));
                int currentByte;

                // establish buffer for writing file
                byte data[] = new byte[BUFFER];

                // write the current file to disk
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest =
                        new BufferedOutputStream(fos, BUFFER);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, BUFFER)) != -1)
                {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
            zipFile.close();
        }
        catch (IOException ioe)
        {
            throw new IOException("I/O error " + ioe.getMessage());
        }

    }

    /**
     * Extracts either a single file or all of a specified ACE XML file type from
     * a zip file. This method differs from the first decompress method in that
     * it only decompresses certain files, not the entire contents of the zip file.
     *
     * @param destpath          The directory to which the selected files will be extracted.
     * @param files_to_extract  List of names of files to be extracted.
     * @param zipFile           Zip file fro which to extract selected files.
     * @return                  List of paths of files that were successfully extracted.
     * @throws Exception        If an error occurs.
     */
    private static String[] decompress(String destpath, String[] files_to_extract, ZipFile zipFile)
            throws Exception
    {
        // specify buffer size for extraction
        final int BUFFER = 2048;
        try
        {
            //Create enumeration of the entries in the zip file
            Enumeration zipFileEntries = zipFile.entries();
            boolean found = false;

            for (int i = 0; i < files_to_extract.length; i++)
            {
                found = false;
                String file = files_to_extract[i];
                if(file.contains(File.separator))
                    file = file.substring(file.lastIndexOf(File.separator)+1);


                while (zipFileEntries.hasMoreElements() && !found)
                {
                    ZipEntry current_entry = (ZipEntry) zipFileEntries.nextElement();
                    String compare = current_entry.getName();
                    if (compare.endsWith(file))
                    {
                        found = true;
                        //File destFile = new File(destpath, file);
                        boolean can_erase = false;
                        File destFile = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(destpath + File.separator + file, can_erase);
                        if (destFile == null)
                        {
                            throw new Exception("File " + file + " was not extracted because a file with that name already exists in " + destpath + ".\n");
                        }

                        BufferedInputStream is =
                                new BufferedInputStream(zipFile.getInputStream(current_entry));
                        int currentByte;
                        // establish buffer for writing file
                        byte data[] = new byte[BUFFER];

                        // write the current file to disk
                        FileOutputStream fos = new FileOutputStream(destFile);

                        BufferedOutputStream dest =
                                new BufferedOutputStream(fos, BUFFER);

                        // read and write until last byte is encountered
                        while ((currentByte = is.read(data, 0, BUFFER)) != -1)
                        {
                            dest.write(data, 0, currentByte);
                        }
                        dest.flush();
                        dest.close();
                        is.close();
                    }
                }
                if(!found)
                    throw new Exception("Unable to find " + file + ".\n");
            }

            //Process each entry
            /*while (zipFileEntries.hasMoreElements() && !found)
            {
                ZipEntry current_entry = (ZipEntry) zipFileEntries.nextElement();
                String compare = current_entry.getName();
System.out.println("compare: " + compare);

                for (int i = 0; i < files_to_extract.length; i++)
                {
                    String file = files_to_extract[i];
System.out.println("file: " + file);
                    if (compare.endsWith(file))
                    {
    System.out.print("found");
                        found = true;
                        File destFile = new File(destpath, file);
                        BufferedInputStream is =
                                new BufferedInputStream(zipFile.getInputStream(current_entry));
                        int currentByte;
                        // establish buffer for writing file
                        byte data[] = new byte[BUFFER];

                        // write the current file to disk
                        FileOutputStream fos = new FileOutputStream(destFile);

                        BufferedOutputStream dest =
                                new BufferedOutputStream(fos, BUFFER);

                        // read and write until last byte is encountered
                        while ((currentByte = is.read(data, 0, BUFFER)) != -1)
                        {
                            dest.write(data, 0, currentByte);
                        }
                        dest.flush();
                        dest.close();
                        is.close();

                    }
                }
                //if(!found)
                  //  throw new Exception("Unable to find " + file + ".");
            }*/

            zipFile.close();

            // Return list of files that were extracted
            return files_to_extract;
        }
        catch (IOException ioe)
        {
            throw new IOException("I/O error" + ioe.getMessage());
        }
    }

    /**
     * If the given array of path names contains any directories, this method recusively
     * looks through the directory structure to find all files.
     *
     * @param paths     At the top level, this array contains the paths that were
     *                  specified to be placed in ACE zip file together. If none
     *                  of the cells of this array contain directory names, this
     *                  method returns the array that it was given.
     * @return          an array containing only file names with no directories.
     */
    private static String[] getFiles(String[] paths)
    {
        LinkedList <String> files = new LinkedList<String>();
        for(int i = 0; i < paths.length; i++)
        {
            File current = new File(paths[i]);
            if (current.isDirectory())
            {
                // Get list of all files and directories in the directory
                String[] x = getFiles(current.list());
                for( int j = 0; j < x.length; j++)
                    files.add(current.getName() + File.separator + x[j]);
            }
            else if(!current.getName().startsWith("."))
            {
                files.add(paths[i]);
            }
        }
        return files.toArray(new String[1]);
    }

    /**
     * Finds the name of the ACE XML project file contained in the given ACE zip
     * file. The special file with extension ".sp" is found first. The name of the
     * ACE XML project file is read from the first and only line of text in the
     * special .sp file.
     *
     * @return      The name of the ACE XML project file contained in this ACE zip file.
     */
    private static String getProjectFile(ZipFile zipFile)
            throws Exception
    {
        try{
            //Create enumeration of the entries in the zip file
            Enumeration zipFileEntries = zipFile.entries();

            // read first line of .sp file to get path to ACE XML project file
            String project_path = "";
            boolean sp_found = false;
            while(zipFileEntries.hasMoreElements() && !sp_found)
            {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

                String currentEntry = entry.getName();
                int start = currentEntry.lastIndexOf(".");
                String ext = mckay.utilities.staticlibraries.StringMethods.getExtension(currentEntry);
                if (start != -1 && ext != null)
                {
                    if(ext.equals(".sp"))
                    {
                        sp_found = true;

                    InputStream in = zipFile.getInputStream(entry);
                    project_path = "";

                    InputStreamReader isr = new InputStreamReader(in);
                    BufferedReader br = new BufferedReader(isr);
                    project_path = br.readLine();
                    in.close();
                    isr.close();
                    br.close();

                    if( project_path == null || project_path.equals(""))
                        throw new Exception ("Unable to extract project file. Error in .sp file.");
                    }
                }
            }
            return project_path;
        }
        catch(IOException ioe)
        {
            throw new Exception ("Unable to find project file. An errror occured " +
                    "while reading from a file");
        }
    }

    /* Not Used currently. Attempt to add individual files to existing zip file
     * without extracting contents and re zipping with new files. Attempt currently
     * unsuccessful.
    /**
     *
     * @param zipFile
     * @param files
     * @throws IOException
     */
    /*private static void addFilesToExistingZip(File zipFile, File[] files)
            throws IOException
    {
        // get a temp file
        File tempFile = File.createTempFile(zipFile.getName() + "jess", null);
        // delete it, otherwise you cannot rename your existing zip to it.
        tempFile.delete();

        boolean renameOk = zipFile.renameTo(tempFile);
        if (!renameOk)
        {
            throw new RuntimeException("could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
        }
        byte[] buf = new byte[1024];

        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

        ZipEntry entry = zin.getNextEntry();
        int j = 2;
        while (entry != null)
        {
            String name = entry.getName();
            boolean notInFiles = true;
            String new_name = "";
            for (File f : files)
            {
                int start = f.getName().lastIndexOf(File.separator);
                if (start < 0)
                    start = 0;
                File notabs = new File((zipFile.getName() + File.separator + f.getName()));
                f.renameTo(notabs);
                new_name = notabs.getName();
                if (new_name.equals(name))
                {
                    //notInFiles = false;
                     new_name = new_name + j++;
                    File newfile = new File(new_name);
                    f.renameTo(newfile);
                    break;
                }
            }
            if (notInFiles)
            {
                // Add ZIP entry to output stream.
                ZipEntry to_add = new ZipEntry(name);
                out.putNextEntry(to_add);
                // Transfer bytes from the ZIP file to the output file
                int len;
                while ((len = zin.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
            }
            entry = zin.getNextEntry();
        }
        // Close the streams
        zin.close();
        // compress the files

        for (int i = 0; i < files.length; i++)
        {
            InputStream in = new FileInputStream(files[i]);
            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(files[i].getName()));
            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            // Complete the entry
            out.closeEntry();
            in.close();
        }
        // Complete the ZIP file
        out.close();
        tempFile.delete();
    }
     */
}
