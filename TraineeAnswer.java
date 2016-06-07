package ui;

import javax.swing.JRadioButton;

import apapl.data.Term;

public class TraineeAnswer extends JRadioButton
{
	private static final long serialVersionUID = 1L;

	private final Term aplAnswerValue;
	
	public TraineeAnswer(String answer, Term aplAnswerValue)
	{
		super(answer);
		
		this.aplAnswerValue = aplAnswerValue;
	}
	
	public Term getAnswer()
	{
		return this.aplAnswerValue;
	}
}
