import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MovieService } from '../../services/movie.service';
import { WatchlistService } from '../../services/watchlist.service';

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
                placeholder="Search by name"
                class="search-input"
                formControlName="q"
                autocomplete="off"
            >
        </form>

        @if (users().length === 1) {
            <h2>Vous êtes tout seul dans ce monde</h2>
        } @else {
            <ul class="status" aria-label="Users list">
            @for (u of users; track u.id) {
                <li
                    class="users__item"
                    role="link"
                    tabindex="0"
                >
                    <div class="users__username">
                        

                    </div>
                </li>
            }
        
    </section>
    `
})
export class FriendsComponent implements OnInit {
    readonly filters = new FormGroup({
        q: new FormControl<string>(''),
    });

    constructor(private readonly router: Router) { }

    ngOnInit(): void {
    }

}