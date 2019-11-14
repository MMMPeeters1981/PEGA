package ui;

import apapl.data.APLFunction;
import apapl.data.APLIdent;
import core.*;

public class ScenarioOption extends TraineeOption
{
	private static final long serialVersionUID = 1L;

	private final Env env;
	private final String functionName;
	private final String[] requiredObjects;
	private final String[] requiredCharacterRoles;

	public ScenarioOption(Env env, String name, String functionName, String[] requiredObjects, String[] requiredCharacterRoles)
	{
		super(name);
		
		this.env = env;
		this.functionName = functionName;
		this.requiredObjects = requiredObjects;
		this.requiredCharacterRoles = requiredCharacterRoles;
		
		this.setEnabled(false);
	}
	
	public void performOption()
	{
		APLFunction aplControlFunction;
		if(this.requiredCharacterRoles.length > 0)
		{
			for(String characterRole : this.requiredCharacterRoles)
			{
				String characterName = this.env.scenario().getCharacterByRole(characterRole);
				aplControlFunction = new APLFunction(this.functionName, new APLIdent(characterName));
				this.env.controlTrainee(aplControlFunction);
				this.env.notifyMonitor(aplControlFunction);
			}
		}
		else
		{
			aplControlFunction = new APLFunction(this.functionName);
			this.env.controlTrainee(aplControlFunction);
			this.env.notifyMonitor(aplControlFunction);
		}
	}
	
	public void scenarioUpdate()
	{
		Scenario scenario = this.env.scenario();
		Position traineePosition = scenario.getCharacterPosition("trainee");
		
		boolean buttonEnabled = true;
		
		for(String requiredObject : this.requiredObjects)
		{
			if(!traineePosition.hasNearbyObject(requiredObject))
				buttonEnabled = false;
		}
		
		for(String requiredCharacterRole : this.requiredCharacterRoles)
		{
			String requiredCharacter = scenario.getCharacterByRole(requiredCharacterRole);
			if(!traineePosition.hasNearbyCharacter(requiredCharacter))
				buttonEnabled = false;
		}
		
		this.setEnabled(buttonEnabled);
	}
}