package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientCallbackImpl extends UnicastRemoteObject implements ClientCallback {

    public ClientCallbackImpl() throws RemoteException {
        super();
    }

    @Override
    public void onResult(String result) throws RemoteException {
        System.out.println("Resultado recibido del servidor: " + result);
    }
}
