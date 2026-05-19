import java.io.*;
import java.net.*;

public class Server
{
    private static final int PORT = 5000;
    private static ServerSocket serverSocket;

    public static void main(String[] args)
    {
        // Register shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[INFO] Server shutting down gracefully...");
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    System.out.println("[INFO] ServerSocket closed.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT);

            SharedClientRegistry registry = SharedClientRegistry.getInstance();

            while (true)
            {
                System.out.println("Waiting for clients to connect...");
                Socket clientSocket = serverSocket.accept();

                System.out.println("[INFO] New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Handle the client connection in a new thread
                ClientHandler clientHandler = new ClientHandler(clientSocket, registry);

                registry.addClient(clientHandler.getClientId(), clientHandler);

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