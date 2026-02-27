<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Location - Gestion de Véhicules</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/modern-style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <style>
        :root { --font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif; }
    </style>
</head>
<body class="landing-page">
    <!-- Navigation -->
    <nav class="landing-nav">
        <div class="landing-logo">
            <div class="landing-logo-icon">
                <i class="fas fa-car"></i>
            </div>
            <span class="landing-logo-text">Location</span>
        </div>
        <div class="landing-nav-links">
            <a href="#features" class="landing-nav-link">Fonctionnalités</a>
            <a href="#gallery" class="landing-nav-link">Galerie</a>
            <a href="${pageContext.request.contextPath}/vehicule/list" class="landing-btn landing-btn-primary" style="padding: 10px 20px;">
                <i class="fas fa-sign-in-alt"></i>
                Accéder au Back Office
            </a>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="landing-hero">
        <div class="landing-hero-content animate-fade-in">
            <div class="landing-hero-badge">
                <i class="fas fa-shield-alt"></i>
                Plateforme de gestion professionnelle
            </div>
            <h1 class="landing-hero-title">
                Gérez votre flotte de <span>véhicules</span> en toute simplicité
            </h1>
            <p class="landing-hero-text">
                Une solution complète pour la gestion de vos véhicules et réservations. 
                Interface moderne, intuitive et performante pour optimiser votre activité de location.
            </p>
            <div class="landing-hero-buttons">
                <a href="${pageContext.request.contextPath}/vehicule/list" class="landing-btn landing-btn-primary">
                    <i class="fas fa-rocket"></i>
                    Commencer maintenant
                </a>
                <a href="#features" class="landing-btn landing-btn-secondary">
                    <i class="fas fa-info-circle"></i>
                    En savoir plus
                </a>
            </div>
        </div>
        <div class="landing-hero-image animate-slide-up">
            <img src="https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=800&q=80" alt="Voiture de luxe">
        </div>
    </section>

    <!-- Features Section -->
    <section class="landing-features" id="features">
        <div class="landing-section-header">
            <h2 class="landing-section-title">Fonctionnalités principales</h2>
            <p class="landing-section-text">
                Découvrez les outils puissants qui vous permettent de gérer efficacement votre parc automobile.
            </p>
        </div>
        <div class="landing-features-grid">
            <div class="landing-feature-card">
                <div class="landing-feature-icon">
                    <i class="fas fa-car-side"></i>
                </div>
                <h3 class="landing-feature-title">Gestion des Véhicules</h3>
                <p class="landing-feature-text">
                    Ajoutez, modifiez et suivez tous vos véhicules avec des informations détaillées : 
                    référence, nombre de places, type de carburant et plus encore.
                </p>
            </div>
            <div class="landing-feature-card">
                <div class="landing-feature-icon">
                    <i class="fas fa-calendar-check"></i>
                </div>
                <h3 class="landing-feature-title">Suivi des Réservations</h3>
                <p class="landing-feature-text">
                    Gérez les réservations de vos clients, assignez des véhicules et suivez 
                    l'état de chaque location en temps réel.
                </p>
            </div>
            <div class="landing-feature-card">
                <div class="landing-feature-icon">
                    <i class="fas fa-filter"></i>
                </div>
                <h3 class="landing-feature-title">Filtres Avancés</h3>
                <p class="landing-feature-text">
                    Filtrez vos réservations par date, par statut d'assignation et trouvez 
                    rapidement les informations dont vous avez besoin.
                </p>
            </div>
            <div class="landing-feature-card">
                <div class="landing-feature-icon">
                    <i class="fas fa-chart-line"></i>
                </div>
                <h3 class="landing-feature-title">Tableau de Bord</h3>
                <p class="landing-feature-text">
                    Visualisez les statistiques clés de votre activité avec un tableau de bord 
                    intuitif et des indicateurs de performance.
                </p>
            </div>
            <div class="landing-feature-card">
                <div class="landing-feature-icon">
                    <i class="fas fa-moon"></i>
                </div>
                <h3 class="landing-feature-title">Mode Sombre</h3>
                <p class="landing-feature-text">
                    Basculez entre le mode clair et sombre selon vos préférences pour un 
                    confort visuel optimal à toute heure.
                </p>
            </div>
            <div class="landing-feature-card">
                <div class="landing-feature-icon">
                    <i class="fas fa-mobile-alt"></i>
                </div>
                <h3 class="landing-feature-title">Design Responsive</h3>
                <p class="landing-feature-text">
                    Accédez à votre back-office depuis n'importe quel appareil : ordinateur, 
                    tablette ou smartphone.
                </p>
            </div>
        </div>
    </section>

    <!-- Gallery Section -->
    <section class="landing-gallery" id="gallery">
        <div class="landing-section-header">
            <h2 class="landing-section-title">Notre Flotte</h2>
            <p class="landing-section-text">
                Découvrez la variété de véhicules que vous pouvez gérer avec notre plateforme.
            </p>
        </div>
        <div class="landing-gallery-grid">
            <div class="landing-gallery-item">
                <img src="https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=600&q=80" alt="Berline">
                <div class="landing-gallery-overlay">
                    <h4 class="landing-gallery-title">Berlines</h4>
                    <p class="landing-gallery-text">Confort et élégance</p>
                </div>
            </div>
            <div class="landing-gallery-item">
                <img src="https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=600&q=80" alt="SUV">
                <div class="landing-gallery-overlay">
                    <h4 class="landing-gallery-title">SUV</h4>
                    <p class="landing-gallery-text">Puissance et polyvalence</p>
                </div>
            </div>
            <div class="landing-gallery-item">
                <img src="https://images.unsplash.com/photo-1502877338535-766e1452684a?w=600&q=80" alt="Sport">
                <div class="landing-gallery-overlay">
                    <h4 class="landing-gallery-title">Sportives</h4>
                    <p class="landing-gallery-text">Performance et style</p>
                </div>
            </div>
            <div class="landing-gallery-item">
                <img src="https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=600&q=80" alt="Citadine">
                <div class="landing-gallery-overlay">
                    <h4 class="landing-gallery-title">Citadines</h4>
                    <p class="landing-gallery-text">Praticité urbaine</p>
                </div>
            </div>
            <div class="landing-gallery-item">
                <img src="https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=600&q=80" alt="Premium">
                <div class="landing-gallery-overlay">
                    <h4 class="landing-gallery-title">Premium</h4>
                    <p class="landing-gallery-text">Luxe et prestige</p>
                </div>
            </div>
            <div class="landing-gallery-item">
                <img src="https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=600&q=80" alt="Électrique">
                <div class="landing-gallery-overlay">
                    <h4 class="landing-gallery-title">Électriques</h4>
                    <p class="landing-gallery-text">Écologie et innovation</p>
                </div>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer class="landing-footer">
        <p class="landing-footer-text">
            &copy; 2024 Location - Système de Gestion de Véhicules. Tous droits réservés.
        </p>
    </footer>

    <script>
        // Smooth scroll for anchor links
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            });
        });
    </script>
</body>
</html>
