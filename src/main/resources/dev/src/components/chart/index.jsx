import React from 'react';
import styles from './index.module.css'
import 'chartjs-adapter-luxon';
import { Chart, Title, Legend, registerables } from 'chart.js';
import { Line } from 'react-chartjs-2';
import zoomPlugin from 'chartjs-plugin-zoom';
import ChartStreaming from 'chartjs-plugin-streaming';
Chart.register(
    zoomPlugin,
    ChartStreaming,
    Title,
    Legend,
    ...registerables
);

const LineChart = ({value, datasets, duration, title}) => {
    const onRefresh = (chart) => {
        if(value){
            chart.data.datasets.forEach((dataset, idx) => {
                const now = Date.now();
                dataset.data.push({
                    x: now,
                    y: value + idx
                });   
            })
        }
    }

    const data = {
        type: 'line',
        datasets 
    }
    const options = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
                align: 'start',
                fullSize: false
            },
            zoom: {
                pan: {
                  enabled: true,
                  mode: 'x'
                },
                zoom: {
                  pinch: {
                    enabled: true
                  },
                  wheel: {
                    enabled: true
                  },
                  mode: 'x'
                },
                limits: {
                  x: {
                    minDelay: 1000,
                    maxDelay: 10000,
                    minDuration: 5000,
                    maxDuration: 360000
                  }
                }
              }
        },
        elements: {
            point: {
                radius: 0
            }
        },
        scales: {
            x: {
                type: 'realtime',
                realtime: {
                    delay: 2000,
                    refresh: 1000,
                    onRefresh,
                    duration,
                    ttl: 360000
                }
            }
        }
    }

    return (
        <div className={styles.chart}>
            <div className={styles.title}>
                <span className={styles.titleText}>{title}</span>
            </div>
            <Line data={data} options={options}/>
        </div>
    )
}

export default React.memo(LineChart);