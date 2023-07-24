package com.aerospike.timf.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.aerospike.timf.databases.DatabaseFunctions;
import com.aerospike.timf.service.AddressGeneratorService;
import com.aerospike.timf.service.DatabaseConfigItem;
import com.aerospike.timf.service.DatabaseParameters;
import com.aerospike.timf.service.DatabaseVersionService;
import com.aerospike.timf.service.NameGeneratorService;
import com.aerospike.timf.service.TimingService;
import com.aerospike.timf.service.WorkloadDetails;
import com.aerospike.timf.service.WorkloadExecutor.State;
import com.aerospike.timf.service.WorkloadManagerService;
import com.aerospike.timf.timing.Sample;
import com.aerospike.timf.timing.TimingCollector;

@RestController
public class ApplicationController {
	@Autowired
	private NameGeneratorService nameGeneratorService;
	
	@Autowired
	private AddressGeneratorService addressGeneratorService;
	
	@Autowired
	private WorkloadManagerService workloadManagerService;
	
//	@Autowired
//	private DatabaseConfigurationService databaseConfigurationService;
	
    @Autowired
    private DatabaseVersionService databaseVersionService;
    
    @Autowired
    private TimingService timingService;
    
	private static final int PEOPLE_COUNT = 0;
	private static final long MS_IN_100_YEARS = TimeUnit.MILLISECONDS.convert(365*100, TimeUnit.DAYS); //100L*365*24*60*60*1000;
	
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

