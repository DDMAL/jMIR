/*
 * HTMLFramesPanel.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.templates;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;

/**
 * A JPanel with one frame on the left and one on the right, each of which
 * can display an HTML file. Typically, the left frame holds a list of links
 * that can be displayed in the right file. Clicking on a link in either frame
 * loads the corresponding file into the right frame.
 *
 * <p>Note that all HTML files that are to be read by objects of this class
 * should be in the same directory.
 *
 * @author Cory McKay
 */
public class HTMLFramesPanel
     extends JPanel
     implements HyperlinkListener
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The left pane.
      */
     private JEditorPane left_pane;
     
     /**
      * The right pane.
      */
     private JEditorPane right_pane;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Set up the frame.
      *
      * @param left_start_path     The path to an HTML file to load into the
      *                            left panel. May be null if there is no
      *                            default file.
      * @param right_start_path    The path to an HTML file to load into the
      *                            right panel. May be null if there is no
      *                            default file.
      */
     public HTMLFramesPanel(String left_start_path, String right_start_path)
     {
          // Prepare the panel layout
          super();
          setLayout(new BorderLayout(2, 0));
          
          // Set up the left frame
          left_pane = new JEditorPane();
          left_pane.setEditable(false);
          if (left_start_path != null)
               setLeftFrame(left_start_path);
          JScrollPane left_scroll_pane = new JScrollPane(left_pane);
          left_pane.addHyperlinkListener(this);
          left_pane.setBorder(BorderFactory.createMatteBorder(7, 7, 7, 7, Color.BLACK));
          add(left_scroll_pane, BorderLayout.WEST);
          
          // Set up the right frame
          right_pane = new JEditorPane();
          right_pane.setEditable(false);
          if (right_pane != null)
               setRightFrame(right_start_path);
          JScrollPane right_scroll_pane = new JScrollPane(right_pane);
          right_pane.addHyperlinkListener(this);
          right_pane.setBorder(BorderFactory.createMatteBorder(7, 7, 7, 7, new Color(228, 228, 228)));
          add(right_scroll_pane, BorderLayout.CENTER);
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Cause the cursor to change when it passes over a hyperlink. Also load
      * any links  clicked on into the right frame.
      *
      * @param event     The HyperlinkEvent that occured.
      */
     public void hyperlinkUpdate( HyperlinkEvent event )
     {
          if (event.getEventType() == HyperlinkEvent.EventType.ENTERED)
               this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          else if (event.getEventType() == HyperlinkEvent.EventType.EXITED)
               this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
          {
               try
               {right_pane.setPage(event.getURL());}
               catch (Exception f)
               {right_pane.setText("Unable to find requested page:\n" + event.getURL());}
          }
     }
     
     
     /**
      * Cause the left frame to display the contents of the file at the given
      * path.
      *
      * @param left_start_path     The path of the file to display.
      */
     public void setLeftFrame(String left_start_path)
     {
          try
          {
               // Clear the stream description
               Document doc = left_pane.getDocument();
               doc.putProperty(Document.StreamDescriptionProperty, null);
               
               // Display the page
               left_pane.setPage("file:" + left_start_path);
          }
          catch (Exception e)
          {
               left_pane.setText("Unable to find requested page:\n" + "file:" + left_start_path);
          }
     }
     
     
     /**
      * Cause the right frame to display the contents of the file at the given
      * path.
      *
      * @param right_start_path     The path of the file to display.
      */
     public void setRightFrame(String right_start_path)
     {
          try
          {
               // Clear the stream description
               Document doc = right_pane.getDocument();
               doc.putProperty(Document.StreamDescriptionProperty, null);
               
               // Display the page
               right_pane.setPage("file:" + right_start_path);
          }
          catch (Exception e)
          {
               right_pane.setText("Unable to find requested page:\n" + "file:" + right_start_path);
          }
     }
}
