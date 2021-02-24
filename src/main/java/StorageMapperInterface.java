import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public interface StorageMapperInterface extends Remote {
    LinkedHashMap<String, ArrayList<ResourceInfo>> getTimeHarMap() throws RemoteException;

    void saveCombinationsMapper(String mapperId, HashMap<String, ArrayList<ProcessCombinationModel>> combinations) throws RemoteException;
}
