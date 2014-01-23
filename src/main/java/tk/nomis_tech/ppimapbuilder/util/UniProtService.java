package tk.nomis_tech.ppimapbuilder.util;

import java.net.URL;
import java.util.logging.Logger;
import tk.nomis_tech.ppimapbuilder.networkbuilder.network.data.UniProtProtein;

public class UniProtService {

	private static final String UNIPROT_SERVER = "http://www.uniprot.org/";
	private static final Logger LOG = Logger.getAnonymousLogger();

	private UniProtService instance;

	public UniProtService getInstance() {
		if (instance == null)
			instance = new UniProtService();
		return instance;
	}

	public void fillProteinData(UniProtProtein prot) {

	}

	private void buildRequest(String tool) throws Exception {

		StringBuilder locationBuilder = new StringBuilder(UNIPROT_SERVER + tool + "/?");
		Parameter[] params;
		for(Parameter p: params){
			if (i > 0)
				locationBuilder.append('&');
			locationBuilder.append(params);
		}

		String location = locationBuilder.toString();
		URL url = new URL(location);
		LOG.info("Submitting...");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		HttpURLConnection.setFollowRedirects(true);
		conn.setDoInput(true);
		conn.connect();

		int status = conn.getResponseCode();
		while (true) {
			int wait = 0;
			String header = conn.getHeaderField("Retry-After");
			if (header != null)
				wait = Integer.valueOf(header);
			if (wait == 0)
				break;
			LOG.info("Waiting (" + wait + ")...");
			conn.disconnect();
			Thread.sleep(wait * 1000);
			conn = (HttpURLConnection) new URL(location).openConnection();
			conn.setDoInput(true);
			conn.connect();
			status = conn.getResponseCode();
		}
		if (status == HttpURLConnection.HTTP_OK) {
			LOG.info("Got a OK reply");
			InputStream reader = conn.getInputStream();
			URLConnection.guessContentTypeFromStream(reader);
			StringBuilder builder = new StringBuilder();
			int a = 0;
			while ((a = reader.read()) != -1) {
				builder.append((char) a);
			}
			System.out.println(builder.toString());
		} else
			LOG.severe("Failed, got " + conn.getResponseMessage() + " for "
					+ location);
		conn.disconnect();
	}

	public static void main(String[] args)
			throws Exception {
		run("mapping", new Parameter[]{
				new Parameter("from", "ACC"),
				new Parameter("to", "P_REFSEQ_AC"),
				new Parameter("format", "tab"),
				new Parameter("query", "P13368 P20806 Q9UM73 P97793 Q17192"),
		});
	}

	private static class Parameter {
		private final String name;
		private final String value;

		public Parameter(String name, String value) throws UnsupportedEncodingException {
			this.name = URLEncoder.encode(name, "UTF-8");
			this.value = URLEncoder.encode(value, "UTF-8");
		}

		@Override
		public String toString() {
			return name + "=" + value;
		}
	}
}
