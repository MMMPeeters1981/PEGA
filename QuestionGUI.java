package ui;

import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.*;

public class QuestionGUI extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	private ButtonGroup answerGroup;
	private TraineeAnswer[] possibleAnswers;
	
	private TraineeAnswer traineeAnswer = null;
	
	public QuestionGUI(MADSGUI owner, String question, TraineeAnswer... traineeAnswers)
	{
		super(owner, question, true);
		
		this.setLayout(new GridLayout(traineeAnswers.length+1, 1));
		
		this.possibleAnswers = traineeAnswers;
		this.answerGroup = new ButtonGroup();
		
		for(TraineeAnswer traineeAnswer : traineeAnswers)
		{
			answerGroup.add(traineeAnswer);
		}
		
		for(TraineeAnswer traineeAnswer : traineeAnswers)
		{
			this.add(traineeAnswer);
		}
		
		JButton confirmButton = new JButton("Confirm");
		confirmButton.addActionListener(new ActionListener()
		{
            public void actionPerformed(ActionEvent event)
            {
            	traineeAnswer = null;
            	
            	for(TraineeAnswer answer : possibleAnswers)
            	{
            		if(answer.isSelected())
            		{
            			traineeAnswer = answer;
            			dispose();
            		}
            	}
            	
            	if(traineeAnswer == null)
            	{
            		JOptionPane.showMessageDialog(null, "Please select an answer.", "Error", JOptionPane.ERROR_MESSAGE);
            	}
            }
        });
		
		this.add(confirmButton);
		
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
	}
	
	public TraineeAnswer getAnswer()
	{
		return this.traineeAnswer;
	}
}
