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
import org.json.JSONArray;

public class DoubleClassifierTable extends BasicStringHashSet {
	
	private boolean freezed;

	//instantly used by classify
	private Set<Integer>[] termCategoryList1;
	private Set<Integer>[] termCategoryList2;
	
	//term x category
	private double[] terms1; 
	private double[] terms2; 
	
	//category
	private double[] termsTotal1; 
	private double[] termsTotal2; 
	private double[] items1; 
	private double[] items2; 
	private double itemsTotal;
	public DoubleIntegerHashSet categoryDataSet1;
	public DoubleIntegerHashSet categoryDataSet2;
	
	/**
	 * default tableSize = 2^16
	 * default hashBucketSize = 2^24
	 * @param tableSize
	 * @param hashBucketSize
	 */
	@SuppressWarnings("unchecked")
	public DoubleClassifierTable(int tableSize, int hashBucketSize, int categoryLength) {
		super(HashFunctions.HashType.RSHash, tableSize, hashBucketSize);
		
		this.termCategoryList1 = new Set[100];
		this.termCategoryList2 = new Set[100];
		this.terms1 = new double[100];
		this.terms2 = new double[100];
		this.termsTotal1 = new double[100];
		this.termsTotal2 = new double[100];
		this.items1 = new double[100];
		this.items2 = new double[100];
		this.itemsTotal = 0;
		this.categoryDataSet1 = new DoubleIntegerHashSet((int)(hashBucketSize * 1.5));
		this.categoryDataSet2 = new DoubleIntegerHashSet((int)(hashBucketSize * 1.5));
		this.freezed = false;
	}

	/**
	 * thread x term (max count) x buffer length
	 * 
	 **/
	private static final double SCORE_IGNORE = -Math.log(0.5);
	
	public void learn(CharSequence term, int categoryId1, int categoryId2, int terms) {
		learn(term, categoryId1, categoryId2, terms, false);
	}
	
	public void learn(CharSequence term, int categoryId1, int categoryId2, int terms, boolean fix) {
		if(!freezed) {
			int termId = put(term);
			int scoreId1 = getScoreId1(termId, categoryId1, terms);
			int scoreId2 = getScoreId2(termId, categoryId2, terms);
			if(logger.isTraceEnabled()) {
				logger.trace("[{}]LEARN CATE[{},{}/{}\"{}\"]:{},{}", fix, categoryId1, categoryId2, termId, get(termId), scoreId1, scoreId2);
			}
			resizeScore(scoreId1);
			if(fix) {
				this.terms1[scoreId1] = terms;
				this.terms2[scoreId2] = terms;
			} else {
				this.terms1[scoreId1] += terms;
				this.terms2[scoreId2] += terms;
			}
		} else {
			throw new RuntimeException("DataFreezedException");
		}
	}
	
	private void resizeCategory(int categoryId) {
		if(this.items1.length <= categoryId) {
			int size = ArrayUtility.growup(categoryId + 1);
			this.items1 = Arrays.copyOf(this.items1, size);
			this.termsTotal1 = Arrays.copyOf(this.termsTotal1, size);
		}
		if(this.items2.length <= categoryId) {
			int size = ArrayUtility.growup(categoryId + 1);
			this.items2 = Arrays.copyOf(this.items2, size);
			this.termsTotal2 = Arrays.copyOf(this.termsTotal2, size);
		}
	}
	private void resizeScore(int scoreId) {
		if(this.terms1.length <= scoreId) {
			this.terms1 = Arrays.copyOf(this.terms1, ArrayUtility.growup(scoreId + 1));
		}
		if(this.terms2.length <= scoreId) {
			this.terms2 = Arrays.copyOf(this.terms2, ArrayUtility.growup(scoreId + 1));
		}
	}
	
	private int getScoreId1(int termId, int categoryId, int terms) {
		if(this.termCategoryList1.length <= termId) {
			this.termCategoryList1 = Arrays.copyOf(this.termCategoryList1, ArrayUtility.growup(termId + 1));
		}
		if(this.termCategoryList1[termId] == null) {
			this.termCategoryList1[termId] = new HashSet<Integer>();
		}
		termCategoryList1[termId].add(categoryId);
		return categoryDataSet1.put(termId, categoryId);
	}
	private int getScoreId2(int termId, int categoryId, int terms) {
		if(this.termCategoryList2.length <= termId) {
			this.termCategoryList2 = Arrays.copyOf(this.termCategoryList2, ArrayUtility.growup(termId + 1));
		}
		if(this.termCategoryList2[termId] == null) {
			this.termCategoryList2[termId] = new HashSet<Integer>();
		}
		termCategoryList2[termId].add(categoryId);
		return categoryDataSet2.put(termId, categoryId);
	}
	
