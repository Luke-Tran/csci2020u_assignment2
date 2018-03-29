import javafx.scene.Group;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import java.io.*;

public class HttpClient extends Application {

	private BorderPane layout;
	private static File sharedFolderPath;
	private static PrintStream printStream;

	private static Socket socket;

	public static ObservableList<String> serverFiles = FXCollections.observableArrayList();
	public static ListView<String> serverFilesList = new ListView<String>(serverFiles);
	private ObservableList<File> localFiles = FXCollections.observableArrayList();
	private ListView<File> localFilesList = new ListView<File>(localFiles);

	private static String localHostAddress = "127.0.0.1";
	private static int portNumber = 1234; 

	//Create the user interface.
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Client");

		GridPane topArea = new GridPane();

		for (String file : sharedFolderPath.list()) {
		  localFiles.add(new File(sharedFolderPath, file));
		}

		localFilesList.setItems(localFiles);

		Button downloadButton = new Button("Download");
		topArea.add(downloadButton, 0, 0);
		downloadButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				//Signal to the server an attempt to download a file of a given name.
				printStream.println("File download");
				String fileName = serverFilesList.getSelectionModel().getSelectedItem();
				printStream.println(fileName);
				download(fileName);
			}
		});

		Button uploadButton = new Button("Upload");
		topArea.add(uploadButton, 1, 0);
		uploadButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				//Signal an attempt to send a file to the server.
				printStream.println("File send");
				upload(localFilesList.getSelectionModel().getSelectedItem().getPath());
				serverFilesList.setItems(serverFiles);
			}
		});

		GridPane middleArea = new GridPane();
		middleArea.add(localFilesList, 0, 0);
		middleArea.add(serverFilesList, 1, 0);

		layout = new BorderPane();
		layout.setTop(topArea);
		layout.setCenter(middleArea);

		primaryStage.setScene(new Scene(layout, 400, 300));
		primaryStage.show();
	}

	public static void main(String[] args) throws IOException {

		if (args.length <= 1) {
			System.out.println("Usage: java HttpClient <computer name> <shared folder path>");
			System.exit(0);
		}

		sharedFolderPath = new File(args[1]);

		try {
			//Connect to socket.		
			socket = new Socket(localHostAddress, portNumber);
			System.out.println("Connecting...");
			printStream = new PrintStream(socket.getOutputStream());
		} 
		catch (Exception e) {
			System.err.println("Cannot connect to the server, try again later.");
			System.exit(1);
		}

		launch(args);
		socket.close();
	}

  //A function that downloads a given file name from the server.
  //Is called when the download button is pressed while an item is selected.
  public void download(String fileName) {
    try {
		socket = new Socket(localHostAddress, portNumber);
		System.out.println("Connecting...");
		
        int bytesToRead;
        InputStream inStream = socket.getInputStream();
        DataInputStream clientData = new DataInputStream(inStream);

		//Read the server file's information
        fileName = clientData.readUTF();
        OutputStream output = new FileOutputStream((fileName));
        long size = clientData.readLong();
        byte[] buffer = new byte[10000];
        while (size > 0 && (bytesToRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            output.write(buffer, 0, bytesToRead);
            size -= bytesToRead;
        }

        output.close();
        inStream.close();
		socket.close();

        System.out.println("File "+ fileName + " downloaded from Server.");
    } 
	catch (IOException e) {
        e.printStackTrace();
    }
  }

  //A function that uploads a given file name to the server.
  //Is called when the upload button is pressed while an item is selected.
  public void upload(String fileName) {
    try {
		socket = new Socket(localHostAddress, portNumber);
		System.out.println("Connecting...");

		//Get the absolute path of the file to be uploaded
		File localFile = new File(fileName);
		
		//Read the information of the file as input
		byte[] byteArray = new byte[(int) localFile.length()];
		FileInputStream fileIn = new FileInputStream(localFile);
		BufferedInputStream bufferIn = new BufferedInputStream(fileIn);
		DataInputStream dataIn = new DataInputStream(bufferIn);
		dataIn.readFully(byteArray, 0, byteArray.length);

		//Write the information of the file to the server
		OutputStream os = socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeUTF(localFile.getName());
		dos.writeLong(byteArray.length);
		dos.write(byteArray, 0, byteArray.length);
		dos.flush();
		System.out.println("File " + fileName + " sent to Server.");
	  
		//Update the local list of files
		serverFiles.add(fileName);
		serverFilesList.setItems(serverFiles);
		
		socket.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
