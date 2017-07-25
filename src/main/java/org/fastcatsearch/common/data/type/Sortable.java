package org.fastcatsearch.common.data.type;

public interface Sortable {
    public int compare(int inx1, int inx2);
    public void swap(int inx1, int inx2);
    public int sortLength();
    public void moveShift(int inxFrom, int inxTo);
}