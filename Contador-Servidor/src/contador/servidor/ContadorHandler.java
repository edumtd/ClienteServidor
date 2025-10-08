import java.util.HashMap;
import java.util.Map;

public class ContadorHandler {
    // Map to store client sessions
    private Map<String, ClientSession> clientSessions = new HashMap<>();
    
    // Class to represent a client session
    private class ClientSession {
        private String clientId;
        private int count;
        private List<Integer> history;
        
        public ClientSession(String clientId) {
            this.clientId = clientId;
            this.count = 0;
            this.history = new ArrayList<>();
        }
        
        public void increment() {
            count++;
            history.add(count);
        }
        
        public void reset() {
            count = 0;
            history.clear();
        }
        
        public int getCount() {
            return count;
        }
        
        public List<Integer> getHistory() {
            return history;
        }
    }
    
    // Method to handle client connection
    public void connectClient(String clientId) {
        if (!clientSessions.containsKey(clientId)) {
            clientSessions.put(clientId, new ClientSession(clientId));
        }
    }
    
    // Method to handle client disconnection with persistence
    public void disconnectClient(String clientId) {
        // Optionally save session state to a database or file
        // SaveClientSession(clientSessions.get(clientId));
        clientSessions.remove(clientId);
    }
    
    // Method for client reconnection
    public void reconnectClient(String clientId) {
        if (clientSessions.containsKey(clientId)) {
            // Restore session state
            // RestoreClientSession(clientId);
        } else {
            connectClient(clientId);
        }
    }
    
    // Method to increment count for a client
    public void incrementCount(String clientId) {
        ClientSession session = clientSessions.get(clientId);
        if (session != null) {
            session.increment();
        }
    }
}