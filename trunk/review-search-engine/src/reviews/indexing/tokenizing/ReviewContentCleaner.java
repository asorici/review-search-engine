package reviews.indexing.tokenizing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class ReviewContentCleaner {
	private double score;
	private String title;
	private String pros;
	private String cons;
	private String summary;
	
	public ReviewContentCleaner(String review) {
		if (review.indexOf("stars") == -1) return;
		
		try {
			
			BufferedReader br = new BufferedReader(new StringReader(review));
			
			score = extractScore(br);
			title = extractTitle(br);
			pros = extractPros(br);
			cons = extractCons(br);
			summary = extractSummary(br);
			
			br.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private double extractScore(BufferedReader br) throws IOException {
		br.readLine();	// skip blank line
		br.readLine();	// skip page title
		return Double.parseDouble(br.readLine());
	}
	
	private String extractTitle(BufferedReader br) throws IOException {
		String title = br.readLine();
		
		return title.substring(title.indexOf('"') + 1, title.lastIndexOf('"'));
	}
	
	private String extractPros(BufferedReader br) throws IOException {
		br.readLine();	// skip author
		br.readLine();	// skip Pros title
		String pros = br.readLine();
		
		return pros.substring(0, pros.indexOf(" Cons:"));
	}
	
	private String extractCons(BufferedReader br) throws IOException {
		String cons = br.readLine();
		
		return cons.substring(0, cons.indexOf(" Summary:"));
	}
	
	private String extractSummary(BufferedReader br) throws IOException {
		String summary = br.readLine();
		
		return summary.substring(0, summary.indexOf(" Reply to this review"));
	}

	public double getScore() {
		return score;
	}

	public String getTitle() {
		return title;
	}

	public String getPros() {
		return pros;
	}

	public String getCons() {
		return cons;
	}

	public String getSummary() {
		return summary;
	}
}
