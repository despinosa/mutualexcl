package mx.ipn.escom.supernaut.mutualexcl.centralized;

import mx.ipn.escom.supernaut.mutualexcl.DistributedProcess;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {
    static class CSConcession {}

    static class CSRelease {}

    static final int port = 8765;

    static class void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(CSRequest.class);
        kryo.register(CSConcession.class);
        kryo.register(CSRelease.class);
    }
}
