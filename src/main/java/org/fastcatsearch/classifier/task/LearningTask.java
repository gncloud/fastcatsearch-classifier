package org.fastcatsearch.classifier.task;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.fastcatsearch.classifier.core.ClassifierTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearningTask implements Closeable {
	
	private static final Logger logger = LoggerFactory.getLogger(LearningTask.class);
	
	private ClassifierTable table;
	private Analyzer analyzer;
	
	public LearningTask(int tableSize, int hashBucketSize, int categoryLength, Analyzer analyzer) {
		this.analyzer = analyzer;
		this.table = new ClassifierTable(tableSize, hashBucketSize, categoryLength);
	}
	
	public void learn(Iterator<LearningItem> data) {
		while (data.hasNext()) {
			LearningItem item = data.next();
			learnItem(item.getCategory(), item.getText());
		}
	}
	
	public void learnItem(int category, String text) {
		Reader reader = null;
		reader = new StringReader(text);
		learnItem(category, analyzer.tokenStream("", reader));
		try {
			reader.close();
		} catch (Exception ignore) { }
	}
	
	public void learnItem(int category, TokenStream tstream) {
		//remove duplicate for 1 item
		Set<String> set = new HashSet<String>();
		int cntWord = 0;
		try {
			CharTermAttribute term = tstream.getAttribute(CharTermAttribute.class);
			tstream.reset();
			while (tstream.incrementToken()) {
				String termStr = term.toString();
				if(!set.contains(termStr)) {
					logger.trace("ADD TERM:{}", termStr);
					table.learnTerm(termStr, category, 1);
					set.add(termStr);
					cntWord++;
				}
			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
		}

		// add 1 item in-category / total
		table.adjust(category, cntWord, 1, 1);
	}
	
	public void done() {
		table.freeze();
	}

	@Override
	public void close() throws IOException {
		analyzer.close();
	}
}