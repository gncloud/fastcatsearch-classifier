package org.fastcatsearch.common.ds;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.fastcatsearch.classifier.core.DoubleClassifierTable;
import org.fastcatsearch.common.data.structure.ArrayUtility;
import org.fastcatsearch.common.data.structure.BasicStringHashSet;
import org.fastcatsearch.common.data.structure.DataIterableHashSet;
import org.fastcatsearch.common.data.structure.DoubleIntegerHashSet;
import org.fastcatsearch.common.data.structure.Sorter;
import org.fastcatsearch.common.data.structure.StreamSort;
import org.fastcatsearch.common.data.structure.Sorter.SortType;
import org.fastcatsearch.common.data.type.Clonable;
import org.fastcatsearch.common.data.type.ComparableKeyValueListSortable;
import org.fastcatsearch.common.data.type.Copyable;
import org.fastcatsearch.common.data.type.IntKeyValueArraySortable;
import org.fastcatsearch.common.data.type.Streamable;
import org.fastcatsearch.common.data.type.StreamableIterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class LearningTableTest {
	
	private static final Logger logger = LoggerFactory.getLogger(LearningTableTest.class);
	
	@Before
	public void init() {
		String LOG_LEVEL = System.getProperty("LOG_LEVEL");
		
		if (LOG_LEVEL == null || "".equals(LOG_LEVEL)) {
			LOG_LEVEL = "DEBUG";
		}
		
		((ch.qos.logback.classic.Logger)
			LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)
			).setLevel(Level.toLevel("DEBUG"));
		
		((ch.qos.logback.classic.Logger)
			LoggerFactory.getLogger(BasicStringHashSet.class)
			).setLevel(Level.toLevel(LOG_LEVEL));
		
		((ch.qos.logback.classic.Logger)
			LoggerFactory.getLogger(DoubleIntegerHashSet.class)
			).setLevel(Level.toLevel(LOG_LEVEL));
	}

