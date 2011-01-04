package reviews.indexing;

public class FeatureMapData {

	String sentence;
	boolean connotation = true;
	double score = 0.0;

	public FeatureMapData(String sentence) {
		this.sentence = sentence;
	}
	
	public FeatureMapData(String sentence, double score) {
		this(sentence);
		this.score = score;
	}
	
	public FeatureMapData(String sentence, double score, boolean connotation) {
		this(sentence, score);
		this.connotation = connotation; 
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public boolean isConnotation() {
		return connotation;
	}

	public void setConnotation(boolean connotation) {
		this.connotation = connotation;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

}
