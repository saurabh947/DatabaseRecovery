/***************************************************************
 ***************  CSE 5331 - DB 2 - Project 1  *****************
 ***************  UNDO/REDO Recovery Protocol  *****************
 ************				Made By:				************
 *********		Saurabh Agrawal (UTA ID: 1000954351)	********
 *********		Shivang Parekh (UTA ID: 1000990285)		********
 ***************************************************************/


// Import the necessary java files
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

// The main class where all the of the program is executed.
public class MainClass {

	// The string builder is used to extract the transaction ID in each operation
	static StringBuilder sb;
	// LSNcount keeps track of log records in main memory
	static int LSNcount = 0;
	// BigLSNcount keeps track of log records on disk
	static int BigLSNcount = 0;
	// cacheCount keeps track of cache records in main memory 
	static int cacheCount = 0;

	static HashMap<String, List<String>> cache_table = new HashMap<String, List<String>>();			// Cache table in memory
	static List<String> cacheList = new ArrayList<String>();

	static HashMap<Integer, List<String>> txn_table = new HashMap<Integer, List<String>>();			// Transaction table in memory
	static List<String> txnList;

	static HashMap<Integer, List<String>> log_memory = new HashMap<Integer, List<String>>();		// Log buffer in memory
	static List<String> logListMem = new ArrayList<String>();

	static HashMap<Integer, List<String>> log_disk = new HashMap<Integer, List<String>>();			// Log file on disk
	static List<String> logListDisk = new ArrayList<String>();

	static HashMap<String, Integer> data_table = new HashMap<String, Integer>();					// Data table on disk

	// The variables for reading the input file and writing the output file
	static BufferedReader inputFile = null;
	static PrintWriter outputFile = null;


	/* Main function is executed first.
	 * It reads the input file and calls appropriate function-
	 * -whenever an operation is encountered.
	 */
	public static void main(String[] args) throws IOException {

		// Integer which holds return values of functions
		int getVal;

		// Static declaration of Data Table on disk.
		// It is initialized to zero, for all (A, ..., Z) values.
		data_table.put("A", 0);	data_table.put("B", 0);	data_table.put("C", 0);	data_table.put("D", 0);	data_table.put("E", 0);	data_table.put("F", 0);
		data_table.put("G", 0);	data_table.put("H", 0);	data_table.put("I", 0);	data_table.put("J", 0);	data_table.put("K", 0);	data_table.put("L", 0);
		data_table.put("M", 0);	data_table.put("N", 0);	data_table.put("O", 0);	data_table.put("P", 0);	data_table.put("Q", 0);	data_table.put("R", 0);
		data_table.put("S", 0);	data_table.put("T", 0);	data_table.put("U", 0);	data_table.put("V", 0);	data_table.put("W", 0);	data_table.put("X", 0);
		data_table.put("Y", 0);	data_table.put("Z", 0);

		try {

			// Specify the paths for input & output files here!
			inputFile = new BufferedReader(new FileReader("C:\\MyPersonal\\Study\\UTA\\Summer 14\\DB 2\\Project 1\\input1.txt"));
			outputFile = new PrintWriter(new FileWriter("C:\\MyPersonal\\Study\\UTA\\Summer 14\\DB 2\\Project 1\\output1.txt"));

			// Update the output file.
			outputFile.println("=============== Schedule Begins ===============");
			outputFile.println("");
			String line;

			// Loop over input file and get each line (operation)
			outerloop:
				while ((line = inputFile.readLine()) != null) {

					// Convert each line to character array and get each character
					char[] charArray = line.toCharArray();
					char c1 = charArray[0];

					// Switch case over first character
					switch(c1) {
					case 'b':
						// BEGIN operation. Get transaction ID and call begin() function
						outputFile.println("");
						outputFile.println("Encountered operation " +line+ " BEGIN of transaction "  +charArray[1]+charArray[2]);
						begin(charArray[1], charArray[2]);
						break;

					case 'r':
						// READ operation. Get transaction ID and call read() function
						outputFile.println("");
						outputFile.println("Encountered operation " +line+ " READ by transaction "  +charArray[1]+charArray[2] + " of DATA_ITEM - " +charArray[4]);
						getVal = read(charArray[1], charArray[2], charArray[4]);
						if(getVal == 0)
						{
							outputFile.println("Error! Cache buffer full. Suspending operations!");
							break outerloop;
						}
						break;

					case 'w':
						// WRITE operation. Get transaction ID and call write() function
						outputFile.println("");
						outputFile.println("Encountered operation " +line+ " WRITE by transaction "  +charArray[1]+charArray[2] + " of DATA_ITEM - " +charArray[4]);
						outputFile.println("Value updated from - BFIM: " +charArray[6] + " to AFIM: " +charArray[8]);
						getVal = write(charArray[1], charArray[2], charArray[4], charArray[6], charArray[8]);
						if(getVal == 0)
						{
							outputFile.println("Error! Cache buffer full. Suspending operations!");
							break outerloop;
						}
						break;

					case 'c':
						// COMMIT operation. Get transaction ID and call commit() function
						outputFile.println("");
						outputFile.println("Encountered operation " +line+ " COMMIT of transaction "  +charArray[1]+charArray[2]);
						commit(charArray[1], charArray[2]);
						break;

					case 'e':
						// END operation. Get transaction ID and call commit() function
						outputFile.println("");
						outputFile.println("Encountered operation " +line+ " END of transaction "  +charArray[1]+charArray[2]);
						commit(charArray[1], charArray[2]);
						break;

					case 'C':
						// CHECKPOINT operation. Call checkpoint() function
						outputFile.println("");
						outputFile.println("Encountered operation " +line+ " CHECKPOINT");
						checkpoint();
						break;

					case 'F':
						// FAILURE operation. Call failure() function
						outputFile.println("");
						outputFile.println("Encountered operation " +line+ " FAILURE");
						failure();
						break;

					case 'A':
						outputFile.println("");
						outputFile.println("Encountered operation " +line+ " ABORT");
						//					abort();
						break;

					case 'L':
						// FORCE-WRITE operation. Call force() function
						outputFile.println("");
						outputFile.println("Encountered operation " +line+ " FORCE WRITE");
						force();
						break;

					}
				}

			// Update the output file
			outputFile.println("");
			outputFile.println("=============== Schedule Ends ===============");

		}

		// Finally close both the files and update in console.
		finally {
			if (inputFile != null) {
				inputFile.close();
				System.out.println("Input reading complete");
			}
			if (outputFile != null) {
				outputFile.close();
				System.out.println("Output writing complete");
			}
		}

//		System.out.println("Cache: " + cache_table);
//		System.out.println("Data on Disk: " + data_table);
	}


