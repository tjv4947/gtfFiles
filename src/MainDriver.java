/*
 * Created on 12 February, 2015, 7:30 PM
 * Author:  Terry J. Violette
 * 
 *  Description:  Utility program to take GTF and text based coordinates file and read them in
 *  	to process them.  The coordinates file entries will be matched with the appropriate GTF entry (if any) and 
 *      generate a logFile.txt file and output.txt file showing each file processed.  
 *      There is addition diagnostic information written to the log file.  Program output is also written to the console 
 *      so that you can monitor progress as it is processing the files. 
 *  
 */


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.text.DateFormat;

public class MainDriver {
	
	private static String nl = System.getProperty("line.separator");   // could change this to unix on final version
	
    public static enum ProcessFileTypes {  // used in switch in processing ; could add additional file types 
    	txt,
    	gtf
    }
	// Files for logging  and output 
	private static Logger logFile = null ;
	private static Logger outFile = null;
	private static String Stars = "*********************************************************************************" ;
	
	// variables to check before processing chromosome coordinates & gtf file -- errors reported if both files not present
	private static boolean coordinatesFileRead = false ;
	private static boolean gtfFileRead = false ; 
	
	static List<coordinatesEntry> coordinatesList = new ArrayList<coordinatesEntry> () ;
	static ArrayList<gtfEntry> gtfIntronEntryList = new ArrayList<gtfEntry> () ;  // initial list to store introns
	static ArrayList<gtfEntry> gtfExonEntryList = new ArrayList<gtfEntry> () ; // initial list to store exons

	

// processing functions
	private static void processGTF (String inpLine, List<gtfEntry> inpGtfIntronEntryList, List<gtfEntry> inpGtfExonEntryList) {

		String inpChromosomeID ;
		String sourceType ;
		String expressionType ;
		boolean inpIsIntron = false ;
		long inpStartCodon = 0 ;
		long inpStopCodon = 0 ;
		String strand  ;
		String inpStubString ;  
		String gtfTokens [] ;

		if (inpLine != null) {
			gtfTokens = inpLine.split("\\t") ;  // tab separated columns
			inpChromosomeID = gtfTokens[0]  ;
			sourceType = gtfTokens[1]  ;
			expressionType = gtfTokens[2]  ;
			inpIsIntron = expressionType.equalsIgnoreCase("intron") ;
			inpStartCodon = Long.parseLong(gtfTokens[3]) ;
			inpStopCodon = Long.parseLong(gtfTokens[4]  ) ;
			strand = gtfTokens[6] ;  // skip prev '.' column
			inpStubString = gtfTokens[8]   ;   // skip prev '.' column 
			
			// assign value to list
			if(inpIsIntron) {  // store intron
				inpGtfIntronEntryList.add(new gtfEntry (inpChromosomeID, sourceType, expressionType, inpStartCodon, inpStopCodon, inpIsIntron, strand, inpStubString)) ;
			} else {   // store exon
				inpGtfExonEntryList.add(new gtfEntry (inpChromosomeID, sourceType, expressionType, inpStartCodon, inpStopCodon, inpIsIntron, strand, inpStubString)) ;
			}

		}
				
	}  // processGTF
	
	
	private static void processCoordinates(String inpLine, List<coordinatesEntry> inpCoordinatesList) {
		
			String coordTokens [] ;

			coordTokens = inpLine.split("\\t") ;  // tab separated columns
			inpCoordinatesList.add(new coordinatesEntry (coordTokens[0], Long.parseLong(coordTokens[1]))) ;  // store parsed chromosome name and position

	
	} // processCoordinates

	
	
	
public static int  matchGTFEntryList(String inpChromosomeID, long inpLocation, List<gtfEntry> inpGTFList) {
	   
   		boolean entryFound = false ;		
		int idx = 0 ;	
		
			while ((! entryFound) && (idx < inpGTFList.size())) {
		        if (inpGTFList.get(idx).chromosomeID.equals(inpChromosomeID)){
		        	if((inpLocation >= inpGTFList.get(idx).startCodon) && (inpLocation <= inpGTFList.get(idx).stopCodon)) {
						entryFound = true ;
		        		return idx  ; 
		        	}
		        }
		        // when list is sorted can halt when chromosomeID is less than next entry
		        if (inpGTFList.get(idx).chromosomeID.compareTo(inpChromosomeID) == 1) {  // value is greater
		        	// already gone past final chromosome entry so return
		        	return -1 ;
		        }
			        idx++ ;
			        if ((idx % 25000) == 0 ){
			        	System.out.print(".") ;  // this will print a dot to console for each 25000 entries to show processing is running
			        }
		        }

			return -1 ; //if not found above return -1 to signal not found

} // matchGTFEntryList
	
public static gtfEntry getGteEntryInfoFromList(int returnIndex, List<gtfEntry> inpGTFList) {
	
	gtfEntry retGTFEntry = new gtfEntry () ;
	
		retGTFEntry = inpGTFList.get(returnIndex) ;
		// could do verification and post processing if needed.
	
	return retGTFEntry;
	
}

public static void  printGteEntryInfo(gtfEntry inpGTFEntry) {

//	String inpChromosomeID, long inpStartCodon, long inpStopCodon, boolean inpIsIntron, String inpStubString
	writeToAllFiles("\t" + inpGTFEntry.chromosomeID +
					"\t" + inpGTFEntry.sourceType +
					"\t" + inpGTFEntry.expressionType +
					"\t" + inpGTFEntry.startCodon + 
					"\t" + inpGTFEntry.stopCodon +
					"\t" + inpGTFEntry.startCodon +
					"\t" + inpGTFEntry.strand +
					"\t" + inpGTFEntry.stubString + 

           			"\n") ;
	
}

