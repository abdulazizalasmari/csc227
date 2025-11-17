import java.util.LinkedList;
import java.util.Queue;

/**
 * Job Queue - Thread-safe queue for storing processes read from file
 * This is the initial queue where all jobs are placed after reading from file
 */
public class JobQueue {
    private Queue<PCB> queue;
    private final Object lock = new Object();
    private boolean readingComplete = false;
    
    public JobQueue() {
        this.queue = new LinkedList<>();
    }
    
    /**
     * Add a process to the job queue
     */
    public void enqueue(PCB process) {
        synchronized (lock) {
            queue.offer(process);
            lock.notifyAll();  // Notify waiting threads
        }
    }
    
    /**
     * Remove and return the next process from the job queue
     */
    public PCB dequeue() {
        synchronized (lock) {
            return queue.poll();
        }
    }
    
    /**
     * Peek at the next process without removing it
     */
    public PCB peek() {
        synchronized (lock) {
            return queue.peek();
        }
    }
    
    /**
     * Check if queue is empty
     */
    public boolean isEmpty() {
        synchronized (lock) {
            return queue.isEmpty();
        }
    }
    
    /**
     * Get the size of the queue
     */
    public int size() {
        synchronized (lock) {
            return queue.size();
        }
    }
    
    /**
     * Mark that file reading is complete
     */
    public void markReadingComplete() {
        synchronized (lock) {
            readingComplete = true;
            lock.notifyAll();
        }
    }
    
    /**
     * Check if reading is complete
     */
    public boolean isReadingComplete() {
        synchronized (lock) {
            return readingComplete;
        }
    }
    
    /**
     * Wait for new jobs to arrive
     */
    public void waitForJobs() throws InterruptedException {
        synchronized (lock) {
            while (queue.isEmpty() && !readingComplete) {
                lock.wait();
            }
        }
    }
}
