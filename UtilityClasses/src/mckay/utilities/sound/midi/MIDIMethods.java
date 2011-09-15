/*
 * MIDIMethods.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.sound.midi;

import javax.sound.midi.*;
import java.io.*;
import java.util.LinkedList;


/**
 * A holder class for static methods relating to MIDI.
 *
 * @author Cory McKay
 */
public class MIDIMethods
{
     /**
      * Plays the given MIDI Sequence and returns the Sequencer that is playing
      * it. The default system Sequencer and Synthesizer are used.
      *
      * @param	midi_sequence The MIDI sequence to play
      * @return               A sequencer that is playing the midi_sequence
      * @throws	Exception     Throws an exception if an empty MIDI sequence
      *                       is passed as an argument or if cannoth play.
      */
     public static Sequencer playMIDISequence(Sequence midi_sequence)
     throws Exception
     {
          // Throw exception if empty midi_sequence passed
          if (midi_sequence == null)
               throw new Exception("No MIDI data passed for playback.");
          
          // Acquire a MIDI Sequencer from the system
          Sequencer sequencer = MidiSystem.getSequencer();
          if (sequencer == null)
               throw new Exception("Could not acquire a MIDI sequencer from the system.");
          
          // Prepare a holder for a MIDI Synthesizer
          Synthesizer synthesizer = null;
          
          // Open the sequencer
          sequencer.open();
          
          // Feed the sequencer the sequence it is to play
          sequencer.setSequence(midi_sequence);
          
          // Set the desinations that the Sequence should be played on.
          // Some Java Sound implemntations combine the default
          // sequencer and the default synthesizer into one. This
          // checks if this is the case, and forms the needed
          // connections if it is not the case.
          if ( !(sequencer instanceof Synthesizer))
          {
               synthesizer = MidiSystem.getSynthesizer();
               synthesizer.open();
               Receiver synth_receiver = synthesizer.getReceiver();
               Transmitter	seq_transmitter = sequencer.getTransmitter();
               seq_transmitter.setReceiver(synth_receiver);
          }
          
          // Begin playback
          sequencer.start();
          
          // Return the sequencer that is performing playback
          return sequencer;
     }
     
     
     /**
      * Returns information regarding a given MIDI file. This information
      * consists of the file name, the number of tracks in the file, its
      * duration in seconds, the total number of MIDI ticks, its MIDI timing
      * division type, its MIDI resolution type and the tick duration. Also
      * returned is the MIDI file type and any stored meta-data.
      *
      * @param	file          The file to return data about.
      * @return               Data in string form about the specified MIDI file.
      * @throws	Exception     Throws informative exceptions if the file is
      *                       invalid.
      */
     public static String getMIDIFileFormatData(File file)
     throws Exception
     {
          try
          {
               // Load the sequence
               Sequence sequence = MidiSystem.getSequence(file);
               
               // Get the MIDI file type
               MidiFileFormat file_format = MidiSystem.getMidiFileFormat(file);
               
               // Find the timing division type used in the MIDI file
               float division_code = sequence.getDivisionType();
               String	division_type = null;
               if (division_code == Sequence.PPQ)
                    division_type = "PPQ";
               else if (division_code == Sequence.SMPTE_24)
                    division_type = "SMPTE, 24 frames per second";
               else if (division_code == Sequence.SMPTE_25)
                    division_type = "SMPTE, 25 frames per second";
               else if (division_code == Sequence.SMPTE_30DROP)
                    division_type = "SMPTE, 29.97 frames per second";
               else if (division_code == Sequence.SMPTE_30)
                    division_type = "SMPTE, 30 frames per second";
               String	timing_resolution_type = null;
               if (sequence.getDivisionType() == Sequence.PPQ)
                    timing_resolution_type = " ticks per beat";
               else
                    timing_resolution_type = " ticks per frame";
               
               // Format and return the information
               String data = new String();
               data += new String("FILE NAME: " + file.getName() + "\n");
               data += new String("MIDI FILE TYPE: " + file_format.getType() + "\n");
               data += new String("NUMBER OF TRACKS: " + sequence.getTracks().length + "\n");
               data += new String("DURATION: " + (sequence.getMicrosecondLength() / 1000000.0) + " seconds\n");
               data += new String("NUMBER OF TICKS: " + sequence.getTickLength() + " ticks\n");
               data += new String("TIMING DIVISION TYPE: " + division_type + "\n");
               data += new String("TIMING RESOLUTION: " + sequence.getResolution() + timing_resolution_type + "\n");
               data += new String("TICK DURATION: " + (double) sequence.getMicrosecondLength() / 1000000.0 / (double) sequence.getTickLength() + " seconds\n");
               data += new String("TITLE: " + ((String) file_format.getProperty("title")) + "\n");
               data += new String("AUTHOR: " + ((String) file_format.getProperty("author")) + "\n");
               data += new String("COPYRIGHT: " + ((String) file_format.getProperty("copyright")) + "\n");
               data += new String("COMMENT: " + ((String) file_format.getProperty("comment")) + "\n");
               return data;
          }
          catch (IOException ex)
          {
               throw new Exception("File " + file.getName() + " is not a readable MIDI file.");
          }
     }
     
     
     /**
      * Returns an array with an entry for each MIDI tick in the given MIDI
      * sequence. The value at each indice gives the duration of a tick in
      * seconds at that particular point in the recording. Tempo change
      * messages ARE taken into account.
      *
      * @param	sequence The MIDI Sequence from which to extract the tick
      *                  durations.
      * @return          An array with an entry for each MIDI tick in the given
      *                  MIDI sequence.
      */
     public static double[] getSecondsPerTick(Sequence sequence)
     {
          // Find the number of PPQ ticks per beat
          int ticks_per_beat = sequence.getResolution();
          
          // Caclulate the average number of MIDI ticks corresponding to 1 second of score time
          double mean_ticks_per_sec = ((double) sequence.getTickLength()) / ((double) sequence.getMicrosecondLength() / 1000000.0);
          
          // Instantiate seconds_per_tick array and initialize entries to the average
          // number of ticks per second
          double[] seconds_per_tick = new double[ (int) sequence.getTickLength() + 1];
          for (int i = 0; i < seconds_per_tick.length; i++)
               seconds_per_tick[i] = 1.0 / mean_ticks_per_sec;
          
          // Get the MIDI tracks from the Sequence
          Track[] tracks = sequence.getTracks();
          
          // Fill in seconds_per_tick to reflect dynamic tempo changes
          for (int n_track = 0; n_track < tracks.length; n_track++)
          {
               // Go through all the events in the current track, searching for tempo
               // change messages
               Track track = tracks[n_track];
               for (int n_event = 0; n_event < track.size(); n_event++)
               {
                    // Get the MIDI message corresponding to the next MIDI event
                    MidiEvent event = track.get(n_event);
                    MidiMessage message = event.getMessage();
                    
                    // If message is a MetaMessage (which tempo change messages are)
                    if (message instanceof MetaMessage)
                    {
                         MetaMessage meta_message = (MetaMessage) message;
                         if (meta_message.getType() == 0x51) // tempo change message
                         {
                              // Find the number of microseconds per beat
                              byte[]	meta_data = meta_message.getData();
                              int	microseconds_per_beat = ((meta_data[0] & 0xFF) << 16)
                              | ((meta_data[1] & 0xFF) << 8)
                              | (meta_data[2] & 0xFF);
                              
                              // Find the number of seconds per tick
                              double current_seconds_per_tick = ((double) microseconds_per_beat) / ((double) ticks_per_beat);
                              current_seconds_per_tick = current_seconds_per_tick / 1000000.0;
                              
                              // Make all subsequent tempos be at the current_seconds_per_tick rate
                              for (int i = (int) event.getTick(); i < seconds_per_tick.length; i++)
                                   seconds_per_tick[i] = current_seconds_per_tick;
                         }
                    }
               }
          }
          
          // Return the results
          return seconds_per_tick;
     }
     
     
     /**
      * <B>IMPORTANT: THIS METHOD IS NOT YET FINISHED. IT CURRENTLY ONLY OUTPUTS EMPTY
      * WINDOWS OF MIDI DATA.</b>
      *
      * Breaks the given MIDI Sequence into windows of equal duration. These
      * windows may or may not be overlapping. The original Sequence is not
      * changed. Tempo change messages ARE taken into account, so different
      * windows will have the same time duration, but not necessarily the same
      * number of MIDI ticks.
      *
      * @param	original_sequence	The MIDI Sequence to break into windows.
      * @param	window_duration		The duration in seconds of each window.
      * @param	window_overlap_offset   The number of seconds that windows are
      *                                 offset by. A value of zero means that
      *                                 there is no window overlap.
      * @return				An array of sequences representing the
      *					windows of the original sequence in
      *					consecutive order.
      * @throws	Exception		Throws an informative exception if the
      *					MIDI file uses SMTPE timing instead of
      *					PPQ timing or if it is too large.
      */
     public static Sequence[] breakSequenceIntoWindows( Sequence original_sequence,
          double window_duration,
          double window_overlap_offset )
          throws Exception
     {
          if (original_sequence.getDivisionType() != Sequence.PPQ)
               throw new Exception("The specified MIDI sequence uses SMPTE time encoding." +
                    "\nOnly PPQ time encoding is accepted here.");
          if ( ((double) original_sequence.getTickLength()) > ((double) Integer.MAX_VALUE) - 1.0)
               throw new Exception("The MIDI sequence could not be processed because it is too long.");
          
          // Get an array with a value at each indice that gives the duration of a tick in
          // seconds at that particular point in the recording.
          double[] seconds_per_tick = getSecondsPerTick(original_sequence);
          
          // Calculate the window start and end tick indices
          LinkedList<Integer> window_start_ticks_list = new LinkedList<Integer>();
          LinkedList<Integer> window_end_ticks_list = new LinkedList<Integer>();
          double total_duration = original_sequence.getMicrosecondLength() / 1000000.0;
          double time_interval_to_next_tick = window_duration - window_overlap_offset;
          boolean found_next_tick = false;
          int tick_of_next_beginning = 0;
          int this_tick = 0;
          double total_seconds_accumulated_so_far = 0.0;
          while (total_seconds_accumulated_so_far < total_duration && this_tick < seconds_per_tick.length)
          {
               window_start_ticks_list.add(new Integer(this_tick));
               double seconds_accumulated_so_far = 0.0;
               while (seconds_accumulated_so_far < window_duration && this_tick < seconds_per_tick.length)
               {
                    seconds_accumulated_so_far += seconds_per_tick[this_tick];
                    this_tick++;
                    if (!found_next_tick)
                         if (seconds_accumulated_so_far > time_interval_to_next_tick)
                         {
                         tick_of_next_beginning = this_tick;
                         found_next_tick = true;
                         }
               }
               window_end_ticks_list.add(new Integer(this_tick - 1));
               if (found_next_tick)
                    this_tick = tick_of_next_beginning;
               found_next_tick = false;
               total_seconds_accumulated_so_far += seconds_accumulated_so_far - window_overlap_offset;
          }
          
          // Store the window start and end tick indices
          Integer[] window_start_ticks_I = window_start_ticks_list.toArray(new Integer[1]);
          int[] window_start_ticks = new int[window_start_ticks_I.length];
          for (int i = 0; i < window_start_ticks.length; i++)
               window_start_ticks[i] = window_start_ticks_I[i].intValue();
          Integer[] window_end_ticks_I = window_end_ticks_list.toArray(new Integer[1]);
          int[] window_end_ticks = new int[window_end_ticks_I.length];
          for (int i = 0; i < window_end_ticks.length; i++)
               window_end_ticks[i] = window_end_ticks_I[i].intValue();
          
          // Prepare the sequences representing each window of MIDI data and the tracks in
          // each sequence
          Sequence[] windowed_sequences = new Sequence[window_start_ticks.length];
          Track[][] windowed_tracks = new Track[window_start_ticks.length][];
          for (int win = 0; win < windowed_sequences.length; win++)
          {
               windowed_sequences[win] = new Sequence( original_sequence.getDivisionType(),
                    original_sequence.getResolution(),
                    original_sequence.getTracks().length );
               windowed_tracks[win] = windowed_sequences[win].getTracks();
          }
          
          // Prepare the original tracks of MIDI data
          Track[] original_tracks = original_sequence.getTracks();
          
// FILL IN THE WINDOWS HERE. REMEMBER THAT EACH WINDOW MUST CONTAIN COMPLETE META-DATA
// AS WELL AS THE LAST RELEVANT PROGRAM CHANGE, PITCH BEND, ETC. MESSAGES.
          
// Starts and ends are in window_start_ticks and window_end_ticks
// Should start on tick 1 rather than tick 0?
          
          // Return the windows of MIDI data
          return windowed_sequences;
     }
}
