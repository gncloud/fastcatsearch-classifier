package org.apache.lucene.analysis;

import java.io.Closeable;
import java.util.Iterator;

public interface AnalyzedTermsIterator extends Iterator<String>, Closeable {
	public void setAnalyzer(Analyzer analyzer);
	public void prepareText(CharSequence text);
}
