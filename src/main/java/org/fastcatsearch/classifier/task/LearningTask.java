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
	
	public LearningTask() {
		
	}
	
	public void learn(Iterator<LearningItem> data) {
		while (data.hasNext()) {
			LearningItem item = data.next();
			learnItem(item.getCategory(), item.getText());
		}
	}
	
	public void learnItem(int category, String text) {
		//remove duplicate for 1 item
		
		Reader reader = new StringReader(text);
		TokenStream tstream = null;
		Set<String> set = new HashSet<String>();
		
		int cntWord = 0;
		try {
			tstream = analyzer.tokenStream("", reader);
			CharTermAttribute term = tstream.getAttribute(CharTermAttribute.class);
			tstream.reset();
			logger.trace("TEXT:{}", text);
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
		analyzer.close();

		// add 1 item in-category / total
		table.adjust(category, cntWord, 1, 1);
	}
	
	public void done() {
		table.freeze();
	}

	@Override
	public void close() throws IOException {
	}
}