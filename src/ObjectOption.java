package ui;

import apapl.data.APLFunction;
import apapl.data.APLIdent;
import core.Env;
import core.Position;
import core.Scenario;
import core.ScenarioObject;

public class ObjectOption extends TraineeOption
{
	private static final long serialVersionUID = 1L;

	private final Env env;
	private final ScenarioObject object;
	private boolean pickedUp = false;
	
	public ObjectOption(Env env, ScenarioObject object)
	{
		super("pickup object " + object.name());
		
		this.env = env;
		this.object = object;
		
		this.setEnabled(false);
	}
	
	public void performOption()
	{
		if(!pickedUp) // pickup object
		{
			if(this.env.scenario().pickupObject("trainee", object))
			{
				this.env.controlTrainee( new APLFunction( "pickup", new APLIdent( this.object.name() ) ) );
				this.setText("drop object " + object.name());
				pickedUp = true;
			}
		}
		else // drop object
		{
			if(this.env.scenario().dropObject("trainee", object))
			{
				this.env.controlTrainee(new APLFunction("drop", new APLIdent(this.object.name())));
				this.setText("pickup object " + object.name());
				pickedUp = false;
			}
		}
	}
	
	public void scenarioUpdate()
	{
		Scenario scenario = this.env.scenario();
		Position traineePosition = scenario.getCharacterPosition("trainee");
		
		boolean buttonEnabled = traineePosition.hasNearbyObject(this.object) && this.object.isAvailable();
		
		this.setEnabled(buttonEnabled);
	}
}
