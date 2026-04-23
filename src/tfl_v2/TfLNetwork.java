package tfl_v2;

import java.util.*;

/**
 * The master class for TfL Network - Version 2.
 */
public class TfLNetwork {

    // V2: HashMap gives O(1) average lookup by station name
    private Map<String, Station> stationMap;

    public TfLNetwork() {
        this.stationMap = new HashMap<>();
    }

    // SETUP METHODS (Used for loading CSV data)


    public void addStation(String name) {
        // HashMap handles dynamic array no need to resize
        stationMap.putIfAbsent(name.toUpperCase(), new Station(name.toUpperCase()));
    }

    /**
     * V2: O(1) HashMap lookup.
     */
    public Station getStation(String name) {
        if (name == null) return null;
        return stationMap.get(name.toUpperCase());
    }

    public void addNewTrack(String start, String target, String line, String dir, double time) {
        Station s1 = getStation(start);
        Station s2 = getStation(target);
        if (s1 != null && s2 != null) {
            s1.addConnection(new Connection(s2, line, dir, time));
        }
    }

    public void addNewInterchange(String name, String from, String to, double time) {
        Station s = getStation(name);
        if (s != null) s.addInterchange(new Interchange(from, to, time));
    }

    // ENGINEER METHODS

    /**
     * Adds a delay to a track between two stations.
     */
    public void addDelayToTrack(String startName, String targetName, double delayMinutes) {
        Station start = getStation(startName);
        Station target = getStation(targetName);

        if (start == null) {
            System.out.println("Error: Start station '" + startName + "' not found.");
            return;
        }
        if (target == null) {
            System.out.println("Error: Target station '" + targetName + "' not found.");
            return;
        }

        for (Connection c : start.getConnections()) {
            if (c.getDestination().getName().equalsIgnoreCase(targetName)) {
                c.setDelayTime(delayMinutes);
                System.out.println("Success: Added " + delayMinutes + " min delay from "
                        + start.getName() + " to " + target.getName() + ".");
                return;
            }
        }
        System.out.println("Error: No direct track between " + startName + " and " + targetName + ".");
    }

    /**
     * Removes any existing delay from a track between two stations.
     */
    public void removeDelayFromTrack(String startName, String targetName) {
        Station start = getStation(startName);
        Station target = getStation(targetName);

        if (start == null) {
            System.out.println("Error: Start station '" + startName + "' not found.");
            return;
        }
        if (target == null) {
            System.out.println("Error: Target station '" + targetName + "' not found.");
            return;
        }

        for (Connection c : start.getConnections()) {
            if (c.getDestination().getName().equalsIgnoreCase(targetName)) {
                if (c.getDelayTime() == 0.0) {
                    System.out.println("Info: No delay exists on this track.");
                } else {
                    c.setDelayTime(0.0);
                    System.out.println("Success: Delay removed from "
                            + start.getName() + " to " + target.getName() + ".");
                }
                return;
            }
        }
        System.out.println("Error: No direct track between " + startName + " and " + targetName + ".");
    }

    /**
     * Opens or closes a track between two stations.
     */
    public void openOrCloseTrack(String startName, String targetName, boolean isOpen) {
        Station start = getStation(startName);
        if (start == null) {
            System.out.println("Error: Start station '" + startName + "' not found.");
            return;
        }
        for (Connection c : start.getConnections()) {
            if (c.getDestination().getName().equalsIgnoreCase(targetName)) {
                c.setOpenStatus(isOpen);
                System.out.println("Success: Track from " + startName + " to "
                        + targetName + " is now " + (isOpen ? "OPEN" : "CLOSED") + ".");
                return;
            }
        }
        System.out.println("Error: No direct track between " + startName + " and " + targetName + ".");
    }

