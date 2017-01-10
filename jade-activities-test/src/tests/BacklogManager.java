package tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jade.core.*;


public class BacklogManager extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static String[] category = {
			"Category 1", "Category 2", "Category 3", "Category 4", "Category 5", "Category 6"
	};
	
	private String[] objectives = {
			"+Category 1", "+Category 2", "+Category 3", "+Category 4", "+Category 5", "+Category 6"
	};
	
	private List<Activity> activities = new ArrayList<Activity>();
	private int idx_current_objective = 0;
	
	/*
	 * Configure backlog.
	 * 
	 */
	protected void setup() {
		auto_configure_activities(1000);
		addBehaviour(new BacklogMngBehaviour(this, this));
	}

	/*
	 * Loads a given amount of activities randomly into the backlog.
	 * Assumes that max activity time will be MAX_TIME+1 (defined here).
	 * Categories are approximately equally distributed and also chosen randomly.
	 */
	protected void auto_configure_activities(int amount) {
		Random rand = new Random();
		String name = "task-";
		String selectedCategory;
		int MAX_TIME = 30;
		int time;
		
		for(int i = 0; i<amount; i++) {
			selectedCategory = category[rand.nextInt(10000) % category.length];
			time = rand.nextInt(MAX_TIME) + 1;
			Activity element = new Activity(name + i, selectedCategory, time);			
			this.addActivity(element);
		}
	}
	
	
	/*
	 *  This method returns the next objective to be used by an Agent.
	 *  Next version, it will assess the activities in backlog to establish a
	 *  reasonable objective in relation to the type and amount of activities.
	 */
	public String get_objective() {
		if (this.idx_current_objective >= Worker.NUM_CATEGORIES)
			this.idx_current_objective = 0;
		
		return this.objectives[this.idx_current_objective++];
	}
	
	/*
	 * Auxiliary function to return a category index.
	 */
	public static int getCategoryIndex(String cat) {		
		for(int i=0; i<category.length; i++) {
			if (category[i].equalsIgnoreCase(cat))
				return i;
		}		
		return -1;
	}
		
	
	public List<Activity> getActivities() {
		return activities;
	}


	public void addActivity(Activity activity) {
		this.activities.add(activity);
	}
	
	/*
	 *  Get an specific activity by its name.
	 */
	public Activity getActivitybyName(String name) {
		if ( activities.isEmpty() || activities.size() == 0) {
			System.out.println(this.getLocalName() +  " is empty\n");
			return null;
		}
		
		Activity item = null;
		int i = 0;
		
		for(Activity task : activities) {
			if(task.getName().equalsIgnoreCase(name)) {
				item = activities.remove(i);
				return item;
			}
			i++;	
		}
		
		return item;
	}
	
	
	/*
	 * Selects one activity randomly based on the number of activities.
	 * If the randomly selected activity has not the same category as the desired objective,
	 * gets a new activity randomly again.
	 * 
	 */	
	public Activity getOneActivity(String wishedCategory) {
		
		if ( activities.isEmpty() || activities.size() == 0) {
			System.out.println(this.getLocalName() +  " is empty\n");
			return null;
		}
		
		Activity item = null;
		Random rand = new Random();
		int size = activities.size();
		int index = -1;
						
		index = rand.nextInt(100000) % size;
		item = activities.get(index);
		
		if (!item.getCategory().startsWith(wishedCategory, 1)) {
			index = rand.nextInt(100000) % size;
			item = activities.get(index);			
		}
		
		return item;
	}
		
}
