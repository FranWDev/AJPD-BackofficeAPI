document.addEventListener("DOMContentLoaded", () => {
  // INICIO MENU MOVIL
  const mobileMenuBtn = document.querySelector(".mobile-menu-btn");
  const mobileNav = document.querySelector(".mobile-nav");
  const mobileDropdownBtns = document.querySelectorAll(".mobile-dropdown-btn");
  const body = document.body;

  const overlay = document.createElement("div");
  overlay.className = "mobile-nav-overlay";
  document.body.appendChild(overlay);

  mobileMenuBtn.addEventListener("click", () => {
    const isExpanded = mobileMenuBtn.getAttribute("aria-expanded") === "true";
    mobileMenuBtn.setAttribute("aria-expanded", !isExpanded);
    mobileMenuBtn.classList.toggle("active");
    mobileNav.classList.toggle("active");
    overlay.classList.toggle("active");
    body.style.overflow = isExpanded ? "" : "hidden";
  });

  overlay.addEventListener("click", () => {
    mobileNav.classList.remove("active");
    mobileMenuBtn.classList.remove("active");
    overlay.classList.remove("active");
    mobileMenuBtn.setAttribute("aria-expanded", "false");
    body.style.overflow = "";
  });

  mobileDropdownBtns.forEach((btn) => {
    btn.addEventListener("click", () => {
      const isExpanded = btn.getAttribute("aria-expanded") === "true";
      btn.setAttribute("aria-expanded", !isExpanded);
      const dropdownContent = btn.nextElementSibling;

      if (!isExpanded) {
        dropdownContent.style.display = "block";
        dropdownContent.style.maxHeight = "0";
        dropdownContent.classList.add("active");
        requestAnimationFrame(() => {
          dropdownContent.style.maxHeight = dropdownContent.scrollHeight + "px";
        });
      } else {
        dropdownContent.style.maxHeight = "0";
        dropdownContent.addEventListener("transitionend", function handler() {
          if (dropdownContent.style.maxHeight === "0px") {
            dropdownContent.classList.remove("active");
            dropdownContent.style.display = "";
            dropdownContent.style.maxHeight = "";
            dropdownContent.removeEventListener("transitionend", handler);
          }
        });
      }

      const icon = btn.querySelector(".fa-chevron-down");
      icon.style.transform = isExpanded ? "rotate(0)" : "rotate(180deg)";
      icon.style.transition =
        "transform 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55)";
    });
  });

  const mobileLinks = document.querySelectorAll(".mobile-nav a");
  mobileLinks.forEach((link) => {
    link.addEventListener("click", () => {
      mobileNav.classList.remove("active");
      mobileMenuBtn.classList.remove("active");
      mobileMenuBtn.setAttribute("aria-expanded", "false");
      body.style.overflow = "";
    });
  });

  // Observador de intersección para animaciones
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("animate-fadeIn");
          observer.unobserve(entry.target);
        }
      });
    },
    {
      threshold: 0.1,
    }
  );

  document.querySelectorAll(".footer-section").forEach((section) => {
    observer.observe(section);
  });

  function initHeroSlider() {
    const slides = document.querySelectorAll(".hero-slide");
    let currentSlide = 0;

    slides[0].classList.add("active");

    setInterval(() => {
      slides[currentSlide].classList.remove("active");
      currentSlide = (currentSlide + 1) % slides.length;
      slides[currentSlide].classList.add("active");
    }, 5000);
  }

  initHeroSlider();
});
