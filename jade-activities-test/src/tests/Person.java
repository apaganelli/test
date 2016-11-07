package tests;

import java.util.ArrayList;
import java.util.List;
import jade.core.*;


public class Person extends Agent {
	/**
	 * 
	 */
	public static final int NUM_CATEGORIES = 4;
	private static final long serialVersionUID = 1L;
	
	private List<String> goals = new ArrayList<String>();
	private int workingTime = 100;
	private int workedTime = 0;
	private boolean[] categories = new boolean[NUM_CATEGORIES];

	protected void setup() {
		addBehaviour(new PersonBehaviour(this));
	}
	
	public int subtractWorkingTime(int value) {
		if (value < this.workingTime)
			this.workingTime -= value;
		else
			this.workingTime = 0;
		
		return this.workingTime;
	}
	
	public int addWorkedTime(int time) {
		if (time > 0)
			this.workedTime += time;
		
		return this.workedTime;
	}
	
	public void setCategory(int idx_category, boolean value) {
		if (idx_category < NUM_CATEGORIES)
			this.categories[idx_category] = value;
			
	}
	
	public boolean getCategory(int idx_category) {
		if (idx_category < NUM_CATEGORIES)
			return Boolean.FALSE;
					
		return this.categories[idx_category];
	}

	public List<String> getGoals() {
		return goals;
	}

	public void setGoals(String goal) {
		this.goals.add(goal);
	}
		
	
}

