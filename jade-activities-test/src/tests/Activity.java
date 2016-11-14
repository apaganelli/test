package tests;

import jade.util.leap.Serializable;

/*
 *  This class represents the activity.
 *  
 *  Every activity belongs to a category and has a duration time in general minutes.
 */

@SuppressWarnings("serial")
public class Activity implements Serializable {
	public static int CAT_STATUS_AVAILABLE = 1;
	public static int CAT_STATUS_ALLOCATED = 2;
	public static int CAT_STATUS_ACTIVE = 3;
	public static int CAT_STATUS_LOCKED = 4;
	public static int CAT_STATUS_FINISHED = 0;
		
	private String name;
	private String category;
	private int duration;
	private int executed_time = 0;
	private int status = CAT_STATUS_AVAILABLE;
	
	public Activity(String name, String category, int duration) {
		this.name = name;
		this.setCategory(category);
		this.setDuration(duration);
	}

	public int getExecuted_time() {
		return executed_time;
	}

	public void setExecuted_time(int executed_time) {
		this.executed_time = executed_time;
	}

	public String getName() {
		return this.name;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		if (duration > 0)
			this.duration = duration;
	}
}
