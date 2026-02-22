<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.entity.Reservation" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Réservations non assignées" %></title>
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
        h1 { text-align: center; }
        .subtitle { text-align: center; color: #666; margin-bottom: 20px; }
        .actions { text-align: center; margin-bottom: 20px; }
        .btn {
            background-color: #007bff;
            color: white;
            padding: 10px 16px;
            text-decoration: none;
            border-radius: 4px;
            margin: 4px;
            display: inline-block;
        }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 10px; border-bottom: 1px solid #ddd; text-align: left; }
        th { background-color: #f2f2f2; }
        .empty-message { text-align: center; color: #666; padding: 25px; }
    </style>
</head>
<body>
<div class="container">
    <h1><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Réservations non assignées" %></h1>
    <p class="subtitle">Date sélectionnée : <strong><%= request.getAttribute("selectedDate") %></strong></p>

    <div class="actions">
        <a class="btn" href="${pageContext.request.contextPath}/reservation/date/filter">Changer la date</a>
        <a class="btn" href="${pageContext.request.contextPath}/reservation/list">Retour liste générale</a>
    </div>

    <%
        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage != null && !errorMessage.isEmpty()) {
    %>
    <div class="empty-message"><%= errorMessage %></div>
    <%
        }

        List<Reservation> unassignedReservations = (List<Reservation>) request.getAttribute("unassignedReservations");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        if (unassignedReservations != null && !unassignedReservations.isEmpty()) {
    %>
    <table>
        <thead>
            <tr>
                <th>Réservation</th>
                <th>Client</th>
                <th>Lieu</th>
                <th>Passagers</th>
                <th>Date/Heure</th>
            </tr>
        </thead>
        <tbody>
        <%
            for (Reservation reservation : unassignedReservations) {
        %>
            <tr>
                <td>#<%= reservation.getId() %></td>
                <td><%= reservation.getClient() %></td>
                <td><%= reservation.getLieuCode() != null ? reservation.getLieuCode() : "N/A" %></td>
                <td><%= reservation.getNbPassager() %></td>
                <td><%= reservation.getDateHeure() != null ? reservation.getDateHeure().format(formatter) : "N/A" %></td>
            </tr>
        <%
            }
        %>
        </tbody>
    </table>
    <%
        } else if (errorMessage == null || errorMessage.isEmpty()) {
    %>
    <div class="empty-message">Aucune réservation non assignée trouvée pour cette date.</div>
    <%
        }
    %>
</div>
</body>
</html>
