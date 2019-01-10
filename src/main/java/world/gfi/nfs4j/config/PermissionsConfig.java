package world.gfi.nfs4j.config;

import world.gfi.nfs4j.fs.permission.PermissionsMapperType;

public class PermissionsConfig {
    PermissionsMapperType type = PermissionsMapperType.ADVANCED;

    int uid = 0;
    int gid = 0;
    Integer mask = null;

    public PermissionsMapperType getType() {
        return type;
    }

    public void setType(PermissionsMapperType type) {
        this.type = type;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public int getMask() {
        if (mask == null) {
            return type == PermissionsMapperType.SIMPLE ? 0775 : 0664;
        }
        return mask;
    }

    public void setMask(Integer mask) {
        this.mask = mask;
    }

    @Override
    public String toString() {
        return "PermissionsConfig{" +
                "type=" + type +
                ", uid=" + uid +
                ", gid=" + gid +
                ", mask=" + mask +
                '}';
    }
}
