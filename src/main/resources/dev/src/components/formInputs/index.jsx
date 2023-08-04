import React from "react";
import styles from './index.module.css';
import clsx from "clsx";

const Input = ({label, type, onChange, placeholder, value, className, required, disabled, ...props}) => {
    return(
        <label className={clsx(styles.label, className)}>
            <span className={styles.span}>{label}</span>
            <input className={styles.input} type={type} onChange={onChange} placeholder={placeholder} value={value} required={required} disabled={disabled} {...props}/>
        </label>
    )
}

const Select = ({label, options, onChange, value, className, required, disabled}) => {
    return(
        <label className={clsx(styles.label, className)}>
            <span className={styles.span}>{label}</span>
            <select className={styles.select} onChange={onChange} value={value} required={required} disabled={disabled} >
                {options.map((opt, idx) => (
                    <option value={typeof opt === 'object' ? opt.value : opt} key={idx}>{typeof opt === 'object' ? opt.label : opt}</option>
                ))}
            </select>
        </label>
    )
}

export {Input, Select};