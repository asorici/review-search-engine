package reviews.generating;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicCrawler {

	public static String CNET_REVIEWS = "http://reviews.cnet.com/";
	public static String BASE = "digital-cameras/";

	public Set<String> visited;

	public BasicCrawler() {
		visited = new HashSet<String>();
	}

	public void crawl(URL url, int depth) {
		try {
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			// System.out.println("Type: "+connection.getContentType());
			if (connection.getContentType() != null
					&& connection.getContentType().equals(
							"text/html; charset=UTF-8")) {

				// Read
				InputStream stream = connection.getInputStream();
				String htmlText = BasicCrawler.readInputStreamAsString(stream);

				// Process
				if (htmlText.contains("rateSum first")){
					System.out.println("Page might contain review!! " + url);
				}
				
				// More levels?
				if (depth == 0) {
					return;
				}
				// Get links
				Set<String> links = BasicCrawler.getTagContent(
						"<a href=\"(\\S+)\">", htmlText);
				Set<URL> urls = getURLs(links, url);
//				System.out.println("Depth: " + depth + " urls: " + urls.size()
//						+ " " + urls);
				htmlText = "";

				for (URL urll : urls) {
					crawl(urll, depth - 1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Set<URL> getURLs(Set<String> urls, URL original)
			throws MalformedURLException {
		Set<URL> set = new HashSet<URL>();
		for (String s : urls) {
			if (!this.visited.contains(s)) {
				this.visited.add(s);

				URL newURL = new URL(original, s);
				if (newURL.toString() != null
						&& newURL.toString().startsWith(
								BasicCrawler.CNET_REVIEWS)) {
					set.add(newURL);
				} else {
					// System.out.println(newURL.toString());
				}
			}
		}

		return set;
	}

	public static Set<String> getTagContent(String match, String htmlText) {
		Set<String> set = new HashSet<String>();
		// the pattern we want to search for
		// Ex: "<a href(\\S+)</a>");

		Pattern p = Pattern.compile(match);
		Matcher m = p.matcher(htmlText);

		// if we find a match, get the group
		while (m.find()) {
			// get the matching group
			String codeGroup = m.group(1);

			// print the group
			set.add(codeGroup);
		}

		return set;
	}

	public static String readInputStreamAsString(InputStream in)
			throws IOException {

		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while (result != -1) {
			byte b = (byte) result;
			buf.write(b);
			result = bis.read();
		}
		return buf.toString();
	}

	/**
	 * @param args
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException {
		BasicCrawler bc = new BasicCrawler();
		bc.crawl(new URL(BasicCrawler.CNET_REVIEWS + BasicCrawler.BASE), 2);
	}
}
