const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');

const app = express();
const port = 3000;

// Middleware
app.use(cors());
app.use(express.json());

// In-memory user storage (for demo purposes)
const users = [
    {
        email: 'test@example.com',
        password: 'password123'
    }
];

app.post('/api/login', async (req, res) => { console.log(req.body)
    const { email, password } = req.body;

    try {
        // Find user
        const user = users.find(u => u.email === email);

        if (!user) {
            return res.status(401).json({ message: 'Invalid user credentials' });
        }

        // Check password
        const isValidPassword = password === user.password;
		console.log(isValidPassword)
        if (!isValidPassword) {
            return res.status(401).json({ message: 'Invalid pwd credentials' });
        }

        // Success
        res.json({ message: 'Login successful' });
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ message: 'Server error' });
    }
});

app.listen(port, '0.0.0.0', () => {
    console.log(`Server running at http://localhost:${port}`);
}); 