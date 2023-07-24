package com.aerospike.timf.service;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.aerospike.timf.databases.DatabaseFunctions;
import com.aerospike.timf.model.Person;
import com.aerospike.timf.timing.TimingCollector;

public class WorkloadExecutor {
    public enum State {
        READY,
        PAUSED,
        RUNNING
    }
    
    private final String name;
    private final Object databaseConnection; 
    private final ExecutorService executor;
    private String currentWorkload;
    private volatile State state = State.READY;
    private volatile boolean paused = false;
    private volatile boolean terminate = false;
    private final AtomicInteger activeThreads = new AtomicInteger();
    private final AtomicLong recordCounter = new AtomicLong();
    private final PersonGeneratorService personGeneratorService;
    private final TimingService timingService;
    private final DatabaseFunctions<?> databaseFunctions;
    private final TimingCollector timingCollector;
    
    public WorkloadExecutor(final String name, final Object databaseConnetion, DatabaseFunctions<?> databaseFunctions, PersonGeneratorService personGenerator, TimingService timingService) {
        this.executor = Executors.newCachedThreadPool();
        this.databaseConnection = databaseConnetion;
        this.personGeneratorService = personGenerator;
        this.databaseFunctions = databaseFunctions;
        this.timingService = timingService;
        this.timingCollector = new TimingCollector();
        this.timingService.registerTimingCollector(name, timingCollector);
        this.name = name;
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
    
    public void startContinuousRun(final int numThreads, final long numRecordsInDatabase, final int writePercent) {
        this.beginJob(numThreads, () -> {
            activeThreads.incrementAndGet();
            try {
                Random random = ThreadLocalRandom.current();
                while (!terminate) {
                    if (!checkPaused()) {
                        long id = random.nextLong(numRecordsInDatabase);
                        int percent = random.nextInt(100);
                        long now = 0;
                        try {
                            if (percent < writePercent) {
                                // Do an update
                                Person person = personGeneratorService.generatePerson(id);
                                now = System.nanoTime();
                                databaseFunctions.updatePerson(databaseConnection, person);
                            }
                            else {
                                now = System.nanoTime();
                                databaseFunctions.readPerson(databaseConnection, id);
                            }
                            timingCollector.addSample(true, (System.nanoTime() - now)/1000);
                        }
                        catch (Exception e) {
                            timingCollector.addSample(false, (System.nanoTime() - now)/1000);
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
    
    public void startSeedData(final int numThreads, final long numRecords) {
        this.recordCounter.set(0);
        this.beginJob(numThreads, () -> {
            activeThreads.incrementAndGet();
            try {
                while (!terminate) {
                    if (!checkPaused()) {
                        long id = recordCounter.getAndIncrement();
                        if (id >= numRecords) {
                            break;
                        }
                        else {
                            Person person = personGeneratorService.generatePerson(id);
                            long now = System.nanoTime();
                            try {
                                databaseFunctions.insertPerson(databaseConnection, person);
                                long time = System.nanoTime() - now;
                                timingCollector.addSample(true, time/1000);
                            }
                            catch (Exception e) {
                                long time = System.nanoTime() - now;
                                timingCollector.addSample(false, time/1000);
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
    
}
