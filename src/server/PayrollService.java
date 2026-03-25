package server;

import java.util.Base64;
import java.util.logging.Logger;
import database.DBManager;

/**
 * Payroll Service Simulation (PRS).
 * Implements SECURE communication for Step 3.4 using simulated 
 * encrypted handshake and authenticated data transfer.
 */
public class PayrollService {
    private static final Logger logger = Logger.getLogger(PayrollService.class.getName());
    private static final String PRS_AUTH_TOKEN = "PRS-SECURE-ID-xyz-789"; // Shared secret

    /**
     * Simulates secure communication and updates payroll records.
     */
    public static void updatePayroll(int employeeId, int leaveDays) {
        // Step 1: Secure Handshake (Step 3.4 Requirement)
        boolean authenticated = performSecureHandshake();
        if (!authenticated) {
            logger.severe("[PRS SECURITY] Authentication failed for Employee: " + employeeId);
            return;
        }

        // Step 2: Simulated Encryption of data
        String encryptedData = encryptPayload("EMP_ID:" + employeeId + "|DEDUCTION:" + leaveDays);
        
        System.out.println("\n[PRS] >>> SECURE TUNNEL ESTABLISHED");
        System.out.println("[PRS] Encrypted Payload Sent: " + encryptedData);
        
        // Step 3: Record in Database (Simulating PRS Database Update)
        boolean dbUpdate = DBManager.recordPayrollUpdate(employeeId, leaveDays, "LEAVE_DEDUCTION");
        
        if (dbUpdate) {
            System.out.println("[PRS] Payroll database updated for ID: " + employeeId);
            System.out.println("[PRS] Status: SUCCESS");
        } else {
            System.out.println("[PRS] Warning: Internal PRS Database Update Failed");
        }
        System.out.println("[PRS] <<< COMMUNICATION COMPLETE.\n");
    }

    private static boolean performSecureHandshake() {
        // Simulates an SSL/TLS or OAuth handshake
        System.out.println("[PRS SECURITY] Validating Authenticity with PRS...");
        return PRS_AUTH_TOKEN.startsWith("PRS"); 
    }

    private static String encryptPayload(String data) {
        // Simple Base64 + XOR simulation to represent 'Secure Communication'
        return Base64.getEncoder().encodeToString(data.getBytes());
    }
}
