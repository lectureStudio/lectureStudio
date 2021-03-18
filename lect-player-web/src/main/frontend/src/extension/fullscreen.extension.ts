const specKeys = {
	fullscreenEnabled: 0,
	fullscreenElement: 1,
	requestFullscreen: 2,
	exitFullscreen: 3,
	fullscreenchange: 4,
	fullscreenerror: 5,
};

const webkit = [
	'webkitFullscreenEnabled',
	'webkitFullscreenElement',
	'webkitRequestFullscreen',
	'webkitExitFullscreen',
	'webkitfullscreenchange',
	'webkitfullscreenerror',
];

const moz = [
	'mozFullScreenEnabled',
	'mozFullScreenElement',
	'mozRequestFullScreen',
	'mozCancelFullScreen',
	'mozfullscreenchange',
	'mozfullscreenerror',
];

const ms = [
	'msFullscreenEnabled',
	'msFullscreenElement',
	'msRequestFullscreen',
	'msExitFullscreen',
	'MSFullscreenChange',
	'MSFullscreenError',
];

const spec = Object.keys(specKeys);
let vendor: string[] = null;

for (let v of [spec, webkit, moz, ms]) {
	if (v[specKeys.fullscreenEnabled] in document) {
		vendor = v;
		break;
	}
}

if (vendor) {
	let proto: any = Element.prototype;
	proto.requestFullscreen = proto[vendor[specKeys.requestFullscreen]];

	proto = Document.prototype;
	proto.exitFullscreen = proto[vendor[specKeys.exitFullscreen]];

	const fullscreenElement = spec[specKeys.fullscreenElement];
	const fullscreenEnabled = spec[specKeys.fullscreenEnabled];

	if (!(fullscreenElement in document)) {
		Object.defineProperty(document, fullscreenElement, {
			get: function () {
				return (document as any)[vendor[specKeys.fullscreenElement]];
			}
		});
		Object.defineProperty(document, fullscreenEnabled, {
			get: function () {
				return (document as any)[vendor[specKeys.fullscreenEnabled]];
			}
		});
	}

	if (spec !== vendor) {
		const proxyListener = (event: Event) => {
			let actionType = event.type.replace(/^(webkit|moz|MS)/, '').toLowerCase();
			let newEvent;

			if (typeof (Event) === 'function') {
				newEvent = new Event(actionType, event);
			}
			else {
				newEvent = document.createEvent('Event');
				newEvent.initEvent(actionType, event.bubbles, event.cancelable);
			}

			event.target.dispatchEvent(newEvent);
		};

		document.addEventListener(vendor[specKeys.fullscreenchange], proxyListener);
		document.addEventListener(vendor[specKeys.fullscreenerror], proxyListener);
	}
}

export { }