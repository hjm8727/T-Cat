import React, { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import styled from 'styled-components';
import AdminApi from '../../../../../api/AdminApi';

const ChartBlock=styled.div`
    .chart{
    padding: 20px;
}
.chartTitle{
    margin-bottom: 30px;
}
.button-53{
  margin-bottom: 10px;
}

`;
const Chart = (props) => {
  const [chartData, setChartData] = useState([]);
  const [chart, setChart] = useState([]);
  
  useEffect(() => {
    const getChartData = async()=> {
      try {
          const res = await AdminApi.getChart();
          if(res.data.statusCode === 200){
            setChartData([...chartData, ...res.data.results]);
            console.log(res.data.results);
            if(chart.length === 0) {
              const mapChart = chartData.reverse().map((data) => {
                return {
                  index: data.id,
                  income: data.cumuAmount,
                  discount: data.cumuDiscount,
                  all: data.finalAmount
                }
              }); 
              setChart(mapChart); 
            }} 
          } catch (e) {
            console.log(e);
          }
      }
      getChartData();
  }, [chart]);
  console.log(chart);

    return (
        <ChartBlock>
        <div className='chart'>
        <button class="button-53" type="button">누적 차트</button>
        <ResponsiveContainer width="100%" aspect={4/1}>
        <BarChart
          data={chart}
          margin={{
            top: 10,
          }}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="index"/>
          <Tooltip />
          <Legend />
          <Bar yAxisId="left" dataKey="income" fill="#FD8A8A" />
          <Bar yAxisId="left" dataKey="discount" fill="#F1F7B5" />
          <Bar yAxisId="left" dataKey="all" fill="#A8D1D1" />
        </BarChart>
      </ResponsiveContainer>
      </div>
      </ChartBlock>

    );
  }
export default Chart;
