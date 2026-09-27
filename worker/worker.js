/**
 * Kavir Browser - Cloudflare Worker Proxy
 *
 * Optional protection:
 * Cloudflare Dashboard -> Worker -> Settings -> Variables and Secrets
 * Add a secret named SECRET_TOKEN.
 */
export default {
  async fetch(request, env) {
    const requestUrl = new URL(request.url);
    const targetUrl = requestUrl.searchParams.get("url");

    const configuredToken = env.SECRET_TOKEN;
    if (configuredToken) {
      const suppliedToken = request.headers.get("X-Kavir-Token");
      if (suppliedToken !== configuredToken) {
        return new Response("Unauthorized Access to Kavir Proxy", { status: 403 });
      }
    }

    if (!targetUrl) {
      return new Response(
        "Kavir Proxy is active. Use ?url=https://example.com",
        {
          status: 200,
          headers: { "Content-Type": "text/plain; charset=utf-8" }
        }
      );
    }

    let target;
    try {
      target = new URL(targetUrl);
    } catch {
      return new Response("Invalid target URL", { status: 400 });
    }

    if (target.protocol !== "https:" && target.protocol !== "http:") {
      return new Response("Only HTTP and HTTPS targets are supported", { status: 400 });
    }

    const headers = new Headers(request.headers);
    headers.delete("Host");
    headers.delete("X-Kavir-Token");

    const init = {
      method: request.method,
      headers,
      redirect: "follow"
    };

    if (request.method !== "GET" && request.method !== "HEAD") {
      init.body = request.body;
    }

    try {
      const response = await fetch(target.toString(), init);
      const responseHeaders = new Headers(response.headers);

      responseHeaders.set("Access-Control-Allow-Origin", "*");
      responseHeaders.set("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
      responseHeaders.set("Access-Control-Allow-Headers", "*");

      return new Response(response.body, {
        status: response.status,
        statusText: response.statusText,
        headers: responseHeaders
      });
    } catch (error) {
      return new Response("Proxy error: " + (error?.message || "unknown error"), {
        status: 502
      });
    }
  }
};
