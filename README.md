# TfL Network Manager — Version 2

Version 2 is a modified version of V1 where all hand-coded data structures and algorithms have been replaced with Java Library classes (`java.util.*`). The UI, CSV loading, and menu structure are identical to V1. Only the internals differ.

---


## V1 → V2 Upgrade Summary

| Component | V1 (Hand-coded) | V2 (Java Library) | Complexity Change |
|---|---|---|---|
| Station storage | Dynamic array (manual resize) | `HashMap<String, Station>` | O(n) lookup → O(1) |
| Station lookup | Linear Search | `HashMap.get()` | O(n) → O(1) |
| Connections per station | Hand-coded array | `ArrayList<Connection>` | Same O(1) add |
| Departures sort | Bubble Sort | `Collections.sort()` with Comparator | O(n²) → O(n log n) |
| Stations on line sort | Bubble Sort | `Arrays.sort()` — Quicksort | O(n²) → O(n log n) |
| Station search | Linear + Binary Search | `TreeMap` + `HashMap` | O(n)/O(log n) → O(log n)/O(1) |
| Dijkstra next node | Array scan O(V) per step | `PriorityQueue` O(log V) per step | O(V²) → O((E+V) log V) |
| Path reconstruction | Manual index reversal | `ArrayList` + `Collections.reverse()` | Cleaner, same O(n) |

---

## Data Structures (Java Library)

### HashMap — Station Lookup
`HashMap<String, Station>` stores all stations with the station name as the key.

- **V1** used a plain `Station[]` array scanned with linear search — O(n)
- **V2** uses `HashMap.get()` — O(1) average case
- Backed internally by a **hash table** — computes a hash of the key to find the bucket directly
- `putIfAbsent()` handles duplicate station names automatically

```
Add station:    O(1) average
Lookup by name: O(1) average
Space:          O(n)
```

### ArrayList — Connections and Interchanges per Station
`ArrayList<Connection>` and `ArrayList<Interchange>` replace hand-coded auto-growing arrays in `Station.java`.

- **V1** manually doubled array size when full and copied all elements
- **V2** delegates this to `ArrayList` which handles growth internally using the same doubling strategy
- Enhanced for-loop (`for (Connection c : connections)`) replaces index-based loops

```
Add element:     O(1) amortised
Access by index: O(1)
Space:           O(n)
```

### PriorityQueue (Min-Heap) — Dijkstra's Algorithm
`PriorityQueue<NodeTime>` ordered by `Comparator.comparingDouble(nt -> nt.time)`.

- **V1** scanned all unvisited stations to find the minimum — O(V) per step
- **V2** uses a `PriorityQueue` backed by a **binary min-heap** — O(log V) per poll/add
- A min-heap is a complete binary tree where every parent node is smaller than its children
- `poll()` always returns the station with the current shortest known time

```
Add to queue (offer): O(log V)
Get minimum (poll):   O(log V)
Overall Dijkstra:     O((E + V) log V)  vs  O(V²) in V1
```

### TreeMap — Station Search (Red-Black Tree)
`TreeMap<String, Station>` with `String.CASE_INSENSITIVE_ORDER` used in `searchStationOnLine()`.

- Backed internally by a **Red-Black Tree** — a self-balancing Binary Search Tree
- Similar to AVL Tree: both guarantee O(log n) operations by keeping the tree balanced
- AVL Tree rebalances more aggressively (stricter height balance) — faster lookups
- Red-Black Tree rebalances less strictly — faster insertions/deletions
- Java chose Red-Black for `TreeMap` because real-world data has more inserts than lookups
- Keys are always kept in sorted order — no separate sort step needed

```
Insert:           O(log n)  — rebalances on insert
Lookup:           O(log n)
Iteration order:  Always alphabetical (A-Z)
```

---

## Algorithms (Java Library)

### 1. HashMap Lookup — O(1)
Used in `getStation()`. Replaces V1's linear search.

The HashMap computes `key.hashCode()`, maps it to an internal bucket, and retrieves the value directly — no loop needed.

```
V1: Linear Search   — up to n comparisons
V2: HashMap.get()   — 1 hash computation, direct access
```

---

### 2. Arrays.sort() — Dual-Pivot Quicksort — O(n log n)
Used in `displayStationsOnLine()` to sort station names alphabetically.

`Arrays.sort(String[], Comparator)` uses **Dual-Pivot Quicksort**:
- Selects two pivot elements instead of one
- Partitions the array into three parts: less than left pivot, between pivots, greater than right pivot
- Recursively sorts each partition
- Proven to be faster in practice than classic single-pivot Quicksort

```
Best case:    O(n log n)
Average case: O(n log n)
Worst case:   O(n²)  — rare with dual-pivot strategy
Space:        O(log n) — recursive call stack
V1 comparison: Bubble Sort was O(n²) worst and average case
```

