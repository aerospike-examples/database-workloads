const serviceInterface = (mock = false) => {
    const headers = {
            "Content-Type": "application/json; charset=utf-8"
        }

    const testInterface = () => {
        return mock ?
            console.log('mock is true')
            :
            console.log('mock is false')
    }

    const getDatabases = async () => {
        return mock ?
            new Promise(function(resolve, reject) {
                setTimeout(() => resolve(['Aerospike', 'Redis']), 100);
            })
            :
            fetch("/demo/api/getDatabases",{
                headers,
                method: "GET",
            });
    }

    const getDatabaseVersions = async (databaseName) => {
        const params = new URLSearchParams({ databaseName });
        return mock ?
            function(){
                if (databaseName === "Aerospike") {
                    return ['Managed', 'Standard'];
                }
                else {
                    return ['Standard'];
                }   
            }
            : 
            fetch("/demo/api/getDatabaseVersions?" + params,{
                headers,
                method: "GET",
            });
    }

    const getDatabaseParameters = async (databaseName, version) => {
        const params = new URLSearchParams({ databaseName, version });
        return mock ?
            [
                {name: 'host', defaultValue: '127.0.0.1', description:"Host identity", label: "Host", promptText: "", required: true, selectionOptions: null, type:'STRING'},
                {name: 'port', defaultValue: 3000, description:"Port To Connect", label: "Port", promptText: "Port", required: true, selectionOptions: null, type:'INTEGER'},
                {name: 'user', defaultValue: null, description: null, label: "User", promptText: "Database Username", required: false, selectionOptions: null, type:'STRING'},
                {name: 'password', defaultValue: null, description: null, label: "Password", promptText: "Database Password", required: false, selectionOptions: null, type:'PASSWORD'},
                {name: 'useTls', defaultValue: true, description: null, label: "Use TLS", promptText: null, required: false, selectionOptions: null, type:'BOOLEAN'},
                {name: 'droplist', defaultValue: "two", description: null, label: "DropList", promptText: "", required: true, selectionOptions: ['one', 'two', 'three'], type:'SELECTION'}
            ]
            : 
            fetch("/demo/api/getDatabaseConfigParams?" + params,{
                headers,
                method: "GET",
            });
    }

    const connectDatabase = async (parameters) => {
        return mock ? 
            true
            :
            fetch("/demo/api/connect",{
                headers,
                body: JSON.stringify(parameters),
                method: "POST"
            })
    }

    const startWorkload = async (databaseName, jobName, params) => {
        let details = {
            name : databaseName,
            jobName: jobName,
            parameters: params
        };
        return mock ?
            true
            :    
            fetch("/demo/api/startWorkload",{
                headers,
                body: JSON.stringify(details),
                method: "POST"
            })
    }

    const pauseWorkload = async (databaseName) => {
        return mock ?
            true
            :
            fetch("/demo/api/pauseWorkload",{
                headers,
                body: {
                    name: databaseName
                },
                method: "GET"
            });
    }

    const resumeWorkload = async (databaseName) => {
        return mock ?
            true
            :
            fetch("/demo/api/resumeWorkload",{
                headers,
                body: {
                    name: databaseName
                },
                method: "GET"
            });
    }

    const stopWorkload = async (databaseName) => {
        return mock ?
            true
            :
            fetch("/demo/api/stopWorkload",{
                headers,
                body: {
                    name: databaseName
                },
                method: "GET"
            });
    }

    const getServerStatuses = async () => {
        return mock ?
            true
            :
            fetch("/demo/api/getServerStatuses",{
                headers,
                method: "GET"
            });
    }

    const getTimingSamples = async (name, since) => {
        return mock ? 
            []
            :
            fetch("/demo/api/samples",{
                headers,
                body: {
                    name: name,
                    since: since
                },
                method: "GET"
            });
    }

    const getServerStatus = async (activeDataseName, since) => {
        return mock ?
            {
                activeWorkloadTenthsPercentComplete: -1,
                activeWorkloadTimings: [],
                workloadStates: {}
            }
            :
            fetch("/demo/api/status",{
                headers,
                body: {
                    name: activeDataseName,
                    since
                },
                method: "GET"
            });
    }

    return {
        testInterface,
        getDatabases,
        getDatabaseVersions,
        getDatabaseParameters,
        connectDatabase,
        startWorkload,
        pauseWorkload,
        resumeWorkload,
        stopWorkload,
        getServerStatuses,
        getTimingSamples,
        getServerStatus
    };
}

export default serviceInterface;