document.addEventListener('DOMContentLoaded', () => {
    const contentArea = document.getElementById('content-area');
    const sidebarNav = document.getElementById('sidebar-nav');
    const searchInput = document.getElementById('wiki-search');
    const homeBtn = document.getElementById('home-btn');
    const themeToggle = document.getElementById('theme-toggle');

    let activeScene = null;

    function handleRoute() {
        if (activeScene) { activeScene.destroy(); activeScene = null; }
        const hash = window.location.hash.substring(1);
        if (!hash) { showHome(); return; }
        const params = new URLSearchParams(hash);
        if (params.has('item')) {
            const id = params.get('item'), item = wikiData.items[id];
            if (!item) { showHome(); return; }
            if (item.type === 'Gallery' || item.noDetailPage) {
                if (item.noDetailPage) {
                    let gId = 'page_essences';
                    if (item.id.includes('ampoule')) gId = 'page_ampoules';
                    else if (item.id.includes('shape_concept')) gId = 'page_shape_concepts';
                    else if (item.name.toLowerCase().includes('dynamic')) gId = 'page_dynamic_equipment';
                    window.location.hash = `item=${gId}`;
                } else showGallery(id);
            } else showItemDetail(id);
        } else if (params.has('category')) showCategory(params.get('category'));
        else if (params.has('search')) showSearchResults(params.get('search'));
        else showHome();
    }
    window.addEventListener('hashchange', handleRoute);

    // ROBUST SEARCH
    searchInput.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase().trim();
        if (query.length > 0) {
            // Update hash without losing focus
            const newHash = `search=${encodeURIComponent(query)}`;
            if (window.location.hash !== '#' + newHash) {
                window.location.hash = newHash;
            }
        } else {
            window.location.hash = '';
        }
    });

    function showSearchResults(query) {
        const results = Object.values(wikiData.items).filter(item => 
            item.name && item.name.toLowerCase().includes(query.toLowerCase()) && !item.id.startsWith('page_')
        );
        contentArea.innerHTML = `
            <div class="detail-view">
                <h2>Search Results: "${query}"</h2>
                <div class="grid" style="margin-top: 2rem;">
                    ${results.map(item => `
                        <div class="card" onclick="window.location.hash='item=${item.id}'">
                            <div class="type">${item.type}</div>
                            ${renderItemIconRobust(item, 48)}
                            <h3>${item.name}</h3>
                        </div>
                    `).join('')}
                </div>
                ${results.length === 0 ? '<p style="margin-top: 2rem; opacity: 0.6;">No matching blocks or items found.</p>' : ''}
            </div>
        `;
    }

    function renderItemIconRobust(item, size) {
        if (!item.textures || item.textures.length === 0) return '';
        const tintStr = item.tint ? `#${item.tint.toString(16).padStart(6, '0')}` : null;
        const shouldGlow = item.id.includes('orb') || item.id.includes('node') || item.id.includes('ampoule') || item.id.includes('essence');
        const glowStyle = (shouldGlow && tintStr) ? `filter: drop-shadow(0 0 ${size/10}px ${tintStr});` : '';

        return `<div class="layered-icon" style="width: ${size}px; height: ${size}px; position: relative; margin: 0 auto; ${glowStyle}">
            ${item.textures.map((t, idx) => {
                const isTinted = t.tintable && tintStr;
                return `<div style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: ${idx}; background: ${isTinted ? tintStr : 'none'};
                    ${isTinted ? `-webkit-mask: url(${t.path}) no-repeat center; mask: url(${t.path}) no-repeat center; -webkit-mask-size: contain; mask-size: contain;` : ''}">
                    ${!isTinted ? `<img src="${t.path}" style="width: 100%; height: 100%; image-rendering: pixelated;">` : ''}
                </div>`;
            }).join('')}</div>`;
    }

    function init3DRenderer(container, item) {
        const width = container.clientWidth || 300, height = container.clientHeight || 300;
        const scene = new THREE.Scene();
        const camera = new THREE.PerspectiveCamera(45, width/height, 0.1, 1000);
        const renderer = new THREE.WebGLRenderer({ antialias: false, alpha: true });
        renderer.setSize(width, height);
        renderer.outputColorSpace = THREE.SRGBColorSpace;
        container.innerHTML = '';
        container.appendChild(renderer.domElement);
        const group = new THREE.Group();
        const loader = new THREE.TextureLoader();
        if (item.modelData && item.modelData.elements) {
            item.modelData.elements.forEach(el => {
                const size = [el.to[0]-el.from[0], el.to[1]-el.from[1], el.to[2]-el.from[2]];
                const faces = [
                    { name: 'east',  rot: [0, Math.PI/2, 0],   pos: [el.to[0], (el.from[1]+el.to[1])/2, (el.from[2]+el.to[2])/2], w: size[2], h: size[1] },
                    { name: 'west',  rot: [0, -Math.PI/2, 0],  pos: [el.from[0], (el.from[1]+el.to[1])/2, (el.from[2]+el.to[2])/2], w: size[2], h: size[1] },
                    { name: 'up',    rot: [-Math.PI/2, 0, 0],  pos: [(el.from[0]+el.to[0])/2, el.to[1], (el.from[2]+el.to[2])/2], w: size[0], h: size[2] },
                    { name: 'down',  rot: [Math.PI/2, 0, 0],   pos: [(el.from[0]+el.to[0])/2, el.from[1], (el.from[2]+el.to[2])/2], w: size[0], h: size[2] },
                    { name: 'south', rot: [0, 0, 0],           pos: [(el.from[0]+el.to[0])/2, (el.from[1]+el.to[1])/2, el.to[2]], w: size[0], h: size[1] },
                    { name: 'north', rot: [0, Math.PI, 0],     pos: [(el.from[0]+el.to[0])/2, (el.from[1]+el.to[1])/2, el.from[2]], w: size[0], h: size[1] }
                ];
                faces.forEach(f => {
                    const faceData = el.faces[f.name];
                    if (faceData) {
                        const geo = new THREE.PlaneGeometry(f.w/16, f.h/16);
                        const texKey = faceData.texture.startsWith('#') ? faceData.texture.substring(1) : faceData.texture;
                        const texPath = item.textureMap[texKey] || item.textures[0].path;
                        const tex = loader.load(texPath);
                        tex.magFilter = THREE.NearestFilter; tex.minFilter = THREE.NearestFilter;
                        tex.colorSpace = THREE.SRGBColorSpace;
                        if (faceData.uv) {
                            const u1 = faceData.uv[0]/16, v1 = faceData.uv[1]/16, u2 = faceData.uv[2]/16, v2 = faceData.uv[3]/16;
                            tex.repeat.set(Math.abs(u2-u1), Math.abs(v2-v1));
                            tex.offset.set(Math.min(u1,u2), 1-Math.max(v1,v2));
                            if (u1 > u2) { tex.repeat.x *= -1; tex.offset.x += Math.abs(u2-u1); }
                        }
                        const mat = new THREE.MeshBasicMaterial({ map: tex, transparent: true, side: THREE.DoubleSide });
                        const plane = new THREE.Mesh(geo, mat);
                        plane.position.set((f.pos[0]-8)/16, (f.pos[1]-8)/16, (f.pos[2]-8)/16);
                        plane.rotation.set(f.rot[0], f.rot[1], f.rot[2]);
                        group.add(plane);
                    }
                });
            });
        } else {
            const geo = new THREE.BoxGeometry(1, 1, 1);
            const materials = [];
            const sideTex = item.textureMap['side'] || item.textureMap['all'] || item.textures[0].path;
            const topTex = item.textureMap['top'] || item.textureMap['all'] || item.textures[0].path;
            const bottomTex = item.textureMap['bottom'] || item.textureMap['all'] || item.textures[0].path;
            [sideTex, sideTex, topTex, bottomTex, sideTex, sideTex].forEach(path => {
                const tex = loader.load(path);
                tex.magFilter = THREE.NearestFilter; tex.minFilter = THREE.NearestFilter;
                tex.colorSpace = THREE.SRGBColorSpace;
                materials.push(new THREE.MeshBasicMaterial({ map: tex, transparent: true }));
            });
            group.add(new THREE.Mesh(geo, materials));
        }
        scene.add(group); camera.position.set(1.5, 1.5, 1.5); camera.lookAt(0, 0, 0);
        let frame; function animate() { frame = requestAnimationFrame(animate); group.rotation.y += 0.01; renderer.render(scene, camera); } animate();
        return { destroy: () => { cancelAnimationFrame(frame); renderer.dispose(); container.innerHTML = ''; } };
    }

    function initSidebar() {
        sidebarNav.innerHTML = wikiData.categories.map(cat => `
            <div class="nav-category">
                <div class="nav-category-title">${cat.name}</div>
                ${cat.items.slice(0, 5).map(id => `<a href="#item=${id}" class="nav-link">${wikiData.items[id].name}</a>`).join('')}
                ${cat.items.length > 5 ? `<a href="#category=${cat.id}" class="nav-link" style="font-size: 0.8rem; opacity: 0.6;">View All...</a>` : ''}
            </div>
        `).join('');
    }

    function showHome() {
        contentArea.innerHTML = `
            <div class="hero">
                <h2>Welcome to Entropica</h2>
                <p>Industrializing Magic. Mastering Vis.</p>
            </div>
            <div class="category-section">
                <h3 style="margin-bottom: 2rem;">Featured Categories</h3>
                <div class="grid">
                    ${wikiData.categories.map(cat => `
                        <div class="card cat-card" onclick="window.location.hash='category=${cat.id}'">
                            <h3>${cat.name}</h3>
                            <p class="desc">${cat.items.length} Entries</p>
                        </div>
                    `).join('')}
                </div>
            </div>
        `;
        searchInput.value = ''; // Clear search when going home
    }

    function showCategory(catId) {
        const cat = wikiData.categories.find(c => c.id === catId);
        contentArea.innerHTML = `<div class="detail-view"><h2>${cat.name}</h2><div class="grid" style="margin-top: 2rem;">${cat.items.map(id => {
            const item = wikiData.items[id];
            return `<div class="card" onclick="window.location.hash='item=${id}'">
                <div class="type">${item.type}</div>${renderItemIconRobust(item, 48)}<h3>${item.name}</h3></div>`;
        }).join('')}</div></div>`;
    }

    function showGallery(pageId) {
        let items = [];
        if (pageId === 'page_essences') items = Object.values(wikiData.items).filter(i => i.id.includes('essence') && i.noDetailPage);
        else if (pageId === 'page_ampoules') items = Object.values(wikiData.items).filter(i => i.id.includes('ampoule') && i.noDetailPage);
        else if (pageId === 'page_dynamic_equipment') items = Object.values(wikiData.items).filter(i => i.name.toLowerCase().includes('dynamic'));
        else if (pageId === 'page_shape_concepts') items = Object.values(wikiData.items).filter(i => i.id.includes('shape_concept'));
        contentArea.innerHTML = `<div class="detail-view"><h2>${wikiData.items[pageId].name}</h2><p style="margin-bottom: 2rem;">${wikiData.items[pageId].description}</p>
            <div class="grid" style="grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));">
                ${items.map(item => `<div class="card gallery-item" style="text-align: center; cursor: default; padding: 1.5rem;">
                    ${renderItemIconRobust(item, 64)}<div style="font-size: 0.8rem; font-weight: bold; margin-top: 1rem;">${item.name}</div></div>`).join('')}
            </div>${buildNavbox()}</div>`;
    }

    function buildNavbox() {
        return `<div class="navbox"><div class="navbox-header">Wiki Quick Navigation</div>${wikiData.categories.map(cat => `
            <div class="navbox-row"><div class="navbox-label">${cat.name}</div><div class="navbox-links">${cat.items.map((id, idx) => `
                <a href="#item=${id}">${wikiData.items[id].name}</a>${idx < cat.items.length - 1 ? '<span class="bullet">•</span>' : ''}
            `).join('')}</div></div>`).join('')}</div>`;
    }

    function showItemDetail(itemId) {
        const item = wikiData.items[itemId], category = wikiData.categories.find(c => c.id === item.category);
        contentArea.innerHTML = `
            <div class="detail-view">
                <div class="item-layout">
                    <div class="item-main-content">
                        <h2 style="font-size: 3rem; margin-bottom: 0.5rem; color: var(--accent-arcanite);">${item.name}</h2>
                        <div style="height: 2px; width: 100px; background: var(--accent-arcanite); margin-bottom: 1.5rem;"></div>
                        <p class="item-description" style="font-size: 1.25rem; line-height: 1.8;">${item.description}</p>
                    </div>
                    <div class="infobox">
                        <div class="infobox-title">${item.name}</div>
                        <div id="canvas-container" class="infobox-image" style="height: 200px; position: relative;">
                            ${item.type !== 'Block' ? renderItemIconRobust(item, 128) : ''}
                        </div>
                        <div class="infobox-section-title">Statistics</div>
                        <div class="infobox-row"><div class="infobox-label">Type</div><div class="infobox-value">${item.type}</div></div>
                        <div class="infobox-row"><div class="infobox-label">Category</div><div class="infobox-value">${category ? category.name : 'Gallery'}</div></div>
                    </div>
                </div>
                ${buildNavbox()}
            </div>
        `;
        if (item.type === 'Block') {
            const container = document.getElementById('canvas-container');
            activeScene = init3DRenderer(container, item);
        }
        window.scrollTo(0, 0);
    }

    initSidebar(); handleRoute();
    const currentTheme = localStorage.getItem('theme') || 'dark';
    if (currentTheme === 'light') document.body.classList.add('light-mode');
    themeToggle.addEventListener('click', () => {
        document.body.classList.toggle('light-mode');
        localStorage.setItem('theme', document.body.classList.contains('light-mode') ? 'light' : 'dark');
    });
    homeBtn.addEventListener('click', () => window.location.hash = '');
});
