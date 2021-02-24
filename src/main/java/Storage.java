import harreader.HarReader;
import harreader.HarReaderException;
import harreader.model.Har;
import harreader.model.HarEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Storage extends UnicastRemoteObject
        implements StorageClientInterface, StorageMapperInterface, StorageReducerInterface, StorageMasterInterface {

    private final LinkedHashMap<String, ArrayList<ResourceInfo>> timeHarMap; // < Resource URL, List of Resources >
    private final ConcurrentHashMap<String, MapperInfo> mappersInfo; // < Mapper ID, MappersInfo >

    public Storage() throws RemoteException {
        timeHarMap = new LinkedHashMap<>();
        mappersInfo = new ConcurrentHashMap<>();
    }

    public void readFileToMap(String path, int fileCount) throws HarReaderException {
        try {
            HarReader harReader = new HarReader();
            File file = new File(path);
            if (file.exists()) {
                Har otherHar = harReader.readFromFile(file);
                for (HarEntry otherEntry : otherHar.getLog().getEntries()) {
                    if (!otherEntry.getResponse().getHeaders().get(0).getValue().contains("no-cache")) {
                        ResourceInfo resourceInfo = new ResourceInfo();
                        resourceInfo.resourceTime = (float) otherEntry.getTime();
                        resourceInfo.resourceType = otherEntry.get_resourceType();
                        resourceInfo.cachedResource = otherEntry.getResponse().getHeaders().get(0).getValue();
                        resourceInfo.resourceLength = otherEntry.getResponse().getBodySize();
                        resourceInfo.harRun = fileCount;

                        if (timeHarMap.containsKey(otherEntry.getRequest().getUrl())) {
                            ArrayList<ResourceInfo> list = timeHarMap.get(otherEntry.getRequest().getUrl());
                            AtomicBoolean repeatedCall = new AtomicBoolean(false);
                            list.forEach(value -> {
                                if (value.resourceTime.equals(resourceInfo.resourceTime)) {
                                    repeatedCall.set(true);
                                    return;
                                }
                            });
                            if (!repeatedCall.get())
                                timeHarMap.get(otherEntry.getRequest().getUrl()).add(resourceInfo);
                        } else {
                            ArrayList<ResourceInfo> l = new ArrayList<>();
                            l.add(resourceInfo);
                            timeHarMap.put(otherEntry.getRequest().getUrl(), l);
                        }

                    }
                }
            }
        } catch (Exception ex) {
            // e.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public Boolean uploadFile(String filename, byte[] data, int len, int fileCount) throws RemoteException {
        try {
            File f = new File("output\\" + filename);
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f, false);
            out.write(data, 0, len);
            out.flush();
            out.close();
            //System.out.println("Done writing data...");
            readFileToMap(f.getPath(), fileCount);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public LinkedHashMap<String, ArrayList<ResourceInfo>> getTimeHarMap() throws RemoteException {
        return timeHarMap;
    }

    @Override
    public void saveCombinationsMapper(
            String mapperId, HashMap<String, ArrayList<ProcessCombinationModel>> combinations
    ) throws RemoteException {
        HashMap<String, ReducerInfo> combos = new HashMap<>();
        for (Map.Entry<String, ArrayList<ProcessCombinationModel>> entry : combinations.entrySet()) {
            String key = entry.getKey();
            List<ProcessCombinationModel> value = entry.getValue();
            ReducerInfo obj = new ReducerInfo();
            obj.PCMs = value;

            combos.put(key, obj);
        }

        MapperInfo info = new MapperInfo();
        info.reducersInfo.putAll(combos);
        mappersInfo.put(mapperId, info);
    }

    @Override
    public void saveCombinationsReducer(
            String mapperId, String reducerId, List<ProcessCombinationModel> combinationsReducer
    ) throws RemoteException {
        MapperInfo mInfo = mappersInfo.get(mapperId);
        if (mInfo == null) {
            MapperInfo temp = new MapperInfo();

            ReducerInfo obj = new ReducerInfo();
            obj.PCMs = combinationsReducer;
            obj.status = true;
            temp.reducersInfo.put(reducerId, obj);

            mappersInfo.put(mapperId, temp);
        } else {
            ReducerInfo temp = mInfo.reducersInfo.get(reducerId);
            if (temp != null && !temp.status) {
                ReducerInfo obj = new ReducerInfo();
                obj.PCMs = combinationsReducer;
                obj.status = true;
                mInfo.reducersInfo.put(reducerId, obj);
            }
        }
    }

    @Override
    public ConcurrentHashMap<String, MapperInfo> getCombinations() throws RemoteException {
        return mappersInfo;
    }

    @Override
    public Boolean hasReducerFinishedJob(String mapperId, String reducerId) throws RemoteException {
        MapperInfo mInfo = mappersInfo.get(mapperId);
        if(mInfo == null) {
            System.out.println("O mapper ainda não adicionou nada na Storage.");
            return null;
        }
        ReducerInfo obj = mInfo.reducersInfo.get(reducerId);
        if (obj != null) {
            return mInfo.reducersInfo.get(reducerId).status;
        }
        System.out.println("O reducer ainda não adicionou nenhuma combinação na Storage.");
        return null;
    }

    @Override
    public Boolean hasMapperFinishedJob(String mapperId) throws RemoteException {
        MapperInfo mInfo = mappersInfo.get(mapperId);
        return mInfo != null;
    }

    @Override
    public List<ProcessCombinationModel> getCombinationsReducer(String mapperId, String reducerId) throws RemoteException {
        MapperInfo mInfo = mappersInfo.get(mapperId);
        if(mInfo == null) {
            System.out.println("O mapper ainda não adicionou nada na Storage.");
            return null;
        }
        ReducerInfo obj = mInfo.reducersInfo.get(reducerId);
        if (obj != null) {
            return obj.PCMs;
        }
        return null;
    }
}
