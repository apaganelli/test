package tests;

import java.io.IOException;
import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
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
					System.out.println("\nBacklog receiving request: " + content + " - " + msg.getSender().getLocalName());				
					
					// Just to take a look at what kind of activities are stored into backlog.
					if (content.equals("SHOW_ACTIVITIES")) {				
						List<Activity> listActivities = backlog.getActivities();
				
						System.out.println(myAgent.getLocalName() + "BL List of first 10 Activities:");
						
						int i = 0;
						for (Activity activity : listActivities) {
							System.out.print("Name: " + activity.getName());
							System.out.print(" - " + activity.getCategory());
							System.out.println(" - Duration: " + activity.getDuration() + " min");
							i++;
							if(i > 9)
								break;
						}	
					}
					else if (content.startsWith("GET_ACTIVITY")) {
						// The format of the command line (content of the message) is:
						// cmds[0] = GET_ACTIVITY, cmds[1] = objective, cmds[2] = available time.
						String[] cmds = content.split(":");
						ACLMessage reply = msg.createReply();
						reply.setProtocol("ACTIVITY");
						
						switch(msg.getPerformative()) {
						case ACLMessage.REQUEST:
							Activity item = backlog.getOneActivity(cmds[1]);
							if( item == null) {
								@SuppressWarnings("unused")
								Notify inform = new Notify("Backlog could not get an activity, probably it is empty", "ALERT");
								reply.setPerformative(ACLMessage.REFUSE);							
								reply.setContent("\nActivities not available. Empty backlog. Wait a while or finishes.\n");
								myAgent.send(reply);
							}
							else {
								System.out.println("Backlog got item " + item.getName());	
								reply.setPerformative(ACLMessage.PROPOSE);
								reply.setContent(item.getCategory() + ":" + item.getDuration() + ":" + item.getName() + ":");
							}
							
							myAgent.send(reply);
							break;
						case ACLMessage.ACCEPT_PROPOSAL:
							Activity task = backlog.getActivitybyName(cmds[1]);

							if( task == null) {
								@SuppressWarnings("unused")
								Notify inform = new Notify("Backlog could not get an activity, probably it is empty", "ALERT");
								reply.setPerformative(ACLMessage.REFUSE);							
								reply.setContent("\nActivities not available. Empty backlog. Wait a while or finishes.\n");
								myAgent.send(reply);
							} else {
								System.out.println("Backlog got item " + task.getName());	
								reply.setPerformative(ACLMessage.AGREE);

								try {
									reply.setContentObject(task);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							
							myAgent.send(reply);
							break;
						}	
					}					
				}
			}
		});
	}	
}