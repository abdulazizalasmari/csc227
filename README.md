# CPU Scheduler Simulator

A comprehensive Java-based CPU Scheduler simulation that implements multiple scheduling algorithms with multithreading support.

## Project Overview

This program simulates a CPU scheduler for an operating system, implementing three different scheduling algorithms:
1. **Shortest Job First (SJF)** - Non-Preemptive
2. **Round-Robin (RR)** - Quantum = 6ms
3. **Priority Scheduling** - With aging to prevent starvation (1=Lowest, 128=Highest)

## Features

- **Multi-threaded Architecture**: Uses separate threads for file reading and job loading
- **Memory Management**: Simulates 2048 MB of system memory with allocation/deallocation
- **System Calls**: Comprehensive simulation of OS system calls for:
  - Process Control (fork, exec, exit, wait, kill)
  - Information Maintenance (getpid, getProcessInfo, getStatistics)
  - Memory Management (malloc, free, sbrk)
- **Starvation Detection**: Identifies and handles process starvation
- **Aging Mechanism**: Priority scheduling includes aging to prevent starvation
- **Visual Output**: Gantt charts and detailed execution timelines
- **Statistics**: Comprehensive analysis including waiting time and turnaround time
- **Comparison Tool**: Compare all scheduling algorithms side-by-side

## System Requirements

- Java JDK 8 or higher
- Any operating system (Windows, macOS, Linux)

## File Structure

```
csc227/
├── CPUSchedulerSimulator.java  # Main program with user interface
├── PCB.java                     # Process Control Block
├── ProcessState.java            # (enum defined in PCB.java)
├── JobQueue.java                # Thread-safe job queue
├── ReadyQueue.java              # Thread-safe ready queue
├── MemoryManager.java           # Memory allocation manager
├── FileReaderThread.java        # Thread for reading job file
├── JobLoaderThread.java         # Thread for loading jobs to ready queue
├── SystemCalls.java             # System call simulation
├── Scheduler.java               # Base scheduler class
├── SJFScheduler.java            # SJF algorithm implementation
├── RoundRobinScheduler.java     # Round-Robin algorithm implementation
├── PriorityScheduler.java       # Priority scheduling with aging
├── job.txt                      # Input file with process information
└── README.md                    # This file
```

## Compilation

To compile all Java files:

```bash
javac *.java
```

Or compile individually:

```bash
javac PCB.java
javac JobQueue.java ReadyQueue.java
javac MemoryManager.java
javac SystemCalls.java
javac FileReaderThread.java JobLoaderThread.java
javac Scheduler.java
javac SJFScheduler.java RoundRobinScheduler.java PriorityScheduler.java
javac CPUSchedulerSimulator.java
```

## Execution

Run the program:

```bash
java CPUSchedulerSimulator
```

## Input File Format

The program reads process information from `job.txt` with the following format:

```
processId:burstTime:priority;memoryRequired
```

**Example:**
```
1:25:4;500
2:13:3;700
3:20:3;100
```

Where:
- `processId`: Unique identifier (integer)
- `burstTime`: CPU burst time in milliseconds
- `priority`: Priority level (1=Lowest, 128=Highest)
- `memoryRequired`: Memory needed in MB

**Constraints:**
- Maximum 30 processes
- Total memory: 2048 MB
- All processes arrive at time 0

## Usage

1. **Start the program**: Run `java CPUSchedulerSimulator`
2. **Select job file**: Press Enter to use default `job.txt` or specify another file
3. **Choose from menu**:
   - Option 1: Run SJF scheduling
   - Option 2: Run Round-Robin scheduling
   - Option 3: Run Priority scheduling with aging
   - Option 4: Run all algorithms sequentially
   - Option 5: Compare all algorithms
   - Option 6: Exit

## Menu Options

### Option 1-3: Run Individual Algorithms
- Displays detailed execution logs
- Shows Gantt chart
- Provides execution timeline
- Calculates statistics (waiting time, turnaround time)
- Identifies starved processes

### Option 4: Run All Algorithms
- Executes all three algorithms sequentially
- Shows complete output for each algorithm

### Option 5: Compare Algorithms
- Runs all algorithms without verbose logging
- Displays side-by-side comparison table
- Identifies best performing algorithm

## Example Output

```
======================================================================
GANTT CHART:
----------------------------------------------------------------------
| P2   | P3   | P1   |
0      13     33     58

EXECUTION TIMELINE:
Process    Start Time      End Time        Execution Duration  
----------------------------------------------------------------------
P2         0               13              13 ms
P3         13              33              20 ms
P1         33              58              25 ms

PROCESS DETAILS
----------------------------------------------------------------------
Process    Burst Time   Priority    Waiting Time    Turnaround Time
----------------------------------------------------------------------
P2         13           3           0               13
P3         20           3           13              33
P1         25           4           33              58
----------------------------------------------------------------------

STATISTICS SUMMARY:
  Total Processes: 3
  Average Waiting Time: 15.33 ms
  Average Turnaround Time: 34.67 ms
======================================================================
```

