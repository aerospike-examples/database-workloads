function createActiveWorkloadManager(services) {
    let $startButton = $("#startWorkload");
    let $startButtonWorkload1 = $startButton.parent().find("ul li").first();
    let $startButtonWorkload2 = $startButtonWorkload1.next();
    let $pauseButton = $("#pauseWorkload");
    let $stopButton = $("#stopWorkload");
    let $deleteButton = $("#deleteConnection");
    let $contentArea = $("#WorkloadDetailsContainer");
    let activeDatabaseManager;
    let currentDatabase;

    function setActiveDatabaseManager(manager) {
        activeDatabaseManager = manager;
    }

    function setButtonsFromState(state) {
        switch (state) {
            case STATE_READY:
                $startButton.prop('disabled', false);
                $pauseButton.prop('disabled', true);
                $stopButton.prop('disabled', true);
                $deleteButton.prop('disabled', false);
                $pauseButton.html('<i class="fa-solid fa-pause"></i> Pause Workload');
                break;

            case STATE_PAUSED:
                $startButton.prop('disabled', true);
                $pauseButton.prop('disabled', false);
                $stopButton.prop('disabled', false);
                $deleteButton.prop('disabled', true);
                $pauseButton.html('<i class="fa-solid fa-play"></i> Resume Workload')
                break;

            case STATE_RUNNING:
                $startButton.prop('disabled', true);
                $pauseButton.prop('disabled', false);
                $stopButton.prop('disabled', false);
                $deleteButton.prop('disabled', true);
                $pauseButton.html('<i class="fa-solid fa-pause"></i> Pause Workload');
                break;

            default: 
                $startButton.prop('disabled', true);
                $pauseButton.prop('disabled', true);
                $stopButton.prop('disabled', true);
                $deleteButton.prop('disabled', true);
                $pauseButton.html('<i class="fa-solid fa-pause"></i> Pause Workload');
                break;
        }
    }

    function updateDisplay() {
        let content = "";
        if (currentDatabase && currentDatabase.name) {
            content += `<h3>Database: ${currentDatabase.name}</h3>`
        }
        else {
            content += `<h3>Database: None</h3>`
        }
        if (currentDatabase && currentDatabase.activeJob) {
            content += `<h5>Active Workload: ${currentDatabase.activeJob}</h5>`
        }
        $contentArea.empty().append(content);
    }

    function displayWorkloadsForDatabase(databaseParams) {
        setButtonsFromState(databaseParams.state);
        currentDatabase = databaseParams;
        updateDisplay();
    }

    function showError(error) {
        $('#workloadErrorDisplay').append(error);
    }
    function clearErrors() {
        $('#workloadErrorDisplay').empty();
    }

    async function startWorkload( jobName, params) {
        clearErrors();
        console.log(`starting ${jobName} with parameters `, params);
        try {
            await services.startWorkload(currentDatabase.name, jobName, params);
            currentDatabase.activeJob = jobName;
            activeDatabaseManager.setDatabaseState(currentDatabase.name, STATE_RUNNING);
            setButtonsFromState(STATE_RUNNING);
            updateDisplay();
        }
        catch (err) {
            console.log("Error starting workload: ", err, jobName, params);
            showError("Error starting workload");
        }
    }

    async function pauseWorkload() {
        clearErrors();
        try {
            await services.pauseWorkload(currentDatabase.name);
            activeDatabaseManager.setDatabaseState(currentDatabase.name, STATE_PAUSED);
            setButtonsFromState(STATE_PAUSED);
        }
        catch (err) {
            console.log("Error pausing workload: ", err);
            showError("Error pausing workload");
        }
    }

    async function resumeWorkload() {
        clearErrors();
        try {
            await services.resumeWorkload(currentDatabase.name);
            activeDatabaseManager.setDatabaseState(currentDatabase.name, STATE_RUNNING);
            setButtonsFromState(STATE_RUNNING);
        }
        catch (err) {
            console.log("Error resuming workload: ", err);
            showError("Error resuming workload");
        }
    }

    async function stopWorkload() {
        clearErrors();
        try {
            await services.stopWorkload(currentDatabase.name);
            activeDatabaseManager.setDatabaseState(currentDatabase.name, STATE_READY);
            setButtonsFromState(STATE_READY);
            currentDatabase.activeJob = null;
            updateDisplay();
        }
        catch (err) {
            console.log("Error stopping workload: ", err);
            showError("Error stopping workload");
        }
    }

    function updateStateForWorkload(workloadName, state) {
        if (currentDatabase && currentDatabase.name === workloadName) {
            setButtonsFromState(state);
            if (state === STATE_READY) {
                currentDatabase.activeJob = null;
            }
        }
    }

    $startButtonWorkload1.on('click',
        function() { 
            // Force the menu to shut.
            $startButton.removeClass('show');
            $startButton.parent().find('ul').removeClass('show');

            $("#seedDataModal").modal('show');
        });

    $startButtonWorkload2.on('click', 
        function() { 
            // Force the menu to shut.
            $startButton.removeClass('show');
            $startButton.parent().find('ul').removeClass('show');

            $("#runWorkloadModal").modal('show');
        });

    $pauseButton.on('click', function() {
        if (currentDatabase.state === STATE_PAUSED) {
            resumeWorkload();
        }
        else {
            pauseWorkload();
        }
    });
    $stopButton.on('click', stopWorkload);

    // Track the slider on the continuous running workload so to display the read:write ratio in real time.
    $("#readWriteRatio").on('input', function() {
        let $this = $(this);
        let val = $this.val();
        $this.next().find(".reads").html(`${100-val}%<br/>reads`);
        $this.next().find(".writes").html(`${val}%<br/>writes`);
    })
    
    $("#continuousExecutionStart").on('click', function() {
        let threads = parseInt($('#numThreadsInRun').val());
        let records = parseInt($('#numRecordsInDatabase').val());
        let writePercent = parseInt($('#readWriteRatio').val());
        $("#numThreadsInRunGroup .invalid-feedback").toggle(threads <= 0);
        $("#numRecordsInRunGroup .invalid-feedback").toggle(records <= 0);

        if (threads > 0 && records > 0) {
            startWorkload('ContinuousRun', {numThreads: threads, numRecordsInDatabase: records, writePercent: writePercent})
            .then(function() {
                $("#runWorkloadModal").modal('hide');
            }, function(err) {
                console.log('error starting continuous workload', err);
                $('#executionStartError').show();
            });
        }
    });

    $("#seedDataModelStart").on('click', function() {
        let threads = parseInt($('#numThreads').val());
        let records = parseInt($('#numRecords').val());
        $("#numThreadsGroup .invalid-feedback").toggle(threads <= 0);
        $("#numRecordsGroup .invalid-feedback").toggle(records <= 0);

        if (threads > 0 && records > 0) {
            startWorkload('SeedData', {numThreads: threads, numRecords: records})
            .then(function() {
                $("#seedDataModal").modal('hide');
            }, function(err) {
                console.log('error starting workload', err);
                $('#seedDataStartError').show();
            });
        }
    });

    return {
        setActiveDatabaseManager: setActiveDatabaseManager,
        displayWorkloadsForDatabase: displayWorkloadsForDatabase,
        updateStateForWorkload: updateStateForWorkload
    }
}