

/*
* Copyright 2024 Iradukunda Moustapa

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
* 
* */

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
public class LongList<T>{
    public static String storageRootDir = Paths.get(".").toAbsolutePath().normalize().toString();
    private final String id;
    private long totalSize = 0L;
    private int blockSize;
    private int recentBlockIndex = -1;
    private ArrayList<T> recentBlockList = null;
    private String dirPath;

    public LongList(String id) throws IOException {
        this.id = id;
        blockSize = 500;
        init();
    }
    public LongList(String id, int blockSize) throws IOException {
        this.id = id;
        this.blockSize = blockSize;
        init();
    }

    public long size() {
        return totalSize;
    }

    private void writeObject(String path, Object obj){
        try {
            FileOutputStream file = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(obj);
            out.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Object readObject(String path){
        Object obj = null;
        try {
            FileInputStream file = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(file);
            // Method for deserialization of object
            obj = in.readObject();
            in.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    private void init() throws IOException {
        dirPath = storageRootDir + "/longlist_data/" + id;
        Path path = Paths.get(dirPath);
        if (Files.exists(path)) {
            ArrayList<Object> attributes = (ArrayList<Object>) readObject(dirPath + "/attributes");
            totalSize = (long) attributes.get(0);
            blockSize = (int) attributes.get(1);
        } else {
            Files.createDirectories(Paths.get(dirPath));
            ArrayList<T> blockList = new ArrayList<>();
            writeObject(dirPath + "/block-0", blockList);
            persistSizeAttribute();
        }
    }

    private void persistSizeAttribute() throws IOException {
        ArrayList<Object> attributes = new ArrayList<>();
        attributes.add(totalSize);
        attributes.add(blockSize);
        writeObject(dirPath + "/attributes", attributes);
    }

     //initialize the LongList with an ArrayList
     public void initialize(ArrayList<T> inputList) throws IOException {
        ArrayList<T> blockList;
        int blockIndex = 0;
        for (int i = 0; i < inputList.size(); i = i + blockSize) {
            int currentBlockSize = 0;
            int j = i;
            blockList = new ArrayList<>();
            while (currentBlockSize < blockSize && j < inputList.size()) {
                blockList.add(inputList.get(j));
                j++;
                currentBlockSize++;
            }
            writeObject(dirPath + "/block-" + blockIndex, blockList);
            blockIndex++;
        }
        totalSize = inputList.size();
        persistSizeAttribute();
    }

    public T get(int index) throws IOException {
        if (index >= totalSize || index < 0) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + totalSize);
        }
        double i = index / blockSize;
        int blockIndex = 0;
        if (i >= 1) {
            blockIndex = (int) Math.ceil(i);
        }
        if (recentBlockList == null || blockIndex != recentBlockIndex) {
            ArrayList<T> blockList = (ArrayList<T>) readObject(dirPath + "/block-" + blockIndex);
            recentBlockList = blockList;
            recentBlockIndex = blockIndex;
        }
        int blockListIndex = index - (recentBlockIndex * blockSize);
        return recentBlockList.get(blockListIndex);
    }



    public void add(T element) throws IOException {
        double i = (totalSize - 1) / blockSize;
        int blockIndex = 0;
        if (i >= 1) {
            blockIndex = (int) Math.ceil(i);
        }
        if (recentBlockList == null || blockIndex != recentBlockIndex) {
            //read the block from filesystem if it is already not present in the memory
            ArrayList<T> blockList = (ArrayList<T>) readObject(dirPath + "/block-" + blockIndex);
            recentBlockIndex = blockIndex;
            recentBlockList = blockList;
        }
        if (recentBlockList.size() == blockSize) {
            //create new block
            recentBlockList = new ArrayList<>();
            recentBlockList.add(element);
            recentBlockIndex++;
        } else {
            //use existing block
            recentBlockList.add(element);
        }
        totalSize++;
        writeObject(dirPath + "/block-" + recentBlockIndex, recentBlockList);
        persistSizeAttribute();
    }

    private static void deleteDir(String path) throws IOException {
        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
                    // delete directories or folders
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                    // delete files
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                }
        );
    }


    public void clear() throws IOException {
        deleteDir(dirPath);
    }

    public static void clear(String id) throws IOException {
        String path = storageRootDir + "/longlist_data/" + id;
        deleteDir(path);
    }

}