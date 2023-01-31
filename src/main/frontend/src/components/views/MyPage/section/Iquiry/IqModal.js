import React, { useState } from 'react';
import { useNavigate} from "react-router-dom";
import styled from 'styled-components';

const IqModal = (props) => {
    const navigate = useNavigate();

    const [inputReply, setInputReply] = useState("");

    const { open, close, header } = props;
    return (
        <QWrap>

        <div className={open ? 'openModal modal' : 'modal'}>
            {open && 
                <section>
                    <header>
                        {header}
                        <button className='close' onClick={close}>
                            &times;
                        </button>
                    </header>
                    <main>
                    {props.children}
                    </main>
                    <footer>
                        <button className='close' onClick={close}>Close</button>
                    </footer>
                </section>
            }
        </div>
        </QWrap>

    );
};
export default IqModal;

const QWrap=styled.div`
.modal > section > header button {
    top: 10px;
}
`;