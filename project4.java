/*
Pass 1 should construct the symbol table, the addresses associated with each 
instruction, addresses of each label. The input file will be in fixed 
format:
Col 1-8 label optional
Col 9 blank
Col 10 + optional
Col 11-17 mnemonic
Col 18 blank
Col 19 #, @ ... optional
Col 20-29 label, register, ',',X optional ...
Col 30-31 blank
Col 32-80 comments optional
*/

import java.util.*;
import java.lang.StringBuilder;
import java.text.DecimalFormat;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.lang.Iterable;
import java.io.*;
import java.time.*;

public class project4 
{
	public static void main(String args[]) throws Exception
	{
		//declare int for counting number of strings
		int n = 136; //set n equal to number of lines in SICOPS.txt
		int z = 0; //counting lines in input file
		int p = 0; //int for finding prime number for quad probing
		int regcount = 0; //keeping track of registers found in SICOPS.txt file
		Register[] regs = new Register[9]; //stores registers
		String fileName = "";
		ArrayList<String> literals = new ArrayList<String>();
		ArrayList<String> objectCodes = new ArrayList<String>();
		ArrayList<Symbol> output = new ArrayList<Symbol>();
		ArrayList<useBlock> blockArr = new ArrayList<useBlock>();

		//find first prime number larger than 2 * n..
		p = (2 * n) + 1;
		while(!primeCheck(p))
		{
			p++;
		}
		
		//instantiate hash table class with prime number
		SicOpTable SICKhash = new SicOpTable(p); //create hash table with size of prime number
		SicOp instr;
		Register reggie;

		//load SICK ops into hash table!
		try
		{
			//read lines from input
			Scanner input = new Scanner(new java.io.File("SICOPS.txt"));

			while(input.hasNextLine())
			{
				String line = input.nextLine();
				String spaces = line.replaceAll("\\s+", ""); //remove spaces
				//System.out.println(line);
				if (!spaces.equals("")) //check if it is NOT an empty line.
				{
					String trim = line.trim();
					String[] splitme = trim.split("\\s+");
					int length = splitme.length;
					
					if(length == 3)
					{
						instr = new SicOp(line);
						SICKhash.insert(instr);
					}
					else if(length == 2) //store registers into a separate array
					{
						reggie = new Register(line);
						regs[regcount] = reggie;
						regcount++;
					}
				}
			}
			input.close();
		}
		catch(Exception fnfex)
		{
			System.out.println(fnfex);
		}
		
		//Commented lines below are for debugging purposes
//		System.out.println("Printing register array: ");
//		for (int i = 0; i < regs.length; i++)
//		{
//			String thing1 = regs[i].getRegister();
//			String thing2 = regs[i].getOpcode();
//			System.out.println(thing1 + " " + thing2);
//		}
		
		//Count lines in input file so we know how big to make our label hashtable
		try
		{
			//read lines from input
			Scanner input = new Scanner(new java.io.File(args[0]));
			fileName = args[0];
			
			while(input.hasNextLine())
			{
				String line = input.nextLine();
				String spaces = line.replaceAll("\\s+", ""); //remove spaces

				if (!spaces.equals("")) //check if it is NOT an empty line.
				{
					z++;
				}
			}
			input.close();
		}
		catch(Exception fnfex)
		{
			System.out.println(fnfex);
		}
		
//		Path rename = Paths.get(args[0]).toAbsolutePath();
//		System.out.println(rename);
//		System.out.println(rename.getParent().resolve(rename.getFileName() + ".lst"));
		
		//trim directories off fileName if any
		fileName = grabFileName(fileName);
		//System.out.println(fileName);
		
		//find first prime number larger than 2 * n..
		p = (2 * z) + 1;
		while(!primeCheck(p))
		{
			p++;
		}
		
		//Instantiate Hashtable for Labels and Literals
		LabelTable LabelHash = new LabelTable(p);
		Pass1 p1 = new Pass1();
		boolean foundLTORG = false;
		
		//Call Pass1 to calculate addresses
		try
		{
			//read lines from input
			Scanner input = new Scanner(new java.io.File(args[0]));
			p1.calcAddr(input, output, literals, objectCodes, SICKhash, LabelHash, 
					blockArr, foundLTORG);
			input.close();
		}
		catch(Exception fnfex)
		{
			System.out.println(fnfex);
		}
		//System.out.println("Finished pass 1");
		//count the number of RESW and RESB's in output array (inefficient but it works)
		int reserved = 0;
		for (int j = 0; j < output.size(); j++)
		{
			if (output.get(j).getInstr().equals("RESW") || output.get(j).getInstr().equals("RESB"))
			{
				reserved++;
			}
		}
		//System.out.println("Reserved arrays: " + reserved);
		
		//store and pass END address for obj file BEFORE calculating literals 
		String endAddr = output.get(output.size() - 1).Address;
		
		//if LTORG not found print literals after END. Remember to calculate addresses
		if (foundLTORG == false)
		{
			//System.out.println("Entered no LTORG found if statement!");
			//Grab last linecount and last location from arraylist
			String locStr = output.get(output.size() - 1).Address;
			String linecount = output.get(output.size() -1).lineNum;
			linecount = linecount.replace("-", "");
			linecount = linecount.replace(" ", "");
						
			//Parse strings to ints
			int totalcount = Integer.parseInt(linecount);
			int loc = Integer.parseInt(locStr, 16);

			//sort literals
			Collections.sort(literals);
			Helpers.calcLit(literals, output, LabelHash, loc, totalcount, "main", blockArr);
			
			//update last location in main block
			String mainHex = output.get(output.size() - 1).Address; //caution: this will only be right if END statement was found!
			int mainAddr = Integer.parseInt(mainHex, 16);
			blockArr.get(0).lastLoc = mainAddr;
			
			//System.out.println("Finished no LTORG if!");
		}
		
		//LabelHash.printTablelbl();
		
		//Print Use block array for debugging purposes
//		System.out.println("\nUse Block array: " );
//		for (int i = 0; i < blockArr.size(); i++)
//		{
//			System.out.println(blockArr.get(i).myName + " " + blockArr.get(i).lastLoc);
//		}
//		System.out.println();
		//Instantiate Pass2
		Pass2 p2 = new Pass2();
		
		//Pass output arraylist to Pass 2
		p2.calcObj(output, SICKhash, LabelHash, objectCodes, regs, endAddr, reserved, blockArr);
		
		//System.out.println();
		
		//print arraylist of output
//		for (int i = 0; i < output.size(); i++)
//		{
//			//System.out.println("Entered final print for loop!");
//			if (output.get(i).getComment() == false && output.get(i).objCode.equals(""))
//			{
//				System.out.println(output.get(i).lineNum + output.get(i).Address + " " + output.get(i).objCode
//						+ "\t\t     " + output.get(i).getOriginal());
//			}
//			else if (output.get(i).getComment() == false && !output.get(i).objCode.equals(""))
//			{
//				System.out.println(output.get(i).lineNum + output.get(i).Address + " " + output.get(i).objCode
//						+ "\t     " + output.get(i).getOriginal());
//			}
//			else if (output.get(i).getComment() == true)
//			{
//				System.out.println(output.get(i).lineNum + "\t\t\t     " + output.get(i).getOriginal());
//			}
//		}
		
//		System.out.println();
//		
//		//print object Code array
//		System.out.println(".Obj File:\n");
//		for (int j = 0; j < objectCodes.size(); j++)
//		{
//			System.out.println(objectCodes.get(j));
//		}
//		System.out.println();
		//String input = args[0];
		//Path input = Paths.get(args[0]).toAbsolutePath();
//		System.out.println(input);
//		System.out.println(input + ".lst");
		
		boolean errorFound = false;
		
		//print output array and object code array to *.lst file and *.obj file.
		try
		{
			String input = args[0];
			String line = "";
			String original = "";
			String objShort = ""; //shortened object code for .lst file if applicable
			boolean error = false;
			List<String> lines = Arrays.asList(line);
			
			//Commented code below is for debugging
			//Path input = Paths.get(args[0]).toAbsolutePath();
//			System.out.println(input);
//			System.out.println(input.getParent().resolve(input.getFileName() + ".lst"));
			
			//create/overwrite output file(s)
			Path file = Paths.get(input + ".lst");
			Path file2 = Paths.get(input + ".obj");
			OutputStream out = Files.newOutputStream(file);
			OutputStream out2 = Files.newOutputStream(file2);
			
			//before looping through the output array, write down column labels to the .lst file
			line = "********************************************\n"
					+ "SIC/XE assembler\n"
					+ "version date 12/7/1990\n"
					+ "account: n00000000; "
					+ " xxxxxxxxxx" + " 2018\n"
					+ "********************************************\n"
					+ "ASSEMBLER REPORT\n" + "----------------\n"
					+ "     Loc   " + "Object Code       " + "Source Code\n"
					+ "     ---   " + "----------- " + "      -----------";
			lines = Arrays.asList(line);
			Files.write(Paths.get(input + ".lst"), lines, Charset.forName("UTF-8"), 
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			
			//loop to read and write from output array
			for (int i = 0; i < output.size(); i++)
			{		
				error = output.get(i).errFlag;
				original = output.get(i).getOriginal(); //grab original string into variable original
				objShort = output.get(i).objCode;
				
				if (error == false)
				{
					if (objShort.length() > 17)
					{
						objShort = objShort.substring(0, 17);
					}
					
					line = String.format("%1$-1s%2$-1s%3$-18s%4$-1s", output.get(i).lineNum, output.get(i).Address + " ",
							objShort, original);
					lines = Arrays.asList(line);
					//write the line to a file
					Files.write(Paths.get(input + ".lst"), lines, Charset.forName("UTF-8"), 
							StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				}
				else if (error == true)
				{
					errorFound = true;
					line = String.format("%1$-1s%2$-24s%3$-1s%4$-1s", output.get(i).lineNum, output.get(i).Address, original,
							"\n" + output.get(i).errPrint);
					lines = Arrays.asList(line);
					//write the line to a file
					Files.write(Paths.get(input + ".lst"), lines, Charset.forName("UTF-8"), 
							StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				}
			}
			
			//loop to read and write from object code array
			for (int j = 0; j < objectCodes.size(); j++)
			{						
				line = objectCodes.get(j);
				lines = Arrays.asList(line);
				//write the line to a file
				Files.write(Paths.get(input + ".obj"), lines, Charset.forName("UTF-8"), 
						StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			}
		}
		catch(Exception fnfex)
		{
			System.out.println(fnfex);
		}
		
		System.out.println("SIC/XE assembler\n" + 
				"version date 12/7/1990");
		
		if (errorFound == false)
		{
			System.out.println("Assembler report file: " + fileName + ".lst");
			System.out.println("          object file: " + fileName + ".obj");
		}
		else if (errorFound == true)
		{
			System.out.println("Errors found. Refer to " + fileName + ".lst");
		}
	}
	
	//this boolean method checks if a number is prime.
	//This is based on a method found in Robert Lafore's Text "Data Structures & Algorithms"
	static boolean primeCheck(int p)
	{
		for(int i = 2; (i * i <= p); i++)
		{
			if(p % i == 0)
				return false; //not prime
		}
		return true; //prime
	}
	
	static String grabFileName(String input)
	{
		String lastIndex = "name";
		
		//replace any backslashes with forward slashes so as to not break the code...
		input = input.replace("\\", "/");
		
		//split the string by forward slashes
		String[] split = input.split("/");
		
		//assign the Last index of split me to lastIndex and return it
		lastIndex = split[split.length - 1];
		
		return lastIndex;
	}
}
