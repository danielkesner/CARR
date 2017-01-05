package backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import backend.AlphabeticalComparator;

public class Compare {

	/* Paths (system-specific) */
	static String parentDir = "C:/Users/Daniel/Documents/CARR/";
	static String LPMASTER = parentDir + "LPMASTER/LPMASTER/LPMASTER_FMT.txt";
	static String CORPMASTER = parentDir + "CORPMASTER/CORPMASTER/CORPMASTER_FMT.txt";
	static String CCLD = parentDir + "CCLD/CCLD_7.10.16_FMT.txt";
	static String compDir = parentDir + "Compare/";

	/* List of common words to filter out of a Record */
	static String[] commonWords = {"A", "A.", "THE", "LLC", "LLC.", "LP", "LP.", "INC.", "&", "LLC-BKD", "FOR",
			"IN", "AT", "SVCS", "-", "OF", "DBA", "CTR", "MGMT", "INC", "CORP.", "CORP", "LVG", "MGM",
			"HOME", "HOMES", "CARE", "SERVICES", "LIVING", "CO", "COMPANY", "AL-LH", "CARING", "COMMUNITY",
			"COMMUNITIES", "CAL", "LIV", "SEN", "DE", "L.L.C.", "AND", "SNR", "CITY", "LTD", "LTD.", "MGT",
			"BKD", "MNGMT", "MNGMNT", "MGT.", "LVG."};

	static final int CCLDTotalRecords = 11734;
	static final int corpTotalRecords = 3580277;
	static final int lpTotalRecords = 1472787;


	// Generates Matches.txt, a listing of all possible matching Corp/LP records
	// for a given CCL record (see Matches.txt for output)
	// Runtime: Long (~18 hours)
	public static void getMatches() {

		FileWriter writer;

		ArrayList<Record> possibleMatches = new ArrayList<Record>(25);

		int iter = 1;
		int similarTokens;

		long start = System.currentTimeMillis();

		// Populate arrays with information from each dataset
		ArrayList<Record> ccl = populateRecordList(CCLD, CCLDTotalRecords, false);
		ArrayList<Record> lp = populateRecordList(LPMASTER, lpTotalRecords, false);
		ArrayList<Record> corp = populateRecordList(CORPMASTER, corpTotalRecords, false);

		System.out.println("ArrayLists filled. Time elapsed so far is " + ((System.currentTimeMillis() - start)/1000)
				+	" seconds; beginning matching algorithm.");

		try {

			writer = new FileWriter(compDir + "Matches.txt");

			// For each record in CCL:
			for (Record cclRecord : ccl) {

				possibleMatches.clear();

				/* Check each corporate record for a possible match */
				for (int corpIter = 0; corpIter < corp.size(); corpIter++) {

					similarTokens = getNumberOfMatchingTokens(cclRecord.trimmedRecord, corp.get(corpIter).trimmedRecord);
					if (similarTokens > 1) {
						// We have two or more similar tokens; high probability of match
						// Add to possibleMatches list

						possibleMatches.add(new Record(corp.get(corpIter).originalRecord,
								corp.get(corpIter).trimmedRecord, similarTokens, true));
					}
				} // end: for each Corporate record

				/* Check each LP/LLC record for a possible match */
				for (int lpIter = 0; lpIter < lp.size(); lpIter++) {

					similarTokens = getNumberOfMatchingTokens(cclRecord.trimmedRecord, lp.get(lpIter).trimmedRecord);
					if (similarTokens > 1) {
						// We have two or more similar tokens; high probability of match
						// Add to possibleMatches list
						possibleMatches.add(new Record(lp.get(lpIter).originalRecord, 
								lp.get(lpIter).trimmedRecord, similarTokens, false));
					}
				} // end: for each LP/LLC record

				writer.write("*** For CCL record: " + cclRecord.originalRecord);
				writer.write(System.lineSeparator());

				Collections.sort(possibleMatches);

				// Write to Matches.txt
				for (Record rc : possibleMatches) {
					// If non-corporate record
					if (! rc.isCorp) {
						writer.write(rc.numMatches + " similar token(s) for LP/LLC record: " + rc.originalRecord);
						writer.write(System.lineSeparator());
					}
					else {
						writer.write(rc.numMatches + " similar token(s) for CORPORATE record: " + rc.originalRecord);
						writer.write(System.lineSeparator());
					}
				}

				writer.write("End possible matches for CCL Record: " + cclRecord.originalRecord);
				writer.write(System.lineSeparator());			

				if (++iter % 50 == 0) {
					System.out.println("Processed " + iter + " CCL records.");
					System.out.println("Elapsed time so far: " + ((System.currentTimeMillis() - start)/60000)  
							+	" minutes, " + ((System.currentTimeMillis()-start)/1000 % 60) +  " seconds.");
				}
			}


		} catch (Exception e) {
			System.out.println("Exception thrown on iteration: " + iter);
			e.printStackTrace();
		}

	}