## Threading Architecture

The program creates **3 threads**:

1. **Main Thread**: 
   - Handles user interface
   - Coordinates scheduling algorithms
   - Manages menu system

2. **FileReaderThread**:
   - Reads processes from job.txt
   - Creates PCB objects
   - Populates job queue
   - Runs independently from main execution

3. **JobLoaderThread**:
   - Monitors memory availability
   - Loads jobs from job queue to ready queue
   - Ensures memory constraints are met
   - Tracks degree of multiprogramming

## Scheduling Algorithms

### 1. Shortest Job First (SJF)
- **Type**: Non-preemptive
- **Selection Criteria**: Process with shortest burst time
- **Tie-Breaker**: Process with lower ID
- **Features**: Starvation detection based on degree of multiprogramming

### 2. Round-Robin (RR)
- **Type**: Preemptive
- **Time Quantum**: 6 milliseconds
- **Selection Criteria**: FIFO order
- **Features**: Fair CPU allocation, no starvation

### 3. Priority Scheduling
- **Type**: Non-preemptive
- **Selection Criteria**: Highest priority (128=Highest, 1=Lowest)
- **Tie-Breaker**: Process with lower ID
- **Features**: 
  - Starvation detection
  - Aging mechanism (increments priority by 1)
  - Prevents indefinite postponement

## Starvation Detection

Both SJF and Priority scheduling implement starvation detection:
- **Threshold**: Degree of multiprogramming at time of acceptance to ready queue
- **Detection**: Process waited longer than threshold
- **Mitigation** (Priority only): Aging increases priority to ensure eventual execution

## Memory Management

- **Total Memory**: 2048 MB
- **Allocation**: First-come, first-served from job queue
- **Deallocation**: When process completes
- **Constraint**: Jobs only loaded to ready queue if sufficient memory available

## System Assumptions

1. All processes arrive at time 0
2. Context switching time is 0 ms
3. No I/O operations
4. Single CPU system
5. No process dependencies
6. Maximum 30 processes

## Testing

To test with the provided sample:
```bash
java CPUSchedulerSimulator
# Press Enter to use job.txt
# Select option 5 to compare all algorithms
```

To test with more processes, edit `job.txt` and uncomment additional test cases.

## Troubleshooting

**Problem**: File not found error  
**Solution**: Ensure `job.txt` is in the same directory as compiled `.class` files

**Problem**: Compilation errors  
**Solution**: Ensure you have JDK 8 or higher installed
```bash
java -version
```

**Problem**: Out of memory errors  
**Solution**: Reduce total memory requirement in job.txt (must be ≤ 2048 MB)

## Project Structure Diagram

```
┌─────────────────────────────────────────────────────┐
│         CPUSchedulerSimulator (Main)                │
│         - User Interface                            │
│         - Menu System                               │
└──────────────────┬──────────────────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
┌───────▼──────────┐  ┌──────▼──────────┐
│ FileReaderThread │  │ JobLoaderThread │
│ - Read job.txt   │  │ - Check memory  │
│ - Create PCBs    │  │ - Load to ready │
└────────┬─────────┘  └────────┬────────┘
         │                     │
         ▼                     ▼
    ┌────────┐           ┌──────────┐
    │JobQueue│───────────▶ReadyQueue│
    └────────┘           └─────┬────┘
                               │
                ┌──────────────┴──────────────┐
                │                             │
         ┌──────▼─────────┐          ┌───────▼────────┐
         │   Scheduler     │          │ MemoryManager  │
         │   (Abstract)    │          │ - Allocate     │
         └────────┬────────┘          │ - Deallocate   │
                  │                   └────────────────┘
      ┌───────────┼───────────┐
      │           │           │
┌─────▼────┐ ┌───▼──────┐ ┌─▼─────────┐
│   SJF    │ │Round-Robin│ │ Priority   │
│Scheduler │ │ Scheduler │ │ Scheduler  │
└──────────┘ └───────────┘ └────────────┘
```

## Authors

Abdulaziz Alasmari 443100261
Yaqoub Alnajrani 443100407
## Course Information

- **Course**: CSC227
- **Project**: CPU Scheduler Simulation
- **Type**: Group Project (up to 3 students)

## License

This is an academic project for educational purposes.

## Acknowledgments

- Project requirements provided by course instructor
- Java documentation and threading concepts from Oracle Java documentation
- Operating System concepts from course materials
