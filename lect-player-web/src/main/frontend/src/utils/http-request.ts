import { HttpResponse } from "./http-response";

export type HttpMethod = "DELETE" | "GET" | "HEAD" | "OPTIONS" | "PATCH" | "POST" | "PUT";

export type HttpProgressListener = (event: ProgressEvent) => void;

export interface HttpRequestOptions {

	params?: string | URLSearchParams;

	headers?: Map<string, string | string[]>;

	returnType?: "body" | "response";

	responseType?: XMLHttpRequestResponseType;

	withCredentials?: boolean;

	timeout?: number;

}

export class HttpRequest {

	private requestOptions: HttpRequestOptions;

	private onProgress: HttpProgressListener;


	constructor(options?: HttpRequestOptions) {
		this.requestOptions = options || {
			returnType: "body",
			responseType: "json",
			withCredentials: false,
			timeout: 0
		};
	}

	setHttpParams(params: string | URLSearchParams): HttpRequest {
		this.requestOptions.params = params;
		return this;
	}

	setHttpHeaders(headers: Map<string, string | string[]>): HttpRequest {
		this.requestOptions.headers = headers;
		return this;
	}

	setReturnType(returnType: "body" | "response"): HttpRequest {
		this.requestOptions.returnType = returnType;
		return this;
	}

	setResponseType(responseType: XMLHttpRequestResponseType): HttpRequest {
		this.requestOptions.responseType = responseType;
		return this;
	}

	setWithCredentials(withCredentials: boolean): HttpRequest {
		this.requestOptions.withCredentials = withCredentials;
		return this;
	}

	setTimeout(timeout: number): HttpRequest {
		this.requestOptions.timeout = timeout;
		return this;
	}

	setOptions(options: HttpRequestOptions): HttpRequest {
		this.requestOptions = options;
		return this;
	}

	setOnProgress(onProgress: HttpProgressListener): HttpRequest {
		this.onProgress = onProgress;
		return this;
	}

	delete(url: string): Promise<void> {
		return this.request("DELETE", url);
	}

	get<T>(url: string): Promise<T> {
		return this.request("GET", url);
	}

	head(url: string): Promise<void> {
		return this.request("HEAD", url);
	}

	options(url: string): Promise<void> {
		return this.request("OPTIONS", url);
	}

	patch<T>(url: string, body: any): Promise<T> {
		return this.request("PATCH", url, body);
	}

	post<T>(url: string, body: any): Promise<T> {
		return this.request("POST", url, body);
	}

	put<T>(url: string, body: any): Promise<T> {
		return this.request("PUT", url, body);
	}

	private request<T>(method: HttpMethod, url: string, body?: any): Promise<T> {
		const request = new XMLHttpRequest();
		request.open(method, this.composeUrl(url), true);
		request.responseType = this.requestOptions.responseType;
		request.timeout = this.requestOptions.timeout;
		request.withCredentials = this.requestOptions.withCredentials;

		this.setHeaders(body, request);

		return new Promise<T>((resolve, reject) => {
			const errorHandler = () => {
				reject(this.getResponse(request));
			};

			request.onload = () => {
				if (request.status >= 200 && request.status < 400) {
					let returnValue = null;

					if (this.requestOptions.returnType === "response") {
						returnValue = this.getResponse(request);
					}
					else {
						returnValue = this.decodeBody(request);
					}

					resolve(returnValue);
				}
				else {
					errorHandler();
				}
			};
			request.ontimeout = errorHandler;
			request.onerror = errorHandler;

			if (this.onProgress) {
				if (body && request.upload) {
					request.upload.onprogress = this.onProgress;
				}
				else {
					request.onprogress = this.onProgress;
				}
			}

			request.send(this.encodeBody(body));
		});
	}

	private getResponse<T>(request: XMLHttpRequest): HttpResponse<T> {
		const response: HttpResponse<T> = {
			body: this.decodeBody(request),
			headers: this.getHeaders(request),
			status: request.status,
			statusText: request.statusText
		};

		return response;
	}

	private composeUrl(url: string): string {
		const params = this.requestOptions.params;

		if (!params) {
			return url;
		}

		const paramsEnc = params ? params.toString() : "";

		if (paramsEnc) {
			const qIndex = url.indexOf("?");
			const sep: string = qIndex === -1 ? "?" : (qIndex < url.length - 1 ? "&" : "");

			url = url + sep + paramsEnc;
		}

		return url;
	}

	private getContentType(body: any): string {
		if (body == null) {
			return null;
		}

		if (body instanceof Blob) {
			return body.type;
		}
		// Rely on the browser's content type assignment.
		if (body instanceof FormData) {
			return null;
		}
		if (typeof body === "string") {
			return "text/plain";
		}
		if (body instanceof URLSearchParams) {
			return "application/x-www-form-urlencoded; charset=UTF-8";
		}
		if (typeof body === "object" || typeof body === "number" || Array.isArray(body)) {
			return "application/json";
		}

		return null;
	}

	private encodeBody(body: any): any {
		if (body == null) {
			return null;
		}

		if (body instanceof ArrayBuffer || body instanceof Blob || body instanceof FormData || typeof body === "string") {
			return body;
		}

		if (typeof body === "object" || typeof body === "boolean" || Array.isArray(body)) {
			return JSON.stringify(body);
		}

		return (body as any).toString();
	}

	private decodeBody(request: XMLHttpRequest): any {
		let body = request.response || null;
		let responseType: string = request.responseType;

		if (!responseType) {
			responseType = request.getResponseHeader("Content-Type");

			const match = new RegExp("^\\w+\/(\\w+);?").exec(responseType);

			if (match) {
				responseType = match[1];
			}
		}

		if (responseType === "json" && typeof body === "string") {
			body = body !== "" ? JSON.parse(body) : null;
		}

		return body;
	}

	private setHeaders(body: any, request: XMLHttpRequest): void {
		const headers = this.requestOptions.headers;

		if (headers) {
			for (const [key, value] of headers) {
				if (Array.isArray(value)) {
					for (const v of value) {
						request.setRequestHeader(key, v);
					}
				}
				else {
					request.setRequestHeader(key, value);
				}
			}
		}
		if (!headers || !headers.has("Content-Type")) {
			const contentType = this.getContentType(body);

			if (contentType) {
				request.setRequestHeader("Content-Type", contentType);
			}
		}
		if (!headers || !headers.has("Accept")) {
			request.setRequestHeader("Accept", "application/json, text/plain, */*");
		}
	}

	private getHeaders(request: XMLHttpRequest): Map<string, string | string[]> {
		const headers = new Map<string, string | string[]>();
		const responseHeaders = request.getAllResponseHeaders();

		if (responseHeaders) {
			const headerList = responseHeaders.trim().split(/[\r\n]+/);

			for (const line of headerList) {
				const index = line.indexOf(':');

				if (index > 0) {
					const name = line.slice(0, index);
					const value = line.slice(index + 1).trim();
					const header = name.toLowerCase();
					const headerValues = headers.get(header);

					if (headerValues) {
						(typeof headerValues === "string" ? [headerValues] : headerValues).push(value);
					}
					else {
						headers.set(header, value);
					}
				}
			}
		}

		return headers;
	}
}