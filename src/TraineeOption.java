package ui;

import javax.swing.JButton;

public abstract class TraineeOption extends JButton
{	
	private static final long serialVersionUID = 1L;
	
	public TraineeOption(String buttonName)
	{
		super(buttonName);
	}
	
	abstract void performOption();
	abstract void scenarioUpdate();
}
