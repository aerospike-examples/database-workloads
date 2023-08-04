import React, { useEffect, useState } from "react";
import styles from './index.module.css';
import Modal from "../modal";
import { Input, Select } from "../formInputs";

const Connect = ({services}) => {
    const [show, setShow] = useState(false);
    const [dbList, setDbList] = useState([]);
    const [dbVers, setDbVers] = useState([]);
    const [config, setConfig] = useState([])
    const [name, setName] = useState('');
    const [database, setDatabase] = useState('');
    const [version, setVersion] = useState('');
    const [configParams, setConfigParams] = useState({});
    const [error, setError] = useState(false);
    const inputs = {
        STRING: "text",
        INTEGER: "number",
        BOOLEAN: "checkbox",
        PASSWORD: "password"
    }
    const getDbs = async () => {
        services.getDatabases()
        .then(async (res) => {
            let dbs = await res.json();
            getVers(dbs[0]);
            setDbList(dbs);
            setDatabase(dbs[0]);
        });
    }

    const getVers = async (db) => {
        services.getDatabaseVersions(db)
        .then(async (res) => {
            let vers = await res.json();
            getConf(db, vers[0]);
            setDbVers(vers);
            setVersion(vers[0]);
        })
    }

    const getConf = async (db, vers) => {
        services.getDatabaseParameters(db, vers)
        .then(async (res) => {
            let conf = await res.json();
            setConfig(conf);
            let tmp = {};
            for (let c of conf) {
                tmp[c.name] = c.defaultValue ?? null;
            }
            setConfigParams(tmp);
        })
    } 

    const updateDatabase = async (db) => {
        setDatabase(db);
        getVers(db); 
    }
    
    const updateVersion = async (vers) => {
        setVersion(vers);
        getConf(database, vers); 
    }

    useEffect(() => {
        getDbs();
    }, [])

    useEffect(() => {
        if(error){
            setError(false);
        }
    }, [show]);
    
    const updateConfig = (key, value, type) => {
        setConfigParams(prev => ({
            ...prev,
            [key]: type === 'BOOLEAN' ? !configParams[key] : value
        }))
    }    

    const handleConnect = async (e) => {
        e.preventDefault();
        let res = await services.connectDatabase({name, database, version, configParams})
        let data = await res.json();
        if(data.status !== 200){
            console.log("Error connecting to database", data);
            setError(true);
        }
        else{
            setShow(false)
        }
    }

    return(
        <div className={styles.connection}>
            <button className="btn btnPrimary" onClick={() => setShow(!show)}>+ Create Connection</button>
            <Modal show={show} setShow={setShow} title="Create database connection" buttons={[{name: "Save", className: "btn btnPrimary", type: "submit", form: "connect"}]}>
                <form id="connect" className={styles.connect} onSubmit={handleConnect}>
                    <Input type="text" placeholder="Unique config name" onChange={(e) => setName(e.currentTarget.value)} label="Name" value={name} required/>
                    <Select label="Database" onChange={(e) => updateDatabase(e.currentTarget.value)} value={database} options={dbList} required/>
                    <Select label="Version" onChange={(e) => updateVersion(e.currentTarget.value)} value={version} options={dbVers} required/>
                    {config.map((param, idx) => {
                        const { description, label,  name, promptText, required, selectionOptions, type } = param;
                        return(
                            selectionOptions ?
                            <Select label={label} value={configParams[name]} title={description} onChange={(e) => updateConfig(name, e.currentTarget.value)} required={required} options={selectionOptions} key={idx} />
                            :
                            <Input type={inputs[type]} label={label} value={configParams[name] ?? ""} title={description} placeholder={promptText} onChange={(e) => updateConfig(name, e.currentTarget.value, type)} required={type !== "BOOLEAN" && required} key={idx} />
                        )
                    })}
                </form>
                {error && <span className={styles.error}>The was an error connecting to the database. Please try again.</span>}
            </Modal>
        </div>
    )
}   

export default Connect;