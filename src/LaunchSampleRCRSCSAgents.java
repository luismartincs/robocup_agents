import java.awt.*;
import java.io.IOException;

import implementation.agents.ambulance.CFAmbulance;
import implementation.agents.civilian.CFCivilian;
import implementation.agents.firebrigade.CFFireBrigade;
import implementation.agents.policecentre.CFPoliceOffice;
import implementation.agents.policeforce.CFPoliceForce;
import rescuecore2.components.ComponentLauncher;
import rescuecore2.components.TCPComponentLauncher;
import rescuecore2.components.ComponentConnectionException;
import rescuecore2.connection.ConnectionException;
import rescuecore2.registry.Registry;
import rescuecore2.misc.CommandLineOptions;
import rescuecore2.config.Config;
import rescuecore2.config.ConfigException;
import rescuecore2.Constants;
import rescuecore2.log.Logger;
import rescuecore2.standard.entities.StandardEntityFactory;
import rescuecore2.standard.entities.StandardPropertyFactory;
import rescuecore2.standard.messages.StandardMessageFactory;

/**
 * Launcher for sample agents. This will launch as many instances of each of the
 * sample agents as possible, all using one connection.
 */
public final class LaunchSampleRCRSCSAgents{
	
	private static final String	FIRE_BRIGADE_FLAG		= "-fb";
	
	private static final String	POLICE_FORCE_FLAG		= "-pf";
	
	private static final String	AMBULANCE_TEAM_FLAG	= "-at";
	
	
	private LaunchSampleRCRSCSAgents(){
	}
	
	
	/**
	 * Launch 'em!
	 * 
	 * @param args
	 *          The following arguments are understood: -p <port>, -h <hostname>,
	 *          -fb <fire brigades>, -pf <police forces>, -at <ambulance teams>
	 */

	//ALT + SHIFT + F10 -> Edit configurations -> Program arguments

	public static void main(String[] args){
		Logger.setLogContext("sample");
		try{
			Registry.SYSTEM_REGISTRY
					.registerEntityFactory(StandardEntityFactory.INSTANCE);
			Registry.SYSTEM_REGISTRY
					.registerMessageFactory(StandardMessageFactory.INSTANCE);
			Registry.SYSTEM_REGISTRY
					.registerPropertyFactory(StandardPropertyFactory.INSTANCE);
			Config config = new Config();
			args = CommandLineOptions.processArgs(args, config);
			int port = config.getIntValue(Constants.KERNEL_PORT_NUMBER_KEY,
					Constants.DEFAULT_KERNEL_PORT_NUMBER);
			String host = config.getValue(Constants.KERNEL_HOST_NAME_KEY,
					Constants.DEFAULT_KERNEL_HOST_NAME);
			int fb = -1;
			int pf = -1;
			int at = -1;
			// CHECKSTYLE:OFF:ModifiedControlVariable
			for(int i = 0; i < args.length; ++i){
				if(args[i].equals(FIRE_BRIGADE_FLAG)){
					fb = Integer.parseInt(args[++i]);
				}else if(args[i].equals(POLICE_FORCE_FLAG)){
					pf = Integer.parseInt(args[++i]);
				}else if(args[i].equals(AMBULANCE_TEAM_FLAG)){
					at = Integer.parseInt(args[++i]);
				}else{
					Logger.warn("Unrecognised option: " + args[i]);
				}
			}
			// CHECKSTYLE:ON:ModifiedControlVariable
			ComponentLauncher launcher = new TCPComponentLauncher(host, port, config);
			connect(launcher, fb, pf, at, config);
		}catch(IOException e){
			Logger.error("Error connecting agents", e);
		}catch(ConfigException e){
			Logger.error("Configuration error", e);
		}catch(ConnectionException e){
			Logger.error("Error connecting agents", e);
		}catch(InterruptedException e){
			Logger.error("Error connecting agents", e);
		}
	}
	
	
	private static void connect(ComponentLauncher launcher, int fb, int pf,
			int at, Config config) throws InterruptedException, ConnectionException{

		/*int i = 0;
		try{
			while(fb-- != 0){
				Logger.info("Connecting fire brigade " + (i++) + "...");
				launcher.connect(new SampleRCRSCSFireBrigade());
				Logger.info("success");
			}
		}catch(ComponentConnectionException e){
			Logger.info("failed: " + e.getMessage());
		}
		try{
			while(pf-- != 0){
				Logger.info("Connecting police force " + (i++) + "...");
				System.out.println("conectado pf");
				launcher.connect(new SampleRCRSCSPoliceForce());
				Logger.info("success");
			}
		}catch(ComponentConnectionException e){
			Logger.info("failed: " + e.getMessage());
		}
		try{
			while(at-- != 0){
				Logger.info("Connecting ambulance team " + (i++) + "...");
				launcher.connect(new SampleRCRSCSAmbulanceTeam());
				Logger.info("success");
			}
		}catch(ComponentConnectionException e){
			Logger.info("failed: " + e.getMessage());
		}*/

		int po = 1;

		try{
			while(po-- != 0){
				Logger.info("Connecting police office ...");
				launcher.connect(new CFPoliceOffice());
				Logger.info("success");
			}
		}catch(ComponentConnectionException e){
			Logger.info("failed: " + e.getMessage());
		}

		int pc = 200;

		try{
			while(pc-- != 0){
				Logger.info("Connecting police...");
				launcher.connect(new CFPoliceForce());
				Logger.info("success");
			}
		}catch(ComponentConnectionException e){
			Logger.info("failed: " + e.getMessage());
		}


		int civ = 200;

		try{
			while(civ-- != 0){
				Logger.info("Connecting dummy agent...");
				System.out.println("conectado civil"+civ);
				launcher.connect(new CFCivilian());
				Logger.info("success");
			}
		}catch(ComponentConnectionException e){
			System.out.println(e.getMessage());
			Logger.info("failed: " + e.getMessage());
		}

		int amb = 200;

		try{
			while(amb-- != 0){
				Logger.info("Connecting dummy agent...");
				System.out.println("conectado ambulancia"+amb);
				launcher.connect(new CFAmbulance());
				Logger.info("success");
			}
		}catch(ComponentConnectionException e){
			System.out.println(e.getMessage());
			Logger.info("failed: " + e.getMessage());
		}

		int fireBgds = 200;

		try{
			while(fireBgds-- != 0){
				Logger.info("Connecting fireBgds agent...");
				System.out.println("conectado Bombero "+fireBgds);
				launcher.connect(new CFFireBrigade());
				Logger.info("success");
			}
		}catch(ComponentConnectionException e){
			System.out.println(e.getMessage());
			Logger.info("failed: " + e.getMessage());
		}
	}
}