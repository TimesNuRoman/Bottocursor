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

// Track active terminals
const terminals = new Map();

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
process.on('SIGINT', () => {
    log('Shutting down...');
    wss.close(() => {
        process.exit(0);
    });
});

process.on('SIGTERM', () => {
    log('Shutting down...');
    wss.close(() => {
        process.exit(0);
    });
});
