package reviews.indexing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import reviews.indexing.tokenizing.SWN.SWN;
import reviews.indexing.tokenizing.SWN.SWNData;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class OpinionAnalyzer {
	
	private HashMap<String,String> posMap;		// maps the Stanford POS tags to the SWN POS tags
	private Set<String> negativeMarkers;		// considered negative markers
	private SWN swn;							// SentiWordNet wrapper
	
	public OpinionAnalyzer(SWN s) {
		posMap = new HashMap<String,String>();
		posMap.put("JJ", "a");
		posMap.put("JJR", "a");
		posMap.put("JJS", "a");
		
		negativeMarkers = new HashSet<String>();
		negativeMarkers.add("not");
		negativeMarkers.add("no");
		negativeMarkers.add("never");
		
		swn = s;
	}
	
	private class OpinionWord {
		String word;
		boolean hasNegMarker;
		
		OpinionWord(String word, boolean hasNegMarker) {
			this.word = word;
			this.hasNegMarker = hasNegMarker;
		}
	}
	
	/**
	 * Detect opinion words in sentence.
	 * 
	 * @param sentence
	 * @return
	 */
	private Set<OpinionWord> detectOpinionWords(CoreMap sentence) {
		Set<OpinionWord> opinionWords = new HashSet<OpinionWord>();
		
		boolean negation = false;
		
		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String pos = token.get(PartOfSpeechAnnotation.class);
			String word = token.get(TextAnnotation.class);
			
			if (posMap.containsKey(pos)) {
				opinionWords.add(new OpinionWord(SWN.createID(word, posMap.get(pos)), negation));
			}
			
			negation = false;
			
			if (negativeMarkers.contains(word)) {
				negation = true;
			}
		}
		
		return opinionWords;
	}

	/**
	 * Compute the score of a sentence based on the opinion words.
	 * 
	 * TODO: use the distance to feature for better scoring.
	 * 
	 * @param feature
	 * @param sentence
	 * @return
	 */
	public double getSentenceScore(String feature, CoreMap sentence) {
		double score = 0.0;
		Set<OpinionWord> opinionWords = detectOpinionWords(sentence);
		
		for (OpinionWord oW : opinionWords) {
			SWNData data = swn.extractWordData(oW.word);
			if (data == null) continue;
			
			if (oW.hasNegMarker) {
				score += (-1) * data.computeScore();
			} else {
				score += data.computeScore();
			}
		}
		
		return score;
	}
}
