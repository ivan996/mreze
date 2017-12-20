package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.LinkedList;

import main.MainServer;

public class Clients extends Thread implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String username = "";
	public String pass;
	public LinkedList<String> upload = new LinkedList<>();
	public static OutputStream output;
	public static BufferedReader clientInput = null;
	public static PrintStream clientOutput = null;
	public static Socket socket = null;

	public Clients(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			int vrednost;
			clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = socket.getOutputStream();
			clientOutput = new PrintStream(socket.getOutputStream());
			boolean valid = true;
			clientOutput.println("\\connectionok");
			while (valid) {
				vrednost = Integer.parseInt(clientInput.readLine().toString());
				switch (vrednost) {
				case 0:
					uploadFile();
					break;
				case 1:
					downloadFile();
					break;
				case 2:
					listFile();
					break;
				case 3:
					singin();
					break;
				case 4:
					register();
					break;
				case -1:
					valid = false;
					break;
				default:
					break;
				}
			}
			MainServer.onlineClients.remove(this);
			socket.close();
		} catch (NumberFormatException e) {
			System.out.println("Neispravan unos!");
		} catch (IOException e) {
			System.out.println("Klijent " + username + " je nasilno prekinu program");
			try {
				socket.close();
			} catch (IOException e1) {
				System.err.println("Greska prilikom zatvaranja soketa!");
			}
		}
	}

	private void singin() {
		try {
			String username = clientInput.readLine();
			String pass = clientInput.readLine();
			for (Clients client : MainServer.allClients) {
				if (client.pass.equals(pass) && client.username.equals(username)) {
					clientOutput.println("\\clientok");
					this.username = username;
					izjednaci();
					return;
				}
			}
			clientOutput.println("\\noclient");
		} catch (IOException e) {
			System.out.println("Greska prilikom citanja");
		}
	}

	private void register() {
		try {
			String username = clientInput.readLine();
			String pass = clientInput.readLine();
			for (Clients client : MainServer.allClients) {
				if (client.username.equals(username)) {
					clientOutput.println("\\badusername");
					return;
				}
			}
			clientOutput.println("\\okusername");
			this.username = username;
			this.pass = pass;
			MainServer.allClients.add(this);
			MainServer.updateList();
		} catch (IOException e) {
			System.out.println("Greska prilikom citanja");
		}
	}

	private void listFile() {
		for (String file : upload) {
			clientOutput.println(file.toString());
		}
		clientOutput.println("\\endlist");
	}

	private void uploadFile() {
		String key;

		while (true) {
			key = getRandomKey(10);
			if (!MainServer.allFiles.contains(key))
				break;
		}

		try {
			String txt = clientInput.readLine();
			PrintWriter fileOut = new PrintWriter(new BufferedWriter(new FileWriter("fajlovi/" + key + ".txt")));
			fileOut.println(txt);
			fileOut.close();
			MainServer.allFiles.add(key);
			upload.add(key);
			ubaci();
			clientOutput.println("\\key," + key);
			MainServer.updateList();
		} catch (IOException e) {
			System.out.println("Greska prilikom citanja");
		}
	}

	private void downloadFile() {
		try {
			String key = clientInput.readLine();
			if (!MainServer.allFiles.contains(key)) {
				clientOutput.println("\\nokey");
				return;
			} else {
				clientOutput.println("\\keyok");
			}
			byte[] bufer = new byte[1024];
			File file = new File(key + ".txt");
			if (file.exists()) {
				RandomAccessFile randomAccessFile = new RandomAccessFile("fajlovi/" + key + ".txt", "r");
				int n;
				while (true) {
					n = randomAccessFile.read(bufer);
					if (n == -1) {
						break;
					}
					output.write(bufer, 0, n);
				}
				randomAccessFile.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getRandomKey(int len) {
		String primaryText = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		SecureRandom random = new SecureRandom();
		StringBuilder stringBuilder = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			stringBuilder.append(primaryText.charAt(random.nextInt(primaryText.length())));
		}
		return stringBuilder.toString();
	}

	private void izjednaci() {
		for (Clients client : MainServer.allClients) {
			if (client.username.equals(username)) {
				this.upload = client.upload;
			}
		}
	}

	private void ubaci() {
		for (Clients client : MainServer.allClients) {
			if (client.username.equals(username)) {
				client.upload = this.upload;
			}
		}
	}

}
