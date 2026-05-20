import java.io.*;
import java.net.*;

public class Client 
{
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) 
    {
        try 
        {
            Socket socket = new Socket(HOST, PORT);
            Logger.logClient("Connected to server at " + HOST + ":" + PORT);

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            // Start a background thread to listen for server broadcasts in real-time
            Thread listenerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = input.readLine()) != null) {
                        Logger.logClient(serverMessage);
                    }
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        Logger.logClientError("Error reading from server: " + e.getMessage());
                    }
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.setName("ServerListener");
            listenerThread.start();

            // Main thread handles capturing user terminal input and sending it
            while (true) {
                String message = userInput.readLine();
                if (message == null) {
                    break;
                }

                output.println(message);

                if (message.equalsIgnoreCase("exit")) {
                    Logger.logClient("Disconnecting cleanly...");
                    break;
                }
            }

            // Clean up resources upon exit
            socket.close();
            userInput.close();
            Logger.logClient("Client stopped.");

        } catch (IOException e) {
            Logger.logClientError("Client failed: " + e.getMessage());
        }
    }
}