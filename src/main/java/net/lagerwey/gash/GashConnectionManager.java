package net.lagerwey.gash;

import java.util.HashMap;
import java.util.Map;

import static net.lagerwey.gash.command.MountCommand.resolveKnownLocator;

/**
 */
public class GashConnectionManager {

    private final Map<String, GashConnection> connections;

    public GashConnectionManager() {
        connections = new HashMap<String, GashConnection>();
    }

    public void close(GashConnection connection) {
        connection.close();
        connections.remove(connection.getKey());
    }

    public GashConnection getConnection(String connectionKey) {
        if (!connections.containsKey(connectionKey)) {
            connectionKey = resolveKnownLocator(connectionKey);
            return connections.get(connectionKey);
        }
        return connections.get(connectionKey);
    }

    public boolean isConnected(String connectionKey) {
        if (!connections.containsKey(connectionKey)) {
            connectionKey = resolveKnownLocator(connectionKey);
            return connections.containsKey(connectionKey);
        }
        return true;
    }

    public GashConnection open(String lookupGroups, String lookupLocators) {
        GashConnection connection = new GashConnection(lookupGroups, lookupLocators);
        connections.put(connection.getKey(), connection);
        connection.connect();
        return connection;
    }

    public void closeAll() {
        for (GashConnection connection : connections.values()) {
            connection.close();
        }
        connections.clear();
    }

    public boolean hasConnections() {
        return connections.size() > 0;
    }

    public void listConnections() {
        for (GashConnection connection : connections.values()) {
            Utils.println(connection.getConnectionString());
        }
    }
}
