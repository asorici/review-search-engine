package reviews.generating;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

/**
 * Class for "cooking" the reviews to a more suitable form, starting from the
 * original pages. The user reviews are in a
 * <code><div class="rateSumWrap"></code> container, each with up to five
 * (usually five) reviews in <li>elements with the class "rateSum"
 * 
 * @author Administrator
 * 
 */
public class ReviewChef {

	public static String START_DIR = "dataset/cnet/extracted";
	public static String TARGET = "dataset/cnet/crawled/";
	public static String FILE_NAME = "review";
	public static String EXTENSION = ".html";

	private static int index = 1;

	public ReviewChef() {
	}

	public static int index() {
		return index++;
	}

	public void cook(String dirName) {
		File f = new File(dirName);
		if (f.isDirectory()) {
			File[] subDirs = f.listFiles(new FileFilter() {

				@Override
				public boolean accept(File arg0) {
					if (arg0.isDirectory() && arg0.toString().contains(".svn"))
						return false;
					return true;
				}
			});
			for (File file : subDirs) {
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					for (File reviews : files) {
						each(reviews);
//						return;
					}
				}
			}
		} else {
			System.out.println("Wrong start place! " + dirName);
		}
	}

	public void each(File reviews) {

		try {
			InputStream is = new FileInputStream(reviews);
			String text = BasicCrawler.readInputStreamAsString(is);
			Parser parser;

			parser = new Parser(text);
			NodeIterator ni = parser.elements();
			while (ni.hasMoreNodes()) {
				Node node = ni.nextNode();
				Pattern p = Pattern.compile("<title>(.*?)</title>");
				Matcher m = p.matcher(node.toHtml());
				String title = null;
				if (m.find() == true) {
					title = m.group(1);
				}

				searchNode(node, title);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void searchNode(Node n, String title) throws IOException {
		String text = n.getText();
		// System.out.println("nodeeee: "+text);
		if (text != null && text.contains("rateSum")
				&& !text.contains("rateSumWrap")) {
			// System.out.println("Ratesum node: " + text);
			// System.out.println("" + n.toHtml());
			String filename = ReviewChef.TARGET + ReviewChef.FILE_NAME
					+ ReviewChef.index() + ReviewChef.EXTENSION;
			System.out.println("File: " + filename + " Title: " + title);
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write("<html><head><title>" + title + "</title></head><body>"
					+ n.toHtml() + "</body></html>");
			out.close();
		}

		NodeList nl = n.getChildren();
		if (nl != null) {
			SimpleNodeIterator sni = nl.elements();

			while (sni.hasMoreNodes()) {
				searchNode(sni.nextNode(), title);
			}
		}
	}

	public static void main(String[] args) throws MalformedURLException {
		System.out.println("GO Chef!");
		ReviewChef chef = new ReviewChef();
		chef.cook(ReviewChef.START_DIR);
	}
}
