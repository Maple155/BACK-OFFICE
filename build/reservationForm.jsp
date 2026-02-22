<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.entity.Reservation" %>
<%@ page import="com.entity.Vehicule" %>
<%@ page import="com.entity.Lieu" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="fr" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Modifier Réservation | Location Admin</title>
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
                    <a href="${pageContext.request.contextPath}/vehicule/list" class="nav-item" data-tooltip="Véhicules">
                        <span class="nav-icon"><i class="fas fa-car-side"></i></span>
                        <span class="nav-text">Véhicules</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/reservation/list" class="nav-item active" data-tooltip="Réservations">
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
                <h1 class="page-title">Modifier la Réservation</h1>
                <div class="header-actions">
                    <a href="${pageContext.request.contextPath}/reservation/list" class="btn btn-secondary">
                        <i class="fas fa-arrow-left"></i> Retour
                    </a>
                </div>
            </header>
            
            <div class="content-area">
                <div class="card animate-slide-up" style="max-width: 700px;">
                    <%
                        Reservation reservation = (Reservation) request.getAttribute("reservation");
                        List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
                        List<Lieu> lieux = (List<Lieu>) request.getAttribute("lieux");
                        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                    %>
                    <div class="card-header">
                        <div class="card-title">
                            <div class="card-title-icon"><i class="fas fa-edit"></i></div>
                            Informations de la Réservation #<%= reservation.getId() %>
                        </div>
                    </div>
                    <div class="card-body">
                        <form action="${pageContext.request.contextPath}/reservation/update" method="post">
                            <input type="hidden" name="id" value="<%= reservation.getId() %>">
                            
                            <div class="form-group">
                                <label class="form-label" for="client">
                                    <i class="fas fa-user"></i> Client
                                </label>
                                <input type="text" class="form-input" id="client" name="client" 
                                       value="<%= reservation.getClient() %>" required>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label" for="dateHeure">
                                    <i class="fas fa-calendar-alt"></i> Date et Heure
                                </label>
                                <input type="datetime-local" class="form-input" id="dateHeure" name="dateHeure" 
                                       value="<%= reservation.getDateHeure() != null ? reservation.getDateHeure().format(dtFormatter) : "" %>" required>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label" for="nbPassager">
                                    <i class="fas fa-users"></i> Nombre de passagers
                                </label>
                                <input type="number" class="form-input" id="nbPassager" name="nbPassager" 
                                       value="<%= reservation.getNbPassager() %>" min="1" max="50" required>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label" for="lieu">
                                    <i class="fas fa-map-marker-alt"></i> Lieu
                                </label>
                                <select class="form-input" id="lieu" name="idLieu" required>
                                    <% if (lieux != null) {
                                        for (Lieu l : lieux) { %>
                                            <option value="<%= l.getId() %>" 
                                                <%= (reservation.getIdLieu() == l.getId()) ? "selected" : "" %>>
                                                <%= l.getCode() %> - <%= l.getNom() %>
                                            </option>
                                    <% }} %>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label class="form-label" for="vehicule">
                                    <i class="fas fa-car"></i> Véhicule (assignation)
                                </label>
                                <select class="form-input" id="vehicule" name="idVehicule">
                                    <option value="">-- Non assigné --</option>
                                    <% if (vehicules != null) {
                                        for (Vehicule v : vehicules) { %>
                                            <option value="<%= v.getId() %>" 
                                                <%= (reservation.getIdVehicule() != null && reservation.getIdVehicule() == v.getId()) ? "selected" : "" %>>
                                                <%= v.getMarque() %> <%= v.getModele() %> - <%= v.getImmatriculation() %> (<%= v.getNbPlace() %> places)
                                            </option>
                                    <% }} %>
                                </select>
                            </div>
                            
                            <div class="form-actions" style="display: flex; gap: 1rem; margin-top: 2rem;">
                                <button type="submit" class="btn btn-primary" style="flex: 1;">
                                    <i class="fas fa-save"></i> Enregistrer
                                </button>
                                <a href="${pageContext.request.contextPath}/reservation/list" class="btn btn-secondary" style="flex: 1;">
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
