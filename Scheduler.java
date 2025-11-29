import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all scheduling algorithms
 */
public abstract class Scheduler {
    protected ReadyQueue readyQueue;
    protected MemoryManager memoryManager;
    protected List<PCB> completedProcesses;
    protected List<GanttEntry> ganttChart;
    protected String algorithmName;
    
    public Scheduler(String algorithmName) {
        this.algorithmName = algorithmName;
        this.completedProcesses = new ArrayList<>();
        this.ganttChart = new ArrayList<>();
    }
    
    /**
     * Initialize scheduler with ready queue and memory manager
     */
    public void initialize(ReadyQueue readyQueue, MemoryManager memoryManager) {
        this.readyQueue = readyQueue;
        this.memoryManager = memoryManager;
    }
    
    /**
     * Execute the scheduling algorithm
     */
    public abstract void schedule();
    
    /**
     * Calculate and display statistics
     */
    public void displayStatistics() {
        SystemCalls.logInfo("\n" + "=".repeat(70));
        SystemCalls.logInfo("SCHEDULING ALGORITHM: " + algorithmName);
        SystemCalls.logInfo("=".repeat(70));
        
        // Display Gantt Chart
        displayGanttChart();
        
        // Display process details
        SystemCalls.logInfo("\n" + "-".repeat(70));
        SystemCalls.logInfo("PROCESS DETAILS");
        SystemCalls.logInfo("-".repeat(70));
        SystemCalls.logInfo(String.format("%-10s %-12s %-12s %-15s %-15s", 
            "Process", "Burst Time", "Priority", "Waiting Time", "Turnaround Time"));
        SystemCalls.logInfo("-".repeat(70));
        
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        int processCount = completedProcesses.size();
        
        for (PCB process : completedProcesses) {
            SystemCalls.logInfo(String.format("P%-9d %-12d %-12d %-15d %-15d",
                process.getProcessId(),
                process.getBurstTime(),
                process.getOriginalPriority(),
                process.getWaitingTime(),
                process.getTurnaroundTime()));
            
            totalWaitingTime += process.getWaitingTime();
            totalTurnaroundTime += process.getTurnaroundTime();
        }
        
        SystemCalls.logInfo("-".repeat(70));
        
        // Display average statistics
        double avgWaitingTime = totalWaitingTime / processCount;
        double avgTurnaroundTime = totalTurnaroundTime / processCount;
        
        SystemCalls.logInfo("\nSTATISTICS SUMMARY:");
        SystemCalls.logInfo(String.format("  Total Processes: %d", processCount));
        SystemCalls.logInfo(String.format("  Average Waiting Time: %.2f ms", avgWaitingTime));
        SystemCalls.logInfo(String.format("  Average Turnaround Time: %.2f ms", avgTurnaroundTime));
        SystemCalls.logInfo("=".repeat(70) + "\n");
    }
    
    /**
     * Display Gantt Chart
     */
    protected void displayGanttChart() {
        SystemCalls.logInfo("\nGANTT CHART:");
        SystemCalls.logInfo("-".repeat(70));
        
        // Calculate box width for each process based on execution time
        List<Integer> boxWidths = new ArrayList<>();
        
        for (GanttEntry entry : ganttChart) {
            int duration = entry.endTime - entry.startTime;
            // Make box width proportional to duration, minimum 8 characters
            int width = Math.max(8, duration);
            boxWidths.add(width);
        }
        
        // Build top border
        StringBuilder topBorder = new StringBuilder("__");
        for (int width : boxWidths) {
            topBorder.append("_".repeat(width));
        }
        
        // Build top line of boxes
        StringBuilder topLine = new StringBuilder("|");
        for (int width : boxWidths) {
            topLine.append(" ".repeat(width)).append("|");
        }
        
        // Build middle line with process names
        StringBuilder middleLine = new StringBuilder("|");
        for (int i = 0; i < ganttChart.size(); i++) {
            GanttEntry entry = ganttChart.get(i);
            String processLabel = String.format("P%d", entry.processId);
            int width = boxWidths.get(i);
            int padding = width - processLabel.length();
            int leftPad = padding / 2;
            int rightPad = padding - leftPad;
            middleLine.append(" ".repeat(leftPad))
                      .append(processLabel)
                      .append(" ".repeat(rightPad))
                      .append("|");
        }
        
        // Build bottom line of boxes
        StringBuilder bottomLine = new StringBuilder("|");
        for (int width : boxWidths) {
            bottomLine.append(" ".repeat(width)).append("|");
        }
        
        // Build bottom border
        StringBuilder bottomBorder = new StringBuilder("__");
        for (int width : boxWidths) {
            bottomBorder.append("_".repeat(width));
        }
        
        // Build timeline
        StringBuilder timeline = new StringBuilder(String.valueOf(ganttChart.get(0).startTime));
        for (int i = 0; i < ganttChart.size(); i++) {
            GanttEntry entry = ganttChart.get(i);
            int width = boxWidths.get(i);
            String endTimeStr = String.valueOf(entry.endTime);
            int padding = width - endTimeStr.length() + 1;
            timeline.append(" ".repeat(padding)).append(endTimeStr);
        }
        
        // Print the Gantt chart
        SystemCalls.logInfo(topBorder.toString());
        SystemCalls.logInfo(topLine.toString());
        SystemCalls.logInfo(middleLine.toString());
        SystemCalls.logInfo(bottomLine.toString());
        SystemCalls.logInfo(bottomBorder.toString());
        SystemCalls.logInfo(timeline.toString());
        
        // Display detailed execution timeline
        SystemCalls.logInfo("\nEXECUTION TIMELINE:");
        SystemCalls.logInfo(String.format("%-10s %-15s %-15s %-20s", 
            "Process", "Start Time", "End Time", "Execution Duration"));
        SystemCalls.logInfo("-".repeat(70));
        
        for (GanttEntry entry : ganttChart) {
            SystemCalls.logInfo(String.format("P%-9d %-15d %-15d %-20d ms",
                entry.processId, entry.startTime, entry.endTime, 
                entry.endTime - entry.startTime));
        }
    }
    
    /**
     * Get average waiting time
     */
    public double getAverageWaitingTime() {
        if (completedProcesses.isEmpty()) return 0;
        
        double total = 0;
        for (PCB process : completedProcesses) {
            total += process.getWaitingTime();
        }
        return total / completedProcesses.size();
    }
    
    /**
     * Get average turnaround time
     */
    public double getAverageTurnaroundTime() {
        if (completedProcesses.isEmpty()) return 0;
        
        double total = 0;
        for (PCB process : completedProcesses) {
            total += process.getTurnaroundTime();
        }
        return total / completedProcesses.size();
    }
    
    /**
     * Get algorithm name
     */
    public String getAlgorithmName() {
        return algorithmName;
    }
    
    /**
     * Get completed processes
     */
    public List<PCB> getCompletedProcesses() {
        return new ArrayList<>(completedProcesses);
    }
}

/**
 * Gantt Chart Entry
 */
class GanttEntry {
    int processId;
    int startTime;
    int endTime;
    
    public GanttEntry(int processId, int startTime, int endTime) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
