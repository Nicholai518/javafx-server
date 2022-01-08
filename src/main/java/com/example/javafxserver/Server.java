package com.example.javafxserver;

import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	// fields
	private ServerSocket serverSocket;    // used to listen for incoming connections
	private Socket socket;                    // socket used to communicate with client, once connection is established
	private BufferedReader bufferedReader;  // used to read data from client / used for efficiency
	private BufferedWriter bufferedWriter;
	// Note: The way a socket allows communication is through streams
	// a socket has an output stream that we can write content to
	// and an inputstream that we can read information from

	// constructors
	public Server(ServerSocket serverSocket) {

		try {
			this.serverSocket = serverSocket;
			// accept() is a blocking method. The program will halt here until a client connects to us
			// returns the socket object we can use to communicate with the client
			this.socket = serverSocket.accept();

			// BufferedReader is for efficiency
			// ends with reader or writer is a character stream
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			System.out.println("Error creating server.");
			e.printStackTrace();
			// close everything for security purposes
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}

	// methods
	// sends message to client
	public void sendMessageToClient(String messageToClient) {

		try {
			bufferedWriter.write(messageToClient);        // Writes message to buffer
			bufferedWriter.newLine();                    // terminating character
			bufferedWriter.flush();                        // flush buffer manually to push message to client
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error sending message to the client");
			// close for security purposes
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}

	// This message needs to be run on a separate thread
	public void receiveMessageFromClient(VBox vBox) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (socket.isConnected()) {
					try {
						String messageFromClient = bufferedReader.readLine();
						Controller.addLabel(messageFromClient, vBox);
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("Error receiving message from the client");
						closeEverything(socket, bufferedReader, bufferedWriter);

						// if there is an error we want to break out of the while loop
						break;
					}
				}
			}
		}).start();
	}

	// used to close objects for security purposes
	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {

		try {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
