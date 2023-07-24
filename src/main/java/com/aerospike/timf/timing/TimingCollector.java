package com.aerospike.timf.timing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.annotation.PostConstruct;

public class TimingCollector {
    private static final int SAMPLES_TO_KEEP = 3600;
    private AtomicInteger failed = new AtomicInteger();
    private AtomicInteger succeeded = new AtomicInteger();
    private AtomicLong cumulativeTimeInUs = new AtomicLong();
    private AtomicLong minUs = new AtomicLong(Long.MAX_VALUE);
    private AtomicLong maxUs = new AtomicLong();
    private Thread timingMonitor;
    private final List<Sample> samples = new ArrayList<>();
    
    public void addSample(boolean wasSuccessful, long timeInUs) {
        if (wasSuccessful) {
            succeeded.incrementAndGet();
        }
        else {
            failed.incrementAndGet();
        }
        cumulativeTimeInUs.addAndGet(timeInUs);
        if (timeInUs < minUs.get()) {
            minUs.set(timeInUs);
        }
        if (timeInUs > maxUs.get()) {
            maxUs.set(timeInUs);
        }
    }
    
    private Sample getTimedSample() {
        return new Sample(new Date().getTime(), succeeded.getAndSet(0), failed.getAndSet(0), cumulativeTimeInUs.getAndSet(0), minUs.getAndSet(Long.MAX_VALUE), maxUs.getAndSet(0));
    }
    
    private synchronized void addSample(Sample sample) {
        samples.add(sample);
        if (samples.size() > SAMPLES_TO_KEEP) {
            samples.remove(0);
        }
    }
    
    public synchronized List<Sample> getSamples(long since) {
        if (since == 0 || this.samples.size() == 0 || this.samples.get(0).getSampleTime() >= since) {
            return this.samples;
        }
        else {
            // The samples are in sorted order so we can use binary search to find the first sample
            Sample compareTo = new Sample(since, 0, 0, 0, 0, 0);
            int insertPoint = Collections.binarySearch(samples, compareTo, (a, b) -> a.getSampleTime() > b.getSampleTime() ? 1 : a.getSampleTime() == b.getSampleTime() ? 0 : -1);
            // We cannot just return the base list as it might be modified by a new sample coming in. 
            if (insertPoint >= 0) {
                return new ArrayList<>(samples.subList(insertPoint, samples.size()));
            }
            else {
                // binarySearch return]s -(insertionPoint)-1 if it doesn't find an exact match.
                insertPoint = -(insertPoint+1);
                if (insertPoint < samples.size()) {
                    return new ArrayList<>(samples.subList(insertPoint, samples.size()));
                }
                else {
                    return new ArrayList<>();
                }
            }

        }
    }
    
    public void takeSample() {
        addSample(getTimedSample());
    }
}
