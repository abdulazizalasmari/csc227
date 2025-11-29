/**
 * JobLoaderThread - Independent thread that loads jobs from Job Queue to Ready Queue
 * Continuously checks available memory before loading jobs
 */
public class JobLoaderThread extends Thread {
    private JobQueue jobQueue;
    private ReadyQueue readyQueue;
    private MemoryManager memoryManager;
    private volatile boolean running;
    
    public JobLoaderThread(JobQueue jobQueue, ReadyQueue readyQueue, MemoryManager memoryManager) {
        this.jobQueue = jobQueue;
        this.readyQueue = readyQueue;
        this.memoryManager = memoryManager;
        this.running = true;
        setName("JobLoaderThread");
    }
    
    @Override
    public void run() {
        try {
            while (running) {
                // Check if there are jobs in the job queue
                PCB process = jobQueue.peek();
                
                if (process != null) {
                    // Check if enough memory is available
                    if (memoryManager.hasEnoughMemory(process.getMemoryRequired())) {
                        // Remove from job queue
                        jobQueue.dequeue();
                        
                        // Allocate memory
                        if (memoryManager.allocate(process)) {
                            // Store degree of multiprogramming for starvation detection
                            process.setDegreeOfMultiprogramming(readyQueue.size());
                            
                            // Add to ready queue
                            readyQueue.enqueue(process);
                        }
                    } else {
                        // Not enough memory, wait for some to be freed
                        Thread.sleep(50);
                    }
                } else {
                    // No jobs in queue
                    if (jobQueue.isReadingComplete()) {
                        // File reading is complete and queue is empty
                        break;
                    } else {
                        // Wait for more jobs to arrive
                        Thread.sleep(50);
                    }
                }
                
                // Small delay to prevent busy waiting
                Thread.sleep(10);
            }
            
        } catch (InterruptedException e) {
            SystemCalls.logInfo("Job loader thread interrupted");
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Stop the job loader thread
     */
    public void stopLoading() {
        running = false;
    }
    
    /**
     * Check if thread is still running
     */
    public boolean isRunning() {
        return running && isAlive();
    }
}
