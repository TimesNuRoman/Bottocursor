/**
 * Cursor AI Remote - Companion Server
 *
 * This WebSocket server runs alongside Cursor IDE on your desktop.
 * It bridges the Android app's commands to Cursor via its CLI and APIs.
 *
 * Usage:
 *   npm install
 *   npm start
 *
 * Environment variables:
 *   PORT          - Server port (default: 9090)
 *   AUTH_TOKEN    - Optional authentication token
 *   CURSOR_PATH   - Path to Cursor CLI (default: auto-detect)
 *   WORKSPACE     - Default workspace path
 */

const WebSocket = require('ws');
const { exec, spawn } = require('child_process');
const path = require('path');
const fs = require('fs');
const os = require('os');

const PORT = process.env.PORT || 9090;
const AUTH_TOKEN = process.env.AUTH_TOKEN || '';
const VERBOSE = process.argv.includes('--verbose');

// Auto-detect Cursor CLI path
function findCursorCLI() {
    const paths = [
        // macOS
        '/usr/local/bin/cursor',
        '/Applications/Cursor.app/Contents/Resources/app/bin/cursor',
        path.join(os.homedir(), '.local/bin/cursor'),
        // Linux
        '/usr/bin/cursor',
        '/snap/bin/cursor',
        // Windows
        path.join(process.env.LOCALAPPDATA || '', 'Programs/cursor/resources/app/bin/cursor.cmd'),
        path.join(process.env.LOCALAPPDATA || '', 'Programs/Cursor/resources/app/bin/cursor.cmd'),
    ];

    for (const p of paths) {
        try {
            if (fs.existsSync(p)) return p;
        } catch (_) {}
    }

    return 'cursor'; // fallback to PATH
}

const CURSOR_CLI = process.env.CURSOR_PATH || findCursorCLI();
const WORKSPACE = process.env.WORKSPACE || process.cwd();

function log(msg) {
    const timestamp = new Date().toISOString().substring(11, 23);
    console.log(`[${timestamp}] ${msg}`);
}

function debug(msg) {
    if (VERBOSE) log(`[DEBUG] ${msg}`);
}

// Create WebSocket server
const wss = new WebSocket.Server({
    port: PORT,
    path: '/ws',
    verifyClient: (info) => {
        if (!AUTH_TOKEN) return true;
        const token = info.req.headers.authorization;
        return token === `Bearer ${AUTH_TOKEN}`;
    }
});

log(`Cursor AI Remote Server started on port ${PORT}`);
log(`Cursor CLI: ${CURSOR_CLI}`);
log(`Workspace: ${WORKSPACE}`);
if (AUTH_TOKEN) log('Authentication: enabled');

// Track active terminals and processes
const terminals = new Map();
const buildProcesses = new Map();
let devServerProcess = null;
let devServerUrl = '';

wss.on('connection', (ws, req) => {
    const clientIP = req.socket.remoteAddress;
    log(`Client connected from ${clientIP}`);

    // Send initial state
    sendFileTree(ws, WORKSPACE);

    ws.on('message', (data) => {
        try {
            const message = JSON.parse(data.toString());
            debug(`Received: ${JSON.stringify(message)}`);
            handleMessage(ws, message);
        } catch (err) {
            log(`Error parsing message: ${err.message}`);
            sendError(ws, 'Invalid message format');
        }
    });

    ws.on('close', () => {
        log(`Client disconnected from ${clientIP}`);
    });

    ws.on('error', (err) => {
        log(`WebSocket error: ${err.message}`);
    });
});

function handleMessage(ws, message) {
    switch (message.type) {
        case 'command':
            executeCommand(ws, message);
            break;

        case 'voice_command':
            handleVoiceCommand(ws, message);
            break;

        case 'ai_prompt':
            handleAIPrompt(ws, message);
            break;

        case 'file_request':
            handleFileRequest(ws, message);
            break;

        case 'terminal_input':
            handleTerminalInput(ws, message);
            break;

        case 'editor_action':
            handleEditorAction(ws, message);
            break;

        case 'build_command':
            handleBuildCommand(ws, message);
            break;

        case 'dev_server':
            handleDevServer(ws, message);
            break;

        case 'deploy_command':
            handleDeployCommand(ws, message);
            break;

        case 'r2_command':
            handleR2Command(ws, message);
            break;

        case 'd1_command':
            handleD1Command(ws, message);
            break;

        case 'status':
            ws.send(JSON.stringify({
                type: 'status',
                payload: 'pong',
                id: message.id,
                timestamp: Date.now()
            }));
            break;

        default:
            debug(`Unknown message type: ${message.type}`);
    }
}

/**
 * Execute a Cursor/VSCode command
 */
