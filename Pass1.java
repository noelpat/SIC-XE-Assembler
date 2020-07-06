import java.util.*;

public class Pass1 
{
	//This class calculates addresses and is pass 1 of the assembler

	public void calcAddr(Scanner input, ArrayList<Symbol> output, ArrayList<String> literals, ArrayList<String> objectCodes, 
			SicOpTable SICKhash, LabelTable LabelHash, ArrayList<useBlock> blockArr, boolean foundLTORG)
	{
		//Declare variables needed
		String[] exceptions = {"START", "END", "BASE", "WORD", "RESW", "BYTE", "RESB", "LTORG", "USE", "EQU",
				"EXTDEF", "EXTREF", "CSECT"};
		int loc = 0;
		int linecount = 1; //count every non-blank line/non-comment line
		int totalcount = 1;
		int instrpos = 0;
		boolean foundstart = false;
		boolean errstart = false;
		boolean usedUse = false;
		boolean mainUSE = true;
		String baselbl = "";
		
		//always start with inserting main useBlock into Block Array position 0.
		useBlock main;
		main = new useBlock("main");
		blockArr.add(main);
		String prevBlk = "";
		String currBlk = "main";
		
		//System.out.println("Entered Pass 1");
		
		try
		{	
			while(input.hasNextLine())
			{
				String line = input.nextLine();
				//System.out.println("current line: " + line);
				String spaces = line.replaceAll("\\s+", ""); //remove spaces
				boolean theStart = false;
				boolean lblchk = false;
				boolean foundmat = false;
				boolean foundEx = false;
				boolean foundErr = false;
				boolean isComment = false;
				int mat = 0;
				int prev = 0;
				
				if (!spaces.equals(""))
				{
					if (spaces.charAt(0) == '.')
					{
						isComment = true;
					}
				}
				
				if (!spaces.equals("") && isComment == false) //check if it is NOT an empty line AND not a comment
				{	
					//interpret this non blank line
					String trim = line.trim();
					String[] splitme = trim.split("\\s+");
					int length = splitme.length;
					
					//System.out.println(line);
					
					if (spaces.equals("LTORG"))
					{
						foundLTORG = true;
					}
					else if (length >= 2)
					{
						lblchk = Helpers.hasLabel(line);
						
						if (lblchk == true && splitme[1].equals("LTORG"))
						{
							foundLTORG = true;
						}
						else if(lblchk == false && splitme[0].equals("LTORG"))
						{
							foundLTORG = true;
						}
					}
					
					//get starting location
					if (length >= 2)
					{
						if (splitme[0].equals("START") || splitme[1].equals("START"))
						{
							//Check to make sure it's not a line like 'START START 100'
							if (linecount == 1 && splitme[0].equals("START") && splitme[1].matches("^[0-9A-Fa-f]+"))
							{
								loc = Integer.parseInt(splitme[1], 16);
								foundstart = true;
								theStart = true;
							}
							else if (splitme.length >= 3) //avoid index error if they leave START number blank
							{
								if (linecount == 1 && splitme[1].equals("START") && splitme[2].matches("^[0-9A-Fa-f]+"))
								{
									loc = Integer.parseInt(splitme[2], 16);
									foundstart = true;
									theStart = true;
								}
							}
						}
					}
					
					if (foundstart == false && errstart == false)
					{
						errstart = true; //this prevents the error printing more than once!
						//System.out.println("WARNING: START statement not found.");
						foundErr = true;
					}
					
					//check if line has a label or not so we can know how to deal with array positions
					lblchk = Helpers.hasLabel(line);
					//System.out.println("Value of lblchk: " + lblchk);
					
					//determine where instruction is going to be in splitme array. This avoids having to create duplicate code.
					if (lblchk == false || length == 1)
					{
						instrpos = 0;
					}
					else if (lblchk == true)
					{
						instrpos = 1;
					}
					
					Symbol var;
					var = new Symbol(line, lblchk);
					//System.out.println("Created symbol!");
					
					//Search for instruction and determine if it is valid. Grab format if it is.
					if (SICKhash.search(splitme[instrpos]) == true)
					{
						mat = SICKhash.getFormat(splitme[instrpos]);
						foundmat = true;
					}
					else //check for exceptions
					{
						foundEx = false;
						for (int i = 0; i < exceptions.length; i++)
						{
							if (exceptions[i].equals(splitme[instrpos]))
							{
								//check for all exceptions that effect locations here (RESB && RESW)
								//otherwise, the bottom if else statements handle everything
								if (splitme[instrpos].equals("RESW"))
								{
									String resw = "";
									
									if (length >= (instrpos + 1)) //avoid index error on a blank
									{
										resw = splitme[instrpos+1];
									}
									//ensure it only contains numbers before parsing (avoid error)
									if (!resw.equals("") && resw.matches("^[0-9]+"))
									{
										int val = Integer.parseInt(resw);
										val *= 3;
										prev = loc + val;
									}
								}
								else if (splitme[instrpos].equals("RESB"))
								{
									String resb = "";
									
									if (length >= (instrpos + 1)) //avoid index error on a blank
									{
										resb = splitme[instrpos+1];
									}
									//ensure it only contains numbers before parsing (avoid error)
									if (!resb.equals("") && resb.matches("^[0-9]+"))
									{
										int val = Integer.parseInt(resb);
										prev = loc + val;
									}
								}
								else if (splitme[instrpos].equals("BASE"))
								{
									//grab the label in the operand position and store it in every symbol after BASE
									baselbl = splitme[instrpos + 1];
								}
								else if (splitme[instrpos].equals("USE"))
								{
									usedUse = true;
//									System.out.println("Made it inside USE if!");
//									System.out.println(totalcount);
									boolean mainChk = false; //we need something that doesn't start at TRUE
									int blkPos = -1;
									
									//check to see if there is a operand after USE
									if (splitme.length > (instrpos + 1))
									{
										if (splitme[instrpos + 1].charAt(0) != '.') //check if comment
										{
											mainUSE = false; //set to true by default
											mainChk = false;
										}
									}
									else
									{
										mainUSE = true;
										mainChk = true;
									}
									
									//System.out.println("Value of mainChk: " + mainChk);
									
									//decide what to do depending on if it was USE with no operand
									if (mainChk == true) //if it refers to main use store location if necessary
									{
										if (!currBlk.equals("main")) //means we are not currently inside main use block
										{
											//get location of current block in blockArr and store loc there
											blkPos = getBlock(blockArr, currBlk);
											if (blkPos != -1) //if block is found (it always SHOULD be)
											{
												//store current location address here
												blockArr.get(blkPos).lastLoc = loc;
											}
											loc = blockArr.get(0).lastLoc; //set loc = to main block address
											prevBlk = currBlk; //set previous block to the current block
											currBlk = "main"; //set current block the main block
										}
										else
										{
											//Update location in main
											blockArr.get(0).lastLoc = loc;
											//otherwise do nothing because we are already in main block
										}
									}
									else if (mainChk == false && splitme.length > (instrpos + 1)) //not main block
									{
										//System.out.println("Entered non-main block!");
										//create this useBlock if not already created
										//call method to search useBlock array.
										boolean exist = searchBlocks(blockArr, splitme[instrpos + 1]);
										
										if (exist == false) //if useBlock not found create it...
										{
											//System.out.println("Block not found. Creating new one...");
											//create new useBlock cuz we aint find this one yet
											useBlock addMe = new useBlock(splitme[instrpos + 1]);
											blockArr.add(addMe);
										}
									
										//store current useBlock address
										blkPos = getBlock(blockArr, currBlk);
										blockArr.get(blkPos).lastLoc = loc;
										
										prevBlk = currBlk; //set previous block to the current block
										currBlk = splitme[instrpos + 1]; //set current block the main block
										
										//set location to location inside current block
										blkPos = getBlock(blockArr, currBlk);
										loc = blockArr.get(blkPos).lastLoc;
									}	
								}
								foundEx = true;
								break;
							}
						}
					}
					
					//System.out.println("Value of foundEx: " + foundEx);
					
					//check to see if it was an invalid mneumonic
					if (foundmat == false && foundEx == false)
					{
						var.errFlag = true;
						var.errPrint = "********** ERROR: Unsupported opcode found in statement " +
								"\"" + splitme[instrpos] + "\"";
					}
					
					//if statement checks for literals in operand space
					if(length >= 2 && foundmat == true && foundEx == false) 
					{
						String litchk = splitme[instrpos+1];
						
						if (litchk.charAt(0) == '=')
						{
							boolean litdupe = false;
							//check for duplicates
							for (int i = 0; i < literals.size(); i++)
							{
								if (litchk.equals(literals.get(i)))
								{
									litdupe = true;
								}
							}
							if (litdupe == false)
							{
								//check for errors in the literal here
								if (litchk.charAt(1) == 'X' || litchk.charAt(1) == 'C')
								{									
									if (litchk.length() >= 2) //protect from index error when looking for quote..
									{
										int opLen = litchk.length();
										
										if (litchk.charAt(opLen - 1) != '\'' 
												|| (litchk.charAt(opLen - 1) == '\'' && opLen == 3)
												|| litchk.charAt(2) != '\'')
										{
											var.errFlag = true;
											
											if (var.errPrint.equals(""))
											{
												var.errPrint = "********** WARNING: Close quote missing in operand field; trailing quote assumed";
											}
											else
											{
												var.errPrint = var.errPrint + "\n" + "********** WARNING: Close quote missing in operand field; trailing quote assumed";
											}
										}
									}
								}
								
								literals.add(litchk);
								//check for it in the Label HashTable as well (but osprey doesn't do this!)
//								if (LabelHash.search(litchk) == false)
//								{
//									//store it into arraylist of literals to be sorted and printed after next LTORG
//									literals.add(litchk);
//								}
//								else if (LabelHash.search(litchk) == true)
//								{
//									//else if dupe is true, subtract addresses to check if it needs to be recreated.
//									//If it is bigger than 0x7FF we need to make it again
//									String hexAddr = "0x" + LabelHash.getAddress(litchk);
//									int oriAddr = Integer.decode(hexAddr);
//									int range = loc - oriAddr;
//									if (range > 2047) //if out of pc range, insert anyway
//									{
//										//store it into arraylist of literals to be sorted and printed after LTORG
//										literals.add(litchk);
//									}
//								}
							}
						}
					}
					
					//System.out.println("Finished checking for literals...");
					
					//Determine whether label is duplicate and add to LabelTable.
					if (lblchk == true && LabelHash.search(splitme[0]) == false)
					{
						grabLabel(line, loc, splitme, exceptions, length, SICKhash, LabelHash, currBlk);
					}
					else if (lblchk == true && LabelHash.search(splitme[0]) == true)
					{
						//System.out.println("ERROR: Duplicate Label " + '"' + splitme[0] + '"');
						var.errFlag = true;
						var.errPrint = "********** ERROR: Duplicate label found " + "\"" + splitme[0] + "\"";
					}
					
					//System.out.println("Finished grabbing label if any...");
					
					//If else statement here determines how much to add to location:
					if (foundmat == true && foundEx == false)
					{
						String tmphex = "";
						tmphex = String.format("%5s", Integer.toHexString(loc)).replace(' ', '0').toUpperCase();
						var.Address = tmphex;
						var.lineNum = String.format("%3s", Integer.toString(totalcount)).replace(' ', '0') + "- ";
						var.base = baselbl;
						var.Use = currBlk;
						output.add(var);
						loc += mat;
						
						if (currBlk.equals("main"))
						{
							blockArr.get(0).lastLoc = loc;
						}
					}
					else if (foundEx == true)
					{
						if (splitme[instrpos].equals("BYTE"))
						{
							int addme = 1;
							String tmphex = "";
							tmphex = String.format("%5s", Integer.toHexString(loc)).replace(' ', '0').toUpperCase();
							var.Address = tmphex;
							var.lineNum = String.format("%3s", Integer.toString(totalcount)).replace(' ', '0') + "- ";
							var.base = baselbl;
							var.Use = currBlk;
							
							//If operand contains no quotes turn error flag on.
							String qtChk = "";
							if(splitme.length - 1 > instrpos)
							{
								qtChk = splitme[instrpos + 1];
							}
							
							if (!qtChk.contains("\'"))
							{
								var.errFlag = true;
								var.errPrint = "********** ERROR: No quotes found in the operand field  [on line " + 
								var.lineNum + "]";
							}
							//If operand contains no closing quote turn error flag on
							else if (splitme.length - 1 > instrpos)
							{
								//System.out.println("Made it inside byte error check!");
								String byteOper = splitme[instrpos + 1]; //to make this code more readable
								int opLen = byteOper.length();
								
								if (byteOper.length() >= 2) //protect from index error when looking for quote..
								{
									if (byteOper.charAt(opLen - 1) != '\'' 
											|| (byteOper.charAt(opLen - 1) == '\'' && opLen == 2)
											|| byteOper.charAt(1) != '\'')
									{
										var.errFlag = true;
										
										if (var.errPrint.equals(""))
										{
											var.errPrint = "********** WARNING: Close quote missing in operand field; trailing quote assumed";
										}
										else
										{
											var.errPrint = var.errPrint + "\n" + "********** WARNING: Close quote missing in operand field; trailing quote assumed";
										}
									}
								}
							}
							
							output.add(var);
							
							if (var.errFlag == false)
							{
								if (splitme.length - 1 > instrpos) //avoid dat index error on a blank!
								{
									if (splitme[instrpos + 1].charAt(0) != '.') //verify it aint a comment!!!
									{
										addme = Helpers.litVal(splitme[instrpos + 1]);
									}
								}
								
								if (addme != -1)
								{
									loc += addme;
									if (currBlk.equals("main"))
									{
										blockArr.get(0).lastLoc = loc;
									}
								}
							}
						}
						else if (splitme[instrpos].equals("WORD"))
						{
							String tmphex = "";
							tmphex = String.format("%5s", Integer.toHexString(loc)).replace(' ', '0').toUpperCase();
							var.Address = tmphex;
							var.lineNum = String.format("%3s", Integer.toString(totalcount)).replace(' ', '0') + "- ";
							var.base = baselbl;
							var.Use = currBlk;
							output.add(var);
							loc += 3;
							if (currBlk.equals("main"))
							{
								blockArr.get(0).lastLoc = loc;
							}
						}
						else if (splitme[instrpos].equals("RESW") || splitme[instrpos].equals("RESB"))
						{
							String tmphex = "";
							tmphex = String.format("%5s", Integer.toHexString(loc)).replace(' ', '0').toUpperCase();
							var.Address = tmphex;
							var.lineNum = String.format("%3s", Integer.toString(totalcount)).replace(' ', '0') + "- ";
							var.base = baselbl;
							var.Use = currBlk;
							output.add(var);
							loc = prev;
							if (currBlk.equals("main"))
							{
								blockArr.get(0).lastLoc = loc;
							}
						}
						else if (splitme[instrpos].equals("LTORG"))
						{
							String tmphex = "";
							tmphex = String.format("%5s", Integer.toHexString(loc)).replace(' ', '0').toUpperCase();
							var.Address = tmphex;
							var.lineNum = String.format("%3s", Integer.toString(totalcount)).replace(' ', '0') + "- ";
							var.base = baselbl;
							var.Use = currBlk;
							output.add(var);
							
							//sort literals
							Collections.sort(literals);
							totalcount = Helpers.calcLit(literals, output, LabelHash, loc, totalcount, currBlk, blockArr);							
							//clear arraylist incase another LTORG shows up
							literals.clear();
							
							//grab new last location and last literal from output array...
							String locStr = output.get(output.size() - 1).Address;
							String byteCheck = output.get(output.size() - 1).getInstr();
							String lastLit = output.get(output.size() - 1).getOper();
							int update = -1;
							int errorAddr = Integer.parseInt(locStr, 16);
							
							if (byteCheck.equals("BYTE"))
							{
								update = Helpers.litVal(lastLit); //call litVal method to calculate value to add to next address
							}
							else if (byteCheck.equals("WORD"))
							{
								update = 3; //if it is a word then just add three.
							}
							
							if (update >= 0)
							{
								//Parse location to int and update loc..
								loc = Integer.parseInt(locStr, 16) + update;
							}
							else
							{
								loc = errorAddr; //if it is a error line, then just grab the address without incrementing anything.
							}
						}
						else if (splitme[instrpos].equals("END"))
						{
							//grab last address from main block
							int mainAddr = 0;
							if (usedUse == true)
							{
								mainAddr = blockArr.get(0).lastLoc;
							}
							else
							{
								mainAddr = loc;
							}
							
							String tmphex = "";
							tmphex = String.format("%5s", Integer.toHexString(mainAddr)).replace(' ', '0').toUpperCase();
							var.Address = tmphex;
							var.lineNum = String.format("%3s", Integer.toString(totalcount)).replace(' ', '0') + "- ";
							var.base = baselbl;
							var.Use = currBlk;
							output.add(var);
						}
						else
						{
							String tmphex = "";
							tmphex = String.format("%5s", Integer.toHexString(loc)).replace(' ', '0').toUpperCase();
							var.Address = tmphex;
							var.lineNum = String.format("%3s", Integer.toString(totalcount)).replace(' ', '0') + "- ";
							var.base = baselbl;
							var.Use = currBlk;
							output.add(var);
						}
					}
					else //we haven't accounted for whatever is going on in this line...
					{
						String tmphex = "";
						tmphex = String.format("%5s", Integer.toHexString(loc)).replace(' ', '0').toUpperCase();
						var.Address = tmphex;
						var.lineNum = String.format("%3s", Integer.toString(totalcount)).replace(' ', '0') + "- ";
						var.base = baselbl;
						var.Use = currBlk;
						output.add(var);
					}					
					linecount++;
					
					//if else block for error messages. Will use and update the output arraylist from here.
					//could be an issue for ltorg statements though....
					
					//checks if start is missing
					if (errstart == true && foundErr == true)
					{
						output.get(0).errFlag = true;
						output.get(0).errPrint = "********** WARNING: START statement not found.";
					}
					
					//checks if end is missing
					
//					if (!splitme[instrpos].equals("LTORG"))
//					{
//						//check for all errors here
//
//					}
				}
				else if (isComment == true)
				{
					Symbol var;
					var = new Symbol(line, lblchk);
					var.lineNum = String.format("%3s", Integer.toString(totalcount)).replace(' ', '0') + "- ";
					var.base = baselbl;
					var.Use = currBlk;
					output.add(var);
				}
				totalcount++;
			}
			input.close();
			
			//check to see if arrayList output contains end statement
			boolean endFound = false;
			for (int i = 0; i < output.size(); i++)
			{
				if (output.get(i).getInstr().equals("END"))
				{
					endFound = true;
				}
			}
			//if end statement not found add error message to final output symbol object
			if (endFound == false)
			{
				output.get(output.size() - 1).errFlag = true;
				output.get(output.size() - 1).errPrint = "********** WARNING: END statement not found";
			}
		}
		catch(Exception fnfex)
		{
			System.out.println(fnfex);
		}
	}

