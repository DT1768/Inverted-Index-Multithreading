package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface InvertedIndexService extends Remote {
    Map<String, List<Integer>> getInvertedIndex(String fileName) throws RemoteException;
}
