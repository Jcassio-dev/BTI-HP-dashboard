const fs = require('fs');
const path = require('path');

const targetPath = path.resolve(__dirname, '../src/environments/environment.prod.ts');

const apiUrl = process.env.NG_APP_API_URL;

const envConfigFile = `export const environment = {
  production: true,
  apiUrl: '${apiUrl}'
};
`;

console.log('🔧 building environment.prod.ts with API_URL:', apiUrl);

fs.writeFile(targetPath, envConfigFile, (err) => {
    if (err) {
        console.error('❌ Error creating environment.prod.ts:', err);
        throw err;
    }
    console.log('✅ environment.prod.ts created successfully!');
});
