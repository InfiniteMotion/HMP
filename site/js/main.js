/* ============================================================
   HMP — Hearable Music Player  Product Site JS
   Canvas dynamic background + interactions
   ============================================================ */

(function () {
  'use strict';

  // ── Canvas Dynamic Background ─────────────────────────────
  var canvas = document.getElementById('bgCanvas');
  if (!canvas) return;

  var ctx = canvas.getContext('2d');
  var w, h, dpr;
  var mode = 'fluid'; // fluid | spots | blur
  var t = 0;
  var raf;

  function resize() {
    dpr = Math.min(window.devicePixelRatio || 1, 2); // cap for perf
    w = window.innerWidth;
    h = window.innerHeight;
    canvas.width = w * dpr;
    canvas.height = h * dpr;
    canvas.style.width = w + 'px';
    canvas.style.height = h + 'px';
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.scale(dpr, dpr);
  }

  window.addEventListener('resize', resize);
  resize();

  // ── FLUID mode: flowing aurora blobs ─────────────────────
  var fluidBlobs = [
    { x: 0.3, y: 0.4, r: 0.35, vx: 0.0003, vy: 0.0002, color: '201,44,44', alpha: 0.30 },
    { x: 0.6, y: 0.3, r: 0.30, vx:-0.0004, vy: 0.0003, color: '0,47,167', alpha: 0.28 },
    { x: 0.5, y: 0.6, r: 0.32, vx: 0.0002, vy:-0.0003, color: '229,115,115', alpha: 0.25 },
    { x: 0.2, y: 0.7, r: 0.25, vx:-0.0003, vy:-0.0002, color: '144,202,249', alpha: 0.25 },
    { x: 0.8, y: 0.5, r: 0.28, vx:-0.0002, vy: 0.0004, color: '201,44,44', alpha: 0.22 },
    { x: 0.4, y: 0.2, r: 0.22, vx: 0.0005, vy:-0.0001, color: '244,143,177', alpha: 0.18 },
  ];

  function drawFluid(elapsed) {
    ctx.clearRect(0, 0, w, h);

    fluidBlobs.forEach(function (b) {
      b.x += b.vx * elapsed * 0.05;
      b.y += b.vy * elapsed * 0.05;

      // Bounce
      if (b.x < -0.3) b.x = 1.3;
      if (b.x > 1.3) b.x = -0.3;
      if (b.y < -0.3) b.y = 1.3;
      if (b.y > 1.3) b.y = -0.3;

      var cx = b.x * w;
      var cy = b.y * h;
      var rx = b.r * w * 1.3;
      var ry = b.r * h * 1.1;

      var grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, Math.max(rx, ry));
      grad.addColorStop(0, 'rgba(' + b.color + ',' + b.alpha + ')');
      grad.addColorStop(0.6, 'rgba(' + b.color + ',' + (b.alpha * 0.3) + ')');
      grad.addColorStop(1, 'rgba(' + b.color + ',0)');

      ctx.fillStyle = grad;
      ctx.beginPath();
      ctx.ellipse(cx, cy, rx, ry, 0, 0, Math.PI * 2);
      ctx.fill();
    });
  }

  // ── SPOTS mode: floating colored orbs ────────────────────
  var spots = [];
  var spotColors = [
    '201,44,44', '0,47,167', '229,115,115', '144,202,249',
    '201,44,44', '244,143,177', '0,47,167', '229,115,115',
  ];

  function initSpots() {
    spots.length = 0;
    var count = Math.max(18, Math.floor((w * h) / 40000));
    for (var i = 0; i < count; i++) {
      spots.push({
        x: Math.random() * w,
        y: Math.random() * h,
        r: Math.random() * 40 + 10,
        vx: (Math.random() - 0.5) * 1.2,
        vy: (Math.random() - 0.5) * 1.2,
        color: spotColors[Math.floor(Math.random() * spotColors.length)],
        alpha: Math.random() * 0.25 + 0.1,
        phase: Math.random() * Math.PI * 2,
      });
    }
  }
  initSpots();
  window.addEventListener('resize', initSpots);

  function drawSpots(elapsed) {
    ctx.clearRect(0, 0, w, h);

    spots.forEach(function (s) {
      s.x += s.vx * elapsed * 0.03;
      s.y += s.vy * elapsed * 0.03;
      s.phase += 0.0005 * elapsed;

      if (s.x < -80) s.x = w + 80;
      if (s.x > w + 80) s.x = -80;
      if (s.y < -80) s.y = h + 80;
      if (s.y > h + 80) s.y = -80;

      var pulse = 1 + Math.sin(s.phase) * 0.2;
      var r = s.r * pulse;

      var grad = ctx.createRadialGradient(s.x, s.y, 0, s.x, s.y, r);
      grad.addColorStop(0, 'rgba(' + s.color + ',' + s.alpha + ')');
      grad.addColorStop(1, 'rgba(' + s.color + ',0)');

      ctx.fillStyle = grad;
      ctx.beginPath();
      ctx.arc(s.x, s.y, r, 0, Math.PI * 2);
      ctx.fill();
    });
  }

  // ── BLUR mode: large slow blurry blobs ───────────────────
  var blurBlobs = [
    { x: 0.25, y: 0.45, r: 0.4, vx: 0.00015, vy: 0.0001, color: '201,44,44', alpha: 0.18 },
    { x: 0.65, y: 0.35, r: 0.35, vx:-0.0002, vy: 0.00015, color: '244,143,177', alpha: 0.16 },
    { x: 0.45, y: 0.65, r: 0.38, vx: 0.0001, vy:-0.0002, color: '0,47,167', alpha: 0.18 },
    { x: 0.75, y: 0.55, r: 0.32, vx:-0.0003, vy: 0.00005, color: '144,202,249', alpha: 0.15 },
  ];

  function drawBlur(elapsed) {
    ctx.clearRect(0, 0, w, h);

    blurBlobs.forEach(function (b) {
      b.x += b.vx * elapsed * 0.03;
      b.y += b.vy * elapsed * 0.03;
      if (b.x < -0.3) b.x = 1.3; if (b.x > 1.3) b.x = -0.3;
      if (b.y < -0.3) b.y = 1.3; if (b.y > 1.3) b.y = -0.3;

      var cx = b.x * w;
      var cy = b.y * h;
      var rx = b.r * w * 1.2;
      var ry = b.r * h * 1.0;

      var grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, Math.max(rx, ry));
      grad.addColorStop(0, 'rgba(' + b.color + ',' + b.alpha + ')');
      grad.addColorStop(0.7, 'rgba(' + b.color + ',' + (b.alpha * 0.15) + ')');
      grad.addColorStop(1, 'rgba(' + b.color + ',0)');

      ctx.fillStyle = grad;
      ctx.filter = 'blur(40px)';
      ctx.beginPath();
      ctx.ellipse(cx, cy, rx, ry, 0, 0, Math.PI * 2);
      ctx.fill();
      ctx.filter = 'none';
    });
  }

  // ── Main loop ─────────────────────────────────────────────
  var lastTime = 0;

  function loop(timestamp) {
    if (!lastTime) lastTime = timestamp;
    var elapsed = Math.min(timestamp - lastTime, 50); // cap delta
    lastTime = timestamp;

    switch (mode) {
      case 'fluid': drawFluid(elapsed); break;
      case 'spots': drawSpots(elapsed); break;
      case 'blur':  drawBlur(elapsed);  break;
    }

    raf = requestAnimationFrame(loop);
  }

  raf = requestAnimationFrame(loop);

  // ── Theme mode switcher ───────────────────────────────────
  var modeBtns = document.querySelectorAll('.theme-mode-btn');
  modeBtns.forEach(function (btn) {
    btn.addEventListener('click', function () {
      modeBtns.forEach(function (b) { b.classList.remove('active'); });
      btn.classList.add('active');
      mode = btn.getAttribute('data-mode');
      if (mode === 'spots') initSpots();
    });
  });

  // ── Preview screenshot switcher ────────────────────────────
  var previewNav = document.getElementById('previewNav');
  var previewImg = document.getElementById('previewImg');

  if (previewNav && previewImg) {
    previewNav.addEventListener('click', function (e) {
      var btn = e.target.closest('.preview-nav-item');
      if (!btn) return;

      previewNav.querySelectorAll('.preview-nav-item').forEach(function (b) { b.classList.remove('active'); });
      btn.classList.add('active');

      var newSrc = btn.getAttribute('data-src');
      if (previewImg.src.indexOf(newSrc) !== -1) return;

      previewImg.classList.add('switching');
      setTimeout(function () {
        previewImg.src = newSrc;
        previewImg.classList.remove('switching');
      }, 150);
    });
  }

  // ── Nav scroll ────────────────────────────────────────────
  var nav = document.getElementById('nav');
  var ticking = false;

  function updateNav() {
    if (window.scrollY > 50) nav.classList.add('scrolled');
    else nav.classList.remove('scrolled');
    ticking = false;
  }

  window.addEventListener('scroll', function () {
    if (!ticking) { requestAnimationFrame(updateNav); ticking = true; }
  });

  // ── Scroll reveal ─────────────────────────────────────────
  var revealEls = document.querySelectorAll('.feature-card, .philo-card, .decision-card, .platform-card, .theme-feature');
  var revealObserver = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (entry.isIntersecting) {
        entry.target.style.opacity = '1';
        entry.target.style.transform = 'translateY(0)';
        revealObserver.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1, rootMargin: '0px 0px -20px 0px' });

  revealEls.forEach(function (el, i) {
    el.style.opacity = '0';
    el.style.transform = 'translateY(20px)';
    el.style.transition = 'opacity 0.6s ease-out, transform 0.6s ease-out';
    el.style.transitionDelay = (i % 6) * 60 + 'ms';
    revealObserver.observe(el);
  });

  // ── Download tabs ─────────────────────────────────────────
  var tabs = document.querySelectorAll('.download-tab');
  var panels = document.querySelectorAll('.download-panel');

  tabs.forEach(function (tab) {
    tab.addEventListener('click', function () {
      var platform = tab.getAttribute('data-platform');
      tabs.forEach(function (t) { t.classList.remove('active'); });
      tab.classList.add('active');
      panels.forEach(function (p) {
        p.classList.remove('active');
        if (p.getAttribute('data-platform') === platform) p.classList.add('active');
      });
    });
  });

  // OS detection for default download tab
  (function () {
    var ua = navigator.userAgent || '';
    var platform = 'android';
    if (ua.indexOf('Mac') !== -1 && ua.indexOf('Android') === -1) platform = 'macos';
    else if (ua.indexOf('Win') !== -1 && ua.indexOf('Android') === -1) platform = 'windows';
    else if (ua.indexOf('Linux') !== -1 && ua.indexOf('Android') === -1) platform = 'linux';

    tabs.forEach(function (t) { t.classList.remove('active'); });
    panels.forEach(function (p) { p.classList.remove('active'); });

    document.querySelector('.download-tab[data-platform="' + platform + '"]').classList.add('active');
    document.querySelector('.download-panel[data-platform="' + platform + '"]').classList.add('active');
  })();

})();
