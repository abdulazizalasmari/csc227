import java.util.LinkedList;
import java.util.Queue;

/**
 * Round-Robin Scheduler with time quantum of 6ms
 * Each process gets a maximum of 6ms CPU time before being preempted
 */
public class RoundRobinScheduler extends Scheduler {
    private static final int TIME_QUANTUM = 6; // 6ms quantum
    
    public RoundRobinScheduler() {
        super("Round-Robin (RR) - Quantum = 6ms");
    }
    
    @Override
    public void schedule() {
        SystemCalls.logInfo("\n" + "=".repeat(70));
        SystemCalls.logInfo("Starting Round-Robin Scheduling (Quantum = 6ms)...");
        SystemCalls.logInfo("=".repeat(70));
        
        int currentTime = 0;
        Queue<PCB> rrQueue = new LinkedList<>();
        
        // Get all processes from ready queue
        while (!readyQueue.isEmpty()) {
            PCB process = readyQueue.dequeue();
            rrQueue.offer(process);
        }
        
        SystemCalls.logInfo(String.format("Total processes to schedule: %d\n", rrQueue.size()));
        
        // Execute processes in round-robin fashion
        while (!rrQueue.isEmpty()) {
            PCB process = rrQueue.poll();
            
            // Set start time if first execution
            if (!process.hasStarted()) {
                process.setStartTime(currentTime);
            }
            
            // Execute the process
            SystemCalls.exec(process);
            
            int startTime = currentTime;
            int executionTime = Math.min(TIME_QUANTUM, process.getRemainingTime());
            
            SystemCalls.logInfo(String.format("[Time %d] Executing P%d (Remaining: %d ms -> %d ms)",
                currentTime, process.getProcessId(), process.getRemainingTime(),
                process.getRemainingTime() - executionTime));
            
            // Execute for quantum or remaining time
            process.execute(executionTime);
            currentTime += executionTime;
            
            // Add to Gantt chart
            ganttChart.add(new GanttEntry(process.getProcessId(), startTime, currentTime));
            
            // Check if process is completed
            if (process.isCompleted()) {
                process.setCompletionTime(currentTime);
                SystemCalls.exit(process, 0);
                
                // Free memory
                memoryManager.deallocate(process);
                
                // Add to completed list
                completedProcesses.add(process);
                
                SystemCalls.logInfo(String.format("[Time %d] P%d completed (WT: %d ms, TAT: %d ms)\n",
                    currentTime, process.getProcessId(), 
                    process.getWaitingTime(), process.getTurnaroundTime()));
            } else {
                // Process not completed, add back to queue
                process.setState(ProcessState.READY);
                rrQueue.offer(process);
                
                SystemCalls.logInfo(String.format("[Time %d] P%d preempted, added back to queue\n",
                    currentTime, process.getProcessId()));
            }
        }
        
        SystemCalls.logInfo(String.format("Round-Robin Scheduling completed at time %d ms\n", currentTime));
    }
}
