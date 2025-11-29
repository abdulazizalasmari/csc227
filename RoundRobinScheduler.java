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
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STATE CHANGES");
        System.out.println("=".repeat(70));
        
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
            
            process.setState(ProcessState.RUNNING);
            
            int startTime = currentTime;
            
            // Print process section header only on first execution
            if (!process.hasStarted() || process.getStartTime() == startTime) {
                System.out.println(String.format("\nProcess %d", process.getProcessId()));
                System.out.println("-".repeat(70));
            }
            
            // Execute the process
            SystemCalls.exec(process);
            
            System.out.println(String.format("Running at time %d", startTime));
            
            int executionTime = Math.min(TIME_QUANTUM, process.getRemainingTime());
            
            // Execute for quantum or remaining time
            process.execute(executionTime);
            currentTime += executionTime;
            
            // Add to Gantt chart
            ganttChart.add(new GanttEntry(process.getProcessId(), startTime, currentTime));
            
            // Check if process is completed
            if (process.isCompleted()) {
                process.setCompletionTime(currentTime);
                process.setState(ProcessState.TERMINATED);
                System.out.println(String.format("Terminated at time %d", currentTime));
                SystemCalls.exit(process, 0);
                
                // Free memory
                memoryManager.deallocate(process);
                
                // Add to completed list
                completedProcesses.add(process);
            } else {
                // Process not completed, add back to queue
                process.setState(ProcessState.READY);
                System.out.println(String.format("Ready at time %d", currentTime));
                rrQueue.offer(process);
            }
        }
    }
}
