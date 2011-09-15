/*
 * NetworkMethods.java
 * Version 3.0.1
 *
 * Last modified on July 23, 2010.
 * McGill University and University of Waikato
 */

package mckay.utilities.staticlibraries;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Vector;

/**
 * A holder class for static methods for performing miscellaneous tasks.
 *
 * @author Cory McKay
 */
public class MiscellaneousMethods
{
	/**
	 * Runs the specified command as a subprocess in the environment of the
	 * specified runtime. Collects any output that it generatesm.
	 * 
	 * @param	command					The command to run.
	 * @param	run_time				The runtime to run the command in.
	 * @param	error_stream_reader		An array of size 1. The value of element
	 *									0 of this array will be changed to a new
	 *									InputStreamReader connected to the
	 *									error stream output of the subprocess
	 *									If null or an array of size != 1 is
	 *									passed in then this	parameter will be
	 *									ignored and the error stream will
	 *									therefore not be stored.
	 * @param	exit_code				An array of size 1. The value of element
	 *									0 of this array will be changed to
	 *									reflect the exit value of the process.
	 *									By convention, a value of 0 indicates
	 *									normal termination. If null or an array
	 *									of size != 1 is passed in then this
	 *									parameter will be ignored and the exit
	 *									code will therefore not be stored.
	 * @return							The standard output of the subprocess.
	 *									Each line of output is stored in a
	 *									separate extra element of the array.
	 *									Null is returned if there is no output.
	 * @throws	Exception				An exception is thrown if a problem
	 *									occurss
	 */
	public static String[] runCommand( String command,
			Runtime run_time,
			InputStreamReader[] error_stream_reader,
			int[] exit_code )
			throws Exception
	{
		// Execute the command
		Process process = run_time.exec(command);

		// Access the error stream if appropriate
		if (error_stream_reader != null)
			if (error_stream_reader.length == 1)
				error_stream_reader[0] = new InputStreamReader(process.getErrorStream());

		// Access the standard out
		Vector<String> output = new Vector<String>();
		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null;
		while ((line = input.readLine()) != null)
			output.add(line);

		// Store the exit code, if apporpriate
		int exit_value = process.waitFor();
		if (exit_code != null)
			if (exit_code.length == 1)
				exit_code[0] = exit_value;

		// Return the output
		if (output.isEmpty()) return null;
		else return output.toArray(new String[output.size()]);
	}


	/**
	 * Validates and parses the given command line arguments. This method
	 * assumes that all command line arguments consist of flag/value pairs, and
	 * that all flags start with the "-" character. If invalid command line
	 * arguments are provided, then an explanation of the errors are printed to
	 * print_stream. An explanation of the permitted inputs are also printed to
	 * print_stream if explanations is non-null. Execution is terminated if
	 * invalid command line parameters are found.
	 *
	 * <p>If only a lone command line argument of "-help" is specified, then
	 * the valid command line arguments are printed to standard out and
	 * execution is terminated.
	 *
	 * @param args				The command line arguments to parse.
	 * @param permitted_flags	The keys of this HashMap represent the
	 *							permissible flags. Each flag maps to a Boolean,
	 *							which is true if the flag is mandatory, and
	 *							false if it is required. Note that these flags
	 *							must start with the "-" character. This may
	 *							not be null.
	 * @param explanation_keys	Each of the flags. Note that these flags
	 *							must start with the "-" character. Flags must
	 *							occur in the same order as in the explanations
	 *							parameter, and must match those in the
	 *							permitted_flags parameter (although the order
	 *							may vary from the latter. This may be null, in
	 *							which case explanations will not be printed out.
	 * @param explanations		Explanations of each of the flags. This will be
	 *							printed to print_stream if invalid input is
	 *							provided. This may be null, in which case this
	 *							output will not be provided.
	 * @param  print_stream		Where to print error messages indicating and
	 *							explaining invalid inputs. Typically standard
	 *							error or standard out.
	 * @return					A mapping between the flags that were provided
	 *							and the values for each of them. Null if invalid
	 *							input was provided.
	 */
	public static HashMap<String, String> parseCommandLineParameters( String[] args,
			HashMap<String, Boolean> permitted_flags,
			String[] explanation_keys,
			String[] explanations,
			PrintStream print_stream )
	{
		try
		{
			// Print out valid command line arguments if only -help is specified
			if (args.length == 1)
			{
				if (args[0].equals("-help"))
				{
					print_stream = System.out;
					throw new Exception("");
				}
			}

			// Verfify that there are an even number of command line arguments
			if ((args.length % 2) != 0)
				throw new Exception("An odd number of command line parameters were provided. Only flag/value are pairs accepted.");

			// Validate the arguments and parse them into parsed_args
			HashMap<String, String> parsed_args = new HashMap<String, String>();
			String current_flag = null;
			for (int i = 0; i < args.length; i++)
			{
				// Deal with flags
				if (i % 2 == 0)
				{
					// Verify that this is a valid flag
					if (args[i].length() < 2)
						throw new Exception ("There must be at least one flag and one value in the command line arguments.");
					if (!args[i].startsWith("-"))
						throw new Exception ("The \"" + args[i] + "\" flag does not start with a \"-\".");
					if (!permitted_flags.containsKey(args[i])) 
						throw new Exception ("\"" + args[i] + "\" is not a recognized flag.");
					if (parsed_args.containsKey(args[i]))
						throw new Exception("The flag \"" + args[i] + "\" appears more than once.");

					// Note the flag
					current_flag = args[i];
				}

				// Deal with values
				else
				{
					parsed_args.put(current_flag, args[i]);
				}
			}

			// Verify that all of the required flags are present
			String[] flags_allowed = permitted_flags.keySet().toArray(new String[1]);
			for (int i = 0; i < flags_allowed.length; i++)
				if (permitted_flags.get(flags_allowed[i]).booleanValue() && !parsed_args.containsKey(flags_allowed[i]))
					throw new Exception ("The mandatory flag " + flags_allowed[i] + " is missing.");

			// Return the parsed command line arguments
			return parsed_args;
		}
		catch (Exception e)
		{
			// e.printStackTrace();

			if (print_stream != null)
			{
				// Print the error message
				print_stream.println(e.getMessage());

				if (explanations != null && explanation_keys != null)
				{
					// Find the number of characthers for the first row
					int first_row_width = 0;
					for (int i = 0; i < explanation_keys.length; i++)
						if (explanation_keys[i].length() > first_row_width)
							first_row_width = explanation_keys[i].length();
					first_row_width += 3;

					// Print the valid output
					print_stream.println("\nValid flags are:\n");
					for (int i = 0; i < explanation_keys.length; i++)
					{
						print_stream.print(explanation_keys[i]);

						int number_spaces = first_row_width - explanation_keys[i].length();
						for (int j = 0; j < number_spaces; j++)
							print_stream.print(" ");

						boolean required = permitted_flags.get(explanation_keys[i]).booleanValue();
						if (required) print_stream.print("Required");
						else print_stream.print("Optional");

						print_stream.print("   " + explanations[i] + "\n");
					}

					print_stream.print("\nThese flags must each be followed by their associated value.\n\n");
				}
			}

			// Terminate execution
			System.exit(0);
			return null;
		}
	}
}