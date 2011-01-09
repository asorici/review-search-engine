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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import reviews.indexing.tokenizing.SWN.SWN;


/** Indexer for HTML files. */
public class IndexReviews {

	public static final Set<String> FEATURE_SET = loadFeaturesFromFile("features.txt");
	public static final CharArraySet STOPWORDS = readStopWords("more_stopwords.txt");
	
	private static CharArraySet readStopWords(String filename){
			
		CharArraySet stopwords = new CharArraySet(StopAnalyzer.ENGLISH_STOP_WORDS_SET, true);

		try {
			BufferedReader br;
			br = new BufferedReader(new FileReader(new File(filename)));
			String line;
			while ((line = br.readLine()) != null) {
				stopwords.add(line.trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return stopwords;
	}

	private IndexReviews() {	
	}

	private static boolean deleting = false; // true during deletion pass
	private static IndexReader reader; // existing index
	private static IndexWriter writer; // new index being built
	private static TermEnum uidIter; // document id iterator

	private static Set<String> loadFeaturesFromFile(String filename) {

		Set<String> features = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(
					filename)));

			String line;
			while ((line = br.readLine()) != null) {
				features.add(line);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return features;
	}

	/** Indexer for HTML files. */
	public static void main(String[] argv) {
		
		// Voi curata mai tarziu aici :P - Andrei
//		SWN senti = new SWN("dataset" + File.separator + "SWN" + File.separator + "SentiWordNet_1.0.1.txt");
		
/*		System.out.println("big: " + senti.extractWordData("big", "a").getLabel() + "[" + senti.extractWordData("big", "a").computeScore() +"]");
		System.out.println("small: " + senti.extractWordData("small", "a").getLabel() + "[" + senti.extractWordData("small", "a").computeScore() +"]");
		System.out.println("amazing: " + senti.extractWordData("amazing", "a").getLabel() + "[" + senti.extractWordData("amazing", "a").computeScore() +"]");
		System.out.println("wonderful: " + senti.extractWordData("wonderful", "a").getLabel() + "[" + senti.extractWordData("wonderful", "a").computeScore() +"]");
		System.out.println("clear: " + senti.extractWordData("clear", "a").getLabel() + "[" + senti.extractWordData("clear", "a").computeScore() +"]");
		System.out.println("clearly: " + senti.extractWordData("clearly", "r").getLabel() + "[" + senti.extractWordData("clearly", "r").computeScore() +"]");
*/
		
//		senti.testSWN(true);
//		senti.testSWN(false);
		
//		System.exit(0);
		
		try {
			File index = new File("index");
			boolean create = false;
			File root = null;

			String usage = "IndexHTML [-create] [-index <index>] <root_directory>";

			if (argv.length == 0) {
				System.err.println("Usage: " + usage);
				return;
			}

			for (int i = 0; i < argv.length; i++) {
				if (argv[i].equals("-index")) { // parse -index option
					index = new File(argv[++i]);
				} else if (argv[i].equals("-create")) { // parse -create option
					create = true;
				} else if (i != argv.length - 1) {
					System.err.println("Usage: " + usage);
					return;
				} else
					root = new File(argv[i]);
			}

			if (root == null) {
				System.err.println("Specify directory to index");
				System.err.println("Usage: " + usage);
				return;
			}

			Date start = new Date();

			if (!create) { // delete stale docs
				deleting = true;
				indexDocs(root, index, create);
			}

			
			StandardAnalyzer standardAnalyzer = new StandardAnalyzer(Version.LUCENE_30, STOPWORDS);

			writer = new IndexWriter(FSDirectory.open(index), standardAnalyzer,
					create, new IndexWriter.MaxFieldLength(1000000));

			indexDocs(root, index, create); // add new docs

			System.out.println("Optimizing index...");
			writer.optimize();
			writer.close();

			Date end = new Date();

			System.out.print(end.getTime() - start.getTime());
			System.out.println(" total milliseconds");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Walk directory hierarchy in uid order, while keeping uid iterator from /*
	 * existing index in sync. Mismatches indicate one of: (a) old documents to
	 * /* be deleted; (b) unchanged documents, to be left alone; or (c) new /*
	 * documents, to be indexed.
	 */

	private static void indexDocs(File file, File index, boolean create)
			throws Exception {
		if (!create) { // incrementally update

			reader = IndexReader.open(FSDirectory.open(index), false); // open
			// existing
			// index
			uidIter = reader.terms(new Term("uid", "")); // init uid iterator

			indexDocs(file);

			if (deleting) { // delete rest of stale docs
				while (uidIter.term() != null
						&& uidIter.term().field() == "uid") {
					System.out.println("deleting "
							+ HTMLDocument.uid2url(uidIter.term().text()));
					reader.deleteDocuments(uidIter.term());
					uidIter.next();
				}
				deleting = false;
			}

			uidIter.close(); // close uid iterator
			reader.close(); // close existing index

		} else
			// don't have exisiting
			indexDocs(file);
	}

	private static void indexDocs(File file) throws Exception {

		if (file.isDirectory()) { // if a directory
			String[] files = file.list(); // list its files
			Arrays.sort(files); // sort the files
			for (int i = 0; i < files.length; i++)
				// recursively index them
				indexDocs(new File(file, files[i]));

		} else if (file.getPath().endsWith(".html") || // index .html files
				file.getPath().endsWith(".htm")) {//|| // index .htm files
//				file.getPath().endsWith(".txt")) { // index .txt files

			if (uidIter != null) {
				String uid = HTMLDocument.uid(file); // construct uid for doc

				while (uidIter.term() != null
						&& uidIter.term().field() == "uid"
						&& uidIter.term().text().compareTo(uid) < 0) {
					if (deleting) { // delete stale docs
						System.out.println("deleting "
								+ HTMLDocument.uid2url(uidIter.term().text()));
						reader.deleteDocuments(uidIter.term());
					}
					uidIter.next();
				}
				if (uidIter.term() != null && uidIter.term().field() == "uid"
						&& uidIter.term().text().compareTo(uid) == 0) {
					uidIter.next(); // keep matching docs
				} else if (!deleting) { // add new docs
					Document doc = HTMLDocument.Document(file);

					System.out.println("adding " + doc.get("path"));
					writer.addDocument(doc);
				}
			} else { // creating a new index
				Document doc = HTMLDocument.Document(file);
				System.out.println("adding " + doc.get("path"));
				writer.addDocument(doc); // add docs unconditionally
			}
		}
	}
}