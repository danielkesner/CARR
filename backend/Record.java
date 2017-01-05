package backend;

public class Record implements Comparable<Record> {

	String originalRecord;
	String trimmedRecord;
	String address;
	String status;
	int numMatches;
	boolean isCorp;

	/* Constructors */
		
	public Record(String originalRecord, String trimmedRecord) {
		this.originalRecord = originalRecord;
		this.trimmedRecord = trimmedRecord;
	}
	
	public Record(String originalRecord, String status, boolean isCorp) {
		this.originalRecord = originalRecord;
		this.status = status;
		this.isCorp = isCorp;
	}
	
	public Record(String trimmedRecord, String status, String address) {
		this.trimmedRecord = trimmedRecord;
		this.status = status;
		this.address = address;
	}
	
	public Record(String originalRecord, String trimmedRecord, String status, String address) {
		this.originalRecord = originalRecord;
		this.trimmedRecord = trimmedRecord;
		this.status = status;
		this.address = address;
	}
	
	public Record(String trimmedRecord, String status, String address, boolean isCorp) {
		//w as original
		this.trimmedRecord = trimmedRecord;
		this.status = status;
		this.address = address;
		this.isCorp = isCorp;
	}
	
	public Record(String originalRecord, String trimmedRecord, int numMatches, boolean isCorp) {
		this.trimmedRecord = trimmedRecord;
		this.originalRecord = originalRecord;
		this.numMatches = numMatches;
		this.isCorp = isCorp;
	}
	


	// Compare two records, used to sort in descending order with longest first
	public int compareTo(Record r) {
		return r.numMatches - this.numMatches;
	}

}