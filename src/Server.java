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
            Server.shutdown();
        }));

        try {
            serverSocket = new ServerSocket(PORT);
            Logger.logServer("Server is listening on port " + PORT);

            SharedClientRegistry registry = SharedClientRegistry.getInstance();

            while (true)
            {
                Logger.logServer("Waiting for clients to connect...");
                Socket clientSocket = serverSocket.accept();

                Logger.logServer("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Handle the client connection in a new thread
                ClientHandler clientHandler = new ClientHandler(clientSocket, registry);

                registry.addClient(clientHandler.getClientId(), clientHandler);

                Thread clientThread = new Thread(clientHandler);
                clientThread.setName(
                    "ClientHandler-" + clientSocket.getPort()
                );
                clientThread.start();
            }

        } catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed()) {
                Logger.logServer("Server stopped accepting connections.");
            } else {
                Logger.logServerError("Server error occurred: " + e.getMessage());
            }
        }
    }

    public static void shutdown() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }

        try {
            serverSocket.close();
            Logger.logServer("ServerSocket closed.");
        } catch (IOException e) {
            Logger.logServerError("Failed to close ServerSocket: " + e.getMessage());
        }
    }
}