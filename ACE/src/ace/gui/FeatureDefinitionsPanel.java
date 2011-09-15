/*
 * FeatureDefinitionsPanel.java
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
import javax.swing.event.*;
import javax.swing.table.*;
import ace.datatypes.*;
import mckay.utilities.gui.tables.*;


/**
 * An object of this class represents a panel in the ACE GUI that allows users
 * to view, edit, load and save feature definitions. This information relates
 * to features themselves, not to actual feature values extracted from features.
 * The <i>Instances Panel</i> should be consulted if one wishes to view ]
 * extracted feature values.
 *
 * <p>The features are displayed in table form. This table may be sorted based
 * on the contents of a given column by clicking on the appropriate column
 * heading. The contents of any row may be changed by double clicking on any
 * entry on the table (or clicking once, in the case of check boxes). Columns
 * may also be reordered by dragging their headings. All features must have
 * unique names and must have at least 1 dimension. Any attempt to enter invalid
 * entries will be rejected by the system.
 *
 * <p>The <i>Name</i> column specifies the name of a feature. The
 * <i>Dimensions</i> column indicates the number of separate values that are
 * stored each time the given feature is extracted. The <i>Sequential</i> column
 * specifies whether a feature can be applied to sub-section of a data set (e.g.
 * a window of audio). A value of true means that it can, and a value of false
 * means that the feature may only be extracted once per data set. The
 * <i>Description</i> column provides a brief description of each feature.
 *
 * <p>The status bar displays the total number of loaded feature definitions,
 * including informaiton on how many of them are multi-dimensional and how many
 * of them are sequential.
 *
 * <p>The <i>View Descriptions</i> button causes the descriptions of each
 * feature selected on the table to be displayed in a separate dialog box.
 *
 * <p>The <i>Add Feature</i> button causes a new feature to be added to the end
 * of the table. It is given a unique name.
 *
 * <p>The <i>Delete Features</i> button causes all selected features to be
 * deleted from the table.
 *
 * <p>The <i>Load Features</i> button loads a feature_key_file ACE XML file into
 * memory and displays it. This overwrites any existing feature definitions.
 *
 * <p>The <i>Save Features</i> button saves the currently loaded feature
 * definitions into the path referred to in the <i>File Path Settings</i> dialog
 * box as a feature_key_file ACE XML file. The <i>Save As</i> button allows the
 * user to choose the path to  which the file is to be saved and updates the
 * path in the <i>File Path Settings</i> dialog box.
 *
 * <p>The loadNewPanelContents and saveAsFile methods can be called to load and
 * save feature definitions by external objects.
 *
 * <p>The actual feature definitions are not stored in an object of this class,
 * but are instead stored in a DataBoard object stored in the MainGUIFrame
 * object that holds the FeatureDefinitionsPanel.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class FeatureDefinitionsPanel
     extends JPanel
     implements ActionListener, TableModelListener
{
     /* FIELDS ****************************************************************/


     // The JFrame that holds this JPanel.
     private	MainGUIFrame	parent;

     // Fields relating to the table used to display the contents of
     // feature_settings
     private	JTable			features_table;

     // Display panels
     private JPanel			features_display_panel;
     private JPanel			button_panel;
     private JScrollPane		features_display_scroll_pane;

     // Displays general messages
     private JTextField		status_bar;

     // Buttons
     private JButton			view_feature_descriptions_button;
     private JButton			add_feature_definition_button;
     private JButton			delete_feature_definitions_button;
     private JButton			load_features_definitions_button;
     private JButton			save_features_definitions_button;
     private JButton			save_features_definitions_as_button;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Basic constructor that sets up the for this JFrame, but does not load
      * any actal feature defintions yet.
      *
      * @param	parent_frame	The JFrame that holds this JPanel.
      */
     public FeatureDefinitionsPanel(MainGUIFrame parent_frame)
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
          view_feature_descriptions_button = new JButton("View Descriptions");
          view_feature_descriptions_button.addActionListener(this);
          add_feature_definition_button = new JButton("Add Feature");
          add_feature_definition_button.addActionListener(this);
          delete_feature_definitions_button = new JButton("Delete Features");
          delete_feature_definitions_button.addActionListener(this);
          load_features_definitions_button = new JButton("Load Features");
          load_features_definitions_button.addActionListener(this);
          save_features_definitions_button = new JButton("Save Features");
          save_features_definitions_button.addActionListener(this);
          save_features_definitions_as_button = new JButton("Save Features As");
          save_features_definitions_as_button.addActionListener(this);

          // Set up button panel
          button_panel = new JPanel(new GridLayout(18, 1, horizontal_gap, vertical_gap));
          button_panel.add(load_features_definitions_button);
          button_panel.add(save_features_definitions_button);
          button_panel.add(save_features_definitions_as_button);
          button_panel.add(new JLabel(""));
          button_panel.add(add_feature_definition_button);
          button_panel.add(delete_feature_definitions_button);
          button_panel.add(new JLabel(""));
          button_panel.add(view_feature_descriptions_button);
          add(button_panel, BorderLayout.EAST);
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event		The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the view_feature_descriptions_button
          if (event.getSource().equals(view_feature_descriptions_button))
               viewFeatureDefinitions();

          // React to the add_feature_definition_button
          else if (event.getSource().equals(add_feature_definition_button))
               addNewFeatureDefinition();

          // React to the delete_feature_definitions_button
          else if (event.getSource().equals(delete_feature_definitions_button))
               deleteFeatureDefinitions();

          // React to the load_features_definitions_button
          else if (event.getSource().equals(load_features_definitions_button))
               browseNewFeatureDefinitions();

          // React to the save_features_definitions_button
          else if (event.getSource().equals(save_features_definitions_button))
               saveAsFile(parent.project.feature_settings_paths[0]);

          // React to the save_features_definitions_as_button
          else if (event.getSource().equals(save_features_definitions_as_button))
               saveAsFileWithBrowse();
     }


     /**
      * Cause this panel to update to reflect the contents of
      * parent.data_board.feature_definitions.
      */
     public void loadNewPanelContents()
     {
          // Remove anything on the left side of the panel
          if (features_display_panel != null)
               remove(features_display_panel);

          // Set up the true table model
          Object[] column_headings = { new String("Name"),
          new String("Dimensions"),
          new String("Sequential"),
          new String("Description") };
          int number_rows = 0;
          if (parent.data_board.getFeatureDefinitions() != null)
          {
               number_rows = parent.data_board.getFeatureDefinitions().length;
          }
          FeatureDefinitionsTableModel features_table_model = new FeatureDefinitionsTableModel(column_headings, number_rows);
          features_table_model.fillTable(parent.data_board.getFeatureDefinitions());

          // Set up the table and make it sortable
          features_table = new JTable(features_table_model);
          TableSortModel sort_model = new TableSortModel(features_table);

          // Make the table striped
          StripedTableCellRenderer.installInTable( features_table,
               Color.lightGray,
               Color.white,
               null,
               null );

          // Adjust the column widths
          setColumnWidths();

          // Add listener to the table
          features_table.getModel().addTableModelListener(this);

          // Set up the display panel
          features_display_scroll_pane = new JScrollPane(features_table);
          features_display_panel = new JPanel(new GridLayout(1, 1));
          features_display_panel.add(features_display_scroll_pane);
          add(features_display_panel, BorderLayout.CENTER);

          // Update the status bar
          updateStatusBar();

          // Display the table and update the status bar
          features_table_model.fireTableDataChanged();
          repaint();
          parent.repaint();
     }


     /**
      * Save the currently loaded feature definitions (as stored in
      * parent.data_board) into a feature_key_file ACE XML file. Use a browse
      * dialog box if the given path is empty ("").
      *
      * @param	save_path	The path to save the feature definitions to.
      */
     public void saveAsFile(String save_path)
     {
          if (parent.data_board.getFeatureDefinitions().length == 0)
               JOptionPane.showMessageDialog(null, "There are not currently any feature definitions to save.", "WARNING", JOptionPane.WARNING_MESSAGE);
          else
          {
               if (!save_path.equals(""))
               {
                    // Get file to write to
                    File save_file = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(save_path, true);

                    // Save the file
                    try
                    {
                         FeatureDefinition.saveFeatureDefinitions( parent.data_board.getFeatureDefinitions(),
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
                    saveAsFileWithBrowse();
          }
     }


     /**
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
          boolean accept_change = true;
          String error_message = "";

          if (event.getType() == TableModelEvent.UPDATE)
          {
               int column = event.getColumn();
               int first_row = event.getFirstRow();
               if (column == 0)
               {
                    FeatureDefinition[] old_features = parent.data_board.feature_definitions;
                    updateFeaturesSettings();
                    String duplicates = FeatureDefinition.verifyFeatureNameUniqueness(parent.data_board.feature_definitions);
                    if (duplicates != null)
                    {
                         error_message = "You have entered a feature name of " + features_table.getModel().getValueAt(first_row, column) + ".\n" +
                              "A feature with this name already exists.";
                         parent.data_board.feature_definitions = old_features;
                         accept_change = false;
                    }
               }
               else if (column == 1)
                    if (((Integer) features_table.getModel().getValueAt(first_row, column)).intValue() < 1)
                    {
                    error_message = "You have entered a dimension value of " + features_table.getModel().getValueAt(first_row, column) + ".\n" +
                         "Each feature must have at least 1 dimension.";
                    accept_change = false;
                    }
          }

          if (accept_change)
          {
               updateFeaturesSettings();
               updateStatusBar();
               setColumnWidths();
          }
          else
          {
               JOptionPane.showMessageDialog(null, error_message, "ERROR", JOptionPane.ERROR_MESSAGE);
               loadNewPanelContents();
          }
     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Displays a dialog box giving the description of each feature selected
      * on the table.
      */
     private void viewFeatureDefinitions()
     {
          int[] rows = features_table.getSelectedRows();
          String[] names = new String[rows.length];
          String[] descriptions = new String[rows.length];
          for (int i = 0; i < descriptions.length; i++)
          {
               names[i] = (String) features_table.getModel().getValueAt(rows[i], 0);
               descriptions[i] = (String) features_table.getModel().getValueAt(rows[i], 3);
          }
          for (int i = 0; i < descriptions.length; i++)
               JOptionPane.showMessageDialog(null, descriptions[i],( names[i] + " Description"), JOptionPane.INFORMATION_MESSAGE);
     }


     /**
      * Adds a new feature to the end of the table. It is automatically given
      * the name New Feature, possibly followed by a number to ensure its
      * uniqueness.
      */
     private void addNewFeatureDefinition()
     {
          // Find the new name of the feature and make sure that it is unique
          String new_name = "New Feature";
          int label_number = 1;
          String[] names = new String[features_table.getRowCount()];
          for (int i = 0; i < names.length; i++)
               names[i] = (String) features_table.getModel().getValueAt(i, 0);
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
          Object[] new_feature = new Object[4];
          new_feature[0] = new_name;
          new_feature[1] = new Integer(1);
          new_feature[2] = new Boolean(false);
          new_feature[3] = "This is a newly added feature.";

          // Add the feature
          ((TableSortModel) features_table.getModel()).addRow(new_feature);
     }


     /**
      * Deletes all features selected on the table.
      */
     private void deleteFeatureDefinitions()
     {
          int[] rows = features_table.getSelectedRows();
          if (rows.length == 0)
               JOptionPane.showMessageDialog(null, "No features are selected to delete.", "WARNING", JOptionPane.WARNING_MESSAGE);
          ((TableSortModel) features_table.getModel()).removeRows(rows);
     }


     /**
      * Opens up a browse dialog box to choose an ACE feature_key_file to load.
      * The file is parsed, stored in the DataBoard and displayed here if it is
      * valid (the feature definitions are left unchanged if it is invalid). The
      * path in the ProjectFilesDialogBox is updated to the chosen path either
      * way.
      */
     private void browseNewFeatureDefinitions()
     {
          boolean ok_pressed = parent.project_files_dialog_box.browsePath("feature_definitions", true);
          if (ok_pressed)
          {
               String definitions_path = parent.project.feature_settings_paths[0];
               try
               {
                    parent.data_board.feature_definitions = FeatureDefinition.parseFeatureDefinitionsFile(definitions_path);
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
      * Save the currently loaded feature definitions (as stored in
      * parent.data_board) into an ACE feature_key_file XML file. The path is
      * chosen using a browse dialog box. The name of the saved file (without
      * directory information or extension) is stored in the
      * ProjectFilesDialogBox.
      */
     private void saveAsFileWithBrowse()
     {

          if (parent.data_board.getFeatureDefinitions().length == 0)
               JOptionPane.showMessageDialog(null, "There are not currently any feature definitions to save.", "WARNING", JOptionPane.WARNING_MESSAGE);
          else
          {
               // Get file to write to through a dialog box
               File save_file = parent.project_files_dialog_box.saveFileExternally("feature_definitions", false);

               // Save the file
               if (save_file != null)
               {
                    try
                    {
                         FeatureDefinition.saveFeatureDefinitions( parent.data_board.getFeatureDefinitions(),
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
      * Updates parent.data_board.feature_definitions to reflect the contents of
      * the features_table.
      */
     private void updateFeaturesSettings()
     {
          TableSortModel table_model = (TableSortModel) features_table.getModel();
          parent.data_board.feature_definitions = new FeatureDefinition[table_model.getRowCount()];
          for (int i = 0; i < table_model.getRowCount(); i++)
               parent.data_board.feature_definitions[i] = new FeatureDefinition( (String) table_model.getValueAt(i, 0),
                    (String) table_model.getValueAt(i, 3),
                    ((Boolean) table_model.getValueAt(i, 2)).booleanValue(),
                    ((Integer) table_model.getValueAt(i, 1)).intValue() );
     }


     /**
      * Causes the status bar to display the number of features, including the
      * the total number of features that are multi-dimensional and the number
      * that are sequential.
      */
     private void updateStatusBar()
     {
          int number_features = 0;
          int number_multi_dimensional_features = 0;
          int number_sequential_features = 0;
          if (parent.data_board.getFeatureDefinitions() != null)
          {
               for (int i = 0; i < parent.data_board.getFeatureDefinitions().length; i++)
               {
                    number_features++;
                    if (parent.data_board.getFeatureDefinitions()[i].dimensions > 1)
                         number_multi_dimensional_features++;
                    if (parent.data_board.getFeatureDefinitions()[i].is_sequential)
                         number_sequential_features++;
               }
          }

          String status_text = new String(number_features + " features available, including  " +
               number_multi_dimensional_features + " multi-dimensional features and " +
               number_sequential_features + " sequential features");
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
          int max_width = 25;
          TableColumn[] columns_to_set_sizes= { features_table.getColumn("Name"),
          features_table.getColumn("Dimensions"),
          features_table.getColumn("Sequential") };
          TableSortModel.setColumnWidths(features_table, columns_to_set_sizes, max_width);
     }
}