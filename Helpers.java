import java.util.*;

public class Helpers 
{
	public static int calcLit(ArrayList<String> literals, ArrayList<Symbol> output, LabelTable LabelHash, int loc, 
			int totalcount, String currBlk, ArrayList<useBlock> blockArr)
	{
		//System.out.println("Entered calcLit method!!");
		
		//print literals for debugging
//		for (int z = 0; z < literals.size(); z++)
//		{
//			System.out.println(literals.get(z));
//		}
		
		//Figure out what Block we are in before jumping into the giant for loop below
		int blkPos = getBlock(blockArr, currBlk);
		
		for(int i = 0; i < literals.size(); i++)
		{
			String locS = String.format("%5s", Integer.toHexString(loc)).replace(' ', '0').toUpperCase();
			String noeqls = "";
			String dupecheck = literals.get(i);
			String shorty = dupecheck;
			
			if(dupecheck.contains("\'")) 
			{
				StringBuilder noEQL = new StringBuilder(""); //LABEL
				for (int j = 1; j < literals.get(i).length(); j++)
				{
					//append charatacters to stringbuilder
					noEQL.append(literals.get(i).charAt(j));
				}
				noeqls = noEQL.toString();

				
				//string for limiting max amount of characters to be printed in first column									
				if(dupecheck.length() > 10)
				{
					shorty = dupecheck.substring(0, 10);
				}
				
				//call method to calculate literal value and make sure it has even amount of digits
				int tmploc = litVal(noeqls);
				//System.out.println("Value of tmploc: " + tmploc);
				if (tmploc >= 0)
				{
					Symbol lit;
					String newLine = shorty + "\tBYTE\t" + noeqls;
					lit = new Symbol(newLine, true);
					lit.Address = locS;
					lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
					lit.Use = currBlk;
					LabelHash.insert(lit);
					output.add(lit);
					loc += tmploc;
					blockArr.get(blkPos).lastLoc = loc;
					//store into symbol table if not duplicate
//					if (LabelHash.search(dupecheck) == false)
//					{
//						Symbol lit;
//						String newLine = shorty + "\tBYTE\t" + noeqls;
//						lit = new Symbol(newLine, true);
//						lit.Address = locS;
//						lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
//						LabelHash.insert(lit);
//						output.add(lit);
//						loc += tmploc;
//					}
//					else if (LabelHash.search(dupecheck) == true)
//					{
//						//else if dupe is true, subtract addresses to check if it needs to be recreated.
//						//If it is bigger than 0x7FF we need to make it again
//						String hexAddr = "0x" + LabelHash.getAddress(dupecheck);
//						int oriAddr = Integer.decode(hexAddr);
//						int range = loc - oriAddr;
//						if (range > 2047 || range < -2048) //if out of pc range, insert anyway
//						{
//							Symbol lit;
//							String newLine = shorty + "\tBYTE\t" + noeqls;
//							lit = new Symbol(newLine, true);
//							lit.Address = locS;
//							lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
//							LabelHash.insert(lit);
//							output.add(lit);
//							loc += tmploc;
//						}
//					}
				}
				else if (tmploc == -1)
				{
					Symbol lit;
					String newLine = shorty + "\tBYTE\t" + noeqls;
					lit = new Symbol(newLine, true);
					lit.Address = locS;
					lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
					lit.errFlag = true;
					lit.Use = currBlk;
					lit.errPrint = "ERROR: Odd number of X bytes found in operand field [" + noeqls + "]";
					LabelHash.insert(lit);
					output.add(lit);
				}
				else if (tmploc == -2)
				{
					Symbol lit;
					String newLine = shorty + "\tBYTE\t" + noeqls;
					lit = new Symbol(newLine, true);
					lit.Address = locS;
					lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
					lit.Use = currBlk;
					lit.errFlag = true;
					lit.errPrint = "ERROR: Invalid hex digit found in the operand [" + noeqls + "]";
					LabelHash.insert(lit);
					output.add(lit);
				}
			}
			else if (!dupecheck.contains("\'"))
			{
				//store this as a WORD and parse any digits as hex when calculating addresses
				StringBuilder noQot = new StringBuilder(""); //LABEL
				for (int j = 1; j < literals.get(i).length(); j++)
				{
					//append charatacters to stringbuilder
					noQot.append(literals.get(i).charAt(j));
				}
				noeqls = noQot.toString();
				
				//string for limiting max amount of characters to be printed in first column									
				if(dupecheck.length() > 10)
				{
					shorty = dupecheck.substring(0, 10);
				}
				
				//call method to calculate literal value and make sure it has even amount of digits
				int tmploc = litVal(noeqls);
				//System.out.println("Value of tmploc: " + tmploc);
				if (tmploc != -1 && tmploc != -2)
				{
					Symbol lit;
					String newLine = shorty + "\tWORD\t" + noeqls;
					lit = new Symbol(newLine, true);
					lit.Address = locS;
					lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
					lit.Use = currBlk;
					LabelHash.insert(lit);
					output.add(lit);
					loc += tmploc;
					loc += 3;
					blockArr.get(blkPos).lastLoc = loc;
					//store into symbol table if not duplicate
//					if (LabelHash.search(dupecheck) == false)
//					{
//						Symbol lit;
//						String newLine = shorty + "\tWORD\t" + noeqls;
//						lit = new Symbol(newLine, true);
//						lit.Address = locS;
//						lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
//						LabelHash.insert(lit);
//						output.add(lit);
//						loc += tmploc;
//						loc += 3;
//					}
//					else if (LabelHash.search(dupecheck) == true)
//					{
//						//else if dupe is true, subtract addresses to check if it needs to be recreated.
//						//If it is bigger than 0x7FF we need to make it again
//						String hexAddr = "0x" + LabelHash.getAddress(dupecheck);
//						int oriAddr = Integer.decode(hexAddr);
//						int range = loc - oriAddr;
//						if (range > 2047 || range < -2048) //if out of pc range, insert anyway
//						{
//							Symbol lit;
//							String newLine = shorty + "\tWORD\t" + noeqls;
//							lit = new Symbol(newLine, true);
//							lit.Address = locS;
//							lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
//							LabelHash.insert(lit);
//							output.add(lit);
//							loc += tmploc;
//							loc += 3;
//						}
//					}
				}
				else if (tmploc == -1)
				{
					Symbol lit;
					String newLine = shorty + "\tWORD\t" + noeqls;
					lit = new Symbol(newLine, true);
					lit.Address = locS;
					lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
					lit.Use = currBlk;
					lit.errFlag = true;
					lit.errPrint = "ERROR: Odd number of X bytes found in operand field [=" + noeqls + "]";
					LabelHash.insert(lit);
					output.add(lit);
				}
				else if (tmploc == -2)
				{
					Symbol lit;
					String newLine = shorty + "\tWORD\t" + noeqls;
					lit = new Symbol(newLine, true);
					lit.Address = locS;
					lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
					lit.Use = currBlk;
					lit.errFlag = true;
					lit.errPrint = "ERROR: Invalid literal found [" + noeqls + "]";
					LabelHash.insert(lit);
					output.add(lit);
				}
				else if (tmploc == -3)
				{
					Symbol lit;
					String newLine = shorty + "\tWORD\t" + noeqls;
					lit = new Symbol(newLine, true);
					lit.Address = locS;
					lit.lineNum = String.format("%3s", Integer.toString(++totalcount)).replace(' ', '0') + "- ";
					lit.Use = currBlk;
					lit.errFlag = true;
					lit.errPrint = "ERROR: Invalid literal found [" + noeqls + "]";
					LabelHash.insert(lit);
					output.add(lit);
				}
			}
			//totalcount++;
		}
		return totalcount;
	}
	
