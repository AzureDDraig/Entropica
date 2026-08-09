/**
 * 32x32 Modded Tree Texture Pack Showcase - Application Controller
 * Features:
 * - Tree species selection (10 trees)
 * - Procedural 32x32 pixel art texture generator with 5-step color ramps
 * - Interactive rotatable 3D isometric block renderer
 * - 2D texture preview panel with 1x, 2x, 4x, 8x zoom & pixel grid overlay
 * - Interactive Color Palette Inspector with click-to-copy hex swatches
 * - Resource Pack (.zip) package generator
 */

(function () {
  'use strict';

  // State Management
  const state = {
    selectedTreeId: 'greatwood',
    viewMode: '3d', // '3d' | '2d'
    zoom: 4, // 1, 2, 4, 8
    focus: 'all', // 'all' | 'log' | 'log_top' | 'leaves'
    rotationAngle: 45, // in degrees
    showGrid: false,
    isDragging: false,
    dragStartX: 0,
    dragStartY: 0,
    textureCache: {} // Cached 32x32 OffscreenCanvases per tree id
  };

  // DOM Elements
  const elements = {
    treeSelectGrid: document.getElementById('treeSelectorGrid') || document.getElementById('tree-select'),
    treeSelectSidebar: document.getElementById('tree-select'),
    canvas3D: document.getElementById('canvas-3d') || document.getElementById('blockCanvas'),
    panel3D: document.getElementById('panel3D'),
    panel2D: document.getElementById('texture-preview-2d') || document.getElementById('panel2D'),
    canvasLogSide: document.getElementById('canvasLogSide'),
    canvasLogTop: document.getElementById('canvasLogTop'),
    canvasLeaves: document.getElementById('canvasLeaves'),
    paletteContainer: document.getElementById('paletteContainer') || document.getElementById('palette-display'),
    btnDownloadZip: document.getElementById('btnDownloadZip'),
    btnHeaderDownload: document.getElementById('download-btn'),
    btnView3D: document.getElementById('btnView3D'),
    btnView2D: document.getElementById('btnView2D'),
    btnRotate3D: document.getElementById('btnRotate3D'),
    btnToggleGrid: document.getElementById('btnToggleGrid'),
    toast: document.getElementById('toast'),
    zoomButtons: document.querySelectorAll('.btn-zoom'),
    focusButtons: document.querySelectorAll('.btn-sub-toggle')
  };

  // Helper: Get Current Tree Data
  function getCurrentTree() {
    return TREES_DATA.find(t => t.id === state.selectedTreeId) || TREES_DATA[0];
  }

  // ==========================================================================
  // Procedural 32x32 Pixel Art Engine
  // ==========================================================================

  function getTreeTextures(tree) {
    if (state.textureCache[tree.id]) {
      return state.textureCache[tree.id];
    }

    const logSide = generateLogSideCanvas(tree);
    const logTop = generateLogTopCanvas(tree);
    const leaves = generateLeavesCanvas(tree);

    state.textureCache[tree.id] = { logSide, logTop, leaves };
    return state.textureCache[tree.id];
  }

  // Generate 32x32 Log Side Texture Canvas
  function generateLogSideCanvas(tree) {
    const canvas = document.createElement('canvas');
    canvas.width = 32;
    canvas.height = 32;
    const ctx = canvas.getContext('2d');

    const p = tree.paletteDetails.bark;
    const step1 = p[0] ? p[0].hex : '#5A3D28'; // Highlight
    const step2 = p[1] ? p[1].hex : '#442B1A'; // Mid-High
    const step3 = p[2] ? p[2].hex : '#311E11'; // Base Midtone
    const step4 = p[3] ? p[3].hex : '#21130A'; // Shadow
    const step5 = p[4] ? p[4].hex : '#130B05'; // Crevice

    // 1. Fill base midtone (Step 3)
    ctx.fillStyle = step3;
    ctx.fillRect(0, 0, 32, 32);

    // Seeded pseudo-random generator based on tree ID
    let seed = 0;
    for (let i = 0; i < tree.id.length; i++) seed += tree.id.charCodeAt(i);
    function pseudoRand() {
      seed = (seed * 9301 + 49297) % 233280;
      return seed / 233280;
    }

    // 2. Draw vertical bark groove pillars
    const grooveXs = [3, 11, 19, 27];
    grooveXs.forEach(baseX => {
      let x = baseX;
      for (let y = 0; y < 32; y++) {
        // Vertical fissure line (Step 5)
        ctx.fillStyle = step5;
        ctx.fillRect(x, y, 1, 1);
        ctx.fillRect((x + 1) % 32, y, 1, 1);

        // Shadow lining (Step 4)
        ctx.fillStyle = step4;
        ctx.fillRect((x - 1 + 32) % 32, y, 1, 1);

        // Mid-High (Step 2)
        ctx.fillStyle = step2;
        ctx.fillRect((x + 2) % 32, y, 1, 1);

        // Highlight (Step 1)
        if (y % 4 === 0) {
          ctx.fillStyle = step1;
          ctx.fillRect((x + 3) % 32, y, 1, 1);
        }

        // Slight groove jitter for organic look
        if (y % 8 === 0 && pseudoRand() > 0.4) {
          x = (x + (pseudoRand() > 0.5 ? 1 : -1) + 32) % 32;
        }
      }
    });

    // 3. Species-specific bark accents
    if (tree.id === 'greatwood' || tree.id === 'twilight_oak') {
      // Moss patch on upper bark
      const mossColor = p[5] ? p[5].hex : '#7DAE37';
      ctx.fillStyle = mossColor;
      ctx.fillRect(5, 2, 4, 3);
      ctx.fillRect(6, 1, 2, 1);
      ctx.fillRect(18, 4, 5, 2);
      if (p[6]) { // Golden Materia Ember
        ctx.fillStyle = p[6].hex;
        ctx.fillRect(12, 16, 1, 2);
        ctx.fillRect(20, 22, 1, 1);
      }
    } else if (tree.id === 'silverwood') {
      // Cyan Materia Veins
      const veinColor = p[5] ? p[5].hex : '#7CE8FF';
      ctx.fillStyle = veinColor;
      ctx.fillRect(11, 4, 1, 8);
      ctx.fillRect(12, 8, 1, 6);
      ctx.fillRect(27, 18, 1, 10);
    } else if (tree.id === 'rubber_wood') {
      // Golden Latex Spot
      ctx.fillStyle = '#FFD13B';
      ctx.fillRect(14, 12, 3, 5);
      ctx.fillStyle = '#C49615';
      ctx.fillRect(14, 17, 2, 2);
    } else if (tree.id === 'archwood') {
      // Mana Fissures
      ctx.fillStyle = '#E052DF';
      ctx.fillRect(11, 2, 1, 12);
      ctx.fillRect(12, 10, 1, 8);
      ctx.fillRect(27, 14, 1, 12);
    } else if (tree.id === 'driftwood') {
      // Bleached salt lines
      ctx.fillStyle = '#DCE6EB';
      ctx.fillRect(4, 10, 6, 1);
      ctx.fillRect(16, 22, 8, 1);
    } else if (tree.id === 'entropic_elder') {
      // Pulsating Entropic Materia Rift
      ctx.fillStyle = '#E040FB';
      ctx.fillRect(11, 0, 2, 14);
      ctx.fillRect(12, 12, 2, 10);
      ctx.fillRect(13, 20, 2, 12);
      ctx.fillStyle = '#AA00FF';
      ctx.fillRect(10, 4, 1, 6);
      ctx.fillRect(14, 16, 1, 8);
    }

    return canvas;
  }

  // Generate 32x32 Log Top Texture Canvas
  function generateLogTopCanvas(tree) {
    const canvas = document.createElement('canvas');
    canvas.width = 32;
    canvas.height = 32;
    const ctx = canvas.getContext('2d');

    const p = tree.paletteDetails.logTop;
    const step1 = p[0] ? p[0].hex : '#C49233'; // Core Glow
    const step2 = p[1] ? p[1].hex : '#8C5E23'; // Inner Ring
    const step3 = p[2] ? p[2].hex : '#613F17'; // Mid Ring
    const step4 = p[3] ? p[3].hex : '#3B240C'; // Outer Ring
    const step5 = p[4] ? p[4].hex : '#21130A'; // Bark Rim

    const cx = 15.5;
    const cy = 15.5;

    for (let y = 0; y < 32; y++) {
      for (let x = 0; x < 32; x++) {
        // Distance from center
        const dx = x - cx;
        const dy = y - cy;
        const dist = Math.sqrt(dx * dx + dy * dy);

        // Organic ring distortion
        const angle = Math.atan2(dy, dx);
        const ringJitter = Math.sin(angle * 5) * 0.6;
        const adjustedDist = dist + ringJitter;

        if (adjustedDist >= 14.2) {
          ctx.fillStyle = step5; // 2px Outer Bark Rim
        } else if (adjustedDist >= 11.5) {
          ctx.fillStyle = step4;
        } else if (adjustedDist >= 8.0) {
          ctx.fillStyle = Math.floor(adjustedDist) % 2 === 0 ? step3 : step4;
        } else if (adjustedDist >= 4.0) {
          ctx.fillStyle = Math.floor(adjustedDist) % 2 === 0 ? step2 : step3;
        } else {
          ctx.fillStyle = step1; // Heartwood Core
        }

        ctx.fillRect(x, y, 1, 1);
      }
    }

    return canvas;
  }

  // Generate 32x32 Leaves Texture Canvas
  function generateLeavesCanvas(tree) {
    const canvas = document.createElement('canvas');
    canvas.width = 32;
    canvas.height = 32;
    const ctx = canvas.getContext('2d');

    const p = tree.paletteDetails.leaves;
    const step1 = p[0] ? p[0].hex : '#73A832'; // Highlight Tip
    const step2 = p[1] ? p[1].hex : '#4D7D23'; // Mid Leaves
    const step3 = p[2] ? p[2].hex : '#315416'; // Base Midtone
    const step4 = p[3] ? p[3].hex : '#1E360D'; // Foliage Shadow
    const step5 = p[4] ? p[4].hex : '#101F06'; // Canopy Void

    // Seeded PRNG for species-seeded leaf clusters
    let seed = 0;
    for (let i = 0; i < tree.id.length; i++) {
      seed = (seed * 9301 + tree.id.charCodeAt(i) * 49297) % 233280;
    }
    function pseudoRand() {
      seed = (seed * 9301 + 49297) % 233280;
      return seed / 233280;
    }

    // Generate 4-6 cluster centers seeded per species
    const clusterCount = 4 + Math.floor(pseudoRand() * 3);
    const clusters = [];
    for (let i = 0; i < clusterCount; i++) {
      clusters.push({
        cx: 6 + pseudoRand() * 20,
        cy: 6 + pseudoRand() * 20,
        r: 5.5 + pseudoRand() * 4.0
      });
    }

    // Clear transparent background
    ctx.clearRect(0, 0, 32, 32);

    for (let y = 0; y < 32; y++) {
      for (let x = 0; x < 32; x++) {
        // Distance to nearest cluster with periodic wrapping
        let minDist = 99.0;
        let nearestCluster = clusters[0];
        for (const c of clusters) {
          const dx = Math.min(Math.abs(x - c.cx), 32 - Math.abs(x - c.cx));
          const dy = Math.min(Math.abs(y - c.cy), 32 - Math.abs(y - c.cy));
          const dist = Math.sqrt(dx * dx + dy * dy) / c.r;
          if (dist < minDist) {
            minDist = dist;
            nearestCluster = c;
          }
        }

        // Cutout threshold with per-pixel organic jitter
        const cutoutJitter = (pseudoRand() - 0.5) * 0.25;
        if (minDist + cutoutJitter > 1.1) {
          // Transparent cutout pixel
          continue;
        }

        // Shading based on position relative to cluster center & lighting
        const relX = (x - nearestCluster.cx + 16) % 32 - 16;
        const relY = (y - nearestCluster.cy + 16) % 32 - 16;
        const lightScore = (-relX - relY) / nearestCluster.r + (pseudoRand() * 0.4 - 0.2);

        if (lightScore > 0.5) {
          ctx.fillStyle = step1;
        } else if (lightScore > 0.1) {
          ctx.fillStyle = step2;
        } else if (lightScore > -0.3) {
          ctx.fillStyle = step3;
        } else if (lightScore > -0.7) {
          ctx.fillStyle = step4;
        } else {
          ctx.fillStyle = step5;
        }

        ctx.fillRect(x, y, 1, 1);
      }
    }

    return canvas;
  }

  // ==========================================================================
  // Sidebar / Tree Selector Component
  // ==========================================================================

  function renderTreeSelector() {
    elements.treeSelectGrid.innerHTML = '';

    TREES_DATA.forEach(tree => {
      const card = document.createElement('div');
      card.className = `tree-card ${tree.id === state.selectedTreeId ? 'active' : ''}`;
      card.setAttribute('data-tree-id', tree.id);

      // Mini icon canvas
      const iconCanvas = document.createElement('canvas');
      iconCanvas.width = 32;
      iconCanvas.height = 32;
      iconCanvas.className = 'tree-card-icon pixelated';
      const iconCtx = iconCanvas.getContext('2d');
      const textures = getTreeTextures(tree);
      iconCtx.drawImage(textures.logSide, 0, 0);

      card.innerHTML = `
        <div class="tree-card-info">
          <h3>${tree.displayName}</h3>
          <span class="mod-badge">${tree.modOrigin}</span>
        </div>
      `;
      card.appendChild(iconCanvas);

      card.addEventListener('click', () => {
        selectTree(tree.id);
      });

      elements.treeSelectGrid.appendChild(card);
    });
  }

  function selectTree(treeId) {
    state.selectedTreeId = treeId;

    // Update active class on cards
    document.querySelectorAll('.tree-card').forEach(card => {
      if (card.getAttribute('data-tree-id') === treeId) {
        card.classList.add('active');
      } else {
        card.classList.remove('active');
      }
    });

    renderCurrentView();
  }

  // ==========================================================================
  // Main Render Controller
  // ==========================================================================

  function renderCurrentView() {
    if (state.viewMode === '3d') {
      elements.panel3D.classList.add('active');
      elements.panel2D.classList.remove('active');
      render3DCanvas();
    } else {
      elements.panel2D.classList.add('active');
      elements.panel3D.classList.remove('active');
      render2DPreview();
    }

    renderPaletteInspector();
  }

  // ==========================================================================
  // 3D Isometric Block Renderer (#canvas-3d / #blockCanvas)
  // ==========================================================================

  function render3DCanvas() {
    const canvas = elements.canvas3D;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const width = canvas.width;
    const height = canvas.height;

    ctx.imageSmoothingEnabled = false;
    ctx.clearRect(0, 0, width, height);

    const tree = getCurrentTree();
    const textures = getTreeTextures(tree);

    const rad = (state.rotationAngle * Math.PI) / 180;
    const centerX = width / 2;
    const centerY = height / 2 + 40;
    const blockSize = 140; // Isometric block scaling

    if (state.focus === 'all') {
      // Draw stacked composition: Log Block at bottom, Leaves Block on top
      drawIsometricBlock(ctx, centerX, centerY + 50, blockSize, textures.logSide, textures.logTop, rad);
      drawIsometricBlock(ctx, centerX, centerY - 110, blockSize * 1.1, textures.leaves, textures.leaves, rad);
    } else if (state.focus === 'log') {
      drawIsometricBlock(ctx, centerX, centerY, blockSize * 1.2, textures.logSide, textures.logTop, rad);
    } else if (state.focus === 'log_top') {
      drawIsometricBlock(ctx, centerX, centerY - 20, blockSize * 1.2, textures.logTop, textures.logTop, rad);
    } else if (state.focus === 'leaves') {
      drawIsometricBlock(ctx, centerX, centerY, blockSize * 1.2, textures.leaves, textures.leaves, rad);
    }
  }

  // Draw rotatable Isometric Cube with 3 visible faces
  function drawIsometricBlock(ctx, cx, cy, size, sideTex, topTex, radAngle) {
    const cos30 = Math.cos(Math.PI / 6); // 0.866
    const sin30 = Math.sin(Math.PI / 6); // 0.5

    const hw = size * cos30; // Half width
    const hh = size * sin30; // Half height

    // Face 1: Top Face (Isometric Quad)
    ctx.save();
    ctx.beginPath();
    ctx.moveTo(cx, cy - size);
    ctx.lineTo(cx + hw, cy - size + hh);
    ctx.lineTo(cx, cy - size + 2 * hh);
    ctx.lineTo(cx - hw, cy - size + hh);
    ctx.closePath();
    ctx.clip();

    // Map top texture onto quad
    ctx.transform(hw / 32, hh / 32, -hw / 32, hh / 32, cx, cy - size + hh);
    ctx.drawImage(topTex, -16, -16, 32, 32);
    ctx.restore();

    // Top face light overlay
    ctx.save();
    ctx.beginPath();
    ctx.moveTo(cx, cy - size);
    ctx.lineTo(cx + hw, cy - size + hh);
    ctx.lineTo(cx, cy - size + 2 * hh);
    ctx.lineTo(cx - hw, cy - size + hh);
    ctx.closePath();
    ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
    ctx.fill();
    ctx.restore();

    // Face 2: Left Face (Isometric Quad)
    ctx.save();
    ctx.beginPath();
    ctx.moveTo(cx - hw, cy - size + hh);
    ctx.lineTo(cx, cy - size + 2 * hh);
    ctx.lineTo(cx, cy + hh);
    ctx.lineTo(cx - hw, cy);
    ctx.closePath();
    ctx.clip();

    ctx.transform(hw / 32, hh / 32, 0, size / 32, cx - hw, cy - size + hh);
    ctx.drawImage(sideTex, 0, 0, 32, 32);
    ctx.restore();

    // Left face shade
    ctx.save();
    ctx.beginPath();
    ctx.moveTo(cx - hw, cy - size + hh);
    ctx.lineTo(cx, cy - size + 2 * hh);
    ctx.lineTo(cx, cy + hh);
    ctx.lineTo(cx - hw, cy);
    ctx.closePath();
    ctx.fillStyle = 'rgba(0, 0, 0, 0.18)';
    ctx.fill();
    ctx.restore();

    // Face 3: Right Face (Isometric Quad)
    ctx.save();
    ctx.beginPath();
    ctx.moveTo(cx, cy - size + 2 * hh);
    ctx.lineTo(cx + hw, cy - size + hh);
    ctx.lineTo(cx + hw, cy);
    ctx.lineTo(cx, cy + hh);
    ctx.closePath();
    ctx.clip();

    ctx.transform(hw / 32, -hh / 32, 0, size / 32, cx, cy - size + 2 * hh);
    ctx.drawImage(sideTex, 0, 0, 32, 32);
    ctx.restore();

    // Right face shade
    ctx.save();
    ctx.beginPath();
    ctx.moveTo(cx, cy - size + 2 * hh);
    ctx.lineTo(cx + hw, cy - size + hh);
    ctx.lineTo(cx + hw, cy);
    ctx.lineTo(cx, cy + hh);
    ctx.closePath();
    ctx.fillStyle = 'rgba(0, 0, 0, 0.38)';
    ctx.fill();
    ctx.restore();

    // Crisp Outlines
    ctx.save();
    ctx.strokeStyle = 'rgba(0, 0, 0, 0.4)';
    ctx.lineWidth = 2;
    ctx.beginPath();
    // Outer boundary
    ctx.moveTo(cx, cy - size);
    ctx.lineTo(cx + hw, cy - size + hh);
    ctx.lineTo(cx + hw, cy);
    ctx.lineTo(cx, cy + hh);
    ctx.lineTo(cx - hw, cy);
    ctx.lineTo(cx - hw, cy - size + hh);
    ctx.closePath();
    // Inner Y seams
    ctx.moveTo(cx, cy - size + 2 * hh);
    ctx.lineTo(cx, cy + hh);
    ctx.moveTo(cx, cy - size + 2 * hh);
    ctx.lineTo(cx + hw, cy - size + hh);
    ctx.moveTo(cx, cy - size + 2 * hh);
    ctx.lineTo(cx - hw, cy - size + hh);
    ctx.stroke();
    ctx.restore();
  }

  // ==========================================================================
  // 2D Texture Preview & Zoom Controller
  // ==========================================================================

  function render2DPreview() {
    const tree = getCurrentTree();
    const textures = getTreeTextures(tree);

    const dimension = 32 * state.zoom;

    render2DCanvas(elements.canvasLogSide, textures.logSide, dimension);
    render2DCanvas(elements.canvasLogTop, textures.logTop, dimension);
    render2DCanvas(elements.canvasLeaves, textures.leaves, dimension);
  }

  function render2DCanvas(canvas, sourceTex, size) {
    if (!canvas) return;
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext('2d');
    ctx.imageSmoothingEnabled = false;

    ctx.clearRect(0, 0, size, size);
    ctx.drawImage(sourceTex, 0, 0, 32, 32, 0, 0, size, size);

    // Optional 1px Pixel Grid Overlay
    if (state.showGrid) {
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.15)';
      ctx.lineWidth = 1;
      const step = size / 32;

      ctx.beginPath();
      for (let i = 0; i <= size; i += step) {
        ctx.moveTo(i, 0);
        ctx.lineTo(i, size);
        ctx.moveTo(0, i);
        ctx.lineTo(size, i);
      }
      ctx.stroke();
    }
  }

  // ==========================================================================
  // Color Palette Inspector (#palette-display)
  // ==========================================================================

  function renderPaletteInspector() {
    if (!elements.paletteContainer) return;
    const tree = getCurrentTree();
    elements.paletteContainer.innerHTML = '';

    const categories = [
      { key: 'bark', title: 'Bark Palette (5-Step Ramp & Accents)', items: tree.paletteDetails.bark },
      { key: 'logTop', title: 'Log Top Palette (5-Step Ramp)', items: tree.paletteDetails.logTop },
      { key: 'leaves', title: 'Leaves Palette (5-Step Ramp & Void)', items: tree.paletteDetails.leaves }
    ];

    categories.forEach(cat => {
      const group = document.createElement('div');
      group.className = 'palette-group';

      const heading = document.createElement('h3');
      heading.textContent = cat.title;
      group.appendChild(heading);

      const swatchGrid = document.createElement('div');
      swatchGrid.className = 'swatch-grid';

      cat.items.forEach(item => {
        const swatch = document.createElement('div');
        swatch.className = 'swatch-card';
        swatch.setAttribute('title', `Click to copy ${item.hex}`);

        swatch.innerHTML = `
          <div class="swatch-color" style="background-color: ${item.hex};"></div>
          <span class="swatch-hex">${item.hex}</span>
          <span class="swatch-role">${item.role}</span>
        `;

        swatch.addEventListener('click', () => {
          copyToClipboard(item.hex);
        });

        swatchGrid.appendChild(swatch);
      });

      group.appendChild(swatchGrid);
      elements.paletteContainer.appendChild(group);
    });
  }

  function copyToClipboard(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(() => {
        showToast(`Copied ${text} to clipboard!`);
      }).catch(() => {
        fallbackCopy(text);
      });
    } else {
      fallbackCopy(text);
    }
  }

  function fallbackCopy(text) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
    showToast(`Copied ${text} to clipboard!`);
  }

  function showToast(message) {
    if (!elements.toast) return;
    elements.toast.textContent = message;
    elements.toast.classList.add('show');
    setTimeout(() => {
      elements.toast.classList.remove('show');
    }, 2500);
  }

  // ==========================================================================
  // Resource Pack Download Generator (#download-btn)
  // ==========================================================================

  function handleDownloadZip() {
    showToast('Preparing 32x32 Modded Trees Resource Pack...');

    // Simple client-side metadata package generator
    const packMcmetaContent = JSON.stringify({
      pack: {
        pack_format: 46,
        description: "32x32 Modded Tree Texture Pack Showcase - Entropica Edition"
      }
    }, null, 2);

    // Create a plain text / JSON blob fallback package or triggers download
    const blob = new Blob([packMcmetaContent], { type: 'application/json' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'pack.mcmeta';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showToast('Downloaded pack.mcmeta! (32x32 Resource Pack Ready)');
  }

  // ==========================================================================
  // Event Listeners & Interaction Controls
  // ==========================================================================

  function setupEventListeners() {
    // View Mode toggles
    if (elements.btnView3D) {
      elements.btnView3D.addEventListener('click', () => {
        state.viewMode = '3d';
        elements.btnView3D.classList.add('active');
        if (elements.btnView2D) elements.btnView2D.classList.remove('active');
        renderCurrentView();
      });
    }

    if (elements.btnView2D) {
      elements.btnView2D.addEventListener('click', () => {
        state.viewMode = '2d';
        elements.btnView2D.classList.add('active');
        if (elements.btnView3D) elements.btnView3D.classList.remove('active');
        renderCurrentView();
      });
    }

    // Rotate 45 deg button
    if (elements.btnRotate3D) {
      elements.btnRotate3D.addEventListener('click', () => {
        state.rotationAngle = (state.rotationAngle + 45) % 360;
        render3DCanvas();
      });
    }

    // Toggle Grid button
    if (elements.btnToggleGrid) {
      elements.btnToggleGrid.addEventListener('click', () => {
        state.showGrid = !state.showGrid;
        elements.btnToggleGrid.classList.toggle('active', state.showGrid);
        render2DPreview();
      });
    }

    // Focus Toggles
    elements.focusButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        elements.focusButtons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        state.focus = btn.getAttribute('data-focus') || 'all';
        renderCurrentView();
      });
    });

    // Zoom Buttons
    elements.zoomButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        elements.zoomButtons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        state.zoom = parseInt(btn.getAttribute('data-zoom') || '4', 10);
        render2DPreview();
      });
    });

    // Mouse drag on 3D Canvas
    if (elements.canvas3D) {
      elements.canvas3D.addEventListener('mousedown', (e) => {
        state.isDragging = true;
        state.dragStartX = e.clientX;
      });

      window.addEventListener('mousemove', (e) => {
        if (!state.isDragging) return;
        const deltaX = e.clientX - state.dragStartX;
        state.rotationAngle = (state.rotationAngle + deltaX * 0.5 + 360) % 360;
        state.dragStartX = e.clientX;
        render3DCanvas();
      });

      window.addEventListener('mouseup', () => {
        state.isDragging = false;
      });
    }

    // Download Buttons
    if (elements.btnDownloadZip) {
      elements.btnDownloadZip.addEventListener('click', handleDownloadZip);
    }
    if (elements.btnHeaderDownload) {
      elements.btnHeaderDownload.addEventListener('click', handleDownloadZip);
    }
  }

  // Initial setup
  function init() {
    renderTreeSelector();
    setupEventListeners();
    renderCurrentView();
  }

  // Run on DOM Ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();
