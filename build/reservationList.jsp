<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.entity.Reservation" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Liste des réservations" %></title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1000px;
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
        .actions {
            margin-bottom: 20px;
            text-align: center;
        }
        .btn {
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            margin: 5px;
        }
        .btn:hover {
            background-color: #45a049;
        }
        .btn-new {
            background-color: #2196F3;
        }
        .btn-new:hover {
            background-color: #0b7dda;
        }
        .btn-delete {
            background-color: #f44336;
        }
        .btn-delete:hover {
            background-color: #da190b;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        th {
            background-color: #f2f2f2;
            font-weight: bold;
        }
        tr:hover {
            background-color: #f5f5f5;
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
        .empty-message {
            text-align: center;
            padding: 40px;
            color: #666;
            font-style: italic;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Liste des réservations" %></h1>
        
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
        
        <div class="actions">
            <a href="${pageContext.request.contextPath}/reservation/form" class="btn btn-new">
                Nouvelle réservation
            </a>
        </div>
        
        <%
            List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            if (reservations != null && !reservations.isEmpty()) {
        %>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Client</th>
                        <th>Hôtel</th>
                        <th>Passagers</th>
                        <th>Date/Heure</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (Reservation reservation : reservations) {
                    %>
                        <tr>
                            <td><%= reservation.getId() %></td>
                            <td><%= reservation.getClient() %></td>
                            <td><%= reservation.getHotelNom() != null ? reservation.getHotelNom() : "N/A" %></td>
                            <td><%= reservation.getNbPassager() %></td>
                            <td><%= reservation.getDateHeure() != null ? reservation.getDateHeure().format(formatter) : "N/A" %></td>
                            <td>
                                <a href="${pageContext.request.contextPath}/reservation/delete/<%= reservation.getId() %>" 
                                   class="btn btn-delete"
                                   onclick="return confirm('Êtes-vous sûr de vouloir supprimer cette réservation ?')">
                                    Supprimer
                                </a>
                            </td>
                        </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        <%
            } else {
        %>
            <div class="empty-message">
                Aucune réservation trouvée. Créez votre première réservation!
            </div>
        <%
            }
        %>
    </div>
</body>
</html>