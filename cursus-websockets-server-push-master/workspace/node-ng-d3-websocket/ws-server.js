var ws = require("nodejs-websocket")
var connect = require('connect')
var serveStatic = require('serve-static')

var app = connect()
app.use(serveStatic('app', {'index': ['numbers.html']}))
app.listen(1337)

var interval;
var server = ws.createServer(function (conn) {
    console.log("New connection: " + conn)
    conn.on("text", function (message) {
        console.log("Received " + message)
        params = JSON.parse(message);
	      var stillToGo = params.repeat;
        interval = setInterval(function() {
	          var randomNumbers = [];
	          for(i=0;i<params.size;i++){
	           	randomNumbers.push(Math.floor(Math.random() * params.max));
	          }	
	          conn.sendText(JSON.stringify(randomNumbers));
	          stillToGo--;
	          if (stillToGo == 0) {
	          	console.log("Done repeating");
	          	clearInterval(interval);
	          }
	      }, params.delay);  
    })
    conn.on("close", function (code, reason) {
        console.log("Connection closed")
       	clearInterval(interval);
    })
}).listen(9160)
