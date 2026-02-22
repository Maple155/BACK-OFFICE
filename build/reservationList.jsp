<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.entity.Reservation" %>
<%@ page import="com.entity.Lieu" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="fr" data-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Réservations | Location Admin</title>
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
                <h1 class="page-title">Gestion des Réservations</h1>
                <div class="header-actions">
                    <button class="btn btn-primary" onclick="openModal('reservationModal')">
                        <i class="fas fa-plus"></i>
                        Nouvelle Réservation
                    </button>
                </div>
            </header>
            
            <div class="content-area">
                <%
                    String successMessage = (String) request.getAttribute("successMessage");
                    String errorMessage = (String) request.getAttribute("errorMessage");
                    if (successMessage != null && !successMessage.isEmpty()) {
                %>
                    <div class="alert alert-success">
                        <span class="alert-icon"><i class="fas fa-check-circle"></i></span>
                        <div class="alert-content"><%= successMessage %></div>
                        <button class="alert-close" onclick="this.parentElement.remove()">&times;</button>
                    </div>
                <% } if (errorMessage != null && !errorMessage.isEmpty()) { %>
                    <div class="alert alert-error">
                        <span class="alert-icon"><i class="fas fa-exclamation-circle"></i></span>
                        <div class="alert-content"><%= errorMessage %></div>
                        <button class="alert-close" onclick="this.parentElement.remove()">&times;</button>
                    </div>
                <% } %>

                <div class="stats-grid">
                    <% 
                        List<Reservation> reservationsStats = (List<Reservation>) request.getAttribute("reservations");
                        int totalReservations = reservationsStats != null ? reservationsStats.size() : 0;
                    %>
                    <div class="stat-card">
                        <div class="stat-icon primary"><i class="fas fa-clipboard-list"></i></div>
                        <div class="stat-content">
                            <div class="stat-value"><%= totalReservations %></div>
                            <div class="stat-label">Total Réservations</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon success"><i class="fas fa-check"></i></div>
                        <div class="stat-content">
                            <div class="stat-value"><%= totalReservations %></div>
                            <div class="stat-label">Confirmées</div>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon warning"><i class="fas fa-clock"></i></div>
                        <div class="stat-content">
                            <div class="stat-value">0</div>
                            <div class="stat-label">En attente</div>
                        </div>
                    </div>
                </div>

                <div class="card animate-slide-up mb-4">
                    <div class="card-body" style="display: flex; gap: 12px; flex-wrap: wrap;">
                        <a href="${pageContext.request.contextPath}/reservation/date/filter" class="btn btn-secondary">
                            <i class="fas fa-calendar-alt"></i> Filtrer par date
                        </a>
                    </div>
                </div>

                <div class="card animate-slide-up">
                    <div class="card-header">
                        <div class="card-title">
                            <div class="card-title-icon"><i class="fas fa-list"></i></div>
                            Liste des Réservations
                        </div>
                    </div>
                    <div class="card-body" style="padding: 0;">
                        <div class="table-wrapper">
                            <%
                                List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
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
                                            <th style="text-align: right;">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (Reservation reservation : reservations) { %>
                                            <tr>
                                                <td><span class="text-muted">#<%= reservation.getId() %></span></td>
                                                <td><strong><%= reservation.getClient() %></strong></td>
                                                <td><span class="badge badge-secondary"><%= reservation.getLieuCode() != null ? reservation.getLieuCode() : "N/A" %></span></td>
                                                <td><span class="badge badge-info"><%= reservation.getNbPassager() %> pers.</span></td>
                                                <td><%= reservation.getDateHeure() != null ? reservation.getDateHeure().format(formatter) : "N/A" %></td>
                                                <td>
                                                    <div class="table-actions">
                                                        <button class="btn btn-ghost btn-sm" onclick="confirmDelete(<%= reservation.getId() %>, '<%= reservation.getClient() %>')" title="Supprimer">
                                                            <i class="fas fa-trash-alt"></i>
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            <% } else { %>
                                <div class="empty-state">
                                    <div class="empty-state-icon"><i class="fas fa-clipboard-list"></i></div>
                                    <div class="empty-state-title">Aucune réservation</div>
                                    <p class="empty-state-text">Créez votre première réservation pour commencer.</p>
                                    <button class="btn btn-primary" onclick="openModal('reservationModal')">
                                        <i class="fas fa-plus"></i>
                                        Nouvelle réservation
                                    </button>
                                </div>
                            <% } %>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <div class="modal-overlay" id="reservationModal">
        <div class="modal">
            <div class="modal-header">
                <h2 class="modal-title">Nouvelle Réservation</h2>
                <button class="modal-close" onclick="closeModal('reservationModal')">&times;</button>
            </div>
            <form action="${pageContext.request.contextPath}/reservation/save" method="post">
                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label required" for="client">Nom du client</label>
                        <input type="text" id="client" name="client" class="form-input" placeholder="Ex: Jean Dupont" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label required" for="idLieu">Lieu de prise en charge</label>
                        <select id="idLieu" name="idLieu" class="form-select" required>
                            <option value="">Sélectionner un lieu</option>
                            <%
                                List<Lieu> lieux = (List<Lieu>) request.getAttribute("lieux");
                                if (lieux != null) {
                                    for (Lieu lieu : lieux) {
                            %>
                                <option value="<%= lieu.getId() %>"><%= lieu.getCode() %> - <%= lieu.getLibelle() %></option>
                            <% }} %>
                        </select>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label required" for="nbPassager">Passagers</label>
                            <input type="number" id="nbPassager" name="nbPassager" class="form-input" min="1" max="20" value="1" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label required" for="dateHeure">Date et heure</label>
                            <input type="datetime-local" id="dateHeure" name="dateHeure" class="form-input" required>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" onclick="closeModal('reservationModal')">Annuler</button>
                    <button type="submit" class="btn btn-primary"><i class="fas fa-save"></i> Créer</button>
                </div>
            </form>
        </div>
    </div>

    <div class="modal-overlay" id="deleteModal">
        <div class="modal" style="max-width: 400px;">
            <div class="modal-header">
                <h2 class="modal-title">Confirmer la suppression</h2>
                <button class="modal-close" onclick="closeModal('deleteModal')">&times;</button>
            </div>
            <div class="modal-body text-center">
                <div class="empty-state-icon" style="background: var(--error-bg); color: var(--error);">
                    <i class="fas fa-trash-alt"></i>
                </div>
                <p>Supprimer la réservation de <strong id="deleteClientName"></strong> ?</p>
                <p class="text-muted">Cette action est irréversible.</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeModal('deleteModal')">Annuler</button>
                <a href="#" id="deleteLink" class="btn btn-danger"><i class="fas fa-trash-alt"></i> Supprimer</a>
            </div>
        </div>
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

        function openModal(modalId) {
            document.getElementById(modalId).classList.add('active');
            document.body.style.overflow = 'hidden';
        }

        function closeModal(modalId) {
            document.getElementById(modalId).classList.remove('active');
            document.body.style.overflow = '';
        }

        document.querySelectorAll('.modal-overlay').forEach(overlay => {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) closeModal(overlay.id);
            });
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                document.querySelectorAll('.modal-overlay.active').forEach(modal => closeModal(modal.id));
            }
        });

        function confirmDelete(id, clientName) {
            document.getElementById('deleteClientName').textContent = clientName;
            document.getElementById('deleteLink').href = '${pageContext.request.contextPath}/reservation/delete/' + id;
            openModal('deleteModal');
        }

        document.getElementById('dateHeure').value = new Date().toISOString().slice(0, 16);

        setTimeout(() => {
            document.querySelectorAll('.alert').forEach(alert => {
                alert.style.opacity = '0';
                setTimeout(() => alert.remove(), 300);
            });
        }, 5000);
    </script>
</body>
</html>
