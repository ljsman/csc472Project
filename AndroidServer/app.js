const net = require('net');

const port = 8000
const separator = ";"

const server = net.createServer(function(sock) {
    console.log("connected: " + sock.remoteAddress + ":" + sock.remotePort)
    
    sock.setEncoding('utf8')

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
    })
    
    sock.on('close', function(data) {
        console.log("disconnected: " + sock.remoteAddress + ":" + sock.remotePort)
        buffer = ""
        unregisterUser(getUserIdentifier(sock))
    })
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
		other_roles: 0,		//0b1: Werewolf, 0b10: villager, 0b100: Seer, 0b1000: xxx, ...
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
		if(game && game.getCurrentCount() < game.config.total_count) {
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

addHandler(6, (identifier, msg) => {
	if(users[identifier] && users[identifier].game_id != -1) {
		console.log("werewolf kill: " + msg.target)
		games[users[identifier].game_id].process(identifier, msg)
	}
})

addHandler(7, (identifier, msg) => {
	if(users[identifier] && users[identifier].game_id != -1) {
		games[users[identifier].game_id].process(identifier, msg)
	}
})

addHandler(8, (identifier, msg) => {
	if(users[identifier] && users[identifier].game_id != -1) {
		console.log("vote to kill: " + msg.target)
		games[users[identifier].game_id].process(identifier, msg)
	}
})

/* type 9, startGame
	{
		...
	}
*/ 
addHandler(9, (identifier) => {
	if(users[identifier] && users[identifier].game_id != -1) {
		console.log("start game id: " + users[identifier].game_id)
		games[users[identifier].game_id].startGame()
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

function shuffleArray(a) {
    var j, x, i;
    for (i = a.length - 1; i > 0; i--) {
        j = Math.floor(Math.random() * (i + 1));
        x = a[i];
        a[i] = a[j];
        a[j] = x;
    }
    return a;
}

/* game class
{
	config: {
		game_name: "game1",
		werewolf_count: 2,
		villager_count: 5,
		other_roles: 1,		//0b1: Werewolf, 0b10: villager, 0b100: Seer, 0b1000: xxx, ...
		total_count: 8,
		creator: "1.1.1.1:1234",
	},
	status: 0,	//0: waiting, 1: started, 2: game over
	players: [
		{
			identifier: "1.1.1.1:1234",
			role: 0b1,
			status: 0, //0: alive, 1: died
		},
		...,
	],
	currentPhase: 0,
	phases:[0, 1, 2, 3, 5],	// phase ids
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
	//TODO hardcode otherrole's count
	let totalCount = msg.werewolf_count + msg.villager_count
	if(msg.other_roles > 0) totalCount += 1
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
		this.sendGameInformation(iden, req, pos)
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
Game.prototype.sendGameInformation = function(iden, req, pos) {
	const msg = {
		position: pos,
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

Game.prototype.getRolesInformation = function() {
	return this.players.map((item, index) => {
		if(item === null) return null
		else {
			return {
				role: item.role
			}
		}
	})
}

Game.prototype.getPlayersStatus = function() {
	return this.players.map((item, index) => {
		if(item === null) return null
		else {
			return {
				status: item.status
			}
		}
	})
}

/* return type 103
{
	roles:[
		{
			role: 1
		},
		...
	]
}
*/ 
Game.prototype.startGame = function() {
	this.status = 1;
	// random roles 
	let roles = []
	for(let i=0; i<this.config.werewolf_count; i++) roles.push(0b1)
	for(let i=0; i<this.config.villager_count; i++) roles.push(0b10)
	let others = this.config.other_roles
	if(others > 0) {
		// TODO hardcode
		roles.push(0b100)
	}

	//shuffleArray(roles)
	for (let i = roles.length - 1; i >= 0; i--) {
		this.players[i].role = roles[i]
		this.players[i].status = 0
	}
	// create phases TODO: hardcode
	if(others > 0) {
		this.phases = [2,3,1,4,1]
	}
	else {
		this.phases = [2,1,4,1]
	}

	// send to clients and start game
	const msg = {
		roles: this.getRolesInformation()
	}
	this.broadcast(103, msg)
	this.currentPhase = -1

	const self = this
	setTimeout(() => {
		self.gotoNextPhase()
	}, 5000)
}

Game.prototype.checkGameStatus = function() {
	let hasWolfwere = false
	let hasHuman = false
	this.players.forEach((player)=> {
		if(player.status === 0) {
			if(player.role === 0b1) hasWolfwere = true
			else hasHuman = true
		}
	})
	if(hasWolfwere === false) this.endGame(1)
	else if(hasHuman === false) this.endGame(0)
}

/* return type 104
{
	winner: 0, // 0: wolfwere win, 1: human win
}
*/ 
Game.prototype.endGame = function(winner) {// 0: wolfwere win, 1: human win
	this.status = 2
	const msg = {
		winner: winner
	}
	this.broadcast(104, msg)
}

Game.prototype.process = function(userIdentifier, msg) {
	if(this.status != 1) {
		console.log("ERROR: game hasn't been started")
		return
	}
	const phaseId = this.phases[this.currentPhase]
	if(!phaseHandlers[phaseId]) {
		console.log("ERROR: phase handler doesn't exist #"+phaseId)
		return
	}
	phaseHandlers[phaseId].process(this, userIdentifier, msg)
}

Game.prototype.gotoNextPhase = function() {
	if(this.phases.length == 0) return //should never happen
	this.currentPhase++
	if(this.currentPhase >= this.phases.length) this.currentPhase = 0

	const phaseId = this.phases[this.currentPhase]
	if(!phaseHandlers[phaseId]) {
		console.log("ERROR: phase handler doesn't exist #"+phaseId)
		return
	}
	phaseHandlers[phaseId].enter(this)
}

Game.prototype.kill = function(pos) {
	if(this.players[pos].status === 1) console.log("ERROR: try to kill a died man #"+pos)
	this.players[pos].status = 1
}

// phase handlers
let phaseHandlers = {}

// phase 1: check game status
phaseHandlers[1] = {
	enter: (game)=> {
		game.checkGameStatus()
		if(game.status === 1) game.gotoNextPhase()
	},
	process: (game, userIdentifier, msg)=> {
		// empty
	}
}

// phase 2: wolfwere
phaseHandlers[2] = {
	/* return type 105
	{
		leader: 0,	// player position
	}
	*/
	enter: (game)=> {
		const msg = {
			leader: game.players.findIndex((player)=> {	// find first wolfwere
				return player.role === 0b1
			}),
		}
		game.broadcast(105, msg)
	},
	/* type 6, wolfwere kill human
		{
			...
			target: 1,	// player position
		}
	*/ 
	process: (game, userIdentifier, msg)=> {
		game.kill(msg.target)
		game.gotoNextPhase()
	}
}

// phase 3: seer
phaseHandlers[3] = {
	/* return type 106
	{
	}
	*/
	enter: (game)=> {
		const msg = {}
		game.broadcast(106, msg)
	},
	/* type 7, go to next turn
		{
			...
		}
	*/ 
	process: (game, userIdentifier, msg)=> {
		game.gotoNextPhase()
	}
}

// phase 4: discussion
phaseHandlers[4] = {
	/* return type 107
	{
		leader: 0,	// player position
		playersStatus: [
			{
				status: 0,	
			},
			...
		]
	}
	*/
	enter: (game)=> {
		const msg = {
			leader: 0,	// always the creator
			playersStatus: game.getPlayersStatus()
		}
		game.broadcast(107, msg)
	},
	/* type 8, vote to kill
		{
			...
			target: 1,	// player position
		}
	*/ 
	/* return type 108
	{
		playersStatus: [
			{
				status: 0,	
			},
			...
		]
	}
	*/
	process: (game, userIdentifier, msg)=> {
		game.kill(msg.target)
		const res = {
			playersStatus: game.getPlayersStatus()
		}
		game.broadcast(108, res)

		setTimeout(()=> {
			game.gotoNextPhase()
		}, 3000)
	}
}
