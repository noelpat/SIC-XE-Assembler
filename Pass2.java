import java.util.*;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class Pass2 
{
	public void calcObj(ArrayList<Symbol> output, SicOpTable SICKhash, LabelTable LabelHash, ArrayList<String> objectCodes, 
			Register[] regs, String endAddr, int reserved, ArrayList<useBlock> blockArr)
	{
		String[] exceptions = {"START", "END", "BASE", "WORD", "RESW", "BYTE", "RESB", "LTORG", "USE", "EQU",
				"EXTDEF", "EXTREF", "CSECT"};
		//Directives we can skip over when calculating object code...
		String[] directives = {"START", "END", "BASE", "RESW", "RESB", "LTORG", "USE", "EQU",
		"EXTDEF", "EXTREF", "CSECT"};
		int resFound = 0;
		
		//System.out.println("Entered pass 2.");
		
		for (int i = 0; i < (output.size() - 0); i++)
		{
			//grab what is needed into variable names to make code more readable
			String lbl = output.get(i).getLabel();
			String instr = output.get(i).getInstr();
			String oper = output.get(i).getOper();
			
			//check for invalid label here first before getting into the BS below
			if (output.get(i).getComment() == false)
			{
//				System.out.println("\nLabel: " + lbl);
//				System.out.println("Instruction: " + instr);
//				System.out.println("Operand: " + oper);
//				System.out.println();
				
				if (SICKhash.getFormat(instr) == 3 || SICKhash.getFormat(instr) == 4)
				{
					if (!oper.equals("")) //avoid index error on a blank
					{
						String grabNum = Helpers.lblFilter(oper);
						
						if (!grabNum.matches("-?[0-9]+")) //if grabNum isn't a number it must be a label...
						{
							//special case: check for shortened literals in the Label Table as well:
							if (oper.charAt(0) == '=' && oper.length() > 10)
							{
								grabNum = oper.substring(0, 10);
							}
							
							//now check for this label in the labeltable
							if (LabelHash.search(grabNum) == false && !instr.equals("RSUB")
									&& !instr.equals("*RSUB"))
							{
								output.get(i).errFlag = true;
								output.get(i).errPrint = "********** ERROR: Operand not found in symbol table";
							}
						}
					}
					else if (oper.equals("")) //operand is equal to null. Mark error unless it is RSUB or *RSUB
					{
						if (!instr.equals("RSUB") && !instr.equals("*RSUB"))
						{
							//System.out.println("Entered blank operand error if with value: " + instr);
							output.get(i).errFlag = true;
							output.get(i).errPrint = "********** ERROR: Operand not found in symbol table";
						}
					}
				}
			}
			
			//System.out.println("Value of current errFlag: " + output.get(i).errFlag);
			if (output.get(i).getComment() == false && output.get(i).errFlag == false)
			{				
//				System.out.println("\nLabel: " + lbl);
//				System.out.println("Instruction: " + instr);
//				System.out.println("Operand: " + oper);
//				System.out.println();
				
				int OPcode = -1;				
				//System.out.println("Line is not a blank or a comment. Calculating object code...");
				boolean dirFound = false;
				
				//if we see an assembler directive we need to skip object code and update the array with an appropriate amount of tabs
				for (int j = 0; j < directives.length; j++)
				{
					if (directives[j].equals(instr))
					{
						if (instr.equals("START"))
						{
							//add starting location to object codes array
							//add leading 0's to starting location before adding to obj array
							int loc = Integer.parseInt(output.get(i).getOper(), 16);
							String objStrt = String.format("%6s", Integer.toHexString(loc)).replace(' ', '0').toUpperCase();
							objectCodes.add(objStrt);
							if (reserved > 0)
							{
								objectCodes.add("000000");
							}
							else if (reserved == 0)
							{
								objectCodes.add(objStrt);
							}
						}
						if (instr.equals("RESW") || instr.equals("RESB"))
						{
							++resFound; //increment the number of RESW and RESB found
							
							objectCodes.add("!"); //add ! to the object code
							
							//get next address after resw and add it to obj code
							if (i <= (output.size() - 1)) //avoid a potential index error
							{
								String getNext = output.get(i + 1).Address;
								getNext = String.format("%6s", getNext).replace(' ', '0').toUpperCase();
								objectCodes.add(getNext);
							}
							
							if (resFound < reserved)
							{
								objectCodes.add("000000");
							}
							else if (resFound == reserved)
							{
								objectCodes.add(objectCodes.get(0));
							}
							
							break;
						}
						dirFound = true;
					}
				}
//				System.out.println("Finished directives for loop!");
//				System.out.println(dirFound);
				if (dirFound == false)
				{
					//Grab opcode from SICKhash
					String OPs = SICKhash.getOPcode(instr);
					
					if (!OPs.equals("")) //Avoid error when no opcode is found
					{
						//System.out.println("Entered giant obj code if!");
						
						if (SICKhash.getFormat(instr) != 1 && !instr.equals("RSUB"))
							OPcode = calcOP(OPs, instr, oper, SICKhash);
						
						String OPcodeStr = String.format("%2s", Integer.toHexString(OPcode)).replace(' ', '0').toUpperCase();
						String disphex = "";
						String objCode = "";
						String flag = "";
						int dispChk = -1;
						Displacement disp; //create new Displacement object for checking if it uses Base or not.
						
						//System.out.println("Finishing calculating OPcode!");
						
						//Call methods to Calculate displacement if any.
						if (SICKhash.getFormat(instr) == 3) //make sure it is format 3 instruction. Format 2 and 4 don't have displacement
						{
							//System.out.println("Entered format 3 if!");
							
							String grabNum = "";
							if (!oper.equals(""))
							{
								grabNum = Helpers.lblFilter(oper);
							}
							//check if RSUB or not
							if (instr.equals("RSUB"))
							{
								OPcodeStr = "4F";
								flag = "0";
								disphex = "000";
							}
							else if (instr.equals("*RSUB"))
							{
								OPcodeStr = "4C";
								flag = "0";
								disphex = "000";
							}
							else if (oper.charAt(0) == '#' && grabNum.matches("-?[0-9]+") && instr.charAt(0) != '*' 
									|| oper.charAt(0) == '@' && grabNum.matches("-?[0-9]+") && instr.charAt(0) != '*')
							{
								//parse int from string
								int numbers = Integer.parseInt(grabNum);
								
								//convert numbers to hex and trim to disphex variable
								if (numbers < 0)
								{
									disphex = String.format("%3s", Integer.toHexString(numbers)).replace(' ', '0').toUpperCase();
									disphex = disphex.substring(5, disphex.length()); //cut out leading F's
								}
								else
								{
									disphex = String.format("%3s", Integer.toHexString(numbers)).replace(' ', '0').toUpperCase();
								}
								
								flag = checkFlags(SICKhash, LabelHash, oper, instr, false, true);
							}
							else if (instr.charAt(0) == '*') //sic instruction
							{
								flag = checkFlags(SICKhash, LabelHash, oper, instr, false, true);
								
								if (grabNum.matches("-?[0-9]+"))
								{
									if (oper.charAt(0) == '#' || oper.charAt(0) == '@')
									{
										int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
										output.get(i).errFlag = true;
										output.get(i).errPrint = "********** ERROR: Immediate/Indirect addressing used with SIC instruction"
												+ "[on line " + lineNumber + "]";
									}
									else
									{
										//parse int from string
										int numbers = Integer.parseInt(grabNum);
										
										//convert numbers to hex and trim to disphex variable
										if (numbers < 0)
										{
											disphex = String.format("%3s", Integer.toHexString(numbers)).replace(' ', '0').toUpperCase();
											disphex = disphex.substring(5, disphex.length()); //cut out leading F's
										}
										else
										{
											disphex = String.format("%3s", Integer.toHexString(numbers)).replace(' ', '0').toUpperCase();
										}
									}
								}
								else //if it's not a number it must be a label...
								{
									boolean indexErr = indexingErr(oper);
									boolean indexErr2 = indexingErr2(oper);
									if (indexErr == true)
									{
										int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
										output.get(i).errFlag = true;
										output.get(i).errPrint = "********** ERROR: Use of X register with immediate/indirect addressing not allowed "
												+ "[on line " + lineNumber + "]";
									}
									
									if (indexErr2 == true)
									{
										int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
										output.get(i).errFlag = true;
										output.get(i).errPrint = "********** ERROR: Operand not found in symbol table "
												+ "[on line " + lineNumber + "]";
									}
									
									if (output.get(i).errFlag == false)
									{
										boolean exist = LabelHash.search(grabNum);
										
										if (oper.charAt(0) == '#' 
												|| oper.charAt(0) == '@')
										{
											int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
											output.get(i).errFlag = true;
											output.get(i).errPrint = "********** ERROR: Immediate/Indirect addressing used with SIC instruction"
													+ "[on line " + lineNumber + "]";
										}
										if (exist == true)
										{
											int purify = Integer.parseInt(LabelHash.getAddress(grabNum), 16);
																				
											String lblloc = String.format("%3s", Integer.toHexString(purify)).replace(' ', '0').toUpperCase();
											disphex = lblloc;
										}
										else
										{
											//throw error
											int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
											output.get(i).errFlag = true;
											output.get(i).errPrint = "********** ERROR: Operand not found in symbol table " + "[on line " + lineNumber + "]";
										}
									}
								}
							}
							else
							{
								//System.out.println("Entered f3 inner else statement!");
								//check if it is using indexing with indirect or immediate
								boolean indexErr = indexingErr(oper);
								boolean indexErr2 = indexingErr2(oper);
								if (indexErr == true)
								{
									int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
									output.get(i).errFlag = true;
									output.get(i).errPrint = "********** ERROR: Use of X register with immediate/indirect addressing not allowed "
											+ "[on line " + lineNumber + "]";
								}
								
								if (indexErr2 == true)
								{
									int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
									output.get(i).errFlag = true;
									output.get(i).errPrint = "********** ERROR: Operand not found in symbol table "
											+ "[on line " + lineNumber + "]";
								}
								
								if (output.get(i).errFlag == false)
								{
									boolean noLblIndex = false;
									//check to see if it uses indexing with a number...
									if (oper.contains(",") && grabNum.matches("-?[0-9]+"))
									{
										//parse int from string
										int numbers = Integer.parseInt(grabNum);
										
										//convert numbers to hex and trim to disphex variable
										if (numbers < 0)
										{
											disphex = String.format("%3s", Integer.toHexString(numbers)).replace(' ', '0').toUpperCase();
											disphex = disphex.substring(5, disphex.length()); //cut out leading F's
										}
										else
										{
											disphex = String.format("%3s", Integer.toHexString(numbers)).replace(' ', '0').toUpperCase();
										}
										
										flag = "8";
										noLblIndex = true;
									}
									
									if (noLblIndex == false)
									{
										//System.out.println("Entered format 3 displacement if with operand: " + oper);
										disp = new Displacement(output.get(i), output, i, LabelHash, SICKhash, blockArr);
										//System.out.println("Finished and created displacement object!");
										//Call method to Calculate flags. Check if in PC range or BASE range here as well.
										if (disp.error == false)
										{
											flag = checkFlags(SICKhash, LabelHash, oper, instr, disp.usedBase, disp.noLblFound);							
										}
										
										//Convert and Trim displacement for object code
										if (disp.dispInt < 0 && disp.dispHex.length() >= 6)
										{
											disphex = disp.dispHex.substring(5, disp.dispHex.length()); //trim the leading F's from negative hex...
										}
										else
										{
											disphex = disp.dispHex;
										}
									}
								}
							}
							//System.out.println("Value of flag: " + flag);
							//Concatenate OPcode hex string and disphex string together
							objCode = OPcodeStr + flag + disphex;
							
							if (output.get(i).errFlag == false)
							{
								output.get(i).objCode = objCode;
								objectCodes.add(objCode);
							}
						}
						//make special else if statements for format 2 and 4 here...
						else if (SICKhash.getFormat(instr) == 4)
						{
							//Flag will always be 1001 or 0001
							String grabNum = Helpers.lblFilter(oper);
							if (oper.charAt(0) == '#' && grabNum.matches("-?[0-9]+")
									|| oper.charAt(0) == '@' && grabNum.matches("-?[0-9]+"))
							{
								//parse int from string
								int numbers = Integer.parseInt(grabNum);
								
								//convert numbers to hex and trim to disphex variable
								if (numbers < 0)
								{
									disphex = String.format("%5s", Integer.toHexString(numbers)).replace(' ', '0').toUpperCase();
									disphex = disphex.substring(3, disphex.length()); //cut out leading F's
								}
								else
								{
									disphex = String.format("%5s", Integer.toHexString(numbers)).replace(' ', '0').toUpperCase();
								}
								
								flag = checkFlags(SICKhash, LabelHash, oper, instr, false, true);
								objCode = OPcodeStr + flag + disphex;
							}
							else
							{
								//check if it is using indexing with indirect or immediate
								boolean indexErr = indexingErr(oper);
								boolean indexErr2 = indexingErr2(oper);
								if (indexErr == true)
								{
									int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
									output.get(i).errFlag = true;
									output.get(i).errPrint = "********** ERROR: Use of X register with immediate/indirect addressing not allowed "
											+ "[on line " + lineNumber + "]";
								}
								
								if (indexErr2 == true)
								{
									int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
									output.get(i).errFlag = true;
									output.get(i).errPrint = "********** ERROR: Operand not found in symbol table "
											+ "[on line " + lineNumber + "]";
								}
								
								if (output.get(i).errFlag == false)
								{
									boolean noLblIndex = false;
									//check to see if it uses indexing with a number...
									if (oper.contains(",") && grabNum.matches("-?[0-9]+"))
									{
										//parse int from string
										int numbers = Integer.parseInt(grabNum);
										
										//convert numbers to hex and trim to disphex variable
										if (numbers < 0)
										{
											disphex = String.format("%5s", Integer.toHexString(numbers)).replace(' ', '0').toUpperCase();
											disphex = disphex.substring(3, disphex.length()); //cut out leading F's
										}
										else
										{
											disphex = String.format("%5s", Integer.toHexString(numbers)).replace(' ', '0').toUpperCase();
										}
										
										objCode = OPcodeStr + "9" + disphex;
										noLblIndex = true;
									}
									
									if (noLblIndex == false)
									{
										flag = checkFlags(SICKhash, LabelHash, oper, instr, false, false);
										
										//Pull location address of label (if any) and add to object code with 8 digits total.
										//Add if statement here that checks if literals are too long and check for the shortened version!
										String lblfltr = Helpers.lblFilter(oper);
										String lblloc = String.format("%5s", LabelHash.getAddress(lblfltr)).replace(' ', '0').toUpperCase();
									
										//Create object code and insert into output array
										objCode = OPcodeStr + flag + lblloc;
									}
								}
							}
							
							//verify object code is 8 digits long. No more and no less.
							if (objCode.length() == 8 && output.get(i).errFlag == false)
							{
								output.get(i).objCode = objCode;
								objectCodes.add(objCode);
							}
							else //if it exceeds or is below 5 digits skip object code
							{
								//output.get(i).objCode = OPcodeStr + flag + "-----";
								output.get(i).errFlag = true;
							}
						}
						else if (SICKhash.getFormat(instr) == 2)
						{	
							//System.out.println("Entered format 2 if statement!");
							String oriOP = SICKhash.getOPcode(instr);
							String reg1 = "";
							String reg2 = "";
							int regNum = 0;
							int regNum2 = 0;
							
							if (instr.equals("SHIFTL") || instr.equals("SHIFTR"))
							{
								//Grab register used before comma
								if (!oper.equals(""))
									reg1 = "" + oper.charAt(0);
								
								regNum = findReg(regs, reg1);
								
								if (regNum == -1)
								{
									int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
									output.get(i).errFlag = true;
									output.get(i).errPrint = "********** ERROR: Invalid register or no register found in operand "
											+ "[on line " + lineNumber + "]";
								}
								
								if (regNum != -1)
								{
									//Grab number after comma and verify it is less than 17
									String shifter = grabShift(oper);
									
									if (shifter.equals(""))
									{
										int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
										output.get(i).errFlag = true;
										output.get(i).errPrint = "********** ERROR: Invalid shift quantity entered [on line " + 
										lineNumber + "]";
									}
									else
									{
										objCode = OPs + regs[regNum].getOpcode() + shifter;
										output.get(i).objCode = objCode;
										objectCodes.add(objCode);
									}
								}
							}
							else if (oper.contains(","))
							{
								//split by the comma and grab regs as individual strings????
								String trim = oper.trim();
								String[] reggies = trim.split(",");
								
								reg1 = reggies[0];
								reg2 = reggies[1];
								
								//System.out.println("Value of reg1: " + reg1 + "\nValue of reg2: " + reg2);
								
								//get number value of registers used. Call method to search regs array
								regNum = findReg(regs, reg1);
								regNum2 = findReg(regs, reg2);							
								
								//flag error message here if either register is not found
								if (regNum == -1 || regNum2 == -1)
								{
									//error
									int lineNumber = Helpers.purifyLineCount(output.get(i).lineNum);
									output.get(i).errFlag = true;
									output.get(i).errPrint = "********** ERROR: Invalid register or no register found in operand "
											+ "[on line " + lineNumber + "]";
								}
								
								//add those numbers to opcode to create object code with 4 digits
								if (regNum != -1 && regNum2 != -1) //avoid index errors if it does not find the register
								{
									objCode = OPs + regs[regNum].getOpcode() + regs[regNum2].getOpcode();
									output.get(i).objCode = objCode;
									objectCodes.add(objCode);
								}
							}
							else //operand does not contain a comma....
							{
								//Clear will only have 1 register...
								if (!oper.equals(""))
									reg1 = "" + oper.charAt(0);
								
								regNum = findReg(regs, reg1);
								
								if (regNum != -1)
								{
									objCode = OPs + regs[regNum].getOpcode() + "0";
									output.get(i).objCode = objCode;
									objectCodes.add(objCode);
								}
							}
						}
						else if (SICKhash.getFormat(instr) == 1)
						{
							//System.out.println("Entered format 1 if!");
							objCode = SICKhash.getOPcode(instr);
							output.get(i).objCode = objCode;
							objectCodes.add(objCode);
						}
					}
					else if (instr.equals("WORD"))
					{
						int negChk = 0;
						
						//Convert number in third column to hex. If it is negative print leading F's instead of 0's.
						//Check if negative or not
						//verify word only contains numbers and NO labels before doing this.
						if (oper.matches("-?[0-9]+"))
						{
							negChk = Integer.parseInt(oper);
							
							if (negChk >= 0)
							{
								String objCode = String.format("%6s", Integer.toHexString(negChk)).replace(' ', '0').toUpperCase();
								output.get(i).objCode = objCode;
								objectCodes.add(objCode);
							}
							else if (negChk < 0)
							{
								//consider checking if number needs more than 6 hex digits here.
								if (negChk > -1000000)
								{
									//Remove leadings F's from java's toHexString method
									//get substring to remove leadings F's that toHexString puts there.
									String objCode = String.format("%6s", Integer.toHexString(negChk)).replace(' ', '0').toUpperCase();
									objCode = objCode.substring(2, objCode.length());
									output.get(i).objCode = objCode;
									objectCodes.add(objCode);
								}
								else
								{
									//it should really be an error though ???
									String objCode = String.format("%6s", Integer.toHexString(negChk)).replace(' ', '0').toUpperCase();
									output.get(i).objCode = objCode;
									objectCodes.add(objCode);
								}
							}
						}
						else if (oper.contains("+") || oper.contains("-")) //arithmetic operators...
						{
							//Split string by operands by removing them
							String replaceOper = oper;
							String removeOp = oper.replaceAll("[+-]+", " ");
							String trim = removeOp.trim();
							String[] mathSplit = trim.split("\\s+");
							ArrayList<objMath> obbies = new ArrayList<objMath>();
							boolean nonNum = false;
							
//							for (int j = 0; j < mathSplit.length; j++)
//							{
//								System.out.println("Value of mathSplit pos" + j + ": " + mathSplit[j]);
//								
//								//replace all parenthesis with null string so they can be found in label table...
//								if (mathSplit[j].contains("(") && LabelHash.search(mathSplit[j]) == false
//										|| mathSplit[j].contains(")") && LabelHash.search(mathSplit[j]) == false)
//								{
//									mathSplit[j] = mathSplit[j].replace("(", "");
//									mathSplit[j] = mathSplit[j].replace(")", "");
//								}
//								
//								System.out.println("Updated value of mathSplit pos" + j + ": " + mathSplit[j]);
//							}
							
							//check for negative at the beginning and re-add it to the array. This will actually throw error.
							if (oper.charAt(0) == '-')
							{
								mathSplit[0] = "-" + mathSplit[0];
							}
							
							//check for each string from mathSplit[] in the LabelTable
							for (int j = 0; j < mathSplit.length; j++)
							{
								if (LabelHash.search(mathSplit[j]) == true) //search for string in the Label Table
								{
									//if it exists grab the location address and convert to decimal (int)
									String grabHex = LabelHash.getAddress(mathSplit[j]);
									String grabBlk = LabelHash.getBlock(mathSplit[j]);
									
									int conHex = 0; 
									if (grabBlk.equals("main"))
									{
										conHex = Integer.parseInt(grabHex, 16);
									}
									else
									{
										//call method in displacement class to calculate absolute location
										conHex = Displacement.absoluteLoc(output, blockArr, grabHex, grabBlk);
									}
									
									//store into objMath arraylist
									objMath hack;
									hack = new objMath(mathSplit[j], grabBlk);
									hack.Address = grabHex;
									hack.intAddr = conHex;
									obbies.add(hack);
									
									//then convert it back to a string and store it in the array
									mathSplit[j] = conHex + "";
								}
							}
							
							//now iterate through the array for any non-numbers. If number found, then throw error.
							//System.out.println("\nBeginning number check for loop!\n");
							for (int j = 0; j < mathSplit.length; j++)
							{
								//System.out.println("Value of mathSplit pos" + j + ": " + mathSplit[j]);
								if (!mathSplit[j].matches("-?[0-9]+"))
								{
									//error: no number value found for label(s) referenced in WORD
									nonNum = true;
								}
							}
							
							//if no errors found, recreate the string with decimal values found from labels...
							if (nonNum == false)
							{
								/* 
								 * This for loop below will make sure every label in the operand gets replaced
								 * by it's equivalent decimal number (For arithmetic)
								 */
								
								//call method to grab all symbols out of the operand for +-()
								String symbols = operandCount(oper);
								String rebuilt = "";
								
								if (symbols.contains("(") || symbols.contains(")"))
								{
									//throw error here
								}
																
								for (int j = 0; j < symbols.length(); j++)
								{
									//System.out.println("Current obbies intAddr: " + obbies.get(j).intAddr);
									
									rebuilt = rebuilt + obbies.get(j).intAddr + symbols.charAt(j);
								}
								
								//add last value to rebuilt string...
								rebuilt = rebuilt + obbies.get(obbies.size() - 1).intAddr;
								
								//System.out.println("replaceOper: " + rebuilt);
								
								//pass to method that will parse the expression...							
								try
								{
									negChk = stringMath(rebuilt);
									//System.out.println(x);
								}
								catch (ScriptException err)
								{
									err.printStackTrace();
								}
								
								//System.out.println("negChk: " + negChk);
								
								if (negChk >= 0)
								{
									String objCode = String.format("%6s", Integer.toHexString(negChk)).replace(' ', '0').toUpperCase();
									output.get(i).objCode = objCode;
									objectCodes.add(objCode);
								}
								else if (negChk < 0)
								{
									//consider checking if number needs more than 6 hex digits here.
									if (negChk > -1000000)
									{
										//Remove leadings F's from java's toHexString method
										//get substring to remove leadings F's that toHexString puts there.
										String objCode = String.format("%6s", Integer.toHexString(negChk)).replace(' ', '0').toUpperCase();
										objCode = objCode.substring(2, objCode.length());
										output.get(i).objCode = objCode;
										objectCodes.add(objCode);
									}
									else
									{
										//it should really be an error though ???
										String objCode = String.format("%6s", Integer.toHexString(negChk)).replace(' ', '0').toUpperCase();
										output.get(i).objCode = objCode;
										objectCodes.add(objCode);
									}
								}
							}
							else
							{
								//throw/add error
								output.get(i).errFlag = true;
								output.get(i).errPrint = "********** ERROR: label in operand not found.";
							}
						}
						else //else check to see if the operand is a label by itself
						{
							//search for the operand in the label table
							if (LabelHash.search(oper) == true) //if operand found in label table
							{
								//getAddress and create object code out of it
								String tmpGrab = LabelHash.getAddress(oper);
								int conHex = Integer.parseInt(tmpGrab, 16);
								String grabHex = String.format("%6s", Integer.toHexString(conHex)).replace(' ', '0').toUpperCase();
								String grabBlk = LabelHash.getBlock(oper);
								
								if (!grabBlk.equals("main"))
								{
									//call method in displacement class to calculate absolute location
									conHex = Displacement.absoluteLoc(output, blockArr, grabHex, grabBlk);
									//update value of grabHex
									grabHex = String.format("%6s", Integer.toHexString(conHex)).replace(' ', '0').toUpperCase();
								}
								
								output.get(i).objCode = grabHex;
								objectCodes.add(grabHex);
							}
							else //else invalid operand.
							{
								//throw/add error
								output.get(i).errFlag = true;
								output.get(i).errPrint = "********** ERROR: label in operand not found.";
							}
						}
					}
					else if (instr.equals("BYTE"))
					{
						String objCode = "";
						
						//Check for errors in the operand before calculating object code
						int byteErr = Helpers.litErr(oper);
						//System.out.println("Value of byteErr: " + byteErr + " when oper is: " + oper);
						
						if (byteErr != 0)
						{
							output.get(i).errFlag = true;
							output.get(i).errPrint = "********** ERROR: Invalid literal found";
						}
						
						if (byteErr == 0)
						{
							//Calculate object code for BYTE
							//We need to check if it begins with X or C
							if (oper.charAt(0) == 'C')
							{
								//get ascii values of each character inside quotes
								String asciiMe = litQuote(oper);
								String objAscii = "";
								
								for (int j = 0; j < asciiMe.length(); j++)
								{
									//add to string
									int ascii = asciiMe.charAt(j);
									//convert to hex before concatening string
									String tmp = Integer.toHexString(ascii).toUpperCase();
									objAscii = objAscii + tmp;
								}
								
								objCode = objAscii;
							}
							else if(oper.charAt(0) == 'X')
							{
								//grab the literal hex value inside the quotes
								objCode = litQuote(oper);
							}
						
							output.get(i).objCode = objCode;
							objectCodes.add(objCode);
						}
					}
				}
			}
			//System.out.println("Finished object code for loop!");
		}
		
		//add ending address to obj file
//		endAddr = String.format("%6s", endAddr).replace(' ', '0').toUpperCase();
//		objectCodes.add(endAddr);
//		
//		//add starting address and ! to end of obj file
//		objectCodes.add(objectCodes.get(0));
		objectCodes.add("!");
	}
	
	private static String operandCount(String oper)
	{
		//System.out.println("Entered operandCount method!");
		
		//grab every + and - out of the string and store them into the arraylist mathSym
		StringBuilder symbols = new StringBuilder(""); //LABEL
		for (int j = 0; j < oper.length(); j++)
		{
			//append charatacters to stringbuilder
			if (Character.toString(oper.charAt(j)).matches("[+-]")
					|| Character.toString(oper.charAt(j)).matches("[(]")
					|| Character.toString(oper.charAt(j)).matches("[)]"))
			{
				symbols.append(oper.charAt(j));
			}
		}
		
		//System.out.println("\nValue of symbols stringbuilder: " + symbols.toString());
		return symbols.toString();
	}
	
	private static int stringMath(String oper) throws ScriptException
	{
		//System.out.println("Entered stringMath!");
		ScriptEngineManager work = new ScriptEngineManager();
		ScriptEngine hard = work.getEngineByName("JavaScript");
		return (int) hard.eval(oper);
	}
	
	public String grabShift(String oper)
	{
		//System.out.println("Entered grabShift method!!");
		
		if (oper.length() >= 3) //make it has length of at least 3. Example: A,1 (smallest possible input)
		{
			StringBuilder number = new StringBuilder(""); //LABEL
			for (int j = 2; j < oper.length(); j++)
			{
				//append charatacters to stringbuilder
				if (Character.toString(oper.charAt(j)).matches("[0-9]+"))
				{
					number.append(oper.charAt(j));
				}
				else
				{
					return ""; //return null when it finds a non-number
				}
			}
			
			
			//convert number to hex
			String beforeHex = number.toString();
			int numInt = Integer.parseInt(beforeHex);
	//		System.out.println("Value of beforeHex: " + beforeHex);
	//		System.out.println("Value of numInt: " + numInt);
			if (numInt > 16 || numInt <= 0) //check if it is a number from 1 to 16
			{
				return ""; //return null if it is out of range.
			}
			
			String converted = Integer.toHexString(numInt-1).toUpperCase();
			//System.out.println(converted);
			return converted;
		}
		
		return "";
	}
	
	public int findReg (Register[] regs, String passed)
	{
		int regNum = -1;
		
		//get number value of registers used. Search regs array
		for (int j = 0; j < regs.length; j++)
		{
			if (regs[j].getRegister().equals(passed)) //figure out a way to insert register here..
			{
				regNum = j;
				break;
			}
		}
		return regNum;
	}
	
	public String checkFlags(SicOpTable SICKhash, LabelTable LabelHash, String oper, String instr, boolean usedBase, boolean noLbl)
	{
		//System.out.println("Entered checkFlags method with oper: " + oper + " and instr: " + instr);
		
		String binary = "0";
		
		//Check if indexing is on. Check if operand column contains ',X'.
		if (!oper.equals(""))
		{
			if (oper.contains(",X"))
			{
				//set indexing to true
				binary = "1";
			}
			
			if (oper.charAt(0) == '#' && Helpers.lblFilter(oper).matches("-?[0-9]+") &&
					SICKhash.getFormat(instr) != 4)
			{
				binary = binary + "000";
				int convert = Integer.parseInt(binary, 2);
				String hexConv = Integer.toString(convert, 16).toUpperCase();
				return hexConv; //return this immediately because the below code will now throw this off.
			}
		}
		
		if (instr.charAt(0) == '*')
		{
			binary = binary + "000";
		}
		//Verify this is a format 3 instruction before doing anything else
		else if (SICKhash.getFormat(instr) == 3)
		{			
//			System.out.println("Entered checkFlag format 3 if!");
//			System.out.println("Value of usedBase: " + usedBase);
//			System.out.println("Value of noLbl: " + noLbl);
			//Check if in PC range or not.
			if (usedBase == false && noLbl == false)
			{
				binary = binary + "010";
			}
			//Check if it is in BASE range. Set BASE flag on and PC flag off.
			//else if (0 <= disp && disp <= 4095)
			else if (usedBase == true && noLbl == false)
			{
				binary = binary + "100";
			}
			else if (noLbl == true) //aka probably an error
			{
				binary = binary + "000";
			}
		}
		else if (SICKhash.getFormat(instr) == 4)
		{
			binary = binary + "001";
		}
		
		//Convert binary to hex and return that number
		//System.out.println(oper + " " + binary);
		int convert = Integer.parseInt(binary, 2);
		String hexConv = Integer.toString(convert, 16).toUpperCase();
		
		return hexConv;
	}
	
	public int calcOP(String OPs, String instr, String oper, SicOpTable SICKhash)
	{
		int OPcode = Integer.parseInt(OPs, 16);
		
		//System.out.println("No directive found! Entered if calcOP method.");
		
		if (instr.charAt(0) == '*')
		{
			return OPcode;
		}
		
		if (oper.charAt(0) == '@') //Check if indirect
		{
			//n is on, i is off. Add 2 to opcode.
			OPcode += 2;
		}
		else if (oper.charAt(0) == '#') //Check if immediate
		{
			//i is on, n is off. Add 1 to opcode.
			OPcode += 1;
		}
		else //it must otherwise be direct. Which means add 3 to opcode.
		{
			OPcode += 3;
		}
		return OPcode;
	}
	
	//Method checks to see if immediate or indexing is used with indexing. Applies to format 3/4
	public boolean indexingErr(String oper)
	{
		boolean error = false;
		if (oper.contains("X") && oper.contains(","))
		{
			//check to see if the first character is a # or @
			if (oper.charAt(0) == '#' || oper.charAt(0) == '@')
			{
				error = true;
				return error;
			}
		}
		
		return error;
	}
	
	//Method checks to see if register X is used in indexing...
	public boolean indexingErr2(String oper)
	{
		boolean error = false;
		if (oper.contains(","))
		{
			//split by comma and check to see what is after the ,
			String trim = oper.trim();
			String[] reggies = trim.split(",");
			
			if (oper.length() > 1)
			{
				if (!reggies[reggies.length - 1].equals("X"))
				{
					error = true;
					return error;
				}
			}
		}
		
		return error;
	}
	
	public String litQuote(String oper)
	{
		//get substring of operand without quotes
		oper = oper.substring(2, oper.length() - 1);
		return oper;
	}
}
