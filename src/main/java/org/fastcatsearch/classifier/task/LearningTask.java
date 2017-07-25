package org.fastcatsearch.classifier.task;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.fastcatsearch.classifier.core.ClassifierTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearningTask implements Closeable {
	
	private static final Logger logger = LoggerFactory.getLogger(LearningTask.class);
	
	private ClassifierTable table;
	
	public LearningTask() {
		
	}
	
	public void learn(int category, String text, TokenStream tstream) {
		
		int cntWord = 0;
		try {
			CharTermAttribute term = tstream.getAttribute(CharTermAttribute.class);
			logger.trace("TEXT:{}", text);
			while (tstream.incrementToken()) {
				logger.trace("TERM:{}", term);
				table.learn(term.toString(), category, 1);
				cntWord++;
			}
		} catch (IOException e) {
			logger.error("", e);
		} finally {
		}
		
		table.adjust(category, cntWord, 1, 1);
	}
	
	public void done() {
		table.freeze();
	}

	@Override
	public void close() throws IOException {
	}
}