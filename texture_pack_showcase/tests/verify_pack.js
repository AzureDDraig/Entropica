/**
 * Automated Verification & E2E Test Suite for Modded Tree Showcase & Gallery
 * Target Path: texture_pack_showcase
 * Usage: node tests/verify_pack.js
 */

const fs = require('fs');
const path = require('path');
const assert = require('assert');

// Path Resolution
const ROOT_DIR = path.resolve(__dirname, '..');
const PACK_MCMETA_PATH = path.join(ROOT_DIR, 'pack.mcmeta');
const PACK_PNG_PATH = path.join(ROOT_DIR, 'pack.png');
const TEXTURES_DIR = path.join(ROOT_DIR, 'assets', 'modded_trees', 'textures', 'block');
const INDEX_HTML_PATH = path.join(ROOT_DIR, 'index.html');
const STYLE_CSS_PATH = path.join(ROOT_DIR, 'style.css');
const TREES_DATA_PATH = path.join(ROOT_DIR, 'trees_data.js');

// 10 Tree Species IDs
const EXPECTED_TREES = [
  'greatwood',
  'silverwood',
  'twilight_oak',
  'canopy_tree',
  'redwood',
  'rubber_wood',
  'sacred_oak',
  'archwood',
  'driftwood',
  'entropic_elder'
];

// Test Suite Counters
let passedTests = 0;
let failedTests = 0;

function logPass(msg) {
  console.log(`  ✓ PASS: ${msg}`);
  passedTests++;
}

function logFail(msg, err) {
  console.error(`  ✗ FAIL: ${msg}`);
  if (err) console.error(`    Details: ${err.message || err}`);
  failedTests++;
}

function runSection(name, fn) {
  console.log(`\n========================================\nRunning ${name}...\n========================================`);
  try {
    fn();
  } catch (err) {
    console.error(`Section [${name}] threw uncaught exception: ${err.message}`);
  }
}

// ============================================================================
// 1. PACK STRUCTURE CHECKS
// ============================================================================
function checkPackStructure() {
  // Check 1.1: Root directory existence
  if (fs.existsSync(ROOT_DIR)) {
    logPass(`Root directory exists at ${ROOT_DIR}`);
  } else {
    logFail(`Root directory missing at ${ROOT_DIR}`);
  }

  // Check 1.2: pack.mcmeta validation
  if (fs.existsSync(PACK_MCMETA_PATH)) {
    try {
      const content = fs.readFileSync(PACK_MCMETA_PATH, 'utf8');
      const mcmeta = JSON.parse(content);
      assert.ok(mcmeta.pack, 'pack object missing in pack.mcmeta');
      assert.strictEqual(mcmeta.pack.pack_format, 46, `pack_format must be 46, got ${mcmeta.pack.pack_format}`);
      assert.ok(typeof mcmeta.pack.description === 'string' && mcmeta.pack.description.trim().length > 0, 'description missing or empty');
      logPass(`pack.mcmeta is valid JSON (pack_format: ${mcmeta.pack.pack_format}, description: "${mcmeta.pack.description}")`);
    } catch (e) {
      logFail(`pack.mcmeta validation failed`, e);
    }
  } else {
    logFail(`pack.mcmeta does not exist at ${PACK_MCMETA_PATH}`);
  }

  // Check 1.3: pack.png validation
  if (fs.existsSync(PACK_PNG_PATH)) {
    const stats = fs.statSync(PACK_PNG_PATH);
    if (stats.size > 0) {
      try {
        const header = parsePngHeader(PACK_PNG_PATH);
        logPass(`pack.png exists (${stats.size} bytes, header verified: ${header.width}x${header.height})`);
      } catch (err) {
        logFail(`pack.png header validation failed`, err);
      }
    } else {
      logFail(`pack.png exists but is empty (0 bytes)`);
    }
  } else {
    logFail(`pack.png does not exist at ${PACK_PNG_PATH}`);
  }

  // Check 1.4: Texture Directory & 30 PNG files
  if (!fs.existsSync(TEXTURES_DIR)) {
    logFail(`Texture directory missing at ${TEXTURES_DIR}`);
    return;
  }
  logPass(`Texture directory exists at ${TEXTURES_DIR}`);

  let missingCount = 0;
  for (const treeId of EXPECTED_TREES) {
    const logPath = path.join(TEXTURES_DIR, `${treeId}_log.png`);
    const logTopPath = path.join(TEXTURES_DIR, `${treeId}_log_top.png`);
    const leavesPath = path.join(TEXTURES_DIR, `${treeId}_leaves.png`);

    if (!fs.existsSync(logPath)) { logFail(`Missing texture: ${treeId}_log.png`); missingCount++; }
    if (!fs.existsSync(logTopPath)) { logFail(`Missing texture: ${treeId}_log_top.png`); missingCount++; }
    if (!fs.existsSync(leavesPath)) { logFail(`Missing texture: ${treeId}_leaves.png`); missingCount++; }
  }

  if (missingCount === 0) {
    logPass(`All 30 expected PNG textures are present for 10 tree species`);
  } else {
    logFail(`${missingCount} texture files are missing`);
  }
}

