import { Component, EventEmitter, Input, Output } from '@angular/core';
import { LoadingState } from '../model';

@Component({
	selector: 'loading-fragment',
	templateUrl: 'loading.component.html',
	styleUrls: ['loading.component.scss']
})
export class LoadingComponent {

	@Input()
	state: LoadingState;

	@Output()
	retryEvent = new EventEmitter<boolean>();


	constructor() {

	}

	retry() {
		this.retryEvent.emit();
	}
}