

import android.content.Context;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectStore<T> {

    private LRUMap<String, T> lruMap=null;
    private String storeName;
    private String cacheStoragePath;
    public ObjectStore(Context context, String storeName, int size) throws IOException {
        this.cacheStoragePath=context.getCacheDir()+"/.mycachestore";
        File cacheDir = new File(cacheStoragePath);
        if(cacheDir.exists()){
            System.out.println("Cache dir exists!");
            loadStoreFile(storeName, size);
        }else{
            System.out.println("Cache dir doesn't exists. Creating a new one.");
            if(cacheDir.mkdirs()) {
                loadStoreFile(storeName, size);
            }else{
                throw new IOException("couldn't create cache directory");
            }
        }
        this.storeName=storeName;
    }

    private void loadStoreFile(String storeName, int size){
        File storeFile=new File(cacheStoragePath+"/"+storeName);
        lruMap=new LRUMap<>(size);
        if(storeFile.exists()) {
            System.out.println("cache store file already exists! Loading it to memory now.");
            try {
                FileInputStream file = new FileInputStream(cacheStoragePath+"/"+storeName);
                ObjectInputStream in = new ObjectInputStream(file);

                // Method for deserialization of object
                lruMap = (LRUMap<String, T>)in.readObject();

                in.close();
                file.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ;
        }
    }

    public void persist(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream file = new FileOutputStream(cacheStoragePath + "/" + storeName);
                    ObjectOutputStream out = new ObjectOutputStream(file);

                    out.writeObject(lruMap);

                    out.close();
                    file.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("wrote cache data from memory to disk and closed it!");
            }
        }).start();
    }

    public T get(String key){
        return lruMap.get(key);
    }

    public ArrayList<T> getAll(){
        ArrayList<T> list=new ArrayList<>();
        Iterator<T> iterator=lruMap.values().iterator();
        while (iterator.hasNext()){
            list.add(iterator.next());
        }
        Collections.reverse(list);
        return list;
    }

    public void put(String key, T value){
        lruMap.put(key, value);
    }

    public void delete(String key){
        lruMap.remove(key);
    }

    private static class LRUMap<K, V> extends LinkedHashMap<K, V> implements Serializable {
        private int size=10;

        LRUMap(int size) {
            super(size, 0.75f, true);
            this.size = size;
        }

        LRUMap() {}


        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > size;
        }

    }
}
