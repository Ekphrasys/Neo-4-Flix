import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { of, throwError } from 'rxjs';
import { CommonModule } from '@angular/common';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj<AuthService>('AuthService', ['login', 'setToken', 'verify2FA']);
    mockRouter = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule, CommonModule],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => 1 } } } },
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should mark form as invalid when empty', () => {
    component.onSubmit();
    expect(component.loginForm.valid).toBe(false);
    expect(mockAuthService.login).not.toHaveBeenCalled();
  });

  it('should call authService.login and navigate on success', () => {
    component.loginForm.controls['email'].setValue('test@example.com');
    component.loginForm.controls['password'].setValue('password123');
    mockAuthService.login.and.returnValue(of({ token: 'fake-jwt-token' }));

    component.onSubmit();

    expect(mockAuthService.login).toHaveBeenCalledWith({ email: 'test@example.com', password: 'password123' });
    expect(mockAuthService.setToken).toHaveBeenCalledWith('fake-jwt-token');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should set error message on login failure', () => {
    component.loginForm.controls['email'].setValue('wrong@example.com');
    component.loginForm.controls['password'].setValue('wrongpass');
    mockAuthService.login.and.returnValue(
      throwError(() => ({ error: { error: 'Invalid credentials' } }))
    );

    component.onSubmit();

    expect(component.error()).toBe('Invalid credentials');
    expect(component.loading()).toBe(false);
  });

  describe('2FA processing', () => {
    it('should set tempToken and requires2FA when login returns 2FA response', () => {
      component.loginForm.controls['email'].setValue('test@example.com');
      component.loginForm.controls['password'].setValue('password123');
      mockAuthService.login.and.returnValue(of({ requires2FA: 'true', tempToken: 'temp-123' }));

      component.onSubmit();

      expect(component.requires2FA()).toBeTrue();
      expect((component as any).tempToken).toBe('temp-123');
      expect(mockAuthService.setToken).not.toHaveBeenCalled();
    });

    it('should not call verify2FA if totpForm is invalid', () => {
      component.requires2FA.set(true);
      (component as any).tempToken = 'temp-123';
      component.totpForm.controls['code'].setValue('12'); // invalid length

      component.onVerify2FA();

      expect(mockAuthService.verify2FA).not.toHaveBeenCalled();
    });

    it('should verify 2FA and navigate on success', () => {
      component.requires2FA.set(true);
      (component as any).tempToken = 'temp-123';
      component.totpForm.controls['code'].setValue('123456');
      
      mockAuthService.verify2FA.and.returnValue(of({ token: 'real-jwt-token' }));

      component.onVerify2FA();

      expect(mockAuthService.verify2FA).toHaveBeenCalledWith('temp-123', '123456');
      expect(mockAuthService.setToken).toHaveBeenCalledWith('real-jwt-token');
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should set error on verify 2FA failure', () => {
      component.requires2FA.set(true);
      (component as any).tempToken = 'temp-123';
      component.totpForm.controls['code'].setValue('123456');
      
      mockAuthService.verify2FA.and.returnValue(
        throwError(() => ({ error: { error: 'Invalid code' } }))
      );

      component.onVerify2FA();

      expect(component.error()).toBe('Invalid code');
      expect(component.loading()).toBeFalse();
    });

    it('should reset state on back()', () => {
      component.requires2FA.set(true);
      component.error.set('some error');
      (component as any).tempToken = 'temp-123';

      const event = new Event('click');
      spyOn(event, 'preventDefault');

      component.back(event);

      expect(event.preventDefault).toHaveBeenCalled();
      expect(component.requires2FA()).toBeFalse();
      expect(component.error()).toBeNull();
      expect((component as any).tempToken).toBe('');
    });
  });
});
