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
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;


public class Worker extends Agent {
	/**
	 * 
	 */
	public static final int NUM_CATEGORIES = 6;
	private static final long serialVersionUID = 1L;
	
	private List<String> goals = new ArrayList<String>();
	private int workingTime = 100;
	private int workedTime = 0;
	private double superiorBound = 1.2;
	private int[] categories = new int[NUM_CATEGORIES];
	private List<Activity> activities = new ArrayList<Activity>();

	/*
	 *  Creates the agent
	 */
	public Worker(int wkTime, double bound) {
		this.workingTime = wkTime;
		this.superiorBound = bound;
	}
	
	
	/*
	 * Initialises the agent.
	 * Register in DF and add its behaviour.
	 * 
	 */
	protected void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Exchange Activities");
		sd.setName(this.getName());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			System.out.println("Erro ao registrar agente");
			e.printStackTrace();
		}
		
		
		addBehaviour(new WorkerBehaviour(this, this));
	}
	
	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	

	public int getWorkingTime() {
		return this.workingTime;
	}
	
	public void addWorkedTime(int time) {
		this.workedTime += time;
	}
	

	/*
	 *  Stores one activity and update status
	 */
	public void setActivity(Activity item) {
		
		if (item != null) {
			item.setStatus(Activity.CAT_STATUS_ALLOCATED);
			this.categories[BacklogManager.getCategoryIndex(item.getCategory())]++;
			this.addWorkedTime(item.getDuration());
			this.activities.add(item);
			System.out.println(getLocalName() + " stored activity " + item.getName() + " type " + item.getCategory() + " and worked time now is "+ this.getWorkedTime());
		}
	}
	
	/*
	 * Prints out the list of activities.
	 */
	
	public void listActivities() {
		String name = this.getLocalName();
		
		System.out.println(name + " list of all allocated activities.");
		
		for(Activity item : activities)
			System.out.println(item.getName() + "  " + item.getCategory() + " duration " + item.getDuration());
		
		System.out.println(name + "   ------  End of activities ----");
	}

	/*
	 * Receives the name of the category and returns the amount of item within this category.
	 * Or "all" for all categories.
	 */
	public int checkAmountItemsCategory(String cat) {
		int amount = 0;
		
		if (cat.equalsIgnoreCase("All"))
			for(int i = 0; i < Worker.NUM_CATEGORIES; i++)
				amount += this.categories[i];
		else 
			amount = this.categories[BacklogManager.getCategoryIndex(cat)];

		return amount;
	}
	
	
	/*
	 *  Intelligent method that analyses if the worker should exchange some activities.
	 */
	public int checkExchangeActivity() {
		int result = 0;
		
		if (! this.hasActvitiestoExchange())
			return result;
		
		if (! this.checkAllCategoriesAttended())
			result = 1;
		
		if (! this.checkObjectiveAttended())
			result = 3;
				
		return result;
	}

	
	//
	// It returns true, if there are 2 activities or more in one category that is not its objective.
	// Should treat when the objective is minus category.
	public boolean hasActvitiestoExchange() {
		int idx_goal = BacklogManager.getCategoryIndex(this.goals.get(0).substring(1));
		
		for(int i = 0; i < Worker.NUM_CATEGORIES; i++)
			if ((this.categories[i] > 1) && (idx_goal != i)) {
				return true;	
			}
		
		return false;
	}

	public boolean hasActvitiestoExchange(String cat) {
		int idx_goal = BacklogManager.getCategoryIndex(this.goals.get(0).substring(1));
		int idx_wished = BacklogManager.getCategoryIndex(cat);
		
		if (idx_wished != idx_goal && this.categories[idx_wished] > 1)
			return true;	

		return false;
	}

	
	
	/*
	 *  Get (if the parameter get is true) or select (false) an activity to exchange.
	 */
	public Activity getActivitytoExchange(boolean get, String category, int maxTime) {
		Activity item = null;
		String goal = this.goals.get(0).substring(1);
		int i = 0;
		int selected = -1;
		int time = 0;
				
		for(Activity task: this.activities) {
			if(task.getStatus() != Activity.CAT_STATUS_LOCKED && ! task.getCategory().equalsIgnoreCase(goal)) {
				if(task.getCategory().equalsIgnoreCase(category) && task.getDuration() > time) {
					selected = i;
					time = task.getDuration();
				}
			}
			i++;	
		}
		
		if (selected != -1) {
			if (get) {
				item = this.activities.remove(selected);
				this.categories[BacklogManager.getCategoryIndex(item.getCategory())]--;
				this.workedTime -= item.getDuration();
			}
			else
				item = this.activities.get(selected);
		}
				
		return item;
	}

	/*
	 * 
	 */
	public String getActivitytoExchange(String category) {
		String items;
		String duration = "";
		String names = "";
		String goal = this.goals.get(0).substring(1);
				
		for(Activity task: this.activities) {
			if(task.getStatus() != Activity.CAT_STATUS_LOCKED && ! task.getCategory().equalsIgnoreCase(goal)) {
				if(task.getCategory().equalsIgnoreCase(category)) {
					names += task.getName() + ",";
					duration += task.getDuration() + ",";
				}
			}	
		}
		
		items = category + ":" + names + ":" + duration;
		return items;
	}
	
	//
	// Removes one specific activity by name.
	//
	public Activity getActivity(String name) {
		Activity item = null;
		
		for(int i = 0; i < this.activities.size(); i++)
			if (this.activities.get(i).getName().equalsIgnoreCase(name)) {
				item = this.activities.remove(i);				
				this.categories[BacklogManager.getCategoryIndex(item.getCategory())]--;
				this.workedTime -= item.getDuration();			
				break;
			}
		
		return item;
	}
	
		
	public List<String> getGoals() {
		return goals;
	}

	public void setGoals(String goal) {
		this.goals.add(goal);
	}

	public int getWorkedTime() {
		return this.workedTime;
	}
	
	
	public double getSuperiorBound() {
		return this.superiorBound;
	}
	
	public int[] getTotalbyCategory() {
		int[] totals = new int[Worker.NUM_CATEGORIES];
		int j = 0;
		
		for (Activity item: this.activities) {
			j = BacklogManager.getCategoryIndex(item.getCategory());
			totals[j] += item.getDuration();
		}
		
		return totals;
	}
	
	/*
	 * Checks if every category has at least one activity.
	 */
	private boolean checkAllCategoriesAttended() {
		for(int i = 0; i < Worker.NUM_CATEGORIES; i++)
			if (this.categories[i] == 0)
				return false;		
		return true;
	}

	/*
	 *  Check if the programmed worked time is in accordance with the given objective(s)
	 */
	protected boolean checkObjectiveAttended() {
		int min = Integer.MAX_VALUE;
		int idx_min = -1;
		int max = 0;
		int idx_max = -1;
		int[] time = new int[Worker.NUM_CATEGORIES];
		List<String> goals = this.getGoals();
		
		/*
		 * Gets the working duration for each category.
		 */
		for(Activity item : this.activities) {
			time[BacklogManager.getCategoryIndex(item.getCategory())] += item.getDuration();
		}
		
		/*
		 *  Checks the category with max and min time.
		 */
		for(int i = 0; i < Worker.NUM_CATEGORIES; i++) { 
			if (time[i] > max) {
				max = time[i];
				idx_max = i;
			}
			
			if (time[i] < min) {
				min = time[i];
				idx_min = i;
			}
		}
		
		/*
		 *  Checks if the objective is being accomplished.
		 */
		if (goals.get(0).startsWith("+"))
			if (BacklogManager.getCategoryIndex(goals.get(0).substring(1)) != idx_max)   
				return false;
			else
				if (max < this.workingTime * 0.8)
					return false;
		else
			if(goals.get(0).startsWith("-"))
				if (BacklogManager.getCategoryIndex(goals.get(0).substring(1)) != idx_min)   
					return false;			
		
		return true;
	}
			
}

