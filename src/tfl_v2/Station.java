package tfl_v2;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a physical TfL station.
 * V2 Upgrade: Replaced hand-coded arrays with ArrayLists for O(1) amortized insertions.
 */
public class Station {

    private String name;

    // Replace Connection array with a List
    private List<Connection> connections;

    // Replace Interchange array with a List
    private List<Interchange> interchanges;

    public Station(String name) {
        this.name = name;
        this.connections = new ArrayList<>();
        this.interchanges = new ArrayList<>();
    }

    // CONNECTION LOGIC (Tracks)

    public void addConnection(Connection newConnection) {
        // Clean code wit Arraylist
        connections.add(newConnection);
    }

    // INTERCHANGE LOGIC (Line Changes)

    public void addInterchange(Interchange newInterchange) {
        interchanges.add(newInterchange);
    }

    /**
     * Calculates how long it takes a passenger to walk from one line to another.
     * V2 Upgrade: Uses enhanced for-loop for better readability.
     */
    public double getInterchangeTime(String fromLine, String toLine) {
        if (fromLine.isEmpty() || fromLine.equalsIgnoreCase(toLine)) {
            return 0.0;
        }

        for (Interchange ic : interchanges) {
            if (ic.getFromLine().equalsIgnoreCase(fromLine) && ic.getToLine().equalsIgnoreCase(toLine)) {
                return ic.getWalkTime();
            }
        }
        // Fallback: If forgot time to put them in the CSV, default to 2 minutes
        return 2.0;
    }

    public String getName() {
        return name;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public int getConnectionCount() {
        return connections.size();
    }


    @Override
    public String toString() {
        return name;
    }
}