---

### 3. Collections.sort() — TimSort — O(n log n)
Used in `displayStationInformation()` to sort departures by line name then destination.

`Collections.sort()` uses **TimSort** — a hybrid of Merge Sort and Insertion Sort:
- Splits the list into small "runs"
- Uses Insertion Sort on each small run (very fast on nearly-sorted data)
- Merges runs using Merge Sort
- Guarantees O(n log n) in all cases — no worst-case degradation like Quicksort

```
Best case:    O(n)      — already sorted data
Average case: O(n log n)
Worst case:   O(n log n) — guaranteed
Space:        O(n)
Stable sort:  Yes — equal elements keep their original order
V1 comparison: Bubble Sort was O(n²) in all cases
```

---

### 4. TreeMap — O(log n) Search
Used in `searchStationOnLine()`. Backed by a Red-Black Tree (self-balancing BST).

`containsKey()` traverses the tree from root, going left if target < current node, right if target > current node — same logic as Binary Search but on a tree structure.

```
V1: Manual Binary Search after Bubble Sort  — O(n²) sort + O(log n) search
V2: TreeMap insert + containsKey()          — O(log n) insert (no separate sort) + O(log n) search
```

Side-by-side comparison printed at runtime:

```
                         TREEMAP         HASHMAP
Complexity:              O(log n)        O(1)
Backed by:               Red-Black Tree  Hash Table
Keeps sorted order?      Yes             No
```

---

### 5. Dijkstra's Algorithm with PriorityQueue — O((E + V) log V)
Used in `findFastestRoute()`. Same algorithm as V1 but the most expensive step is replaced.

**The key difference:**

V1 — finding the next unvisited station with the shortest time:
```java
// Scan entire array every iteration — O(V)
for (int i = 0; i < totalStations; i++) {
    if (!visited[i] && times[i] < min) { min = times[i]; }
}
// Total: O(V) × O(V iterations) = O(V²)
```

V2 — PriorityQueue does it automatically:
```java
NodeTime current = pq.poll(); // O(log V) — heap extracts minimum
// Total: O(E + V) × O(log V) = O((E + V) log V)
```

The `PriorityQueue` is a **binary min-heap**. Every `poll()` removes the root (minimum), then re-heapifies in O(log V). Every `add()` bubbles up in O(log V).

```
V1 Dijkstra: O(V²)              — array scan
V2 Dijkstra: O((E + V) log V)   — PriorityQueue
```

Execution time is printed after every route search for direct benchmarking comparison.

---

## Function Reference

| Method | Class | What it does |
|---|---|---|
| `findFastestRoute(start, end)` | TfLNetwork | Dijkstra with PriorityQueue — prints step-by-step itinerary |
| `searchStationOnLine(line, station)` | TfLNetwork | TreeMap + HashMap search, prints side-by-side comparison |
| `displayStationsOnLine(line)` | TfLNetwork | Arrays.sort() Quicksort A-Z, prints with sort time |
| `displayStationInformation(station)` | TfLNetwork | Collections.sort() TimSort by line+destination |
| `addDelayToTrack(start, end, mins)` | TfLNetwork | Sets delay on a connection |
| `removeDelayFromTrack(start, end)` | TfLNetwork | Clears delay on a connection |
| `openOrCloseTrack(start, end, bool)` | TfLNetwork | Opens or closes a connection |
| `printDelayStatus()` | TfLNetwork | Lists all connections with active delays |
| `printClosureStatus()` | TfLNetwork | Lists all closed connections |
| `getStation(name)` | TfLNetwork | O(1) HashMap lookup |
| `addStation(name)` | TfLNetwork | putIfAbsent into HashMap |
| `addConnection(c)` | Station | ArrayList.add() |
| `getInterchangeTime(from, to)` | Station | Enhanced for-loop over ArrayList |
| `getTotalTime()` | Connection | normalTime + delayTime |

---

## Benchmarking Summary

| Algorithm | Java Class | Complexity | Used For |
|---|---|---|---|
| HashMap lookup | `HashMap.get()` | O(1) | Station lookup |
| Quicksort | `Arrays.sort()` | O(n log n) avg | Stations on line A-Z |
| TimSort | `Collections.sort()` | O(n log n) guaranteed | Departures sort |
| Red-Black Tree search | `TreeMap.containsKey()` | O(log n) | Station search on line |
| Min-Heap extraction | `PriorityQueue.poll()` | O(log V) | Dijkstra next node |
| Dijkstra (PriorityQueue) | `PriorityQueue` | O((E+V) log V) | Fastest route — V2 |
| Dijkstra (array scan) | Hand-coded | O(V²) | Fastest route — V1 |