    /*
	@GetMapping("/seed")
	public int seedData() {
		personRepsitory.deleteAll();
		
		IAerospikeClient client = new AerospikeClient("172.17.0.2", 3000);
		AeroMapper mapper = new AeroMapper.Builder(client).build();
		
		Address homeAddress = new Address("123 Main St", "Como", "CO", "81111");
		Address workAddress = new Address("222 Smith St", "New York", "NY", "10001");
		
		int count = 0;
		Random random = ThreadLocalRandom.current();
		for (int i = 0; i < PEOPLE_COUNT; i++) {
			try {
				String firstName = nameGeneratorService.getFirstName();
				String lastName = nameGeneratorService.getLastName();
				long now = new Date().getTime();
				Date date = new Date(now - random.nextLong(MS_IN_100_YEARS));
				
				Address homeAddr = new Address(
				        addressGeneratorService.getAddressLine1(), 
				        addressGeneratorService.getSuburb(), 
				        addressGeneratorService.getState(),
				        addressGeneratorService.getZipCode());
                Address workAddr = new Address(
                        addressGeneratorService.getAddressLine1(), 
                        addressGeneratorService.getSuburb(), 
                        addressGeneratorService.getState(),
                        addressGeneratorService.getZipCode());
				
                List<Account> accounts = new ArrayList<>();
				Person person = new Person(i, firstName, lastName, date, Gender.MALE, homeAddr, workAddr, accounts);
				personRepsitory.save(person);
				
				Account account = new Account();
				account.setAccountName(person.getFirstName() + " " + person.getLastName() + " account 1");
				account.setAcctNum(random.nextLong(100_000_000L));
				account.setBalance(random.nextInt(50_000_00));
				account.setOpenDate(new Date());
				account.setRoutingNo(Math.abs(random.nextLong()));
				accountRepository.save(account);
				count++;
			}
			catch (RuntimeException re) {
				re.printStackTrace();
			}
		}
        List<Account> accounts = new ArrayList<>();
        accounts.add(new Account(10000, "Bob Jones Main Account", new Date(), 0, 7000001242L));
        Person person = new Person(10001, "Bob", "Jones", new Date(), Gender.MALE, homeAddress, workAddress, accounts);
		person.setHomeAddress(new Address("123 Main St", "Como", "CO", "81111"));
		person.setWorkAddress(new Address("222 Cow St", "Mountain View", "CA", "90232"));
		personRepsitory.save(person);
		mapper.save(person, accounts.get(0));
		client.close();
		return count;
	}
	
	@GetMapping("/multiplePredicates")
	public List<Person> multiplePredicate() {
	    IAerospikeClient client = aerospikeTemplate.getAerospikeClient();
	    Statement stmt = new Statement();
	    stmt.setFilter(Filter.equal("lastName", "Brown"));
	    stmt.setNamespace("test");
	    stmt.setSetName("Person");
	    QueryPolicy queryPolicy = new QueryPolicy(client.getQueryPolicyDefault());
	    queryPolicy.filterExp = Exp.build(Exp.eq(MapExp.getByKey(MapReturnType.VALUE, Exp.Type.STRING, Exp.val("state"), Exp.mapBin("homeAddress")), Exp.val("CO")));
	    RecordSet recordSet = client.query(queryPolicy, stmt);
	    while (recordSet.next()) {
	        System.out.println(recordSet.getRecord());
	    }
	    return null;
	    
	}
	@GetMapping("/findByLastName/{lastName}")
	public List<Person> findByLastName(@PathVariable(name = "lastName", required=true) String lastName) {
		return personRepsitory.findByLastName(lastName);
	}
	
	@GetMapping("/findByHomeAddressState/{state}")
	public List<Person> findByHomeAddressState(@PathVariable(name = "state", required=true) String state) {
		return personRepsitory.findByHomeAddressState(state);
	}
	
	@GetMapping("/findByLastNameHomeAddressState/{lastName}/{state}")
	public List<Person> findByHomeAddressState(@PathVariable(name = "lastName", required = true) String lastName, @PathVariable(name = "state", required=true) String state) {
		return personRepsitory.findByLastNameAndHomeAddressState(lastName, state);
	}
	
	@GetMapping("/findSearchPersonByLastName/{lastName}")
	public List<SearchPerson> findSearchPersonByLastName(@PathVariable(name = "lastName", required = true) String lastName) {
		return personRepsitory.findSearchPersonByLastName(lastName);
	}
	
	@GetMapping("/findByDobBetween/{startDate}/{endDate}")
	public List<Person> findByDistinctLastNameContaining(@PathVariable(name = "startDate", required=true) String startDateStr, @PathVariable(name = "endDate", required=true) String endDateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date startDate = sdf.parse(startDateStr);
			Date endDate = sdf.parse(endDateStr);
			return personRepsitory.findByDateOfBirthBetween(startDate.getTime(), endDate.getTime());
//			return personRepsitory.findByDateOfBirthGreaterThanEqualAndDateOfBirthLessThanEqual(startDate, endDate);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	@GetMapping("/findByLastNameContaining/{lastName}")
	public List<Person> findByLastNameContaining(@PathVariable(name = "lastName", required=true) String lastName) {
		return personRepsitory.findByLastNameContaining(lastName);
	}
	
	@GetMapping("/findByLastNameStartingWith/{lastName}")
	public List<Person> findByLastNameStartingWith(@PathVariable(name = "lastName", required=true) String lastName) {
		return personRepsitory.findByLastNameStartingWith(lastName);
	}
	
	@GetMapping("/findByLastNameAndFirstName/{lastName}/{firstName}")
	public List<Person> findByLastNameAndFirstName(@PathVariable(name = "lastName", required=true) String lastName, @PathVariable(name = "firstName", required=true) String firstName) {
		return personRepsitory.findByLastNameAndFirstNameContaining(lastName, firstName);
	}
	
	
	@Autowired
	private PersonService personService;
	
	@Autowired
	private AerospikeTemplate aerospikeTemplate;
	
	@Autowired
	private MappingAerospikeConverter mappingConverter;
	
//	@GetMapping("/seed1")
//	public int seedData1() {
//		Person person = new Person(1, "Bob", "Jones", new GregorianCalendar(1971, 12, 19).getTime());
//		
//		personService.save(person);
//		Person p = personService.read(1).get();
//		Key key = new Key(aerospikeTemplate.getNamespace(), aerospikeTemplate.getSetName(Person.class), "1");
//		
//		int purchaseAmount = 12345;
//		
//		IAerospikeClient client = aerospikeTemplate.getAerospikeClient();
//		Policy policy = new Policy(client.getReadPolicyDefault());
//		policy.filterExp = Exp.build(Exp.gt(
//				  Exp.sub(Exp.intBin("creditLimit"), Exp.intBin("exposure")), 
//				  Exp.val(purchaseAmount)
//				));
//		com.aerospike.client.Record record = aerospikeTemplate.getAerospikeClient().get(null, key);
//		AerospikeReadData readData = AerospikeReadData.forRead(key, record);
//		Person readPerson = mappingConverter.read(Person.class, readData);
//		
//		//System.out.println(readPerson.getVersion());
//		readPerson.setLastName("Smith");
//		aerospikeTemplate.save(readPerson);
//		
//		return 1;
//	}
 */
}