	/* This function is executed whenever a BEGIN operation is encountered.
	 * It updates Log table and Transaction table 
	 */
	public static void begin(char n1, char n2) {

		// Get the transaction ID from arguments of the method
		sb = new StringBuilder().append(n1).append(n2);
		int TID = Integer.parseInt(sb.toString());

		// Add the new record to the log buffer
		logListMem = new ArrayList<String>();
		logListMem.add(""+TID);
		logListMem.add(""+0);
		logListMem.add("begin");
		logListMem.add(null);
		logListMem.add(null);
		logListMem.add(null);
		LSNcount++;
		log_memory.put(LSNcount, logListMem);
		outputFile.println("\t-Updated the log table with a BEGIN record");

		// Check to see if the log buffer in memory is full
		if(LSNcount == 4)
		{
			// Log buffer is full, Force write to disk
			for (Entry<Integer, List<String>> ee : log_memory.entrySet()) {
				List<String> values = ee.getValue();

				logListDisk = new ArrayList<>(values);
				BigLSNcount++;
				log_disk.put(BigLSNcount, logListDisk);
			}

			// Clear the counters and the log buffer
			LSNcount = 0;
			log_memory.clear();

			outputFile.println("\t-Log buffer full. Force written to disk");
		}
		else
		{
			outputFile.println("\t-Log buffer NOT full");
		}


		// Update Transaction Table
		txnList = new ArrayList<String>();
		txnList.add(""+LSNcount);
		txnList.add("in progress");
		txn_table.put(TID, txnList);
		outputFile.println("\t-Updated the Transaction table with status: In Progress, for transaction " +TID);

	}


