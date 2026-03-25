package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMI Server entry point.
 */
public class HRMServer {

    public static void main(String[] args) {
        try {
            // Start Socket Chat Server in background thread
            Thread socketThread = new Thread(() -> {
                try {
                    new socket.ChatServer().start();
                } catch (Exception e) {
                    System.out.println("Socket server error: " + e.getMessage());
                }
            });
            socketThread.setDaemon(true);
            socketThread.start();
            System.out.println("Chat Socket Server started on port 12345");

            // Create the RMI registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // Create an instance of the remote service implementation
            HRMServiceImpl service = new HRMServiceImpl();

            // Bind the remote object to the registry with the name "HRMService"
            registry.rebind("HRMService", service);

            System.out.println("==============================================");
            System.out.println("   HRM RMI Server started successfully!");
            System.out.println("   Registry running on port 1099");
            System.out.println("   Service bound as: HRMService");
            System.out.println("==============================================");
            System.out.println("Server is ready and waiting for client connections...");

        } catch (Exception e) {
            System.err.println("HRM Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
