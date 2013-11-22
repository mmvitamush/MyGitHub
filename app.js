
/**
 * Module dependencies.
 */
var cels_up_c = 'gpio -g  write 4 ',
      cels_dw_c = 'gpio -g write 17 ',
      humd_up_c = 'gpio -g write 22 ',
      humd_dw_c = 'gpio -g write 25 ';

var express = require('express')
    , routes = require('./routes')
    , path = require('path')
    , serialport = require('serialport');
var async = require('async');

var app = express()
    , http = require('http');
var server = http.createServer(app);
var dbmongo = require('./models/database_mongo');
//各Raspiの存在するライン番号などが設定されているファイル
var conf = require('./server_config');

var io = require('socket.io').listen(server);
var exec = require('child_process').exec;    

// Serial Port
//var portName = '/dev/tty.usbmodemfd121'; // Mac環境
var portName = '/dev/ttyACM0'; // RaspberryPi環境
var sp = new serialport.SerialPort(portName, {
    baudRate: 9600,
    dataBits: 8,
    parity: 'none',
    stopBits: 1,
    flowControl: false,
	parser: serialport.parsers.readline("\n")
});

//node.jsからraspberry piのGPIOを操作するための準備
exec('gpio -g mode 17 out');//温度下げ装置
exec('gpio -g mode 4 out');//温度上げ装置
exec('gpio -g mode 25 out');//湿度度下げ装置
exec('gpio -g mode 22 out');//湿度上げ装置

// all environments

app.configure(function(){
    app.set('port', process.env.PORT || 3000);
    app.set('views', __dirname + '/views');
    app.set('view engine', 'jade');
    app.use(express.favicon());
    app.use(express.logger('dev'));
    app.use(express.bodyParser());
    app.use(express.methodOverride());
    app.use(express.cookieParser('your secret here'));
    app.use(express.session());
    app.use(app.router);
    app.use(express.static(path.join(__dirname, 'public')));
});

//development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}


app.get('/', routes.index);
//ライン管理画面
app.get('/line',routes.line);
//タスク管理画面
app.get('/tasks', routes.tasks);
//app.get('/users', user.list);
//スケジュール管理
app.get('/schedule',routes.schedule);

app.post('/topic/create',routes.createTopic);
app.get('/api/topics/list',routes.topicList);
app.post('/api/setting',routes.settingData);
app.post('/api/schedule_create',routes.scheduleCreate);
app.post('/api/schedule_read',routes.scheduleRead);

// クライアントの接続を待つ(IPアドレスとポート番号を結びつけます)
server.listen(app.get('port'),function(){
    console.log("Express server listening on port " + app.get('port'));
});


// クライアントが接続してきたときの処理
io.sockets.on('connection', function(socket) {
    console.log("connection");

    // メッセージを受けたときの処理
    socket.on('message', function(data) {
        //console.log(data.value.led);
        console.log(data.value.celsius);
        // つながっているクライアント全員に送信
        socket.broadcast.json.emit('message', { value: data.value });

        //console.log('Client sent us: ' + data.value.led);
        console.log('!!!Client sent us: ' + data.value.celsius);
        sp.write(data.value.celsius, function(err, bytesWritten) {
            console.log('bytes written: ', bytesWritten);
        });
    });

    // クライアントが切断したときの処理
    socket.on('disconnect', function(){
        console.log("disconnect");
    });
    
    socket.on('sendArduino',function(data){
        console.log('sendArduino');
        console.log(data);
        sp.write(data,function(err,bytesWritten){
             if(err){
                 console.log(err);
                 return;
             }
             console.log('bytes written: ', bytesWritten);
        });
    });
    
});

// data from Serial port
sp.on('data', function(input) {

    var buffer = new Buffer(input, 'utf8');
    var jsonData;
    try {
        jsonData = JSON.parse(buffer);
        console.log('温度: ' + jsonData.temp1);
        console.log('湿度:' + jsonData.temp2);//***
        console.log('led状態: ' + jsonData.led);
       // setMush_data(jsonData);
        check_pp(jsonData);
    } catch(e) {
        // データ受信がおかしい場合無視する
        return;
    }
    // つながっているクライアント全員に送信
    io.sockets.json.emit('message', { value: jsonData });
});

sp.on('close', function(err) {
    console.log('port closed!!!');
});

sp.on('open', function(err) {
    console.log('port opened!!!');
});

//http.createServer(app).listen(app.get('port'), function(){
//  console.log('Express server listening on port ' + app.get('port'));
//});

