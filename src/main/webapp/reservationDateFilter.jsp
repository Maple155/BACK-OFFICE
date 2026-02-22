<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Filtrer les réservations" %></title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 700px;
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
        input[type="date"] {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            box-sizing: border-box;
        }
        .actions {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        .btn {
            background-color: #007bff;
            color: white;
            padding: 12px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 15px;
            text-decoration: none;
            display: inline-block;
        }
        .btn-secondary {
            background-color: #28a745;
        }
        .btn-back {
            background-color: #6c757d;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Liste des réservations par date" %></h1>

        <form id="dateForm" method="get">
            <div class="form-group">
                <label for="date">Date :</label>
                <input type="date" id="date" name="date" required>
            </div>
            <div class="actions">
                <button type="button" class="btn" onclick="goToAssigned()">Voir liste assignée</button>
                <button type="button" class="btn btn-secondary" onclick="goToUnassigned()">Voir non assignée</button>
                <a href="${pageContext.request.contextPath}/reservation/list" class="btn btn-back">Retour liste générale</a>
            </div>
        </form>
    </div>

    <script>
        const dateInput = document.getElementById('date');
        dateInput.value = new Date().toISOString().split('T')[0];

        function getSelectedDate() {
            return encodeURIComponent(dateInput.value);
        }

        function goToAssigned() {
            if (!dateInput.value) {
                alert('Veuillez sélectionner une date');
                return;
            }
            window.location.href = '${pageContext.request.contextPath}/reservation/date/assigned?date=' + getSelectedDate();
        }

        function goToUnassigned() {
            if (!dateInput.value) {
                alert('Veuillez sélectionner une date');
                return;
            }
            window.location.href = '${pageContext.request.contextPath}/reservation/date/unassigned?date=' + getSelectedDate();
        }
    </script>
</body>
</html>
