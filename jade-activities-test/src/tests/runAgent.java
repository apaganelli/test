package tests;

import jade.Boot;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * This class facilitates the bootstrap processs of Jade Environment. 
 * @author Francisco Cunha
 */

public class runAgent {

	/**
	 * @param agent
	 * 
	 * @param nameAgent
	 * 
	 * @param nameContainer
	 */

	public runAgent(int quant, String nameContainer) {

		setAgentInContainer(quant, nameContainer);
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

	private void setAgentInContainer(int quant, String nameContainer) {
		String[] names = { "José", "João", "Maria", "Joana", "Marcelo", "Adriana", "Marcos", "Luana", "Carlos",
				"Guerno", "Kenzo", "Gabriel"};
		
		if (quant > 12)
		   quant = 12;

		Runtime runtime = Runtime.instance();

		Profile profile = new ProfileImpl();

		profile.setParameter(Profile.CONTAINER_NAME, nameContainer);

		AgentContainer controllerAgentContainer = runtime

				.createAgentContainer(profile);

		try {
			AgentController controller = controllerAgentContainer.acceptNewAgent("Backlog-mng", new BacklogManager());
			controller.start();
			
			for (int i=0; i < quant; i++) {
				controller = controllerAgentContainer.acceptNewAgent(names[i], new Worker(200, 1.1));
				controller.start();						
			}		
			
		} catch (StaleProxyException ex) {
			System.out.println("Agente não pode ser iniciado");
		}
	}

	/**
	
	*/

	public static void main(String args[]) {

		Boot.main(new String[] { "-gui" });
		
		// Pass the amount of workers and name of the container. Max of twelve.
		new runAgent(6, "meuContainer");
	}

}