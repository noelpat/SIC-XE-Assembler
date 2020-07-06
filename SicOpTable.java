
public class SicOpTable 
{
	private SicOp[] hashArray; //array holds hash table
	private int arraySize;
	
	public SicOpTable(int size)
	{
		arraySize = size;
		hashArray = new SicOp[arraySize];
	}
	
	public int hashFunc(String line)
	{
		//Hashes using Horner's polynomial.
		//String line = item.getWord();
		int hash = line.charAt(0);
		int ascii = 0;
		char ch;
		
		//if it is a one character string...
		if(line.length() == 1)
		{
			//calculate hash
			hash %= arraySize;
		}
		else
		{
			for(int i = 1; i < line.length(); i++)
			{
				ch = line.charAt(i);
				ascii = ch; //convert char to ascii value
				hash = hash * 28 + ascii;
				hash %= arraySize;
			}
		}
		return hash; //hash function
	}
	
	public void insert(SicOp item) //insert Symbol
	{		
		int hash = hashFunc(item.getInstr());
		int coll = 0; //for tracking collisions

		while(hashArray[hash] != null && 
				!hashArray[hash].getInstr().equals("-1"))
		{
			++hash; //linear probing
			hash %= arraySize; //wraparound if necessary
			item.iPLength++; //increment probe length
		}
		
		hashArray[hash] = item; //insert item into arraylist
		
		//System.out.println("stored " + item.getWord() + " " + item.getNum() + " at location " + hash);
		
		coll = (item.iPLength - 1); //Collisions = Probe Length - 1
		
//		if (coll > 0) //Don't display number of collisions when there are none.
//		{
//			System.out.println("Number of Collisions: " + coll);
//		}
	}
	
	public boolean search(SicOp item) //Search for symbol
	{
		int hash = hashFunc(item.getInstr()); // hash the key
		boolean exist = false;

		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getInstr().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getInstr().equals(item.getInstr()))//if the item exists
			{
				exist = true;
				break;
			}
			++hash; //linear probing
			hash %= arraySize; //wrap around if necessary
			item.sPLength++; //increment probe length count
		}
		if (exist == true)
		{
			//System.out.println("ERROR " + item.getWord() + " already exists at location " + found);
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean search(String item) //Search for string
	{
		int hash = hashFunc(item); // hash the key
		boolean exist = false;
		int sPLength = 1;

		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getInstr().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getInstr().equals(item))//if the item exists
			{
				exist = true;
				break;
			}
			++hash; //linear probing
			hash %= arraySize; //wrap around if necessary
			sPLength++; //increment probe length count
		}
		if (exist == true)
		{
			//System.out.println("ERROR " + item.getWord() + " already exists at location " + found);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getFormat(String item) //Search for symbol
	{
		int hash = hashFunc(item); // hash the key
		boolean exist = false;
		//int sPLength = 1;

		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getInstr().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getInstr().equals(item))//if the item exists
			{
				exist = true;
				break;
			}
			++hash; //linear probing
			hash %= arraySize; //wrap around if necessary
			//sPLength++; //increment probe length count
		}
		if (exist == true)
		{
			//System.out.println("ERROR " + item.getWord() + " already exists at location " + found);
			return hashArray[hash].getFormat();
		}
		else
		{
			//System.out.println("getFormat method called but failed to find location! Returning 0.");
			return 0;
		}
	}
	
	public String getOPcode(String item) //Search for symbol
	{
		int hash = hashFunc(item); // hash the key
		boolean exist = false;
		//int sPLength = 1;

		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getInstr().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getInstr().equals(item))//if the item exists
			{
				exist = true;
				break;
			}
			++hash; //linear probing
			hash %= arraySize; //wrap around if necessary
			//sPLength++; //increment probe length count
		}
		if (exist == true)
		{
			//System.out.println("ERROR " + item.getWord() + " already exists at location " + found);
			return hashArray[hash].getOPcode();
		}
		else
		{
			//System.out.println("getOPcode method called but failed to find location! Returning empty string.");
			return "";
		}
	}
	
	public int getLocation(String item) //get location in hashtable of this Label
	{
		int hash = hashFunc(item); // hash the key
		boolean exist = false;
		//int sPLength = 1;

		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getInstr().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getInstr().equals(item))//if the item exists
			{
				exist = true;
				break;
			}
			++hash; //linear probing
			hash %= arraySize; //wrap around if necessary
			//sPLength++; //increment probe length count
		}
		if (exist == true)
		{
			return hash;
		}
		else
		{
			//System.out.println("ERROR: getLocation method called but failed to find location! Returning 0.");
			return 0;
		}
	}
		
	public void printTable() //print whole table for debugging purposes
	{
		System.out.print("HashTable: \n");
		
		for (int i = 0; i < arraySize; i++)
		{
			if (hashArray[i] != null)
			{
				System.out.print(hashArray[i].getInstr() + " "
						+ hashArray[i].getInstr() + " ");
			}
			else
			{
				System.out.print("-- ");
			}
			if (i > 0 && i % 5 == 0) //insert line breaks every 5 cells
			{
				System.out.println();
			}
		}
		System.out.println();
	}
}
