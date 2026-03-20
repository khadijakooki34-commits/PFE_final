import { Component, Input } from '@angular/core';

@Component({
    standalone: false,
    selector: 'app-destination-card',
    templateUrl: './destination-card.component.html',
    styleUrls: ['./destination-card.component.css']
})
export class DestinationCardComponent {
    @Input() destination: any;

    handleImageError(event: any) {
        event.target.src = 'assets/placeholder.jpg';
    }
}
