package mx.ipn.escom.supernaut.mutualexcl;


public class CSRequest {
    public final Integer pid;
    public final Long time;

    public CSRequest(int pid, long time) {
        this.pid = pid;
        this.time = time;
    }

    public int compareTo(CSRequest other) {
        return this.time.compareTo(other.time);
    }
}