function setMush_data(jsd){
    var  topics = dbmongo.getMush_data();
    var newTopics = {
       date:new Date(),
       lineNo:[conf.lineCode,conf.lineNum],
       celsius:jsd.temp1,        
       humidity:jsd.temp2
    };
    topics.insert(newTopics,function(err,topic){
        topics.close();
        if(err){
            console.log('Insert ERR!');
            return;
        }
        console.log('INSERT FINISHED!');
    });
    
};

//DBから設定値を読み込んでLEDのON/OFFを切り替える
function check_pp(jsd){
    console.log('Check_pp');
    var topics = dbmongo.getMush_data();
    var topics2 = dbmongo.getMush_data();
    var topics3 = dbmongo.getMush_data();
    topics.getAutoSetting(function(err,items){
       // topics.close();
        if(err){
            console.log(err);
            return;
        }
        var pp = items[0];
        var cels_p1 = (pp.celsius.point -0)-(pp.celsius.range.top-0),
                  cels_p2 = (pp.celsius.point -0)+(pp.celsius.range.bottom-0),
                  humd_p1 = (pp.humidity.point-0)-(pp.humidity.range.top-0),
                  humd_p2 = (pp.humidity.point-0)+(pp.humidity.range.bottom-0),
                  cels_active = pp.celsius.active,
                  humd_active = pp.humidity.active,
                  cels_mode = pp.celsius.mode,
                  humd_mode = pp.humidity.mode,
                  cels_top_lim = (pp.celsius.point -0)+(pp.celsius.range.top-0),
                  cels_bot_lim = (pp.celsius.point -0)-(pp.celsius.range.bottom-0),
                  hum_top_lim = (pp.humidity.point-0)+(pp.humidity.range.top-0),
                  hum_bot_lim = (pp.humidity.point-0)-(pp.humidity.range.bottom-0);
       
//**********************************湿度*******************************************
        //DBの各activeがONなら設定値まで一直線に行く
        //jsd.temp1 = 現在値  pp.◯◯ = DBコレクション設定値
        if(cels_active === "ON"){
                //設定値 > 現在値
                if(pp.celsius.point > jsd.temp1) {
                       if(cels_mode === 'UP'){
                           console.log('設定値 > 現在値 cels_mode:UP');
                            //温度上げる装置ON 下げOFF
                            exec('gpio -g write 4 1');
                            exec('gpio -g write 17 0');
                            var str = cels_p1+' ~ '+pp.celsius.point+' | now:'+jsd.temp1;
                            console.log('celsius:{p1:ON | p2:OFF | mode:UP | active:ON | '+str+'}');
                       } else if(cels_mode === 'DW'){
                           console.log('設定値 > 現在値 cels_mode:DW');
                           //温度下げ、上げOFF active:OFF 下降仕切った後の自然上昇状態にする
                                exec('gpio -g write 17 0');
                                exec('gpio -g write 4 0');
                                topics2.update({d_id:'celsius_mode',d_mode:'UP'},function(){});
                                topics2.update({d_id:'celsius_active',d_active:'OFF'},function(){});
                                 var str = pp.celsius.point+' ~ '+cels_p2+' | now:'+jsd.temp1;
                                 console.log('celsius:{p1:OFF | p2:OFF | mode:UP | active:OFF | '+str+'}'); 
                       };
                } else if (pp.celsius.point < jsd.temp1){//設定値 < 現在値
                     if(cels_mode === 'UP') {//上昇状態の時
                                console.log('設定値 < 現在値 cels_mode:UP');
                                //温度上げる装置OFF 下げるOFF |  モード:DW  active:OFF つまり上昇仕切った後の自然下降状態にする
                                exec('gpio -g write 17 0');
                                exec('gpio -g write 4 0');
                                //モードを下降状態にセット
                                topics2.update({d_id:'celsius_mode',d_mode:'DW'},function(){});
                                //猶予範囲なら起動しないようにする
                                topics2.update({d_id:'celsius_active',d_active:'OFF'},function(){});   
                                var str = cels_p1+' ~ '+pp.celsius.point+' | now:'+jsd.temp1;
                                console.log('celsius:{p1:OFF | p2:OFF |  mode:DW | active:OFF | '+str+'}');                                                                  
                     } else if(cels_mode === 'DW'){
                            console.log('設定値 < 現在値 cels_mode:DW');
                            //温度上げOFF 下げON
                            exec('gpio -g write 17 1');
                            exec('gpio -g write 4 0');
                            var str = cels_p2+' ~ '+pp.celsius.point+' ~ '+cels_p1+' | now:'+jsd.temp1;
                            console.log('celsius:{p1:OFF | p2:ON | mode:DW | active:ON | '+str+'}');
                     }
                } else if(pp.celsius.point === jsd.temp1){//設定値 = 現在値　2013/10/25
                        console.log('設定値 = 現在値 | cels_mode:'+cels_mode+' | cels_active:'+cels_active);
                }
        } else if (cels_active === "OFF"){//各activeがOFFのときは猶予値の範囲内、もしくは範囲から出た場合。
                //設定値 > 現在値
                if(pp.celsius.point > jsd.temp1){
                        if(cels_p1 >= jsd.temp1){//猶予値 >= 現在値 猶予値内に収まっている
                            if(cels_mode === "DOWN"){
                                console.log('celsius 猶予値 >= 現在値 | 下降中 | 猶予値範囲内');
                                var str = cels_p1+' ~ '+pp.celsius.point+' | now:'+jsd.temp1;
                                console.log('celsius:{'+str+'}');
                            } else if(cels_mode === "UP"){
                                console.log('celsius 猶予値 >= 現在値 | 上昇中 | 猶予値範囲内');
                                var str = cels_p1+' ~ '+pp.celsius.point+' | now:'+jsd.temp1;
                                console.log('celsius:{'+str+'}');
                            }
                        } else if(cels_p1 < jsd.temp1){//猶予値を下回った   
                                 console.log('cels 猶予値 < 現在値');                      
                                //温度上げON 下げOFF,active:ON モードUP
                                exec('gpio -g write 17 0');
                                exec('gpio -g write 4 1');
                                topics2.update({d_id:'celsius_mode',d_mode:'UP'},function(){});  
                                //猶予範囲なら起動しないようにする
                                topics2.update({d_id:'celsius_active',d_active:'ON'},function(){});
                                var str = cels_p1+' ~ '+pp.celsius.point+' | now:'+jsd.temp1;
                                console.log('celsius:{p1:ON | p2:OFF | mode:UP | active:ON | '+str+'}'); 
                        }
                } else if (pp.celsius.point < jsd.temp1){//設定値 < 現在値
                        if(cels_p2 >= jsd.temp1) {//猶予値 >= 現在値
                            if(cels_mode === "UP"){
                                console.log('celsius 猶予値 >= 現在値 | 上昇中 | 猶予値範囲内');
                                var str = pp.celsius.point+' ~ '+cels_p2+' | now:'+jsd.temp1;
                                console.log('celsius:{'+str+'}');
                            } else if(cels_mode === "DW"){
                                console.log('celsius 猶予値 >= 現在値 | 下降中 | 猶予値範囲内');
                                var str = pp.celsius.point+' ~ '+cels_p2+' | now:'+jsd.temp1;
                                console.log('celsius:{'+str+'}');
                            }
                        } else if(cels_p2 < jsd.temp1){//猶予値 < 現在値 猶予値を上回った
                                console.log('cels 猶予値 < 現在値');
                                 //温度上げOFF 下げON active:ON モードDW
                                exec('gpio -g write 17 1');
                                exec('gpio -g write 4 0');
                                topics2.update({d_id:'celsius_mode',d_mode:'DW'},function(){});
                                //猶予範囲なら起動しないようにする
                                topics2.update({d_id:'celsius_active',d_active:'ON'},function(){}); 
                                var str = pp.celsius.point+' ~ '+cels_p2+' | now:'+jsd.temp1;
                                console.log('celsius:{p1:OFF | p2:ON | mode:DW | active:ON | '+str+'}'); 
                        }
                } else if (pp.celsius.point === jsd.temp1){//設定値 = 現在値
                    console.log('設定値 = 現在値 | cels_mode:'+cels_mode+' | cels_active:'+cels_active);
                }
        }
        
//**********************************湿度*******************************************        
        if(humd_active === "ON"){
                //設定値 > 現在値
                if(pp.humidity.point > jsd.temp2) {
                       if(humd_mode === 'UP'){
                            //湿度上げる装置ON 下げOFF
                           exec('gpio -g write 22 1');
                           exec('gpio -g write 25 0');
                           var str = humd_p1+' ~ '+pp.humidity.point+' | now:'+jsd.temp2;
                           console.log('humidity:{p3:ON | p4:OFF | mode:UP | active:ON | '+str+'}');                                       
                       } else if(humd_mode === 'DW'){
                           //湿度下げ、上げOFF active:OFF 下降仕切った後の自然上昇状態にする
                            exec('gpio -g write 22 0');
                            exec('gpio -g write 25 0');
                            topics3.update({d_id:'humidity_mode',d_mode:'UP'},function(){});
                            topics3.update({d_id:'humidity_active',d_active:'OFF'},function(){}); 
                            var str = pp.humidity.point+' ~ '+humd_p2+' | now:'+jsd.temp2;
                            console.log('humidity:{p3:OFF | p4:OFF | mode:UP | active:OFF | '+str+'}');
                       }
                } else if (pp.humidity.point < jsd.temp2){//設定値 < 現在値
                     if(humd_mode === 'UP') {//上昇状態の時
                            //温度上げる装置OFF 下げるOFF |  モード:DW  active:OFF つまり上昇仕切った後の自然下降状態にする
                            exec('gpio -g write 22 0');
                            exec('gpio -g write 25 0');
                            //モードを下降状態にセット
                            topics3.update({d_id:'humidity_mode',d_mode:'DW'},function(){});  
                             //猶予範囲なら起動しないようにする
                             topics3.update({d_id:'humidity_active',d_active:'OFF'},function(){});  
                             var str = humd_p1+' ~ '+pp.humidity.point+' | now:'+jsd.temp2;
                             console.log('humidity:{p3:OFF | p4:OFF | mode:DW | active:OFF | '+str+'}');
                     } else if(humd_mode === 'DW'){
                            //温度上げOFF 下げON    
                            exec('gpio -g write 22 0');
                            exec('gpio -g write 25 1');
                            var str = pp.humidity.point+' ~ '+humd_p2+' | now:'+jsd.temp2;
                            console.log('humidity:{p3:OFF | p4:ON | mode:DW | active:ON | '+str+'}');                      
                     }
                }  else if (pp.humidity.point === jsd.temp2){//設定値 = 現在値          
                    console.log('設定値 = 現在値 | humd_mode:'+humd_mode+' | humd_active:'+humd_active);
                }
        } else if(humd_active === "OFF"){
                //設定値 > 現在値
                if(pp.humidity.point > jsd.temp2){
                        if(humd_p1 >= jsd.temp2){//猶予値 >= 現在値 猶予値内に収まっている
                            if(humd_mode === "DW"){
                                console.log('humidity 猶予値 >= 現在値 | 下降中 | 猶予値範囲内');
                                var str = humd_p1+' ~ '+pp.humidity.point+' | now:'+jsd.temp2;
                                console.log('humidity:{'+str+'}');
                            } else if(humd_mode === "UP"){
                                console.log('humidity 猶予値 >= 現在値 | 上昇中 | 猶予値範囲内');
                                var str = humd_p1+' ~ '+pp.humidity.point+' | now:'+jsd.temp2;
                                console.log('humidity:{'+str+'}');
                            }
                        } else if(humd_p1 < jsd.temp2){//猶予値を下回った                         
                                    //温度上げON 下げOFF,active:ON モードUP
                                    exec('gpio -g write 22 1');
                                    exec('gpio -g write 25 0');
                                    topics3.update({d_id:'humidity_mode',d_mode:'UP'},function(){});  
                                    //猶予範囲なら起動しないようにする
                                    topics3.update({d_id:'humidity_active',d_active:'ON'},function(){});    
                                    var str = pp.humidity.point+' ~ '+humd_p2+' | now:'+jsd.temp2;
                                    console.log('humidity:{p3:ON | p4:OFF | mode:UP | active:ON | '+str+'}'); 
                        }
                } else if (pp.humidity.point < jsd.temp2){//設定値 < 現在値
                        if(humd_p2 >= jsd.temp2) {//猶予値 >= 現在値
                            if(humd_mode === "UP"){
                                console.log('humidity 猶予値 >= 現在値 | 上昇中 | 猶予値範囲内');
                                var str = pp.humidity.point+' ~ '+humd_p2+' | now:'+jsd.temp2;
                                console.log('humidity:{'+str+'}');
                            } else if(humd_mode === "DW"){
                                console.log('humidity 猶予値 >= 現在値 | 下降中 | 猶予値範囲内');
                                var str = pp.humidity.point+' ~ '+humd_p2+' | now:'+jsd.temp2;
                                console.log('humidity:{'+str+'}');
                            }
                        } else if(humd_p2 < jsd.temp2){//猶予値 < 現在値 猶予値を上回った
                                //温度上げOFF 下げON active:ON モードDW
                                exec('gpio -g write 22 0');
                                exec('gpio -g write 25 1');
                                topics3.update({d_id:'humidity_mode',d_mode:'DW'},function(){});
                                //猶予範囲なら起動しないようにする
                                topics3.update({d_id:'humidity_active',d_active:'ON'},function(){});    
                                var str = pp.humidity.point+' ~ '+humd_p2+' | now:'+jsd.temp2;
                                console.log('humidity:{p3:OFF | p4:ON | mode:DW | active:ON | '+str+'}');
                        }
                } else if (pp.humidity.point === jsd.temp2){//設定値 = 現在値
                    console.log('設定値 = 現在値 | humd_mode:'+humd_mode+' | humd_active:'+humd_active);
                }        
        }
        
        topics.close();
        topics2.close();
        topics3.close();
    });
    
};

function disp_console(id,data){
    if(id === "cels") {
        
    }
    if(id === "humd"){
        
    }
};