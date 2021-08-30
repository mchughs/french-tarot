const colors = require('tailwindcss/colors')

module.exports = {
    purge: [
        './src/**/*.cljs', // probably doesn't work.
    ],
    darkMode: false, // or 'media' or 'class'
    theme: {
        extend: {
            inset: {
                '1/6': '17%',
            },
            colors: {
                transparent: 'transparent',
                current: 'currentColor',

                // red: colors.red,
                blue: colors.sky,
                yellow: "#ffc800",

                gray: colors.trueGray,
                // white: colors.white,
                // black: colors.black,

                primary: colors.emerald,
                secondary: colors.green,
                tertiary: '#ef6f6c',
                accent: colors.rose,

                background: '#eeffbe',
                typeface: colors.coolGray,
            },
        },
    },
    variants: {
        opacity: ({ after }) => after(['disabled']),
        backgroundColor: ({ after }) => after(['disabled']),
    },
    plugins: [
        require('@tailwindcss/forms'),
    ],
}