function executeCommand(ws, message) {
    const command = message.payload;

    // Handle special prefixed commands
    if (command.startsWith('vscode.open:')) {
        const filePath = command.substring('vscode.open:'.length);
        const fullPath = path.resolve(WORKSPACE, filePath);
        exec(`"${CURSOR_CLI}" "${fullPath}"`, (err) => {
            if (err) {
                sendError(ws, `Failed to open file: ${err.message}`);
            } else {
                sendFileContent(ws, fullPath);
            }
        });
        return;
    }

    // Use Cursor CLI to execute command
    // Cursor supports --command flag similar to VS Code
    const cmd = `"${CURSOR_CLI}" --command "${command}"`;
    debug(`Executing: ${cmd}`);

    exec(cmd, { cwd: WORKSPACE, timeout: 10000 }, (err, stdout, stderr) => {
        if (err) {
            debug(`Command error: ${err.message}`);
            // Fallback: try xdotool/keyboard simulation on Linux
            if (os.platform() === 'linux') {
                simulateKeybinding(command);
            }
        }
        sendResponse(ws, message.id, stdout || 'Command executed');
    });
}

/**
 * Handle voice command - same as command but logged differently
 */
function handleVoiceCommand(ws, message) {
    log(`Voice command: "${message.payload}"`);
    executeCommand(ws, { ...message, type: 'command' });
}

/**
 * Handle AI prompt - forward to Cursor AI
 */
function handleAIPrompt(ws, message) {
    const prompt = message.payload;
    log(`AI Prompt: "${prompt}"`);

    // Send streaming response indication
    ws.send(JSON.stringify({
        type: 'ai_response',
        payload: `Processing: "${prompt}"...\n`,
        id: message.id,
        timestamp: Date.now()
    }));

    // Use Cursor CLI or API to send AI prompt
    // This is a simplified version - in production you'd use the Cursor API
    const cmd = `"${CURSOR_CLI}" --command "cursor.aiChat" --args "${prompt.replace(/"/g, '\\"')}"`;

    exec(cmd, { cwd: WORKSPACE, timeout: 30000 }, (err, stdout) => {
        ws.send(JSON.stringify({
            type: 'ai_response',
            payload: stdout || `Sent to Cursor AI: "${prompt}"`,
            id: message.id,
            timestamp: Date.now()
        }));
    });
}

/**
 * Handle file requests
 */
function handleFileRequest(ws, message) {
    const requestPath = message.payload || WORKSPACE;
    const fullPath = path.resolve(WORKSPACE, requestPath);
    sendFileTree(ws, fullPath);
}

/**
 * Build and send file tree
 */
function sendFileTree(ws, dirPath) {
    try {
        const tree = buildFileTree(dirPath, 0, 3);
        ws.send(JSON.stringify({
            type: 'file_tree',
            payload: JSON.stringify(tree),
            id: '',
            timestamp: Date.now()
        }));
    } catch (err) {
        sendError(ws, `Failed to read directory: ${err.message}`);
    }
}

function buildFileTree(dirPath, depth, maxDepth) {
    if (depth > maxDepth) return [];

    const items = [];
    const entries = fs.readdirSync(dirPath, { withFileTypes: true });

    // Sort: directories first, then files
    const sorted = entries.sort((a, b) => {
        if (a.isDirectory() && !b.isDirectory()) return -1;
        if (!a.isDirectory() && b.isDirectory()) return 1;
        return a.name.localeCompare(b.name);
    });

    for (const entry of sorted) {
        // Skip hidden and common ignored dirs
        if (entry.name.startsWith('.') || entry.name === 'node_modules' ||
            entry.name === '__pycache__' || entry.name === 'build' ||
            entry.name === 'dist' || entry.name === '.git') continue;

        const fullPath = path.join(dirPath, entry.name);
        const relativePath = path.relative(WORKSPACE, fullPath);

        if (entry.isDirectory()) {
            items.push({
                name: entry.name,
                path: relativePath,
                isDirectory: true,
                children: buildFileTree(fullPath, depth + 1, maxDepth),
                extension: ''
            });
        } else {
            const ext = path.extname(entry.name).slice(1);
            items.push({
                name: entry.name,
                path: relativePath,
                isDirectory: false,
                children: [],
                extension: ext
            });
        }
    }

    return items;
}

/**
 * Send file content
 */
