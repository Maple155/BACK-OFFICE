<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.entity.Vehicule" %>
<%@ page import="com.entity.TypeCarburant" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Formulaire de véhicule" %></title>
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
        .btn-secondary {
            background-color: #6c757d;
        }
        .btn-secondary:hover {
            background-color: #5a6268;
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
        .button-group {
            display: flex;
            gap: 10px;
            margin-top: 20px;
        }
        .button-group .btn {
            flex: 1;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1><%= request.getAttribute("title") != null ? request.getAttribute("title") : "Formulaire de véhicule" %></h1>
        
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
        
        <form action="${pageContext.request.contextPath}/vehicule/save" method="post">
            <% 
                Vehicule vehicule = (Vehicule) request.getAttribute("vehicule");
                boolean isEdit = vehicule != null && vehicule.getId() > 0;
                
                if (isEdit) {
            %>
                <input type="hidden" name="id" value="<%= vehicule.getId() %>">
            <%
                }
            %>
            
            <div class="form-group">
                <label for="reference">Référence:</label>
                <input type="text" id="reference" name="reference" 
                       value="<%= isEdit ? vehicule.getReference() : "" %>" required>
            </div>
            
            <div class="form-group">
                <label for="nbPlaces">Nombre de places:</label>
                <input type="number" id="nbPlaces" name="nbPlaces" min="1" max="50"
                       value="<%= isEdit ? vehicule.getNbPlaces() : "4" %>" required>
            </div>
            
            <div class="form-group">
                <label for="typeCarburantId">Type de carburant:</label>
                <select id="typeCarburantId" name="typeCarburantId" required>
                    <option value="">Sélectionner un type de carburant</option>
                    <%
                        List<TypeCarburant> typesCarburant = (List<TypeCarburant>) request.getAttribute("typesCarburant");
                        if (typesCarburant != null) {
                            for (TypeCarburant type : typesCarburant) {
                                String selected = "";
                                if (isEdit && vehicule.getTypeCarburantId() == type.getId()) {
                                    selected = "selected";
                                }
                    %>
                        <option value="<%= type.getId() %>" <%= selected %>>
                            <%= type.getLibelle() %>
                        </option>
                    <%
                            }
                        }
                    %>
                </select>
            </div>
            
            <div class="button-group">
                <button type="submit" class="btn">
                    <%= isEdit ? "Modifier" : "Créer" %> le véhicule
                </button>
                <a href="${pageContext.request.contextPath}/vehicule/list" class="btn btn-secondary" style="text-align: center; line-height: 20px;">
                    Annuler
                </a>
            </div>
        </form>
        
        <a href="${pageContext.request.contextPath}/vehicule/list" class="link-button">
            Voir tous les véhicules
        </a>
    </div>
</body>
</html>