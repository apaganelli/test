package tests;

import java.io.IOException;
import java.util.List;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.core.AID;

/*
 *  Next steps:
 *  	Request an activity based on current objective. (ok)
 *  	Backlog will select one activity randomly, but given a priority for agent's objective. (ok)
 *  	Withdraw the activity from the backlog and give it to the Agent. (ok)
 *  	Agent should store the activity and update working time. (ok)
 *  	Deal with an empty backlog. (ok)
 *  		May create/receive more activities automatically.
 *  		May say to agents to update their list of available backlogs
 *  	Limit getting new activities from backlog when worked time exceed worker superiorBound. (ok)
 *  
 *  	Communication among Agents to exchange Activities.
 *  		Register workers in DF (ok)  
 *  		Check if can or should exchange (oK)
 *  		Propose (Ok) 
 *  		Receive and verify (Ok)
 *  		Perform (Ok)
 *  		Refuse
 *  		Check for new workers
 *  		Deal with many partners at same time.
 *  		Suspend/delete/register again in DF
 *  
 *  	Register backlog-managers in DF.
 *  
 *  	Create the messages and exchange mechanisms
 *  
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
	
	public PersonBehaviour(Agent a, Person p) {
		super(a);
		
		// Register state A (first state)
		this.registerFirstState(new PhaseOne(p), "a");
		this.registerLastState(new PhaseTwo(p), "b");
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
		private Person p;
		
		public PhaseOne(Person a) {
			super(a);
			this.p = a;
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
		private Activity item;
		private Person p;
		private DFAgentDescription[] partners;
		
		public PhaseTwo(Person a) {
			super(a);
			this.p = a;

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Exchange Activities");
			template.addServices(sd);
				
			try {
				partners = DFService.search(p, template);
				
				System.out.println(partners.length);
				
				for(int i=0; i < partners.length; i++) {
					 if (! partners[i].getName().getName().equalsIgnoreCase(p.getAID().getName())) {
					 	 System.out.println("DF - other registered workers to exchange " + partners[i].getName().getLocalName());
					 }
				}
				
			} catch (FIPAException e) {
				System.out.println("Erro ao consultar DF");
				e.printStackTrace();
			}
		}
		
		
		/*
		 *   Cycle to request activities and exchange activities with other agents
		 * 
		 */				
		public void action() {
			String name = myAgent.getLocalName();
	
			MessageTemplate mt = MessageTemplate.MatchProtocol("EXCHANGE_ACTIVITY");
			ACLMessage ex_msg = p.receive(mt);
			
			if(ex_msg != null) {
				Inform inform = new Inform("CASE 5 (" + ex_msg.getPerformative() + ")");
				ExchangeActivity(p, ex_msg.getPerformative(), ex_msg);				
				this.stage = 2;
			}
	
			switch(this.stage) {
			case 1:
				/*
				 *  Just to show the activities registered in the backlog.
				 */
				System.out.println(name + " getting activities and negotiation.");
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
				
				
				if (p.getWorkedTime() < (p.getWorkingTime() * p.getSuperiorBound())) {
					System.out.println("CASE 2 = " + name + " == Asks new activity.");
					ACLMessage msg1 = new ACLMessage(ACLMessage.REQUEST);
					AID r_aid = new AID ("Backlog-mng", AID.ISLOCALNAME);
					msg1.addReceiver(r_aid);
					List<String> goals = p.getGoals();
					msg1.setProtocol("ACTIVITY");
					msg1.setPerformative(ACLMessage.REQUEST);
					int limit = (int) (p.getWorkingTime()*p.getSuperiorBound()) - p.getWorkedTime();
					msg1.setContent("GET_ACTIVITY:" + goals.get(0) + ":" + limit);
					myAgent.send(msg1);
					this.stage = 3;
				}
				else {
					// We have already surpass our capacity to carry out the activities.
					@SuppressWarnings("unused")
					Inform inform = new Inform(name + " Superior Bound reached " + p.getWorkedTime(), "CASE 2B - Inform");
					this.stage = 4;
				}
				
				break;
				
			case 3:
				/*
				 *  Receiving an activity from backlog.
				 */
				MessageTemplate mt2 = MessageTemplate.MatchProtocol("ACTIVITY");
				ACLMessage msg2 = myAgent.receive(mt2);
				
				if (msg2 != null) {
					int code = msg2.getPerformative();
					System.out.println("CASE 3 received from backlog. " + name + " Performative " + code);
				
					switch(code) {
					case ACLMessage.AGREE:
						System.out.println("Storing object "  + name);
						try {
							item = (Activity) msg2.getContentObject();
							p.setOneActivity(item);
						} catch (UnreadableException e) {
							System.out.println("Não conseguiu pegar o item.");
							e.printStackTrace();
						}
						
						this.stage = 4;
						break;
						
					case ACLMessage.PROPOSE:
						String[] cmd = msg2.getContent().split(":");
						String goal = p.getGoals().get(0).substring(1);
						int duration = Integer.parseInt(cmd[1]);
						String id = cmd[2];
						System.out.println("Analizing proposal id :" + cmd[2] + ":" + cmd[0] + ": " + name + " Objective: " + goal);
						ACLMessage reply = msg2.createReply();
						reply.setContent("GET_ACTIVITY");

						if ((duration + p.getWorkedTime()) > (p.getWorkingTime() * p.getSuperiorBound())) {
							// REJECT proposal - can't go beyond limit of time.
							System.out.println("Rejected by time " + cmd[2] + " " + name);
							reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
							this.stage = 4;
							block(300);
						} else if(!goal.equalsIgnoreCase(cmd[0]) && p.checkAmountItemsCategory(cmd[0]) > 10) {
							// Reject if we have 10 or more tasks of this category that is not our goal.
							// Not allow more than 35% of the Working-time. - have to be implemented.
							System.out.println("Rejected by not desired category " + cmd[2] + " " + name);
							reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
							this.stage = 2;
							block(300);
						} else {
							System.out.println("Accepted proposal " + cmd[2] + " " + name);
							reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							reply.setContent("GET_ACTIVITY:" + id + ":");
							this.stage = 3;
						}
													 									
						p.send(reply);
						
						break;
					}
				}
			case 4:				
				/*
				 *  If decide to exchange with other agents, that is the right time.
				 *  If not, just wait to make a new request or finishes if working time is ok.
				 *  May implement a token pass in order to organise orders to backlog.
				 *  Thus, getting from backlog would be ordered and exchange among agents asynchronous.
				 */
				 int worked = p.getWorkedTime();
				 float working = p.getWorkingTime() * 0.5F;
				
				 if (worked < working) {
				    this.stage = 2;
				 }
				 else {
					System.out.println("CASE 4B: " + name +  " - " + worked + " - " + working);
					
					if (p.hasActvitiestoExchange()) {
						System.out.println("CASE 4C: " + name +  " - " + p.getWorkedTime());
						p.listActivities();
						
						ExchangeActivity(p, -2, null);
					}
					
					if(p.getWorkedTime() > (p.getWorkingTime()*p.getSuperiorBound()) && p.hasActvitiestoExchange())
						this.stage = 4;
					else
						this.stage = 5;
				}					
				
				break;
			case 5:
				p.listActivities();
				this.stage = 6;
				break;
			
			case 6:
				onEnd();
			}			
		}		
	
		//
		// Routine to manage all the exchange process between workers
		//
		public boolean ExchangeActivity(Person p, int performative, ACLMessage received) {
			String[] args = null;
			ACLMessage answer;
			
			if (performative == -2 )
				answer = new ACLMessage(ACLMessage.REQUEST);
			else {
				answer = received.createReply();
				args = received.getContent().split(":");
			}
			
			answer.setProtocol("EXCHANGE_ACTIVITY");

			Inform inform = new Inform("Exchange Activity Function");
			
			if (partners.length > 0) {
				switch(performative) {
				case -2:
					// Handshake
					// Send a request for activities.					
					String message = "WANT:" + p.getGoals().get(0).substring(1) + ":MAXTIME:" + p.getWorkingTime();
					answer.setContent(message);
					
					for(int i = 0; i < partners.length; i++) {
						if (! partners[i].getName().getName().equalsIgnoreCase(p.getAID().getName())) 
							answer.addReceiver(partners[i].getName());
					}

					p.send(answer);
					inform.show(p.getLocalName() + " " + answer.getContent() + " Request sent.");					
					break;
					
				case ACLMessage.REQUEST:
					// Receiving a request.
					// FORMAT:  WANT:category:MAXTIME:1000
					// Return: 	A PROPOSE with a task description (category, duration and id)
					//			REQUEST_WHEN, don't have a task for the specific request (YET).
					//			REQUEST_WHEN, just not ready yet to choose an activity.	
					//			REFUSE, complete our objective, no need to get rid of tasks.
					
					inform.show(p.getLocalName() + " Request received");
					
					if(p.hasActvitiestoExchange(args[1])) {
						// just get the item without removing it. its category and wished max_time.
						Activity item = p.getActivitytoExchange(false, args[1], Integer.parseInt(args[3]));
						String list = p.getActivitytoExchange(args[1]);
						
						inform.show("test exchange: " + list);
												
						if (item != null) {
							answer.setPerformative(ACLMessage.PROPOSE);
							answer.setContent("CATEGORY:" + item.getCategory() + ":DURATION:" + item.getDuration() + ":ID:" + item.getName());
							inform.show(p.getLocalName() + " sent proposal " + item.getName() + "-" +item.getCategory());
						}
						else {
							answer.setPerformative(ACLMessage.REQUEST_WHEN);
							answer.setContent("REASON:NO TASK FOR WISHED CATEGORY");
						}
					} else {
						
						if(p.checkObjectiveAttended()) {
							answer.setPerformative(ACLMessage.REFUSE);
							answer.setContent("REASON:DONE");
						}
						else {
							answer.setPerformative(ACLMessage.REQUEST_WHEN);
							answer.setContent("REASON:NOT READY");
						}
					}
					
					p.send(answer);
					break;
					
				case ACLMessage.PROPOSE:
					// Receive a propose.
					// FORMAT:	CATEGORY:category type:DURATION:time:ID:name of the activity
					// Return:	ACCEPT_PROPOSAL, if the category is really our objective/need.
					//			REFUSE: activity not necessary.
					String[]  propose  = received.getContent().split(":");
					
					if (propose[1].equalsIgnoreCase(p.getGoals().get(0).substring(1))) {
						inform.show(p.getLocalName() + " propose received. Sending acceptance " + propose[5] + " Content recieved" + received.getContent());
						
						if((p.getWorkedTime() + Integer.parseInt(propose[3])) <= (p.getWorkingTime() * p.getSuperiorBound())) {
							answer.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							answer.setContent("ID:" + propose[5]);
						}
						else {
							answer.setPerformative(ACLMessage.REFUSE);
							answer.setContent("REASON:REACHED WORKING TIME BOUND");
						}
					}
					else {
						// We could check if we have interest in completing portfolio of categories.
						// Not implemented for yet.
						answer.setPerformative(ACLMessage.REJECT_PROPOSAL);
						answer.setContent("REASON:UNECESSARY");
					}
					
					p.send(answer);							
					break;
				
				case ACLMessage.ACCEPT_PROPOSAL:		
					// Nice let's send the activity and get rid of it.
					// FORMAT:    ID:NAME
					// RETURN:	  AGREE and activity selected.
					// 			  FAILURE if the activity does not exist.
					String [] accept = received.getContent().split(":");
					Activity item = p.getActivity(accept[1]);
									
					if (item != null) {
						inform.show(p.getLocalName() + " proposal accepted. " +  accept[1] + " Sending activity object. " + item.getName() + "  " + item.getCategory());
						answer.setPerformative(ACLMessage.AGREE);
						
						try {
							answer.setContentObject(item);
						} catch (IOException e) {
							e.printStackTrace();
						}	
					} else {
						answer.setPerformative(ACLMessage.FAILURE);
						answer.setContent("REASON:ID NOT FOUND");
					}
					
					p.send(answer);					
					
					break;
				case ACLMessage.AGREE:
					// Receive the accepted activity. 
					// FORMAT:  The activity object.

					try {
						Activity task = (Activity) received.getContentObject();						
						p.setOneActivity(task);						
						inform.show(p.getLocalName() + " received/stored activity " + task.getCategory() + " " + task.getDuration());
						p.listActivities();						
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
					
					break;					
				case ACLMessage.REQUEST_WHEN:
					// The other agent is not ready. Just give some time to repeat the query.
					// 
					String[] content = received.getContent().split(":");
					if (content[1].equalsIgnoreCase("NOT READY")) {
						block(300);
					}
					break;
					
				case ACLMessage.REFUSE:
					inform.show(p.getLocalName() + " REFUSED " + received.getContent());
					
					int[] totals = p.getTotalbyCategory();
					int i = 1;
					
					for(int num: totals) {
						System.out.println("Cateoory " + i + "  total: " + num);
						i++;
					}
					
					System.out.println(p.getLocalName() + " " + p.getGoals());
										
					break;
				}
				
				
			}	
										
			return true;
		}	
	}
		
}
