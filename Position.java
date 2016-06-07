package core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.Term;

public class Position implements APLable
{
	private String name;
	private String description;
	private HashSet<Position> neighbours;
	private HashMap<String, ScenarioObject> nearbyObjects;
	private HashSet<String> nearbyCharacters;
	
	public Position(String name, String description)
	{
		this.name = name;
		this.description = description;
		
		this.neighbours = new HashSet<Position>();
		this.nearbyObjects = new HashMap<String, ScenarioObject>();
		this.nearbyCharacters = new HashSet<String>();
	}
	
	/* 2APL:
	 * 'name', 'description', [Object] 
	 * */
	public Position(APLIdent aplPositionName, APLIdent aplPositionDescription, APLList aplObjects)
	{
		this.neighbours = new HashSet<Position>();
		this.nearbyObjects = new HashMap<String, ScenarioObject>();
		this.nearbyCharacters = new HashSet<String>();
		
		/* 'name' */
		this.name = aplPositionName.getName();
		
		/* 'description' */
		this.description = aplPositionDescription.getName();
		
		/* [Object] */
		LinkedList<Term> objectTerms = aplObjects.toLinkedList();
		
		for(Term objectTerm : objectTerms)
		{
			ScenarioObject scenarioObject = new ScenarioObject((APLIdent)objectTerm);
			this.nearbyObjects.put(scenarioObject.name(), scenarioObject);
		}
	}
	
	/* 2APL:
	 * position('name', 'description', [object(Name)]) 
	 * */
	@Override
	public Term toTerm()
	{
		LinkedList<Term> aplObjects = new LinkedList<Term>();
		
		/* [object(Name)] */
		for(ScenarioObject nearbyObject : this.nearbyObjects.values())
			aplObjects.push(nearbyObject.toTerm());
		
		return new APLFunction("position", new APLIdent(this.name), new APLIdent(this.description), new APLList(aplObjects));
	}
	
	public String name()
	{
		return this.name;
	}
	
	public String description()
	{
		return this.description;
	}
	
	public HashSet<Position> neighbours()
	{
		return new HashSet<Position>(this.neighbours);
	}
	
	public boolean isNeighbour(Position neighbour)
	{
		return this.neighbours.contains(neighbour);
	}
	
	public void addNeighbour(Position neighbour)
	{
		this.neighbours.add(neighbour);
	}
	
	public void addObject(ScenarioObject object)
	{
		this.nearbyObjects.put(object.name(), object);
	}
	
	public boolean hasNearbyObject(ScenarioObject object)
	{
		return this.nearbyObjects.containsKey(object.name());
	}
	
	public boolean hasNearbyObject(String objectName)
	{
		return this.nearbyObjects.containsKey(objectName);
	}
	
	public void removeObject(ScenarioObject object)
	{
		this.nearbyObjects.remove(object);
	}
	
	public boolean addCharacter(String characterName)
	{
		return this.nearbyCharacters.add(characterName);
	}
	
	public boolean removeCharacter(String characterName)
	{
		return this.nearbyCharacters.remove(characterName);
	}
	
	public boolean hasNearbyCharacter(String characterName)
	{
		return this.nearbyCharacters.contains(characterName);
	}
	
	public boolean moveCharacter(String characterName, Position newPosition)
	{
		if(this.removeCharacter(characterName))
		{
			newPosition.addCharacter(characterName);
			return true;
		}
		else
			return false;
	}

	public static boolean moveCharacter(String characterName, Position oldPosition, Position newPosition)
	{
		return oldPosition.moveCharacter(characterName, newPosition);
	}

	public HashSet<String> getNearbyCharacters()
	{
		return new HashSet<String>(this.nearbyCharacters);
	}

	public HashMap<String, ScenarioObject> getNearbyObjects()
	{
		return new HashMap<String, ScenarioObject>(this.nearbyObjects);
	}

	@Override
	public int hashCode()
	{
		return this.name.hashCode();
	}
}
