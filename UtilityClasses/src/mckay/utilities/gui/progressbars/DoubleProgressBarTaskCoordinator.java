/*
 * DoubleProgressBarTaskCoordinator.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.progressbars;

import javax.swing.JOptionPane;


/**
 * Monitors, updates and coordinates a DoubleProgressBarDialog progress bar and
 * oversees processing. An error message is generated if a problem occurs
 * during processing. Tasks may be cancelled.
 *
 * @author Cory McKay
 */
public class DoubleProgressBarTaskCoordinator
     extends DoubleProgressBarTaskMonitor
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The object that will perform actual processing.
      */
     private ProcessCoordinator              coordinator;
     
     /**
      * The dialog box that this object is monitoring.
      */
     private DoubleProgressBarDialog         progress_dialog;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Prepares the processor that will perform processing.
      *
      * @param coordinator    The object that will perform actual processing.
      */
     public DoubleProgressBarTaskCoordinator(ProcessCoordinator coordinator)
     {
          super();
          this.coordinator = coordinator;
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Begin processing with the progress bar.
      */
     public void go()
     {
          // Initialize the progress bar settings
          initializeOverallTask(coordinator.findNumberOfTopLevelTasks());
          
          // Begin processing
          final SwingWorker worker = new AnalysisSwingWorker();
          worker.start();
     }
     
     
     /**
      * Store the progress bar whose data this object will monitor.
      *
      * @param progress_dialog     The prgress dialog that is being monitored.
      */
     public void setProgressBarDialog(DoubleProgressBarDialog progress_dialog)
     {
          this.progress_dialog = progress_dialog;
     }
     
     
     /* INTERNAL CLASSES ******************************************************/
     
     
     /**
      * Performs the actual analysis tasks.
      */
     private class ProcessingCoordinator
     {
          /**
           * This constructor initiates the processing that is monitored by the
           * progress bar.
           *
           * @throws     Exception Forwards on exceptions that occur during
           *                       processing.
           */
          public ProcessingCoordinator()
          throws Exception
          {
               coordinator.performProcessing();
          }
     }
     
     
     /**
      * Initiates the processing tasks and reports exceptions in a dialog box.
      */
     private class AnalysisSwingWorker
          extends SwingWorker
     {
          /**
           * Instantiates the ProcessingCoordinator. If an error occurs during
           * this process, an informative dialog box is displayed and the
           * dialog box and processing is cancelled.
           *
           * @return     The Object performing analysis. Null is returned if a
           *             a problem occured.
           */
          public Object construct()
          {
               ProcessingCoordinator coordinator = null;
               
               try
               {
                    coordinator = new ProcessingCoordinator();
               }
               catch (Throwable t)
               {
                    // React to the Java Runtime running out of memory
                    if (t.toString().equals("java.lang.OutOfMemoryError"))
                         JOptionPane.showMessageDialog(null, "The Java Runtime ran out of memory. Please rerun this program with a higher amount of memory assigned to the Java Runtime heap.\n\nAnalysis cancelled.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    else
                    {
                         // t.printStackTrace();
                         JOptionPane.showMessageDialog(null, t.getMessage() + "\n\nAnalysis cancelled.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                    
                    // Mark task as cancelled
                    stop();
                    progress_dialog.cancel();
                    
                    // Display error details
                    // t.printStackTrace();
               }
               
               return coordinator;
          }
     }
}