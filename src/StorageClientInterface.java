import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface StorageClientInterface extends Remote {
    boolean uploadFile(String filename, byte[] data, int len, int fileCount) throws RemoteException;
    LinkedHashMap<String, ArrayList<ResourceInfo>> getTimeHarMap() throws RemoteException;
}
