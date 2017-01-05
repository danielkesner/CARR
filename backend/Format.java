/* Extract.java
 * @author Daniel Kesner
 * Summer 2016
 * Contains methods meant to extract information from one or more 
 * datasets related to CARR student summer internship. 
 */

package backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;

public class Format {

	static String parentDir = "C:/Users/Daniel/Documents/CARR/Build_Tables/";
	static String LPMASTER = "C:/Users/Daniel/Documents/CARR/CASOS_Disk_1_LP_LLC/LPMASTER/";
	static String CORPMASTER = "C:/Users/Daniel/Documents/CARR/CASOS Disk 2 - Corps/CORPMASTER/";

	static final String whiteSpace = " ";
	static final int corpBeginName = 70;
	static final int corpEndName = 420;
	static final int corpBeginMailAddrLine1 = 470;
	static final int corpBeginMailAddrLine2 = 510;
	static final int corpBeginMailAddrCity = 550;
	static final int corpBeginMailAddrState = 574;
	static final int corpBeginMailAddrZIP = 589;
	static final int corpEndMailAddrZIP = 599;

	static final int LPbeginName = 22;
	static final int LPendName = 242;
	static final int LPbeginAddr = 242;
	static final int LPendAddr = 282;
	static final int LPbeginCity = 282;
	static final int LPendCity = 306;
	static final int LPbeginState = 308;
	static final int LPendState = 310;
	static final int LPbeginZip = 310;
	static final int LPendZip = 320;

	static final int corpTotalRecords = 3580319;

	// Returns true if char c is a number (0..9), false otherwise
	private static boolean isNum(char c) {

		String s = String.valueOf(c);

		try {

			Integer.parseInt(s);

		} catch (NumberFormatException nfe) {
			return false;
		}

		return true;

	}
	
	public static void extractCorp(String readFrom, String writeTo) {
		Scanner read;
		FileWriter write;
		FileWriter debug;
		String sb = "";
		int ctr = 1;
		int unable = 0;
		int idx = 0;
		int save;
		String name = "";
		String status = "";
		String addrLine1 = "";
		String addrLine2 = "";
		String addrCity = "";
		String addrState = "";
		String addrZip = "";

		try {

			read = new Scanner(new File(readFrom));
			write = new FileWriter(writeTo);
			debug = new FileWriter(CORPMASTER + "debug.txt");
			debug.write("All files unable to be processed by usual algorithm: ");
			debug.write(System.lineSeparator());

			// For each line
			while (read.hasNextLine()) {
				idx = 0;
				sb = read.nextLine();

				/* Try to extract each field according to the PRODUCT INFO specifications */
				try {
					/* Extract information */
					name = sb.substring(corpBeginName, corpEndName).trim();
					addrLine1 = sb.substring(corpBeginMailAddrLine1, corpBeginMailAddrLine2 - 1).trim();
					addrLine2 = sb.substring(corpBeginMailAddrLine2, corpBeginMailAddrCity - 1).trim();
					addrCity = sb.substring(corpBeginMailAddrCity, corpBeginMailAddrState - 1).trim();
					addrState = sb.substring(corpBeginMailAddrState, corpBeginMailAddrZIP - 1).trim();
					addrZip = sb.substring(corpBeginMailAddrZIP, corpEndMailAddrZIP).trim();
					status = getCorpStatus(sb);

				/* Case: we have a record that doesn't conform to the PRODUCT INFO sheet */
				} catch (StringIndexOutOfBoundsException se) {

					// First, format the line of text for ease of parsability (is that a word?)
					// Trim pre/post-word whitespace with trim()
					// regex: replace all instances of 2+ whitespace chars with null ""
					sb = sb.trim();
					sb = sb.replaceAll(whiteSpace + "{2,}+", "");
					// Convert String to char[] to simplify algorithm
					char[] chbf = sb.toCharArray();

					// Walk the String until not pointing to a number
					// idx points to the first letter of the 4-letter status, i.e. '->A<-RTS'
					while (isNum(chbf[++idx]));

					// Walk 7 indices to the right to skip over the corporation status
					idx = idx + 7;

					// idx now points to the first numerical digit after status, i.e. 'ARTSS->0<-'
					// Again continue until non-numeric, this is the first letter of the corp name
					while (isNum(chbf[++idx]));

					// Save beginning of name location
					save = idx;

					// Try to walk the String until you see a number or space
					// If you hit the end of the line, the name continues all the way to the end
					// of the line, and we simply take from beginName to string length-1.
					try {

						while (! isNum(chbf[++idx]));

					} catch (ArrayIndexOutOfBoundsException ai) {
						idx = sb.length();
					}
					// idx now points to the end of the Corporation name  

					// The name will be the substring between the saved index and idx
					name = sb.substring(save, idx);

					unable++;
				}

				write.write(status + "|" + name + "|" + addrLine1 
						+ " " + addrLine2 + " " + addrCity + ", " + addrState + " " + addrZip + "|" + ctr);
				write.write(System.lineSeparator());

				if (++ctr % 50000 == 0)
					System.out.println("Processed " + ctr + " records.");

			} // end while hasNextLine()

		} catch (Exception e) {
			System.out.println("Exception thrown on line " + ctr + " for string:");
			System.out.println(sb);
			e.printStackTrace();
		}

		System.out.println("'Unable' counter is: " + unable);
		System.out.println("Total is: " + ctr);

	}

