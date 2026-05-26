import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MovieService } from '../../services/movie.service';
import { WatchlistService } from '../../services/watchlist.service';

@Component({
    selector: 'app-create-movie',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    template: `
    <section class="page">
        <div class="create_card">
            <header class="create_title">
                <h1 class="create_title">Create Movie</h1>
            </header>

            <form class="form" [formGroup]="movieForm" (ngSubmit)="createMovie()" novalidate>
                <div class="field">
                    <label class="field__label" for="title">Title</label>
                    <input
                        class="field__control"
                        id="title"
                        type="text"
                        autocomplete="title"
                        required
                        formControlName="title"
                        placeholder="Title..."
                    />
                </div>

                <div class="field">
                    <label class="field__label" for="releaseYear">Release Year</label>
                    <input
                        class="field__control"
                        id="releaseYear"
                        type="number"
                        autocomplete="releaseYear"
                        required
                        formControlName="releaseYear"
                        placeholder="Release Year..."
                    />
                </div>

                <div class="field">
                    <label class="field__label" for="genre">Genre</label>
                    <select class="field__control" formControlName="genre">
				        @for (g of genres; track g) {
				            <option [value]="g">{{ g }}</option>
				        }
			        </select>
                </div>

                <div class="field">
                    <label class="field__label" for="description">Description</label>
                    <textarea
                        class="field__control"
                        id="description"
                        autocomplete="description"
                        required
                        formControlName="description"
                        placeholder="Description..."
                    ></textarea>
                </div>
                
                    <button type="submit" class="field__label">
                        Register Movie
                    </button>
            </form>
        </div>
    </section>
    `,
    styleUrl: './create-movie.component.css',
})

export class MovieCreateComponent implements OnInit {

    movieForm!: FormGroup;
    movie = signal<any | null>(null);
    loading = signal<boolean>(false);
    error = signal<string | null>(null);

    readonly genres: string[] = [
        'ACTION',
        'HORROR',
        'SCI_FI',
        'ROMANCE',
        'FANTASY',
        'ANIMATION',
        'DOCUMENTARY',
    ];

    constructor(
        private readonly formBuilder: FormBuilder,
        private readonly movieService: MovieService,
        private readonly watchlistService: WatchlistService,
        private readonly route: ActivatedRoute,
        private readonly router: Router
    ) { }

    ngOnInit(): void {
        this.movieForm = this.formBuilder.group({
            title: ['', Validators.required],
            releaseYear: ['', Validators.required],
            genre: ['', Validators.required],
            description: ['']
        });
    }

    createMovie(): void {
        const { title, releaseYear, genre, description } = this.movieForm.value;

        if (this.movieForm.invalid) return;

        this.loading.set(true);
        this.error.set(null);
        this.movie.set(null);


        this.movieService.createMovie({ title, releaseYear, genre, description }).subscribe({
            next: (createdMovie) => {
                this.loading.set(false);
                this.movie.set(createdMovie);
                this.router.navigate(['/movies']);
            },
            error: (err) => {
                this.loading.set(false);
                this.error.set("Impossible d'enregistrer le film. Veuillez réessayer.");
                console.error("Erreur lors de la création :", err);
            }
        });

    }


}