import { Component, Input } from '@angular/core';
import { Route, Router } from '@angular/router';
import { MatSelectChange } from '@angular/material/select';
import { Classroom, ClassroomServiceType, ServiceData } from '../../classroom/model';
import { Locale } from '../model';
import { LocaleService } from '../service';
import { MessageFormComponent } from '../../classroom/message-form/message-form.component';
import { QuizFormComponent } from '../../classroom/quiz-form/quiz-form.component';
import { RouteAnimations } from '../animation';

@Component({
	selector: 'app-nav',
	templateUrl: './nav.component.html',
	styleUrls: ['./nav.component.scss'],
	animations: [RouteAnimations]
})
export class NavComponent {

	readonly locales: Locale[];

	locale: string;

	_classroom: Classroom;

	navLinks: any[];


	constructor(
		private router: Router,
		private localeService: LocaleService)
	{
		this.locales = localeService.getLocales();
		this.locale = localeService.getSelectedLocale().tag;
	}

	get classroom(): Classroom {
		return this._classroom;
	}

	@Input()
	set classroom(classroom: Classroom) {
		if (classroom) {
			this._classroom = classroom;

			this.initRoutes();
		}
	}

	setLocale(event: MatSelectChange): void {
		this.localeService.selectLocale(event.value);
	}

	private initRoutes() {
		this.navLinks = [];

		let defaultPath: string;

		this.classroom.services.forEach((service: ServiceData) => {
			const type = service._type;
			let route: Route;

			switch (type) {
				case ClassroomServiceType.Quiz:
					route = { path: 'quiz', component: QuizFormComponent };
					break;

				case ClassroomServiceType.Message:
					route = { path: 'message', component: MessageFormComponent };
					break;
			}

			this.router.config.push(route);
			this.navLinks.push({ text: type, path: route.path });

			if (!defaultPath) {
				// Use first available route as the default one.
				defaultPath = route.path;
			}
		});

		// Set default route.
		this.router.config.push({ path: '', redirectTo: defaultPath });
		this.router.config.push({ path: '**', redirectTo: defaultPath });

		this.router.initialNavigation();
	}
}
