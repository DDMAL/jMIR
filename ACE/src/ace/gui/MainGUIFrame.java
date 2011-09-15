/*
 * MainGUIFrame.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import java.util.LinkedList;
import ace.datatypes.*;
import mckay.utilities.gui.templates.HelpDialog;


/**
 * Part of the ACE project. This is the main GUI container that is used to hold
 * tabbed panes and menus. The DataBoard used by ACE to store taxonomy, feature
 * and instance information is also stored in objects of this class, and can
 * be accessed externally by other objects.
 *
 * <p>The File menu allows the user to load and save project files or to quit.
 * The <i>Load Configuration Files</i> menu item allows the users to load an
 * ACE project the related ACE files using the <i>File Path Settings</i>
 * dialog box. This erases any existing project information currently in memory.
 * The <i>Save Project</i> menu item saves all information currently in memory
 * to the paths specified in the <i>File Path Settings</i> dialog box. The
 * <i>Save Project Paths</i> menu item stores the paths specified in the
 * <i>File Path Settings</i> dialog box as an ACE project file, but does not
 * actually save data to each of these paths. The <i>Load Zip</i> menu item allows
 * the user to specify an ACE zip file to be loaded. The contents of the specified
 * zip file are extracted into a temporary directory. The <i>Quit</i> menu item quits
 * the ACE software. If a zipfile has been loaded, the user will be prompted to
 * save the current data back into a zipfile. Otherwise, all unsaved data will be
 * lost when ACE exits.
 *
 * <p>The Help menu allows the user to view basic information about ACE or view
 * the on-line manual. The <i>About</i> menu item shows basic version and
 * authourship information on the ACE software. The <i>View Manual</i> displays
 * the on-line browsable manual to ACE.
 *
 * <p>The <i>Taxonomy</i> panel allows the user to view and edit information
 * relating to the taxonomy which classifiers are to be made based on, if any.
 *
 * <p>The <i>Instances</i> panel allows the user to view and edit information
 * relating to features extracted from instances, model classifications of
 * instances and meta-data relating to instances.
 *
 * <p>The <i>Feature Settings</i> panel allows the user to view meta-data on
 * features.
 *
 * <p>The <i>Preferences</i> panel allows the user to view and edit user
 * preferences relating to the ACE GUI.
 *
 * <p>The <i>Classification Settings</i> panel allows the user to view and edit
 * information relating to how training, testing and meta-learning is to be
 * performed.
 *
 * <p>The <i>Experimenter</i> panel allows the user to perform classification
 * experiments.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */

