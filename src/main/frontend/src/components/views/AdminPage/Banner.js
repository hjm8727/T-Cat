import { useState, useEffect } from "react";
import NavBar from "./Tool/TopBar";
import styled from "styled-components";
import { storage } from "./Tool/Firebase";
import {ref, listAll, getDownloadURL,deleteObject, uploadString} from "firebase/storage";
import {v4, v4 as uuidv4} from "uuid";
import { useNavigate } from "react-router-dom";


const Banner=()=>{
        const [attachment, setAttachment] = useState("");
        const [imageList, setImageList] =useState([]);
        const Navigate = useNavigate();
        
        let attachmentUrl = "";
        //사진 첨부 없이 텍스트만 트윗하고 싶을 때도 있으므로 기본 값을 ""로 해야한다.
        //트윗할 때 텍스트만 입력시 이미지 url ""로 비워두기 위함

        const onChangeImg = (e) => {                    
            const {
            target: { files },
            } = e;
            const theFile = files[0];
        
            const reader = new FileReader();
            reader.onloadend = (finishedEvent) => {
                const {
                currentTarget: { result },
            } = finishedEvent;
            setAttachment(result);
        }
        reader.readAsDataURL(theFile);
        };

        const onSubmit = async (e) => {
            e.preventDefault();
            //이미지 첨부하지 않고 텍스트만 올리고 싶을 때도 있기 때문에 attachment가 있을때만 아래 코드 실행
            //이미지 첨부하지 않은 경우엔 attachmentUrl=""이 된다.
            if (attachment !== "") {
                //파일 경로 참조 만들기
                const attachmentRef = ref(storage, `/image/${uuidv4()}`);
                //storage 참조 경로로 파일 업로드 하기
                const response = await uploadString(attachmentRef, attachment, "data_url");
                //storage 참조 경로에 있는 파일의 URL을 다운로드해서 attachmentUrl 변수에 넣어서 업데이트
                attachmentUrl = await getDownloadURL(response.ref);
                getDownloadURL(response.ref).then((url)=>{
                    setImageList((prev)=>[...prev,url])
                    alert("업로드 성공");
                    setAttachment(null);
                })
            }
    };
    
    useEffect(()=>{
            const imageListReg=ref(storage,"image/");
            listAll(imageListReg).then((response)=>{
                response.items.forEach((item)=>{
                    getDownloadURL(item).then((url)=>{
                        setImageList((prev)=>[...prev,url])
                    })
                })
            })
        },[])
        
        const onSelecet = (e) =>{
            setAttachment(e)
        }
        
        const onDelete = async () => {
            const urlRef = ref(storage, attachment);
            try {
                if (attachment !== "") {
                    await deleteObject(urlRef);
                    alert("삭제 성공");
                    Navigate(0);
                }
            } catch (error) {
                window.alert("이미지를 삭제하는 데 실패했습니다!");
            }
        }

    return(
        <BannerBlock>
            <div className="BannerContainer">
            <NavBar className="BannerNav" name="광고/배너 관리"/>
                <div className="SelectBox">
                    <input type="file" onChange={onChangeImg}/>
                    <div>
                        <button onClick={onSubmit}> 업로드하기</button>
                        <button onClick={onDelete}>삭제하기</button>
                    </div>
                </div>
                <div className="BannerContent">

                <div className="SelectItem">

            {attachment && (
                    <img src={attachment} alt=""/>
                    )}
                </div>
            {imageList.map((url) => (
                <ul>
                    <li><img src={url} alt="" onClick={()=>{onSelecet(url)}}/></li>
                </ul>
        ))} 

        </div>
        </div>
        </BannerBlock>
    );
}
export default Banner;

const BannerBlock=styled.div`
    width: 100%;
    .BannerContainer{
        width: 80%;
        margin: 0 auto;
    }
    .BannerContent{
        padding:20px;
        border: 1px solid black;
    }

    button{
        border: 1px solid black;
        margin: 0px 10px;
    }
    input{
        margin: 0px 10px;
    }
    .SelectBox{
        display: flex;
        justify-content: space-between;
        align-items: center;
        border: 1px solid black;
        border-bottom: 0px;
        height:40px;

    }
    .SelectItem{
        width: 60%;
        border: 1px solid black;
        min-height: 300px;
        margin: 20px auto;
    }
    img{
        cursor: pointer;
        width: 100%;
        height: 300px;
    }
    li {
        margin: 20px;
        list-style: none;
    }
`;