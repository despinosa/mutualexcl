package mx.ipn.escom.supernaut.mutualexcl.centralized;

import mx.ipn.escom.supernaut.mutualexcl.DistributedProcess;
import mx.ipn.escom.supernaut.mutualexcl.CSRequest;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {
    public static final int port = 8765;

    static void register(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(CSRequest.class);
        kryo.register(CSConcession.class);
        kryo.register(CSRelease.class);
    }
}
