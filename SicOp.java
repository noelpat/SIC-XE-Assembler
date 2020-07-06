
public class SicOp 
{
	// LIOC!!!! = Label Instruction Operand Comment
	private String instr = "";	//label
	private String OPcode = ""; //op code for SICK OPS only. Otherwise left blank.
	private int	format = 2;	//call it format 2 by default. Can easily be changed later.
	int iPLength = 1; //probe length for inserting hashtable
	int sPLength = 1; //probe length for searching hashtable
	
	public SicOp(String str)
	{				
		String trim = str.trim();
		String[] splitme = trim.split("\\s+");
		
		if (splitme.length == 3)
		{
			this.instr = splitme[0];
			this.OPcode = splitme[1];
			this.format = Integer.parseInt(splitme[2]); //convert string to int
		}
		
//		test to make sure we worked!
//		System.out.print(instr + " ");
//		System.out.print(OPcode + " ");
//		System.out.println(format + " ");
	}
	
	public String getInstr()
	{
		return instr;
	}
	
	public int getFormat()
	{
		return format;
	}
	
	public String getOPcode()
	{
		return OPcode;
	}
}
