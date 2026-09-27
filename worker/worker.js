/** Kavir Browser - Advanced Cloudflare Worker Proxy */
const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS",
  "Access-Control-Allow-Headers": "*",
  "Access-Control-Expose-Headers": "Accept-Ranges,Content-Length,Content-Range,Content-Type,Location,ETag,Last-Modified",
  "Access-Control-Max-Age": "86400"
};

function proxyUrl(workerUrl, target) {
  const u = new URL(workerUrl);
  u.search = "";
  u.searchParams.set("url", target.toString());
  return u.toString();
}

export default {
  async fetch(request, env) {
    const incoming = new URL(request.url);

    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: corsHeaders });
    }

    const targetParam = incoming.searchParams.get("url");
    if (!targetParam) {
      return new Response("<h1>Kavir Browser Proxy</h1><p>Worker is online.</p>", {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "text/html; charset=utf-8" }
      });
    }

    if (env.SECRET_TOKEN && request.headers.get("X-Kavir-Token") !== env.SECRET_TOKEN) {
      return new Response("Forbidden", { status: 403, headers: corsHeaders });
    }

    let target;
    try {
      target = new URL(targetParam);
    } catch {
      return new Response("Invalid target URL", { status: 400, headers: corsHeaders });
    }

    if (target.protocol !== "http:" && target.protocol !== "https:") {
      return new Response("Only HTTP/HTTPS targets are supported", {
        status: 400, headers: corsHeaders
      });
    }

    const headers = new Headers();
    for (const [key, value] of request.headers) {
      const lower = key.toLowerCase();
      if (lower === "host" || lower === "x-kavir-token" || lower === "content-length" || lower === "connection") continue;
      headers.set(key, value);
    }

    headers.set(
      "User-Agent",
      request.headers.get("User-Agent") ||
      "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"
    );

    const init = {
      method: request.method,
      headers,
      redirect: "manual"
    };

    if (request.method !== "GET" && request.method !== "HEAD") {
      init.body = request.body;
    }

    try {
      const upstream = await fetch(target.toString(), init);
      const responseHeaders = new Headers(upstream.headers);

      for (const [key, value] of Object.entries(corsHeaders)) responseHeaders.set(key, value);
      responseHeaders.delete("content-security-policy");
      responseHeaders.delete("content-security-policy-report-only");
      responseHeaders.delete("x-frame-options");

      if (upstream.status >= 300 && upstream.status < 400) {
        const location = upstream.headers.get("Location");
        if (location) responseHeaders.set("Location", proxyUrl(request.url, new URL(location, target)));
      }

      return new Response(upstream.body, {
        status: upstream.status,
        statusText: upstream.statusText,
        headers: responseHeaders
      });
    } catch (error) {
      return new Response("Proxy error: " + (error?.message || "unknown"), {
        status: 502,
        headers: { ...corsHeaders, "Content-Type": "text/plain; charset=utf-8" }
      });
    }
  }
};