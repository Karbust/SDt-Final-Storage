import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIStorage {
    public static void main(String[] args) {
        Registry r = null;

        try {
            r = LocateRegistry.createRegistry(2025);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            Storage storage = new Storage();
            assert r != null;
            r.bind("storage", storage);
            System.out.println("Storage server ready");
        } catch (Exception e) {
            System.out.println("Storage server main " + e.getMessage());
        }
    }
}
