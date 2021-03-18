export class HttpResponse<T> {

	readonly status: number;

	readonly statusText: string;

	readonly headers: Map<string, string | string[]>;

	readonly body: T;

}