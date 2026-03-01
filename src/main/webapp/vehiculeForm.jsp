<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.entity.Vehicule" %>
<%@ page import="com.entity.TypeCarburant" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="fr" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Modifier Véhicule | Location Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/modern-style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <style>
        :root { --font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif; }
    </style>
</head>
<body>
    <div class="app-layout" id="appLayout">
        <div class="sidebar-overlay" onclick="toggleMobileSidebar()"></div>
        
        <aside class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <a href="${pageContext.request.contextPath}/" class="sidebar-logo">
                    <div class="sidebar-logo-icon"><i class="fas fa-car"></i></div>
                    <div>
                        <div class="sidebar-logo-text">Location</div>
                        <div class="sidebar-logo-subtitle">Back Office</div>
                    </div>
                </a>
            </div>
            <button class="sidebar-toggle" onclick="toggleSidebarCollapse()" title="Réduire/Agrandir"><i class="fas fa-chevron-left"></i></button>
            
            <nav class="sidebar-nav">
                <div class="nav-section">
                    <div class="nav-section-title">Gestion</div>
                    <a href="${pageContext.request.contextPath}/vehicule/list" class="nav-item active" data-tooltip="Véhicules">
                        <span class="nav-icon"><i class="fas fa-car-side"></i></span>
                        <span class="nav-text">Véhicules</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/reservation/list" class="nav-item" data-tooltip="Réservations">
                        <span class="nav-icon"><i class="fas fa-clipboard-list"></i></span>
                        <span class="nav-text">Réservations</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/reservation/date/filter" class="nav-item" data-tooltip="Filtrer par date">
                        <span class="nav-icon"><i class="fas fa-calendar-alt"></i></span>
                        <span class="nav-text">Filtrer par date</span>
                    </a>
                </div>
            </nav>
            
            <div class="sidebar-footer">
                <div class="theme-toggle" onclick="toggleTheme()">
                    <span class="theme-toggle-text">Mode sombre</span>
                    <div class="theme-switch"></div>
                </div>
            </div>
        </aside>

        <main class="main-content">
            <header class="top-header">
                <button class="mobile-menu-btn" onclick="toggleMobileSidebar()">
                    <i class="fas fa-bars"></i>
                </button>
                <h1 class="page-title">Modifier le Véhicule</h1>
                <div class="header-actions">
                    <a href="${pageContext.request.contextPath}/vehicule/list" class="btn btn-secondary">
                        <i class="fas fa-arrow-left"></i> Retour
                    </a>
                </div>
            </header>
            
            <div class="content-area">
                <div class="card animate-slide-up" style="max-width: 600px;">
                    <%
                        Vehicule vehicule = (Vehicule) request.getAttribute("vehicule");
                        List<TypeCarburant> carburants = (List<TypeCarburant>) request.getAttribute("typesCarburant");
                    %>
                    <div class="card-header">
                        <div class="card-title">
                            <div class="card-title-icon"><i class="fas fa-edit"></i></div>
                            Informations du Véhicule
                        </div>
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/vehicule/save" method="post">
                            <input type="hidden" name="id" value="<%= vehicule != null ? vehicule.getId() : "" %>">
                            
                            <div class="form-group">
                                <label class="form-label" for="reference">
                                    <i class="fas fa-trademark"></i> Référence
                                </label>
                                <input type="text" class="form-input" id="reference" name="reference" 
                                       value="<%= vehicule != null ? vehicule.getReference() : "" %>" required>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label" for="nbPlaces">
                                    <i class="fas fa-users"></i> Nombre de places
                                </label>
                                <input type="number" class="form-input" id="nbPlaces" name="nbPlaces" 
                                       value="<%= vehicule != null ? vehicule.getNbPlaces() : "" %>" min="1" max="50" required>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label" for="typeCarburantId">
                                    <i class="fas fa-gas-pump"></i> Type de carburant
                                </label>
                                <select class="form-input" id="typeCarburantId" name="typeCarburantId" required>
                                    <% if (carburants != null) {
                                        for (TypeCarburant tc : carburants) { %>
                                            <option value="<%= tc.getId() %>" 
                                                <%= (vehicule != null && vehicule.getTypeCarburantId() == tc.getId()) ? "selected" : "" %>>
                                                <%= tc.getLibelle() %>
                                            </option>
                                    <% }} %>
                                </select>
                            </div>
                            
                            <div class="form-actions" style="display: flex; gap: 1rem; margin-top: 2rem;">
                                <button type="submit" class="btn btn-primary" style="flex: 1;">
                                    <i class="fas fa-save"></i> Enregistrer
                                </button>
                                <a href="${pageContext.request.contextPath}/vehicule/list" class="btn btn-secondary" style="flex: 1;">
                                    <i class="fas fa-times"></i> Annuler
                                </a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <script>
        function toggleTheme() {
            const html = document.documentElement;
            const newTheme = html.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            html.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
        }
        document.documentElement.setAttribute('data-theme', localStorage.getItem('theme') || 'light');

        function toggleMobileSidebar() {
            document.querySelector('.sidebar').classList.toggle('open');
            document.querySelector('.sidebar-overlay').classList.toggle('active');
        }

        // Sidebar Collapse Toggle (Desktop)
        function toggleSidebarCollapse() {
            const sidebar = document.getElementById('sidebar');
            const appLayout = document.getElementById('appLayout');
            const isCollapsed = sidebar.classList.toggle('collapsed');
            appLayout.classList.toggle('sidebar-collapsed', isCollapsed);
            localStorage.setItem('sidebarCollapsed', isCollapsed);
        }

        // Load saved sidebar state
        const sidebarCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
        if (sidebarCollapsed) {
            document.getElementById('sidebar').classList.add('collapsed');
            document.getElementById('appLayout').classList.add('sidebar-collapsed');
        }
    </script>
</body>
</html>
