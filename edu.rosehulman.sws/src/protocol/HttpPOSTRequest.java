/*
 * HttpGETthis.java
 * Apr 26, 2015
 *
 * Simple Web Server (SWS) for EE407/507 and CS455/555
 * 
 * Copyright (C) 2011 Chandan Raj Rupakheti, Clarkson University
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
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Department of Electrical and Computer Engineering
 * Clarkson University
 * Potsdam
 * NY 13699-5722
 * http://clarkson.edu/~rupakhcr
 */

package protocol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import server.Server;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class HttpPOSTRequest extends HttpRequest {

	/*
	 * (non-Javadoc)
	 * 
	 * @see protocol.Httpthis#handle()
	 */
	@Override
	public IHttpResponse handle(Server s) {
		IHttpResponse returner;
		File f = new File(this.getHeader().get("file-name"));

		try {
			FileWriter fw = new FileWriter(f, false);
			fw.write(this.getBody());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*returner = HttpResponseFactory.create201Created(Protocol.CLOSE,
				f.getPath());*/
		returner = HttpResponseFactory
				.create404NotFound(Protocol.CLOSE);

		return returner;
	}

}
