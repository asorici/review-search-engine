package reviews.indexing.tokenizing.SWN;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;

public class SWN {
	
	String swnPath;
	HashMap<String, SWNData> dictionary;
	
	public SWN(String filepath) {
		swnPath = filepath;
		loadSWN();
	}
	
	private void loadSWN() {
		try {
			
			System.out.print("Loading SentiWordNet 1.0...");
			
			dictionary = new HashMap<String, SWNData>();
			
			BufferedReader br = new BufferedReader(new FileReader(swnPath));
			
			String line = "";
			
			while ((line = br.readLine()) != null) {
				if (line.indexOf('#') == 0) continue;
				
				String data[] = line.split("\t");
				String words[] = data[4].split(" ");
				
				for (String w : words) {
					// Split SWN word data into tokens: word#pos#sense
					String wordTokens[] = w.split("#");
					
					SWNData wordData;
					// build word id for dictionary: word#pos
					String id = wordTokens[0] + "#" + data[0];
					
					if ((wordData = dictionary.get(id)) != null) {
						wordData.addSense(Integer.parseInt(wordTokens[2]),
								Double.parseDouble(data[2]), Double.parseDouble(data[3]));
						dictionary.put(id, wordData);
					} else {
						dictionary.put(id, new SWNData(Integer.parseInt(wordTokens[2]),
								Double.parseDouble(data[2]), Double.parseDouble(data[3])));
					}
				}
			}
			
			System.out.println(" Done!");
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public SWNData extractWordData(String word, String pos) {
		return dictionary.get(word + "#" + pos);
	}
	
	private int testClassifyScore(double score) {
		if(score>=0.75)
			return 6;
		else
		if(score >= 0.25 && score<0.75)
			return 5;
		else
		if(score > 0 && score<0.25)
			return 4;
		else
		if(score < 0 && score>-0.25)
			return 2;
		else
		if(score <= -0.25 && score>-0.75)
			return 1;
		else
		if(score<=-0.75)
			return 0;
		
		return 3;
	}
	
	public void testSWN(boolean allMeanings) {
		Set<String> ids = dictionary.keySet();
		
		double max = 0.0;
		String maxID = "";
		double min = 0.0;
		String minID = "";
		
		double sum = 0.0;
		
		int counter[] = {0, 0, 0, 0, 0, 0, 0};
		
		for (String id : ids) {
			double score;
			if (allMeanings) {
				score = dictionary.get(id).computeScore();
			} else {
				score = dictionary.get(id).getBasicScore();
			}
			
			if (max < score) {
				max = score;
				maxID = id;
			}
			
			if (min > score) {
				min = score;
				minID = id;
			}
			
			sum += score;
			
			counter[testClassifyScore(score)] ++;
		}
		
		System.out.println("Max score: " + maxID + " " + max);
		System.out.println("Min score: " + minID + " " + min);
		System.out.println("Avg score:" + ((double) sum / ids.size()));

		for (int c : counter) {
			System.out.print(c + ",");
		}
		
		System.out.println("\n");
	}
}