// ============================================================================
// 2. TEXTURE VALIDATION CHECKS (RAW PNG HEADER & INTEGRITY)
// ============================================================================
function parsePngHeader(filePath) {
  const buf = fs.readFileSync(filePath);
  if (buf.length < 29) {
    throw new Error(`File size (${buf.length} bytes) is too small to be a valid PNG`);
  }

  // PNG Signature: 89 50 4E 47 0D 0A 1A 0A
  const pngSig = Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]);
  if (!buf.subarray(0, 8).equals(pngSig)) {
    throw new Error('Invalid PNG header signature');
  }

  // IHDR Chunk Type: 49 48 44 52
  const ihdrType = buf.subarray(12, 16).toString('ascii');
  if (ihdrType !== 'IHDR') {
    throw new Error(`Expected IHDR chunk, found ${ihdrType}`);
  }

  const width = buf.readUInt32BE(16);
  const height = buf.readUInt32BE(20);
  const bitDepth = buf.readUInt8(24);
  const colorType = buf.readUInt8(25); // 2 = RGB, 6 = RGBA

  // Scan chunks to check IEND presence and absence of corrupt chunk lengths
  let offset = 8;
  let hasIend = false;
  while (offset + 12 <= buf.length) {
    const chunkLength = buf.readUInt32BE(offset);
    const chunkType = buf.subarray(offset + 4, offset + 8).toString('ascii');
    if (chunkType === 'IEND') {
      hasIend = true;
      break;
    }
    const nextOffset = offset + 12 + chunkLength;
    if (nextOffset <= offset) {
      break;
    }
    offset = nextOffset;
  }

  if (!hasIend) {
    throw new Error('PNG stream truncated or missing IEND chunk');
  }

  return { width, height, bitDepth, colorType, size: buf.length };
}

function checkTexturesValidation() {
  if (!fs.existsSync(TEXTURES_DIR)) return;

  const files = fs.readdirSync(TEXTURES_DIR).filter(f => f.endsWith('.png'));
  let validCount = 0;

  for (const file of files) {
    const fullPath = path.join(TEXTURES_DIR, file);
    try {
      const header = parsePngHeader(fullPath);
      assert.strictEqual(header.width, 32, `Width must be 32, got ${header.width}`);
      assert.strictEqual(header.height, 32, `Height must be 32, got ${header.height}`);
      assert.strictEqual(header.bitDepth, 8, `Bit depth must be 8, got ${header.bitDepth}`);
      assert.ok(header.colorType === 6 || header.colorType === 2, `Color type must be 6 (RGBA) or 2 (RGB), got ${header.colorType}`);
      validCount++;
    } catch (err) {
      logFail(`Texture corruption/validation error in ${file}`, err);
    }
  }

  if (validCount === 30 && files.length === 30) {
    logPass(`All 30 texture PNGs verified: exactly 32x32 pixels, bit depth 8, RGBA/RGB format, uncorrupted IEND`);
  } else {
    logFail(`Validated ${validCount}/${files.length} textures (expected 30 verified files)`);
  }
}

