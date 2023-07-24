serviceInterface = function() {

    async function getDatabases() {
        return $.ajax({
                url: "/demo/api/getDatabases",
                dataType: "json",
                method: "GET",
            });
    }

    async function getDatabaseVersions(databaseName) {
        return $.ajax({
            url: "/demo/api/getDatabaseVersions",
            dataType: "json",
            data: {
                databaseName: databaseName
            },
            method: "GET",
        });
    }

    async function getDatabaseParameters(databaseName, version) {
        return $.ajax({
            url: "/demo/api/getDatabaseConfigParams",
            dataType: "json",
            data: {
                databaseName: databaseName,
                version: version
            },
            method: "GET",
        });
    }

    async function connectDatabase(parameters) {
        return $.ajax({
            url: "/demo/api/connect",
            dataType: "json",
            data: JSON.stringify(parameters),
            contentType: "application/json; charset=utf-8",
            method: "POST"
        })
    }

    async function startWorkload(databaseName, jobName, params) {
        let details = {
            name : databaseName,
            jobName: jobName,
            parameters: params
        };
        return $.ajax({
            url: "/demo/api/startWorkload",
            dataType: "json",
            data: JSON.stringify(details),
            contentType: "application/json; charset=utf-8",
            method: "POST"
        })
    }

    async function pauseWorkload(databaseName) {
        return $.ajax({
            url: "/demo/api/pauseWorkload",
            dataType: "json",
            data: {
                name: databaseName
            },
            method: "GET"
        });
    }

    async function resumeWorkload(databaseName) {
        return $.ajax({
            url: "/demo/api/resumeWorkload",
            dataType: "json",
            data: {
                name: databaseName
            },
            method: "GET"
        });
    }

    async function stopWorkload(databaseName) {
        return $.ajax({
            url: "/demo/api/stopWorkload",
            dataType: "json",
            data: {
                name: databaseName
            },
            method: "GET"
        });
    }

    async function getServerStatuses() {
        return $.ajax({
            url: "/demo/api/getServerStatuses",
            dataType: "json",
            method: "GET"
        });
    }

    async function getTimingSamples(name, since) {
        return $.ajax({
            url: "/demo/api/samples",
            data: {
                name: name,
                since: since
            },
            dataType: "json",
            method: "GET"
        });
    }
    return {
        getDatabases: getDatabases,
        getDatabaseVersions: getDatabaseVersions,
        getDatabaseParameters: getDatabaseParameters,
        connectDatabase: connectDatabase,
        startWorkload : startWorkload,
        pauseWorkload : pauseWorkload,
        resumeWorkload : resumeWorkload,
        stopWorkload : stopWorkload,
        getServerStatuses : getServerStatuses,
        getTimingSamples : getTimingSamples
    };
} ();

serviceInterfaceMock = function() {
    async function getDatabases() {
        return new Promise(function(resolve, reject) {
            setTimeout(() => resolve(['Aerospike', 'Redis']), 100);
        })
    }

    async function getDatabaseVersions(databaseName) {
        if (databaseName === "Aerospike") {
            return ['Managed', 'Standard'];
        }
        else {
            return ['Standard'];
        }
    }
    async function getDatabaseParameters(databaseName, version) {
        return [
            {name: 'host', defaultValue: '127.0.0.1', description:"Host identity", label: "Host", promptText: "", required: true, selectionOptions: null, type:'STRING'},
            {name: 'port', defaultValue: 3000, description:"Port To Connect", label: "Port", promptText: "Port", required: true, selectionOptions: null, type:'INTEGER'},
            {name: 'user', defaultValue: null, description: null, label: "User", promptText: "Database Username", required: false, selectionOptions: null, type:'STRING'},
            {name: 'password', defaultValue: null, description: null, label: "Password", promptText: "Database Password", required: false, selectionOptions: null, type:'PASSWORD'},
            {name: 'useTls', defaultValue: true, description: null, label: "Use TLS", promptText: null, required: false, selectionOptions: null, type:'BOOLEAN'},
            {name: 'droplist', defaultValue: "two", description: null, label: "DropList", promptText: "", required: true, selectionOptions: ['one', 'two', 'three'], type:'SELECTION'},

        ];
    }
    async function connectDatabase(parameters) {
        return true;
    }

    async function startWorkload(databaseName, jobName, params) {
        return true;
    }

    async function pauseWorkload(databaseName) {
        return true;
    }

    async function resumeWorkload(databaseName) {
        return true;
    }

    async function stopWorkload(databaseName) {
        return true;
    }

    async function getServerStatuses() {
        return true;
    }

    async function getTimingSamples(since) {
        return [];
    }
    return {
        getDatabases: getDatabases,
        getDatabaseVersions: getDatabaseVersions,
        getDatabaseParameters : getDatabaseParameters,
        connectDatabase : connectDatabase,
        startWorkload : startWorkload,
        pauseWorkload : pauseWorkload,
        resumeWorkload : resumeWorkload,
        stopWorkload : stopWorkload,
        getServerStatuses : getServerStatuses,
        getTimingSamples : getTimingSamples
    };
}()