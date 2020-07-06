
public class Register 
{
	private String reg = "";	//label
	private String OPcode = ""; //op code for SICK OPS only. Otherwise left blank.
	
	public Register(String str)
	{
		String trim = str.trim();
		String[] splitme = trim.split("\\s+");
		int length = splitme.length;

		if (length == 2)
		{
			this.reg = splitme[0];
			this.OPcode = splitme[1];
		}
	}
	
	public String getRegister()
	{
		return reg;
	}
	
	public String getOpcode()
	{
		return OPcode;
	}
}
