<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.test.test.User" %>

<%
    Object test = request.getAttribute("test");
    Object test2 = request.getAttribute("test2");
    Object user = request.getAttribute("user");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
    <h1> Mety </h1>

    <h3> Bonjour tous le monde !!! </h3>

    <%
        if (test != null) {
    %>
    <p><%= test.toString() %></p>
    <%
        } 
        if (test2 != null) {
    %>
    <p><%= test2.toString() %></p>
    <%
        }
    %>

    <% 
        if (user != null) {
    %>
    <p><%= ((User) user).getName() %></p>
    <%
        }
    %>

    <hr>

    <h1> (ETU 3113) </h1>
    <h2> POST avec tableaux </h2>
    <form action="/framework/controllerA/map" method="POST">

        <label for="nom"> Nom : </label>
        <input type="text" name="nom" value="Ranto" id="nom" required>
        <br><br>

        <label for="age"> Age : </label>
        <input type="number" name="age" value="19" id="age" required>
        <br><br>

        <fieldset>
            <legend>Langages de programmation (checkboxes - tableau)</legend>
            <input type="checkbox" name="langages" value="Java" id="java" checked>
            <label for="java">Java</label><br>
            
            <input type="checkbox" name="langages" value="Python" id="python">
            <label for="python">Python</label><br>
            
            <input type="checkbox" name="langages" value="JavaScript" id="js">
            <label for="js">JavaScript</label><br>
            
            <input type="checkbox" name="langages" value="C++" id="cpp">
            <label for="cpp">C++</label><br>
        </fieldset>
        <br>

        <fieldset>
            <legend>Compétences (select multiple - tableau)</legend>
            <select name="competences" multiple size="4">
                <option value="Web Development" selected>Web Development</option>
                <option value="Mobile Development">Mobile Development</option>
                <option value="Data Science">Data Science</option>
                <option value="DevOps">DevOps</option>
                <option value="AI/ML">AI/ML</option>
            </select>
        </fieldset>
        <br>

        <fieldset>
            <legend>Villes visitées (plusieurs champs avec même nom)</legend>
            <input type="text" name="villes" value="Paris" placeholder="Ville 1">
            <input type="text" name="villes" value="Lyon" placeholder="Ville 2">
            <input type="text" name="villes" value="" placeholder="Ville 3">
        </fieldset>
        <br>

        <fieldset>
            <legend>Préférences (radio buttons - valeur simple)</legend>
            <input type="radio" name="preference" value="frontend" id="front" checked>
            <label for="front">Frontend</label>
            
            <input type="radio" name="preference" value="backend" id="back">
            <label for="back">Backend</label>
            
            <input type="radio" name="preference" value="fullstack" id="full">
            <label for="full">Fullstack</label>
        </fieldset>
        <br>

        <input type="submit" value="Valider">
    </form>

</body>
</html>