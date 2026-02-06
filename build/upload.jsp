<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Upload de fichiers</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
        }
        .form-group {
            margin-bottom: 15px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        input[type="file"],
        input[type="text"],
        textarea {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        button {
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
        }
        button:hover {
            background-color: #45a049;
        }
    </style>
</head>
<body>
    <h1>Upload de fichiers</h1>
    
    <!-- Formulaire standard -->
    <h2>Upload standard</h2>
    <form action="${pageContext.request.contextPath}/upload" 
          method="post" 
          enctype="multipart/form-data">
        
        <div class="form-group">
            <label for="file1">Fichier 1:</label>
            <input type="file" id="file1" name="file1" required>
        </div>
        
        <div class="form-group">
            <label for="file2">Fichier 2 (optionnel):</label>
            <input type="file" id="file2" name="file2">
        </div>
        
        <div class="form-group">
            <label for="description">Description:</label>
            <textarea id="description" name="description" rows="4" 
                      placeholder="Décrivez vos fichiers..."></textarea>
        </div>
        
        <button type="submit">Envoyer</button>
    </form>
    
    <hr style="margin: 40px 0;">
    
    <!-- Formulaire avec upload multiple -->
    <h2>Upload multiple</h2>
    <form action="${pageContext.request.contextPath}/upload" 
          method="post" 
          enctype="multipart/form-data">
        
        <div class="form-group">
            <label for="files">Sélectionner plusieurs fichiers:</label>
            <input type="file" id="files" name="files" multiple required>
        </div>
        
        <div class="form-group">
            <label for="desc2">Description:</label>
            <input type="text" id="desc2" name="description" 
                   placeholder="Description...">
        </div>
        
        <button type="submit">Envoyer tous les fichiers</button>
    </form>
    
    <hr style="margin: 40px 0;">
    
    <!-- Upload via API (avec JavaScript) -->
    <h2>Upload via API (JSON)</h2>
    <div class="form-group">
        <label for="apiFiles">Fichiers:</label>
        <input type="file" id="apiFiles" multiple>
    </div>
    <button onclick="uploadViaApi()">Upload via API</button>
    <div id="apiResult" style="margin-top: 20px;"></div>
    
    <script>
        function uploadViaApi() {
            const fileInput = document.getElementById('apiFiles');
            const files = fileInput.files;
            
            if (files.length === 0) {
                alert('Veuillez sélectionner au moins un fichier');
                return;
            }
            
            const formData = new FormData();
            for (let i = 0; i < files.length; i++) {
                formData.append('file' + i, files[i]);
            }
            
            fetch('${pageContext.request.contextPath}/api/upload', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                document.getElementById('apiResult').innerHTML = 
                    '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
            })
            .catch(error => {
                document.getElementById('apiResult').innerHTML = 
                    '<p style="color: red;">Erreur: ' + error + '</p>';
            });
        }
    </script>
</body>
</html>