function sendFileContent(ws, filePath) {
    try {
        const content = fs.readFileSync(filePath, 'utf-8');
        const ext = path.extname(filePath).slice(1);
        const fileName = path.basename(filePath);

        ws.send(JSON.stringify({
            type: 'file_content',
            payload: JSON.stringify({
                filePath: path.relative(WORKSPACE, filePath),
                fileName: fileName,
                content: content,
                language: getLanguageId(ext),
                cursorLine: 0,
                cursorColumn: 0,
                isModified: false
            }),
            id: '',
            timestamp: Date.now()
        }));
    } catch (err) {
        sendError(ws, `Failed to read file: ${err.message}`);
    }
}

/**
 * Handle terminal input
 */
function handleTerminalInput(ws, message) {
    const input = message.payload;
    log(`Terminal: ${input}`);

    const child = spawn('sh', ['-c', input], {
        cwd: WORKSPACE,
        env: { ...process.env },
        timeout: 30000
    });

    child.stdout.on('data', (data) => {
        ws.send(JSON.stringify({
            type: 'terminal_output',
            payload: data.toString(),
            id: message.id,
            timestamp: Date.now()
        }));
    });

    child.stderr.on('data', (data) => {
        ws.send(JSON.stringify({
            type: 'terminal_output',
            payload: data.toString(),
            id: message.id,
            timestamp: Date.now()
        }));
    });

    child.on('close', (code) => {
        ws.send(JSON.stringify({
            type: 'terminal_output',
            payload: `\nProcess exited with code ${code}\n`,
            id: message.id,
            timestamp: Date.now()
        }));
    });
}

/**
 * Handle editor actions
 */
function handleEditorAction(ws, message) {
    const action = message.payload;
    debug(`Editor action: ${action}`);

    // Map simple actions to Cursor CLI commands
    const actionMap = {
        'undo': 'undo',
        'redo': 'redo',
        'copy': 'editor.action.clipboardCopyAction',
        'cut': 'editor.action.clipboardCutAction',
        'paste': 'editor.action.clipboardPasteAction',
        'find': 'actions.find',
        'replace': 'editor.action.startFindReplaceAction',
        'format': 'editor.action.formatDocument',
        'save': 'workbench.action.files.save',
        'cursor.compose': 'cursor.compose',
    };

    const command = actionMap[action] || action;
    exec(`"${CURSOR_CLI}" --command "${command}"`, { cwd: WORKSPACE, timeout: 5000 }, (err) => {
        if (err) debug(`Action error: ${err.message}`);
        sendResponse(ws, message.id, `Action: ${action}`);
    });
}

/**
 * Simulate keyboard shortcut (Linux fallback)
 */
function simulateKeybinding(command) {
    const keyMap = {
        'undo': 'ctrl+z',
        'redo': 'ctrl+shift+z',
        'workbench.action.files.save': 'ctrl+s',
        'workbench.action.files.saveAll': 'ctrl+shift+s',
        'actions.find': 'ctrl+f',
        'workbench.action.findInFiles': 'ctrl+shift+f',
        'editor.action.formatDocument': 'ctrl+shift+i',
        'workbench.action.showCommands': 'ctrl+shift+p',
        'workbench.action.terminal.toggleTerminal': 'ctrl+grave',
        'workbench.action.quickOpen': 'ctrl+p',
    };

    const keys = keyMap[command];
    if (keys) {
        exec(`xdotool key ${keys}`, (err) => {
            if (err) debug(`xdotool error: ${err.message}`);
        });
    }
}

/**
 * Handle build commands (npm run build, npm test, etc.)
 */
function handleBuildCommand(ws, message) {
    const command = message.payload;

    if (command === 'STOP') {
        // Kill active build processes
        buildProcesses.forEach((proc, id) => {
            try { proc.kill('SIGTERM'); } catch (_) {}
        });
        buildProcesses.clear();
        ws.send(JSON.stringify({ type: 'build_status', payload: 'idle', id: message.id, timestamp: Date.now() }));
        return;
    }

    log(`Build: ${command}`);
    ws.send(JSON.stringify({ type: 'build_status', payload: 'building', id: message.id, timestamp: Date.now() }));
    ws.send(JSON.stringify({ type: 'build_output', payload: `$ ${command}`, id: message.id, timestamp: Date.now() }));

    const child = spawn('sh', ['-c', command], {
        cwd: WORKSPACE,
        env: { ...process.env, FORCE_COLOR: '0' },
        timeout: 300000 // 5 min
    });

    buildProcesses.set(message.id, child);

    child.stdout.on('data', (data) => {
        const text = data.toString();
        text.split('\n').forEach(line => {
            if (line.trim()) {
                ws.send(JSON.stringify({ type: 'build_output', payload: line, id: message.id, timestamp: Date.now() }));
            }
        });
    });

    child.stderr.on('data', (data) => {
        const text = data.toString();
        text.split('\n').forEach(line => {
            if (line.trim()) {
                ws.send(JSON.stringify({ type: 'build_output', payload: line, id: message.id, timestamp: Date.now() }));
            }
        });
    });

    child.on('close', (code) => {
        buildProcesses.delete(message.id);
        const status = code === 0 ? 'success' : 'failed';
        ws.send(JSON.stringify({ type: 'build_output', payload: `\nProcess exited with code ${code}`, id: message.id, timestamp: Date.now() }));
        ws.send(JSON.stringify({ type: 'build_status', payload: status, id: message.id, timestamp: Date.now() }));
    });
}

