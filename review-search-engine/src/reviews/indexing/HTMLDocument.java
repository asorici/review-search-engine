package reviews.indexing;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.demo.html.HTMLParser;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.json.JSONException;
import org.json.JSONObject;

import reviews.indexing.tokenizing.ReviewContentCleaner;
import reviews.indexing.tokenizing.SWN.SWN;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/** A utility for making Lucene Documents for HTML documents. */

public class HTMLDocument {

	static char dirSep = System.getProperty("file.separator").charAt(0);

	public static String uid(File f) {
		// Append path and date into a string in such a way that lexicographic
		// sorting gives the same results as a walk of the file hierarchy. Thus
		// null (\u0000) is used both to separate directory components and to
		// separate the path from the date.
		return f.getPath().replace(dirSep, '\u0000')
				+ "\u0000"
				+ DateTools.timeToString(f.lastModified(),
						DateTools.Resolution.SECOND);
	}

	public static String uid2url(String uid) {
		String url = uid.replace('\u0000', '/'); // replace nulls with slashes
		return url.substring(0, url.lastIndexOf('/')); // remove date from end
	}

	public static Document Document(File f, SWN swn) throws IOException,
			InterruptedException {
		// make a new, empty document
		Document doc = new Document();
		
		// Add the url as a field named "path". Use a field that is
		// indexed (i.e. searchable), but don't tokenize the field into words.
		doc.add(new Field("path", f.getPath().replace(dirSep, '/'),
				Field.Store.YES, Field.Index.NOT_ANALYZED));

		// Add the last modified date of the file a field named "modified".
		// Use a field that is indexed (i.e. searchable), but don't tokenize
		// the field into words.
		doc.add(new Field("modified", DateTools.timeToString(f.lastModified(),
				DateTools.Resolution.MINUTE), Field.Store.YES,
				Field.Index.NOT_ANALYZED));

		// Add the uid as a field, so that index can be incrementally
		// maintained.
		// This field is not stored with document, it is indexed, but it is not
		// tokenized prior to indexing.
		doc.add(new Field("uid", uid(f), Field.Store.NO,
				Field.Index.NOT_ANALYZED));

		FileInputStream fis = new FileInputStream(f);
		HTMLParser parser = new HTMLParser(fis);

		Reader reader = parser.getReader();

		// getting text content
		String contents = "";
		int c;
		while ((c = reader.read()) != -1) {
			char buf[] = Character.toChars(c);
			contents += String.valueOf(buf);
		}

		// clean the review content
		ReviewContentCleaner rcc = new ReviewContentCleaner(contents);
		
		Set<String> featureSet = new HashSet<String>();
		String features = "";
		
		HashMap<String, ArrayList<FeatureMapData>> featureMap = new HashMap<String, ArrayList<FeatureMapData>>();

		// creates a StanfordCoreNLP object, with POS tagging, parsing
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos");
		props.put("pos.model", "left3words-wsj-0-18.tagger");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(rcc.getSummary());

		// run all Annotators on this text
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		OpinionAnalyzer opinion = new OpinionAnalyzer(swn);
		
		for (CoreMap sentence : sentences) {
//			System.out.println("sentence: " + sentence.toString());
//			System.out.println("sentence: "+ sentence.get(TextAnnotation.class));

			// sentence features set
			Set<String> sfSet = new HashSet<String>();
			
			// traversing the words in the current sentence
//			System.out.println("Has the following words: ");
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				String pos = token.get(PartOfSpeechAnnotation.class);	// used for debugging

				if (IndexReviews.FEATURE_SET.contains(word)) {
					sfSet.add(word);
				}
//				System.out.print(word + "#" + pos + "  ");
			}
//			System.out.println();
//			System.out.println(opinion.getSentenceScore("", sentence));
			
			// for each identified feature, update featureMap
			for (String word : sfSet) {
				double score = opinion.getSentenceScore(word, sentence);
				
				// check to see if we have any more sentences for this feature
				ArrayList<FeatureMapData> featureFeedback = featureMap.get(word);
				
				if (featureFeedback == null) {
					featureFeedback = new ArrayList<FeatureMapData>();
				}
				
				// add a new sentence
				featureFeedback.add(new FeatureMapData(sentence
						.get(TextAnnotation.class), score, ((score >= 0) ? true : false)));
				
				featureMap.put(word, featureFeedback);
			}
			
			// add all new features to the review's featureSet
			featureSet.addAll(sfSet);
		}

		for(String s:featureSet)
			features += s + " ";
		
		System.out.println(features);
		doc.add(new Field("features", features, Field.Store.YES, Field.Index.ANALYZED));
		
		JSONObject serializedFeatureMap = new JSONObject();
		
		try {
			serializedFeatureMap.put("featureMap", featureMap);

			doc.add(new Field("feature-contents", serializedFeatureMap
					.toString(), Field.Store.YES, Field.Index.NO));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

	//	doc.add(new Field("contents", parser.getReader()));
		
		// Add the summary as a field that is stored and returned with
		// hit documents for display
		doc.add(new Field("summary", parser.getSummary(), Field.Store.YES,
				Field.Index.NO));

		// Add the title as a field that it can be searched and that is stored.
		doc.add(new Field("title", parser.getTitle(), Field.Store.YES,
				Field.Index.ANALYZED));

		// return the document
		return doc;
	}

	private HTMLDocument() {
	}
}
