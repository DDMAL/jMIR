/*
 * SwingWorker.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.gui.progressbars;

import javax.swing.SwingUtilities;


/**
 * NOTE: this class is based on a class from the Sun Java Tutorial available at:
 * http://java.sun.com/docs/books/tutorial/uiswing/components/example-1dot4/SwingWorker.java
 *
 * <p>This is an abstract class that can be subclassed to perform GUI-related
 * work in a dedicated thread. For instructions on and examples of using this
 * class, see: http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 *
 * The star method must be invoked on a SwingWorker after it is instantiated.
 *
 * @author Cory McKay
 */
public abstract class SwingWorker
{
     /* FIELDS ****************************************************************/
     
     
     private Object      value;
     
     private ThreadVar   threadVar;
     
     
     /* CONSTRUCTORS **********************************************************/
     
     
     /**
      * Start a thread that will call the <code>construct</code> method
      * and then exit.
      */
     public SwingWorker()
     {
          final Runnable doFinished = new Runnable()
          {
               public void run()
               {finished();}
          };
          
          Runnable doConstruct = new Runnable()
          {
               public void run()
               {
                    try
                    {
                         setValue(construct());
                    }
                    finally
                    {
                         threadVar.clear();
                    }
                    SwingUtilities.invokeLater(doFinished);
               }
          };
          
          Thread t = new Thread(doConstruct);
          threadVar = new ThreadVar(t);
     }
     
     
     /* PUBLIC METHODS ********************************************************/
     
     
     /**
      * Compute the value to be returned by the get method.
      *
      * @return The object that will perform the actual processing.
      */
     public abstract Object construct();
     
     
     /**
      * Called on the event dispatching thread (not on the worker thread)
      * after the construct method has returned.
      */
     public void finished()
     {
     }
     
     
     /**
      * A new method that interrupts the worker thread. Call this method
      * to force the worker to stop what it's doing.
      */
     public void interrupt()
     {
          Thread t = threadVar.get();
          if (t != null)
          {
               t.interrupt();
          }
          threadVar.clear();
     }
     
     
     /**
      * Return the value created by the construct method. Returns null if either
      * the constructing thread or the current thread was interrupted before a
      * value was produced.
      *
      * @return     The value created by the construct method.
      */
     public Object get()
     {
          while (true)
          {
               Thread t = threadVar.get();
               if (t == null)
               {
                    return getValue();
               }
               try
               {
                    t.join();
               }
               catch (InterruptedException e)
               {
                    Thread.currentThread().interrupt(); // propagate
                    return null;
               }
          }
     }
     
     
     /**
      * Start the worker thread.
      */
     public void start()
     {
          Thread t = threadVar.get();
          if (t != null)
          {
               t.start();
          }
     }
     
     
     /* PRIVATE METHODS *******************************************************/
     
     
     /**
      * Set the value produced by worker thread
      */
     private synchronized void setValue(Object x)
     {
          value = x;
     }
     
     
     /* PROTECTED METHODS *****************************************************/
     
     /**
      * Get the value produced by the worker thread, or null if it
      * hasn't been constructed yet.
      *
      * @return     The value produced by the worker thread.
      */
     protected synchronized Object getValue()
     {
          return value;
     }
     
     
     /* INTERNAL CLASS ********************************************************/
     
     
     /**
      * Class to maintain reference to current worker thread under separate
      * synchronization control.
      */
     private static class ThreadVar
     {
          private Thread thread;
          ThreadVar(Thread t)
          { thread = t; }
          synchronized Thread get()
          { return thread; }
          synchronized void clear()
          { thread = null; }
     }
}