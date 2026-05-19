import java.io.*;
import java.net.*;

public class ClientHandler extends Thread 
{

    private Socket socket;
    private String clientId;

    public ClientHandler(Socket socket) 
    {
        this.socket = socket;
        this.clientId = socket.getRemoteSocketAddress().toString();
    }

    public String getClientId() 
    {
        return clientId;
    }

    @Override
    public void run() 
    {

        try 
        {

            BufferedReader input =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));

            PrintWriter output =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true);

            output.println("[INFO] Connected to server!");

            String message;

            while ((message = input.readLine()) != null) 
            {

                System.out.println("[INFO] Client says: " + message);

                output.println("[INFO] Server received: " + message);

                if (message.equalsIgnoreCase("exit")) 
                {
                    break;
                }
            }

            System.out.println("[INFO] Client disconnected.");

        }
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        finally
        {
            SharedClientRegistry.removeClient(clientId);
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}