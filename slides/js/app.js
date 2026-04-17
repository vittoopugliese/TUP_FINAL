const TOTAL_SLIDES = 16;
const SLIDE_WIDTH = 1280;
const SLIDE_HEIGHT = 720;

const slideStyleEl = document.getElementById("slide-dynamic-style");
const slideMount = document.getElementById("slide-mount");
const stageScale = document.getElementById("stage-scale");
const viewport = document.getElementById("viewport");

const btnPrev = document.getElementById("nav-prev");
const btnNext = document.getElementById("nav-next");
const btnFs = document.getElementById("nav-fs");
const counterEl = document.getElementById("nav-counter");

let currentIndex = 1;
const slideCache = new Map();

function parseSlideFromHash() {
    const h = window.location.hash.replace(/^#/, "");
    const n = parseInt(h, 10);
    if (Number.isFinite(n) && n >= 1 && n <= TOTAL_SLIDES) return n;
    return 1;
}

function setHash(n) {
    const next = `#${n}`;
    if (window.location.hash !== next) window.location.hash = next;
}

function updateScale() {
    const sx = window.innerWidth / SLIDE_WIDTH;
    const sy = window.innerHeight / SLIDE_HEIGHT;
    const s = Math.min(sx, sy);
    stageScale.style.transform = `scale(${s})`;
}

function updateNav() {
    btnPrev.disabled = currentIndex <= 1;
    btnNext.disabled = currentIndex >= TOTAL_SLIDES;
    counterEl.innerHTML = `<strong>${currentIndex}</strong> / ${TOTAL_SLIDES}`;
}

async function loadSlideModule(n) {
    if (slideCache.has(n)) return slideCache.get(n);
    const mod = await import(`./slides/slide${n}.js`);
    slideCache.set(n, mod);
    return mod;
}

async function showSlide(n) {
    if (n < 1 || n > TOTAL_SLIDES) return;
    currentIndex = n;
    const mod = await loadSlideModule(n);
    slideStyleEl.textContent = mod.styles;
    slideMount.innerHTML = mod.html;
    document.title = mod.meta.title;
    updateNav();
    updateScale();
}

function goDelta(delta) {
    const next = currentIndex + delta;
    if (next < 1 || next > TOTAL_SLIDES) return;
    setHash(next);
}

function onHashChange() {
    const n = parseSlideFromHash();
    if (n !== currentIndex) showSlide(n);
}

function onKeyDown(e) {
    if (e.target && (e.target.tagName === "INPUT" || e.target.tagName === "TEXTAREA")) return;
    switch (e.key) {
        case "ArrowLeft":
        case "PageUp":
            e.preventDefault();
            goDelta(-1);
            break;
        case "ArrowRight":
        case "PageDown":
        case " ":
            e.preventDefault();
            goDelta(1);
            break;
        case "Home":
            e.preventDefault();
            setHash(1);
            break;
        case "End":
            e.preventDefault();
            setHash(TOTAL_SLIDES);
            break;
        default:
            break;
    }
}

btnPrev.addEventListener("click", () => goDelta(-1));
btnNext.addEventListener("click", () => goDelta(1));

btnFs.addEventListener("click", async () => {
    try {
        if (!document.fullscreenElement) {
            await viewport.requestFullscreen();
            btnFs.innerHTML = '<i class="fas fa-compress" aria-hidden="true"></i>';
        } else {
            await document.exitFullscreen();
            btnFs.innerHTML = '<i class="fas fa-expand" aria-hidden="true"></i>';
        }
    } catch {
        /* ignore */
    }
});

document.addEventListener("fullscreenchange", () => {
    if (!document.fullscreenElement) {
        btnFs.innerHTML = '<i class="fas fa-expand" aria-hidden="true"></i>';
    }
    updateScale();
});

window.addEventListener("resize", updateScale);
window.addEventListener("hashchange", onHashChange);
window.addEventListener("keydown", onKeyDown);

function init() {
    currentIndex = parseSlideFromHash();
    if (!window.location.hash) {
        history.replaceState(null, "", `#${currentIndex}`);
    }
    showSlide(currentIndex);
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}
