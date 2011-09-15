/*
 * ProjectFilesDialogBox.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import ace.xmlparsers.ParseACEZipFile;
import ace.datatypes.Project;


/**
 * An object of this class represents a dialog box that can be used to find and
 * store ACE configuration file paths.
 *
 * <p>The public fields of this class can be accessed in order to get the
 * current paths. They are automatically updated as changes are made to the
 * corresponding GUI fields. It would be best for them not to be changed
 * directly by external objects, however. External objects may call the
 * browsePath or the saveFileExternally methods to reset one of these paths
 * using a dialog box.
 *
 * <p>Overall ACE projects can be stored in ace_project_files. New projects can
 * be created externally using the startNewProject method, and the
 * saveCurrentProject method can be used to save the settings currently in the
 * dialog box to a file.
 *
 * <p>The <i>Project file</i> field holds the path of the currently loaded
 * ace_project_file. The <i>Browse</i> button next to this field allows it to be
 * updated.
 *
 * <p>The <i>Use ARFF file rather than ACE XML files</i> check box specifies
 * whether or not Weka ARFF files are to be used rather than ACE
 * feature_vector_files, taxonomy_files, feature_key_files and
 * classifications_file. Selecting or deselecting this box deactivates and
 * activates the appropriate GUI elements.
 *
 * <p>The <i>Taxonomy file</i> field holds the path of the currently specified
 * ACE taxonomy_file. The <i>Browse</i> button next to this field allows it to
 * be updated. This file holds the taxonomy to classify instances into.
 *
 * <p>The <i>Feature definitions file</i> field holds the path of the currently
 * specified ACE feature_key_file. The <i>Browse</i> button next to this field
 * allows it to be updated. This file holds details of the features which are
 * used for classification.
 *
 * <p>The <i>Feature vector files</i> field holds the paths of the currently
 * specified ACE feature_vector_files. The <i>Add</i> button next to this field
 * allows files to be added to the list, and the <i>Remove</i> button deletes
 * any currently selected files. This file holds extracted feature values for
 * instances to be classified.
 *
 * <p>The <i>Model classifications file</i> field holds the path of the
 * currently specified ACE classifications_file. The <i>Browse</i> button next
 * to this field allows it to be updated. This file holds model classifications
 * for instances. This can be used to train or test classifiers.
 *
 * <p>The <i>GUI preferences file</i> field holds the path of the currently
 * specified ACE gui_preferences_file. The <i>Browse</i> button next to this
 * field allows it to be updated. This file holds user preferences for
 * configuring the ACE interface.
 *
 * <p>The <i>Classifier settings file</i> field holds the path of the currently
 * specified ACE classifier_settings_file. The <i>Browse</i> button next to this
 * field allows it to be updated. This file holds preferences for performing
 * classifications and classifier optimization searches.
 *
 * <p>The <i>Weka ARFF file</i> field holds the path of the currently specified
 * Weka ARFF file. The <i>Browse</i> button next to this field allows it to be
 * updated. This file is an alternative to the native ACE file formats. It can
 * hold feature values of instances, model classifications and an implied flat
 * taxonomy.
 *
 * <p>The <i>Start New Project</i> button  Brings up a dialog box that allows
 * the user to choose the name of a new ACE project file and where to save it.
 * The user is asked if s/he would like to copy the files referred to by the
 * existing GUI fields to the new project directory. If so, they are copied to a
 * new subdirectory with the same name as the project file.
 *
 * <p>The <i>Save This Project</i> button saves the paths displayed in the GUI
 * fields to the path specified in the <i>Project file</i> field. If this field
 * is empty, then the user is presented with a <i>Start New Project</i> dialog
 * box.
 *
 * <p>The <i>Save As Startup Defaults</i> button stores the paths currently
 * entered in the dialog box to disk so that they will be the defaults loaded
 * when the ACE GUI is next run.
 *
 * <p>The <i>Cancel</i> button cancels all changes made in this dialog box,
 * restores previously stored entries to the fields and hides this dialog box.
 *
 * <p>The <i>Clear Paths</i> button clears all GUI fields in this dialog box.
 *
 * <p>The <i>OK</i> button stores all changes made in this dialog box  and hides
 * this dialog box.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class ProjectFilesDialogBox
     extends JFrame
     implements ActionListener
{
     /* FIELDS ****************************************************************/


     /**
      * Path of the current ACE XML project file.
      */
     public	String			project_path;

     /**
      * If current files were loaded from a zip file, this specified the file path
      * to the file from which they were originally extracted.
      */
     public     String                  zip_path;

     /**
      * Denotes whether data is to be be loaded from ACE XML files or a Weka ARFF
      * file.
      */
     public	boolean			use_arff_rather_than_ace_xml;

     /**
      * Path of the current Weka ARFF file that can be used to import instances.
      * This ARFF file can be used as an alternative to Weka ACE XML files, but
      * is less flexible. The usingARFFRatherThanACEXML method can be used to
      * determine which option is selected for use.
      */
     //public	String			arff_path;

     /**
      * Path of the current taxonomy_file that holds a taxonomy.
      */
     //public	String			taxonomy_path;

     /**
      * Paths of the current feature_key_files that holds the details about the
      * features.
      */
     //public	String			feature_definitions_path;

     /**
      * Paths of the current feature_vector_files that holds extracted feature
      * values.
      */
     //public	String[]		feature_vectors_paths;

     /**
      * Paths of the current classifications_files that holds model
      * classifications for instances.
      */
     //public	String			model_classifications_path;

     /**
      * Path of the current gui_preferences_file that holds preferences for the
      * GUI.
      */
    // public	String			gui_preferences_path;

     /**
      * Path of the current classifier_settings_file that holds preferences for
      * use in training and testing classifiers.
      */
     //public	String			classifier_settings_path;

     /**
      * Path of the current trained_classifiers_file that holds a set of trained
      * classifiers.
      */
     //public	String			trained_classifiers_path;


     // The main window of the ACE GUI
     private	MainGUIFrame            parent;

     // The last directory browsed in
     private   String			current_directory = ".";

     // Fields to display and allow editing of currently selected file paths
     private   JTextField		project_field;
     private   JTextField		arff_field;
     private   JTextField		taxonomy_field;
     private   JTextField		feature_settings_field;
     private   JList			feature_vectors_field;
     private   JTextField		model_classifications_field;
     private   JTextField		gui_preferences_field;
     private   JTextField		classifier_settings_field;
     private   JTextField		trained_classifiers_field;

     // Checkbox to denote whether the files to be loaded are in ACE XML or
     // Weka ARFF format
     private   JCheckBox		ace_xml_or_arff_checkbox;

     // Buttons to alter the indicated file paths
     private   JButton			browse_project_button;
     private   JButton			browse_arff_button;
     private   JButton			browse_taxonomy_button;
     private   JButton			browse_feature_settings_file_button;
     private   JButton			add_feature_vectors_file_button;
     private   JButton			remove_feature_vectors_file_button;
     private   JButton			browse_model_classifications_file_button;
     private   JButton			browse_gui_preferences_button;
     private   JButton			browse_classifier_settings_button;
     private   JButton			browse_trained_classifiers_button;

     // Button to clear all file paths
     private   JButton			clear_button;

     // Button to create a new project
     private   JButton			new_project_button;

     // Button to save current project settings
     private   JButton			save_this_project_button;

     // Button to store currently selected paths as defaults
     private   JButton			save_as_defaults_button;

     // Button to finalize the paths entered
     private   JButton			cancel_button;

     // Button to finalize the paths entered
     private   JButton			ok_button;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Construct this dialog box, but do not set it to being visible. If a
      * default startup project (called default_ace_project.xml) is available,
      * it is automatically loaded, if not all fields are defaulted to empty.
      *
      * @param	parent	The main ACE GUI component.
      */
     /*public ProjectFilesDialogBox(MainGUIFrame parent)
     {
          // Initialize the GUI
          prepareGUI(parent);

          // Load startup default project file into the GUI fields if it's available
          if ((new File("." + File.separator + "default_ace_project.xml")).exists())
               try
               {
                    parseProjectFile("." + File.separator + "default_ace_project.xml");
               }
               catch (Exception e)
               {
                    // e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
               }
     }*/


     /**
      * Construct this dialog box, but do not set it to being visible. Load the
      * paths contained in the project file referred to by the project_file_name
      * parameter.
      *
      * @param	parent             The main ACE GUI component.
      * @param	project_file_name  The path of the project file to load.
      */
     public ProjectFilesDialogBox(MainGUIFrame parent, String project_file_name)
     {
          // Initialize the GUI
          prepareGUI(parent);

          // Load contents of given project file
          try
          {
               parent.project.parseProjectFile(project_file_name);
          }
          catch (Exception e)
          {
               // e.printStackTrace();
               JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR1", JOptionPane.ERROR_MESSAGE);
          }
     }


     /**
      * Construct this dialog box, but do not set it to being visible. Intialize
      * the paths based on the contents of the Project object of the parent MainGUIFrame.
      * If a default startup project (called default_ace_project.xml) is available, then the
      * gui_settings_path and classifier_settings_file are loaded from it,
      * otherwise they are defaulted to empty.
      *
      * @param	parent                       The main ACE GUI component.
      *
      */
     public ProjectFilesDialogBox( MainGUIFrame parent)
     {
          // Initialize the GUI
          prepareGUI(parent);

          // Load startup default project file into the GUI fields if it's available
          // in order to get the gui preferences and classifier settings paths if
          // they're available.
          if ((new File("." + File.separator + "default_ace_project.xml")).exists())
               try
               {
                    parent.project.parseProjectFile("." + File.separator + "default_ace_project.xml");
               }
               catch (Exception e)
               {
                    // e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
               }
          else // Initialize fields to blank?

          // Set text fields based on parameters
          setTextFields(parent.project, null);

          // Set lists based on parameters
          if(parent.project.feature_vectors_paths != null && parent.project.feature_vectors_paths.length != 0)
          {
          clearJList(feature_vectors_field);
          for (int i = 0; i < parent.project.feature_vectors_paths.length; i++)
               ((DefaultListModel) (feature_vectors_field.getModel())).addElement(parent.project.feature_vectors_paths[i]);
          }

          // Update the check box and deactivate appropriate GUI elements
          updateCheckBox();
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event		The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the ace_xml_or_arff_checkbox
          if (event.getSource().equals(ace_xml_or_arff_checkbox))
               deactivateAppropriateGUIElements(ace_xml_or_arff_checkbox.isSelected());

          // React to the browse_project_button
          else if (event.getSource().equals(browse_project_button))
               browsePath("ace_project", false);

          // React to the browse_arff_button
          else if (event.getSource().equals(browse_arff_button))
               browsePath("arff", false);

          // React to the browse_taxonomy_button
          else if (event.getSource().equals(browse_taxonomy_button))
               browsePath("taxonomy", false);

          // React to the browse_feature_settings_file_button
          else if (event.getSource().equals(browse_feature_settings_file_button))
               browsePath("feature_definitions", false);

          // React to the add_feature_vectors_file_button
          else if (event.getSource().equals(add_feature_vectors_file_button))
               browsePath("feature_vectors", false);

          // React to the remove_feature_vectors_file_button
          else if (event.getSource().equals(remove_feature_vectors_file_button))
               removeItemsFromPathList(feature_vectors_field);

          // React to the browse_model_classifications_file_button
          else if (event.getSource().equals(browse_model_classifications_file_button))
               browsePath("model_classifications", false);

          // React to the browse_gui_preferences_button
          else if (event.getSource().equals(browse_gui_preferences_button))
               browsePath("gui_preferences", false);

          // React to the browse_classifier_settings_button
          else if (event.getSource().equals(browse_classifier_settings_button))
               browsePath("classifier_settings", false);

          // React to the browse_trained_classifiers_button
          else if (event.getSource().equals(browse_trained_classifiers_button))
               browsePath("trained_classifiers", false);

          // React to the clear_button
          else if (event.getSource().equals(clear_button))
               clearAllGUIFields();

          // React to the new_project_button
          else if (event.getSource().equals(new_project_button))
               startNewProject();

          // React to the save_this_project_button
          else if (event.getSource().equals(save_this_project_button))
               saveCurrentProject();

          // React to the save_as_defaults_button
          else if (event.getSource().equals(save_as_defaults_button))
          {
              readFields();
              try
              {
              parent.project.saveProjectFile("." + File.separator + "default_ace_project.xml", true);
              }
              catch(Exception e)
              {
                  JOptionPane.showMessageDialog(null, "Unable to save default project file. " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
              }
          }

          // React to the cancel_button
          else if (event.getSource().equals(cancel_button))
               cancel();

          // React to the ok_button
          else if (event.getSource().equals(ok_button))
               parseAllFiles();
     }


     /**
      * Stores the entries entered in the GUI fields in the public fields and
      * loads and and parses all of the corresponding files. A warning message
      * will be displayed if a project is already loaded.
      */
     public void parseAllFiles()
     {
          if (parent.project_previously_loaded)
          {
               int response = JOptionPane.showConfirmDialog(ProjectFilesDialogBox.this,
                    "This will erase any unsaved changes you have made to your project.\nDo you wish to proceed?",
                    "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
               if (response == JOptionPane.YES_OPTION)
               {
                    readFields();
                    parent.loadNewConfigurationFiles();
                    ProjectFilesDialogBox.this.setVisible(false);
               }
          }
          else
          {
               readFields();
               parent.loadNewConfigurationFiles();
               ProjectFilesDialogBox.this.setVisible(false);
          }
     }


     /**
      * Opens a dialog box to browse for files of the appropriate type. Only XML
      * files or ARFF files are displayed, as appropriate.
      *
      * <p>If the file to browse is an ace_project file, then this file is
      * parsed and the fields of this dialog box are set to the appropriate
      * values. No other types of files are parsed by this method, and calling
      * this method with other types of files only results in the storing of the
      * path to the file in the appropriate field(s).
      *
      * @param	what_to_browse     A code indicating what type of file to browse
      *                            for. Options are ace_project, arff, taxonomy,
      *                            feature_definitions,	feature_vectors,
      *                            model_classifications, gui_preferences,
      *                            classifier_settings and trained_classifiers.
      * @param	called_externally  Should be true if called by an external
      *                            object and false if called by a browse button
      *                            of this ProjectFilesDialogBox. If this is
      *                            false, then only the GUI fields are changed.
      *                            If it is true, then the stored public fields
      *                            are changed as well.
      * @return                    True if the OK button was pressed, false if
      *                            the Cancel button was pressed.
      */
     public boolean browsePath(String what_to_browse, boolean called_externally)
     {
          JFileChooser browse_dialog = new JFileChooser(new File(current_directory));

          if ( what_to_browse.equals("ace_project"))
          {
              String[] extension = {"xml", "zip"};
              browse_dialog.setFileFilter(new mckay.utilities.general.FileFilterImplementation(extension));
          }
          else if ( what_to_browse.equals("taxonomy") ||
               what_to_browse.equals("feature_definitions") ||
               what_to_browse.equals("feature_vectors") ||
               what_to_browse.equals("model_classifications") ||
               what_to_browse.equals("gui_preferences") ||
               what_to_browse.equals("classifier_settings") ||
               what_to_browse.equals("trained_classifiers") )
          {
               String[] extension = {"xml"};
               browse_dialog.setFileFilter(new mckay.utilities.general.FileFilterImplementation(extension));
          }
          else if (what_to_browse.equals("arff"))
          {
               String[] extension = {"arff"};
               browse_dialog.setFileFilter(new mckay.utilities.general.FileFilterImplementation(extension));
          }
          else return false;

          int browse_return = browse_dialog.showOpenDialog(ProjectFilesDialogBox.this);
          if (browse_return == JFileChooser.APPROVE_OPTION) // only do if OK chosen
          {
               File selection = browse_dialog.getSelectedFile();
               current_directory = selection.getParentFile().getAbsolutePath();

               if (what_to_browse.equals("ace_project"))
               {
                    String selected_project_file = selection.getPath();
                    String ext = mckay.utilities.staticlibraries.StringMethods.getExtension(selected_project_file);
                    try
                    {   if(ext.equals(".zip"))
                        {
                            // Extract contents of zip file into a temporary directory
                            selected_project_file = ParseACEZipFile.parseZip(selected_project_file, "TEMP");
                            // This directory will be deleted when ACE closes
                            parent.temp_files.add("TEMP");
                            parent.zipfile_loaded = true;
                        }
                        parent.project = new Project();
                        parent.project.parseProjectFile(selected_project_file);
                        setTextFields(parent.project, selected_project_file);
                    }
                    catch (Exception e)
                    {
                         // e.printStackTrace();
                         JOptionPane.showMessageDialog(null, "A problem occured while trying to load your ACE project. " + e.getMessage(), "zip/project ERROR", JOptionPane.ERROR_MESSAGE);
                    }
               }
               else if (what_to_browse.equals("arff"))
                    arff_field.setText(selection.getPath());
               else if (what_to_browse.equals("taxonomy"))
                    taxonomy_field.setText(selection.getPath());
               else if (what_to_browse.equals("feature_definitions"))
                    feature_settings_field.setText(selection.getPath());
               else if (what_to_browse.equals("feature_vectors"))
                    ((DefaultListModel) (feature_vectors_field.getModel())).addElement(selection.getPath());
               else if (what_to_browse.equals("model_classifications"))
                    model_classifications_field.setText(selection.getPath());
               else if (what_to_browse.equals("gui_preferences"))
                    gui_preferences_field.setText(selection.getPath());
               else if (what_to_browse.equals("classifier_settings"))
                    classifier_settings_field.setText(selection.getPath());
               else if (what_to_browse.equals("trained_classifiers"))
                    trained_classifiers_field.setText(selection.getPath());

               if (called_externally) readFields();

               return true;
          }
          else
               return false;
     }


     /**
      * Opens a save dialog box that allows the user to choose the path of a
      * file to be saved. Makes sure can write to this path and returns the
      * corresponding File. Displays an error dialog box if there is a problem.
      * Returns null if can or should not save. If the file is saved, then both
      * the GUI and public fields are updated. Automatically adds .xml as an
      * extension if this is not already present.
      *
      * @param	file_type     The code identifying the type of file to be saved.
      *                       Options are arff, taxonomy, feature_definitions,
      *                       feature_vectors, model_classifications,
      *                       gui_preferences, classifier_settings and
      *                       trained_classifiers.
      * @param	can_erase     Whether or not existing files with the specified
      *                       path should be overwritten without asking the
      *                       user.
      * @return               A File if appropriate, null if nothing should be
      *                       saved after all.
      */
     public File saveFileExternally(String file_type, boolean can_erase)
     {
          int save_return; // what is returned by the JFileChooser object
          JFileChooser save_dialog = new JFileChooser(new File(current_directory));
          save_return = save_dialog.showSaveDialog(this);
          File selection = null;
          if (save_return == JFileChooser.APPROVE_OPTION) // only do if OK chosen
          {
               // Get file and its path
               File temp_file = save_dialog.getSelectedFile();
               String path = temp_file.getPath();

               // Make sure has .xml extension
// What about arff?
               String ext = mckay.utilities.staticlibraries.StringMethods.getExtension(path);
               if (ext == null)
               {
                    path += ".xml";
                    temp_file = new File(path);
               }
               else if (!ext.equals(".xml"))
               {
                    path = mckay.utilities.staticlibraries.StringMethods.removeExtension(path) + ".xml";
                    temp_file = new File(path);
               }

               // Get file to write to
               selection = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(path, can_erase);

               // Update fields
               if (selection != null)
               {
                    if (file_type.equals("arff"))
                    {
                         parent.project.weka_arff_path = path;
                         arff_field.setText(path);
                    }
                    else if (file_type.equals("taxonomy"))
                    {
                         parent.project.taxonomy_path = path;
                         taxonomy_field.setText(path);
                    }
                    else if (file_type.equals("feature_definitions"))
                    {
                         parent.project.feature_settings_paths = new String[1];
                         parent.project.feature_settings_paths[0] = path;
                         feature_settings_field.setText(path);
                    }
                    else if (file_type.equals("feature_vectors"))
                    {
                         ((DefaultListModel) (feature_vectors_field.getModel())).addElement(path);

                         ListModel feature_vector_model = feature_vectors_field.getModel();
                         parent.project.feature_vectors_paths = new String[feature_vector_model.getSize()];
                         for (int i = 0; i < parent.project.feature_vectors_paths.length; i++)
                              parent.project.feature_vectors_paths[i] = (String) feature_vector_model.getElementAt(i);
                    }
                    else if (file_type.equals("model_classifications"))
                    {
                         parent.project.classification_paths = new String[1];
                         parent.project.classification_paths[0] = path;
                         model_classifications_field.setText(path);
                    }
                    else if (file_type.equals("gui_preferences"))
                    {
                         parent.project.gui_preferences_path = path;
                         gui_preferences_field.setText(path);
                    }
                    else if (file_type.equals("classifier_settings"))
                    {
                         parent.project.classifier_settings_path = path;
                         classifier_settings_field.setText(path);
                    }
                    else if (file_type.equals("trained_classifiers"))
                    {
                         parent.project.trained_classifiers_path = path;
                         trained_classifiers_field.setText(path);
                    }

               }
          }
          return selection;
     }


     /**
      * Save the paths displayed in the GUI fields to the path specified in the
      * Project File field. If this field is empty, then the user is presented
      * with a Start New Project dialog box. None of the actual files referred
      * to are resaved, as only the project file storing the paths to them is
      * saved.
      */
     public void saveCurrentProject()
     {
          if (project_field.getText().equals(""))
               startNewProject();
          else
          {
              try
              {
                  readFields();
                  parent.project.saveProjectFile(project_field.getText(), true);
              }
              catch(Exception e)
              {
                  JOptionPane.showMessageDialog(null, "Unable to save project file for current project. " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
              }
          }
     }


     /**
      * Brings up a dialog box that allows the user to choose the name
      * of a new project file and where to save it. The ace_project XML file
      * is then created at the given location with the given name. The user
      * is asked if s/he would like to copy the files referred to by
      * the existing GUI fields to the new directory. If so, they are copied
      * to a new subdirectory with the same name as the project file.
      */
     public void startNewProject()
     {
          int save_return; // what is returned by the JFileChooser object
          JFileChooser save_dialog = new JFileChooser(new File("."));
          save_dialog.setDialogTitle("Save New Project File");
          save_return = save_dialog.showSaveDialog(this);
          if (save_return == JFileChooser.APPROVE_OPTION) // only do if OK chosen
          {
               // Get name of selected project_file
               File selection = save_dialog.getSelectedFile();
               String new_project_path = selection.getPath();

               // Make sure project_file has .xml extension and give if doesn't
               String ext = mckay.utilities.staticlibraries.StringMethods.getExtension(new_project_path);
               if (ext == null)
               {
                    new_project_path += ".xml";
                    selection = new File(new_project_path);
               }
               else if (!ext.equals(".xml"))
               {
                    new_project_path = mckay.utilities.staticlibraries.StringMethods.removeExtension(new_project_path) + ".xml";
                    selection = new File(new_project_path);
               }

               try
               {
                    // Make sure file doesn't already exist
                    if (selection.exists())
                         throw new Exception("Project file by same name already exists.");

                    // Ask user if wishes to keep pre-existing paths
                    int copy = JOptionPane.showConfirmDialog(null, "Do you wish to copy project files to new location?", "New Project", JOptionPane.YES_NO_OPTION);

                    // Copy files over to new directory if this option selected
                    if (copy == JOptionPane.YES_OPTION)
                    {
                         // Create new directory for project file and set as current directory
                         String new_directory = mckay.utilities.staticlibraries.StringMethods.removeExtension(new_project_path) + File.separator;
                         File new_directory_file = new File(new_directory);
                         if (new_directory_file.exists())
                              throw new Exception("Directory called " + new_directory + " already exists.");
                         new_directory_file.mkdir();

                         // Read JLists
                         ListModel feature_vector_model = feature_vectors_field.getModel();
                         String[] current_feature_vector_paths = new String[feature_vector_model.getSize()];
                         for (int i = 0; i < current_feature_vector_paths.length; i++)
                              current_feature_vector_paths[i] = (String) feature_vector_model.getElementAt(i);

                         // Find new paths
                         String new_arff_path = new_directory + mckay.utilities.staticlibraries.StringMethods.convertFilePathToFileName(arff_field.getText());
                         String new_taxonomy_path = new_directory + mckay.utilities.staticlibraries.StringMethods.convertFilePathToFileName(taxonomy_field.getText());
                         String new_feature_definitions_path = new_directory + mckay.utilities.staticlibraries.StringMethods.convertFilePathToFileName(feature_settings_field.getText());
                         String new_model_classifications_path = new_directory + mckay.utilities.staticlibraries.StringMethods.convertFilePathToFileName(model_classifications_field.getText());
                         String new_gui_preferences_path = new_directory + mckay.utilities.staticlibraries.StringMethods.convertFilePathToFileName(gui_preferences_field.getText());
                         String new_classifier_settings_path = new_directory + mckay.utilities.staticlibraries.StringMethods.convertFilePathToFileName(classifier_settings_field.getText());
                         String new_trained_classifiers_path = new_directory + mckay.utilities.staticlibraries.StringMethods.convertFilePathToFileName(trained_classifiers_field.getText());
                         String[] new_feature_vectors_paths = new String[current_feature_vector_paths.length];
                         if (current_feature_vector_paths != null)
                              for (int i = 0; i < current_feature_vector_paths.length; i++)
                                   new_feature_vectors_paths[i] = new_directory + mckay.utilities.staticlibraries.StringMethods.convertFilePathToFileName(current_feature_vector_paths[i]);

                         // Copy files and store names of new files
                         if (!arff_field.getText().equals(""))
                         {
                              mckay.utilities.staticlibraries.FileMethods.copyFile(arff_field.getText(), new_arff_path);
                              arff_field.setText(new_arff_path);
                         }
                         if (!taxonomy_field.getText().equals(""))
                         {
                              mckay.utilities.staticlibraries.FileMethods.copyFile(taxonomy_field.getText(), new_taxonomy_path);
                              taxonomy_field.setText(new_taxonomy_path);
                         }
                         if (!feature_settings_field.getText().equals(""))
                         {
                              mckay.utilities.staticlibraries.FileMethods.copyFile(feature_settings_field.getText(), new_feature_definitions_path);
                              feature_settings_field.setText(new_feature_definitions_path);
                         }
                         if (!model_classifications_field.getText().equals(""))
                         {
                              mckay.utilities.staticlibraries.FileMethods.copyFile(model_classifications_field.getText(), new_model_classifications_path);
                              model_classifications_field.setText(new_model_classifications_path);
                         }
                         if (!gui_preferences_field.getText().equals(""))
                         {
                              mckay.utilities.staticlibraries.FileMethods.copyFile(gui_preferences_field.getText(), new_gui_preferences_path);
                              gui_preferences_field.setText(new_gui_preferences_path);
                         }
                         if (!classifier_settings_field.getText().equals(""))
                         {
                              mckay.utilities.staticlibraries.FileMethods.copyFile(classifier_settings_field.getText(), new_classifier_settings_path);
                              classifier_settings_field.setText(new_classifier_settings_path);
                         }
                         if (!trained_classifiers_field.getText().equals(""))
                         {
                              mckay.utilities.staticlibraries.FileMethods.copyFile(trained_classifiers_field.getText(), new_trained_classifiers_path);
                              trained_classifiers_field.setText(new_trained_classifiers_path);
                         }
                         if (current_feature_vector_paths != null)
                         {
                              clearJList(feature_vectors_field);
                              for (int i = 0; i < current_feature_vector_paths.length; i++)
                              {
                                   mckay.utilities.staticlibraries.FileMethods.copyFile(current_feature_vector_paths[i], new_feature_vectors_paths[i]);
                                   ((DefaultListModel) (feature_vectors_field.getModel())).addElement(new_feature_vectors_paths[i]);
                              }
                         }
                    }

                    // Save the actual project file
                    parent.project.saveProjectFile(new_project_path, true);
                    project_field.setText(new_project_path);
               }
               catch (Exception e)
               {
                    // e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
               }
          }
     }
     /**
      * Presents the user with the option of saving the files that are currently
      * loaded back into an ACE zip file before quitting. This method is only called when the data
      * was originally loaded from a zip file. The temporary directory into which
      * the data files were extracted will be deleted when the program exits. Any
      * changes that were made to the data within ACE will be lost unless the files
      * are saved.
      */
     public void saveZipPrompt()
     {
         File selection = null;
         try
         {
             // Present warning message to user
            int choice = JOptionPane.showConfirmDialog(this, "Do you wish to save zip project file before closing?",
                    "Warning", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
            // Save the data
            if (choice == JOptionPane.YES_OPTION)
            {
                JFileChooser browse_dialog = new JFileChooser(current_directory);
                String[] extension = {"zip"};
                browse_dialog.setFileFilter(new mckay.utilities.general.FileFilterImplementation(extension));
                int save_return = browse_dialog.showSaveDialog(this);
                if (save_return == JFileChooser.APPROVE_OPTION) // only do if OK chosen
                {
// WARNING: this needs to be revised! Should save zipfile with original names.
                    selection = browse_dialog.getSelectedFile();
                    ParseACEZipFile.saveZip(parent.project.taxonomy_path, parent.project.feature_settings_paths,
                            parent.project.feature_vectors_paths, parent.project.classification_paths, null, selection.getName(), null);
                }
            }
         }
         catch(Exception e)
         {
             JOptionPane.showMessageDialog(null, ("Unable to save file " + selection.getName() + "."), "ERROR", JOptionPane.ERROR_MESSAGE);
         }

     }

     /**
      * This method has been replaced with the new method setTextFields
      *
      * Updates the text fields of this project file dialog box to reflect the
      * currently loaded ACE XML files. selected_project_file is the path to the
	  * ACE XML project file from which the data of this project was loaded.
      */
     /*public void setText(String selected_project_file)
     {
          project_field.setText(selected_project_file);
          taxonomy_field.setText(parent.project.taxonomy_path);
          feature_settings_field.setText(parent.project.feature_settings_paths[0]);
          model_classifications_field.setText(parent.project.classification_paths[0]);
          ((DefaultListModel) (feature_vectors_field.getModel())).addElement(parent.project.feature_vectors_paths[0]);
     }*/

     /*
      * Sets the text fields of this ProjectFilesDiologBox to reflect the contents
      * of the current Project object. If the corresponding field in the project
      * obbject is null, then the text field is set to "".
      *
      * @param project      The Project object of the parent MainGUIFrame.
      * @param project_path Text representing the project path.
      */
     public void setTextFields(Project project, String project_path)
     {
         if(project_path != null)
             project_field.setText(project_path);
         else
             project_field.setText("");
         if(project.weka_arff_path != null)
            arff_field.setText(project.weka_arff_path);
         else
            arff_field.setText("");
         if(project.taxonomy_path != null)
            taxonomy_field.setText(project.taxonomy_path);
         else
             taxonomy_field.setText("");
         if(project.feature_settings_paths!= null && project.feature_settings_paths[0]!= null)
            feature_settings_field.setText(project.feature_settings_paths[0]);
         else
             feature_settings_field.setText("");
         if(project.feature_vectors_paths!= null && project.feature_vectors_paths[0] != null)
            ((DefaultListModel) (feature_vectors_field.getModel())).addElement(project.feature_vectors_paths[0]);
         else
             removeItemsFromPathList(feature_vectors_field);
         if(project.classification_paths != null && project.classification_paths[0] != null)
             model_classifications_field.setText(project.classification_paths[0]);
         else
             model_classifications_field.setText("");
         if(project.gui_preferences_path != null)
            gui_preferences_field.setText(project.gui_preferences_path);
         else
            gui_preferences_field.setText("");
         if(project.classifier_settings_path != null)
            classifier_settings_field.setText(project.classifier_settings_path);
         else
             classifier_settings_field.setText("");
         if(project.trained_classifiers_path != null)
            trained_classifiers_field.setText(project.trained_classifiers_path);
         else
             trained_classifiers_field.setText("");
     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Initializes the GUI and prepares it for display. Does not actually
      * display it yet, however.
      *
      * @param	parent	The main ACE GUI component.
      */
     private void prepareGUI(MainGUIFrame parent)
     {
          // Set the parent window
          this.parent = parent;

          // Configure overall window settings
          setTitle("File Path Settings");
          int preferred_width = 350; // preferred width of each field field
          int preferred_height = 20; // preferred height of each field field
          int horizontal_gap = 6; // horizontal space between GUI elements
          int vertical_gap = 11; // horizontal space between GUI elements

          // Instantiate and configure display fields
          project_field = new JTextField();
          project_field.setEditable(false);
          project_field.setPreferredSize(new Dimension(preferred_width, preferred_height));
          arff_field =  new JTextField();
          arff_field.setPreferredSize(new Dimension(preferred_width, preferred_height));
          taxonomy_field = new JTextField();
          taxonomy_field.setPreferredSize(new Dimension(preferred_width, preferred_height));
          feature_settings_field = new JTextField();
          feature_settings_field.setPreferredSize(new Dimension(preferred_width, preferred_height));
          feature_vectors_field = new JList(new DefaultListModel());
          model_classifications_field = new JTextField();
          model_classifications_field.setPreferredSize(new Dimension(preferred_width, preferred_height));
          gui_preferences_field = new JTextField();
          gui_preferences_field.setPreferredSize(new Dimension(preferred_width, preferred_height));
          classifier_settings_field = new JTextField();
          classifier_settings_field.setPreferredSize(new Dimension(preferred_width, preferred_height));
          trained_classifiers_field = new JTextField();
          trained_classifiers_field.setPreferredSize(new Dimension(preferred_width, preferred_height));

          // Instantiate and configure buttons and their ActionListener
          browse_project_button = new JButton("Browse");
          browse_project_button.addActionListener(this);
          browse_arff_button = new JButton("Browse");
          browse_arff_button.addActionListener(this);
          browse_taxonomy_button = new JButton("Browse");
          browse_taxonomy_button.addActionListener(this);
          browse_feature_settings_file_button = new JButton("Browse");
          browse_feature_settings_file_button.addActionListener(this);
          add_feature_vectors_file_button = new JButton("Add");
          add_feature_vectors_file_button.addActionListener(this);
          remove_feature_vectors_file_button = new JButton("Remove");
          remove_feature_vectors_file_button.addActionListener(this);
          browse_model_classifications_file_button = new JButton("Browse");
          browse_model_classifications_file_button.addActionListener(this);
          browse_gui_preferences_button = new JButton("Browse");
          browse_gui_preferences_button.addActionListener(this);
          browse_classifier_settings_button = new JButton("Browse");
          browse_classifier_settings_button.addActionListener(this);
          browse_trained_classifiers_button = new JButton("Browse");
          browse_trained_classifiers_button.addActionListener(this);
          clear_button = new JButton("Clear Paths");
          clear_button.addActionListener(this);
          save_this_project_button = new JButton("Save Project Paths");
          save_this_project_button.addActionListener(this);
          save_as_defaults_button = new JButton("Save As Startup Defaults");
          save_as_defaults_button.addActionListener(this);
          new_project_button = new JButton("Start New Project");
          new_project_button.addActionListener(this);
          cancel_button = new JButton("Cancel");
          cancel_button.addActionListener(this);
          ok_button = new JButton("OK");
          ok_button.addActionListener(this);

          // Instantiate checkbox and add an ActionListener
          ace_xml_or_arff_checkbox = new JCheckBox("Use ARFF file rather than ACE XML files");
          ace_xml_or_arff_checkbox.addActionListener(this);

          // Instantiate and configurepanels
          Container content_pane = getContentPane();
          content_pane.setLayout(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel sub_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel sub_panel_top = new JPanel(new GridLayout(4, 1, horizontal_gap, vertical_gap));
          JPanel sub_panel_bottom = new JPanel(new GridLayout(5, 1, horizontal_gap, vertical_gap));
          JPanel project_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel checkbox_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel arff_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel taxonomy_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel feature_settings_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel feature_vectors_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JScrollPane scroll_pane = new JScrollPane(feature_vectors_field);
          scroll_pane.setPreferredSize(new Dimension(preferred_width, preferred_height));
          JPanel feature_vectors_panel_right = new JPanel(new GridLayout(2, 1));
          JPanel model_classifications_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel gui_preferences_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel classifier_settings_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel trained_classifiers_path_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          JPanel button_panel = new JPanel(new GridLayout(2, 3, horizontal_gap, vertical_gap));

          // Add labels and previously created elements to panels and container
          project_panel.add(new JLabel("Project file:"), BorderLayout.NORTH);
          project_panel.add(project_field, BorderLayout.WEST);
          project_panel.add(browse_project_button, BorderLayout.CENTER);
          sub_panel_top.add(project_panel);
          checkbox_panel.add(ace_xml_or_arff_checkbox, BorderLayout.CENTER);
          sub_panel_top.add(checkbox_panel);
          taxonomy_panel.add(new JLabel("Taxonomy file:"), BorderLayout.NORTH);
          taxonomy_panel.add(taxonomy_field, BorderLayout.WEST);
          taxonomy_panel.add(browse_taxonomy_button, BorderLayout.CENTER);
          sub_panel_top.add(taxonomy_panel);
          feature_settings_panel.add(new JLabel("Feature definitions file:"), BorderLayout.NORTH);
          feature_settings_panel.add(feature_settings_field, BorderLayout.WEST);
          feature_settings_panel.add(browse_feature_settings_file_button, BorderLayout.CENTER);
          sub_panel_top.add(feature_settings_panel);
          sub_panel.add(sub_panel_top, BorderLayout.NORTH);
          feature_vectors_panel.add(new JLabel("Feature vector files:"), BorderLayout.NORTH);
          feature_vectors_panel_right.add(add_feature_vectors_file_button);
          feature_vectors_panel_right.add(remove_feature_vectors_file_button);
          feature_vectors_panel.add(scroll_pane, BorderLayout.WEST);
          feature_vectors_panel.add(feature_vectors_panel_right, BorderLayout.CENTER);
          sub_panel.add(feature_vectors_panel, BorderLayout.CENTER);
          model_classifications_panel.add(new JLabel("Model classifications file:"), BorderLayout.NORTH);
          model_classifications_panel.add(model_classifications_field, BorderLayout.WEST);
          model_classifications_panel.add(browse_model_classifications_file_button, BorderLayout.CENTER);
          sub_panel_bottom.add(model_classifications_panel);
          gui_preferences_panel.add(new JLabel("GUI preferences file:"), BorderLayout.NORTH);
          gui_preferences_panel.add(gui_preferences_field, BorderLayout.WEST);
          gui_preferences_panel.add(browse_gui_preferences_button, BorderLayout.CENTER);
          sub_panel_bottom.add(gui_preferences_panel);
          classifier_settings_panel.add(new JLabel("Classifier settings file:"), BorderLayout.NORTH);
          classifier_settings_panel.add(classifier_settings_field, BorderLayout.WEST);
          classifier_settings_panel.add(browse_classifier_settings_button, BorderLayout.CENTER);
          sub_panel_bottom.add(classifier_settings_panel);
          trained_classifiers_path_panel.add(new JLabel("Trained classifiers file:"), BorderLayout.NORTH);
          trained_classifiers_path_panel.add(trained_classifiers_field, BorderLayout.WEST);
          trained_classifiers_path_panel.add(browse_trained_classifiers_button, BorderLayout.CENTER);
          sub_panel_bottom.add(trained_classifiers_path_panel);
          arff_panel.add(new JLabel("Weka ARFF file:"), BorderLayout.NORTH);
          arff_panel.add(arff_field, BorderLayout.WEST);
          arff_panel.add(browse_arff_button, BorderLayout.CENTER);
          sub_panel_bottom.add(arff_panel);
          sub_panel.add(sub_panel_bottom, BorderLayout.SOUTH);
          content_pane.add(sub_panel, BorderLayout.CENTER);
          button_panel.add(new_project_button);
          button_panel.add(save_this_project_button);
          button_panel.add(save_as_defaults_button);
          button_panel.add(cancel_button);
          button_panel.add(clear_button);
          button_panel.add(ok_button);
          content_pane.add(button_panel, BorderLayout.SOUTH);

          // Cause program to react when the exit box is pressed
          addWindowListener(new WindowAdapter()
          {
               public void windowClosing(WindowEvent e)
               {
                    cancel();
               }
          });

          // Put elements together
          pack();

          // Initialize text fields
          setTextFields(parent.project, project_path);

          project_path = new String("");
          use_arff_rather_than_ace_xml = false;

          /*
          parent.project.weka_arff_path = new String("");
          parent.project.taxonomy_path = new String("");
          parent.project.feature_settings_paths = new String[1];
          parent.project.feature_settings_paths[0] = "";
          parent.project.feature_vectors_paths = null;
          parent.project.classification_paths = new String[1];
          parent.project.classification_paths[0] = "";
          parent.project.gui_preferences_path = new String("");
          parent.project.classifier_settings_path = new String("");
          parent.project.trained_classifiers_path = new String("");
           */
     }


     /**
      * Cause the stored public paths to correspond with the contents of the GUI
      * fields.
      */
     private void readFields()
     {
          // Read JTextFields
         if(project_field.getText().length() > 0)
          project_path = project_field.getText();
         if(arff_field.getText().length() > 0)
          parent.project.weka_arff_path = arff_field.getText();
         if(taxonomy_field.getText().length() > 0)
          parent.project.taxonomy_path = taxonomy_field.getText();
         if(feature_settings_field.getText().length() > 0)
         {
          parent.project.feature_settings_paths = new String[1];
          parent.project.feature_settings_paths[0] = feature_settings_field.getText();
         }
         if(model_classifications_field.getText().length() > 0)
         {
          parent.project.classification_paths = new String[1];
          parent.project.classification_paths[0] = model_classifications_field.getText();
         }
         if(gui_preferences_field.getText().length() > 0)
          parent.project.gui_preferences_path = gui_preferences_field.getText();
         if(classifier_settings_field.getText().length() > 0)
          parent.project.classifier_settings_path = classifier_settings_field.getText();
         if (trained_classifiers_field.getText().length() > 0)
          parent.project.trained_classifiers_path = trained_classifiers_field.getText();

          // Read JLists
          ListModel feature_vector_model = feature_vectors_field.getModel();
          parent.project.feature_vectors_paths = new String[feature_vector_model.getSize()];
          for (int i = 0; i < parent.project.feature_vectors_paths.length; i++)
          {
              String current = (String) feature_vector_model.getElementAt(i);
              if(current.length() > 0)
                parent.project.feature_vectors_paths[i] = current;
          }

          // Read check boxes
          use_arff_rather_than_ace_xml = ace_xml_or_arff_checkbox.isSelected();

          parent.loadNewConfigurationFiles();

     }


     /**
      * Cause the GUI fields to correspond with the contents of the stored fields.
      */
     private void resetFields()
     {
          // Reset JTextFields
         // Only reset if project paths have been initialized
         if(project_path != null)
            project_field.setText(project_path);
         else
             project_field.setText("");
         if(parent.project.weka_arff_path != null)
            arff_field.setText(parent.project.weka_arff_path);
         else
             arff_field.setText("");
         if(parent.project.taxonomy_path != null)
            taxonomy_field.setText(parent.project.taxonomy_path);
         else
             taxonomy_field.setText("");
         if(parent.project.feature_settings_paths != null && parent.project.feature_settings_paths[0] != null)
            feature_settings_field.setText(parent.project.feature_settings_paths[0]);
         else
             feature_settings_field.setText("");
         if(parent.project.classification_paths != null && parent.project.classification_paths[0] != null)
            model_classifications_field.setText(parent.project.classification_paths[0]);
         else
             model_classifications_field.setText("");
         if(parent.project.gui_preferences_path != null)
            gui_preferences_field.setText(parent.project.gui_preferences_path);
         else
             gui_preferences_field.setText("");
         if(parent.project.classifier_settings_path != null)
            classifier_settings_field.setText(parent.project.classifier_settings_path);
         else
             classifier_settings_field.setText("");
         if(parent.project.trained_classifiers_path != null)
            trained_classifiers_field.setText(parent.project.trained_classifiers_path);
         else
             trained_classifiers_field.setText("");

          // Read JLists
          DefaultListModel feature_vectors_model = new DefaultListModel();
          if (parent.project.feature_vectors_paths != null)
               for (int i = 0; i < parent.project.feature_vectors_paths.length; i++)
                    feature_vectors_model.addElement(parent.project.feature_vectors_paths[i]);
          feature_vectors_field.setModel(feature_vectors_model);

          // Read check boxes
          ace_xml_or_arff_checkbox.setSelected(use_arff_rather_than_ace_xml);

          // Set the appropriate elements to be active and inactive
          deactivateAppropriateGUIElements(use_arff_rather_than_ace_xml);
     }


     /**
      * Sets the ace_xml_or_arff_checkbox to selected if there is a Weka ARFF
      * file path in stored in the GUI and to false if there is not. Activates
      * and deactivates the appropriate GUI elements based on this.
      */
     private void updateCheckBox()
     {
          boolean parsed_using_arff = true;
          if (arff_field.getText().equals(""))
               parsed_using_arff = false;
          ace_xml_or_arff_checkbox.setSelected(parsed_using_arff);
          deactivateAppropriateGUIElements(parsed_using_arff);
     }


     /**
      * Deactivates and activates the appropriate text boxes and buttons based
      * on the given parameter.
      *
      * @param	using_arff	Whether a Weka ARFF file is to be loaded or ACE
      *                         XML files are to be loaded.
      */
     private void deactivateAppropriateGUIElements(boolean using_arff)
     {
          arff_field.setEnabled(using_arff);
          taxonomy_field.setEnabled(!using_arff);
          feature_settings_field.setEnabled(!using_arff);
          feature_vectors_field.setEnabled(!using_arff);
          model_classifications_field.setEnabled(!using_arff);

          browse_arff_button.setEnabled(using_arff);
          browse_taxonomy_button.setEnabled(!using_arff);
          browse_feature_settings_file_button.setEnabled(!using_arff);
          add_feature_vectors_file_button.setEnabled(!using_arff);
          remove_feature_vectors_file_button.setEnabled(!using_arff);
          browse_model_classifications_file_button.setEnabled(!using_arff);
     }


     /**
      * OBSOLETE - use new method in Project class
      *
      * Parses the given ace_project_file and fills in the appropriate GUI
      * fields based on its contents.
      *
      * @param	selected_project_file	The path to the project file to parse.
      * @throws                         Throws an exception if the file cannot
      *					be parsed.
      */
     /*private void parseProjectFile(String selected_project_file)
     throws Exception
     {
          // Parse the file
          LinkedList[] parsed_file = (LinkedList[]) XMLDocumentParser.parseXMLDocument(selected_project_file, "ace_project_file");

          // Clear all previous entries in GUI fields
          clearAllGUIFields();

          // Update text fields
          if (parsed_file[1].size() != 0)
               taxonomy_field.setText( (String) ((LinkedList) parsed_file[1]).getFirst() );
          if (parsed_file[2].size() != 0)
               feature_settings_field.setText( (String) ((LinkedList) parsed_file[2]).getFirst() );
          //parsed_file[3] is the feature vectors file, why isn't that included here?

          if (parsed_file[4].size() != 0)
               model_classifications_field.setText( (String) ((LinkedList) parsed_file[4]).getFirst() );
          if (parsed_file[5].size() != 0)
               gui_preferences_field.setText( (String) ((LinkedList) parsed_file[5]).getFirst() );
          if (parsed_file[6].size() != 0)
               classifier_settings_field.setText( (String) ((LinkedList) parsed_file[6]).getFirst() );
          if (parsed_file[7].size() != 0)
               trained_classifiers_field.setText( (String) ((LinkedList) parsed_file[7]).getFirst() );
          if (parsed_file[8].size() != 0)
               arff_field.setText( (String) ((LinkedList) parsed_file[8]).getFirst() );

          // Update lists
          Object[] parsed_feature_vector_path_objects = parsed_file[3].toArray();
          for (int i = 0; i < parsed_feature_vector_path_objects.length; i++)
               ((DefaultListModel) (feature_vectors_field.getModel())).addElement((String) parsed_feature_vector_path_objects[i]);

          // Update checkbox
          updateCheckBox();

          // Update project field
          project_field.setText(selected_project_file);
     }*/


     /**
      * OBSOLETE - use method in Project class instead
      *
      * Saves an ace_project_file XML file in the specified location with the
      * specified comments. This file is given the contents specified in the
      * text boxes (as opposed to the public fields) in this dialog box.
      *
      * @param	path		The path under which the file is to be saved.
      * @param	comments	Any comments to give the file.
      * @param	can_erase	True means the user is not warned if s/he is
      *                         overwrting an existing file, false means s/he is
      *                         warned.
      */
     /*private void saveProjectFile(String path, String comments, boolean can_erase)
     {
          // Get a file to write to
          File to_file = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(path, can_erase);

          // Read JLists
          ListModel feature_vector_model = feature_vectors_field.getModel();
          String[] current_feature_vector_paths = new String[feature_vector_model.getSize()];
          for (int i = 0; i < current_feature_vector_paths.length; i++)
               current_feature_vector_paths[i] = (String) feature_vector_model.getElementAt(i);

          // Prepare the String to write to
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
               "   <comments>" + comments + "</comments>\n" +
               "   <taxonomy_path>" + taxonomy_field.getText() + "</taxonomy_path>\n" +
               "   <feature_definitions_path>\n" +
               "      <path>" + feature_settings_field.getText() + "</path>\n" +
               "   </feature_definitions_path>\n" +
               "   <feature_vectors_path>\n"
               );
          if (current_feature_vector_paths != null)
               for (int i = 0; i < current_feature_vector_paths.length; i++)
                    contents += "      <path>" + current_feature_vector_paths[i] + "</path>\n";
          contents +=
               "   </feature_vectors_path>\n" +
               "   <model_classifications_path>\n" +
               "      <path>" + model_classifications_field.getText() + "</path>\n" +
               "   </model_classifications_path>\n" +
               "   <gui_preferences_path>" + gui_preferences_field.getText() + "</gui_preferences_path>\n" +
               "   <classifier_settings_path>" + classifier_settings_field.getText() + "</classifier_settings_path>\n" +
               "   <trained_classifiers_path>" + trained_classifiers_field.getText() + "</trained_classifiers_path>\n" +
               "   <weka_arff_path>" + arff_field.getText() + "</weka_arff_path>\n" +
               "</ace_project_file>";

          // Write to the file
          try
          {
               FileOutputStream to = new FileOutputStream(to_file);
               DataOutputStream writer = new DataOutputStream(to);
               writer.writeBytes(contents);
               writer.close();
          }
          catch (Exception e)
          {
               // e.printStackTrace();
               JOptionPane.showMessageDialog(null, ("Unable to write file " + to_file.getName() + "."), "ERROR", JOptionPane.ERROR_MESSAGE);
          }
     }*/


     /**
      * Set all GUI fields to empty.
      */
     private void clearAllGUIFields()
     {
          // Clear text fields
          project_field.setText("");
          arff_field.setText("");
          taxonomy_field.setText("");
          feature_settings_field.setText("");
          model_classifications_field.setText("");
          gui_preferences_field.setText("");
          classifier_settings_field.setText("");
          trained_classifiers_field.setText("");

          // Clear list fields
          clearJList(feature_vectors_field);
     }


     /**
      * Remove all items from the given JList.
      *
      * @param	list_to_clear	The JList to clear.
      */
     private static void clearJList(JList list_to_clear)
     {
          int[] all_indices = new int[list_to_clear.getModel().getSize()];
          for (int i = 0; i < all_indices.length; i++)
               all_indices[i] = i;
          list_to_clear.setSelectedIndices(all_indices);
          removeItemsFromPathList(list_to_clear);
     }


     /**
      * Remove all of the items that are selected on the given list from the
      * given list.
      *
      * @param	list	The list to remove elements from.
      */
     private static void removeItemsFromPathList(JList list)
     {
          Object[] selected_objects = list.getSelectedValues();
          DefaultListModel model = (DefaultListModel) list.getModel();
          for (int i = 0; i < selected_objects.length; i++)
               model.removeElement(selected_objects[i]);
     }


     /**
      * React to the user pressing the exit box or pressing the Cancel button.
      * GUI fields are restored to their original values from before this window
      * was shown and this window is hidden.
      */
     private void cancel()
     {
          resetFields();
          ProjectFilesDialogBox.this.setVisible(false);
     }




}