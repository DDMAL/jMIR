/*
 * AudioMethodsDSP.java
 * Version 3.0
 *
 * Last modified on April 11, 2010.
 * McGill University
 */

package mckay.utilities.sound.sampled;

/**
 * A holder class for general static methods relating to processing signals
 * in the form of samples stored as arrays of doubles.
 *
 * @author	Cory McKay
 */
public class AudioMethodsDSP
{
     /**
      * Applies a gain to the given samples. Note that a negative gain
      * effectively applies a phase shift of pi. There is no guarantee that the
      * returned samples will be between -1 and +1. The returned samples are
      * copies, so changes to them will not affect the original samples.
      *
      * @param	samples  Audio samles to modify, usually with a minimum value
      *                  of -1 and a maximum value of +1. The first indice
      *                  corresponds to the channel and the second indice
      *                  corresponds to the sample number.
      * @param	gain     The gain to apply.
      * @return          The given audio samples after the application of the
      *                  given gain.
      */
     public static double[][] applyGain(double[][] samples, double gain)
     {
          double[][] altered_samples = new double[samples.length][];
          for (int i = 0; i < altered_samples.length; i++)
          {
               altered_samples[i] = new double[samples[i].length];
               for (int j = 0; j < altered_samples[i].length; j++)
                    altered_samples[i][j] = samples[i][j] * gain;
          }
          return altered_samples;
     }
     
     
     /**
      * Apply an overall gain and panning to the provided set of samples. This
      * set of samples will be modified by this method. All samples in the
      * provided set of samples should be between -1 and +1 before processing,
      * and the same should be true after processing.
      *
      * <p>It should be noted that gain and panning do not amplify samples, but
      * rather attenuate them. The provided samples should already take
      * advantage of the full available dynamic range (-1 to +1), and a gain of
      * 1 and and a panning of 0 will maintain this. Other values of gain or
      * panning will cause attenuation.
      *
      * @param	samples_to_modify	A 2-D array of doubles whose first
      *                                 indice indicates channel and whose
      *                                 second indice indicates sample value. In
      *                                 stereo, indice 0 corresponds to left and
      *                                 1 to right. All samples should fall
      *                                 between -1 and +1.
      * @param	gain			The overall gain to apply to the
      *                                 samples. This value must be between 0
      *                                 and 1, with 0 being silence and 1 being
      *                                 maximum amplitude.
      * @param	panning			The relative strength of the two stereo
      *                                 channels. This parameter is ignored in
      *                                 non-stereo cases. Value must be between
      *                                 -1 and +1, with -1 corresponding to full
      *                                 amplitude on the left channel and
      *                                 silence on the right, and +1
      *                                 corresponding to the reverse. A value of
      *                                 0 indicates a balance, and no
      *                                 attenuation is applied to either
      *                                 channel.
      * @throws	Exception		Throws an exception if an invalid
      *                                 <i>gain</i> or <i>panning</i> value is
      *                                 specified, of if the
      *                                 <i>samples_to_modify</i> parameter is
      *                                 null or contains empty channels.
      */
     public static void applyGainAndPanning( double[][] samples_to_modify,
          double gain,
          double panning )
          throws Exception
     {
          // Throw exceptions if invalid parameters provided
          if (gain < 0.0 || gain > 1.0)
               throw new Exception( "Gain of " + gain + " specified.\n" +
                    "This value must be between 0.0 and 1.0." );
          if (panning < -1.0 || panning > 1.0)
               throw new Exception( "Panning of " + panning + " specified.\n" +
                    "This value must be between -1.0 and 1.0." );
          if (samples_to_modify == null)
               throw new Exception( "Empty set of samples provided." );
          for (int chan = 0; chan < samples_to_modify.length; chan++)
               if (samples_to_modify[chan] == null)
                    throw new Exception("Channel " + chan + " is empty.");
          
          // Apply gain to all samples equally accross all channels
          for (int chan = 0; chan < samples_to_modify.length; chan++)
               for (int samp = 0; samp < samples_to_modify[chan].length; samp++)
                    samples_to_modify[chan][samp] *= gain;
          
          // Apply panning in the stereo case
          if (samples_to_modify.length == 2 && panning != 0.0)
          {
               // Adjust left channel if panning is to the right
               if (panning > 0.0)
               {
                    double left_multiplier = 1.0 - panning;
                    for (int samp = 0; samp < samples_to_modify[0].length; samp++)
                         samples_to_modify[0][samp] *= left_multiplier;
               }
               
               // Adjust right channel if panning is to the left
               if (panning < 0.0)
               {
                    double right_multiplier = panning + 1.0;
                    for (int samp = 0; samp < samples_to_modify[1].length; samp++)
                         samples_to_modify[1][samp] *= right_multiplier;
               }
          }
     }
     
          
     /**
      * Returns the given set of samples as a set of samples mixed down into one
      * channel.
      *
      * @param	audio_samples Audio samles to modify, with a minimum value of -1
      *                       and a maximum value of +1. The first indice
      *                       corresponds to the channel and the second indice
      *                       corresponds to the sample number.
      * @return               The given audio samples mixed down, with equal
      *                       gain, into one channel.
      */
     public static double[] getSamplesMixedDownIntoOneChannel(double[][] audio_samples)
     {
          if (audio_samples.length == 1)
               return audio_samples[0];
          
          double number_channels = (double) audio_samples.length;
          int number_samples = audio_samples[0].length;
          
          double[] samples_mixed_down = new double[number_samples];
          for (int samp = 0; samp < number_samples; samp++)
          {
               double total_so_far = 0.0;
               for (int chan = 0; chan < number_channels; chan++)
                    total_so_far += audio_samples[chan][samp];
               samples_mixed_down[samp] = total_so_far / number_channels;
          }
          
          return samples_mixed_down;
     }
     
     
     /**
      * Clips the given samples so that all values below -1 are set to -1 and 
      * all values above 1 are set to 1. The returned array is a copy so the 
      * original array is not altered.
      *
      * @param      original_samples    A 2-D array of doubles whose first
      *                                 indice indicates channel and whose
      *                                 second indice indicates sample value. In
      *                                 stereo, indice 0 corresponds to left and
      *                                 1 to right.
      * @return                         A clipped copy of the original_samples
      *					parameter.
      * @throws     Exception		If a null parameter is passed.
      */
     public static double[][] clipSamples(double[][] original_samples)
     throws Exception
     {
          // Throw exceptions for invalid parameters
          if (original_samples == null)
               throw new Exception( "Empty set of samples to provided." );
          
          // Perform clipping
          double[][] clipped_samples = new double[original_samples.length][];
          for (int chan = 0; chan < clipped_samples.length; chan++)
          {
               clipped_samples[chan] = new double[original_samples[chan].length];
               for (int samp = 0; samp < clipped_samples[chan].length; samp++)
               {
                    if (original_samples[chan][samp] < -1.0)
                         clipped_samples[chan][samp] = -1.0;
                    else if (original_samples[chan][samp] > 1.0)
                         clipped_samples[chan][samp] = 1.0;
                    else
                         clipped_samples[chan][samp] = original_samples[chan][samp];
               }
          }
          return clipped_samples;
     }
     
     
     /**
      * Normalizes the given samples so that the absolute value of the highest
      * sample amplitude is 1. Does nothing if also samples are 0.
      *
      * @param	samples_to_normalize    The samples to normalize.
      * @return				Returns a copy of the given samples
      *					after normalization.
      */
     public static double[] normalizeSamples(double[] samples_to_normalize)
     {
          double[] normalized_samples = new double[samples_to_normalize.length];
          for (int samp = 0; samp < normalized_samples.length; samp++)
               normalized_samples[samp] = samples_to_normalize[samp];
          
          double max_sample_value = 0.0;
          for (int samp = 0; samp < normalized_samples.length; samp++)
               if (Math.abs(normalized_samples[samp]) > max_sample_value)
                    max_sample_value = Math.abs(normalized_samples[samp]);
          if (max_sample_value != 0.0)
               for (int samp = 0; samp < normalized_samples.length; samp++)
                    normalized_samples[samp] /= max_sample_value;
          
          return normalized_samples;
     }
     
     
     /**
      * Normalizes the given samples dependantly so that the absolute
      * value of the highest sample amplitude is 1. Does nothing if all
      * samples are 0.
      *
      * @param	samples_to_normalize    The samples to normalize. The first
      *                                 indice denotes channel and the second
      *                                 denotes sample number.
      * @return                         Returns a copy of the given samples
      *					after normalization.
      */
     public static double[][] normalizeSamples(double[][] samples_to_normalize)
     {
          double[][] normalized_samples = new double[samples_to_normalize.length][samples_to_normalize[0].length];
          for (int chan = 0; chan < normalized_samples.length; chan++)
               for (int samp = 0; samp < normalized_samples[chan].length; samp++)
                    normalized_samples[chan][samp] = samples_to_normalize[chan][samp];
          
          double max_sample_value = 0.0;
          for (int chan = 0; chan < normalized_samples.length; chan++)
               for (int samp = 0; samp < normalized_samples[chan].length; samp++)
                    if (Math.abs(normalized_samples[chan][samp]) > max_sample_value)
                         max_sample_value = Math.abs(normalized_samples[chan][samp]);
          if (max_sample_value != 0.0)
               for (int chan = 0; chan < normalized_samples.length; chan++)
                    for (int samp = 0; samp < normalized_samples[chan].length; samp++)
                         normalized_samples[chan][samp] /= max_sample_value;
          
          return normalized_samples;
     }
     
     
     /**
      * Returns a copy of the given array of samples.
      *
      * @param	original_samples   Audio samles to modify, usually with a
      *                            minimum value of value of- 1 and a maximum
      *                            value of +1. The first indice corresponds to
      *                            the channel and the second indice corresponds
      *                            to the sample number.
      * @return                    A copy of the original_samples parameter.
      */
     public static double[][] getCopyOfSamples(double[][] original_samples)
     {
          double[][] new_samples = new double[original_samples.length][];
          for (int chan = 0; chan < new_samples.length; chan++)
          {
               new_samples[chan] = new double[original_samples[chan].length];
               for (int samp = 0; samp < new_samples[chan].length; samp++)
                    new_samples[chan][samp] = original_samples[chan][samp];
          }
          return new_samples;
     }
     
     
     /**
      * Returns the sample corresponding to the given time with the given
      * sampling rate.
      *
      * @param	time          The time in seconds to convert to a sample indice.
      * @param	sampling_rate The sampling rate of the audio in question.
      * @return               The corresponding sample indice.
      */
     public static int convertTimeToSample(double time, float sampling_rate)
     {
          return (int) Math.round((time * (double) sampling_rate));
     }
     
     
     /**
      * Returns the time corresponding to the given sample indice with the given
      * sampling rate.
      *
      * @param	sample        The sample indice to convert to time.
      * @param	sampling_rate The sampling rate of the audio in question.
      * @return               The corresponding time in seconds.
      */
     public static double convertSampleToTime(int sample, float sampling_rate)
     {
          return ((double) sample) / ((double) sampling_rate);
     }
     
     
     /**
      * Returns the maximum possible value that a signed sample can have under
      * the given bit depth. May be 1 or 2 values smaller than actual max,
      * depending on specifics of encoding used.
      *
      * @param	bit_depth     The bit depth to examine.
      * @return               The maximum possible positive sample value as a
      *                       double.
      */
     public static double findMaximumSampleValue(int bit_depth)
     {
          int max_sample_value_int = 1;
          for (int i = 0; i < (bit_depth - 1); i++)
               max_sample_value_int *= 2;
          max_sample_value_int--;
          double max_sample_value = ((double) max_sample_value_int) - 1.0;
          return max_sample_value;
     }     
     
     
     /**
      * Calculates the auto-correlation of the given signal. The
      * auto-correlation is only calculated between the given lags.
      *
      * <p>The getAutoCorrelationLabels method can be called to find the labels
      * in Hz for each of the returned bins.
      *
      * @param	signal   The digital signal to auto-correlate.
      * @param	min_lag  The minimum lag in samples to look for in the
      *                  auto-correlation.
      * @param	max_lag  The maximum lag in samples to look for in the
      *                  auto-correaltion.
      * @return          The auto-correlation for each lag from min_lag to
      *                  max_lag. Entry 0 corresponds to min_lag, and the last
      *                  entry corresponds to max_lag.
      */
     public static double[] getAutoCorrelation( double[] signal,
          int min_lag,
          int max_lag )
     {
          double[] autocorrelation = new double[max_lag - min_lag + 1];
          for (int lag = min_lag; lag <= max_lag; lag++)
          {
               int auto_indice = lag - min_lag;
               autocorrelation[auto_indice] = 0.0;
               for (int samp = 0; samp < signal.length - lag; samp++)
                    autocorrelation[auto_indice] += signal[samp] * signal[samp + lag];
          }
          return autocorrelation;
     }
     
     
     /**
      * Returns the bin labels for each bin of an auto-correlation calculation
      * that involved the given paremeters (most likely using the
      * getAutoCorrelation method).
      *
      * @param	sampling_rate The sampling rate that was used to encode the
      *                       signal that was auto-correlated.
      * @param	min_lag       The minimum lag in samples that was used in the
      *                       auto-correlation.
      * @param	max_lag       The maximum lag in samples that was used in the
      *                       auto-correlation.
      * @return               The labels, in Hz, for the corresponding bins
      *                       produced by the getAutoCorrelation method.
      */
     public static double[] getAutoCorrelationLabels( double sampling_rate,
          int min_lag,
          int max_lag )
     {
          double[] labels = new double[max_lag - min_lag + 1];
          for (int i = 0; i < labels.length; i++)
               labels[i] = sampling_rate / ((double) (i + min_lag));
          return labels;
     }
     
     
     /**
      * Applies linear attenuation to either end of the given samples. This is
      * done in order to eliminate clicks. The attenuation on each side is
      * determined by the <i>click_avoid_env_length</i> parameter.
      *
      * @param	sample_values		A 2-D array of doubles whose first
      *                                 indice indicates channel and whose
      *                                 second indice indicates sample value. In
      *                                 stereo, indice 0 corresponds to left and
      *                                 1 to right. All samples should fall
      *                                 between -1 and +1.
      * @param	click_avoid_env_length  The duration in seconds of the envelope
      *                                 applied at the beginning and end of the
      *                                 synthesized audio in order to avoid
      *                                 clicks.
      * @param	sample_rate		The sampling rate that was used to
      *                                 encode the <i>sample_values</i>.
      * @throws	Exception               Throws an exception if an invalid
      *                                 parameter is passed.
      */
     public static void applyClickAvoidanceAttenuationEnvelope( double[][] sample_values,
          double click_avoid_env_length,
          float sample_rate )
          throws Exception
     {
          // Throw exceptions if parameters are invalid
          if (sample_values == null)
               throw new Exception( "Empty set of samples provided." );
          if (sample_rate <= 0.0F)
               throw new Exception( "Given sample rate is " + sample_rate + " Hz.\n" +
                    "This value should be greater than zero." );
          if (click_avoid_env_length < 0.0)
               throw new Exception( "Click avoidance envelope length is " + click_avoid_env_length + " seconds.\n" +
                    "This value should be 0.0 seconds or higher." );
          double duration_of_audio = sample_values[0].length / sample_rate;
          if ( (2.0 * click_avoid_env_length) >= duration_of_audio )
               throw new Exception( "Click avoidance envelope length is " + click_avoid_env_length + " seconds.\n" +
                    "This would lead to combined envelope lengths longer than the provided audio." );
          
          // Find the duration in samples of each envelope
          int sample_duration = (int) (click_avoid_env_length * sample_rate);
          
          // Find sample to start and stop envelopes
          int start_sample_1 = 0;
          int end_sample_1 = sample_duration - 1;
          int start_sample_2 = sample_values[0].length - 1 - sample_duration;
          int end_sample_2 = sample_values[0].length - 1;
          
          // Apply first envelope
          for (int samp = start_sample_1; samp <= end_sample_1; samp++)
          {
               double amplitude_multipler = (double) samp / (double) end_sample_1;
               for (int chan = 0; chan < sample_values.length; chan++)
                    sample_values[chan][samp] *= amplitude_multipler;
          }
          
          // Apply second envelope
          for (int samp = start_sample_2; samp <= end_sample_2; samp++)
          {
               double amplitude_multipler = 1.0 - ((double) (samp - start_sample_2) / (double) (end_sample_2 - start_sample_2));
               for (int chan = 0; chan < sample_values.length; chan++)
                    sample_values[chan][samp] *= amplitude_multipler;
          }
     }  
}