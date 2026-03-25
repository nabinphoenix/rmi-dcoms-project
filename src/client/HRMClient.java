package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import remote.HRMService;

/**
 * RMI Client connector class.
 * Connects to the RMI registry at localhost:1099, looks up the
 * "HRMService" remote object, and returns a stub for the client
 * to use. Includes reconnect logic for handling connection failures.
 */
public class HRMClient {

    // RMI connection parameters
    private static final String HOST = "localhost";
    private static final int PORT = 1099;
    private static final String SERVICE_NAME = "HRMService";

    // Cached stub reference
    private static HRMService service = null;

    /**
     * Returns the HRMService stub. If not already connected or if the
     * connection was lost, attempts to reconnect to the RMI registry.
     * This method acts as a singleton accessor for the RMI service.
     * 
     * @return HRMService remote stub
     */
    public static HRMService getService() {
        if (service == null) {
            connect();
        }
        return service;
    }

    /**
     * Connects to the RMI registry and looks up the HRMService.
     * Uses LocateRegistry.getRegistry() to get a reference to the
     * remote registry, then performs a lookup by service name.
     * Implements retry logic with up to 3 attempts.
     */
    public static void connect() {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                attempt++;
                System.out.println("[CLIENT] Connecting to RMI registry at " + HOST + ":" + PORT + " (Attempt "
                        + attempt + ")...");

                // Get a reference to the remote RMI registry
                Registry registry = LocateRegistry.getRegistry(HOST, PORT);

                // Look up the remote service by its bound name
                // This returns a stub (proxy) that implements HRMService
                service = (HRMService) registry.lookup(SERVICE_NAME);

                System.out.println("[CLIENT] Successfully connected to HRMService.");
                return;

            } catch (Exception e) {
                System.err.println("[CLIENT] Connection attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        System.out.println("[CLIENT] Retrying in 2 seconds...");
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        System.err.println("[CLIENT] Failed to connect to RMI server after " + maxRetries + " attempts.");
        service = null;
    }

    /**
     * Forces a reconnection to the RMI server.
     * Useful when the connection is lost during operation.
     */
    public static void reconnect() {
        service = null;
        connect();
    }
}
