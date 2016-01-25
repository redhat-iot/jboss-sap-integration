var sys = require('sys');
var net = require('net');
var mqtt = require('mqtt');
 
var io  = require('socket.io').listen(5000);
io.set('origins', '*:*');

var client = mqtt.connect("mqtt://admin:admin@localhost:1883");

io.sockets.on('connection', function (socket) {
  socket.on('subscribe', function (data) {
    console.log('Subscribing to '+data.topic);
    socket.join(data.topic);
    client.subscribe(data.topic);
  });
});

client.on('message', function(topic, message) {
	  console.log(message);
	  sys.puts(topic+'='+message);
	  io.sockets.in(topic).emit('mqtt',{'topic': String(topic), 'payload':String(message)});
	});