import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['register', 'setToken']);

    await TestBed.configureTestingModule({
      imports: [RegisterComponent, RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates the component', () => {
    expect(component).toBeTruthy();
  });

  it('submits registration and navigates on success', () => {
    authServiceSpy.register.and.returnValue(of({ token: 'new-token' }));
    const event = new Event('submit');
    spyOn(event, 'preventDefault');

    component.name = 'sam';
    component.email = 'sam@example.com';
    component.password = '123456';
    component.register(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(authServiceSpy.register).toHaveBeenCalledWith({
      name: 'sam',
      email: 'sam@example.com',
      password: '123456',
    });
    expect(authServiceSpy.setToken).toHaveBeenCalledWith('new-token');
    expect(router.navigate).toHaveBeenCalledWith(['/']);
    expect(component.error).toBe('');
  });

  it('shows API error message on failure', () => {
    authServiceSpy.register.and.returnValue(
      throwError(() => ({ error: { error: 'Account already exists' } }))
    );
    const event = new Event('submit');
    spyOn(event, 'preventDefault');

    component.register(event);

    expect(component.error).toBe('Account already exists');
  });
});
