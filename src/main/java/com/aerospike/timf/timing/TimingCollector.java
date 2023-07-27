package com.aerospike.timf.timing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public class TimingCollector {
    private static final int SAMPLES_TO_KEEP = 3600;
    private AtomicInteger failed = new AtomicInteger();
    private AtomicInteger succeeded = new AtomicInteger();
    private AtomicLong cumulativeTimeInUs = new AtomicLong();
    private AtomicLong minUs = new AtomicLong(Long.MAX_VALUE);
    private AtomicLong maxUs = new AtomicLong();
    
    // To implement p95, p99, p99.9 without sorting we will follow the technique found here
    // https://blog.bramp.net/post/2018/01/16/measuring-percentile-latency/
    // We will start our lowest bucket <= 100us and 40 buckets gives us over 1 minute latency
    private static final int NUM_BUCKETS = 40;
    private static final double BUCKET_MULTIPLIER = 1.41;
    private final long[] bucketLimits = new long[NUM_BUCKETS];
    private AtomicLongArray buckets = new AtomicLongArray(40);
    private final List<Sample> samples = new ArrayList<>();
    
    public TimingCollector() {
        long START_BUCKET_VALUE_US = 100;
        for (int i = 0; i < NUM_BUCKETS; i++) {
            bucketLimits[i] = (long)(Math.pow(BUCKET_MULTIPLIER, i) * START_BUCKET_VALUE_US);
        }
    }
    
    private int findBucket(long sampleInUs) {
        int index = Arrays.binarySearch(bucketLimits, sampleInUs);
        if (index >= 0) {
            return index;
        }
        else {
            int result = -index-1;
            if (result >= NUM_BUCKETS) {
                result = NUM_BUCKETS-1;
            }
            return result;
        }
    }
    
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
        buckets.addAndGet(findBucket(timeInUs), 1);
    }
    
    private Sample getTimedSample() {
        long[] pcts = compute99Percentages();
        return new Sample(new Date().getTime(), succeeded.getAndSet(0), failed.getAndSet(0), cumulativeTimeInUs.getAndSet(0), minUs.getAndSet(Long.MAX_VALUE), 
                maxUs.getAndSet(0), pcts[0], pcts[1], pcts[2]);
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
            Sample compareTo = new Sample(since, 0, 0, 0, 0, 0, 0, 0, 0);
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
    
    public void showBuckets() {
        for (int i = 0; i < NUM_BUCKETS; i++) {
            System.out.printf("%d=[%,d-%,d]:%d\n", i, i == 0? 0 : bucketLimits[i-1]+1, bucketLimits[i], buckets.get(i));
        }
        System.out.println();
    }
    
    private long estimatePercentage(double pct, int index, double currentPct, long totalSamples, long previousCount, long currentCount) {
        double previousPct = ((double)previousCount)/totalSamples;
        if (index == 0) {
            return bucketLimits[0];
        }
        else {
            long currentBucketLimit = bucketLimits[index];
            long previousBucketLimit = bucketLimits[index-1];
            return (long)(previousBucketLimit + (currentBucketLimit-previousBucketLimit) * (pct-previousPct)/(currentPct-previousPct));
        }
    }
    
    private long[] compute99Percentages() {
        long previousCount = 0;
        long currentCount = 0;
        long totalSamples = succeeded.get() + failed.get();
        long p95 = 0;
        long p99 = 0;
        long p999 = 0;
        if (totalSamples > 0) {
            for (int i = 0; i < NUM_BUCKETS; i++) {
                currentCount += buckets.getAndSet(i, 0);
                double currentPct = ((double)currentCount)/totalSamples;
                if (p95 == 0 && currentPct >= 0.95) {
                    p95 = estimatePercentage(0.95, i, currentPct, totalSamples, previousCount, currentCount);
                }
                if (p99 == 0 && currentPct >= 0.99) {
                    p99 = estimatePercentage(0.99, i, currentPct, totalSamples, previousCount, currentCount);
                }
                if (p999 == 0 && currentPct >= 0.999) {
                    p999 = estimatePercentage(0.999, i, currentPct, totalSamples, previousCount, currentCount);
                }
                previousCount = currentCount;
            }
        }
        return new long[] {p95, p99, p999};
    }
    
    public static void main(String[] args) {
        TimingCollector tc = new TimingCollector();
        System.out.println("50 = "+ tc.findBucket(50));
        System.out.println("100 = "+ tc.findBucket(100));
        System.out.println("150 = "+ tc.findBucket(150));
        System.out.println("500 = "+ tc.findBucket(500));
        System.out.println("1000 = "+ tc.findBucket(1000));
        System.out.println("1050 = "+ tc.findBucket(1050));
        System.out.println("2500 = "+ tc.findBucket(2500));
        System.out.println("2146573 = "+ tc.findBucket(2146573));
        System.out.println("50,000,000 = "+ tc.findBucket(50_000_000L));
        System.out.println("5,000,000,000 = "+ tc.findBucket(500_0000_000L));
        
        int SAMPLES = 1_000_000;
        long[] longs = new long[SAMPLES];
        Random rand = ThreadLocalRandom.current();
        for (int i = 0; i < SAMPLES; i++) {
            longs[i] = (long)(rand.nextLong(100_000_000) * rand.nextDouble() * rand.nextDouble() * rand.nextDouble());
            tc.addSample(true, longs[i]);
        }
        tc.showBuckets();
        long now = System.nanoTime();
        Arrays.sort(longs);
        System.out.printf("Sort took %,dus\n", (System.nanoTime()-now)/1000);
        System.out.printf("95%% = %,d\n", longs[(int)(0.95*SAMPLES)]);
        System.out.printf("99%% = %,d\n", longs[(int)(0.99*SAMPLES)]);
        System.out.printf("99.9%% = %,d\n", longs[(int)(0.995*SAMPLES)]);
        now = System.nanoTime();
        long[] pcts = tc.compute99Percentages();
        System.out.printf("Percentages took %,dus\n", (System.nanoTime()-now)/1000);
        System.out.printf("p95 ~= %,d\n", pcts[0]);
        System.out.printf("p99 ~= %,d\n", pcts[1]);
        System.out.printf("p99.9 ~= %,d\n", pcts[2]);

        tc.showBuckets();
    }
}
