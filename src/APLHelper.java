package core;

import java.util.*;
import apapl.data.*;

/**
 * Class for making conversions between (arrays of) normal objects and (arrays of) APL objects
 */
public class APLHelper 
{
	/**
	 * createAPLIdentList, this function creates a APLList of APLIdent from a Iterable collection of Strings.
	 * 
	 * @param src the Iterable collection of Strings.
	 * 
	 * @return [APLList]: List of APLIdents converted from the Strings.
	 */
	public static APLList createAPLIdentList(Iterable<String> src)
	{
		LinkedList<Term> out = new LinkedList<Term>();
		
		for(String item : src)
		{
			if(item != null)
				out.add(new APLIdent(item));
		}
		
		return new APLList(out);
	}
	
	/**
	 * createAPLList, this function creates a APLList of Terms from a Iterable collection of APLables. The toTerm function from the APLAble 
	 * interface is used in the conversion from APLable to Term.
	 * 
	 * @param src the Iterable collection of APLables.
	 * 
	 * @return [APLList]: List of the Terms converted from APLables.
	 */
	public static APLList createAPLList(Iterable<? extends APLable> src)
	{
		LinkedList<Term> out = new LinkedList<Term>();
		
		for(APLable item : src)
		{
			Term t = item.toTerm();
			if(t != null)
				out.add(t);
		}
		
		return new APLList(out);
	}
	
	/**
	 * createIdentArrayList, this function creates an ArrayList of Strings converted from an APLList of APLIdents.
	 * 
	 * @param src APLList of APLIdents.
	 * 
	 * @return [ArrayList<String>]: ArrayList of the Strings converted from APLIdents.
	 */
	public static ArrayList<String> createIdentArrayList(APLList src)
	{
		LinkedList<Term> tmp = src.toLinkedList();
		ArrayList<String> out = new ArrayList<String>();
		
		for(Term item : tmp)
		{
			if(item != null && item instanceof APLIdent)
				out.add(((APLIdent)item).getName());
		}
		
		return out;
	}
	
}
