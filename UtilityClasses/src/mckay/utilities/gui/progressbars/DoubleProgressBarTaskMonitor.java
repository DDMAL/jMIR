/*
 * DoubleProgressBarTaskMonitor.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.progressbars;


/**
 * An abstract class that is used by a DoubleProgressBarDialog windows in order
 * to perform the processing that is measured by that window as well as to keep
 * it updated. This involves two progress bars, one which shows the progress of
 * an overall task, and one which shows the progress of a subtask that is part
 * of this overall task.
 *
 * <p>All parts of this abstract class are implemented here except for the go
 * method. This method should use a SwingWorker object to start and perform the
 * task to be performed. The go method should also keep the fields of this
 * DoubleProgressBarTaskMonitor object updated so that they can be queried by
 * the calling DoubleProgressBarDialog.
 *
 * <p>It is assumed that all sub-tasks will take an equal amount of time
 * which, while incorrect in most cases, is assumed to be close enough
 *
 * <p>One part of the class implemented here essentially provides an interface
 * for the calling DoubleProgressBarTaskMonitor and calculates the time elapsed
 * and the estimated processing time remaining.
 *
 * The other part of this class provides methods that the extending class can
 * make use of to keep the progress bar fields updated. These methods consist
 * of (aside from the abstract go method) the initializeOverallTask,
 * startNewSubTask, setSubTaskProgressValue and markTaskComplete methods.
 *
 * @author Cory McKay
 */
public abstract class DoubleProgressBarTaskMonitor
{
     /* FIELDS ****************************************************************/
     
     
     /**
      * The time when processing begins.
      */
     protected	long	 overall_start_time;
     
     /**
      * The time when all processing finishes.
      */
     protected	long	 overall_end_time;
     
     /**
      * The index of the sub task currently being processed. Used for the
      * sub_task_names and sub_task_durations arrays.
      */
     protected	int      sub_task_index = 0;
     
     /**
      * An array keeping track of the name of each sub-task.
      */
     protected	String[] sub_task_names;
     
     /**
      * An array keeping track of how long each sub-task took. The entries
      * correspond to those of sub_task_names.
      */
     protected	long[]   sub_task_durations;
     
     /**
      * Whether or not a new progress bar needs to be started for the top task.
      */
     protected	boolean  new_top_task_started;
     
     /**
      * Whether or not a new progress bar needs to be started for the bottom
      * task.
      */
     protected	boolean  new_bottom_task_started;
     
     /**
      * The total number of sub-tasks in the top task.
      */
     protected	int      top_length_of_task;
     
     /**
      * The total number of sub-tasks in the bottom task.
      */
     protected	int      bottom_length_of_task;
     
     /**
      * The number of sub-tasks completed so far in the top task.
      */
     protected	int      top_current_amount_completed;
     
     /**
      * The number of sub-tasks completed so far in the bottom task.
      */
     protected	int      bottom_current_amount_completed;
     
     /**
      * The time when the top task was started.
      */
     protected	long     top_time_started;
     
     /**
      * The time when the bottom task was started.
      */
     protected	long     bottom_time_started;
     
     /**
      * Messages to be displayed for the top bar.
      */
     protected	String   top_progress_message;
     
     /**
      * Messages to be displayed for the bottom bar.
      */
     protected	String   bottom_progress_message;
     
     /**
      * Whether or not processing has been completed.
      */
     protected	boolean  task_completed;
     
