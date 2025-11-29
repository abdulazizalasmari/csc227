import java.util.ArrayList;
import java.util.List;

/**
 * Priority Scheduler with Aging to prevent starvation
 * Higher priority number = Higher priority (1=Lowest, 128=Highest)
 * Implements aging when a process waits more than the degree of multiprogramming
 */
public class PriorityScheduler extends Scheduler {
    private List<String> starvationLog;
    
    public PriorityScheduler() {
        super("Priority Scheduling (1=Lowest, 128=Highest) with Aging");
        this.starvationLog = new ArrayList<>();
    }
    
    @Override
    public void schedule() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("STATE CHANGES");
        System.out.println("=".repeat(70));
        
        int currentTime = 0;
        List<PCB> waitingProcesses = new ArrayList<>();
        
        // Get all processes from ready queue
        while (!readyQueue.isEmpty()) {
            waitingProcesses.add(readyQueue.dequeue());
        }
        
        SystemCalls.logInfo(String.format("Total processes to schedule: %d\n", waitingProcesses.size()));
        
        // Execute processes in priority order with aging
        while (!waitingProcesses.isEmpty()) {
            // Check and apply aging
            applyAging(waitingProcesses, currentTime);
            
            // Find process with highest priority
            PCB highestPriority = findHighestPriority(waitingProcesses);
            
            if (highestPriority == null) {
                break;
            }
            
            // Remove from waiting list
            waitingProcesses.remove(highestPriority);
            
            int startTime = currentTime;
            highestPriority.setStartTime(startTime);
            highestPriority.setState(ProcessState.RUNNING);
            
            System.out.println(String.format("\nProcess %d", highestPriority.getProcessId()));
            System.out.println("-".repeat(70));
            
            // Execute the process
            SystemCalls.exec(highestPriority);
            
            System.out.println(String.format("Running at time %d", startTime));
            
            // Execute entire burst (non-preemptive)
            currentTime += highestPriority.getBurstTime();
            
            // Mark as completed
            highestPriority.setCompletionTime(currentTime);
            highestPriority.setState(ProcessState.TERMINATED);
            System.out.println(String.format("Terminated at time %d", currentTime));
            SystemCalls.exit(highestPriority, 0);
            
            // Add to Gantt chart
            ganttChart.add(new GanttEntry(highestPriority.getProcessId(), startTime, currentTime));
            
            // Free memory
            memoryManager.deallocate(highestPriority);
            
            // Add to completed list
            completedProcesses.add(highestPriority);
        }
    }
    
    @Override
    public void displayStatistics() {
        // Call parent display first
        super.displayStatistics();
        
        // Then display starvation log if any
        if (!starvationLog.isEmpty()) {
            System.out.println("\n--- Starvation and Aging Log ---");
            for (String log : starvationLog) {
                System.out.println(log);
            }
            System.out.println("=".repeat(70) + "\n");
        }
    }
    
    /**
     * Find the process with the highest priority
     */
    private PCB findHighestPriority(List<PCB> processes) {
        if (processes.isEmpty()) {
            return null;
        }
        
        PCB highest = processes.get(0);
        for (PCB process : processes) {
            if (process.getPriority() > highest.getPriority()) {
                highest = process;
            } else if (process.getPriority() == highest.getPriority()) {
                // Tie-breaker: choose process with lower ID
                if (process.getProcessId() < highest.getProcessId()) {
                    highest = process;
                }
            }
        }
        
        return highest;
    }
    
    /**
     * Apply aging to prevent starvation
     * A process's priority is increased if it has waited more than the
     * degree of multiprogramming at the time it was accepted to ready queue
     */
    private void applyAging(List<PCB> waitingProcesses, int currentTime) {
        for (PCB process : waitingProcesses) {
            int waitTime = currentTime - process.getArrivalTime();
            int threshold = process.getDegreeOfMultiprogramming();
            
            // Check for starvation
            if (threshold > 0 && waitTime > threshold) {
                if (!process.isStarved()) {
                    process.setStarved(true);
                }
                
                // Apply aging: increase priority
                int oldPriority = process.getPriority();
                process.applyAging();
                
                if (process.getPriority() != oldPriority) {
                    // Log starvation and aging event
                    String logEntry = String.format("Process P%d starved at time=%dms (Waited: %dms, DOP: %d). Priority Aged to: %d.",
                        process.getProcessId(), currentTime, waitTime, threshold, process.getPriority());
                    starvationLog.add(logEntry);
                }
            }
        }
    }
}
