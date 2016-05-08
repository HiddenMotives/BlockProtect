package BlockProtect;

import BlockProtect.Storage.Stream;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BlockProtect extends PluginBase {

    public HashMap map;
    public Stream stream;
    public Config config;

    @Override
    public void onEnable() {

        getLogger().info("BlockProtect is now running");

        getDataFolder().mkdirs();
        config = new Config(
                new File(this.getDataFolder(), "config.yml"),
                Config.YAML,
                new LinkedHashMap<String, Object>() {
            {
                put("protection-blockid", 22);
                put("protection-radius", 10);
                put("maxium-protections", 5);
            }
        });
        config.save();

        stream = new Stream(this);
        stream.init();
        
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getServer().getScheduler().scheduleRepeatingTask(new SaveTask(this), 2400, true);
    }

    @Override
    public void onDisable() {
        stream.save();
    }
    
    public String msg(String msg) {
        return TextFormat.AQUA + "[" + TextFormat.DARK_BLUE + "BlockProtect" + 
                TextFormat.AQUA + "] " + TextFormat.WHITE + msg;
    }

}
