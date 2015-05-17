
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import protocol.HttpRequest;
import protocol.HttpResponseFactory;
import protocol.IHttpResponse;
import protocol.Protocol;
import protocol.Servlet;



public class PutServlet implements Servlet{

	@Override
	public IHttpResponse handleRequest(HttpRequest request) {
		IHttpResponse returner;
		File f = new File(request.getHeader().get("file-name"));

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
