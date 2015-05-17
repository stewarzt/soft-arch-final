
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import protocol.HttpRequest;
import protocol.HttpResponseFactory;
import protocol.IHttpResponse;
import protocol.Protocol;
import protocol.Servlet;



public class DeleteServlet implements Servlet{

	@Override
	public IHttpResponse handleRequest(HttpRequest request) {

		String[] uriValues = request.uri.split("/");
		StringBuilder fName = new StringBuilder();

		for (int x = 3; x < uriValues.length; x++) {
			fName.append(uriValues[x] + "/");
		}
		IHttpResponse returner;
		File f = new File(fName.toString());
		try {
			Files.deleteIfExists(f.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Do Response
		returner = HttpResponseFactory
				.create204NoContent(Protocol.CLOSE);
		return returner;
	}

}
