import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import harreader.HarReader;
import harreader.HarReaderException;
import harreader.model.Har;
import harreader.model.HarEntry;

public class Storage extends UnicastRemoteObject implements StorageClientInterface {
    private LinkedHashMap<String, ArrayList<ResourceInfo>> timeHarMap;

    public Storage() throws RemoteException {
        timeHarMap = new LinkedHashMap<>();
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
    public boolean uploadFile(String filename, byte[] data, int len, int fileCount) throws RemoteException {
        try {
            File f = new File("output\\" + filename);
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f,false);
            out.write(data, 0, len);
            out.flush();
            out.close();
            System.out.println("Done writing data...");
            readFileToMap(f.getPath(), fileCount);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public LinkedHashMap<String, ArrayList<ResourceInfo>> getTimeHarMap() throws RemoteException {
        return timeHarMap;
    }
}
