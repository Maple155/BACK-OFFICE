<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.entity.Vehicule" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Liste des véhicules" %></title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1200px;
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
            margin-bottom: 30px;
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
        .table-container {
            overflow-x: auto;
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
            background-color: #f8f9fa;
            font-weight: bold;
            color: #333;
        }
        tr:hover {
            background-color: #f5f5f5;
        }
        .actions {
            display: flex;
            gap: 10px;
        }
        .btn {
            padding: 8px 16px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            font-size: 14px;
            display: inline-block;
            text-align: center;
        }
        .btn-edit {
            background-color: #ffc107;
            color: #212529;
        }
        .btn-edit:hover {
            background-color: #e0a800;
        }
        .btn-delete {
            background-color: #dc3545;
            color: white;
        }
        .btn-delete:hover {
            background-color: #c82333;
        }
        .btn-add {
            background-color: #28a745;
            color: white;
            padding: 10px 20px;
            margin-bottom: 20px;
        }
        .btn-add:hover {
            background-color: #218838;
        }
        .badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: bold;
        }
        .badge-info {
            background-color: #17a2b8;
            color: white;
        }
        .search-form {
            margin-bottom: 20px;
            display: flex;
            gap: 10px;
        }
        .search-form input {
            flex: 1;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        .search-form button {
            padding: 10px 20px;
            background-color: #007bff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        .search-form button:hover {
            background-color: #0056b3;
        }
        .no-data {
            text-align: center;
            padding: 40px;
            color: #6c757d;
            font-style: italic;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Liste des véhicules" %></h1>
        
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
        
        <div class="search-form">
            <input type="text" id="searchInput" placeholder="Rechercher par référence ou type de carburant...">
            <button onclick="searchVehicules()">Rechercher</button>
        </div>
        
        <a href="${pageContext.request.contextPath}/vehicule/form" class="btn btn-add">
            + Ajouter un nouveau véhicule
        </a>
        
        <div class="table-container">
            <%
                List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
                if (vehicules != null && !vehicules.isEmpty()) {
            %>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Référence</th>
                            <th>Nombre de places</th>
                            <th>Type de carburant</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            for (Vehicule vehicule : vehicules) {
                        %>
                            <tr>
                                <td><%= vehicule.getId() %></td>
                                <td><strong><%= vehicule.getReference() %></strong></td>
                                <td>
                                    <span class="badge badge-info">
                                        <%= vehicule.getNbPlaces() %> place(s)
                                    </span>
                                </td>
                                <td><%= vehicule.getTypeCarburantLibelle() != null ? vehicule.getTypeCarburantLibelle() : "N/A" %></td>
                                <td class="actions">
                                    <a href="${pageContext.request.contextPath}/vehicule/edit/<%= vehicule.getId() %>" 
                                       class="btn btn-edit">Modifier</a>
                                    <a href="${pageContext.request.contextPath}/vehicule/delete/<%= vehicule.getId() %>" 
                                       class="btn btn-delete" 
                                       onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce véhicule ?')">Supprimer</a>
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
                <div class="no-data">
                    Aucun véhicule trouvé. <a href="${pageContext.request.contextPath}/vehicule/form">Ajoutez-en un !</a>
                </div>
            <%
                }
            %>
        </div>
    </div>
    
    <script>
        function searchVehicules() {
            const searchTerm = document.getElementById('searchInput').value.trim();
            if (searchTerm) {
                window.location.href = '${pageContext.request.contextPath}/vehicule/list?search=' + encodeURIComponent(searchTerm);
            } else {
                window.location.href = '${pageContext.request.contextPath}/vehicule/list';
            }
        }
        
        document.getElementById('searchInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchVehicules();
            }
        });
    </script>
</body>
</html>