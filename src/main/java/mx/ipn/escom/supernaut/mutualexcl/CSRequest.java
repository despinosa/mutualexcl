package mx.ipn.escom.supernaut.mutualexcl;


public class CSRequest {
    Integer pid;
    Integer time;

    public CSRequest(int pid, int time) {
        this.pid = pid;
        this.time = time;
    }

    public int compareTo(CSRequest other) {
        return this.time.compareTo(other.time);
    }
}
