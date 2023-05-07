package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface provides methods for handling requests between two synchronized directories.
 */
public interface RequestHandler extends Remote {

    /**
     * Checks if the remote directories is ready for synchronization.
     *
     * @return true if the remote directories is ready, false otherwise.
     * @throws RemoteException If a communication error occurs.
     */
    boolean isReady() throws RemoteException;

    /**
     * Sets the turn number for the remote directories.
     *
     * @param turn The turn number to set.
     * @throws RemoteException If a communication error occurs.
     */
    void setRemoteTurn(int turn) throws RemoteException;
}