	private static ArrayList<Record> populateRecordList(String path, int buffsize, 
			boolean sortAlphabetically) {

		ArrayList<Record> ret = new ArrayList<Record>(buffsize);
		ArrayList<String> trimmedTokens = new ArrayList<String>(5);

		String[] splitTokens;

		BufferedReader br;

		String strbf = "";
		String originalName = "";
		String trimmedName = "";

		try {

			br = new BufferedReader(new FileReader(path));

			// For each line of the file
			while ((strbf = br.readLine()) != null) {

				originalName = getName(strbf);

				trimmedName = originalName.replaceAll("/", " ");
				trimmedName = trimmedName.replaceAll(",", " ");
				// Split into tokens
				splitTokens = trimmedName.split("\\s+");

				trimmedTokens = removeCommonWords(splitTokens);
				trimmedTokens = removeInternalNonAlpha(trimmedTokens);
				trimmedTokens = removeDuplicates(trimmedTokens);
				trimmedTokens = removeSmallEntries(trimmedTokens);

				// Sort tokens by String length, so that longest Strings are first
				// Increases probability of finding a "good" match
				StringLengthComparator strcomp = new StringLengthComparator();
				Collections.sort(trimmedTokens, strcomp);

				ret.add(new Record(originalName, concat(trimmedTokens.toString())));
			}

			if (sortAlphabetically) {
				AlphabeticalComparator alpha = new AlphabeticalComparator();
				Collections.sort(ret, alpha);
			}

		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		}


		return ret;
	}

	public static ArrayList<Record> createRecordListForDirectMatches(String path, int buffsize) {
		ArrayList<Record> ret = new ArrayList<Record>(buffsize);

		BufferedReader read;

		String strbf = "";
		String name = "";
		String status = "";
		String addr = "";

		try {

			read = new BufferedReader(new FileReader(path));

			while ((strbf = read.readLine()) != null) {

				name = getName(strbf);
				status = getStatus(strbf);
				addr = getAddr(strbf);

				// Only add the record if it is NOT in Active status
				if (! status.equalsIgnoreCase("ACTIVE")) {
					ret.add(new Record(strbf, name, status, addr));
				}
			}		

			Comparator<Record> ALPHABETICAL_ORDER = new Comparator<Record>() {
				public int compare(Record r1, Record r2) {
					int res = String.CASE_INSENSITIVE_ORDER.compare(r1.trimmedRecord, r2.trimmedRecord);
					if (res == 0) {
						res = r1.compareTo(r2);
					}
					return res;
				}
			};

			Collections.sort(ret, ALPHABETICAL_ORDER);

			read.close();

		} catch (Exception e) {
			System.out.println("createRecordListForDirectMatches() failed for line: " + strbf + " on file " + path);
			e.printStackTrace();
		}

		return ret;
	}

