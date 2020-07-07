package esa.s1pdgs.cpoc.obs_sdk;

import java.time.Instant;

public class ObsObjectMetadata {

    private final String key;
    private final Instant lastModified;

    public ObsObjectMetadata(String key, Instant lastModified) {
        this.key = key;
        this.lastModified = lastModified;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public String getKey() {
        return key;
    }
}
