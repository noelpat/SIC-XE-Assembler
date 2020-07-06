import java.util.ArrayList;

/*
 		//Program-counter relative range
//		int lowPC = -2048;
//		int highPC = 2047;
//		//Base relative range
//		int lowB = 0;
//		int highB = 4095;
 */

public class Displacement 
{
	boolean usedBase = false;
	boolean noLblFound = false;
	int dispInt = -1;
	String dispHex = "000";
	boolean error = false;
	String errMsg = "";
	
	public Displacement(Symbol current, ArrayList<Symbol> output, int i, LabelTable LabelHash, SicOpTable SICKhash, 
			ArrayList<useBlock> blockArr)
	{
		//Program-counter relative range
//		int lowPC = -2048;
//		int highPC = 2047;
		int lblloc = -1;
		int disp = -1;
		String lblAddr = "";
		String lblBlk = "";
		boolean sameBlock = false;
		
		//System.out.println("Entered displacement class!");
		//Call method to Filter out #, @, and Indexing from label in operand place		
		String oriLbl = Helpers.lblFilter(current.getOper());
		
		//get value of next location address from output array
		int nextAddr = Helpers.nextLoc(output, i, blockArr);
		//System.out.println("Value of nextAddr: " + nextAddr);
		
		//check if literal and grab the shortened version that will be in the label table for searching!
		//If we don't do this, we won't find the literal in the labeltable even though it might be there!
		//For now we only need to do this for literals...
		//System.out.println("Value of oriLbl: " + oriLbl);
		if (oriLbl.charAt(0) == '=' && oriLbl.length() > 10)
		{
			oriLbl = oriLbl.substring(0, 10);
			//System.out.println("Reduced label value of oriLbl: " + oriLbl);
		}
		
		//if it is a literal we need to use the getAddress method designed to deal with duplicate literals in the Symbol Table
		if (oriLbl.charAt(0) == '=')
		{
			//grab block location first
			lblBlk = LabelHash.getBlock(oriLbl);
			
			//use a different method depending on if in the same block or not
			if (current.Use.equals(lblBlk))
			{
				lblAddr = LabelHash.getAddressLit(oriLbl, nextAddr, blockArr, lblBlk);
			}
			else
			{
				lblAddr = LabelHash.getAddress(oriLbl);
			}
		}
		else //grab label location normally
		{
			//grab label location
			lblAddr = LabelHash.getAddress(oriLbl);
			lblBlk = LabelHash.getBlock(oriLbl);
		}
//		System.out.println("Does " + oriLbl + " exist: " + LabelHash.search(oriLbl));
//		System.out.println("Value of oriLbl: " + oriLbl);
//		System.out.println("Value of lblBlk: " + lblBlk);
		//System.out.println("Value of lblAddr: " + lblAddr);
		
		//check if they are in the same Use block or not...
		if (current.Use.equals(lblBlk))
		{
			sameBlock = true;
		}
		else
		{
//			System.out.println("Found a label not in the same block!");
//			System.out.println(current.lineNum);
			
			//call method to add up and retrieve absolute location...
			lblloc = absoluteLoc(output, blockArr, lblAddr, lblBlk);
		}
		
		//System.out.println(oriLbl + " found with address: " + lblAddr); //For debugging.
		if (!lblAddr.equals("")) //only do all of this if a label was actually found...
		{
			if (sameBlock == true) //only do this if it is in the same block
			{
				lblloc = Integer.parseInt(lblAddr, 16);
			}
			
			//System.out.println("Next address: " + nextAddr);
			//subtract label's address from next address
			if (lblloc != -1) //verify lblloc was updated with an address
			{
				disp = lblloc - nextAddr;
				//System.out.println("PC disp: " + disp);
			}
			
			//check if in PC range here before returning. If it is not in PC range, we need to recalculate with BASE.
			if (!current.base.equals("") && -2048 > disp || !current.base.equals("") && disp > 2047) //else if out if PC range, recalculate and check for BASE
			{
				//System.out.println("Entered base calculation if!");
				//recalculate with base
				//grab base label and address
				String baseCurr = output.get(i).base;
				String baseAddr = LabelHash.getAddress(baseCurr);
				
				//convert base address to int
				int baseloc = Integer.parseInt(baseAddr, 16);
				
				//subtract label loc from base
				disp = lblloc - baseloc;
				
				if (0 <= disp && disp <= 4095) //verify this new displacement is actually in base range before returning.
				{
					usedBase = true;
				}
				else
				{
					error = true;
				}
			}
			else if (-2048 > disp || disp > 2047 && output.get(i).base.equals(""))
			{
				//Exceeds PC range and BASE not set!! Error.
				error = true;
				if (current.base.equals(""))
				{
					errMsg = "Exceeds PC range and no Base set.";
				}
				else
				{
					errMsg = "Out of PC range and Base range.";
				}
			}
	
			if (error == false)
			{
				dispInt = disp;
				
				//convert to hex string here as well
				dispHex = String.format("%3s", Integer.toHexString(dispInt)).replace(' ', '0').toUpperCase();
			}
		}
		else
		{
			noLblFound = true;
		}

//		System.out.println(current.getInstr() + " " + current.getOper());
//		System.out.println(lblloc + " " + disp + " " + lblAddr);
//		System.out.println(error + " Exceeds PC range and no base found!");
	}
	
	public static int absoluteLoc(ArrayList<Symbol> output, ArrayList<useBlock> blockArr, String lblAddr, String lblBlk)
	{
		//System.out.println("Entered absoluteLoc method!");
		
		int lblloc = -1;
		
		//Calculate the real address of the label.
		//grab last address inside main block/output array
		int mainAddr = blockArr.get(0).lastLoc;
		
		//find location of useBlock in useArr
		int blkPos = Helpers.getBlock(blockArr, lblBlk);
		
		//loop to add up absolute location of label
		//add up the last location(s) with last address from main block
		//if there is a block between main the current block we must add that as well.
		if (blkPos > 1) //more than 1 meaning there are blocks inbetween us and main!
		{
			lblloc = 0; //set lblloc to 0
			int addMe = 0;
			
			//loop to add up each block between block position and main block
			for (int i = 0; i < blkPos; i++)
			{
				//grab last location from block and add it to lblloc
				lblloc += blockArr.get(i).lastLoc;
			}
			
			//after adding up the last location of all previous blocks, also add the location of the label inside current block
			if (!lblAddr.equals(""))
				addMe = Integer.parseInt(lblAddr, 16);
			
			lblloc += addMe;
		}
		else //otherwise, just add the lbls address with the last value of the main block
		{
			//System.out.println("Value of lblAddr: " + lblAddr);
			if (!lblAddr.equals(""))
				lblloc = Integer.parseInt(lblAddr, 16);
			
			lblloc = mainAddr + lblloc;
		}
		
		//System.out.println("Value of lblloc: " + lblloc);
		return lblloc;
	}
}