	public static gtfEntry findCoordinate(String inpChromosomeID, long inpLocation, ArrayList<gtfEntry> inpGtfIntronEntryList, List<gtfEntry> inpGtfExonEntryList)  
	{    
	   
		boolean exonFound = false ;
		boolean intronFound = false ;
		int returnIndex = -1 ;
		
		gtfEntry retGTFEntry = null ;
		
			// check exon first
			returnIndex = matchGTFEntryList(inpChromosomeID, inpLocation,  inpGtfExonEntryList) ;
			if (returnIndex != -1) {
				exonFound = true ;
				retGTFEntry = getGteEntryInfoFromList(returnIndex, inpGtfExonEntryList) ;
				return retGTFEntry; 
			}
// writeToLogFile("\nExon not found") ;
			if (! exonFound) {  // exon not found check inton list
				returnIndex = matchGTFEntryList(inpChromosomeID, inpLocation,  inpGtfIntronEntryList) ; 
				if (returnIndex != -1) {
					intronFound = true ;
//writeToLogFile("\nIntron found") ;
				retGTFEntry = getGteEntryInfoFromList(returnIndex, inpGtfIntronEntryList) ;
				return retGTFEntry; 
			}
		}
//writeToLogFile("\n!!!!Not found in list") ;

	return retGTFEntry; // if not found above will return null entry

	}  // findCoordinate
	
	private static void processChromosomeMap(List<coordinatesEntry> inpCoordinatesList, ArrayList<gtfEntry> inpGtfIntronEntryList, List<gtfEntry> inpGtfExonEntryList) {
		
		gtfEntry retGTFEntry = null ;
		String inpChromosomeID = null ; 
		int matchFoundCnt = 0 ;
		int matchNotFoundCnt = 0 ;
		long inpLocation = 0; 

		writeToAllFiles("Number of Introns found:  "+ inpGtfIntronEntryList.size()) ;		
		writeToAllFiles("\nNumber of Exons found:  "+ inpGtfExonEntryList.size()) ;	

	// Walk through all coordinates
	for (int idx=0 ; idx <= inpCoordinatesList.size()-1 ; idx++) {
		
		inpChromosomeID = inpCoordinatesList.get(idx).chromosomeID ;
		inpLocation = inpCoordinatesList.get(idx).chromosomePos ;

		retGTFEntry = findCoordinate(inpChromosomeID, inpLocation, inpGtfIntronEntryList, inpGtfExonEntryList) ;  
	    if (retGTFEntry != null) {
	    	writeToAllFiles("\n>" + inpChromosomeID + " at location:  " + inpLocation) ;
	    	printGteEntryInfo(retGTFEntry) ; 
	    	matchFoundCnt++ ;
	    } else {
	    	writeToAllFiles("\nNo match found for: " + inpChromosomeID + " at location:  " + inpLocation) ;
	    	matchNotFoundCnt++ ;
	    }

	} //for
	
	writeToAllFiles("\nTotal matches found: " + matchFoundCnt + "\t Total NOT found:  " + matchNotFoundCnt) ;
    System.out.println("\n\n\n") ;
	
} // processChromosomeMap

	
	
