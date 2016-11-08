package tests;

import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

class BacklogMngBehaviour extends ParallelBehaviour {

	/**
	 *   BacklogMngBehaviour has three main activities
	 *   1- Give objectives to Agents to consume the backlog
	 *   2- Give activities to be consumed by Agents
	 *   3- Negotiate activities, exchanging them whenever it is convenient.
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("serial")
	public BacklogMngBehaviour(Agent agent, BacklogManager backlog) {
		super(agent,  ParallelBehaviour.WHEN_ALL);
		
		//
		// This behaviour will deal with establishing goals to Agents
		// It receives a request to get an objective and send an objective to the agent.
		//
		this.addSubBehaviour(new CyclicBehaviour(agent) {
			
			public void action() {
				// System.out.println("Cyclic behaviour of Backlog Manager");
				MessageTemplate mt = MessageTemplate.MatchProtocol("OBJECTIVE");
				
				ACLMessage msg = agent.receive(mt);
				
				if (msg != null) {
					String content = msg.getContent();
					ACLMessage reply = msg.createReply();
					reply.setProtocol("OBJECTIVE");
					
					if (content.equals("GET OBJECTIVE"))
						reply.setContent(backlog.get_objective());
					else
						reply.setPerformative(ACLMessage.REFUSE);
					
					myAgent.send(reply);					
				}
			}
		});
		

		this.addSubBehaviour(new CyclicBehaviour(agent) {
			private static final long serialVersionUID = 1L;

			public void action() {
				MessageTemplate mt = MessageTemplate.MatchProtocol("ACTIVITY");
				ACLMessage msg = agent.receive(mt);
				
				if(msg != null) {
					String content = msg.getContent();
					if (content.equals("SHOW_ACTIVITIES")) {				
						List<Activity> listActivities = backlog.getActivities();
				
						System.out.println(myAgent.getLocalName() + "BL List of Activities: ");
						
						for (Activity activity : listActivities) {
							System.out.print("Name: " + activity.getName());
							System.out.print(" - " + activity.getCategory());
							System.out.println(" - Duration: " + activity.getDuration() + " min");
						}	
					}
					else if (content.equals("GET_ACTIVITY")) {
						System.out.println("Received a request for new activity");
					}					
				}
			}
		});
	}	
}