    /**
     * Returns OPEN/CLOSED/Not Found for a track — used by engineer menu before changing status.
     */
    public String getTrackStatusString(String startName, String targetName) {
        Station start = getStation(startName);
        if (start == null) return "Not Found";
        for (Connection c : start.getConnections()) {
            if (c.getDestination().getName().equalsIgnoreCase(targetName)) {
                return c.isOpen() ? "OPEN" : "CLOSED";
            }
        }
        return "Not Found";
    }

    /**
     * Prints all tracks that currently have a delay.
     */
    public void printDelayStatus() {
        System.out.println("\n--- DELAY STATUS REPORT ---");
        boolean found = false;
        for (Station s : stationMap.values()) {
            for (Connection c : s.getConnections()) {
                if (c.getDelayTime() > 0) {
                    System.out.println(c.getLineName() + " (" + c.getDirection() + "): "
                            + s.getName() + " to " + c.getDestination().getName()
                            + " - " + c.getDelayTime() + " min delay");
                    found = true;
                }
            }
        }
        if (!found) System.out.println("No delays on the network.");
        System.out.println("---------------------------");
    }

    /**
     * Prints all tracks that are currently closed.
     */
    public void printClosureStatus() {
        System.out.println("\n--- CLOSURE STATUS REPORT ---");
        boolean found = false;
        for (Station s : stationMap.values()) {
            for (Connection c : s.getConnections()) {
                if (!c.isOpen()) {
                    System.out.println(c.getLineName() + " (" + c.getDirection() + "): "
                            + s.getName() + " to " + c.getDestination().getName() + " - CLOSED");
                    found = true;
                }
            }
        }
        if (!found) System.out.println("All tracks are currently OPEN.");
        System.out.println("-----------------------------");
    }

    // CUSTOMER METHOD: Station Information
    // V2: Collections.sort() O(n log n) vs V1 Bubble Sort O(n^2)

    public void displayStationInformation(String stationName) {
        long startTime = System.nanoTime();

        Station s = getStation(stationName);
        if (s == null) {
            System.out.println("Error: Station '" + stationName + "' not found.");
            return;
        }

        // V2: Copy into ArrayList then sort using Comparator — O(n log n)
        // V1: Bubble Sort was O(n^2)
        List<Connection> tracks = new ArrayList<>(s.getConnections());
        tracks.sort((c1, c2) -> {
            int lineComp = c1.getLineName().compareToIgnoreCase(c2.getLineName());
            if (lineComp != 0) return lineComp;
            return c1.getDestination().getName().compareToIgnoreCase(c2.getDestination().getName());
        });

        long endTime = System.nanoTime();
        double timeMs = (endTime - startTime) / 1_000_000.0;

        System.out.println("\n=======================================================");
        System.out.println("   LIVE DEPARTURES: " + s.getName().toUpperCase());
        System.out.println("=======================================================");

        String currentLine = "";
        for (Connection c : tracks) {
            if (!c.getLineName().equalsIgnoreCase(currentLine)) {
                currentLine = c.getLineName();
                System.out.println("\n[ " + currentLine.toUpperCase() + " LINE ]");
            }

            String dest = c.getDestination().getName().toUpperCase();

            if (!c.isOpen()) {
                System.out.printf("  %-30s %s%n", dest, "CLOSED");
                continue;
            }
            if (c.getDelayTime() > 5.0) {
                System.out.printf("  %-30s %s%n", dest, "SEVERE DELAYS");
                continue;
            }

            int t1 = (int)(Math.random() * 3);
            int t2 = t1 + (int)(Math.random() * 4) + 2;
            int t3 = t2 + (int)(Math.random() * 5) + 3;
            String t1Disp = (t1 == 0) ? "Due" : t1 + " min";
            System.out.printf("  %-30s %s, %d min, %d min%n", dest, t1Disp, t2, t3);
        }

        System.out.println("\n=======================================================");
        System.out.println("V2 Sort Time (Collections.sort): " + String.format("%.5f", timeMs) + " ms  | O(n log n)");
        System.out.println("V1 comparison: Bubble Sort was O(n^2)");
        System.out.println("=======================================================");
    }

