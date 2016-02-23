var tempWebSocket;
var dimledWebSocket;
var dimblasterWebSocket;


/**
 * 
 * Aufbau der Websocket Verbindungen beim Laden der Seite
 */
$(document).ready(function () {
    if (tempWebSocket == undefined) {

        tempWebSocket = new WebSocket("ws://" + serveradress + "/temppoint");
        tempWebSocket.onmessage = function (event) {
            console.log("Websocket receive:"+event.data);
            var jobj = jQuery.parseJSON(event.data);
            $('#h1temp').text("Temperatur " + jobj.temperature + " C");
            $('#tslider').val(jobj.temperature).slider('refresh');
            $('#h1pressure').text("Pressure " + jobj.pressure + " Pa");
            $('#pslider').val(jobj.pressure).slider('refresh');


        };
    }
    if (dimblasterWebSocket == undefined) {

        dimblasterWebSocket = new WebSocket("ws://" + serveradress + "/blasterpoint");
        dimblasterWebSocket.onmessage = function (event) {
            var dimblaster = event.data;
            console.log("dimSocket blaster receive (" + dimblaster + ")");

            $('#sliderNew').val(dimblaster);
            if ($('#sliderNew').is(":visible")) {
                $('#sliderNew').slider('refresh');
            }
        };
    }
    if (dimledWebSocket == undefined) {

        dimledWebSocket = new WebSocket("ws://" + serveradress + "/ledpoint");
        dimledWebSocket.onmessage = function (event) {
            var dimled = event.data;
            console.log("dimSocket led receive (" + dimled + ")");

            $('#slider1').val(dimled);
            if ($('#slider1').is(":visible")) {
                $('#slider1').slider('refresh');
            }
            if (dimled == 0) {
                $('#toggle').val('0');
                if ($('#toggle').is(":visible")) {
                    $('#toggle').slider('refresh');
                }
            }
            else {
                $('#toggle').val('100');
                if ($('#toggle').is(":visible")) {
                    $('#toggle').slider('refresh');
                }

            }
        };
    }
        $('#fieldsliderNew').change(function () {
        if (dimblasterWebSocket != undefined) {
        dimblasterWebSocket.send($('#sliderNew').val());
        console.log("send via Websocket blaster " + $('#sliderNew').val());
        }
        else {
            console.log("Keine Websocket Verbindung");
        }
        });
      
        $('#toggle').change(function () {
        if (dimledWebSocket != undefined) {
            dimledWebSocket.send($('#toggle').val());
            console.log("send via Websocket led " + $('#toggle').val());
        }
        else {
            console.log("Keine Websocket Verbindung");
        }
    });

});

/*
 $(document).on('pagebeforeshow', '#page2', function () {
    $('#sliderNew').slider('refresh');
    $('#fieldsliderNew').change(function () {
    if (dimblasterWebSocket != undefined) {
        dimblasterWebSocket.send($('#sliderNew').val());
        console.log("send via Websocket blaster " + $('#sliderNew').val());
        }
        else {
            console.log("Keine Websocket Verbindung");
        }
        });
    });
    
 */   
    
$(document).on('pagebeforeshow', '#page3', function () {

    $('#slider1').slider('refresh');
    if (window.DeviceMotionEvent == undefined) {
        $("#gyro").attr("disabled", true);
        console.log("kein DeviceMotionEvent");
    }
    else {
        $("#gyro").attr("disabled", false);
        console.log("mit DeviceMotionEvent");
        window.ondevicemotion = function(event) {
                ax = event.accelerationIncludingGravity.x
                if (ax==null) {
                    ax=-2;
                }
                ax=Math.round(-(ax*5-50));
                if ($('#gyro').is(":checked")) {
                    //console.log("Set Slider to:"+ax);
                    $('#slider1').val(ax);
                     $('#slider1').slider('refresh');
                    dimledWebSocket.send(ax);
                }
        }   
    }

    $('#fieldslider1').change(function () {
        if (dimledWebSocket != undefined) {
            dimledWebSocket.send($('#slider1').val());
            console.log("send via Websocket " + $('#slider1').val());
        }
        else {
            console.log("Keine Websocket Verbindung");
        }


    });
});

