module.exports = {
  mode: 'production',
  devtool: 'source-map',
  stats: 'errors-only',
  module: {
    rules: [
      {
        test: /\.scss$/, 
        use:           
          { 
            loader: "sass-loader",
            options: {
              sassOptions: {
                includePaths: [
                  "./node_modules/@uswds/uswds/packages"
                ],
              },
            }, 
          }
      },
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules\/(?!(@uswds)\/).*/,
      },
    ],
  },
  resolve: {
    extensions: ['.ts', '.js'],
  },
  entry: {
    applicant: './app/assets/javascripts/applicant_entry_point.ts',
    admin: './app/assets/javascripts/admin_entry_point.ts',
    uswds: [
      '/node_modules/@uswds/uswds/dist/js/uswds.min.js', 
      './app/assets/sass/uswds/styles.scss'
    ],

  },
  output: {
    filename: `[name].bundle.js`,
    sourceMapFilename: '[name].bundle.js.map',
    iife: true,
  },
}