/**
 * Handle dev server start/stop
 * Detects URLs from stdout (Vite, Next.js, CRA, etc.)
 */
function handleDevServer(ws, message) {
    const command = message.payload;

    if (command === 'STOP') {
        if (devServerProcess) {
            log('Stopping dev server...');
            try {
                // Kill process group
                process.kill(-devServerProcess.pid, 'SIGTERM');
            } catch (_) {
                try { devServerProcess.kill('SIGTERM'); } catch (_) {}
            }
            devServerProcess = null;
            devServerUrl = '';
            ws.send(JSON.stringify({
                type: 'dev_server_status',
                payload: JSON.stringify({ isRunning: false, url: '', port: 0, framework: '', pid: 0 }),
                id: message.id, timestamp: Date.now()
            }));
        }
        return;
    }

    // Stop existing dev server if running
    if (devServerProcess) {
        try { process.kill(-devServerProcess.pid, 'SIGTERM'); } catch (_) {
            try { devServerProcess.kill('SIGTERM'); } catch (_) {}
        }
    }

    log(`Starting dev server: ${command}`);
    ws.send(JSON.stringify({ type: 'build_status', payload: 'building', id: message.id, timestamp: Date.now() }));
    ws.send(JSON.stringify({ type: 'build_output', payload: `$ ${command}`, id: message.id, timestamp: Date.now() }));

    devServerProcess = spawn('sh', ['-c', command], {
        cwd: WORKSPACE,
        env: { ...process.env, FORCE_COLOR: '0', BROWSER: 'none' },
        detached: true
    });

    const detectUrl = (text) => {
        // Detect dev server URLs from various frameworks
        const urlPatterns = [
            /(?:Local|Network|URL):\s+(https?:\/\/[^\s]+)/i,
            /(?:listening|running|started)\s+(?:on|at)\s+(https?:\/\/[^\s]+)/i,
            /(?:http:\/\/localhost:\d+)/,
            /(?:http:\/\/127\.0\.0\.1:\d+)/,
            /(?:http:\/\/0\.0\.0\.0:\d+)/,
        ];

        for (const pattern of urlPatterns) {
            const match = text.match(pattern);
            if (match) {
                let url = match[1] || match[0];
                // Replace 0.0.0.0 / 127.0.0.1 with the server's host for remote access
                url = url.replace('0.0.0.0', getLocalIP()).replace('127.0.0.1', getLocalIP()).replace('localhost', getLocalIP());
                return url;
            }
        }
        return null;
    };

    devServerProcess.stdout.on('data', (data) => {
        const text = data.toString();
        text.split('\n').forEach(line => {
            if (line.trim()) {
                ws.send(JSON.stringify({ type: 'build_output', payload: line, id: message.id, timestamp: Date.now() }));
                ws.send(JSON.stringify({ type: 'terminal_output', payload: line, id: message.id, timestamp: Date.now() }));

                const url = detectUrl(line);
                if (url && !devServerUrl) {
                    devServerUrl = url;
                    log(`Dev server URL detected: ${url}`);
                    ws.send(JSON.stringify({ type: 'preview_url', payload: url, id: message.id, timestamp: Date.now() }));
                    ws.send(JSON.stringify({ type: 'build_status', payload: 'success', id: message.id, timestamp: Date.now() }));
                    ws.send(JSON.stringify({
                        type: 'dev_server_status',
                        payload: JSON.stringify({
                            isRunning: true,
                            url: url,
                            port: parseInt(url.match(/:(\d+)/)?.[1] || '0'),
                            framework: detectFramework(),
                            pid: devServerProcess.pid
                        }),
                        id: message.id, timestamp: Date.now()
                    }));
                }
            }
        });
    });

    devServerProcess.stderr.on('data', (data) => {
        const text = data.toString();
        text.split('\n').forEach(line => {
            if (line.trim()) {
                ws.send(JSON.stringify({ type: 'build_output', payload: line, id: message.id, timestamp: Date.now() }));

                const url = detectUrl(line);
                if (url && !devServerUrl) {
                    devServerUrl = url;
                    log(`Dev server URL detected: ${url}`);
                    ws.send(JSON.stringify({ type: 'preview_url', payload: url, id: message.id, timestamp: Date.now() }));
                    ws.send(JSON.stringify({ type: 'build_status', payload: 'success', id: message.id, timestamp: Date.now() }));
                }
            }
        });
    });

    devServerProcess.on('close', (code) => {
        log(`Dev server exited with code ${code}`);
        devServerProcess = null;
        devServerUrl = '';
        ws.send(JSON.stringify({
            type: 'dev_server_status',
            payload: JSON.stringify({ isRunning: false, url: '', port: 0, framework: '', pid: 0 }),
            id: message.id, timestamp: Date.now()
        }));
        if (code !== 0 && code !== null) {
            ws.send(JSON.stringify({ type: 'build_status', payload: 'failed', id: message.id, timestamp: Date.now() }));
        }
    });
}