    // CUSTOMER METHOD: View Stations on a Line
    // V2: TreeMap gives naturally sorted keys O(log n) insert

    public void displayStationsOnLine(String lineName) {
        System.out.println("\n--- Stations on the " + lineName + " Line (Sorted A-Z) ---");

        long sortStart = System.nanoTime();

        // V2: TreeMap keeps keys sorted automatically — O(log n) per insert
        // V1: Collected into array then Bubble Sorted — O(n^2)
        TreeMap<String, Station> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Station s : stationMap.values()) {
            for (Connection c : s.getConnections()) {
                if (c.getLineName().equalsIgnoreCase(lineName)) {
                    sorted.put(s.getName(), s);
                    break;
                }
            }
        }

        long sortEnd = System.nanoTime();
        double sortTimeMs = (sortEnd - sortStart) / 1_000_000.0;

        if (sorted.isEmpty()) {
            System.out.println("No stations found for line: " + lineName);
            return;
        }

        int count = 1;
        for (String name : sorted.keySet()) {
            System.out.println(count + "  " + name);
            count++;
        }

        System.out.println("-------------------------------------");
        System.out.println("V2 TreeMap Sort Time: " + String.format("%.5f", sortTimeMs) + " ms  | O(n log n)");
        System.out.println("V1 comparison: Bubble Sort was O(n^2)");
        System.out.println("-------------------------------------");
    }

    // CUSTOMER METHOD: Search Station on Line
    // V2: TreeMap.containsKey() — O(log n) vs V1 manual Binary Search O(log n)
    // Also shows HashMap O(1) lookup as alternative

    public void searchStationOnLine(String lineName, String stationName) {

        // Step 1: Build TreeMap of stations on this line — O(n log n)
        TreeMap<String, Station> lineStations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Station s : stationMap.values()) {
            for (Connection c : s.getConnections()) {
                if (c.getLineName().equalsIgnoreCase(lineName)) {
                    lineStations.put(s.getName(), s);
                    break;
                }
            }
        }

        if (lineStations.isEmpty()) {
            System.out.println("Error: No stations found for line '" + lineName + "'.");
            return;
        }

        int count = lineStations.size();

        // Step 2: TreeMap search — O(log n)
        long treeStart = System.nanoTime();
        boolean treeFound = lineStations.containsKey(stationName);
        long treeEnd = System.nanoTime();
        long treeNs = treeEnd - treeStart;

        // Step 3: HashMap search — O(1)
        // Build a HashMap of the same stations for direct comparison
        Map<String, Station> lineMap = new HashMap<>(lineStations);
        long hashStart = System.nanoTime();
        boolean hashFound = lineMap.containsKey(stationName.toUpperCase())
                || lineMap.containsKey(stationName);
        long hashEnd = System.nanoTime();
        long hashNs = hashEnd - hashStart;

        boolean found = treeFound || hashFound;
        String resultLabel = found ? "FOUND" : "NOT FOUND";

        System.out.println("\n=======================================================");
        System.out.println("  Search: \"" + stationName + "\" on " + lineName + " Line");
        System.out.println("  Total stations on this line: " + count);
        System.out.println("=======================================================");
        System.out.println("  RESULT: " + resultLabel);
        System.out.println("-------------------------------------------------------");
        System.out.printf("  %-28s %-15s %-15s%n", "", "TREEMAP", "HASHMAP");
        System.out.println("  -------------------------------------------------------");
        System.out.printf("  %-28s %-15s %-15s%n", "Complexity:", "O(log n)", "O(1)");
        System.out.printf("  %-28s %-15s %-15s%n", "Backed by:", "Red-Black Tree", "Hash Table");
        System.out.printf("  %-28s %-15s %-15s%n", "Keeps sorted order?", "Yes", "No");
        System.out.printf("  %-28s %-15d %-15d%n", "Time taken (ns):", treeNs, hashNs);
        System.out.println("  -------------------------------------------------------");
        System.out.println("  V1 comparison: Hand-coded Binary Search O(log n) after Bubble Sort O(n^2)");
        System.out.println("  V2 TreeMap: O(log n) insert keeps data sorted — no separate sort step needed");
        System.out.println("  V2 HashMap: O(1) lookup — fastest possible search");

        if (found) {
            Station foundStation = lineStations.get(stationName) != null
                    ? lineStations.get(stationName)
                    : lineStations.floorEntry(stationName) != null
                      ? lineStations.floorEntry(stationName).getValue()
                      : null;

            // Try case-insensitive get
            for (Map.Entry<String, Station> e : lineStations.entrySet()) {
                if (e.getKey().equalsIgnoreCase(stationName)) {
                    foundStation = e.getValue();
                    break;
                }
            }

            if (foundStation != null) {
                System.out.println("\n  Departures from " + foundStation.getName()
                        + " on " + lineName + " line:");
                boolean hasDep = false;
                for (Connection c : foundStation.getConnections()) {
                    if (c.getLineName().equalsIgnoreCase(lineName)) {
                        String status = !c.isOpen() ? " [CLOSED]"
                                : (c.getDelayTime() > 0 ? " [+" + c.getDelayTime() + " min delay]" : "");
                        System.out.printf("    -> %-28s (%s)  %.2f min%s%n",
                                c.getDestination().getName(),
                                c.getDirection(),
                                c.getNormalTime(),
                                status);
                        hasDep = true;
                    }
                }
                if (!hasDep) System.out.println("    No departures on this line.");
            }
        }
        System.out.println("=======================================================");
    }

    // CUSTOMER METHOD: Find Fastest Route
    // V2: Dijkstra with PriorityQueue — O((E + V) log N)
    // V1: Dijkstra with array scan    — O(N^2)

    public void findFastestRoute(String startName, String targetName) {
        if (startName.equalsIgnoreCase(targetName)) {
            System.out.println("Error: Start and destination are the same station.");
            return;
        }

        Station startNode  = getStation(startName);
        Station targetNode = getStation(targetName);

        if (startNode == null) {
            System.out.println("Error: Start station '" + startName + "' not found.");
            return;
        }
        if (targetNode == null) {
            System.out.println("Error: Destination station '" + targetName + "' not found.");
            return;
        }

        // START BENCHMARK
        long startTime = System.nanoTime();

        // V2 Dijkstra — HashMap for distances, PriorityQueue for next node selection
        Map<Station, Double>  shortestTimes    = new HashMap<>();
        Map<Station, Station> previousStation  = new HashMap<>();
        Map<Station, String>  previousLine     = new HashMap<>();
        Map<Station, String>  previousDir      = new HashMap<>();
        Map<Station, Double>  legTravelTime    = new HashMap<>();
        Map<Station, Double>  interchangeTime  = new HashMap<>();

        // Initialise all distances to infinity
        for (Station s : stationMap.values()) {
            shortestTimes.put(s, Double.MAX_VALUE);
        }

        shortestTimes.put(startNode, 0.0);
        previousLine.put(startNode, "");

        // PriorityQueue automatically gives us the lowest-cost unvisited node
        // V1 had to scan the entire array each time — O(V) per iteration
        // PriorityQueue does it in O(log V)
        PriorityQueue<NodeTime> pq = new PriorityQueue<>(Comparator.comparingDouble(nt -> nt.time));
        pq.add(new NodeTime(startNode, 0.0));

        while (!pq.isEmpty()) {
            NodeTime current = pq.poll();
            Station u = current.node;

            if (u.equals(targetNode)) break;

            // Skip if we already found a better path to u
            if (current.time > shortestTimes.get(u)) continue;

            for (Connection track : u.getConnections()) {
                if (!track.isOpen()) continue;

                Station v = track.getDestination();
                if (!shortestTimes.containsKey(v)) continue;

                String lineArrivedOn = previousLine.getOrDefault(u, "");
                double walkPenalty   = u.getInterchangeTime(lineArrivedOn, track.getLineName());
                double newTime       = shortestTimes.get(u) + walkPenalty + track.getTotalTime();

                if (newTime < shortestTimes.get(v)) {
                    shortestTimes.put(v, newTime);
                    previousStation.put(v, u);
                    previousLine.put(v, track.getLineName());
                    previousDir.put(v, track.getDirection());
                    legTravelTime.put(v, track.getTotalTime());
                    interchangeTime.put(v, walkPenalty);
                    pq.add(new NodeTime(v, newTime));
                }
            }
        }

        // STOP BENCHMARK
        long endTime = System.nanoTime();
        double executionTimeMs = (endTime - startTime) / 1_000_000.0;

        // No route found
        if (shortestTimes.get(targetNode) == Double.MAX_VALUE) {
            System.out.println("\nNo route found between " + startName + " and " + targetName + ".");
            System.out.println("Some tracks on this route may be closed.");
            return;
        }

        // Reconstruct path using Collections — trace backwards then reverse
        List<Station> path = new ArrayList<>();
        Station curr = targetNode;
        while (curr != null) {
            path.add(curr);
            curr = previousStation.get(curr);
        }
        Collections.reverse(path);

        // Print route in same spec format as V1
        System.out.println("\n=======================================================");
        System.out.println("  Route: " + startName + " to " + targetName);
        System.out.println("=======================================================");

        int stepNumber = 1;
        String firstLine = previousLine.getOrDefault(path.get(1), "");
        String firstDir  = previousDir.getOrDefault(path.get(1), "");

        System.out.println("(" + stepNumber + ") Start: " + path.get(0).getName()
                + ", " + firstLine + " (" + firstDir + ")");
        stepNumber++;

        for (int i = 1; i < path.size(); i++) {
            Station from = path.get(i - 1);
            Station to   = path.get(i);

            String lineName = previousLine.getOrDefault(to, "");
            String dir      = previousDir.getOrDefault(to, "");
            double travelT  = legTravelTime.getOrDefault(to, 0.0);
            double icTime   = interchangeTime.getOrDefault(to, 0.0);

            // Print interchange step if line changed
            if (icTime > 0.0) {
                String prevLine = previousLine.getOrDefault(from, "");
                String prevDir  = previousDir.getOrDefault(from, "");
                if (prevLine.isEmpty()) {
                    prevLine = firstLine;
                    prevDir  = firstDir;
                }
                System.out.println("(" + stepNumber + ") Change: " + from.getName()
                        + "  " + prevLine + " (" + prevDir + ")"
                        + " to " + lineName + " (" + dir + ")  "
                        + String.format("%.2f", icTime) + "min");
                stepNumber++;
            }

            if (i == path.size() - 1) {
                System.out.println("(" + stepNumber + ") " + lineName + " (" + dir + "): "
                        + from.getName() + " to " + to.getName() + "  "
                        + String.format("%.2f", travelT) + "min");
                stepNumber++;
                System.out.println("(" + stepNumber + ") End: " + to.getName()
                        + ", " + lineName + " (" + dir + ")");
            } else {
                System.out.println("(" + stepNumber + ") " + lineName + " (" + dir + "): "
                        + from.getName() + " to " + to.getName() + "  "
                        + String.format("%.2f", travelT) + "min");
                stepNumber++;
            }
        }

        System.out.println("-------------------------------------------------------");
        System.out.printf("Total Journey Time: %.2f minutes%n", shortestTimes.get(targetNode));
        System.out.println("-------------------------------------------------------");
        System.out.println("V2 Algorithm Execution Time: " + String.format("%.4f", executionTimeMs) + " ms");
        System.out.println("V2 Complexity: O((E + V) log V) with PriorityQueue");
        System.out.println("V1 Complexity: O(V^2) with array scan");
        System.out.println("=======================================================");
    }

    // HELPER: NodeTime for PriorityQueue

    private static class NodeTime {
        Station node;
        double  time;
        NodeTime(Station n, double t) {
            this.node = n;
            this.time = t;
        }
    }
}