package tests;

import java.util.ArrayList;
import java.util.List;

import jade.core.*;



public class BacklogManager extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static String[] category = {
			"Category 1", "Category 2", "Category 3", "Category 4"
	};
	
	private String[] objectives = {
			"+Category 1", "+Category 2", "+Category 3", "+Category 4"
	};
	
	private List<Activity> activities = new ArrayList<Activity>();
	private int idx_current_objective = 0;
	
	
	protected void setup() {
		auto_configure_activities(20, 5);
		addBehaviour(new BacklogMngBehaviour(this, this));
		

	}


	/*
	 * Loads certain amount of activities into the backlog.
	 */
	protected void auto_configure_activities(int amount, int duration) {
		int j = 0;
		int k = 0;
		String name = "task-";
		String cat;
		int time;
		
		for(int i = 0; i<amount; i++) {
			cat = category[j];
			time = (k * category.length) + duration + j;
			Activity element = new Activity(name + i, cat, time);			
			this.addActivity(element);
			
			if (++j == category.length) {
				k++;
				j = 0;
			}
		}
	}
	
	
	/*
	 *  This method returns the next objective to be used by an Agent.
	 *  Next version, it will assess the activities in backlog to establish a
	 *  reasonable objective in relation to the type and amount of activities.
	 */
	public String get_objective() {
		if (this.idx_current_objective >= 4)
			this.idx_current_objective = 0;
		
		return this.objectives[this.idx_current_objective++];
	}
	
	
	public List<Activity> getActivities() {
		return activities;
	}


	public void addActivity(Activity activity) {
		this.activities.add(activity);
	}
	
	
}
