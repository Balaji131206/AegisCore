import java.util.concurrent.ConcurrentHashMap;

public class SharedClientRegistry
{
    private static final ConcurrentHashMap<String, ClientHandler>
            connectedClients = new ConcurrentHashMap<>();

    private static final SharedClientRegistry instance = new SharedClientRegistry();

    private SharedClientRegistry() {}

    public static SharedClientRegistry getInstance() {
        return instance;
    }

    public void addClient(String clientId,
                          ClientHandler handler)
    {
        connectedClients.put(clientId, handler);

        System.out.println(
                "[INFO] Client connected: " + clientId
        );
        System.out.println("[INFO] Total connected clients: " + getClientCount());
        System.out.println("[INFO] Connected clients: " + connectedClients.keySet());
    }


    public void removeClient(String clientId)
    {
        connectedClients.remove(clientId);

        System.out.println(
                "[INFO] Client disconnected: " + clientId
        );
        System.out.println("[INFO] Total connected clients: " + getClientCount());
        System.out.println("[INFO] Connected clients: " + connectedClients.keySet());
    }


    public ClientHandler getClient(String clientId)
    {
        return connectedClients.get(clientId);
    }


    public void showConnectedClients()
    {
        System.out.println("[INFO] Currently connected clients: " + connectedClients.keySet());
    }

    public int getClientCount() {
        return connectedClients.size();
    }

    public void BroadcastMessage(String message)
    {
        for (ClientHandler handler : connectedClients.values())
        {
            handler.sendMessage(message);
        }
    }
}