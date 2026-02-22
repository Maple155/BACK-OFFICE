<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="fr" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Réservations Assignées | Location Admin</title>
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
                <h1 class="page-title">Réservations Assignées</h1>
                <div class="header-actions">
                    <a href="${pageContext.request.contextPath}/reservation/date/filter" class="btn btn-secondary">
                        <i class="fas fa-arrow-left"></i> Retour
                    </a>
                </div>
            </header>
            
            <div class="content-area">
                <%
                    String dateDebut = (String) request.getAttribute("dateDebut");
                    String dateFin = (String) request.getAttribute("dateFin");
                %>
                <% if (dateDebut != null && dateFin != null) { %>
                <div class="card animate-slide-up mb-4" style="margin-bottom: 1.5rem;">
                    <div class="card-body" style="padding: 1rem 1.5rem;">
                        <strong><i class="fas fa-calendar"></i> Période :</strong> 
                        Du <%= dateDebut %> au <%= dateFin %>
                    </div>
                </div>
                <% } %>
                
                <div class="card animate-slide-up">
                    <div class="card-header">
                        <div class="card-title">
                            <div class="card-title-icon"><i class="fas fa-check-circle"></i></div>
                            Liste des Réservations Assignées
                        </div>
                    </div>
                    <div class="card-body" style="padding: 0;">
                        <div class="table-wrapper">
                            <%
                                List<Map<String, Object>> reservations = (List<Map<String, Object>>) request.getAttribute("assignedReservations");
                                if (reservations != null && !reservations.isEmpty()) {
                            %>
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>Client</th>
                                            <th>Lieu</th>
                                            <th>Passagers</th>
                                            <th>Date/Heure</th>
                                            <th>Véhicule</th>
                                            <th>Statut</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Map<String, Object> reservation : reservations) { %>
                                            <tr>
                                                <td><span class="text-muted">#<%= reservation.get("id") %></span></td>
                                                <td><strong><%= reservation.get("client") %></strong></td>
                                                <td><span class="badge badge-secondary"><%= reservation.get("lieuCode") != null ? reservation.get("lieuCode") : "N/A" %></span></td>
                                                <td><span class="badge badge-info"><%= reservation.get("nbPassager") %> pers.</span></td>
                                                <td><%= reservation.get("dateHeure") != null ? reservation.get("dateHeure") : "N/A" %></td>
                                                <td><%= reservation.get("vehicule") != null ? reservation.get("vehicule") : "N/A" %></td>
                                                <td><span class="badge badge-success"><i class="fas fa-check"></i> Assignée</span></td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            <% } else { %>
                                <div class="empty-state">
                                    <div class="empty-state-icon"><i class="fas fa-clipboard-check"></i></div>
                                    <div class="empty-state-title">Aucune réservation assignée</div>
                                    <p class="empty-state-text">Il n'y a pas de réservation assignée pour cette date.</p>
                                </div>
                            <% } %>
                        </div>
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
