/*
 * DoubleProgressBarDialog.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.progressbars;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A progress bar frame that contains two progress bars. The top one shows
 * the progress of an overall task, and the bottom one shows the progress of a
 * subtask that is part of this overall task.
 *
 * <p>Aside from the two progress bars, this frame also displays the name of
 * the overall task and of the subtask currently being processed, as well as
 * the total time elapsed and the estimated processing time remaining for each
 * bar.
 *
 * <p>The user may cancel processing during at any time by pressing the
 * <i>Cancel</i> button.
 *
 * <p>It is assumed that all sub-tasks will take an equal amount of time
 * which, while incorrect in most cases, is assumed to be close enough
 *
 * <p>The actual processing is done by the DoubleProgressBarTaskMonitor
 * object that is passed to the constructor. This object also provides the
 * information needed to keep this window updated.
 *
 * <p>The parent_window JFrame which is passed to the constructor is made
 * inaccessible to the user during processing in order to avoid concurrency
 * problems.
 *
 * @author Cory McKay
 */
public class DoubleProgressBarDialog
     extends JFrame
     implements ActionListener
{
     /* FIELDS ****************************************************************/
     
     
     // Performs and monitors the task that is reproted on by this windos
     private DoubleProgressBarTaskMonitor    task_monitor;
     
     // Timer that queries task_monitor to keep the progress bars up to date
     private Timer                           timer;
     
     // JFrame that is involved in instantiation of this window
     private JFrame                          parent_window;
     
     // Components to display progress
     private JProgressBar                    top_progress_bar;
     private JProgressBar                    bottom_progress_bar;
     private JTextArea                       top_report_text_area;
     private JTextArea                       bottom_report_text_area;
     
     // Containers to hold Swing elements
     private Container                       content_pane;
     private JPanel                          progress_panel;
     private JPanel                          button_panel;
     
     // Buttons
     private JButton                         cancel_button;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * A constructor that configures and displays the window, triggers
      * processing by the given DoubleProgressBarTaskMonitor and initiates
      * processing to keep the progress bars updated during processing.
      *
      * @param	parent_window                Name of the JFrame that
      *                                      instantiated this window. May be
      *                                      null.
      * @param	task_monitor                 Performs processing and provides
      *                                      information needed for updates to
      *                                      this window.
      * @param	time_between_polls           Time in milliseconds between
      *                                      polling of
      *                                      task_checker_and_performer.
      */
     public DoubleProgressBarDialog(JFrame parent_window,
          DoubleProgressBarTaskMonitor task_monitor,
          int time_between_polls)
     {
          // Store the task monitor
          this.task_monitor = task_monitor;
          
          // Disable the parent window
          this.parent_window = parent_window;
          if (parent_window != null)
               parent_window.setEnabled(false);
          
          // Configure overall window settings and instantiate related panels
          setTitle("Progress");
          int horizontal_gap = 6; // horizontal space between GUI elements
          int vertical_gap = 4; // horizontal space between GUI elements
          content_pane = getContentPane();
          content_pane.setLayout(new BorderLayout(horizontal_gap, vertical_gap));
          progress_panel = new JPanel(new GridLayout(4, 1, horizontal_gap, vertical_gap));
          button_panel = new JPanel();
          
          // Configure progress bars
          top_progress_bar = new JProgressBar(0, 1);
          top_progress_bar.setValue(0);
          top_progress_bar.setStringPainted(true);
          bottom_progress_bar = new JProgressBar(0, 1);
          bottom_progress_bar.setValue(0);
          bottom_progress_bar.setStringPainted(true);
          
          // Configure text areas
          int number_text_columns = 50;
          int number_text_rows = 2;
          top_report_text_area = new JTextArea(number_text_rows, number_text_columns);
          top_report_text_area.setEditable(false);
          top_report_text_area.setLineWrap(false);
          top_report_text_area.setCursor(null); //inherit the panel's cursor (see Java bug 4851758)
          bottom_report_text_area = new JTextArea(number_text_rows, number_text_columns);
          bottom_report_text_area.setEditable(false);
          bottom_report_text_area.setLineWrap(false);
          bottom_report_text_area.setCursor(null); //inherit the panel's cursor (see Java bug 4851758)
          
          // Configure buttons
          cancel_button = new JButton("Cancel");
          cancel_button.addActionListener(this);
          
          // Add labels and previously created elements to panels and container
          progress_panel.add(top_progress_bar);
          progress_panel.add(top_report_text_area);
          progress_panel.add(bottom_progress_bar);
          progress_panel.add(bottom_report_text_area);
          button_panel.add(cancel_button);
          content_pane.add(progress_panel, BorderLayout.CENTER);
          content_pane.add(button_panel, BorderLayout.SOUTH);
          
          // Cause program to react when the exit box is pressed
          addWindowListener(new WindowAdapter()
          {
               public void windowClosing(WindowEvent e)
               {
                    cancel();
               }
          });
          
          // Create a timer to check progress every time_between_polls and update
          // this window accordingly. Also takes appropriate actions when
          // processing is finished.
          timer = new Timer(time_between_polls, new ProgressBarActionListener());
          
          // Prepare to begin the task and monitor it
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          timer.start();
          
          // Center and dispay this window
          pack();
          setLocationRelativeTo(parent_window);
          setVisible(true);
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Calls the appropriate methods when the buttons are pressed.
      *
      * @param	event		The event that is to be reacted to.
      */
     public void actionPerformed(ActionEvent event)
     {
          if (event.getSource().equals(cancel_button))
               cancel();
     }
     
     
     /**
      * React to the exit box or the cancel button.
      * Stop the process, reenable the parent_window and hide this window
      * without taking any actions to enact results of processing.
      */
     public void cancel()
     {
          timer.stop();
          task_monitor.stop();
          setCursor(null); // turn off the wait cursor
          if (parent_window != null)
               parent_window.setEnabled(true);
          this.setVisible(false);
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * React to finishing the task. Beeps, stops the timer, reenables the
      * parent window and hides this window.
      */
     private void done()
     {
          Toolkit.getDefaultToolkit().beep(); // beep when done
          timer.stop(); // stop the timer
          setCursor(null); //turn off the wait cursor
          if (parent_window != null)
               parent_window.setEnabled(true);
          this.setVisible(false);
     }
     
     
     /* INTERNAL CLASSES ******************************************************/
     
     
     /**
      * Listener (to be attached to a Timer) that checks the task_monitor
      * whenever the Timer goes off and updates the task_monitor's status
      * and this progress bar's status appropriately.
      */
     private class ProgressBarActionListener
          implements ActionListener
     {
          public void actionPerformed(ActionEvent evt)
          {
               if (task_monitor.startNewTopProgress())
               {
                    top_progress_bar.setValue(0);
                    top_progress_bar.setMaximum(task_monitor.getTopLengthOfTask());
               }
               top_progress_bar.setValue(task_monitor.getTopCurrentAmountCompleted());
               top_report_text_area.setText(task_monitor.getTopTextMessage());
               top_report_text_area.setCaretPosition(top_report_text_area.getDocument().getLength());
               
               if (task_monitor.startNewBottomProgress())
               {
                    bottom_progress_bar.setValue(0);
                    bottom_progress_bar.setMaximum(task_monitor.getBottomLengthOfTask());
               }
               bottom_progress_bar.setValue(task_monitor.getBottomCurrentAmountCompleted());
               bottom_report_text_area.setText(task_monitor.getBottomTextMessage());
               bottom_report_text_area.setCaretPosition(bottom_report_text_area.getDocument().getLength());
               
               if (task_monitor.isDone())
                    done();
          }
     }
}
