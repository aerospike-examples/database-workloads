let STATE_READY = 1;
let STATE_RUNNING = 2;
let STATE_PAUSED = 3;
let STATE_SELECTED = 0x100;  

let currentSelection = "";

function createActiveDatabaseManager(services, activeWorkloadManager) {
    const ID_NAME_PREFIX = 'activeDatabaseConnection_';
    let activeDatabases = [];

    function findDatabase(name) {
        for (let i = 0; i < activeDatabases.length; i++) {
            if (activeDatabases[i].name === name) {
                return activeDatabases[i];
            }
        }
        return null;
    }

    function getWidgetFromName(name) {
        return $(`.databases .${name}`);
    }

    function addDatabase(params, ignoreSelection) {
        $(".databases").append(`<button type="button" class="btn btn-outline-success w-auto existing-database ${params.name}" id="${ID_NAME_PREFIX}${params.name}">
            <i class="fa-solid fa-database d-block"></i>${params.name}</button>`);
        params.state = STATE_READY;

        activeDatabases.push(params);
        let state = STATE_READY;
        if (!ignoreSelection) {
            state |= STATE_SELECTED;
        }
        setDatabaseState(params.name, state);

    }

    function setDatabaseSelected(database) {
        let $widget = getWidgetFromName(database.name);
        if ($widget) {
            // First unset the current database as active
            if (currentSelection) {
                let $currentWidget = getWidgetFromName(currentSelection);
                $currentWidget.removeClass('btn-success').addClass('btn-outline-success');
            }
            $widget.removeClass('btn-outline-success').addClass('btn-success');
            currentSelection = database.name;

            activeWorkloadManager.displayWorkloadsForDatabase(database);
        }
    }

    function setDatabaseState(name, state) {
        let database = findDatabase(name);
        if (database) {
            let selected = (state & STATE_SELECTED) === STATE_SELECTED;
            if (selected) {
                setDatabaseSelected(database);
            }

            let exectionState = (state & (~STATE_SELECTED));
            if (exectionState === 0) {
                // This is just making it active, don't both with the state
                return;
            }
            let $databaseWidget = getWidgetFromName(name);
            let $iconWidget = $databaseWidget.find('i');
            $iconWidget.removeClass('fa-database fa-person-running fa-pause');
            switch (exectionState) {
            case STATE_READY:
                $iconWidget.addClass("fa-database");
                break;
            case STATE_RUNNING:
                $iconWidget.addClass("fa-person-running");
                break;
            case STATE_PAUSED:
                $iconWidget.addClass("fa-pause");
                break;
            }
            database.state = exectionState;
        }
    }

    function reconcileServerStatus(serverStatus) {
        console.log(serverStatus);
        for (const workloadName in serverStatus) {
            if (serverStatus.hasOwnProperty(workloadName)) {
                let details = serverStatus[workloadName];
                let database = findDatabase(workloadName);
                if (!database) {
                    addDatabase({name:workloadName}, true);
                }
                let serverState = details;
                let stateToSet = serverState == 'RUNNING' ? STATE_RUNNING : serverState == 'PAUSED' ? STATE_PAUSED : STATE_READY;
                setDatabaseState(workloadName, stateToSet)
                activeWorkloadManager.updateStateForWorkload(workloadName, stateToSet);
            }
        }
    }

    function getCurrentDatabaseName() {
        return currentSelection;
    }
    $(".databases").on('click','.existing-database', function() {
        let $this = $(this);
        let id = $this.attr('id');
        let name = id.substring(ID_NAME_PREFIX.length);
        setDatabaseState(name, STATE_SELECTED);
    })

    return {
        addDatabase : addDatabase,
        findDatabase: findDatabase,
        setDatabaseState : setDatabaseState,
        reconcileServerStatus : reconcileServerStatus,
        getCurrentDatabaseName : getCurrentDatabaseName
    };
}