//	@Test
//	public void test() {
//		LearningTable tbl = new LearningTable();
//		tbl.init(10, 9999);
//		
//		String key1 = "한글";
//		String key2 = "테스트";
//		
//		int id1 = tbl.put(key1);
//		int id2 = tbl.put(key2);
//		logger.debug("ID:{} / {}", id1, id2);
//		logger.debug("GET1:[{}] / GET2:[{}]", tbl.get(id1), tbl.get(id2));
//		logger.debug("{}:{} / {}:{}", key1.hashCode(), tbl.get(id1).hashCode(), key2.hashCode(), tbl.get(id2).hashCode());
//		logger.debug("{} / {}", tbl.get(id1).equals(key1), tbl.get(id2).equals(key2));
//		logger.debug("{} / {}", key1.equals(tbl.get(id1)), key2.equals(tbl.get(id2)));
//		
//		assertEquals(tbl.get(id1), key1);
//		assertEquals(tbl.get(id2), key2);
//	}
	
	@Test
	public void testSimpleHashSet() throws IOException {
		BasicStringHashSet set = new BasicStringHashSet(10);
		set.put("test1");
		set.put("test2");
		set.put("test3");
		set.put("test1");
		
		
		logger.debug("TEST:{}", set.get(1));
		logger.debug("TEST:{}", set.get(2));
		logger.debug("TEST:{}", set.get(3));
		logger.debug("TEST:{}", set.get(4));
		
		BasicStringHashSet set2 = set.cloneOf();
		
		logger.debug("ID:{}", set2.getId("test1"));
		logger.debug("ID:{}", set2.getId("test2"));
		logger.debug("ID:{}", set2.getId("test3"));
		logger.debug("ID:{}", set2.getId("test4"));
		
		
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		set2.writeTo(ostream, new byte[1024]);
		byte[] buf = ostream.toByteArray();
		
		ByteArrayInputStream istream = new ByteArrayInputStream(buf);
		
		BasicStringHashSet set3 = new BasicStringHashSet();
		
		set3.readFrom(istream, new byte[1024]);
		logger.debug("================================================================================");
		logger.debug("TEST:{}", set3.get(1));
		logger.debug("TEST:{}", set3.get(2));
		logger.debug("TEST:{}", set3.get(3));
		logger.debug("TEST:{}", set3.get(4));
		logger.debug("ID:{}", set3.getId("test1"));
		logger.debug("ID:{}", set3.getId("test2"));
		logger.debug("ID:{}", set3.getId("test3"));
		logger.debug("ID:{}", set3.getId("test4"));
	}

	@Test
	public void testDoubleIntHashSet() throws IOException {
		DoubleIntegerHashSet hash = new DoubleIntegerHashSet();
		hash.put(100, 100);
		hash.put(200, 100);
		hash.put(100, 300);
		hash.put(500, 100);
		hash.put(800, 100);
		hash.put(200, 10);
		hash.put(20, 10);
		hash.put(150, 150);
		hash.put(250, 180);
		hash.put(80, 280);
		hash.put(90, 290);
		
		int[] v = new int[2];
		logger.debug("--------------------------------------------------------------------------------");
		
		for(int inx=1;inx<=hash.dataCount();inx++) {
			hash.get(inx, v);
			logger.debug("V1:{} V2:{} / {}", v[0], v[1], hash.getId(v[0], v[1]));
		}
		
		logger.debug("--------------------------------------------------------------------------------");
		
		File f = File.createTempFile("test", "bin");
		OutputStream ostream = new FileOutputStream(f);
		hash.writeTo(ostream, null);
		ostream.close();
		
		
		DoubleIntegerHashSet hash2 = new DoubleIntegerHashSet();
		InputStream istream = new FileInputStream(f);
		hash2.readFrom(istream, null);
		istream.close();
		
		f.delete();
		
		logger.debug("================================================================================");
		logger.debug("GET-ID 100,100 = {}", hash2.getId(100, 100));
		logger.debug("GET-ID 100,300 = {}", hash2.getId(100, 300));
		
		for(int inx=1;inx<=hash2.dataCount();inx++) {
			hash2.get(inx, v);
			logger.debug("V1:{} V2:{} / {}", v[0], v[1], hash2.getId(v[0], v[1]));
		}
	}
	
	@Test
	public void testByteArrayUtil() {
		byte[] buf = new byte[1024];
		
		ArrayUtility.mapChar('한', buf, 0);
		logger.debug("RESTORE:{}", ArrayUtility.restoreChar(buf, 0));
		
		ArrayUtility.mapInteger(49996267, buf, 0);
		logger.debug("RESTORE:{}", ArrayUtility.restoreInteger(buf, 0));
	}
	
	@Test
	public void testIntBufferMap() throws Exception {
		int[] idata = new int[100];
		
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		ostream.write("안녕하세요".getBytes());
		
		byte[] bdata = ostream.toByteArray();
		
		ArrayUtility.toIntBuffer(bdata, 0, idata, 0, bdata.length);
		
		Arrays.fill(bdata, (byte)0);
		
		ArrayUtility.fromIntBuffer(idata, 0, bdata, 0, bdata.length);
		
		logger.debug("RESTORE:/{}/", new String(bdata));
	}
	
	
	@Test
	public void testDataIterableHashSet() throws Exception {
		DataIterableHashSet table = new DataIterableHashSet(100, 100);
		table.store("test1", new TestItem(1));
		table.store("test2", new TestItem(2));
		table.store("test1", new TestItem(3));
		table.store("test1", new TestItem(4));
		table.store("test1", new TestItem(5));
		table.store("test2", new TestItem(6));
		table.store("test2", new TestItem(7));
		table.store("test2", new TestItem(8));
		table.store("test2", new TestItem(9));
		
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		table.writeTo(ostream, new byte[1024]);
		
		byte[] buffer = ostream.toByteArray();
		
		ByteArrayInputStream istream = new ByteArrayInputStream(buffer);
		
		DataIterableHashSet table2 = new DataIterableHashSet(100, 100);
		table2.readFrom(istream, new byte[1024]);
		
		DataIterableHashSet table3 = table2.cloneOf();
		
		
		Iterator<TestItem> iter = null;
		
		iter = table3.find("test1", TestItem.class);
		
		for (; iter.hasNext();) {
			TestItem item = iter.next();
			logger.debug("ITEM:{}", item);
		}
		
		iter = table3.find("test2", TestItem.class);
		
		for (; iter.hasNext();) {
			TestItem item = iter.next();
			logger.debug("ITEM:{}", item);
		}
		
	}
	
	public static class TestItem implements Streamable, Comparable<TestItem>, Copyable<TestItem>, Clonable<TestItem> {
		int item;
		byte[] buf;
		
		public TestItem() {
			buf = new byte[4];
		}
		
		public TestItem(int item) {
			this();
			this.item = item;
		}

		@Override
		public int readFrom(InputStream istream, byte[] buffer) throws IOException {
			int ret = 0;
			synchronized(buf) {
				ret += istream.read(buf, 0, ArrayUtility.BYTES_INTEGER);
				item = ArrayUtility.restoreInteger(buf, 0);
			}
			return ret;
		}

		@Override
		public int writeTo(OutputStream ostream, byte[] buffer) throws IOException {
			synchronized(buf) {
				ArrayUtility.mapInteger(item, buf, 0);
				ostream.write(buf, 0, ArrayUtility.BYTES_INTEGER);
			}
			return streamLength();
		}

		@Override
		public int streamLength() {
			return ArrayUtility.BYTES_INTEGER;
		}
		
		@Override
		public String toString() {
			return String.valueOf(item);
		}
		
		@Override 
		public TestItem clone () {
			return new TestItem(this.item);
		}

		@Override
		public int compareTo(TestItem o) {
			if(this.item > o.item) {
				return 1;
			} else if(this.item < o.item) {
				return -1;
			}
			return 0;
		}

		@Override
		public TestItem copy() {
			return clone();
		}

		@Override
		public TestItem cloneOf() {
			TestItem ret = new TestItem();
			ret.item = this.item;
			return ret;
		}

		@Override
		public void cloneTo(TestItem instance) {
			instance.item = item;
		}
	}
	
	@Test
	public void sortTest() {
		
		String[] strs = new String[] {
			"aaaa",
			"bbbb",
			"cccc",
			"dddd"
		};
		
		List<String> stra = Arrays.asList(strs);
		
		ComparableKeyValueListSortable<String> sortable = new ComparableKeyValueListSortable<String>(stra, stra, strs.length);
		
		Sorter sort = Sorter.getInstance(SortType.HeapSort);
		sort.init(sortable, false);
		sort.sort();
		
		for (int inx = 0; inx < strs.length; inx++) {
			logger.debug("[{}] = {}", inx, strs[inx]);
		}
	}
	
	@Test
	public void sortTest2() {
		int[] data = {
			1084529398,
			1646668452,
			719623270,
			1454585595,
			-614861839,
			-1069603783,
			137640549,
			-1579701636,
			1403250480,
			-34247148
		};
		
		IntKeyValueArraySortable sortable = new IntKeyValueArraySortable(data, null, data.length);
		
		Sorter sort = null;
		
		sort = Sorter.getInstance(SortType.HeapSort);
		sort.init(sortable, true);
		sort.sort();
		for(int inx=0;inx<data.length;inx++) {
			logger.debug("DATA[{}] = {}", data[inx]);
		}
	}
	
