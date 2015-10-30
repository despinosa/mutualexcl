package mx.ipn.escom.supernaut.mutualexcl.ring;

import mx.ipn.escom.supernaut.mutualexcl.DistributedProcess;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class RingProcess extends DistributedProcess {
    final class RingThread extends DistributedProcess.AlgorithmThread {
        Lock lock;
        Client 
    }
}
