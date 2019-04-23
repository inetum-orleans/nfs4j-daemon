package world.gfi.nfs4j.config;

import org.apache.commons.lang3.SystemUtils;
import world.gfi.nfs4j.fs.permission.PermissionsMapperType;

public class PermissionsConfig {
    PermissionsMapperType type = null;
    Integer uid = null;
    Integer gid = null;
    Integer mask = null;

    public PermissionsMapperType getType() {
        if (type == null) {
            if (SystemUtils.IS_OS_LINUX) {
                return PermissionsMapperType.UNIX;
            }
            return PermissionsMapperType.DISABLED;
        }
        return type;
    }

    public void setType(PermissionsMapperType type) {
        this.type = type;
    }

    public int getUid() {
        if (uid == null) {
            return 0;
        }
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        if (gid == null) {
            return 0;
        }
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public int getMask() {
        if (mask == null) {
            return getType() == PermissionsMapperType.DISABLED ? 0775 : 0664;
        }
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }
    
    public void defaultMask() {
        this.mask = null;
    }

    @Override
    public String toString() {
        return "PermissionsConfig{" +
                "type=" + getType() +
                ", uid=" + getUid() +
                ", gid=" + getGid() +
                ", mask=" + Integer.toOctalString(getMask()) +
                '}';
    }

    /**
     * Creates a new PermissionsConfig object that is merged from this permissions and given other permissions.
     * <p>
     * Only defined properties from other will be set, while keeping undefined properties of other from this.
     *
     * @param other
     * @return merged permissions config
     */
    public PermissionsConfig merge(PermissionsConfig other) {
        PermissionsConfig merged = new PermissionsConfig();

        merged.type = other.type == null ? type : other.type;
        merged.uid = other.uid == null ? uid : other.uid;
        merged.gid = other.gid == null ? gid : other.gid;
        merged.mask = other.mask == null ? mask : other.mask;

        return merged;
    }
}
