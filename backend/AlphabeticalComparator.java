package backend;

import java.util.Comparator;

public class AlphabeticalComparator implements Comparator<Record> {
	
	public int compare(Record r1, Record r2) {
		int res = String.CASE_INSENSITIVE_ORDER.compare(r1.trimmedRecord, r2.trimmedRecord);
		if (res == 0) {
			res = r1.compareTo(r2);
		}
		return res;
	}
	
	
}
