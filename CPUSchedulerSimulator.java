import java.util.Scanner;
import java.io.File;

/**
 * Main CPU Scheduler Simulator
 * Simulates three scheduling algorithms: SJF, Round-Robin, and Priority with Aging
 * Uses multiple threads for file reading and job loading
 */
public class CPUSchedulerSimulator {
    private static final String DEFAULT_JOB_FILE = "job.txt";
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Display welcome message
        displayWelcome();
        
        // Get job file name
        String jobFile = getJobFileName(scanner);
        
        // Verify file exists
        if (!new File(jobFile).exists()) {
            System.out.println("\nError: File '" + jobFile + "' not found!");
            System.out.println("Please make sure the file exists in the current directory.");
            scanner.close();
            return;
        }
        
        try {
            // Main menu loop
            boolean running = true;
            while (running) {
                displayMenu();
                int choice = getMenuChoice(scanner);
                
                switch (choice) {
                    case 1:
                        runScheduler(jobFile, new SJFScheduler());
                        break;
                    case 2:
                        runScheduler(jobFile, new RoundRobinScheduler());
                        break;
                    case 3:
                        runScheduler(jobFile, new PriorityScheduler());
                        break;
                    case 4:
                        runAllSchedulers(jobFile);
                        break;
                    case 5:
                        compareSchedulers(jobFile);
                        break;
                    case 6:
                        running = false;
                        System.out.println("\nThank you for using CPU Scheduler Simulator!");
                        break;
                    default:
                        System.out.println("\nInvalid choice. Please try again.");
                }
                
                if (running && choice >= 1 && choice <= 5) {
                    System.out.print("\nPress Enter to continue...");
                    scanner.nextLine();
                }
            }
            
        } catch (Exception e) {
            System.out.println("\nAn error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Display welcome message
     */
    private static void displayWelcome() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("          CPU SCHEDULER SIMULATOR");
        System.out.println("          Multi-threaded Process Scheduling Simulation");
        System.out.println("=".repeat(70));
        System.out.println("\nThis simulator demonstrates three CPU scheduling algorithms:");
        System.out.println("  1. Shortest Job First (SJF) - Non-Preemptive");
        System.out.println("  2. Round-Robin (RR) - Quantum = 6ms");
        System.out.println("  3. Priority Scheduling (1=Lowest, 128=Highest) with Aging");
        System.out.println("\nSystem Specifications:");
        System.out.println("  - Total Memory: 2048 MB");
        System.out.println("  - Context Switch Time: 0 ms");
        System.out.println("  - All processes arrive at time 0");
        System.out.println("  - Multi-threaded: File Reader + Job Loader");
    }
    
    /**
     * Get job file name from user
     */
    private static String getJobFileName(Scanner scanner) {
        System.out.print("\nEnter job file name (default: " + DEFAULT_JOB_FILE + "): ");
        String fileName = scanner.nextLine().trim();
        
        if (fileName.isEmpty()) {
            fileName = DEFAULT_JOB_FILE;
        }
        
        System.out.println("Using job file: " + fileName);
        return fileName;
    }
    
    /**
     * Display main menu
     */
    private static void displayMenu() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("MAIN MENU");
        System.out.println("=".repeat(70));
        System.out.println("1. Run SJF (Shortest Job First) Scheduling");
        System.out.println("2. Run Round-Robin Scheduling");
        System.out.println("3. Run Priority Scheduling with Aging");
        System.out.println("4. Run All Algorithms");
        System.out.println("5. Compare All Algorithms");
        System.out.println("6. Exit");
        System.out.print("\nEnter your choice (1-6): ");
    }
    
