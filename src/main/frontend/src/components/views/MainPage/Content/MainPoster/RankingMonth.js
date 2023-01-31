import styled from "styled-components";
import HorizontalScroll from 'react-horizontal-scrolling'
import { useEffect, useState } from "react";
import MainApi from "../../../../../api/MainApi";
import { Link } from "react-router-dom";


const PosterImgContainer = styled.div`        
    background-color: #f5f5f5;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow-x: auto;
    overflow-y: hidden;
    img{
        width: 110px;
        height: 130px;
    }
    ul{
        display: flex;
        list-style: none;
        margin: 0px;
        padding: 0px;
    }
    li{
        align-items: center;
        list-style: none;
        height: 280px;
        margin:0 15px;
    }
    li:hover{
        font-weight: bold;
    }
    p{
        width: 110px;
        margin-top:10px;
        font-size: 1em;
    }
    a{
        text-decoration:none;
        color : inherit;
    }
    @media (max-width : 1440px){
        img{
            width: 140px;
            height: 160px;
        }
        li{
            margin:0 10px;
        }
    }
`
const PosterCategoryContainer = styled.div`
    width: 100%;
    margin: 30px 0;
    .PosterTitle{
        margin: 5px;
    }
    h2{
        display: inline-block;
        font-size: 1.5em;
        font-weight: bold;
        margin: 0 1 0px;
    }
    li{
        font-size: 1.2em;
        display: inline-block;
        list-style: none;
        border-radius: 10px;
        padding :0.5em;
        cursor: pointer;
        transition: all 0.4s;
        margin: 0 5px;
    }
    li:hover{
        background-color: #86868b;
        color: white;
    }
    @media (max-width : 1440px){
        margin: 10px 0;
    }
    `


    const categories = [
        {
            name : 'MUSICAL',
            text : '뮤지컬'
        },
        {
            name : 'CLASSIC',
            text : '클래식 / 무용'
        },
        {
            name : 'DRAMA',
            text : '연극'
        },
        {
            name : 'EXHIBITION',
            text : '전시회'
        }
    ]

const RankingMonth = () =>{
    const [category , setCategory] = useState('MUSICAL');
    const [ItemData , setItemData] = useState([]);

    const onSelect = (e) =>{
        setCategory(e)
    }

    useEffect(() => {
        const PosterAsync = async() =>{
            try{
                const res = await MainApi.rankingMonth(category, 10);
                if(res.data.statusCode === 200){
                    setItemData(res.data.results)
                }
            }catch(e){
                console.log(e);
            }
        }
        PosterAsync();
    }, [category])
    
    return(
            <>
                <PosterCategoryContainer>
                    <div className="PosterTitle">
                        <h2>월간랭킹</h2>
                    {categories.map(c=>(
                        <li 
                            key={c.name}
                            onClick={()=>onSelect(c.name)}
                        >{c.text}</li>
                    ))}
                    </div>
                </PosterCategoryContainer>
                <div>
                                <HorizontalScroll>
                        <PosterImgContainer>
                            <ul>
                                {ItemData.map((ItemData , index)=>(
                                    <li key={index} >
                                    <Link to={`/detail/${ItemData.code}`} >
                                    <img src={ItemData.product.poster_url} code={ItemData.code} alt="이미지오류"/>
                                    <p>{ItemData.product.title}</p>
                                    </Link>
                                </li>
                                ))}
                            </ul>
                        </PosterImgContainer>
                                </HorizontalScroll>
                </div>
            </>
    )
}
export default RankingMonth;