	/* This function is executed whenever a READ operation is encountered.
	 * It updates Log table, Transaction table, & cache table.
	 * Fetches a data item from data table if not present in cache.
	 */
	public static int read(char n1, char n2, char data) {

		int retVal;
		// Get the transaction ID from arguments of the method
		sb = new StringBuilder().append(n1).append(n2);
		int TID = Integer.parseInt(sb.toString());

		// Get last LSN from transaction table
		int last_lsn = Integer.parseInt(txn_table.get(TID).get(0));

		// Add the new record to the log buffer
		logListMem = new ArrayList<String>();
		logListMem.add(""+TID);
		logListMem.add(""+last_lsn);
		logListMem.add("read");
		logListMem.add(""+data);
		logListMem.add(null);
		logListMem.add(null);
		LSNcount++;
		log_memory.put(LSNcount, logListMem);
		outputFile.println("\t-Updated the log table with a READ record");

		// Check to see if the log buffer in memory is full
		if(LSNcount == 4)
		{
			// Log buffer is full, Force write to disk
			for (Entry<Integer, List<String>> ee : log_memory.entrySet()) {
				List<String> values = ee.getValue();

				logListDisk = new ArrayList<>(values);
				BigLSNcount++;
				log_disk.put(BigLSNcount, logListDisk);
			}

			// Clear the counters and the log buffer
			LSNcount = 0;
			log_memory.clear();

			outputFile.println("\t-Log buffer full. Force written to disk");
		}
		else
		{
			outputFile.println("\t-Log buffer NOT full");
		}

		//		System.out.println("read	"+log_memory);

		// Update Transaction Table
		txnList = new ArrayList<String>();
		txnList.add(""+LSNcount);
		txnList.add("in progress");
		txn_table.put(TID, txnList);
		outputFile.println("\t-Updated the Transaction table with status: In Progress, for transaction " +TID);
		//		System.out.println("read	"+txn_table);


		// Check to see if the cache buffer is full
		if(cacheCount != 9)
		{	// The buffer is NOT full

			if(cache_table.containsKey(""+data))
			{
				// If the cache buffer already has the data item required
				outputFile.println("\t-Data Item " + data + " found in cache table.");
			}
			else
			{
				if(!cache_table.isEmpty())
				{
					// If the cache buffer does NOT have required data item & it is not empty, then fetch it from data table - 
					cacheList = new ArrayList<String>();
					cacheList.add(""+TID);
					cacheList.add(""+0);
					cacheList.add(""+data_table.get(""+data));
					cache_table.put(""+data, cacheList);
					cacheCount++;
					outputFile.println("\t-Data Item " + data + " NOT found in cache table. Retrieved it from data table on disk and updated the cache table");
				}
				else
				{
					// If the cache table is empty, fetch the data item from data table.
					cacheList = new ArrayList<String>();
					cacheList.add(""+TID);
					cacheList.add(""+0);
					cacheList.add(""+data_table.get(""+data));
					cache_table.put(""+data, cacheList);
					cacheCount++;
					outputFile.println("\t-Cache is empty. Retrieved data item " + data +" from data table on disk and updated the cache table");
				}
			}
			retVal = 1;
		}
		// The buffer is full
		else
		{

			retVal = 0;
		}

		outputFile.println("\t-Reading operation complete");
		return retVal;

	}


