import java.io.File;

import protocol.HttpRequest;
import protocol.HttpResponseFactory;
import protocol.IHttpResponse;
import protocol.Protocol;
import protocol.Servlet;

public class GetServlet implements Servlet {

	public int id = 0;
	@Override
	public IHttpResponse handleRequest(HttpRequest request) {
		IHttpResponse returner;
		// Handling GET this here
		// Combine them together to form absolute file path
		String[] uriValues = request.uri.split("/");
		StringBuilder f = new StringBuilder();

		for (int x = 3; x < uriValues.length; x++) {
			f.append(uriValues[x] + "/");
		}
		File file = new File(f.toString());
		// Check if the file exists
		if (file.exists()) {
			if (file.isDirectory()) {
				StringBuilder s = new StringBuilder();
				s.append("{\"files\": [");
				recurseBuild(s, file);
				String output = s.substring(0, s.length() - 1) + "]}";
				System.out.println(output);
				returner = HttpResponseFactory.create200OK(output,
						Protocol.CLOSE);
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
	
	public void recurseBuild(StringBuilder s, File file)
	{
		File[] fList = file.listFiles();

		for (int x = 0; x < fList.length; x++) {
			if (fList[x].isFile()) {
				if (x != fList.length - 1) {
					s.append("{\"id\": " + id + ", \"path\" : \""
							+ fList[x].getAbsolutePath() + "\"},");
				} else {
					s.append("{\"id\": " + id + ", \"path\" : \""
							+ fList[x].getAbsolutePath() + "\"},");
				}
				id++;
			}
			else
			{
				recurseBuild(s, fList[x]);
			}
		}

	}
	
	

}
