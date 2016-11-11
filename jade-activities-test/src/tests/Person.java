/*
 * Class Person is a consumer of activities/tasks following general policies and personal objectives.
 * 
 * Each Person has some working time in which it should execute as many activities as possible.
 * Any available time ideally should be filled out with new activities.
 * 
 * There is a backlog of activities where each Person asks for an objective which depends on the amount
 * of activities per category, total duration and number of active workers (Person).
 * 
 * After getting one objective, Person starts getting activities from the backlog based on its given
 * objective. However, the activities are selected randomly by the backlog. The backlog tries to increase
 * the change of providing activities related to Person's objective, but it is not guarantee.
 * 
 * Meanwhile Persons fills up theirs working time, they may negotiate activities with their pairs based on 
 * the needs to optimise its portfolio of activities. Person might exchange with pairs objectives 
 * when their portfolio of activities are very distant from their objectives.
 * 
 * The general objective of the system is to consume the maximum of activities in the backlog following
 * the available resources (sum of working time of all workers) and their personal objectives.
 * 
 * Each activity belongs to a category and has a duration. It may have an associated cost, a deadline and
 * a starting time. It may require some skills. It means that only skilled Person can executed that 
 * activity. 
 * 
 * The environment may have more than one backlog (warehouse of activities - another agent) and they may 
 * negotiate activities as well, specialising in some category of activity.
 * 
 * The Warehouses or Person might start new workers to help them in order to consume/profit more activities.
 * 
 */

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
	
	public int getWorkingTime() {
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

