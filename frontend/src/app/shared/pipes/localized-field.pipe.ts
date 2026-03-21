import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Pipe({
  name: 'localizedField',
  pure: false, // Impure so it updates when language changes
  standalone: false
})
export class LocalizedFieldPipe implements PipeTransform {

  constructor(private translate: TranslateService) {}

  transform(obj: any, fieldBaseName: string, fallbackField: string = 'nom'): string {
    if (!obj) return '';

    const lang = this.translate.currentLang || this.translate.defaultLang || 'en';
    
    let localizedFieldName = '';
    if (lang === 'en') localizedFieldName = fieldBaseName + 'En';
    else if (lang === 'fr') localizedFieldName = fieldBaseName + 'Fr';
    else if (lang === 'ar') localizedFieldName = fieldBaseName + 'Ar';
    else if (lang === 'es') localizedFieldName = fieldBaseName + 'Es';

    // If the localized field exists and is not null/empty, return it
    if (obj[localizedFieldName] && obj[localizedFieldName].trim() !== '') {
      return obj[localizedFieldName];
    }

    // Default fallback to 'nom' or whatever field is specified
    return obj[fallbackField] || '';
  }
}