	/* This function is executed whenever a WRITE operation is encountered.
	 * It updates Log table, Transaction table and Cache table.
	 * Fetches a data item from data table if not present in cache. 
	 */
	public static int write(char n1, char n2, char data, char first, char second) {

		// Get the transaction ID, before image and after image of the data, from arguments of the method
		sb = new StringBuilder().append(n1).append(n2);
		int TID = Integer.parseInt(sb.toString());
		int BFIM = (int) (first - '0');
		int AFIM = (int) (second - '0');
		int retVal;

		// Get last LSN from transaction table
		int last_lsn = Integer.parseInt(txn_table.get(TID).get(0));

		// Add the new record to the log buffer
		logListMem = new ArrayList<String>();
		logListMem.add(""+TID);
		logListMem.add(""+last_lsn);
		logListMem.add("write");
		logListMem.add(""+data);
		logListMem.add(""+BFIM);
		logListMem.add(""+AFIM);
		LSNcount++;
		log_memory.put(LSNcount, logListMem);
		outputFile.println("\t-Updated the log table with a WRITE record");

		// Check to see if the log buffer in memory is full
		if(LSNcount == 4)
		{
			// Log buffer is full, Force write to disk
			for (Entry<Integer, List<String>> ee : log_memory.entrySet()) {
				List<String> values = ee.getValue();

				logListDisk = new ArrayList<>(values);
				BigLSNcount++;
				log_disk.put(BigLSNcount, logListDisk);
			}

			// Clear the counters and the log buffer
			LSNcount = 0;
			log_memory.clear();

			outputFile.println("\t-Log buffer full. Force written to disk");
		}
		else
		{
			outputFile.println("\t-Log buffer NOT full");
		}


		// Update Transaction Table
		txnList = new ArrayList<String>();
		txnList.add(""+LSNcount);
		txnList.add("in progress");
		txn_table.put(TID, txnList);
		outputFile.println("\t-Updated the Transaction table with status: In Progress, for transaction " +TID);


		// Check to see if the cache buffer is full
		if(cacheCount != 9)
		{	// The buffer is NOT full

			if(cache_table.containsKey(""+data))
			{
				// If the cache buffer already has the data item required
				cacheList = new ArrayList<String>();
				cacheList.add(""+TID);
				cacheList.add(""+1);
				cacheList.add(""+AFIM);
				cache_table.put(""+data, cacheList);
				outputFile.println("\t-Data Item " + data + " found in cache table. New value "+ AFIM + " written to it");
			}
			else
			{
				if(!cache_table.isEmpty())
				{
					// If the cache buffer does NOT have required data item & it is not empty, then fetch it from data table - 
					cacheList = new ArrayList<String>();
					cacheList.add(""+TID);
					cacheList.add(""+1);
					cacheList.add(""+AFIM);
					cache_table.put(""+data, cacheList);
					cacheCount++;
					outputFile.println("\t-Data Item " + data + " NOT found in cache table. Retrieved it from data table on disk and updated the cache table");
				}
				else
				{
					// If the cache table is empty, fetch the data item from data table.
					cacheList = new ArrayList<String>();
					cacheList.add(""+TID);
					cacheList.add(""+1);
					cacheList.add(""+AFIM);
					cache_table.put(""+data, cacheList);
					cacheCount++;
					outputFile.println("\t-Cache is empty. Retrieved data item " + data +" from data table on disk and updated the cache table");
				}
			}
			retVal = 1;
		}
		// The buffer is full
		else
		{

			retVal = 0;
		}

		// Update the outputfile. Return cache buffer status
		outputFile.println("\t-Writing operation complete");
		return retVal;

	}


	/* This function is executed whenever a COMMIT operation is encountered.
	 * It updates Log table and Transaction table
	 */
	public static void commit(char n1, char n2) {

		// Get the transaction ID from arguments of the method
		sb = new StringBuilder().append(n1).append(n2);
		int TID = Integer.parseInt(sb.toString());

		// Get last LSN from transaction table
		int last_lsn = Integer.parseInt(txn_table.get(TID).get(0));

		// Add the new record to the log buffer
		logListMem = new ArrayList<String>();
		logListMem.add(""+TID);
		logListMem.add(""+last_lsn);
		logListMem.add("commit");
		logListMem.add(null);
		logListMem.add(null);
		logListMem.add(null);
		LSNcount++;
		log_memory.put(LSNcount, logListMem);
		outputFile.println("\t-Updated the Log table with a COMMIT record");

		// Check to see if the log buffer in memory is full
		if(LSNcount == 4)
		{
			// Log buffer is full, Force write to disk
			for (Entry<Integer, List<String>> ee : log_memory.entrySet()) {
				List<String> values = ee.getValue();

				logListDisk = new ArrayList<>(values);
				BigLSNcount++;
				log_disk.put(BigLSNcount, logListDisk);
			}

			// Clear the counters and the log buffer
			LSNcount = 0;
			log_memory.clear();

			outputFile.println("\t-Log buffer full. Force written to disk");
		}
		else
		{
			outputFile.println("\t-Log buffer NOT full");
		}

		/*
		// Update the data table on disk by getting values from cache table
		int newVal = 0;
		for (Entry<String, List<String>> ee : cache_table.entrySet()) {
			String key = ee.getKey();
			List<String> values = ee.getValue();

			// Set the dirty bit to 0 and get that data's value
			if((Integer.parseInt(values.get(1)) == 1) && (values.get(0) == ""+TID))
			{
				values.set(1, ""+0);
				newVal = Integer.parseInt(values.get(2));
			}
			data_table.put(key, newVal);
		}
		outputFile.println("\t-Updated the data table with updated values by transaction "+ TID);
		 */
		// Update Transaction Table
		txnList = new ArrayList<String>();
		txnList.add(""+LSNcount);
		txnList.add("committed");
		txn_table.put(TID, txnList);

		// Update the outputfile.
		outputFile.println("\t-Updated the Transaction table with status: Committed, for transaction " +TID);
		outputFile.println("\t-COMMIT/END operation complete");

	}


