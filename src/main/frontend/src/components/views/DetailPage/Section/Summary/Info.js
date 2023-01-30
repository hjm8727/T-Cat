import { CaretRightOutlined } from '@ant-design/icons';
import React, { useEffect, useState } from 'react';
import styled from 'styled-components';
import Modal from '../../../../../util/Modal/Modal';
import MapModalBody from './MapModal';

const InfoStyle = styled.div`
    * {
        font-size: 16px;
    }
    div {
        margin-top: 4rem;
    }
    .infoDesc {
        margin-top: 1rem;
        margin-left: 1.5rem;
    }
    .name {
    }
    .price {
        color: #ED4037;
        margin-left: 1.5rem;
        font-weight: bold;
    }
    td{
        width: 150px;
        height: 35px;
    }
    .info {
        padding: 0 0 0 32px;
    }
    .infoBtn {
        margin-left: 1.7rem;
    }
    svg {
        vertical-align: baseline;
    }
    .modal > section > header button {
    top: 8px;
    right: 8px;
    }
    .modal > section {
        height: auto;
    }
    @media (max-width: 1225px) {
    .infoPriceList {
        margin-left: 1rem;
    }
    .modal > section > main > div > div{
        margin-top: 0;
    }

}
`;

// 상세페이지 상단 공연 정보
function Info(props) {
    const [modalOpen, setModalOpen] = useState(false);
    const [kAge, setKage] = useState(props.kage);
    const [inter, setInter] = useState(props.break);
    const [openR, setopenR] = useState(props.end);

    const openModal = () =>{
        setModalOpen(true)
    }
    const closeModal = () =>{
        setModalOpen(false)
    }
    useEffect(() => {
        setKage(props.kage);
        setInter(props.break);
        setopenR(props.end);
    },[props.kage, props.break, props.end])

    return (
        <InfoStyle>
        <div>
            <ul className="info" style={{listStyle: 'none'}}>
                <li className="infoItem">
                    <strong className="infoLabel">장소</strong>
                <span className="infoDesc">
                    <span className="infoBtn" data-popup="info-place" role="button" onClick={openModal}>
                    {props.loc}<CaretRightOutlined/>
                    </span>
                </span>
                </li>
                <br/>
            
                <li className="infoItem">
                    <strong className="infoLabel">공연기간</strong>
                    <span span='true' className="infoDesc">
                    {openR === "OPENRUN" ? 
                    <span className="infoText">{props.start} ~ 오픈런</span> :
                    <span className="infoText">{props.start} ~ {props.end === 'null' ? '당일 공연' : props.end}</span>
                    }
                </span>
                </li>
                <br/>

                <li className="infoItem"><strong className="infoLabel">공연시간</strong>
                    <span className="infoDesc">
                        {inter === 0 ? <span className="infoText">{props.time}분</span> :
                        <span className="infoText">{props.time}(인터미션 {inter}분 포함)</span>
                        }
                    </span>
                </li>
                <br/>

                <li className="infoItem">
                    <strong className="infoLabel">관람연령</strong>
                    <span className="infoDesc">
                        {kAge === true ? <span className="infoText">{props.age}세이상 관람가능</span> :
                        <span className="infoText">만 {props.age}세이상 관람가능</span>
                        }
                    </span>
                </li>
                <br/>
            
                <li className="infoItem infoPrice">
                    <strong className="infoLabel">가격</strong>
                    <div className="infoDesc">
                        {props.seat && props.seat.map((seats, index) => (
                                <table className="infoPriceList" key={index}>
                                <tbody>
                                    <tr >
                                        <td className="name">{seats.seat}</td>
                                        <td className="price">{seats.price}</td>
                                    </tr>
                                </tbody>
                                </table>
                        ))}
                    </div>
                </li>
            </ul>
        </div>
        <Modal open={modalOpen} close={closeModal} header={"찾아오시는 길"}><div>{<MapModalBody loc2={props.loc2}/>}</div></Modal>
        </InfoStyle>
    )
}

export default Info;