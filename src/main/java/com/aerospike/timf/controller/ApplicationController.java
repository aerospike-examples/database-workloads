package com.aerospike.timf.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.aerospike.timf.databases.DatabaseFunctions;
import com.aerospike.timf.service.DatabaseConfigItem;
import com.aerospike.timf.service.DatabaseParameters;
import com.aerospike.timf.service.DatabaseVersionService;
import com.aerospike.timf.service.TimingService;
import com.aerospike.timf.service.WorkloadDetails;
import com.aerospike.timf.service.WorkloadExecutor.State;
import com.aerospike.timf.service.WorkloadManagerService;
import com.aerospike.timf.timing.Sample;
import com.aerospike.timf.timing.TimingCollector;

@RestController
public class ApplicationController {
	@Autowired
	private WorkloadManagerService workloadManagerService;
	
//	@Autowired
//	private DatabaseConfigurationService databaseConfigurationService;
	
    @Autowired
    private DatabaseVersionService databaseVersionService;
    
    @Autowired
    private TimingService timingService;
	
	@GetMapping("/demo/api/getDatabases") 
	public List<String> getDatabases() {
	    return this.databaseVersionService.getDatabaseImplementations();
	}
	
    @GetMapping("/demo/api/getDatabaseVersions") 
    public List<String> getDatabaseVersions(@RequestParam(name = "databaseName") String databaseName) {
        return this.databaseVersionService.getDatabaseVersions(databaseName);
    }
    
    @GetMapping("/demo/api/getDatabaseConfigParams") 
    public List<DatabaseConfigItem> getDatabaseConfigParams(@RequestParam(name = "databaseName") String databaseName, 
                    @RequestParam(name = "version") String version) {
        
        return this.databaseVersionService.getDatabaseImplmentation(databaseName, version).getConfigItems();
    }
    
    @PostMapping(value = "/demo/api/connect") 
    @ResponseBody
    public boolean connectDatabase(@RequestBody DatabaseParameters parameters) {
        DatabaseFunctions<?> databaseFunctions = this.databaseVersionService.getDatabaseImplmentation(parameters.getDatabase(), parameters.getVersion());
        workloadManagerService.createDatabaseInstance(parameters, databaseFunctions);
        return true;
    }
    
    @PostMapping(value = "/demo/api/startWorkload") 
    @ResponseBody
    public boolean startWorkload(@RequestBody WorkloadDetails parameters) {
        workloadManagerService.startWorkload(parameters.getName(), parameters.getJobName(), parameters.getParameters());
        return true;
    }

    @GetMapping("/demo/api/stopWorkload") 
    public boolean stopWorkload(@RequestParam(name = "name") String databaseName) {
        workloadManagerService.stopWorkload(databaseName);
        return true;
    }

    @GetMapping("/demo/api/pauseWorkload") 
    public boolean pauseWorkload(@RequestParam(name = "name") String databaseName) {
        workloadManagerService.pauseWorkload(databaseName);
        return true;
    }

    @GetMapping("/demo/api/resumeWorkload") 
    public boolean resumeWorkload(@RequestParam(name = "name") String databaseName) {
        workloadManagerService.resumeWorkload(databaseName);
        return true;
    }
    
    @GetMapping("/demo/api/getServerStatuses") 
    public Map<String, State> getAllStates() {
        return workloadManagerService.getStateOfAllWorkloads();
    }

    
    @GetMapping("/demo/api/samples")
    public List<Sample> getSamples(@RequestParam(name = "name") String databaseName, @RequestParam(name = "since") long since) {
        TimingCollector collector = timingService.getCollector(databaseName);
        if (collector != null) {
            return collector.getSamples(since);
        }
        else {
            return new ArrayList<>();
        }
    }
    
    @GetMapping("/demo/api/status")
    public ServerStatus getStatus(@RequestParam(name = "name") String databaseName, @RequestParam(name = "since") long since) {
        List<Sample> samples;
        TimingCollector collector = timingService.getCollector(databaseName);
        if (collector != null) {
            samples = collector.getSamples(since);
        }
        else {
            samples = new ArrayList<>();
        }
        return new ServerStatus(workloadManagerService.getStateOfAllWorkloads(), samples, 
                workloadManagerService.getTenthsPercentageComplete(databaseName));
    }
}
