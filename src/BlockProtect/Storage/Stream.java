package BlockProtect.Storage;

import BlockProtect.BlockProtect;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;


public class Stream {
    
    private BlockProtect plugin;
    private String dataFile = "block.dat";
    
    public Stream(BlockProtect plugin) {
        this.plugin = plugin;
    }
    
    public void init() {
        try {
            plugin.getLogger().info("Loading Block Protect data...");
            FileInputStream fis = new FileInputStream(plugin.getDataFolder() + File.separator + dataFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            plugin.map = (HashMap) ois.readObject();
            ois.close();
            plugin.getLogger().info("Block Protect data has been loaded!");
        } catch (FileNotFoundException e) {
            plugin.getLogger().info("No Block Protect data found.");
            plugin.map = new HashMap();
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().critical("Unable to get Block Protect data: " + e.getMessage());
            plugin.map = new HashMap();
        }
    }
    
    public void save() {
        try {
            plugin.getLogger().info("Saving Block Protect data...");
            FileOutputStream fos = new FileOutputStream(plugin.getDataFolder() + File.separator + dataFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(plugin.map);
            oos.close();
            plugin.getLogger().info("Block Protect data has been saved!");
        } catch (IOException e) {
            plugin.getLogger().critical("Unable to save Block Protect data: " + e.getMessage());
        }
    }
    
    
    
}
