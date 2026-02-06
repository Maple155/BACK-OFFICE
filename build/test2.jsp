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

    <hr>

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