//	@Test 
//	public void shiftTest() throws Exception {
//		int[] data = { 10, 5, 6, 7, 8, 9, 11, 12, 13, };
//		//data = new int[] { 5, 6, 7, 8, 9, 11, 12, 13, 10 };
//		
//		IntKeyValueArraySortable sortable = new IntKeyValueArraySortable(data, null, data.length);
//		
//		//sortable.moveShift(8, 5);
//		
//		Sorter sorter = Sorter.getInstance(SortType.HeadInsertOnly);
//		sorter.init(sortable,  true);
//		sorter.sort();
//		
//		for(int inx=0;inx<data.length;inx++) {
//			logger.debug("[{}]:{}", inx, data[inx]);
//		}
//		
//		String[] str = { "i", "d", "e", "f", "g", "h", "j", "k", "l" };
////		str = new String[] { "d", "e", "f", "g", "h", "j", "k", "l", "i" };
//		
//		List data2 = new ArrayList();
//		data2.addAll(Arrays.asList(str));
//		ComparableKeyValueListSortable sortable2 = new ComparableKeyValueListSortable(data2, null, data2.size());
//		
//		//sortable2.moveShift(0, 5);
//		
//		
//		sorter.init(sortable2, true);
//		sorter.sort();
//		
//		for(int inx=0;inx<data2.size();inx++) {
//			logger.debug("[{}]:{}", inx, data2.get(inx));
//		}
//		
//	}
	
