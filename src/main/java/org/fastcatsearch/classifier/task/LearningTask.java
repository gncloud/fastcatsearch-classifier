package org.fastcatsearch.classifier.task;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.analysis.AnalyzedTermsIterator;
import org.fastcatsearch.classifier.core.ClassifierTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LearningTask implements Closeable {
	
	private static final Logger logger = LoggerFactory.getLogger(LearningTask.class);
	
	private ClassifierTable table;
	private AnalyzedTermsIterator termsIterator;
	
	public LearningTask(int tableSize, int hashBucketSize, int categoryLength, AnalyzedTermsIterator termIterator) {
		this.termsIterator = termIterator;
		this.table = new ClassifierTable(tableSize, hashBucketSize, categoryLength);
	}
	
	public void learn(Iterator<LearningItem> data) {
		while (data.hasNext()) {
			LearningItem item = data.next();
			learnItem(item.getCategory(), item.getText());
		}
	}
	
	public void learnItem(int category, String text) {
		//remove duplicate for 1 item
		Set<String> set = new HashSet<String>();
		int cntWord = 0;
		termsIterator.prepareText(text);
		while (termsIterator.hasNext()) {
			String termStr = termsIterator.next();
			if(!set.contains(termStr)) {
				logger.trace("ADD TERM:{}", termStr);
				table.learnTerm(termStr, category, 1);
				set.add(termStr);
				cntWord++;
			}
		}

		// add 1 item in-category / total
		table.adjust(category, cntWord, 1, 1);
	}
	
	public void done() {
		table.freeze();
	}

	@Override
	public void close() throws IOException {
		termsIterator.close();
	}
}