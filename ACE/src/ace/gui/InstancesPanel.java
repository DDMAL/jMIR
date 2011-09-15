/*
 * InstancesPanel.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.gui;

import java.io.File;
import java.util.LinkedList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import ace.datatypes.*;
import mckay.utilities.gui.tables.*;


/**
 *
 * An object of this class represents a panel in the ACE GUI that allows users
 * to view, edit, load and save the classifications and feature values of a data
 * set. This information relates to the actual feature values, not the features
 * themselves. The <i>FeatureDefinitionsPanel</i> should be consulted if one wishes
 * to view information about the features themselves.
 *
 * <p>The instances are displayed in table form. The contents of any row may be changed by double clicking on any
 * entry on the table (or clicking once, in the case of check boxes). Columns
 * may also be reordered by dragging their headings. All instances must have
 * unique identifiers and must have at least 1 dimension. Any attempt to enter invalid
 * entries will be rejected by the system. If feature vectors and classifications
 * are loaded for the same instance, the data will be merged into one row. By
 * default, feature values and miscellaneous information is not displayed. The
 * checkboxes <i>Display Feature Values</i> and <i>Show Misc Info</i> must be
 * selected for this information to be displayed.
 *
 * <p>If a data set with subsections has been loaded, the <i>Show Subsections</i> column will contain
 * checkboxes for each instance that has subsections that, when selected, will display all of the sub-sections
 * of that instance.
 * The <i>identifier</i> column specifies the unique name of a instance. The
 * <i>Start</i> column indicates the point in time (in seconds) that an subsection of an instance begins. The <i>Stop</i> column
 * specifies the point in time (in seconds) that a subsection of an instance ends. The
 * <i>Classes</i> column should only displayed if an ACE XML classifications file has been loaded and will display the given classification(s) for each instance.
 *
 * <p>The status bar displays the total number of loaded feature vectors and the total number of loaded classifications.
 *
 * <p>The <i>Display Feature Values</i> button causes the feature values columns to become visible.
 *
 * <p>The <i>Show Misc Info</i> button causes columns containing miscellaneous
 * information to become visible.
 *
 * <p>The <i>Add Instance</i> button causes a new instance to be added to the end
 * of the table. It is given a unique name.
 *
 * <p>The <i>Delete Instance</i> button causes the selected instance to be
 * deleted from the table.
 *
 * <p>The <i>Load Feature Vectors</i> button loads a feature_vectors_file ACE XML file into
 * memory and displays it. If feature values are already loaded, the user will be given a prompt
 * asking them if they want to continue and previously loaded values will be overwritten.
 *
 * <p>The <i>Load Classifications</i> button loads a model_classifications_file ACE XML file into
 * memory and displays it. If classifications are already loaded, the user will be given a prompt
 * asking them if they want to continue and previously loaded classes will be overwritten.
 *
 * <p>The <i>Save Feature Vectors</i> button saves the currently loaded feature
 * values into the path referred to in the <i>File Path Settings</i> dialog
 * box as a feature_vectors_file ACE XML file. The <i>Save Feature Vectors As</i> button allows the
 * user to choose the path to which the file is to be saved and updates the
 * path in the <i>File Path Settings</i> dialog box.
 *
 * <p>The <i>Save Classifications</i> button saves the currently loaded classifications into the path
 * referred to in the <i>File Path Settings</i> dialog
 * box as a classifications_file ACE XML file. The <i>Save Classifications As</i> button allows the
 * user to choose the path to which the file is to be saved and updates the
 * path in the <i>File Path Settings</i> dialog box.
 *
 * <p>The loadNewPanelContents method can be called by external objects to load instances.
 *
 * <p>The actual feature values and classifications are not stored in an object of this class,
 * but are instead stored in a DataBoard object stored in the MainGUIFrame
 * object that holds the InstancesPanel.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class InstancesPanel
     extends JPanel
     implements ActionListener, TableModelListener
{
     /* FIELDS ****************************************************************/


     // The JFrame that holds this JPanel.
     private	MainGUIFrame            parent;

     // Fields relating to the table used to display the instances
     private	JTable			instances_table;

     // Display panels
     private   JPanel			instances_display_panel;
     private   JPanel			button_panel;
     private   JScrollPane		instances_display_scroll_pane;

     // Displays general messages
     private   JTextField		status_bar;

     //Buttons
     private JButton                    load_classification_button;
     private JButton                    load_feature_vectors_button;
     private JButton                    save_classification_button;
     private JButton                    save_feature_vectors_button;
     private JButton                    save_as_classification_button;
     private JButton                    save_as_feature_vectors_button;
     private JButton                    add_instance_button;
     private JButton                    delete_instance_button;

     //Checkboxes
     private JCheckBox                  display_feature_values_checkbox;
     private JCheckBox                  show_misc_info_checkbox;

     // Value of above checkboxes
     private boolean display_meta_data;
     private boolean display_feature_values;


     /* CONSTRUCTORS **********************************************************/

     /**
      * Basic constructor.
      *
      * @param	parent_frame	The JFrame that holds this JPanel.
      */
     public InstancesPanel(MainGUIFrame parent_frame)
     {
          // Note the parent of this window
          parent = parent_frame;

          // Initialize layout settings
          setLayout(new BorderLayout());
          int horizontal_gap = 4; // horizontal space between GUI elements
          int vertical_gap = 4; // horizontal space between GUI elements

          // Set up status bar
          status_bar = new JTextField("");
          status_bar.setEditable(false);
          add(status_bar, BorderLayout.SOUTH);

          // Set up buttons
          load_classification_button = new JButton("Load Classifications");
          load_classification_button.addActionListener(this);
          load_feature_vectors_button = new JButton("Load Feature Vectors");
          load_feature_vectors_button.addActionListener(this);
          save_classification_button = new JButton("Save Classifications");
          save_classification_button.addActionListener(this);
          save_feature_vectors_button = new JButton("Save Feature Vectors");
          save_feature_vectors_button.addActionListener(this);
          save_as_classification_button = new JButton("Save Classifications As");
          save_as_classification_button.addActionListener(this);
          save_as_feature_vectors_button = new JButton("Save Feature Vectors As");
          save_as_feature_vectors_button.addActionListener(this);
          add_instance_button = new JButton("Add Instance");
          add_instance_button.addActionListener(this);
          delete_instance_button = new JButton("Delete Instance");
          delete_instance_button.addActionListener(this);

          //Set up checkboxes
          display_feature_values_checkbox = new JCheckBox("Display Feature Values");
          display_feature_values_checkbox.addActionListener(this);
          show_misc_info_checkbox = new JCheckBox("Display Misc Info");
          show_misc_info_checkbox.addActionListener(this);

          // Set up button/checkbox panel
          button_panel = new JPanel(new GridLayout(18, 1, horizontal_gap, vertical_gap));
          button_panel.add(load_classification_button);
          button_panel.add(load_feature_vectors_button);
          button_panel.add(new JLabel(""));
          button_panel.add(save_classification_button);
          button_panel.add(save_feature_vectors_button);
          button_panel.add(save_as_classification_button);
          button_panel.add(save_as_feature_vectors_button);
          button_panel.add(new JLabel(""));
          button_panel.add(add_instance_button);
          button_panel.add(delete_instance_button);
          button_panel.add(new JLabel(""));
          button_panel.add(display_feature_values_checkbox);
          button_panel.add(show_misc_info_checkbox);
          add(button_panel, BorderLayout.EAST);

     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Calls the appropriate methods when the buttons/checkboxes are pressed/checked.
      *
      * @param	event		The event to react to.
      */
     public void actionPerformed(ActionEvent event)
     {
         // React to the load_classification_button
          if (event.getSource().equals(load_classification_button))
          {
              browseNewClassifications();
          }

          // React to the load_feature_vectors_button
          else if (event.getSource().equals(load_feature_vectors_button))
          {
              browseNewFeatureVectors();
          }
          // React to the save_classification_button
          else if (event.getSource().equals(save_classification_button))
              saveAsClassificationsFile(parent.project.classification_paths[0]);
          // React to the save_feature_vectors_button
          else if (event.getSource().equals(save_feature_vectors_button))
              saveAsFeatureVectorsFile(parent.project.feature_vectors_paths[0]);
          // React to the save_as_classification_button
          else if (event.getSource().equals(save_as_classification_button))
              saveAsClassificationsFileWithBrowse();
          // React to the save_as_feature_vectors_button
          else if (event.getSource().equals(save_as_feature_vectors_button))
              saveAsFeatureVectorsFileWithBrowse();
          // React to the add_instance_button
          else if (event.getSource().equals(add_instance_button))
              addNewInstance();
          // React to the delete_instance_button
          else if (event.getSource().equals(delete_instance_button))
              deleteInstances();
          // React to display_feature_values_checkbox
          else if(event.getSource().equals(display_feature_values_checkbox))
          {
              if(display_feature_values_checkbox.isSelected())
                display_feature_values = true;
              else
                  display_feature_values = false;
              loadNewPanelContents();
          }
          // React to show_misc_info_checkbox
          else if(event.getSource().equals(show_misc_info_checkbox))
          {
              if(show_misc_info_checkbox.isSelected())
                  display_meta_data = true;
              else
                  display_meta_data = false;
              loadNewPanelContents();
          }
     }


     /**
      * Cause this panel to update to reflect the contents of parent.data_board.
      */
     public void loadNewPanelContents()
     {
         /* Before clearing the contents of the table, take note of which checkboxes
         in the "Show Sections" column are checked */
         String[] identifiers_to_be_expanded = getIdentifiersToBeExpanded(instances_table);
         if(parent.data_board.model_classifications != null && (parent.data_board.taxonomy == null || parent.data_board.taxonomy.isTreeEmpty()))
         {
             parent.data_board.taxonomy = Taxonomy.generateTaxonomy(parent.data_board.model_classifications);
             parent.taxonomy_panel.loadNewPanelContents();
         }
         if(parent.data_board.feature_vectors != null &&
                 ((parent.data_board.feature_definitions == null)
                 || !(parent.data_board.feature_definitions.length > 0)))
         {
             try
             {
             parent.data_board.feature_definitions = FeatureDefinition.
                     generateFeatureDefinitions(parent.data_board.feature_vectors);
             }
             catch(Exception e)
             {
                JOptionPane.showMessageDialog(null, "Unable to automatically generate " +
                        "feature definitions from feature vectors.  " + e.getMessage(),
                        "ERROR", JOptionPane.ERROR_MESSAGE);
             }
             parent.feature_definitions_panel.loadNewPanelContents();
         }


          // Remove anything on the left side of the panel
          if (instances_display_panel != null)
               remove(instances_display_panel);

          // Set up the table model
          InstancesTableModel instances_table_model = new InstancesTableModel();
          instances_table_model.addTableModelListener(this);

          // Fill table based on contents of parent.data_board and boolean options
          try
          {
            instances_table_model.fillTable(parent.data_board, identifiers_to_be_expanded, display_meta_data, display_feature_values);
          }
          catch(Exception e)
          {
              JOptionPane.showMessageDialog(null, "Unable to Fill Table.  " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
          }

          // Set up the table and make it sortable
          instances_table = new JTable(instances_table_model);
          //TableSortModel sort_model = new TableSortModel(instances_table);

          // Make the class column able to have multiple lines per row
          // Set up Show Sections column
          int class_column_indice = -1;
          int show_sections_column_indice = -1;
          for (int i = 0; i < instances_table_model.getColumnCount(); i++)
          {
              if (instances_table_model.getColumnName(i).equals("Classes"))
              {
                  class_column_indice = i;
              }
              if (instances_table_model.getColumnName(i).equals("Show Sections"))
                  show_sections_column_indice = i;
          }

          TableColumnModel column_model = instances_table.getColumnModel();
          if (class_column_indice != -1)
          {
             MultiLineCellRenderer multi_line_renderer = new MultiLineCellRenderer(SwingConstants.LEFT,
                  SwingConstants.CENTER);
             column_model.getColumn(class_column_indice).setCellRenderer(multi_line_renderer);
          }
          if(show_sections_column_indice != -1)
          {
              String[] expandable = getExpandable(parent.data_board);
              ShowSectionsColumnRenderer show_sections_renderer = new ShowSectionsColumnRenderer(expandable);
              column_model.getColumn(show_sections_column_indice).setCellRenderer(show_sections_renderer);
          }

             // Set the proper row height based on the maximum number of classes in
             // a cell
             /*int number_rows = instances_table.getRowCount();
             int max_number_classes = 1;
             for (int i = 0; i < number_rows; i++)
             {
                 Object[] classes = (Object[]) instances_table.getModel().getValueAt(i, class_column_indice);
                 if (classes.length > max_number_classes)
                 {
                     max_number_classes = classes.length;
                 }
             }
             int line_height = instances_table.getFontMetrics(instances_table.getFont()).getHeight();
             int row_height = max_number_classes * line_height;
             instances_table.setRowHeight(row_height + 1);*/


          // Make the table striped
         StripedTableCellRenderer.installInTable( instances_table,
               Color.lightGray,
               Color.white,
               null,
               null );
//I'm not sure that below makes it look any better
          // Adjust the column widths
		//setColumnWidths();

          //Add listener to the table
		instances_table.getModel().addTableModelListener(this);

          // Set up the display panel
          instances_display_scroll_pane = new JScrollPane(instances_table);
          instances_display_panel = new JPanel(new GridLayout(1, 1));
          instances_display_panel.add(instances_display_scroll_pane);
          add(instances_display_panel, BorderLayout.CENTER);

          // Update the status bar
          updateStatusBar();

          // Display the table and update the status bar
          instances_table_model.fireTableDataChanged();
          repaint();
          parent.repaint();



// UPDATE GUI

// must be able to deal with empty contents
     }

     /**
      * Saves the classifications displayed in the Instances Panel to an ACE XML
      * model classifications file with the given name. User is not given option
      * of specifiying save path; classifications are saved in the same file from
      * which they came (unless save_path parameter is empty, in which case the
      * user is given the chance to select a save path).
      *
      * @param save_path    The path to which the model classifications displayed
      *                     in the GUI should be saved.
      */
     public void saveAsClassificationsFile(String save_path)
     {
         if (parent.data_board.getModelClassifications().length == 0)
               JOptionPane.showMessageDialog(null, "There are not currently any model classifications to save.", "WARNING", JOptionPane.WARNING_MESSAGE);
          else
          {
               if (!save_path.equals(""))
               {
                    // Get file to write to
                    File save_file = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(save_path, true);

                    // Save the file
                    try
                    {
                         SegmentedClassification.saveClassifications( parent.data_board.getModelClassifications(),
                              save_file,
                              "" );
                    }
                    catch (Exception e)
                    {
                         // e.printStackTrace();
                         JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
               }
               else
                    saveAsClassificationsFileWithBrowse();
          }
     }

     /**
      * Saves the currently loaded feature vectors to the original file without
      * giving the user the option to choose a new file path (unless save_path parameter
      * is empty, in which case the user is given the chance to select a save path).
      *
      * @param save_path    The path to which the feature vectors will be saved.
      */
     public void saveAsFeatureVectorsFile(String save_path)
     {
         if (parent.data_board.getFeatureVectors().length == 0)
               JOptionPane.showMessageDialog(null, "There are not currently any feature values to save.", "WARNING", JOptionPane.WARNING_MESSAGE);
          else
          {
               if (!save_path.equals(""))
               {
                    // Get file to write to
                    File save_file = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(save_path, true);

                    // Save the file
                    try
                    {
                         DataSet.saveDataSets( parent.data_board.getFeatureVectors(), parent.data_board.getFeatureDefinitions(),
                              save_file,
                              "" );
                    }
                    catch (Exception e)
                    {
                         // e.printStackTrace();
                         JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
               }
               else
                    saveAsFeatureVectorsFileWithBrowse();
          }
     }
//concerning save methods:
// if multiple vectors_save_paths are given, should only use one, and update project_files_dialog_box to reflect this

// must be able to deal with not saving anything if field of this object is null

// must be able to deal with case where there is something to save but no save_path specified in parameter



     /**
      * Updates the table whenever a checkbox in the "Show Sections" column is
      * checked or unchecked.
      *
      *
      * formerly:
      * A listener that causes the parent.data_board.feature_definitions to
      * reflect the contents of the table whenever the contents of the table are
      * changed. The status bar is also updated to reflect any changes.
      *
      * <p>The listener also ensures that only valid input is entered by the
      * user. All features must have dimensions of at least 1 and all feature
      * names must be unique. The users change is discarded if an invalid entry
      * is entered.
      *
      * <p>Columns are also automatically resized if necessary, based on the new
      * contents.
      *
      * @param	event	An event indicating the change in the table.
      */
     public void tableChanged(TableModelEvent event)
     {
         if (event.getColumn() == 0 )
         loadNewPanelContents();/* boolean accept_change = true;
          String error_message = "";

          if (event.getType() == TableModelEvent.UPDATE)
          {
               int column = event.getColumn();
               int first_row = event.getFirstRow();
               if (column == 1)
               {
                    SegmentedClassification[] old_classes = parent.data_board.model_classifications;
                    updateModelClassifications();
                    boolean duplicates = SegmentedClassification.verifyUniquenessOfIdentifiers(parent.data_board.model_classifications);
                    if (!duplicates)
                    {
                         error_message = "You have entered a feature name of " + instances_table.getModel().getValueAt(first_row, column) + ".\n" +
                              "A classification with this name already exists.";
                         parent.data_board.model_classifications = old_classes;
                         accept_change = false;
                    }
               }
               else if (column == 2)
                    if (((Integer) instances_table.getModel().getValueAt(first_row, column)).intValue() < 1)
                    {
                    error_message = "You have entered a dimension value of " + instances_table.getModel().getValueAt(first_row, column) + ".\n" +
                         "Each feature must have at least 1 dimension.";
                    accept_change = false;
                    }
          }

          if (accept_change)
          {
               updateModelClassifications();
               updateFeatureVectors();
               updateStatusBar();
               setColumnWidths();
          }
          else
          {
               JOptionPane.showMessageDialog(null, error_message, "ERROR", JOptionPane.ERROR_MESSAGE);
               loadNewPanelContents();
          }*/
     }

     /* PRIVATE METHODS *******************************************************/

     /**
      * Presents a dialog box that allows the user to specify an ACE XML model
      * classifications file to be loaded. The given file is parsed and loaded into
      * the DataBoard.
      *
      * @throws Exception   if unable to parse the given ACE XML classification file.
      */
     private void browseNewClassifications()
     {
          boolean ok_pressed = parent.project_files_dialog_box.browsePath("model_classifications", true);
          if (ok_pressed)
          {
               String classifications_path = parent.project.classification_paths[0];
               try
               {
                    parent.data_board.model_classifications = SegmentedClassification.parseClassificationsFile(classifications_path);
               }
               catch (Exception e)
               {
                    // e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
               }

                loadNewPanelContents();
          }
     }

     /**
      * Presents a dialog box that allows the user to specify an ACE XML feature
      * vectors file to be loaded. The given file is parsed and loaded into the
      * DataBoard.
      *
      * @throws Exception   if unable to parse the given ACE XML feature vectors
      *                     file.
      */
     private void browseNewFeatureVectors()
     {
          boolean ok_pressed = parent.project_files_dialog_box.browsePath("feature_vectors", true);
          if (ok_pressed)
          {
               String feature_vectors_path = parent.project.feature_vectors_paths[0];
               try
               {
                    parent.data_board.feature_vectors = DataSet.parseDataSetFile(feature_vectors_path, parent.data_board.feature_definitions);
               }
               catch (Exception e)
               {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
               }

                loadNewPanelContents();
          }
     }

     /**
      * Save the currently loaded feature definitions (as stored in
      * parent.data_board) into an ACE feature_key_file XML file. The path is
      * chosen using a browse dialog box. The name of the saved file (without
      * directory information or extension) is stored in the
      * ProjectFilesDialogBox.
      */
     private void saveAsClassificationsFileWithBrowse()
     {

          if (parent.data_board.getModelClassifications().length == 0)
               JOptionPane.showMessageDialog(null, "There are not currently any classifications to save.", "WARNING", JOptionPane.WARNING_MESSAGE);
          else
          {
               // Get file to write to through a dialog box
               File save_file = parent.project_files_dialog_box.saveFileExternally("model_classifications", false);

               // Save the file
               if (save_file != null)
               {
                    try
                    {
                         SegmentedClassification.saveClassifications( parent.data_board.getModelClassifications(),
                              save_file,
                              "" );
                    }
                    catch (Exception e)
                    {
                         // e.printStackTrace();
                         JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
               }
          }
     }

     /**
      * Save the currently loaded feature definitions (as stored in
      * parent.data_board) into an ACE feature_key_file XML file. The path is
      * chosen using a browse dialog box. The name of the saved file (without
      * directory information or extension) is stored in the
      * ProjectFilesDialogBox.
      */
     private void saveAsFeatureVectorsFileWithBrowse()
     {

          if (parent.data_board.getFeatureVectors().length == 0)
               JOptionPane.showMessageDialog(null, "There are not currently any feature vectors to save.", "WARNING", JOptionPane.WARNING_MESSAGE);
          else
          {
               // Get file to write to through a dialog box
               File save_file = parent.project_files_dialog_box.saveFileExternally("feature_vectors", false);

               // Save the file
               if (save_file != null)
               {
                    try
                    {
                         DataSet.saveDataSets( parent.data_board.getFeatureVectors(), parent.data_board.getFeatureDefinitions(),
                              save_file,
                              "" );
                    }
                    catch (Exception e)
                    {
                         e.printStackTrace();
                         JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
               }
          }
     }

      /**
       * **** This should be altered to accomodate different number of columns.
       *
       *
      * Adds a new feature to the end of the table. It is automatically given
      * the name New Feature, possibly followed by a number to ensure its
      * uniqueness.
      */
     private void addNewInstance()
     {
          // Find the new name of the feature and make sure that it is unique
          String new_name = "New Instance";
          int label_number = 1;
          String[] names = new String[instances_table.getRowCount()];
          for (int i = 0; i < names.length; i++)
               names[i] = (String) instances_table.getModel().getValueAt(i, 0);
          boolean found = true;
          while (found)
          {
               found = false;
               for (int i = 0; i < names.length; i++)
                    if (new_name.equals(names[i]))
                         found = true;
               if (found)
               {
                    new_name = "New Feature " + label_number;
                    label_number++;
               }
          }

          // Set up feature
          Object[] new_feature = new Object[5];
          new_feature[0] = new JCheckBox();
          new_feature[1] = new_name;
          new_feature[2] = new String("-");
          new_feature[3] = new String("-");
          new_feature[4] = new String("");

          // Add the feature
          ((TableSortModel) instances_table.getModel()).addRow(new_feature);
     }

     /**
      * Deletes all features selected on the table.
      */
     private void deleteInstances()
     {
          int[] rows = instances_table.getSelectedRows();
          if (rows.length == 0)
               JOptionPane.showMessageDialog(null, "No instances are selected to delete.", "WARNING", JOptionPane.WARNING_MESSAGE);
          ((TableSortModel) instances_table.getModel()).removeRows(rows);
     }

     /**
      * Updates parent.data_board.model_classifications to reflect the contents of
      * the instances_table.
      *
      * //NOTE THIS MIGHT NOT WORK
      * EDIT: definitely won't work anymore
      */
     private void updateModelClassifications()
     {
          /*TableSortModel table_model = (TableSortModel) instances_table.getModel();
          parent.data_board.model_classifications = new SegmentedClassification[table_model.getRowCount()];
          for (int i = 0; i < table_model.getRowCount(); i++)
               parent.data_board.model_classifications[i] = new SegmentedClassification( (String) table_model.getValueAt(i, 1),
                    (Double) table_model.getValueAt(i, 2),
                    (Double) table_model.getValueAt(i, 3),
                    (String[]) table_model.getValueAt(i, 4),
                    null, null, null);*/
     }

     /**
      * Updates parent.data_board.feature_vectors to reflect the contents of
      * the instances_table.
      */
    @SuppressWarnings("empty-statement")
     private void updateFeatureVectors()
     {

     ;
          /*TableSortModel table_model = (TableSortModel) instances_table.getModel();
          parent.data_board.feature_vectors = new DataSet[table_model.getRowCount()];
          for (int i = 0; i < table_model.getRowCount(); i++)
               parent.data_board.feature_vectors[i] = new DataSet( (String) table_model.getValueAt(i, 1),
                    null, (Double) table_model.getValueAt(i, 2),
                    (Double) table_model.getValueAt(i, 3),
                    (double[][]) table_model.getValueAt(i, 4),
                    (String[]) table_model.getValueAt(i, 5), null);*/
     }

     /**
      * Causes the status bar to display the number of features, including the
      * the total number of features that are multi-dimensional and the number
      * that are sequential.
      */
     private void updateStatusBar()
     {
          int number_classifications = 0;
          if (parent.data_board.getModelClassifications() != null)
          {
               for (int i = 0; i < parent.data_board.getModelClassifications().length; i++)
               {
                    number_classifications++;


               }
          }
          int number_feature_vectors = 0;
          if (parent.data_board.getFeatureVectors() != null)
          {
               for (int i = 0; i < parent.data_board.getFeatureVectors().length; i++)
               {
                    number_feature_vectors++;


               }
          }

          String status_text = new String(number_classifications + " classifications available, " + number_feature_vectors + " feature values available" );
          status_bar.setText(status_text);
     }

     /**
      * Initializes the column widths so that they fit the column headings and
      * column contents. Prevents any columns from being greater than roughly
      * 25 characters wide, however. None of this applies to theDescription
      * column, which will automatically be sized to fill the left-over space
      * from the other three columns.
      */
     private void setColumnWidths()
     {
         // This is no longer appropriate
          int max_width = 25;
          TableColumn[] columns_to_set_sizes= { instances_table.getColumn("Show Sections"),
          instances_table.getColumn("Identifier"),
          instances_table.getColumn("Start"),
          instances_table.getColumn("Stop")};
          TableSortModel.setColumnWidths(instances_table, columns_to_set_sizes, max_width);
     }

     /**
      * This is used for determining which lines in the Instances Table should have
      * a "Show Sections" checkbox present.
      * <p>
      * If feature vectors are present, returns an array of booleans, one for each
      * instance in the feature vectors file, indicating whether or not each instance
      * is expandable (has sub-sections). If only classifications are loaded, then
      * each cell of the returned boolean array will correspond to a different
      * SegmentedClassification. If an overall instance has sub sections, its corresponding
      * boolean will be set to true. Otherwise it will be set to false. If the instance
      * is a subsection, its corresponding boolean will be false.
      *
      * @param data_board   The instances to be considered and displayed.
      * @return             An array of booleans, one for each instance (including
      *                     subsections), to indicate whether or not an instance
      *                     is expandable and should have a checkbox in the Instances
      *                     Panel.
      */
     private String[] getExpandable(DataBoard data_board)
     {
         // Add values to a LinkedLinked list
         LinkedList<String> expandable = new LinkedList<String>();
         if(data_board.feature_vectors != null && data_board.feature_vectors.length != 0)
         {
             for (int i = 0; i<data_board.feature_vectors.length; i++)
             {
                 if (data_board.feature_vectors[i].sub_sets != null)
                 {
                     expandable.add(data_board.feature_vectors[i].identifier);

                 }

             }
         }
         // If only model classifications are loaded, not feature vectors
         else if(data_board.model_classifications != null && data_board.model_classifications.length != 0)
         {
              for(int i = 0; i<data_board.model_classifications.length; i++)
              {
                  if (data_board.model_classifications[i].sub_classifications != null)
                  {
                      expandable.add(data_board.model_classifications[i].identifier);

                  }
              }
         }
         else
         {
             return null;
         }

         return expandable.toArray(new String[expandable.size()]);
     }

     /**
      * Gets list of identifiers of overall instances to be expanded. This list
      * is used to determine whether or not to display subsections for each overall
      * instance. For each row, if the "Show Sections" checkbox is checked, the
      * identifier for that row is added to the array to return.
      *
      * @param instances_table      The table from which to get the identifiers.
      * @return                     List containing identifiers of all overall instances
      *                             to be expanded (show subsections).
      */
     private String[] getIdentifiersToBeExpanded(JTable instances_table)
     {
         LinkedList <String> identifiers_to_be_expanded = new LinkedList <String>();
         // table has not yet been filled
         if (instances_table==null || instances_table.getRowCount() ==0)
         {
             return null;
         }
         else if (instances_table.getColumnName(0).equals("Show Sections") && instances_table.getRowCount() > 0)
         {
            for(int i = 0; i<instances_table.getRowCount(); i++)
            {
                // Look for checkboxes
                if(instances_table.getValueAt(i,0) instanceof Boolean)
                {
                    // Keep track of the checkboxes that are checked
                if(((Boolean)instances_table.getValueAt(i, 0)).booleanValue() == true)
                    identifiers_to_be_expanded.add(instances_table.getValueAt(i, 1).toString());
                }
                else if (instances_table.getValueAt(i,0) instanceof String)
                    return null;
            }
         return identifiers_to_be_expanded.toArray(new String[1]);
         }
         else
            return null;

     }

}