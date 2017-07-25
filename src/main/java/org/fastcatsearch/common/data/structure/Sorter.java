package org.fastcatsearch.common.data.structure;

import org.fastcatsearch.common.data.type.Sortable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Sorter {
	protected static final Logger logger = LoggerFactory.getLogger(Sorter.class);
	
	public enum SortType {
		BubbleSort,
		HeapSort,
		QuickSort,
		SelectionSort
	}
	
    protected Sortable data;
    protected boolean isAscending;
    
	public static final Sorter getInstance(SortType sortType) {
		String clsName = sortType.name();
		String clsStr = Sorter.class.getName() + "$" + clsName;
		return getInstanceFromClassName(clsStr);
		
	}
	
	public static Sorter getInstanceFromClassName(String clsStr) {
		Sorter ret = null;
		try {
			ret = (Sorter) Class.forName(clsStr).newInstance();
		} catch (InstantiationException e) {
			logger.error("", e);
		} catch (IllegalAccessException e) {
			logger.error("", e);
		} catch (ClassNotFoundException e) {
			logger.error("", e);
		}
		return ret;
	}
    
	public void init(Sortable data, boolean isAscending) {
		this.data = data;
		this.isAscending = isAscending;
	}
	
    public abstract void sort();

	public static class BubbleSort extends Sorter {
		@Override
		public void sort() {
			boolean sorted;
			int compare;
			//logger.trace("size:[{}]", data.sortLength());
			int maxInx = data.sortLength() - 1;
			do {
				sorted = true;
				for (int inx1 = 0; inx1 < maxInx; inx1++) {
					int inx2 = inx1+1;
					compare = data.compare(inx1, inx2);
					logger.trace("inx:[{}:{}]={}", inx1, inx2, compare);

					if ((isAscending && compare > 0) || (!isAscending && compare < 0)) {
						data.swap(inx1, inx2);
						sorted = false;
					}
				}
				//logger.trace("data : {}", data);
			} while (!sorted);
		}
	}
	
	public static class SelectionSort extends Sorter {
		@Override
		public void sort() {
			int maxInx = data.sortLength();
			logger.trace("data : {}", data);
			for (int inx1 = 0; inx1 < maxInx - 1; inx1++) {
				for (int inx2 = inx1 + 1; inx2 < maxInx; inx2++) {
					int compare = data.compare(inx1, inx2);
					logger.trace("inx:[{}:{}]={}", inx1, inx2, compare);
					if ((isAscending && compare > 0) || (!isAscending && compare < 0)) {
						data.swap(inx1, inx2);
					}
				}
				logger.trace("data : {}", data);
			}
		}
	}
	
	public static class QuickSort extends Sorter {
		@Override
		public void sort() {
	        quicksort(data, 0, data.sortLength() - 1);
		}
		public void quicksort(Sortable data, int left, int right) {
			//logger.trace("quicksort");
			int diff = right - left;
			int comp = 0;
			if(diff == 1) {
				comp = data.compare(left, right);
				if((isAscending && comp > 0) || (!isAscending && comp < 0)) {
					data.swap(left, right);
				}
			} else if (diff > 1) {
				int leftInx = left;
				int rightInx = right;
				int pivot = (left + right) >> 1;
				//partitioning.
				do {
					//logger.trace("LEFT:{} / RIGHT:{} / PIVOT:{} / LEN:{}", leftInx, rightInx, pivot, data.sortLength());
					for (comp = 0; ((comp = data.compare(leftInx, pivot)) < 0 && isAscending)
						|| (comp > 0 && !isAscending);) {
						leftInx++;
					}
					for (comp = 0; ((comp = data.compare(rightInx, pivot)) > 0 && isAscending)
						|| (comp < 0 && !isAscending);) {
						rightInx--;
					}
					if(leftInx <= rightInx) {
						if(leftInx == pivot) {
							logger.trace("relocate pivot : {}->{}", pivot, rightInx);
							pivot = rightInx;
						} else if(rightInx == pivot) {
							logger.trace("relocate pivot : {}->{}", pivot, leftInx);
							pivot = leftInx;
						}
						data.swap(leftInx, rightInx);
						leftInx++;
						rightInx--;
					}

				} while (leftInx <= rightInx);
				//sorting.
				if(left < rightInx) {
					quicksort(data, left, rightInx);
				}
				if(leftInx < right) {
					quicksort(data, leftInx, right);
				}
			}
		}
	}
	
	public static class HeapSort extends Sorter {
		@Override
		public void init(Sortable data, boolean isAscending) {
			this.data = data;
			this.isAscending = isAscending;
			makeHeap(data, 0, data.sortLength(), isAscending);
		}
		@Override
		public void sort() {
	        int maxInx;
	        int size = data.sortLength();
	        for (; size > 1;) {
	            //take node on top
	            size --;
	            maxInx = size >> 1;
	            data.swap(0, size);
	            logger.trace("move top to tail [{}]->[{}]", 0, size);
	            //makeHeap(data, 0, size, isAscending);
	            heapify(data, 1, maxInx, size, isAscending);
	        }
		}
	    public static void makeHeap(Sortable data, int top, int size, boolean isAscending) {
	        logger.trace("make heap data");
	        int maxInx = size >> 1;
	        //make heap tree from base of the tree
	        for (int inx = maxInx; inx > top; inx--) {
	            heapify(data, inx, maxInx, size, isAscending);
	            logger.trace("heapify data : {} / [{}:{}]", data, inx, maxInx);
	        }
	    }
	    public static boolean heapify(Sortable data, int inx, int maxInx, int size, boolean isAscending) {
	        logger.trace("heapify inx:{}~{}:{} / data:{}", inx, size, maxInx, data);
	        maxInx += 1;
	        boolean isBin = false;
	        int compare, parent, child, child1, child2;
	        for (; inx < maxInx;) {
	            parent = inx - 1;
	            child1 = (inx << 1) - 1; // top * 2
	            child2 = child1 + 1; //top * 2 + 1
	            if (child2 < size) {
	                compare = data.compare(child1, child2);
					if ((isAscending && compare > 0) || (!isAscending && compare < 0)) {
	                    child = child1;
	                } else {
	                    child = child2;
	                }
	            } else {
	                child = child1;
	            }
	            logger.trace("parent:{} / child:{}", parent, child);
	            compare = data.compare(parent, child);
				if ((isAscending && compare < 0) || (!isAscending && compare > 0)) {
	                data.swap(parent, child);
	                isBin = true;
	                inx = child + 1;
	            } else {
	                break;
	            }
	        }
	        return isBin;
	    }
	}
}