/**
 * Process Control Block (PCB) class
 * Contains all information needed to identify and manage a process
 */
public class PCB {
    // Process identification
    private int processId;
    
    // Process state: NEW, READY, RUNNING, WAITING, TERMINATED
    private ProcessState state;
    
    // CPU scheduling information
    private int burstTime;           // Total burst time required
    private int remainingTime;       // Remaining burst time
    private int priority;            // Priority (1=Lowest, 128=Highest)
    private int originalPriority;    // Store original priority for aging
    
    // Memory management
    private int memoryRequired;      // Memory required in MB
    
    // Process statistics
    private int arrivalTime;         // Time process arrived (all arrive at 0)
    private int startTime;           // Time process first started execution
    private int completionTime;      // Time process completed
    private int waitingTime;         // Total waiting time
    private int turnaroundTime;      // Total turnaround time
    private int responseTime;        // Time from arrival to first execution
    
    // Aging and starvation detection
    private int timeInReadyQueue;    // Time spent waiting in ready queue
    private boolean hasStarted;      // Has the process started execution
    private boolean starved;         // Did the process experience starvation
    private int degreeOfMultiprogramming; // For starvation detection
    
    /**
     * Constructor for PCB
     */
    public PCB(int processId, int burstTime, int priority, int memoryRequired) {
        this.processId = processId;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.originalPriority = priority;
        this.memoryRequired = memoryRequired;
        
        // Initialize state and statistics
        this.state = ProcessState.NEW;
        this.arrivalTime = 0;
        this.startTime = -1;
        this.completionTime = -1;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.responseTime = -1;
        this.timeInReadyQueue = 0;
        this.hasStarted = false;
        this.starved = false;
        this.degreeOfMultiprogramming = 0;
    }
    
    // Getters
    public int getProcessId() { return processId; }
    public ProcessState getState() { return state; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getPriority() { return priority; }
    public int getOriginalPriority() { return originalPriority; }
    public int getMemoryRequired() { return memoryRequired; }
    public int getArrivalTime() { return arrivalTime; }
    public int getStartTime() { return startTime; }
    public int getCompletionTime() { return completionTime; }
    public int getWaitingTime() { return waitingTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public int getResponseTime() { return responseTime; }
    public int getTimeInReadyQueue() { return timeInReadyQueue; }
    public boolean hasStarted() { return hasStarted; }
    public boolean isStarved() { return starved; }
    public int getDegreeOfMultiprogramming() { return degreeOfMultiprogramming; }
    
    // Setters
    public void setState(ProcessState state) { this.state = state; }
    public void setRemainingTime(int remainingTime) { this.remainingTime = remainingTime; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setStartTime(int startTime) { 
        this.startTime = startTime;
        if (!hasStarted) {
            this.responseTime = startTime - arrivalTime;
            this.hasStarted = true;
        }
    }
    public void setCompletionTime(int completionTime) { 
        this.completionTime = completionTime;
        this.turnaroundTime = completionTime - arrivalTime;
        this.waitingTime = turnaroundTime - burstTime;
    }
    public void setWaitingTime(int waitingTime) { this.waitingTime = waitingTime; }
    public void setTurnaroundTime(int turnaroundTime) { this.turnaroundTime = turnaroundTime; }
    public void incrementTimeInReadyQueue() { this.timeInReadyQueue++; }
    public void setStarved(boolean starved) { this.starved = starved; }
    public void setDegreeOfMultiprogramming(int degree) { this.degreeOfMultiprogramming = degree; }
    
    /**
     * Execute process for given time quantum
     * @param quantum Time to execute
     * @return Actual time executed
     */
    public int execute(int quantum) {
        int timeExecuted = Math.min(quantum, remainingTime);
        remainingTime -= timeExecuted;
        return timeExecuted;
    }
    
    /**
     * Check if process is completed
     */
    public boolean isCompleted() {
        return remainingTime == 0;
    }
    
    /**
     * Apply aging to increase priority
     */
    public void applyAging() {
        if (priority < 128) {
            priority++;
        }
    }
    
    /**
     * Reset priority to original value
     */
    public void resetPriority() {
        priority = originalPriority;
    }
    
    @Override
    public String toString() {
        return String.format("P%d [Burst=%d, Priority=%d, Memory=%dMB]", 
                           processId, burstTime, priority, memoryRequired);
    }
    
    /**
     * Get detailed string representation
     */
    public String getDetailedInfo() {
        return String.format("Process %d:\n" +
                           "  Burst Time: %d ms\n" +
                           "  Priority: %d (Original: %d)\n" +
                           "  Memory: %d MB\n" +
                           "  State: %s\n" +
                           "  Waiting Time: %d ms\n" +
                           "  Turnaround Time: %d ms\n" +
                           "  Response Time: %d ms\n" +
                           "  Starved: %s",
                           processId, burstTime, priority, originalPriority,
                           memoryRequired, state, waitingTime, turnaroundTime,
                           responseTime, starved ? "Yes" : "No");
    }
}

/**
 * Enum for process states
 */
enum ProcessState {
    NEW,        // Process is being created
    READY,      // Process is ready to run
    RUNNING,    // Process is currently executing
    WAITING,    // Process is waiting for I/O or event
    TERMINATED  // Process has finished execution
}
