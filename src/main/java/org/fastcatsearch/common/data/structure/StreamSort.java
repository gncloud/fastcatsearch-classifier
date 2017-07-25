package org.fastcatsearch.common.data.structure;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fastcatsearch.common.data.type.ComparableKeyValueListSortable;
import org.fastcatsearch.common.data.type.Copyable;
import org.fastcatsearch.common.data.type.Streamable;
import org.fastcatsearch.common.data.type.StreamableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamSort<T extends Streamable & Comparable<?> & Copyable<T>> implements FileFilter {
	private static final Logger logger = LoggerFactory.getLogger(StreamSort.class);
	static { logger.trace(""); }
	
	private T instance;
	
	private int keySize;
	private int mergeSize;
	
	public StreamSort(T instance, int keySize, int mergeSize) {
		this.instance = instance;
		this.keySize = keySize;
		this.mergeSize = mergeSize;
		
		if(keySize < 1) {
			keySize = 1;
		}
	}
	
	public void set(List<T> list, int inx, T item) {
		if(inx >= list.size()) {
			list.add(item);
		} else {
			list.set(inx, item);
		}
	}

	//FIXME:need to implement multi-thread merge sort
	public void sort(Iterator<T> iter, OutputStream ostream, boolean isAscending) throws IOException {
		byte[] buf = new byte[1024];
		List<T> array = new ArrayList<T>();
		File baseFile = File.createTempFile("sort", "");
		baseFile.delete();
		baseFile.mkdir();
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ComparableKeyValueListSortable lsort = new ComparableKeyValueListSortable(array, null, keySize);
		Sorter sorter1 = Sorter.getInstance(Sorter.SortType.HeapSort);
		Sorter sorter2 = new HeadElementSort();
		Sorter sorter = sorter1;
		try {
			//phase 1 distribute;
			logger.trace("PHASE1");
			int finx = 0;
			int tinx = 0;
			for (; iter.hasNext(); tinx = (tinx + 1) % keySize) {
				set(array, tinx, iter.next());
				if(tinx == keySize - 1) {
					//local sort
					lsort.sortLength(tinx + 1);
					sorter.init(lsort, isAscending);
					sorter.sort();
					dumpToFile(array, keySize, new File(baseFile, String.valueOf(finx ++)), buf);
				}
			}
			if(tinx > 0) {
				lsort.sortLength(tinx + 1);
				sorter.init(lsort, isAscending);
				sorter.sort();
				dumpToFile(array, keySize, new File(baseFile, String.valueOf(finx ++)), buf);
			}
			
			//phase 2 merge
			logger.trace("PHASE2");
			List<T> keyList = new ArrayList<T>();
			List<Iterator<T>> iterList = new ArrayList<Iterator<T>>();
			List<InputStream> streamList = new ArrayList<InputStream>();
			@SuppressWarnings({ "rawtypes", "unchecked" })
			
			ComparableKeyValueListSortable msort = new ComparableKeyValueListSortable(keyList, iterList, keyList.size());
			InputStream mistream = null;
			OutputStream mostream = null;
			
			int files = finx;
			int merges = mergeSize;
			int delta = 1;
			if(files < mergeSize) {
				merges = files;
			}
			for (; delta < files; delta *= merges) {
				logger.trace("FILES:{} / DELTA:{} / MERGES:{}", files, delta, merges);
				for (int binx = 0; binx < files; binx += (delta * merges)) {
					for (int minx = binx; minx < files && minx < (binx + (delta * merges)); minx += delta) {
						File file = new File(baseFile, String.valueOf(minx));
						mistream = new FileInputStream(file);
						streamList.add(mistream);
						@SuppressWarnings({ "unchecked", "rawtypes" })
						Iterator<T> miIter = new StreamableIterator(instance, mistream, file.length(), buf);
						iterList.add(miIter);
						if(miIter.hasNext()) {
							keyList.add(miIter.next());
						} else {
							iterList.remove(miIter);
						}
					}
					
					msort.sortLength(keyList.size());
					boolean done = false;
					try {
						File mergeFile = new File(baseFile, "m" + String.valueOf(binx));
						if ((delta * merges) < files) {
							mostream = new FileOutputStream(mergeFile);
						} else {
							mostream = ostream;
						}
						for (int sinx = 0; !done; sinx++) {
							if(sinx == 0) {
								sorter = sorter1;
							} else {
								sorter = sorter2;
							}	
							sorter.init(msort, isAscending);
							sorter.sort();
							if(keyList.size() > 0) {
								T t = keyList.get(0);
								t.writeTo(mostream, buf);
								Iterator<T> miter = iterList.get(0);
								if(miter.hasNext()) {
									T i = miter.next();
									keyList.set(0, i);
								} else {
									keyList.remove(0);
									iterList.remove(0);
								}
							} else {
								done = true;
							}
						}
						File targetFile = new File(baseFile, String.valueOf(binx));
						targetFile.delete();
						logger.trace("TARGET CREATED:{} / {}", targetFile, delta);
						if(mergeFile.exists()) {
							mergeFile.renameTo(targetFile);
						}
					} finally {
						if (mostream != null && mostream != ostream) try { mostream.close(); } catch (IOException ignore) { }
					}
					for (int minx = 0; minx < streamList.size(); minx++) {
						mistream = streamList.get(minx);
						if(mistream != null) try { mistream.close(); } catch (IOException ignore) { }
					}
				}
			}
		} finally {
			logger.trace("deleting..");
			try { baseFile.listFiles(this); } catch (Exception ignore) { }
			try { baseFile.delete(); } catch (Exception ignore) { }
		}
	}

	@Override
	public boolean accept(File file) {
		try { file.delete(); } catch (Exception ignore) { }
		return false;
	}
	
	private static final synchronized void dumpToFile(List<? extends Streamable> obj, int keySize, File file, byte[] buf) {
		OutputStream ostream = null;
		try {
			ostream = new FileOutputStream(file);
			for (int inx = 0; inx < keySize; inx++) {
				obj.get(inx).writeTo(ostream, buf);
			}
		} catch (IOException e) {
		} finally {
			try { ostream.close(); } catch (IOException ignore) { }
		}
	}
	
	public static class HeadElementSort extends Sorter {
		@Override
		public void sort() {
			int maxInx = data.sortLength();
			logger.trace("data : {}", data);
			boolean inserted = false;
			for (int inx2 = 1; inx2 < maxInx; inx2++) {
				int compare = data.compare(0, inx2);
				logger.trace("inx:[{}:{}]={}", 0, inx2, compare);
				if ((isAscending && compare < 0) || (!isAscending && compare > 0)) {
					if(inx2 > 1) {
						data.moveShift(0, inx2 - 1);
					}
					inserted = true;
					break;
				}
				logger.trace("data : {}", data);
			}
			if (!inserted) {
				data.moveShift(0, maxInx - 1);
			}
		}
	}
}