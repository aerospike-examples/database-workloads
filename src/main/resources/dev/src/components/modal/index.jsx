import React, { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import styles from './index.module.css';
import clsx from "clsx";

const Modal = ({show, setShow, title, children, danger, buttons = "default", className}) => {
    const modal = useRef();
    const [open, setOpen] = useState(false);
    const [visible, setVisible] = useState(false);

    const handleOutside = (e) => {
    	if(show && !modal.current.contains(e.target)){
            setShow(false);
    	}
	}

	const escFunction = (e) => {
		if(show && e.key === 'Escape'){
            setShow(false);
		}
	}

    useEffect(() => {
        if(!danger){
            document.addEventListener("mousedown", handleOutside);
            document.addEventListener("keydown", escFunction);

            return () => {
                document.removeEventListener("mousedown", handleOutside);
                document.removeEventListener("keydown", escFunction);
            }
        }
	}, [show, danger]);

    useEffect(() => {
        if(show){
            setVisible(true);
            setTimeout(() => setOpen(true), 100);
        }
        else{
            setOpen(false);
            setTimeout(() => setVisible(false), 300);
        }
    }, [show]);


    return(
        <>
        {visible && createPortal(
        <div className={styles.modal}>
        <div className={clsx(styles.modalBackground, open ? styles.visibleModal : styles.hiddenModal)} >    
        </div>
        <div ref={modal} className={clsx(styles.modalPopUp, open ? styles.visiblePopUp : styles.hiddenPopUp, className)}>
            <div className={styles.iconControls}>
                <div className={styles.icon} onClick={() => setShow(false)}><span>&#10005;</span></div>
            </div>
            <div className={styles.modalContainer}>
            {title && 
            <div className={styles.title}>
                <h2>{title}</h2>
            </div>}
            <div className={styles.modalContent}>
                {children}
            </div>
            </div>
            {typeof buttons === 'object' ?
            <div className={styles.controls}>
                {buttons.map((button, idx) => (
                    <button key={idx} {...button} >{button.name}</button>
                ))}
            </div>
            :
            <div className={styles.controls}>
                <button className="btn btnPrimary" onClick={() => setShow(false)} type="buton">OK</button>
            </div>}
        </div>
        </div>, document.getElementById('portal'))}
        </>
    )
}

export default Modal;
