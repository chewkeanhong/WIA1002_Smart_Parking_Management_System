# 🅿️ SmartPark — Smart Parking Management System

> **WIA1002 Data Structures — Group Assignment**

A desktop **Smart Parking Management System** built in **Java Swing**, where every
core feature is powered by a **data structure implemented from scratch** (no
`java.util` collections for the assignment-critical parts). The app simulates a
real car park: vehicles queue at the gate, get assigned the nearest free slot,
can be searched and retrieved instantly, and routed to their bay on a map.

---

## 👥 Authors

> **WIA1002 Data Structures — Group Assignment**, Faculty of Computer Science &
> Information Technology, University of Malaya.

| Name | Matrics Number |
|------|--------|
| Chew Kean Hong | 25006697 |
| Tey Yong Zhun | 25006379 |
| Edrian Tan | 25006656 |
| Soo Kang Shi | 25005517 |
| Lee Wuey Yong | 25005996 |
---

## ✨ Features

| Module | What it does | Data Structure |
|--------|--------------|----------------|
| 🏠 **Dashboard** | Live overview of occupancy, queue length and system stats | — |
| 🚗 **Entry / Exit** | Vehicles arrive and are processed in arrival order; mistakes can be undone | **FIFO Queue** + **LIFO Undo Stack** (linked list / array) |
| ⭐ **Slot Priority** | Assigns the **nearest available slot** to each vehicle | **Binary Min-Heap** (priority queue) |
| 🔍 **Search** | Look up / add / remove a vehicle by license plate; list sorted alphabetically | **Self-balancing AVL Tree** |
| 🗺️ **Routes** | Computes the shortest path from the gate to a parking bay | **Weighted Graph + Dijkstra** |
| 📋 **Logs** | Chronological activity feed of every system action | — |
| 🧾 **Management** | Stores and manages all parking records dynamically | **Custom Generic Linked List** |
| ⚡ **Retrieval** | O(1) lookup of vehicles and slots by key | **Custom Hash Table** (separate chaining) |
| 👤 **User** | User-facing view: join the queue and check position | — |

---

## 🧠 Data Structures Implemented

All of these are **hand-written** under [`src/`](src/):

| Structure | File | Key operations & complexity |
|-----------|------|------------------------------|
| **Min-Heap** | [`assignment/SlotMinHeap.java`](src/assignment/SlotMinHeap.java) | insert `O(log n)`, poll-min `O(log n)`, peek `O(1)` |
| **FIFO Queue** | [`gate_control/EntryQueue.java`](src/gate_control/EntryQueue.java) | enqueue / dequeue / peek `O(1)` |
| **Undo Stack (LIFO)** | [`gate_control/UndoStack.java`](src/gate_control/UndoStack.java) | push / pop / peek `O(1)` amortised |
| **Generic Linked List** | [`management/RecordLinkedList.java`](src/management/RecordLinkedList.java) | dynamic, resizable record storage |
| **Hash Table** | [`retrieval/HashMap.java`](src/retrieval/HashMap.java) | put / get / remove `O(1)` avg, auto-resize at 0.75 load |
| **AVL Tree** | [`search/VehicleBST.java`](src/search/VehicleBST.java) | insert / search / delete `O(log n)`, in-order sort `O(n)` |
| **Graph + Dijkstra** | [`navigation/RouteGraph.java`](src/navigation/RouteGraph.java), [`navigation/DijkstraPathfinder.java`](src/navigation/DijkstraPathfinder.java) | shortest-path routing |

---

## 📂 Project Structure

The code is organised into **packages by responsibility** — each data-structure
module is self-contained, and the `ui` package wires them together into the Swing
interface. The dependency flow is roughly:

```
ui  →  (assignment · gate_control · management · retrieval · search · navigation)  →  models
```

