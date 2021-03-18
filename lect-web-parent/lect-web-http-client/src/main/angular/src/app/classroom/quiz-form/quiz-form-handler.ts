import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { QuizType } from '../model';

export interface QuizFormHandler {

	createFormGroup(options: string[]): FormGroup;

	getInput(formGroup: FormGroup): string[];

}

class MultipleChoiceHandler implements QuizFormHandler {

	createFormGroup(options: string[]): FormGroup {
		const controls = options.map(o => new FormControl());

		return new FormGroup({
			options: new FormArray(controls)
		});
	}

	getInput(formGroup: FormGroup): string[] {
		const selectedOptions = formGroup.value.options
			.map((v, i) => v ? i : null)
			.filter(v => v !== null);

		return selectedOptions;
	}
}

class NumericChoiceHandler implements QuizFormHandler {

	createFormGroup(options: string[]): FormGroup {
		const controls = options.map(o => new FormControl());

		return new FormGroup({
			options: new FormArray(controls)
		});
	}

	getInput(formGroup: FormGroup): string[] {
		return formGroup.value.options;
	}
}

class SingleChoiceHandler implements QuizFormHandler {

	createFormGroup(options: string[]): FormGroup {
		return new FormGroup({
			options: new FormControl()
		});
	}

	getInput(formGroup: FormGroup): string[] {
		return formGroup.value.options;
	}
}

export const getChoiceHandler = (type: QuizType): QuizFormHandler => {
	switch (type) {
		case QuizType.Multiple:
			return new MultipleChoiceHandler();

		case QuizType.Numeric:
			return new NumericChoiceHandler();

		case QuizType.Single:
			return new SingleChoiceHandler();
	}
};