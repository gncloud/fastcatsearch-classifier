package org.fastcatsearch.classifier.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fastcatsearch.common.data.structure.ArrayUtility;
import org.fastcatsearch.common.data.structure.BasicStringHashSet;
import org.fastcatsearch.common.data.structure.DoubleIntegerHashSet;
import org.fastcatsearch.common.data.structure.HashFunctions;

public class ClassifierTable extends BasicStringHashSet {
	
	private boolean freezed;

	//instantly used by classify
	private Set<Integer>[] termCategoryList;
	
	//term x category
	private double[] terms; 
	
	//category
	private double[] termsTotal; 
	private double[] items; 
	//private double[] itemsTotal;
	private double itemsTotal;
	
	//File indexFile;
	
	public DoubleIntegerHashSet categoryDataSet;
	
	/**
	 * default tableSize = 2^16
	 * default hashBucketSize = 2^24
	 * @param tableSize
	 * @param hashBucketSize
	 */
	@SuppressWarnings("unchecked")
	public ClassifierTable(int tableSize, int hashBucketSize, int categoryLength) {
		super(HashFunctions.HashType.RSHash, tableSize, hashBucketSize);
		
		this.termCategoryList = new Set[100];
		this.terms = new double[100];
		this.termsTotal = new double[100];
		this.items = new double[100];
		this.itemsTotal = 0;
		this.categoryDataSet = new DoubleIntegerHashSet((int)(hashBucketSize * 1.5));
		this.freezed = false;
	}

	/**
	 * thread x term (max count) x buffer length
	 * 
	 **/
	private static final double SCORE_IGNORE = -Math.log(0.5);
	
	public void learn(CharSequence term, int categoryId, int terms) {
		learn(term, categoryId, terms, false);
	}
	
	public void learn(CharSequence term, int categoryId, int terms, boolean fix) {
		if(!freezed) {
			int termId = put(term);
			int scoreId = getScoreId(termId, categoryId, terms);
			if(logger.isTraceEnabled()) {
				logger.trace("[{}]LEARN CATE[{}/{}\"{}\"]:{}", fix, categoryId, termId, get(termId), scoreId);
			}
			resizeScore(scoreId);
			if(fix) {
				this.terms[scoreId] = terms;
			} else {
				this.terms[scoreId] += terms;
			}
		} else {
			throw new RuntimeException("DataFreezedException");
		}
	}
	
	private void resizeCategory(int categoryId) {
		if(this.items.length <= categoryId) {
			int size = ArrayUtility.growup(categoryId + 1);
			this.items = Arrays.copyOf(this.items, size);
			this.termsTotal = Arrays.copyOf(this.termsTotal, size);
		}
	}
	private void resizeScore(int scoreId) {
		if(this.terms.length <= scoreId) {
			this.terms = Arrays.copyOf(this.terms, ArrayUtility.growup(scoreId + 1));
		}
	}
	
	private int getScoreId(int termId, int categoryId, int terms) {
		if(this.termCategoryList.length <= termId) {
			this.termCategoryList = Arrays.copyOf(this.termCategoryList, ArrayUtility.growup(termId + 1));
		}
		
		if(this.termCategoryList[termId] == null) {
			this.termCategoryList[termId] = new HashSet<Integer>();
		}
		
		termCategoryList[termId].add(categoryId);
		
		return categoryDataSet.put(termId, categoryId);
	}
	
	public void adjust(int categoryId, int termsTotal, int items, int itemsTotal) {
		adjust(categoryId, termsTotal, items, itemsTotal, false);
	}
	
	public void adjust(int categoryId, int termsTotal, int items, int itemsTotal, boolean fix) {
		if(!freezed) {
			resizeCategory(categoryId);
			if(fix) {
				this.termsTotal[categoryId] = termsTotal;
				this.items[categoryId] = items;
				this.itemsTotal = itemsTotal;
			} else {
				this.termsTotal[categoryId] += termsTotal;
				this.items[categoryId] += items;
				this.itemsTotal += itemsTotal;
			}
		} else {
			throw new RuntimeException("DataFreezedException");
		}
	}
	
	public void prepareLearning() {
		if(freezed) {
			unfreeze();
		}
	}
	
