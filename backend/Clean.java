/* Clean.java
 * @author Daniel Kesner
 * Summer 2016
 * Used to clean up or reformat data files to be used
 * for CARR summer internship projects. 
 *  
 *  All public methods are those that provide major 
 *  functionality, i.e. replacing delimeters in a 
 *  .txt file, formatting files, etc.
 *  
 *  This class also contains several private helper
 *  methods which are either called in the larger
 *  public methods or were used for small tasks
 *  related to the overall project, i.e. estimating
 *  the number of characters per line, etc. Private
 *  methods contain fewer comments but are all extremely
 *  simple.
 * 
 */

package backend;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Clean {
	
	static String parentDir = "C:/Users/Daniel/Documents/CARR/Build_Tables/";
	static String LPMASTER = "C:/Users/Daniel/Documents/CARR/CASOS_Disk_1_LP_LLC/LPMASTER/";
	
	// For each line in a file, identify old delimeter, replace with new
	public static void replaceDelim (String csvPath, char oldDelim, char newDelim) {
		
		Scanner read;
		FileWriter fw;
		char[] cBuff;
		String out = "";
		boolean write = false;
		int ctr = 0;
		
		try {
			
			read = new Scanner(new File(csvPath));
			fw = new FileWriter(parentDir + "RECF_output.txt");
			cBuff = new char[100];
			
			// For each line in file
			while (read.hasNextLine()) {

				// Read in a line, convert to char[]
				cBuff = read.nextLine().toCharArray();
				
				// Find occurrences of ",LICENSED" or ",ON PROBATION"
				for (int i = 0; i < cBuff.length; i++) {
					
					// Scan the String char by char until you find ","; once you find
					// comma, check to see if String afterward is LICENSED or ON PROBATION
					if (cBuff[i] == oldDelim) {
						
						// If next word is 'Licensed'
						if (cBuff[i+1] == 'L' && cBuff[i+2] == 'I' && cBuff[i+3] == 'C'
								&& cBuff[i+4] == 'E' && cBuff[i+5] == 'N' && cBuff[i+6] == 'S'
								&& cBuff[i+7] == 'E' && cBuff[i+8] == 'D') {
							cBuff[i] = newDelim;	// replace old delimeter with new delim
							write = true;
						}
						
						// Else if next word is 'On probation'
						else if (cBuff[i+1] == 'O' && cBuff[i+2] == 'N' && cBuff[i+3] == ' ') {
							cBuff[i] = newDelim;
							write = true;
						}
						// add more attributes to select if you want
					}
					
					if (write == true) {
					// Convert edited char[] back to String, write to output file, repeat
					out = String.valueOf(cBuff);
					//System.out.println(out);
					fw.write(out);
					fw.write(System.lineSeparator());
					//System.out.println("Iteration: " + ctr++ + ", string: " + out);
					}
					write = false;
				}
				
			}
			read.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Returns number of lines in a .txt/.csv (i.e. rows in a SQL table)
	private static int countLines(String path) {
		int ct = 1;
		
		try {
		Scanner s = new Scanner(new File(path));
		while (s.hasNextLine()) {
			s.nextLine();
			ct++;
		}
		s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ct;
	}

	// Cleans up LPMASTER.txt to make it possible to store it
	// into a Oracle RDBMS table using the sqlldr utility
	// Removes extra spaces/tabs/other crap in an attempt to format
	// the .txt file in a regular, predictable way
	// Note: Writes formatted output to second file. Does not overwrite original.
	public static void clean(String inPath, String outPath) {
		
		BufferedInputStream bis;
		BufferedReader br;
		String sBuff = "";
		FileWriter fw;
		int i = 0;
		int numRecords = 0;
		
		try {
			
			bis = new BufferedInputStream(new FileInputStream(inPath));
			br = new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8));
			fw = new FileWriter(outPath);
			numRecords = getNumRecords("C:/Users/Daniel/Documents/CARR/CASOS_Disk_1_LP_LLC/LPMASTERCNT.txt");
			
			// For each record in the file (read until next line is null)
			for (sBuff = br.readLine(); sBuff != null; sBuff = br.readLine()) {
			
				sBuff = sBuff.replaceAll("\n", " ");	// convert non-terminating newlines to spaces
				sBuff = sBuff.replaceAll("\\s+", " ");	// trim extra spaces/tabs
			
			// add any extra formatting to the line you'd like to do here
			
			// Write the trimmed line to file
				fw.write(sBuff);						// write trimmed String to output file
				fw.write(System.lineSeparator());		// + \n
				if (i++ % 1000 == 0)					// print to console every 1000 writes
					System.out.println("Wrote line " + i + " to output file.");	// debugging
			}	// end for
			
			bis.close();
			fw.close();
			
		} catch (Exception e) {
			System.out.println("Exception thrown in Clean.clean(): ");
			System.out.println("Iteration: " + i);
			System.out.println("Number of records used: " + numRecords);
			System.out.println("Buffer at time of exception: " + sBuff);
			e.printStackTrace();
		}	
	}
	
	// Read LPMASTERCNT.txt
	private static int getNumRecords(String path) {
		
		Scanner s;
		int size = 0;
		
		try {
			
			s = new Scanner(new File(path));
			size = (int) s.nextInt();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}
	
	// Estimate length of one record in LPMASTER.txt
	// Assumes one space separation per token
	// Returns the length of a line of a String stored in some file
	private static int getStringLen(String path) {
		
		Scanner s;
		String str = "";
		
		try {
			
			s =  new Scanner(new File(path));
			str = (String) s.nextLine();
			
		} catch (Exception e) {}
		
		return str.length();
	}
	
	// Add line numbers before each line of a file
	private static void addLineNumbers(String path) {
		Scanner s;
		String sb = "";
		int ctr = 0;
		FileWriter fw;
		
		try {
			
			s = new Scanner(new File(path));
			fw = new FileWriter(LPMASTER + "LPMASTER_TMP.txt");
			
			while (s.hasNextLine()) {
				sb = s.nextLine();
				System.out.println(sb);
				sb = ctr++ + ": " + sb;
				System.out.println(sb);
				fw.write(sb);
				fw.write(System.lineSeparator());
			}
			
			
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
	
	public static void main (String [] a) {
		long start = System.currentTimeMillis();
		
		//replaceDelim(parentDir + "RECF_stripped.csv", ',', '|');
		
		//clean(LPMASTER + "LPMASTER.txt", LPMASTER + "LPMASTER_FORMATTED.txt");
		
		//addLineNumbers(LPMASTER + "LPMASTER_FORMATTED.txt");
		
	//	System.out.println(new String(readLine("C:/Users/Daniel/Documents/CARR/CASOS Disk 2 - Corps/CORPMASTER/CORPMASTER.txt")));
		
		System.out.println("Done. Total time elapsed: " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		
		}

}