```
WIA1002_Smart_Parking_Management_System/
│
├── src/                              # All Java source code
│   │
│   ├── core/                         # ▶ Program entry point
│   │   └── SmartParkingApp.java      #   main() — boots the Swing app
│   │
│   ├── models/                       # ▶ Domain objects (shared everywhere)
│   │   ├── Vehicle.java              #   A car: license plate, type, timestamps
│   │   ├── ParkingSlot.java          #   A bay: id, distance-to-gate, occupied?
│   │   └── ParkingMap.java           #   Tracks which slots are occupied
│   │
│   ├── assignment/                   # ▶ Nearest-slot allocation  (MIN-HEAP)
│   │   ├── SlotMinHeap.java          #   Binary min-heap ordered by distance
│   │   └── PriorityAllocator.java    #   Facade: assign nearest free slot
│   │
│   ├── gate_control/                 # ▶ Gate entry/exit  (QUEUE + STACK)
│   │   ├── EntryQueue.java           #   FIFO queue of arriving vehicles
│   │   ├── UndoStack.java            #   LIFO stack of actions to undo
│   │   └── GateProcessor.java        #   Orchestrates queue + undo together
│   │
│   ├── management/                   # ▶ Record storage  (LINKED LIST)
│   │   ├── RecordLinkedList.java     #   Custom generic singly-linked list
│   │   └── RecordManager.java        #   CRUD over the parking records
│   │
│   ├── retrieval/                    # ▶ Fast lookup  (HASH TABLE)
│   │   ├── HashMap.java              #   Custom hash table (separate chaining)
│   │   └── FastAccessor.java         #   O(1) lookup by plate / slot id
│   │
│   ├── search/                       # ▶ Vehicle search  (AVL TREE)
│   │   ├── TreeNode.java             #   A node of the balanced tree
│   │   ├── VehicleBST.java           #   Self-balancing AVL tree (by plate)
│   │   └── SearchEngine.java         #   Facade over the tree
│   │
│   ├── navigation/                   # ▶ Shortest-path routing  (GRAPH)
│   │   ├── RouteGraph.java           #   Weighted graph of nodes/bays
│   │   └── DijkstraPathfinder.java   #   Dijkstra shortest-path algorithm
│   │
│   └── ui/                           # ▶ Swing user interface
│       ├── MainFrame.java            #   Admin window + sidebar navigation
│       ├── UserFrame.java            #   User-facing window
│       ├── UITheme.java              #   Shared colors, fonts, components
│       ├── ActivityLog.java          #   In-memory log of system actions
│       ├── DashboardPanel.java       #   Overview & stats screen
│       ├── GateControlPanel.java     #   Entry / Exit screen
│       ├── AssignmentPanel.java      #   Slot Priority screen
│       ├── SearchPanel.java          #   Search screen
│       ├── NavigationPanel.java      #   Routes screen
│       ├── LogsPanel.java            #   Logs screen
│       ├── ManagementPanel.java      #   Management screen
│       ├── RetrievalPanel.java       #   Retrieval screen
│       └── UserPanel.java            #   User screen
│
├── data/                             # Runtime data files
├── images/                           # Image assets (icons, map art)
├── audio/                            # Audio assets
├── docs/                             # Assignment brief (PDF) & documentation
├── out/  ·  bin/                     # Compiled .class output (generated)
└── README.md
```

**How a package maps to its data structure**

| Package | Data Structure | Core class |
|---------|----------------|-----------|
| `assignment` | Min-Heap (priority queue) | `SlotMinHeap` |
| `gate_control` | Queue + Stack | `EntryQueue`, `UndoStack` |
| `management` | Linked List | `RecordLinkedList` |
| `retrieval` | Hash Table | `HashMap` |
| `search` | AVL Tree | `VehicleBST` |
| `navigation` | Graph + Dijkstra | `RouteGraph`, `DijkstraPathfinder` |

---

## 🚀 Getting Started

### Prerequisites
- **JDK 17 or newer** (developed and tested on **JDK 25**)
- Any OS with a desktop environment (Windows / macOS / Linux)

### Build & Run

From the project root:

**Windows (PowerShell)**
```powershell
# Compile all sources into the out/ folder
javac -d out (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object FullName)

# Run the application
java -cp out core.SmartParkingApp
```

**macOS / Linux (bash)**
```bash
# Compile all sources into the out/ folder
javac -d out $(find src -name '*.java')

# Run the application
java -cp out core.SmartParkingApp
```

> 💡 You can also open the project in **IntelliJ IDEA / Eclipse / VS Code**,
> mark `src` as the sources root, and run the `main` method in
> [`core/SmartParkingApp.java`](src/core/SmartParkingApp.java).

---

## 🕹️ Usage

1. Launch the app — the **SmartPark** window opens maximised.
2. Use the **left sidebar** to switch between modules.
3. Typical flow:
   - **Entry / Exit** → a vehicle arrives and joins the queue.
   - **Slot Priority** → the nearest free slot is popped from the min-heap and assigned.
   - **Routes** → view the shortest path from the gate to that slot.
   - **Search / Retrieval** → find any vehicle by plate in `O(log n)` or `O(1)`.
   - **Logs** → review everything that happened.
4. The **Reset All** button (Dashboard) clears all records, occupancy and queues.

---

## 📄 License

This project was created for academic purposes as part of the WIA1002 coursework.
