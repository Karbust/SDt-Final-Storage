import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public interface StorageReducerInterface extends Remote {
    LinkedHashMap<String, ArrayList<ResourceInfo>> getTimeHarMap() throws RemoteException;

    //vai ser um save mas é como se fosse um update
    void saveCombinationsReducer(String mapperId, String storageId, List<ProcessCombinationModel> combinationsReducer) throws RemoteException;
    List<ProcessCombinationModel> getCombinationsReducer(String mapperId, String reducerId) throws RemoteException;
}