	public int getBlock(ArrayList<useBlock> blockArr, String passed)
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
	
	public boolean searchBlocks(ArrayList<useBlock> blockArr, String passed)
	{
		for (int i = 0; i < blockArr.size(); i++)
		{
			if (blockArr.get(i).myName.equals(passed))
			{
				return true;
			}
		}
		
		return false;
	}
	
	//This method grabs labels and adds them to the LabelTable.
	//This method can only be used when you are sure a line has a label. Otherwise,
	//it will throw the whole program off.
	public void grabLabel(String line, int loc, String[] splitme, String[] exceptions, 
			int length, SicOpTable SICKhash, LabelTable LabelHash, String currBlk)
	{
		//If position 2 is an instruction, assume line has a label and store it
		if (length >= 2)
		{
			if (SICKhash.search(splitme[1]) == true)
			{
				Symbol var;
				var = new Symbol(line, true);
				String tmphex = Integer.toHexString(loc);
				var.Address = tmphex;
				var.Use = currBlk;
				LabelHash.insert(var);
			}
			else if (SICKhash.search(splitme[1]) == false) //check for exceptions
			{
				//check for variable declarations like WORD or BYTE
				boolean foundEx = false;
				for (int i = 0; i < exceptions.length; i++)
				{
					if (exceptions[i].equals(splitme[1]))
					{
						//check for all exceptions that effect locations here (RESB && RESW)
						//otherwise, the bottom if else statements handle everything
						foundEx = true;
						break;
					}
				}
				if (foundEx == true)
				{
					Symbol var;
					var = new Symbol(line, true);
					String tmphex = Integer.toHexString(loc);
					var.Address = tmphex;
					var.Use = currBlk;
					LabelHash.insert(var);
				}
			}
		}
	}
}
