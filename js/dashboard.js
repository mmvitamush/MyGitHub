$(function(){
	$('#myTab a:last').tab('show');
	//WebSocketの使用準備
	var ws = new WebSocket("ws://www.vita-factory.com/ws");
	ws.onopen = function(){
   		console.log('ws_open_on_js');   
	};
	ws.onmessage = function(evt){
		var data = JSON.parse(evt.data);//json文字列からオブジェクトに変換
		console.log(data);
		if(typeof data.celsius != undefined) {
			chgTable(data.line,data.lineno,data.celsius,data.humidity,'now_p');
		}
	};
	ws.onclose = function(){
		console.log('ws_close_on_js');
	};

	var d_cels = [];//温度グラフデータ
	var d_humd = [];//湿度グラフデータ
	var totalPoints = 50;//グラフに表示される点の最大数
	var dataset;
	
	//グラフの基本設定
	var chart_option = {
		series: { shadowSize:0,lines:{show:true},points:{show:true}},
       grid:{clickable:true,hoverable:true,backgroundColor:"#F5F5F5",borderColor:"#DDDDDD"},
       yaxis: {min:0,max:80,tickColor:"#CCCCCC",font:{size:13,color:"#333333"}},
       legend:{show:true,position:"nw"},
       xaxis: {ticks:5, show: true, mode: "time", timeformat: "%Y/%m/%d %H:%M:%S",tickColor:"#999999"}
	};
	var mydata = [
              [1309446000000, 6],
              [1312124400000, 9],
              [1314802800000,18],
              [1317394800000,23],
              [1320073200000,27],
              [1322665200000,27],
              [1325343600000,31],
              [1328022000000,35],
              [1330527600000,38],
              [1333206000000,44],
              [1335798000000,49],
              [1338476400000,50]
             ];
	
	//日付選択用グラフを描画
	if($('#chart-tab').hasClass('active')){
		$.plot($('#chartline1A'),[mydata],chart_option);
	}

	$('.samebtn').click(function(){
		var i = $(this).attr('id');
	  	var target = $('#line'+i.slice(4));
	  	var t = target.offset().top-50;
	  	$('html,body').animate({scrollTop:t},"fast");
	  	return false;
	});
	
function chgTable(line,lineno,celsius,humidity,mode){
	var n = "#line"+id;
	if(mode == "now_p"){
		$(n).next().children('tbody').children('tr:nth-child(lineno)').children('td:nth-child(3)').text(celsius);
		$(n).next().children('tbody').children('tr:nth-child(lineno+1)').children('td:nth-child(3)').text(humidity);
	} else if(mode == "set_p"){
		$(n).next().children('tbody').children('tr:nth-child(lineno)').children('td:nth-child(4)').text(celsius);
		$(n).next().children('tbody').children('tr:nth-child(lineno+1)').children('td:nth-child(4)').text(humidity);
	} else if(mode == "status") {
		$(n).next().children('tbody').children('tr:nth-child(lineno)').children('td:nth-child(5)').children().remove();
		$(n).next().children('tbody').children('tr:nth-child(lineno)').children('td:nth-child(5)').append("<span class='label label-primary'>test</span>");
		$(n).next().children('tbody').children('tr:nth-child(lineno+1)').children('td:nth-child(5)').children().remove();
		$(n).next().children('tbody').children('tr:nth-child(lineno+1)').children('td:nth-child(5)').append("<span class='label label-primary'>test</span>");
	}
};
});