	//Calculate value of literal to increment location addresses.
	//Only pass a String containing a literal with the equal sign stripped.
	public static int litVal(String line)
	{
		String lit = line; 
		
		char first = lit.charAt(0);
		int val = 0;
		
		if (first == 'C')
		{
			for (int i = 2; i < lit.length(); i++)
			{
				//check for end of literal single quote/apostrophe
				if (lit.charAt(i) == '\'')
				{
					break;
				}
				else
				{
					val += 1;
				}
			}
		}
		else if (first == 'X')
		{
			//first check if it's an even amount of digits
			int even = 0;
			for (int i = 2; i < lit.length(); i++)
			{
				//check for end of literal single quote/apostrophe
				if (lit.charAt(i) == '\'')
				{
					break;
				}
				else
				{
					even += 1;
				}
			}
			//check if even
			if (even % 2 == 0)
			{
				//now count values
				for (int i = 2; i < lit.length(); i++)
				{
					//check to make sure it is a hex value
					if (Character.toString(lit.charAt(i)).matches("[0-9A-F]+") || lit.charAt(i) == '\'')
					{
						//check for end of literal single quote/apostrophe
						if (lit.charAt(i) == '\'')
						{
							break;
						}
						else if (i % 2 == 0) //only increment by 1 when i is even.
						{
							val += 1;
						}
					}
					else
					{
						return -2; //return -2 if it finds non-hex digit
					}
				}
			}
			else
			{
				return -1;
			}
		}
		//first character is not a X or C. Assume it is a word. Increment by 0 and it will be incremented by 3 elsewhere as WORD
		else if (lit.matches("-?[0-9]+"))
		{
			return 0;
		}
		else
		{
			return -1; //return -2 for error
		}
		return val;
	}
	
