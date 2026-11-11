const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const HOST = '127.0.0.1';

const MIME_TYPES = {
  '.html': 'text/html',
  '.css': 'text/css',
  '.js': 'text/javascript',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml'
};

const server = http.createServer((req, res) => {
  // Security headers
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'DENY');

  let reqUrl = req.url.split('?')[0];

  // API endpoint to return sample Google Drive JSON for local testing
  if (reqUrl === '/api/sample-drive-expenses' || reqUrl === '/sample-expenses') {
    const samplePath = path.join(__dirname, 'sample_google_drive_expenses.json');
    if (fs.existsSync(samplePath)) {
      res.writeHead(200, {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*'
      });
      return res.end(fs.readFileSync(samplePath, 'utf8'));
    }
  }

  if (reqUrl === '/') {
    reqUrl = '/index.html';
  }

  const filePath = path.join(__dirname, 'web', reqUrl);

  // Prevent path traversal
  if (!filePath.startsWith(path.join(__dirname, 'web'))) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    return res.end('Access Denied');
  }

  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      res.writeHead(404, { 'Content-Type': 'text/plain' });
      return res.end('404 Not Found');
    }

    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    res.writeHead(200, { 'Content-Type': contentType });
    fs.createReadStream(filePath).pipe(res);
  });
});

server.listen(PORT, HOST, () => {
  console.log(`FundTracker web preview running at http://${HOST}:${PORT}`);
});
