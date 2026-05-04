import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { Router } from '@angular/router';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;
  const baseUrl = 'http://localhost:8081/api/auth';

  const createToken = (payload: object): string => {
    const encodedPayload = btoa(JSON.stringify(payload));
    return `header.${encodedPayload}.signature`;
  };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);
    localStorage.clear();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService, { provide: Router, useValue: routerSpy }],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('posts register payload', () => {
    const body = { name: 'sam', email: 'sam@example.com', password: '123456' };

    service.register(body).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ token: 'abc' });
  });

  it('posts login payload', () => {
    const body = { email: 'sam@example.com', password: '123456' };

    service.login(body).subscribe();

    const req = httpMock.expectOne(`${baseUrl}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({ token: 'abc' });
  });

  it('stores token and toggles login state', (done) => {
    const token = createToken({ id: 'u-1', role: 'SELLER' });

    service.setToken(token);

    expect(service.getToken()).toBe(token);
    service.isLoggedIn$.subscribe((isLoggedIn) => {
      expect(isLoggedIn).toBeTrue();
      done();
    });
  });

  it('parses current user data from token payload', () => {
    const token = createToken({ id: 'u-99', role: 'SELLER' });
    localStorage.setItem('token', token);

    expect(service.getUserData()).toEqual({ id: 'u-99', role: 'SELLER' });
    expect(service.getUserId()).toBe('u-99');
  });

  it('logs out and redirects to login', () => {
    localStorage.setItem('token', createToken({ id: 'u-1' }));

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });
});
