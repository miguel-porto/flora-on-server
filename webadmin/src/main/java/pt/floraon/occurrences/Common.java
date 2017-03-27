package pt.floraon.occurrences;

import org.jfree.util.Log;
import pt.floraon.occurrences.entities.InventoryList;
import pt.floraon.occurrences.fieldparsers.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by miguel on 23-03-2017.
 */
public final class Common {
    static InventoryList readInventoryListFromFile(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream oist;
        InventoryList invList;

        File f = new File("/tmp/" + fileName);
        if (f.canRead()) {
            Log.info("Read " + f.getName());
            oist = new ObjectInputStream(new FileInputStream(f));
            invList = (InventoryList) oist.readObject();
            Log.info(invList.size());
            oist.close();
            return invList;
        } else throw new IOException("File not found.");
    }
}
