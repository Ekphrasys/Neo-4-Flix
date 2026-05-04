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
    mockAuthService = jasmine.createSpyObj<AuthService>('AuthService', ['login', 'setToken']);
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
});
