import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';

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