import { Input } from '@angular/core';
import { FormGroup } from '@angular/forms';

export abstract class OptionComponent {

	@Input()
	form: FormGroup;

	@Input()
	options: string[];


	indexChar(index: number): string {
		return String.fromCharCode(65 + index);
	}
}