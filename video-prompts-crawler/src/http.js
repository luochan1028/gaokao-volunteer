const UA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36';

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function get(url, opts = {}) {
  const res = await fetch(url, {
    headers: {
      'User-Agent': UA,
      'Accept': 'text/html,application/json;q=0.9,*/*;q=0.8',

      ...(opts.headers || {})
    },
    redirect: 'follow',
    signal: AbortSignal.timeout(opts.timeout || 20000)
  });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status} ${url}`);
  }
  return res.text();
}

module.exports = { get, sleep, UA };