const fs = require('fs');
const path = require('path');

const projectRoot = 'c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica';
const assetsSrc = path.join(projectRoot, 'src/main/resources/assets');
const wikiSrc = path.join(projectRoot, 'Wiki');
const distDir = path.join(projectRoot, 'docs');

// 1. Create docs structure
if (!fs.existsSync(distDir)) fs.mkdirSync(distDir);
if (!fs.existsSync(path.join(distDir, 'assets'))) fs.mkdirSync(path.join(distDir, 'assets'), { recursive: true });

// 2. Copy Core Wiki Files
['index.html', 'style.css', 'wiki.js'].forEach(file => {
    fs.copyFileSync(path.join(wikiSrc, file), path.join(distDir, file));
});

// 3. Extraction Logic (Modified for Dist)
const langData = JSON.parse(fs.readFileSync(path.join(assetsSrc, 'entropica/lang/en_us.json'), 'utf8'));
const modBlockStates = fs.readdirSync(path.join(assetsSrc, 'entropica/blockstates')).map(f => f.replace('.json', ''));
const modBlockModels = fs.readdirSync(path.join(assetsSrc, 'entropica/models/block')).map(f => f.replace('.json', ''));
const modBlockTex = fs.readdirSync(path.join(assetsSrc, 'entropica/textures/block')).map(f => f.replace('.png', ''));
const allModBlocks = new Set([...modBlockStates, ...modBlockModels, ...modBlockTex]);

const data = {
    categories: [
        { id: 'getting_started', name: 'Getting Started', items: [] },
        { id: 'multiblocks', name: 'Multiblock Systems', items: [] },
        { id: 'machines', name: 'Machines & Automation', items: [] },
        { id: 'logistics', name: 'Logistics & Fluids', items: [] },
        { id: 'bestiary', name: 'Bestiary (Mobs)', items: [] },
        { id: 'world_objects', name: 'World Objects', items: [] },
        { id: 'galleries', name: 'Galleries & Concepts', items: ['page_essences', 'page_ampoules', 'page_dynamic_equipment', 'page_shape_concepts'] },
        { id: 'equipment', name: 'Tools & Weapons', items: [] },
        { id: 'materials', name: 'Materials & Resources', items: [] },
        { id: 'components', name: 'Crafting Components', items: [] },
        { id: 'runes', name: 'Runes', items: [] },
        { id: 'other', name: 'Miscellaneous', items: [] }
    ],
    items: {
        'page_essences': { id: 'page_essences', name: 'The Essence Codex', type: 'Gallery', description: 'Magical energy variants.', category: 'galleries', texture: 'assets/entropica/textures/item/strong_essence.png' },
        'page_ampoules': { id: 'page_ampoules', name: 'The Ampoule Vault', type: 'Gallery', description: 'Storage ampoule variants.', category: 'galleries', texture: 'assets/entropica/textures/item/large_ampoule.png' },
        'page_dynamic_equipment': { id: 'page_dynamic_equipment', name: 'Dynamic Equipment', type: 'Gallery', description: 'Scaling weapons and tools.', category: 'galleries', texture: 'assets/entropica/textures/item/dynamic_sword.png' },
        'page_shape_concepts': { id: 'page_shape_concepts', name: 'Arsenal Concepts', type: 'Gallery', description: 'Conceptual blueprints.', category: 'galleries', texture: 'assets/entropica/textures/item/adze_shape_concept.png' }
    }
};

function copyAsset(relPath) {
    if (!relPath) return;
    const src = path.join(projectRoot, relPath);
    const dest = path.join(distDir, relPath);
    if (fs.existsSync(src)) {
        fs.mkdirSync(path.dirname(dest), { recursive: true });
        fs.copyFileSync(src, dest);
    }
}

function resolveModel(modelPath) {
    if (!modelPath) return null;
    const parts = modelPath.split(':'), ns = parts.length > 1 ? parts[0] : 'minecraft', rp = parts.length > 1 ? parts[1] : parts[0];
    const fullPath = path.join(assetsSrc, ns, 'models', `${rp}.json`);
    if (!fs.existsSync(fullPath)) return null;
    const model = JSON.parse(fs.readFileSync(fullPath, 'utf8'));
    if (model.parent) {
        const parent = resolveModel(model.parent);
        if (parent) return { ...parent, ...model, textures: { ...parent.textures, ...model.textures }, elements: model.elements || parent.elements };
    }
    return model;
}

function resolveTexture(textures, key) {
    if (!key) return null;
    const val = textures[key.startsWith('#') ? key.substring(1) : key];
    if (!val) return null;
    if (val.startsWith('#')) return resolveTexture(textures, val);
    const p = val.split(':'), ns = p.length > 1 ? p[0] : 'minecraft', ps = p.length > 1 ? p[1] : p[0];
    const relPath = `assets/${ns}/textures/${ps}.png`;
    copyAsset(relPath);
    return relPath;
}

const entries = {};
for (const [key, value] of Object.entries(langData)) {
    if (key.startsWith('item.entropica.') || key.startsWith('block.entropica.')) {
        const baseId = key.split('.').pop();
        if (!entries[baseId]) entries[baseId] = {};
        entries[baseId][key.split('.')[0]] = { key, name: value };
    }
}

