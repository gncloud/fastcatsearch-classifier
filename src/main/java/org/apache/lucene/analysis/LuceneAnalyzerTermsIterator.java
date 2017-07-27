package org.apache.lucene.analysis;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class LuceneAnalyzerTermsIterator implements AnalyzedTermsIterator {
	
	private Analyzer analzyer;
	private TokenStream tokenStream;
	private CharTermAttribute termAttribute;
	private String resultTerm;
	
	public LuceneAnalyzerTermsIterator(Analyzer analyzer) {
		setAnalyzer(analyzer);
	}
	
	private String getNext() {
		String ret = null;
		try {
			if(tokenStream.incrementToken()) {
				ret = termAttribute.toString();
			} else if (tokenStream != null) {
				tokenStream.close();
				tokenStream = null;
			}
		} catch (IOException e) {
			throw new CannotAnalyzeException();
		}
		return ret;
	}

	@Override
	public boolean hasNext() {
		if(resultTerm == null) {
			resultTerm = getNext();
		}
		return resultTerm != null;
	}

	@Override
	public String next() {
		String ret = null;
		if(resultTerm == null) {
			resultTerm = getNext();
		}
		ret = resultTerm;
		resultTerm = null;
		
		return ret;
	}

	@Override
	public void close() throws IOException {
		analzyer.close();
	}

	@Override
	public void setAnalyzer(Analyzer analyzer) {
		this.analzyer = analyzer;

	}

	@Override
	public void prepareText(String text) {
		StringReader reader = new StringReader(text);
		try {
			tokenStream = analzyer.tokenStream("", reader);
			termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
			tokenStream.reset();
		} catch (IOException e) { 
			throw new CannotAnalyzeException();
		}
	}
}
