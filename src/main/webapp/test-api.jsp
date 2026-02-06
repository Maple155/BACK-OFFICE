<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Test API JSON</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            min-height: 100vh;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
        }

        h1 {
            color: white;
            text-align: center;
            margin-bottom: 30px;
            font-size: 2.5em;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }

        .test-section {
            background: white;
            border-radius: 15px;
            padding: 25px;
            margin-bottom: 20px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.2);
        }

        .test-section h2 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 1.5em;
            border-bottom: 2px solid #667eea;
            padding-bottom: 10px;
        }

        .endpoint {
            background: #f8f9fa;
            padding: 15px;
            margin: 10px 0;
            border-radius: 8px;
            border-left: 4px solid #667eea;
        }

        .endpoint-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }

        .method {
            display: inline-block;
            padding: 5px 15px;
            border-radius: 5px;
            font-weight: bold;
            color: white;
            font-size: 0.9em;
        }

        .method.get {
            background: #28a745;
        }

        .method.post {
            background: #007bff;
        }

        .url {
            font-family: 'Courier New', monospace;
            color: #333;
            font-weight: bold;
        }

        .test-btn {
            background: #667eea;
            color: white;
            border: none;
            padding: 10px 25px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1em;
            transition: all 0.3s;
        }

        .test-btn:hover {
            background: #5568d3;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }

        .test-btn:active {
            transform: translateY(0);
        }

        .result {
            margin-top: 15px;
            padding: 15px;
            background: #1e1e1e;
            border-radius: 8px;
            overflow-x: auto;
        }

        .result pre {
            color: #a9b7c6;
            font-family: 'Courier New', monospace;
            font-size: 0.9em;
            margin: 0;
            white-space: pre-wrap;
            word-wrap: break-word;
        }

        .form-section {
            background: #f0f0f0;
            padding: 15px;
            border-radius: 8px;
            margin-top: 10px;
        }

        .form-group {
            margin-bottom: 10px;
        }

        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #333;
        }

        .form-group input {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 0.95em;
        }

        .loading {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 3px solid #f3f3f3;
            border-top: 3px solid #667eea;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin-left: 10px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .status {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 5px;
            font-weight: bold;
            margin-left: 10px;
        }

        .status.success {
            background: #d4edda;
            color: #155724;
        }

        .status.error {
            background: #f8d7da;
            color: #721c24;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 Test API JSON</h1>

        <!-- Tests GET simples -->
        <div class="test-section">
            <h2>📥 Tests GET - Objets simples</h2>

            <div class="endpoint">
                <div class="endpoint-header">
                    <div>
                        <span class="method get">GET</span>
                        <span class="url">/api/user</span>
                    </div>
                    <button class="test-btn" onclick="testEndpoint('/framework/api/user', 'GET', 'result1')">
                        Tester
                    </button>
                </div>
                <p>Retourne un objet User en JSON</p>
                <div id="result1" class="result" style="display:none;">
                    <pre></pre>
                </div>
            </div>

            <div class="endpoint">
                <div class="endpoint-header">
                    <div>
                        <span class="method get">GET</span>
                        <span class="url">/api/personne</span>
                    </div>
                    <button class="test-btn" onclick="testEndpoint('/framework/api/personne', 'GET', 'result2')">
                        Tester
                    </button>
                </div>
                <p>Retourne un objet Personne en JSON</p>
                <div id="result2" class="result" style="display:none;">
                    <pre></pre>
                </div>
            </div>

            <div class="endpoint">
                <div class="endpoint-header">
                    <div>
                        <span class="method get">GET</span>
                        <span class="url">/api/message</span>
                    </div>
                    <button class="test-btn" onclick="testEndpoint('/framework/api/message', 'GET', 'result3')">
                        Tester
                    </button>
                </div>
                <p>Retourne un String en JSON</p>
                <div id="result3" class="result" style="display:none;">
                    <pre></pre>
                </div>
            </div>

            <div class="endpoint">
                <div class="endpoint-header">
                    <div>
                        <span class="method get">GET</span>
                        <span class="url">/api/count</span>
                    </div>
                    <button class="test-btn" onclick="testEndpoint('/framework/api/count', 'GET', 'result4')">
                        Tester
                    </button>
                </div>
                <p>Retourne un nombre en JSON</p>
                <div id="result4" class="result" style="display:none;">
                    <pre></pre>
                </div>
            </div>
        </div>

        <!-- Tests avec ModelView -->
        <div class="test-section">
            <h2>📦 Test ModelView → JSON</h2>

            <div class="endpoint">
                <div class="endpoint-header">
                    <div>
                        <span class="method get">GET</span>
                        <span class="url">/api/data</span>
                    </div>
                    <button class="test-btn" onclick="testEndpoint('/framework/api/data', 'GET', 'result5')">
                        Tester
                    </button>
                </div>
                <p>Retourne le data d'un ModelView (la vue est ignorée)</p>
                <div id="result5" class="result" style="display:none;">
                    <pre></pre>
                </div>
            </div>
        </div>

        <!-- Tests avec listes -->
        <div class="test-section">
            <h2>📋 Test Collections</h2>

            <div class="endpoint">
                <div class="endpoint-header">
                    <div>
                        <span class="method get">GET</span>
                        <span class="url">/api/users</span>
                    </div>
                    <button class="test-btn" onclick="testEndpoint('/framework/api/users', 'GET', 'result6')">
                        Tester
                    </button>
                </div>
                <p>Retourne une liste d'objets User en JSON</p>
                <div id="result6" class="result" style="display:none;">
                    <pre></pre>
                </div>
            </div>
        </div>

        <!-- Test POST -->
        <div class="test-section">
            <h2>📤 Test POST avec données</h2>

            <div class="endpoint">
                <div class="endpoint-header">
                    <div>
                        <span class="method post">POST</span>
                        <span class="url">/api/user/create</span>
                    </div>
                    <button class="test-btn" onclick="testPostUser()">
                        Tester
                    </button>
                </div>
                <p>Crée un User et le retourne en JSON</p>
                
                <div class="form-section">
                    <div class="form-group">
                        <label>Nom:</label>
                        <input type="text" id="post_name" value="Dupont">
                    </div>
                    <div class="form-group">
                        <label>Prénom:</label>
                        <input type="text" id="post_prenom" value="Jean">
                    </div>
                    <div class="form-group">
                        <label>Âge:</label>
                        <input type="number" id="post_age" value="35">
                    </div>
                    <div class="form-group">
                        <label>Date de naissance (YYYY-MM-DD):</label>
                        <input type="text" id="post_date" value="1988-03-15">
                    </div>
                </div>

                <div id="result7" class="result" style="display:none;">
                    <pre></pre>
                </div>
            </div>
        </div>
    </div>

    <script>
        // Fonction pour tester un endpoint GET
        async function testEndpoint(url, method, resultId) {
            const resultDiv = document.getElementById(resultId);
            const resultPre = resultDiv.querySelector('pre');
            
            resultDiv.style.display = 'block';
            resultPre.textContent = 'Chargement...';

            try {
                const response = await fetch(url, {
                    method: method,
                    headers: {
                        'Accept': 'application/json'
                    }
                });

                const contentType = response.headers.get('content-type');
                let data;

                if (contentType && contentType.includes('application/json')) {
                    data = await response.json();
                } else {
                    data = await response.text();
                }

                // Formater le JSON pour l'affichage
                const formatted = typeof data === 'object' 
                    ? JSON.stringify(data, null, 2)
                    : data;

                resultPre.textContent = `Status: ${response.status} ${response.statusText}\n\n${formatted}`;
            } catch (error) {
                resultPre.textContent = `Erreur: ${error.message}`;
            }
        }

        // Fonction pour tester le POST
        async function testPostUser() {
            const resultDiv = document.getElementById('result7');
            const resultPre = resultDiv.querySelector('pre');
            
            resultDiv.style.display = 'block';
            resultPre.textContent = 'Envoi des données...';

            const formData = new URLSearchParams();
            formData.append('user.name', document.getElementById('post_name').value);
            formData.append('user.prenom', document.getElementById('post_prenom').value);
            formData.append('user.age', document.getElementById('post_age').value);
            formData.append('user.dateNaissance', document.getElementById('post_date').value);

            try {
                const response = await fetch('/framework/api/user/create', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'Accept': 'application/json'
                    },
                    body: formData.toString()
                });

                const contentType = response.headers.get('content-type');
                let data;

                if (contentType && contentType.includes('application/json')) {
                    data = await response.json();
                } else {
                    data = await response.text();
                }

                const formatted = typeof data === 'object' 
                    ? JSON.stringify(data, null, 2)
                    : data;

                resultPre.textContent = `Status: ${response.status} ${response.statusText}\n\n${formatted}`;
            } catch (error) {
                resultPre.textContent = `Erreur: ${error.message}`;
            }
        }
    </script>
</body>
</html>