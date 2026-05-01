const express = require('express');
const helmet = require('helmet');
const cors = require('cors');

const app = express();
const port = process.env.PORT || 3000;

app.use(helmet());
app.use(cors());
app.use(express.json());

app.get('/', (req, res) => {
  res.send('Healthcare System DevSecOps API');
});

app.get('/api/patient/:id', (req, res) => {
  const patientId = req.params.id;
  if (!patientId.match(/^[0-9]+$/)) {
    return res.status(400).json({ error: 'Invalid patient ID format' });
  }
  res.json({ id: patientId, status: 'secure_record_retrieved' });
});

app.get('/health', (req, res) => {
  res.status(200).send('OK');
});

app.listen(port, () => {
  console.log(`Healthcare App running on port ${port}`);
});
