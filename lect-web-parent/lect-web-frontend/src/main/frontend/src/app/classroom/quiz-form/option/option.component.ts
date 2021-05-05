import { Input, Directive } from '@angular/core';
import { FormGroup } from '@angular/forms';

@Directive()
export abstract class OptionComponent {

	@Input()
	form: FormGroup;

	@Input()
	options: string[];


	indexChar(index: number): string {
		return String.fromCharCode(65 + index);
	}
}