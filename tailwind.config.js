module.exports = {
    purge: [
        './src/**/*.cljs', // probably doesn't work.
    ],
    darkMode: false, // or 'media' or 'class'
    theme: {
        extend: {},
    },
    variants: {
        opacity: ({ after }) => after(['disabled']),
        backgroundColor: ({ after }) => after(['disabled']),
    },
    plugins: [
        require('@tailwindcss/forms'),
    ],
}
