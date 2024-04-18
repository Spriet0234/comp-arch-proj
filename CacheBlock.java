public class CacheBlock {
    private long tag;
    private boolean valid;
    private long lastAccessTime; 

    public CacheBlock() {
        this.tag = -1; 
        this.valid = false;
        this.lastAccessTime = 0; 
    }

    public long getTag() {
        return tag;
    }

    public void setTag(long tag) {
        this.tag = tag;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}
