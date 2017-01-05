package backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Statistics {

	// Counters, one for each statuses[i] & total number
	static int total = 0;
	static int active = 0;
	static int dis_check = 0;
	static int cancelled = 0 ;
	static int dissolved = 0;
	static int pending_canc = 0;
	static int tax_board_susp = 0;
	static int merged_out = 0;
	static int converted_out = 0;
	static int ss_sf = 0;
	static int sos_ftb_sf = 0;
	
	static String parentDir = "C:/Users/Daniel/Documents/CARR/";
	static String LPMASTER = "C:/Users/Daniel/Documents/CARR/CASOS_Disk_1_LP_LLC/LPMASTER/";
	static String CORPMASTER = "C:/Users/Daniel/Documents/CARR/CASOS Disk 2 - Corps/CORPMASTER/";

	static String[] statuses = new String[]{"Active", "Dishonored Check", "Cancelled",
			"Dissolved", "Pending Cancellation", "Franchise Tax Board Supended/Forfeited",
			"Merged out", "Converted out", "Sec. State Suspended/Forfeited", 
	"SOS/FTB Suspended/Forfeited"};

	public void calculatePercentages(boolean debug) {
		System.out.println("Total records read: " + total);
		System.out.println("Proportion that are 'Active': " + ((double) active / total) + "%.");
		System.out.println("Proportion that are 'Dishonored Check': " + ((double) dis_check / total) + "%.");
		System.out.println("Proportion that are 'Cancelled': " + ((double) cancelled / total) + "%.");
		System.out.println("Proportion that are 'Dissolved': " + ((double) dissolved / total) + "%.");
		System.out.println("Proportion that are 'Pending Cancellation': " + ((double) pending_canc / total) + "%.");
		System.out.println("Proportion that are 'Franchise Tax Board Suspended/Forfeited': "
				+ ((double) tax_board_susp / total) + "%.");
		System.out.println("Proportion that are 'Merged out': " + ((double) merged_out / total) + "%.");
		System.out.println("Proportion that are 'Converted out': " + ((double) converted_out / total) + "%.");
		System.out.println("Proportion that are 'Sec. State Suspended/Forfeited': " + ((double) ss_sf /total) 
				+ "%.");
		System.out.println("Proportion that are 'SOS/FTB Suspended/Forfeited': " + ((double) sos_ftb_sf / total)
				+ "%.");

		if (debug) {
			System.out.println("Sum total of all categories: " + 
					(active + dis_check + cancelled + dissolved + pending_canc + tax_board_susp + merged_out +
							converted_out + ss_sf + sos_ftb_sf));
			System.out.println("Total: " + total);
		}
	}

	public static double getUnableToProcess(String path) {
		Scanner sc;
		int tot = 0;
		int unable = 0;
		String sb = "";

		try {

			sc = new Scanner(new File(path));

			while (sc.hasNextLine()) {
				sb = sc.nextLine();
				if (sb.substring(1,6) == "Unable") {
					unable++;
				}
				tot++;
			}

		} catch  (Exception e) {
			e.printStackTrace();
		}
		return (double) (unable / tot);
	}

	public int getOneWordNames(String path) {
		Scanner sc;
		String sb = "";
		int idx = 0;
		char[] ch;
		String name;
		int start;
		int count = 0;

		try {
			sc = new Scanner(new File(path));

			while (sc.hasNextLine()) {
				sb = sc.nextLine();
				ch = sb.toCharArray();

				// Walk until first '|'
				while (ch[++idx] != '|');

				start = idx;

				while (ch[++idx] != '|');

				name = sb.substring(start, idx);

				System.out.println(name);

				if (name.length() == 1 || name.length() == 2)
					count++;

				idx = 0;

			}

		} catch (Exception e) {

		}
		return count;
	}

	public int getNumLines(String path) {
		int lines = 0;
		BufferedReader reader;

		try {

			reader = new BufferedReader(new FileReader(path));


			while (reader.readLine() != null) lines++;

			reader.close();
		} catch (Exception e) {

		}
		return lines;
	}

	public void getStatusPercentages() {
		BufferedReader corp;
		BufferedReader lp;
		BufferedReader ccl;
		String tmp = "";
		int LPcancelled = 0, LPactive = 0, LPdissolved = 0, LPother = 0, LPmerged = 0, LPconverted = 0, LPpending = 0;
		int LPfranchiseSusp = 0;
		int corpSuspended = 0, corpActive = 0, corpDissolved = 0, corpOther = 0, corpSurr = 0, corpForf = 0;
		int ccLic = 0, ccPending = 0, ccClosed = 0, ccProbation = 0, ccUnlic = 0, ccOther = 0;
		int lptot = 0; int ctot = 0; int ccltot = 0;
		
		try {
			
			corp = new BufferedReader(new FileReader(CORPMASTER + "CORPMASTER_FMT.txt"));
			lp = new BufferedReader(new FileReader(LPMASTER + "LPMASTER_FMT.txt"));
			ccl = new BufferedReader(new FileReader(parentDir + "CCLD/CCLD_FMT.txt"));
			
			while ((tmp = ccl.readLine()) != null) {
				switch (tmp.substring(0,3)) {
				case "LIC":
					ccLic++;
					break;
				case "CLO":
					ccClosed++;
					break;
				case "PEN":
					ccPending++;
					break;
				case "ON ":
					ccProbation++;
					break;
				case "UNL":
					ccUnlic++;
					break;
				default:
				}
				ccltot++;
			}
			
			ccOther = ccltot - (ccLic + ccClosed + ccPending + ccProbation + ccUnlic);
			
			while ((tmp = corp.readLine()) != null) {
				switch (tmp.substring(0,3)) {
				case "Act":
					corpActive++;
					break;
				case "Dis": 
					corpDissolved++;
					break;
				case "Sus": 
					corpSuspended++;
					break;
				case "Sur": 
					corpSurr++;
					break;
				case "For": 
					corpForf++;
					break;
				default:
				}
				ctot++;
			}
			
			corpOther = ctot - (corpActive + corpDissolved + corpSuspended + corpSurr + corpForf);
			
			while ((tmp = lp.readLine()) != null) {
				switch (tmp.substring(0,3)) {
				case "Act": 
					LPactive++;
					break;
				case "Can":
					LPcancelled++;
					break;
				case "Dis": 
					LPdissolved++;
					break;
				case "Mer":
					LPmerged++;
					break;
				case "Con":
					LPconverted++;
					break;
				case "Pen":
					LPpending++;
					break;
				case "Fra":
					LPfranchiseSusp++;
					break;
				default: 
				}
				lptot++;
			}
			
			LPother = lptot - (LPactive + LPcancelled + LPdissolved + LPmerged + LPconverted + LPpending
					+ LPfranchiseSusp);
			
			System.out.println("Results for CORP: ");
			System.out.println("Active = " + corpActive + ", Dissolved = " + corpDissolved + ", Suspended = "
					+ corpSuspended + ", Surrendered = " + corpSurr + ", Forfeited = " + corpForf + ", Other = " + corpOther + ", TOTAL CORP = " + ctot);
			System.out.println("Results for LP:");
			System.out.println("Active = " + LPactive + ", Cancelled = " + LPcancelled + ", Dissolved = "
					+ LPdissolved + ", Franchise Tax Board Suspended: " + LPfranchiseSusp + ", Pending: " + LPpending + ", Converted out: " + LPconverted + ", Merged out: " + LPmerged + ", Other = " + LPother + ", TOTAL LP = " + lptot);
			System.out.println("Results for CCL: ");
			System.out.println("Licensed = " + ccLic + ", Pending = " + ccPending + ", Closed = " + ccClosed
					+ ", On Probation = " + ccProbation + ", Unlicensed = " + ccUnlic + ", Other = " + ccOther
					+ ", CCL TOTAL = " + ccltot);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main (String[] a) {
		System.out.println("Beginning: ");
		long start = System.currentTimeMillis();
		Statistics s = new Statistics();
		
		s.getStatusPercentages();

		System.out.println("Done. Elapsed time: " + (System.currentTimeMillis() - start) + " ms; " + 
				(System.currentTimeMillis() - start)/1000 + " seconds.");
	}


}

