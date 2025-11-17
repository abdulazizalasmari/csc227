/**
 * Memory Manager - Manages system memory allocation and deallocation
 * Total memory available: 2048 MB
 */
public class MemoryManager {
    private static final int TOTAL_MEMORY = 2048; // MB
    private int availableMemory;
    private final Object lock = new Object();
    
    public MemoryManager() {
        this.availableMemory = TOTAL_MEMORY;
    }
    
    /**
     * Check if enough memory is available for a process
     */
    public boolean hasEnoughMemory(int required) {
        synchronized (lock) {
            return availableMemory >= required;
        }
    }
    
    /**
     * Allocate memory for a process
     * @param process The process requesting memory
     * @return true if allocation successful, false otherwise
     */
    public boolean allocate(PCB process) {
        synchronized (lock) {
            int required = process.getMemoryRequired();
            if (availableMemory >= required) {
                availableMemory -= required;
                SystemCalls.logMemoryAllocation(process.getProcessId(), required, availableMemory);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Deallocate memory when process completes
     * @param process The process releasing memory
     */
    public void deallocate(PCB process) {
        synchronized (lock) {
            int released = process.getMemoryRequired();
            availableMemory += released;
            SystemCalls.logMemoryDeallocation(process.getProcessId(), released, availableMemory);
            lock.notifyAll(); // Notify waiting threads that memory is available
        }
    }
    
    /**
     * Get available memory
     */
    public int getAvailableMemory() {
        synchronized (lock) {
            return availableMemory;
        }
    }
    
    /**
     * Get used memory
     */
    public int getUsedMemory() {
        synchronized (lock) {
            return TOTAL_MEMORY - availableMemory;
        }
    }
    
    /**
     * Get total memory
     */
    public int getTotalMemory() {
        return TOTAL_MEMORY;
    }
    
    /**
     * Get memory utilization percentage
     */
    public double getUtilization() {
        synchronized (lock) {
            return ((double)(TOTAL_MEMORY - availableMemory) / TOTAL_MEMORY) * 100;
        }
    }
    
    /**
     * Reset memory manager (for running multiple scheduling algorithms)
     */
    public void reset() {
        synchronized (lock) {
            availableMemory = TOTAL_MEMORY;
        }
    }
    
    @Override
    public String toString() {
        synchronized (lock) {
            return String.format("Memory: %d/%d MB used (%.1f%%)", 
                               getUsedMemory(), TOTAL_MEMORY, getUtilization());
        }
    }
}
