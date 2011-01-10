package reviews.generating;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for retrieving web pages from the "reviews.cnet.com/digital-cameras"
 * web site. Only pages containing the "rateSum" tag will be retrieved, having
 * previously identified as containing user reviews
 * 
 * @author Administrator
 * 
 */
public class BasicCrawler {

	public static String CNET_REVIEWS = "http://reviews.cnet.com/";
	public static String DIGITAL_CAMERAS = "digital-cameras/";
	public static String CNET_REVIEWS_DIGITAL_CAMERAS = CNET_REVIEWS
			+ DIGITAL_CAMERAS;
	public static String DATASET_CNET = "dataset/cnet/extracted/";

	public Set<String> visited;
	public int reviews;

	// public Queue<String> toVisit;

	public BasicCrawler() {
		reviews = 0;
		visited = new HashSet<String>();
		// toVisit = new LinkedList<String>();
	}

	public void crawl(URL url, int depth) {
		// System.out.println("trying to crawl " + url);
		try {
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			// System.out.println("Type: "+connection.getContentType());
			if (connection.getContentType() != null
					&& connection.getContentType().startsWith("text/html")) {

				// Read
				InputStream stream = connection.getInputStream();
				String htmlText = BasicCrawler.readInputStreamAsString(stream);

				// Process
				if (htmlText.contains("rateSum first")) {
					reviews++;
					System.out.println("Page contains reviews!! " + url);
					String filename = url.toString().substring(
							BasicCrawler.CNET_REVIEWS_DIGITAL_CAMERAS.length());

					if (url.getQuery() != null) {
						filename = filename.substring(0,
								filename.indexOf(url.getQuery()) - 1);
					}
					String dirname = filename.substring(0,
							filename.indexOf("/"));
					new File(DATASET_CNET + dirname).mkdirs();

					BufferedWriter out = new BufferedWriter(new FileWriter(
							DATASET_CNET + filename));
					out.write(htmlText);
					out.close();
				}

				// More levels?
				if (depth == 0) {
					return;
				}
				// Get links
				Set<String> links = BasicCrawler.findHrefLink(htmlText);
				Set<URL> urls = getURLs(links, url);
				System.out.println("Depth: " + depth + " urls: " + urls.size()
						+ " " + urls);
				System.out.println("Visited up to now " + this.visited.size()
						+ " and found " + this.reviews + " user review pages");

				htmlText = "";

				for (URL urll : urls) {
					crawl(urll, depth - 1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Transforms a list of URI strings into URLs
	 * 
	 * @param urls
	 * @param original
	 * @return
	 * @throws MalformedURLException
	 */
	public Set<URL> getURLs(Set<String> urls, URL original)
			throws MalformedURLException {
		Set<URL> set = new HashSet<URL>();
		for (String s : urls) {
			if (!this.visited.contains(s)) {
				this.visited.add(s);

				URL newURL = new URL(original, s);
				if (newURL.toString() != null
						&& newURL.toString().startsWith(
								BasicCrawler.CNET_REVIEWS
										+ BasicCrawler.DIGITAL_CAMERAS)
						&& !newURL.toString().contains("%")) {
					set.add(newURL);
				} else {
					// System.out.println(newURL.toString());
				}
			}
		}

		return set;
	}

	/**
	 * Produces a list of string URIs from the anchor tags in a HTML string
	 * @param html
	 * @return
	 */
	public static Set<String> findHrefLink(String html) {
		Set<String> set = new HashSet<String>();

		if (html != null) {
			String regex = ".*<a.+href=\"(.+)\".*>";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(html.toLowerCase());
			while (m.find()) {
				// get the matching group
				String codeGroup = m.group(1);
				if (codeGroup.contains("\"")) { // somehow the regex failed
					// System.out.println("Problem: "+codeGroup);
					codeGroup = codeGroup.substring(0, codeGroup.indexOf("\""));
					// System.out.println("Solved : "+codeGroup);
				}
				// print the group
				set.add(codeGroup);
			}
		}

		return set;
	}

	/**
	 * Transforms the inputStream into a String
	 * @param in
	 * @return
	 * @throws IOException
	 */
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
		System.out.println("GO!");
		int maxDepth = 2;
		BasicCrawler bc = new BasicCrawler();
		bc.crawl(new URL(BasicCrawler.CNET_REVIEWS_DIGITAL_CAMERAS), maxDepth);
		System.out.println("Visited up to now " + bc.visited.size()
				+ " and found " + bc.reviews + " user review pages");
	}
}
