package mx.ipn.escom.supernaut.mutualexcl.centralized;

import mx.ipn.escom.supernaut.mutualexcl.CSRequest;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import java.lang.Thread;

public class ExclusionServer extends Server {
    Server server;
    Queue<CSRequest> requests;
}
