const fs = require('fs');
const path = require('path');

const projectRoot = 'c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica';
const assetsSrc = path.join(projectRoot, 'src/main/resources/assets');
const wikiDataPath = path.join(projectRoot, 'Wiki/data.js');

const langData = JSON.parse(fs.readFileSync(path.join(assetsSrc, 'entropica/lang/en_us.json'), 'utf8'));

// Pre-scan for all blocks in the mod's assets
const modBlockStates = fs.readdirSync(path.join(assetsSrc, 'entropica/blockstates')).map(f => f.replace('.json', ''));
const modBlockModels = fs.readdirSync(path.join(assetsSrc, 'entropica/models/block')).map(f => f.replace('.json', ''));
const modBlockTex = fs.readdirSync(path.join(assetsSrc, 'entropica/textures/block')).map(f => f.replace('.png', ''));
const allModBlocks = new Set([...modBlockStates, ...modBlockModels, ...modBlockTex]);

const ESSENCE_COLORS = {
    AIR: [168, 134, 84], WATER: [34, 92, 124], NATURE: [26, 89, 35], EARTH: [119, 50, 20],
    FROZEN: [152, 220, 242], NETHER: [184, 60, 8], RADIANT: [255, 234, 120], UMBRAL: [33, 26, 33],
    UNDEAD: [77, 100, 83], VOID: [65, 48, 78], ARID: [194, 178, 143], LIGHTNING: [255, 230, 0],
    VITAE: [255, 107, 157], BLOOD: [138, 3, 3], MAGMA: [184, 60, 8], STORM: [66, 155, 163],
    ASTRAL: [65, 48, 78], ECLIPSE: [255, 234, 120], ENTROPIC: [65, 48, 78], CHIMERA: [255, 255, 255]
};

const data = {
    categories: [
        { id: 'getting_started', name: 'Getting Started', items: [] },
        { id: 'multiblocks', name: 'Multiblocks', items: [] },
        { id: 'machines', name: 'Machines', items: [] },
        { id: 'logistics', name: 'Logistics', items: [] },
        { id: 'bestiary', name: 'Bestiary', items: [] },
        { id: 'world_objects', name: 'World', items: [] },
        { id: 'equipment', name: 'Equipment', items: [] },
        { id: 'materials', name: 'Materials', items: [] },
        { id: 'components', name: 'Components', items: [] },
        { id: 'runes', name: 'Runes', items: [] }
    ],
    items: {
        'page_essences': { id: 'page_essences', name: 'The Essence Codex', type: 'Gallery', description: 'Magical energy variants.', category: 'getting_started', texture: 'assets/entropica/textures/item/strong_essence.png' },
        'page_ampoules': { id: 'page_ampoules', name: 'The Ampoule Vault', type: 'Gallery', description: 'Storage ampoule variants.', category: 'getting_started', texture: 'assets/entropica/textures/item/large_ampoule.png' },
        'page_dynamic_equipment': { id: 'page_dynamic_equipment', name: 'Dynamic Equipment', type: 'Gallery', description: 'Scaling weapons and tools.', category: 'equipment', texture: 'assets/entropica/textures/item/dynamic_sword.png' },
        'page_shape_concepts': { id: 'page_shape_concepts', name: 'Arsenal Concepts', type: 'Gallery', description: 'Conceptual blueprints.', category: 'equipment', texture: 'assets/entropica/textures/item/adze_shape_concept.png' }
    }
};

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
    return `assets/${ns}/textures/${ps}.png`;
}

const entries = {};
for (const [key, value] of Object.entries(langData)) {
    if (key.startsWith('item.entropica.') || key.startsWith('block.entropica.')) {
        const baseId = key.split('.').pop();
        if (!entries[baseId]) entries[baseId] = {};
        entries[baseId][key.split('.')[0]] = { key, name: value };
    }
}

