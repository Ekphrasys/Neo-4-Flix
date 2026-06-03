import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-friends',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    styleUrl: './friends.component.css',
    template: `
    <section class="page">
        <h1>Add Friends</h1>

        <form class="search" [formGroup]="filters" (submit)="$event.preventDefault()">
            <input
                type="text"
                placeholder="Search by name..."
                class="search-input"
                formControlName="q"
                autocomplete="off"
            >
        </form>

        @if (loading()) {
            <p class="status">Chargement...</p>
        } @else if (error()) {
            <p class="status status--error">{{ error() }}</p>
        } @else if (users().length === 0) {
            <h2 class="status">Vous êtes tout seul dans ce monde</h2>
        } @else {
            <ul class="users-list" aria-label="Users list">
            @for (u of users(); track u.id) {
                <li class="users__item" tabindex="0">
                    <div class="users__username">
                        {{ u.username }}
                    </div>
                    <div class="users__email">
                        {{ u.email }}
                    </div>
                </li>
            }
            </ul>
        }
    </section>
    `
})
export class FriendsComponent implements OnInit {
    users = signal<any[]>([]);
    loading = signal<boolean>(true);
    error = signal<string | null>(null);

    readonly filters = new FormGroup({
        q: new FormControl<string>(''),
    });

    private readonly destroyRef = inject(DestroyRef);

    constructor(
        private readonly router: Router,
        private readonly authService: AuthService
    ) { }

    ngOnInit(): void {
        this.filters.valueChanges
            .pipe(
                debounceTime(300),
                map(() => this.filters.getRawValue()),
                distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
                takeUntilDestroyed(this.destroyRef)
            )
            .subscribe(() => this.applyFilters());

        this.applyFilters();
    }

    private applyFilters(): void {
        const query = this.filters.getRawValue().q?.trim() ?? '';
        this.loading.set(true);
        this.error.set(null);

        this.authService.searchUsers(query).subscribe({
            next: (data) => {
                const currentUserId = this.authService.getUserId();
                // Filter out the current user themselves
                this.users.set((data ?? []).filter(u => u.id !== currentUserId));
                this.loading.set(false);
            },
            error: (err) => {
                console.error(err);
                this.error.set('Impossible de charger les utilisateurs.');
                this.loading.set(false);
            }
        });
    }
}