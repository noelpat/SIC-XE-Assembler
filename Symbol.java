
public class Symbol
{
	// LIOC!!!! = Label Instruction Operand Comment
	private String label = "";	//label
	private String instr = "";	//instruction
	private String oper = "";	//operand
	private String original = ""; //original line from input file
	String lineNum = ""; //line number
	String Address = "";	//for saving/checking label address locations
	String objCode = ""; //object code
	String base = "";
	String Use = "";
	int iPLength = 1; //probe length for inserting hashtable
	int sPLength = 1; //probe length for searching hashtable
	private boolean isComment = false; //check whether it is a comment line or not
	boolean errFlag = false; //error flag
	String errPrint = "";
	
	public Symbol(String str, boolean hasLabel)
	{
		int start = 0; //int for tracking location within the string
		String spaces = str.replaceAll("\\s+", ""); //remove spaces
		
		if (str.equals("-1")) //for deleting symbols if necessary
		{
			this.label = "-1";
			this.instr = "";
			this.oper = "";
		}
		else if (!str.equals("-1") && spaces.charAt(0) != '.')
		{
			//Store original string passed to this symbol for reprinting later.
			this.original = str;
			
			//System.out.println("Entered symbol class with: " + str);
			String trim = str.trim();
			String[] splitme = trim.split("\\s+");
			int length = splitme.length;
			
			//System.out.println("Value of length: " + length);
			
			/*
			This block of code is for reading lines from the input file.
			due to the if statements in the while loop we KNOW for sure this line contains:
			a Label and a valid instruction after it.
			*/		

			if (length == 1)
			{
				//As long as we don't pass literals here directly this should work
				this.instr = splitme[0]; //If length is one, then it MUST be an instruction or exception like LTORG.
			}
			else if (length >= 2) //avoid crashing if we pass a blank line by mistake
			{
				
				if (hasLabel == true)
				{
					this.label = splitme[0];
					
					if (splitme[1].charAt(0) != '.')
					{
						this.instr = splitme[1];
					}
				}
				else if (hasLabel == false)
				{
					this.instr = splitme[0];
					
					if (splitme[1].charAt(0) != '.')
					{
						this.oper = splitme[1];
					}
				}
			}
			
			//We need a way to seperate the operands from the comments here incase there is a line with no operand etc.
			//If a comment does not begin with a period it will get stored here.
			if (length >= 3)
			{
				if (splitme[2].charAt(0) != '.')
				{
					this.oper = splitme[2];
				}
			}
		}
		else if (spaces.charAt(0) == '.')
		{
			this.isComment = true;
			this.original = str;
		}
	}
	public String getLabel()
	{
		return label;
	}
	public String getInstr()
	{
		return instr;
	}
	public String getOper()
	{
		return oper;
	}
	public String getAddr()
	{
		return Address;
	}
	public String getBlock()
	{
		return Use;
	}
	public String getOriginal()
	{
		return original;
	}
	public boolean getComment()
	{
		return isComment;
	}
}