const ESSENCE_COLORS = { AIR: [168,134,84], WATER: [34,92,124], NATURE: [26,89,35], EARTH: [119,50,20], FROZEN: [152,220,242], NETHER: [184,60,8], RADIANT: [255,234,120], UMBRAL: [33,26,33], UNDEAD: [77,100,83], VOID: [65,48,78], ARID: [194,178,143], LIGHTNING: [255,230,0], VITAE: [255,107,157], BLOOD: [138,3,3], MAGMA: [184,60,8], STORM: [66,155,163], ASTRAL: [65,48,78], ECLIPSE: [255,234,120], ENTROPIC: [65,48,78], CHIMERA: [255,255,255] };

function categorize(baseId, info) {
    const selected = info.block || info.item;
    const key = selected.key, name = selected.name, lKey = key.toLowerCase(), lName = name.toLowerCase();
    const isBlock = key.startsWith('block') || allModBlocks.has(baseId);
    
    let tint = null;
    if (lKey.includes('ampoule') || lKey.includes('orb')) {
        for (const [type, rgb] of Object.entries(ESSENCE_COLORS)) {
            if (lKey.includes(type.toLowerCase())) { tint = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]; break; }
        }
    }

    let textures = [], modelData = null, textureMap = {};
    modelData = resolveModel(`entropica:block/${baseId}`) || resolveModel(`entropica:item/${baseId}`);
    
    if (modelData && modelData.textures) {
        Object.keys(modelData.textures).forEach(tKey => {
            const resolved = resolveTexture(modelData.textures, tKey);
            if (resolved) {
                textureMap[tKey] = resolved;
                if (tKey.startsWith('layer') || ['all','top','side','bottom','face','0','1'].includes(tKey)) {
                    textures.push({ path: resolved, tintable: tint && tKey !== 'layer0' });
                }
            }
        });
    }
    if (textures.length === 0) {
        const path = isBlock ? `assets/entropica/textures/block/${baseId}.png` : `assets/entropica/textures/item/${baseId}.png`;
        copyAsset(path);
        textures.push({ path, tintable: !!tint });
    }

    const itemData = { id: key, name, type: isBlock ? 'Block' : 'Item', description: langData[`tooltip.${key}`] || 'Documentation in progress.', category: 'other', textures, tint, modelData, textureMap };
    
    if (lKey.endsWith('essence') && !lKey.includes('blade') || lKey.includes('ampoule') || lKey.includes('shape_concept') || lName.includes('dynamic')) {
        itemData.category = 'galleries'; itemData.noDetailPage = !lName.includes('dynamic');
        data.items[key] = itemData;
        if (lName.includes('dynamic')) data.categories.find(c => c.id === 'galleries').items.push(key);
        return;
    }

    let catId = 'other';
    if (isBlock && (lName.includes('controller') || lName.includes('port') || lName.includes('chamber') || lName.includes('vessel') || lName.includes('loom') || lName.includes('lathe') || lName.includes('hatch') || lName.includes('readout') || lName.includes('exhaust') || lName.includes('apparatus') || lName.includes('base') || lName.includes('valve'))) catId = 'multiblocks';
    else if (lName.includes('synthesizer') || lName.includes('furnace') || lName.includes('smelter') || lName.includes('machine') || lName.includes('automator') || lName.includes('table')) catId = 'machines';
    else if (lName.includes('pipe') || lName.includes('valve') || lName.includes('glass')) catId = 'logistics';
    else if (lName.includes('sword') || lName.includes('axe') || lName.includes('pickaxe') || lName.includes('focus') || lName.includes('blade') || lName.includes('monocle')) catId = 'equipment';
    else if (lName.includes('ingot') || lName.includes('plate') || lName.includes('nugget')) catId = 'materials';
    else if (lName.includes('filament') || lName.includes('core') || lName.includes('spool') || lName.includes('gear') || lName.includes('gasket') || lName.includes('frame')) catId = 'components';
    else if (lName.includes('rune')) catId = 'runes';

    itemData.category = catId; data.items[key] = itemData;
    const cat = data.categories.find(c => c.id === catId);
    if (cat) cat.items.push(key);
}

for (const [baseId, info] of Object.entries(entries)) categorize(baseId, info);

const worldObjects = [{id:'essence_node', name:'Essence Node', tex:'assets/entropica/textures/entity/essence_rift_main.png', desc:'Faster extraction node.'}, {id:'essence_orb', name:'Essence Orb', tex:'assets/entropica/textures/entity/orb_base.png', desc:'Unpure vis form.'}];
worldObjects.forEach(o => {
    copyAsset(o.tex);
    const key = `entity.entropica.${o.id}`;
    data.items[key] = { id: key, name: o.name, type: 'Object', description: o.desc, category: 'world_objects', textures: [{path: o.tex, tintable: true}], tint: 0xFFFFFF };
    data.categories.find(c => c.id === 'world_objects').items.push(key);
});

// Finalize
copyAsset('assets/entropica/textures/item/strong_essence.png');
copyAsset('assets/entropica/textures/item/large_ampoule.png');
copyAsset('assets/entropica/textures/item/dynamic_sword.png');
copyAsset('assets/entropica/textures/item/adze_shape_concept.png');

fs.writeFileSync(path.join(distDir, 'data.js'), `const wikiData = ${JSON.stringify(data, null, 2)};`);
console.log('Wiki Built successfully in /docs! Upload this folder to GitHub Pages.');
