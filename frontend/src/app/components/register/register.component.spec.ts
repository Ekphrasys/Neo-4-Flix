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
    component.password = 'Pass1!aa';
    component.validatePassword();
    component.register(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(authServiceSpy.register).toHaveBeenCalledWith({
      username: 'sam',
      email: 'sam@example.com',
      password: 'Pass1!aa',
    });
    expect(authServiceSpy.setToken).toHaveBeenCalledWith('new-token');
    expect(router.navigate).toHaveBeenCalledWith(['/2fa-setup']);
    expect(component.error).toBe('');
  });

  it('shows API error message on failure', () => {
    authServiceSpy.register.and.returnValue(
      throwError(() => ({ error: { error: 'Account already exists' } }))
    );
    const event = new Event('submit');
    spyOn(event, 'preventDefault');

    component.password = 'Pass1!aa';
    component.validatePassword();
    component.register(event);

    expect(component.error).toBe('Account already exists');
  });

  it('fails if password rules are not met', () => {
    component.password = 'weak';
    component.validatePassword();

    expect(component.allRulesPass).toBeFalse();
    
    const event = new Event('submit');
    spyOn(event, 'preventDefault');
    component.register(event);

    expect(component.error).toBe('Please fix the password requirements above');
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('validates password correctly', () => {
    component.password = 'weak';
    component.validatePassword();
    expect(component.rules.minLength).toBeFalse();
    expect(component.rules.uppercase).toBeFalse();
    expect(component.rules.digit).toBeFalse();
    expect(component.rules.special).toBeFalse();

    component.password = 'Strong1!';
    component.validatePassword();
    expect(component.rules.minLength).toBeTrue();
    expect(component.rules.uppercase).toBeTrue();
    expect(component.rules.lowercase).toBeTrue();
    expect(component.rules.digit).toBeTrue();
    expect(component.rules.special).toBeTrue();
    expect(component.allRulesPass).toBeTrue();
  });
});