//	@Test
//	public void headInsertTest() throws Exception {
//		int[] key = new int[10];
//		
//		Random r = new Random();
//		
//		for (int count = 0; count < 100000; count++) {
//			
//			for (int inx = 1; inx < key.length; inx++) {
//				int k = r.nextInt(100);
//				if(k < 0) { k *= 1; }
//				key[inx] = key[inx - 1] + k;
//			}
//			
//			int k = r.nextInt(1000);
//			if(k < 0) { k *=1; };
//			key[0] = k;
//			
//			//for (int inx = 0; inx < key.length; inx++) {
//			//	logger.debug("key[{}] = {}", inx, key[inx]);
//			//}
//			
//			IntKeyValueArraySortable sortable = new IntKeyValueArraySortable(key, null, key.length);
//			
//			Sorter sorter = Sorter.getInstance(SortType.HeadInsertOnly);
//			sorter.init(sortable,  true);
//			sorter.sort();
//			
//			logger.debug("================================================================================");
//			
//			int prev = 0;
//			
//			for (int inx = 0; inx < key.length; inx++) {
//				if(inx == 0) { prev = key[inx]; }
//				if( prev > key[inx]) {
//					logger.debug("SORTING ERROR! {} / {}", prev, key[inx]);
//				}
//				logger.debug("key[{}] = {}", inx, key[inx]);
//				prev = key[inx];
//			}
//		}
//	}
	
	@Test
	public void streamSortTest() throws IOException {
		
		boolean isAscending = false;
		
		byte[] buf = new byte[1024];
		TestItem item = new TestItem();
		StreamSort<TestItem> sort = new StreamSort<TestItem>(item, 100000, 10);
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		Random r = new Random();
		int cnt = 1000000;
		for (int inx = 0; inx < cnt; inx++) {
			item.item = r.nextInt();
			item.writeTo(ostream, buf);
		}
		
		byte[] data = ostream.toByteArray();
		
		InputStream istream = new ByteArrayInputStream(data);
		Iterator<TestItem> iter = new StreamableIterator<TestItem>(item, istream, data.length, buf);
		
		long time = System.currentTimeMillis();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		sort.sort(iter, out, isAscending);
		out.close();
		istream.close();
		
		data = out.toByteArray();
		
		istream = new ByteArrayInputStream(data);
		iter = new StreamableIterator<TestItem>(item, istream, data.length, buf);
		
		int prev = 0;
		int inx = 0;
		for (inx = 0; iter.hasNext(); inx++) {			
			TestItem v = iter.next();
			if(inx == 0) {
				prev = v.item;
			}
			if ((isAscending && prev > v.item) || (!isAscending && prev < v.item)) {
				logger.debug("SORTING ERROR! {} / {}", prev, v.item);
			}
			//logger.debug("DATA[{}]={}", inx, v);
		}
		istream.close();
		
		time = System.currentTimeMillis() - time;
		
		logger.debug("COUNT:{} / TIME:{}", inx, time);
	}
	
	@Test
	public void testIODouble() throws Exception {
		byte[] buf = new byte[1024];
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		double[] data = { 0.54D, 0.11D, 0.14D };
		
		ArrayUtility.writeOutput(ostream, data, buf);
		
		byte[] wdata = ostream.toByteArray();
		ostream.close();
		
		Arrays.fill(buf, (byte)0);
		Arrays.fill(data, 0D);
		
		InputStream istream = new ByteArrayInputStream(wdata);
		ArrayUtility.readInput(istream, buf, data);
		istream.close();
		
		for (int inx = 0; inx < data.length; inx++) {
			logger.debug("DATA[{}] = {}", inx, data[inx]);
		}
	}
	
	
	@Test
	public void testLearnClassify() throws IOException {
		DoubleClassifierTable ltb = new DoubleClassifierTable(100, 500, 20);
		
		ltb.learn("WIFI",   1, 4, 3, false);
		ltb.learn("CPU",    1, 4, 2, false);
		ltb.learn("IPAD",   1, 4, 5, false);
		ltb.adjust(1, 4, 10, 30, 180, false);
		
		ltb.learn("WIFI",   2, 5, 2, false);
		ltb.learn("HDD",    2, 5, 3, false);
		ltb.learn("SVIEW",  2, 5, 1, false);
		ltb.learn("USB",    2, 5, 5, false);
		ltb.adjust(2, 5, 11, 100, 180, false);
		
		ltb.learn("HDD",    3, 6, 3, false);
		ltb.learn("USB",    3, 6, 1, false);
		ltb.learn("CPU",    3, 6, 5, false);
		ltb.adjust(3, 6, 9, 50, 180, false);
		
		ltb.freeze();
		
		byte[] buffer = new byte[1024];
		
		
		File file = File.createTempFile("classify", ".ltb");
		logger.debug("FILE:{}", file);
		OutputStream ostream = new FileOutputStream(file);
		ltb.writeTo(ostream, buffer);
		ostream.close();
		
		DoubleClassifierTable ltb2 = new DoubleClassifierTable(100, 500, 20);		
		InputStream istream = new FileInputStream(file);
		ltb2.readFrom(istream, buffer);
		istream.close();
		file.delete();
		
		//ltb2 = ltb;
		
		for (int inx = 1; inx <= ltb2.dataCount(); inx++) {
			logger.debug("DATA[{}]:{} / {}", inx, ltb2.get(inx), ltb2.getId(ltb2.get(inx)));
		}
		
		
		int[] category1 = new int[3];
		double[] scoreTable1 = new double[3];
		int[] category2 = new int[3];
		double[] scoreTable2 = new double[3];
		List<CharSequence> termList = null;
		logger.debug("================================================================================");
		{
			termList = Arrays.asList(new CharSequence[] { "WIFI", "CPU", "IPAD" });
			ltb2.classify(termList, category1, category2, scoreTable1, scoreTable2, null);
			for (int inx = 0; inx < category1.length; inx++) {
				logger.debug("   CLASSIFIED[{}] = {},{} / {},{}", inx, category1[inx], category2[inx], scoreTable1[inx], scoreTable2[inx]);
			}
		}
		logger.debug("================================================================================");
		{
			termList = Arrays.asList(new CharSequence[] { "HDD", "USB", "IPAD" });
			ltb2.classify(termList, category1, category2, scoreTable1, scoreTable2, null);
			for (int inx = 0; inx < category1.length; inx++) {
				logger.debug("   CLASSIFIED[{}] = {},{} / {},{}", inx, category1[inx], category2[inx], scoreTable1[inx], scoreTable2[inx]);
			}
		}
		logger.debug("================================================================================");
		{
			termList = Arrays.asList(new CharSequence[] { "HDD", "USB", "CPU", "IPAD" });
			ltb2.classify(termList, category1, category2, scoreTable1, scoreTable2, null);
			for (int inx = 0; inx < category1.length; inx++) {
				logger.debug("   CLASSIFIED[{}] = {},{} / {},{}", inx, category1[inx], category2[inx], scoreTable1[inx], scoreTable2[inx]);
			}
		}
		logger.debug("================================================================================");
		{
			termList = Arrays.asList(new CharSequence[] { "SAMSUNG", "USB", "CPU", "GALAXY" });
			ltb2.classify(termList, category1, category2, scoreTable1, scoreTable2, null);
			for (int inx = 0; inx < category1.length; inx++) {
				logger.debug("   CLASSIFIED[{}] = {},{} / {},{}", inx, category1[inx], category2[inx], scoreTable1[inx], scoreTable2[inx]);
			}
		}
	}
	