	public static void extractLPMASTER(String readFrom) {

		BufferedReader reader;
		FileWriter write;
		int ctr = 1;
		String sb = "";
		String name = "";
		String status = "";
		String addr = "";
		String addrCity = "";
		String addrState = "";
		String addrZip = "";

		try {

			reader = new BufferedReader(new FileReader(readFrom));
			write = new FileWriter(LPMASTER + "LPMASTER_FMT.txt");

			while ((sb = reader.readLine()) != null) {
				status = getLPStatus(sb);

				try {

					name = sb.substring(LPbeginName, LPendName).trim();
					addr = sb.substring(LPbeginAddr, LPendAddr).trim();
					addrCity = sb.substring(LPbeginCity, LPendCity).trim();
					addrState = sb.substring(LPbeginState, LPendState).trim();
					addrZip = sb.substring(LPbeginZip, LPendZip).trim();

				} catch (StringIndexOutOfBoundsException s) {
					write.write("Unable to process record for line " + ctr);
					write.write(System.lineSeparator());
				}

				write.write(status + "|" + name + "|" + addr 
						+ " " + addrCity + ", " + addrState + " " + addrZip + "|" + ctr);
				write.write(System.lineSeparator());

				if (++ctr % 25000 == 0)
					System.out.println("Processed " + ctr + " records.");

			}		
		} catch (Exception e) {
			System.out.println();
			e.printStackTrace();
		}

	}

	// Returns the n'th line from a file
	public String getNthLine(String path, int n) {

		Scanner scan;
		String sb = "";

		try {

			scan = new Scanner(new File(path));

			for (int i = 1; i < n; i++){
				scan.nextLine();
			}

			sb = scan.nextLine();	

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.trim().replaceAll(whiteSpace + "{2,}+", "");
	}

	// Returns a String containing the full word status, i.e. Active, Cancelled, etc.
	public static String getLPStatus(String line) {
		switch (line.substring(20,21)) {

		case "A": return "Active";
		case "B": return "Dishonored Check";
		case "C": return "Cancelled";
		case "D": return "Dissolved";
		case "P": return "Pending Cancellation";
		case "F": return "Franchise Tax Board Supended/Forfeited";
		case "M": return "Merged out";
		case "O": return "Converted out";
		case "S": return "Sec. State Suspended/Forfeited";
		case "2": return "SOS/FTB Suspended/Forfeited";
		default: throw new RuntimeException("Exception thrown in getStatus(String): status code in entry: "
				+ line + "\n" + " does not conform to any of the given constants from product info sheet.");

		}
	}

	// Returns the full word version of each Corporation status code
	public static String getCorpStatus(String line) {
		switch (line.substring(21,22)) {
		case "1": return "Active";
		case "2": return "Suspended";
		case "3": return "Canceled";
		case "4": return "Surrendered";
		case "5": return "Term Expired";
		case "6": return "Dissolved";
		case "7": return "Forfeited";
		case "8": return "Deleted";
		case "9": return "Inactive";
		case "C": return "Merged out";
		case "E": return "State to Federal Bank Conversion";
		case "F": return "Conditionally dissolved (no tax clearance)";

		default: return "Converted out";
		}

	}

	public static void main(String[] a) {

		long start = System.currentTimeMillis();
		long min, sec;

		Statistics stats = new Statistics();

		/* Main method calls */

		extractLPMASTER(LPMASTER + "LPMASTER.txt");

		min = ((System.currentTimeMillis()-start) / 1000) / 60;
		sec = ((System.currentTimeMillis())-start) / 1000 % 60;

		System.out.println("Done. Elapsed time: " + min + " minutes, " + sec + " seconds.");
	}
}
