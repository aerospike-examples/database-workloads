function createConnectionManager(services, activeDatabaseManager, callbackWhenConnected) {
    let currentDatabase;
    let currentVersion;
    let configParams;
    const ID_NAME_PREFIX = 'databaseConnectionControls_';

    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    const forms = document.querySelectorAll('.needs-validation')

    // Loop over them and prevent submission
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault()
                event.stopPropagation()
            }

            form.classList.add('was-validated')
        }, false)
    });

    function setDisplayValues(valueList, $parent) {
        if (valueList.length == 1) {
            $parent.find("select").hide();
            let $input = $parent.find("input");
            $input.show();
            $input.val(valueList[0]);
        }
        else {
            $parent.find("input").hide();
            let $select = $parent.find("select");
            $select.show();
            $select.empty();
            for (let i = 0; i < valueList.length; i++) {
                $select.append(`<option value="${valueList[i]}">${valueList[i]}</option>`);
            }
        }
    }

    function createGuiFromParameter(param, currentValue, readonly) {
        let valueToSet = (currentValue !== null && currentValue !== undefined) ? currentValue : (param.defaultValue) ? param.defaultValue : "";
        let html = `<div class="mb-3 row config-param-container"><label for="${param.name}" class="col-sm-2 col-form-label">${param.label}</label><div class="col-sm-10">`;
        switch (param.type) {
        case 'STRING':
            html += `<input id="${ID_NAME_PREFIX}${param.name}" name="${param.name}" type="text" class="form-control config-param" placeholder="${param.promptText}" value="${valueToSet}">
            <div class="invalid-feedback">${param.label} muse be specified</div>`;
            break;
        case 'INTEGER':
            html += `<input id="${ID_NAME_PREFIX}${param.name}" name="${param.name}" type="number" class="form-control config-param" placeholder="${param.promptText}" value="${valueToSet}">
            <div class="invalid-feedback">${param.label} muse be specified</div>`;
            break;
        case 'PASSWORD':
            html += `<input id="${ID_NAME_PREFIX}${param.name}" name="${param.name}" type="password" class="form-control config-param" placeholder="${param.promptText}" value="${valueToSet}">
            <div class="invalid-feedback">${param.label} muse be specified</div>`;
            break;
        case 'BOOLEAN':
            html += `<input id="${ID_NAME_PREFIX}${param.name}" name="${param.name}" type="checkbox" class="form-check-input config-param" placeholder="${param.promptText}" ${valueToSet ? ' checked' : ''}>`;
            break;
        case 'SELECTION':
            html += `<select id="${ID_NAME_PREFIX}${param.name}" name="${param.name}" class="form-select config-paam">`;
            for (let i = 0; param.selectionOptions && i < param.selectionOptions.length; i++) {
                let thisValue = param.selectionOptions[i];
                html += `<option value="${thisValue}" ${thisValue ===valueToSet ? ' selected':''}>${thisValue}</option>`
            }
            html += `</select><div class="invalid-feedback">${param.label} muse be specified</div>`;
        }
        html += '</div></div>';
        $("#databaseConnectionControls").append(html);
    }
    async function loadParameters() {
        let params = await services.getDatabaseParameters(currentDatabase, currentVersion);
        $("#databaseConnectionControls").find(".config-param-container").remove();
        for (let i = 0; i < params.length; i++) {
            createGuiFromParameter(params[i], null, false);
        }
        configParams = params;
    }

    async function loadDatabaseVersions() {
        try {
            let versions = await services.getDatabaseVersions(currentDatabase);
            setDisplayValues(versions, $('#databaseVersionGroup'));
            currentVersion = versions[0];
            loadParameters();
        }
        catch (err) {

        }
    }
    async function loadDatabases() {
        try {
            let databases = await services.getDatabases();
            setDisplayValues(databases, $('#databaseNameGroup'));
            currentDatabase = databases[0];
            loadDatabaseVersions();
        } catch (err) {

        }
    }
    function isConfigNameUnique(configName) {
        return !activeDatabaseManager.findDatabase(configName);
    }

    function validateForm()  {
        let isValid = true;
        let $control = $("#configName");
        $control.next().toggle(!$control.val() || !isConfigNameUnique($control.val()));
        for (let i = 0; i < configParams.length; i++) {
            let thisParam = configParams[i];
            if (thisParam.required) {
                switch (thisParam.type) {
                    case 'STRING':
                    case 'INTEGER':
                    case 'PASSWORD':
                    case 'SELECTION':
                        $control = $("#databaseConnectionControls_" + thisParam.name);
                        $control.next().toggle(!$control.val());
                        break;

                }
            }
        }
        $("#databaseConnectionControls").find(".invalid-feedback").each(function(i, e) {
            if ($(e).is(":visible")) {
                isValid = false;
            }
        });
        return isValid;
    }

    function findConfigType(id) {
        for (let i = 0; i < configParams.length; i++) {
            if (configParams[i].name === id) {
                return configParams[i].type;
            }
        } 
        return 'STRING';
    }

    function getConnectionDetails() {
        let map = {
            name: $("#configName").val(),
            database: currentDatabase,
            version: currentVersion,
            configParams: {}
        }
        $("#databaseConnectionControls .config-param").each(function(i, e) {
            let $control = $(e);
            let id = $control.attr("id").substring(ID_NAME_PREFIX.length);  // Trim off the unique differentiator
            let val = $control.val();
            if ($control.attr('type') === 'checkbox') {
                val = $control.is(":checked");
            }
            let type = findConfigType(id);
            if (type === 'INTEGER') {
                val = parseInt(val);
            }
            map.configParams[id] = val;
        });
        return map;
    }
    async function validateConnection() {
        $("#connectionError").hide();
        let details = getConnectionDetails();
        try {
            await services.connectDatabase(details);
            return true;
        }
        catch (err) {
            console.log("error connecting to database", err);
            $("#connectionError").empty().append("Error connecting to database");
            $("#connectionError").show();
            return false;
        }
    }

    function registerControls() {
        $("#databaseNameGroup select").on('change', function() {
            let optionSelected = this.value;
            currentDatabase = optionSelected;
            loadDatabaseVersions();
        });
        $("#databaseVersionGroup select").on('change', function() {
            let optionSelected = this.value;
            currentVersion = optionSelected;
            loadParameters();
        });

        $("#newDatabaseConnectionFSubmit").on('click', function() {
            if (validateForm()) {
                validateConnection().then(function(result) {
                    if (result) {
                        $("#createConnection").modal('hide');
                        if (callbackWhenConnected) {
                            callbackWhenConnected(getConnectionDetails());
                        }
                    }
                });
            }
        })
    }

    function show() {
        $("#createConnection").modal('show');
        $("#configName").val("");
        $("#connectionError").empty();
        loadDatabases();
    }

    registerControls();
    return {
        show: show
    };
};
