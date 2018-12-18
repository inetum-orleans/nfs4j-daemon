package world.gfi.nfs4j.config;

import world.gfi.nfs4j.fs.permission.PermissionsMapperType;

public class PermissionsConfig {
    PermissionsMapperType type = PermissionsMapperType.SIMPLE;

    int uid = 0;
    int gid = 0;
    int mask = 0755;

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
        return mask;
    }

    public void setMask(int mask) {
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
