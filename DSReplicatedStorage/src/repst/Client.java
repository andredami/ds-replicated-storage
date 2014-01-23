/**
 * 
 */
package repst;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * This is the REPL Client to access the replicated storage via command line.<br />
 * The command line to start it is {@code client serverIpAddress [-p:port]}.<br />
 * The commands the Client accepts are:<br />
 * - Retrieve a value: {@code GET key}<br />
 * - Store a value: {@code PUT key value}<br />
 * - Quit: {@code EXIT}
 * 
 * @author Andrea
 * 
 */
public class Client {
	private static final String NON_NEWLINE_WHITESPACES = "[ \\t\\x0B\\f]+";
	private static final Integer DEFAULT_RMI_PORT = 1099;
	private static final String SERVER_REGISTERED_NAME = "/Server";

	private static ServerRemoteInterface server;

	private static enum Action {
		GET, PUT, EXIT, NONE
	}

	/**
	 * Client main program
	 * 
	 * @param args
	 *            Command line arguments: serverIpAddress [-p:port]
	 */
	public static void main(String[] args) {

		String serverIpAddress;

		boolean isPortSet = false;
		int port = DEFAULT_RMI_PORT;

		// Command line arguments parsing
		serverIpAddress = args[0];

		for (int i = 1; i < args.length; i++) {
			if (args[i].startsWith("-p:")) {
				if (!isPortSet) {
					String[] portCommand = args[i].split(":");
					if (portCommand.length == 2) {
						try {
							port = Integer.parseInt(portCommand[1]);
							isPortSet = true;
						} catch (NumberFormatException e) {
							System.err
									.println("WARNING: wrong format for argument -p:port (you supplied "
											+ args[i] + ")");
							System.err.println("Default server port "
									+ DEFAULT_RMI_PORT + " will be used.");
						}
					}
				}
			}
		}

		if (!isPortSet) {
			System.err.println("No server port supplied");
			System.err.println("Default server port " + DEFAULT_RMI_PORT
					+ " will be used.");
		}

		// Connection to server
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		String serverUri = "//" + serverIpAddress + ":"
				+ Integer.toString(port);
		try {
			server = (ServerRemoteInterface) Naming.lookup(serverUri + "/"
					+ SERVER_REGISTERED_NAME);
		} catch (MalformedURLException e) {
			System.err
					.println("ERROR: the Server IP Address you supplied is not a valid IP Addres or Authority");
			return;
		} catch (RemoteException e) {
			System.err.println("ERROR: Server [" + serverUri + "] unreachable");
			return;
		} catch (NotBoundException e) {
			System.err.println("ERROR: Server [" + serverUri
					+ "] not available");
			return;
		}

		// Show Application Usage
		showUsage(false);

		// Application Loop
		Action action;
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(
				System.in));
		do {
			// Input acquisition
			String[] input = null;
			try {
				do {
					input = inputStream.readLine().split(
							NON_NEWLINE_WHITESPACES);
				} while (input != null && input.length > 0);

				// Input Action Validation
				action = Action.valueOf(input[0].toUpperCase());
				switch (input.length) {
				case 1:
					if (action != Action.EXIT) {
						action = Action.NONE;
					}
					break;
				case 2:
					if (action != Action.GET) {
						action = Action.NONE;
					}
				case 3:
					if (action != Action.PUT) {
						action = Action.NONE;
					}
					break;
				default:
					action = Action.NONE;
					break;
				}
			} catch (IOException e) {
				System.err.println("ERROR: No input available.");
				action = Action.NONE;
			} catch (IllegalArgumentException e) {
				action = Action.NONE;
			}

			// Input Parameter Validation and Action Execution
			switch (action) {
			case GET:
				try {
					actionGet(Integer.parseInt(input[1]));
				} catch (NumberFormatException e) {
					showUsage(true);
				}
				break;
			case PUT:
				try {
					actionPut(Integer.parseInt(input[1]),
							Integer.parseInt(input[2]));
				} catch (NumberFormatException e) {
					showUsage(true);
				}
				break;
			default:
				showUsage(true);
				break;
			}
		} while (action == Action.EXIT);

		System.out.println("Server is closing...");
		server = null;
		return;
	}

	private static void actionPut(int key, int value) {
		System.out.println("Storing value " + Integer.toString(value)
				+ " at key " + Integer.toString(key) + "...");
		try {
			server.writeValue(key, value);
			System.out.println("The value at key " + Integer.toString(key)
					+ " is now " + Integer.toString(value));
		} catch (RemoteException e) {
			System.err.println("ERROR: Unable to accomplish the request");
		}
	}

	private static void actionGet(int key) {
		System.out.println("Retrieving value at key " + Integer.toString(key)
				+ "...");
		try {
			Integer value = server.readValue(key);
			if (value != null) {
				System.out.println("The value at key " + Integer.toString(key)
						+ " is " + Integer.toString(value));
			} else {
				System.out.println("No value stored at key "
						+ Integer.toString(key));
			}
		} catch (RemoteException e) {
			System.err.println("ERROR: Unable to accomplish the request");
		}
	}

	private static void showUsage(boolean onError) {
		if (onError) {
			System.err
					.println("WARNING: The command you have entered is incorrect");
		}
		System.out.println("AVAILABLE COMMANDS");
		System.out.println("------------------------------------");
		System.out.println("Retrieve a value: GET key");
		System.out.println("Store a value: PUT key value");
		System.out.println("Quit: EXIT");
	}
}