import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * SystemCalls - Simulates operating system calls for process control,
 * information maintenance, and memory management
 */
public class SystemCalls {
    private static boolean verboseLogging = true;
    private static final DateTimeFormatter timeFormatter = 
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    // ==================== Process Control System Calls ====================
    
    /**
     * fork() - Create a new process (simulate process creation)
     * @param processId Process ID to create
     * @return Process ID of created process, -1 on failure
     */
    public static int fork(int processId) {
        log(String.format("SYSCALL: fork() - Creating process P%d", processId));
        // Simulate process creation
        return processId;
    }
    
    /**
     * exec() - Execute a process (simulate loading program into memory)
     * @param process Process to execute
     * @return 0 on success, -1 on failure
     */
    public static int exec(PCB process) {
        log(String.format("SYSCALL: exec() - Executing process P%d [Burst=%dms, Priority=%d]",
            process.getProcessId(), process.getBurstTime(), process.getPriority()));
        process.setState(ProcessState.RUNNING);
        return 0;
    }
    
    /**
     * exit() - Terminate a process
     * @param process Process to terminate
     * @param exitCode Exit code
     */
    public static void exit(PCB process, int exitCode) {
        log(String.format("SYSCALL: exit(%d) - Process P%d terminated",
            exitCode, process.getProcessId()));
        process.setState(ProcessState.TERMINATED);
    }
    
    /**
     * wait() - Wait for a process to complete
     * @param processId Process ID to wait for
     */
    public static void wait(int processId) {
        log(String.format("SYSCALL: wait() - Waiting for process P%d", processId));
    }
    
    /**
     * kill() - Terminate a process forcefully
     * @param processId Process ID to kill
     * @return 0 on success, -1 on failure
     */
    public static int kill(int processId) {
        log(String.format("SYSCALL: kill() - Terminating process P%d", processId));
        return 0;
    }
    
    // ==================== Information Maintenance System Calls ====================
    
    /**
     * getpid() - Get process ID
     * @param process Process
     * @return Process ID
     */
    public static int getpid(PCB process) {
        return process.getProcessId();
    }
    
    /**
     * getProcessInfo() - Get process information
     * @param process Process to query
     * @return Process information string
     */
    public static String getProcessInfo(PCB process) {
        log(String.format("SYSCALL: getProcessInfo() - Querying info for P%d", 
            process.getProcessId()));
        return process.getDetailedInfo();
    }
    
    /**
     * time() - Get current system time (simulation time in ms)
     * @param currentTime Current simulation time
     * @return Current time
     */
    public static int time(int currentTime) {
        return currentTime;
    }
    
    /**
     * getStatistics() - Get process statistics
     * @param process Process to query
     */
    public static void getStatistics(PCB process) {
        log(String.format("SYSCALL: getStatistics() - P%d: WT=%dms, TAT=%dms, RT=%dms",
            process.getProcessId(), process.getWaitingTime(), 
            process.getTurnaroundTime(), process.getResponseTime()));
    }
    
    /**
     * setPriority() - Set process priority (for aging)
     * @param process Process to modify
     * @param newPriority New priority value
     */
    public static void setPriority(PCB process, int newPriority) {
        int oldPriority = process.getPriority();
        process.setPriority(newPriority);
        log(String.format("SYSCALL: setPriority() - P%d: Priority changed from %d to %d",
            process.getProcessId(), oldPriority, newPriority));
    }
    
    // ==================== Memory Management System Calls ====================
    
    /**
     * malloc() - Allocate memory for a process
     * @param size Size of memory to allocate in MB
     * @param processId Process requesting memory
     * @return Pointer (simulated as size) on success, -1 on failure
     */
    public static int malloc(int size, int processId) {
        log(String.format("SYSCALL: malloc(%d MB) - Process P%d requesting memory",
            size, processId));
        return size;
    }
    
    /**
     * free() - Free allocated memory
     * @param processId Process releasing memory
     * @param size Size of memory to free
     */
    public static void free(int processId, int size) {
        log(String.format("SYSCALL: free(%d MB) - Process P%d releasing memory",
            size, processId));
    }
    
    /**
     * sbrk() - Change data segment size (simulate memory allocation)
     * @param increment Amount to increase/decrease
     * @return Previous program break on success
     */
    public static int sbrk(int increment) {
        log(String.format("SYSCALL: sbrk(%d) - Adjusting data segment", increment));
        return 0;
    }
    
    /**
     * getMemoryInfo() - Get memory information
     * @param memoryManager Memory manager to query
     * @return Memory information string
     */
    public static String getMemoryInfo(MemoryManager memoryManager) {
        log("SYSCALL: getMemoryInfo() - Querying memory status");
        return memoryManager.toString();
    }
    
    // ==================== Logging System Calls ====================
    
    /**
     * Log memory allocation
     */
    public static void logMemoryAllocation(int processId, int size, int remaining) {
        log(String.format("MEMORY: Allocated %d MB to P%d (Available: %d MB)",
            size, processId, remaining));
    }
    
    /**
     * Log memory deallocation
     */
    public static void logMemoryDeallocation(int processId, int size, int remaining) {
        log(String.format("MEMORY: Deallocated %d MB from P%d (Available: %d MB)",
            size, processId, remaining));
    }
    
    /**
     * Log process state change
     */
    public static void logStateChange(int processId, ProcessState oldState, 
                                      ProcessState newState) {
        log(String.format("STATE: P%d changed from %s to %s",
            processId, oldState, newState));
    }
    
    /**
     * Log context switch
     */
    public static void logContextSwitch(int fromProcessId, int toProcessId, int time) {
        log(String.format("CONTEXT SWITCH: From P%d to P%d at time %dms",
            fromProcessId, toProcessId, time));
    }
    
    /**
     * Log starvation detection
     */
    public static void logStarvation(int processId, int waitTime, int threshold) {
        log(String.format("STARVATION DETECTED: P%d waited %dms (threshold: %dms)",
            processId, waitTime, threshold));
    }
    
    /**
     * Log aging applied
     */
    public static void logAging(int processId, int oldPriority, int newPriority) {
        log(String.format("AGING: P%d priority increased from %d to %d",
            processId, oldPriority, newPriority));
    }
    
    // ==================== Utility Methods ====================
    
    /**
     * Enable/disable verbose logging
     */
    public static void setVerboseLogging(boolean enabled) {
        verboseLogging = enabled;
    }
    
    /**
     * Log a message with timestamp
     */
    private static void log(String message) {
        if (verboseLogging) {
            String timestamp = LocalDateTime.now().format(timeFormatter);
            System.out.println("[" + timestamp + "] " + message);
        }
    }
    
    /**
     * Log a message without timestamp
     */
    public static void logInfo(String message) {
        System.out.println(message);
    }
}
