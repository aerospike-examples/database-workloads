

async function getServerStatuses(services, activeDatabaseManager) {
    let statuses = await services.getServerStatuses();
    activeDatabaseManager.reconcileServerStatus(statuses);
}

async function getTimingSamples(name, since) {
    let timings = await services.getTimingSamples(activeDatabaseManager.getCurrentDatabaseName(), 0);
    return timings;
}

function formatTime(seconds) {
    let mins = Math.floor(seconds/60);
    let secs = seconds - mins * 60;
    return `${mins}m ${(""+secs).padStart(2, '0')}s`;
}
$(function() {
    let mock = false;
    let services = mock ? serviceInterfaceMock : serviceInterface;

    // TODO: Turn this dependency mess into something cleaner (event driven);
    let activeWorkloadManager = createActiveWorkloadManager(services);
    let activeDatabaseManager = createActiveDatabaseManager(services, activeWorkloadManager);
    activeWorkloadManager.setActiveDatabaseManager(activeDatabaseManager);

    let connectionManager = createConnectionManager(services, activeDatabaseManager, function(params) {
        console.log("connected", params);
        activeDatabaseManager.addDatabase(params);
    } )

    $("#newConnection").on("click", function() {
        connectionManager.show();
        // services.getDatabases()
        //     .then((data) => console.log("databases are: " + data),
        //         (data) => console.log("Failed to get data: " + data));
        // services.getDatabaseVersions('Aerospike')
        //     .then((data) => console.log("Versions:", data));
    });

    // setInterval(function() {
    //     getServerStatuses(services, activeDatabaseManager);
    // }, 5000);

    let $latencyGraph = $("#latencyGraph");
    let $throughputGraph = $("#throughputGraph");
    let latencyGraph = StatisticsGraph($latencyGraph, "Aggregate", "LATENCY");
    let throughputGraph = StatisticsGraph($throughputGraph, "", "THROUGHPUT");
    let now = 0;
    // setInterval(() => {
    //     services.getTimingSamples(activeDatabaseManager.getCurrentDatabaseName(), 0)
    //         .then(function(data) {
    //             latencyGraph.update(data);
    //             throughputGraph.update(data);
    //         }, 
    //         function(err) {
    //             console.log("error getting timings", err);
    //         });
    // }, 1000);

    setInterval(() => {
        // TODO: Keep a running tally of where time is at to replace the fixed 0 here
        services.getServerStatus(activeDatabaseManager.getCurrentDatabaseName(), 0)
            .then(function(data) {
                latencyGraph.update(data.activeWorkloadTimings);
                throughputGraph.update(data.activeWorkloadTimings);
                activeDatabaseManager.reconcileServerStatus(data.workloadStates);
                activeWorkloadManager.updateProgress(data.activeWorkloadTenthsPercentComplete);
            },
            function(err) {
                console.log("error getting server status", err);
            });
    }, 1000);

    $("#graphDuration").on('input', function() {
        let $this = $(this);
        let val = parseInt($this.val());
        $("#selectedDuration").html(formatTime(val));
        latencyGraph.setVisibleDuration(val*1000);
        throughputGraph.setVisibleDuration(val*1000);
    });
})