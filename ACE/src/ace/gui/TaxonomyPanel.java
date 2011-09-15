/*
 * TaxonomyPanel.java
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
import javax.swing.tree.*;
import ace.datatypes.*;


/**
 * An object of this class represents a panel in the ACE GUI that allows users
 * to view, edit, load, and save taxonomies that classifications can be based on.
 *
 * <p>The currently loaded taxonomy can be seen in one of two views. The default
 * view, called the Hierarchical View, allows users to see a hierarchal tree
 * reflecting the relationships of broad categories going down to narrower
 * categories. The names of these categories may be edited by clicking on them
 * (the return button must be pressed to register changes) or by using the
 * buttons described below. In this view, the status bar displays the total
 * number of top level categories.
 *
 * <p>The second view, called the Ancestry View, allows users to see the
 * taxonomy in the form of a list of leaf categories. Children of leaf
 * categories represent a list of all ancestors of the given category. Leaf
 * categories that appear more than once in the taxonomy are combined into one
 * under this view (the stored taxonomy itself is not changed) so that all of
 * the combined ancestors are listed together. The taxonomy is not editable in
 * this view. In this view, the status bar displays the total number of leaf
 * categories.
 *
 * <p>Both of the trees in the above two views are sorted alphebetically. The
 * <i>Display Hierarchy</i> and <i>Display Ancestry</i> buttons allow users to
 * switch between the two views. In both views, a beige bullet next to a
 * category means that it has children that can be viewed or hidden by pressing
 * on the handle to the left of the bullet, and a blue bullet next to a category
 * means that it is a leaf category with no children.
 *
 * <p>The <i>New Taxonomy</i> button deletes the current taxonomy and creates a
 * new empty taxonomy.
 *
 * <p>The <i>Load Taxonomy</i> button loads a taxonomy_file ACE XML file into
 * memory and displays it. This overwrites any existing taxonomy. The root node,
 * which has no internal significance to ACE, is given the name of the file.
 *
 * <p>The <i>Save Taxonomy</i> button saves the currently loaded taxonomy into
 * the path referred to in the <i>File Path Settings</i> dialog box as a
 * taxonomy_file ACE XML file. The <i>Save As</i> button allows the user to
 * choose the path to which the file is to be saved and updates the path in the
 * <i>File Path Settings</i> dialog box. The name of the root node, which has no
 * internal significance to ACE, is not saved.
 *
 * <p>The <i>Add New Sibling Category</i> and the <i>Add New Child</i> buttons
 * add a new sibling or a new child category to the taxonomy respectively, with
 * a location based on the category that is currently selected by the user. The
 * <i>Delete Branch</i> button removes the selected category and its decendants.
 *
 * <p>The loadNewPanelContents and saveAsFile methods can be called to load and
 * save taxonomies by external objects.
 *
 * <p>The actual taxonomy is not stored in an object of this class, but is
 * instead stored in a DataBoard object stored in the MainGUIFrame object that
 * holds the TaxonomyPanel.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class TaxonomyPanel
     extends JPanel
     implements ActionListener
{
     /* FIELDS ****************************************************************/


     // The JFrame that holds this JPanel.
     private MainGUIFrame               parent;

     // JTree that holds the taxonomy DefaultTreeModel
     private JTree			taxonomy_display;

     // Display panels
     private JPanel			tree_display_panel;
     private JPanel			ancestry_display_panel;
     private JPanel			button_panel;
     private JScrollPane		taxonomy_display_scroll_pane;

     // Displays general messages
     private JTextField                 status_bar;

     // Buttons
     private JButton			display_hierarchy_button;
     private JButton			display_ancestry_button;
     private JButton			new_taxonomy_button;
     private JButton			load_taxonomy_button;
     private JButton			save_taxonomy_button;
     private JButton			save_taxonomy_as_button;
     private JButton			add_new_sibling_category_button;
     private JButton			add_new_child_category_button;
     private JButton			delete_branch_button;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Basic constructor that sets up the for this JFrame, but does not load
      * any actal taxonomy yet.
      *
      * @param	parent_frame	The JFrame that holds this JPanel.
      */
     public TaxonomyPanel(MainGUIFrame parent_frame)
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
          button_panel = new JPanel(new GridLayout(18, 1, horizontal_gap, vertical_gap));
          display_hierarchy_button = new JButton("Display Hierarchy");
          display_hierarchy_button.setEnabled(false);
          display_ancestry_button = new JButton("Display Ancestry");
          new_taxonomy_button = new JButton("New Taxonomy");
          load_taxonomy_button = new JButton("Load Taxonomy");
          save_taxonomy_button = new JButton("Save Taxonomy");
          save_taxonomy_as_button = new JButton("Save Taxonomy As");
          add_new_sibling_category_button = new JButton("Add New Sibling Category");
          add_new_child_category_button = new JButton("Add New Child Category");
          delete_branch_button = new JButton("Delete Branch");
          display_hierarchy_button.addActionListener(this);
          display_ancestry_button.addActionListener(this);
          new_taxonomy_button.addActionListener(this);
          load_taxonomy_button.addActionListener(this);
          save_taxonomy_button.addActionListener(this);
          save_taxonomy_as_button.addActionListener(this);
          add_new_sibling_category_button.addActionListener(this);
          add_new_child_category_button.addActionListener(this);
          delete_branch_button.addActionListener(this);
          button_panel.add(display_hierarchy_button);
          button_panel.add(display_ancestry_button);
          button_panel.add(new JLabel(""));
          button_panel.add(new_taxonomy_button);
          button_panel.add(load_taxonomy_button);
          button_panel.add(save_taxonomy_button);
          button_panel.add(save_taxonomy_as_button);
          button_panel.add(new JLabel(""));
          button_panel.add(add_new_sibling_category_button);
          button_panel.add(add_new_child_category_button);
          button_panel.add(delete_branch_button);
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
          // React to the display_taxonomy_button
          if (event.getSource().equals(display_hierarchy_button))
               displayTaxonomyHierarchically();

          // React to the display_ancestry_button
          if (event.getSource().equals(display_ancestry_button))
               displayAncestryTree();

          // React to the new_taxonomy_button
          else if (event.getSource().equals(new_taxonomy_button))
               newTaxonomy();

          // React to the load_taxonomy_button
          else if (event.getSource().equals(load_taxonomy_button))
               browseNewTaxonomy();

          // React to the save_taxonomy_button
          else if (event.getSource().equals(save_taxonomy_button))
               saveAsFile(parent.project.taxonomy_path);

          // React to the save_taxonomy_as_button
          else if (event.getSource().equals(save_taxonomy_as_button))
               saveAsFileWithBrowse();

          // React to the add_new_sibling_category_button
          else if (event.getSource().equals(add_new_sibling_category_button))
               addNewNode(false);

          // React to the add_new_child_category_button
          else if (event.getSource().equals(add_new_child_category_button))
               addNewNode(true);

          // React to the delete_branch_button
          else if (event.getSource().equals(delete_branch_button))
               deleteBranch();
     }


     /**
      * Causes this panel to update to reflect the contents of
      * parent.data_board. The contents are displayed hierarchically. A new
      * empty taxonomy is generated if none is present in parent.data_board.
      */
     public void loadNewPanelContents()
     {
          // If the parent.data_board does not contain an existing taxonomy,
          // then set it to a new empty one and display it
          if (parent.data_board.taxonomy == null)
               newTaxonomy();

          // If a taxonomy already exists in parent.data_board, then display it
          else
               displayTaxonomyHierarchically();
     }


     /**
      * Save the currently loaded taxonomy (as stored in parent.data_board) into
      * an ACE taxonomy_file XML file. Use a browse dialog box if the given path
      * is empty (""). The root name of the taxonomy is changed to reflect the
      * new file name (without directory information or extension).
      *
      * @param	save_path	The path to save the taxonomy file to.
      */
     public void saveAsFile(String save_path)
     {
          if (!save_path.equals(""))
          {
               // Get file to write to
               File save_file = mckay.utilities.staticlibraries.FileMethods.getNewFileForWriting(save_path, true);

               // Change the root name of the taxonomy
               Taxonomy.setRootName(parent.data_board.taxonomy, save_file.getPath());

               // Save the file
               try
               {
                    Taxonomy.saveTaxonomy(parent.data_board.taxonomy, save_file, "");
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


     /* PRIVATE METHODS *******************************************************/


     /**
      * Creates and displays a new empty taxonomy. Stores it in
      * parent.data_board.taxonomy.
      */
     private void newTaxonomy()
     {
          DefaultMutableTreeNode new_node = new DefaultMutableTreeNode("New Taxonomy");
          DefaultTreeModel taxonomy_tree = new DefaultTreeModel(new_node);
          parent.data_board.taxonomy = new Taxonomy(taxonomy_tree);
          taxonomy_display = new JTree(taxonomy_tree);
          setTreeIcons(taxonomy_display, new String("." + File.separator + "ProgramFiles" + File.separator + "bluebull.gif"),
               new String("." + File.separator + "ProgramFiles" + File.separator + "beigebull.gif"));
          taxonomy_display.setEditable(true);

          TreeNode[] nodes = taxonomy_tree.getPathToRoot(new_node);

          TreePath path = new TreePath(nodes);
          taxonomy_display.setSelectionPath(path);

          displayTaxonomyHierarchically();
     }


     /**
      * Opens up a browse dialog box to choose an ACE taxonomy_file to load.
      * The file is parsed, stored in the DataBoard and displayed here if it is
      * valid (the taxonomy is left unchanged if it is invalid). The path in
      * the ProjectFilesDialogBox is updated to the chosen path either way.
      */
     private void browseNewTaxonomy()
     {
          boolean ok_pressed = parent.project_files_dialog_box.browsePath("taxonomy", true);
          if (ok_pressed)
          {
               String taxonomy_path = parent.project.taxonomy_path;
               try
               {
                    parent.data_board.taxonomy = Taxonomy.parseTaxonomyFile(taxonomy_path);
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
      * Save the currently loaded taxonomy (as stored in parent.data_board) into
      * an ACE taxonomy_file XML file. The path is chosen using a browse dialog
      * box. The name of the saved file (without directory information or
      * extension) is stored in the ProjectFilesDialogBox and is used as the new
      * root name of the taxonomy.
      */
     private void saveAsFileWithBrowse()
     {
          // Get file to write to through a dialog box
          File save_file = parent.project_files_dialog_box.saveFileExternally("taxonomy", false);

          if (save_file != null)
          {
               // Change the root name of the taxonomy
               Taxonomy.setRootName(parent.data_board.taxonomy, save_file.getPath());

               // Save the file
               try
               {
                    Taxonomy.saveTaxonomy(parent.data_board.taxonomy, save_file, "");
               }
               catch (Exception e)
               {
                    // e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
               }
          }

          // Repaint to show any changes to root name
          repaint();
          parent.repaint();
     }


     /**
      * Displays the taxonomy in the left section of the panel in the form of a
      * hierarchal tree, with broad categories going down to narrower
      * categories. Also sorts the tree categories alphebetically by name,
      * refreshes the display and updates the status bar.
      */
     private void displayTaxonomyHierarchically()
     {
          // Remove anything on the left side of the panel
          if (tree_display_panel != null)
               remove(tree_display_panel);
          if (ancestry_display_panel != null)
               remove(ancestry_display_panel);

          // Sort tree alphabetically, make it editable and configure its display
          parent.data_board.taxonomy.taxonomy = new DefaultTreeModel(mckay.utilities.staticlibraries.SortingMethods.sortTree((DefaultMutableTreeNode) parent.data_board.taxonomy.taxonomy.getRoot()));
          taxonomy_display = new JTree(parent.data_board.taxonomy.taxonomy);
          setTreeIcons(taxonomy_display, new String("." + File.separator + "ProgramFiles" + File.separator + "bluebull.gif"),
               new String("." + File.separator + "ProgramFiles" + File.separator + "beigebull.gif"));
          taxonomy_display.setEditable(true);

          // Expand the tree
          expandTree(taxonomy_display);

          // Update the status bar
          updateStatusBar(null);

          // Refressh the display
          taxonomy_display_scroll_pane = new JScrollPane(taxonomy_display);
          tree_display_panel = new JPanel(new GridLayout(1, 1));
          tree_display_panel.add(taxonomy_display_scroll_pane);
          add(tree_display_panel, BorderLayout.CENTER);
          repaint();
          parent.repaint();

          // Set proper enabling of buttons
          display_hierarchy_button.setEnabled(false);
          display_ancestry_button.setEnabled(true);
          new_taxonomy_button.setEnabled(true);
          load_taxonomy_button.setEnabled(true);
          save_taxonomy_button.setEnabled(true);
          save_taxonomy_as_button.setEnabled(true);
          add_new_sibling_category_button.setEnabled(true);
          add_new_child_category_button.setEnabled(true);
          delete_branch_button.setEnabled(true);
     }


     /**
      * Displays the taxonomy in the left section of the panel in the form of a
      * list of leaf categories. Children of these categories represent a list
      * of all ancestors of the given category. The categories are sorted
      * alphebetically by name. The display is refreshed and the status bar is
      * updated.
      */
     private void displayAncestryTree()
     {
          // Do nothing if no children
          if (((DefaultMutableTreeNode) (parent.data_board.taxonomy.taxonomy.getRoot())).getChildCount() != 0)
          {
               // Remove anything on the left side of the panel
               if (tree_display_panel != null)
                    remove(tree_display_panel);
               if (ancestry_display_panel != null)
                    remove(ancestry_display_panel);

               // Construct ancestor tree, sort it alphabetically, make it
               // editable and configure its display
               DefaultTreeModel ancestry = getAncestorTree();
               ancestry = new DefaultTreeModel(mckay.utilities.staticlibraries.SortingMethods.sortTree((DefaultMutableTreeNode) ancestry.getRoot()));
               JTree anestry_display = new JTree(ancestry);
               setTreeIcons(anestry_display, new String("." + File.separator + "ProgramFiles" + File.separator + "bluebull.gif"),
                    new String("." + File.separator + "ProgramFiles" + File.separator + "beigebull.gif"));

               // Expand the tree
               expandTree(anestry_display);

               // Update the status bar
               updateStatusBar((DefaultMutableTreeNode) ancestry.getRoot());

               // Refressh the display
               JScrollPane ancesty_display_scroll_pane = new JScrollPane(anestry_display);
               ancestry_display_panel = new JPanel(new GridLayout(1, 1));
               ancestry_display_panel.add(ancesty_display_scroll_pane);
               add(ancestry_display_panel, BorderLayout.CENTER);
               repaint();
               parent.repaint();

               // Set proper enabling of buttons
               display_hierarchy_button.setEnabled(true);
               display_ancestry_button.setEnabled(false);
               new_taxonomy_button.setEnabled(false);
               load_taxonomy_button.setEnabled(false);
               save_taxonomy_button.setEnabled(false);
               save_taxonomy_as_button.setEnabled(false);
               add_new_sibling_category_button.setEnabled(false);
               add_new_child_category_button.setEnabled(false);
               delete_branch_button.setEnabled(false);
          }
     }


     /**
      * Causes the status bar to display the number of parent categories
      * in the taxonomy field if the root parameter is null.
      * If the root paratmeter is not null, then sets the status bar to
      * display the number of leaf categories in the taxonomy field.
      *
      * @param	root     Null if the number of parent categories are to
      *                  be displayed, otherwise is the root of the tree
      *                  for which the number of leaf categories are to
      *                  be found.
      */
     private void updateStatusBar(DefaultMutableTreeNode root)
     {
          if (root == null) // hierarchy display
          {
               int number_parent_categories = ((DefaultMutableTreeNode)(parent.data_board.taxonomy.taxonomy.getRoot())).getChildCount();
               status_bar.setText(new String(number_parent_categories + " top-level categories"));
          }
          else
          {
               int number_parent_categories = root.getChildCount();
               status_bar.setText(new String(number_parent_categories + " leaf categories"));
          }
     }


     /**
      * Constructs a tree representation of the taxonomy whereby the taxonomy
      * is represented by a list of leaf categories. Children of leaf categories
      * represent the ancestors of the corresponding category.
      *
      * @return	The root of the resulting tree.
      */
     private DefaultTreeModel getAncestorTree()
     {
          Object root_label_of_taxonomy = ((DefaultMutableTreeNode) parent.data_board.taxonomy.taxonomy.getRoot()).getUserObject();
          DefaultMutableTreeNode root = new DefaultMutableTreeNode(root_label_of_taxonomy);
          String[][] ancestry = parent.data_board.taxonomy.getLeafsAndTheirParents();
          for (int i = 0; i < ancestry.length; i++)
          {
               DefaultMutableTreeNode parent = new DefaultMutableTreeNode(ancestry[i][0]);
               for (int j = 1; j < ancestry[i].length; j++)
                    parent.add(new DefaultMutableTreeNode(ancestry[i][j]));
               root.add(parent);
          }
          return new DefaultTreeModel(root);
     }


     /**
      * Adds a new category to the taxonomy and calls it New Category.
      *
      * @param	child    If this is true, then the new category is added as a
      *                  child of the selected node in the taxonomy. If it is
      *                  false, then it is added as a sibling.
      */
     private void addNewNode(boolean child)
     {
          DefaultMutableTreeNode selected_node = (DefaultMutableTreeNode) taxonomy_display.getLastSelectedPathComponent();
          if (selected_node == null) return;

          DefaultMutableTreeNode new_node = new DefaultMutableTreeNode("New Category");

          if (child)
               parent.data_board.taxonomy.taxonomy.insertNodeInto(new_node, selected_node, selected_node.getChildCount());
          else
          {
               DefaultMutableTreeNode tree_parent = (DefaultMutableTreeNode) selected_node.getParent();
               if (tree_parent != null)
               {
                    int selected_index = tree_parent.getIndex(selected_node);
                    parent.data_board.taxonomy.taxonomy.insertNodeInto(new_node, tree_parent, selected_index + 1);
               }
          }

          TreeNode[] nodes = parent.data_board.taxonomy.taxonomy.getPathToRoot(new_node);
          TreePath path = new TreePath(nodes);
          taxonomy_display.scrollPathToVisible(path);
          updateStatusBar(null);
     }


     /**
      * Delete the selected node and all of its children from the taxonomy.
      */
     private void deleteBranch()
     {
          DefaultMutableTreeNode selected_node = (DefaultMutableTreeNode) taxonomy_display.getLastSelectedPathComponent();
          if (selected_node == null) return;
          if (selected_node.getParent() != null)
               parent.data_board.taxonomy.taxonomy.removeNodeFromParent(selected_node);
          updateStatusBar(null);
     }


     /**
      * Sets the icons for the given tree to the images specified by the given
      * paths. Open and closed icons are displayed identically.
      *
      * @param	tree			The tree to give icons to.
      * @param	leaf_icon_path		The path to the icon for leaf nodes.
      * @param	non_leaf_icon_path	The path to the icon for non-leaf nodes.
      */
     private void setTreeIcons(JTree tree, String leaf_icon_path, String non_leaf_icon_path)
     {
          DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
          renderer.setLeafIcon(new ImageIcon(leaf_icon_path));
          renderer.setOpenIcon(new ImageIcon(non_leaf_icon_path));
          renderer.setClosedIcon(new ImageIcon(non_leaf_icon_path));
          tree.setCellRenderer(renderer);
     }


     /**
      * Expand all nodes of the given tree so that their children can be seen.
      *
      * @param	tree	The tree to expand.
      */
     private static void expandTree(JTree tree)
     {
          int row = 0;
          while (row < tree.getRowCount())
          {
               tree.expandRow(row);
               row++;
          }
     }
}