package BlockProtect;

import cn.nukkit.scheduler.PluginTask;

public class SaveTask extends PluginTask<BlockProtect> {
    private BlockProtect plugin;
    
    public SaveTask(BlockProtect owner) {
        super(owner);
        this.plugin = owner;
    }

    @Override
    public void onRun(int i) {
        plugin.stream.save();
    }
}
