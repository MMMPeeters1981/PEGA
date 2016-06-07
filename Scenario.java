package core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import apapl.data.APLFunction;
import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.Term;


public class Scenario implements APLable
{
	private String name;
	private String description;
	private HashMap<String, Position> positions;
	private HashMap<String, Position> characterPositions;
	private HashMap<String, String> rolePlayers;
	private HashMap<String, HashSet<ScenarioObject>> characterCarryingObjects;
	
	public Scenario(String name, String description)
	{
		this.name = name;
		this.description = description;
		
		this.positions = new HashMap<String, Position>();
		this.characterPositions = new HashMap<String, Position>();
		this.rolePlayers = new HashMap<String, String>();
		this.characterCarryingObjects = new HashMap<String, HashSet<ScenarioObject>>();
	}
	
	/* 2APL:
	 * 'name', 'description', ['name', 'description', [Positions]]
	 * */
	public Scenario(APLIdent aplScenarioName, APLIdent aplScenarioDescription, APLList aplPositions)
	{
		this.positions = new HashMap<String, Position>();
		this.characterPositions = new HashMap<String, Position>();
		this.rolePlayers = new HashMap<String, String>();
		this.characterCarryingObjects = new HashMap<String, HashSet<ScenarioObject>>();
		
		/* 'name' */
		this.name = aplScenarioName.getName();
		
		/* 'description' */
		this.description = aplScenarioDescription.getName();
		
		/* ['name', 'description', [Object]] */
		LinkedList<Term> positionTerms = aplPositions.toLinkedList();
		
		for(Term positionTerm : positionTerms)
		{
			LinkedList<Term> positionVars = ((APLList)positionTerm).toLinkedList();
			
			Position position = new Position((APLIdent)positionVars.get(0), (APLIdent)positionVars.get(1), (APLList)positionVars.get(2));
			this.positions.put(position.name(), position);
		}
		
		for(Position position1 : this.positions.values()) // all positions are neighbours.
		{
			for(Position position2 : this.positions.values())
			{
				if(position1 != position2)
				{
					position1.addNeighbour(position2);
				}
			}
		}
	}
	
	public String name()
	{
		return this.name;
	}
	
	public String description()
	{
		return this.description;
	}

	public void addPosition(Position position)
	{
		this.positions.put(position.name(), position);
	}
	
	public Position getPosition(String positionName)
	{
		return this.positions.get(positionName);
	}
	
	public HashMap<String, Position> getPositions()
	{
		return new HashMap<String, Position>(this.positions);
	}

	public Position getCharacterPosition(String characterName)
	{
		return this.characterPositions.get(characterName);
	}

	public String getCharacterPositionName(String characterName)
	{
		Position position = this.characterPositions.get(characterName);
		if (position != null)
			return position.name();
		else
			return null;
	}

	public APLList getPositionNames()
	{
		LinkedList<Term> aplPositionNames = new LinkedList<Term>();

		for(Position position : this.positions.values())
			aplPositionNames.add(new APLIdent(position.name()));

		return new APLList(aplPositionNames);
	}

	public APLIdent getPositionDescription(String positionName)
	{
		Position position = this.positions.get(positionName);
		if (position != null)
			return new APLIdent(position.description());
		
		return null;
	}

	/**
	 * placeCharacter, places a character in a certain position.
	 * 
	 * @param characterName, name of the character.
	 * @param positionName, name of the position.
	 * 
	 * @return [boolean]: whether the character was successfully placed.
	 */
	public boolean placeCharacter(String characterName, String positionName)
	{
		boolean success = false;
		
		Position newPosition = this.positions.get(positionName);
		if (newPosition != null)
		{
			Position oldPosition = this.characterPositions.get(characterName);
			if (oldPosition != null)
				success = Position.moveCharacter(characterName, oldPosition, newPosition);
			else
				success = newPosition.addCharacter(characterName);
			
			if (success)
				this.characterPositions.put(characterName, newPosition);
		}
		
		return success;
	}
	
	/**
	 * pickupObject, have a character pick up an item.
	 * 
	 * @param characterName, name of character picking up the item.
	 * @param object, the object being picked up.
	 * 
	 * @return [boolean]: whether the object was successfully picked up.
	 */
	public boolean pickupObject(String characterName, ScenarioObject object)
	{
		Position position = this.getCharacterPosition(characterName);
		
		if(!this.characterCarryingObjects.containsKey(characterName))
			this.characterCarryingObjects.put(characterName, new HashSet<ScenarioObject>());

		if(position != null && object.isAvailable() && position.hasNearbyObject(object) && !this.characterCarryingObjects.get(characterName).contains(object) )
		{
			position.removeObject(object);
			this.characterCarryingObjects.get(characterName).add(object);
			
			return true;
		}
		else
			return false;
	}
	
	public boolean pickupObject(String characterName, String objectName)
	{
		return this.pickupObject(characterName, new ScenarioObject(objectName));
	}
	
	/**
	 * dropObject, have a character drop an item.
	 * 
	 * @param characterName, name of character dropping the item.
	 * @param object, the object being dropped.
	 * 
	 * @return [boolean]: whether the object was successfully dropped.
	 */
	public boolean dropObject(String characterName, ScenarioObject object)
	{
		Position position = this.getCharacterPosition(characterName);
		
		if(position != null && object.isAvailable() && this.characterCarryingObjects.containsKey(characterName) && this.characterCarryingObjects.get(characterName).contains(object))
		{
			this.characterCarryingObjects.get(characterName).remove(object);
			position.addObject(object);
			
			return true;
		}
		else
			return false;
	}
	
	public boolean dropObject(String characterName, String objectName)
	{
		return this.dropObject(characterName, new ScenarioObject(objectName));
	}

	public void giveRole(String characterName, String roleName)
	{		
		this.rolePlayers.put(roleName, characterName);
	}

	public String getCharacterByRole(String roleName)
	{
		return this.rolePlayers.get(roleName);
	}

	/* 2APL:
	 * 'name', 'description', ['name', 'description', [Object]]
	 * */
	@Override
	public Term toTerm()
	{
		LinkedList<Term> aplPositions = new LinkedList<Term>();

		/* [position('name', 'description', [object(Name)])] */
		for(Position position : this.positions.values())
			aplPositions.push(position.toTerm());

		return new APLFunction("scenario", new APLIdent(this.name), new APLIdent(this.description), new APLList(aplPositions));
	}
}
