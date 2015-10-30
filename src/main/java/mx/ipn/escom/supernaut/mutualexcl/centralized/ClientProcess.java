package mx.ipn.escom.supernaut.mutualexcl.centralized;

import mx.ipn.escom.supernaut.mutualexcl.DistributedProcess;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class ClientProcess extends DistributedProcess {
    Client client;

    final class ClientThread extends DistributedProcess.AlgorithmThread {
        public void csRequested() {
            CSRequest request;
            request = new CSRequest(pid, )
        }
        public void csFreed() {}
        public void run() {}
    }
}