	private static void processFile (File procFile, String fileName) {
	

		BufferedReader inpBuffReader = null ;
		
		String inpLine = null ;
		String inpFileType = null ;
		ProcessFileTypes fileType = null ;

		boolean moreToRead = true ;
		
		
		// used switch below to determine how to process file
		inpFileType = fileName.substring(fileName.lastIndexOf(".")+1,  fileName.length()) ;
		fileType =  ProcessFileTypes.valueOf(inpFileType.toLowerCase()) ;  
		
		int numEntriesRead = 0 ;  // number of entries read from file
		
		try{
			// test if there are no files in directory and quit with log message
			inpBuffReader 	= new BufferedReader(new FileReader (procFile));  // add in a loop


			if (inpBuffReader != null) {
				while (moreToRead) {
					inpLine = inpBuffReader.readLine() ;
					if (inpLine != null ) {
						switch (fileType) { 
							case txt: // coordinates file
									// one coordinate per line
									processCoordinates(inpLine, coordinatesList) ;
									numEntriesRead++ ;
								break ;
							case gtf: 
								processGTF(inpLine, gtfIntronEntryList, gtfExonEntryList) ; 
								numEntriesRead++ ;
							break ;
						} // switch
					} else {  // finished processing   
						moreToRead = false ;
						switch (fileType) { 
						case txt: 
							writeToLogFile("\n**  Count of coordinates processed:  "+  coordinatesList.size() + "\n\n") ;				
							break ;
						case gtf: 
							writeToLogFile("\n**  Total Count of gtf processed:  "+  numEntriesRead + "\n\n") ;
							break ;
						} // switch
						 
    				}
					
				}

			}
			
		}
			catch(Exception e) {
				e.printStackTrace();	
		} finally {
			// free up resources
            if(inpBuffReader != null)
            {
                try {
                	inpBuffReader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
		
	} // processFile
	

	private static void readFileDirectory(File fileDir) {
		
			try {
				// read file directory and populate list 
				// traverse directories and sub-directories				

			      if (fileDir.isDirectory()) 
			        {
			            String[] theFiles = fileDir.list();
			            for (int i=0; theFiles != null && i< theFiles.length; i++) 
			            {
			                System.out.println("Processing files from:  " + fileDir.getAbsolutePath());
			            	readFileDirectory(new File(fileDir, theFiles[i]));
			            }
			        }
			        if (fileDir.isFile()) 
			        {
			        	// determine file types and how to process
						writeToLogFile("\tFile:  " + fileDir.getName());
			        	if (fileDir.getName().startsWith(".")) {
			        		// hidden file or Mac file ; ignore  
			        		// System.out.println("ignoring file") ;
						} else if (fileDir.getName().endsWith ("txt")) {
							//writeToLogFile("\tFile:  " + fileDir.getName() + " is text format.");
			        		// process coordinates text file
							processFile (fileDir, fileDir.getName()) ;
							coordinatesFileRead = true ;
			        	} else if (fileDir.getName().endsWith ("gtf")) {
							writeToLogFile("\tFile:  " + fileDir.getName() + " is gtf format.");
			        		// process gtf file
							processFile (fileDir, fileDir.getName()) ;
							Collections.sort(gtfIntronEntryList, new Comparator<gtfEntry>(){
								public int compare(gtfEntry gtf1, gtfEntry gtf2) {
									String compoundIdx1 = gtf1.chromosomeID  + gtf1.startCodon ;
									String compoundIdx2 = gtf2.chromosomeID  + gtf2.startCodon ;
						            return  compoundIdx1.compareTo(compoundIdx2) ; 

								}
							}) ;
							Collections.sort(gtfExonEntryList, new Comparator<gtfEntry>(){
								public int compare(gtfEntry gtf1, gtfEntry gtf2) {
									String compoundIdx1 = gtf1.chromosomeID  + gtf1.startCodon ;
									String compoundIdx2 = gtf2.chromosomeID  + gtf2.startCodon ;
						            return  compoundIdx1.compareTo(compoundIdx2) ; 
								}
							}) ;
							gtfFileRead = true ;
						}  else {
							writeToLogFile("\tFile:  " + fileDir.getName() + " is unknown format.  File ignored.");
			        		// unknown file type -- ignore processing 
			        	}
			        }

				
			} catch(Exception e) {
				e.printStackTrace();	
			}
		
	} // readFileDirectory
	
// main	
	public static void main(String[] args) throws IOException
	{

		// setup file directory
		if (args.length == 0 ) {
			System.out.println();
			System.out.println(Stars) ;
			System.out.println("\n\tERROR:  No data file directory specified,  please correct this problem. \n\t\tProgram halted.\n") ;
			System.out.println(Stars) ;
			System.exit(-1) ;
		}
		String dirName = args[0] ;  // initially assuming all files in one directory otherwise will need to traverse directory
		File fileDir = new File(dirName) ;
		File [] fileList = fileDir.listFiles() ;
		String outFileDirectory = "." ; // will use current directory for output files.
		
		if (args.length > 1) {
			outFileDirectory = args[1] ;  // put second input argument as output file directory
		}
		logFile = new Logger(outFileDirectory +  "/logFile.txt" );
		outFile = new Logger(outFileDirectory +  "/outfile.txt" );
		
		writeToLogFile("=====================================================================" + nl);
		writeToLogFile("Began Processing Files at:  " + 
				DateFormat.getDateTimeInstance().format(new Date()) + nl);
		writeToLogFile("=====================================================================" + nl);
	

		writeToLogFile("Input File Directory:  " + args[0] + nl) ;	


		  
		if (fileList == null) {
			// There is no directory write out log message and quit
			writeToLogFile("Directory does not exist!!  Quitting." + nl) ;

	 } else if (fileList.length == 0) {
				// If no files write out log message and quit 
				writeToLogFile("Directory is empty!!  Quitting." + nl) ;

			} else {
				// everything is OK to process
	
				readFileDirectory(fileDir) ;

				// now that all files are processed determine if ChromosomeMap can be processed
		        if (coordinatesFileRead && gtfFileRead) {
		    		writeToAllFiles(Stars) ; 	
		        	writeToAllFiles("\n\nProcessing Chromosome Map.") ;			        	

						processChromosomeMap(coordinatesList, gtfIntronEntryList, gtfExonEntryList) ;
			        	
			        } else {
			        	writeToLogFile("Chromosome Map not processed due to missing information/file(s).") ;			        	
			        }

				}
		
		
		writeToLogFile("=====================================================================" + nl);
		writeToLogFile("Finished Processing Files at:  " + 
				DateFormat.getDateTimeInstance().format(new Date()) + nl);
		writeToLogFile("=====================================================================" + nl);
	
	} // main 

	
// convenience wrapper functions to write to files
	public static void writeToLogFile(String strToWrite) {		
		   masterWriteToFiles(strToWrite, 1) ;  // log file		
	} //writeToLogFile

	public static void writeToOutputFiles(String strToWrite) {
		   masterWriteToFiles(strToWrite, 2) ;  // just the output file 
	} // writeToOutputFiles

	public static void writeToAllFiles(String strToWrite) {
			   masterWriteToFiles(strToWrite, 3) ;  // all files
	}  // writeToAllFiles

	public static void masterWriteToFiles(String strToWrite, int whichFiles) {
		// routine to write out to logger and output files
		try {
				if (whichFiles == 1 || whichFiles == 3) {
					   logFile.write(strToWrite);
				}
				if (whichFiles == 2 || whichFiles == 3) {
					   outFile.write(strToWrite) ;
				}
			
			} 	catch(Exception e) {
				e.printStackTrace();	
			}
	}  // masterWriteToFiles

	
} // MainDriver	




class Logger
{
	private  BufferedWriter bufw ;
	
	public Logger(String file) 	{
		String path;
		
		try{	
			int idx = file.lastIndexOf('/');
			
			if(idx != 0) {
				path = file.substring(0, idx);
			
				File fpath = new File(path);
				if(fpath.exists() == false) {
					fpath.mkdirs() ;
				}
			}
			
			bufw = new BufferedWriter(new FileWriter(file)) ;
		}
			
		catch(Exception e) {
			e.printStackTrace() ;	
		}
	}
	
	public void write(String msg) throws IOException 	{
		bufw.write(msg) ;
		bufw.newLine() ;
		bufw.flush() ;
		System.out.println(msg) ;
	}

	

} // logger

	