    /**
     * Get menu choice from user
     */
    private static int getMenuChoice(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Run a single scheduler
     */
    private static void runScheduler(String jobFile, Scheduler scheduler) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("INITIALIZING SYSTEM");
        System.out.println("=".repeat(70));
        
        // Create shared resources
        JobQueue jobQueue = new JobQueue();
        ReadyQueue readyQueue = new ReadyQueue();
        MemoryManager memoryManager = new MemoryManager();
        
        // Create and start file reader thread
        FileReaderThread fileReader = new FileReaderThread(jobFile, jobQueue);
        fileReader.start();
        
        // Create and start job loader thread
        JobLoaderThread jobLoader = new JobLoaderThread(jobQueue, readyQueue, memoryManager);
        jobLoader.start();
        
        try {
            // Wait for both threads to complete
            fileReader.join();
            jobLoader.join();
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SYSTEM READY - Starting Scheduler");
            System.out.println("=".repeat(70));
            
            // Initialize and run scheduler
            scheduler.initialize(readyQueue, memoryManager);
            scheduler.schedule();
            
            // Display statistics
            scheduler.displayStatistics();
            
        } catch (InterruptedException e) {
            System.out.println("Scheduling interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Run all schedulers sequentially
     */
    private static void runAllSchedulers(String jobFile) {
        Scheduler[] schedulers = {
            new SJFScheduler(),
            new RoundRobinScheduler(),
            new PriorityScheduler()
        };
        
        for (Scheduler scheduler : schedulers) {
            runScheduler(jobFile, scheduler);
            System.out.println("\n" + "=".repeat(70));
            System.out.println();
        }
    }
    
    /**
     * Compare all schedulers
     */
    private static void compareSchedulers(String jobFile) {
        Scheduler[] schedulers = {
            new SJFScheduler(),
            new RoundRobinScheduler(),
            new PriorityScheduler()
        };
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("RUNNING ALL SCHEDULERS FOR COMPARISON");
        System.out.println("=".repeat(70));
        
        // Disable verbose logging for cleaner comparison
        SystemCalls.setVerboseLogging(false);
        
        for (Scheduler scheduler : schedulers) {
            // Create fresh resources for each scheduler
            JobQueue jobQueue = new JobQueue();
            ReadyQueue readyQueue = new ReadyQueue();
            MemoryManager memoryManager = new MemoryManager();
            
            // Create and start threads
            FileReaderThread fileReader = new FileReaderThread(jobFile, jobQueue);
            JobLoaderThread jobLoader = new JobLoaderThread(jobQueue, readyQueue, memoryManager);
            
            fileReader.start();
            jobLoader.start();
            
            try {
                fileReader.join();
                jobLoader.join();
                
                // Run scheduler
                scheduler.initialize(readyQueue, memoryManager);
                scheduler.schedule();
                
            } catch (InterruptedException e) {
                System.out.println("Scheduling interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        // Re-enable verbose logging
        SystemCalls.setVerboseLogging(true);
        
        // Display comparison
        displayComparison(schedulers);
    }
    
    /**
     * Display comparison of all schedulers
     */
    private static void displayComparison(Scheduler[] schedulers) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SCHEDULER COMPARISON");
        System.out.println("=".repeat(70));
        System.out.println(String.format("%-40s %-15s %-15s", 
            "Algorithm", "Avg Wait Time", "Avg TAT"));
        System.out.println("=".repeat(70));
        
        for (Scheduler scheduler : schedulers) {
            System.out.println(String.format("%-40s %-15.2f %-15.2f",
                scheduler.getAlgorithmName(),
                scheduler.getAverageWaitingTime(),
                scheduler.getAverageTurnaroundTime()));
        }
        
        System.out.println("=".repeat(70));
        
        // Find best algorithm
        Scheduler bestWT = schedulers[0];
        Scheduler bestTAT = schedulers[0];
        
        for (Scheduler scheduler : schedulers) {
            if (scheduler.getAverageWaitingTime() < bestWT.getAverageWaitingTime()) {
                bestWT = scheduler;
            }
            if (scheduler.getAverageTurnaroundTime() < bestTAT.getAverageTurnaroundTime()) {
                bestTAT = scheduler;
            }
        }
        
        System.out.println("\nBEST PERFORMING ALGORITHMS:");
        System.out.println("  Best Average Waiting Time: " + bestWT.getAlgorithmName() + 
            String.format(" (%.2f ms)", bestWT.getAverageWaitingTime()));
        System.out.println("  Best Average Turnaround Time: " + bestTAT.getAlgorithmName() + 
            String.format(" (%.2f ms)", bestTAT.getAverageTurnaroundTime()));
        System.out.println("=".repeat(70));
    }
}
