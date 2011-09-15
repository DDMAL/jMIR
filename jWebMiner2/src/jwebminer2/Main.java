/*
 * Main.java
 * Version 2.0.1
 *
 * Last modified on July 26, 2010.
 * McGill University
 */

package jwebminer2;

import mckay.utilities.general.*;


/**
 * Runs the jWebMiner software. This software is designed to extract cultural
 * features from the web. A special emphasis is placed on applications related
 * to music information retrieval, although the software is also suitable for
 * general data mining use. Sets up proxy server settings if needed.
 *
 * <p>Only the GUI version of this software is currently available. A command
 * line version is planned for the future.
 *
 * @author Cory McKay (1.x) and Gabriel Vigliensoni (2.x)
 */
public class Main
{
     /**
      * Instantiate the GUI. Command line arguments are ignored. Also sets up
	  * proxy server settings if needed.
      *
      * @param args The command line arguments
      */
     public static void main(String[] args)
     {
          try
          {
               // Verify if proxy settings are needed, and set them up if they are
               new mckay.utilities.webservices.ProxyServerAccessor(null, null);

               // Set up the GUI
               jwebminer2.gui.OuterFrame gui = new jwebminer2.gui.OuterFrame();
          }
          catch (Exception e)
          {
               System.out.println("ERROR: " + e.getMessage());
               e.printStackTrace();
          }
     }
}