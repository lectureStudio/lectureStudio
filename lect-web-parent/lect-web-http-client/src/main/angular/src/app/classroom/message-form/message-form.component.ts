import { Component, OnInit } from '@angular/core';
import { Message } from './model/message.model';
import { MessageService } from './service/message.service';
import { MessageServiceData, ClassroomServiceResponse, ServiceResponseStatus, ClassroomServiceType } from '../model';
import { AppStateService, LocaleService, NotificationService } from '../../core/service';

@Component({
	selector: 'message-form',
	templateUrl: './message-form.component.html',
	styleUrls: ['./message-form.component.scss']
})
export class MessageFormComponent implements OnInit {

	serviceData: MessageServiceData;

	message: Message;

	readonly minLength = 8;


	constructor(
		private messageService: MessageService,
		private notifyService: NotificationService,
		private appStateService: AppStateService,
		private localeService: LocaleService)
	{
		this.serviceData = appStateService.classroom.services.find(service => {
			return service._type === ClassroomServiceType.Message;
		});
	}

	ngOnInit(): void {
		this.message = {
			serviceId: this.serviceData.serviceId,
			text: ''
		};
	}

	onSubmit(): void {
		this.messageService.addMessage(this.message).subscribe(
			(response: ClassroomServiceResponse) => {
				const message = this.localeService.translate(response.statusMessage);

				if (response.statusCode !== ServiceResponseStatus.Success) {
					this.notifyService.showErrorMessage(message);
				}
				else {
					// Reset message text.
					this.message.text = '';
					this.notifyService.showMessage(message);
				}
			}
		);
	}

}
