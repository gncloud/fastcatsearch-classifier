package org.fastcatsearch.common.data.type;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.fastcatsearch.common.data.structure.VString;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VStringTest {
	
	private static final Logger logger = LoggerFactory.getLogger(VStringTest.class);

	@Test
	public void test() {
		String str1 = "테스트";
		VString str2 = new VString("테스트");
		VString str3 = new VString("abCd");
		VString str4 = new VString("AbcD");
		
		str3.setIgnoreCase(true);
		str4.setIgnoreCase(true);
		
		assertEquals(str3, str4);
		
		VString[] arrays = new VString[] {
			new VString("테스트1"),
			new VString("테스트2"),
			new VString("테스트"),
			new VString("테스트3"),
			new VString("테스트4")
		};
		
		logger.debug("DIFF:{}", (int)('a'-'A'));
		
		Set<CharSequence> list = new TreeSet<CharSequence>();
		
		list.add(str1);
		list.add(str2);
		
		for(VString item : arrays) {
			list.add(item);
		}
		
		Iterator<CharSequence> iter = list.iterator();
		
		assertEquals(str1.hashCode(), str2.hashCode());
		
		for(int inx=0;iter.hasNext();inx++) {
			CharSequence item = iter.next();
			logger.debug("ELEM[{}] = {} / {}", inx, item, item.hashCode());
			
			if(inx == 0) {
				assertEquals(item, "테스트");
				assertEquals(item.hashCode(), "테스트".hashCode());
			} else if(inx == 1) {
				assertEquals(item, "테스트1");
				assertEquals(item.hashCode(), "테스트1".hashCode());
			} else if(inx == 2) {
				assertEquals(item, "테스트2");
				assertEquals(item.hashCode(), "테스트2".hashCode());
			} else if(inx == 3) {
				assertEquals(item, "테스트3");
				assertEquals(item.hashCode(), "테스트3".hashCode());
			} else if(inx == 4) {
				assertEquals(item, "테스트4");
				assertEquals(item.hashCode(), "테스트4".hashCode());
			}
		}
	}
	
	@Test
	public void test2() {
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		ByteArrayInputStream istream = null;
		try {
			String orgTxt = "아름다운이땅에금수강산에단군할아버지가터잡으시고홍익인간뜻으로나라세우니대대손손훌륭한인물도많아고구려세운동명왕백제온조왕알에서나온혁거세만주벌판달려라광개토대왕신라장군이사부";
			
			VString str1 = new VString(orgTxt.toCharArray(), 1, 3);
			
			logger.debug("STR:{} / {} / {}", str1, str1.hashCode(), str1.wholeString() );
			
			str1.writeTo(ostream, null);
			
			byte[] buffer = ostream.toByteArray();
			
			istream = new ByteArrayInputStream(buffer);
			
			VString str2 = new VString();
			
			str2.readFrom(istream, null);
			
			logger.debug("STR2:{} / {} / {}", str2, str2.hashCode(), str1.wholeString());
			
		} catch (Exception e) {
			logger.error("", e);
		}
		
		try {
			ostream.close();
		} catch (IOException ignore) { }
		try {
			istream.close();
		} catch (IOException ignore) { }
	}
	
	@Test
	public void test3() {
		VString str1 = new VString("테스트1");
		VString str2 = new VString("트1");
		
		logger.debug("{} startsWith {} : {}", str2, str1, str2.startsWith(str1));
		logger.debug("{} startsWith {} : {}", str1, str2, str1.startsWith(str2));
		logger.debug("{} endsWith {} : {}", str1, str2, str1.endsWith(str2));
	}
}