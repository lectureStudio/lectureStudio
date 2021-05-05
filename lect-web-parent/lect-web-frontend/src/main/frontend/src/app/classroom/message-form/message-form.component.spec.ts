import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MessageFormComponent } from './message-form.component';

describe('MessagePanelComponent', () => {
	let component: MessageFormComponent;
	let fixture: ComponentFixture<MessageFormComponent>;

	beforeEach(waitForAsync(() => {
		TestBed.configureTestingModule({
			declarations: [MessageFormComponent]
		})
			.compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(MessageFormComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create', () => {
		expect(component).toBeTruthy();
	});
});