/**
 * Detect project framework from package.json
 */
function detectFramework() {
    try {
        const pkg = JSON.parse(fs.readFileSync(path.join(WORKSPACE, 'package.json'), 'utf-8'));
        const deps = { ...pkg.dependencies, ...pkg.devDependencies };
        if (deps['next']) return 'Next.js';
        if (deps['nuxt']) return 'Nuxt';
        if (deps['vite']) return 'Vite';
        if (deps['react-scripts']) return 'Create React App';
        if (deps['@angular/core']) return 'Angular';
        if (deps['svelte']) return 'Svelte';
        if (deps['vue']) return 'Vue';
        if (deps['gatsby']) return 'Gatsby';
        if (deps['astro']) return 'Astro';
        if (deps['remix']) return 'Remix';
    } catch (_) {}
    return 'Unknown';
}

/**
 * Get local IP for dev server URL replacement
 */
function getLocalIP() {
    const interfaces = os.networkInterfaces();
    for (const name of Object.keys(interfaces)) {
        for (const iface of interfaces[name]) {
            if (iface.family === 'IPv4' && !iface.internal) {
                return iface.address;
            }
        }
    }
    return 'localhost';
}

/**
 * Handle Cloudflare Wrangler deploy commands.
 * Supports: deploy, pages deploy, dev, tail, init, kv, r2, d1, secret, whoami
 */
let deployProcess = null;

