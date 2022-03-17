const fs = require('fs');

// For stylesDict from Styles.java and ReferenceClasses.java
const RGX_KEY = /(?<= +public +static +final +String +)([0-9A-Z_]+)/g;
const RGX_VAL = /(?<= +public +static +final +String +[0-9A-Z_]+ += +")([a-z0-9-/]+)/g;

//const STYLE_RGX = /(?<=(Styles|ReferenceClasses)\.[0-9A-Z_]+)(/g
const STYLE_RGX = /(?<=(Styles|ReferenceClasses)\.)([0-9A-Z_]+)/g

const STR_LITERAL = /["'][\.a-z0-9/:-]+["']/g

const PREFIXES = [ 'even', 'focus', 'focus-within', 'hover', 'disabled', 'sm', 'md', 'lg', 'xl', '2xl', ]

// Used to read Styles.java and ReferenceClasses.java to get dictionary mapping 
// for all possible base styles (no prefixes)
function addStyleDictMatches(matches, file_contents) {
  let count = 1
  for (const line of file_contents) {
    let match_key = line.match(RGX_KEY);
    let match_val = line.match(RGX_VAL);

    // Both 'variable' and tailwind str are probably on same line
    // Even though java probably doesn't require it
    if (Array.isArray(match_key) && Array.isArray(match_val)) {
      if (match_key.length === 1 && match_val.length === 1) {
        matches[match_key[0]] = match_val[0];
      } else {
        throw "strange line in 'Styles.java' at line " + count.toString();
      }
    }

    count++;
  }
}

function getStylesDict() {
  const matches = {};
  let folder = './app/views/style/'
  try {
    let specialFiles = ['Styles.java', 'ReferenceClasses.java'];
    for (file of specialFiles) {
      let data = fs.readFileSync(folder+file, 'utf8').split('\n');
      addStyleDictMatches(matches, data);
    }
  }
  catch (error) {
    throw 'error reading Styles.java for tailwindcss processing: ' + error.message;
  }

  return matches;
}

const styleDict = getStylesDict();


module.exports = {
  purge: {
    enabled: true,
    content: [
      './app/views/**/*.java',
      './app/assets/javascripts/*.ts',
    ],
    extract: {
      java: (content) => {
        return content
      },
    },
    transform: {
      java: (code) => {
        const output = [];

        let matchIter = code.match(STYLE_RGX)

        if (matchIter) {
          for (const tailwindClassId of matchIter) {
            let tailwindClass = styleDict[tailwindClassId]
            
            if (tailwindClass !== undefined) {
              output.push(tailwindClass)
              // We don't know which, if any, of these prefixes are in use for any class in particular.
              // We therefore have to use every combination of them.
              for (const prefix of PREFIXES) {
                output.push(prefix + ':' + tailwindClass)
              }
            }
          }
        }

        return output
      },
    },
  },

  darkMode: false, // or 'media' or 'class'
  theme: {
    extend: {
      colors: {
        'civiform-white': {
          DEFAULT: '#f8f9fa',
        },
        'seattle-blue': {
          DEFAULT: '#113f9f',
        },
      },
    },
  },
  variants: {
    extend: {
      backgroundColor: ['disabled', 'odd'],
      textColor: ['disabled'],
      opacity: ['disabled'],
    },
  },
  plugins: [require('@tailwindcss/line-clamp')],
}
