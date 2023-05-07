package synchronization;

import gui.MainFrame;

import javax.swing.*;
import java.rmi.RemoteException;

/**
 * The SyncMasterPro class serves as the entry point for the application.
 * It initializes the MainFrame to facilitate synchronization between two directories.
 */
public class SyncMasterPro {
    /**
     * The main method of the SyncMasterPro class.
     * It starts the MainFrame, which handles the synchronization process.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MainFrame();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