public class MainGUIFrame
     extends JFrame
     implements ActionListener
{
     /* FIELDS ****************************************************************/


     /**
      * The object that stores the current taxonomy, feature settings, instance
      * feature values and model classifications.
      */
     public	DataBoard                    data_board;

     /**
      * This object stores the path names to all component ACE XML files of an
      * ACE project.
      */
     public     Project                      project;

     /**
      * List of files and/or directories to be deleted when ACE closes.
      * example: The temporary directory into which zipfiles are extracted
      * will be deleted when ACE closes.
      */
     public     LinkedList<String>           temp_files;

     /**
      * Notes whether a project has already been loaded
      */
     public	boolean                      project_previously_loaded;

     /**
      * Notes whether a zipfile has been loaded.
      */
     public     boolean                      zipfile_loaded;

     /**
      * Dialog box that stores paths of ACE XML or Weka ACE files used in
      * current project.
      */
     public	ProjectFilesDialogBox        project_files_dialog_box;

     /**
      * The panel used to view and edit the current taxonomy.
      */
     public	TaxonomyPanel                taxonomy_panel;

     /**
      * The panel used to view and edit instances.
      */
     private	InstancesPanel               instances_panel;

     /**
      * The panel used to view and edit feature settings.
      */
     public	FeatureDefinitionsPanel      feature_definitions_panel;

     /**
      * The panel used to view and edit GUI preferences.
      */
     private	GUIPreferencesPanel          gui_preferences_panel;

     /**
      * The panel used to view and edit the settings used for training and
      * classification of instances.
      */
     private	ClassificationSettingsPanel  classification_settings_panel;

     /**
      * The panel used to run training and classification experiments.
      */
     private	ExperimentationPanel         experimentation_panel;

     /**
      * A dialog box allowing the user to view the on-line help.
      */
     private	HelpDialog                   help_dialog;

     // GUI components
     private JTabbedPane                     tabbed_pane;
     private Container                       content_pane;

     // Menu fields
     private JMenuBar                        menu_bar;
     private JMenu                           file_menu;
     private JMenu                           help_menu;
     private JMenuItem                       load_configuration_files_menu_item;
     private JMenuItem                       save_project_menu_item;
     private JMenuItem                       save_project_paths_menu_item;
     private JMenuItem                       about_menu_item;
     private JMenuItem                       view_manual_menu_item;
     private JMenuItem                       quit_menu_item;
     private JMenuItem                       load_zip_item;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Construct the ACE GUI and load the default startup project (called
      * default_ace_project.xml) if it is is available. Then display the GUI.
      */
     public MainGUIFrame()
     {
          // Initialize the GUI
          prepareGUI();

          //Prepare project
          project = new Project();
          data_board = new DataBoard();

          // Configure the ProjectFilesDialogBox
          project_files_dialog_box = new ProjectFilesDialogBox(this);

          // Create storage for files to be deleted
          temp_files = new LinkedList<String>();

          // Display the GUI
          this.setVisible(true);
     }

     /**
      * Construct the ACE GUI and load the specified startup project.
      * Then display the GUI.
      *
      * @param	project_file_name   The ACE project file to load upon startup.
      * @throws Exception           If an error occurs.
      */
     public MainGUIFrame(String project_file_name)
             throws Exception
     {
         try
         {
          // Initialize the GUI
          prepareGUI();

          // Parse the given project file
          // Store paths to componant ACE XML files in Project object
          project = new Project();
          project.parseProjectFile(project_file_name);

          // Create storage for names of files to be deleted, add name of directory
          // to which zip files will be extracted (if zip file is used)
          temp_files = new LinkedList<String>();
          temp_files.add("TEMPZIP");

          // Configure the ProjectFilesDialogBox, find the starting
          // configuration file paths and set up data_board based on the
          // contents of project_files_dialog_box
          project_files_dialog_box = new ProjectFilesDialogBox(this, project_file_name);
          project_files_dialog_box.setTextFields(project, project_file_name);
          // Parse all startup files
          project_files_dialog_box.parseAllFiles();

          // Display the GUI
          this.setVisible(true);
          //loadNewConfigurationFiles();
         }
         catch(Exception e)
         {
             e.printStackTrace();
             JOptionPane.showMessageDialog(null, e.getMessage() + " Unable to Initialize GUI with selected project file.", "ERROR", JOptionPane.ERROR_MESSAGE);
         }
     }


     /**
      * Construct the ACE GUI and load the specified configuration files. Then
      * display the GUI. If a default startup project (called
      * default_ace_project.xml) is available, then the gui_settings_path and
      * classifier_settings_file are loaded from it, otherwise they are
      * defaulted to empty.
      *
      * @param	taxonomy_file                The path of the taxonomy file to
      *                                      store in this dialog box. If no
      *                                      entry is to be stored, "" should be
      *                                      passed, not null.
      * @param	feature_key_files            The path of the feature key file to
      *                                      store in this dialog box. If no
      *                                      entry is to be stored, "" should be
      *                                      passed, not null.
      * @param	feature_vector_files         The path of the feacture vector
      *                                      files to store in this dialog box.
      *                                      If no entry is to be stored, ""
      *                                      should be passed, not null.
      * @param	model_classifications_files  The path of the model
      *                                      classifications file to store in
      *                                      this dialog box. If no entry is to
      *                                      be stored, "" should be passed, not
      *                                      null.
      * @param	weka_arff_file               The path of the Weka ARFF file to
      *                                      store in this dialog box. If no
      *                                      entry is to be stored, "" should be
      *                                      passed, not null.
      * @param	trained_classifiers_file     The path of the trained classifiers
      *                                      file to store in this dialog box.
      *                                      If no entry is to be stored, ""
      *                                      should be passed, not null.
      */
     public MainGUIFrame( String taxonomy_file,
          String[] feature_key_files,
          String[] feature_vector_files,
          String[] model_classifications_files,
          String weka_arff_file,
          String trained_classifiers_file )
     {
         try
         {
          // Initialize the GUI
          prepareGUI();

          // Create storage for names of files to be deleted
          temp_files = new LinkedList<String>();

          project = new Project(null, null, taxonomy_file, feature_key_files, feature_vector_files,
                                model_classifications_files,  null,
                                null, trained_classifiers_file, weka_arff_file);


          // Configure the ProjectFilesDialogBox, find the starting
          // configuration file paths and set up data_board based on the
          // contents of project_files_dialog_box
          project_files_dialog_box = new ProjectFilesDialogBox(this);


          // Parse all startup files
          project_files_dialog_box.parseAllFiles();

          // Display the GUI
          this.setVisible(true);
          //loadNewConfigurationFiles();
         }
         catch(Exception e)
         {
             e.printStackTrace();
             JOptionPane.showMessageDialog(null, e.getMessage()+ " Unable to Initialize GUI with selected ACE XML files.", "ERROR", JOptionPane.ERROR_MESSAGE);
         }
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Calls the appropriate methods when the menu items are selected.
      *
      * @param	event		The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the load_configuration_files_menu_item
          if (event.getSource().equals(load_configuration_files_menu_item))
               project_files_dialog_box.setVisible(true);

          // React to the save_project_menu_item
          if (event.getSource().equals(save_project_menu_item))
               saveProject();

          // React to the save_project_paths_menu_item
          if (event.getSource().equals(save_project_paths_menu_item))
               project_files_dialog_box.saveCurrentProject();

          // React to the about_menu_item
          if (event.getSource().equals(about_menu_item))
               new mckay.utilities.gui.templates.AboutDialog(this, "ACE 2.2.1", "Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)", "2010 (GNU GPL)", "McGill University");

          // React to the view_manual_menu_item
          if (event.getSource().equals(view_manual_menu_item))
               help_dialog.setVisible(true);

          // React to the quit_menu_item
          if (event.getSource().equals(quit_menu_item))
          {
              if (zipfile_loaded)
              {
                  // Ask if user would like to save contents of zipfile
                  project_files_dialog_box.saveZipPrompt();
              }
              deleteTempFiles();
              System.exit(0);
          }

          if (event.getSource().equals(load_zip_item))
          {
              project_files_dialog_box.browsePath("ace_project", false);
              loadNewConfigurationFiles();
              zipfile_loaded = true;
          }
     }


     /**
      * Load a new set of configuration files based on the contents of the
      * project_files_dialog_box. Instantiate a new data_board based on these
      * files If an error occurs, then the previous settings are restored. The
      * GUI panes are then (re)set if no errors occured.
      */
     public void loadNewConfigurationFiles()
     {
         //testStoredPaths();
          DataBoard temp_data_board = data_board;
          try
          {
               // Instantiate data_board based on the contents of project_files_dialog_box
               if (project_files_dialog_box.use_arff_rather_than_ace_xml)
               {// Load data_board based on ARFF file (must make sure that it is a valid file)
                   data_board = new DataBoard(project.weka_arff_path);
               }
               else if(project.feature_settings_paths!= null || project.feature_settings_paths != null
                       || project.classification_paths != null || project.taxonomy_path != null)
               {
                   String feature_definitions_path = null;
                   String classifications_path = null;
                   String[] feature_vectors_paths = null;
                   String taxonomy_path = null;

                   if(project.feature_settings_paths != null && project.feature_settings_paths.length > 0 &&project.feature_settings_paths[0]!= null)
                       feature_definitions_path = project.feature_settings_paths[0];
                   if(project.classification_paths != null && project.classification_paths.length > 0 &&project.classification_paths[0]!= null)
                       classifications_path = project.classification_paths[0];
                   if(project.feature_vectors_paths != null && project.feature_vectors_paths.length > 0 && project.feature_vectors_paths[0] != null)
                   {
                       feature_vectors_paths = new String[1];
                       feature_vectors_paths[0] = project.feature_vectors_paths[0];
                   }
                   if(project.taxonomy_path != null)
                       taxonomy_path = project.taxonomy_path;
                   data_board = new DataBoard( taxonomy_path,
                         feature_definitions_path,
                         feature_vectors_paths,
                         classifications_path);
               }
               else
                   data_board = new DataBoard();


               // Update GUI panes
               taxonomy_panel.loadNewPanelContents();
               instances_panel.loadNewPanelContents();
               feature_definitions_panel.loadNewPanelContents();
               gui_preferences_panel.loadNewPanelContents(project.gui_preferences_path);
               classification_settings_panel.loadNewPanelContents(project.classifier_settings_path);
               experimentation_panel.loadNewPanelContents(project.trained_classifiers_path);

               // Update the title of this frame
 //**              //setTitle("ACE           " + mckay.utilities.staticlibraries.StringMethods.convertFilePathToFileName(project_path));

               // Note that some data has been loaded
               project_previously_loaded = true;
          }
          catch (Exception e)
          {
               e.printStackTrace();
               JOptionPane.showMessageDialog(null, "Unable to load Configuration Files. "+ e.getMessage(), "ERROR ", JOptionPane.ERROR_MESSAGE);
               data_board = temp_data_board;
          }
     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Initialize this GUI elements of this frame.
      */
     private void prepareGUI()
     {
          // Initialize GUI
          setTitle("ACE");
          content_pane = getContentPane();
          tabbed_pane = new JTabbedPane();

          // Set up menus
          load_configuration_files_menu_item = new JMenuItem("Load Configuration Files");
          load_configuration_files_menu_item.addActionListener(this);
          load_configuration_files_menu_item.setMnemonic('L');
          save_project_menu_item = new JMenuItem("Save Project");
          save_project_menu_item.addActionListener(this);
          save_project_menu_item.setMnemonic('S');
          save_project_paths_menu_item = new JMenuItem("Save Project Paths");
          save_project_paths_menu_item.addActionListener(this);
          save_project_paths_menu_item.setMnemonic('P');
          load_zip_item = new JMenuItem("Load ACE Zip File");
          load_zip_item.addActionListener(this);
          load_zip_item.setMnemonic('Z');
          quit_menu_item = new JMenuItem("Quit");
          quit_menu_item.addActionListener(this);
          quit_menu_item.setMnemonic('Q');
          file_menu = new JMenu("File");
          file_menu.setMnemonic('F');
          file_menu.add(load_configuration_files_menu_item);
          file_menu.add(save_project_menu_item);
          file_menu.add(save_project_paths_menu_item);
          file_menu.add(load_zip_item);
          file_menu.add(quit_menu_item);
          about_menu_item = new JMenuItem("About");
          about_menu_item.addActionListener(this);
          about_menu_item.setMnemonic('A');
          view_manual_menu_item = new JMenuItem("View Manual");
          view_manual_menu_item.addActionListener(this);
          view_manual_menu_item.setMnemonic('M');
          help_menu = new JMenu("Help");
          help_menu.setMnemonic('H');
          help_menu.add(about_menu_item);
          help_menu.add(view_manual_menu_item);
          menu_bar = new JMenuBar();
          menu_bar.add(file_menu);
          menu_bar.add(help_menu);
          setJMenuBar(menu_bar);

          // Set up tabbed panes
          taxonomy_panel = new TaxonomyPanel(this);
          tabbed_pane.addTab("Taxonomy", taxonomy_panel);
          feature_definitions_panel = new FeatureDefinitionsPanel(this);
          tabbed_pane.addTab("Features", feature_definitions_panel);
          instances_panel = new InstancesPanel(this);
          tabbed_pane.addTab("Instances", instances_panel);
          gui_preferences_panel = new GUIPreferencesPanel(this);
          tabbed_pane.addTab("Preferences", gui_preferences_panel);
          classification_settings_panel = new ClassificationSettingsPanel(this);
          tabbed_pane.addTab("Classification Settings", classification_settings_panel);
          experimentation_panel = new ExperimentationPanel(this);
          tabbed_pane.addTab("Experimenter", experimentation_panel);
          content_pane.add(tabbed_pane);

          // Set the default selected pane to the instances_panel
          tabbed_pane.setSelectedIndex(2);

          // Set up help dialog box
          String help_path = "ProgramFiles" + File.separator + "Help" + File.separator;
          mckay.utilities.gui.templates.HelpDialog help_dialog =
               new mckay.utilities.gui.templates.HelpDialog(help_path + "contents.html", help_path + "title_page.html");

          // Cause program to quit when the exit box is pressed
          addWindowListener(new WindowAdapter()
          {
               public void windowClosing(WindowEvent e)
               {
                   if (zipfile_loaded)
                   {
                       // Ask if user would like to save contents of zipfile
                       project_files_dialog_box.saveZipPrompt();
                   }
                   // Delete any temporary files that were used during this session
                   deleteTempFiles();
                   System.exit(0);
               }
          });

          // Combine GUI elements into this frame at the left corner of the
          // screen with a size of 800 x 600
          setBounds(0, 0, 800, 600);

          // Note that no project files have been previously loaded
          project_previously_loaded = false;
     }

     /**
      * Save all project files in the ProjectFilesDialogBox to an ACE project
      * file and save all currently loaded data to the corresponding paths.
      */
     private void saveProject()
     {
          // Save the project paths
          project_files_dialog_box.saveCurrentProject();

          // Save the each of the configuration files that are related to the
          // project under the name referred to in
          taxonomy_panel.saveAsFile(project.taxonomy_path);
          instances_panel.saveAsFeatureVectorsFile(project.feature_vectors_paths[0]);
          instances_panel.saveAsClassificationsFile(project.classification_paths[0]);
          feature_definitions_panel.saveAsFile(project.feature_settings_paths[0]);
          gui_preferences_panel.saveAsFile(project.gui_preferences_path);
          classification_settings_panel.saveAsFile(project.classifier_settings_path);
          experimentation_panel.saveAsFile(project.trained_classifiers_path);
     }

     /**
      * This method is called whenever the ACE GUI is closing. Any temporary files
      * that were created during this session (for example, the temporary directory
      * into which contents of a zip file would be extracted) will now be deleted.
      * The user will be prompted to save their data prior to this method being called.
      * In many cases there will be no temporary files to be deleted.
      */
     private void deleteTempFiles()
     {
         if (temp_files != null)
         {
             for (int i = 0; i < temp_files.size(); i++)
             {
                 File to_del = new File(temp_files.get(i));
                 if (to_del.isDirectory())
                     mckay.utilities.staticlibraries.FileMethods.deleteDirectoryRecursively(to_del);
                 else
                     to_del.delete();
             }
         }
     }

/**
 * Test method to check contents of fields.
 */
private void testStoredPaths()
{
     System.out.println("project_path: " + project_files_dialog_box.project_path);
     System.out.println("use_arff_rather_than_ace_xml: " + project_files_dialog_box.use_arff_rather_than_ace_xml);
     System.out.println("taxonomy_path: " + project.taxonomy_path);
     System.out.println("feature_definitions_path: " + project.feature_settings_paths[0]);
     for (int i = 0; i < project.feature_vectors_paths.length; i++)
          System.out.println("feature_vectors_paths " + i + ": " + project.feature_vectors_paths[i]);
     System.out.println("model_classifications_path: " + project.classification_paths[0]);
     System.out.println("gui_preferences_path: " + project.gui_preferences_path);
     System.out.println("classifier_settings_path: " + project.classifier_settings_path);
     System.out.println("trained_classifiers_path: " + project.trained_classifiers_path);
     System.out.println("arff_path: " + project.weka_arff_path);
}
}