	/* This function is executed whenever a CHECKPOINT operation is encountered.
	 * It updates Log table, data table on disk and Cache table.
	 */
	public static void checkpoint() {

		// Add the new record to the log buffer
		logListMem = new ArrayList<String>();
		logListMem.add(null);
		logListMem.add(null);
		logListMem.add("begin checkpoint");
		logListMem.add(null);
		logListMem.add(null);
		logListMem.add(null);
		LSNcount++;
		log_memory.put(LSNcount, logListMem);
		outputFile.println("\t-Updated the Log table with a BEGIN CHECKPOINT record");

		// Check to see if the log buffer in memory is full
		if(LSNcount == 4)
		{
			// Log buffer is full, Force write to disk
			for (Entry<Integer, List<String>> ee : log_memory.entrySet()) {
				List<String> values = ee.getValue();

				logListDisk = new ArrayList<>(values);
				BigLSNcount++;
				log_disk.put(BigLSNcount, logListDisk);
			}

			// Clear the counters and the log buffer
			LSNcount = 0;
			log_memory.clear();

			outputFile.println("\t-Log buffer full. Force written to disk");
		}
		else
		{
			outputFile.println("\t-Log buffer NOT full");
		}


		// Update the data table on disk by getting values from cache table
		for (Entry<String, List<String>> ee : cache_table.entrySet()) {
			int newVal = 0;
			String key = ee.getKey();
			List<String> values = ee.getValue();

			// Set the dirty bit to 0 and get that data's value
			if(Integer.parseInt(values.get(1)) == 1)
			{
				values.set(1, ""+0);
				newVal = Integer.parseInt(values.get(2));
				//values.set(1, ""+0);
			}
			else
			{
				newVal = Integer.parseInt(values.get(2));
			}
			data_table.put(key, newVal);
		}

		// Update the outputfile.
		outputFile.println("\t-Values of Cache table written to data table on disk.");

		// Add the new record to the log buffer
		logListMem = new ArrayList<String>();
		logListMem.add(null);
		logListMem.add(null);
		logListMem.add("end checkpoint");
		logListMem.add(null);
		logListMem.add(null);
		logListMem.add(null);
		LSNcount++;
		log_memory.put(LSNcount, logListMem);
		outputFile.println("\t-Updated the Log table with a END CHECKPOINT record");

		// Check to see if the log buffer in memory is full
		if(LSNcount == 4)
		{
			// Log buffer is full, Force write to disk
			for (Entry<Integer, List<String>> ee : log_memory.entrySet()) {
				List<String> values = ee.getValue();

				logListDisk = new ArrayList<>(values);
				BigLSNcount++;
				log_disk.put(BigLSNcount, logListDisk);
			}

			// Clear the counters and the log buffer
			LSNcount = 0;
			log_memory.clear();

			outputFile.println("\t-Log buffer full. Force written to disk");
		}
		else
		{
			outputFile.println("\t-Log buffer NOT full");
		}

	}


