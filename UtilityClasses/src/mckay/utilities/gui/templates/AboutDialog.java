/*
 * AboutDialog.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.templates;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * A basic information dialog box to provide information about a piece of
 * software.
 *
 * @author Cory McKay
 */
public class AboutDialog
     extends JDialog
{
     /**
      * Prepares and displays the dialog box.
      * @param parent         The JFrame that instantiated this object.
      * @param title          The name of the software.
      * @param owner          The owner/author of the software.
      * @param year           The year in which the software is copyrighted.
      * @param institution    The institution at which the software was made.
      */
     public AboutDialog(JFrame parent, String title, String owner, String year,
          String institution)
     {
          // Give the dialog box its owner, its title and make it modal
          super(parent, title, true);
          Container content_pane = getContentPane();
          
          // Add label to centre
          JLabel label = new JLabel( "<HTML><CENTER><H1><I>" + title +
               "</I></H1><HR><p>© " + owner + " " + year + "<br>" +
               institution + "</CENTER></HTML>" );
          JPanel label_panel = new JPanel();
          label_panel.setBorder(BorderFactory.createEmptyBorder(1, 7, 1, 7));
          label_panel.add(label);
          content_pane.add(label_panel, BorderLayout.CENTER);
          
          // Add OK button to close the dialog box
          JButton ok = new JButton("OK");
          ok.addActionListener(new
               ActionListener()
          {
               public void actionPerformed(ActionEvent evt)
               {
                    setVisible(false);
               }
          });
          JPanel button_panel = new JPanel();
          button_panel.add(ok);
          
          // Center and display components
          content_pane.add(button_panel, BorderLayout.SOUTH);
          pack();
          setLocationRelativeTo(parent);
          setVisible(true);
     }
}
