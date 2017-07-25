package org.fastcatsearch.common.data.type;

public class IntKeyValueArraySortable implements Sortable {

	private int[] key;
    private Object[] data;
    private int length;

    public IntKeyValueArraySortable(int[] key, Object[] data, int length) {
    	this.key = key;
        this.data = data;
        this.length = length;
    }

    @Override
    public int compare(int inx1, int inx2) {
    	if( key[inx1] > key[inx2]) {
			return 1;
        } else if( key[inx1] < key[inx2]) {
			return -1;
        }
        return 0;
    }

    @Override
    public void swap(int inx1, int inx2) {
    	if(key.length > inx1 && key.length > inx2) {
			int k = key[inx1];
			key[inx1] = key[inx2];
			key[inx2] = k;
			
			if (data != null && !data.equals(key)) {
				Object v = data[inx1];
				data[inx1] = data[inx2];
				data[inx2] = v;
			}
    	}
    }
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for(int inx=0;inx<key.length;inx++) {
    		if(inx > 0) { sb.append(","); }
    		sb.append(String.valueOf(key[inx]));
    	}
    	return sb.toString();
    }

	public void sortLength(int size) { this.length = size; }
	
    @Override public int sortLength() { return length; }

	@Override
	public void moveShift(int inxFrom, int inxTo) {
		if(key.length > inxFrom && key.length > inxTo) {
			int k = key[inxFrom];
			if(inxFrom < inxTo) {
				int len = inxTo - inxFrom;
				System.arraycopy(key, inxFrom + 1, key, inxFrom, len);
				key[inxTo] = k;
				if(data != null && !data.equals(key)) {
					Object v = data[inxFrom];
					System.arraycopy(data, inxFrom + 1, data, inxFrom, len);
					data[inxTo] = v;
				}
			} else if(inxFrom > inxTo) {
				int len = inxFrom - inxTo;
				System.arraycopy(key, inxTo, key, inxTo + 1, len);
				key[inxTo] = k;
				if(data != null && !data.equals(key)) {
					Object v = data[inxFrom];
					System.arraycopy(data, inxTo, data, inxTo + 1, len);
					data[inxTo] = v;
				}
			}
    	}
	}
}