function categorize(baseId, info) {
    const selected = info.block || info.item;
    const key = selected.key, name = selected.name, lKey = key.toLowerCase(), lName = name.toLowerCase();
    const isBlock = key.startsWith('block') || allModBlocks.has(baseId);
    const isPureEssence = lKey.endsWith('essence') && !lKey.includes('blade');
    const isAmpoule = lKey.includes('ampoule'), isShapeConcept = lKey.includes('shape_concept'), isDynamicEquip = lName.includes('dynamic');
    const shouldTint = lKey.includes('ampoule') || lKey.includes('orb');
    
    let tint = null;
    if (shouldTint) {
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
                if (tKey.startsWith('layer') || tKey === 'all' || tKey === 'top' || tKey === 'side' || tKey === 'bottom' || tKey === 'face' || tKey === '0' || tKey === '1') {
                    textures.push({ path: resolved, tintable: shouldTint && tKey !== 'layer0' });
                }
            }
        });
    }
    if (textures.length === 0) textures.push({ path: isBlock ? `assets/entropica/textures/block/${baseId}.png` : `assets/entropica/textures/item/${baseId}.png`, tintable: shouldTint });

    const itemData = { id: key, name, type: isBlock ? 'Block' : 'Item', description: langData[`tooltip.${key}`] || 'Documentation in progress.', category: 'other', textures, tint, modelData, textureMap };

    if (isPureEssence || isAmpoule || isShapeConcept || isDynamicEquip) {
        itemData.category = isDynamicEquip ? 'equipment' : 'getting_started';
        itemData.noDetailPage = (isPureEssence || isAmpoule || isShapeConcept);
        data.items[key] = itemData;
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

for (const [baseId, info] of Object.entries(entries)) { categorize(baseId, info); }

// POPULATE BESTIARY
const bestiaryEntries = [
    { 
        id: 'grot', 
        name: 'Grot', 
        tex: 'assets/entropica/textures/entity/orb_base.png', 
        desc: 'A curious, slime-like creature that thrives on essence. Grots are highly intelligent and can be bred to produce rare elemental variants.',
        stats: [
            { label: 'Purity', value: '1 - 28 (Affects HP & Magic Dmg)' },
            { label: 'Intelligence', value: '1 - 20 (Unlocks Games & Medicine)' },
            { label: 'Friendliness', value: '0 - 255 (Required for Taming)' },
            { label: 'Fertility', value: '1 - 20 (Breeding cooldown & count)' }
        ],
        behaviors: [
            { name: 'Tag', desc: 'Friendly Grots will play tag with players and each other. Tagged Grots perform "Happy Jumps" and swap roles.' },
            { name: 'Hide & Seek', desc: 'Smart Grots (Int > 10) can start a game. One counts while others run and hide. Found grots celebrate with particles.' },
            { name: 'Combat Medic', desc: 'Nature-aligned Grots (Int > 12) will actively heal injured allies during combat.' },
            { name: 'Elemental Combat', desc: 'Attacks vary by type: Lightning Grots stun, Nether Grots ignite, and Void Grots teleport after striking.' },
            { name: 'Biological Splitting', desc: 'Non-sterile large Grots will split into smaller ones when killed, allowing for colony growth.' }
        ]
    },
    { id: 'veil_fox', name: 'Veil Fox', tex: 'assets/entropica/textures/entity/orb_base.png', desc: 'A mystical fox that can phase through dimensions. It leaves behind afterimages and is often found near areas of high Vis concentration.' },
    { id: 'ashen_stalker', name: 'Ashen Stalker', tex: 'assets/entropica/textures/entity/orb_base.png', desc: 'A silent predator that dwells in the shadows of industrial ruins. It is highly resistant to heat and physical damage.' },
    { id: 'eidolic_shadow', name: 'Eidolic Shadow', tex: 'assets/entropica/textures/entity/orb_base.png', desc: 'A spectral manifestation of pure Eidolite. It has no physical form and can possess nearby machinery to cause chaos.' }
];
bestiaryEntries.forEach(o => {
    const key = `entity.entropica.${o.id}`;
    data.items[key] = { id: key, name: o.name, type: 'Mob', description: o.desc, category: 'bestiary', textures: [{path: o.tex, tintable: true}], tint: 0xFFFFFF, mobStats: o.stats, mobBehaviors: o.behaviors };
    data.categories.find(c => c.id === 'bestiary').items.push(key);
});

// WORLD OBJECTS
const worldObjects = [
    { id: 'essence_node', name: 'Essence Node', tex: 'assets/entropica/textures/entity/essence_rift_main.png', desc: 'A natural tear in reality where pure essence bleeds into the physical world. Use a Vis Extraction Apparatus to harness its power.' },
    { id: 'essence_orb', name: 'Essence Orb', tex: 'assets/entropica/textures/entity/orb_base.png', desc: 'The unpure, physical form of concentrated vis. These orbs can be collected and processed into refined essence.' }
];
worldObjects.forEach(o => {
    const key = `entity.entropica.${o.id}`;
    data.items[key] = { id: key, name: o.name, type: 'Object', description: o.desc, category: 'world_objects', textures: [{path: o.tex, tintable: true}], tint: 0xFFFFFF };
    data.categories.find(c => c.id === 'world_objects').items.push(key);
});

fs.writeFileSync(wikiDataPath, `const wikiData = ${JSON.stringify(data, null, 2)};`);
console.log('Wiki Data updated with Comprehensive Grot Documentation.');
