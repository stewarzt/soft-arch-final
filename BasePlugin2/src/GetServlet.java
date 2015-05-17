
import java.io.File;

import protocol.HttpRequest;
import protocol.HttpResponseFactory;
import protocol.IHttpResponse;
import protocol.Protocol;
import protocol.Servlet;



public class GetServlet implements Servlet{

	@Override
	public IHttpResponse handleRequest(HttpRequest request) {
		IHttpResponse returner;
		// Handling GET this here	
		// Combine them together to form absolute file path
		File file = new File("test2.txt");
		// Check if the file exists
		if (file.exists()) {
			if (file.isDirectory()) {
				// Look for default index.html file in a directory
				String location = "test2.txt";
				file = new File(location);
				if (file.exists()) {
					// Lets create 200 OK response
					returner = HttpResponseFactory.create200OK(file,
							Protocol.CLOSE);
				} else {
					// File does not exist so lets create 404 file not
					// found code
					returner = HttpResponseFactory
							.create404NotFound(Protocol.CLOSE);
				}
			} else { // Its a file
						// Lets create 200 OK response
				returner = HttpResponseFactory
						.create200OK(file, Protocol.CLOSE);
			}
		} else {
			// File does not exist so lets create 404 file not found
			// code
			returner = HttpResponseFactory.create404NotFound(Protocol.CLOSE);
		}
		return returner;
	}

}