	/* The following function is executed whenever FAILURE occurs in the system.
	 * Assumptions: 
	 * 1.) There are no operations after Failure.
	 */
	public static void failure() {

		int point = 0;
		int check = 0;
		String now = "";
		String getData = "";
		List<String> getID = new ArrayList<String>();

		outputFile.println("\t-Checking the log file...");

		forloop:
			// Check the log file for any record with "checkpoint"
			for(int x = log_disk.size(); x > 0; x--) {
//			for (Entry<Integer, List<String>> ee : log_disk.entrySet()) {
//				int key = log_disk.get(x);
				List<String> values = log_disk.get(x);

				// Check to see if the log table contains "checkpoint" record
				if(values.get(2) == "end checkpoint")
				{
					// Log table contains checkpoint record
					point = x + 1;
					check = 1;
					break forloop;
				}
				else
				{
					// Log table does NOT contain checkpoint record
					check = 0;
					point = 0;
				}
			}
//		System.out.println(point);
		// Print in the output file accordingly
		if(check == 1)
			outputFile.println("\t-Checkpoint record found");
		else
			outputFile.println("\t-Checkpoint record NOT found");


		// Get the IDs of the transactions that got committed
		for(int i = 1; i <= log_disk.size(); i++)
		{
			if(log_disk.get(i).get(2) == "commit")
			{
				getID.add(log_disk.get(i).get(0));
			}
		}

		// REDO operation
		for(int i = point; i <= log_disk.size(); i++)
		{
			// Check to see which transactions did "write" operation
			if(log_disk.get(i).get(2) == "write")
			{
				// REDO if the transaction has written something and has committed AFTER checkpoint
				if(getID.contains(log_disk.get(i).get(0)))
				{
					// Update Cache tablle
					cacheList = new ArrayList<>();
					cacheList.add(log_disk.get(i).get(0));
					cacheList.add(""+0);
					cacheList.add(log_disk.get(i).get(5));
					cache_table.put(log_disk.get(i).get(3), cacheList);

					// Update data table on disk
					logListDisk = new ArrayList<String>(log_disk.get(i));
					data_table.put(logListDisk.get(3), Integer.parseInt(logListDisk.get(5)));
					outputFile.println("\t-REDO done on transaction " + logListDisk.get(0));
				}
			}
			// Check to see which transactions did ONLY "Commit" operation
			else if (log_disk.get(i).get(2) == "commit")
			{
				int no = Integer.parseInt(log_disk.get(i).get(0));
				for(int j = i-1; j > 0; j--)
				{
					if((log_disk.get(j).get(2).equals("write")) && (log_disk.get(j).get(0).equals(""+no)))
					{
						getData = log_disk.get(j).get(3);
						now = log_disk.get(j).get(5);
						break;
					}
				}

				// Update Cache table
				cacheList = new ArrayList<>();
				cacheList.add(""+no);
				cacheList.add(""+0);
				cacheList.add(now);
				cache_table.put(getData, cacheList);

				// Update data table on disk
				logListDisk = new ArrayList<String>(log_disk.get(i));
				data_table.put(getData, Integer.parseInt(now));
				outputFile.println("\t-REDO done on transaction " + logListDisk.get(0));
			}
		}

		// UNDO operation
		for(int i = log_disk.size(); i > 0 ; i--)
		{
			// Check to see which transactions did "write" operation
			if(log_disk.get(i).get(2) == "write")
			{
				// UNDO if the transaction has written something and is NOT committed AFTER checkpoint
				if(!getID.contains(log_disk.get(i).get(0)))
				{
					// Update Cache table
					cacheList = new ArrayList<>();
					cacheList.add(log_disk.get(i).get(0));
					cacheList.add(""+0);
					cacheList.add(log_disk.get(i).get(4));
					cache_table.put(log_disk.get(i).get(3), cacheList);

					// Update data table
					logListDisk = new ArrayList<String>(log_disk.get(i));
					data_table.put(logListDisk.get(3), Integer.parseInt(logListDisk.get(4)));
					outputFile.println("\t-UNDO done on transaction " + logListDisk.get(0) + " for data item: " + logListDisk.get(3));
				}
			}
		}

		// Update the outputfile.
		outputFile.println("\t-Data table on disk updated");
		outputFile.println("\t-Cache table updated");
		outputFile.println("\t-UNDO/REDO operation complete!");
//		System.out.println("Committed list " + getID);
	}


	/* This function is called whenever 'L'(FORCE WRITE) is encountered.
	 * It flushes the log buffer to disk. 
	 */
	public static void force() {

		List<Integer> map = new ArrayList<>();
		int tempCount = BigLSNcount;

		// Log buffer is full, Force write to disk
		for (Entry<Integer, List<String>> ee : log_memory.entrySet()) {
			int key = ee.getKey();
			map.add(key);
			List<String> values = ee.getValue();

			logListDisk = new ArrayList<>(values);
			tempCount++;
			log_disk.put(tempCount, logListDisk);
		}
		// Reuse tempCount by setting it to zero. Update the outputfile.
		tempCount = 0;
		outputFile.println("\t-Log buffer flushed to disk");

	}

}