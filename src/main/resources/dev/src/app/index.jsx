import React, { useState } from 'react'
import styles from './index.module.css'
import Connect from '../components/connect';
import serviceInterface from '../components/services';
import LineChart from '../components/chart';
import { Input } from '../components/formInputs';

function App() {
  const [duration, setDuration] = useState('5');
  const mock = false;  
  const services = serviceInterface(mock);

  return (
    <div className={styles.app}>
      <Connect services={services}/>
      <div className={styles.chartContainer}>
        <div className={styles.chart}>
          <LineChart value={1} 
            datasets={[
              {label: "Avg", data: [], backgroundColor: "rgba(0, 128, 0, .5)", borderColor: "rgba(0, 128, 0, 1)"},
              {label: "Min", data: [], backgroundColor: "rgba(106, 195, 255, .5)", borderColor: "rgba(106, 195, 255, 1)"},
              {label: "Max", data: [], backgroundColor: "rgba(195, 22, 24, .5)", borderColor: "rgba(195, 22, 24, 1)"},
              {label: "95%", data: [], backgroundColor: "rgba(0, 0, 139, .5)", borderColor: "rgba(0, 0, 139, 1)"},
              {label: "99%", data: [], backgroundColor: "rgba(0, 191, 255, .5)", borderColor: "rgba(0, 191, 255, 1)"}
            ]} 
            duration={(duration * 60000)}
            title="Latency" />
        </div>
        <div className={styles.chart}>
          <LineChart value={1} 
            datasets={[
              {label: "Total", data: [], backgroundColor: "rgba(0, 128, 0, .5)", borderColor: "rgba(0, 128, 0, 1)"},
              {label: "Success", data: [], backgroundColor: "rgba(106, 195, 255, .5)", borderColor: "rgba(106, 195, 255, 1)"},
              {label: "Failed", data: [], backgroundColor: "rgba(195, 22, 24, .5)", borderColor: "rgba(195, 22, 24, 1)"}
            ]} 
            duration={(duration * 60000)}
            title="Throughput" />
        </div>
        <div className={styles.chartControl}>
          <Input label="Graph Duration" type='range' min='1' max='60' step='1' value={duration} onChange={(e) => setDuration(e.currentTarget.value)}/>
          <span>{duration} min</span>
        </div>
      </div>
    </div>
  )
}

export default App
