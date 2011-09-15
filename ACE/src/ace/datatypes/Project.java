/*
 * Project.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.datatypes;

import java.util.LinkedList;
import ace.xmlparsers.XMLDocumentParser;
import java.io.*;

/**
 * Objects of this class each hold reference to the componant ACE XML files and other
 * characteristics of an ACE XML project file. This class contains methods to save and parse an ACE XML project file.
 *
 * <p>The ACE XML project file is one of the ACE XML file formats.
 * Its main purpose is to provide the user with an easy, single step way of loading a
 * previously saved ACE project. Without this project file, the user would have to
 * individually load each the taxonomy file, feature vectors file, feature definitions
 * file, and classifications file. The ACE XML project file format is designed to contain
 * references to all componant ACE XML files of an ACE project. Thus the user can simply
 * load an ACE project file, and ACE can load each of the individual component files internally
 * without the user ever having to explicitly specify them. This project file is also used in the
 * context of an ACE Zip File. In this case, the project file
 * contains reference to the ACE XML files compressed in the zipfile. Note that with an ACE project
 * file, only reference to the component ACE XML files are stored, not the actual files
 * themselves, therefore the files themselves must be stored in a location that ACE can
 * access.
 *
 * <p>The following is an example of an ACE XML project file:
 * <p><code>
 * &#60?xml version="1.0"?&#62<br>
 * &#60!DOCTYPE ace_project_file [<br>
 *  &#60!ELEMENT ace_project_file (comments, taxonomy_path, feature_definitions_path, feature_vectors_path, model_classifications_path, gui_preferences_path, classifier_settings_path, trained_classifiers_path, weka_arff_path)&#62<br>
 *  &#60!ELEMENT comments (#PCDATA)&#62<br>
 *  &#60!ELEMENT taxonomy_path (#PCDATA)&#62<br>
 *  &#60!ELEMENT feature_definitions_path (path*)&#62<br>
 *  &#60!ELEMENT feature_vectors_path (path*)&#62<br>
 *  &#60!ELEMENT model_classifications_path (path*)&#62<br>
 *  &#60!ELEMENT gui_preferences_path (#PCDATA)&#62<br>
 *  &#60!ELEMENT classifier_settings_path (#PCDATA)&#62<br>
 *  &#60!ELEMENT trained_classifiers_path (#PCDATA)&#62<br>
 *  &#60!ELEMENT weka_arff_path (#PCDATA)&#62<br>
 *  &#60!ELEMENT path (#PCDATA)&#62<br>
 * ]&#62<br>
 * <br>
 * &#60ace_project_file&#62<br>
 *  &nbsp&nbsp&#60comments&#62this is a comment&#60/comments&#62<br>
 *  &nbsp&nbsp&#60taxonomy_path&#62/jMIR/ACE/ADDITIONAL_FILES/TestFiles/Taxonomy.xml&#60/taxonomy_path&#62<br>
 *  &nbsp&nbsp&#60feature_definitions_path&#62<br>
 *     &nbsp&nbsp&nbsp&nbsp&#60path&#62/jMIR/ACE/ADDITIONAL_FILES/TestFiles/FeatureKey.xml&#60/path&#62<br>
 *  &nbsp&nbsp&#60/feature_definitions_path&#62<br>
 *  &nbsp&nbsp&#60feature_vectors_path&#62<br>
 *     &nbsp&nbsp&nbsp&nbsp&#60path&#62/jMIR/ACE/ADDITIONAL_FILES/TestFiles/FeatureVectors.xml&#60/path&#62<br>
 *  &nbsp&nbsp&#60/feature_vectors_path&#62<br>
 *  &nbsp&nbsp&#60model_classifications_path&#62<br>
 *     &nbsp&nbsp&nbsp&nbsp&#60path&#62/jMIR/ACE/ADDITIONAL_FILES/TestFiles/Classifications.xml&#60/path&#62<br>
 *  &nbsp&nbsp&#60/model_classifications_path&#62<br>
 *  &nbsp&nbsp&#60gui_preferences_path&#62&#60/gui_preferences_path&#62<br>
 *  &nbsp&nbsp&#60classifier_settings_path&#62&#60/classifier_settings_path&#62<br>
 *  &nbsp&nbsp&#60trained_classifiers_path&#62&#60/trained_classifiers_path&#62<br>
 *  &nbsp&nbsp&#60weka_arff_path&#62&#60/weka_arff_path&#62<br>
 * &#60/ace_project_file&#62<br>
 * </code>
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class Project {

    /* FIELDS ****************************************************************/

    /**
     *
     */
    public String filename;

    /**
     * Comments concerning this ACE project to be included in the ACE XML project
     * file with the &#60comments&gt tag.
     */
    public String comments;

    /**
     * Path to the ACE XML taxonomy file for this project.
     * Unlike the other file path fields of this class, only one file
     * path may be specified because an ACE project may only have one taxonomy..
     */
    public String taxonomy_path;

    /**
     * Array of paths to the ACE XML feature key/definition files that contain information about the
     * features to be used for this ACE project.
     */
    public String[] feature_settings_paths;

    /**
     * Array of paths to the ACE XML feature vector files that hold extracted feature
     * values for an ACE project.
     */
    public String[] feature_vectors_paths;

    /**
     * Array of paths to the ACE XML classification files that hold model
     * classifications for instances.
     */
    public String[] classification_paths;

    /**
     * Path to the current gui_preferences_file.
     */
    public String gui_preferences_path;

    /**
     * Path to the current classifier_settings_file that holds preferences for
     * use in training and testing classifiers.
     */
    public String classifier_settings_path;

    /**
     * Path of the current trained_classifiers_file that holds a set of trained
     * classifiers.
     */
    public String trained_classifiers_path;

    /**
     * Specifies the path to the weka arff file from which instances will be loaded.
     * This is only used if ACE XML files are not being used.
     */
    public String weka_arff_path;


    /* CONSTRUCTORS **********************************************************/

    /**
     * Constructs an instance of a Project.
     *
     * <p>All fields are initialized to the values given as parameters.
     *
     * @param filename                  The filename of the ACE project file that
     *                                  this object represents.
     * @param comments                  Comments concerning the ACE XML project file.
     * @param taxonomy_path             Path to the current taxonomy file that contains the taxonomy that the user
     *                                  wishes to use.
     * @param feature_settings_paths    Arrray of paths to the feature key/settings XML files that hold the details about the
     *                                  features to be used for an ACE project.
     * @param feature_vectors_paths     Array of paths to the feature vector XML files that hold extracted feature
     *                                  values for an ACE project.
     * @param classification_paths      Array of paths to the classification XML files that hold model
     *                                  classifications for instances.
     * @param gui_preferences_path      Path of the current gui_preferences_file that holds preferences for the GUI.
     * @param classifier_settings_path  Path of the current classifier_settings_file that holds preferences for
     *                                  use in training and testing classifiers.
     * @param trained_classifiers_path  Path of the current trained_classifiers_file that holds a set of trained
     *                                  classifiers.
     * @param weka_arff_path            Specifies the path to the weka arff file that the user wants to use in place of ACE XML files.
     */
    public Project(String filename, String comments, String taxonomy_path,
            String[] feature_settings_paths, String[] feature_vectors_paths,
            String[] classification_paths,  String gui_preferences_path,
            String classifier_settings_path, String trained_classifiers_path,
            String weka_arff_path)
    {
        this.filename = filename;
        this.comments = comments;
        this.taxonomy_path = taxonomy_path;
        this.feature_settings_paths = feature_settings_paths;
        this.feature_vectors_paths = feature_vectors_paths;
        this.classification_paths = classification_paths;
        this.gui_preferences_path = gui_preferences_path;
        this.classification_paths = classification_paths;
        this.gui_preferences_path = gui_preferences_path;
        this.classifier_settings_path = classifier_settings_path;
        this.trained_classifiers_path = trained_classifiers_path;
        this.weka_arff_path = weka_arff_path;
    }

    /**
     * Constructs an instance of a Project.
     *
     * <p>All fields are initialized to null.
     */
    public Project()
    {
        comments = null;
        taxonomy_path = null;
        feature_settings_paths = null;
        feature_vectors_paths = null;
        classification_paths = null;
        gui_preferences_path = null;
        classification_paths = null;
        gui_preferences_path = null;
        classifier_settings_path = null;
        trained_classifiers_path = null;
        weka_arff_path = null;
    }

    /* PUBLIC METHODS ********************************************************/

    /**
     * Given an ACE XML project file, this method sets the fields of this class
     * equal to the paths specified in the project file.
     *
     * @param selected_project_file     Specifies the path to the ACE XML Project
     *                                  file that the user wishes to parse.
     * @throws java.lang.Exception      If an error occurs.
     */
    public void parseProjectFile(String selected_project_file)
    throws Exception
    {
        // Parse the file
        LinkedList[] parsed_file = (LinkedList[]) XMLDocumentParser.parseXMLDocument(selected_project_file, "ace_project_file");

        //null check?
        // Update fields, text and lists
        if (parsed_file[0].size() != 0)
            comments = (String)(parsed_file[0]).getFirst();
        if (parsed_file[1].size() != 0)
            taxonomy_path = (String)(parsed_file[1]).getFirst();
        if (parsed_file[2].size() != 0)
            feature_settings_paths = (String[])parsed_file[2].toArray(new String [0]);
        if (parsed_file[3].size() != 0)
            feature_vectors_paths = (String[]) parsed_file[3].toArray(new String[0]);
        if (parsed_file[4].size() != 0)
            classification_paths = (String[])parsed_file[4].toArray(new String[0]);
        if (parsed_file[5].size() != 0)
            gui_preferences_path = (String)(parsed_file[5]).getFirst();
        if (parsed_file[6].size() != 0)
            classifier_settings_path = (String)(parsed_file[6]).getFirst();
        if (parsed_file[7].size() != 0)
            trained_classifiers_path = (String)(parsed_file[7]).getFirst();
        if (parsed_file[8].size() != 0)
            weka_arff_path = (String)(parsed_file[8]).getFirst();
    }


    /**
     * Creates, writes to, and saves an ACE XML project file. Componant ACE XML
     * files and other features of an ACE project are given as parameters.
     *
     * @param path                      This will be the name of the saved XML project file.
     * @param can_erase                 Specifies whether or not a previously existing file with the same name should be overwritten or not.
     *                                  If false, it will send a prompt to the user asking if they would like to overwrite the previously existing file.
     *                                  If true, it will overwrite any previously existing files with the same name automatically.
     * @param comments                  Comments concerning the ACE XML project file.
     * @param taxonomy_path             Path to the current taxonomy file that contains the taxonomy that the user
     *                                  wishes to use.
     * @param feature_settings_paths    Arrray of paths to the feature key/settings XML files that hold the details about the
     *                                  features to be used for an ACE project.
     * @param feature_vectors_paths     Array of paths to the feature vector XML files that hold extracted feature
     *                                  values for an ACE project.
     * @param classification_paths      Array of paths to the classification XML files that hold model
     *                                  classifications for instances.
     * @param gui_preferences_path      Path of the current gui_preferences_file that holds preferences for the GUI.
     * @param classifier_settings_path  Path of the current classifier_settings_file that holds preferences for
     *                                  use in training and testing classifiers.
     * @param trained_classifiers_path  Path of the current trained_classifiers_file that holds a set of trained
     *                                  classifiers.
     * @param weka_arff_path            Specifies the path to the weka arff file that the user wants to use in place of ACE XML files.
     * @return                          The path to the newly written ACE XML project file.
     * @throws Exception                If unable to save the file.
     *
     */
    public static String saveProjectFile(String path, boolean can_erase,
            String comments, String taxonomy_path,  String feature_settings_paths[],
            String feature_vectors_paths[], String classification_paths[],  String gui_preferences_path,
            String classifier_settings_path, String trained_classifiers_path, String weka_arff_path)
    throws Exception
    {
        // Get a file to write to
        //File to_file = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(path, can_erase);
        File to_file = new File(path);
        mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(path, can_erase);


        to_file.delete();
        to_file.createNewFile();

        // Prepare the String to save
        String contents = new String
            (
            "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE ace_project_file [\n" +
            "   <!ELEMENT ace_project_file (comments, taxonomy_path, feature_definitions_path, feature_vectors_path, model_classifications_path, gui_preferences_path, classifier_settings_path, trained_classifiers_path, weka_arff_path)>\n" +
            "   <!ELEMENT comments (#PCDATA)>\n" +
            "   <!ELEMENT taxonomy_path (#PCDATA)>\n" +
            "   <!ELEMENT feature_definitions_path (path*)>\n" +
            "   <!ELEMENT feature_vectors_path (path*)>\n" +
            "   <!ELEMENT model_classifications_path (path*)>\n" +
            "   <!ELEMENT gui_preferences_path (#PCDATA)>\n" +
            "   <!ELEMENT classifier_settings_path (#PCDATA)>\n" +
            "   <!ELEMENT trained_classifiers_path (#PCDATA)>\n" +
            "   <!ELEMENT weka_arff_path (#PCDATA)>\n" +
            "   <!ELEMENT path (#PCDATA)>\n" +
            "]>\n\n" +
            "<ace_project_file>\n" +
            "   <comments>");
        if (comments != null)
            contents += comments;
        contents +=
            "</comments>\n" +
            "   <taxonomy_path>";
        if (taxonomy_path != null)
            contents += taxonomy_path;
        contents +=
            "</taxonomy_path>\n" +
            "   <feature_definitions_path>\n";
        if(feature_settings_paths != null)
        {
            for (int i = 0; i<=(feature_settings_paths.length-1)&&feature_settings_paths[i]!=null ; i++)
                contents +=
                    "      <path>" + feature_settings_paths[i] + "</path>\n";
        }
        contents +=
            "   </feature_definitions_path>\n" +
            "   <feature_vectors_path>\n";
        if (feature_vectors_paths != null)
        {
            for (int i = 0; i <= (feature_vectors_paths.length - 1) && feature_vectors_paths[i] != null; i++)
            {
                contents +=
                    "      <path>" + feature_vectors_paths[i] + "</path>\n";
            }
        }
        contents +=
            "   </feature_vectors_path>\n" +
            "   <model_classifications_path>\n";
        if(classification_paths!= null)
        {
            for (int i = 0; i<=(classification_paths.length-1)&&classification_paths[i]!=null; i++)
                contents +=
                    "      <path>" + classification_paths[i] + "</path>\n";
        }

        contents +=
            "   </model_classifications_path>\n" +
            "   <gui_preferences_path>";
        if (gui_preferences_path != null)
            contents += gui_preferences_path;
        contents +=
            "</gui_preferences_path>\n" +
            "   <classifier_settings_path>";
        if (classifier_settings_path != null)
            contents += classifier_settings_path;
        contents +=
            "</classifier_settings_path>\n" +
            "   <trained_classifiers_path>";
        if (trained_classifiers_path != null)
            contents += trained_classifiers_path;
        contents +=
             "</trained_classifiers_path>\n" +
             "   <weka_arff_path>" ;
        if (weka_arff_path != null)
            contents += weka_arff_path;
        contents +=
             "</weka_arff_path>\n" +
             "</ace_project_file>";

        // Write to the file
        try
          {
               FileOutputStream to = new FileOutputStream(to_file);
               DataOutputStream writer = new DataOutputStream(to);
               writer.writeBytes(contents);
               writer.close();
          }
          catch (IOException ioe)
          {
               throw new Exception("Problem saving file " + path + ".\nERROR: " + ioe.getMessage());
          }

        return to_file.getAbsolutePath();
    }

    /**
     * This method saves the ACE XML project file using the fields
     * of an instance of a Project instead of specifying the values explicitly in the parameters.
     *
     * <p> It simply calls the above saveProjectFile method using the fields of this instance
     * of a Project as parameters.
     *
     * @param project_name  This will be the name of the saved ACE XML project file.
     * @param can_erase     Specifies whether or not a previously existing file with the same name should be overwritten or not.
     *                      If false, it will send a prompt to the user asking if they would like to overwrite the previously existing file.
     *                      If true, it will overwrite any previously existing files with the same name automatically.
     * @return              The path to the newly written ACE XML project file.
     * @throws Exception    If a problem is encountered.
     */
    public String saveProjectFile(String project_name, boolean can_erase) throws Exception
    {
        String proj =  saveProjectFile(project_name, can_erase, comments, taxonomy_path,  feature_settings_paths, feature_vectors_paths,
                                classification_paths,  gui_preferences_path,
                                classifier_settings_path, trained_classifiers_path, weka_arff_path);
        return proj;
    }

    /**
     * Changes the path names specified in the ACE XML project file to reflect
     * the new location of the unzipped files.
     * <p>After a project that is being loaded from a zipfile has been unzipped, this
     * method rewrites the ACE XML project file so that it specifies the paths to
     * the newly unzipped ACE XML files in their new directory instead of the pre-zippage
     * file paths.
     * This becomes relevant when the user wishes load a project from a zip file,
     * or for extracting and adding individual ACE XML files to a zip file.
     * New paths will be absolute paths.
     *
     * @param directory_path    Specifies the path to the directory to which the zipped files were extracted.
     *                          In the new project file, pathnames will be specified in the format: directory_path + separator + previously specified file name
     * @param project_name      Specifies the name of the ACE XML file that is being rewritten.
     * @throws Exception        If an error occurs.
     */
    public void saveUnzippedProjectFile(String directory_path, String project_name)throws Exception
    {
        String sep = File.separator;

        //for each element of ACE XML project file, if not null, change path name to reflect new location
        // Remove previous parent file structure if present
        // If files were zipped from working directory and no parent directories were specified,
        // simply add new parent directory path to the specified file name.

        // Change path name of taxonomy file
        if (project_name!=null)
        {
            if(project_name.contains(sep))
                project_name = project_name.substring(project_name.lastIndexOf(sep));
            filename = directory_path + sep + project_name;
        }

        // Change path name of taxonomy file
        if (taxonomy_path!=null)
        {
            if(taxonomy_path.contains(sep))
                taxonomy_path = taxonomy_path.substring(taxonomy_path.lastIndexOf(sep));
            taxonomy_path = directory_path + sep + taxonomy_path;
        }

        // Change path name of feature definiitions files
        if(feature_settings_paths != null)
        {
            for(int i=0; i<feature_settings_paths.length&&feature_settings_paths[i]!=null; i++)
            {   if (feature_settings_paths[i].contains(sep))
                    feature_settings_paths[i] = feature_settings_paths[i].substring(feature_settings_paths[i].lastIndexOf(sep));
                feature_settings_paths[i] = directory_path + sep + feature_settings_paths[i];
            }
        }

        // Change path name of feature vectors files
        if(feature_vectors_paths != null)
        {
            for(int i=0; i<feature_vectors_paths.length&&feature_vectors_paths[i]!=null; i++)
            {   if (feature_vectors_paths[i].contains(sep))
                    feature_vectors_paths[i] = feature_vectors_paths[i].substring(feature_vectors_paths[i].lastIndexOf(sep));
                feature_vectors_paths[i] = directory_path + sep + feature_vectors_paths[i];
            }
        }

        // Change path name of model classifications files
        if(classification_paths != null)
        {
            for(int i=0; i<classification_paths.length&&classification_paths[i]!=null; i++)
            {   if (classification_paths[i].contains(sep))
                    classification_paths[i] = classification_paths[i].substring(classification_paths[i].lastIndexOf(sep));
                classification_paths[i] = directory_path + sep + classification_paths[i];
            }
        }
        if (gui_preferences_path!=null)
        {
            if(gui_preferences_path.contains(sep))
                gui_preferences_path = gui_preferences_path.substring(gui_preferences_path.lastIndexOf(sep));
            gui_preferences_path = directory_path + sep + gui_preferences_path;
        }
        if (classifier_settings_path!=null)
        {
            if(classifier_settings_path.contains(sep))
                classifier_settings_path = classifier_settings_path.substring(classifier_settings_path.lastIndexOf(sep));
            classifier_settings_path = directory_path + sep + classifier_settings_path;
        }
        if (trained_classifiers_path!=null)
        {
            if(trained_classifiers_path.contains(sep))
                trained_classifiers_path = trained_classifiers_path.substring(trained_classifiers_path.lastIndexOf(sep));
            trained_classifiers_path = directory_path + sep + trained_classifiers_path;
        }
        if (weka_arff_path!=null)
        {
            if(weka_arff_path.contains(sep))
                weka_arff_path = weka_arff_path.substring(weka_arff_path.lastIndexOf(sep));
            weka_arff_path = directory_path + sep + weka_arff_path;
        }

        // Saves current values of fields into an ACE XML project file
        // Overwrites previous project file with the same name.
        saveProjectFile(filename, true);

    }

    /**
     * Adds a new taxonomy file to this Project. The given path will replace any
     * previously present Taxonomy paths.
     *
     * @param new_taxonomy      The the file path to the ACE XML Taxonomy file to
     *                          be referenced by this Project.
     */
    public void addTaxonomy(String new_taxonomy)
    {
        taxonomy_path = new_taxonomy;
    }

    /**
     * Adds a new feature vector file to this Project. The given path will be added
     * to the array of paths to ACE XML feature vectors files that are a part of
     * this ACE project.
     *
     * @param new_dataset       The ACE XML feature vectors file to be added to
     *                          this ACE Project.
     */
    public void addDataSet(String new_dataset)
    {
        if(feature_vectors_paths == null)
        {
            feature_vectors_paths = new String[1];
            feature_vectors_paths[0] = new_dataset;
        }
        else
        {
            String[] temp = new String[feature_vectors_paths.length + 1];
            System.arraycopy(feature_vectors_paths, 0, temp, 0, feature_vectors_paths.length);
            temp[temp.length -1] = new_dataset;
            feature_vectors_paths = temp;
        }

    }

    /**
     * Adds a new feature definitions file to this Project. The given path will
     * be added to the array of paths to ACE XML feature definition files that are
     * a part of this ACE project.
     *
     * @param new_feature_definition        The ACE XML feature definitions file
     *                                      to be added to this ACE Project
     */
    public void addFeatureDefinition(String new_feature_definition)
    {
       if(feature_settings_paths == null)
        {
            feature_settings_paths = new String[1];
            feature_settings_paths[0] = new_feature_definition;
        }
        else
        {
            String[] temp = new String[feature_settings_paths.length + 1];
            System.arraycopy(feature_settings_paths, 0, temp, 0, feature_settings_paths.length);
            temp[temp.length -1] = new_feature_definition;
            feature_settings_paths = temp;
        }
    }

    /**
     * Adds a new model classifications file to this Project. The given path will
     * be added to the array of paths to ACE XML model classifications files that are
     * a part of this ACE project.
     *
     * @param new_classification        The ACE XML feature definitions file to
     *                                  be added to this ACE Project.
     */
    public void addClassification(String new_classification)
    {
        if(classification_paths == null)
        {
            classification_paths = new String[1];
            classification_paths[0] = new_classification;
        }
        else
        {
            String[] temp = new String[classification_paths.length + 1];
            System.arraycopy(classification_paths, 0, temp, 0, classification_paths.length);

            temp[temp.length -1] = new_classification;
            classification_paths = temp;
        }
    }

    /**
     * Deletes the ACE XML Project file that this object represents.
     */
    public void delete()
    {
        if(filename != null && !filename.equals(""))
        {
            File file = new File(filename);
            file.delete();
        }
    }
}
