/*
 * GUIPreferencesPanel.java
 * Version 2.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package ace.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import ace.datatypes.*;


/**
 * A holder class that will eventually hold code for the GUI preferences pane.
 *
 * @author Cory McKay (ACE 1.x) and Jessica Thompson (ACE 2.x)
 */
public class GUIPreferencesPanel
     extends JPanel
     implements ActionListener
{
     /* FIELDS ****************************************************************/


     // The JFrame that holds this JPanel.
     private	MainGUIFrame	parent;


     /* CONSTRUCTORS **********************************************************/


     /**
      * Basic constructor.
      *
      * @param	parent_frame	The JFrame that holds this JPanel.
      */
     public GUIPreferencesPanel(MainGUIFrame parent_frame)
     {
          parent = parent_frame;
     }


     /* PUBLIC METHODS ********************************************************/


     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event		The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {

     }


     /**
      * Cause this panel to update to reflect the contents of the given
      * configuration file.
      *
      * @param	gui_preferences_path	The path of the file containing the
      *					GUI preferences to load.
      */
     public void loadNewPanelContents(String gui_preferences_path)
     {
// error check file

// UPDATE GUI

// must be able to deal with empty contents
     }


     /**
      * Save the currently loaded GUI preferencs into an XML file.
      *
      * @param	save_path	The path to save the GUI preferences file to.
      */
     public void saveAsFile(String save_path)
     {

// must be able to deal with not saving anything if field of this object is null

// must be able to deal with case where there is something to save but no save_path specified in parameter

     }
}