const http = require('http');
const WebSocket = require('ws');

const server = http.createServer((req, res) => {
    console.log(`[${new Date().toISOString()}] HTTP ${req.method} request to ${req.url} from ${req.socket.remoteAddress}`);
    
    // Log the request body if any
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
    });
    req.on('end', () => {
        if (body) {
            console.log(`[${new Date().toISOString()}] HTTP Body: ${body}`);
        }
        res.writeHead(200, { 'Content-Type': 'text/plain' });
        res.end('Test Server is Reachable!\n');
    });
});


const wss = new WebSocket.Server({ server });

server.listen(8081, '0.0.0.0', () => {
    console.log('Test server started on http://0.0.0.0:8081 and ws://0.0.0.0:8081');
});

wss.on('connection', function connection(ws, req) {
    const ip = req.socket.remoteAddress;
    console.log(`[${new Date().toISOString()}] New device connected from ${ip}`);

    ws.on('message', function incoming(message) {
        console.log(`[${new Date().toISOString()}] Received from device: %s`, message);
        
        try {
            // Check if it's JSON and try to reply based on the demo protocol
            const msg = JSON.parse(message);
            if (msg.cmd === 'reg') {
                const reply = {
                    ret: "reg",
                    result: true,
                    cloudtime: new Date().toISOString().replace('T', ' ').substring(0, 19)
                };
                console.log(`[${new Date().toISOString()}] Replying to reg:`, JSON.stringify(reply));
                ws.send(JSON.stringify(reply));
            } else if (msg.cmd === 'sendlog') {
                const reply = {
                    ret: "sendlog",
                    result: true,
                    count: msg.count,
                    logindex: msg.logindex,
                    cloudtime: new Date().toISOString().replace('T', ' ').substring(0, 19)
                };
                console.log(`[${new Date().toISOString()}] Replying to sendlog:`, JSON.stringify(reply));
                ws.send(JSON.stringify(reply));
            } else if (msg.cmd === 'senduser') {
                const reply = {
                    ret: "senduser",
                    result: true,
                    cloudtime: new Date().toISOString().replace('T', ' ').substring(0, 19)
                };
                console.log(`[${new Date().toISOString()}] Replying to senduser:`, JSON.stringify(reply));
                ws.send(JSON.stringify(reply));
            }
        } catch (e) {
            console.error('Failed to parse message as JSON or reply:', e.message);
        }
    });

    ws.on('close', () => {
        console.log(`[${new Date().toISOString()}] Device disconnected`);
    });
    
    ws.on('error', (err) => {
        console.error(`[${new Date().toISOString()}] WebSocket error:`, err);
    });
});
