import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

public interface StorageMasterInterface extends Remote {
    ConcurrentHashMap<String, MapperInfo> getCombinations() throws RemoteException;
    Boolean hasReducerFinishedJob(String mapperId, String reducerId) throws RemoteException;
    Boolean hasMapperFinishedJob(String mapperId) throws RemoteException;
}
