package org.fastcatsearch.common.data.type;

import java.util.List;

@SuppressWarnings("rawtypes")
public class ComparableKeyValueListSortable<T extends Comparable<T>>  implements Sortable {

    private List<T> key;
	private List data;
    private int length;

    public ComparableKeyValueListSortable(List<T>key, List data, int length) {
    	this.key = key;
        this.data = data;
        this.length = length;
    }

    @Override
    public int compare(int inx1, int inx2) {
    	int keySize = key.size();
    	if(keySize > inx1 && keySize > inx2) {
			return key.get(inx1).compareTo(key.get(inx2));
    	}
    	return 0;
    }

	@Override
    @SuppressWarnings("unchecked")
    public void swap(int inx1, int inx2) {
    	int keySize = key.size();
    	if(keySize > inx1 && keySize > inx2) {
			T k = key.get(inx1);
			key.set(inx1, key.get(inx2));
			key.set(inx2, k);
			if (data != null && !data.equals(key)) {
				Object v = data.get(inx1);
				data.set(inx1, data.get(inx2));
				data.set(inx2, v);
			}
    	}
    }

	public void sortLength(int size) { this.length = size; }
    @Override public int sortLength() { return length; }

	@Override
    @SuppressWarnings("unchecked")
	public void moveShift(int inxFrom, int inxTo) {
    	int keySize = key.size();
    	if(keySize > inxFrom && keySize > inxTo) {
			if(inxFrom < inxTo) {
				T k = key.remove(inxFrom);
				key.add(inxTo, k);
				if (data != null && !data.equals(key)) {
					Object v = data.remove(inxFrom);
					data.add(inxTo, v);
				}
			} else if(inxFrom > inxTo) {
				T k = key.remove(inxFrom);
				key.add(inxTo, k);
				if (data != null && !data.equals(key)) {
					Object v = data.remove(inxFrom);
					data.add(inxTo, v);
				}
			}
    	}
	}
}