	public void adjust(int categoryId1, int categoryId2, int termsTotal, int items, int itemsTotal) {
		adjust(categoryId1, categoryId2, termsTotal, items, itemsTotal, false);
	}
	
	public void adjust(int categoryId1, int categoryId2, int termsTotal, int items, int itemsTotal, boolean fix) {
		if(!freezed) {
			resizeCategory(categoryId1);
			resizeCategory(categoryId2);
			if(fix) {
				this.termsTotal1[categoryId1] = termsTotal;
				this.items1[categoryId1] = items;
				
				this.termsTotal2[categoryId2] = termsTotal;
				this.items2[categoryId2] = items;
				this.itemsTotal = itemsTotal;
			} else {
				this.termsTotal1[categoryId1] += termsTotal;
				this.items1[categoryId1] += items;
				
				this.termsTotal2[categoryId2] += termsTotal;
				this.items2[categoryId2] += items;
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
			for (int inx = 0; inx < items1.length; inx++) {
				//add bias value
				termsTotal1[inx] = -Math.log(termsTotal1[inx] + 1);
				termsTotal2[inx] = -Math.log(termsTotal2[inx] + 1);
				items1[inx] = -Math.log(items1[inx]);
				items2[inx] = -Math.log(items2[inx]);
			}
			itemsTotal = -Math.log(itemsTotal);
			for (int inx = 0; inx < terms1.length; inx++) {
				terms1[inx] = -Math.log(terms1[inx]);
				terms2[inx] = -Math.log(terms2[inx]);
			}
			freezed = true;
			logger.debug("DATA FREEZED");
		}
	}
	
	public void unfreeze() {
		if (freezed) {
			for (int inx = 0; inx < items1.length; inx++) {
				//add bias value
				termsTotal1[inx] = Math.pow(Math.E, -termsTotal1[inx]) - 1;
				termsTotal2[inx] = Math.pow(Math.E, -termsTotal2[inx]) - 1;
				items1[inx] = Math.pow(Math.E, -items1[inx]);
				items2[inx] = Math.pow(Math.E, -items2[inx]);
			}
			itemsTotal = Math.pow(Math.E, -itemsTotal);
			for (int inx = 0; inx < terms1.length; inx++) {
				terms1[inx] = Math.pow(Math.E, -terms1[inx]);
				terms2[inx] = Math.pow(Math.E, -terms2[inx]);
			}
			freezed = false;
			logger.debug("DATA UNFREEZED");
		}
	}
	
	public void classify(List<CharSequence> termList, int[] category1, int[] category2, double[] score1, double[] score2, JSONArray tracer) {
		List<Integer> termIdList = new ArrayList<Integer>();
		for (int inx = 0; inx < termList.size(); inx++) {
			termIdList.add(put(termList.get(inx)));
		}
		classifyById(termIdList, category1, category2, score1, score2, tracer);
	}
	
	public void classifyById(List<Integer> termIdList, int[] category1, int[] category2, double[] score1, double[] score2, JSONArray tracer) {
		
		if(!freezed) {
			freeze();
		}
		
		if(score1 == null) {
			score1 = new double[category1.length];
		}
		if(score2 == null) {
			score2 = new double[category2.length];
		}
		
		Arrays.fill(score1, Double.MAX_VALUE);
		Arrays.fill(score2, Double.MAX_VALUE);
		Arrays.fill(category1, -1);
		Arrays.fill(category2, -1);
		
		Set<Integer> categoryCheck1 = new HashSet<Integer>();
		Set<Integer> categoryCheck2 = new HashSet<Integer>();
		
		for (int tinx = 0; tinx < termIdList.size(); tinx++) {
			int termId = termIdList.get(tinx);
			if (termId >= 0 && termId < this.termCategoryList1.length && this.termCategoryList1[termId] != null) {
				categoryCheck1.addAll(this.termCategoryList1[termId]);
				categoryCheck2.addAll(this.termCategoryList2[termId]);
			}
		}
		
		if(logger.isTraceEnabled()) {
			int[] data = new int[2];
			for (int inx = 1; inx <= categoryDataSet1.dataCount(); inx++) {
				categoryDataSet1.get(inx, data );
				logger.trace("FINDING CATEDATA1[{}]={}:{} / {} / {}", inx, data[0], data[1], categoryDataSet1.getId(data[0], data[1]), terms1[inx]);
			}
			for (int inx = 1; inx <= categoryDataSet2.dataCount(); inx++) {
				categoryDataSet2.get(inx, data );
				logger.trace("FINDING CATEDATA2[{}]={}:{} / {} / {}", inx, data[0], data[1], categoryDataSet2.getId(data[0], data[1]), terms2[inx]);
			}
		}
		
		Iterator<Integer> iter = null;
		DoubleIntegerHashSet categoryDataSet = null;
		double[] items = null;
		double[] terms = null;
		double[] termsTotal = null;
		double[] score = null;
		int[] category = null;
		
		for (int iinx = 0; iinx < 2; iinx++) {
			if (iinx == 0) {
				iter = categoryCheck1.iterator();
				items = items1;
				terms = terms1;
				score = score1;
				category = category1;
				termsTotal = termsTotal1;
				categoryDataSet = categoryDataSet1;
			} else {
				iter = categoryCheck2.iterator();
				items = items2;
				terms = terms2;
				score = score2;
				category = category2;
				termsTotal = termsTotal2;
				categoryDataSet = categoryDataSet2;
			}
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
					}//for inx
				}
			}// iter
		}//for iinx
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized int readFrom(InputStream istream, byte[] buffer) throws IOException {
		int ret = 0;
		int termsLength = 0;
		int itemsLength = 0;
		int termCategoryListSize = 0;
		if (buffer == null || buffer.length < 512) { buffer = new byte[512]; }
		ret += super.readFrom(istream, buffer);
		ret += categoryDataSet1.readFrom(istream, buffer);
		ret += categoryDataSet2.readFrom(istream, buffer);
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER * 2);
		termsLength = ArrayUtility.restoreInteger(buffer, 0);
		itemsLength = ArrayUtility.restoreInteger(buffer, ArrayUtility.BYTES_INTEGER);
		this.terms1 = new double[termsLength];
		this.items1 = new double[itemsLength];
		this.termsTotal1 = new double[itemsLength];
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER * 2);
		termsLength = ArrayUtility.restoreInteger(buffer, 0);
		itemsLength = ArrayUtility.restoreInteger(buffer, ArrayUtility.BYTES_INTEGER);
		this.terms2 = new double[termsLength];
		this.items2 = new double[itemsLength];
		this.termsTotal2 = new double[itemsLength];
		ret += ArrayUtility.readInput(istream, buffer, this.terms1);
		ret += ArrayUtility.readInput(istream, buffer, this.termsTotal1);
		ret += ArrayUtility.readInput(istream, buffer, this.items1);
		ret += ArrayUtility.readInput(istream, buffer, this.terms2);
		ret += ArrayUtility.readInput(istream, buffer, this.termsTotal2);
		ret += ArrayUtility.readInput(istream, buffer, this.items2);
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_DOUBLE);
		this.itemsTotal = ArrayUtility.restoreDouble(buffer, 0);
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER);
		termCategoryListSize = ArrayUtility.restoreInteger(buffer, 0);
		this.termCategoryList1 = new Set[termCategoryListSize];
		ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER);
		termCategoryListSize = ArrayUtility.restoreInteger(buffer, 0);
		this.termCategoryList2 = new Set[termCategoryListSize];
		Set<Integer>[] termCategoryList = null;
		for (int iinx = 0; iinx < 2; iinx++) {
			if (iinx == 0) {
				termCategoryList = termCategoryList1;
			} else {
				termCategoryList = termCategoryList2;
			}
			for (int inx = 0; inx < termCategoryListSize; inx++) {
				ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER);
				int categorySize = ArrayUtility.restoreInteger(buffer, 0);
				if(categorySize > 0) {
					termCategoryList[inx] = new HashSet<Integer>();
					for (int inx2 = 0; inx2 < categorySize; inx2++) {
						ret += istream.read(buffer, 0, ArrayUtility.BYTES_INTEGER);
						int categoryId = ArrayUtility.restoreInteger(buffer, 0);
						termCategoryList[inx].add(categoryId);
					}
					if(logger.isTraceEnabled()) {
						logger.trace("READ-TERMCATE[{}]:{}", inx, termCategoryList[inx]);
					}
				}
			}
			if(logger.isTraceEnabled()) {
				DoubleIntegerHashSet categoryDataSet = null;
				double[] terms = null;
				if(iinx == 0) {
					categoryDataSet = categoryDataSet1;
					terms = terms1;
				} else {
					categoryDataSet = categoryDataSet1;
					terms = terms2;
				}
				for (int inx = 1; inx <= categoryDataSet.dataCount(); inx++) {
					int[] data = new int[2];
					categoryDataSet.get(inx, data );
					logger.trace("READ CATEDATA[{}]={}:{} / {} / {}", inx, data[0], data[1], categoryDataSet.getId(data[0], data[1]), terms[inx]);
				}
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
		categoryDataSet1.writeTo(ostream, buffer);
		categoryDataSet2.writeTo(ostream, buffer);
		ArrayUtility.mapInteger(terms1.length, buffer, 0);
		ArrayUtility.mapInteger(items1.length, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ostream.write(buffer, 0, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.mapInteger(terms2.length, buffer, (pos = 0));
		ArrayUtility.mapInteger(items2.length, buffer, pos += ArrayUtility.BYTES_INTEGER);
		ostream.write(buffer, 0, pos += ArrayUtility.BYTES_INTEGER);
		ArrayUtility.writeOutput(ostream, terms1, buffer);
		ArrayUtility.writeOutput(ostream, termsTotal1, buffer);
		ArrayUtility.writeOutput(ostream, items1, buffer);
		ArrayUtility.writeOutput(ostream, terms2, buffer);
		ArrayUtility.writeOutput(ostream, termsTotal2, buffer);
		ArrayUtility.writeOutput(ostream, items2, buffer);
		ArrayUtility.mapDouble(this.itemsTotal, buffer, 0);
		ostream.write(buffer, 0, ArrayUtility.BYTES_DOUBLE);
		ArrayUtility.mapInteger(termCategoryList1.length, buffer, 0);
		ostream.write(buffer, 0, ArrayUtility.BYTES_INTEGER);
		ArrayUtility.mapInteger(termCategoryList2.length, buffer, 0);
		ostream.write(buffer, 0, ArrayUtility.BYTES_INTEGER);
		Set<Integer>[] termCategoryList = null;
		for (int iinx = 0; iinx < 2; iinx++) {
			if (iinx == 0) {
				termCategoryList = termCategoryList1;
			} else {
				termCategoryList = termCategoryList2;
			}
			for (int inx = 0; inx < termCategoryList.length; inx++) {
				if(termCategoryList[inx] == null) {
					ArrayUtility.mapInteger(0, buffer, 0);
					ostream.write(buffer, 0, ArrayUtility.BYTES_INTEGER);
				} else {
					if(logger.isTraceEnabled()) {
						logger.trace("WRITE-TERMCATE[{}]:{}", inx, termCategoryList[inx]);
					}
					ArrayUtility.mapInteger(termCategoryList[inx].size(), buffer, 0);
					ostream.write(buffer, 0, ArrayUtility.BYTES_INTEGER);
					for(Integer item : termCategoryList[inx]) {
						ArrayUtility.mapInteger(item, buffer, 0);
						ostream.write(buffer, 0, ArrayUtility.BYTES_INTEGER);
					}
				}
			}
		}
		return streamLength();
	}

	@Override
	public synchronized int streamLength() {
		int ret = super.streamLength();
		ret += categoryDataSet1.streamLength();
		ret += ArrayUtility.BYTES_DOUBLE * terms1.length;
		ret += ArrayUtility.BYTES_DOUBLE * termsTotal1.length;
		ret += ArrayUtility.BYTES_DOUBLE * items1.length;
		ret += categoryDataSet2.streamLength();
		ret += ArrayUtility.BYTES_DOUBLE * terms2.length;
		ret += ArrayUtility.BYTES_DOUBLE * termsTotal2.length;
		ret += ArrayUtility.BYTES_DOUBLE * items2.length;
		ret += ArrayUtility.BYTES_DOUBLE;
		return ret;
	}
}