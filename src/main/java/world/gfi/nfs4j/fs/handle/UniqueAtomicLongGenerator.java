package world.gfi.nfs4j.fs.handle;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Unique handle generator using an atomic long to generate it's value.
 */
public class UniqueAtomicLongGenerator implements UniqueHandleGenerator {
    AtomicLong atomicLong = new AtomicLong(1);

    @Override
    public long uniqueHandle() {
        return atomicLong.getAndIncrement();
    }
}
