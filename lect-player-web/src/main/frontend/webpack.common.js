const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');

module.exports = {
	entry: {
		'main': './src/index.ts',
		'pdf.worker': 'pdfjs-dist/build/pdf.worker.entry',
	},
	devServer: {
		contentBase: './dist'
	},
	plugins: [
		new CleanWebpackPlugin(['dist']),
		new HtmlWebpackPlugin({
			template: path.resolve(__dirname, "./src/index.html"),
			minify: false
		})
	],
	output: {
		path: path.resolve(__dirname, '../resources/META-INF/resources'),
		filename: '[name].bundle.js'
	},
	resolve: {
		extensions: ['.ts', '.js']
	},
	module: {
		rules: [
			// Process component templates
			{
				test: /\.html$/,
				type: 'asset/source',
				exclude: [
					path.resolve(__dirname, './src/index.html')
				]
			},
			{
				test: /\.(s?)css$/,
				type: 'asset/source',
				exclude: [
					path.resolve(__dirname, './src/style.css'),
					path.resolve(__dirname, './src/icons.css'),
					path.resolve(__dirname, './src/tooltip.css')
				]
			},
			{
				test: /\.(woff|woff2|eot|ttf|otf)$/i,
				type: 'asset/inline',
			},
			// Process the global stylesheet
			{
				test: /\.(s?)css$/,
				use: [
					'style-loader',
					'css-loader',
					'postcss-loader',
					'sass-loader'
				],
				exclude: [
					/node_modules/,
					path.resolve(__dirname, "src/view"),
					path.resolve(__dirname, "src/component")
				],
			},
			{
				test: /\.worker\.ts$/,
				use: {
					loader: 'worker-loader',
					options: { inline: true }
				},
			},
			{
				test: /\.(ts|js)x?$/,
				exclude: /node_modules/,
				use: [
					'babel-loader',
					'angular2-template-loader',
				]
			}
		]
	}
};