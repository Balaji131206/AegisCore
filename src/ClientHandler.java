import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable 
{
    private final SharedClientRegistry registry;
    private Socket socket;
    private String clientId;

    public ClientHandler(Socket socket,
                     SharedClientRegistry registry)
    {
            this.socket = socket;
            this.registry = registry;

            this.clientId =
                    socket.getRemoteSocketAddress()
                        .toString();
    }

    public String getClientId() 
    {
        return clientId;
    }

    public synchronized void sendMessage(String message)
    {
        try
        {
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.println(message);
              
        } catch (IOException e) 
        {
            Logger.logClientHandlerError("Failed to send message to client " + clientId + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try (
            BufferedReader input = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()
                )
            );
            PrintWriter output = new PrintWriter(
                socket.getOutputStream(), 
                true)
        ) 
        {
            output.println("[INFO] Connected to server!");

            String message;
            while ((message = input.readLine()) != null) {
                Logger.logClientHandler("Client " + clientId + " says: " + message);

                output.println("[INFO] Server received: " + message);

                registry.BroadcastMessage("[BROADCAST] " + clientId + " says: " + message);

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            Logger.logClientHandler("Client " + clientId + " disconnected.");
        } catch (IOException e) {
            Logger.logClientHandlerError("Connection error for client " + clientId + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        registry.removeClient(clientId);
        closeSocket();
    }


    private void closeSocket() {
        if (socket == null || socket.isClosed()) {
            return;
        }

        try {
            socket.close();
        } catch (IOException e) {
            Logger.logClientHandlerError("Failed to close socket for client " + clientId + ": " + e.getMessage());
        }
    }
}
