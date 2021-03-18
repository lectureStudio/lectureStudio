import { NgModule } from '@angular/core';
import {
	MatButtonModule,
	MatCheckboxModule,
	MatDialogModule,
	MatFormFieldModule,
	MatIconModule,
	MatInputModule,
	MatListModule,
	MatProgressBarModule,
	MatRadioModule,
	MatSelectModule,
	MatSnackBarModule,
	MatTabsModule,
	MatToolbarModule
}
from '@angular/material';

@NgModule({
	imports: [
		MatButtonModule,
		MatCheckboxModule,
		MatDialogModule,
		MatFormFieldModule,
		MatIconModule,
		MatInputModule,
		MatListModule,
		MatProgressBarModule,
		MatRadioModule,
		MatSelectModule,
		MatSnackBarModule,
		MatTabsModule,
		MatToolbarModule,
	],
	exports: [
		MatButtonModule,
		MatCheckboxModule,
		MatDialogModule,
		MatFormFieldModule,
		MatIconModule,
		MatInputModule,
		MatListModule,
		MatProgressBarModule,
		MatRadioModule,
		MatSelectModule,
		MatSnackBarModule,
		MatTabsModule,
		MatToolbarModule,
	]
})
export class MaterialModule { }