package demo.DutchAuctionDemo;

import java.util.Vector;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import examples.protocols.DutchAuctionInitiatorAgent;
import examples.protocols.DutchAuctionResponderAgent;

public class DutchAuctionDemo
{
	public static void main(String[] args) throws InterruptedException
	{
		Runtime jade = Runtime.instance();
		Properties mainProps = new ExtendedProperties();
		mainProps.setProperty(Profile.MAIN, "true");
		mainProps.setProperty(Profile.GUI, "true");
		mainProps.setProperty(Profile.MAIN_HOST, "127.0.0.1");
		
		jade.createMainContainer(new ProfileImpl(mainProps));
		ContainerController container = jade.createAgentContainer(new ProfileImpl());
		try
		{
			String[] bidderNames = new String[] {"Albert", "Benny", "Celia", "Dan", "Eustace", "Fenny"};
			Vector<AgentController> ctrls = new Vector<AgentController>();			
			for(String bidderName : bidderNames)
				ctrls.add(container.createNewAgent(bidderName, DutchAuctionResponderAgent.class.getCanonicalName(), new Object[] {}));
			
			AgentController auct = container.createNewAgent("Auctioneer", DutchAuctionInitiatorAgent.class.getCanonicalName(), bidderNames);
			Thread.sleep(10000);
			auct.start();
			for(AgentController ctrl : ctrls)
				ctrl.start();

		} catch(StaleProxyException e)
		{
			e.printStackTrace();
		}
	}
}
