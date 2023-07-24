package com.aerospike.timf.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.aerospike.timf.timing.TimingCollector;

import jakarta.annotation.PostConstruct;

@Service
public class TimingService {
    private Thread timingMonitor;
    private final Map<String, TimingCollector> timingCollectors = new HashMap<>();
    
    public synchronized void registerTimingCollector(String name, TimingCollector collector) {
        timingCollectors.put(name, collector);
    }
    
    public synchronized void deregisterTimingCollector(String name) {
        timingCollectors.remove(name);
    }
    
    private synchronized void takeSamples() {
        for (String name : timingCollectors.keySet()) {
            timingCollectors.get(name).takeSample();
        }
    }
    
    public synchronized TimingCollector getCollector(String name) {
        return this.timingCollectors.get(name);
    }
    
    @PostConstruct
    private void init() {
        this.timingMonitor = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
                takeSamples();
            }
        });
        this.timingMonitor.setDaemon(true);
        this.timingMonitor.start();
    }
}
