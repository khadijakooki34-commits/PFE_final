import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
    standalone: false,
    selector: 'app-event-list',
    templateUrl: './event-list.component.html',
    styleUrls: ['./event-list.component.css']
})
export class EventListComponent implements OnInit {
    events: any[] = [];
    loading = true;
    searchTerm: string = '';
    locationTerm: string = '';
    selectedCategory: string = ''; // Initialized to empty string, will be set by loadCategories
    categories: string[] = []; // Changed to empty array

    filteredEvents: any[] = []; // Explicit array instead of getter

    constructor(
        private apiService: ApiService,
        private translate: TranslateService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.loadCategories(); // Load categories first
        this.loadEvents(); // Then load events
    }

    private loadCategories(): void {
        this.categories = [
            this.translate.instant('EVENTS.ALL_EVENTS'),
            this.translate.instant('EVENTS.FESTIVALS'),
            this.translate.instant('EVENTS.CULTURAL'),
            this.translate.instant('EVENTS.MUSIC'),
            this.translate.instant('EVENTS.TRADITIONAL')
        ];
        this.selectedCategory = this.categories[0]; // Set default selected category after loading
    }

    private loadEvents(): void {
        this.loading = true;
        this.apiService.getEvents().subscribe({
            next: (data) => {
                // Parse date arrays from backend so the Angular date pipe doesn't crash
                this.events = data.map((evt: any) => ({
                    ...evt,
                    dateDebut: this.parseDate(evt.dateDebut),
                    dateFin: this.parseDate(evt.dateFin)
                }));
                this.applyFilters(); // Initial filter application
                this.loading = false;
                // Removed cdr.detectChanges()
            },
            error: (e) => {
                console.error(e);
                this.loading = false;
                // Removed cdr.detectChanges()
            }
        });
    }

    private parseDate(dateStr: any): Date | null {
        if (!dateStr) return null;
        if (Array.isArray(dateStr)) {
            const [y, m, d, h = 0, min = 0, s = 0] = dateStr;
            return new Date(y, m - 1, d, h, min, s);
        }
        const parsed = new Date(dateStr);
        if (isNaN(parsed.getTime()) && typeof dateStr === 'string') {
            return new Date(dateStr.replace('T', ' ').replace(/\.\d+/, ''));
        }
        return parsed;
    }

    applyFilters() {
        const search = (this.searchTerm || '').trim().toLowerCase();
        const location = (this.locationTerm || '').trim().toLowerCase();
        const selectedCat = this.selectedCategory;

        // Find the matching internal type by checking which category index was selected
        // categories = [All, Festivals, Cultural, Music, Traditional]
        const categoryIndex = this.categories.indexOf(selectedCat);
        const internalTypes = [null, 'festival', 'cultural', 'music', 'traditional'];
        const targetType = internalTypes[categoryIndex] || null;

        this.filteredEvents = this.events.filter(event => {
            const matchesSearch = !search || 
                (event.nom && event.nom.toLowerCase().includes(search)) ||
                (event.description && event.description.toLowerCase().includes(search));

            const matchesLocation = !location || 
                (event.lieu && event.lieu.toLowerCase().includes(location));

            let matchesCategory = true;
            if (targetType) {
                const eventType = (event.eventType || '').toLowerCase();
                matchesCategory = eventType === targetType;
            }

            return matchesSearch && matchesLocation && matchesCategory;
        });

        this.cdr.detectChanges();
    }

    filterCategory(category: string) {
        this.selectedCategory = category;
        this.applyFilters();
    }

    // Search trigger
    onSearch() {
        this.applyFilters();
    }

    resetFilters() {
        this.searchTerm = '';
        this.locationTerm = '';
        this.selectedCategory = this.categories[0];
        this.applyFilters();
    }
}