	// Read from Matches.txt, build Direct_Matches.txt
	// Note: Very resource heavy, may throw GC overhead exceptions on machines with <8GB RAM
	// If GC exceptions are thrown, try increasing heap alloc to 2/4/6/8 GB (-Xmx nG)
	public static void getDirectMatches(boolean db_flag) { 

		BufferedReader readFromMatches;

		String line = "";
		String cclName = "";
		String status = "";
		String corpLPName = "";
		String corpLPRecord = "";
		String savedLine = "";
		String address = "";

		FileWriter write;
		FileWriter debug;

		ArrayList<Record> direct_matches = new ArrayList<Record> (3000);

		ArrayList<Record> corp;
		ArrayList<Record> lp;

		boolean isCorp;

		int idx;
		int record_idx = 0;
		int iter = 1;

		long start = System.currentTimeMillis();

		try {

			readFromMatches = new BufferedReader(new FileReader(compDir + "Matches.txt"));

			write = new FileWriter(compDir + "Direct_Matches.txt");
			debug = new FileWriter(compDir + "debug.txt");

			corp = createRecordListForDirectMatches(CORPMASTER, corpTotalRecords);
			lp = createRecordListForDirectMatches(LPMASTER, lpTotalRecords);
			
			AlphabeticalComparator alphaSort = new AlphabeticalComparator();

			System.out.println("Buffers filled, entering main loop.");
			System.out.println("Elapsed time: " + ((System.currentTimeMillis() - start) / 1000)
					+ " seconds.");

			// Read lines from Matches.txt
			while ((line = readFromMatches.readLine()) != null) {

				idx = 0;
				savedLine = line;

				// If line isn't identifying a CCL record
				if (! (line.substring(0,3).equals("***"))) {
					// Skip that line, jump back to beginning of loop
					readFromMatches.readLine();
				}

				// If the line is a CCL record
				else {
					// Extract the CCL name from the line
					cclName = getNameFromMatches(line);
					// Extract the Corp/LP name from the following line
					line = readFromMatches.readLine();
					char[] CLP = line.toCharArray();
					while (CLP[++idx] != ':');
					idx += 2;
					corpLPName = line.substring(idx, line.length());

					// Check to see if we have a direct match
					if (cclName.equalsIgnoreCase(corpLPName)) {
						// Is the matching record in the Corporate or LP/LLC set? True if corporate record
						isCorp = getRecordLocation(savedLine);
						// Case: Match comes from Corporate database
						if (isCorp) {
							record_idx = Collections.binarySearch(corp, new Record(cclName, null, null), alphaSort);
							// If the element is actually in the list (binSearch returns -1 if not):
							if (record_idx > 0) {
								corpLPRecord = corp.get(record_idx).originalRecord;
								try {
									status = getStatus(corpLPRecord);
									address = getAddr(corpLPRecord);
								} catch (ArrayIndexOutOfBoundsException ai) {
									status = "Unable to read status";
									address = "Unable to read address";
								}
								direct_matches.add(new Record(getName(corpLPRecord), status, address, true));

								if (db_flag) {
									debug.write("For iteration " + iter + ", CCLName = " + cclName + ", corpLPRecord = " + corpLPRecord + ", taken from Corp");
									debug.write("\r\n");
								}
							}
						}
						// LP/LLC
						else {
							record_idx = Collections.binarySearch(lp, new Record(cclName, null, null), alphaSort);
							if (record_idx > 0) {
								corpLPRecord = lp.get(record_idx).originalRecord;
								try {
									status = getStatus(corpLPRecord);
									address = getAddr(corpLPRecord);
								} catch (ArrayIndexOutOfBoundsException aie) {
									status = "Unable to read status";
									address = "Unable to read address";
								}
								direct_matches.add(new Record(getName(corpLPRecord), status, address, false));
								if (db_flag) {
									debug.write("For iteration " + iter + ", CCLName = " + cclName + ", corpLPRecord = " + corpLPRecord + ", taken from LP");
									debug.write("\r\n");
								}
							}
						}
					}

				}

				if (++iter % 100000 == 0) 
					System.out.println("Completed " + iter + " iterations.");
				
				System.gc();

			} // end: for each line in Matches.txt

			// Write to file
			write.write("All direct matches between the CCL dataset and the CASOS dataset: ");
			write.write(System.lineSeparator());
			write.write(System.lineSeparator());

			for (Record r : direct_matches) {
				if (r.isCorp) {
					write.write("Corporate facility " + r.trimmedRecord + ", located at " + r.address.trim() + " is in " + r.status.toUpperCase() + " status.");
					write.write(System.lineSeparator());
				}
				else {
					write.write("LP/LLC facility " + r.trimmedRecord + ", located at " + r.address.trim() + " is in " + r.status.toUpperCase() + " status.");
					write.write(System.lineSeparator());
				}
			}

		} catch (Exception e) {
			System.out.println("Exception thrown on iteration " + iter + ", for line: " + line);
			System.out.println("CCLName = " + cclName + ", record_idx = " + record_idx);
			e.printStackTrace();
		}
	}

	// Returns true if record is from Corporate database, false if from LP/LLC
	// Used for reading from Matches.txt
	public static boolean getRecordLocation(String line) {

		int begin = 0;
		int end;
		char[] cb = line.toCharArray();

		// idx will point to the ":" after 'record:'
		while (cb[++begin] != ':');
		// Walk 7 steps left, idx points to the space before the 'r' in 'record'
		begin -= 7;
		end = begin;
		// Decrement begin until it points to the first letter of the status
		while (cb[--begin] != ' ');
		return line.substring(begin+1, end).equals("CORPORATE");
	}

	private static String getNameFromMatches(String line) {
		int idx = 0;
		char[] ccl = line.toCharArray();
		while (ccl[++idx] != ':');
		idx += 2;
		return line.substring(idx, line.length());
	}

