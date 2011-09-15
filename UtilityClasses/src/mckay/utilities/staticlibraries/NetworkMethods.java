/*
 * NetworkMethods.java
 * Version 3.0.1
 *
 * Last modified on July 7, 2010.
 * McGill University and University of Waikato
 */

package mckay.utilities.staticlibraries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A holder class for static methods relating to strings.
 *
 * @author Cory McKay
 */
public class NetworkMethods
{
	/**
	 * Makes the equivalent of an HTTP GET request in order to access content
	 * from a server. Based on code by Aviran Mordo originally posted at
	 * www.aviransplace.com/2008/01/08/make-http-post-or-get-request-from-java/
	 *
	 * @param url_to_access			The URL of the server to access.
	 *								e.g. "http://www.google.com"
	 * @param request_parameters	The request parameters. Note that this
	 *								adds the question mark to the request, so it
	 *								should not be present here. No parameters
	 *								are used if this string is null or empty.
	 *								e.g. "param1=val1&param2=val2"
	 * @return						The response from the server. Null is
	 *								returned if the specified url_to_access
	 *								does not begin with "http://".
	 * @throws	Exception			Throws an exception if the get request was
	 *								unsuccesful.
	 */
	public static String sendGetRequest(String url_to_access,
			String request_parameters)
			throws Exception
	{
		String result = null;
		if (url_to_access.startsWith("http://"))
		{
			// Send the request
			String url_with_parameters = url_to_access;
			if (request_parameters != null && request_parameters.length() > 0)
				url_with_parameters += "?" + request_parameters;
			URLConnection connection = (new URL(url_with_parameters)).openConnection();

			// Read the response
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuffer buffer = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null)
				buffer.append(line);
			reader.close();
			result = buffer.toString();
		}
		return result;
	}


	/**
	 * Makes the equivalent of an HTTP POST request in order to post content
	 * to a server. Based on code by Aviran Mordo originally posted at
	 * www.aviransplace.com/2008/01/08/make-http-post-or-get-request-from-java/
	 *
	 * @param	url_to_access	The URL of the server to access.
	 *							e.g. "http://www.google.com"
	 * @param	to_send			The data to send to post to the server.
	 * @param	response_writer	Where to write the server's response to.
	 * @throws	Exception		Throws an exception if the post request was
	 *							unsuccesful.
	 */
	public static void postData(URL url_to_access,
			Reader to_send,
			Writer response_writer)
			throws Exception
	{
		HttpURLConnection url_connection = null;
		try
		{
			// Set up the connection
			url_connection = (HttpURLConnection) url_to_access.openConnection();
			try
			{
				url_connection.setRequestMethod("POST");
			}
			catch (ProtocolException e)
			{
				throw new Exception("HttpURLConnection does not support POST", e);
			}
			url_connection.setDoOutput(true);
			url_connection.setDoInput(true);
			url_connection.setUseCaches(false);
			url_connection.setAllowUserInteraction(false);
			url_connection.setRequestProperty("Content-type", "text/xml; charset=" + "UTF-8");

			// Post the data
			OutputStream out = url_connection.getOutputStream();
			try
			{
				Writer writer = new OutputStreamWriter(out, "UTF-8");
				pipe(to_send, writer);
				writer.close();
			}
			catch (IOException e)
			{
				throw new Exception("IOException while posting data", e);
			}
			finally
			{
				if (out != null) out.close();
			}

			// Read the response
			InputStream in = url_connection.getInputStream();
			try
			{
				Reader reader = new InputStreamReader(in);
				pipe(reader, response_writer);
				reader.close();
			}
			catch (IOException e)
			{
				throw new Exception("IOException while reading response", e);
			}
			finally
			{
				if (in != null)	in.close();
			}

		}
		catch (IOException e)
		{
			throw new Exception("Connection error while attempting to post to " + url_to_access + ": " + e);
		}
		finally
		{
			if (url_connection != null)	url_connection.disconnect();
		}
	}


	/**
	 * Pipes data from the specified reader to the specified writer via a
	 * buffer.
	 *
	 * @param reader	The reader to pipe data from.
	 * @param writer	The writer to pipe data to.
	 * @throws java.io.IOException
	 */
	public static void pipe(Reader reader, Writer writer)
			throws IOException
	{
		char[] buffer = new char[1024];
		int read = 0;
		while ((read = reader.read(buffer)) >= 0)
			writer.write(buffer, 0, read);
		writer.flush();
	}
}
