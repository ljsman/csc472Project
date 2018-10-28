const net = require('net');

const port = 8000
const separator = ";"

const server = net.createServer(function(sock) {
    console.log("connected: " + sock.remoteAddress + ":" + sock.remotePort)
    
    sock.setEncoding('utf8');

    let buffer = ""
    sock.on('data', function(data) {
        console.log("received: " + sock.remoteAddress + ":" + sock.remotePort + " : " + data)
        
        buffer = buffer+data
        let separatorIdx = buffer.indexOf(separator)
        let lastIdx = -1
        while(separatorIdx != -1) {
        	let str = buffer.slice(lastIdx+1, separatorIdx)
        	let msg
        	try {
        		msg = JSON.parse(str)
        	}
        	catch (err) {
        		console.log("ERROR: received unvalid string: " + str)
        		console.log("ERROR: " + err)
        	}

        	handleMessage(sock, msg)

        	lastIdx = separatorIdx
        	separatorIdx = buffer.indexOf(separator, lastIdx+1)
        }
        if(lastIdx != -1) buffer = buffer.slice(lastIdx+1)
    });
    
    sock.on('close', function(data) {
        console.log("disconnected: " + sock.remoteAddress + ":" + sock.remotePort)
        buffer = ""
        unregisterUser(getUserIdentifier(sock))
    });
})

server.listen(port, () => {
	console.log("server listened at " + server.address().address + ":" + server.address().port)
})

/*  user
	{
		identifier: "address:port",
		socket: socket,
		name: "jason",
		avatar: 5,
		heartbeat_timestamp: 1300000,
		game_id: -1,
	}
*/
let users = {}

let gameIdx = 1
let games = {}

let messageHandlers = {}
/* msg
	{
		request_id: 1,
		type: 1,
		others...
	}
*/
function handleMessage(sock, msg) {
	console.log("handle message: " + JSON.stringify(msg))
	let userIdentifier = getUserIdentifier(sock)

	if(typeof msg === 'undefined' || typeof msg.type === 'undefined' 
		|| typeof msg.request_id  == 'undefined') {// check format
		console.log("ERROR: msg format is unvalid")
	}
	else if(!messageHandlers[msg.type]) {
		console.log("ERROR: msg type is wrong")
	}
	else if(msg.type != 1 && !users[userIdentifier]) {// user should register first
		console.log("ERROR: user hasn't been registered " + userIdentifier)
	}
	else {
		messageHandlers[msg.type](userIdentifier, msg, sock)
	}
}

function addHandler(type, handler) {
	messageHandlers[type] = handler
}

/* response msg
	{
		request_id: 1,
		type: 5,
		others...
	}
*/
function sendResponseMessage(sock, req, type, res) {
	try {
		if(req != null) res.request_id = req.request_id
		res.type = type
		
		const str = JSON.stringify(res)
		sock.write(str, 'utf8')
		sock.write(";", 'utf8')
		console.log("sent to " + sock.remoteAddress + ":" + sock.remotePort + " : " + str)
	}
	catch (err) {
		console.log("ERROR: response is unvalid: " + err)
	}
}

/* type 0, heartbeat
	{
		...
	}
*/ 
addHandler(0, (identifier) => {
	if(users[identifier]) users[identifier].heartbeat_timestamp = Date.now()
})

/* type 1, registerUser
	{
		...
		name: "jason",
		avatar: 5,
	}
*/ 
addHandler(1, (identifier, msg, sock) => {
	console.log("registerUser: " + msg.name)
	if(users[identifier]) return;	//error
	else {
		users[identifier] = {
			identifier: identifier,
			socket: sock,
			name: msg.name,
			avatar: msg.avatar,
			heartbeat_timestamp: Date.now(),
			game_id: -1,
		}
	}
})

/* type 2, getGamesList
	{
		...
	}
	return type 100
	{
		games_list: [
			{
				game_name: "game1",
				total_count: 8,
				current_count: 6,
			},
			...
		]
	}
*/ 
addHandler(2, (identifier, msg, sock) => {
	let list = []
	for(const key in games) {
		const game = games[key]
		if(game.status === 0) {
			list.push(game.getShortDescription())
		}
	}
	sendResponseMessage(sock, msg, 100, {"games_list":list})
})

/* type 3, createGame
	{
		...
		game_name: "game1",
		werewolf_count: 2,
		villager_count: 5,
		other_roles: 0,		//0b1: Seer, 0b10: xxx, ...
	}
*/ 
addHandler(3, (identifier, msg) => {
	console.log("createGame: " + JSON.stringify(msg))
	games[gameIdx] = new Game(gameIdx, identifier, msg)
	gameIdx++
})