	public void freeze() {
		if (!freezed) {
			for (int inx = 0; inx < items.length; inx++) {
				//add bias value
				termsTotal[inx]	= -Math.log(termsTotal[inx] + 1);
				items[inx]		= -Math.log(items[inx]);
			}
			itemsTotal			= -Math.log(itemsTotal);
			for (int inx = 0; inx < terms.length; inx++) {
				//if(terms[inx] != 0) {
					terms[inx] = -Math.log(terms[inx]);
				//}
			}
			freezed = true;
			logger.debug("DATA FREEZED");
		}
	}
	
	public void unfreeze() {
		if (freezed) {
			for (int inx = 0; inx < items.length; inx++) {
				//add bias value
				termsTotal[inx]	= Math.pow(Math.E, -termsTotal[inx]) - 1;
				items[inx]		= Math.pow(Math.E, -items[inx]);
			}
			itemsTotal			= Math.pow(Math.E, -itemsTotal);
			for (int inx = 0; inx < terms.length; inx++) {
				//if(terms[inx] != 0) {
					terms[inx] = Math.pow(Math.E, -terms[inx]);
				//}
			}
			freezed = false;
			logger.debug("DATA UNFREEZED");
		}
	}
	
	public void classify(List<CharSequence> termList, int[] category, double[] scoreTable) {
		List<Integer> termIdList = new ArrayList<Integer>();
		for (int inx = 0; inx < termList.size(); inx++) {
			termIdList.add(put(termList.get(inx)));
		}
		classifyById(termIdList, category, scoreTable);
	}
	
	public void classifyById(List<Integer> termIdList, int[] category, double[] score) {
		
		if(!freezed) {
			freeze();
		}
		
		if(score == null) {
			score = new double[category.length];
		}
		
		Arrays.fill(score, Double.MAX_VALUE);
		Arrays.fill(category, -1);
		
		Set<Integer> categoryCheck = new HashSet<Integer>();
		
		for (int tinx = 0; tinx < termIdList.size(); tinx++) {
			int termId = termIdList.get(tinx);
			if (termId >= 0 && termId < this.termCategoryList.length && this.termCategoryList[termId] != null) {
				categoryCheck.addAll(this.termCategoryList[termId]);
			}
		}
		
		if(logger.isTraceEnabled()) {
			for (int inx = 1; inx <= categoryDataSet.dataCount(); inx++) {
				int[] data = new int[2];
				categoryDataSet.get(inx, data );
				logger.trace("FINDING CATEDATA[{}]={}:{} / {} / {}", inx, data[0], data[1], categoryDataSet.getId(data[0], data[1]), terms[inx]);
			}
		}
		
		Iterator<Integer> iter = categoryCheck.iterator();
		for (; iter.hasNext();) {
			int categoryId = iter.next();
			double currentScore = 0;
			currentScore = items[categoryId] - itemsTotal;
			
			for (int inx = 0; inx < termIdList.size(); inx++) {
				int scoreId = categoryDataSet.getId(termIdList.get(inx), categoryId);
				
				if (logger.isTraceEnabled()) {
					logger.trace("CATE[{}/{}={}]:{}", categoryId, termIdList.get(inx), get(termIdList.get(inx)), scoreId);
				}
				if(scoreId > 0) {
					currentScore += (terms[scoreId] - termsTotal[categoryId]);
				} else {
					currentScore += (SCORE_IGNORE - termsTotal[categoryId]);
				}
				if (currentScore > score[score.length - 1]) {
					break;
				}
			}//category-score-loop
			if(logger.isTraceEnabled()) {
				logger.trace("CATEGORY:{} / SCORE:{} / {} / {}", categoryId, currentScore, categoryId, score[score.length - 1]);
			}
			if(currentScore < score[score.length - 1]) {
				for (int inx = 0; inx < score.length; inx++) {
					if(categoryId != category[inx] && currentScore < score[inx]) {
						if (score.length > (inx + 2)) {
							System.arraycopy(score, inx, score, inx+1, score.length - inx - 2);
							System.arraycopy(category, inx, category, inx+1, category.length - inx - 2);
						}
						logger.trace("SCORE[{}] = {} / CATEGORY[{}] = {}", inx, currentScore, inx, categoryId);
						score[inx] = currentScore;
						category[inx] = categoryId;
						break;
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized int readFrom(InputStream istream, byte[] buffer) throws IOException {
		int ret = 0;
		if (buffer == null || buffer.length < 512) { buffer = new byte[512]; }
		ret += super.readFrom(istream, buffer);
		ret += categoryDataSet.readFrom(istream, buffer);
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER * 2);
		int termsLength = ArrayUtility.restoreInteger(buffer, 0);
		int itemsLength = ArrayUtility.restoreInteger(buffer, ArrayUtility.BYTES_INTEGER);
		this.terms = new double[termsLength];
		this.items = new double[itemsLength];
		this.termsTotal = new double[itemsLength];
		ret += ArrayUtility.readInput(istream, buffer, this.terms);
		ret += ArrayUtility.readInput(istream, buffer, this.termsTotal);
		ret += ArrayUtility.readInput(istream, buffer, this.items);
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_DOUBLE);
		this.itemsTotal = ArrayUtility.restoreDouble(buffer, 0);
		
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER);
		int termCategoryListSize = ArrayUtility.restoreInteger(buffer, 0);
		this.termCategoryList = new Set[termCategoryListSize];
		for (int inx = 0; inx < termCategoryListSize; inx++) {
			ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER);
			int categorySize = ArrayUtility.restoreInteger(buffer, 0);
			if(categorySize > 0) {
				this.termCategoryList[inx] = new HashSet<Integer>();
				for (int inx2 = 0; inx2 < categorySize; inx2++) {
					ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER);
					int categoryId = ArrayUtility.restoreInteger(buffer, 0);
					this.termCategoryList[inx].add(categoryId);
				}
				if(logger.isTraceEnabled()) {
					logger.trace("READ-TERMCATE[{}]:{}", inx, this.termCategoryList[inx]);
				}
			}
		}
		if(logger.isTraceEnabled()) {
			for (int inx = 1; inx <= categoryDataSet.dataCount(); inx++) {
				int[] data = new int[2];
				categoryDataSet.get(inx, data );
				logger.trace("READ CATEDATA[{}]={}:{} / {} / {}", inx, data[0], data[1], categoryDataSet.getId(data[0], data[1]), terms[inx]);
			}
		}
		freezed = true;
		return ret;
	}

