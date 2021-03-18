import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material';

@Component({
	selector: 'service-absent-dialog',
	templateUrl: 'service-absent.component.html',
})
export class ServiceAbsentComponent {

	constructor(public dialogRef: MatDialogRef<ServiceAbsentComponent>) {

	}

	close(): void {
		this.dialogRef.close();
	}

}