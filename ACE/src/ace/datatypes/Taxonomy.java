/*
 * Taxonomy.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.datatypes;

import javax.swing.tree.*;
import ace.xmlparsers.XMLDocumentParser;
import java.io.*;
import java.util.LinkedList;
import weka.core.Instances;


/**
 * Objects of this class each hold a taxonomy. Each class is stored as a string
 * giving its name. The taxonomy is stored as a tree, with the root giving the
 * name of the taxonomy. A given class may occur at more than one node in the
 * tree.
 *
 * <p>Methods are available for adding classes, deleting classes, viewing the
 * classes and various sub-sets of the classes, saving the classes to disk
 * and loading the classes from disk. Methods are also available for checking
 * which classes are in a taxonomy but not in a set of classificatiosns, and
 * vice versa.
 *
 * <p>If the user only wishes to use a flat taxonomy, the addClass, deleteClass
 * and getLeafLabels methods will be the most useful.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class Taxonomy
     implements Serializable
{
     /* FIELDS ****************************************************************/


     /**
      * The tree holding the taxonomy. The root element is the name of the
      * taxonomy.
      */
     public	DefaultTreeModel	taxonomy;


     /**
      * An identifier for use in serialization.
      */
     private static final long	serialVersionUID = 1L;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Generate an empty taxonomy with the name "Taxonomy".
      */
     public Taxonomy()
     {
          DefaultMutableTreeNode new_node = new DefaultMutableTreeNode("Taxonomy");
          taxonomy = new DefaultTreeModel(new_node);
     }


     /**
      * Generate an empty taxonomy with the specified name.
      *
      * @param	taxonomy_name	The name to set as the name of the taxonomy.
      */
     public Taxonomy(String taxonomy_name)
     {
          DefaultMutableTreeNode new_node = new DefaultMutableTreeNode(taxonomy_name);
          taxonomy = new DefaultTreeModel(new_node);
     }


     /**
      * Set the taxonomy to an existing DefaultTreeModel.
      *
      * @param	existing_taxonomy	The tree to set the taxonomy field to.
      */
     public Taxonomy(DefaultTreeModel existing_taxonomy)
     {
          taxonomy = existing_taxonomy;
     }

     /**
      * Generates a Taxonomy from a Weka ARFF file.
      * Generates an empty taxonomy with the name TaxonomyFromARFF
      * then adds the values of the class attribute one by one.
      */
     public Taxonomy(Instances instances)
     {
         DefaultMutableTreeNode new_node = new DefaultMutableTreeNode("TaxonomyFromARFF");
         taxonomy = new DefaultTreeModel(new_node);
         for(int i = 0; i < instances.classAttribute().numValues(); i++)
         {
             addClass(instances.classAttribute().value(i));
         }
     }

     /* PUBLIC METHODS ********************************************************/


     /**
      * Adds a new class with the given name to the taxonomy. The new
      * class is added as the last child of the root in the taxonomy.
      * If the taxonomy is flat, then this is equivalent to adding the
      * new class to the end of the list of categories.
      *
      * @param	new_class_name     The name of the new class that is to
      *                            be added to the taxonomy.
      */
     public void addClass(String new_class_name)
     {
          DefaultMutableTreeNode new_node = new DefaultMutableTreeNode(new_class_name);
          DefaultMutableTreeNode root_node = (DefaultMutableTreeNode) taxonomy.getRoot();
          taxonomy.insertNodeInto(new_node, root_node, root_node.getChildCount());
     }


     /**
      * Inserts the given new_node into the taxonomy as the last child of
      * the given parent_node. The parent_node should already be in the taxonomy.
      *
      * @param	new_node      The node that is to be inserted into
      *                       the taxonomy.
      * @param	parent_node   The node that is already in the taxonomy
      *                       that is to be the parent of the new node.
      */
     public void addChildClass( DefaultMutableTreeNode new_node,
          DefaultMutableTreeNode parent_node )
     {
          taxonomy.insertNodeInto(new_node, parent_node, parent_node.getChildCount());
     }


     /**
      * Inserts the given new_node into the taxonomy next to the given
      * sibling_node. The sibling_node must already be in the taxonomy.
      *
      * @param	new_node      The node that is to be inserted into the taxonomy.
      * @param	sibling_node  The node that is already in the taxonomy that is
      *                       to be at the same level and just preceeding the
      *                       new node.
      * @throws	Exception     Informative exceptions is thrown if the
      *                       sibling_node is not already in the taxonomy or if
      *                       the parent of the sibling_node is null.
      */
     public void addSiblingClass( DefaultMutableTreeNode new_node,
          DefaultMutableTreeNode sibling_node )
          throws Exception
     {
          DefaultMutableTreeNode
               parent_node = (DefaultMutableTreeNode) sibling_node.getParent();
          if (parent_node != null)
          {
               int selected_index = parent_node.getIndex(sibling_node);
               if (selected_index == -1)
                    throw new Exception("Given sibling node is not in the taxonomy");
               taxonomy.insertNodeInto(new_node, parent_node, selected_index + 1);
          }
          else throw new Exception("Parent of given sibling node is null.");
     }


     /**
      * Searches through the taxonomy and deletes all classes with the
      * given name. If the taxonomy is hierarchical, then children of
      * classes with the given name are also deleted.
      *
      * @param	name_of_class_to_delete      The name that classes must have
      *                                      in order to be deleted.
      */
     public void deleteClass(String name_of_class_to_delete)
     {
          DefaultMutableTreeNode root = (DefaultMutableTreeNode) taxonomy.getRoot();
          for (int i = 0; i < root.getChildCount() ; i++)
          {
               DefaultMutableTreeNode child =
                    (DefaultMutableTreeNode) root.getChildAt(i);
               searchForBranchesToRemove(name_of_class_to_delete, child);
          }
     }


     /**
      * Deletes the given node_to_delete and all of its children from the
      * taxonomy.
      *
      * @param	node_to_delete	The node that is to be deleted.
      * @throws	Exception	An informative exception is thrown if the
      *				parent of the node_to_delete is null.
      */
     public void deleteBranchOfTaxonomy(DefaultMutableTreeNode node_to_delete)
     throws Exception
     {
          if (node_to_delete.getParent() != null)
               taxonomy.removeNodeFromParent(node_to_delete);
          else throw new Exception("Parent of given node to delete is null.");
     }


     /**
      * Sets the name of the stored taxonomy to the given taxonomy_name.
      *
      * @param	taxonomy_name	The name to set as the name of the taxonomy.
      */
     public void setTaxonomyName(String taxonomy_name)
     {
          DefaultMutableTreeNode root = (DefaultMutableTreeNode) taxonomy.getRoot();
          root.setUserObject(taxonomy_name);
     }


     /**
      * Returns a string showing the entire taxonomy. The string is formatted
      * with spaces and new lines to reflect the potentially hierarchical
      * structure of the taxonomy. An empty string is returned if the taxonomy
      * is empty.
      *
      * @return	The taxonomy in the form of a formatted string.
      */
     public String getFormattedTreeStructure()
     {
          if (taxonomy == null)
               return new String("");

          else
          {
               String output_text = new String();
               DefaultMutableTreeNode root = (DefaultMutableTreeNode) taxonomy.getRoot();
               for (int i = 0; i < root.getChildCount() ; i++)
               {
                    DefaultMutableTreeNode child =
                         (DefaultMutableTreeNode) root.getChildAt(i);
                    output_text += assembleTreeElements(child, 1);
               }
               return output_text;
          }
     }


     /**
      * Returns an array holding the labels of all leaf classes in the
      * taxonomy field. No duplicates are present.
      *
      * @return	The labels of all leaf classes in the taxonomy field,
      *		with duplicates removed.
      */
     public String[] getLeafLabels()
     {
          String[][] categories = getLeafsAndTheirParents();
          String[] leaf_categories = new String[categories.length];
          for (int i = 0; i < leaf_categories.length; i++)
               leaf_categories[i] = categories[i][0];
          return leaf_categories;
     }


     /**
      * Returns an array holding the labels of all parent classes
      * (i.e. classes at the highest level of the taxonomy) in the
      * taxonomy field. No duplicates are present.
      *
      * @return	The labels of all parent classes in the taxonomy field,
      *		with duplicates removed.
      */
     public String[] getParentLabels()
     {
          DefaultMutableTreeNode root = (DefaultMutableTreeNode) taxonomy.getRoot();
          String[] parent_categories = new String[root.getChildCount()];
          DefaultMutableTreeNode this_parent = null;
          for (int i = 0; i < parent_categories.length; i++)
          {
               if (i == 0) this_parent = (DefaultMutableTreeNode) root.getFirstChild();
               else this_parent = this_parent.getNextSibling();

               parent_categories[i] = (String) this_parent.getUserObject();
          }
          return parent_categories;
     }


     /**
      * Returns an array holding the labels of the classes in the taxonomy
      * field that are neither leaves nor parent classes. No duplicates are
      * present.
      *
      * @return	The labels of all mid-level categories in the taxonomy field,
      *		with duplicates removed.
      */
     public String[] getMiddleLabels()
     {
          // Get the set of all categories and the set of parent categories
          String[][] categories = getLeafsAndTheirParents();
          String[] parent_categories = getParentLabels();

          // Find non-leaf categories that are not parent categories and add
          // them to the list if they have not been found already
          LinkedList<Object> categories_so_far = new LinkedList<Object>();
          for (int i = 0; i < categories.length; i++)
          {
               for (int j = 1; j < categories[i].length; j++)
               {
                    // Check if the category in categories is in parent_categories
                    boolean present = false;
                    for (int k = 0; k < parent_categories.length; k++)
                    {
                         if (categories[i][j].equals(parent_categories[k]))
                         {
                              present = true;
                              k = parent_categories.length + 1; // exit the loop
                         }
                    }

                    // If not present, then check if it has already been added
                    // to categories_so_far. If not, then add it.
                    if (!present)
                    {
                         boolean present_2 = false;
                         for (int k = 0; k < categories_so_far.size(); k++)
                         {
                              if (categories[i][j].equals((String) categories_so_far.get(k)))
                              {
                                   present_2 = true;
                                   k = categories_so_far.size() + 1; // exit the loop
                              }
                         }
                         if (!present_2)
                         {
                              categories_so_far.add(categories[i][j]);
                         }
                    }

               }
          }

          // Convert categories_so_far to an array of strings and return it
          String[] final_categories = new String[categories_so_far.size()];
          for (int i = 0; i < categories_so_far.size(); i++)
               final_categories[i] = (String) categories_so_far.get(i);
          return final_categories;
     }


     /**
      * Returns each leaf class in the taxonomy field and its ancestry.
      * Duplicates are removed (i.e. if the same leaf class appears more than
      * once in the taxonomy then the duplicate is removed and the ancestors are
      * combined).
      *
      * <p>This information is returned in the form of a 2-D array of Strings.
      * The first indice specifies a particular leaf class and its ancestors.
      * he second indice specifies the particular ancestor of a given leaf class
      * or the leaf class itself. Indice 0 always corresponds to the name of the
      * leaf class itself, but the order of the ancestors is not necessarily
      * meaningful.
      *
      * @return	The leaf categories and their ancestors.
      */
     public String[][] getLeafsAndTheirParents()
     {
          // The root of the tree from which leaf categories and ancestors are
          // to be extracted
          DefaultMutableTreeNode root = (DefaultMutableTreeNode) taxonomy.getRoot();

          // The initial results (duplicates not removed)
          String[][] results = new String[root.getLeafCount()][];

          // Extract the leaf categories and their ancestors
          DefaultMutableTreeNode leaf = root.getFirstLeaf();
          String[] ancestor_names;
          int count = 0;
          while (count < root.getLeafCount())
          {
               // Get the ancestry of the leaf
               TreeNode[] ancestry_uncasted = taxonomy.getPathToRoot(leaf);
               DefaultMutableTreeNode[] ancestry = new DefaultMutableTreeNode[ancestry_uncasted.length];
               for (int i = 0; i < ancestry.length; i++)
                    ancestry[i] = (DefaultMutableTreeNode) ancestry_uncasted[i];

               // Convert to string form
               ancestor_names = new String[ancestry.length - 1]; // 11 to avoid root
               for (int i = 0; i < ancestry.length - 1; i++)
                    ancestor_names[i] = (String) ancestry[i + 1].getUserObject(); // +1 to avoid root
               results[count] = ancestor_names;

               // Prepare for next iteration
               count++;
               leaf = leaf.getNextLeaf();
          }

          // Reverse the contents of each string array so that leaf categories
          // come first
          String[][] reversed_results = new String[results.length][];
          for (int i = 0; i < results.length; i++)
          {
               reversed_results[i] = new String[results[i].length];
               int k = 0;
               for (int j = results[i].length - 1; j >= 0; j--)
               {
                    reversed_results[i][k] = results[i][j];
                    k++;
               }
               results[i] = reversed_results[i];
          }

          // If leaf categories appear more than once, combine into one
          // category with all ancestors combined
          int replacement_count = 0;
          for (int i = 0; i < results.length; i++)
          {
               for (int j = 0; j < i; j++)
               {
                    if (results[i] != null && results[j] != null)
                    {
                         if (results[i][0].equals(results[j][0]))
                         {
                              String[] temp = new String[results[i].length - 1]; // holds ancestors of results[i]
                              int m = 1;
                              for (int k = 0; k < results[i].length - 1; k++)
                              {
                                   temp[k] = results[i][m];
                                   m++;
                              }

                              String[] temp_2 = new String[results[j].length + temp.length]; // holds combined ancestors
                              for (int k = 0; k < temp_2.length; k++)
                              {
                                   if (k < results[j].length)
                                        temp_2[k] = results[j][k];
                                   else
                                        temp_2[k] = temp[k - results[j].length];
                              }

                              results[j] = temp_2; // List all ancestors in first occurence

                              results[i] = null;
                              replacement_count++;
                         }
                    }
               }
          }
          String[][] reduced_results = new String[results.length - replacement_count][];
          int m = 0;
          for (int i = 0; i < reduced_results.length; i++)
          {
               while (results[m] == null)
                    m++;
               reduced_results[i] = results[m];
               m++;
          }

          // Remove ancestors that are listed more than once
          for (int i = 0; i < reduced_results.length; i++)
          {

               int repetition_count = 0;
               for (int j = 1; j < reduced_results[i].length; j++)
               {
                    for (int k = 1; k < reduced_results[i].length; k++)
                    {
                         if (reduced_results[i][j] != null && j != k)
                         {
                              if (reduced_results[i][j].equals(reduced_results[i][k]))
                              {
                                   repetition_count++;
                                   reduced_results[i][j] = null;
                              }
                         }
                    }
               }
               if (repetition_count != 0)
               {
                    String[] temp = new String[reduced_results.length];
                    for (int j = 0; j < reduced_results[i].length; j++)
                         temp[j] = reduced_results[i][j];
                    reduced_results[i] = new String[reduced_results[i].length - repetition_count];
                    reduced_results[i][0] = temp[0];
                    int reduced_results_index = 1;
                    for (int j = 1; j < temp.length; j++)
                    {
                         if (temp[j] != null)
                         {
                              reduced_results[i][reduced_results_index] = temp[j];
                              reduced_results_index++;
                         }
                    }
               }
          }

          // Return the final results
          return reduced_results;
     }


     /**
      * Returns the name of each class with children and its direct descendants.
      * The first indice corresponds to the different parents. The second indice
      * goes through the children. The first entry of each row is the name of
      * the parent.
      *
      * @return	The names of parent classes and their children.
      */
     public String[][] getAllParentsAndTheirDirectChildren()
     {
          // Prepare the list of parents and their direct descendants
          LinkedList<String[]> parents_and_their_children = new LinkedList<String[]>();

          // Recurse through the tree
          DefaultMutableTreeNode root = (DefaultMutableTreeNode) taxonomy.getRoot();
          int number_children = root.getChildCount();
          for (int child = 0; child < number_children; child++)
          {
               DefaultMutableTreeNode this_child = (DefaultMutableTreeNode) root.getChildAt(child);
               addChildrenToList(parents_and_their_children, this_child);
          }

          // Format the information to return
          Object[] list_as_object = parents_and_their_children.toArray();
          String[][] results = new String[list_as_object.length][];
          for (int i = 0; i < results.length; i++)
               results[i] = (String[]) list_as_object[i];

          // Return the results
          return results;
     }


     /**
      * Returns true if the taxonomy field is empty and false if it is not.
      *
      * @return	Whether or not the taxonomy field is empty.
      */
     public boolean isTreeEmpty()
     {
          if (taxonomy == null)
               return true;
          DefaultMutableTreeNode root = (DefaultMutableTreeNode) taxonomy.getRoot();
          if (root.getChildCount() == 0)
               return true;
          else
               return false;
     }


     /**
      * Returns an array listing the classes that are found in the given
      * classifications (either overall classifications or classifications of
      * sub-sections) but not in this taxonomy. No name of a class witll be
      * returned more than once in the returned array. Returns null if there are
      * no such classes.
      *
      * @param	seg_classes   The classifications to check.
      * @return               The outstanding classes. Null is returned if there
      *                       are no outstanding classes.
      */
     public String[] getClassesInClassificationsButNotTaxonomy(SegmentedClassification[] seg_classes)
     {
          String[] classification_leaf_classes =  SegmentedClassification.getLeafClasses(seg_classes);
          String[] taxonomy_leaf_classes = getLeafLabels();

          LinkedList<String> classes_list = new LinkedList<String>();
          for (int i = 0; i < classification_leaf_classes.length; i++)
          {
               boolean found = false;
               for (int j = 0; j < taxonomy_leaf_classes.length; j++)
                    if (classification_leaf_classes[i].equals(taxonomy_leaf_classes[j]))
                    {
                    found = true;
                    j = taxonomy_leaf_classes.length;
                    }
               if (!found)
                    classes_list.add(classification_leaf_classes[i]);
          }

          if (classes_list.size() == 0)
               return null;
          else
               return classes_list.toArray(new String[1]);
     }


     /**
      * Returns an array listing the classes that are found in this taxonomy but
      * not in the given classifications (either overall classifications or
      * classifications of sub-sections). No name of a class witll be returned
      * more than once in the returned array. Returns null if there are no such
      * classes.
      *
      * @param	seg_classes   The classifications to check.
      * @return               The outstanding classes. Null is returned if there
      *                       are no outstanding classes.
      */
     public String[] getClassesInTaxonomyButNotInClassifications(SegmentedClassification[] seg_classes)
     {
          String[] taxonomy_leaf_classes = getLeafLabels();
          String[] classification_leaf_classes =  SegmentedClassification.getLeafClasses(seg_classes);

          LinkedList<String> classes_list = new LinkedList<String>();
          for (int i = 0; i < taxonomy_leaf_classes.length; i++)
          {
               boolean found = false;
               for (int j = 0; j < classification_leaf_classes.length; j++)
                    if (classification_leaf_classes[j].equals(taxonomy_leaf_classes[i]))
                    {
                    found = true;
                    j = classification_leaf_classes.length;
                    }
               if (!found)
                    classes_list.add(taxonomy_leaf_classes[i]);
          }

          if (classes_list.size() == 0)
               return null;
          else
               return classes_list.toArray(new String[1]);
     }


     /**
      * Parses a taxonomy_file XML file and returns a Taxonomy object that has
      * had its taxonomy field populated with the file's contents. Throws an
      * informative exception if a problem occurs or if the file path or the
      * file itself is invalid. Sets the name of the root to the file name of
      * the taxonomy with the extension removed.
      *
      * @param	taxonomy_file_path The path of the XML file to parse.
      * @return                    The loaded taxonomy.
      * @throws	Exception          Informative exceptions is thrown if an
      *                            invalid file or file path is specified.
      */
     public static Taxonomy parseTaxonomyFile(String taxonomy_file_path)
     throws Exception
     {
          DefaultTreeModel[] parse_results =
               (DefaultTreeModel[])(XMLDocumentParser.parseXMLDocument(taxonomy_file_path, "taxonomy_file"));
          Taxonomy new_taxonomy = new Taxonomy(parse_results[0]);
          setRootName(new_taxonomy, taxonomy_file_path);
          return new_taxonomy;
     }


     /**
      * Saves a taxonomy_file XML file with the contents specified in
      * the taxonomy_to_save parameter and the comments specified in the
      * comments parameter.
      *
      * @param	taxonomy_to_save   The taxonomy to save.
      * @param	to_save_to         The file to save to.
      * @param	comments           Any comments to be saved inside the
      *                            comments element of the XML file.
      * @throws	Exception          An informative exception is thrown if
      *                            the file cannot be saved.
      */
     public static void saveTaxonomy( Taxonomy taxonomy_to_save,
          File to_save_to,
          String comments )
          throws Exception
     {
          try
          {
               // Prepare stream writer
               FileOutputStream to = new FileOutputStream(to_save_to);
               DataOutputStream writer = new DataOutputStream(to);

               // Write the header and the first element of the XML file
               String pre_tree_part = new String
                    (
                    "<?xml version=\"1.0\"?>\n" +
                    "<!DOCTYPE taxonomy_file [\n" +
                    "   <!ELEMENT taxonomy_file (comments, parent_class+)>\n" +
                    "   <!ELEMENT comments (#PCDATA)>\n" +
                    "   <!ELEMENT parent_class (class_name, sub_class*)>\n" +
                    "   <!ELEMENT class_name (#PCDATA)>\n" +
                    "   <!ELEMENT sub_class (class_name, sub_class*)>\n" +
                    "]>\n\n" +
                    "<taxonomy_file>\n\n" +
                    "   <comments>" + comments + "</comments>\n\n"
                    );
               writer.writeBytes(pre_tree_part);

               // Write the XML code to represent the contents of the taxonomy field
               DefaultMutableTreeNode taxonomy_node = (DefaultMutableTreeNode) taxonomy_to_save.taxonomy.getRoot();
               for (int i = 0; i < taxonomy_node.getChildCount() ; i++)
               {
                    writer.writeBytes("   <parent_class>\n");
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) taxonomy_node.getChildAt(i);
                    writer.writeBytes(assembleTreeXMLElements(child, 1));
                    writer.writeBytes("   </parent_class>\n\n");
               }
               writer.writeBytes("</taxonomy_file>");

               // Close the output stream
               writer.close();
          }
          catch (Exception e)
          {
               throw new Exception("Unable to write file " + to_save_to.getName() + ".");
          }
     }


     /**
      * Sets the root of the given taxonomy to the name of the given file path
      * (with directory structure and extension stripped off.
      *
      * @param	given_taxonomy	The Taxonomy to update.
      * @param	file_path	The path of the file to base the root name on.
      */
     public static void setRootName(Taxonomy given_taxonomy, String file_path)
     {
          String file_name = mckay.utilities.staticlibraries.StringMethods.removeExtension(mckay.utilities.staticlibraries.StringMethods.convertFilePathToFileName(file_path));
          ((DefaultMutableTreeNode) given_taxonomy.taxonomy.getRoot()).setUserObject(file_name);
     }


     /* PRIVATE METHODS *******************************************************/


     /**
      * Recursively searches through the given node in the taxonomy and all of
      * its children, if any, and deletes those nodes with the same name as
      * name_of_class_to_delete as well as all of their children.
      *
      * @param	name_of_class_to_delete The name that nodes must have in order
      *					to be deleted along with their children.
      * @param	node			The node to begin the search on.
      */
     private void searchForBranchesToRemove( String name_of_class_to_delete,
          DefaultMutableTreeNode node )
     {
          String label = (String) node.getUserObject();
          if ( label.equals(name_of_class_to_delete) )
          {
               try
               { deleteBranchOfTaxonomy(node); }
               catch (Exception e)
               { }
          }
          else if (!node.isLeaf())
               for (int i = 0; i < node.getChildCount(); i++)
               {
               DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
               searchForBranchesToRemove(name_of_class_to_delete, node);
               }
     }


     /**
      * Adds the name of of the given DefaultMutableTreeNode to the given
      * LinkedList as well as its direct decendants. Recurses through the
      * children of the given node, applying the same procedure. Does nothing
      * if thenode is a leaf.
      *
      * @param	list	The list to add nodes to.
      * @param	node	The node to be added to the list.
      */
     private void addChildrenToList(LinkedList<String[]> list, DefaultMutableTreeNode node)
     {
          if (!node.isLeaf())
          {
               // Find the number of children of node
               int number_children = node.getChildCount();

               // Find the direct descendants of node
               String[] direct_descendants = new String[number_children];
               for (int child = 0; child < number_children; child++)
               {
                    DefaultMutableTreeNode this_child = (DefaultMutableTreeNode) node.getChildAt(child);
                    direct_descendants[child] = (String) this_child.getUserObject();
               }

               // Add the name of node and its direct descendants to the list
               String[] results = new String[1 + number_children];
               results[0] = (String) node.getUserObject();
               for (int child = 0; child < number_children; child++)
                    results[child + 1] = direct_descendants[child];
               list.add(results);

               // Recursively apply this process to non-leaf children
               for (int child = 0; child < number_children; child++)
               {
                    DefaultMutableTreeNode this_child = (DefaultMutableTreeNode) node.getChildAt(child);
                    if (!this_child.isLeaf())
                         addChildrenToList(list, this_child);
               }
          }
     }


     /**
      * Recursively assembles a formatted string describing the (sub)tree with a
      * root of the given node that is actually at the given depth of the full
      * tree.
      *
      * @param	node	The DefaultMutableTreeNode to serve as the roor of the
      *			(sub)tree.
      * @param	depth	The depth in the full tree of the given node.
      * @return		A string descrcribing the sub(tree).
      */
     private static String assembleTreeElements(DefaultMutableTreeNode node, int depth)
     {
          String tree_string = new String("");

          if (depth != 1)
          {
               for (int j = 0; j <= depth; j++)
                    tree_string += " ";
          }

          if (node.isLeaf())
          {
               for (int j = 0; j <= depth + 1; j++)
                    tree_string += " ";
               tree_string += node.getUserObject() + "\n";
          }
          else
          {
               for (int i = 0; i < node.getChildCount(); i++)
               {
                    if (i == 0)
                    {
                         for (int j = 0; j <= depth + 1; j++)
                              tree_string += " ";
                         tree_string += node.getUserObject() + "\n";
                    }
                    DefaultMutableTreeNode child =
                         (DefaultMutableTreeNode) node.getChildAt(i);
                    tree_string += assembleTreeElements(child, depth + 1);
               }
          }

          return tree_string;
     }


     /**
      * Recursively assembles the XML code to represent the contents of the
      * taxonomy field.
      */
     private static String assembleTreeXMLElements(DefaultMutableTreeNode node, int depth)
     {
          String XML_string = new String("");

          if (depth != 1)
          {
               for (int j = 0; j <= depth; j++)
                    XML_string += "   ";
               XML_string += "<sub_class>\n";
          }

          if (node.isLeaf())
          {
               for (int j = 0; j <= depth + 1; j++)
                    XML_string += "   ";
               XML_string += "<class_name>" + node.getUserObject() + "</class_name>\n";
          }
          else
          {
               for (int i = 0; i < node.getChildCount(); i++)
               {
                    if (i == 0)
                    {
                         for (int j = 0; j <= depth + 1; j++)
                              XML_string += "   ";
                         XML_string += "<class_name>" + node.getUserObject() + "</class_name>\n";
                    }
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                    XML_string += assembleTreeXMLElements(child, depth + 1);
               }
          }

          if (depth != 1)
          {
               for (int j = 0; j <= depth; j++)
                    XML_string += "   ";
               XML_string += "</sub_class>\n";
          }

          return XML_string;
     }

     /**
      * Automatically generates a flat taxonomy based on the class labels in the
      * given SegmentedClassification object.
      *
      * @param classifications  The SegmentedClassification object from which to
      *                         derive the classes of the Taxonomy.
      * @return                 A flat Taxonomy object containing the same classes
      *                         as the given SegmentedClassification object.
      */
     public static Taxonomy generateTaxonomy(SegmentedClassification[] classifications)
    {
         // The Taxonomy object to return
        Taxonomy generated = new Taxonomy();

        // Find all classes
        if (classifications != null)
        {
            /* For each SegmentedClassificaion, look at each classification and
            the classifications of each subsection.*/
            for (int i = 0; i < classifications.length; i++)
            {
                if(classifications[i].classifications != null)
                {
                for (int j = 0; j < classifications[i].classifications.length; j++)
                {
                    // Add to Taxonomy if not already present
                    if (!mckay.utilities.staticlibraries.StringMethods.isStringInArray
                            (classifications[i].classifications[j], generated.getParentLabels()))
                        generated.addClass(classifications[i].classifications[j]);
                }
                }
                if (classifications[i].sub_classifications != null)
                {
                    for (int k = 0; k < classifications[i].sub_classifications.length; k++)
                    {
                        for (int l = 0; l < classifications[i].sub_classifications[k].classifications.length; l++)
                        {
                            // Add to Taxonomy if not already present
                            if (!mckay.utilities.staticlibraries.StringMethods.isStringInArray
                                    (classifications[i].sub_classifications[k].classifications[l], generated.getParentLabels()))
                                generated.addClass(classifications[i].sub_classifications[k].classifications[l]);
                        }
                    }
                }
            }
        }
        return generated;
    }

}