function handleDeployCommand(ws, message) {
    const command = message.payload;

    if (command === 'STOP') {
        if (deployProcess) {
            try { deployProcess.kill('SIGTERM'); } catch (_) {}
            deployProcess = null;
        }
        ws.send(JSON.stringify({ type: 'deploy_status', payload: 'idle', id: message.id, timestamp: Date.now() }));
        return;
    }

    // Kill existing deploy process
    if (deployProcess) {
        try { deployProcess.kill('SIGTERM'); } catch (_) {}
    }

    log(`Deploy: ${command}`);
    ws.send(JSON.stringify({ type: 'deploy_status', payload: 'deploying', id: message.id, timestamp: Date.now() }));
    ws.send(JSON.stringify({ type: 'deploy_output', payload: `$ ${command}`, id: message.id, timestamp: Date.now() }));
    ws.send(JSON.stringify({ type: 'deploy_output', payload: '', id: message.id, timestamp: Date.now() }));

    // Check for wrangler.toml
    const hasWranglerToml = fs.existsSync(path.join(WORKSPACE, 'wrangler.toml')) ||
                            fs.existsSync(path.join(WORKSPACE, 'wrangler.jsonc')) ||
                            fs.existsSync(path.join(WORKSPACE, 'wrangler.json'));

    if (!hasWranglerToml && !command.includes('init') && !command.includes('whoami') &&
        !command.includes('list') && !command.includes('--help')) {
        ws.send(JSON.stringify({
            type: 'deploy_output',
            payload: '⚠️  No wrangler.toml found. Run "wrangler init" first.',
            id: message.id, timestamp: Date.now()
        }));
    }

    deployProcess = spawn('sh', ['-c', command], {
        cwd: WORKSPACE,
        env: {
            ...process.env,
            FORCE_COLOR: '0',
            // Pass through Cloudflare credentials if set
            CLOUDFLARE_API_TOKEN: process.env.CLOUDFLARE_API_TOKEN || '',
            CLOUDFLARE_ACCOUNT_ID: process.env.CLOUDFLARE_ACCOUNT_ID || '',
            CF_API_TOKEN: process.env.CF_API_TOKEN || process.env.CLOUDFLARE_API_TOKEN || '',
        },
        timeout: 300000
    });

    let deployUrl = '';

    deployProcess.stdout.on('data', (data) => {
        const text = data.toString();
        text.split('\n').forEach(line => {
            if (line.trim()) {
                ws.send(JSON.stringify({ type: 'deploy_output', payload: line, id: message.id, timestamp: Date.now() }));

                // Detect deployed URL from wrangler output
                const urlPatterns = [
                    /Published\s+.*?(https:\/\/[^\s]+\.workers\.dev)/i,
                    /Deployment complete!\s+.*?(https:\/\/[^\s]+)/i,
                    /(https:\/\/[^\s]+\.workers\.dev)/,
                    /(https:\/\/[^\s]+\.pages\.dev)/,
                    /URL:\s+(https:\/\/[^\s]+)/i,
                    /Preview URL:\s+(https:\/\/[^\s]+)/i,
                ];

                for (const pattern of urlPatterns) {
                    const match = line.match(pattern);
                    if (match && !deployUrl) {
                        deployUrl = match[1] || match[0];
                        log(`Deploy URL detected: ${deployUrl}`);
                    }
                }

                // Detect worker name
                const nameMatch = line.match(/Deploying\s+["']?(\S+?)["']?\s/i) ||
                                  line.match(/Worker\s+["']?(\S+?)["']?\s/i);
                if (nameMatch) {
                    debug(`Worker name: ${nameMatch[1]}`);
                }
            }
        });
    });

    deployProcess.stderr.on('data', (data) => {
        const text = data.toString();
        text.split('\n').forEach(line => {
            if (line.trim()) {
                ws.send(JSON.stringify({ type: 'deploy_output', payload: line, id: message.id, timestamp: Date.now() }));
            }
        });
    });

    deployProcess.on('close', (code) => {
        deployProcess = null;
        const status = code === 0 ? 'success' : 'failed';
        ws.send(JSON.stringify({ type: 'deploy_output', payload: '', id: message.id, timestamp: Date.now() }));

        if (code === 0 && deployUrl) {
            ws.send(JSON.stringify({
                type: 'deploy_output',
                payload: `✅ Deployed successfully to: ${deployUrl}`,
                id: message.id, timestamp: Date.now()
            }));
        } else if (code === 0) {
            ws.send(JSON.stringify({
                type: 'deploy_output',
                payload: `✅ Command completed successfully`,
                id: message.id, timestamp: Date.now()
            }));
        } else {
            ws.send(JSON.stringify({
                type: 'deploy_output',
                payload: `✘ Process exited with code ${code}`,
                id: message.id, timestamp: Date.now()
            }));
        }

        ws.send(JSON.stringify({ type: 'deploy_status', payload: status, id: message.id, timestamp: Date.now() }));
    });
}

/**
 * Handle R2 Object Storage commands.
 * Wraps wrangler r2 CLI for bucket/object operations and parses JSON output.
 */
function handleR2Command(ws, message) {
    let cmd;
    try { cmd = JSON.parse(message.payload); } catch (_) { sendError(ws, 'Invalid R2 command'); return; }

    const action = cmd.action;
    const bucket = cmd.bucket || '';
    const key = cmd.key || '';
    const prefix = cmd.prefix || '';

    log(`R2: ${action} bucket=${bucket} key=${key}`);

    switch (action) {
        case 'list_buckets': {
            exec('npx wrangler r2 bucket list --json 2>/dev/null || npx wrangler r2 bucket list', { cwd: WORKSPACE, timeout: 30000 }, (err, stdout) => {
                let buckets = [];
                try {
                    buckets = JSON.parse(stdout);
                } catch (_) {
                    // Parse text output: lines like "  name  created"
                    stdout.split('\n').filter(l => l.trim() && !l.includes('Name')).forEach(line => {
                        const parts = line.trim().split(/\s{2,}/);
                        if (parts[0]) buckets.push({ name: parts[0], createdAt: parts[1] || '' });
                    });
                }
                ws.send(JSON.stringify({ type: 'r2_data', payload: JSON.stringify({ type: 'buckets', data: buckets }), id: message.id, timestamp: Date.now() }));
            });
            break;
        }
        case 'list_objects': {
            const prefixArg = prefix ? `--prefix="${prefix}"` : '';
            exec(`npx wrangler r2 object list "${bucket}" ${prefixArg} --json 2>/dev/null || npx wrangler r2 object list "${bucket}" ${prefixArg}`, { cwd: WORKSPACE, timeout: 30000, maxBuffer: 10 * 1024 * 1024 }, (err, stdout) => {
                let objects = [];
                try {
                    const parsed = JSON.parse(stdout);
                    objects = (parsed.objects || parsed || []).map(o => ({
                        key: o.key || o.Key || '',
                        size: o.size || o.Size || 0,
                        lastModified: o.last_modified || o.LastModified || o.uploaded || ''
                    }));
                } catch (_) {
                    // Fallback: parse text
                    stdout.split('\n').filter(l => l.trim()).forEach(line => {
                        const parts = line.trim().split(/\s{2,}/);
                        if (parts[0] && !parts[0].startsWith('Key')) {
                            objects.push({ key: parts[0], size: parseInt(parts[1]) || 0, lastModified: parts[2] || '' });
                        }
                    });
                }
                // Add folder entries for common prefixes
                const folders = new Set();
                const prefixLen = prefix.length;
                objects.forEach(o => {
                    const rest = o.key.substring(prefixLen);
                    const slashIdx = rest.indexOf('/');
                    if (slashIdx > 0) folders.add(prefix + rest.substring(0, slashIdx + 1));
                });
                const folderEntries = [...folders].map(f => ({ key: f, size: 0, lastModified: '' }));
                const fileEntries = objects.filter(o => {
                    const rest = o.key.substring(prefixLen);
                    return !rest.includes('/') || rest.endsWith('/');
                });
                ws.send(JSON.stringify({ type: 'r2_data', payload: JSON.stringify({ type: 'objects', data: [...folderEntries, ...fileEntries] }), id: message.id, timestamp: Date.now() }));
            });
            break;
        }
        case 'get_object': {
            exec(`npx wrangler r2 object get "${bucket}/${key}" --pipe 2>/dev/null | head -c 50000`, { cwd: WORKSPACE, timeout: 15000, maxBuffer: 5 * 1024 * 1024 }, (err, stdout) => {
                ws.send(JSON.stringify({ type: 'r2_data', payload: JSON.stringify({ type: 'preview', key: key, data: stdout || '(binary or empty)' }), id: message.id, timestamp: Date.now() }));
            });
            break;
        }
        case 'delete_object': {
            exec(`npx wrangler r2 object delete "${bucket}/${key}"`, { cwd: WORKSPACE, timeout: 15000 }, (err) => {
                if (err) sendError(ws, `R2 delete failed: ${err.message}`);
                else log(`R2: Deleted ${bucket}/${key}`);
            });
            break;
        }
    }
}

/**
 * Handle D1 Database commands.
 * Wraps wrangler d1 CLI for database/table/query operations.
 */
function handleD1Command(ws, message) {
    let cmd;
    try { cmd = JSON.parse(message.payload); } catch (_) { sendError(ws, 'Invalid D1 command'); return; }

    const action = cmd.action;
    const database = cmd.database || '';
    const table = cmd.table || '';
    const sql = cmd.sql || '';

    log(`D1: ${action} db=${database} table=${table}`);

    switch (action) {
        case 'list_databases': {
            exec('npx wrangler d1 list --json 2>/dev/null || npx wrangler d1 list', { cwd: WORKSPACE, timeout: 30000 }, (err, stdout) => {
                let databases = [];
                try {
                    databases = JSON.parse(stdout);
                } catch (_) {
                    stdout.split('\n').filter(l => l.trim() && !l.includes('UUID')).forEach(line => {
                        const parts = line.trim().split(/\s{2,}|\t+/);
                        if (parts.length >= 2) databases.push({ uuid: parts[0], name: parts[1], num_tables: 0 });
                    });
                }
                ws.send(JSON.stringify({ type: 'd1_data', payload: JSON.stringify({ type: 'databases', data: databases }), id: message.id, timestamp: Date.now() }));
            });
            break;
        }
        case 'list_tables': {
            const q = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE '_cf_%' ORDER BY name";
            exec(`npx wrangler d1 execute "${database}" --command="${q}" --json 2>/dev/null`, { cwd: WORKSPACE, timeout: 30000 }, (err, stdout) => {
                let tables = [];
                try {
                    const parsed = JSON.parse(stdout);
                    const results = parsed[0]?.results || parsed.results || parsed;
                    tables = (Array.isArray(results) ? results : []).map(r => ({ name: r.name }));
                } catch (_) {
                    // Try non-JSON fallback
                    exec(`npx wrangler d1 execute "${database}" --command="${q}"`, { cwd: WORKSPACE, timeout: 30000 }, (err2, stdout2) => {
                        stdout2.split('\n').forEach(line => {
                            const trimmed = line.trim();
                            if (trimmed && !trimmed.startsWith('┌') && !trimmed.startsWith('├') && !trimmed.startsWith('└') && !trimmed.includes('name')) {
                                const name = trimmed.replace(/[│\s]/g, '');
                                if (name) tables.push({ name });
                            }
                        });
                        ws.send(JSON.stringify({ type: 'd1_data', payload: JSON.stringify({ type: 'tables', data: tables }), id: message.id, timestamp: Date.now() }));
                    });
                    return;
                }
                ws.send(JSON.stringify({ type: 'd1_data', payload: JSON.stringify({ type: 'tables', data: tables }), id: message.id, timestamp: Date.now() }));
            });
            break;
        }
        case 'table_schema': {
            exec(`npx wrangler d1 execute "${database}" --command="PRAGMA table_info(${table})" --json 2>/dev/null`, { cwd: WORKSPACE, timeout: 15000 }, (err, stdout) => {
                let columns = [];
                try {
                    const parsed = JSON.parse(stdout);
                    const results = parsed[0]?.results || parsed.results || parsed;
                    columns = (Array.isArray(results) ? results : []).map(r => ({
                        name: r.name, type: r.type || 'TEXT', pk: r.pk === 1, notnull: r.notnull === 1, default: r.dflt_value
                    }));
                } catch (_) {}
                ws.send(JSON.stringify({ type: 'd1_data', payload: JSON.stringify({ type: 'columns', data: columns }), id: message.id, timestamp: Date.now() }));
            });
            break;
        }
        case 'execute': {
            const startTime = Date.now();
            const escapedSql = sql.replace(/"/g, '\\"');
            exec(`npx wrangler d1 execute "${database}" --command="${escapedSql}" --json 2>/dev/null`, { cwd: WORKSPACE, timeout: 30000, maxBuffer: 10 * 1024 * 1024 }, (err, stdout, stderr) => {
                const duration = Date.now() - startTime;

                if (err && !stdout) {
                    ws.send(JSON.stringify({ type: 'd1_data', payload: JSON.stringify({
                        type: 'query_result', columns: [], rows: [], rows_affected: 0, duration, error: stderr || err.message
                    }), id: message.id, timestamp: Date.now() }));
                    return;
                }

                try {
                    const parsed = JSON.parse(stdout);
                    const result = parsed[0] || parsed;
                    const results = result.results || [];
                    const columns = results.length > 0 ? Object.keys(results[0]) : [];
                    const rows = results.map(r => columns.map(c => r[c] != null ? String(r[c]) : 'null'));

                    ws.send(JSON.stringify({ type: 'd1_data', payload: JSON.stringify({
                        type: 'query_result',
                        columns,
                        rows,
                        rows_affected: result.meta?.changes || 0,
                        duration,
                        error: ''
                    }), id: message.id, timestamp: Date.now() }));
                } catch (_) {
                    // Fallback: send raw output
                    ws.send(JSON.stringify({ type: 'd1_data', payload: JSON.stringify({
                        type: 'query_result', columns: ['output'], rows: stdout.split('\n').filter(l => l.trim()).map(l => [l]), rows_affected: 0, duration, error: ''
                    }), id: message.id, timestamp: Date.now() }));
                }
            });
            break;
        }
    }
}

function sendResponse(ws, id, payload) {
    ws.send(JSON.stringify({
        type: 'response',
        payload,
        id,
        timestamp: Date.now()
    }));
}

function sendError(ws, errorMsg) {
    ws.send(JSON.stringify({
        type: 'error',
        payload: errorMsg,
        id: '',
        timestamp: Date.now()
    }));
}

function getLanguageId(ext) {
    const map = {
        'ts': 'typescript', 'tsx': 'typescriptreact',
        'js': 'javascript', 'jsx': 'javascriptreact',
        'py': 'python', 'rb': 'ruby',
        'java': 'java', 'kt': 'kotlin', 'kts': 'kotlin',
        'go': 'go', 'rs': 'rust',
        'cpp': 'cpp', 'c': 'c', 'h': 'c',
        'cs': 'csharp', 'swift': 'swift',
        'json': 'json', 'xml': 'xml',
        'html': 'html', 'css': 'css', 'scss': 'scss',
        'md': 'markdown', 'yaml': 'yaml', 'yml': 'yaml',
        'sh': 'shellscript', 'bash': 'shellscript',
        'sql': 'sql', 'graphql': 'graphql',
        'dart': 'dart', 'php': 'php',
    };
    return map[ext] || 'plaintext';
}

// Graceful shutdown
function cleanup() {
    log('Shutting down...');

    // Kill dev server
    if (devServerProcess) {
        try { process.kill(-devServerProcess.pid, 'SIGTERM'); } catch (_) {
            try { devServerProcess.kill('SIGTERM'); } catch (_) {}
        }
    }

    // Kill build processes
    buildProcesses.forEach((proc) => {
        try { proc.kill('SIGTERM'); } catch (_) {}
    });

    // Kill deploy process
    if (deployProcess) {
        try { deployProcess.kill('SIGTERM'); } catch (_) {}
    }

    wss.close(() => {
        process.exit(0);
    });
}

process.on('SIGINT', cleanup);
process.on('SIGTERM', cleanup);
