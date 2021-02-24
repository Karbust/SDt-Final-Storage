import java.rmi.Remote;
import java.rmi.RemoteException;

public interface StorageClientInterface extends Remote {
    Boolean uploadFile(String filename, byte[] data, int len, int fileCount) throws RemoteException;
}
