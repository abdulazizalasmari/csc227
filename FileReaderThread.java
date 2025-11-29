import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * FileReaderThread - Independent thread that reads process information from job.txt
 * and creates PCBs, placing them in the job queue
 */
public class FileReaderThread extends Thread {
    private String filename;
    private JobQueue jobQueue;
    private int processCount;
    
    public FileReaderThread(String filename, JobQueue jobQueue) {
        this.filename = filename;
        this.jobQueue = jobQueue;
        this.processCount = 0;
        setName("FileReaderThread");
    }
    
    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                
                try {
                    PCB process = parseProcessLine(line);
                    if (process != null) {
                        // Simulate fork system call
                        SystemCalls.fork(process.getProcessId());
                        
                        jobQueue.enqueue(process);
                        processCount++;
                    }
                } catch (Exception e) {
                    SystemCalls.logInfo("  Error parsing line: " + line);
                    SystemCalls.logInfo("  " + e.getMessage());
                }
            }
            
            jobQueue.markReadingComplete();
            
        } catch (IOException e) {
            SystemCalls.logInfo("Error reading file: " + e.getMessage());
            jobQueue.markReadingComplete();
        }
    }
    
    /**
     * Parse a line from the input file and create a PCB
     * Format: processId:burstTime:priority;memoryRequired
     * Example: 1:25:4;500
     */
    private PCB parseProcessLine(String line) {
        try {
            // Split by semicolon to separate process info and memory
            String[] parts = line.split(";");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid format. Expected: id:burst:priority;memory");
            }
            
            // Parse process information (id:burst:priority)
            String[] processInfo = parts[0].split(":");
            if (processInfo.length != 3) {
                throw new IllegalArgumentException("Invalid process info. Expected: id:burst:priority");
            }
            
            int processId = Integer.parseInt(processInfo[0].trim());
            int burstTime = Integer.parseInt(processInfo[1].trim());
            int priority = Integer.parseInt(processInfo[2].trim());
            int memoryRequired = Integer.parseInt(parts[1].trim());
            
            // Validate input
            if (processId < 1) {
                throw new IllegalArgumentException("Process ID must be positive");
            }
            if (burstTime < 1) {
                throw new IllegalArgumentException("Burst time must be positive");
            }
            if (priority < 1 || priority > 128) {
                throw new IllegalArgumentException("Priority must be between 1 and 128");
            }
            if (memoryRequired < 1) {
                throw new IllegalArgumentException("Memory required must be positive");
            }
            
            return new PCB(processId, burstTime, priority, memoryRequired);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in line: " + line);
        }
    }
    
    /**
     * Get the number of processes loaded
     */
    public int getProcessCount() {
        return processCount;
    }
}
