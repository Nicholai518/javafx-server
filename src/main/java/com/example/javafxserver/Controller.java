package com.example.javafxserver;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;

// Implementing Initializable allows us to override initilize method
public class Controller implements Initializable {

	// inject FXML widgets
	@FXML
	private Button button_send;
	@FXML
	private TextField tf_message;
	@FXML
	private VBox vbox_messages;
	@FXML
	private ScrollPane sp_main;

	// Server
	private Server server;

	// We will be allowed to work with our FX and our widgets that we made in our server.fxml file
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		try {
			// try to create a new server object
			// passing a ServerSocket as an argument that uses port 1234
			// ServerSockets listen to incoming connections
			server = new Server(new ServerSocket(1234));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error creating server.");
		}


		// add a listener to the hieght property of our Vbox container
		vbox_messages.heightProperty().addListener(new ChangeListener<Number>() {

			// This allows our scrollpane to automatically scroll to the bottom when new messages arrive
			// without this, the user would need to manually scroll down to see new messages
			@Override
			public void changed(ObservableValue<? extends Number> ovservable, Number oldValue, Number newValue) {

				// when the height of the vBox changes we want to set the vertical value of our scroll pane to be
				// the new value
				sp_main.setVvalue((Double) newValue);
			}
		});

		// Use a server message to listen for messages from the client
		// This method will be run on a separate thread
		// This is because waiting for messages is a "blocking operation"
		// In other words, our server is constantly waiting for messages from our client
		// but we want to be able to perform other actions such as "Send messages" while waiting for new messages
		server.receiveMessageFromClient(vbox_messages);

		// adding functionality to our button when the button is "clicked"
		// this will send a message to our client
		button_send.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				// holds the message characters to be sent to client
				String messageToSend = tf_message.getText();

				// input validation
				// if the message is NOT empty, send it to client
				// otherwise do not do anything
				if (!messageToSend.isEmpty()) {
					// create an hbox with text in it
					HBox hBox = new HBox();
					// add it to our GUI on the right side
					// this is visually similar to how our phones display texts
					hBox.setAlignment(Pos.CENTER_RIGHT);
					// adding padding so there is space from the edges of our hbox
					hBox.setPadding(new Insets(5, 5, 5, 10));

					// create text object to display text
					Text text = new Text(messageToSend);
					// want to wrap inside Textflow
					// this is a layout design for rich text, allow us to style text
					// this also provides wrapping functionality for long messages
					TextFlow textFlow = new TextFlow();
					// Passing in CSS like styling to change text color
					// -fx-background-radius: 20px  provides curved shape to message


					// This is the original, includes an error
					//					textFlow.setStyle("-fx-color: rgb(239,242,255 " +
					//							          "-fx-background-color: rgb(15,25,242)" +
					//					                  " -fx-background-radius: 20px");

					// this is the original, help from intellij
					textFlow.setStyle("-fx-color: rgb(239,242,255); " +
							"-fx-background-color: rgb(15,25,242);" +
							" -fx-background-radius: 20px;");

					//					textFlow.setStyle("-fx-color: rgb(239,242,255)");
					//					textFlow.setStyle("-fx-background-color: rgb(15,25,242)");
					//					textFlow.setStyle("-fx-background-radius: 20px");

					// set padding
					textFlow.setPadding(new Insets(5, 10, 5, 10));

					// Text color
					text.setFill(Color.color(0.934, 0.945, 0.996));

					// add to our horizontal box
					hBox.getChildren().add(textFlow);

					// Vertical box messages
					vbox_messages.getChildren().add(hBox);

					// send to client, so they can add to their GUI
					server.sendMessageToClient(messageToSend);

					// finally, we want to clear our text field
					// allowing a new message to be entered
					tf_message.clear();
				}
			}
		});
	}

	// when we receive a message
	// This message will append an hbox to our GUI
	public static void addLabel(String messageFromTheClient, VBox vbox) {
		HBox hBox = new HBox();
		// position is center left, this is visually similar to
		// how incoming text messages look on our phones
		hBox.setAlignment(Pos.CENTER_LEFT);
		hBox.setPadding(new Insets(5, 5, 5, 10));

		Text text = new Text(messageFromTheClient);

		TextFlow textFlow = new TextFlow();

		textFlow.setStyle("-fx-background-color: rgb(233,233,235);" +
				"-fx-background-radius: 20px;");

		//		textFlow.setStyle("-fx-background-color: rgb(233,233,235)");
		//		textFlow.setStyle("-fx-background-radius: 20px");

		textFlow.setPadding(new Insets(5, 10, 5, 10));
		hBox.getChildren().add(textFlow);

		// In JavaFX we cannot update the GUI from a thread other than the application thread
		// in other words, we cannot be using another thread to add our hbox
		// to our scroll pane or to our vertical box
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				vbox.getChildren().add(hBox);
			}
		});
	}
}
