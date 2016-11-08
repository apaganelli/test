package tests;

import java.util.List;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

/*
 *  Next steps:
 *  	Request an activity based on current objective. (ok)
 *  	Backlog will select one activity randomly, but given a priority for agent's objective.
 *  	Withdraw the activity from the backlog and give it to the Agent.
 *  	Agent should store the activity and update working time.
 *  
 *  	Communication among Agents to exchange Activities.
 *  	Create the messages and exchange mechanisms
 *  
 *  	May refuse activities that will surpass working time.
 *  	Exchange activities with backlog.
 *  	May implement combination of objectives.
 *  
 *  	Implement another Warehouse of activities and open the negotiation between all members (n x n).
 *  
 *  	After all basic and simple mechanisms running, implement adaptation and reasoning mechanisms
 *  	in order to optimise the solution.
 */

public class PersonBehaviour extends FSMBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Person p;
	
	public PersonBehaviour(Agent a) {
		super(a);
		this.p = (Person) a;
		
		// Register state A (first state)
		this.registerFirstState(new PhaseOne(a), "a");
		this.registerLastState(new PhaseTwo(), "b");
		this.registerDefaultTransition("a", "b");	
	}
	
	/*
	 *  During PhaseOne the objective is to get an objective for this Agent.
	 *  
	 */
	private class PhaseOne extends SimpleBehaviour {
		/**
		 *  This action aims to get an objective to our Agent.
		 */
		private static final long serialVersionUID = 1L; 
		private int stage;
		
		public PhaseOne(Agent a) {
			super(a);
			this.stage = 1;
		}
		
		public void action() {
			switch(this.stage) {
			case 1:
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				AID r_aid = new AID ("Backlog-mng", AID.ISLOCALNAME);
				msg.addReceiver(r_aid);
				msg.setProtocol("OBJECTIVE");
				msg.setContent("GET OBJECTIVE");
				
				System.out.println(myAgent.getLocalName() + " is requesting an Objective.");
				
				myAgent.send(msg);

				this.stage++;

				break;
			case 2:
				MessageTemplate mt = MessageTemplate.MatchProtocol("OBJECTIVE");
				
				msg = myAgent.receive(mt);

				if (msg != null) {
					p.setGoals(msg.getContent());
					this.stage++;
				}				

				break;
			}
				
		}
		
		public boolean done() {
			if (this.stage > 2) {
				System.out.println(myAgent.getLocalName() + " got an Objective " + p.getGoals());
				return true;
			}
			
			return false;
		}		
	}
	
	/*
	 * After getting an objective the Agent should get activities from backlog and/or exchange activities among
	 * other Agents.
	 */
	private class PhaseTwo extends CyclicBehaviour {
		/**
		 *  This class will keep the Agent alive until it fills up the working time.
		 */
		private static final long serialVersionUID = 1L;
		private int stage = 1;
		
		public void action() {
			
			switch(this.stage) {
			case 1:
				/*
				 *  Just to show the activities registered in the backlog.
				 */
				System.out.println(myAgent.getLocalName() + " getting activities and negotiation.");
				{
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					AID r_aid = new AID ("Backlog-mng", AID.ISLOCALNAME);
					msg.addReceiver(r_aid);
					msg.setProtocol("ACTIVITY");
					msg.setContent("SHOW_ACTIVITIES");
					myAgent.send(msg);
				} 
				block(300);
				this.stage++;
				break;
			case 2:
				/*
				 *  The request of activity may consider one category preference (+/-) 
				 *  inform high or low preference and time_limit. 
				 */
				System.out.println(myAgent.getLocalName() + " - Asking one activity.");
				{
					ACLMessage msg1 = new ACLMessage(ACLMessage.REQUEST);
					AID r_aid = new AID ("Backlog-mng", AID.ISLOCALNAME);
					msg1.addReceiver(r_aid);
					List<String> goals = p.getGoals();
					msg1.setProtocol("ACTIVITY");
					msg1.setContent("GET_ACTIVITY:" + goals.get(0) + ":" + p.getWorkingTime());
					myAgent.send(msg1);
				}
				this.stage++;
				block(300);
				break;
				
			case 3:
				/*
				 *  Receiving an activity from backlog.
				 */
				ACLMessage msg2 = myAgent.receive();
				if (msg2 != null) {
					System.out.println("FSM completed. " + myAgent.getLocalName());
					System.out.println(msg2.getContent());
					this.stage++;
				}
				break;
			case 4:
				/*
				 *  If decide to exchange with other agents, that is the right time.
				 *  If not, just wait to make a new request or finishes if working time is ok.
				 *  May implement a token pass in order to organise orders to backlog.
				 *  Thus, getting from backlog would be ordered and exchange among agents asynchronous.
				 */
				break;
			}			
		}		
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see jade.core.behaviours.FSMBehaviour#onEnd()
	 */
	public int onEnd() {
		System.out.println("FSM completed. " + myAgent.getLocalName());
		return super.onEnd();
	}
	
	
	

}
