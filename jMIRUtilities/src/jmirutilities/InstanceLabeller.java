/*
 * Main.java
 * Version 1.4
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jmirutilities;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.LinkedList;
import mckay.utilities.general.FileFilterImplementation;
import mckay.utilities.staticlibraries.StringMethods;
import ace.datatypes.SegmentedClassification;


/**
 * A basic piece of software for labelling audio files and saving the resulting
 * model classifications in an ACE XML file.
 *
 * @author Cory McKay
 */
public class InstanceLabeller
     extends JFrame
     implements ActionListener
{
     /* FIELDS ******************************************************************/
     
     
     /**
      * Holds references to model classifications to save.
      */
     public	LinkedList<SegmentedClassification>	recording_list;
     
     /**
      * GUI buttons
      */
     private JButton								add_recordings_button;
     private JButton								clear_button;
     private JButton								done_button;
     private JButton								cancel_button;
     
     /**
      * GUI text fields
      */
     private JTextField							current_label_text_field;
     
     /**
      * GUI dialog boxes
      */
     private JFileChooser						add_recordings_chooser;
     private JFileChooser						save_file_chooser;
     
     
     /* CONSTRUCTOR *************************************************************/
     
     
     /**
      * Set up panel.
      */
     public InstanceLabeller()
     {
          // Initialize fields
          recording_list = new LinkedList<SegmentedClassification>();
          add_recordings_chooser = null;
          save_file_chooser = null;
          
          // Make quit when exit box pressed
          setDefaultCloseOperation(EXIT_ON_CLOSE);
          
          // General container preparations containers
          int horizontal_gap = 6; // horizontal space between GUI elements
          int vertical_gap = 11; // horizontal space between GUI elements
          setLayout(new BorderLayout(horizontal_gap, vertical_gap));
          
          // Set up the current_label_text_field
          JPanel text_panel = new JPanel(new BorderLayout(horizontal_gap, vertical_gap));
          current_label_text_field = new JTextField();
          text_panel.add(new JLabel("Class:"), BorderLayout.WEST);
          text_panel.add(current_label_text_field, BorderLayout.CENTER);
          add(text_panel, BorderLayout.NORTH);
          
          // Set up buttons
          JPanel button_panel = new JPanel(new GridLayout(1, 4, horizontal_gap, vertical_gap));
          add_recordings_button = new JButton("Add Recordings");
          add_recordings_button.addActionListener(this);
          button_panel.add(add_recordings_button);
          clear_button = new JButton("Clear");
          clear_button.addActionListener(this);
          button_panel.add(clear_button);
          cancel_button = new JButton("Cancel");
          cancel_button.addActionListener(this);
          button_panel.add(cancel_button);
          done_button = new JButton("Save");
          done_button.addActionListener(this);
          button_panel.add(done_button);
          add(button_panel, BorderLayout.SOUTH);
          
          // Display GUI
          pack();
          setVisible(true);
     }
     
     
     /* PUBLIC METHODS *****************************************************/
     
     
     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event		The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the clear_button
          if (event.getSource().equals(clear_button))
               clear();
          
          // React to the add_recordings_button
          if (event.getSource().equals(add_recordings_button))
               addRecordings();
          
          // React to the cancel_button
          else if (event.getSource().equals(cancel_button))
               cancel();
          
          // React to the done_button
          else if (event.getSource().equals(done_button))
               done();
     }
     
     
     /* PRIVATE METHODS ****************************************************/
     
     
     /**
      * Instantiates a JFileChooser for the load_recording_chooser field if
      * one does not already exist. This dialog box allows the user to choose one
      * or more files to add to the recording_list list of references to audio files.
      *
      * <p>Gives all of the selected files the label currently entered in the
      * current_label_text_field. If a file has already been loaded, then this label
      * is added to any existing labels for the file.
      *
      * <p>An error message is displayed if no class has been entered by the user.
      *
      * <p>If a given file path corresponds to a file that does not exist,
      * then an error message is displayed.
      */
     private void addRecordings()
     {
          // Store the class name to assign to all selected files
          String class_name = current_label_text_field.getText();
          
          // Proceed if a class name has been entered
          if (!class_name.equals(""))
          {
               // Initialize the load_recording_chooser if it has not been opened yet
               if (add_recordings_chooser == null)
               {
                    add_recordings_chooser = new JFileChooser();
                    add_recordings_chooser.setCurrentDirectory(new File("."));
                    add_recordings_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    add_recordings_chooser.setMultiSelectionEnabled(true);
               }
               
               // Read the user's choice of load or cancel
               int dialog_result = add_recordings_chooser.showOpenDialog(InstanceLabeller.this);
               
               // Add the files to the list
               if (dialog_result == JFileChooser.APPROVE_OPTION) // only do if OK chosen
               {
                    File[] load_files = add_recordings_chooser.getSelectedFiles();
                    addRecordings(load_files, class_name);
               }
          }
          else
          {
               JOptionPane.showMessageDialog(null, "Please enter a class name.", "ERROR", JOptionPane.ERROR_MESSAGE);
          }
     }
     
     
     /**
      * Adds the given files to the list of model classifications and gives them the
      * given class name. Each instance is given an identifier corresponding to its
      * path. If one of the given files has already been added to the list then the
      * new class name is added to any previous class names that were added.
      *
      * <p>Verifies that the files are valid audio files. If a given file path
      * corresponds to a file that does not exist, then an error message is displayed.
      *
      * @param	files_to_add	The files to add to the table.
      * @param	class_name		A class name to assign to each file.
      */
     private void addRecordings(File[] files_to_add, String class_name)
     {
          // Go through the files one by one
          for (int i = 0; i < files_to_add.length; i++)
          {
               // Verify that the file exists
               if ( files_to_add[i].exists() )
               {
                    try
                    {
                         // Extract the file path
                         String current_path = files_to_add[i].getPath();
                         
                         // Checks if this file has already been added to the list. If so, adds this
                         // class to its list of classes.
                         boolean file_already_added = false;
                         for (int j = 0; j < recording_list.size(); j++)
                         {
                              String this_path = ((SegmentedClassification) recording_list.get(j)).identifier;
                              if (current_path.equals(this_path))
                              {
                                   // Verify that the current label has not already been added
                                   String[] existing_labels = ((SegmentedClassification) recording_list.get(j)).classifications;
                                   boolean label_already_added = false;
                                   for (int k = 0; k < existing_labels.length; k++)
                                        if (existing_labels[k].equals(class_name))
                                        {
                                        label_already_added = true;
                                        k = existing_labels.length;
                                        }
                                   
                                   // Add the new label
                                   if (!label_already_added)
                                   {
                                        String[] new_labels = new String[existing_labels.length + 1];
                                        for (int k = 0; k < existing_labels.length; k++)
                                             new_labels[k] = existing_labels[k];
                                        new_labels[existing_labels.length] = class_name;
                                        ((SegmentedClassification) recording_list.get(j)).classifications = new_labels;
                                   }
                                   
                                   // Note that the file has already been found and updated, if necessary
                                   file_already_added = true;
                                   j = recording_list.size();
                              }
                         }
                         
                         // Add an instance if the file has not already been added
                         if (!file_already_added)
                         {
                              SegmentedClassification this_sc = new SegmentedClassification();
                              this_sc.identifier = current_path;
                              this_sc.classifications = new String[1];
                              this_sc.classifications[0] = class_name;
                              recording_list.add(this_sc);
                         }
                    }
                    catch (Exception e)
                    {
                         JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
               }
               else
               {
                    JOptionPane.showMessageDialog(null, "The selected file " + files_to_add[i].getName() + " does not exist.", "ERROR", JOptionPane.ERROR_MESSAGE);
               }
          }
     }
     
     
     /**
      * Erase any previously added instances.
      */
     private void clear()
     {
          recording_list = new LinkedList<SegmentedClassification>();
     }
     
     
     /**
      * Quit the program without saving.
      */
     private void cancel()
     {
          System.exit(0);
     }
     
     
     /**
      * Allow the user to save all added instances to a file chosen with a
      * JFileChooser.
      */
     private void done()
     {
          // Only proceed if there are instances to save
          if (recording_list.size() == 0)
          {
               JOptionPane.showMessageDialog(null, "Please add instances first", "ERROR", JOptionPane.ERROR_MESSAGE);
          }
          else
          {
               // Initialize the save_file_chooser if it has not been opened yet
               if (save_file_chooser == null)
               {
                    save_file_chooser = new JFileChooser();
                    save_file_chooser.setCurrentDirectory(new File("."));
                    String[] filters = {"xml"};
                    save_file_chooser.setFileFilter(new FileFilterImplementation(filters));
               }
               
               // Save the model classifications if the user chooses OK
               int dialog_result = save_file_chooser.showSaveDialog(InstanceLabeller.this);
               if (dialog_result == JFileChooser.APPROVE_OPTION) // only do if OK chosen
               {
                    // Prepare a temporary File
                    File save_file = save_file_chooser.getSelectedFile();
                    boolean proceed = true;
                    
                    // Verify that the file has the correct extension
                    String correct_extension = ".xml";
                    String path = save_file.getAbsolutePath();
                    String ext = mckay.utilities.staticlibraries.StringMethods.getExtension(path);
                    if (ext == null)
                    {
                         path += correct_extension;
                         JOptionPane.showMessageDialog(null, "Incorrect file extension specified.\nChanged from " + ext + " to " + correct_extension + ".", "WARNING", JOptionPane.ERROR_MESSAGE);
                         save_file = new File(path);
                    }
                    else if (!ext.equals(correct_extension))
                    {
                         path = mckay.utilities.staticlibraries.StringMethods.removeExtension(path) + correct_extension;
                         JOptionPane.showMessageDialog(null, "Incorrect file extension specified.\nChanged from " + ext + " to " + correct_extension + ".", "WARNING", JOptionPane.ERROR_MESSAGE);
                         save_file = new File(path);
                    }
                    
                    // See if user wishes to overwrite if a file with the same name exists
                    if (save_file.exists())
                    {
                         int overwrite = JOptionPane.showConfirmDialog( null,
                              "This file already exists.\nDo you wish to overwrite it?",
                              "WARNING",
                              JOptionPane.YES_NO_OPTION );
                         if (overwrite != JOptionPane.YES_OPTION)
                              proceed = false;
                    }
                    
                    // If appropriate, save the final File
                    if (proceed)
                    {
                         try
                         {
                              SegmentedClassification[] results = recording_list.toArray(new SegmentedClassification[1]);
                              SegmentedClassification.saveClassifications( results,
                                   save_file,
                                   "" );
                         }
                         catch (Exception e)
                         {
                              JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                         }
                    }
               }
          }
     }
}