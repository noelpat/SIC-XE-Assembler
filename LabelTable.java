import java.util.ArrayList;

public class LabelTable 
{
	private Symbol[] hashArray; //array holds hash table
	private int arraySize;
	private Symbol removed; //deleted items
	
	public LabelTable(int size)
	{
		arraySize = size;
		hashArray = new Symbol[arraySize];
		removed = new Symbol("-1", false); //deleted item key
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
	
	public void insert(Symbol item) //insert Symbol
	{		
		int hash = hashFunc(item.getLabel());
		int coll = 0; //for tracking collisions

		while(hashArray[hash] != null && 
				!hashArray[hash].getLabel().equals("-1"))
		{
			++hash; //linear probing
			hash %= arraySize; //wraparound if necessary
			item.iPLength++; //increment probe length
		}
		
		hashArray[hash] = item; //insert item into arraylist
		
		//System.out.println("stored " + item.getWord() + " " + item.getNum() + " at location " + hash);
//		coll = (item.iPLength - 1); //Collisions = Probe Length - 1
//		if (coll > 0) //Don't display number of collisions when there are none.
//		{
//			System.out.println("Number of Collisions: " + coll);
//		}
	}
	
	public boolean search(Symbol item) //Search for symbol
	{
		int hash = hashFunc(item.getLabel()); // hash the key
		boolean exist = false;

		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getLabel().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getLabel().equals(item.getLabel()))//if the item exists
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
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean search(String item) //Search for symbol
	{
		int hash = hashFunc(item); // hash the key
		boolean exist = false;
		int sPLength = 1;

		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getLabel().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getLabel().equals(item))//if the item exists
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
	
	public String getAddress(String item) //Search for symbol
	{
		int hash = hashFunc(item); // hash the key
		boolean exist = false;
		//int sPLength = 1;

		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getLabel().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getLabel().equals(item))//if the item exists
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
			return hashArray[hash].getAddr();
		}
		else
		{
			//System.out.println("ERROR: getAddress method called but failed to find location! Returning null.");
			return "";
		}
	}
	
	public String getAddressLit(String item, int loc, ArrayList<useBlock> blockArr, String currBlk) //Search for symbol
	{
		int hash = hashFunc(item); // hash the key
		boolean exist = false;
		//int sPLength = 1;
		int lblAddr = 0;
		
		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getLabel().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getLabel().equals(item))//if the item exists & location is less than label location (For dupes)
			{
				lblAddr = Integer.parseInt(hashArray[hash].getAddr(), 16);
				String thisBlock = hashArray[hash].Use;
				
				if (lblAddr >= loc && thisBlock.equals(currBlk)) //check to see if this literal is a dupe from an above LTORG in the SAME BLOCK
				{
					exist = true;
					break;
				}
			}
			++hash; //linear probing
			hash %= arraySize; //wrap around if necessary
			//sPLength++; //increment probe length count
		}
		if (exist == true)
		{
			//System.out.println("ERROR " + item.getWord() + " already exists at location " + found);
			return hashArray[hash].getAddr();
		}
		else
		{
			//System.out.println("ERROR: getAddressLit method called but failed to find location! Returning null.");
			return "";
		}
	}
	
	public int getLocation(String item) //get location in hashtable of this Label
	{
		int hash = hashFunc(item); // hash the key
		boolean exist = false;
		//int sPLength = 1;

		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getLabel().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getLabel().equals(item))//if the item exists
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
	
	public String getBlock(String item) //Search for symbol
	{
		int hash = hashFunc(item); // hash the key
		boolean exist = false;
		//int sPLength = 1;

		while(hashArray[hash] != null && exist == false && 
				!hashArray[hash].getLabel().equals("-1")) //until empty cell
		{
			if(hashArray[hash].getLabel().equals(item))//if the item exists
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
			return hashArray[hash].getBlock();
		}
		else
		{
			//System.out.println("ERROR: getBlock method called but failed to find location! Returning null. \n Input: " + item);
			return "";
		}
	}
	
	public Symbol delete(String line) //delete symbol if necessary
	{
		int hash = hashFunc(line);
		
		while(hashArray[hash] != null) //until empty cell
		{
			if(hashArray[hash].getLabel().equals(line))
			{
				Symbol deleted = hashArray[hash]; //temporarily store item
				hashArray[hash] = removed;
				return deleted; //to be able to print what was deleted if desired
			}
			++hash; //next cell
			hash %= arraySize; //wrap around
		}
		return null; //no such item found
	}
	
	public void printTable() //print whole table for debugging purposes
	{
		System.out.print("HashTable: \n");
		
		for (int i = 0; i < arraySize; i++)
		{
			if (hashArray[i] != null)
			{
				System.out.print(hashArray[i].getLabel() + " "
						+ hashArray[i].getOper() + " ");
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
	
	public void printTablelbl() //print whole table for debugging purposes
	{
		System.out.format("%1$-17s%2$-13s%3$-12s%4$-8s%5$-10s", "Table Location", "Label", "Address", "Use", "Csect");
		System.out.println();
		for (int i = 0; i < arraySize; i++)
		{
			if (hashArray[i] != null)
			{
				System.out.format("%1$-17s%2$-13s%3$-12s%4$-8s%5$-10s", i, hashArray[i].getLabel(), hashArray[i].Address.toUpperCase(), 
						hashArray[i].Use, "main");
				System.out.println();
			}
		}
		System.out.println();
	}
}
