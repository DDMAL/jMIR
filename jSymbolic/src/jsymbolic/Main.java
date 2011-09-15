/*
 * Main.java
 * Version 1.2.1
 *
 * Last modified on July 26, 2010.
 * McGill University and the University of Waikato
 */

package jsymbolic;


/**
 * Runs the jSymbolic Feature Extractor GUI, if no command line arguments are
 * specified, or the command line if there are command line arguments.
 *
 * @author Cory McKay
 */
public class Main
{
	/**
	 * Runs the GUI.
	 */
	public static void main(String[] args)
	{
		if (args.length == 0)
			new jsymbolic.gui.OuterFrame();
		else
			new CommandLine(args);
	}
}