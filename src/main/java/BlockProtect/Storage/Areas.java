package BlockProtect.Storage;

import cn.nukkit.level.Location;
import java.io.Serializable;

public class Areas implements Serializable {
    
    private double x;
    private double y;
    private double z;
    
    public Areas(Location location) {
        this.x = location.x;
        this.y = location.y;
        this.z = location.z;
    }
    
    public Location getLocation() {
        return new Location(x,y,z);
    }
}
