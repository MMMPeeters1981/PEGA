package core;

import java.util.*;

import apapl.*;
import apapl.data.*;

import ui.*;

public class Env extends Environment 
{
	private final boolean log = true;
	private MADSGUI gui;
	
	private Scenario scenario = null;	
	private HashSet<String> characters;
	
	private Timer timer;

	/**
	 * Empty main function, no main necessary.
	 * @param args
	 */
	public static void main(String[] args) 
	{
	}
	
	/**
	 * Initiate the classes variables.
	 */
	public Env()
	{
		this.characters = new HashSet<String>();
		this.timer = new Timer();
	}
	
	/**
	 * Get function for the instantiated scenario.
	 * 
	 * @return [Scenario]: current scenario.
	 */
	public Scenario scenario()
	{
		return this.scenario;
	}
	
	/**
	 * controlTrainee, throws an event to the trainee puppet character in 2apl. 
	 * 
	 * @param aplControlFunction the function to pass in the event.
	 */
	public void controlTrainee(APLFunction aplControlFunction)
	{
		throwEvent(aplControlFunction, "trainee");
	}
	
	/**
	 * notifyMonitor, notifies the monitor of the trainee's actions.
	 * 
	 * @param traineeAction the function the trainee executes.
	 */
	public void notifyMonitor(APLFunction traineeAction)
	{
		throwEvent(new APLFunction( "trainee_action", new APLIdent(traineeAction.getName()) ), "monitor");
	}
	
	/**
	 * placeCharacter, places a character in a position in the scenario.
	 * 
	 * @param characterName is the name of the character to be placed.
	 * @param positionName is the name of the position for the character to be placed at.
	 * 
	 * @return [HashSet<String>]: the nearby characters for this position.
	 */
	public HashSet<String> placeCharacter(String characterName, String positionName)
	{
		if(!this.characters.contains(characterName)) // add the character if it's not known yet.
			this.characters.add(characterName);
		else
		{
			Position oldPosition = this.scenario.getCharacterPosition(characterName); // try to get the original position.
			
			if(oldPosition != null) // if character already has a position, move it.
			{
				HashSet<String> nearbyCharacters = oldPosition.getNearbyCharacters();
				nearbyCharacters.remove(characterName);
				
				if(nearbyCharacters.size() > 0) // tell all characters in the current position the character has departed.
					throwEvent(new APLFunction("character_departed", new APLIdent(characterName)), nearbyCharacters.toArray(new String[nearbyCharacters.size()]));
			}
		}
		
		if(this.scenario.placeCharacter(characterName, positionName)) // place the character in the position.
		{			
			Position newPosition = this.scenario.getPosition(positionName);
			
			HashSet<String> nearbyCharacters = newPosition.getNearbyCharacters();
			
			nearbyCharacters.remove(characterName);
			
			if(nearbyCharacters.size() > 0) // tell the characters in the new position that this character has entered.
				throwEvent(new APLFunction("character_entered", new APLIdent(characterName)), nearbyCharacters.toArray(new String[nearbyCharacters.size()]));
			
			if(characterName.equals("trainee")) // if the trainee was moved, tell the environment that the trainee is present.
				this.gui.traineePresent();
			
			return nearbyCharacters;
		}
		else
			return null;
	}
	
	/**
	 * This method is automatically called whenever an agent enters the MAS.
	 * 
	 * @param agName the name of the agent that just registered
	 */
	protected void addAgent(String agName) 
	{
		log("env> agent " + agName + " has registered to the environment.");
	}
	
	/**
	 * timerEvent, creates a timer and throws target event after a number of seconds.
	 * 
	 * @param agName, the agents name to throw the event to.
	 * @param aplEventName, the event to throw.
	 * @param aplSeconds, the amount of seconds to wait before throwing the event.
	 * 
	 * @return [APLIdent]: indicating success.
	 */
	public Term timerEvent(final String agName, final APLIdent aplEventName, APLNum aplSeconds)
	{
		timer.schedule(new TimerTask() 
		{
			@Override
			public void run()
			{
				throwEvent(new APLFunction("timer", aplEventName), agName);
			}
		}, aplSeconds.toInt()*1000);
		
		return new APLIdent("ok");
	}
	
