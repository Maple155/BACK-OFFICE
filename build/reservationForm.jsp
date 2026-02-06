<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.entity.Hotel" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Formulaire de réservation" %></title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #333;
            text-align: center;
            margin-bottom: 30px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }
        input[type="text"],
        input[type="number"],
        input[type="datetime-local"],
        select {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            box-sizing: border-box;
        }
        .btn {
            background-color: #4CAF50;
            color: white;
            padding: 12px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            width: 100%;
        }
        .btn:hover {
            background-color: #45a049;
        }
        .alert {
            padding: 15px;
            border-radius: 4px;
            margin-bottom: 20px;
        }
        .alert-success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .alert-error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .link-button {
            display: inline-block;
            padding: 10px 20px;
            background-color: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            margin-top: 20px;
        }
        .link-button:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Formulaire de réservation" %></h1>
        
        <%-- Affichage des messages --%>
        <%
            String successMessage = (String) request.getAttribute("successMessage");
            String errorMessage = (String) request.getAttribute("errorMessage");
            
            if (successMessage != null && !successMessage.isEmpty()) {
        %>
            <div class="alert alert-success">
                <%= successMessage %>
            </div>
        <%
            }
            
            if (errorMessage != null && !errorMessage.isEmpty()) {
        %>
            <div class="alert alert-error">
                <%= errorMessage %>
            </div>
        <%
            }
        %>
        
        <form action="${pageContext.request.contextPath}/reservation/save" method="post">
            <div class="form-group">
                <label for="client">Nom du client:</label>
                <input type="text" id="client" name="client" required>
            </div>
            
            <div class="form-group">
                <label for="idHotel">Hôtel:</label>
                <select id="idHotel" name="idHotel" required>
                    <option value="">Sélectionner un hôtel</option>
                    <%
                        List<Hotel> hotels = (List<Hotel>) request.getAttribute("hotels");
                        if (hotels != null) {
                            for (Hotel hotel : hotels) {
                    %>
                        <option value="<%= hotel.getId() %>"><%= hotel.getNom() %></option>
                    <%
                            }
                        }
                    %>
                </select>
            </div>
            
            <div class="form-group">
                <label for="nbPassager">Nombre de passagers:</label>
                <input type="number" id="nbPassager" name="nbPassager" min="1" max="20" required>
            </div>
            
            <div class="form-group">
                <label for="dateHeure">Date et heure:</label>
                <input type="datetime-local" id="dateHeure" name="dateHeure" required>
            </div>
            
            <button type="submit" class="btn">Enregistrer la réservation</button>
        </form>
        
        <a href="${pageContext.request.contextPath}/reservation/list" class="link-button">
            Voir toutes les réservations
        </a>
    </div>
    
    <script>
        document.getElementById('dateHeure').value = new Date().toISOString().slice(0, 16);
    </script>
</body>
</html>