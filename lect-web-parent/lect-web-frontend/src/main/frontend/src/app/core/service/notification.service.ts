import { Injectable, TemplateRef } from '@angular/core';
import { ComponentType } from '@angular/cdk/portal';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';

@Injectable({
	providedIn: 'root'
})
export class NotificationService {

	private readonly duration = 3000;

	private readonly verticalPosition = 'top';


	constructor(private snackBar: MatSnackBar, private dialog: MatDialog) {

	}

	showMessage(text: string) {
		this.snackBar.open(text, null, {
			duration: this.duration,
			verticalPosition: this.verticalPosition,
			panelClass: 'text-notification'
		});
	}

	showErrorMessage(text: string) {
		this.snackBar.open(text, null, {
			duration: this.duration,
			verticalPosition: this.verticalPosition,
			panelClass: 'error-notification'
		});
	}

	showDialog<T>(dialogComponent: ComponentType<T> | TemplateRef<T>): Observable<any> {
		const dialogRef = this.dialog.open(dialogComponent);

		return dialogRef.afterClosed();
	}
}