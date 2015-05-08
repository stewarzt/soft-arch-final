/*
 * ConnectionHandler.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 * 
 * Copyright (C) 2012 Chandan Raj Rupakheti
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 */

package server;

import gui.WebServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.ws.WebEndpoint;

import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.IHttpResponse;
import protocol.Protocol;
import protocol.ProtocolException;
import protocol.Servlet;

/**
 * This class is responsible for handling a incoming request by creating a
 * {@link HttpRequest} object and sending the appropriate response be creating a
 * {@link HttpResponse} object. It implements {@link Runnable} to be used in
 * multi-threaded environment.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ConnectionHandler implements Runnable {
	private Server server;
	private Socket socket;

	public ConnectionHandler(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * The entry point for connection handler. It first parses incoming request
	 * and creates a {@link HttpRequest} object, then it creates an appropriate
	 * {@link HttpResponse} object and sends the response back to the client
	 * (web browser).
	 */
	public void run() {
		// Get the start time
		long start = System.currentTimeMillis();

		InputStream inStream = null;
		OutputStream outStream = null;

		try {
			inStream = this.socket.getInputStream();
			outStream = this.socket.getOutputStream();
		} catch (Exception e) {
			// Cannot do anything if we have exception reading input or output
			// stream
			// May be have text to log this for further analysis?
			e.printStackTrace();

			// Increment number of connections by 1
			server.incrementConnections(1);
			// Get the end time
			long end = System.currentTimeMillis();
			this.server.incrementServiceTime(end - start);
			return;
		}

		// At this point we have the input and output stream of the socket
		// Now lets create a HttpRequest object
		IHttpResponse response = null;
		try {
			response = this.read(inStream);
		} catch (ProtocolException pe) {
			// We have some sort of protocol exception. Get its status code and
			// create response
			// We know only two kind of exception is possible inside
			// fromInputStream
			// Protocol.BAD_REQUEST_CODE and Protocol.NOT_SUPPORTED_CODE
			int status = pe.getStatus();
			if (status == Protocol.BAD_REQUEST_CODE) {
				response = HttpResponseFactory
						.create400BadRequest(Protocol.CLOSE);
			}
			// TODO: Handle version not supported code as well
		} catch (Exception e) {
			e.printStackTrace();
			// For any other error, we will create bad request response as well
			response = HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
		}

		if (response != null) {
			// Means there was an error, now write the response object to the
			// socket
			try {
				response.write(outStream);
				System.out.println(response);
			} catch (Exception e) {
				// We will ignore this exception
				e.printStackTrace();
			}

			// Increment number of connections by 1
			server.incrementConnections(1);
			// Get the end time
			long end = System.currentTimeMillis();
			this.server.incrementServiceTime(end - start);
			return;
		}

		// Increment number of connections by 1
		server.incrementConnections(1);
		// Get the end time
		long end = System.currentTimeMillis();
		this.server.incrementServiceTime(end - start);
	}

	public IHttpResponse read(InputStream inputStream) throws Exception {
		// We will fill this object with the data from input stream and return
		// it
		Object request;

		InputStreamReader inStreamReader = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(inStreamReader);

		// First Request Line: GET /somedir/page.html HTTP/1.1
		String line = reader.readLine(); // A line ends with either a \r, or a
											// \n, or both

		if (line == null) {
			throw new ProtocolException(Protocol.BAD_REQUEST_CODE,
					Protocol.BAD_REQUEST_TEXT);
		}

		// We will break this line using space as delimeter into three parts
		StringTokenizer tokenizer = new StringTokenizer(line, " ");

		// Error checking the first line must have exactly three elements
		if (tokenizer.countTokens() != 3) {
			throw new ProtocolException(Protocol.BAD_REQUEST_CODE,
					Protocol.BAD_REQUEST_TEXT);
		}

		String method = tokenizer.nextToken();

		// Set Request Properly
		Class reqClass = Class.forName("protocol.Http" + method.toUpperCase()
				+ "Request");
		request = reqClass.newInstance();

		reqClass.getField("method").set(request, method);
		reqClass.getField("uri").set(request, tokenizer.nextToken());
		reqClass.getField("version").set(request, tokenizer.nextToken());

		// Rest of the request is a header that maps keys to values
		// e.g. Host: www.rose-hulman.edu
		// We will convert both the strings to lower case to be able to search
		// later
		line = reader.readLine().trim();

		while (!line.equals("")) {
			// THIS IS A PATCH
			// Instead of a string tokenizer, we are using string split
			// Lets break the line into two part with first space as a separator

			// First lets trim the line to remove escape characters
			line = line.trim();

			// Now, get index of the first occurrence of space
			int index = line.indexOf(' ');

			if (index > 0 && index < line.length() - 1) {
				// Now lets break the string in two parts
				String key = line.substring(0, index); // Get first part, e.g.
														// "Host:"
				String value = line.substring(index + 1); // Get the rest, e.g.
															// "www.rose-hulman.edu"

				// Lets strip off the white spaces from key if any and change it
				// to lower case
				key = key.trim().toLowerCase();

				// Lets also remove ":" from the key
				key = key.substring(0, key.length() - 1);

				// Lets strip white spaces if any from value as well
				value = value.trim();

				// Now lets put the key=>value mapping to the header map
				((Map<String, String>) reqClass.getField("header").get(request))
						.put(key, value);
			}

			// Processed one more line, now lets read another header line and
			// loop
			line = reader.readLine().trim();
		}

		int contentLength = 0;
		try {
			contentLength = Integer.parseInt(((Map<String, String>) reqClass
					.getField("header").get(request))
					.get(Protocol.CONTENT_LENGTH.toLowerCase()));
		} catch (Exception e) {
		}

		if (contentLength > 0) {
			reqClass.getField("body").set(request, new char[contentLength]);

			reader.read((char[]) reqClass.getField("body").get(request));
		}

		String[] uriValues = ((String) reqClass.getField("uri").get(request))
				.split("/");

		if((method.equals("PUT") || method.equals("POST") || method.equals("DELETE")) && !WebServer.whitelist.contains(socket.getInetAddress()))
		{
			//Change to Not Auth
			return HttpResponseFactory.create403Forbidden(Protocol.CLOSE);
		}
		if (WebServer.config.containsKey(uriValues[1])) {
			if (WebServer.config.get(uriValues[1]).containsKey(uriValues[2])) {
				if (WebServer.config.get(uriValues[1]).get(uriValues[2])
						.contains(method.toUpperCase())) {

					ClassLoader servletLoader;
					File servletFile = new File("plugins\\" + uriValues[1]
							+ ".jar");

					servletLoader = URLClassLoader
							.newInstance(new URL[] { servletFile.toURL() });

					Servlet servlet;
					servlet = (Servlet) servletLoader.loadClass(uriValues[2])
							.newInstance();

					return servlet.handleRequest((HttpRequest) request);
				}
			}
		}
		return HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
	}
}