	/**
	 * @category 2APL function
	 * 
	 * storyMessage, adds the target text to the GUI as if the character said it.
	 * 
	 * @param agName, the character saying the story part.
	 * @param aplTextString, the story part, %t gets replaced by parts of the aplTextVariables list in order.
	 * @param aplTextVariables, the variables to replaced %t in order.
	 * 
	 * @return [APLIdent]: indicating success.
	 * 
	 * @throws ExternalActionFailedException
	 */
	public Term storyMessage(String agName, APLIdent aplTextString, APLList aplTextVariables) throws ExternalActionFailedException
	{
		String storyText = aplTextString.getName();
		LinkedList<Term> textVariables = aplTextVariables.toLinkedList();
		
		for(Term textVariable : textVariables)
			storyText = storyText.replaceFirst("%t", textVariable.toString());
		
		log("env> character " + agName + " attempts to add to the story with \"" + agName + " " + storyText + "\"");
		this.gui.addText(agName + ' ' + storyText);
		
		return new APLIdent("ok");
	}
	
	/**
	 * @category 2APL function
	 * 
	 * speechAct, a character will ask a question to the trainee, after which a popup appears with possible answers.
	 * 
	 * @param agName, the agent calling the speech act.
	 * @param aplAgentName, the character to ask the question.
	 * @param aplQuestionString, the question to be asked.
	 * @param aplPossibleAnswers, a list of possible answers each linked with an apl term return value.
	 * 
	 * @return [Term]: the apl term value linked to the trainee's answer.
	 * 
	 * @throws ExternalActionFailedException
	 */
	public Term speechAct(String agName, APLIdent aplAgentName, APLIdent aplQuestionString, APLList aplPossibleAnswers) throws ExternalActionFailedException
	{
		String question = aplAgentName.getName() + " asks: \"" + aplQuestionString.getName() + "\"";
		
		LinkedList<Term> possibleAnswerTerms = aplPossibleAnswers.toLinkedList();
		ArrayList<TraineeAnswer> traineeAnswers = new ArrayList<TraineeAnswer>();
		
		for(Term possibleAnswerTerm : possibleAnswerTerms)
		{
			LinkedList<Term> possibleAnswer = ((APLList)possibleAnswerTerm).toLinkedList();
			
			String answer = ((APLIdent)possibleAnswer.get(0)).getName();
			Term aplAnswerValue = possibleAnswer.get(1);
			
			traineeAnswers.add(new TraineeAnswer(answer, aplAnswerValue));
		}
		
		TraineeAnswer traineesAnswer = this.gui.askQuestion( question, traineeAnswers.toArray( new TraineeAnswer[traineeAnswers.size()] ) );
		return traineesAnswer.getAnswer();
	}
	
	/**
	 * @category 2APL function
	 * 
	 * placeCharacter, agent (character) places itself in the scenario at the specified position. 
	 * If the character is already a certain position, the character is removed from its current position 
	 * before being placed at the new position.
	 * 
	 * @param characterName [String]: agent (character) calling the function
	 * @param aplPositionName [APLIdent]: name of the position in which the character should be placed.
	 * 
	 * @throwsevent character_entered(characterName) to all other characters also in the position.
	 * @throwsevent character_departed(characterName) to all other characters also in the position.
	 * 
	 * @return [APLList]: list of all other characters also in the position.
	 **/
	public Term placeCharacter(String characterName, APLIdent aplPositionName) throws ExternalActionFailedException
	{
		String positionName = aplPositionName.getName();
		log("env> character " + characterName + " attempts to register at position " + positionName);
		
		try
		{
			HashSet<String> nearbyCharacters = this.placeCharacter(characterName, aplPositionName.getName());
			
			if(nearbyCharacters != null)
			{				
				return APLHelper.createAPLIdentList(nearbyCharacters);
			}
			else
			{
				log("env>[FAIL] failed placing character " + characterName + " at " + positionName);
				return new APLIdent("fail");
			}
		}
		catch (Exception e) 
		{
			System.err.println("env>[ERROR] external action registerCharacter(" + characterName + ", " + positionName + ") failed: " + e.getMessage());
			return new APLIdent("error");
		}
	}
	
	/**
	 * @category 2APL function
	 * 
	 * getNearbyObjects, agent (character) gets all nearby objects in the specified position. 
	 * 
	 * @param agName [String]: agent (character) calling the function
	 * @param aplPositionName [APLIdent]: name of the position for which the nearby object list is requested.
	 * 
	 * @return [APLList]: list of all nearby objects in the specified position.
	 **/
	public Term getNearbyObjects(String agName, APLIdent aplPositionName) throws ExternalActionFailedException
	{
		log("env> agent " + agName + " attempts to get nearby objects for position " + aplPositionName.toString());
		
		try
		{
			Position position = this.scenario.getPosition(aplPositionName.getName());
			
			HashMap<String, ScenarioObject> nearbyObjects = position.getNearbyObjects();
			
			return APLHelper.createAPLList(nearbyObjects.values());
		}
		catch (Exception e) 
		{
			System.err.println("env>[ERROR] external action getNearbyObjects(" + aplPositionName.toString() + ") failed: " + e.getMessage());
			return new APLIdent("error");
		}
	}
	