	public static int litErr(String litchk)
	{
		/*
		 * Literals should be passed to this method WITHOUT the leading = sign...
		 * Example: X'1234' or C'ABCD'
		 * 
		 * MAP of return numbers for this method:
		 * 0 = no error found
		 * -1 = no quotes found
		 * -2 = missing trailing or starting quote
		 * -3 = literal not long enough in length (missing something)
		 * -4 = literal is a null string ""
		 */
		//System.out.println("Entered litErr method with: " + litchk);
		
		int errNumber = 0;
		int opLen = litchk.length();
		
		//for loop for debugging purposes
//		for (int i = 0; i < opLen; i++)
//		{
//			System.out.println("litChk position " + i + ": " + litchk.charAt(i));
//		}
		
		if (opLen >= 2)
		{
			//check for errors in the literal here
			if (litchk.charAt(0) == 'X' || litchk.charAt(0) == 'C')
			{
				if (!litchk.contains("\'"))
				{
					return -1;
				}
					
				if (litchk.charAt(opLen - 1) != '\'' 
						|| (litchk.charAt(opLen - 1) == '\'' && opLen == 2))
				{
					return -2;
				}
				else if (litchk.charAt(1) != '\'')
				{
					return -2;
				}
			}
		}
		else if (litchk.equals(""))
		{
			return -4;
		}
		else if (opLen < 2)
		{
			return -3;
		}
		
		return 0;
	}
	
	//This method determines if a line has a label or not.
	public static boolean hasLabel(String line)
	{
		if (line.length() >= 8)
		{
			line = line.substring(0,8);
		}

		//trim spaces from line and check if empty...
		String spaces = line.replaceAll("\\s+", ""); //remove spaces
		
		if (!spaces.equals("") && line.charAt(0) != '\t') //column 1-8 are not blank and first character not a tab.
		{
			return true;
		}
		
		return false;
	}
	
	public static String lblFilter(String lblAddr)
	{
		//Check for and remove @ or # from lblAddr here.
		if (lblAddr.charAt(0) == '#' || lblAddr.charAt(0) == '@')
		{
			lblAddr = lblAddr.substring(1, lblAddr.length()); //Cut out first character
		}
		//Check if it has a comma for indexing
		if (lblAddr.contains(","))
		{
			StringBuilder lbl = new StringBuilder(""); //LABEL
			for (int j = 0; j < lblAddr.length(); j++)
			{
				//append charatacters to stringbuilder
				if (lblAddr.charAt(j) != ',')
				{
				lbl.append(lblAddr.charAt(j));
				}
				else
				{
					break;
				}
			}
			lblAddr = lbl.toString();
		}
		
		return lblAddr;
	}
	
	public static int nextLoc(ArrayList<Symbol> output, int current, ArrayList<useBlock> blockArr)
	{
		//System.out.println("Entered nextLoc method!");
		int next = current + 1;
		int loc = -1; //if it returns -1 that is an error or next is out of output range
		String currBlk = output.get(current).Use;
		
		while (output.get(next).getComment() == true || 
				!output.get(next).getAddr().matches("[0-9A-F]+") || //find the next position in output that IS NOT a comment/error
				!output.get(next).Use.equals(currBlk))  //and is also in the same USE BLOCK!!
		{
			++next;
		}

		//System.out.println("Value of next line: " + output.get(next).lineNum);
		//avoid index errors by comparing size
		if (next < output.size())
		{
			String tmphex = output.get(next).getAddr();
			//Convert Address to an int
			loc = Integer.parseInt(tmphex, 16);
		}
		
		//System.out.println("Value of loc: " + loc);
		return loc;
	}
	
	public static int purifyLineCount(String linecount)
	{
		linecount = linecount.replace("-", "");
		linecount = linecount.replace(" ", "");
					
		//Parse strings to ints
		int totalcount = Integer.parseInt(linecount);
		
		return totalcount;
	}
	
	public static int getBlock(ArrayList<useBlock> blockArr, String passed)
	{
		for (int i = 0; i < blockArr.size(); i++)
		{
			if (blockArr.get(i).myName.equals(passed))
			{
				return i;
			}
		}
		
		return -1;
	}
}
