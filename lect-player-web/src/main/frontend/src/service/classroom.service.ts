import { HttpRequest } from "../utils/http-request";
import { Classroom } from "../model/classroom";
import { SlideDocument } from "../model/document";
import { ClassroomDocument } from "../model/classroom-document";
import { DocumentService } from "./document.service";

export class ClassroomServiceClient {

	private readonly apiPath = "/bcast/ws";

	private readonly host: string;


	constructor(host: string) {
		this.host = host;
	}

	getClassrooms(): Promise<Classroom[]> {
		return new HttpRequest().get(this.getFullPath("/classroom/list"));
	}

	getClassroomDocument(classroom: Classroom, classroomDoc: ClassroomDocument): Promise<SlideDocument> {
		return new Promise<SlideDocument>((resolve, reject) => {
			const formData = new FormData();
			formData.set("classroomName", classroom.shortName);
			formData.set("fileName", classroomDoc.fileName);

			new HttpRequest().setResponseType("arraybuffer").post<ArrayBuffer>(this.getFullPath("/stream/document/get"), formData)
				.then((dataBuffer: ArrayBuffer) => {
					if (!dataBuffer) {
						reject("Received empty classroom document");
						return;
					}

					const byteBuffer = new Uint8Array(dataBuffer);

					const docService = new DocumentService();
					docService.loadDocument(byteBuffer)
						.then((doc: SlideDocument) => {
							resolve(doc);
						})
						.catch((reason: string) => {
							reject(reason);
						});
				})
				.catch((error: any) => {
					reject(error);
				});
		});
	}

	private getFullPath(path: string): string {
		return this.host + this.apiPath + path;
	}

}