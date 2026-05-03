const express = require('express');
const app = express();
const port = process.env.PORT || 3000;
const color = process.env.DEPLOY_COLOR || 'unknown';

app.get('/', (req, res) => {
  res.json({ message: 'Blue-Green Deployment App', activeColor: color });
});

app.get('/health', (req, res) => {
  res.status(200).send('OK');
});

app.listen(port, () => {
  console.log(`Blue-Green App [${color}] running on port ${port}`);
});
