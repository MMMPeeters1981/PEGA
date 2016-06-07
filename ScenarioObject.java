package core;

import apapl.data.APLIdent;
import apapl.data.Term;

public class ScenarioObject implements APLable
{
	private String name;
	private boolean available;
	
	public ScenarioObject(String name, boolean available)
	{
		this.name = name;
		this.isAvailable(available);
	}
	
	public ScenarioObject(String name)
	{
		this(name, true);
	}
	
	public ScenarioObject(APLIdent aplObjectName)
	{
		this(aplObjectName.getName());
	}
	
	public String name()
	{
		return name;
	}

	public void isAvailable(boolean available) 
	{
		this.available = available;
	}

	public boolean isAvailable() 
	{
		return available;
	}
	
	/* 2APL:
	 * Object 
	 * */
	@Override
	public Term toTerm() 
	{
		if(this.available)
			return new APLIdent(this.name);
		else
			return null;
	}
	
	@Override
	public int hashCode()
	{
		return this.name.hashCode();
	}
}
