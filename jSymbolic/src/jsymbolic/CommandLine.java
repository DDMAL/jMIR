/*
 * CommandLine.java
 * Version 1.2.1
 *
 * Last modified on June 24, 2010.
 * McGill University and the University of Waikato
 */

package jsymbolic;

import jsymbolic.features.MIDIFeatureExtractor;
import jsymbolic.gui.FeatureSelectorPanel;
import jsymbolic.processing.MIDIFeatureProcessor;
import mckay.utilities.staticlibraries.FileMethods;
import java.io.File;


/**
 * Allows jSymbolic's functionality to be accessed from the command line.
 *
 * @author Cory McKay
 */
public class CommandLine
{
	/**
	 * Interprets the command line arguments and begins feature extraction.
	 */
	public CommandLine(String[] args)
	{
		// If there are a proper number of command line arguments
		if (args.length == 3)
		{
			extractFeatures(args[0], args[1], args[2], true);
			System.exit(0);
		}

		// If invalid command line arguments are used
		else
		{
			System.err.println("Incorrest usage of jSymbolic. Proper usage requires one of the following:");
			System.err.println("\t1) No arguments: Runs the GUI");
			System.err.println("\t2) <SourceMIDIPath> <FeatureValuesOutputPath> <FeatureDescriptionsOutputPath>");
			System.exit(-1);
		}
	}


	/**
	 * Extracts all available features from a single MIDI file. Any errors
	 * encountered are printed to standard error.
	 * 
	 * @param input_MIDI_path					The path of the MIDI file to
	 *											extract features from.
	 * @param feature_values_save_path			The path to save the resulting
	 *											ACE XML Feature Values file to.
	 * @param feature_descriptions_save_path	The path to save the resulting
	 *											ACE XML Feature Description file
	 *											to.
	 * @param print_log							Whether or not to print a log
	 *											of actions to standard out.
	 */
	public static void extractFeatures(String input_MIDI_path,
			String feature_values_save_path,
			String feature_descriptions_save_path,
			boolean print_log)
	{
		try
		{
			// Note progress
			if (print_log) System.out.println("jSymbolic is parsing " + input_MIDI_path + "...");

			// Prepare and validate the input file
			File input_MIDI_file = new File(input_MIDI_path);
			FileMethods.validateFile(input_MIDI_file, true, false);
			try {javax.sound.midi.MidiSystem.getSequence(input_MIDI_file);}
			catch (javax.sound.midi.InvalidMidiDataException e)
			{
				throw new Exception(input_MIDI_path + " is not a valid MIDI file.");
			}

			// Get all available features
			MIDIFeatureExtractor[] feature_extractors = FeatureSelectorPanel.getAllAvailableFeatureExtractors(null);

			// Choose to extract all features
			// NOTE: could instead get defaults isntead by using non-null argument above for FeatureSelectorPanel.getAllAvailableFeatureExtractors() call
			boolean[] features_to_save = new boolean[feature_extractors.length];
			for (int i = 0; i < features_to_save.length; i++)
				features_to_save[i] = true;

			// Set the default feature extraction parameters
			boolean extract_overall_only = true;
			boolean save_features_for_each_window = false;
			boolean save_overall_recording_features = true;
			double window_size = 1.0;
			double window_overlap = 0.0;

			// Note progress
			if (print_log) System.out.println("jSymbolic is extracting features from " + input_MIDI_path + "...");

			// Prepare to extract features
			MIDIFeatureProcessor processor = new MIDIFeatureProcessor(extract_overall_only,
				window_size,
				window_overlap,
				feature_extractors,
				features_to_save,
				save_features_for_each_window,
				save_overall_recording_features,
				feature_values_save_path,
				feature_descriptions_save_path );

			// Extract features from the MIDI file and save them in an XML file
			processor.extractFeatures(input_MIDI_file);

			// Finalize saved XML files
			processor.finalize();

			// Note progress
			if (print_log) System.out.println("jSymbolic succesfully extracted features from " + input_MIDI_path + "...");
		}
		catch (Throwable t)
		{
			// Print a preparatory error message
			System.err.println("JSYMBOLIC ERROR WHILE PROCESSING " + input_MIDI_path + ":");

			// React to the Java Runtime running out of memory
			if (t.toString().startsWith("java.lang.OutOfMemoryError"))
			{
				System.err.println("- The Java Runtime ran out of memory.");
				System.err.println("- Please rerun this program with more more assigned to the runtime heap.");
			}
			else if (t instanceof Exception)
			{
				Exception e = (Exception) t;
				System.err.println("- " + e.getMessage());
				// e.printStackTrace(System.err);
			}

			// End execution
			System.exit(-1);
		}
	}
}