	// Concatenates an ArrayList<String> entry back to a standard String
	// i.e. [QUICKSILVER, ALTOONA, MINING] --> QUICKSILVER ALTOONA MINING
	private static String concat(String original) {
		String ret;
		ret = original.replace("[", "");
		ret = ret.replace("]", "");
		ret = ret.replace(",", "");

		return ret;
	}

	// Returns the number of similar tokens between two Strings
	private static int getNumberOfMatchingTokens(String cclRec, String corpLPRec) {

		String[] ccl = cclRec.split("\\s+");
		String[] cLP = corpLPRec.split("\\s+");

		int matches = 0;

		for (int i = 0; i < ccl.length; i++) {
			for (int j = 0; j < cLP.length; j++) {
				if (ccl[i].equalsIgnoreCase(cLP[j])) {
					matches++;
				}
			}
		}
		return matches;
	}

	private static ArrayList<String> removeCommonWords(String[] ccld_name_tokens) {
		ArrayList<String> ret = new ArrayList<String> (ccld_name_tokens.length);

		for (int i = 0; i < ccld_name_tokens.length; i++) {
			if (! isMemberOf(ccld_name_tokens[i], commonWords)) {
				ret.add(ccld_name_tokens[i]);
			}
		}
		return ret;
	}

	private static ArrayList<String> removeInternalNonAlpha(ArrayList<String> arr) {
		ArrayList<String> ret = new ArrayList<String>(arr.size());
		for (String s: arr) {
			s = s.replaceAll("/", "");
			s = s.replaceAll(",", "");
			s = s.replaceAll(";", "");
			ret.add(s);
		}
		return ret;
	}

	// Returns the 'name'/'licensee' field for any record (CCLD/Corp/LP)
	protected static String getName(String line) {
		int begin = 0;
		int end;
		char[] cb = line.toCharArray();

		// Walk down the String until you see the first '|', idx now points to beginning of licensee
		while (cb[begin++] != '|');

		end = begin;

		try {
			// Walk until second delimiter, return the substring between begin, end 
			while (cb[++end] != '|');

			// For malformed Strings (i.e. Active|JIN ANAHEIM PA), just cut it off at str.len()
		} catch (ArrayIndexOutOfBoundsException ai) {
			end = line.length();
		}

		return line.substring(begin, end);
	}

	// Returns true if 'check' is a member of 'arr' 
	// Assumes arr[] is an array of tokenized WORDS, not of full records; i.e.
	// arr = {"MASONIC", "HOMES", "CAL", "ACACIA", "CREEK"}
	private static boolean isMemberOf(String check, String[] arr) {

		for (int k = 0; k < arr.length; k++) {
			if (check.equalsIgnoreCase(arr[k])) 
				return true;
		}
		return false;
	}

	// Removes duplicate elements of an ArrayList<String>
	private static ArrayList<String> removeDuplicates(ArrayList<String> a) {

		String[] seen = new String[a.size()];

		for (int i = 0; i < a.size(); i++) {
			if (! isMemberOf(a.get(i), seen))
				seen[i] = a.get(i);
			else 
				a.remove(a.get(i));
		}
		return a;
	}

	// Remove all entries of size < 3 from an ArrayList<String>
	private static ArrayList<String> removeSmallEntries(ArrayList<String> a) {

		Iterator<String> iter = a.iterator();

		while (iter.hasNext()) {
			if (iter.next().length() < 3) {
				iter.remove();
			}
		}

		return a;
	}

	// Return the status of any record
	private static String getStatus(String line) {
		int end = 0;
		char[] c = line.toCharArray();
		try {
			while (c[++end] != '|');
			return line.substring(0, end);
		} catch (ArrayIndexOutOfBoundsException a) {
			return "";
		}
	}

	// Returns the address from any record
	private static String getAddr(String line) {
		int begin = 0;
		char[] c = line.toCharArray();
		// Skip past Status
		while (c[++begin] != '|');
		begin++;
		// Skip past name
		while (c[++begin] != '|');
		begin++;

		return line.substring(begin, line.length()-2);
	}

	public static void main(String[] args) {

		long start = System.currentTimeMillis();
		long min, sec;

		getMatches();

		//getDirectMatches(false);

		min = ((System.currentTimeMillis()-start) / 1000) / 60;
		sec = ((System.currentTimeMillis()-start) / 1000) % 60;

		System.out.println("Done. Elapsed time: " + min + " minutes, " + sec + " seconds.");
	}

}
