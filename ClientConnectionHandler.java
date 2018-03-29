import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Date;

public class ClientConnectionHandler implements Runnable {
	private Socket socket = null;
	private BufferedReader requestInput = null;
	private DataOutputStream responseOutput = null;

	//Initialize the handler
	public ClientConnectionHandler(Socket socket) throws IOException {
		this.socket = socket;
		requestInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		responseOutput = new DataOutputStream(socket.getOutputStream());
	}

	public void run() {
		try {
			String clientInput;
			//When there is a signal that a file has been sent, the handler calls a function to receive it.
			//When there is a signal for a file to be downloaded, the handler calls a function to send it.
			while ((clientInput = requestInput.readLine()) != null) {
				if (clientInput.equals("File send")) {
					receiveFile();
					break;
				} 
				else if (clientInput.equals("File download")) {
					String fileToClient;
					while ((fileToClient = requestInput.readLine()) != null) {
						sendFile(fileToClient);
					}
					break;
				}         
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
  
	public void receiveFile() {
		try {
			int bytesToRead;

			DataInputStream clientData = new DataInputStream(socket.getInputStream());

			//Read the incoming file's information
			String fileName = clientData.readUTF();
			OutputStream output = new FileOutputStream(("received_from_client_" + fileName));
			long size = clientData.readLong();
			byte[] buffer = new byte[1024];
			while (size > 0 && (bytesToRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
				output.write(buffer, 0, bytesToRead);
				size -= bytesToRead;
			}

			output.close();
			clientData.close();

			System.out.println("File " + fileName + " received from client.");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFile(String fileName) {
		try {
			File serverFile = new File(fileName);
			byte [] byteArray  = new byte [(int)serverFile.length()];
			
			FileInputStream fileIn = new FileInputStream(serverFile);
            BufferedInputStream bufferedIn = new BufferedInputStream(fileIn);
			
			DataInputStream dataIn = new DataInputStream(bufferedIn);
            dataIn.readFully(byteArray, 0, byteArray.length);
			
            OutputStream os = socket.getOutputStream();

            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(serverFile.getName());
            dos.writeLong(byteArray.length);
            dos.write(byteArray, 0, byteArray.length);
            dos.flush();
            System.out.println("File " + fileName + " sent to client.");
        } 
		catch (Exception e) {
			e.printStackTrace();
        }
	}
}
