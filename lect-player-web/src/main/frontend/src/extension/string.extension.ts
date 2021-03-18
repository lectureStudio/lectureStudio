declare global {

	interface StringConstructor {
		format(str: string, ...arr: any[]): string;
	}

}

String.format = function (str: string, ...arr: any[]) {
	var i = -1;

	function callback(exp: any, p0: any, p1: any, p2: any, p3: any, p4: any) {
		if (exp == '%%') return '%';
		if (arr[++i] === undefined) return undefined;

		exp = p2 ? parseInt(p2.substr(1)) : undefined;

		var base = p3 ? parseInt(p3.substr(1)) : undefined;
		var val;

		switch (p4) {
			case 's': val = arr[i]; break;
			case 'c': val = arr[i][0]; break;
			case 'f': val = parseFloat(arr[i]).toFixed(exp); break;
			case 'p': val = parseFloat(arr[i]).toPrecision(exp); break;
			case 'e': val = parseFloat(arr[i]).toExponential(exp); break;
			case 'x': val = parseInt(arr[i]).toString(base ? base : 16); break;
			case 'd': val = parseFloat(parseInt(arr[i], base ? base : 10).toPrecision(exp)).toFixed(0); break;
		}

		val = typeof (val) == 'object' ? JSON.stringify(val) : val.toString(base);
		var sz = parseInt(p1); /* padding size */
		var ch = p1 && p1[0] == '0' ? '0' : ' '; /* isnull? */

		while (val.length < sz) val = p0 !== undefined ? val + ch : ch + val; /* isminus? */

		return val;
	}

	var regex = /%(-)?(0?[0-9]+)?([.][0-9]+)?([#][0-9]+)?([scfpexd%])/g;

	return str.replace(regex, callback);
}

export { }