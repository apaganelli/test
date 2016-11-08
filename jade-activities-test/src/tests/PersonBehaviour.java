package tests;

import jade.core.*;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

/*
 *  Next steps:
 *  	Request an activity based on current objective.
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
	 * other Agents
	 */
	private class PhaseTwo extends SimpleBehaviour {
		/**
		 *  This class will keep the Agent alive until it fills up the working time.
		 */
		private static final long serialVersionUID = 1L;
		private int stage = 1;
		
		public void action() {
			
			System.out.println(myAgent.getLocalName() + " getting activities and negotiation.");
			
			switch(this.stage) {
			case 1:
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				AID r_aid = new AID ("Backlog-mng", AID.ISLOCALNAME);
				msg.addReceiver(r_aid);
				
				if (p.getLocalName().equals("José"))
					msg.setProtocol("ERR");
				else
					msg.setProtocol("ACTIVITY");
				
				msg.setContent("SHOW_ACTIVITIES");
				
				this.stage++;
				myAgent.send(msg);
				break;
			case 2:
				this.stage++;
				break;
			}
			
		}
				
		public boolean done() {
			if(this.stage > 2)
				return false;
			
			return true;
		}
	}

	
	
	public int onEnd() {
		System.out.println("FSM completed. " + myAgent.getLocalName());
		return super.onEnd();
	}
	
	
	

}