	@Override
	public synchronized int writeTo(OutputStream ostream, byte[] buffer) throws IOException {
		if(!freezed) {
			freeze();
		}
		if (buffer == null || buffer.length < 512) { buffer = new byte[512]; }
		int pos = 0;
		super.writeTo(ostream, buffer);
		categoryDataSet.writeTo(ostream, buffer);
		ArrayUtility.mapInteger(terms.length, buffer, 0);
		ArrayUtility.mapInteger(items.length, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ostream.write(buffer, 0, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.writeOutput(ostream, terms, buffer);
		ArrayUtility.writeOutput(ostream, termsTotal, buffer);
		ArrayUtility.writeOutput(ostream, items, buffer);
		ArrayUtility.mapDouble(this.itemsTotal, buffer, 0);
		ostream.write(buffer, 0, ArrayUtility.BYTES_DOUBLE);
		ArrayUtility.mapInteger(termCategoryList.length, buffer, 0);
		ostream.write(buffer, 0, ArrayUtility.BYTES_INTEGER);
		for (int inx = 0; inx < this.termCategoryList.length; inx++) {
			if(this.termCategoryList[inx] == null) {
				ArrayUtility.mapInteger(0, buffer, 0);
				ostream.write(buffer, 0, ArrayUtility.BYTES_INTEGER);
			} else {
				if(logger.isTraceEnabled()) {
					logger.trace("WRITE-TERMCATE[{}]:{}", inx, this.termCategoryList[inx]);
				}
				ArrayUtility.mapInteger(this.termCategoryList[inx].size(), buffer, 0);
				ostream.write(buffer, 0, ArrayUtility.BYTES_INTEGER);
				for(Integer item : this.termCategoryList[inx]) {
					ArrayUtility.mapInteger(item, buffer, 0);
					ostream.write(buffer, 0, ArrayUtility.BYTES_INTEGER);
				}
			}
		}
		return streamLength();
	}

	@Override
	public synchronized int streamLength() {
		int ret = super.streamLength();
		ret += categoryDataSet.streamLength();
		ret += ArrayUtility.BYTES_DOUBLE * terms.length;
		ret += ArrayUtility.BYTES_DOUBLE * termsTotal.length;
		ret += ArrayUtility.BYTES_DOUBLE * items.length;
		ret += ArrayUtility.BYTES_DOUBLE;
		return ret;
	}
}