package demo.DutchAuctionDemo;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import examples.protocols.DutchAuctionInitiatorAgent;
import examples.protocols.DutchAuctionResponderAgent;

public class DutchAuctionDemo
{
	public static void main(String[] args)
	{
		Runtime jade = Runtime.instance();
		Properties mainProps = new ExtendedProperties();
		mainProps.setProperty(Profile.MAIN, "true");
		mainProps.setProperty(Profile.GUI, "true");
		
		ContainerController mainContainer = jade.createMainContainer(new ProfileImpl(mainProps));
		try
		{
			String[] bidderNames = new String[] {"Albert", "Benny", "Celia", "Dan", "Eustace", "Fenny"};
			mainContainer.createNewAgent("Auctioneer", DutchAuctionInitiatorAgent.class.getCanonicalName(), bidderNames);
			
			for(String bidderName : bidderNames)
				mainContainer.createNewAgent(bidderName, DutchAuctionResponderAgent.class.getCanonicalName(), new Object[] {});
		} catch(StaleProxyException e)
		{
			e.printStackTrace();
		}
	}
}
