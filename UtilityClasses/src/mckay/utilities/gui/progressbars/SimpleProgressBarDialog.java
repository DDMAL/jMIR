/*
 * SimpleProgressBarDialog.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.progressbars;

import java.awt.*;
import javax.swing.*;


/**
 * A dialog box containing only a progress bar. It is constructed with a set
 * number of tasks to perform and these are incremented one by one by
 * calling the incrementStatus method. This dialog box is modal in the sense
 * that it prevents its parent window from being modified by the user, but
 * it allows the parent window to perform tasks. A separate thread is not
 * necessary to use this class. This dialog box disappears when the taks is
 * complete. There is no cancel functionality.
 *
 * @author Cory McKay
 */
public class SimpleProgressBarDialog
     extends JDialog
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The window that called this dialog box and that is performing the task
      * whose progress is being measured.
      */
     private   Component      parent;
     
     /**
      * The progress bar measuring the progress of the task.
      */
     private   JProgressBar   progress_bar;
     
     /**
      * The current task number being performed.
      */
     private   int            task_status;
     
     /**
      * The total number of tasks to be performed. The maximum value of the
      * progress bar.
      */
     private   int            number_tasks;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Creates a new instance of SimpleProgressBarDialog. Displays the window,
      * initializes the progress bar and deactivates the parent window.
      *
      *
      * @param number_tasks   The total number of tasks to be performed. The
      *                       maximum value of the progress bar.
      * @param parent         The parent window. May be null.
      */
     public SimpleProgressBarDialog(int number_tasks, Component parent)
     {
          // Initialize the progress dialog box
          super();
          setTitle("Task Progress");
          
          // Set the size of the dialog box and center it relative to the parent
          // window
          setSize(225, 75);
          setLocationRelativeTo(parent);
          
          // Disable the parent window
          if (parent != null)
               parent.setEnabled(false);
          
          // Set fields
          this.parent = parent;
          this.number_tasks = number_tasks;
          task_status = 0;
          
          // Set up the progress bar
          progress_bar = new JProgressBar(0, number_tasks);
          progress_bar.setStringPainted(true);
          progress_bar.setValue(task_status);
          
          // Set the wait cursor
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          
          // Set up the GUI
          setLayout(new BorderLayout());
          add(progress_bar, BorderLayout.CENTER);
          setVisible(true);
          
          // Paint the progress bar immediately
          progress_bar.paintImmediately(progress_bar.getBounds());
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Increment the status of the progress bar. This method must be called
      * each time that a new part of the task has been performed. This dialog
      * box is hidden if all parts of the task are complete.
      */
     public void incrementStatus()
     {
          // Make sure that the focus is on the progress bar
          requestFocus();
          
          // Store the new task numer
          task_status++;
          progress_bar.setValue(task_status);
          
          // Paint the progress bar immediately
          progress_bar.paintImmediately(progress_bar.getBounds());
          
          // End the progress bar if all tasks have been completed.
          if (task_status == number_tasks) done();
     }
     
     
     /**
      * To be called by the incrementStatus method when all tasks are complete.
      * Resets the cursor, hides the dialog box and reactivates the parent
      * window. Can also be called by an external method to end processing
      * prematurely.
      */
     public void done()
     {
          // Reset the cursor
          setCursor(null);
          
          // Hide the dialog
          setVisible(false);
          
          // Reactivate the parent window
          if (parent != null)
               parent.setEnabled(true);
          if (parent instanceof Window)
               ((Window) parent).toFront();
     }
}