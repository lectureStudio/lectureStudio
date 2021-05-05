import {of as observableOf, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { TranslateLoader, TranslateService } from '@ngx-translate/core';
import { Locale } from '../model';


declare var require: any;

class LocaleFileLoader {

	private static _instance: LocaleFileLoader = new LocaleFileLoader();

	private readonly bundles: object;


	constructor() {
		if (LocaleFileLoader._instance) {
			throw new Error('Use getInstance() instead of new.');
		}
		LocaleFileLoader._instance = this;

		this.bundles = this.mapLocaleFiles(require.context('../../', true, /^.*\/i18n\/.*\.json$/));
	}

	public static getInstance(): LocaleFileLoader {
		return LocaleFileLoader._instance;
	}

	public getBundles(): object {
		return this.bundles;
	}

	private mapLocaleFiles(ctx) {
		const keys = ctx.keys();
		const values = keys.map(ctx);

		return keys.reduce((o, k, i) => {
			const name = k.substring(k.lastIndexOf('/') + 1, k.lastIndexOf('.'));
			o[name] = Object.assign(o[name] || {}, values[i]);

			return o;
		}, {});
	}
}

@Injectable({
	providedIn: 'root'
})
export class LocaleService {

	private static readonly defaultLocaleTag = 'en';

	private readonly locales: Locale[];

	private selectedLocale: Locale;


	constructor(public translateService: TranslateService) {
		const bundles = LocaleFileLoader.getInstance().getBundles();
		const browserLang = translateService.getBrowserCultureLang().replace('-', '_');

		if (!bundles) {
			this.selectedLocale = {
				name: '',
				tag: ''
			};

			return;
		}

		this.locales = Object.keys(bundles).map(key => {
			return bundles[key].context;
		});

		this.selectedLocale = this.locales.find((locale: Locale) => {
			return locale.tag.toLowerCase().startsWith(browserLang);
		});

		const defaultLocale = this.locales.find((locale: Locale) => {
			return locale.tag.startsWith(LocaleService.defaultLocaleTag);
		});

		if (!this.selectedLocale) {
			this.selectedLocale = this.locales.find((locale: Locale) => {
				return locale.tag.toLowerCase().startsWith(browserLang.slice(0, 2));
			});
		}

		translateService.addLangs(this.getLocaleTags());
		translateService.setDefaultLang(defaultLocale.tag);
		translateService.use(this.selectedLocale.tag);
	}

	getLocales(): Locale[] {
		return this.locales;
	}

	getSelectedLocale(): Locale {
		return this.selectedLocale;
	}

	getLocaleTags(): string[] {
		const localeTags: string[] = this.locales.map(locale => {
			return locale.tag;
		});

		return localeTags;
	}

	translate(key: string): string {
		return this.translateService.instant(key);
	}

	selectLocale(tag: string): void {
		this.translateService.use(tag);
	}

}

export class LocaleTranslateLoader implements TranslateLoader {

	private readonly bundles: object;


	constructor() {
		this.bundles = LocaleFileLoader.getInstance().getBundles();
	}

	getTranslation(lang: string): Observable<any> {
		return observableOf(this.bundles[lang]);
	}

}
