import java.util.ArrayList;
import java.util.List;

/**
 * Shortest Job First (SJF) Non-Preemptive Scheduler
 * Selects the process with the shortest burst time first
 */
public class SJFScheduler extends Scheduler {
    
    public SJFScheduler() {
        super("Shortest Job First (SJF) - Non-Preemptive");
    }
    
    @Override
    public void schedule() {
        SystemCalls.logInfo("\n" + "=".repeat(70));
        SystemCalls.logInfo("Starting SJF Scheduling...");
        SystemCalls.logInfo("=".repeat(70));
        
        int currentTime = 0;
        List<PCB> allProcesses = new ArrayList<>();
        
        // Get all processes from ready queue
        while (!readyQueue.isEmpty()) {
            allProcesses.add(readyQueue.dequeue());
        }
        
        SystemCalls.logInfo(String.format("Total processes to schedule: %d\n", allProcesses.size()));
        
        // Execute processes in SJF order
        while (!allProcesses.isEmpty()) {
            // Find process with shortest burst time
            PCB shortestJob = findShortestJob(allProcesses);
            
            if (shortestJob == null) {
                break;
            }
            
            // Remove from list
            allProcesses.remove(shortestJob);
            
            // Execute the process
            SystemCalls.exec(shortestJob);
            
            int startTime = currentTime;
            shortestJob.setStartTime(startTime);
            
            SystemCalls.logInfo(String.format("[Time %d] Executing P%d (Burst: %d ms, Priority: %d)",
                currentTime, shortestJob.getProcessId(), shortestJob.getBurstTime(), 
                shortestJob.getPriority()));
            
            // Execute entire burst (non-preemptive)
            currentTime += shortestJob.getBurstTime();
            
            // Mark as completed
            shortestJob.setCompletionTime(currentTime);
            SystemCalls.exit(shortestJob, 0);
            
            // Add to Gantt chart
            ganttChart.add(new GanttEntry(shortestJob.getProcessId(), startTime, currentTime));
            
            // Free memory
            memoryManager.deallocate(shortestJob);
            
            // Check for starvation in remaining processes
            checkStarvation(allProcesses, currentTime);
            
            // Add to completed list
            completedProcesses.add(shortestJob);
            
            SystemCalls.logInfo(String.format("[Time %d] P%d completed (WT: %d ms, TAT: %d ms)\n",
                currentTime, shortestJob.getProcessId(), 
                shortestJob.getWaitingTime(), shortestJob.getTurnaroundTime()));
        }
        
        SystemCalls.logInfo(String.format("SJF Scheduling completed at time %d ms\n", currentTime));
    }
    
    /**
     * Find the process with the shortest burst time
     */
    private PCB findShortestJob(List<PCB> processes) {
        if (processes.isEmpty()) {
            return null;
        }
        
        PCB shortest = processes.get(0);
        for (PCB process : processes) {
            if (process.getBurstTime() < shortest.getBurstTime()) {
                shortest = process;
            } else if (process.getBurstTime() == shortest.getBurstTime()) {
                // Tie-breaker: choose process with lower ID
                if (process.getProcessId() < shortest.getProcessId()) {
                    shortest = process;
                }
            }
        }
        
        return shortest;
    }
    
    /**
     * Check for starvation in waiting processes
     * A process is considered starved if it has waited more than the
     * degree of multiprogramming at the time it was accepted to ready queue
     */
    private void checkStarvation(List<PCB> waitingProcesses, int currentTime) {
        for (PCB process : waitingProcesses) {
            int waitTime = currentTime - process.getArrivalTime();
            int threshold = process.getDegreeOfMultiprogramming();
            
            if (threshold > 0 && waitTime > threshold && !process.isStarved()) {
                process.setStarved(true);
                SystemCalls.logStarvation(process.getProcessId(), waitTime, threshold);
            }
        }
    }
}