	/**
	 * @category 2APL function
	 * 
	 * createScenario, creates a Scenario object based on the parameter. This function can only be called by the planner.
	 * 
	 * @param agName, if the agent calling is not the planner, this function does nothing.
	 * @param aplScenarioName, the name of the scenario.
	 * @param aplScenarioDescription, a description of the scenario.
	 * @param aplPositions, all the possible positions for characters/trainee. Each position has a name, a description and its own list of 
	 * 						nearby objects.
	 * @param aplTraineeOptions, all actions the trainee can do in this scenario.
	 * 
	 * @return [APLList]: list of all position names.
	 * 
	 * @throws ExternalActionFailedException
	 */
	public Term createScenario(String agName, APLIdent aplScenarioName, APLIdent aplScenarioDescription, APLList aplPositions, APLList aplTraineeOptions) throws ExternalActionFailedException
	{
		log("env> agent " + agName + " attempts to create scenario " + aplScenarioName.getName() + ", " + aplScenarioDescription.getName() + ":\n" + aplPositions.toString() + "\nTrainee options:" + aplTraineeOptions.toString());

		/*	added	*/
		if( this.gui != null || this.scenario != null) {
			
			this.gui.endGui();									
			//this.gui = null;
			//this.scenario = null;			
		}
		/*	added	*/
		
		if(agName.compareTo("planner") == 0)
		{
			try
			{
				ArrayList<ScenarioOption> traineeOptions = new ArrayList<ScenarioOption>();
				
				LinkedList<Term> traineeOptionTerms = aplTraineeOptions.toLinkedList();
				for(Term traineeOptionTerm : traineeOptionTerms)
				{
					LinkedList<Term> traineeOption = ((APLList)traineeOptionTerm).toLinkedList();
					
					String optionName = ((APLIdent)traineeOption.get(0)).getName();
					String optionFunctionName = ((APLIdent)traineeOption.get(1)).getName();
					ArrayList<String> requiredObjects = APLHelper.createIdentArrayList(((APLList)traineeOption.get(2)));
					ArrayList<String> requiredCharacterRoles = APLHelper.createIdentArrayList(((APLList)traineeOption.get(3)));
					
					traineeOptions.add( new ScenarioOption( this, optionName, optionFunctionName, 
														   requiredObjects.toArray(new String[requiredObjects.size()]), 
														   requiredCharacterRoles.toArray(new String[requiredCharacterRoles.size()])) );
				}
				
				this.scenario = new Scenario(aplScenarioName, aplScenarioDescription, aplPositions);
				
				this.gui = new MADSGUI( this, traineeOptions.toArray(new ScenarioOption[traineeOptions.size()]) );
				this.gui.setVisible(true);
				this.gui.startAnimation();				
				
				return this.scenario.getPositionNames();
			}
			catch (Exception e)
			{
				System.err.println("env>[ERROR] external action createScenario(" + aplScenarioName.getName() + ") failed: " + e.getMessage());
				return new APLList();
			}
		}

		return new APLList();
	}
	
	/**
	 * @category 2APL function
	 * 
	 * giveRole, tells the environment that a certain character has a certain role.
	 * 
	 * @param agName, the agent calling the function
	 * @param aplCharacterName, the name of the character who has the role.
	 * @param aplRoleName, the name of the role for the character.
	 * 
	 * @return [Term]: the apl term value linked to the trainee's answer.
	 * 
	 * @throws ExternalActionFailedException
	 */
	public Term giveRole(String agName, APLIdent aplCharacterName, APLIdent aplRoleName) throws ExternalActionFailedException
	{
		try
		{
			if(this.scenario != null)
				this.scenario.giveRole(aplCharacterName.getName(), aplRoleName.getName());
			else
				return new APLIdent("fail");
		}
		catch (Exception e) 
		{
			System.err.println("env>[ERROR] external action giveRole(" + agName + ", " + aplCharacterName.getName() + ", " + aplRoleName.getName() + ") failed: " + e.getMessage());
			return new APLIdent("error");
		}
		
		return new APLIdent("ok");
	}
	
	private void log(String str) 
	{
		if (log) System.out.println(str);
	}
}