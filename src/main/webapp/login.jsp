<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Connexion</title>
</head>
<body>
    <h1>Connexion</h1>
    
    <form action="/auth/login" method="post">
        <div>
            <label for="username">Nom d'utilisateur:</label>
            <input type="text" id="username" name="username" required>
        </div>
        
        <div>
            <label for="password">Mot de passe:</label>
            <input type="password" id="password" name="password" required>
        </div>
        
        <div>
            <label>Rôle:</label><br>
            <input type="radio" id="user" name="role" value="USER" checked>
            <label for="user">Utilisateur</label><br>
            
            <input type="radio" id="admin" name="role" value="ADMIN">
            <label for="admin">Administrateur</label><br>
            
            <input type="radio" id="superadmin" name="role" value="SUPER_ADMIN">
            <label for="superadmin">Super Administrateur</label>
        </div>
        
        <button type="submit">Se connecter</button>
    </form>
    
    <p>Pages de test:</p>
    <ul>
        <li><a href="/public/page">Page publique (accessible à tous)</a></li>
        <li><a href="/user/profile">Profil utilisateur (nécessite connexion)</a></li>
        <li><a href="/admin/dashboard">Tableau de bord admin (nécessite rôle ADMIN)</a></li>
    </ul>
</body>
</html>