import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import protocol.HttpRequest;
import protocol.HttpResponseFactory;
import protocol.IHttpResponse;
import protocol.Protocol;
import protocol.Servlet;

public class AppendServlet implements Servlet {

	@Override
	public IHttpResponse handleRequest(HttpRequest request) {
		String[] uriValues = request.uri.split("/");

		IHttpResponse returner;
		File f = new File(uriValues[3]);
		File f2 = new File(uriValues[4]);

		try {

			FileInputStream fis = new FileInputStream(f2);
			FileWriter fw = new FileWriter(f, true);
			// Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));

			String line = null;
			while ((line = br.readLine()) != null) {
				fw.write("\r\n" + line);
			}

			br.close();

			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		returner = HttpResponseFactory.create201Created(Protocol.CLOSE, f);

		return returner;
	}

}
