import java.util.ArrayList;
import java.util.List;

/**
 * Ready Queue - Thread-safe queue for processes ready to execute
 * Processes are loaded here from Job Queue when memory is available
 */
public class ReadyQueue {
    private List<PCB> queue;
    private final Object lock = new Object();
    
    public ReadyQueue() {
        this.queue = new ArrayList<>();
    }
    
    /**
     * Add a process to the ready queue
     */
    public void enqueue(PCB process) {
        synchronized (lock) {
            process.setState(ProcessState.READY);
            queue.add(process);
            lock.notifyAll();
        }
    }
    
    /**
     * Remove and return a specific process from the ready queue
     */
    public PCB dequeue(PCB process) {
        synchronized (lock) {
            queue.remove(process);
            return process;
        }
    }
    
    /**
     * Remove and return the first process from the ready queue
     */
    public PCB dequeue() {
        synchronized (lock) {
            if (queue.isEmpty()) {
                return null;
            }
            return queue.remove(0);
        }
    }
    
    /**
     * Get process at specific index
     */
    public PCB get(int index) {
        synchronized (lock) {
            if (index >= 0 && index < queue.size()) {
                return queue.get(index);
            }
            return null;
        }
    }
    
    /**
     * Remove process at specific index
     */
    public PCB remove(int index) {
        synchronized (lock) {
            if (index >= 0 && index < queue.size()) {
                return queue.remove(index);
            }
            return null;
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
     * Get the size of the queue (degree of multiprogramming)
     */
    public int size() {
        synchronized (lock) {
            return queue.size();
        }
    }
    
    /**
     * Get all processes in the ready queue (for scheduling decisions)
     */
    public List<PCB> getAllProcesses() {
        synchronized (lock) {
            return new ArrayList<>(queue);
        }
    }
    
    /**
     * Clear the ready queue
     */
    public void clear() {
        synchronized (lock) {
            queue.clear();
        }
    }
    
    /**
     * Get a copy of the queue for scheduling algorithms
     */
    public List<PCB> getQueueCopy() {
        synchronized (lock) {
            return new ArrayList<>(queue);
        }
    }
    
    /**
     * Find process with shortest burst time (for SJF)
     */
    public PCB findShortestJob() {
        synchronized (lock) {
            if (queue.isEmpty()) {
                return null;
            }
            
            PCB shortest = queue.get(0);
            for (PCB process : queue) {
                if (process.getRemainingTime() < shortest.getRemainingTime()) {
                    shortest = process;
                }
            }
            return shortest;
        }
    }
    
    /**
     * Find process with highest priority (for Priority Scheduling)
     */
    public PCB findHighestPriority() {
        synchronized (lock) {
            if (queue.isEmpty()) {
                return null;
            }
            
            PCB highest = queue.get(0);
            for (PCB process : queue) {
                if (process.getPriority() > highest.getPriority()) {
                    highest = process;
                }
            }
            return highest;
        }
    }
    
    /**
     * Increment waiting time for all processes in ready queue
     */
    public void incrementWaitingTimes(int excludeProcessId) {
        synchronized (lock) {
            for (PCB process : queue) {
                if (process.getProcessId() != excludeProcessId) {
                    process.incrementTimeInReadyQueue();
                }
            }
        }
    }
}