     /**
      * Whether or not processing has been cancelled.
      */
     protected	boolean  task_canceled;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Basic constructor that sets fields to defaluts.
      */
     public DoubleProgressBarTaskMonitor()
     {
          sub_task_index = 0;
          top_length_of_task = 1;
          bottom_length_of_task = 1;
          top_current_amount_completed = 0;
          bottom_current_amount_completed = 0;
          top_time_started = System.currentTimeMillis();
          bottom_time_started = System.currentTimeMillis();
          new_top_task_started = false;
          new_bottom_task_started = false;
          top_progress_message = "Overall Processing";
          bottom_progress_message = new String("");
          task_completed = false;
          task_canceled = false;
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Called to start the overall task that the progress bar is monitoring.
      * Sets up a SwingWorker object to carry out the thread and sets time
      * started. Keeps the appropriate fields updated using the startNewSubTask,
      * setSubTaskProgressValue and markTaskComplete methods. The first thing
      * that this method should do is call the initializeOverallTask class.
      */
     public abstract void go();
     
     
     /**
      * Initializes the overall task. Should be the first thing called by
      * the go method in extensions of this class.
      *
      * @param number_of_subtasks  The number of subtasks in the overall job
      *                            that is being processed (i.e. the number
      *                            of increments to divide the top progress bar
      *                            into.
      */
     public void initializeOverallTask(int number_of_subtasks)
     {
          // Processing initializations
          top_time_started = System.currentTimeMillis();
          top_current_amount_completed = -1;
          top_length_of_task = number_of_subtasks;
          new_top_task_started = true;
          
          // Keep track of task times for later reference
          overall_start_time = top_time_started;
          sub_task_names = new String[top_length_of_task];
          sub_task_durations = new long[top_length_of_task];
     }
     
     
     /**
      * Called when a new sub-task is being started (i.e. when the bottom
      * progress bar is restarting). Makes the appropriate modifications to
      * both of the progress bars.
      *
      * @param tasks_in_subtask    The number of tasks that make up this new
      *                            sub-task (i.e. how many sections to divide
      *                            the bottom progress bar into).
      * @param sub_task_name       The name of this overall sub-task.
      */
     public void startNewSubTask(int tasks_in_subtask, String sub_task_name)
     {
          top_current_amount_completed++;
          
          bottom_current_amount_completed = -1;
          bottom_time_started = System.currentTimeMillis();
          
          bottom_progress_message = sub_task_name;
          bottom_length_of_task = tasks_in_subtask;
          new_bottom_task_started = true;
          
          // Keep track of task times for later reference
          sub_task_names[sub_task_index] = sub_task_name;
          sub_task_durations[sub_task_index] = bottom_time_started;
          
          if (sub_task_index != 0)
               sub_task_durations[sub_task_index - 1] = bottom_time_started - sub_task_durations[sub_task_index - 1];
          
          sub_task_index++;
     }
     
     
     /**
      * Update the progress of the bottom progress bar.
      *
      * @param progress_value How many parts of the current sub-task have been
      *                       completed.
      */
     public void setSubTaskProgressValue(int progress_value)
     {
          bottom_current_amount_completed = progress_value;
     }
     
     
     /**
      * Called if the overall task is complete. Stops processing and records
      * final task times.
      */
     public void markTaskComplete()
     {
          // Finalize end times of tasks for later reference.
          overall_end_time = System.currentTimeMillis();
          sub_task_durations[sub_task_index - 1] = overall_end_time - sub_task_durations[sub_task_index - 1];
          
          // Mark overall task as complete
          task_completed = true;
     }
     
     
     /**
      * Returns a breakdown of how long each subtask took to complete.
      *
      * @param want_raw_milliseconds    If this is true, the processing times
      *                                 are returned as milliseconds. If it
      *                                 is false, then they are formatted based
      *                                 on duration.
      * @return     The processing time logs. The first dimension corresponds
      *             to the the task number (there is also one additional entry
      *             at the end for the overall combined processing time). The
      *             second dimension is as follows: entry 0 provides the name
      *             of the sub-task and entry 1 details how long it took to
      *             complete that sub-task.
      */
     public String[][] getProcessingTimeLogs(boolean want_raw_milliseconds)
     {
          String[][] log_data = new String[sub_task_names.length + 1][2];
          
          for (int i = 0; i < log_data.length - 1; i++)
          {
               log_data[i][0] = sub_task_names[i];
               if (want_raw_milliseconds)
                    log_data[i][1] = "" + sub_task_durations[i];
               else
                    log_data[i][1] = findTimeWithProperUnits( (new Long(sub_task_durations[i] / 1000)).intValue() );
          }
          
          log_data[sub_task_names.length][0] = "TOTAL";
          if (want_raw_milliseconds)
               log_data[sub_task_names.length][1] = "" + (overall_end_time - overall_start_time);
          else
               log_data[sub_task_names.length][1] = findTimeWithProperUnits( (new Long((overall_end_time - overall_start_time)/1000)).intValue() );
          
          return log_data;
     }
     
     
     /**
      * Called if the overall task is cancelled. Stops processing.
      */
     public void stop()
     {
          task_canceled = true;
     }
     
     
     /**
      * Called to check if the overall task has been cancelled. This method is
      * typically called by DoubleProgressBarDialog objects.
      *
      * @return     Whether or not the task is marked as cancelled.
      */
     public boolean isCancelled()
     {
          return task_canceled;
     }
     
     
     /**
      * Called to find out if the overall task has completed. This method is
      * typically called by DoubleProgressBarDialog objects.
      *
      * @return     Whether all processing is finished.
      */
     public boolean isDone()
     {
          return task_completed;
     }
     
     
     /**
      * Sets the new_top_task_started field to false and returns its previous
      * value. This method is typically called by DoubleProgressBarDialog
      * objects.
      *
      * @return     The previous value of the new_top_task_started field.
      */
     public boolean startNewTopProgress()
     {
          boolean temp = new_top_task_started;
          new_top_task_started = false;
          return temp;
     }
     
     
     /**
      * Sets the new_bottom_task_started field to false and returns its previous
      * value. This method is typically called by DoubleProgressBarDialog
      * objects.
      *
      * @return     The previous value of the new_bottom_task_started field.
      */
     public boolean startNewBottomProgress()
     {
          boolean temp = new_bottom_task_started;
          new_bottom_task_started = false;
          return temp;
     }
     
     
     /**
      * Called from a DoubleProgressBarDialog to find out how many tasks in the
      * overall progress bar remain to be done.  This method is typically called
      * by DoubleProgressBarDialog objects.
      *
      * @return     The number of tasks in the overall progress bar remain to be
      *             done.
      */
     public int getTopLengthOfTask()
     {
          return top_length_of_task;
     }
     
     
     /**
      * Called from a DoubleProgressBarDialog to find out how many tasks in the
      * sub-task progress bar remain to be done. This method is typically called
      * by DoubleProgressBarDialog objects.
      *
      * @return     The number of tasks in the sub-task progress bar remain to
      *             be done.
      */
     public int getBottomLengthOfTask()
     {
          return bottom_length_of_task;
     }
     
     
     /**
      * Called from a DoubleProgressBarDialog to find out how many tasks for
      * the overall progress bar have been done. This method is typically called
      * by DoubleProgressBarDialog objects.
      *
      * @return     The number of tasks for the overall progress bar have been
      *             done.
      */
     public int getTopCurrentAmountCompleted()
     {
          return top_current_amount_completed;
     }
     
     
     /**
      * Called from a DoubleProgressBarDialog to find out how many tasks for
      * the sub-task progress bar have been done. This method is typically called by DoubleProgressBarDialog objects.
      *
      *
      * @return     The number of tasks for the sub-task progress bar have been
      *             done.
      */
     public int getBottomCurrentAmountCompleted()
     {
          return bottom_current_amount_completed;
     }
     
     
     /**
      * Returns a string indicating, for the overall progress bar, how much time
      * has already passed and the estimated time remaining. This method is
      * typically called by DoubleProgressBarDialog objects.
      *
      * @return     The informative string.
      */
     public String getTopTextMessage()
     {
          // Return empty if nothing ready yet
          if (top_length_of_task == 0)
               return new String("");
          
          // Include the message on the first line
          String return_string = new String(top_progress_message + "\n");
          
          // Include the time passed and estimate
          return_string += getTimeEstimate( (double) top_current_amount_completed,
               (double) top_length_of_task,
               (double) bottom_current_amount_completed / bottom_length_of_task,
               top_time_started );
          
          // Return the message
          return return_string;
     }
     
     
     /**
      * Returns a string indicating, for the sub-task progress bar, how much
      * time has already passed and the estimated time remaining. This method is
      * typically called by DoubleProgressBarDialog objects.
      *
      * @return     The informative string.
      */
     public String getBottomTextMessage()
     {
          // Return empty if nothing ready yet
          if (bottom_length_of_task == 0)
               return new String("");
          
          // Include the message on the first line
          String return_string = new String(bottom_progress_message + "\n");
          
          // Include the time passed and estimate
          return_string += getTimeEstimate( (double) bottom_current_amount_completed,
               (double) bottom_length_of_task,
               -1.0,
               bottom_time_started );
          
          // Return the message
          return return_string;
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Calculate the time passed since the start of task and estimates the
      * remaining time.
      */
     private String getTimeEstimate(double current_amount_completed,
          double length_of_task,
          double fraction_sub_task_completed,
          long time_started)
     {
          // Calculate the estimated time remaining before task is completed
          double fraction_completed = current_amount_completed / length_of_task;
          if (fraction_sub_task_completed > 0.001)
               fraction_completed += fraction_sub_task_completed / length_of_task;
          double time_already_passed_sec = (double) ((System.currentTimeMillis() - time_started) / 1000);
          double time_total_sec = time_already_passed_sec / fraction_completed;
          double time_remaining_sec = time_total_sec - time_already_passed_sec;
          int time_already_passed = (int) time_already_passed_sec;
          int time_remaining = (int) time_remaining_sec;
          if (time_remaining < 0)
               time_remaining = 0;
          
          // Devise the string to return
          String time_passed_message = findTimeWithProperUnits(time_already_passed);
          String time_remaining_message = findTimeWithProperUnits(time_remaining);
          if (current_amount_completed < 0.99)
               return new String("Time elapsed: " + time_passed_message);
          else
               return new String("Time elapsed: " + time_passed_message +
                    "        Estimated time remaining: " + time_remaining_message);
     }
     
     
     /* PROTECTED METHODS *****************************************************/
     
     
     /**
      * Takes in a number of seconds and returns the appropriatly formatted
      * amount of time corresponding to this based on how many seconds there
      * are. Some rounding takes place.
      *
      * @param seconds   The number of seconds to base the formatted string on.
      * @return          The formatted number of seconds.
      */
     protected String findTimeWithProperUnits(int seconds)
     {
          if (seconds > 3601)
          {
               int hours = seconds / 3600;
               int minutes = (seconds - (hours * 3600)) / 60;
               return new String(hours + " hours and " + minutes + " minutes");
          }
          else if (seconds > 120)
          {
               int minutes = seconds / 60;
               return new String(minutes + " minutes");
          }
          else
               return new String(seconds + " seconds");
     }
}
