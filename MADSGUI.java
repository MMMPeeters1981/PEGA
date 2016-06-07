package ui;

import javax.swing.*;

import core.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MADSGUI extends JFrame
					 implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private final int windowWidth = 600;
	private final int optionPanelWidth = windowWidth/4;
	private final int storyPanelWidth = windowWidth-optionPanelWidth;
	private final int windowHeight = 400;
	
	private final Env environment;
	
	private ArrayList<TraineeOption> traineeOptions;
	private boolean traineePresent = false;
	
	private FrameCaller frameCaller;
	private Thread frameCallerThread;
	
	private JPanel optionPanel;
	private JScrollPane optionScrollPane;
	private JPanel storyPanel;
	private JTextArea storyTextArea;
	
	public MADSGUI(Env environment, ScenarioOption... scenarioOptions)
	{
		this.environment = environment;
		this.traineeOptions = new ArrayList<TraineeOption>();
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
		
		this.optionPanel = new JPanel();
		this.optionPanel.setPreferredSize(new Dimension(optionPanelWidth, windowHeight));
		this.storyPanel = new JPanel();
		this.storyPanel.setPreferredSize(new Dimension(storyPanelWidth, windowHeight));
		
		/* set up the option panel */
		
		HashMap<String, Position> positions = this.environment.scenario().getPositions();
		
		for(Position position : positions.values())
		{
			this.traineeOptions.add( new MovementOption(this.environment, position) ); // Add possible movement options
			
			for(ScenarioObject object : position.getNearbyObjects().values())
				this.traineeOptions.add( new ObjectOption(this.environment, object) );
		}
		
		for (ScenarioOption scenarioOption : scenarioOptions)
			this.traineeOptions.add(scenarioOption); // Add scenario defined options
		
		for (TraineeOption option : this.traineeOptions)
		{
			int buttonSize = optionPanelWidth-10;
			option.setPreferredSize(new Dimension(buttonSize, buttonSize/4));
			option.setMinimumSize(new Dimension(buttonSize, buttonSize/4));
			option.setMaximumSize(new Dimension(buttonSize, buttonSize/4));
			
			option.addActionListener(this);
			
			this.optionPanel.add(option);
		}
		
		/* set up the story panel */
		this.storyTextArea = new JTextArea();
		this.storyTextArea.setSize(storyPanel.getSize());
		this.storyTextArea.setEditable(false);
		
		storyPanel.add(this.storyTextArea);
		
		this.optionScrollPane = new JScrollPane(this.optionPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		this.add(this.optionScrollPane);
		this.add(this.storyPanel);
		
		//this.setResizable(false);
		
		this.pack();
		
		this.frameCaller = new FrameCaller(this, 30);
	}
	
	
	/*	added	*/
	/** Procedure for ending GUI	**/
	public void endGui()
	{
		
		this.stopAnimation();									// Will stop the current run-loop
		this.frameCallerThread.interrupt();						// Will stop the thread
		this.setVisible(false); 								// Stop displaying the GUI
		this.dispose();
	}
	
	/*	added	*/
	
	
	/** Start updates and animation */
	public void startAnimation()
	{
		this.frameCaller.setHalt(false);						// Will prevent the run-loop from halting
		this.frameCallerThread = new Thread(this.frameCaller); 	// Create new thread
		this.frameCallerThread.start();							// Start it
	}
	
	/** Stop updates and animation */
	public void stopAnimation()
	{
		this.frameCaller.setHalt(true); 						// Will stop the current run-loop		
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		
		/*
		 * Make trainee agent puppet receive a general event which handles everything
		 */
		if(source instanceof TraineeOption)
			((TraineeOption)source).performOption();		
	}
	
	public void addText(String text)
	{
		this.storyTextArea.append(text + "\n");
	}
	
	public TraineeAnswer askQuestion(String question, TraineeAnswer... traineeAnswers)
	{
		this.addText(question);
		
		QuestionGUI questionGUI = new QuestionGUI(this, question, traineeAnswers);
		return questionGUI.getAnswer();
	}
	
	public void traineePresent()
	{
		this.traineePresent = true;
	}
	
	/*public void frameUpdate()
	{
		for(int nr = 0; nr < queens.length; nr++)
		{
			queens[nr] = queens[nr] + (int)((gl.get_board()[nr]*cell_size-queens[nr])*0.2d); // Update the queen positions
		}
	}*/
	
	public void repaint()
	{
		super.repaint();
		
		if(traineePresent)
		{
			for(TraineeOption traineeOption : this.traineeOptions)
				traineeOption.scenarioUpdate();
		}
	}
	
	private class FrameCaller implements Runnable 
	{
		MADSGUI frame; 		// Panel on which the world is drawn
		int fps;		 		// Frames per second
		boolean halt = false;   // Halt condition
		
		public FrameCaller(MADSGUI frame, int fps)
		{
			this.frame = frame; 
			this.fps = fps;
		}
		
		public void run()
		{
			try
			{
				while(!halt)
				{
					Thread.sleep(1000/fps); // Sleep a bit
					frame.repaint();		// Repaint the state
				}
			}
			catch(Exception e)
			{ 
				//e.printStackTrace(); 
			}
		}
		
		/**
		 * If this runnable is running and setHalt(true) is called, then this runnable will break the run loop.
		 * @param b New value of halt.
		 */
		public void setHalt(boolean b)
		{
			halt = b;
		}
	}
}
