/*
 * OuterFrame.java
 * Version 1.2.1
 *
 * Last modified on July 26, 2010.
 * McGill University
 */

package jMusicMetaManager.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import mckay.utilities.gui.templates.AboutDialog;
import mckay.utilities.gui.templates.HTMLFramesPanel;
import mckay.utilities.gui.templates.HelpDialog;


/**
 * The outer component of the jMusicMetaManager GUI that is used to hold tabbed
 * panes and menus.
 *
 * <p>The Analysis menu items allow the user to restore default processing and
 * report generation preferences or run an analysis.
 *
 * <p>The Export menu items export metadata derived from an iTunes XML file
 * and/or from MP3 ID3 tags to either Weka ARFF or ACE XML files.
 *
 * <p>The Help menu item opens a window displaying the interactive window. The
 * About menu item displays version and ownership information.
 *
 * <p>The Options tab displays preferences controlling the details of the
 * metadata processing. Processing can also be initialized from this pane.
 *
 * <p>The Report tab displays the results of the last processing.
 *
 * @author Cory McKay
 */
public class OuterFrame
     extends JFrame
     implements ActionListener
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * Contains the tabbed panels.
      */
     private JTabbedPane      tabbed_pane;
     
     /**
      * Allows user control of extraction options.
      */
     private OptionsPanel     options_panel;
     
     /**
      * Shows the results of the last extraction.
      */
     private HTMLFramesPanel  report_panel;
     
     /**
      * Displays the on-line manual.
      */
     private HelpDialog       help_dialog;
     
     /**
      * Holds menu items.
      */
     private JMenuBar         menu_bar;

     /**
      * The Export menu.
      */
     private JMenu            analysis_menu;
     
     /**
      * Export music collection metadata to Weka ARFF format.
      */
     private JMenuItem        restore_defaults_menu_item;
     
     /**
      * Export music collection metadata to ACE XML format.
      */
     private JMenuItem        begin_analysis_menu_item;

     /**
      * The Export menu.
      */
     private JMenu            export_menu;
     
     /**
      * Export music collection metadata to Weka ARFF format.
      */
     private JMenuItem        export_to_ARFF_menu_item;
     
     /**
      * Export music collection metadata to ACE XML format.
      */
     private JMenuItem        export_to_ACE_XML_menu_item;
     
     /**
      * The Information menu.
      */
     private JMenu            information_menu;
     
     /**
      * Displays ownership and version information.
      */
     private JMenuItem        about_menu_item;
     
     /**
      * Makes the HelpDialog visible.
      */
     private JMenuItem        help_menu_item;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Sets up and displays the jMusicMetaManager GUI.
      */
     public OuterFrame()
     {
          // Set the title of the window
          setTitle("jMusicMetaManager");
          
          // Set up the menus
          menu_bar = new JMenuBar();
          analysis_menu = new JMenu("Analysis");
          analysis_menu.setMnemonic('a');
          restore_defaults_menu_item = new JMenuItem("Restore Default Settings");
          restore_defaults_menu_item.setMnemonic('r');
          restore_defaults_menu_item.addActionListener(this);
          analysis_menu.add(restore_defaults_menu_item);
          begin_analysis_menu_item = new JMenuItem("Begin Metadata Analysis");
          begin_analysis_menu_item.setMnemonic('b');
          begin_analysis_menu_item.addActionListener(this);
          analysis_menu.add(begin_analysis_menu_item);
          menu_bar.add(analysis_menu);
          export_menu = new JMenu("Export");
          export_menu.setMnemonic('e');
          export_to_ARFF_menu_item = new JMenuItem("Export to Weka ARFF");
          export_to_ARFF_menu_item.setMnemonic('w');
          export_to_ARFF_menu_item.addActionListener(this);
          export_menu.add(export_to_ARFF_menu_item);
          export_to_ACE_XML_menu_item = new JMenuItem("Export to ACE XML");
          export_to_ACE_XML_menu_item.setMnemonic('x');
          export_to_ACE_XML_menu_item.addActionListener(this);
          export_menu.add(export_to_ACE_XML_menu_item);
          menu_bar.add(export_menu);
          information_menu = new JMenu("Information");
          information_menu.setMnemonic('i');
          about_menu_item = new JMenuItem("About");
          about_menu_item.setMnemonic('a');
          about_menu_item.addActionListener(this);
          information_menu.add(about_menu_item);
          help_menu_item = new JMenuItem("Help");
          help_menu_item.setMnemonic('h');
          help_menu_item.addActionListener(this);
          information_menu.add(help_menu_item);
          menu_bar.add(information_menu);
          
          // Set up the tabs
          tabbed_pane = new JTabbedPane();
          options_panel = new OptionsPanel(this);
          tabbed_pane.addTab("Options", options_panel);
          report_panel = new HTMLFramesPanel(null, null);
          tabbed_pane.addTab("Report", report_panel);
          
          // Set the default selected pane to the options_panel
          tabbed_pane.setSelectedIndex(0);
          
          // Disable the report_panel by default
          enableReportPanel(false);
          
          // Set up help dialog box
          help_dialog = new HelpDialog("ProgramFiles" + File.separator + "Manual" + File.separator + "contents.html", 
               "ProgramFiles" + File.separator + "Manual" + File.separator + "splash.html" );
          
          // Cause program to quit when the exit box is pressed
          addWindowListener(new WindowAdapter()
          {
               public void windowClosing(WindowEvent e)
               {
                    System.exit(0);
               }
          });
          
          // Add items to the GUI
          setJMenuBar(menu_bar);
          Container content_pane = getContentPane();
          content_pane.add(tabbed_pane);
          
          // Combine GUI elements into this frame at the left corner of the
          // screen with a size of 800 x 600
          setBounds(0, 0, 800, 600);
          
          // Display the GUI
          this.setVisible(true);
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event    The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          // React to the restore_defaults_menu_item
          if (event.getSource().equals(restore_defaults_menu_item))
               options_panel.restoreDefaults();
          
          // React to the begin_analysis_menu_item
          if (event.getSource().equals(begin_analysis_menu_item))
               options_panel.execute();
          
          // React to the export_to_ARFF_menu_item
          if (event.getSource().equals(export_to_ARFF_menu_item))
               options_panel.export(true, false);
          
          // React to the export_to_ACE_XML_menu_item
          if (event.getSource().equals(export_to_ACE_XML_menu_item))
               options_panel.export(false, true);
          
          // React to the about_menu_item
          if (event.getSource().equals(about_menu_item))
               new AboutDialog(this, "jMusicMetaManager 1.2.1", "Cory McKay",
                    "2010 (GNU GPL)", "McGill University");
          
          // React to the view_manual_menu_item
          if (event.getSource().equals(help_menu_item))
               help_dialog.setVisible(true);
     }
     
     
     /**
      * Enable or disable (i.e. grey out) the Report Panel.
      *
      * @param enable    Whether or not to enable the Report Panel.
      */
     public void enableReportPanel(boolean enable)
     {
          tabbed_pane.setEnabledAt(1, enable);
     }
     
     
     /**
      * Load the HTML report files at the given path into the Report Panel. The
      * Report Panel is enbaled and displayed.
      *
      * @param contents_path  The path of the HTML reports content file to load.
      * @param report_path    The path of the HTML report title page file to
      *                       load.
      */
     public void loadReportIntoReportPanel(String contents_path, String report_path)
     {
          report_panel.setLeftFrame(contents_path);
          report_panel.setRightFrame(report_path);
          tabbed_pane.setEnabledAt(1, true);
          tabbed_pane.setSelectedIndex(1);
     }
}
