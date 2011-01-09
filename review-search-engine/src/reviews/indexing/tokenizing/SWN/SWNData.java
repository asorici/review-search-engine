package reviews.indexing.tokenizing.SWN;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SWNData {
	HashMap<Integer, Score> senseMap;
	
	private class Score {
		double pScore;
		double nScore;
		
		Score(double posScore, double negScore) {
			pScore = posScore;
			nScore = negScore;
		}

		public double getpScore() {
			return pScore;
		}

		public double getnScore() {
			return nScore;
		}
	}
	
	public SWNData(int sense, double posScore, double negScore) {
		senseMap = new HashMap<Integer, Score>();
		addSense(sense, posScore, negScore);
	}
	
	public void addSense(int sense, double posScore, double negScore) {
		senseMap.put(sense, new Score(posScore, negScore));		
	}
	
	public double computeScore() {
		Set<Integer> senses = senseMap.keySet();
		
		double score = 0.0;
		double sum = 0.0;
		for (Integer s : senses) {
			score += ((double)1/s) * senseMap.get(s).getpScore() - senseMap.get(s).getnScore();
			sum += (double) 1/s;
		}
		
		return (double) score / sum;
	}
	
	public String getLabel(boolean allMeanings) {
		double score;
		
		if (allMeanings) {
			score = computeScore();
		} else {
			score = getBasicScore();
		}
		
		if(score>=0.75)
			return "strong_positive";
		else
		if(score > 0.25 && score<=0.5)
			return "positive";
		else
		if(score > 0 && score<=0.25)
			return "weak_positive";
		else
		if(score < 0 && score>=-0.25)
			return "weak_negative";
		else
		if(score < -0.25 && score>=-0.5)
			return "negative";
		else
		if(score<=-0.75)
			return "strong_negative";
		
		return "";
	}

	public double getPositiveScore(int sense) {
		return senseMap.get(new Integer(sense)).getpScore();
	}
	
	public double getNegativeScore(int sense) {
		return senseMap.get(new Integer(sense)).getnScore();
	}
	
	public double getPositiveScore() {
		if (senseMap.size() == 0) {
			System.err.println("Word has no sense!");
			System.exit(0);
		}
		
		return getPositiveScore((Integer) senseMap.keySet().toArray()[0]);
	}
	
	public double getNegativeScore() {
		if (senseMap.size() == 0) {
			System.err.println("Word has no sense!");
			System.exit(0);
		}
		
		return getNegativeScore((Integer) senseMap.keySet().toArray()[0]);
	}
	
	public double getBasicScore() {
		return getPositiveScore() - getNegativeScore();
	}
	
	@Override
	public String toString() {
		String text = "";
		
		Set<Integer> senses = senseMap.keySet();
		
		for (Integer s : senses) {
			text += "[" + s + ", " + senseMap.get(s).getpScore() +
					", " + senseMap.get(s).getnScore() +  "] ";
		}
		
		return text;
	}
}
