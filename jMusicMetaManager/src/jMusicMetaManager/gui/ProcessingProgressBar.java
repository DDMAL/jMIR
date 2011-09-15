/*
 * ProcessingProgressBar.java
 * Version 1.2
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package jMusicMetaManager.gui;

import javax.swing.JOptionPane;
import jMusicMetaManager.*;
import mckay.utilities.gui.progressbars.DoubleProgressBarDialog;
import mckay.utilities.gui.progressbars.DoubleProgressBarTaskMonitor;
import mckay.utilities.gui.progressbars.SwingWorker;


/**
 * Monitors, updates and coordinates a DoubleProgressBarDialog progress bar and
 * oversees processing. An error message is generated if a problem occurs
 * during processing. Tasks may be cancelled.
 *
 * @author Cory McKay
 */
public class ProcessingProgressBar
     extends DoubleProgressBarTaskMonitor
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The object that will perform actual analysis.
      */
     private AnalysisProcessor               processor;
     
     /**
      * The dialog box that this object is monitoring.
      */
     private DoubleProgressBarDialog         progress_dialog;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Prepares the processor that will perform analysis.
      *
      * @param parent         The GUI panel instantiating this object.
      * @param preferences    The preferences to base analysis upon.
      */
     public ProcessingProgressBar(OptionsPanel parent,
          AnalysisPreferences preferences)
     {
          super();
          processor = new AnalysisProcessor(preferences, parent, this);
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Begin processing with the progress bar.
      */
     public void go()
     {
          // Initialize the progress bar settings
          initializeOverallTask(processor.findNumberOfTasks());
          
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
     private class AnalysisCoordinator
     {
          /**
           * This constructor initiates the processing that is monitored by the
           * progress bar.
           *
           * @throws     Exception Forwards on exceptions that occur during
           *                       processing.
           */
          public AnalysisCoordinator()
          throws Exception
          {
               processor.performProcessing();
          }
     }
     
     
     /**
      * Initiates the processing tasks and reports exceptions in a dialog box.
      */
     private class AnalysisSwingWorker
          extends SwingWorker
     {
          /**
           * Instantiates the AnalysisCoordinator. If an error occurs during
           * this process, an informative dialog box is displayed and the
           * dialog box and processing are cancelled.
           *
           * @return     The Object performing analysis. Null is returned if a
           *             a problem occured.
           */
          public Object construct()
          {
               AnalysisCoordinator coordinator = null;
               
               try
               {
                    coordinator = new AnalysisCoordinator();
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
