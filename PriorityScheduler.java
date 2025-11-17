import java.util.ArrayList;
import java.util.List;

/**
 * Priority Scheduler with Aging to prevent starvation
 * Higher priority number = Higher priority (1=Lowest, 128=Highest)
 * Implements aging when a process waits more than the degree of multiprogramming
 */
public class PriorityScheduler extends Scheduler {
    
    public PriorityScheduler() {
        super("Priority Scheduling (1=Lowest, 128=Highest) with Aging");
    }
    
    @Override
    public void schedule() {
        SystemCalls.logInfo("\n" + "=".repeat(70));
        SystemCalls.logInfo("Starting Priority Scheduling with Aging...");
        SystemCalls.logInfo("=".repeat(70));
        
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
            
            // Execute the process
            SystemCalls.exec(highestPriority);
            
            int startTime = currentTime;
            highestPriority.setStartTime(startTime);
            
            SystemCalls.logInfo(String.format("[Time %d] Executing P%d (Burst: %d ms, Priority: %d%s)",
                currentTime, highestPriority.getProcessId(), 
                highestPriority.getBurstTime(), highestPriority.getPriority(),
                highestPriority.getPriority() != highestPriority.getOriginalPriority() 
                    ? " [Aged from " + highestPriority.getOriginalPriority() + "]" : ""));
            
            // Execute entire burst (non-preemptive)
            currentTime += highestPriority.getBurstTime();
            
            // Mark as completed
            highestPriority.setCompletionTime(currentTime);
            SystemCalls.exit(highestPriority, 0);
            
            // Add to Gantt chart
            ganttChart.add(new GanttEntry(highestPriority.getProcessId(), startTime, currentTime));
            
            // Free memory
            memoryManager.deallocate(highestPriority);
            
            // Add to completed list
            completedProcesses.add(highestPriority);
            
            SystemCalls.logInfo(String.format("[Time %d] P%d completed (WT: %d ms, TAT: %d ms)\n",
                currentTime, highestPriority.getProcessId(), 
                highestPriority.getWaitingTime(), highestPriority.getTurnaroundTime()));
        }
        
        SystemCalls.logInfo(String.format("Priority Scheduling completed at time %d ms\n", currentTime));
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
                    SystemCalls.logStarvation(process.getProcessId(), waitTime, threshold);
                }
                
                // Apply aging: increase priority
                int oldPriority = process.getPriority();
                process.applyAging();
                
                if (process.getPriority() != oldPriority) {
                    SystemCalls.logAging(process.getProcessId(), oldPriority, process.getPriority());
                }
            }
        }
    }
}
