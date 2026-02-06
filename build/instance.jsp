<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Formulaire Instance Test</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
        }
        .form-section {
            border: 2px solid #333;
            padding: 20px;
            margin-bottom: 20px;
            border-radius: 5px;
        }
        .form-section h2 {
            margin-top: 0;
            color: #333;
        }
        .form-group {
            margin-bottom: 15px;
        }
        label {
            display: inline-block;
            width: 150px;
            font-weight: bold;
        }
        input[type="text"],
        input[type="number"],
        input[type="date"] {
            width: 300px;
            padding: 5px;
            border: 1px solid #ccc;
            border-radius: 3px;
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
        .info {
            background-color: #e7f3fe;
            border-left: 4px solid #2196F3;
            padding: 10px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <h1>Test d'Instanciation Automatique d'Objets</h1>
    <br>
    <h1>(ETU 3113)</h1>
    <div class="info">
        <strong>Convention de nommage :</strong> 
        Pour remplir automatiquement un objet, utilisez la notation : <code>nomArgument.nomAttribut</code>
    </div>

    <form action="/framework/controllerA/instance/param" method="POST">
        
        <!-- Section User -->
        <div class="form-section">
            <h2>User</h2>
            
            <div class="form-group">
                <label for="user.name">Nom :</label>
                <input type="text" id="user.name" name="user.name" value="RAKOTO">
            </div>
            
            <div class="form-group">
                <label for="user.prenom">Prénom :</label>
                <input type="text" id="user.prenom" name="user.prenom" value="Jean">
            </div>
            
            <div class="form-group">
                <label for="user.age">Âge :</label>
                <input type="number" id="user.age" name="user.age" value="25">
            </div>
            
            <div class="form-group">
                <label for="user.dateNaissance">Date de naissance :</label>
                <input type="date" id="user.dateNaissance" name="user.dateNaissance" value="1999-05-15">
            </div>
            
            <div class="form-group">
                <label for="user.hobbies">Hobbies :</label>
                <input type="text" id="user.hobbies" name="user.hobbies" value="Football">
                <input type="text" name="user.hobbies" value="Lecture" style="margin-left: 155px; margin-top: 5px;">
                <input type="text" name="user.hobbies" value="Musique" style="margin-left: 155px; margin-top: 5px;">
            </div>
            
            <div class="form-group">
                <label for="user.diplome">Diplômes :</label>
                <input type="text" id="user.diplome" name="user.diplome" value="CEPE">
                <input type="text" name="user.diplome" value="BEPC" style="margin-left: 155px; margin-top: 5px;">
                <input type="text" name="user.diplome" value="BACC" style="margin-left: 155px; margin-top: 5px;">
            </div>
        </div>

        <!-- Section Personne -->
        <div class="form-section">
            <h2>Personne</h2>
            
            <div class="form-group">
                <label for="personne.name">Nom :</label>
                <input type="text" id="personne.name" name="personne.name" value="RASOANAIVO">
            </div>
            
            <div class="form-group">
                <label for="personne.prenom">Prénom :</label>
                <input type="text" id="personne.prenom" name="personne.prenom" value="Marie">
            </div>
            
            <div class="form-group">
                <label for="personne.age">Âge :</label>
                <input type="number" id="personne.age" name="personne.age" value="30">
            </div>
            
            <div class="form-group">
                <label for="personne.dateNaissance">Date de naissance :</label>
                <input type="date" id="personne.dateNaissance" name="personne.dateNaissance" value="1994-08-20">
            </div>
            
            <div class="form-group">
                <label for="personne.hobbies">Hobbies :</label>
                <input type="text" id="personne.hobbies" name="personne.hobbies" value="Cuisine">
                <input type="text" name="personne.hobbies" value="Voyage" style="margin-left: 155px; margin-top: 5px;">
            </div>
            
            <div class="form-group">
                <label for="personne.diplome">Diplômes :</label>
                <input type="text" id="personne.diplome" name="personne.diplome" value="License">
                <input type="text" name="personne.diplome" value="Master" style="margin-left: 155px; margin-top: 5px;">
            </div>
        </div>

        <button type="submit">Envoyer</button>
    </form>

</body>
</html>