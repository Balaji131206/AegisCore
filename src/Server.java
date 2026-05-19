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

        } catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed()) {
                System.out.println("[INFO] Server stopped accepting connections.");
            } else {
                e.printStackTrace();
            }
        }
    }

    public static void shutdown() {
        if (serverSocket == null || serverSocket.isClosed()) {
            return;
        }

        try {
            serverSocket.close();
            System.out.println("[INFO] ServerSocket closed.");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to close ServerSocket: " + e.getMessage());
        }
    }
}