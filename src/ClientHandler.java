import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread 
{

    private Socket socket;

    public ClientHandler(Socket socket) 
    {
        this.socket = socket;
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

            output.println("Connected to server!");

            String message;

            while ((message = input.readLine()) != null) 
            {

                System.out.println("Client says: " + message);

                output.println("Server received: " + message);

                if (message.equalsIgnoreCase("exit")) 
                {
                    break;
                }
            }

            socket.close();

            System.out.println("Client disconnected.");

        } catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}