import java.io.*;
import java.net.*;

public class Client 
{

    public static void main(String[] args) 
    {

        try 
        {

            Socket socket = new Socket("localhost", 5000);

            BufferedReader input =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()));

            PrintWriter output =
                    new PrintWriter(
                            socket.getOutputStream(),
                            true);

            BufferedReader userInput =
                    new BufferedReader(
                            new InputStreamReader(
                                    System.in));

            System.out.println(input.readLine());

            while (true) {
                System.out.print("Enter message to send to server (type 'exit' to quit): ");
                String message = userInput.readLine();

                if (message == null) 
                    break;

                output.println(message);

                String response = input.readLine();

                System.out.println(response);

                if (message.equalsIgnoreCase("exit") || response == null) 
                {
                    break;
                }
            }

            socket.close();
            userInput.close();

        } catch (IOException e) 
        {
            e.printStackTrace();
        }
    }
}