import java.io.*;
import java.net.*;

public class Server
{
    private static final int PORT = 5000;

    public static void main(String[] args)
    {
        try (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            System.out.println("Server is listening on port " + PORT);

            while (true)
            {
                System.out.println("Waiting for clients to connect...");
                Socket clientSocket = serverSocket.accept();

                System.out.println("[INFO] New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Handle the client connection in a new thread
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                SharedClientRegistry.addClient(clientHandler.getClientId(), clientHandler);

                SharedClientRegistry.showConnectedClients();

                Thread clientThread = new Thread(clientHandler);
                clientThread.setName(
                    "ClientHandler-" + clientSocket.getPort()
                );
                clientThread.start();
            }

        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}