$(document).on('pagebeforeshow', '#page4', function () {
    $('#slider2').empty();
    $('#slider3').empty();

    $('<input>').appendTo('#slider2').attr({'name': 'slider', 'id': 'tslider', 'data-highlight': 'true', 'min': '15', 'max': '35', 'value': '15', 'type': 'range'}).slider({
        create: function (event, ui) {
            $(this).parent().find('input').hide();
            $(this).parent().find('input').css('margin-left', '-9999px'); // Fix for some FF versions
            $(this).parent().find('.ui-slider-track').css('margin', '0 15px 0 15px');
            $(this).parent().find('.ui-slider-handle').hide();
        }
    }).slider("refresh");

    $('<input>').appendTo('#slider3').attr({'name': 'slider', 'id': 'pslider', 'data-highlight': 'true', 'min': '80000', 'max': '120000', 'value': '80000', 'type': 'range'}).slider({
        create: function (event, ui) {
            $(this).parent().find('input').hide();
            $(this).parent().find('input').css('margin-left', '-9999px'); // Fix for some FF versions
            $(this).parent().find('.ui-slider-track').css('margin', '0 15px 0 15px');
            $(this).parent().find('.ui-slider-handle').hide();
        }
    }).slider("refresh");


});
$(document).on('pagebeforeshow', '#page10', function () {
    google.load("visualization", "1", {packages: ["corechart"]});
    google.setOnLoadCallback(drawChart);


    function drawChart() {

        var fromDate = getYesterday();
        var toDay = getToday();
        var materialChart;
        var materialDiv = document.getElementById('material');
        var data = new google.visualization.DataTable();
        data.addColumn('date', 'Last 24h');
        data.addColumn('number', "Temperature C");
        data.addColumn('number', "Pressure mBar");
        var param = "?out=json&from=" + fromDate+"&to"+toDay;
        console.log(param);
        $.ajax({url: "SensorServlet?out=json&from=" + fromDate + "&to=" + toDay, success: function (result) {
                // demo = JSON.parse("{\"sensordata\" : [{\"temperature\" : 22.0,\"pressure\" : 17646,\"timestamp\" : \"2015-03-03 16:36:19.182\"}]}");
                console.log("receive data");
                var rdata = new Array();
                for (i = 0; i < result.sensordata.length; i++) {
                    var r = new Array();
                    r[0] = new Date(result.sensordata[i].timestamp);
                    r[1] = result.sensordata[i].temperature;
                    r[2] = result.sensordata[i].pressure / 100;
                    rdata[i] = r;
                    //console.log(rdata);
                }

                data.addRows(rdata);
                var options = {
                    title: 'Temperature and Pressure with Raspberry PI and BMP180',
                    width: 900,
                    height: 500,
                    vAxes: {0: {},
                        1: {}
                    },
                    series: {
                        0: {axis: 'Temps'},
                        1: {axis: 'Pressure'}
                    },
                    axes: {
                        // Adds labels to each axis; they don't have to match the axis names.
                        y: {
                            Temps: {label: 'Temps (Celsius)'},
                            Pressure: {label: 'Pressure (Pascal)'},
                        }
                    },
                    colors: ["red", "blue"]

                };
                var chart = new google.charts.Line(document.getElementById('chartdiv'));
                chart.draw(data, options);

            }});



    }
});

/**
 * Das gestrige Datum im SQL Format
 * @returns {String} Der Datum String
 */
function getYesterday() {
    var now = new Date();
    var yesterday = new Date(now.getTime() - 1000 * 60 * 60 * 24);
    console.log("yesterday="+yesterday);
    return "'" + yesterday.getFullYear() + "-" + (yesterday.getMonth() + 1) + "-" + yesterday.getDate() + " " + yesterday.getHours() + ":" + yesterday.getMinutes() + ":" + yesterday.getSeconds() + "'";
}

/**
 * Das heutige Datum im SQL Format
 * @returns {String} Der Datum String
 */
function getToday() {
    var now = new Date();
    console.log("now="+now);
    return "'" + now.getFullYear() + "-" + (now.getMonth() + 1) + "-" + now.getDate() + " " + now.getHours() + ":" + now.getMinutes() + ":" + now.getSeconds() + "'";
}