/* type 4, joinGame
	{
		...
		game_id: 1,
	}
*/ 
addHandler(4, (identifier, msg, sock) => {
	console.log("joinGame: " + identifier + " join game id: " + msg.game_id)
	if(users[identifier].game_id != -1) {
		sendResponseMessage(sock, msg, 200, {
			msg: "ERROR: please exit current game first"
		})
	}
	else {
		const game = games[msg.game_id]
		if(game && game.getCurrentCount() < game.config.total_count-1) {
			game.addPlayer(identifier, msg)
		}
		else {
			/* return type 200
			{
				msg: "ERROR: game is full"
			}
			*/ 
			sendResponseMessage(sock, msg, 200, {
				msg: "ERROR: game is not available"
			})
		}
	}
})

/* type 5, exitGame
	{
		...
	}
*/ 
addHandler(5, (identifier) => {
	if(users[identifier] && users[identifier].game_id != -1) {
		console.log("exitGame: " + identifier + " exit game id: " + users[identifier].game_id)
		games[users[identifier].game_id].removePlayer(identifier)
	}
})

// tools
function getUserIdentifier(sock) {
	return sock.remoteAddress + ":" + sock.remotePort;
}

function unregisterUser(identifier) {
	messageHandlers[5](identifier)	// call exitGame
	delete users[identifier]
}

/* game class
{
	config: {
		game_name: "game1",
		werewolf_count: 2,
		villager_count: 5,
		other_roles: [1],		//1: Seer
		total_count: 8,
		creator: "1.1.1.1:1234",
	},
	status: 0,	//0: waiting, 1: started, 2: game over
	players: [
		{
			identifier: "1.1.1.1:1234",
		},
		{
			...
		},
	]
}
*/
function Game(game_id, identifier, msg) {
	this.config = {
		game_id: game_id,
		game_name: msg.game_name,
		werewolf_count: msg.werewolf_count,
		villager_count: msg.villager_count,
		other_roles: msg.other_roles,
	}
	this.config.creator = identifier
	//TODO calculate other roles
	const totalCount = msg.werewolf_count + msg.villager_count
	this.config.total_count = totalCount

	this.status = 0
	this.players = new Array(totalCount)
	this.addPlayer(identifier, msg)
}

Game.prototype.getCurrentCount = function() {
	let current = 0
	for(let i=0; i<this.players.length; i++) {
		if(this.players[i]) current++
	}
	return current
}

Game.prototype.addPlayer = function(iden, req) {
	if(users[iden].game_id && users[iden].game_id != -1) {// exit current game first
		games[users[iden].game_id].removePlayer(iden)
	}
	let pos = 0
	while(this.players[pos]) pos++
	if(pos < this.config.total_count) {
		this.players[pos] = {
			identifier: iden
		}
		users[iden].game_id = this.config.game_id
		this.sendGameInformation(iden, req)
		this.notifyPlayersChanged(iden)
	}
	else {
		console.log("ERROR: wrong code(1000)")
	}
}

Game.prototype.removePlayer = function(iden) {
	for(let i=0; i<this.players.length; i++) {
		if(this.players[i] != null && this.players[i].identifier === iden) {
			this.players[i] = null
			users[iden].game_id = -1
			break;
		}
	}
	if(this.getCurrentCount() === 0) {
		delete games[this.config.game_id]
	}
	else this.notifyPlayersChanged()
}

Game.prototype.broadcast = function(type, msg, except) {
	for(let i=0; i<this.players.length; i++) {
		if(this.players[i]) {
			const iden = this.players[i].identifier
			if(iden && iden !== except && users[iden]) {
				sendResponseMessage(users[iden].socket, null, type, msg)
			}
		}
	}
}

/* return type 101
{
	config: {...}
	players: {...}
}
*/ 
Game.prototype.sendGameInformation = function(iden, req) {
	const msg = {
		config: this.config,
		players: this.getPlayersInformation()
	}
	sendResponseMessage(users[iden].socket, req, 101, msg)
}

/* return type 102
{
	players: {...}
}
*/ 
Game.prototype.notifyPlayersChanged = function(except) {
	const msg = {
		players: this.getPlayersInformation()
	}
	this.broadcast(102, msg, except)
}

Game.prototype.getShortDescription = function() {
	return {
		game_id: this.config.game_id,
		game_name: this.config.game_name,
		total_count: this.config.total_count,
		current_count: this.getCurrentCount(),
	}
}

Game.prototype.getPlayersInformation = function() {
	return this.players.map((item, index) => {
		if(item === null) return null
		else {
			const user = users[item.identifier]
			return {
				name: user.name,
				avatar: user.avatar,
				heartbeat_timestamp: user.heartbeat_timestamp,
			}
		}
	})
}