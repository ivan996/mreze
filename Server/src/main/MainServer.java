package main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import client.Clients;

public class MainServer {
	public static LinkedList<Clients> allClients = new LinkedList<>();
	public static LinkedList<String> allFiles = new LinkedList<>();
	public static LinkedList<Clients> onlineClients = new LinkedList<>();
	public static ObjectOutputStream upisKey = null;
	public static ObjectOutputStream upisiClient = null;
	public static ObjectInputStream inKey = null;
	public static ObjectInputStream inClient = null;

	public static void main(String[] args) {
		
		loadList();

		ServerSocket serverSocet = null;
		Socket socket = null;
		try {
			serverSocet = new ServerSocket(9001);
			while (true) {
				System.out.println("Cekanje na konekciju...");
				socket = serverSocet.accept();
				System.out.println("Konekcija uspostavljena!");

				Clients newClient = new Clients(socket);
				newClient.start();
				onlineClients.add(newClient);

			}
		} catch (IOException e) {
			System.err.println("Doslo je do greske prilikom konekcije!");
		}

	}

	public static void updateList() {
		try {
			upisKey = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("keys.out")));
			for (String key : MainServer.allFiles) {
				upisKey.flush();
				upisKey.writeObject(key);
			}
			upisiClient = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("client.out")));
			for (Clients client : MainServer.allClients) {
				upisiClient.flush();
				upisiClient.writeObject(client);
			}
			upisKey.close();
			upisiClient.close();
		} catch (FileNotFoundException e) {
			System.err.println("File ne postoji!!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void loadList() {
		try {
			inKey = new ObjectInputStream(new BufferedInputStream(new FileInputStream("keys.out")));
			try {
				while (true) {
					String k = (String) (inKey.readObject());
					MainServer.allFiles.add(k);
				}
			} catch (EOFException e) {
				inKey.close();
			} catch (ClassNotFoundException e) {
				System.out.println("Nije pronadjena klasa!");
			}

			inClient = new ObjectInputStream(new BufferedInputStream(new FileInputStream("client.out")));
			try {
				while (true) {
					Clients c = (Clients) (inClient.readObject());
					MainServer.allClients.add(c);
				}
			} catch (EOFException e) {
				inClient.close();
			} catch (ClassNotFoundException e) {
				System.out.println("Nije pronadjena klasa!");
			}
		} catch (FileNotFoundException e) {
			System.err.println("File ne postoji!!");
		} catch (IOException e) {
			System.out.println("Greska prilikom citanja iz file!");
		}
	}
}