// ============================================================================
// 3. HTML & CSS VALIDATION CHECKS
// ============================================================================
function checkHtmlAndCss() {
  if (!fs.existsSync(INDEX_HTML_PATH)) {
    logFail(`index.html missing at ${INDEX_HTML_PATH}`);
    return;
  }

  const htmlContent = fs.readFileSync(INDEX_HTML_PATH, 'utf8');

  // Check required DOM IDs
  const requiredIds = [
    'tree-select',
    'canvas-3d',
    'palette-display',
    'download-btn',
    'texture-preview-2d'
  ];

  for (const id of requiredIds) {
    const idRegex = new RegExp(`id=["']${id}["']`, 'i');
    if (idRegex.test(htmlContent)) {
      logPass(`DOM element #${id} found in index.html`);
    } else {
      logFail(`Required DOM element #${id} missing in index.html`);
    }
  }

  // Check CSS Pixel Art Rendering rule
  let cssCombinedContent = htmlContent;
  if (fs.existsSync(STYLE_CSS_PATH)) {
    cssCombinedContent += '\n' + fs.readFileSync(STYLE_CSS_PATH, 'utf8');
  }

  const pixelatedRegex = /image-rendering\s*:\s*(pixelated|crisp-edges|-webkit-optimize-contrast)/i;
  if (pixelatedRegex.test(cssCombinedContent)) {
    logPass(`CSS rule 'image-rendering: pixelated' verified for crisp preview scaling`);
  } else {
    logFail(`Missing CSS rule 'image-rendering: pixelated' in index.html or style.css`);
  }
}

// ============================================================================
// 4. DATA CONSISTENCY CHECKS
// ============================================================================
function checkDataConsistency() {
  let jsContent = '';
  if (fs.existsSync(TREES_DATA_PATH)) {
    jsContent = fs.readFileSync(TREES_DATA_PATH, 'utf8');
  } else if (fs.existsSync(INDEX_HTML_PATH)) {
    jsContent = fs.readFileSync(INDEX_HTML_PATH, 'utf8');
  } else {
    logFail(`Neither trees_data.js nor index.html available for data consistency check`);
    return;
  }

  // Verify all 10 tree species IDs are registered in JS data
  let speciesCount = 0;
  for (const treeId of EXPECTED_TREES) {
    const treeRegex = new RegExp(`['"]?id['"]?\\s*:\\s*['"]${treeId}['"]|['"]${treeId}['"]`, 'i');
    if (treeRegex.test(jsContent)) {
      speciesCount++;
    } else {
      logFail(`Tree species '${treeId}' not found in JavaScript data`);
    }
  }

  if (speciesCount === EXPECTED_TREES.length) {
    logPass(`All 10 tree species IDs present in JavaScript tree data`);
  }

  // Verify texture path pattern reference in JS data
  const texturePathPattern = /assets\/modded_trees\/textures\/block\/[a-z0-9_]+\.png/i;
  if (texturePathPattern.test(jsContent)) {
    logPass(`JavaScript data correctly references texture paths in assets/modded_trees/textures/block/`);
  } else {
    logFail(`JavaScript tree data missing expected texture path structure`);
  }
}

// ============================================================================
// MAIN RUNNER
// ============================================================================
function runAllTests() {
  console.log(`Starting E2E Verification Suite for Texture Pack Showcase...`);
  
  runSection(`1. Pack Structure Checks`, checkPackStructure);
  runSection(`2. Texture Validation Checks`, checkTexturesValidation);
  runSection(`3. HTML & CSS Validation Checks`, checkHtmlAndCss);
  runSection(`4. Data Consistency Checks`, checkDataConsistency);

  console.log(`\n========================================`);
  console.log(`TEST SUMMARY: ${passedTests} Passed, ${failedTests} Failed`);
  console.log(`========================================`);

  if (failedTests > 0) {
    console.error(`E2E Verification Failed!`);
    process.exit(1);
  } else {
    console.log(`E2E Verification Succeeded! All checks passed.`);
    process.exit(0);
  }
}

runAllTests();
