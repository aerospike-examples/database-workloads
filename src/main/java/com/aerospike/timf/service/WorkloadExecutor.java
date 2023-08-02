package com.aerospike.timf.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.ResultCode;
import com.aerospike.timf.databases.DatabaseFunctions;
import com.aerospike.timf.timing.TimingCollector;
import com.aerospike.timf.workloads.SubordinateObjectsWorkloadManager;
import com.aerospike.timf.workloads.WorkloadManager;

public class WorkloadExecutor<C> {
    public enum State {
        READY,
        PAUSED,
        RUNNING
    }
    
    private final String name;
    private final C databaseConnection; 
    private final ExecutorService executor;
    private volatile State state = State.READY;
    private volatile boolean paused = false;
    private volatile boolean terminate = false;
    private final AtomicInteger activeThreads = new AtomicInteger();
    private final AtomicLong recordCounter = new AtomicLong();
    private final TimingService timingService;
    private final DatabaseFunctions<C> databaseFunctions;
    private final TimingCollector timingCollector;
    private volatile long targetRecords = -1;
    
    public WorkloadExecutor(final String name, final C databaseConnetion, DatabaseFunctions<C> databaseFunctions, TimingService timingService) {
        this.executor = Executors.newCachedThreadPool();
        this.databaseConnection = databaseConnetion;
        this.databaseFunctions = databaseFunctions;
        this.timingService = timingService;
        this.timingCollector = new TimingCollector();
        this.timingService.registerTimingCollector(name, timingCollector);
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    private synchronized void beginJob(int numThreads, Runnable runner) {
        if (this.activeThreads.get() > 0) {
            throw new IllegalArgumentException(String.format("Cannot start a new job when the old one still has %d threads running.", this.activeThreads.get()));
        }
        this.paused = false;
        this.terminate = false;
        this.state = State.RUNNING;
        for (int i = 0; i < numThreads; i++) {
            executor.execute(runner);
        }
    }
    
    /**
     * Determine if the paused flag is set. If it is, pause for 100ms then return true. Otherwise, return false.
     * This would be better implemented as a wait/notify block (TODO)
     * @return
     * @throws InterruptedException
     */
    private boolean checkPaused() throws InterruptedException {
        if (!paused) {
            return false;
        }
        else {
            Thread.sleep(100);
            return true;
        }
    }

    public State getState() {
        return state;
    }
    
    private synchronized void setState(State existingState, State newState) {
        if (existingState != null) {
            if (this.state == existingState) {
                this.state = newState;
            }
        }
        else {
            this.state = newState;
        }
    }
    
    public void pauseJob() {
        this.paused = true;
        this.setState(State.RUNNING, State.PAUSED);
    }
    
    public void resumeJob() {
        this.paused = false;
        if (this.activeThreads.get() > 0) {
            this.setState(null, State.RUNNING);
        }
        else {
            this.setState(null, State.READY);
        }
    }

    public void terminateJob() {
        this.terminate = true;
        while (this.activeThreads.get() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    
    public <T> void startContinuousRun(final int numThreads, final long numRecordsInDatabase, 
            final int writePercent, final WorkloadManager<T> manager) {
        
        this.beginJob(numThreads, () -> {
            Map<String, Object> options = new HashMap<>();
            options.put("writePercent", writePercent);
            activeThreads.incrementAndGet();
            try {
                Random random = ThreadLocalRandom.current();
                while (!terminate) {
                    if (!checkPaused()) {
                        long id = random.nextLong(numRecordsInDatabase);
                        
                        long now = 0;
                        try {
                            Object object = manager.prepareForContinualRunOperation(id, numRecordsInDatabase, options);
                            now = System.nanoTime();
                            manager.executeContinualRunOperation(id, object, options, databaseFunctions, databaseConnection);
                            timingCollector.addSample(true, (System.nanoTime() - now)/1000);
                        }
                        catch (Exception e) {
                            if (now > 0) {
                                timingCollector.addSample(false, (System.nanoTime() - now)/1000);
                            }
                            else {
                                // must be an exception in the prepare step, do not log the sample
                                System.err.printf("Error preparing workload: %s (%s)\n", e.getMessage(), e.getClass());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            catch (InterruptedException ignored) {}
            finally {
                int activeCount = activeThreads.decrementAndGet();
                if (activeCount == 0) {
                    this.setState(null, State.READY);
                }
            }
        });
    }
    
    public <T> void startSeedData(final int numThreads, final long numRecords, final WorkloadManager<T> manager) {
        this.recordCounter.set(0);
        this.targetRecords = numRecords;
        SubordinateObjectsWorkloadManager subordinateManager = (manager instanceof SubordinateObjectsWorkloadManager) ? (SubordinateObjectsWorkloadManager<T, ?>) manager : null;
        
        this.beginJob(numThreads, () -> {
            activeThreads.incrementAndGet();
            try {
                T entity = null;
                int subordinateObjectsLeft = 0;
                while (!terminate) {
                    if (!checkPaused()) {
                        if (entity == null) {
                            // Have to generate the next top level entity
                            long id = recordCounter.getAndIncrement();
                            if (id >= numRecords) {
                                break;
                            }
                            else {
                                long now = 0;
                                try {
                                    entity = manager.generatePrimaryEntity(id);
                                    subordinateObjectsLeft = subordinateManager == null ? 0 : subordinateManager.getNumberOfSubordinateObjects(entity);
                                    now = System.nanoTime();
                                    manager.insertPrimaryEntity(entity, databaseFunctions, databaseConnection);
                                    timingCollector.addSample(true, (System.nanoTime() - now)/1000);
                                }
                                catch (Exception e) {
                                    if (now > 0) {
                                        timingCollector.addSample(false, (System.nanoTime() - now)/1000);
                                    }
                                    else {
                                        // must be an exception in the prepare step, do not log the sample
                                        System.err.printf("Error preparing workload: %s (%s)\n", e.getMessage(), e.getClass());
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        else {
                            // We can only get to here if this object has subordinate objects.
                            long now = 0;
                            boolean succeeded = true;
                            try {
                                Object object = subordinateManager.generateSubordinateEntity(entity, subordinateObjectsLeft);
                                now = System.nanoTime();
                                subordinateManager.saveSubordinateObject(object, entity, databaseFunctions, databaseConnection);
                            }
                            catch (AerospikeException ae) {
                                if (ae.getResultCode() == ResultCode.MAX_ERROR_RATE || ae.getResultCode() == ResultCode.DEVICE_OVERLOAD) {
                                    // Server is busy, back this thread off
                                    Thread.sleep(ThreadLocalRandom.current().nextInt(20));
                                    // Disqualify this result
                                    now = 0;
                                    subordinateObjectsLeft++;
                                }
                                else {
                                    succeeded = false;
                                }
                            }
                            catch (Exception e) {
                                succeeded = false;
                                if (now == 0) {
                                    // must be an exception in the prepare step, do not log the sample
                                    System.err.printf("Error preparing workload: %s (%s)\n", e.getMessage(), e.getClass());
                                    e.printStackTrace();
                                }
                            }
                            finally {
                                if (now > 0) {
                                    long time = System.nanoTime() - now;
                                    timingCollector.addSample(succeeded, time/1000);
                                }
                                subordinateObjectsLeft--;
                            }
                        }
                        if (subordinateObjectsLeft == 0) {
                            entity = null;
                        }
                    }
                }
            }
            catch (InterruptedException ignored) {}
            finally {
                int activeCount = activeThreads.decrementAndGet();
                if (activeCount == 0) {
                    this.setState(null, State.READY);
                    this.targetRecords = -1;
                }
            }
        });
    }
    
    // Return the percentage complete, but if it's not a seeding workload return -1
    public long getTenthsPercentageComplete() {
        long totalRecords = this.targetRecords;
        long currentlyDone = this.recordCounter.get();
        State currentState = this.getState();
        if ((currentState == State.RUNNING || currentState == State.PAUSED) && (totalRecords > 0)) {
            return (currentlyDone * 1000)/totalRecords;
        }
        return -1;
    }
}
