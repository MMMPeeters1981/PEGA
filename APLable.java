package core;

import apapl.data.Term;

/**
 * Interface to give an object the property of simplying it to an APL term.
 */
public interface APLable 
{
	/**
	 * Function to be overwritten
	 * @return Term
	 */
	public Term toTerm();
}