//	@Test
//	public void testAnalyzer() throws Exception {
//		//System.setProperty("lucene.analysis.dictionary.config.korean", "/Users/lupfeliz/TEST_HOME/korean/config.prop");
//		String pText = "아름다운이땅에금수강산에단군할아버지가터잡으시고홍익인간뜻으로나라세우니대대손손훌륭한인물도많아고구려세운동명왕백제온조왕알에서나온혁거세만주벌판달려라광개토대왕신라장군이사부";
//		
//		pText = "대기업 甲질에 고통받는 하청업체 직원들";
//		
//		KoreanAnalyzer analyzer = new KoreanAnalyzer();
//		analyzer.setHasOrigin(true);
//		analyzer.setOriginCNoun(true);
//		analyzer.setBigrammable(true);
//		analyzer.setQueryMode(true);
//		Reader reader = new StringReader(pText);
//		TokenStream tstream = analyzer.tokenStream("", reader);
//		tstream.reset();
//		
//		CharTermAttribute term = tstream.getAttribute(CharTermAttribute.class);
//		while(tstream.incrementToken()) {
//			logger.debug("ADD-TERM:{}", term);
//		}
//		analyzer.close();
//	}
	
//	@Test
//	public void getInstanceAndLearn() throws Exception {
//		Pattern ptnCrnc = Pattern.compile("^[0-9,일이삼사오육륙칠팔구십백천만억 ]+원[ ]*((으로)|(짜리)|(어치)|(까지)|(까지도)|([권만이인과에을은의씩도]+)){0,1}$");
//		Pattern ptnYear = Pattern.compile("^[0-9,일이삼사오육륙칠팔구십백천만억 ]+년[ ]*((으로)|(이나)|(부터)|(전에)|([경전과째간이에여의]+)){0,1}$");
//		Pattern ptnDate = Pattern.compile("^[0-9,일이삼사오육륙칠팔구십백천만억 ]+일[ ]*((으로)|(이나)|(부터)|(전에)|([경전과째간이에여의]+)){0,1}$");
//		Pattern ptnNumb = Pattern.compile("^[0-9,. -]+$");
//		Pattern ptnPoin = Pattern.compile("^[a-zA-Z ]+[씨는가의에게]{0,2}$");
//		
//		DoubleClassifierTable table = new DoubleClassifierTable(1280, 4096, 100);
//		
//		BasicStringHashSet categorySet = new BasicStringHashSet(100, 200);
//		
//		Map<String, Map<String, AtomicInteger>> termCateMap = new HashMap<String, Map<String, AtomicInteger>>();
//		Set<String> cateSet = new HashSet<String>();
//		URL url = new URL("http://app1.lawkick.co.kr:9981/lawkick/home/view-instance_!!_0_9999.json");
//		BufferedReader reader = null;
//		Writer writer = null;
//		try {
//			StringBuilder sb = new StringBuilder();
//			reader = new BufferedReader(new InputStreamReader(url.openStream()));
//			for (String rline = null; (rline = reader.readLine()) != null;) {
//				sb.append(rline).append("\n");
//			}
//			
//			logger.trace("STR:{}", sb);
//			
//			JSONObject obj = new JSONObject(sb.toString());
//			
//			KoreanAnalyzer analyzer = null;
//			Reader qreader = null;
//			TokenStream tstream = null;
//			CharTermAttribute term = null;
//			
//			JSONArray list = obj.optJSONArray("list");
//			for (int qinx = 0; qinx < list.length(); qinx++) {
//				JSONObject item = list.optJSONObject(qinx);
//				String question = item.optString("question", "");
//				question = item.optString("title", "") + " " + question;
//				String cate = item.optString("cate","");
//				
//				cateSet.add(cate);
//				
//				int categoryId = categorySet.put(cate);
//				int cntWord = 0;
//				
//				question = question.replaceAll("[&]quot[;]", "\"");
//				question = question.replaceAll("[&]middot[;]", ".");
//				
//				analyzer = new KoreanAnalyzer();
//				analyzer.setHasOrigin(true);
//				analyzer.setOriginCNoun(true);
//				analyzer.setBigrammable(true);
//				analyzer.setQueryMode(true);
//				
//				qreader = new StringReader(question);
//				tstream = analyzer.tokenStream("", qreader);
//				tstream.reset();
//				
//				term = tstream.getAttribute(CharTermAttribute.class);
//				logger.trace("================================================================================");
//				logger.trace("[{}]:QUESTION:{}", cate, question);
//				
//				List<String> termList = new ArrayList<String>();
//				
//				for (int tinx = 0; tstream.incrementToken(); tinx++) {
//					String termStr = term.toString();
//					termList.add(termStr);
//				}
//				for (int tinx = 0; tinx < (termList.size() - 1); tinx++) {
//					for (int tinx2 = tinx + 1; tinx2 < termList.size(); tinx2++) {
//						String termStr1 = termList.get(tinx);
//						String termStr2 = termList.get(tinx2);
//						if( termStr1.equals(termStr2+"은") || termStr1.equals(termStr2+"는") || termStr1.equals(termStr2+"로")
//						|| termStr1.equals(termStr2+"도") || termStr1.equals(termStr2+"에") || termStr1.equals(termStr2+"과")
//						|| termStr1.equals(termStr2+"이") || termStr1.equals(termStr2+"가") || termStr1.equals(termStr2+"를")
//						|| termStr1.equals(termStr2+"에서") || termStr1.equals(termStr2+"까지")
//						) {
//							termList.remove(tinx2);
//							tinx2--;
//							break;
//						}
//					}
//				}
//				
//				for (int tinx = 0; tinx < termList.size(); tinx++) {
//					String termStr = termList.get(tinx);
//					int termLen = termStr.length();
//					
//					Matcher mat = null;
//					
//					if (termStr != null) {
//						mat = ptnCrnc.matcher(termStr);
//						if(mat.find()) { termStr = null; }
//					}
//					if (termStr != null) {
//						mat = ptnYear.matcher(termStr);
//						if(mat.find()) { termStr = null; }
//					}
//					if (termStr != null) {
//						mat = ptnDate.matcher(termStr);
//						if(mat.find()) { termStr = null; }
//					}
//					if (termStr != null) {
//						mat = ptnNumb.matcher(termStr);
//						if(mat.find()) { termStr = null; }
//					}
//					if (termStr != null) {
//						mat = ptnPoin.matcher(termStr);
//						if(mat.find()) { termStr = null; }
//					}
//					
//					if(termStr != null) {
//						if(termStr.endsWith("았습니다") ||termStr.endsWith("었습니다")	) {
//							if(termLen > 4) {
//								 termStr = termStr.substring(0, termStr.length() - 4) + "다";
//							}
//						} else if( termStr.endsWith("입니다") || termStr.endsWith("에게는") || termStr.endsWith("에서는")
//							|| termStr.endsWith("이라고") || termStr.endsWith("인가요")
//							) {
//							if(termLen > 3) {
//								 termStr = termStr.substring(0, termStr.length() - 3);
//							} else {
//								termStr = null;
//							}
//						} else if( termStr.endsWith("이라고만")) {
//							if(termLen > 4) {
//								 termStr = termStr.substring(0, termStr.length() - 4);
//							} else {
//								termStr = null;
//							}
//						} else if(termStr.endsWith("인데")) {
//							if(termLen > 2) {
//								 termStr = termStr.substring(0, termStr.length() - 2);
//							} else {
//								termStr = null;
//							}
//						} else if(termStr.endsWith("나요")) {
//							if(termLen > 3) {
//								 termStr = termStr.substring(0, termStr.length() - 3)+"다";
//							} else {
//								termStr = null;
//							}
//						} else if(termStr.endsWith("었는데")) {
//							if(termLen > 3) {
//								 termStr = termStr.substring(0, termStr.length() - 3)+"다";
//							} else {
//								termStr = null;
//							}
//						} else if(termStr.endsWith("하였고") || termStr.endsWith("하다가") || termStr.endsWith("했는데") || termStr.endsWith("합니다")
//							|| termStr.endsWith("한가요") || termStr.endsWith("하는데") || termStr.endsWith("하는가") || termStr.endsWith("하는지")
//							|| termStr.endsWith("하라는") || termStr.endsWith("해달라") || termStr.endsWith("하다며") || termStr.endsWith("하고는")
//							|| termStr.endsWith("하고자") || termStr.endsWith("해서만") || termStr.endsWith("하면서") || termStr.endsWith("해서는")
//							|| termStr.endsWith("할까요") || termStr.endsWith("한지요") || termStr.endsWith("하여야") || termStr.endsWith("하였음")
//							|| termStr.endsWith("하려고") || termStr.endsWith("하기로") || termStr.endsWith("하지는") || termStr.endsWith("하겠다")
//							|| termStr.endsWith("하기도") || termStr.endsWith("하기만") || termStr.endsWith("하기에") || termStr.endsWith("하다고")
//							|| termStr.endsWith("한다면") || termStr.endsWith("한다고") || termStr.endsWith("한다는") || termStr.endsWith("하게도")
//							|| termStr.endsWith("하셨다") || termStr.endsWith("한다는") || termStr.endsWith("하지도") || termStr.endsWith("하도록")
//							|| termStr.endsWith("하다고") || termStr.endsWith("했더니") || termStr.endsWith("했다가") || termStr.endsWith("했다는")
//							|| termStr.endsWith("했다며") || termStr.endsWith("했다고") || termStr.endsWith("했으나") || termStr.endsWith("하지요")
//							|| termStr.endsWith("하려면") || termStr.endsWith("되면서") || termStr.endsWith("되었다") || termStr.endsWith("되어야")
//							|| termStr.endsWith("되었고") || termStr.endsWith("되는데")
//							) {
//							if(termLen > 3) {
//								 termStr = termStr.substring(0, termStr.length() - 3)+"하다";
//							} else {
//								termStr = null;
//							}
//						} else if( termStr.endsWith("해야") || termStr.endsWith("하기") || termStr.endsWith("하여") || termStr.endsWith("하고")
//							|| termStr.endsWith("했고") || termStr.endsWith("하는") || termStr.endsWith("해서") || termStr.endsWith("할까")
//							|| termStr.endsWith("하며") || termStr.endsWith("하면") || termStr.endsWith("하던") || termStr.endsWith("하려")
//							|| termStr.endsWith("하게") || termStr.endsWith("하지") || termStr.endsWith("하자") || termStr.endsWith("해도")
//							|| termStr.endsWith("했던") || termStr.endsWith("되어") || termStr.endsWith("되다") || termStr.endsWith("되면")
//							|| termStr.endsWith("되는") || termStr.endsWith("되고") || termStr.endsWith("되지") || termStr.endsWith("되자")
//							|| termStr.endsWith("되며") || termStr.endsWith("됐다") || termStr.endsWith("받고")
//							) {
//							if(termLen > 2) {
//								 termStr = termStr.substring(0, termStr.length() - 2)+"하다";
//							} else {
//								if( !"하자".equals(termStr) && !"하지".equals(termStr)) {
//									termStr = null;
//								}
//							}
//						} else if (termStr.endsWith("하였으나") || termStr.endsWith("하는가요") || termStr.endsWith("하였는데")
//							|| termStr.endsWith("하였다는") || termStr.endsWith("하였다고") || termStr.endsWith("하였다가")
//							|| termStr.endsWith("하겠다고") || termStr.endsWith("한다든지") || termStr.endsWith("해달라고")
//							|| termStr.endsWith("하기까지") || termStr.endsWith("하였으며") || termStr.endsWith("하였는바")
//							) {
//							if(termLen > 4) {
//								 termStr = termStr.substring(0, termStr.length() - 4)+"하다";
//							} else {
//								termStr = null;
//							}
//						} else if (termStr.endsWith("하였음에도")) {
//							if(termLen > 5) {
//								 termStr = termStr.substring(0, termStr.length() - 5)+"하다";
//							} else {
//								termStr = null;
//							}
//						} else if(termStr.endsWith("더라도")) {
//							if(termStr.length() > 4) {
//								if(termStr.charAt(termStr.length() - 4) == '았') {
//									termStr = termStr.substring(0, termStr.length() - 4) + "다";
//								} else {
//									termStr = termStr.substring(0, termStr.length() - 3) + "다";
//								}
//							} else {
//								termStr = termStr.substring(0, termStr.length() - 3) + "다";
//							}
//						} else if(termStr.endsWith("습니다")) {
//							if(termStr.length() > 4) {
//								if(termStr.charAt(termStr.length() - 4) == '였') {
//									termStr = termStr.substring(0, termStr.length() - 4) + "다";
//								} else if(termStr.charAt(termStr.length() - 4) == '했') {
//									termStr = termStr.substring(0, termStr.length() - 4) + "하다";
//								} else {
//									termStr = termStr.substring(0, termStr.length() - 3) + "다";
//								}
//							} else {
//								termStr = termStr.substring(0, termStr.length() - 3) + "다";
//							}
//						} else if(termStr.endsWith("는지요") || termStr.endsWith("하나요") || termStr.startsWith("있")) {
//							termStr = null;
//						}
//					}
//					
//					if(termStr != null) {
//					
//						Map<String, AtomicInteger> cateMap = null;
//						if(termCateMap.containsKey(termStr)) {
//							cateMap = termCateMap.get(termStr);
//						} else {
//							cateMap = new HashMap<String, AtomicInteger>();
//							termCateMap.put(termStr, cateMap);
//						}
//						table.learn(termStr, categoryId, categoryId, 1);
////if("형사소송법".equals(cate)) {
////	int termId = table.put(termStr);
////	logger.debug("TERM:{} / {}", termStr, table.categoryDataSet.getId(termId, categoryId));
////}
//						cntWord++;
//						
//						AtomicInteger cnt = null;
//						if(cateMap.containsKey(cate)) {
//							cnt = cateMap.get(cate);
//						} else {
//							cnt = new AtomicInteger();
//							cateMap.put(cate, cnt);
//						}
//						cnt.incrementAndGet();
//						
//						//logger.debug("TERM:{}", termStr);
//					}
//				}
//				reader.close();
//				analyzer.close();
//				
//				table.adjust(categoryId, categoryId, cntWord, 1, 1);
//			}
//			
//			Iterator<String> kiter = termCateMap.keySet().iterator();
//			
//			logger.trace("================================================================================");
//			
//			Map<Integer, List<String>> tierMap = new HashMap<Integer, List<String>>();
//			
//			int maxTier = -1;
//			
//			for (; kiter.hasNext();) {
//				String key = kiter.next();
//				Map<String, AtomicInteger> cateMap = termCateMap.get(key);
//				Integer tier = cateMap.size();
//				
//				if(maxTier < tier) {
//					maxTier = tier;
//				}
//				
//				List<String> termList = null;
//				
//				if(tierMap.containsKey(tier)) {
//					termList = tierMap.get(tier);
//				} else {
//					termList = new ArrayList<String>();
//					tierMap.put(tier, termList);
//				}
//				termList.add(key);
//			}
//			
//			File baseDir = null;
//			
//			baseDir = new File("/tmp");
//			if(!baseDir.exists()) {
//				baseDir = new File("G:/Documents/workspace/TEST_HOME");
//			}
//			
//			File file = new File(baseDir, "aaa.txt");
//			
//			File learned = new File(baseDir, "learn.data");
//			
//			File cateStore = new File(baseDir, "cate.data");
//			
//			writer = new FileWriter(file);
//			
//			for (int inx = maxTier - 1; inx >= 0; inx--) {
//				writer.append("================================================================================\r\n");
//				writer.append("T:"+String.valueOf(inx)+"\r\n");
//				if (tierMap.containsKey(inx)) {
//					List<String> termList = tierMap.get(inx);
//					for(int tinx=0;tinx<termList.size();tinx++) {
//						String termStr = termList.get(tinx);
//						logger.trace("TIER[{}] / TERM:{}", inx, termStr);
//						writer.append(termStr+"\r\n");
//					}
//				}
//			}
//			
//			byte[] buffer = new byte[1024];
//			OutputStream ostream = null;
//			ostream = new FileOutputStream(learned);
//			table.freeze();
//			table.writeTo(ostream, buffer);
//			ostream.close();
//			
//			ostream = new FileOutputStream(cateStore);
//			categorySet.writeTo(ostream, buffer);
//			ostream.close();
//		} finally {
//			if(writer != null) try {
//				writer.close();
//			} catch (Exception ignore) { }
//			if(reader != null) try {
//				reader.close();
//			} catch (Exception ignore) { }
//		}
//	}
	
