import React from 'react';
import { Button, Form, Input} from 'antd';
import TextArea from 'antd/es/input/TextArea';
import { useState } from 'react';
import MemberApi from '../../../../../api/MemberApi';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';


const Contact = () => {
    const userInfo = useSelector((state) => state.user.info);
    const navigate = useNavigate();
    const [inputSelect, setInputSelect] = useState("티켓예매/발권");
    const [inputQnaTitle, setInputQnaTitle] = useState("");
    const [inputQnaContent, setInputQnaContent] = useState("");

    const onChangeSelect=(e)=>{setInputSelect(e.target.value);}
    const onChangeQnaTitle=(e)=>{setInputQnaTitle(e.target.value);}
    const onChangeQnaContent=(e)=>{setInputQnaContent(e.target.value);}
    console.log("셀렉트 표시 : " + inputSelect);
    console.log("회원인덱스 : " + userInfo.userIndex);

    const onClickReply=async(e)=>{
        if(inputQnaContent.length <= 5 || inputQnaContent.length >= 1000) {
        alert('문의 사항을 최소 5 ~ 1000글자 이내로 부탁드립니다.');
        e.preventDefault();
    } else {
        const res = await MemberApi.sendQna(userInfo.userIndex,inputSelect,inputQnaTitle,inputQnaContent);
        if(res.data.statusCode === 200) {
            console.log(res.data.message);
            alert('문의가 정상 전송 되었습니다.');
            navigate('/MyPage/IqList')
        } else {
            alert('문의 전송이 실패 하였습니다.');
            console.log("전송 실패");
            e.preventDefault();
        }
    }
}

    return (
        <Form
            labelCol={{span: 4,}}
            wrapperCol={{span: 14,}}>
        <div style={{margin : '20px 0px'}}>
        <Form.Item label="문의 항목">
        <select style={{ width: 220 }} value={inputSelect} onChange={onChangeSelect}>
            <option>티켓예매/발권</option>
            <option>취소/환불</option>
            <option>신고</option>
        </select>
        </Form.Item>
                <Form.Item label="문의 제목">
                    <Input type='text' onChange={onChangeQnaTitle} value={inputQnaTitle}/>
                </Form.Item>
                <Form.Item label="문의 내용">
                    <TextArea rows={5} style={{resize:'none'}} onChange={onChangeQnaContent} value={inputQnaContent}/>
                </Form.Item>
            </div>
            <Form.Item label="문의">
                <Button onClick={onClickReply}>제출</Button>
            </Form.Item>
        </Form>
    );
};
export default Contact;