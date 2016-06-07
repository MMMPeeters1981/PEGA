package ui;

import java.util.HashMap;
import java.util.HashSet;

import apapl.data.APLFunction;
import apapl.data.APLIdent;
import core.*;

public class MovementOption extends TraineeOption
{
	private static final long serialVersionUID = 1L;

	private final Env env;
	private final Position direction;

	public MovementOption(Env env, Position direction)
	{
		super("move " + direction.name());
		
		this.setActionCommand("move(" + direction.name() + ")");
		this.env = env;
		this.direction = direction;
		
		this.setEnabled(false);
	}
	
	public void performOption()
	{
		HashSet<String> nearbyCharacters = this.env.placeCharacter("trainee", direction.name());
		
		if(nearbyCharacters == null)
			nearbyCharacters = new HashSet<String>();
		
		HashMap<String, ScenarioObject> nearbyObjects = direction.getNearbyObjects();
		
		this.env.controlTrainee(new APLFunction("new_position", 
									new APLIdent(this.direction.name()),
									APLHelper.createAPLList(nearbyObjects.values()),
									APLHelper.createAPLIdentList(nearbyCharacters)
									));
	}
	
	public void scenarioUpdate()
	{
		Scenario scenario = this.env.scenario();
		Position traineePosition = scenario.getCharacterPosition("trainee");
		
		boolean buttonEnabled = traineePosition.isNeighbour(this.direction);
		
		this.setEnabled(buttonEnabled);
	}
}
