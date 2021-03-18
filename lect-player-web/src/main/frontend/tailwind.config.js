module.exports = {
	purge: {
		enabled: true,
		content: [
			'./src/**/*.html',
			'./src/**/*.js',
			'./src/**/*.ts',
		],
		options: {
			keyframes: true,
		},
	},
	darkMode: false, // or 'media' or 'class'
	theme: {
		extend: {},
	},
	variants: {
		extend: {},
	},
	plugins: [],
}
