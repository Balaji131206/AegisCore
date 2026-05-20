import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe logging system that logs events to both the console 
 * and specific log files stored in the /logs directory.
 */
public class Logger {
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        // Ensure logs directory exists
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private static synchronized void writeToFile(String filename, String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);
        
        // Output to console
        if ("ERROR".equals(level)) {
            System.err.println(logEntry);
        } else {
            System.out.println(logEntry);
        }

        // Output to file
        File file = new File(LOG_DIR, filename);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to write to log file " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Writes information logs for the Server to logs/Server.log
     */
    public static void logServer(String message) {
        writeToFile("Server.log", "INFO", message);
    }

    /**
     * Writes error logs for the Server to logs/Server.log
     */
    public static void logServerError(String message) {
        writeToFile("Server.log", "ERROR", message);
    }

    /**
     * Writes information logs for the ClientHandler to logs/CliendHandler.log
     */
    public static void logClientHandler(String message) {
        writeToFile("CliendHandler.log", "INFO", message);
    }

    /**
     * Writes error logs for the ClientHandler to logs/CliendHandler.log
     */
    public static void logClientHandlerError(String message) {
        writeToFile("CliendHandler.log", "ERROR", message);
    }

    /**
     * Writes information logs for the Client to logs/ClientID.log
     */
    public static void logClient(String message) {
        writeToFile("ClientID.log", "INFO", message);
    }

    /**
     * Writes error logs for the Client to logs/ClientID.log
     */
    public static void logClientError(String message) {
        writeToFile("ClientID.log", "ERROR", message);
    }
}
