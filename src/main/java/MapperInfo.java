import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class MapperInfo implements Serializable {
    ConcurrentHashMap<String, ReducerInfo> reducersInfo = new ConcurrentHashMap<>();
}
