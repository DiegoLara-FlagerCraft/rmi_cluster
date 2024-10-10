package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClusterService extends Remote {
    String submitAlgorithm(String fileName, byte[] fileData, ClientCallback callback) throws RemoteException;
}
