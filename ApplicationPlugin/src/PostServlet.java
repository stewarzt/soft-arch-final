
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import protocol.HttpRequest;
import protocol.HttpResponseFactory;
import protocol.IHttpResponse;
import protocol.Protocol;
import protocol.Servlet;



public class PostServlet implements Servlet{

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
			FileWriter fw = new FileWriter(f, false);
			fw.write(request.getBody());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		returner = HttpResponseFactory.create201Created(Protocol.CLOSE,
				f);

		return returner;
	}

}
