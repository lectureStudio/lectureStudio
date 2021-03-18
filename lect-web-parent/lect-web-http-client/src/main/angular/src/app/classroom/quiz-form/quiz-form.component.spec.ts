import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { QuizFormComponent } from './quiz-form.component';

describe('QuizPanelComponent', () => {
	let component: QuizFormComponent;
	let fixture: ComponentFixture<QuizFormComponent>;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [QuizFormComponent]
		})
			.compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(QuizFormComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
