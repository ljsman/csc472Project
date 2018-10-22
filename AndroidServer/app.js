const express = require('express')
const app = express()
const port = 8000

let idx = 0
let games = []

// tools
function ValidateName(name) {
	if(name && name.length > 0) return true
	return false
}

function ValidateIPaddress(ipaddress) {  
  if (ipaddress && /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(ipaddress)) {  
    return true
  }  
  return false 
}  

function ValidatePort(port) {
    if(port && port.length>0 && /^\d+$/.test(port)) return true
    return false
}

// GET /create_game?game_name=wolf&ip=0.0.0.0&port=3000
app.get('/create_game', (req, res) => {
	let gName = req.query.game_name
	let gIp = req.query.ip
	let gPort = req.query.port	
	if(ValidateName(gName) === false) res.status(500).send("invalid name")
	else if(ValidateIPaddress(gIp) === false) res.status(500).send("invalid ip.")
	else if(ValidatePort(gPort) === false) res.status(500).send("invalid port")
	else {
		let gIdx = idx.toString()
		idx++
		games[gIdx] =  {name: gName, ip:gIp, port:gPort, timestamp:Date.now()}
		console.log("create game id: "+gIdx+" info: "+JSON.stringify(games[gIdx]))
		res.status(200).send(JSON.stringify({game_id: gIdx}))
	}
})

// GET /beatheart?game_id=0
app.get('/beatheart', (req, res) => {
	let gId = req.query.game_id
	if(gId && games[gId]) {
		games[gId].timestamp = Date.now()
		res.sendStatus(200)
	}
	else {
		res.status(500).send("wrong game id")
	}
})

// GET /remove_game?game_id=0
app.get('/remove_game', (req, res) => {
	let gId = req.query.game_id
	if(gId && games[gId]) {
		delete games[gId]
		res.sendStatus(200)
	}
	else {
		res.status(500).send("wrong game id")
	}
})

app.get('/get_games_list', (req, res) => {
	res.status(200).send(JSON.stringify(games))
})

app.listen(port, () => console.log(`Server listening on port ${port}!`))