//	@Test
//	public void testLearn() throws Exception {
//		String question = "지인이 돈을 빌려달라기에 담보를 요구했더니, 지인이 살고 있는 집의 전세보증금을 담보로 하겠다고 합니다. 안전할까요?";
//		//question = "안녕하십니까 변호사님 저는 약 1주일 전 쯤에 회사에서 친구 에게 상해/폭행 사건을 당하였습니다 가해자는 도구/흉기를 사용했고, 1명 이었습니다 피해 정도는 전치 4주 이내 이며 가해자의 범행은 이번이 처음 인것 같습니다 당시 사건에 대한 증거를 가지고 있으며 이러한 내용으로 가해자를 법적으로 처벌하고 싶습니다 변호사님의 도움이 필요합니다. ";
//		//question = "안녕하십니까! 저는 인터넷 게임관련업체에 종사하는 사람입니다. 궁금한게 있어서 이렇게 펜을 들었습니다. 온라인상에서 개최되는 포커게임을 오프라인상에서 개최를하려 생각하게 되었습니다. 일정회비를 받고 참가자를 모집할 계획이며 순위에따라 상품지급도 하려 합니다. 이럴경우 게임이 아닌 도박이 되는지요? 실제대회를 개최한다면 법적으로 문제되는것이 무었인지요? 법적으로 문제가 된다면 달리 대회를 개최할 방법은 없는지요? 자세한 설명을 부탁 드립니다. ";
//		//question = "토지를 최초로 취득하거나, 건물을 신축한 경우에 어떠한 등기절차를 요합니까? ";
//		
//		File baseDir = null;
//		
//		baseDir = new File("/tmp");
//		if(!baseDir.exists()) {
//			baseDir = new File("G:/Documents/workspace/TEST_HOME");
//		}
//		
//		File learned = new File(baseDir, "learn.data");
//		File cateStore = new File(baseDir, "cate.data");
//		
//		DoubleClassifierTable table = new DoubleClassifierTable(0,0,0);
//		BasicStringHashSet cateSet = new BasicStringHashSet();
//		
//		byte[] buffer = new byte[1024];
//		InputStream istream = null;
//		try {
//			istream = new FileInputStream(learned);
//			table.readFrom(istream , buffer);
//		} finally {
//			if(istream != null) try { istream.close(); } catch (IOException ignore) { }
//		}
//		try {
//			istream = new FileInputStream(cateStore);
//			cateSet.readFrom(istream, buffer);
//		} finally {
//			if(istream != null) try { istream.close(); } catch (IOException ignore) { }
//		}
//		
//		table.unfreeze();
//		table.freeze();
//		
//		int[] category1 = new int[4];
//		double[] scoreTable1 = new double[4];
//		int[] category2 = new int[4];
//		double[] scoreTable2 = new double[4];
//		List<CharSequence> termList = new ArrayList<CharSequence>();
//		KoreanAnalyzer analyzer = null;
//		StringReader qreader = null;
//		TokenStream tstream = null;
//		CharTermAttribute term = null;
//		{
//			analyzer = new KoreanAnalyzer();
//			qreader = new StringReader(question);
//			tstream = analyzer.tokenStream("", qreader);
//			
//			analyzer.setHasOrigin(true);
//			analyzer.setOriginCNoun(true);
//			analyzer.setBigrammable(true);
//			analyzer.setQueryMode(false);
//			
//			term = tstream.getAttribute(CharTermAttribute.class);
//			for(;tstream.incrementToken();) {
//				logger.debug("TERM:{}", term);
//				termList.add(term.toString());
//			}
//			analyzer.close();
//		}
//		
//		JSONArray tracer = new JSONArray();
//		
//		table.classify(termList, category1, category2, scoreTable1, scoreTable2, tracer);
//		
//		//for(int inx=0;inx<cateSet.dataCount();inx++) {
//		//	logger.debug("[{}]:{}", inx, cateSet.get(inx));
//		//}
//		
//		for (int inx = 0; inx < category1.length; inx++) {
//			logger.debug("[{}] = {} / {}", inx, cateSet.get(category1[inx]), scoreTable1[inx]);
//		}
//		
//		//for(int inx=0;inx<cateSet.dataCount();inx++) {
//		//	int cateId = inx+1;
//		//	int termId = table.getId("폭행");
//		//	Integer tid = table.categoryDataSet.getId(termId, cateId);
//		//	logger.debug("[{}/{}/{}] / TID:{}", cateId,cateSet.get(cateId), termId, tid);
//		//}
//	}
}