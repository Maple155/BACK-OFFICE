<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <h1>Bonjour</h1>

    <h2> Avec Param </h2>
    <form action="/framework/testFormulaire" method="post">

        <label for="nom"> Nom : </label>
        <input type="text" name="nom" value="Ranto" id="nom" required>
        <br><br>
        <label for="age"> Age : </label>
        <input type="number" name="age" value="19" id="age" required>
        <br><br>
        <input type="submit" value="Valider">
    </form>

    <h2> sans Param </h2>
    <form action="/framework/testFormulaireSansParam" method="post">

        <label for="nom"> Nom : </label>
        <input type="text" name="nom" value="Ranto" id="nom" required>
        <br><br>
        <label for="age"> Age : </label>
        <input type="number" name="age" value="19" id="age" required>
        <br><br>
        <input type="submit" value="Valider">
    </form>
</body>
</html>