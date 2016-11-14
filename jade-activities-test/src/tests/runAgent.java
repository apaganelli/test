package tests;

import jade.Boot;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * 
 * 
 * 
 * 
 * 
 * @author Francisco Cunha
 * 
 */

public class runAgent {

	/**
	 * 
	 * @param agent
	 * 
	 * @param nameAgent
	 * 
	 * @param nameContainer
	 * 
	 */

	public runAgent(Agent agent, String nameAgent, String nameContainer) {

		setAgentInContainer(agent, nameAgent, nameContainer);

	}

	/**
	 * 
	 * @param agent
	 * 
	 * @param nameAgent
	 * 
	 * @param nameContainer
	 * 
	 */

	private void setAgentInContainer(Agent agent, String nameAgent,

			String nameContainer) {

		Runtime runtime = Runtime.instance();

		Profile profile = new ProfileImpl();

		profile.setParameter(Profile.CONTAINER_NAME, nameContainer);

		AgentContainer controllerAgentContainer = runtime

				.createAgentContainer(profile);

		try {

			AgentController controller = controllerAgentContainer

					.acceptNewAgent(nameAgent, agent);

			
			controller.start();

			controller = controllerAgentContainer

					.acceptNewAgent("João", new Person(200, 1.1));

			controller.start();
			
			controller = controllerAgentContainer

					.acceptNewAgent("Maria", new Person(200, 1.1));

			controller.start();

			controller = controllerAgentContainer

					.acceptNewAgent("Joana", new Person(200, 1.1));

			controller.start();

			
			controller = controllerAgentContainer

					.acceptNewAgent("Backlog-mng", new BacklogManager());

			controller.start();
			
		} catch (StaleProxyException ex) {

			System.out.println("Agente não pode ser iniciado");

		}
	}

	/**
	
	* 
	
	*/

	public static void main(String args[]) {

		Boot.main(new String[] { "-gui" });

		new runAgent(new Person(200, 1.1), "José", "meuContainer");
		// new runAgent(new BacklogManager(), "Backlog-Mng", "meuContainer");

	}

}