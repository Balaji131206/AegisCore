import java.util.concurrent.ConcurrentHashMap;

public class SharedClientRegistry {
    private static final ConcurrentHashMap<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();

    public static void addClient(String clientId, ClientHandler handler)
    {
        ClientHandler existing =
        connectedClients.putIfAbsent(clientId, handler);

        if (existing != null)
        {
            System.out.println("[ERROR] Duplicate client ID");
        }

        System.out.println("[INFO] Client connected: " + clientId);
    }

    public static void removeClient(String clientId)
    {
        connectedClients.remove(clientId);

        System.out.println("[INFO] Client disconnected: " + clientId);
    }

    public static ClientHandler getClient(String clientId)
    {
        return connectedClients.get(clientId);
    }

    public static void showConnectedClients()
    {
        System.out.println("[INFO] Currently connected clients:");
        System.out.println(connectedClients.keySet().size());
    }    
}
