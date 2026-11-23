(function () {
  "use strict";

  // =========================================================================
  // 1. CONSTANTS & ECONOMY TABLES (1:1 parity with TycoonModels & IndustrialModels)
  // =========================================================================
  const RESOURCES = {
    wood:        { name: "Wood",        baseValue: 6,   color: 0xb45309, css: "#fbbf24" },
    stone:       { name: "Stone",       baseValue: 10,  color: 0x94a3b8, css: "#cbd5e1" },
    coal:        { name: "Coal",        baseValue: 16,  color: 0x334155, css: "#94a3b8" },
    copper:      { name: "Copper",      baseValue: 26,  color: 0xea580c, css: "#fb923c" },
    iron:        { name: "Iron",        baseValue: 38,  color: 0xcbd5e1, css: "#e2e8f0" },
    gold:        { name: "Gold",        baseValue: 65,  color: 0xfbbf24, css: "#fde047" },
    crystal:     { name: "Crystal",     baseValue: 110, color: 0x22d3ee, css: "#67e8f9" },
    rare:        { name: "Rare Relic",  baseValue: 220, color: 0xe879f9, css: "#f0abfc" },
    quartz:      { name: "Prism Quartz",baseValue: 145, color: 0xf472b6, css: "#f9a8d4" },
    aether:      { name: "Aetherium",   baseValue: 185, color: 0x7dd3fc, css: "#bae6fd" },
    venom:       { name: "Bio-Resin",   baseValue: 240, color: 0x84cc16, css: "#bef264" },
    frostite:    { name: "Frostite",    baseValue: 310, color: 0x67e8f9, css: "#a5f3fc" },
    obsidian:    { name: "Obsidian",    baseValue: 390, color: 0xa855f7, css: "#d8b4fe" },
    titan_alloy: { name: "Titan Core",  baseValue: 520, color: 0x38bdf8, css: "#7dd3fc" }
  };

  const BIOMES = [
    { id: "forest",               name: "Green Forest",          cx: 0,   cz: 0,   unlockCost: 0,       reqAxe: 1, reqPick: 1, reqBag: 1, sky: 0x6ec3eb, fog: 0x9bd7f2, ground: 0x3b8c47, accent: 0x10b981, gate: null },
    { id: "mountains",            name: "Rocky Mountains",       cx: 0,   cz: -34, unlockCost: 150,     reqAxe: 2, reqPick: 2, reqBag: 1, sky: 0x7c96ad, fog: 0x9ab0c4, ground: 0x5c6672, accent: 0x94a3b8, gate: { x: 0,   z: -17, dir: "z" } },
    { id: "desert",               name: "Golden Desert",         cx: 34,  cz: 0,   unlockCost: 650,     reqAxe: 1, reqPick: 3, reqBag: 1, sky: 0xf3be78, fog: 0xf7cf97, ground: 0xd9a14a, accent: 0xf59e0b, gate: { x: 17,  z: 0,   dir: "x" } },
    { id: "frozen",               name: "Frozen Lands",          cx: -34, cz: 0,   unlockCost: 2000,    reqAxe: 1, reqPick: 4, reqBag: 3, sky: 0x95cde8, fog: 0xbfe2f4, ground: 0xcbe7f5, accent: 0x38bdf8, gate: { x: -17, z: 0,   dir: "x" } },
    { id: "volcanic",             name: "Volcanic Region",       cx: 0,   cz: 34,  unlockCost: 6500,    reqAxe: 1, reqPick: 5, reqBag: 4, sky: 0x4d201d, fog: 0x6b2c25, ground: 0x2d2726, accent: 0xef4444, gate: { x: 0,   z: 17,  dir: "z" } },
    { id: "jungle",               name: "Ancient Jungle",        cx: 34,  cz: -34, unlockCost: 18000,   reqAxe: 6, reqPick: 6, reqBag: 1, sky: 0x44a399, fog: 0x6ac2b8, ground: 0x1e5c4a, accent: 0x14b8a6, gate: { x: 34,  z: -17, dir: "z" } },
    { id: "crystal_desert",       name: "Crystal Desert",        cx: 68,  cz: 0,   unlockCost: 42000,   reqAxe: 6, reqPick: 6, reqBag: 4, sky: 0xe69ec8, fog: 0xf4c2e1, ground: 0xe3a8c9, accent: 0xf472b6, gate: { x: 51,  z: 0,   dir: "x" } },
    { id: "sky_islands",          name: "Sky Islands",           cx: 0,   cz: -68, unlockCost: 95000,   reqAxe: 6, reqPick: 7, reqBag: 5, sky: 0x8be4ff, fog: 0xcff5ff, ground: 0x5cc4a2, accent: 0x7dd3fc, gate: { x: 0,   z: -51, dir: "z" } },
    { id: "toxic_swamp",          name: "Toxic Swamp",           cx: -34, cz: 34,  unlockCost: 185000,  reqAxe: 7, reqPick: 7, reqBag: 5, sky: 0x4c6838, fog: 0x6f8c4f, ground: 0x354d2b, accent: 0x84cc16, gate: { x: -34, z: 17,  dir: "z" } },
    { id: "frozen_mountains",     name: "Frozen Mountains",      cx: -68, cz: 0,   unlockCost: 340000,  reqAxe: 7, reqPick: 8, reqBag: 6, sky: 0x6ea6d6, fog: 0xaad5f2, ground: 0xb7dff5, accent: 0x67e8f9, gate: { x: -51, z: 0,   dir: "x" } },
    { id: "volcanic_lands",       name: "Volcanic Lands",        cx: 0,   cz: 68,  unlockCost: 620000,  reqAxe: 8, reqPick: 8, reqBag: 7, sky: 0x3d1414, fog: 0x66221b, ground: 0x241918, accent: 0xfb7185, gate: { x: 0,   z: 51,  dir: "z" } },
    { id: "ancient_machine_world",name: "Ancient Machine World", cx: 34,  cz: 34,  unlockCost: 1200000, reqAxe: 8, reqPick: 8, reqBag: 8, sky: 0x2b4560, fog: 0x47688a, ground: 0x394b5e, accent: 0x38bdf8, gate: { x: 34,  z: 17,  dir: "z" } }
  ];

  const BUILDING_DEFS = [
    { id: "sawmill",                title: "Timber Sawmill",     biome: "forest",                x: 7.5,   z: 6.5,   baseCost: 120,     growth: 2.25, passive: 4 },
    { id: "warehouse",              title: "Grand Warehouse",    biome: "forest",                x: 7.5,   z: -4.5,  baseCost: 180,     growth: 2.3,  passive: 2 },
    { id: "miner_forest",           title: "Lumber Bot",         biome: "forest",                x: -8.0,  z: -6.0,  baseCost: 90,      growth: 2.15, passive: 3 },
    { id: "smelter",                title: "Ore Smelter",        biome: "mountains",             x: 7.5,   z: -29.0, baseCost: 320,     growth: 2.3,  passive: 10 },
    { id: "miner_mountains",        title: "Drill Rig",          biome: "mountains",             x: -7.5,  z: -29.0, baseCost: 350,     growth: 2.15, passive: 12 },
    { id: "miner_desert",           title: "Solar Extractor",    biome: "desert",                x: 34.0,  z: 7.5,   baseCost: 950,     growth: 2.15, passive: 30 },
    { id: "miner_frozen",           title: "Cryo Harvester",     biome: "frozen",                x: -34.0, z: 7.5,   baseCost: 2800,    growth: 2.15, passive: 75 },
    { id: "miner_volcanic",         title: "Magma Extractor",    biome: "volcanic",              x: 7.5,   z: 34.0,  baseCost: 8000,    growth: 2.15, passive: 190 },
    { id: "miner_jungle",           title: "Relic Synth",        biome: "jungle",                x: 34.0,  z: -26.5, baseCost: 22000,   growth: 2.15, passive: 450 },
    { id: "miner_crystal_desert",   title: "Prism Siphon",       biome: "crystal_desert",        x: 68.0,  z: 7.5,   baseCost: 48000,   growth: 2.15, passive: 900 },
    { id: "miner_sky_islands",      title: "Cloud Turbine",      biome: "sky_islands",           x: 7.5,   z: -62.0, baseCost: 110000,  growth: 2.15, passive: 1800 },
    { id: "miner_toxic_swamp",      title: "Bio-Resin Purifier", biome: "toxic_swamp",           x: -26.5, z: 34.0,  baseCost: 220000,  growth: 2.15, passive: 3400 },
    { id: "miner_frozen_mountains", title: "Glacier Bore Rig",   biome: "frozen_mountains",      x: -68.0, z: 7.5,   baseCost: 420000,  growth: 2.15, passive: 6200 },
    { id: "miner_volcanic_lands",   title: "Caldera Forge",      biome: "volcanic_lands",        x: 7.5,   z: 62.0,  baseCost: 780000,  growth: 2.15, passive: 11500 },
    { id: "miner_ancient_machine",  title: "Titan Dynamo",       biome: "ancient_machine_world", x: 26.5,  z: 34.0,  baseCost: 1450000, growth: 2.15, passive: 22000 }
  ];

  const MACHINE_DEFS = [
    { id: "wood_cutter",         biome: "forest",                x: 11.6,  z: 4.8,   rotY: -Math.PI / 2, color: 0xd97706 },
    { id: "stone_crusher",       biome: "forest",                x: -11.4, z: -5.2,  rotY: Math.PI / 2,  color: 0x94a3b8 },
    { id: "conveyor_belt",       biome: "forest",                x: 9.6,   z: 0.6,   rotY: 0,            color: 0x38bdf8 },
    { id: "sawmill_machine",     biome: "forest",                x: 11.6,  z: 9.2,   rotY: -Math.PI / 2, color: 0xf59e0b },
    { id: "auto_miner_machine",  biome: "mountains",             x: -11.2, z: -32.0, rotY: 0.4,          color: 0x10b981 },
    { id: "ore_processor",       biome: "mountains",             x: 11.4,  z: -33.2, rotY: -Math.PI / 2, color: 0xfb923c },
    { id: "smelting_furnace",    biome: "mountains",             x: 11.4,  z: -25.4, rotY: -Math.PI / 2, color: 0xef4444 },
    { id: "gold_refinery",       biome: "desert",                x: 27.6,  z: -5.4,  rotY: 0.3,          color: 0xfde047 },
    { id: "crystal_processor",   biome: "frozen",                x: -27.6, z: -5.4,  rotY: -0.3,         color: 0x22d3ee },
    { id: "industrial_factory",  biome: "volcanic",              x: 11.4,  z: 28.5,  rotY: -Math.PI / 2, color: 0xe879f9 },
    { id: "crystal_synthesizer", biome: "crystal_desert",        x: 78.2,  z: -5.2,  rotY: -Math.PI / 2, color: 0xf472b6 },
    { id: "titan_forge",         biome: "ancient_machine_world", x: 44.2,  z: 28.8,  rotY: -Math.PI / 2, color: 0x38bdf8 }
  ];

  const DRIVABLE_VEHICLES = {
    mining_rover:   { id: "mining_rover",   name: "Small Mining Rover",      speedMult: 1.35, armor: 0.25, drillBonus: 0.20, radarRadius: 0 },
    heavy_hauler:   { id: "heavy_hauler",   name: "Heavy Mining Transport",  speedMult: 1.20, armor: 0.50, drillBonus: 0.08, radarRadius: 0 },
    explorer_buggy: { id: "explorer_buggy", name: "Exploration Vehicle",     speedMult: 1.55, armor: 0.35, drillBonus: 0.12, radarRadius: 20 }
  };

  const BAG_CAPS = [12, 22, 36, 56, 84, 126, 184, 260];
  const MOVE_SPEEDS = [6.4, 7.4, 8.5, 9.7, 11.0, 12.4, 13.9, 15.6];
  const GATHER_RATES = [1.0, 1.25, 1.55, 1.9, 2.3, 2.75, 3.25, 3.85];
  const TOOL_DAMAGES = [1, 2, 3, 5, 7, 9, 12, 16];

  const TIER_COLORS = [
    0x92400e, // 1: Wooden
    0x94a3b8, // 2: Stone
    0xea580c, // 3: Copper
    0xcbd5e1, // 4: Iron
    0x64748b, // 5: Steel
    0xfbbf24, // 6: Gold
    0x22d3ee, // 7: Diamond
    0xe879f9  // 8: Legendary
  ];

  // =========================================================================
  // 2. GAME STATE & MENU MODE
  // =========================================================================
  let state = {
    coins: 0,
    lifetimeEarned: 0,
    xp: 0,
    inventory: {},
    gatheredTotals: {},
    axeLevel: 1,
    pickaxeLevel: 1,
    backpackLevel: 1,
    moveSpeedLevel: 1,
    gatherSpeedLevel: 1,
    resourceUpgradeLevels: {},
    buildingLevels: {},
    buildingWorkers: {},
    buildingLines: {},
    machineLevels: {},
    machineActive: {},
    warehouseStock: {},
    fleetLevels: {},
    unlockedBiomes: ["forest"],
    claimedQuestIds: [],
    uniqueItems: [],
    discoveredSecrets: [],
    defeatedBosses: [],
    unlockedVehicles: [],
    activeVehicleId: null,
    insectsDefeated: 0,
    playerHp: 100,
    isMuted: false,
    masterVolume: 1.0,
    ambientVolume: 0.70,
    weatherVolume: 0.75,
    waterVolume: 0.80,
    sfxVolume: 0.85,
    musicVolume: 0.45,
    nativeHasNatureAudio: false,
    graphicsQuality: 1,
    hapticsEnabled: true,
    shadowQuality: 1,
    cameraSensitivity: 1.0,
    cameraDistance: 12.2,
    fpsLimit: 60,
    playerPosX: 0.0,
    playerPosZ: 2.0,
    lastBiomeId: "forest"
  };

  let isMenuMode = true;
  let isAppBackgrounded = false;
  let stateDirty = false;
  let lastSyncTime = 0;

  function hasUniqueItem(id) {
    return Array.isArray(state.uniqueItems) && state.uniqueItems.indexOf(id) !== -1;
  }

  function hasVehicleUnlocked(id) {
    return Array.isArray(state.unlockedVehicles) && state.unlockedVehicles.indexOf(id) !== -1;
  }

  function getActiveVehicle() {
    if (!state.activeVehicleId) return null;
    return DRIVABLE_VEHICLES[state.activeVehicleId] || null;
  }

  function getPlayerMaxHp() {
    return 100 + (Math.max(1, state.backpackLevel) - 1) * 10 + (Math.max(1, state.axeLevel) - 1) * 5;
  }

  function getResUpgradeLevel(id) {
    if (!state.resourceUpgradeLevels) return 1;
    return Math.min(8, Math.max(1, state.resourceUpgradeLevels[id] || 1));
  }

  function getBuildingWorkers(id) {
    return (state.buildingWorkers && state.buildingWorkers[id]) ? state.buildingWorkers[id] : 0;
  }

  function getBuildingLines(id) {
    return (state.buildingLines && state.buildingLines[id]) ? state.buildingLines[id] : 0;
  }

  function getMachineLevel(id) {
    return (state.machineLevels && state.machineLevels[id]) ? state.machineLevels[id] : 0;
  }

  function isMachineRunning(id) {
    if (getMachineLevel(id) <= 0) return false;
    if (!state.machineActive || state.machineActive[id] === undefined) return true;
    return Boolean(state.machineActive[id]);
  }

  function getFleetLevel(id) {
    return (state.fleetLevels && state.fleetLevels[id]) ? state.fleetLevels[id] : 0;
  }

  function meetsBiomeGear(biome) {
    return state.axeLevel >= (biome.reqAxe || 1) &&
      state.pickaxeLevel >= (biome.reqPick || 1) &&
      state.backpackLevel >= (biome.reqBag || 1);
  }

  function getMaxBag() {
    const base = BAG_CAPS[Math.min(Math.max(state.backpackLevel, 1), 8) - 1];
    const storageLvl = getResUpgradeLevel("storageCapacity");
    const storageFlat = (storageLvl - 1) * 6;
    const storageMult = 1 + (storageLvl - 1) * 0.1;
    const wh = state.buildingLevels.warehouse || 0;
    const whLines = getBuildingLines("warehouse");
    const whBonus = wh * (8 + whLines * 4);
    const lantern = hasUniqueItem("prism_lantern") ? 10 : 0;
    const haulerBonus = hasVehicleUnlocked("heavy_hauler") ? 45 : 0;
    return Math.round((base + storageFlat + whBonus + lantern + haulerBonus) * storageMult);
  }

  function getBagCount() {
    let sum = 0;
    for (const k in state.inventory) sum += state.inventory[k] || 0;
    return sum;
  }

  function getPassivePerSec() {
    const autoLvl = getResUpgradeLevel("autoProduction");
    const autoMult = 1 + (autoLvl - 1) * 0.2;
    let total = 0;
    for (const b of BUILDING_DEFS) {
      const lvl = state.buildingLevels[b.id] || 0;
      if (lvl <= 0) continue;
      const staffMult = (1 + getBuildingWorkers(b.id) * 0.35) * (1 + getBuildingLines(b.id) * 0.65);
      total += lvl * b.passive * staffMult;
    }
    total *= autoMult;
    if (hasUniqueItem("tycoon_crown")) total *= 1.35;
    total *= (1 + getFleetLevel("excavator") * 0.15);
    return total;
  }

  function getSellValue(resourceId) {
    const def = RESOURCES[resourceId];
    if (!def) return 5;
    const tradeLvl = getResUpgradeLevel("tradeValue");
    const whLvl = state.buildingLevels.warehouse || 0;
    const whLines = getBuildingLines("warehouse");
    let mult = (1.0 + (tradeLvl - 1) * 0.15) + whLvl * (0.2 + whLines * 0.08);
    if (resourceId === "wood") {
      const sawLvl = state.buildingLevels.sawmill || 0;
      const sawBoost = 1 + getBuildingWorkers("sawmill") * 0.25 + getBuildingLines("sawmill") * 0.35;
      mult += sawLvl * 0.45 * sawBoost;
      if (hasUniqueItem("timber_charm")) mult += 0.25;
    }
    if (resourceId === "stone" || resourceId === "coal" || resourceId === "copper" || resourceId === "iron" || resourceId === "gold") {
      const smLvl = state.buildingLevels.smelter || 0;
      const smBoost = 1 + getBuildingWorkers("smelter") * 0.25 + getBuildingLines("smelter") * 0.35;
      mult += smLvl * 0.45 * smBoost;
      if (hasUniqueItem("pickaxe_crest")) mult += 0.25;
    }
    if (resourceId === "crystal" && hasUniqueItem("prism_lantern")) mult += 0.25;
    if (hasUniqueItem("golden_compass")) mult += 0.15;
    if (hasUniqueItem("tycoon_crown")) mult += 0.35;
    return Math.max(1, Math.round(def.baseValue * mult));
  }

  function isBiomeUnlocked(biomeId) {
    return state.unlockedBiomes.indexOf(biomeId) !== -1;
  }

  function formatMoney(n) {
    if (n >= 1000000) return "$" + (n / 1000000).toFixed(2) + "M";
    if (n >= 10000) return "$" + (n / 1000).toFixed(1) + "K";
    return "$" + Math.floor(n).toLocaleString();
  }

  // =========================================================================
  // 3. MULTI-LAYERED WEB AUDIO STUDIO + BIOME AMBIENT & COMBAT SFX
  // =========================================================================
  let audioCtx = null;
  let noiseBuffer = null;
  let musicStep = 0;
  let musicTimer = 0;
  let ambientTimer = 0;
  let fallbackBed = null;
  let birdChirpTimer = 2.8;
  const BIOME_SCALES = {
    forest:                [261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 392.00, 329.63],
    mountains:             [196.00, 220.00, 246.94, 293.66, 329.63, 392.00, 293.66, 220.00],
    desert:                [293.66, 311.13, 392.00, 415.30, 466.16, 587.33, 466.16, 392.00],
    frozen:                [392.00, 493.88, 587.33, 659.25, 783.99, 659.25, 587.33, 493.88],
    volcanic:              [146.83, 164.81, 174.61, 220.00, 233.08, 174.61, 164.81, 146.83],
    jungle:                [261.63, 311.13, 349.23, 392.00, 466.16, 523.25, 392.00, 349.23],
    crystal_desert:        [440.00, 523.25, 659.25, 783.99, 880.00, 783.99, 659.25, 523.25],
    sky_islands:           [523.25, 587.33, 659.25, 783.99, 987.77, 783.99, 659.25, 587.33],
    toxic_swamp:           [174.61, 207.65, 220.00, 261.63, 293.66, 220.00, 207.65, 174.61],
    frozen_mountains:      [329.63, 392.00, 493.88, 587.33, 659.25, 493.88, 392.00, 329.63],
    volcanic_lands:        [130.81, 155.56, 174.61, 196.00, 233.08, 196.00, 155.56, 130.81],
    ancient_machine_world: [220.00, 261.63, 329.63, 392.00, 440.00, 329.63, 261.63, 220.00]
  };

  function ensureAudio() {
    if (state.isMuted || isAppBackgrounded) return null;
    if (!audioCtx) {
      const AC = window.AudioContext || window.webkitAudioContext;
      if (AC) audioCtx = new AC();
    }
    if (audioCtx && audioCtx.state === "suspended") {
      audioCtx.resume().catch(function () {});
    }
    if (audioCtx && !noiseBuffer) {
      const len = Math.floor(audioCtx.sampleRate * 1.5);
      noiseBuffer = audioCtx.createBuffer(1, len, audioCtx.sampleRate);
      const data = noiseBuffer.getChannelData(0);
      let b0 = 0, b1 = 0, b2 = 0;
      for (let i = 0; i < len; i++) {
        const white = Math.random() * 2 - 1;
        b0 = 0.997 * b0 + white * 0.029;
        b1 = 0.985 * b1 + white * 0.032;
        b2 = 0.950 * b2 + white * 0.048;
        data[i] = Math.max(-1, Math.min(1, (b0 + b1 + b2) * 1.6));
      }
    }
    return audioCtx;
  }

  function getMasterGain() {
    if (state.isMuted || isAppBackgrounded) return 0;
    return Math.max(0, Math.min(1, typeof state.masterVolume === "number" ? state.masterVolume : 1.0));
  }

  function getSfxGain() {
    const m = getMasterGain();
    if (m <= 0) return 0;
    return m * Math.max(0, Math.min(1, typeof state.sfxVolume === "number" ? state.sfxVolume : 0.85));
  }

  function getMusicGain() {
    const m = getMasterGain();
    if (m <= 0) return 0;
    return m * Math.max(0, Math.min(1, typeof state.musicVolume === "number" ? state.musicVolume : 0.45));
  }

  function getAmbientGain() {
    const m = getMasterGain();
    if (m <= 0) return 0;
    return m * Math.max(0, Math.min(1, typeof state.ambientVolume === "number" ? state.ambientVolume : 0.70));
  }

  function ensureNatureFallbackBed(ctx) {
    if (fallbackBed || !ctx || !noiseBuffer) return fallbackBed;
    const src = ctx.createBufferSource();
    src.buffer = noiseBuffer;
    src.loop = true;
    const filter = ctx.createBiquadFilter();
    filter.type = "bandpass";
    filter.frequency.value = 540;
    filter.Q.value = 0.8;
    const gain = ctx.createGain();
    gain.gain.value = 0;
    src.connect(filter);
    filter.connect(gain);
    gain.connect(ctx.destination);
    src.start();
    fallbackBed = { src: src, filter: filter, gain: gain };
    return fallbackBed;
  }

  function playSoftBirdPhrase(ctx, vol) {
    if (!ctx || vol <= 0.004) return;
    const t0 = ctx.currentTime + 0.02;
    const baseF = 1650 + Math.random() * 950;
    const chirps = 2 + (Math.random() > 0.45 ? 1 : 0);
    for (let c = 0; c < chirps; c++) {
      const start = t0 + c * 0.11;
      const osc = ctx.createOscillator();
      const g = ctx.createGain();
      osc.type = "sine";
      const f0 = baseF * (1 + c * 0.08);
      osc.frequency.setValueAtTime(f0, start);
      osc.frequency.exponentialRampToValueAtTime(f0 * (c % 2 === 0 ? 1.24 : 0.86), start + 0.075);
      g.gain.setValueAtTime(0.0005, start);
      g.gain.linearRampToValueAtTime(Math.min(0.032, vol * 0.04), start + 0.015);
      g.gain.exponentialRampToValueAtTime(0.0004, start + 0.085);
      osc.connect(g);
      g.connect(ctx.destination);
      osc.start(start);
      osc.stop(start + 0.095);
    }
  }

  function playTone(freq, endFreq, dur, type, baseVol, delaySec) {
    const master = getSfxGain();
    if (master <= 0.01) return;
    const ctx = ensureAudio();
    if (!ctx) return;
    const t0 = ctx.currentTime + (delaySec || 0);
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = type || "triangle";
    osc.frequency.setValueAtTime(Math.max(25, freq), t0);
    if (endFreq && endFreq !== freq) {
      osc.frequency.exponentialRampToValueAtTime(Math.max(25, endFreq), t0 + dur);
    }
    const peak = Math.max(0.001, (baseVol || 0.12) * master);
    gain.gain.setValueAtTime(peak, t0);
    gain.gain.exponentialRampToValueAtTime(0.0008, t0 + dur);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start(t0);
    osc.stop(t0 + dur + 0.02);
  }

  function playNoiseBurst(filterFreq, qVal, dur, baseVol, delaySec, filterType) {
    const master = getSfxGain();
    if (master <= 0.01) return;
    const ctx = ensureAudio();
    if (!ctx || !noiseBuffer) return;
    const t0 = ctx.currentTime + (delaySec || 0);
    const src = ctx.createBufferSource();
    src.buffer = noiseBuffer;
    const filter = ctx.createBiquadFilter();
    filter.type = filterType || "bandpass";
    filter.frequency.setValueAtTime(filterFreq || 800, t0);
    filter.Q.setValueAtTime(qVal || 2.0, t0);
    const gain = ctx.createGain();
    const peak = Math.max(0.001, (baseVol || 0.1) * master);
    gain.gain.setValueAtTime(peak, t0);
    gain.gain.exponentialRampToValueAtTime(0.0008, t0 + dur);
    src.connect(filter);
    filter.connect(gain);
    gain.connect(ctx.destination);
    src.start(t0);
    src.stop(t0 + dur + 0.02);
  }

  function playMaterialHitSfx(kind, dmgRatio) {
    const pitchLift = 1 + dmgRatio * 0.18;
    if (kind === "wood") {
      playTone(175 * pitchLift, 78, 0.09, "triangle", 0.19, 0);
      playNoiseBurst(760 * pitchLift, 2.2, 0.06, 0.14, 0.005, "bandpass");
      playTone(880, 420, 0.035, "sawtooth", 0.05, 0.015);
    } else if (kind === "stone" || kind === "obsidian") {
      playTone(1180 * pitchLift, 720, 0.06, "triangle", 0.11, 0);
      playNoiseBurst(1600, 1.6, 0.08, 0.13, 0.005, "bandpass");
      playTone(145, 70, 0.08, "sine", 0.12, 0.01);
    } else if (kind === "coal" || kind === "venom") {
      playNoiseBurst(2100, 1.2, 0.075, 0.15, 0, "highpass");
      playTone(210, 95, 0.07, "square", 0.07, 0.006);
    } else if (kind === "copper") {
      playTone(720 * pitchLift, 690, 0.14, "sine", 0.11, 0);
      playTone(1080 * pitchLift, 1020, 0.11, "triangle", 0.07, 0.01);
      playNoiseBurst(1900, 3.0, 0.04, 0.08, 0, "bandpass");
    } else if (kind === "iron" || kind === "titan_alloy") {
      playTone(520 * pitchLift, 495, 0.16, "triangle", 0.14, 0);
      playTone(1360 * pitchLift, 1290, 0.12, "sine", 0.08, 0.005);
      playNoiseBurst(2600, 3.5, 0.05, 0.09, 0, "bandpass");
    } else if (kind === "gold") {
      playTone(880 * pitchLift, 870, 0.18, "sine", 0.12, 0);
      playTone(1320 * pitchLift, 1310, 0.15, "sine", 0.09, 0.015);
      playTone(1760, 1740, 0.11, "sine", 0.05, 0.03);
    } else if (kind === "crystal" || kind === "quartz" || kind === "frostite" || kind === "aether") {
      playTone(1320 * pitchLift, 1480, 0.18, "sine", 0.11, 0);
      playTone(1980 * pitchLift, 2120, 0.15, "sine", 0.08, 0.02);
    } else {
      playTone(440 * pitchLift, 660, 0.16, "triangle", 0.11, 0);
      playTone(1120, 1540, 0.18, "sine", 0.09, 0.02);
    }
  }

  function playMaterialBreakSfx(kind) {
    if (kind === "wood") {
      playTone(250, 95, 0.24, "sawtooth", 0.07, 0);
      playNoiseBurst(1250, 2.5, 0.06, 0.14, 0.08, "bandpass");
      playNoiseBurst(980, 2.0, 0.07, 0.13, 0.19, "bandpass");
      playTone(95, 34, 0.2, "sine", 0.2, 0.34);
    } else if (kind === "stone" || kind === "obsidian") {
      playTone(125, 42, 0.18, "triangle", 0.17, 0);
      playNoiseBurst(900, 1.1, 0.16, 0.16, 0.01, "lowpass");
      playNoiseBurst(2200, 3.0, 0.04, 0.09, 0.11, "bandpass");
    } else if (kind === "coal" || kind === "venom") {
      playNoiseBurst(1600, 1.0, 0.18, 0.16, 0, "bandpass");
      playTone(160, 60, 0.14, "square", 0.08, 0.02);
    } else if (kind === "copper") {
      [720, 900, 1080].forEach((f, idx) => playTone(f, f * 0.98, 0.16, "sine", 0.1, idx * 0.04));
    } else if (kind === "iron" || kind === "titan_alloy") {
      [520, 780, 1040].forEach((f, idx) => playTone(f, f * 0.97, 0.18, "triangle", 0.11, idx * 0.045));
    } else if (kind === "gold") {
      [880, 1108, 1318, 1760].forEach((f, idx) => playTone(f, f * 1.01, 0.22, "sine", 0.1, idx * 0.04));
    } else if (kind === "crystal" || kind === "quartz" || kind === "frostite" || kind === "aether") {
      [1244, 1568, 1975, 2489].forEach((f, idx) => playTone(f, f * 1.03, 0.24, "sine", 0.09, idx * 0.035));
    } else {
      [523, 784, 1046, 1568].forEach((f, idx) => playTone(f, f * 1.02, 0.26, "sine", 0.11, idx * 0.045));
    }
  }

  function playSfx(kind) {
    if (kind === "pickup") {
      playTone(540, 820, 0.07, "sine", 0.1, 0);
    } else if (kind === "coin") {
      playTone(987, 1318, 0.08, "sine", 0.12, 0);
      playTone(1318, 1567, 0.11, "sine", 0.11, 0.05);
    } else if (kind === "upgrade") {
      playTone(460, 440, 0.09, "triangle", 0.13, 0);
      playTone(698, 700, 0.11, "sine", 0.11, 0.07);
      playTone(932, 1046, 0.16, "sine", 0.12, 0.14);
    } else if (kind === "unlock") {
      [523, 659, 784, 1046].forEach((f, i) => {
        playTone(f, f * 1.02, 0.2, "triangle", 0.13, i * 0.07);
      });
    } else if (kind === "hit_enemy") {
      playTone(320, 140, 0.08, "triangle", 0.14, 0);
      playNoiseBurst(1100, 1.8, 0.05, 0.11, 0.004, "bandpass");
    } else if (kind === "player_hurt") {
      playTone(185, 85, 0.14, "sawtooth", 0.14, 0);
    } else if (kind === "boss_roar") {
      playTone(110, 58, 0.36, "sawtooth", 0.15, 0);
      playTone(165, 82, 0.32, "triangle", 0.12, 0.05);
    } else if (kind === "secret_found") {
      [659, 880, 1174, 1567].forEach((f, i) => {
        playTone(f, f * 1.04, 0.24, "sine", 0.12, i * 0.06);
      });
    } else if (kind === "geyser") {
      playNoiseBurst(520, 0.9, 0.25, 0.11, 0, "bandpass");
      playTone(130, 75, 0.2, "sine", 0.1, 0.02);
    }
  }

  function tickBackgroundMusic(dt, biomeId, indoorFactor, rainIntensity) {
    if (isAppBackgrounded || state.isMuted) {
      if (fallbackBed && audioCtx) {
        fallbackBed.gain.gain.setTargetAtTime(0, audioCtx.currentTime, 0.08);
      }
      return;
    }
    const mg = getMusicGain();
    if (mg > 0.01 && audioCtx && audioCtx.state === "running") {
      musicTimer += dt;
      if (musicTimer >= 1.05) {
        musicTimer = 0;
        const now = audioCtx.currentTime;
        const scale = BIOME_SCALES[biomeId] || BIOME_SCALES.forest;
        const freq = scale[musicStep % scale.length];
        const harmony = scale[(musicStep + 2) % scale.length] * 0.5;
        musicStep++;
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.type = biomeId === "ancient_machine_world" ? "triangle" : "sine";
        osc.frequency.setValueAtTime(freq, now);
        gain.gain.setValueAtTime(0.001, now);
        gain.gain.linearRampToValueAtTime(0.04 * mg, now + 0.04);
        gain.gain.exponentialRampToValueAtTime(0.0006, now + 0.92);
        osc.connect(gain);
        gain.connect(audioCtx.destination);
        osc.start(now);
        osc.stop(now + 0.95);
        if (musicStep % 2 === 1) {
          const pad = audioCtx.createOscillator();
          const padGain = audioCtx.createGain();
          pad.type = "triangle";
          pad.frequency.setValueAtTime(harmony, now);
          padGain.gain.setValueAtTime(0.001, now);
          padGain.gain.linearRampToValueAtTime(0.023 * mg, now + 0.18);
          padGain.gain.exponentialRampToValueAtTime(0.0006, now + 1.85);
          pad.connect(padGain);
          padGain.connect(audioCtx.destination);
          pad.start(now);
          pad.stop(now + 1.9);
        }
      }

      // Subtle biome atmospheric sound texture
      ambientTimer += dt;
      if (ambientTimer >= 3.2 && !isMenuMode) {
        ambientTimer = 0;
        if (biomeId === "sky_islands" || biomeId === "frozen_mountains" || biomeId === "frozen") {
          playNoiseBurst(950, 1.4, 0.34, 0.025, 0, "bandpass");
        } else if (biomeId === "toxic_swamp") {
          playTone(210, 340, 0.14, "sine", 0.028, 0);
        } else if (biomeId === "crystal_desert") {
          playTone(1480, 1520, 0.22, "sine", 0.02, 0);
        } else if (biomeId === "volcanic" || biomeId === "volcanic_lands") {
          playTone(72, 54, 0.32, "sine", 0.032, 0);
        } else if (biomeId === "ancient_machine_world") {
          playTone(165, 165, 0.18, "triangle", 0.022, 0);
        }
      }
    }

    // Procedural outdoor nature fallback (used ONLY when no nature MP3 is bundled in assets/audio/)
    if (audioCtx && audioCtx.state === "running") {
      if (state.nativeHasNatureAudio) {
        if (fallbackBed) fallbackBed.gain.gain.setTargetAtTime(0, audioCtx.currentTime, 0.15);
      } else {
        const ag = getAmbientGain();
        const biomeWeight = (biomeId === "forest" || biomeId === "jungle") ? 1.0 :
          ((biomeId === "mountains" || biomeId === "sky_islands") ? 0.8 :
          (biomeId === "toxic_swamp" ? 0.55 :
          ((biomeId === "volcanic" || biomeId === "volcanic_lands" || biomeId === "ancient_machine_world") ? 0.1 : 0.3)));
        const indoorScale = Math.max(0, 1 - (indoorFactor || 0) * 0.88);
        const menuScale = isMenuMode ? 0.5 : 1.0;
        const rainDuck = 1 - (rainIntensity || 0) * 0.28;
        const targetNature = ag * biomeWeight * indoorScale * menuScale * rainDuck;
        const bed = ensureNatureFallbackBed(audioCtx);
        if (bed) {
          bed.gain.gain.setTargetAtTime(targetNature * 0.028, audioCtx.currentTime, 0.35);
        }
        birdChirpTimer -= dt;
        if (birdChirpTimer <= 0) {
          birdChirpTimer = 3.2 + Math.random() * 4.5;
          if (!isMenuMode && targetNature > 0.12 && (biomeId === "forest" || biomeId === "jungle" || biomeId === "mountains" || biomeId === "sky_islands")) {
            playSoftBirdPhrase(audioCtx, targetNature);
          }
        }
      }
    }
  }

  function haptic(kind) {
    if (window.AndroidBridge && window.AndroidBridge.triggerHaptic) {
      try { window.AndroidBridge.triggerHaptic(kind); } catch (_) {}
    }
  }

  // =========================================================================
  // 4. FLOATING UI & BANNER HELPERS
  // =========================================================================
  const floatLayer = document.getElementById("floating-layer");
  const floatingBadges = [];
  const bannerEl = document.getElementById("banner-toast");
  const joyZoneEl = document.getElementById("joystick-zone");
  let bannerTimer = null;

  function showBanner(msg) {
    bannerEl.textContent = msg;
    bannerEl.classList.add("visible");
    clearTimeout(bannerTimer);
    bannerTimer = setTimeout(function () { bannerEl.classList.remove("visible"); }, 2200);
  }

  function spawnFloatingText(worldPos, text, colorCss) {
    if (isMenuMode) return;
    const el = document.createElement("div");
    el.className = "float-badge";
    el.textContent = text;
    el.style.color = colorCss || "#ffffff";
    floatLayer.appendChild(el);
    floatingBadges.push({
      el,
      wx: worldPos.x,
      wy: worldPos.y + 1.45,
      wz: worldPos.z,
      age: 0,
      life: 1.05
    });
  }

  // =========================================================================
  // 5. PBR RENDERER, HDR STUDIO ENVIRONMENT, LIGHTING & SKY DOME
  // =========================================================================
  const canvas = document.getElementById("game-canvas");

  function createRenderer() {
    const attempts = [
      { canvas, antialias: true, alpha: false, powerPreference: "high-performance" },
      { canvas, antialias: true, alpha: false, powerPreference: "default" },
      { canvas, antialias: false, alpha: false, preserveDrawingBuffer: true, failIfMajorPerformanceCaveat: false }
    ];
    for (const opts of attempts) {
      try {
        const r = new THREE.WebGLRenderer(opts);
        if (r && r.getContext()) return r;
      } catch (err) {
        console.warn("WebGLRenderer attempt failed:", err);
      }
    }
    throw new Error("Unable to initialize WebGLRenderer");
  }

  function getViewportSize() {
    const w = Math.max(1, window.innerWidth || document.documentElement.clientWidth || canvas.clientWidth || 360);
    const h = Math.max(1, window.innerHeight || document.documentElement.clientHeight || canvas.clientHeight || 640);
    return { w, h };
  }

  function linCol(hex) {
    if (hex && hex.isColor) return hex.clone();
    return new THREE.Color(hex).convertSRGBToLinear();
  }
  function setSrgbHex(colorObj, hex) {
    return colorObj.setHex(hex).convertSRGBToLinear();
  }

  const renderer = createRenderer();
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 1.75));
  renderer.setClearColor(linCol(0x5eb8e5), 1);
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.autoUpdate = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.0;
  renderer.outputEncoding = THREE.sRGBEncoding;

  const scene = new THREE.Scene();
  const currentSkyColor = linCol(0x5eb8e5);
  const currentFogColor = linCol(0x8bcbe8);
  const currentRenderedSkyColor = currentSkyColor.clone();
  const currentRenderedFogColor = currentFogColor.clone();
  const currentRenderedBottomColor = currentFogColor.clone().multiplyScalar(0.5);
  const overcastSkyTop = linCol(0x6b8096);
  const overcastFogHorizon = linCol(0x95a6b5);
  const overcastGroundTint = linCol(0x465664);
  const overcastSunColor = linCol(0xe4edf7);
  const currentBaseHemiSky = linCol(0xd8f0ff);
  const currentBaseHemiGround = linCol(0x3a4d32);
  const currentBaseSunColor = linCol(0xfff1d6);
  let baseFogDensity = 0.0024;
  let baseSunIntensity = 1.38;
  const BASE_AMBIENT_INTENSITY = 0.34;
  const BASE_HEMI_INTENSITY = 0.56;
  const BASE_RIM_INTENSITY = 0.50;
  scene.background = currentRenderedSkyColor;
  scene.fog = new THREE.FogExp2(currentRenderedFogColor.getHex(), baseFogDensity);

  const initialSize = getViewportSize();
  const GAMEPLAY_FOV = 70;
  const MENU_FOV = 54;
  const camera = new THREE.PerspectiveCamera(GAMEPLAY_FOV, initialSize.w / initialSize.h, 0.35, 300);
  camera.position.set(0, 8.2, 11.8);
  camera.lookAt(0, 0.92, 2.0);

  let lastW = 0;
  let lastH = 0;
  function syncViewportSize() {
    const { w, h } = getViewportSize();
    if (w !== lastW || h !== lastH) {
      lastW = w;
      lastH = h;
      camera.aspect = w / h;
      camera.updateProjectionMatrix();
      renderer.setSize(w, h, false);
    }
  }
  syncViewportSize();
  window.addEventListener("resize", syncViewportSize);
  if (typeof ResizeObserver !== "undefined") {
    new ResizeObserver(syncViewportSize).observe(document.body);
  }

  // Atmospheric Sky Dome (linear uniforms + tonemapping & encodings match scene fog seamlessly)
  const skyUniforms = {
    uTop: { value: linCol(0x3da2dc) },
    uHorizon: { value: linCol(0x9bd7f2) },
    uBottom: { value: linCol(0x2a6678) }
  };
  const skyDome = new THREE.Mesh(
    new THREE.SphereGeometry(260, 28, 18),
    new THREE.ShaderMaterial({
      side: THREE.BackSide,
      depthWrite: false,
      fog: false,
      uniforms: skyUniforms,
      vertexShader: "varying vec3 vWorldPos; void main(){ vec4 wp = modelMatrix * vec4(position,1.0); vWorldPos = position; gl_Position = projectionMatrix * viewMatrix * wp; }",
      fragmentShader: [
        "uniform vec3 uTop,uHorizon,uBottom; varying vec3 vWorldPos;",
        "void main(){",
        "  float y = normalize(vWorldPos).y;",
        "  vec3 col = y >= 0.0 ? mix(uHorizon, uTop, pow(max(y, 0.0001), 0.55)) : mix(uHorizon, uBottom, pow(max(-y, 0.0001), 0.6));",
        "  gl_FragColor = vec4(col, 1.0);",
        "  #include <tonemapping_fragment>",
        "  #include <encodings_fragment>",
        "}"
      ].join("\n")
    })
  );
  skyDome.renderOrder = -1;
  scene.add(skyDome);

  // Balanced 4-Light Outdoor PBR Rig: warm ambient + sky/moss hemisphere + dual shadow-casting directional lights
  const ambientLight = new THREE.AmbientLight(linCol(0xfff5e4), 0.34);
  scene.add(ambientLight);

  const hemiLight = new THREE.HemisphereLight(linCol(0xd8f0ff), linCol(0x3a4d32), 0.56);
  scene.add(hemiLight);

  const d = 24;
  const sunLight = new THREE.DirectionalLight(linCol(0xfff1d6), 1.38);
  sunLight.position.set(16, 26, 14);
  sunLight.castShadow = true;
  sunLight.shadow.mapSize.width = 1024;
  sunLight.shadow.mapSize.height = 1024;
  sunLight.shadow.camera.near = 2;
  sunLight.shadow.camera.far = 80;
  sunLight.shadow.camera.left = -d;
  sunLight.shadow.camera.right = d;
  sunLight.shadow.camera.top = d;
  sunLight.shadow.camera.bottom = -d;
  sunLight.shadow.bias = -0.0004;
  sunLight.shadow.normalBias = 0.025;
  scene.add(sunLight);
  scene.add(sunLight.target);

  const rimLight = new THREE.DirectionalLight(linCol(0xaedcff), 0.50);
  rimLight.position.set(-14, 18, -16);
  rimLight.castShadow = true;
  rimLight.shadow.mapSize.width = 1024;
  rimLight.shadow.mapSize.height = 1024;
  rimLight.shadow.camera.near = 2;
  rimLight.shadow.camera.far = 80;
  rimLight.shadow.camera.left = -d;
  rimLight.shadow.camera.right = d;
  rimLight.shadow.camera.top = d;
  rimLight.shadow.camera.bottom = -d;
  rimLight.shadow.bias = -0.0004;
  rimLight.shadow.normalBias = 0.025;
  scene.add(rimLight);
  scene.add(rimLight.target);

  let lastShadowMapRes = 1024;
  function applyGraphicsQuality() {
    const q = typeof state.graphicsQuality === "number" ? state.graphicsQuality : 1;
    const sq = typeof state.shadowQuality === "number" ? state.shadowQuality : q;
    const dpr = window.devicePixelRatio || 1;
    if (q === 0) {
      renderer.setPixelRatio(Math.min(dpr, 1.15));
    } else if (q === 1) {
      renderer.setPixelRatio(Math.min(dpr, 1.65));
    } else {
      renderer.setPixelRatio(Math.min(dpr, 2.0));
    }

    const shadowsEnabled = sq > 0;
    renderer.shadowMap.enabled = shadowsEnabled;
    const targetRes = sq >= 2 ? 2048 : 1024;
    if (shadowsEnabled && targetRes !== lastShadowMapRes) {
      lastShadowMapRes = targetRes;
      sunLight.shadow.mapSize.set(targetRes, targetRes);
      rimLight.shadow.mapSize.set(Math.min(1024, targetRes), Math.min(1024, targetRes));
      if (sunLight.shadow.map) { sunLight.shadow.map.dispose(); sunLight.shadow.map = null; }
      if (rimLight.shadow.map) { rimLight.shadow.map.dispose(); rimLight.shadow.map = null; }
    }
    renderer.shadowMap.needsUpdate = true;
  }

  // =========================================================================
  // 6. HIGH-RES PROCEDURAL PBR TEXTURES & SCULPTED GEOMETRY FACTORY
  // =========================================================================
  function makeCanvasTex(size, repeatX, repeatY, isColor, drawFn) {
    const c = document.createElement("canvas");
    c.width = size;
    c.height = size;
    const ctx = c.getContext("2d");
    drawFn(ctx, size);
    const tex = new THREE.CanvasTexture(c);
    tex.wrapS = THREE.RepeatWrapping;
    tex.wrapT = THREE.RepeatWrapping;
    tex.repeat.set(repeatX || 1, repeatY || 1);
    tex.encoding = isColor ? THREE.sRGBEncoding : THREE.LinearEncoding;
    tex.anisotropy = 4;
    tex.needsUpdate = true;
    return tex;
  }

  function hash2(x, y) {
    const n = Math.sin(x * 127.1 + y * 311.7) * 43758.5453123;
    return n - Math.floor(n);
  }

  const texGrass = makeCanvasTex(256, 8, 8, true, (ctx, s) => {
    ctx.fillStyle = "#c4ccb2";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 160; i++) {
      ctx.fillStyle = i % 2 === 0 ? "rgba(130, 148, 114, 0.28)" : "rgba(218, 226, 198, 0.24)";
      ctx.beginPath();
      ctx.arc(hash2(i, 9) * s, hash2(i, 10) * s, 5 + hash2(i, 11) * 10, 0, Math.PI * 2);
      ctx.fill();
    }
    for (let i = 0; i < 2000; i++) {
      const x = hash2(i, 1) * s;
      const y = hash2(i, 2) * s;
      const g = Math.floor(156 + hash2(i, 3) * 58);
      const r = Math.floor(138 + hash2(i, 4) * 48);
      const b = Math.floor(118 + hash2(i, 5) * 40);
      ctx.fillStyle = `rgba(${r},${g},${b},0.46)`;
      ctx.fillRect(x, y, 2, 4 + hash2(i, 7) * 5);
    }
    for (let i = 0; i < 65; i++) {
      ctx.fillStyle = "rgba(132, 110, 86, 0.2)";
      ctx.beginPath();
      ctx.arc(hash2(i, 12) * s, hash2(i, 13) * s, 2 + hash2(i, 14) * 3, 0, Math.PI * 2);
      ctx.fill();
    }
  });

  const texGrassBump = makeCanvasTex(256, 8, 8, false, (ctx, s) => {
    ctx.fillStyle = "#7f7f7f";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 1400; i++) {
      const v = Math.floor(75 + hash2(i, 15) * 150);
      ctx.fillStyle = `rgb(${v},${v},${v})`;
      ctx.fillRect(hash2(i, 1) * s, hash2(i, 2) * s, 2, 5);
    }
  });

  const texCobble = makeCanvasTex(256, 3, 3, true, (ctx, s) => {
    ctx.fillStyle = "#524b43";
    ctx.fillRect(0, 0, s, s);
    const cols = 6, rows = 6;
    const cw = s / cols, ch = s / rows;
    for (let ry = 0; ry < rows; ry++) {
      for (let cx = 0; cx < cols; cx++) {
        const ox = (ry % 2) * (cw * 0.45);
        const shade = Math.floor(135 + hash2(cx, ry) * 38);
        ctx.fillStyle = `rgb(${shade},${shade - 4},${shade - 10})`;
        ctx.beginPath();
        ctx.roundRect ? ctx.roundRect((cx * cw + ox) % s + 2, ry * ch + 2, cw - 5, ch - 5, 7)
                      : ctx.fillRect((cx * cw + ox) % s + 2, ry * ch + 2, cw - 5, ch - 5);
        ctx.fill();
      }
    }
  });

  const texBark = makeCanvasTex(256, 2, 3, true, (ctx, s) => {
    ctx.fillStyle = "#784520";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 480; i++) {
      const x = hash2(i, 21) * s;
      const y = hash2(i, 22) * s;
      const dark = hash2(i, 23) > 0.42;
      ctx.fillStyle = dark ? "rgba(48, 22, 8, 0.55)" : "rgba(150, 92, 50, 0.42)";
      ctx.fillRect(x, y, 2 + hash2(i, 24) * 3, 18 + hash2(i, 25) * 38);
    }
  });

  const texBarkBump = makeCanvasTex(256, 2, 3, false, (ctx, s) => {
    ctx.fillStyle = "#808080";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 420; i++) {
      const v = hash2(i, 23) > 0.5 ? 40 : 215;
      ctx.fillStyle = `rgba(${v},${v},${v},0.62)`;
      ctx.fillRect(hash2(i, 21) * s, hash2(i, 22) * s, 3, 28);
    }
  });

  const texFoliage = makeCanvasTex(256, 2, 2, true, (ctx, s) => {
    ctx.fillStyle = "#62bf5a";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 950; i++) {
      const x = hash2(i, 31) * s;
      const y = hash2(i, 32) * s;
      const g = Math.floor(155 + hash2(i, 33) * 65);
      const r = Math.floor(68 + hash2(i, 37) * 42);
      ctx.fillStyle = `rgba(${r}, ${g}, 74, 0.65)`;
      ctx.beginPath();
      ctx.ellipse(x, y, 4 + hash2(i, 34) * 6, 2 + hash2(i, 35) * 3, hash2(i, 36) * Math.PI, 0, Math.PI * 2);
      ctx.fill();
    }
  });

  const texRock = makeCanvasTex(256, 4, 4, true, (ctx, s) => {
    ctx.fillStyle = "#8d98a6";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 650; i++) {
      const v = Math.floor(110 + hash2(i, 41) * 78);
      ctx.fillStyle = `rgba(${v},${v + 4},${v + 10},0.48)`;
      ctx.beginPath();
      ctx.arc(hash2(i, 42) * s, hash2(i, 43) * s, 3 + hash2(i, 44) * 10, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.strokeStyle = "rgba(42, 50, 62, 0.45)";
    ctx.lineWidth = 1.6;
    for (let j = 0; j < 26; j++) {
      ctx.beginPath();
      const sx = hash2(j, 45) * s, sy = hash2(j, 46) * s;
      ctx.moveTo(sx, sy);
      ctx.lineTo(sx + (hash2(j, 47) - 0.5) * 60, sy + (hash2(j, 48) - 0.5) * 40);
      ctx.stroke();
    }
  });

  const texWoodPlank = makeCanvasTex(256, 2, 2, true, (ctx, s) => {
    ctx.fillStyle = "#a86932";
    ctx.fillRect(0, 0, s, s);
    const planks = 4;
    const ph = s / planks;
    for (let p = 0; p < planks; p++) {
      const shade = (p % 2 === 0) ? "#b8763c" : "#9c5f2b";
      ctx.fillStyle = shade;
      ctx.fillRect(0, p * ph + 2, s, ph - 3);
      ctx.fillStyle = "rgba(56, 28, 10, 0.55)";
      ctx.fillRect(0, p * ph, s, 2);
      for (let g = 0; g < 35; g++) {
        ctx.fillStyle = "rgba(68, 34, 12, 0.2)";
        ctx.fillRect(0, p * ph + hash2(p, g) * ph, s, 1.5);
      }
      ctx.fillStyle = "#334155";
      ctx.beginPath();
      ctx.arc(14, p * ph + ph * 0.5, 2.5, 0, Math.PI * 2);
      ctx.arc(s - 14, p * ph + ph * 0.5, 2.5, 0, Math.PI * 2);
      ctx.fill();
    }
  });

  const texBrushedMetal = makeCanvasTex(256, 2, 2, true, (ctx, s) => {
    ctx.fillStyle = "#94a3b8";
    ctx.fillRect(0, 0, s, s);
    for (let y = 0; y < s; y += 2) {
      const v = Math.floor(132 + hash2(y, 55) * 52);
      ctx.fillStyle = `rgba(${v},${v + 5},${v + 12},0.45)`;
      ctx.fillRect(0, y, s, 1.5);
    }
    ctx.strokeStyle = "rgba(38, 48, 62, 0.45)";
    ctx.lineWidth = 3;
    ctx.strokeRect(4, 4, s - 8, s - 8);
    ctx.fillStyle = "#e2e8f0";
    [[14, 14], [s - 14, 14], [14, s - 14], [s - 14, s - 14]].forEach(([rx, ry]) => {
      ctx.beginPath();
      ctx.arc(rx, ry, 3.5, 0, Math.PI * 2);
      ctx.fill();
    });
  });

  const texHazard = makeCanvasTex(256, 3, 1, true, (ctx, s) => {
    ctx.fillStyle = "#eab308";
    ctx.fillRect(0, 0, s, s);
    ctx.fillStyle = "#1e293b";
    const step = 32;
    for (let x = -s; x < s * 2; x += step * 2) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x + step, 0);
      ctx.lineTo(x + step - s, s);
      ctx.lineTo(x - s, s);
      ctx.closePath();
      ctx.fill();
    }
  });

  const texSand = makeCanvasTex(256, 6, 6, true, (ctx, s) => {
    ctx.fillStyle = "#dfa955";
    ctx.fillRect(0, 0, s, s);
    for (let y = 0; y < s; y += 10) {
      ctx.fillStyle = "rgba(255, 228, 175, 0.2)";
      ctx.fillRect(0, y, s, 4);
      ctx.fillStyle = "rgba(166, 108, 34, 0.18)";
      ctx.fillRect(0, y + 5, s, 3);
    }
  });

  const texIce = makeCanvasTex(256, 6, 6, true, (ctx, s) => {
    ctx.fillStyle = "#b7e4fa";
    ctx.fillRect(0, 0, s, s);
    ctx.strokeStyle = "rgba(96, 185, 232, 0.65)";
    ctx.lineWidth = 2;
    for (let i = 0; i < 18; i++) {
      ctx.beginPath();
      const x0 = hash2(i, 71) * s, y0 = hash2(i, 72) * s;
      ctx.moveTo(x0, y0);
      ctx.lineTo(x0 + (hash2(i, 73) - 0.5) * 90, y0 + (hash2(i, 74) - 0.5) * 90);
      ctx.stroke();
    }
  });

  const texBasalt = makeCanvasTex(256, 6, 6, true, (ctx, s) => {
    ctx.fillStyle = "#483c3a";
    ctx.fillRect(0, 0, s, s);
    ctx.strokeStyle = "rgba(249, 115, 22, 0.65)";
    ctx.lineWidth = 2.2;
    for (let i = 0; i < 14; i++) {
      ctx.beginPath();
      const x0 = hash2(i, 81) * s, y0 = hash2(i, 82) * s;
      ctx.moveTo(x0, y0);
      ctx.lineTo(x0 + (hash2(i, 83) - 0.5) * 70, y0 + (hash2(i, 84) - 0.5) * 70);
      ctx.stroke();
    }
  });

  const texPlaid = makeCanvasTex(256, 1.5, 1.5, true, (ctx, s) => {
    ctx.fillStyle = "#c82317";
    ctx.fillRect(0, 0, s, s);
    const step = s / 4;
    ctx.fillStyle = "rgba(22, 10, 8, 0.64)";
    for (let i = 0; i < 4; i += 2) {
      ctx.fillRect(i * step, 0, step, s);
      ctx.fillRect(0, i * step, s, step);
    }
    ctx.fillStyle = "rgba(255, 115, 45, 0.28)";
    for (let i = 1; i < 4; i += 2) {
      ctx.fillRect(i * step, 0, 4, s);
      ctx.fillRect(0, i * step, s, 4);
    }
  });

  // Industrial Engineering Textures (panel seams/rivets, louvers, moving belt cleats, electrical cabinets)
  const texPanelMetal = makeCanvasTex(256, 1, 1, true, (ctx, s) => {
    const grad = ctx.createLinearGradient(0, 0, 0, s);
    grad.addColorStop(0, "#dbe4ee");
    grad.addColorStop(0.5, "#cbd5e1");
    grad.addColorStop(1, "#94a3b8");
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, s, s);
    ctx.strokeStyle = "rgba(30, 41, 59, 0.38)";
    ctx.lineWidth = 3;
    ctx.strokeRect(4, 4, s - 8, s - 8);
    ctx.strokeRect(4, s * 0.5, s - 8, 1);
    ctx.strokeStyle = "rgba(255, 255, 255, 0.32)";
    ctx.lineWidth = 2;
    ctx.strokeRect(7, 7, s - 14, s - 14);
    for (let i = 0; i < 90; i++) {
      ctx.fillStyle = i % 2 === 0 ? "rgba(71, 85, 105, 0.08)" : "rgba(255, 255, 255, 0.07)";
      ctx.fillRect(hash2(i, 91) * s, hash2(i, 92) * s, 14 + hash2(i, 93) * 26, 2);
    }
    [[16, 16], [s - 16, 16], [16, s - 16], [s - 16, s - 16], [s * 0.5, 16], [s * 0.5, s - 16]].forEach(([rx, ry]) => {
      ctx.fillStyle = "#475569";
      ctx.beginPath();
      ctx.arc(rx, ry, 4.5, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = "#e2e8f0";
      ctx.beginPath();
      ctx.arc(rx - 1, ry - 1, 2.2, 0, Math.PI * 2);
      ctx.fill();
    });
  });

  const texVentLouvers = makeCanvasTex(128, 1, 1, true, (ctx, s) => {
    ctx.fillStyle = "#b8c4d0";
    ctx.fillRect(0, 0, s, s);
    ctx.strokeStyle = "#334155";
    ctx.lineWidth = 4;
    ctx.strokeRect(4, 4, s - 8, s - 8);
    for (let y = 16; y < s - 14; y += 14) {
      ctx.fillStyle = "#0f172a";
      ctx.fillRect(14, y, s - 28, 7);
      ctx.fillStyle = "#e2e8f0";
      ctx.fillRect(14, y + 7, s - 28, 3);
    }
  });

  const texConveyorBelt = makeCanvasTex(128, 1, 4, true, (ctx, s) => {
    ctx.fillStyle = "#1c2430";
    ctx.fillRect(0, 0, s, s);
    for (let y = 0; y < s; y += 32) {
      ctx.fillStyle = "#334155";
      ctx.fillRect(8, y + 6, s - 16, 8);
      ctx.fillStyle = "#0f172a";
      ctx.fillRect(8, y + 14, s - 16, 4);
      ctx.fillStyle = "#475569";
      ctx.fillRect(4, y + 2, 6, 28);
      ctx.fillRect(s - 10, y + 2, 6, 28);
    }
  });

  const texControlCabinet = makeCanvasTex(128, 1, 1, true, (ctx, s) => {
    ctx.fillStyle = "#2b6cb0";
    ctx.fillRect(0, 0, s, s);
    ctx.strokeStyle = "#1e3a5f";
    ctx.lineWidth = 4;
    ctx.strokeRect(4, 4, s - 8, s - 8);
    ctx.fillStyle = "#0f172a";
    ctx.fillRect(16, 16, s - 32, 34);
    ctx.fillStyle = "#22d3ee";
    ctx.fillRect(22, 24, 34, 10);
    ctx.fillStyle = "#22c55e";
    ctx.fillRect(64, 24, 14, 10);
    ctx.fillStyle = "#f59e0b";
    ctx.beginPath();
    ctx.moveTo(s * 0.5, 62);
    ctx.lineTo(s * 0.5 + 20, 96);
    ctx.lineTo(s * 0.5 - 20, 96);
    ctx.closePath();
    ctx.fill();
    ctx.fillStyle = "#0f172a";
    ctx.fillRect(s * 0.5 - 2, 74, 4, 12);
    ctx.fillRect(s * 0.5 - 2, 89, 4, 4);
  });

  const texContactShadow = (() => {
    const c = document.createElement("canvas");
    c.width = 128;
    c.height = 128;
    const ctx = c.getContext("2d");
    const g = ctx.createRadialGradient(64, 64, 6, 64, 64, 62);
    g.addColorStop(0, "rgba(4, 12, 8, 0.58)");
    g.addColorStop(0.52, "rgba(4, 12, 8, 0.28)");
    g.addColorStop(1, "rgba(4, 12, 8, 0.0)");
    ctx.fillStyle = g;
    ctx.fillRect(0, 0, 128, 128);
    const t = new THREE.CanvasTexture(c);
    t.needsUpdate = true;
    return t;
  })();

  // Helper PBR & Unlit Materials (linearized sRGB hex colors for accurate r128 tone mapping)
  function pbrMat(color, opts) {
    const merged = Object.assign({
      color,
      roughness: 0.72,
      metalness: 0.04
    }, opts || {});
    merged.color = linCol(merged.color);
    if (merged.emissive !== undefined) merged.emissive = linCol(merged.emissive);
    return new THREE.MeshStandardMaterial(merged);
  }

  function metalMat(color, opts) {
    const merged = Object.assign({
      color,
      roughness: 0.3,
      metalness: 0.52
    }, opts || {});
    if (merged.metalness > 0.62) merged.metalness = 0.58;
    merged.color = linCol(merged.color);
    if (merged.emissive !== undefined) merged.emissive = linCol(merged.emissive);
    return new THREE.MeshStandardMaterial(merged);
  }

  function basicMat(color, opts) {
    const merged = Object.assign({ color }, opts || {});
    merged.color = linCol(merged.color);
    return new THREE.MeshBasicMaterial(merged);
  }

  // Sculpted Geometry Helpers
  function makeCapsuleGeo(r, len, radialSegs) {
    const pts = [];
    const half = len * 0.5;
    const capSteps = 5;
    for (let i = 0; i <= capSteps; i++) {
      const a = -Math.PI / 2 + (i / capSteps) * (Math.PI / 2);
      pts.push(new THREE.Vector2(Math.max(0.001, Math.cos(a) * r), -half + Math.sin(a) * r));
    }
    for (let i = 0; i <= capSteps; i++) {
      const a = (i / capSteps) * (Math.PI / 2);
      pts.push(new THREE.Vector2(Math.max(0.001, Math.cos(a) * r), half + Math.sin(a) * r));
    }
    return new THREE.LatheGeometry(pts, radialSegs || 10);
  }

  function makeOrganicGeo(radius, detail, noiseAmp, seed) {
    const geo = new THREE.IcosahedronGeometry(radius, detail || 1);
    const pos = geo.attributes.position;
    for (let i = 0; i < pos.count; i++) {
      const vx = pos.getX(i), vy = pos.getY(i), vz = pos.getZ(i);
      const n = Math.sin(vx * 3.1 + seed) * Math.cos(vy * 3.7 - seed) * Math.sin(vz * 2.9 + seed * 1.3);
      const sc = 1 + n * (noiseAmp || 0.22);
      pos.setXYZ(i, vx * sc, vy * sc, vz * sc);
    }
    geo.computeVertexNormals();
    return geo;
  }

  function makeRoundedBoxGeo(w, h, d, r) {
    const s = new THREE.Shape();
    const hw = w * 0.5 - r, hh = d * 0.5 - r;
    s.moveTo(-hw, -hh - r);
    s.lineTo(hw, -hh - r);
    s.quadraticCurveTo(hw + r, -hh - r, hw + r, -hh);
    s.lineTo(hw + r, hh);
    s.quadraticCurveTo(hw + r, hh + r, hw, hh + r);
    s.lineTo(-hw, hh + r);
    s.quadraticCurveTo(-hw - r, hh + r, -hw - r, hh);
    s.lineTo(-hw - r, -hh);
    s.quadraticCurveTo(-hw - r, -hh - r, -hw, -hh - r);
    const g = new THREE.ExtrudeGeometry(s, {
      depth: Math.max(0.02, h - r * 2),
      bevelEnabled: true,
      bevelThickness: r,
      bevelSize: r,
      bevelSegments: 2
    });
    g.rotateX(-Math.PI / 2);
    g.center();
    return g;
  }

  function makeGearGeo(outerR, innerR, teeth, thickness) {
    const shape = new THREE.Shape();
    const step = (Math.PI * 2) / (teeth * 2);
    for (let i = 0; i < teeth * 2; i++) {
      const a0 = i * step;
      const a1 = a0 + step * 0.65;
      const r = (i % 2 === 0) ? outerR : innerR;
      if (i === 0) shape.moveTo(Math.cos(a0) * r, Math.sin(a0) * r);
      else shape.lineTo(Math.cos(a0) * r, Math.sin(a0) * r);
      shape.lineTo(Math.cos(a1) * r, Math.sin(a1) * r);
    }
    shape.closePath();
    const g = new THREE.ExtrudeGeometry(shape, {
      depth: thickness,
      bevelEnabled: true,
      bevelThickness: 0.02,
      bevelSize: 0.02,
      bevelSegments: 1
    });
    g.center();
    return g;
  }

  function makeSawbladeGeo(radius, teeth, thickness) {
    const shape = new THREE.Shape();
    const step = (Math.PI * 2) / teeth;
    for (let i = 0; i < teeth; i++) {
      const a0 = i * step;
      const a1 = a0 + step * 0.55;
      const a2 = a0 + step * 0.92;
      if (i === 0) shape.moveTo(Math.cos(a0) * radius * 0.78, Math.sin(a0) * radius * 0.78);
      else shape.lineTo(Math.cos(a0) * radius * 0.78, Math.sin(a0) * radius * 0.78);
      shape.lineTo(Math.cos(a1) * radius, Math.sin(a1) * radius);
      shape.lineTo(Math.cos(a2) * radius * 0.78, Math.sin(a2) * radius * 0.78);
    }
    shape.closePath();
    const g = new THREE.ExtrudeGeometry(shape, { depth: thickness, bevelEnabled: false });
    g.center();
    return g;
  }

  // Shared PBR base geometries
  const boxGeo = new THREE.BoxGeometry(1, 1, 1);
  const coneGeo = new THREE.ConeGeometry(1, 1, 10);
  const cylGeo = new THREE.CylinderGeometry(1, 1, 1, 14);
  const sphereGeo = new THREE.SphereGeometry(1, 14, 12);
  const octaGeo = new THREE.OctahedronGeometry(1, 0);
  const organicRockGeoA = makeOrganicGeo(1.0, 2, 0.24, 1.4);
  const organicRockGeoB = makeOrganicGeo(1.0, 2, 0.26, 4.7);
  const organicCanopyGeo = makeOrganicGeo(1.0, 1, 0.22, 2.9);
  const crystalPrismGeo = new THREE.CylinderGeometry(0.14, 0.34, 1.35, 6);
  const wheelGeo = new THREE.CylinderGeometry(0.32, 0.32, 0.22, 16);
  wheelGeo.rotateZ(Math.PI / 2);
  const gearWheelGeo = makeGearGeo(0.45, 0.34, 10, 0.14);
  const sawbladeGeo = makeSawbladeGeo(0.55, 16, 0.05);

  const shadowDiscGeo = new THREE.PlaneGeometry(2.1, 2.1);
  const shadowDiscMat = new THREE.MeshBasicMaterial({
    map: texContactShadow,
    transparent: true,
    opacity: 0.92,
    depthWrite: false
  });

  function addContactShadow(parent, radius) {
    const sh = new THREE.Mesh(shadowDiscGeo, shadowDiscMat);
    sh.rotation.x = -Math.PI / 2;
    sh.position.y = 0.024;
    sh.scale.set(radius, radius, 1);
    parent.add(sh);
    return sh;
  }

  // Shared Reusable Industrial Geometries & PBR Materials (optimized for mobile GPU batching)
  const indFlangeGeo = new THREE.CylinderGeometry(0.145, 0.145, 0.055, 10);
  const indBoltGeo = new THREE.CylinderGeometry(0.032, 0.032, 0.04, 6);
  const indMotorGeo = new THREE.CylinderGeometry(0.19, 0.19, 0.44, 12);
  indMotorGeo.rotateZ(Math.PI / 2);
  const indTankEndGeo = new THREE.SphereGeometry(1, 12, 8, 0, Math.PI * 2, 0, Math.PI * 0.5);
  const indPipeJointGeo = new THREE.SphereGeometry(1, 10, 8);
  const indRailPostGeo = new THREE.CylinderGeometry(0.028, 0.028, 0.38, 6);
  const indStepGeo = new THREE.BoxGeometry(0.28, 0.028, 0.045);

  const indMats = {
    steelDark: metalMat(0x273444, { map: texBrushedMetal, roughness: 0.44, metalness: 0.52 }),
    steelMid: metalMat(0x4b5b6e, { map: texPanelMetal, roughness: 0.36, metalness: 0.55 }),
    steelLight: metalMat(0xc4d0dc, { map: texPanelMetal, roughness: 0.3, metalness: 0.45 }),
    concreteBase: pbrMat(0x64748b, { map: texPanelMetal, roughness: 0.72, metalness: 0.14 }),
    deckSlate: pbrMat(0x364152, { map: texBrushedMetal, roughness: 0.48, metalness: 0.38 }),
    domeCream: pbrMat(0xf1efe7, { map: texPanelMetal, roughness: 0.38, metalness: 0.14 }),
    chrome: metalMat(0xe2e8f0, { roughness: 0.18, metalness: 0.62 }),
    panelWhite: pbrMat(0xeaeff5, { map: texPanelMetal, roughness: 0.4, metalness: 0.16 }),
    panelBlue: pbrMat(0x1e6091, { map: texPanelMetal, roughness: 0.38, metalness: 0.25 }),
    panelTeal: pbrMat(0x0f766e, { map: texPanelMetal, roughness: 0.4, metalness: 0.24 }),
    turbineGreen: pbrMat(0x22c55e, { map: texPanelMetal, roughness: 0.36, metalness: 0.22 }),
    safetyYellow: pbrMat(0xf59e0b, { roughness: 0.34, metalness: 0.24 }),
    indOrange: pbrMat(0xea580c, { map: texPanelMetal, roughness: 0.38, metalness: 0.24 }),
    copper: metalMat(0xd97706, { roughness: 0.26, metalness: 0.6 }),
    brass: metalMat(0xfbbf24, { roughness: 0.24, metalness: 0.6 }),
    rubberDark: pbrMat(0x181f29, { roughness: 0.88, metalness: 0.04 }),
    vents: pbrMat(0xffffff, { map: texVentLouvers, roughness: 0.48, metalness: 0.3 }),
    cabinet: pbrMat(0xffffff, { map: texControlCabinet, roughness: 0.42, metalness: 0.22 }),
    beltMat: pbrMat(0xffffff, { map: texConveyorBelt, roughness: 0.84, metalness: 0.08 }),
    glowCyan: pbrMat(0x67e8f9, { emissive: 0x06b6d4, emissiveIntensity: 0.95, roughness: 0.18 }),
    coolantChute: pbrMat(0x38bdf8, { emissive: 0x0ea5e9, emissiveIntensity: 0.72, roughness: 0.15, transparent: true, opacity: 0.88 }),
    glowAmber: pbrMat(0xfef08a, { emissive: 0xf59e0b, emissiveIntensity: 0.95, roughness: 0.2 }),
    glowGreen: pbrMat(0x4ade80, { emissive: 0x16a34a, emissiveIntensity: 0.85, roughness: 0.25 }),
    hazardDeck: pbrMat(0xffffff, { map: texHazard, roughness: 0.52, metalness: 0.18 })
  };

  const _boltDummy = new THREE.Object3D();
  function addBoltsInstanced(parent, coords, mat) {
    if (!coords || !coords.length) return null;
    const im = new THREE.InstancedMesh(indBoltGeo, mat || indMats.chrome, coords.length);
    for (let i = 0; i < coords.length; i++) {
      const c = coords[i];
      _boltDummy.position.set(c[0], c[1], c[2]);
      _boltDummy.rotation.set(c[3] || 0, c[4] || 0, c[5] || 0);
      _boltDummy.scale.setScalar(c[6] || 1);
      _boltDummy.updateMatrix();
      im.setMatrixAt(i, _boltDummy.matrix);
    }
    im.instanceMatrix.needsUpdate = true;
    parent.add(im);
    return im;
  }

  function addPipeRun(parent, pts, radius, mat, flangeMat) {
    const curve = new THREE.CatmullRomCurve3(pts.map(([x, y, z]) => new THREE.Vector3(x, y, z)));
    const pipe = new THREE.Mesh(
      new THREE.TubeGeometry(curve, Math.max(8, pts.length * 4), radius, 8, false),
      mat || indMats.steelLight
    );
    pipe.castShadow = true;
    parent.add(pipe);
    const flMat = flangeMat || indMats.steelDark;
    const flScale = radius / 0.08;
    for (let i = 0; i < pts.length; i++) {
      const pt = pts[i];
      const fl = new THREE.Mesh(indFlangeGeo, flMat);
      fl.position.set(pt[0], pt[1], pt[2]);
      fl.scale.set(flScale, 1, flScale);
      if (i > 0) {
        const prev = pts[i - 1];
        const dx = pt[0] - prev[0], dy = pt[1] - prev[1], dz = pt[2] - prev[2];
        if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) >= Math.abs(dz)) fl.rotation.z = Math.PI / 2;
        else if (Math.abs(dz) > Math.abs(dy)) fl.rotation.x = Math.PI / 2;
      } else if (pts.length > 1) {
        const nxt = pts[1];
        const dx = nxt[0] - pt[0], dy = nxt[1] - pt[1], dz = nxt[2] - pt[2];
        if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) >= Math.abs(dz)) fl.rotation.z = Math.PI / 2;
        else if (Math.abs(dz) > Math.abs(dy)) fl.rotation.x = Math.PI / 2;
      }
      parent.add(fl);
    }
    return pipe;
  }

  function addSafetyRail(parent, x1, z1, x2, z2, y, mat, railR) {
    const len = Math.hypot(x2 - x1, z2 - z1);
    if (len < 0.1) return;
    const rMat = mat || indMats.safetyYellow;
    const ang = Math.atan2(x2 - x1, z2 - z1);
    const barRadius = railR || 0.036;
    const posts = Math.max(2, Math.round(len / 0.65) + 1);
    for (let i = 0; i < posts; i++) {
      const t = i / (posts - 1);
      const post = new THREE.Mesh(indRailPostGeo, rMat);
      post.scale.set(barRadius / 0.028, 1.05, barRadius / 0.028);
      post.position.set(x1 + (x2 - x1) * t, y + 0.2, z1 + (z2 - z1) * t);
      parent.add(post);
    }
    [0.18, 0.38].forEach((ry) => {
      const bar = new THREE.Mesh(new THREE.CylinderGeometry(barRadius, barRadius, len, 8), rMat);
      bar.position.set((x1 + x2) * 0.5, y + ry, (z1 + z2) * 0.5);
      bar.rotation.y = ang;
      bar.rotation.x = Math.PI / 2;
      parent.add(bar);
    });
  }

  function addLadder(parent, x, y0, y1, z, rotY, mat) {
    const lg = new THREE.Group();
    lg.position.set(x, 0, z);
    lg.rotation.y = rotY || 0;
    const h = y1 - y0;
    const lMat = mat || indMats.safetyYellow;
    [-0.14, 0.14].forEach((sx) => {
      const rail = new THREE.Mesh(new THREE.CylinderGeometry(0.024, 0.024, h, 6), lMat);
      rail.position.set(sx, y0 + h * 0.5, 0);
      lg.add(rail);
    });
    const steps = Math.max(3, Math.floor(h / 0.24));
    for (let i = 0; i < steps; i++) {
      const rung = new THREE.Mesh(indStepGeo, lMat);
      rung.position.set(0, y0 + 0.14 + i * (h / steps), 0);
      lg.add(rung);
    }
    parent.add(lg);
    return lg;
  }

  function addMotorPod(parent, x, y, z, scale, mat) {
    const mg = new THREE.Group();
    mg.position.set(x, y, z);
    mg.scale.setScalar(scale || 1);
    const body = new THREE.Mesh(indMotorGeo, mat || indMats.panelBlue);
    body.castShadow = true;
    mg.add(body);
    for (let f = -0.14; f <= 0.14; f += 0.07) {
      const fin = new THREE.Mesh(new THREE.CylinderGeometry(0.215, 0.215, 0.022, 12), indMats.steelDark);
      fin.rotation.z = Math.PI / 2;
      fin.position.x = f;
      mg.add(fin);
    }
    const jBox = new THREE.Mesh(makeRoundedBoxGeo(0.16, 0.12, 0.14, 0.02), indMats.steelDark);
    jBox.position.set(0, 0.21, 0);
    mg.add(jBox);
    parent.add(mg);
    return mg;
  }

  function addPressureTank(parent, x, y, z, r, len, horizontal, bodyMat, bandMat) {
    const tg = new THREE.Group();
    tg.position.set(x, y, z);
    if (horizontal) tg.rotation.z = Math.PI / 2;
    const cyl = new THREE.Mesh(new THREE.CylinderGeometry(r, r, len, 14), bodyMat || indMats.panelWhite);
    cyl.castShadow = true;
    tg.add(cyl);
    const topCap = new THREE.Mesh(indTankEndGeo, bandMat || indMats.steelLight);
    topCap.scale.setScalar(r);
    topCap.position.y = len * 0.5;
    tg.add(topCap);
    const botCap = new THREE.Mesh(indTankEndGeo, bandMat || indMats.steelLight);
    botCap.scale.set(r, -r, r);
    botCap.position.y = -len * 0.5;
    tg.add(botCap);
    [-len * 0.32, len * 0.32].forEach((by) => {
      const strap = new THREE.Mesh(new THREE.CylinderGeometry(r * 1.055, r * 1.055, 0.065, 14), bandMat || indMats.indOrange);
      strap.position.y = by;
      tg.add(strap);
    });
    parent.add(tg);
    return tg;
  }

  // Multi-Tier Industrial Plant Architectural Helpers (Stepped Plinths, Cantilever Decks, Domed Silos & Chutes)
  function addSteppedPlinth(parent, w, d, withFrontSteps, baseMat) {
    const plinth = new THREE.Mesh(makeRoundedBoxGeo(w, 0.28, d, 0.05), baseMat || indMats.concreteBase);
    plinth.position.y = 0.22;
    plinth.castShadow = true;
    plinth.receiveShadow = true;
    parent.add(plinth);
    [[-w * 0.44, -d * 0.44], [w * 0.44, -d * 0.44], [-w * 0.44, d * 0.44], [w * 0.44, d * 0.44]].forEach(([bx, bz]) => {
      const pier = new THREE.Mesh(makeRoundedBoxGeo(0.32, 0.34, 0.32, 0.04), indMats.steelDark);
      pier.position.set(bx, 0.24, bz);
      parent.add(pier);
    });
    if (withFrontSteps) {
      for (let si = 0; si < 3; si++) {
        const step = new THREE.Mesh(makeRoundedBoxGeo(0.92, 0.1, 0.18, 0.02), indMats.steelLight);
        step.position.set(0, 0.11 + si * 0.08, d * 0.5 + 0.16 - si * 0.11);
        step.receiveShadow = true;
        parent.add(step);
      }
    }
    return plinth;
  }

  function addCantileverDeck(parent, w, d, y, baseY, railMat, openFrontGap) {
    const slab = new THREE.Mesh(makeRoundedBoxGeo(w, 0.16, d, 0.05), indMats.deckSlate);
    slab.position.y = y;
    slab.castShadow = true;
    slab.receiveShadow = true;
    parent.add(slab);
    const colH = Math.max(0.4, y - baseY);
    [[-w * 0.42, -d * 0.42], [w * 0.42, -d * 0.42], [-w * 0.42, d * 0.42], [w * 0.42, d * 0.42]].forEach(([cx, cz]) => {
      const col = new THREE.Mesh(makeRoundedBoxGeo(0.18, colH, 0.18, 0.03), indMats.steelDark);
      col.position.set(cx, baseY + colH * 0.5, cz);
      col.castShadow = true;
      parent.add(col);
    });
    const rY = y + 0.07;
    const hw = w * 0.46, hd = d * 0.46;
    const rMat = railMat || indMats.safetyYellow;
    addSafetyRail(parent, -hw, -hd, hw, -hd, rY, rMat, 0.042);
    addSafetyRail(parent, -hw, -hd, -hw, hd, rY, rMat, 0.042);
    addSafetyRail(parent, hw, -hd, hw, hd, rY, rMat, 0.042);
    if (openFrontGap) {
      addSafetyRail(parent, -hw, hd, -0.45, hd, rY, rMat, 0.042);
      addSafetyRail(parent, 0.45, hd, hw, hd, rY, rMat, 0.042);
    } else {
      addSafetyRail(parent, -hw, hd, hw, hd, rY, rMat, 0.042);
    }
    return slab;
  }

  function addSiloSuperstructure(parent, x, y, z, r, h, bodyMat, domeMat, glowMat) {
    const sg = new THREE.Group();
    sg.position.set(x, y, z);
    const cyl = new THREE.Mesh(new THREE.CylinderGeometry(r, r * 1.05, h, 18), bodyMat || indMats.domeCream);
    cyl.position.y = h * 0.5;
    cyl.castShadow = true;
    cyl.receiveShadow = true;
    sg.add(cyl);
    const collar = new THREE.Mesh(new THREE.CylinderGeometry(r * 1.06, r * 1.06, 0.14, 18), indMats.steelDark);
    collar.position.y = h;
    sg.add(collar);
    const dome = new THREE.Mesh(new THREE.CylinderGeometry(r * 0.36, r * 1.02, r * 0.52, 18), domeMat || indMats.domeCream);
    dome.position.y = h + r * 0.26;
    dome.castShadow = true;
    sg.add(dome);
    const hatch = new THREE.Mesh(new THREE.CylinderGeometry(r * 0.38, r * 0.38, 0.18, 14), indMats.steelMid);
    hatch.position.y = h + r * 0.54;
    sg.add(hatch);
    const hatchGlow = new THREE.Mesh(new THREE.CylinderGeometry(r * 0.26, r * 0.26, 0.06, 12), glowMat || indMats.glowCyan);
    hatchGlow.position.y = h + r * 0.62;
    sg.add(hatchGlow);
    for (let ri = 0; ri < 6; ri++) {
      const ang = ri * (Math.PI / 3);
      const rib = new THREE.Mesh(makeRoundedBoxGeo(0.11, h * 0.92, 0.14, 0.02), indMats.steelMid);
      rib.position.set(Math.cos(ang) * r * 1.01, h * 0.48, Math.sin(ang) * r * 1.01);
      rib.rotation.y = -ang;
      sg.add(rib);
    }
    [0.34, 0.72].forEach((frac) => {
      const ring = new THREE.Mesh(new THREE.TorusGeometry(r * 1.04, 0.048, 8, 24), glowMat || indMats.glowCyan);
      ring.rotation.x = Math.PI / 2;
      ring.position.y = h * frac;
      sg.add(ring);
    });
    parent.add(sg);
    return sg;
  }

  function addSideBalconyTower(parent, x, z, h, r, bodyMat, railMat, glowMat) {
    const tg = new THREE.Group();
    tg.position.set(x, 0, z);
    const cyl = new THREE.Mesh(new THREE.CylinderGeometry(r, r * 1.04, h, 14), bodyMat || indMats.steelMid);
    cyl.position.y = h * 0.5;
    cyl.castShadow = true;
    tg.add(cyl);
    const band = new THREE.Mesh(new THREE.CylinderGeometry(r * 1.06, r * 1.06, 0.18, 14), indMats.indOrange);
    band.position.y = h * 0.55;
    tg.add(band);
    const win = new THREE.Mesh(makeRoundedBoxGeo(r * 0.75, 0.16, r * 0.25, 0.02), glowMat || indMats.glowCyan);
    win.position.set(0, h * 0.74, r * 0.92);
    tg.add(win);
    const deck = new THREE.Mesh(new THREE.CylinderGeometry(r * 1.15, r * 1.1, 0.12, 14), indMats.deckSlate);
    deck.position.y = h + 0.05;
    deck.castShadow = true;
    tg.add(deck);
    const rail = new THREE.Mesh(new THREE.TorusGeometry(r * 1.08, 0.036, 8, 18), railMat || indMats.safetyYellow);
    rail.rotation.x = Math.PI / 2;
    rail.position.y = h + 0.32;
    tg.add(rail);
    const propCrate = new THREE.Mesh(makeRoundedBoxGeo(r * 0.62, 0.26, r * 0.42, 0.04), indMats.indOrange);
    propCrate.position.set(0, h + 0.24, 0);
    tg.add(propCrate);
    parent.add(tg);
    return tg;
  }

  function addCoolantChute(parent, x, y, z, rotY, mat) {
    const cg = new THREE.Group();
    cg.position.set(x, y, z);
    cg.rotation.y = rotY || 0;
    const cMat = mat || indMats.coolantChute;
    for (let si = 0; si < 4; si++) {
      const seg = new THREE.Mesh(makeRoundedBoxGeo(0.56 - si * 0.03, 0.06, 0.34, 0.02), cMat);
      seg.position.set(0, -si * 0.17, si * 0.25);
      seg.rotation.x = 0.42 + si * 0.14;
      cg.add(seg);
    }
    parent.add(cg);
    return cg;
  }

  function addElbowStack(parent, x, y, z, h, r, bodyMat, collarMat) {
    const eg = new THREE.Group();
    eg.position.set(x, y, z);
    const shaft = new THREE.Mesh(new THREE.CylinderGeometry(r * 0.85, r, h, 12), bodyMat || indMats.steelDark);
    shaft.position.y = h * 0.5;
    shaft.castShadow = true;
    eg.add(shaft);
    const midCollar = new THREE.Mesh(new THREE.CylinderGeometry(r * 1.15, r * 1.15, 0.1, 12), collarMat || indMats.indOrange);
    midCollar.position.y = h * 0.65;
    eg.add(midCollar);
    addPipeRun(eg, [[0, h * 0.96, 0], [0, h + 0.28, 0], [r * 1.6, h + 0.42, 0]], r * 0.72, indMats.steelMid, collarMat || indMats.steelDark);
    parent.add(eg);
    return eg;
  }

  // Helper to build a wheeled rubber tire + metallic alloy rim
  function createWheelMesh(r, w) {
    const wg = new THREE.Group();
    const tire = new THREE.Mesh(
      new THREE.CylinderGeometry(r, r, w, 16),
      pbrMat(0x1e293b, { roughness: 0.88, metalness: 0.05 })
    );
    tire.rotation.z = Math.PI / 2;
    tire.castShadow = true;
    wg.add(tire);
    const hub = new THREE.Mesh(
      new THREE.CylinderGeometry(r * 0.56, r * 0.56, w * 1.04, 12),
      metalMat(0xcbd5e1, { roughness: 0.3, metalness: 0.6 })
    );
    hub.rotation.z = Math.PI / 2;
    wg.add(hub);
    return wg;
  }

  // Helper to build a rounded industrial storage crate / product bar
  function createProductCrate(colHex, isBar) {
    if (isBar) {
      const m = new THREE.Mesh(makeRoundedBoxGeo(0.38, 0.14, 0.2, 0.03), metalMat(colHex, { roughness: 0.22, metalness: 0.6 }));
      m.castShadow = true;
      return m;
    }
    const g = new THREE.Group();
    const body = new THREE.Mesh(makeRoundedBoxGeo(0.38, 0.32, 0.38, 0.04), pbrMat(colHex, { map: texWoodPlank, roughness: 0.58 }));
    body.castShadow = true;
    g.add(body);
    return g;
  }

  // =========================================================================
  // 7. WORLD TERRAIN, CHUNK STREAMING, MOUNTAINS, FOLIAGE, BRIDGES & GATES
  // =========================================================================
  const worldGroup = new THREE.Group();
  scene.add(worldGroup);

  const biomeChunkGroups = {};
  function getBiomeChunk(biomeId) {
    if (!biomeChunkGroups[biomeId]) {
      const cg = new THREE.Group();
      cg.name = "chunk_" + biomeId;
      worldGroup.add(cg);
      biomeChunkGroups[biomeId] = cg;
    }
    return biomeChunkGroups[biomeId];
  }
  BIOMES.forEach((b) => {
    const cg = getBiomeChunk(b.id);
    cg.visible = (b.id === "forest" || Math.hypot(b.cx, b.cz) < 78);
  });

  // Shimmering Lagoon Water
  const waterMesh = new THREE.Mesh(
    new THREE.PlaneGeometry(520, 520),
    pbrMat(0x1f85ad, { roughness: 0.2, metalness: 0.25 })
  );
  waterMesh.rotation.x = -Math.PI / 2;
  waterMesh.position.y = -0.88;
  worldGroup.add(waterMesh);

  // Sculpted Distant Alpine Mountain Range
  for (let m = 0; m < 24; m++) {
    const ang = (m / 24) * Math.PI * 2;
    const dist = 138 + (m % 3) * 12;
    const peakH = 18 + (m % 4) * 6;
    const peakR = 13 + (m % 3) * 4;
    const mtn = new THREE.Group();
    mtn.position.set(Math.cos(ang) * dist, -0.9, Math.sin(ang) * dist);
    const mGeo = new THREE.ConeGeometry(peakR, peakH, 9, 3);
    const mPos = mGeo.attributes.position;
    for (let vi = 0; vi < mPos.count; vi++) {
      const py = mPos.getY(vi);
      if (py < peakH * 0.42 && py > -peakH * 0.45) {
        mPos.setX(vi, mPos.getX(vi) * (1 + hash2(vi, m) * 0.28 - 0.14));
        mPos.setZ(vi, mPos.getZ(vi) * (1 + hash2(m, vi) * 0.28 - 0.14));
      }
    }
    mGeo.computeVertexNormals();
    const body = new THREE.Mesh(mGeo, pbrMat(m % 2 === 0 ? 0x3b7a57 : 0x566575, { map: texRock, roughness: 0.85 }));
    body.position.y = peakH * 0.5;
    mtn.add(body);
    const cap = new THREE.Mesh(new THREE.ConeGeometry(peakR * 0.36, peakH * 0.35, 9), pbrMat(0xeef5fc, { roughness: 0.45 }));
    cap.position.y = peakH * 0.82;
    mtn.add(cap);
    worldGroup.add(mtn);
  }

  // Additional biome textures for Toxic Swamp & Ancient Machine World
  const texSwamp = makeCanvasTex(256, 6, 6, true, (ctx, s) => {
    ctx.fillStyle = "#486933";
    ctx.fillRect(0, 0, s, s);
    for (let i = 0; i < 65; i++) {
      ctx.fillStyle = i % 3 === 0 ? "rgba(132, 204, 22, 0.24)" : "rgba(38, 56, 26, 0.36)";
      ctx.beginPath();
      ctx.arc(hash2(i, 101) * s, hash2(i, 102) * s, 6 + hash2(i, 103) * 14, 0, Math.PI * 2);
      ctx.fill();
    }
  });
  const texTech = makeCanvasTex(256, 6, 6, true, (ctx, s) => {
    ctx.fillStyle = "#3c4d61";
    ctx.fillRect(0, 0, s, s);
    ctx.strokeStyle = "rgba(56, 189, 248, 0.35)";
    ctx.lineWidth = 3;
    for (let g = 0; g < s; g += 64) {
      ctx.strokeRect(g + 2, 2, 60, 60);
      ctx.strokeRect(2, g + 2, 60, 60);
    }
    ctx.strokeStyle = "rgba(15, 23, 42, 0.45)";
    ctx.lineWidth = 2;
    ctx.strokeRect(8, 8, s - 16, s - 16);
  });

  const extraEnvTex = window.BiomeEnvBuilder
    ? window.BiomeEnvBuilder.buildEnvironmentTextures(THREE, makeCanvasTex)
    : {};

  // Vertex-Colored Sculpted Biome Terrains across all 12 World Areas
  const biomeFloorTex = {
    forest: texGrass,
    mountains: texRock,
    desert: extraEnvTex.texDuneRipple || texSand,
    frozen: texIce,
    volcanic: texBasalt,
    jungle: texGrass,
    crystal_desert: extraEnvTex.texCrystalSand || texSand,
    sky_islands: texGrass,
    toxic_swamp: texSwamp,
    frozen_mountains: texIce,
    volcanic_lands: texBasalt,
    ancient_machine_world: texTech
  };

  function distToSegment(px, pz, ax, az, bx, bz) {
    const dx = bx - ax, dz = bz - az;
    const lenSq = dx * dx + dz * dz || 1;
    const t = Math.max(0, Math.min(1, ((px - ax) * dx + (pz - az) * dz) / lenSq));
    return Math.hypot(px - (ax + t * dx), pz - (az + t * dz));
  }

  const BIOME_PALETTES = {
    forest:                [0x5ca842, 0xb57542, 0x3b752c],
    mountains:             [0x8b9bab, 0x6f7c8c, 0x525e6e, 0x3f4a57],
    desert:                [0xf2c578, 0xd99e4c, 0xc78436, 0x9c6122],
    frozen:                [0xd8effc, 0x9bd7f2, 0x82b8d9, 0x5b97bf],
    volcanic:              [0x614e4c, 0x8c3b28, 0x332726, 0x211312],
    jungle:                [0x249658, 0x8f6f46, 0x165e3e, 0x0f472f],
    crystal_desert:        [0xedb0d0, 0xd946ef, 0xb47eb3, 0x8a4d94],
    sky_islands:           [0x64cf8d, 0xe2f3ff, 0x2c8f8d, 0x1e6e70],
    toxic_swamp:           [0x46662f, 0x7c8f43, 0x25381b, 0x192912],
    frozen_mountains:      [0xdbeafe, 0x7dd3fc, 0x78a9db, 0x4b77a6],
    volcanic_lands:        [0x42201e, 0x991b1b, 0x261514, 0x170b0a],
    ancient_machine_world: [0x435266, 0x0284c7, 0x2c3a4c, 0x1d2938]
  };

  BIOMES.forEach((b, bi) => {
    const chunkGrp = getBiomeChunk(b.id);
    const isSky = b.id === "sky_islands";

    const baseRim = new THREE.Mesh(
      makeRoundedBoxGeo(31.8, 0.92, 31.8, 0.28),
      pbrMat(b.id === "forest" ? 0x6b4f35 : (isSky ? 0xe0f2fe : 0x64748b), { map: isSky ? undefined : texRock, roughness: 0.88 })
    );
    baseRim.position.set(b.cx, -0.49, b.cz);
    chunkGrp.add(baseRim);

    if (isSky) {
      for (let ci = 0; ci < 6; ci++) {
        const ang = (ci / 6) * Math.PI * 2;
        const puff = new THREE.Mesh(
          makeOrganicGeo(4.8, 1, 0.2, ci + 7),
          pbrMat(0xf0f9ff, { emissive: 0xbae6fd, emissiveIntensity: 0.14, roughness: 0.95 })
        );
        puff.scale.set(1.5, 0.55, 1.5);
        puff.position.set(b.cx + Math.cos(ang) * 9.5, -1.5, b.cz + Math.sin(ang) * 9.5);
        chunkGrp.add(puff);
      }
    } else {
      const cliff = new THREE.Mesh(
        makeRoundedBoxGeo(32.9, 0.82, 32.9, 0.24),
        pbrMat(b.id === "forest" ? 0x5c422c : 0x4b5563, { map: texRock, roughness: 0.9 })
      );
      cliff.position.set(b.cx, -0.96, b.cz);
      chunkGrp.add(cliff);
    }

    // Preserve Map 1 ("forest") exact geometry, shading, and details untouched.
    // Maps 2-12 terrain meshes & dense scenery are constructed via BiomeEnvBuilder once all pads/nodes exist.
    if (b.id === "forest" || !window.BiomeEnvBuilder) {
      const segs = 40;
      const topGeo = new THREE.PlaneGeometry(31.2, 31.2, segs, segs);
      topGeo.rotateX(-Math.PI / 2);
      const pos = topGeo.attributes.position;
      const colors = new Float32Array(pos.count * 3);
      const pal = BIOME_PALETTES[b.id] || BIOME_PALETTES.forest;
      const cMain = linCol(pal[0]);
      const cTrail = linCol(pal[1]);
      const cMoss = linCol(pal[2]);
      const tmpCol = new THREE.Color();

      for (let i = 0; i < pos.count; i++) {
        const lx = pos.getX(i);
        const lz = pos.getZ(i);
        const n1 = Math.sin(lx * 0.42 + bi * 1.7) * Math.cos(lz * 0.38 - bi);
        const n2 = Math.sin((lx + lz) * 0.75 + bi) * 0.5;
        const edgeDist = Math.max(Math.abs(lx), Math.abs(lz));
        const nearBridgeAxis = Math.abs(lx) < 3.0 || Math.abs(lz) < 3.0;

        let pathFactor = 0;
        if (b.id === "forest") {
          const d1 = distToSegment(lx, lz, 0, 0.5, -4.6, 5.4);
          const d2 = distToSegment(lx, lz, 0, 0.5, 7.5, 6.5);
          const d3 = distToSegment(lx, lz, 0, 0.5, 7.5, -4.5);
          const d4 = distToSegment(lx, lz, 0, 0.5, -8.0, -6.0);
          const d5 = distToSegment(lx, lz, 0, -15.5, 0, 15.5);
          const d6 = distToSegment(lx, lz, -15.5, 0, 15.5, 0);
          const minPath = Math.min(d1, d2, d3, d4, d5, d6);
          pathFactor = Math.max(0, Math.min(1, (2.1 - minPath) / 1.3));
        } else {
          const dCross = Math.min(Math.abs(lx), Math.abs(lz));
          pathFactor = Math.max(0, Math.min(1, (2.0 - dCross) / 1.2));
        }

        let lift = 0;
        if (edgeDist > 13.6 && !nearBridgeAxis) {
          lift = ((edgeDist - 13.6) / 2.0) * (0.28 + n1 * 0.08);
        } else if (pathFactor < 0.15 && Math.hypot(lx, lz) > 7.4) {
          lift = Math.max(0, n1 * 0.05 + n2 * 0.025);
        }
        pos.setY(i, lift);

        const mossWeight = Math.max(0, Math.min(1, 0.35 + n1 * 0.3 + (edgeDist > 12.6 ? 0.32 : 0)));
        tmpCol.copy(cMain).lerp(cMoss, mossWeight).lerp(cTrail, pathFactor * 0.85);
        colors[i * 3] = tmpCol.r;
        colors[i * 3 + 1] = tmpCol.g;
        colors[i * 3 + 2] = tmpCol.b;
      }
      topGeo.setAttribute("color", new THREE.BufferAttribute(colors, 3));
      topGeo.computeVertexNormals();

      const isIcy = b.id === "frozen" || b.id === "frozen_mountains";
      const padMat = pbrMat(0xffffff, {
        map: biomeFloorTex[b.id] || texRock,
        bumpMap: b.id === "forest" ? texGrassBump : undefined,
        bumpScale: 0.06,
        vertexColors: true,
        roughness: isIcy ? 0.35 : (b.id === "ancient_machine_world" ? 0.55 : 0.86),
        metalness: isIcy ? 0.1 : (b.id === "ancient_machine_world" ? 0.3 : 0.02)
      });
      const topMesh = new THREE.Mesh(topGeo, padMat);
      topMesh.position.set(b.cx, 0.01, b.cz);
      topMesh.receiveShadow = true;
      chunkGrp.add(topMesh);
    }

    // Sculpted shoreline cliff boulders around platform corners
    [[-15.4, -15.4], [15.4, -15.4], [-15.4, 15.4], [15.4, 15.4]].forEach(([rx, rz], ci) => {
      const cr = new THREE.Mesh(organicRockGeoA, pbrMat(isSky ? 0xdbeafe : 0x738090, { map: texRock, roughness: 0.88 }));
      cr.scale.set(1.65, 0.9, 1.65);
      cr.position.set(b.cx + rx, -0.58, b.cz + rz);
      cr.rotation.y = ci + bi;
      chunkGrp.add(cr);
    });
  });

  // Central Cobble Courtyard & Natural Stepping Flagstones (attached to Forest chunk)
  const forestChunk = getBiomeChunk("forest");
  const plaza = new THREE.Mesh(
    new THREE.CylinderGeometry(6.6, 7.0, 0.12, 24),
    pbrMat(0xdcd6cc, { map: texCobble, roughness: 0.82 })
  );
  plaza.position.set(0, 0.02, 0.5);
  plaza.receiveShadow = true;
  forestChunk.add(plaza);

  const stoneCurb = new THREE.Mesh(
    new THREE.TorusGeometry(6.75, 0.2, 8, 32),
    pbrMat(0x7a8594, { map: texRock, roughness: 0.86 })
  );
  stoneCurb.rotation.x = Math.PI / 2;
  stoneCurb.position.set(0, 0.05, 0.5);
  forestChunk.add(stoneCurb);

  [
    [0, -8.4], [0, -11.4], [0, -14.0],
    [8.2, 0], [11.2, 0], [-8.4, 0], [-11.4, 0],
    [-2.4, 3.6], [-3.6, 4.6], [4.2, 4.0], [5.4, 5.1]
  ].forEach(([sx, sz], si) => {
    const st = new THREE.Mesh(
      makeOrganicGeo(0.72, 1, 0.16, si + 3.1),
      pbrMat(0xb6bcc4, { map: texCobble, roughness: 0.84 })
    );
    st.scale.set(1.0, 0.11, 0.95);
    st.position.set(sx, 0.04, sz);
    st.receiveShadow = true;
    forestChunk.add(st);
  });

  // Arched Bridges connecting all 12 World Areas
  function addBridge(x, z, rotY, style) {
    const brGroup = new THREE.Group();
    brGroup.position.set(x, -0.22, z);
    brGroup.rotation.y = rotY;
    const deckMat = style === "sky" ? pbrMat(0xe0f2fe, { emissive: 0x38bdf8, emissiveIntensity: 0.2, roughness: 0.35 }) :
      (style === "tech" ? indMats.steelMid :
      (style === "crystal" ? pbrMat(0xfbcfe8, { roughness: 0.45 }) :
      pbrMat(0xe8d5bf, { map: texWoodPlank, roughness: 0.76 })));
    const railMat = style === "tech" ? indMats.safetyYellow :
      (style === "sky" ? pbrMat(0x7dd3fc, { roughness: 0.3 }) :
      pbrMat(0x6b3510, { map: texBark }));
    for (let pi = -3; pi <= 3; pi++) {
      const plank = new THREE.Mesh(
        makeRoundedBoxGeo(5.4, 0.24, 0.72, 0.05),
        deckMat
      );
      plank.position.set(0, 0.18 - Math.abs(pi) * 0.02, pi * 0.82);
      plank.receiveShadow = true;
      plank.castShadow = true;
      brGroup.add(plank);
    }
    [-2.55, 2.55].forEach((rx) => {
      const beam = new THREE.Mesh(makeRoundedBoxGeo(0.26, 0.22, 5.8, 0.04), railMat);
      beam.position.set(rx, 0.68, 0);
      brGroup.add(beam);
      [-2.4, 0, 2.4].forEach((pz) => {
        const post = new THREE.Mesh(makeCapsuleGeo(0.14, 0.75, 8), railMat);
        post.position.set(rx, 0.45, pz);
        post.castShadow = true;
        brGroup.add(post);
      });
    });
    worldGroup.add(brGroup);
  }
  addBridge(0, -17, 0);
  addBridge(17, 0, Math.PI / 2);
  addBridge(-17, 0, Math.PI / 2);
  addBridge(0, 17, 0);
  addBridge(34, -17, 0);
  addBridge(51, 0, Math.PI / 2, "crystal");
  addBridge(0, -51, 0, "sky");
  addBridge(-34, 17, 0, "swamp");
  addBridge(-51, 0, Math.PI / 2, "ice");
  addBridge(0, 51, 0, "lava");
  addBridge(34, 17, 0, "tech");

  // Detailed Wildflowers, Grass Blades, Fern Rosettes, Stumps, Logs, Mushrooms & Lamp Posts
  const grassTufts = [];
  const grassCoords = [
    [-7.5, 4.5], [-8.8, -4.2], [7.8, -1.8], [3.5, 8.2], [-1.5, 9.6],
    [-9.5, 7.2], [9.2, 4.2], [-5.8, -8.5], [6.2, -8.8], [12.5, -5.5],
    [-12.2, -6.2], [8.5, 11.5], [-13.0, 11.2], [13.2, -12.0], [-4.2, 9.5], [4.4, -9.8],
    [-3.2, -7.8], [3.6, -7.6], [-6.8, 2.1], [5.8, -1.6], [10.8, -7.2], [-11.4, 6.4]
  ];
  const flowerColors = [0xfacc15, 0xf472b6, 0xfef3c7, 0x60a5fa];
  const bladeShape = new THREE.Shape();
  bladeShape.moveTo(-0.07, 0);
  bladeShape.quadraticCurveTo(0.02, 0.28, 0.04, 0.52);
  bladeShape.quadraticCurveTo(0.08, 0.26, 0.07, 0);
  const curvedBladeGeo = new THREE.ExtrudeGeometry(bladeShape, { depth: 0.02, bevelEnabled: false });

  grassCoords.forEach(([gx, gz], idx) => {
    const tuft = new THREE.Group();
    tuft.position.set(gx, 0, gz);
    for (let b = 0; b < 5; b++) {
      const blade = new THREE.Mesh(curvedBladeGeo, pbrMat(b % 2 === 0 ? 0x22a84a : 0x15803d, { roughness: 0.65 }));
      blade.rotation.y = b * 1.25;
      blade.rotation.z = (b - 2) * 0.16;
      tuft.add(blade);
    }
    if (idx % 2 === 0) {
      const fCol = flowerColors[idx % flowerColors.length];
      const fCenter = new THREE.Mesh(sphereGeo, pbrMat(0xd97706, { roughness: 0.4 }));
      fCenter.scale.setScalar(0.075);
      fCenter.position.set(0.06, 0.48, 0.05);
      tuft.add(fCenter);
      for (let pt = 0; pt < 5; pt++) {
        const pAng = (pt / 5) * Math.PI * 2;
        const petal = new THREE.Mesh(sphereGeo, pbrMat(fCol, { emissive: fCol, emissiveIntensity: 0.1, roughness: 0.45 }));
        petal.scale.set(0.068, 0.028, 0.068);
        petal.position.set(0.06 + Math.cos(pAng) * 0.09, 0.48, 0.05 + Math.sin(pAng) * 0.09);
        tuft.add(petal);
      }
    }
    forestChunk.add(tuft);
    grassTufts.push(tuft);
  });

  // Forest Edge Bushes
  [[-12.5, 8.5], [12.8, 7.5], [-13.4, -10.5], [12.2, -12.2], [7.2, 12.6], [-8.4, 13.0], [-14.2, -4.8], [14.1, 3.6]].forEach(([bx, bz], i) => {
    const bush = new THREE.Group();
    bush.position.set(bx, 0, bz);
    addContactShadow(bush, 1.05);
    for (let c = 0; c < 3; c++) {
      const lobe = new THREE.Mesh(organicCanopyGeo, pbrMat(c === 0 ? 0x166534 : 0x229944, { map: texFoliage, roughness: 0.8 }));
      const sc = 0.68 - c * 0.12;
      lobe.scale.set(sc * 1.15, sc * 0.85, sc * 1.15);
      lobe.position.set((c - 1) * 0.34, 0.36 + (c === 1 ? 0.16 : 0), (c % 2) * 0.2);
      lobe.castShadow = true;
      bush.add(lobe);
    }
    forestChunk.add(bush);
  });

  // Natural Forest Dressing: Chopped Stumps, Mossy Logs, Toadstools & Warm Path Lanterns
  [[-13.5, 4.5], [13.2, -5.5], [-9.2, 13.4], [9.8, 13.2]].forEach(([sx, sz], si) => {
    const stump = new THREE.Group();
    stump.position.set(sx, 0, sz);
    addContactShadow(stump, 0.65);
    const barkBody = new THREE.Mesh(new THREE.CylinderGeometry(0.32, 0.44, 0.44, 10), pbrMat(0x8a5a36, { map: texBark, roughness: 0.88 }));
    barkBody.position.y = 0.22;
    barkBody.castShadow = true;
    stump.add(barkBody);
    const heartwood = new THREE.Mesh(new THREE.CylinderGeometry(0.29, 0.29, 0.04, 10), pbrMat(0xdcb47e, { roughness: 0.72 }));
    heartwood.position.y = 0.44;
    stump.add(heartwood);
    for (let mi = 0; mi < 2; mi++) {
      const mStem = new THREE.Mesh(new THREE.CylinderGeometry(0.03, 0.04, 0.12, 6), pbrMat(0xfef3c7));
      mStem.position.set(0.36 + mi * 0.14, 0.06, (mi - 0.5) * 0.22);
      stump.add(mStem);
      const mCap = new THREE.Mesh(new THREE.ConeGeometry(0.09, 0.08, 7), pbrMat(mi === 0 ? 0xdc2626 : 0xf59e0b, { roughness: 0.5 }));
      mCap.position.set(0.36 + mi * 0.14, 0.14, (mi - 0.5) * 0.22);
      stump.add(mCap);
    }
    forestChunk.add(stump);
  });

  [[-13.0, -7.8, 0.5], [13.5, 5.2, -0.8]].forEach(([lx, lz, rot]) => {
    const log = new THREE.Group();
    log.position.set(lx, 0.24, lz);
    log.rotation.y = rot;
    const cyl = new THREE.Mesh(new THREE.CylinderGeometry(0.24, 0.28, 2.2, 10), pbrMat(0x85522c, { map: texBark, roughness: 0.88 }));
    cyl.rotation.z = Math.PI / 2;
    cyl.castShadow = true;
    log.add(cyl);
    const mossPatch = new THREE.Mesh(organicCanopyGeo, pbrMat(0x2e8b40, { map: texFoliage, roughness: 0.85 }));
    mossPatch.scale.set(0.55, 0.14, 0.24);
    mossPatch.position.set(0.2, 0.22, 0);
    log.add(mossPatch);
    forestChunk.add(log);
  });

  [[-2.8, -13.6], [2.8, -13.6], [-6.6, 3.6], [6.4, 3.2]].forEach(([px, pz]) => {
    const lamp = new THREE.Group();
    lamp.position.set(px, 0, pz);
    addContactShadow(lamp, 0.5);
    const pole = new THREE.Mesh(makeCapsuleGeo(0.075, 1.8, 8), pbrMat(0x5c3419, { map: texBark }));
    pole.position.y = 0.92;
    pole.castShadow = true;
    lamp.add(pole);
    const cap = new THREE.Mesh(new THREE.ConeGeometry(0.22, 0.18, 6), metalMat(0x334155));
    cap.position.y = 2.04;
    lamp.add(cap);
    const bulb = new THREE.Mesh(sphereGeo, pbrMat(0xfde047, { emissive: 0xf59e0b, emissiveIntensity: 1.0, roughness: 0.2 }));
    bulb.scale.set(0.11, 0.14, 0.11);
    bulb.position.y = 1.88;
    lamp.add(bulb);
    forestChunk.add(lamp);
  });

  [[-13.8, -13.6], [13.8, -13.6], [-13.6, 13.6], [13.8, 13.5]].forEach(([rx, rz], ri) => {
    const rk = new THREE.Mesh(organicRockGeoA, pbrMat(0x788492, { map: texRock, flatShading: true, roughness: 0.9 }));
    rk.scale.set(1.1, 0.72, 1.0);
    rk.position.set(rx, 0.35, rz);
    rk.rotation.y = ri * 1.1;
    rk.castShadow = true;
    rk.receiveShadow = true;
    forestChunk.add(rk);
  });

  // Soft Multi-Lobe Volumetric Clouds
  const skyClouds = [];
  [[-14, 16, -18], [12, 18, -24], [-6, 17, 14], [18, 16, 8], [-24, 17, -6]].forEach(([cx, cy, cz], idx) => {
    const cg = new THREE.Group();
    cg.position.set(cx, cy, cz);
    const puffMat = pbrMat(0xf1f5f9, { roughness: 0.95, metalness: 0.0, emissive: 0xe2e8f0, emissiveIntensity: 0.12 });
    [[0, 0, 0, 1.6, 0.75, 1.2], [-1.15, -0.15, 0.2, 1.1, 0.55, 0.95], [1.2, -0.1, -0.15, 1.2, 0.6, 0.95], [0.2, 0.35, 0.1, 0.95, 0.55, 0.85]].forEach(([px, py, pz, sx, sy, sz], pi) => {
      const puff = new THREE.Mesh(makeOrganicGeo(1, 1, 0.16, idx + pi), puffMat);
      puff.scale.set(sx, sy, sz);
      puff.position.set(px, py, pz);
      cg.add(puff);
    });
    scene.add(cg);
    skyClouds.push(cg);
  });

  // Glowing Atmospheric Fireflies
  const fireflies = [];
  for (let fi = 0; fi < 22; fi++) {
    const fMesh = new THREE.Mesh(
      sphereGeo,
      basicMat(fi % 2 === 0 ? 0xfef08a : 0xa7f3d0)
    );
    fMesh.scale.setScalar(0.065);
    const fx = (Math.random() - 0.5) * 24;
    const fy = 0.9 + Math.random() * 2.4;
    const fz = (Math.random() - 0.5) * 24;
    fMesh.position.set(fx, fy, fz);
    scene.add(fMesh);
    fireflies.push({ mesh: fMesh, baseX: fx, baseY: fy, baseZ: fz, phase: Math.random() * Math.PI * 2, speed: 0.7 + Math.random() * 0.8 });
  }

  // Detailed Frontier Trading Post Pavilion (Sell Zone) at (-4.6, 5.4)
  const SELL_ZONE = { x: -4.6, z: 5.4, radius: 3.1 };
  const sellPadGroup = new THREE.Group();
  sellPadGroup.position.set(SELL_ZONE.x, 0.05, SELL_ZONE.z);
  forestChunk.add(sellPadGroup);

  const sellRing = new THREE.Mesh(
    new THREE.RingGeometry(2.3, 2.85, 32),
    basicMat(0xf59e0b, { side: THREE.DoubleSide })
  );
  sellRing.rotation.x = -Math.PI / 2;
  sellRing.position.y = 0.04;
  sellPadGroup.add(sellRing);

  const sellFloor = new THREE.Mesh(
    new THREE.CylinderGeometry(2.3, 2.45, 0.12, 24),
    pbrMat(0xd97706, { map: texCobble, emissive: 0x78350f, emissiveIntensity: 0.18, roughness: 0.68 })
  );
  sellFloor.receiveShadow = true;
  sellPadGroup.add(sellFloor);

  const stall = new THREE.Group();
  stall.position.set(-1.95, 0, 1.95);
  addContactShadow(stall, 2.2);
  // Timber deck & posts
  const deck = new THREE.Mesh(makeRoundedBoxGeo(2.5, 0.2, 2.0, 0.04), pbrMat(0xe5d3be, { map: texWoodPlank }));
  deck.position.y = 0.1;
  deck.receiveShadow = true;
  stall.add(deck);
  for (const [px, pz] of [[-0.95, -0.75], [0.95, -0.75], [-0.95, 0.75], [0.95, 0.75]]) {
    const post = new THREE.Mesh(makeCapsuleGeo(0.09, 1.85, 8), pbrMat(0x78350f, { map: texBark }));
    post.position.set(px, 1.0, pz);
    post.castShadow = true;
    stall.add(post);
  }
  // Pitched Gable Roof (ExtrudeGeometry)
  const roofShape = new THREE.Shape();
  roofShape.moveTo(-1.45, 0);
  roofShape.lineTo(0, 0.85);
  roofShape.lineTo(1.45, 0);
  roofShape.lineTo(1.25, -0.1);
  roofShape.lineTo(0, 0.68);
  roofShape.lineTo(-1.25, -0.1);
  roofShape.closePath();
  const pitchedRoofGeo = new THREE.ExtrudeGeometry(roofShape, { depth: 2.25, bevelEnabled: true, bevelThickness: 0.03, bevelSize: 0.03 });
  pitchedRoofGeo.center();
  const roof = new THREE.Mesh(pitchedRoofGeo, pbrMat(0xb91c1c, { map: texWoodPlank, roughness: 0.6 }));
  roof.position.set(0, 2.28, 0);
  roof.castShadow = true;
  stall.add(roof);

  // Market Counter, Wooden Barrel & Hanging Lantern
  const counter = new THREE.Mesh(makeRoundedBoxGeo(1.7, 0.75, 0.6, 0.04), pbrMat(0xd97706, { map: texWoodPlank }));
  counter.position.set(0, 0.52, -0.55);
  counter.castShadow = true;
  stall.add(counter);

  const barrelPts = [new THREE.Vector2(0.01, 0), new THREE.Vector2(0.24, 0.02), new THREE.Vector2(0.3, 0.3), new THREE.Vector2(0.24, 0.58), new THREE.Vector2(0.01, 0.6)];
  const barrel = new THREE.Mesh(new THREE.LatheGeometry(barrelPts, 14), pbrMat(0x92400e, { map: texWoodPlank }));
  barrel.position.set(1.45, 0.02, -0.2);
  barrel.castShadow = true;
  stall.add(barrel);

  const lantern = new THREE.Mesh(sphereGeo, pbrMat(0xfef08a, { emissive: 0xf59e0b, emissiveIntensity: 0.95, roughness: 0.2 }));
  lantern.scale.set(0.14, 0.2, 0.14);
  lantern.position.set(0.95, 1.68, -0.78);
  stall.add(lantern);

  const coinSign = new THREE.Mesh(
    new THREE.CylinderGeometry(0.54, 0.54, 0.12, 20),
    metalMat(0xfbbf24, { emissive: 0xd97706, emissiveIntensity: 0.25, roughness: 0.22, metalness: 0.6 })
  );
  coinSign.rotation.x = Math.PI / 2;
  coinSign.position.set(0, 3.15, 0);
  stall.add(coinSign);
  sellPadGroup.add(stall);

  // Stone Arch Biome Gates
  const biomeGates = {};
  BIOMES.forEach((b) => {
    if (!b.gate) return;
    const gGroup = new THREE.Group();
    gGroup.position.set(b.gate.x, 0, b.gate.z);
    if (b.gate.dir === "x") gGroup.rotation.y = Math.PI / 2;

    [-2.4, 2.4].forEach((px) => {
      const pillar = new THREE.Mesh(makeRoundedBoxGeo(0.85, 3.2, 0.85, 0.08), pbrMat(0x64748b, { map: texCobble, roughness: 0.82 }));
      pillar.position.set(px, 1.6, 0);
      pillar.castShadow = true;
      gGroup.add(pillar);
      const cap = new THREE.Mesh(makeOrganicGeo(0.52, 1, 0.15, px), pbrMat(b.accent, { emissive: b.accent, emissiveIntensity: 0.35, roughness: 0.3 }));
      cap.position.set(px, 3.35, 0);
      gGroup.add(cap);
    });

    const barrier = new THREE.Mesh(
      makeRoundedBoxGeo(4.1, 2.5, 0.24, 0.06),
      pbrMat(b.accent, {
        transparent: true,
        opacity: 0.45,
        emissive: b.accent,
        emissiveIntensity: 0.45,
        roughness: 0.18,
        metalness: 0.35
      })
    );
    barrier.position.set(0, 1.35, 0);
    gGroup.add(barrier);

    const padRing = new THREE.Mesh(
      new THREE.RingGeometry(1.4, 1.85, 24),
      basicMat(0xfbbf24, { side: THREE.DoubleSide })
    );
    padRing.rotation.x = -Math.PI / 2;
    padRing.position.set(0, 0.08, 0);
    gGroup.add(padRing);

    worldGroup.add(gGroup);
    biomeGates[b.id] = { biome: b, group: gGroup, barrier, padRing, charge: 0 };
  });

  // Detailed Multi-Tier Industrial World Buildings + Drones (Monumental 3-Story Scale)
  const worldBuildings = {};
  BUILDING_DEFS.forEach((def) => {
    const grp = new THREE.Group();
    grp.position.set(def.x, 0, def.z);
    addContactShadow(grp, 2.55);

    const padMesh = new THREE.Mesh(
      new THREE.CylinderGeometry(2.15, 2.28, 0.16, 24),
      indMats.steelDark
    );
    padMesh.position.y = 0.08;
    padMesh.receiveShadow = true;
    grp.add(padMesh);

    const ring = new THREE.Mesh(
      new THREE.RingGeometry(1.85, 2.15, 28),
      basicMat(0x10b981, { side: THREE.DoubleSide })
    );
    ring.rotation.x = -Math.PI / 2;
    ring.position.y = 0.17;
    grp.add(ring);

    const struct = new THREE.Group();
    struct.visible = false;

    const isSmelter = def.id === "smelter" || def.biome === "volcanic" || def.biome === "volcanic_lands";
    const isWood = def.id === "sawmill";
    const hullColor = isSmelter ? 0x526275 :
      (def.id === "warehouse" ? 0x1e6091 :
      (isWood ? 0xb45309 :
      (def.biome === "crystal_desert" ? 0x9d174d :
      (def.biome === "sky_islands" ? 0x0284c7 :
      (def.biome === "toxic_swamp" ? 0x3f6212 :
      (def.biome === "frozen_mountains" ? 0x0e7490 :
      (def.biome === "ancient_machine_world" ? 0x1e40af : 0x3b4d63)))))));
    const hullMat = pbrMat(hullColor, {
      map: isWood ? texWoodPlank : texPanelMetal,
      roughness: 0.44,
      metalness: isWood ? 0.14 : 0.38
    });
    const accentGlow = isSmelter ? indMats.glowAmber : (def.biome === "toxic_swamp" ? indMats.glowGreen : indMats.glowCyan);

    const legacyBody = def.biome !== "forest" ? new THREE.Group() : struct;
    if (legacyBody !== struct) struct.add(legacyBody);

    // Tier 1: Stepped Foundation Plinth + Front Entry Steps + Twin Pressure Boilers + Core Bay
    addSteppedPlinth(legacyBody, 2.95, 2.78, true, indMats.concreteBase);

    const baseHull = new THREE.Mesh(makeRoundedBoxGeo(2.05, 1.38, 1.85, 0.08), hullMat);
    baseHull.position.set(0, 1.04, -0.1);
    baseHull.castShadow = true;
    baseHull.receiveShadow = true;
    legacyBody.add(baseHull);

    // Center Recessed Core Turbine / Bay (green turbine drum or timber intake)
    const frontTurbine = new THREE.Mesh(
      new THREE.CylinderGeometry(0.42, 0.42, 0.92, 14),
      isSmelter ? indMats.indOrange : (isWood ? indMats.copper : indMats.turbineGreen)
    );
    frontTurbine.rotation.z = Math.PI / 2;
    frontTurbine.position.set(0, 0.84, 0.92);
    frontTurbine.castShadow = true;
    legacyBody.add(frontTurbine);
    const doorLight = new THREE.Mesh(makeRoundedBoxGeo(0.74, 0.14, 0.12, 0.02), accentGlow);
    doorLight.position.set(0, 1.36, 0.88);
    legacyBody.add(doorLight);

    // Twin Flanking Horizontal Saddle Pressure Tanks + Elbow Feeds
    addPressureTank(legacyBody, -1.16, 0.88, 0.25, 0.28, 0.88, true, indMats.domeCream, indMats.indOrange);
    addPressureTank(legacyBody, 1.16, 0.88, 0.25, 0.28, 0.88, true, indMats.domeCream, indMats.indOrange);
    addPipeRun(legacyBody, [[-1.62, 0.88, 0.25], [-1.72, 0.42, 0.85], [-1.25, 0.42, 1.05]], 0.065, indMats.safetyYellow);
    addPipeRun(legacyBody, [[1.62, 0.88, 0.25], [1.72, 0.42, 0.85], [1.25, 0.42, 1.05]], 0.065, indMats.safetyYellow);

    const louver = new THREE.Mesh(makeRoundedBoxGeo(0.08, 0.65, 0.85, 0.015), indMats.vents);
    louver.position.set(1.06, 1.05, -0.2);
    legacyBody.add(louver);

    // Tier 2: Cantilevered Mezzanine Deck + Continuous Safety-Amber Perimeter Rails + Side Balcony Tower
    addCantileverDeck(legacyBody, 3.02, 2.74, 1.88, 0.36, indMats.safetyYellow, !isWood);
    addLadder(legacyBody, -1.48, 0.36, 1.92, -0.35, -Math.PI / 2, indMats.safetyYellow);
    addSideBalconyTower(legacyBody, 1.48, -0.82, 2.25, 0.56, indMats.steelMid, indMats.safetyYellow, accentGlow);

    // Tier 3: Towering Ribbed Domed Silo Superstructure + Cyan Coolant Chute + Elbow Smokestack + Rotor
    addSiloSuperstructure(
      legacyBody, -0.08, 1.96, -0.18, 0.92, 1.25,
      isWood ? hullMat : indMats.domeCream,
      isSmelter ? indMats.indOrange : (def.id === "warehouse" ? indMats.panelBlue : indMats.domeCream),
      accentGlow
    );
    if (!isWood) {
      addCoolantChute(legacyBody, -0.08, 2.42, 0.72, 0, isSmelter ? indMats.glowAmber : indMats.coolantChute);
    }
    addPipeRun(legacyBody, [[-1.16, 1.18, 0.25], [-1.18, 2.55, -0.18], [-0.95, 2.75, -0.18]], 0.075, indMats.steelLight);
    addElbowStack(legacyBody, 0.68, 1.96, -0.86, 1.78, 0.2, indMats.steelDark, indMats.indOrange);

    const spinner = new THREE.Mesh(
      gearWheelGeo,
      metalMat(isSmelter ? 0xf97316 : 0x38bdf8, {
        emissive: isSmelter ? 0xea580c : 0x0284c7,
        emissiveIntensity: 0.45
      })
    );
    spinner.scale.setScalar(1.45);
    spinner.rotation.x = Math.PI / 2;
    spinner.position.set(-0.08, 3.92, -0.18);
    spinner.castShadow = true;
    struct.add(spinner);

    // Modular Production Line Bays (max 3)
    const lineModules = [];
    for (let li = 0; li < 3; li++) {
      const lm = new THREE.Group();
      const pod = new THREE.Mesh(makeRoundedBoxGeo(0.76, 0.82, 0.76, 0.06), indMats.panelBlue);
      pod.castShadow = true;
      lm.add(pod);
      const podVent = new THREE.Mesh(makeRoundedBoxGeo(0.54, 0.48, 0.78, 0.02), indMats.vents);
      lm.add(podVent);
      const lang = li * (Math.PI * 2 / 3);
      lm.position.set(Math.cos(lang) * 1.85, 0.56, Math.sin(lang) * 1.85);
      lm.visible = false;
      struct.add(lm);
      lineModules.push(lm);
    }

    // Orbiting Quad-Rotor Industrial Worker Drones (max 4)
    const workerGroup = new THREE.Group();
    struct.add(workerGroup);
    const workerMeshes = [];
    for (let wi = 0; wi < 4; wi++) {
      const drone = new THREE.Group();
      const chassis = new THREE.Mesh(makeRoundedBoxGeo(0.32, 0.14, 0.32, 0.04), metalMat(0x0ea5e9));
      drone.add(chassis);
      const eye = new THREE.Mesh(sphereGeo, pbrMat(0x22d3ee, { emissive: 0x22d3ee, emissiveIntensity: 1.0 }));
      eye.scale.setScalar(0.08);
      eye.position.set(0, 0, 0.17);
      drone.add(eye);
      for (const [rx, rz] of [[-0.2, -0.2], [0.2, -0.2], [-0.2, 0.2], [0.2, 0.2]]) {
        const rotor = new THREE.Mesh(new THREE.CylinderGeometry(0.12, 0.12, 0.02, 8), indMats.chrome);
        rotor.position.set(rx, 0.1, rz);
        drone.add(rotor);
      }
      const wang = wi * (Math.PI / 2);
      drone.position.set(Math.cos(wang) * 2.25, 3.05, Math.sin(wang) * 2.25);
      drone.visible = false;
      workerGroup.add(drone);
      workerMeshes.push(drone);
    }

    if (def.biome !== "forest" && window.ModelAssetManager) {
      window.ModelAssetManager.registerBuildingMount(def, struct, legacyBody, 0.16, (metrics) => {
        spinner.position.set(metrics.mainX, Math.max(2.05, metrics.mainTopY + 0.18), metrics.mainZ);
        if (def.id === "miner_sky_islands") spinner.scale.setScalar(0.95);
        const droneY = Math.max(2.45, metrics.maxTopY + 0.22);
        for (let wi = 0; wi < workerMeshes.length; wi++) {
          workerMeshes[wi].position.y = droneY;
        }
      });
    }

    grp.add(struct);
    getBiomeChunk(def.biome).add(grp);
    worldBuildings[def.id] = {
      def, group: grp, padMesh, ring, struct, spinner,
      lineModules, workerGroup, workerMeshes, charge: 0
    };
  });

  // =========================================================================
  // 8. MONUMENTAL MULTI-TIER INDUSTRIAL MACHINERY (12 MACHINES) & FLEET (5)
  // =========================================================================
  const worldMachines = {};
  const machineSmoke = [];

  function spawnMachineSmoke(wx, wy, wz, col) {
    if (machineSmoke.length > 32) return;
    const m = new THREE.Mesh(
      makeOrganicGeo(0.28, 0, 0.16, wx),
      pbrMat(col || 0xd1d5db, { transparent: true, opacity: 0.54, roughness: 0.95 })
    );
    m.position.set(wx + (Math.random() - 0.5) * 0.18, wy, wz + (Math.random() - 0.5) * 0.18);
    scene.add(m);
    machineSmoke.push({ mesh: m, age: 0, life: 1.45, vy: 1.25 + Math.random() * 0.45 });
  }

  function createMachineModel(def) {
    const grp = new THREE.Group();
    grp.position.set(def.x, 0, def.z);
    grp.rotation.y = def.rotY;
    addContactShadow(grp, def.id === "conveyor_belt" ? 3.1 : 2.45);

    const isBelt = def.id === "conveyor_belt";
    const padW = isBelt ? 2.45 : 3.02;
    const padD = isBelt ? 5.6 : 3.02;

    // Steel Diamond-Plate Foundation Pad with Hazard Perimeter
    const pad = new THREE.Mesh(
      makeRoundedBoxGeo(padW, 0.16, padD, 0.05),
      indMats.steelDark
    );
    pad.position.y = 0.08;
    pad.receiveShadow = true;
    grp.add(pad);

    const hazardTrim = new THREE.Mesh(
      new THREE.BoxGeometry(padW + 0.09, 0.09, padD + 0.09),
      indMats.hazardDeck
    );
    hazardTrim.position.y = 0.05;
    grp.add(hazardTrim);

    const rig = new THREE.Group();
    rig.visible = false;
    grp.add(rig);

    // Industrial Electrical Breaker Cabinet + Tall Telemetry Beacon
    const cabinet = new THREE.Mesh(makeRoundedBoxGeo(0.46, 0.96, 0.38, 0.04), indMats.cabinet);
    cabinet.position.set(isBelt ? -1.04 : -1.24, 0.64, isBelt ? 2.35 : 1.22);
    cabinet.castShadow = true;
    rig.add(cabinet);
    const mastRod = new THREE.Mesh(new THREE.CylinderGeometry(0.028, 0.028, 0.42, 6), indMats.steelDark);
    mastRod.position.set(cabinet.position.x, 1.32, cabinet.position.z);
    rig.add(mastRod);
    const statusLed = new THREE.Mesh(sphereGeo, pbrMat(0x22c55e, { emissive: 0x22c55e, emissiveIntensity: 1.35 }));
    statusLed.scale.setScalar(0.11);
    statusLed.position.set(cabinet.position.x, 1.56, cabinet.position.z);
    rig.add(statusLed);

    const anim = { rotParts: [], pistons: [], crates: [], smokeOrigin: null, scrollBelt: false };

    if (def.id === "wood_cutter") {
      // 1. WOOD CUTTING COMPLEX (~4.0u tall): Stepped Plinth + Feed Rollers + 2nd-Story Gantry Bridge + Cyclone Tower + Twin Sawblades
      addSteppedPlinth(rig, 2.95, 2.75, true, indMats.concreteBase);

      const lowerHousing = new THREE.Mesh(makeRoundedBoxGeo(2.35, 0.86, 1.85, 0.06), indMats.steelDark);
      lowerHousing.position.set(0, 0.78, -0.08);
      lowerHousing.castShadow = true;
      rig.add(lowerHousing);

      const sawBed = new THREE.Mesh(makeRoundedBoxGeo(2.55, 0.16, 1.48, 0.04), indMats.steelLight);
      sawBed.position.set(0, 1.26, 0.02);
      sawBed.receiveShadow = true;
      rig.add(sawBed);

      const greenDrum = new THREE.Mesh(new THREE.CylinderGeometry(0.38, 0.38, 0.85, 14), indMats.turbineGreen);
      greenDrum.rotation.z = Math.PI / 2;
      greenDrum.position.set(0, 0.78, 0.84);
      greenDrum.castShadow = true;
      rig.add(greenDrum);
      const vent = new THREE.Mesh(makeRoundedBoxGeo(0.75, 0.46, 0.06, 0.01), indMats.vents);
      vent.position.set(-0.72, 0.78, 0.88);
      rig.add(vent);

      [-0.95, -0.95].forEach((rx, ri) => {
        const ry = ri === 0 ? 1.46 : 1.82;
        const bracket = new THREE.Mesh(makeRoundedBoxGeo(0.55, 0.12, 0.14, 0.02), indMats.steelLight);
        bracket.position.set(rx, ry - 0.04, 0.68);
        bracket.rotation.z = 0.25;
        rig.add(bracket);
        const rPivot = new THREE.Group();
        rPivot.position.set(rx - 0.18, ry, 0.05);
        const roller = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.18, 1.18, 12), indMats.rubberDark);
        roller.rotation.x = Math.PI / 2;
        roller.castShadow = true;
        rPivot.add(roller);
        rig.add(rPivot);
        anim.rotParts.push({ mesh: rPivot, axis: "z", speed: -4.2 });
      });

      // 2nd-Story Cantilevered Overhead Gantry Deck + Safety Rails + Ladder
      addCantileverDeck(rig, 2.9, 2.35, 2.25, 0.36, indMats.safetyYellow, false);
      addLadder(rig, 1.42, 0.36, 2.28, 0.35, Math.PI / 2, indMats.safetyYellow);

      const overheadBeam = new THREE.Mesh(makeRoundedBoxGeo(0.44, 0.34, 1.56, 0.05), indMats.indOrange);
      overheadBeam.position.set(0.08, 2.52, -0.05);
      overheadBeam.castShadow = true;
      rig.add(overheadBeam);
      addMotorPod(rig, 0.08, 2.86, -0.58, 1.05, indMats.panelBlue);

      // Twin Exposed Giant Circular Sawblades + Safety Shroud
      [0.02, 0.36].forEach((bz, bi) => {
        const bladeSpin = new THREE.Group();
        bladeSpin.position.set(0.08, 1.72, bz);
        bladeSpin.rotation.y = Math.PI / 2;
        const blade = new THREE.Mesh(sawbladeGeo, indMats.chrome);
        blade.scale.setScalar(bi === 0 ? 1.32 : 1.18);
        blade.castShadow = true;
        bladeSpin.add(blade);
        const arbor = new THREE.Mesh(new THREE.CylinderGeometry(0.14, 0.14, 0.14, 10), indMats.brass);
        arbor.rotation.x = Math.PI / 2;
        bladeSpin.add(arbor);
        rig.add(bladeSpin);
        anim.rotParts.push({ mesh: bladeSpin, axis: "z", speed: 10.5 });
      });
      const shroud = new THREE.Mesh(new THREE.TorusGeometry(0.72, 0.08, 8, 16, Math.PI), indMats.safetyYellow);
      shroud.rotation.y = Math.PI / 2;
      shroud.position.set(0.08, 1.75, 0.18);
      rig.add(shroud);

      // Tall Domed Cyclone Sawdust Collector + Elbow Pipework
      addSiloSuperstructure(rig, 0.96, 2.32, -0.68, 0.44, 1.12, indMats.domeCream, indMats.indOrange, indMats.glowAmber);
      addPipeRun(rig, [[0.08, 2.48, 0.18], [0.55, 3.05, -0.35], [0.96, 3.35, -0.68]], 0.09, indMats.steelLight);
      addBoltsInstanced(rig, [
        [-1.12, 1.34, 0.68], [1.12, 1.34, 0.68], [-1.12, 1.34, -0.68], [1.12, 1.34, -0.68],
        [-1.25, 2.34, 1.05], [1.25, 2.34, 1.05]
      ]);

      const plankOut = createProductCrate(0xd97706, true);
      plankOut.position.set(0.55, 1.44, 0.2);
      rig.add(plankOut);
      anim.crates.push({ mesh: plankOut, axis: "x", min: -0.58, max: 1.05, speed: 0.72 });

    } else if (def.id === "stone_crusher") {
      // 2. STONE CRUSHER COMPLEX (~4.4u tall): Heavy Jaw Chassis + Hydraulic Stabilizers + Catwalk + Towering Hopper & Jib Crane
      addSteppedPlinth(rig, 2.95, 2.85, true, indMats.concreteBase);

      const frame = new THREE.Mesh(makeRoundedBoxGeo(2.38, 1.25, 2.1, 0.07), indMats.steelDark);
      frame.position.set(0, 0.98, -0.05);
      frame.castShadow = true;
      rig.add(frame);

      const armorPlate = new THREE.Mesh(makeRoundedBoxGeo(2.46, 0.82, 1.54, 0.05), indMats.panelWhite);
      armorPlate.position.set(0, 1.02, -0.05);
      rig.add(armorPlate);

      const louverL = new THREE.Mesh(makeRoundedBoxGeo(0.08, 0.56, 1.02, 0.01), indMats.vents);
      louverL.position.set(-1.24, 1.02, -0.05);
      rig.add(louverL);
      const louverR = louverL.clone();
      louverR.position.x = 1.24;
      rig.add(louverR);

      // 2nd-Story Structural Catwalk + Safety Rails + Access Ladder
      addCantileverDeck(rig, 2.96, 2.6, 2.02, 0.36, indMats.safetyYellow, false);
      addLadder(rig, 1.44, 0.36, 2.05, 0.52, Math.PI / 2, indMats.safetyYellow);

      // Towering Flared Steel Hopper with Safety Amber Rim
      const hopper = new THREE.Mesh(new THREE.CylinderGeometry(1.18, 0.56, 1.05, 4), indMats.steelMid);
      hopper.rotation.y = Math.PI / 4;
      hopper.position.set(0, 2.62, 0);
      hopper.castShadow = true;
      rig.add(hopper);
      const hopperRim = new THREE.Mesh(new THREE.CylinderGeometry(1.26, 1.18, 0.16, 4), indMats.safetyYellow);
      hopperRim.rotation.y = Math.PI / 4;
      hopperRim.position.set(0, 3.18, 0);
      rig.add(hopperRim);

      // Upper Silo Dust Suppression Drum & Pipe Manifold
      addPressureTank(rig, -1.04, 2.55, -0.78, 0.3, 1.05, false, indMats.panelWhite, indMats.indOrange);
      addElbowStack(rig, 0.98, 2.1, -0.88, 1.45, 0.16, indMats.steelDark, indMats.indOrange);
      addPipeRun(rig, [[-1.04, 3.12, -0.78], [-0.45, 3.52, -0.45], [0.42, 3.35, 0]], 0.075, indMats.steelLight);

      // Counter-Rotating Toothed Crusher Rollers + Giant External Brass Flywheel
      [-0.28, 0.28].forEach((ox, oi) => {
        const rollPivot = new THREE.Group();
        rollPivot.position.set(ox, 2.38, 0);
        const roller = new THREE.Mesh(gearWheelGeo, indMats.chrome);
        roller.scale.set(1.2, 1.2, 2.4);
        rollPivot.add(roller);
        rig.add(rollPivot);
        anim.rotParts.push({ mesh: rollPivot, axis: "z", speed: oi === 0 ? 5.6 : -5.6 });
      });

      const flywheelSpin = new THREE.Group();
      flywheelSpin.position.set(1.36, 1.28, -0.15);
      flywheelSpin.rotation.y = Math.PI / 2;
      const fw = new THREE.Mesh(gearWheelGeo, indMats.brass);
      fw.scale.setScalar(1.62);
      fw.castShadow = true;
      flywheelSpin.add(fw);
      rig.add(flywheelSpin);
      anim.rotParts.push({ mesh: flywheelSpin, axis: "z", speed: 4.8 });

      addMotorPod(rig, -1.16, 1.38, -0.75, 1.02, indMats.panelBlue);

      // Twin Heavy Hydraulic Crusher Pistons
      [[-0.86, 0.85], [0.86, 0.85]].forEach(([px, pz]) => {
        const sleeve = new THREE.Mesh(new THREE.CylinderGeometry(0.16, 0.16, 0.82, 12), indMats.safetyYellow);
        sleeve.position.set(px, 1.18, pz);
        rig.add(sleeve);
        const rod = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.08, 0.82, 10), indMats.chrome);
        rod.position.set(px, 1.62, pz);
        rig.add(rod);
        anim.pistons.push({ mesh: rod, baseY: 1.62, amp: 0.24, speed: 6.4 });
      });

      const block = createProductCrate(0x94a3b8, false);
      block.scale.setScalar(0.95);
      block.position.set(0, 0.54, 1.08);
      rig.add(block);
      anim.crates.push({ mesh: block, axis: "z", min: 0.42, max: 1.32, speed: 0.48 });

    } else if (def.id === "conveyor_belt") {
      // 3. CONVEYOR BELT & OVERHEAD SORTING TOWER (~3.9u tall): Stepped Pier Pads + Elevated Truss Belt + 2-Story Control Bridge
      anim.scrollBelt = true;
      for (const rz of [-2.35, -0.8, 0.8, 2.35]) {
        for (const rx of [-0.82, 0.82]) {
          const foot = new THREE.Mesh(makeRoundedBoxGeo(0.28, 0.16, 0.28, 0.03), indMats.concreteBase);
          foot.position.set(rx, 0.2, rz);
          rig.add(foot);
          const leg = new THREE.Mesh(makeCapsuleGeo(0.085, 0.68, 8), indMats.steelDark);
          leg.position.set(rx, 0.56, rz);
          leg.castShadow = true;
          rig.add(leg);
        }
        const cross = new THREE.Mesh(makeRoundedBoxGeo(1.7, 0.1, 0.12, 0.02), indMats.steelMid);
        cross.position.set(0, 0.62, rz);
        rig.add(cross);
      }
      [-0.84, 0.84].forEach((rx) => {
        const rail = new THREE.Mesh(makeRoundedBoxGeo(0.16, 0.28, 5.25, 0.03), indMats.panelBlue);
        rail.position.set(rx, 0.94, 0);
        rail.castShadow = true;
        rig.add(rail);
        const guide = new THREE.Mesh(makeRoundedBoxGeo(0.07, 0.14, 5.2, 0.01), indMats.safetyYellow);
        guide.position.set(rx * 0.9, 1.12, 0);
        rig.add(guide);
      });
      const beltBed = new THREE.Mesh(makeRoundedBoxGeo(1.42, 0.16, 5.08, 0.04), indMats.beltMat);
      beltBed.position.y = 0.92;
      beltBed.receiveShadow = true;
      rig.add(beltBed);

      for (let ri = -2.35; ri <= 2.35; ri += 0.78) {
        const rPivot = new THREE.Group();
        rPivot.position.set(0, 0.86, ri);
        const roller = new THREE.Mesh(new THREE.CylinderGeometry(0.14, 0.14, 1.5, 10), indMats.chrome);
        roller.rotation.z = Math.PI / 2;
        rPivot.add(roller);
        rig.add(rPivot);
        anim.rotParts.push({ mesh: rPivot, axis: "x", speed: 6.2 });
      }

      // Monumental 2-Story Overhead Scanner & Logistics Bridge Arch
      [-1.02, 1.02].forEach((gx) => {
        const col = new THREE.Mesh(makeRoundedBoxGeo(0.28, 2.15, 0.62, 0.04), indMats.steelDark);
        col.position.set(gx, 1.22, 0);
        col.castShadow = true;
        rig.add(col);
      });
      const bridge = new THREE.Mesh(makeRoundedBoxGeo(2.38, 0.24, 1.38, 0.05), indMats.deckSlate);
      bridge.position.set(0, 2.32, 0);
      bridge.castShadow = true;
      rig.add(bridge);
      addSafetyRail(rig, -1.1, -0.62, 1.1, -0.62, 2.42, indMats.safetyYellow, 0.04);
      addSafetyRail(rig, -1.1, 0.62, 1.1, 0.62, 2.42, indMats.safetyYellow, 0.04);
      addLadder(rig, 1.18, 0.16, 2.42, 0, Math.PI / 2, indMats.safetyYellow);

      const scanCabin = new THREE.Mesh(makeRoundedBoxGeo(1.55, 0.92, 0.95, 0.06), indMats.panelWhite);
      scanCabin.position.set(-0.15, 2.86, 0);
      scanCabin.castShadow = true;
      rig.add(scanCabin);
      const scanGlow = new THREE.Mesh(makeRoundedBoxGeo(1.48, 0.1, 0.24, 0.02), indMats.glowCyan);
      scanGlow.position.set(0, 2.16, 0);
      rig.add(scanGlow);
      addPressureTank(rig, 0.78, 2.86, 0, 0.24, 0.85, false, indMats.panelBlue, indMats.indOrange);
      addMotorPod(rig, -0.15, 3.5, 0, 0.92, indMats.indOrange);

      [0xd97706, 0xcbd5e1, 0xfbbf24, 0x22d3ee].forEach((cCol, ci) => {
        const cr = createProductCrate(cCol, ci % 2 === 1);
        cr.position.set(0, 1.15, -2.0 + ci * 1.3);
        rig.add(cr);
        anim.crates.push({ mesh: cr, axis: "z", min: -2.25, max: 2.25, speed: 1.15 });
      });

    } else if (def.id === "sawmill_machine") {
      // 4. AUTOMATED SAWMILL COMPLEX (~4.6u tall): 2-Story Mill Bay + Mezzanine + Exposed Twin Band-Saw Flywheels + Cyclone Tower
      addSteppedPlinth(rig, 2.95, 2.75, true, indMats.concreteBase);

      const bay = new THREE.Mesh(makeRoundedBoxGeo(2.35, 1.48, 1.92, 0.08), indMats.panelBlue);
      bay.position.set(0, 1.08, -0.08);
      bay.castShadow = true;
      rig.add(bay);

      const lowerWoodPod = new THREE.Mesh(makeRoundedBoxGeo(1.22, 0.94, 0.68, 0.06), indMats.panelWhite);
      lowerWoodPod.position.set(-0.42, 0.82, 0.94);
      lowerWoodPod.castShadow = true;
      rig.add(lowerWoodPod);
      const podVent = new THREE.Mesh(makeRoundedBoxGeo(0.84, 0.56, 0.06, 0.01), indMats.vents);
      podVent.position.set(-0.42, 0.84, 1.28);
      rig.add(podVent);

      addCantileverDeck(rig, 2.92, 2.52, 1.94, 0.36, indMats.safetyYellow, false);
      addLadder(rig, 1.44, 0.36, 1.98, 0.45, Math.PI / 2, indMats.safetyYellow);

      // Upper Steel Saw Gantry + Twin Large Band-Saw Flywheels
      [-0.78, 0.58].forEach((fx) => {
        const post = new THREE.Mesh(makeRoundedBoxGeo(0.24, 1.45, 0.24, 0.03), indMats.steelDark);
        post.position.set(fx, 2.72, -0.15);
        post.castShadow = true;
        rig.add(post);

        const fwSpin = new THREE.Group();
        fwSpin.position.set(fx, 2.86, 0.14);
        const fw = new THREE.Mesh(gearWheelGeo, indMats.safetyYellow);
        fw.scale.setScalar(1.38);
        fw.castShadow = true;
        fwSpin.add(fw);
        rig.add(fwSpin);
        anim.rotParts.push({ mesh: fwSpin, axis: "z", speed: 6.0 });
      });
      const sawBand = new THREE.Mesh(makeRoundedBoxGeo(1.48, 0.08, 0.12, 0.01), indMats.chrome);
      sawBand.position.set(-0.1, 3.35, 0.14);
      rig.add(sawBand);

      addSiloSuperstructure(rig, 0.92, 2.02, -0.72, 0.44, 1.25, indMats.copper, indMats.domeCream, indMats.glowAmber);
      addPipeRun(rig, [[-0.72, 2.95, -0.35], [0.15, 3.45, -0.72], [0.92, 3.22, -0.72]], 0.085, indMats.steelLight);
      addMotorPod(rig, -0.12, 2.24, -0.58, 0.98, indMats.indOrange);
      addElbowStack(rig, -0.96, 2.02, -0.78, 1.68, 0.17, indMats.steelDark, indMats.indOrange);

      const crateOut = createProductCrate(0xf59e0b, false);
      crateOut.scale.setScalar(1.05);
      crateOut.position.set(0.65, 1.05, 1.02);
      rig.add(crateOut);
      anim.crates.push({ mesh: crateOut, axis: "z", min: 0.42, max: 1.22, speed: 0.55 });
      anim.smokeOrigin = { lx: -0.96, ly: 4.15, lz: -0.78, col: 0xe5e7eb };

    } else if (def.id === "auto_miner_machine") {
      // 5. AUTOMATIC MINING RIG (~5.3u tall): Stepped Foundation + Diesel Generator Bay + Cantilever Platform + Towering Steel Derrick & Auger
      addSteppedPlinth(rig, 2.98, 2.88, true, indMats.concreteBase);

      const deck = new THREE.Mesh(makeRoundedBoxGeo(2.55, 0.58, 2.35, 0.06), indMats.steelDark);
      deck.position.y = 0.62;
      deck.castShadow = true;
      rig.add(deck);

      // Generator Pod + Twin Hydraulic Pressure Tanks + Side Cooling Louvers
      const genPod = new THREE.Mesh(makeRoundedBoxGeo(0.95, 1.04, 1.48, 0.06), indMats.panelWhite);
      genPod.position.set(0.88, 1.38, -0.35);
      genPod.castShadow = true;
      rig.add(genPod);
      const genVent = new THREE.Mesh(makeRoundedBoxGeo(0.08, 0.66, 0.94, 0.01), indMats.vents);
      genVent.position.set(1.36, 1.38, -0.35);
      rig.add(genVent);
      addPressureTank(rig, -0.95, 1.32, 0.15, 0.28, 1.05, false, indMats.panelBlue, indMats.safetyYellow);

      // 2nd-Story Drilling Operations Deck + Safety Amber Perimeter Guardrails
      addCantileverDeck(rig, 2.86, 2.66, 1.98, 0.36, indMats.safetyYellow, false);
      addLadder(rig, -1.42, 0.36, 2.02, 0.35, -Math.PI / 2, indMats.safetyYellow);

      // Towering 4-Column Tapered Steel Derrick Lattice + Crown Block (up to y = 5.1)
      [[-0.88, -0.88], [0.88, -0.88], [-0.88, 0.88], [0.88, 0.88]].forEach(([tx, tz]) => {
        const leg = new THREE.Mesh(makeCapsuleGeo(0.085, 3.05, 8), indMats.panelTeal);
        leg.position.set(tx * 0.66, 3.38, tz * 0.66);
        leg.lookAt(tx * 0.3, 4.95, tz * 0.3);
        leg.rotateX(Math.PI / 2);
        leg.castShadow = true;
        rig.add(leg);
      });
      const midBrace = new THREE.Mesh(makeRoundedBoxGeo(1.18, 0.14, 1.18, 0.03), indMats.safetyYellow);
      midBrace.position.y = 3.45;
      rig.add(midBrace);
      const crown = new THREE.Mesh(makeRoundedBoxGeo(1.08, 0.34, 1.08, 0.05), indMats.safetyYellow);
      crown.position.y = 4.92;
      crown.castShadow = true;
      rig.add(crown);
      const crownBeacon = new THREE.Mesh(sphereGeo, indMats.glowCyan);
      crownBeacon.scale.setScalar(0.16);
      crownBeacon.position.y = 5.22;
      rig.add(crownBeacon);

      addElbowStack(rig, 0.96, 2.05, -0.78, 1.65, 0.16, indMats.steelDark, indMats.indOrange);
      addPipeRun(rig, [[-0.95, 1.92, 0.15], [-0.65, 2.55, 0.15], [-0.25, 2.75, 0]], 0.075, indMats.rubberDark);

      // Huge Reciprocating Helical Kelly-Drive Auger
      const augerGroup = new THREE.Group();
      augerGroup.position.set(0, 2.25, 0);
      const driveBox = new THREE.Mesh(makeRoundedBoxGeo(0.62, 0.48, 0.62, 0.05), indMats.panelBlue);
      driveBox.position.y = 1.18;
      augerGroup.add(driveBox);
      const shaft = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.06, 2.55, 14), indMats.chrome);
      shaft.castShadow = true;
      augerGroup.add(shaft);
      for (let hi = 0; hi < 7; hi++) {
        const fin = new THREE.Mesh(new THREE.CylinderGeometry(0.46 - hi * 0.045, 0.46 - hi * 0.045, 0.068, 10), indMats.brass);
        fin.position.y = 0.88 - hi * 0.3;
        fin.rotation.x = 0.26;
        fin.rotation.y = hi * 0.9;
        augerGroup.add(fin);
      }
      rig.add(augerGroup);
      anim.rotParts.push({ mesh: augerGroup, axis: "y", speed: 8.0 });
      anim.pistons.push({ mesh: augerGroup, baseY: 2.25, amp: 0.32, speed: 3.8 });
      anim.smokeOrigin = { lx: 0.96, ly: 4.12, lz: -0.78, col: 0x9ca3af };

    } else if (def.id === "ore_processor") {
      // 6. ORE PROCESSING PLANT (~4.5u tall): Stepped Plinth + Massive Rotary Trommel + Mezzanine + Hydro-Cyclone Dome + Slurry Chute
      addSteppedPlinth(rig, 2.98, 2.82, true, indMats.concreteBase);

      const stand = new THREE.Mesh(makeRoundedBoxGeo(2.45, 0.78, 1.98, 0.06), indMats.steelDark);
      stand.position.set(0, 0.72, -0.06);
      stand.castShadow = true;
      rig.add(stand);

      // Inclined Heavy Rotary Trommel Drum with Steel Tyre Bands
      const drumMount = new THREE.Group();
      drumMount.position.set(0, 1.56, 0.14);
      drumMount.rotation.z = -0.18;
      const drumSpin = new THREE.Group();
      const drumBody = new THREE.Mesh(new THREE.CylinderGeometry(0.68, 0.68, 1.95, 16), indMats.indOrange);
      drumBody.rotation.z = Math.PI / 2;
      drumBody.castShadow = true;
      drumSpin.add(drumBody);
      [-0.66, 0, 0.66].forEach((bx, bi) => {
        const ring = new THREE.Mesh(
          new THREE.CylinderGeometry(bi === 1 ? 0.76 : 0.72, bi === 1 ? 0.76 : 0.72, 0.12, 16),
          bi === 1 ? indMats.safetyYellow : indMats.chrome
        );
        ring.rotation.z = Math.PI / 2;
        ring.position.x = bx;
        drumSpin.add(ring);
      });
      drumMount.add(drumSpin);
      rig.add(drumMount);
      anim.rotParts.push({ mesh: drumSpin, axis: "x", speed: 4.5 });

      addPressureTank(rig, -1.04, 1.15, 0.72, 0.28, 0.98, false, indMats.panelBlue, indMats.chrome);
      addMotorPod(rig, 0.92, 1.15, -0.78, 1.0, indMats.panelBlue);

      // 2nd-Story Inspection Catwalk + Safety Rails + Upper Cyclone Silo Dome
      addCantileverDeck(rig, 2.94, 2.48, 2.38, 0.36, indMats.safetyYellow, true);
      addLadder(rig, -1.44, 0.36, 2.42, -0.58, -Math.PI / 2, indMats.safetyYellow);
      addSiloSuperstructure(rig, 0.05, 2.46, -0.25, 0.78, 1.18, indMats.domeCream, indMats.panelBlue, indMats.glowCyan);
      addCoolantChute(rig, 0.05, 2.88, 0.56, 0, indMats.coolantChute);
      addPipeRun(rig, [[-1.04, 1.78, 0.72], [-0.95, 3.05, 0.12], [0.05, 3.65, -0.25]], 0.08, indMats.chrome);

      const conc = createProductCrate(0xea580c, true);
      conc.scale.setScalar(1.08);
      conc.position.set(0.65, 0.95, 0.85);
      rig.add(conc);
      anim.crates.push({ mesh: conc, axis: "x", min: -0.45, max: 1.05, speed: 0.52 });

    } else if (def.id === "smelting_furnace") {
      // 7. SMELTING BLAST FURNACE (upgraded to Kenney building-b + chimney-basic + container-a with molten hearth & smoke)
      const legacyBody = new THREE.Group();
      rig.add(legacyBody);
      addSteppedPlinth(legacyBody, 3.0, 2.86, true, indMats.concreteBase);

      const vessel = new THREE.Mesh(
        new THREE.CylinderGeometry(0.96, 1.18, 2.45, 16),
        pbrMat(0x6b211a, { map: texCobble, roughness: 0.76 })
      );
      vessel.position.set(-0.22, 1.56, -0.1);
      vessel.castShadow = true;
      legacyBody.add(vessel);

      [0.85, 1.55, 2.25, 2.75].forEach((ry) => {
        const band = new THREE.Mesh(new THREE.CylinderGeometry(1.16 - ry * 0.07, 1.16 - ry * 0.07, 0.12, 16), indMats.steelDark);
        band.position.set(-0.22, ry, -0.1);
        legacyBody.add(band);
      });

      // Glowing Molten Crucible Hearth & Cast-Iron Tap Arch (kept visible front-of-building)
      const hearthArch = new THREE.Mesh(makeRoundedBoxGeo(0.94, 0.82, 0.42, 0.05), indMats.steelDark);
      hearthArch.position.set(-0.22, 0.76, 0.96);
      rig.add(hearthArch);
      const hearth = new THREE.Mesh(sphereGeo, pbrMat(0xff6b00, { emissive: 0xff4500, emissiveIntensity: 1.5, roughness: 0.18 }));
      hearth.scale.set(0.38, 0.34, 0.26);
      hearth.position.set(-0.22, 0.76, 1.08);
      rig.add(hearth);

      // Twin Hot-Blast Preheater Stove Towers + 2nd-Story Charging Deck
      addPressureTank(legacyBody, 1.06, 1.32, -0.45, 0.38, 1.65, false, indMats.domeCream, indMats.indOrange);
      addCantileverDeck(legacyBody, 2.94, 2.48, 2.38, 0.36, indMats.safetyYellow, false);
      addLadder(legacyBody, 1.46, 0.36, 2.42, 0.1, Math.PI / 2, indMats.safetyYellow);

      const topDome = new THREE.Mesh(new THREE.CylinderGeometry(0.45, 0.98, 0.75, 16), indMats.steelMid);
      topDome.position.set(-0.22, 3.16, -0.1);
      topDome.castShadow = true;
      legacyBody.add(topDome);
      const cyanRing = new THREE.Mesh(new THREE.TorusGeometry(0.98, 0.052, 8, 24), indMats.glowAmber);
      cyanRing.rotation.x = Math.PI / 2;
      cyanRing.position.set(-0.22, 2.65, -0.1);
      legacyBody.add(cyanRing);

      addPipeRun(legacyBody, [[1.06, 2.28, -0.45], [0.58, 3.12, -0.25], [-0.22, 3.42, -0.1]], 0.095, indMats.copper);
      addElbowStack(legacyBody, -0.58, 2.45, -0.78, 2.15, 0.26, indMats.steelDark, indMats.indOrange);

      addBoltsInstanced(legacyBody, [
        [-0.6, 1.12, 1.0], [0.16, 1.12, 1.0], [-0.6, 0.44, 1.0], [0.16, 0.44, 1.0]
      ]);

      const bar = createProductCrate(0x94a3b8, true);
      bar.scale.setScalar(1.08);
      bar.position.set(0.65, 0.48, 1.05);
      rig.add(bar);
      anim.crates.push({ mesh: bar, axis: "z", min: 0.6, max: 1.24, speed: 0.42 });
      anim.smokeOrigin = { lx: -0.2, ly: 5.05, lz: -0.78, col: 0x9ca3af };
      if (window.ModelAssetManager) {
        window.ModelAssetManager.registerMachineMount(def, rig, legacyBody, 0.16, () => {
          hearthArch.position.set(-0.12, 0.68, 0.78);
          hearth.position.set(-0.12, 0.68, 0.90);
          anim.smokeOrigin = { lx: -0.68, ly: 3.22, lz: -0.78, col: 0x9ca3af };
        });
      }

    } else if (def.id === "gold_refinery") {
      // 8. GOLD REFINERY (upgraded to Kenney building-c + chimney-small + detail-tank + solar-panel-flat)
      const legacyBody = new THREE.Group();
      rig.add(legacyBody);
      addSteppedPlinth(legacyBody, 3.0, 2.86, true, indMats.concreteBase);

      const lowerBay = new THREE.Mesh(makeRoundedBoxGeo(2.05, 1.22, 1.75, 0.08), indMats.steelDark);
      lowerBay.position.set(0, 0.96, -0.22);
      lowerBay.castShadow = true;
      legacyBody.add(lowerBay);

      const centerGreenDrum = new THREE.Mesh(new THREE.CylinderGeometry(0.46, 0.46, 0.98, 16), indMats.turbineGreen);
      centerGreenDrum.rotation.z = Math.PI / 2;
      centerGreenDrum.position.set(0, 0.88, 0.88);
      centerGreenDrum.castShadow = true;
      legacyBody.add(centerGreenDrum);

      // Twin Pressurized Horizontal Saddle Tanks with Orange Straps & Front Elbow Pipes
      addPressureTank(legacyBody, -1.04, 0.94, 0.25, 0.34, 1.02, true, indMats.domeCream, indMats.indOrange);
      addPressureTank(legacyBody, 1.04, 0.94, 0.25, 0.34, 1.02, true, indMats.domeCream, indMats.indOrange);
      addPipeRun(legacyBody, [[-1.62, 0.94, 0.25], [-1.72, 0.42, 0.88], [-1.2, 0.42, 1.15]], 0.075, indMats.safetyYellow);
      addPipeRun(legacyBody, [[1.62, 0.94, 0.25], [1.72, 0.42, 0.88], [1.2, 0.42, 1.15]], 0.075, indMats.safetyYellow);

      // Cantilevered 2nd-Story Deck with Safety Amber Rails & Open Coolant Chute Gap + Side Balcony Sub-Tower
      addCantileverDeck(legacyBody, 3.04, 2.75, 1.98, 0.36, indMats.safetyYellow, true);
      addLadder(legacyBody, -1.48, 0.36, 2.02, -0.45, -Math.PI / 2, indMats.safetyYellow);
      addSideBalconyTower(legacyBody, 1.48, -0.78, 2.45, 0.58, indMats.steelMid, indMats.safetyYellow, indMats.glowCyan);

      // Towering 3rd-Story Ribbed Cream Domed Silo + Glowing Cyan Conduit Rings + Cascading Coolant Chute
      addSiloSuperstructure(legacyBody, -0.1, 2.06, -0.22, 0.98, 1.38, indMats.domeCream, indMats.domeCream, indMats.glowCyan);
      addCoolantChute(legacyBody, -0.1, 2.52, 0.72, 0, indMats.coolantChute);
      addPipeRun(legacyBody, [[-1.04, 1.3, 0.25], [-1.18, 2.95, -0.22], [-0.95, 3.25, -0.22]], 0.09, indMats.domeCream);
      addPipeRun(legacyBody, [[1.04, 1.3, 0.25], [1.18, 2.55, 0.05], [0.82, 2.65, -0.22]], 0.075, indMats.brass);
      addElbowStack(legacyBody, -0.58, 3.55, -0.72, 0.95, 0.2, indMats.safetyYellow, indMats.steelDark);

      const rotorPivot = new THREE.Group();
      rotorPivot.position.set(0, 0.88, 1.24);
      const rotor = new THREE.Mesh(gearWheelGeo, indMats.brass);
      rotor.scale.setScalar(1.05);
      rotor.castShadow = true;
      rotorPivot.add(rotor);
      rig.add(rotorPivot);
      anim.rotParts.push({ mesh: rotorPivot, axis: "z", speed: 6.5 });

      const goldBar = createProductCrate(0xfde047, true);
      goldBar.scale.setScalar(1.1);
      goldBar.position.set(0.55, 0.45, 1.2);
      rig.add(goldBar);
      anim.smokeOrigin = { lx: -0.26, ly: 4.95, lz: -0.72, col: 0xfef08a };
      if (window.ModelAssetManager) {
        window.ModelAssetManager.registerMachineMount(def, rig, legacyBody, 0.16, () => {
          rotorPivot.position.set(-0.05, 0.82, 1.06);
          anim.smokeOrigin = { lx: -0.68, ly: 1.82, lz: -0.78, col: 0xfef08a };
        });
      }

    } else if (def.id === "crystal_processor") {
      // 9. CRYSTAL PROCESSOR COMPLEX (upgraded to Kenney building-h + detail-tank-large + rooftop crystal gimbal)
      const legacyBody = new THREE.Group();
      rig.add(legacyBody);
      addSteppedPlinth(legacyBody, 2.98, 2.78, true, indMats.concreteBase);

      const baseBay = new THREE.Mesh(makeRoundedBoxGeo(2.38, 1.28, 1.98, 0.08), indMats.panelBlue);
      baseBay.position.set(0, 0.98, -0.1);
      baseBay.castShadow = true;
      legacyBody.add(baseBay);

      const coolPod = new THREE.Mesh(makeRoundedBoxGeo(1.12, 0.88, 0.78, 0.08), indMats.domeCream);
      coolPod.position.set(0.42, 0.82, 0.92);
      coolPod.castShadow = true;
      legacyBody.add(coolPod);
      const coolVent = new THREE.Mesh(makeRoundedBoxGeo(0.72, 0.5, 0.06, 0.01), indMats.vents);
      coolVent.position.set(0.42, 0.84, 1.31);
      legacyBody.add(coolVent);
      const podLight = new THREE.Mesh(makeRoundedBoxGeo(0.24, 0.46, 0.08, 0.01), indMats.glowCyan);
      podLight.position.set(-0.35, 0.84, 0.94);
      legacyBody.add(podLight);

      addPressureTank(legacyBody, -1.04, 1.05, 0.35, 0.28, 0.92, true, indMats.domeCream, indMats.indOrange);
      addCantileverDeck(legacyBody, 2.94, 2.58, 1.96, 0.36, indMats.safetyYellow, false);
      addLadder(legacyBody, 1.44, 0.36, 2.0, 0.2, Math.PI / 2, indMats.safetyYellow);
      addSideBalconyTower(legacyBody, -1.42, -0.78, 2.38, 0.52, indMats.panelBlue, indMats.safetyYellow, indMats.glowCyan);

      // Upper Cryo Chamber Crown + Triple Gimbal Rings + Levitating Prism
      [-0.86, 0.86].forEach((px) => {
        const pylon = new THREE.Mesh(makeRoundedBoxGeo(0.24, 1.95, 0.24, 0.04), indMats.steelMid);
        pylon.position.set(px, 3.0, 0);
        pylon.castShadow = true;
        legacyBody.add(pylon);
      });
      const crownRing = new THREE.Mesh(new THREE.CylinderGeometry(0.72, 0.95, 0.42, 16), indMats.domeCream);
      crownRing.position.set(0, 4.18, 0);
      crownRing.castShadow = true;
      legacyBody.add(crownRing);
      const crownCap = new THREE.Mesh(new THREE.CylinderGeometry(0.24, 0.72, 0.36, 16), indMats.glowCyan);
      crownCap.position.set(0, 4.56, 0);
      legacyBody.add(crownCap);

      addPipeRun(legacyBody, [[-1.04, 1.38, 0.35], [-1.04, 2.55, -0.4], [-0.45, 3.98, 0]], 0.07, indMats.chrome);

      const fxMount = new THREE.Group();
      fxMount.position.set(0, 3.05, 0);
      rig.add(fxMount);

      const ringA = new THREE.Mesh(new THREE.TorusGeometry(0.84, 0.062, 10, 26), indMats.glowCyan);
      fxMount.add(ringA);
      const ringB = new THREE.Mesh(new THREE.TorusGeometry(0.64, 0.052, 10, 24), indMats.brass);
      ringB.rotation.y = Math.PI / 2;
      fxMount.add(ringB);
      anim.rotParts.push({ mesh: ringA, axis: "x", speed: 2.6 });
      anim.rotParts.push({ mesh: ringB, axis: "y", speed: 3.4 });

      const gem = new THREE.Mesh(crystalPrismGeo, pbrMat(0x67e8f9, { emissive: 0x22d3ee, emissiveIntensity: 0.82, roughness: 0.12, metalness: 0.22 }));
      gem.scale.setScalar(0.82);
      fxMount.add(gem);
      anim.rotParts.push({ mesh: gem, axis: "y", speed: 2.0 });
      if (window.ModelAssetManager) {
        window.ModelAssetManager.registerMachineMount(def, rig, legacyBody, 0.16, (metrics) => {
          fxMount.position.set(metrics.mainX, metrics.mainTopY + 0.86, metrics.mainZ);
        });
      }

    } else if (def.id === "industrial_factory") {
      // 10. INDUSTRIAL MEGA-FACTORY (upgraded to Kenney building-p + twin chimneys + container-b + rooftop welder arms)
      const legacyBody = new THREE.Group();
      rig.add(legacyBody);
      addSteppedPlinth(legacyBody, 3.02, 2.9, true, indMats.concreteBase);

      const bay = new THREE.Mesh(makeRoundedBoxGeo(2.42, 1.32, 2.04, 0.08), indMats.panelBlue);
      bay.position.set(0, 1.0, -0.1);
      bay.castShadow = true;
      legacyBody.add(bay);

      const frontModule = new THREE.Mesh(makeRoundedBoxGeo(1.22, 0.88, 0.78, 0.08), indMats.domeCream);
      frontModule.position.set(0.45, 0.8, 0.98);
      frontModule.castShadow = true;
      legacyBody.add(frontModule);
      const frontVent = new THREE.Mesh(makeRoundedBoxGeo(0.84, 0.52, 0.06, 0.01), indMats.vents);
      frontVent.position.set(0.45, 0.82, 1.38);
      legacyBody.add(frontVent);
      addPressureTank(legacyBody, -1.04, 0.96, 0.35, 0.3, 0.98, true, indMats.domeCream, indMats.indOrange);

      const glowStrip = new THREE.Mesh(makeRoundedBoxGeo(2.48, 0.12, 2.08, 0.02), indMats.glowCyan);
      glowStrip.position.set(0, 1.66, -0.1);
      legacyBody.add(glowStrip);

      // 2nd-Story Structural Mezzanine Deck + Side Tower + Ladder
      addCantileverDeck(legacyBody, 3.04, 2.74, 1.98, 0.36, indMats.safetyYellow, false);
      addLadder(legacyBody, 1.48, 0.36, 2.02, 0.35, Math.PI / 2, indMats.safetyYellow);
      addSideBalconyTower(legacyBody, -1.46, -0.78, 2.42, 0.54, indMats.steelMid, indMats.safetyYellow, indMats.glowCyan);

      // 3rd-Tier Domed Assembly Reactor + Twin Elbow Smokestacks + Articulated Robotic Arms
      addSiloSuperstructure(legacyBody, 0, 2.06, -0.48, 0.72, 1.18, indMats.domeCream, indMats.panelBlue, indMats.glowCyan);
      addElbowStack(legacyBody, 0.96, 2.06, -0.82, 2.38, 0.22, indMats.steelDark, indMats.indOrange);
      addElbowStack(legacyBody, -0.96, 2.06, -0.82, 1.95, 0.18, indMats.steelDark, indMats.safetyYellow);
      addPipeRun(legacyBody, [[-0.96, 3.15, -0.82], [0, 3.55, -0.48], [0.96, 3.35, -0.82]], 0.08, indMats.chrome);

      const fxMount = new THREE.Group();
      fxMount.position.set(0, 2.08, 0.62);
      rig.add(fxMount);

      [-0.56, 0.64].forEach((ax, ai) => {
        const armBase = new THREE.Group();
        armBase.position.set(ax, 0, 0);
        const pedestal = new THREE.Mesh(new THREE.CylinderGeometry(0.22, 0.26, 0.26, 10), indMats.steelMid);
        armBase.add(pedestal);
        const upper = new THREE.Mesh(makeCapsuleGeo(0.095, 0.92, 8), indMats.safetyYellow);
        upper.position.y = 0.55;
        upper.rotation.z = ai === 0 ? -0.45 : 0.45;
        upper.castShadow = true;
        armBase.add(upper);
        const fore = new THREE.Mesh(makeCapsuleGeo(0.07, 0.64, 8), indMats.steelLight);
        fore.position.set(ai === 0 ? 0.28 : -0.28, 0.94, 0.18);
        fore.rotation.x = 0.65;
        armBase.add(fore);
        const head = new THREE.Mesh(octaGeo, indMats.glowCyan);
        head.scale.setScalar(0.16);
        head.position.set(ai === 0 ? 0.28 : -0.28, 0.9, 0.46);
        armBase.add(head);
        fxMount.add(armBase);
        anim.rotParts.push({ mesh: armBase, axis: "y", speed: ai === 0 ? 2.4 : -2.4 });
      });

      addBoltsInstanced(legacyBody, [
        [-1.36, 2.08, 1.15], [1.36, 2.08, 1.15], [-1.36, 2.08, -1.15], [1.36, 2.08, -1.15]
      ]);
      anim.smokeOrigin = { lx: 1.32, ly: 4.86, lz: -0.82, col: 0xe879f9 };
      if (window.ModelAssetManager) {
        window.ModelAssetManager.registerMachineMount(def, rig, legacyBody, 0.16, (metrics) => {
          fxMount.position.set(0, metrics.mainTopY + 0.08, -0.18);
          anim.smokeOrigin = { lx: 0.92, ly: 1.45, lz: -0.82, col: 0xe879f9 };
        });
      }

    } else if (def.id === "crystal_synthesizer") {
      // 11. AETHER-QUARTZ CRYSTAL SYNTHESIZER (upgraded to Kenney building-j + solar-panel-portrait + resonator rings)
      const legacyBody = new THREE.Group();
      rig.add(legacyBody);
      addSteppedPlinth(legacyBody, 2.98, 2.84, true, indMats.concreteBase);

      const lowerDeck = new THREE.Mesh(makeRoundedBoxGeo(2.38, 1.24, 1.98, 0.08), indMats.domeCream);
      lowerDeck.position.set(0, 0.96, -0.1);
      lowerDeck.castShadow = true;
      legacyBody.add(lowerDeck);

      const frontVents = new THREE.Mesh(makeRoundedBoxGeo(1.1, 0.56, 0.08, 0.02), indMats.vents);
      frontVents.position.set(0, 0.92, 0.91);
      legacyBody.add(frontVents);
      addPressureTank(legacyBody, -1.04, 1.0, 0.28, 0.3, 0.96, true, indMats.panelBlue, indMats.brass);
      addPressureTank(legacyBody, 1.04, 1.0, 0.28, 0.3, 0.96, true, indMats.panelBlue, indMats.brass);

      addCantileverDeck(legacyBody, 2.98, 2.66, 1.94, 0.36, indMats.safetyYellow, true);
      addLadder(legacyBody, -1.46, 0.36, 1.98, -0.35, -Math.PI / 2, indMats.safetyYellow);
      addSideBalconyTower(legacyBody, 1.46, -0.78, 2.42, 0.54, indMats.panelBlue, indMats.safetyYellow, indMats.glowCyan);

      // Upper Domed Condenser Core + Dual Rotating Torus Resonance Rings
      addSiloSuperstructure(legacyBody, -0.08, 2.02, -0.45, 0.68, 1.12, indMats.panelBlue, indMats.domeCream, indMats.glowCyan);
      addCoolantChute(legacyBody, -0.08, 2.42, 0.25, 0, indMats.coolantChute);
      addElbowStack(legacyBody, -0.96, 2.02, -0.78, 1.92, 0.18, indMats.steelDark, indMats.brass);

      const fxMount = new THREE.Group();
      fxMount.position.set(0, 3.68, 0.12);
      rig.add(fxMount);

      const dishRing = new THREE.Mesh(
        new THREE.TorusGeometry(0.92, 0.072, 10, 26),
        metalMat(0xf472b6, { emissive: 0xdb2777, emissiveIntensity: 0.55 })
      );
      fxMount.add(dishRing);
      anim.rotParts.push({ mesh: dishRing, axis: "y", speed: 2.8 });

      const dishRingInner = new THREE.Mesh(new THREE.TorusGeometry(0.68, 0.052, 10, 24), indMats.glowCyan);
      dishRingInner.rotation.x = Math.PI / 3;
      fxMount.add(dishRingInner);
      anim.rotParts.push({ mesh: dishRingInner, axis: "z", speed: -3.2 });

      const prism = new THREE.Mesh(crystalPrismGeo, pbrMat(0x7dd3fc, { emissive: 0x38bdf8, emissiveIntensity: 0.9, roughness: 0.14 }));
      prism.scale.setScalar(0.86);
      fxMount.add(prism);
      anim.rotParts.push({ mesh: prism, axis: "x", speed: 2.0 });
      addPipeRun(legacyBody, [[-1.04, 1.36, 0.28], [-1.04, 2.85, -0.35], [-0.45, 3.45, -0.35]], 0.07, indMats.chrome);
      anim.smokeOrigin = { lx: -0.68, ly: 4.35, lz: -0.78, col: 0xf472b6 };
      if (window.ModelAssetManager) {
        window.ModelAssetManager.registerMachineMount(def, rig, legacyBody, 0.16, (metrics) => {
          fxMount.position.set(metrics.mainX, metrics.mainTopY + 0.88, metrics.mainZ);
          anim.smokeOrigin = null;
        });
      }

    } else {
      // 12. TITAN ALLOY SUPER-FORGE (upgraded to Kenney building-t + container-a + flanking brass flywheels + plasma core)
      const legacyBody = new THREE.Group();
      rig.add(legacyBody);
      addSteppedPlinth(legacyBody, 3.04, 2.92, true, indMats.concreteBase);

      const hull = new THREE.Mesh(makeRoundedBoxGeo(2.42, 1.38, 2.06, 0.08), indMats.steelMid);
      hull.position.set(0, 1.04, -0.08);
      hull.castShadow = true;
      legacyBody.add(hull);

      const frontCore = new THREE.Mesh(makeRoundedBoxGeo(1.06, 0.88, 0.65, 0.06), indMats.steelDark);
      frontCore.position.set(0, 0.84, 0.96);
      legacyBody.add(frontCore);
      const plasmaSlit = new THREE.Mesh(makeRoundedBoxGeo(0.76, 0.42, 0.1, 0.02), indMats.glowCyan);
      plasmaSlit.position.set(0, 0.86, 1.26);
      legacyBody.add(plasmaSlit);

      const flywheels = [];
      [-1.24, 1.24].forEach((gx, gi) => {
        const fly = new THREE.Mesh(gearWheelGeo, indMats.brass);
        fly.scale.setScalar(2.05);
        fly.rotation.y = Math.PI / 2;
        fly.position.set(gx, 1.42, 0.05);
        fly.castShadow = true;
        rig.add(fly);
        flywheels.push(fly);
        anim.rotParts.push({ mesh: fly, axis: "x", speed: gi === 0 ? 3.2 : -3.2 });
      });

      addCantileverDeck(legacyBody, 3.04, 2.74, 2.04, 0.36, indMats.safetyYellow, false);
      addLadder(legacyBody, 1.48, 0.36, 2.08, 0.42, Math.PI / 2, indMats.safetyYellow);
      addSideBalconyTower(legacyBody, -1.46, -0.76, 2.52, 0.56, indMats.steelDark, indMats.safetyYellow, indMats.glowCyan);

      // 3rd-Tier Ribbed Plasma Dome + Cage Pillars + Spinning Arc Core + Twin Brass Stacks
      addSiloSuperstructure(legacyBody, 0, 2.12, -0.22, 0.88, 1.38, indMats.steelDark, indMats.domeCream, indMats.glowCyan);
      const fxMount = new THREE.Group();
      fxMount.position.set(0, 4.35, -0.22);
      rig.add(fxMount);
      const arcCageRing = new THREE.Mesh(new THREE.TorusGeometry(0.68, 0.055, 8, 22), indMats.brass);
      arcCageRing.rotation.x = Math.PI / 2;
      fxMount.add(arcCageRing);
      const arcCore = new THREE.Mesh(octaGeo, indMats.glowCyan);
      arcCore.scale.setScalar(0.56);
      arcCore.position.set(0, 0.07, 0);
      fxMount.add(arcCore);
      anim.rotParts.push({ mesh: arcCore, axis: "y", speed: 4.0 });

      addElbowStack(legacyBody, 0.96, 2.12, -0.8, 2.45, 0.24, indMats.copper, indMats.brass);
      addElbowStack(legacyBody, -0.96, 2.12, -0.8, 2.1, 0.2, indMats.copper, indMats.brass);
      addPipeRun(legacyBody, [[-0.96, 3.25, -0.8], [0, 3.65, -0.22], [0.96, 3.45, -0.8]], 0.085, indMats.brass);
      anim.smokeOrigin = { lx: 1.34, ly: 5.02, lz: -0.8, col: 0x38bdf8 };
      if (window.ModelAssetManager) {
        window.ModelAssetManager.registerMachineMount(def, rig, legacyBody, 0.16, (metrics) => {
          flywheels.forEach((f) => {
            f.position.y = 1.08;
            f.position.z = -0.18;
          });
          fxMount.position.set(metrics.mainX, metrics.mainTopY + 0.72, metrics.mainZ);
          anim.smokeOrigin = { lx: metrics.mainX, ly: metrics.mainTopY + 0.95, lz: metrics.mainZ, col: 0x38bdf8 };
        });
      }
    }

    getBiomeChunk(def.biome).add(grp);
    worldMachines[def.id] = { def, group: grp, pad, rig, statusLed, anim, crateT: 0 };
  }
  MACHINE_DEFS.forEach(createMachineModel);

  // =========================================================================
  // 8B. DETAILED 3D FLEET VEHICLES & CARGO CONTAINERS
  // =========================================================================
  const worldFleet = {};

  // 1. Patrolling Industrial Forklift (moves along Forest Logistics Lane x=9.6, z=-2..4)
  (function buildForklift() {
    const grp = new THREE.Group();
    grp.position.set(9.4, 0, -1.5);
    grp.visible = false;
    addContactShadow(grp, 1.35);

    const chassis = new THREE.Mesh(makeRoundedBoxGeo(1.05, 0.58, 1.45, 0.08), pbrMat(0xfbbf24, { roughness: 0.38, metalness: 0.35 }));
    chassis.position.y = 0.52;
    chassis.castShadow = true;
    grp.add(chassis);
    const roof = new THREE.Mesh(makeRoundedBoxGeo(0.85, 0.08, 0.95, 0.02), metalMat(0x1e293b));
    roof.position.set(0, 1.45, -0.1);
    grp.add(roof);
    [[-0.4, -0.5], [0.4, -0.5], [-0.4, 0.32], [0.4, 0.32]].forEach(([px, pz]) => {
      const post = new THREE.Mesh(makeCapsuleGeo(0.04, 0.75, 6), metalMat(0x1e293b));
      post.position.set(px, 1.08, pz);
      grp.add(post);
    });
    const mast = new THREE.Mesh(makeRoundedBoxGeo(0.75, 1.55, 0.14, 0.02), metalMat(0x334155));
    mast.position.set(0, 1.0, 0.85);
    grp.add(mast);
    const forkGrp = new THREE.Group();
    forkGrp.position.set(0, 0.35, 1.15);
    [-0.24, 0.24].forEach((fx) => {
      const tine = new THREE.Mesh(makeRoundedBoxGeo(0.1, 0.06, 0.78, 0.01), metalMat(0x94a3b8));
      tine.position.set(fx, 0, 0.25);
      forkGrp.add(tine);
    });
    const cargo = createProductCrate(0xd97706, false);
    cargo.scale.setScalar(1.2);
    cargo.position.set(0, 0.24, 0.32);
    forkGrp.add(cargo);
    grp.add(forkGrp);

    for (const [wx, wz] of [[-0.56, -0.48], [0.56, -0.48], [-0.56, 0.48], [0.56, 0.48]]) {
      const w = createWheelMesh(0.24, 0.18);
      w.position.set(wx, 0.24, wz);
      grp.add(w);
    }
    worldGroup.add(grp);
    worldFleet.forklift = { group: grp, forkGrp };
  })();

  // 2. Automated Mining Cart on Railroad Tracks (Forest-Mountain Quarry Rail)
  (function buildMiningCart() {
    const trackGrp = new THREE.Group();
    trackGrp.position.set(-5.8, 0, -9.2);
    trackGrp.visible = false;

    for (let si = -2.8; si <= 2.8; si += 0.7) {
      const tie = new THREE.Mesh(makeRoundedBoxGeo(0.24, 0.08, 1.35, 0.02), pbrMat(0x78350f, { map: texWoodPlank }));
      tie.position.set(si, 0.04, 0);
      trackGrp.add(tie);
    }
    [-0.45, 0.45].forEach((rz) => {
      const rail = new THREE.Mesh(makeRoundedBoxGeo(6.0, 0.08, 0.08, 0.01), metalMat(0x94a3b8));
      rail.position.set(0, 0.1, rz);
      trackGrp.add(rail);
    });

    const cart = new THREE.Group();
    cart.position.y = 0.14;
    const tub = new THREE.Mesh(makeRoundedBoxGeo(1.35, 0.65, 0.95, 0.08), metalMat(0x475569, { map: texBrushedMetal }));
    tub.position.y = 0.52;
    tub.castShadow = true;
    cart.add(tub);
    [[-0.25, 0xfbbf24], [0.22, 0xcbd5e1], [0, 0x22d3ee]].forEach(([ox, oCol], oi) => {
      const ore = new THREE.Mesh(makeOrganicGeo(0.24, 1, 0.22, oi), metalMat(oCol, { roughness: 0.25 }));
      ore.position.set(ox, 0.88, (oi - 1) * 0.12);
      cart.add(ore);
    });
    [[-0.45, -0.45], [0.45, -0.45], [-0.45, 0.45], [0.45, 0.45]].forEach(([wx, wz]) => {
      const wheel = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.18, 0.1, 12), metalMat(0x1e293b));
      wheel.rotation.x = Math.PI / 2;
      wheel.position.set(wx, 0.18, wz);
      cart.add(wheel);
    });
    trackGrp.add(cart);
    worldGroup.add(trackGrp);
    worldFleet.mining_cart = { group: trackGrp, cart };
  })();

  // 3. Heavy Delivery Truck Parked at Trading Post Loading Bay
  (function buildDeliveryTruck() {
    const grp = new THREE.Group();
    grp.position.set(-9.2, 0, 7.6);
    grp.rotation.y = 0.62;
    grp.scale.setScalar(1.08);
    grp.visible = false;
    addContactShadow(grp, 2.3);

    const chassis = new THREE.Mesh(makeRoundedBoxGeo(1.45, 0.26, 3.7, 0.05), metalMat(0x1e293b));
    chassis.position.y = 0.42;
    grp.add(chassis);
    const cargoBox = new THREE.Mesh(makeRoundedBoxGeo(1.56, 1.35, 2.4, 0.08), pbrMat(0x0284c7, { map: texBrushedMetal, roughness: 0.42 }));
    cargoBox.position.set(0, 1.22, -0.58);
    cargoBox.castShadow = true;
    grp.add(cargoBox);
    const cab = new THREE.Mesh(makeRoundedBoxGeo(1.42, 1.15, 1.05, 0.1), pbrMat(0xf8fafc, { roughness: 0.32, metalness: 0.2 }));
    cab.position.set(0, 1.1, 1.22);
    cab.castShadow = true;
    grp.add(cab);
    const windshield = new THREE.Mesh(makeRoundedBoxGeo(1.28, 0.46, 0.14, 0.02), metalMat(0x38bdf8, { roughness: 0.1, metalness: 0.9 }));
    windshield.position.set(0, 1.28, 1.72);
    grp.add(windshield);
    const beacon = new THREE.Mesh(sphereGeo, pbrMat(0xf59e0b, { emissive: 0xf59e0b, emissiveIntensity: 1.2 }));
    beacon.scale.setScalar(0.12);
    beacon.position.set(0, 1.78, 1.22);
    grp.add(beacon);

    for (const [wx, wz] of [[-0.78, -1.3], [0.78, -1.3], [-0.78, -0.35], [0.78, -0.35], [-0.78, 1.22], [0.78, 1.22]]) {
      const w = createWheelMesh(0.32, 0.22);
      w.position.set(wx, 0.32, wz);
      grp.add(w);
    }
    worldGroup.add(grp);
    worldFleet.delivery_truck = { group: grp, beacon };
  })();

  // 4. Heavy Articulated Excavator Rig
  (function buildExcavator() {
    const grp = new THREE.Group();
    grp.position.set(-11.8, 0, -11.4);
    grp.rotation.y = 0.78;
    grp.scale.setScalar(1.12);
    grp.visible = false;
    addContactShadow(grp, 2.0);

    // Crawler Tracks
    [-0.72, 0.72].forEach((tx) => {
      const track = new THREE.Mesh(makeRoundedBoxGeo(0.42, 0.44, 2.15, 0.18), pbrMat(0x1e293b, { roughness: 0.9 }));
      track.position.set(tx, 0.24, 0);
      track.castShadow = true;
      grp.add(track);
    });

    const turret = new THREE.Group();
    turret.position.y = 0.58;
    grp.add(turret);

    const engineBay = new THREE.Mesh(makeRoundedBoxGeo(1.42, 0.85, 1.55, 0.1), pbrMat(0x10b981, { roughness: 0.38, metalness: 0.35 }));
    engineBay.position.set(0, 0.45, -0.22);
    engineBay.castShadow = true;
    turret.add(engineBay);

    const cab = new THREE.Mesh(makeRoundedBoxGeo(0.68, 0.98, 0.92, 0.08), metalMat(0x334155, { roughness: 0.28 }));
    cab.position.set(-0.42, 0.65, 0.25);
    turret.add(cab);

    const boomPivot = new THREE.Group();
    boomPivot.position.set(0.28, 0.65, 0.42);
    turret.add(boomPivot);

    const boom = new THREE.Mesh(makeRoundedBoxGeo(0.24, 1.55, 0.28, 0.04), pbrMat(0xfbbf24, { roughness: 0.35, metalness: 0.4 }));
    boom.position.set(0, 0.68, 0.45);
    boom.rotation.x = 0.55;
    boom.castShadow = true;
    boomPivot.add(boom);

    const stick = new THREE.Mesh(makeRoundedBoxGeo(0.2, 1.15, 0.24, 0.04), metalMat(0xf59e0b));
    stick.position.set(0, 0.78, 1.28);
    stick.rotation.x = -0.78;
    boomPivot.add(stick);

    const bucket = new THREE.Mesh(new THREE.CylinderGeometry(0.32, 0.36, 0.62, 10, 1, false, 0, Math.PI * 1.25), metalMat(0x475569));
    bucket.rotation.z = Math.PI / 2;
    bucket.position.set(0, 0.18, 1.65);
    boomPivot.add(bucket);

    worldGroup.add(grp);
    worldFleet.excavator = { group: grp, turret, boomPivot };
  })();

  // 5. Stacked Corrugated Cargo Containers (beside Warehouse x=11.8, z=-6.5)
  (function buildContainers() {
    const grp = new THREE.Group();
    grp.position.set(11.6, 0, -6.5);
    grp.scale.setScalar(1.06);
    grp.visible = false;
    addContactShadow(grp, 2.2);

    const containers = [];
    const cols = [0x0284c7, 0xd97706, 0x10b981];
    for (let ci = 0; ci < 3; ci++) {
      const c = new THREE.Mesh(
        makeRoundedBoxGeo(2.6, 1.08, 1.32, 0.04),
        pbrMat(cols[ci], { map: texBrushedMetal, roughness: 0.48, metalness: 0.55 })
      );
      c.position.set(ci === 2 ? 0.1 : 0, 0.56 + (ci % 2) * 1.1, ci === 2 ? 1.45 : 0);
      c.castShadow = true;
      c.visible = false;
      grp.add(c);
      containers.push(c);
    }
    worldGroup.add(grp);
    worldFleet.storage_container = { group: grp, containers };
  })();

  // =========================================================================
  // 9. ORGANIC BRANCHING TREES & NATURAL STRATIFIED MINERAL NODES
  // =========================================================================
  const resourceNodes = [];
  const nodesByBiome = {};
  const flyingDrops = [];

  function createNodeMesh(kind, biomeId, variantIdx) {
    const grp = new THREE.Group();
    addContactShadow(grp, kind === "wood" ? 1.45 : 1.15);

    const visualRoot = new THREE.Group();
    grp.add(visualRoot);
    let crackMesh = null;

    if (kind === "wood") {
      const isPine = biomeId === "mountains" || biomeId === "frozen" || biomeId === "frozen_mountains" || (biomeId === "forest" && (variantIdx % 3 === 1));
      const trunkPts = [
        new THREE.Vector2(0.48, 0.0),
        new THREE.Vector2(0.30, 0.25),
        new THREE.Vector2(0.22, 0.85),
        new THREE.Vector2(0.16, 1.65),
        new THREE.Vector2(0.08, isPine ? 2.65 : 2.3)
      ];
      const trunkGeo = new THREE.LatheGeometry(trunkPts, 10);
      const trunk = new THREE.Mesh(trunkGeo, pbrMat(biomeId === "toxic_swamp" ? 0x5c5346 : 0xe4cfb8, { map: texBark, bumpMap: texBarkBump, bumpScale: 0.09, roughness: 0.88 }));
      trunk.castShadow = true;
      trunk.receiveShadow = true;
      visualRoot.add(trunk);

      // Axe Chop Wedge Notch
      crackMesh = new THREE.Mesh(
        new THREE.ConeGeometry(0.28, 0.22, 4),
        pbrMat(0xd97706, { roughness: 0.65 })
      );
      crackMesh.rotation.z = Math.PI / 2;
      crackMesh.position.set(0.18, 0.72, 0);
      crackMesh.visible = false;
      visualRoot.add(crackMesh);

      if (isPine) {
        const pineDark = biomeId === "frozen" || biomeId === "frozen_mountains" ? 0x67c5c9 : 0x1a743b;
        const pineLit = biomeId === "frozen" || biomeId === "frozen_mountains" ? 0xa5e6eb : 0x269b50;
        const tiers = [
          [1.62, 1.25, 1.35, pineDark],
          [2.32, 0.98, 1.22, pineLit],
          [2.95, 0.72, 1.12, pineDark],
          [3.52, 0.45, 0.95, pineLit]
        ];
        tiers.forEach(([ty, tr, th, col], ti) => {
          const tierCone = new THREE.Mesh(
            new THREE.ConeGeometry(tr, th, 9),
            pbrMat(col, { map: texFoliage, flatShading: true, roughness: 0.82 })
          );
          tierCone.position.y = ty;
          tierCone.rotation.y = ti * 0.65;
          tierCone.castShadow = true;
          tierCone.receiveShadow = true;
          visualRoot.add(tierCone);
        });
      } else {
        for (let bi = 0; bi < 3; bi++) {
          const bAng = bi * (Math.PI * 2 / 3) + 0.4;
          const bough = new THREE.Mesh(makeCapsuleGeo(0.068, 0.74, 7), pbrMat(0xe4cfb8, { map: texBark, roughness: 0.9 }));
          bough.position.set(Math.cos(bAng) * 0.26, 1.34 + bi * 0.16, Math.sin(bAng) * 0.26);
          bough.rotation.z = -Math.cos(bAng) * 0.72;
          bough.rotation.x = Math.sin(bAng) * 0.72;
          bough.castShadow = true;
          visualRoot.add(bough);
        }

        const leafA = biomeId === "frozen" ? 0x6fd1cb : (biomeId === "jungle" ? 0x0d7a55 : (biomeId === "sky_islands" ? 0x38bdf8 : (biomeId === "toxic_swamp" ? 0x65a30d : 0x268f40)));
        const leafB = biomeId === "frozen" ? 0xa7ece8 : (biomeId === "jungle" ? 0x16a36b : (biomeId === "sky_islands" ? 0x7dd3fc : (biomeId === "toxic_swamp" ? 0x84cc16 : 0x3eb857)));
        const clusters = [
          [0, 2.2, 0, 1.18, 0.88, leafA],
          [-0.6, 1.92, 0.42, 0.82, 0.64, leafB],
          [0.64, 1.96, -0.35, 0.78, 0.62, leafA],
          [0.14, 2.74, 0.22, 0.76, 0.7, leafB],
          [0, 3.22, 0, 0.56, 0.56, leafB]
        ];
        clusters.forEach(([lx, ly, lz, sx, sy, col], ci) => {
          const foliage = new THREE.Mesh(
            makeOrganicGeo(1, 1, 0.25, ci + 1.5),
            pbrMat(col, { map: texFoliage, roughness: 0.8 })
          );
          foliage.scale.set(sx, sy, sx);
          foliage.position.set(lx, ly, lz);
          foliage.castShadow = true;
          foliage.receiveShadow = true;
          visualRoot.add(foliage);
        });
      }
    } else if (kind === "crystal" || kind === "rare" || kind === "quartz" || kind === "aether" || kind === "frostite") {
      const col = RESOURCES[kind].color;
      // Stone Cluster Base + Faceted Hexagonal Crystal Spires
      const baseBed = new THREE.Mesh(organicRockGeoA, pbrMat(kind === "aether" ? 0xe0f2fe : 0x64748b, { map: texRock, flatShading: true, roughness: 0.88 }));
      baseBed.scale.set(0.92, 0.36, 0.88);
      baseBed.position.y = 0.22;
      baseBed.castShadow = true;
      visualRoot.add(baseBed);

      const shardMat = pbrMat(col, {
        emissive: col,
        emissiveIntensity: 0.42,
        roughness: 0.16,
        metalness: 0.28,
        flatShading: true
      });
      [[0, 0.84, 0, 1.0, 1.18, 0], [-0.36, 0.62, 0.24, 0.68, 0.84, 0.35], [0.38, 0.58, -0.2, 0.65, 0.78, -0.32]].forEach(([sx, sy, sz, rx, ry, rz]) => {
        const sp = new THREE.Mesh(crystalPrismGeo, shardMat);
        sp.scale.set(rx, ry, rx);
        sp.position.set(sx, sy, sz);
        sp.rotation.z = rz;
        sp.castShadow = true;
        visualRoot.add(sp);
      });
      crackMesh = new THREE.Mesh(new THREE.RingGeometry(0.45, 0.68, 6), basicMat(0xffffff, { side: THREE.DoubleSide }));
      crackMesh.rotation.x = -Math.PI / 2;
      crackMesh.position.y = 0.35;
      crackMesh.visible = false;
      visualRoot.add(crackMesh);
    } else if (kind === "venom") {
      // Luminescent Bio-Resin Spore Node
      const bed = new THREE.Mesh(organicRockGeoA, pbrMat(0x3f4d2c, { map: texRock, flatShading: true }));
      bed.scale.set(0.9, 0.35, 0.9);
      bed.position.y = 0.22;
      visualRoot.add(bed);
      const podMat = pbrMat(0x84cc16, { emissive: 0x65a30d, emissiveIntensity: 0.48, roughness: 0.3 });
      [[0, 0.78, 0, 0.42], [-0.38, 0.56, 0.25, 0.28], [0.36, 0.54, -0.22, 0.26]].forEach(([px, py, pz, pr]) => {
        const pod = new THREE.Mesh(sphereGeo, podMat);
        pod.scale.set(pr, pr * 1.2, pr);
        pod.position.set(px, py, pz);
        pod.castShadow = true;
        visualRoot.add(pod);
      });
      crackMesh = new THREE.Mesh(new THREE.RingGeometry(0.45, 0.68, 6), basicMat(0xbef264, { side: THREE.DoubleSide }));
      crackMesh.rotation.x = -Math.PI / 2;
      crackMesh.position.y = 0.34;
      crackMesh.visible = false;
      visualRoot.add(crackMesh);
    } else if (kind === "titan_alloy") {
      // Ancient Cog-Plated Resource Core
      const chassis = new THREE.Mesh(makeRoundedBoxGeo(1.05, 0.85, 1.05, 0.12), indMats.steelMid);
      chassis.position.y = 0.48;
      chassis.castShadow = true;
      visualRoot.add(chassis);
      const cog = new THREE.Mesh(gearWheelGeo, indMats.brass);
      cog.scale.setScalar(1.3);
      cog.position.set(0, 0.96, 0);
      cog.rotation.x = Math.PI / 2;
      visualRoot.add(cog);
      const core = new THREE.Mesh(octaGeo, indMats.glowCyan);
      core.scale.setScalar(0.32);
      core.position.set(0, 1.28, 0);
      visualRoot.add(core);
      crackMesh = new THREE.Mesh(new THREE.RingGeometry(0.45, 0.72, 8), basicMat(0x38bdf8, { side: THREE.DoubleSide }));
      crackMesh.rotation.x = -Math.PI / 2;
      crackMesh.position.y = 0.36;
      crackMesh.visible = false;
      visualRoot.add(crackMesh);
    } else {
      // Natural Irregular Stratified Boulder + Moss Collar + Ore Seam Deposits
      const mainGeo = kind === "stone" ? organicRockGeoA : organicRockGeoB;
      const baseRock = new THREE.Mesh(
        mainGeo,
        pbrMat(kind === "coal" ? 0x4b525c : (kind === "obsidian" ? 0x261a2e : 0x8c97a4), { map: texRock, flatShading: true, roughness: 0.88 })
      );
      baseRock.scale.set(0.98, 0.74, 0.9);
      baseRock.position.y = 0.52;
      baseRock.castShadow = true;
      baseRock.receiveShadow = true;
      visualRoot.add(baseRock);

      const sideRock = new THREE.Mesh(organicRockGeoA, pbrMat(0x6d7987, { map: texRock, flatShading: true, roughness: 0.9 }));
      sideRock.scale.set(0.54, 0.4, 0.48);
      sideRock.position.set(0.62, 0.26, 0.32);
      sideRock.castShadow = true;
      visualRoot.add(sideRock);

      if (biomeId === "forest") {
        const mossCap = new THREE.Mesh(organicCanopyGeo, pbrMat(0x2e853e, { map: texFoliage, roughness: 0.85 }));
        mossCap.scale.set(0.46, 0.14, 0.42);
        mossCap.position.set(-0.22, 0.92, -0.15);
        visualRoot.add(mossCap);
      }

      crackMesh = new THREE.Mesh(new THREE.BoxGeometry(1.15, 0.08, 0.14), pbrMat(0x0f172a));
      crackMesh.position.y = 0.62;
      crackMesh.rotation.z = 0.35;
      crackMesh.visible = false;
      visualRoot.add(crackMesh);

      if (kind !== "stone") {
        const oreCol = RESOURCES[kind].color;
        const oreMat = metalMat(oreCol, {
          emissive: kind === "gold" ? 0xb45309 : (kind === "obsidian" ? 0x9333ea : 0x000000),
          emissiveIntensity: kind === "obsidian" ? 0.35 : 0.22,
          roughness: 0.25,
          metalness: 0.6,
          flatShading: true
        });
        for (let i = 0; i < 6; i++) {
          const nugget = new THREE.Mesh(makeOrganicGeo(0.22, 0, 0.26, i + 2), oreMat);
          const ang = i * 1.05;
          nugget.position.set(Math.cos(ang) * 0.56, 0.54 + (i % 2) * 0.24, Math.sin(ang) * 0.54);
          nugget.rotation.set(0.4, ang, 0.3);
          nugget.castShadow = true;
          visualRoot.add(nugget);
        }
      }
    }

    return { grp, visualRoot, crackMesh };
  }

  function populateBiomeNodes() {
    const specs = [
      { biome: "forest", kind: "wood", x: -5.2, z: -0.8, hp: 3 },
      { biome: "forest", kind: "wood", x: 5.0, z: 1.4, hp: 3 },
      { biome: "forest", kind: "stone", x: -2.6, z: -5.0, hp: 3 },
      { biome: "forest", kind: "stone", x: 4.0, z: -4.8, hp: 3 },
      { biome: "forest", kind: "wood", x: -10, z: 9, hp: 3 },
      { biome: "forest", kind: "wood", x: -12, z: 3, hp: 3 },
      { biome: "forest", kind: "wood", x: -11, z: -11, hp: 3 },
      { biome: "forest", kind: "wood", x: 10, z: -10, hp: 3 },
      { biome: "forest", kind: "wood", x: 7.4, z: 12.2, hp: 3 },
      { biome: "forest", kind: "wood", x: 2, z: 12, hp: 3 },
      { biome: "forest", kind: "wood", x: -7.2, z: -9.5, hp: 3 },
      { biome: "forest", kind: "wood", x: 8.6, z: -10.8, hp: 3 },
      { biome: "forest", kind: "stone", x: -4, z: -11, hp: 3 },
      { biome: "forest", kind: "stone", x: 13.0, z: -0.6, hp: 3 },
      { biome: "forest", kind: "stone", x: -12, z: -2, hp: 3 },

      { biome: "mountains", kind: "stone", x: -10, z: -40, hp: 4 },
      { biome: "mountains", kind: "coal",  x: -4,  z: -43, hp: 4 },
      { biome: "mountains", kind: "coal",  x: 11,  z: -36, hp: 4 },
      { biome: "mountains", kind: "iron",  x: 4,   z: -44, hp: 5 },
      { biome: "mountains", kind: "iron",  x: -11, z: -34, hp: 5 },
      { biome: "mountains", kind: "iron",  x: 10,  z: -24, hp: 5 },
      { biome: "mountains", kind: "wood",  x: -6,  z: -22, hp: 3 },

      { biome: "desert", kind: "copper", x: 26, z: -9,  hp: 5 },
      { biome: "desert", kind: "copper", x: 42, z: -6,  hp: 5 },
      { biome: "desert", kind: "copper", x: 25, z: 9,   hp: 5 },
      { biome: "desert", kind: "gold",   x: 43, z: 8,   hp: 7 },
      { biome: "desert", kind: "gold",   x: 38, z: -11, hp: 7 },
      { biome: "desert", kind: "stone",  x: 30, z: 12,  hp: 4 },

      { biome: "frozen", kind: "crystal", x: -42, z: -8, hp: 8 },
      { biome: "frozen", kind: "crystal", x: -26, z: -10, hp: 8 },
      { biome: "frozen", kind: "crystal", x: -43, z: 9,  hp: 8 },
      { biome: "frozen", kind: "iron",    x: -25, z: 10, hp: 6 },
      { biome: "frozen", kind: "wood",    x: -36, z: -11, hp: 4 },
      { biome: "frozen", kind: "wood",    x: -44, z: 0,  hp: 4 },

      { biome: "volcanic", kind: "coal", x: -10, z: 26, hp: 6 },
      { biome: "volcanic", kind: "gold", x: 11,  z: 25, hp: 8 },
      { biome: "volcanic", kind: "gold", x: -11, z: 42, hp: 8 },
      { biome: "volcanic", kind: "rare", x: 0,   z: 44, hp: 10 },
      { biome: "volcanic", kind: "rare", x: 11,  z: 43, hp: 10 },

      { biome: "jungle", kind: "wood",    x: 24, z: -42, hp: 5 },
      { biome: "jungle", kind: "wood",    x: 44, z: -26, hp: 5 },
      { biome: "jungle", kind: "crystal", x: 24, z: -24, hp: 9 },
      { biome: "jungle", kind: "rare",    x: 43, z: -42, hp: 12 },
      { biome: "jungle", kind: "rare",    x: 34, z: -44, hp: 12 },

      { biome: "crystal_desert", kind: "quartz",  x: 59, z: -9,  hp: 10 },
      { biome: "crystal_desert", kind: "quartz",  x: 60, z: 9,   hp: 10 },
      { biome: "crystal_desert", kind: "crystal", x: 76, z: 8,   hp: 9 },
      { biome: "crystal_desert", kind: "gold",    x: 75, z: -11, hp: 8 },
      { biome: "crystal_desert", kind: "quartz",  x: 64, z: -11, hp: 10 },

      { biome: "sky_islands", kind: "aether",  x: -9,  z: -76, hp: 12 },
      { biome: "sky_islands", kind: "aether",  x: 9,   z: -76, hp: 12 },
      { biome: "sky_islands", kind: "wood",    x: -10, z: -60, hp: 6 },
      { biome: "sky_islands", kind: "crystal", x: 11,  z: -72, hp: 10 },
      { biome: "sky_islands", kind: "aether",  x: -11, z: -71, hp: 12 },

      { biome: "toxic_swamp", kind: "venom", x: -43, z: 25, hp: 13 },
      { biome: "toxic_swamp", kind: "venom", x: -43, z: 42, hp: 13 },
      { biome: "toxic_swamp", kind: "wood",  x: -25, z: 25, hp: 7 },
      { biome: "toxic_swamp", kind: "venom", x: -26, z: 43, hp: 13 },
      { biome: "toxic_swamp", kind: "coal",  x: -39, z: 24, hp: 9 },

      { biome: "frozen_mountains", kind: "frostite", x: -77, z: -9,  hp: 15 },
      { biome: "frozen_mountains", kind: "frostite", x: -59, z: -10, hp: 15 },
      { biome: "frozen_mountains", kind: "frostite", x: -77, z: 10,  hp: 15 },
      { biome: "frozen_mountains", kind: "iron",     x: -58, z: 10,  hp: 10 },
      { biome: "frozen_mountains", kind: "crystal",  x: -72, z: -12, hp: 12 },

      { biome: "volcanic_lands", kind: "obsidian", x: -10, z: 60, hp: 17 },
      { biome: "volcanic_lands", kind: "obsidian", x: 10,  z: 76, hp: 17 },
      { biome: "volcanic_lands", kind: "obsidian", x: -10, z: 76, hp: 17 },
      { biome: "volcanic_lands", kind: "gold",     x: 11,  z: 58, hp: 11 },
      { biome: "volcanic_lands", kind: "rare",     x: -5,  z: 79, hp: 15 },

      { biome: "ancient_machine_world", kind: "titan_alloy", x: 25, z: 24, hp: 20 },
      { biome: "ancient_machine_world", kind: "titan_alloy", x: 43, z: 43, hp: 20 },
      { biome: "ancient_machine_world", kind: "titan_alloy", x: 25, z: 43, hp: 20 },
      { biome: "ancient_machine_world", kind: "rare",        x: 39, z: 24, hp: 16 },
      { biome: "ancient_machine_world", kind: "iron",        x: 29, z: 45, hp: 12 }
    ];

    specs.forEach((sp, i) => {
      const built = createNodeMesh(sp.kind, sp.biome, i);
      built.grp.position.set(sp.x, 0, sp.z);
      built.grp.rotation.y = (i * 1.37) % (Math.PI * 2);
      getBiomeChunk(sp.biome).add(built.grp);
      const nodeObj = {
        id: i,
        biome: sp.biome,
        kind: sp.kind,
        x: sp.x,
        z: sp.z,
        maxHp: sp.hp,
        hp: sp.hp,
        respawnTimer: 0,
        bounceTimer: 0,
        fallTimer: 0,
        fallTotal: 0.48,
        fallDirZ: 1,
        mesh: built.grp,
        visualRoot: built.visualRoot,
        crackMesh: built.crackMesh
      };
      resourceNodes.push(nodeObj);
      if (!nodesByBiome[sp.biome]) nodesByBiome[sp.biome] = [];
      nodesByBiome[sp.biome].push(nodeObj);
    });
  }
  populateBiomeNodes();

  // =========================================================================
  // 9B. EXPLORATION LANDMARKS (12), SECRET CAVES (12) & HAZARDS (6)
  // =========================================================================
  const worldObstacles = [];
  function addObstacle(x, z, r) {
    worldObstacles.push({ x, z, r });
  }
  BUILDING_DEFS.forEach((d) => addObstacle(d.x, d.z, 1.95));
  MACHINE_DEFS.forEach((m) => addObstacle(m.x, m.z, 2.05));
  addObstacle(SELL_ZONE.x - 1.95, SELL_ZONE.z + 1.95, 1.9);

  const worldLandmarks = [];
  const LANDMARK_SPECS = [
    { biome: "forest",                x: -11.5, z: 11.2,  kind: "totem",     col: 0xf59e0b },
    { biome: "mountains",             x: 11.5,  z: -42.5, kind: "beacon",    col: 0x94a3b8 },
    { biome: "desert",                x: 43.5,  z: 11.5,  kind: "obelisk",   col: 0xfbbf24 },
    { biome: "frozen",                x: -43.5, z: -12.0, kind: "ice_arch",  col: 0x38bdf8 },
    { biome: "volcanic",              x: -11.5, z: 25.0,  kind: "magma_sp",  col: 0xef4444 },
    { biome: "jungle",                x: 43.5,  z: -34.0, kind: "temple",    col: 0x14b8a6 },
    { biome: "crystal_desert",        x: 78.0,  z: 11.5,  kind: "cathedral", col: 0xf472b6 },
    { biome: "sky_islands",           x: 0.0,   z: -78.5, kind: "sky_altar", col: 0x7dd3fc },
    { biome: "toxic_swamp",           x: -44.0, z: 34.0,  kind: "giant_sh",  col: 0x84cc16 },
    { biome: "frozen_mountains",      x: -78.5, z: 0.0,   kind: "ice_crown", col: 0x67e8f9 },
    { biome: "volcanic_lands",        x: 0.0,   z: 78.5,  kind: "caldera",   col: 0xfb7185 },
    { biome: "ancient_machine_world", x: 34.0,  z: 44.5,  kind: "monolith",  col: 0x38bdf8 }
  ];

  LANDMARK_SPECS.forEach((lm, idx) => {
    const g = new THREE.Group();
    g.position.set(lm.x, 0, lm.z);
    addContactShadow(g, 1.85);
    const plinth = new THREE.Mesh(new THREE.CylinderGeometry(1.35, 1.55, 0.4, 8), pbrMat(0x475569, { map: texRock }));
    plinth.position.y = 0.2;
    plinth.castShadow = true;
    g.add(plinth);
    const body = new THREE.Mesh(new THREE.CylinderGeometry(0.45, 0.85, 3.2, 6), pbrMat(lm.col, { emissive: lm.col, emissiveIntensity: 0.25, roughness: 0.32 }));
    body.position.y = 1.9;
    body.castShadow = true;
    g.add(body);
    const orb = new THREE.Mesh(octaGeo, pbrMat(lm.col, { emissive: lm.col, emissiveIntensity: 0.9, roughness: 0.15 }));
    orb.scale.setScalar(0.55);
    orb.position.y = 4.05;
    g.add(orb);

    let baseOrbY = 4.05;
    if (lm.biome !== "forest" && window.BiomeEnvBuilder) {
      const bObj = BIOMES.find((b) => b.id === lm.biome) || { id: lm.biome, accent: lm.col };
      baseOrbY = window.BiomeEnvBuilder.enhanceBiomeLandmark(
        THREE, bObj, g, orb, indMats, window.BiomeEnvBuilder.getAnimList(lm.biome)
      );
    }

    getBiomeChunk(lm.biome).add(g);
    addObstacle(lm.x, lm.z, 1.65);
    worldLandmarks.push({ id: lm.biome + "_lm", group: g, orb, baseOrbY, biome: lm.biome, x: lm.x, z: lm.z });
  });

  // 12 Hidden Underground Cave Entrances with Discovery Rewards
  const secretCaves = [];
  const CAVE_SPECS = [
    { id: "cave_forest",     biome: "forest",                x: 13.0,  z: -12.8, drop: "stone",       coins: 120,    xp: 25 },
    { id: "cave_mountains",  biome: "mountains",             x: -12.8, z: -45.5, drop: "iron",        coins: 280,    xp: 45 },
    { id: "cave_desert",     biome: "desert",                x: 45.2,  z: -12.5, drop: "gold",        coins: 650,    xp: 80 },
    { id: "cave_frozen",     biome: "frozen",                x: -45.2, z: 12.5,  drop: "crystal",     coins: 1400,   xp: 120 },
    { id: "cave_volcanic",   biome: "volcanic",              x: -12.8, z: 45.2,  drop: "rare",        coins: 3200,   xp: 180 },
    { id: "cave_jungle",     biome: "jungle",                x: 45.2,  z: -45.2, drop: "rare",        coins: 6800,   xp: 260 },
    { id: "cave_crystal_d",  biome: "crystal_desert",        x: 56.5,  z: 12.5,  drop: "quartz",      coins: 12500,  xp: 360 },
    { id: "cave_sky",        biome: "sky_islands",           x: 12.5,  z: -80.0, drop: "aether",      coins: 22000,  xp: 480 },
    { id: "cave_swamp",      biome: "toxic_swamp",           x: -46.0, z: 45.5,  drop: "venom",       coins: 38000,  xp: 620 },
    { id: "cave_frozen_m",   biome: "frozen_mountains",      x: -80.0, z: -12.5, drop: "frostite",    coins: 65000,  xp: 820 },
    { id: "cave_volcanic_l", biome: "volcanic_lands",        x: 12.5,  z: 80.0,  drop: "obsidian",    coins: 110000, xp: 1050 },
    { id: "cave_machine",    biome: "ancient_machine_world", x: 22.0,  z: 46.0,  drop: "titan_alloy", coins: 195000, xp: 1400 }
  ];

  CAVE_SPECS.forEach((cs) => {
    const cg = new THREE.Group();
    cg.position.set(cs.x, 0, cs.z);
    const archRock = new THREE.Mesh(organicRockGeoA, pbrMat(0x334155, { map: texRock, flatShading: true, roughness: 0.92 }));
    archRock.scale.set(1.5, 1.15, 1.35);
    archRock.position.y = 0.72;
    archRock.castShadow = true;
    cg.add(archRock);
    const portalDark = new THREE.Mesh(new THREE.CircleGeometry(0.62, 12), basicMat(0x020617, { side: THREE.DoubleSide }));
    portalDark.position.set(0, 0.56, 0.95);
    cg.add(portalDark);
    const chest = new THREE.Mesh(makeRoundedBoxGeo(0.58, 0.42, 0.42, 0.05), metalMat(0xfbbf24, { emissive: 0xd97706, emissiveIntensity: 0.35 }));
    chest.position.set(0, 0.25, 1.25);
    cg.add(chest);
    const beacon = new THREE.Mesh(octaGeo, pbrMat(0x22d3ee, { emissive: 0x22d3ee, emissiveIntensity: 1.0 }));
    beacon.scale.setScalar(0.24);
    beacon.position.set(0, 1.85, 0.4);
    cg.add(beacon);
    getBiomeChunk(cs.biome).add(cg);
    secretCaves.push({ def: cs, group: cg, chest, beacon });
  });

  // 6 Environmental Challenges / Dynamic Features
  const envHazards = [
    { id: "ice_patch_1",   biome: "frozen",                kind: "ice_boost",  x: -34, z: -4,  r: 3.1 },
    { id: "ice_patch_2",   biome: "frozen_mountains",      kind: "ice_boost",  x: -68, z: -4,  r: 3.1 },
    { id: "sky_updraft",   biome: "sky_islands",           kind: "wind_boost", x: 0,   z: -68, r: 3.2 },
    { id: "swamp_spore",   biome: "toxic_swamp",           kind: "spore_vent", x: -35, z: 35,  r: 2.3, phase: 0.4 },
    { id: "volc_geyser_1", biome: "volcanic",              kind: "geyser",     x: -6,  z: 36,  r: 2.2, phase: 0.0 },
    { id: "volc_geyser_2", biome: "volcanic_lands",        kind: "geyser",     x: -5,  z: 67,  r: 2.2, phase: 1.8 }
  ];
  envHazards.forEach((hz) => {
    const hg = new THREE.Group();
    hg.position.set(hz.x, 0.04, hz.z);
    const col = hz.kind === "ice_boost" ? 0x67e8f9 : (hz.kind === "wind_boost" ? 0xbae6fd : (hz.kind === "spore_vent" ? 0x84cc16 : 0xf97316));
    const disc = new THREE.Mesh(
      new THREE.RingGeometry(hz.r * 0.55, hz.r * 0.95, 24),
      basicMat(col, { side: THREE.DoubleSide, transparent: true, opacity: 0.65 })
    );
    disc.rotation.x = -Math.PI / 2;
    hg.add(disc);
    const core = new THREE.Mesh(new THREE.CylinderGeometry(hz.r * 0.42, hz.r * 0.55, 0.14, 14), pbrMat(col, { emissive: col, emissiveIntensity: 0.55, roughness: 0.25 }));
    hg.add(core);
    getBiomeChunk(hz.biome).add(hg);
    hz.mesh = hg;
    hz.ring = disc;
    hz.core = core;
    hz.cooldown = 0;
  });

  // =========================================================================
  // 10. STYLIZED SPIKY-HAIRED LUMBERJACK HERO + 8-TIER EXTRUDED TOOLS
  // =========================================================================
  const playerGroup = new THREE.Group();
  playerGroup.position.set(0, 0, 2);
  scene.add(playerGroup);
  addContactShadow(playerGroup, 0.58);

  const playerBodyPivot = new THREE.Group();
  playerBodyPivot.scale.setScalar(0.68);
  playerGroup.add(playerBodyPivot);

  // Buffalo-Plaid Flannel Shirt Torso + Collar Lapels + Leather Belt
  const torsoMat = pbrMat(0xffffff, { map: texPlaid, roughness: 0.68, metalness: 0.04 });
  const torsoGeo = makeRoundedBoxGeo(0.8, 0.86, 0.48, 0.12);
  const torso = new THREE.Mesh(torsoGeo, torsoMat);
  torso.position.y = 1.08;
  torso.castShadow = true;
  playerBodyPivot.add(torso);

  // Shirt Collar Lapels + Warm Skin Neck
  const neckMesh = new THREE.Mesh(new THREE.CylinderGeometry(0.14, 0.17, 0.22, 12), pbrMat(0xffd275, { roughness: 0.5 }));
  neckMesh.position.y = 1.54;
  playerBodyPivot.add(neckMesh);
  [-0.14, 0.14].forEach((lx, li) => {
    const lapel = new THREE.Mesh(makeRoundedBoxGeo(0.2, 0.08, 0.26, 0.025), torsoMat);
    lapel.position.set(lx, 1.49, 0.08);
    lapel.rotation.z = li === 0 ? 0.22 : -0.22;
    lapel.rotation.y = li === 0 ? 0.35 : -0.35;
    playerBodyPivot.add(lapel);
  });

  // Dark Brown Leather Belt + Golden Square Frame Buckle
  const belt = new THREE.Mesh(makeRoundedBoxGeo(0.84, 0.15, 0.52, 0.04), pbrMat(0x452212, { roughness: 0.55 }));
  belt.position.y = 0.71;
  playerBodyPivot.add(belt);
  const buckle = new THREE.Mesh(makeRoundedBoxGeo(0.2, 0.16, 0.08, 0.025), metalMat(0xf59e0b, { roughness: 0.24 }));
  buckle.position.set(0, 0.71, 0.25);
  playerBodyPivot.add(buckle);

  // Smooth Rounded Cylindrical Toy-Hero Head, Ears, Glossy Eyes, Cheek Warmth & Smile
  const skinMat = pbrMat(0xffd275, { roughness: 0.5, metalness: 0.02 });
  const headPts = [
    new THREE.Vector2(0.01, -0.31),
    new THREE.Vector2(0.25, -0.3),
    new THREE.Vector2(0.33, -0.22),
    new THREE.Vector2(0.33, 0.22),
    new THREE.Vector2(0.25, 0.31),
    new THREE.Vector2(0.01, 0.32)
  ];
  const head = new THREE.Mesh(new THREE.LatheGeometry(headPts, 20), skinMat);
  head.position.y = 1.9;
  head.castShadow = true;
  playerBodyPivot.add(head);

  // Rounded Side Ears
  [-0.32, 0.32].forEach((ex) => {
    const ear = new THREE.Mesh(sphereGeo, skinMat);
    ear.scale.set(0.065, 0.095, 0.075);
    ear.position.set(ex, 1.86, 0.0);
    playerBodyPivot.add(ear);
  });

  // Expressive Dark Pill Eyes + Glossy White Catchlights + Warm Cheeks + Happy Smile
  const eyeMat = pbrMat(0x0f172a, { roughness: 0.16 });
  const glintMat = basicMat(0xffffff);
  [-0.105, 0.105].forEach((ex) => {
    const eye = new THREE.Mesh(makeCapsuleGeo(0.038, 0.085, 10), eyeMat);
    eye.position.set(ex, 1.93, 0.31);
    playerBodyPivot.add(eye);
    const glint = new THREE.Mesh(sphereGeo, glintMat);
    glint.scale.setScalar(0.014);
    glint.position.set(ex + 0.015, 1.97, 0.346);
    playerBodyPivot.add(glint);
    const cheek = new THREE.Mesh(sphereGeo, pbrMat(0xfca5a5, { roughness: 0.7 }));
    cheek.scale.set(0.055, 0.032, 0.025);
    cheek.position.set(ex * 1.65, 1.82, 0.295);
    playerBodyPivot.add(cheek);
  });
  const smileGeo = new THREE.TorusGeometry(0.1, 0.024, 8, 16, Math.PI * 0.85);
  const smile = new THREE.Mesh(smileGeo, eyeMat);
  smile.rotation.z = Math.PI * 1.075;
  smile.position.set(0, 1.79, 0.31);
  playerBodyPivot.add(smile);

  // Iconic Spiky Vibrant Orange Hair Crown
  const hairMat = pbrMat(0xf9690e, { roughness: 0.42, metalness: 0.04 });
  const hairGroup = new THREE.Group();
  hairGroup.position.set(0, 2.1, -0.02);
  playerBodyPivot.add(hairGroup);

  const hairCap = new THREE.Mesh(sphereGeo, hairMat);
  hairCap.scale.set(0.35, 0.17, 0.35);
  hairCap.position.y = 0.04;
  hairCap.castShadow = true;
  hairGroup.add(hairCap);

  const backHair = new THREE.Mesh(makeRoundedBoxGeo(0.64, 0.34, 0.24, 0.08), hairMat);
  backHair.position.set(0, -0.08, -0.2);
  hairGroup.add(backHair);

  // 9 Swept Flame-Like Spikes matching reference silhouette
  const hairSpikes = [
    [-0.18, 0.19, 0.14, 0.14, 0.38, 0.35, -0.2],
    [0.0,   0.26, 0.16, 0.15, 0.46, 0.0,  -0.28],
    [0.18,  0.21, 0.14, 0.14, 0.40, -0.35, -0.2],
    [-0.28, 0.11, 0.02, 0.12, 0.32, 0.65, -0.15],
    [0.28,  0.11, 0.02, 0.12, 0.32, -0.65, -0.15],
    [-0.11, 0.23, -0.08, 0.14, 0.42, 0.25, -0.45],
    [0.12,  0.23, -0.08, 0.14, 0.42, -0.25, -0.45],
    [-0.24, 0.02, 0.18, 0.09, 0.22, 0.85, 0.12],
    [0.24,  0.02, 0.18, 0.09, 0.22, -0.85, 0.12]
  ];
  hairSpikes.forEach(([hx, hy, hz, r, h, rz, rx]) => {
    const spike = new THREE.Mesh(new THREE.ConeGeometry(r, h, 8), hairMat);
    spike.position.set(hx, hy, hz);
    spike.rotation.z = rz;
    spike.rotation.x = rx;
    spike.castShadow = true;
    hairGroup.add(spike);
  });

  const helmetCrest = new THREE.Mesh(octaGeo, metalMat(0x22d3ee, { emissive: 0x06b6d4, emissiveIntensity: 0.7 }));
  helmetCrest.scale.set(0.14, 0.2, 0.14);
  helmetCrest.position.set(0, 2.54, 0.08);
  helmetCrest.visible = false;
  playerBodyPivot.add(helmetCrest);

  // Armor Shoulder Pauldrons (unlocked at Steel+ tier)
  const pauldronMat = metalMat(0x64748b);
  const pauldronL = new THREE.Mesh(makeOrganicGeo(0.22, 1, 0.12, 1), pauldronMat);
  pauldronL.scale.set(1.1, 0.7, 1.12);
  pauldronL.position.set(-0.5, 1.46, 0);
  pauldronL.visible = false;
  playerBodyPivot.add(pauldronL);
  const pauldronR = pauldronL.clone();
  pauldronR.position.x = 0.5;
  playerBodyPivot.add(pauldronR);

  // Chainsaw/Power Pack Harness
  const powerPackMesh = new THREE.Mesh(makeRoundedBoxGeo(0.26, 0.34, 0.2, 0.04), metalMat(0xf97316));
  powerPackMesh.position.set(-0.34, 0.95, -0.2);
  powerPackMesh.visible = false;
  playerBodyPivot.add(powerPackMesh);

  const playerAuraRing = new THREE.Mesh(
    new THREE.RingGeometry(0.62, 0.78, 28),
    basicMat(0xe879f9, { side: THREE.DoubleSide, transparent: true, opacity: 0.65 })
  );
  playerAuraRing.rotation.x = -Math.PI / 2;
  playerAuraRing.position.y = 0.05;
  playerAuraRing.visible = false;
  playerGroup.add(playerAuraRing);

  // Charcoal Trousers + Chunky Tan-Orange Work Boots with Laces
  const legMat = pbrMat(0x2e2b29, { roughness: 0.72 });
  const bootMat = pbrMat(0xf59e0b, { roughness: 0.48, metalness: 0.04 });
  const soleMat = pbrMat(0x452212, { roughness: 0.82 });
  function makeLeg(x) {
    const lg = new THREE.Group();
    lg.position.set(x, 0.66, 0);
    const limb = new THREE.Mesh(makeCapsuleGeo(0.125, 0.36, 10), legMat);
    limb.position.y = -0.22;
    limb.castShadow = true;
    lg.add(limb);

    const bootBody = new THREE.Mesh(makeRoundedBoxGeo(0.27, 0.21, 0.4, 0.075), bootMat);
    bootBody.position.set(0, -0.54, 0.06);
    bootBody.castShadow = true;
    lg.add(bootBody);
    const bootCuff = new THREE.Mesh(new THREE.CylinderGeometry(0.14, 0.15, 0.15, 12), bootMat);
    bootCuff.position.set(0, -0.42, 0);
    lg.add(bootCuff);
    const sole = new THREE.Mesh(makeRoundedBoxGeo(0.29, 0.06, 0.42, 0.02), soleMat);
    sole.position.set(0, -0.64, 0.06);
    lg.add(sole);
    const lace = new THREE.Mesh(makeRoundedBoxGeo(0.11, 0.09, 0.05, 0.01), soleMat);
    lace.position.set(0, -0.44, 0.15);
    lg.add(lace);

    playerBodyPivot.add(lg);
    return lg;
  }
  const legL = makeLeg(-0.19);
  const legR = makeLeg(0.19);

  // Articulated Arms with Flannel Sleeves, Brown Leather Wristbands & Rounded Hands
  const wristMat = pbrMat(0x452212, { roughness: 0.6 });
  function buildHeroArm(sideX) {
    const piv = new THREE.Group();
    piv.position.set(sideX, 1.4, 0);
    const sleeve = new THREE.Mesh(makeCapsuleGeo(0.12, 0.16, 10), torsoMat);
    sleeve.position.y = -0.1;
    sleeve.castShadow = true;
    piv.add(sleeve);
    const forearm = new THREE.Mesh(makeCapsuleGeo(0.096, 0.28, 10), skinMat);
    forearm.position.y = -0.3;
    forearm.castShadow = true;
    piv.add(forearm);
    const band = new THREE.Mesh(new THREE.CylinderGeometry(0.108, 0.108, 0.1, 12), wristMat);
    band.position.y = -0.39;
    piv.add(band);
    const hand = new THREE.Mesh(sphereGeo, skinMat);
    hand.scale.set(0.115, 0.115, 0.115);
    hand.position.y = -0.5;
    piv.add(hand);
    playerBodyPivot.add(piv);
    return piv;
  }
  const armL = buildHeroArm(-0.49);
  const armRPivot = buildHeroArm(0.49);

  const toolGroup = new THREE.Group();
  toolGroup.position.set(0, -0.52, 0.14);
  armRPivot.add(toolGroup);

  function buildAxeTierModel(tier) {
    const g = new THREE.Group();
    const haftCol = tier >= 7 ? 0x1e293b : (tier >= 5 ? 0x475569 : 0x78350f);
    const haft = new THREE.Mesh(
      makeCapsuleGeo(0.042, 0.85 + tier * 0.04, 8),
      pbrMat(haftCol, { map: tier < 5 ? texBark : texBrushedMetal, roughness: 0.65 })
    );
    haft.rotation.x = Math.PI / 2;
    haft.position.z = 0.3;
    g.add(haft);

    const headCol = TIER_COLORS[tier - 1];
    const bladeMat = tier >= 6
      ? metalMat(headCol, { emissive: headCol, emissiveIntensity: tier === 8 ? 0.6 : (tier === 7 ? 0.42 : 0.2), roughness: 0.18 })
      : (tier <= 2 ? pbrMat(headCol, { roughness: 0.72 }) : metalMat(headCol, { roughness: 0.3 }));

    // Curved Beveled Extruded Axe Blade Shape
    const bs = new THREE.Shape();
    const bw = 0.22 + tier * 0.025;
    const bh = 0.22 + tier * 0.032;
    bs.moveTo(-0.04, 0.06);
    bs.lineTo(bw, bh);
    bs.quadraticCurveTo(bw + 0.12, 0, bw, -bh * (tier >= 5 ? 0.85 : 0.35));
    bs.lineTo(-0.04, -0.06);
    bs.closePath();

    const bladeGeo = new THREE.ExtrudeGeometry(bs, {
      depth: 0.055,
      bevelEnabled: true,
      bevelThickness: 0.02,
      bevelSize: 0.02,
      bevelSegments: 2
    });
    bladeGeo.rotateY(Math.PI / 2);
    bladeGeo.rotateX(-Math.PI / 2);
    bladeGeo.translate(-0.0275, 0, 0);
    const blade = new THREE.Mesh(bladeGeo, bladeMat);
    blade.position.set(0, -0.04, 0.66 + tier * 0.02);
    blade.castShadow = true;
    g.add(blade);

    if (tier >= 5) {
      const twin = blade.clone();
      twin.rotation.z = Math.PI;
      twin.scale.set(0.85, 0.85, 0.85);
      twin.position.set(0, 0.04, 0.66 + tier * 0.02);
      g.add(twin);
    }
    if (tier === 8) {
      const core = new THREE.Mesh(octaGeo, pbrMat(0xfde047, { emissive: 0xfde047, emissiveIntensity: 1.2 }));
      core.scale.setScalar(0.11);
      core.position.set(0, 0.02, 0.9);
      g.add(core);
    }
    g.visible = false;
    toolGroup.add(g);
    return g;
  }

  function buildPickTierModel(tier) {
    const g = new THREE.Group();
    const haftCol = tier >= 7 ? 0x1e293b : (tier >= 5 ? 0x475569 : 0x78350f);
    const haft = new THREE.Mesh(
      makeCapsuleGeo(0.042, 0.85 + tier * 0.04, 8),
      pbrMat(haftCol, { map: tier < 5 ? texBark : texBrushedMetal })
    );
    haft.rotation.x = Math.PI / 2;
    haft.position.z = 0.3;
    g.add(haft);

    const headCol = TIER_COLORS[tier - 1];
    const headMat = tier >= 6
      ? metalMat(headCol, { emissive: headCol, emissiveIntensity: tier === 8 ? 0.6 : 0.32, roughness: 0.18 })
      : metalMat(headCol, { roughness: tier <= 2 ? 0.72 : 0.32, metalness: tier <= 2 ? 0.15 : 0.85 });

    // Curved Swept Pickaxe Crescent Arc
    const ps = new THREE.Shape();
    const span = 0.38 + tier * 0.032;
    ps.moveTo(-span, -0.09);
    ps.quadraticCurveTo(0, 0.14, span, -0.09);
    ps.quadraticCurveTo(0, 0.02, -span, -0.09);
    const pickGeo = new THREE.ExtrudeGeometry(ps, {
      depth: 0.07,
      bevelEnabled: true,
      bevelThickness: 0.02,
      bevelSize: 0.02,
      bevelSegments: 2
    });
    pickGeo.center();
    pickGeo.rotateZ(Math.PI / 2);
    pickGeo.rotateY(Math.PI / 2);
    const headMesh = new THREE.Mesh(pickGeo, headMat);
    headMesh.position.set(0, 0.0, 0.7 + tier * 0.02);
    headMesh.castShadow = true;
    g.add(headMesh);

    const collar = new THREE.Mesh(makeRoundedBoxGeo(0.16, 0.16, 0.18, 0.03), metalMat(0x1e293b));
    collar.position.set(0, 0.02, 0.7 + tier * 0.02);
    g.add(collar);

    g.visible = false;
    toolGroup.add(g);
    return g;
  }

  const axeTierModels = [1, 2, 3, 4, 5, 6, 7, 8].map(buildAxeTierModel);
  const pickTierModels = [1, 2, 3, 4, 5, 6, 7, 8].map(buildPickTierModel);
  let activeToolKind = "axe";

  function updateActiveToolMesh() {
    const axeIdx = Math.min(Math.max(state.axeLevel, 1), 8) - 1;
    const pickIdx = Math.min(Math.max(state.pickaxeLevel, 1), 8) - 1;
    axeTierModels.forEach((m, i) => { m.visible = (activeToolKind === "axe" && i === axeIdx); });
    pickTierModels.forEach((m, i) => { m.visible = (activeToolKind === "pick" && i === pickIdx); });
  }

  // Sculpted Expedition Backpack + Rolled Bedroll + Side Canisters
  const backpackMat = pbrMat(0x92400e, { roughness: 0.55 });
  const backpackMesh = new THREE.Mesh(makeRoundedBoxGeo(0.56, 0.68, 0.32, 0.08), backpackMat);
  backpackMesh.position.set(0, 1.08, -0.36);
  backpackMesh.castShadow = true;
  playerBodyPivot.add(backpackMesh);

  const bedroll = new THREE.Mesh(new THREE.CylinderGeometry(0.1, 0.1, 0.52, 10), pbrMat(0xfef0c7, { roughness: 0.75 }));
  bedroll.rotation.z = Math.PI / 2;
  bedroll.position.set(0, 1.44, -0.36);
  playerBodyPivot.add(bedroll);

  const bagPodMat = metalMat(0xcbd5e1);
  const bagPodL = new THREE.Mesh(makeCapsuleGeo(0.09, 0.34, 8), bagPodMat);
  bagPodL.position.set(-0.36, 1.06, -0.4);
  bagPodL.visible = false;
  playerBodyPivot.add(bagPodL);
  const bagPodR = bagPodL.clone();
  bagPodR.position.x = 0.36;
  bagPodR.visible = false;
  playerBodyPivot.add(bagPodR);

  const bagCoreMat = pbrMat(0x22d3ee, { emissive: 0x22d3ee, emissiveIntensity: 0.85, roughness: 0.15 });
  const bagCore = new THREE.Mesh(octaGeo, bagCoreMat);
  bagCore.scale.set(0.16, 0.2, 0.16);
  bagCore.position.set(0, 1.08, -0.62);
  bagCore.visible = false;
  playerBodyPivot.add(bagCore);

  const bagStackGroup = new THREE.Group();
  bagStackGroup.position.set(0, 1.56, -0.38);
  playerBodyPivot.add(bagStackGroup);

  function refreshCharacterVisuals() {
    const bagLvl = Math.min(Math.max(state.backpackLevel, 1), 8);
    const bagCol = TIER_COLORS[bagLvl - 1];
    setSrgbHex(backpackMat.color, bagCol);
    const s = 1 + (bagLvl - 1) * 0.08;
    backpackMesh.scale.set(s, s, s);
    backpackMesh.position.z = -0.35 - (bagLvl - 1) * 0.02;
    bagPodL.visible = bagLvl >= 4;
    bagPodR.visible = bagLvl >= 4;
    setSrgbHex(bagPodMat.color, bagLvl >= 7 ? 0x22d3ee : (bagLvl >= 6 ? 0xfbbf24 : 0xcbd5e1));
    bagCore.visible = bagLvl >= 7;
    if (bagLvl === 8) {
      setSrgbHex(bagCoreMat.color, 0xe879f9);
      setSrgbHex(bagCoreMat.emissive, 0xe879f9);
    } else {
      setSrgbHex(bagCoreMat.color, 0x22d3ee);
      setSrgbHex(bagCoreMat.emissive, 0x22d3ee);
    }

    const avgGear = (state.axeLevel + state.pickaxeLevel + state.backpackLevel + state.moveSpeedLevel + state.gatherSpeedLevel) / 5;
    pauldronL.visible = avgGear >= 4.2;
    pauldronR.visible = avgGear >= 4.2;
    helmetCrest.visible = avgGear >= 5.5;
    playerAuraRing.visible = avgGear >= 6.8;
    powerPackMesh.visible = getFleetLevel("chainsaw_drill") > 0;

    if (avgGear >= 7.0) {
      setSrgbHex(torsoMat.color, 0xe9d5ff);
      setSrgbHex(hairMat.color, 0xfde047);
      setSrgbHex(pauldronMat.color, 0xe879f9);
    } else if (avgGear >= 5.8) {
      setSrgbHex(torsoMat.color, 0xbae6fd);
      setSrgbHex(hairMat.color, 0xfb923c);
      setSrgbHex(pauldronMat.color, 0x38bdf8);
    } else if (avgGear >= 4.2) {
      setSrgbHex(torsoMat.color, 0xfef3c7);
      setSrgbHex(hairMat.color, 0xf9690e);
      setSrgbHex(pauldronMat.color, 0xfbbf24);
    } else {
      setSrgbHex(torsoMat.color, 0xffffff);
      setSrgbHex(hairMat.color, 0xf9690e);
    }

    updateActiveToolMesh();

    while (bagStackGroup.children.length > 0) bagStackGroup.remove(bagStackGroup.children[0]);
    const keys = Object.keys(state.inventory).filter((k) => state.inventory[k] > 0);
    let idx = 0;
    for (const k of keys) {
      const count = Math.min(state.inventory[k], 4);
      const col = RESOURCES[k] ? RESOURCES[k].color : 0xffffff;
      for (let c = 0; c < count && idx < 10; c++) {
        const item = createProductCrate(col, k !== "wood");
        item.scale.setScalar(0.55);
        item.position.set((idx % 2 === 0 ? -0.12 : 0.12), Math.floor(idx / 2) * 0.14, 0);
        bagStackGroup.add(item);
        idx++;
      }
    }

    const activeV = getActiveVehicle();
    const showV = !isMenuMode && Boolean(activeV);
    playerVehicleRoot.visible = showV;
    if (showV) {
      for (const vid in vehicleMeshes) {
        vehicleMeshes[vid].visible = (vid === activeV.id);
      }
      playerBodyPivot.position.y = 0.40;
      legL.rotation.x = -0.95;
      legR.rotation.x = -0.95;
      backpackMesh.visible = false;
      bedroll.visible = false;
      bagStackGroup.visible = false;
    } else {
      playerBodyPivot.position.y = 0;
      backpackMesh.visible = true;
      bedroll.visible = true;
      bagStackGroup.visible = true;
    }
  }

  // =========================================================================
  // 10B. USABLE DRIVABLE MINING VEHICLES (ROVER, HEAVY HAULER, EXPLORER)
  // =========================================================================
  const playerVehicleRoot = new THREE.Group();
  playerVehicleRoot.scale.setScalar(0.76);
  playerVehicleRoot.visible = false;
  playerGroup.add(playerVehicleRoot);
  const vehicleMeshes = {};
  const vehicleWheels = {};
  let explorerDish = null;
  let explorerRadarRing = null;

  (function buildDrivableVehicles() {
    // 1. Small Mining Rover
    const rover = new THREE.Group();
    const rWheels = [];
    const rFrame = new THREE.Mesh(makeRoundedBoxGeo(1.45, 0.42, 2.05, 0.08), pbrMat(0xf59e0b, { roughness: 0.35, metalness: 0.35 }));
    rFrame.position.y = 0.42;
    rFrame.castShadow = true;
    rover.add(rFrame);
    const rDrill = new THREE.Mesh(new THREE.ConeGeometry(0.28, 0.65, 8), indMats.chrome);
    rDrill.rotation.x = Math.PI / 2;
    rDrill.position.set(0, 0.44, 1.3);
    rover.add(rDrill);
    [[-0.78, -0.68], [0.78, -0.68], [-0.78, 0.68], [0.78, 0.68]].forEach(([wx, wz]) => {
      const w = createWheelMesh(0.32, 0.22);
      w.position.set(wx, 0.32, wz);
      rover.add(w);
      rWheels.push(w);
    });
    playerVehicleRoot.add(rover);
    vehicleMeshes.mining_rover = rover;
    vehicleWheels.mining_rover = rWheels;

    // 2. Heavy Mining Transport
    const hauler = new THREE.Group();
    const hWheels = [];
    const hChassis = new THREE.Mesh(makeRoundedBoxGeo(1.75, 0.5, 2.55, 0.08), pbrMat(0x0284c7, { map: texPanelMetal, roughness: 0.42, metalness: 0.4 }));
    hChassis.position.y = 0.48;
    hChassis.castShadow = true;
    hauler.add(hChassis);
    const hBed = new THREE.Mesh(makeRoundedBoxGeo(1.55, 0.45, 1.15, 0.05), indMats.steelDark);
    hBed.position.set(0, 0.88, -0.68);
    hauler.add(hBed);
    [[-0.95, -0.95], [0.95, -0.95], [-0.95, 0], [0.95, 0], [-0.95, 0.95], [0.95, 0.95]].forEach(([wx, wz]) => {
      const w = createWheelMesh(0.35, 0.24);
      w.position.set(wx, 0.35, wz);
      hauler.add(w);
      hWheels.push(w);
    });
    playerVehicleRoot.add(hauler);
    vehicleMeshes.heavy_hauler = hauler;
    vehicleWheels.heavy_hauler = hWheels;

    // 3. Exploration Vehicle (Scout Buggy + Radar)
    const exp = new THREE.Group();
    const eWheels = [];
    const eBody = new THREE.Mesh(makeRoundedBoxGeo(1.38, 0.38, 2.2, 0.1), pbrMat(0x10b981, { roughness: 0.32, metalness: 0.28 }));
    eBody.position.y = 0.42;
    eBody.castShadow = true;
    exp.add(eBody);
    const mast = new THREE.Mesh(new THREE.CylinderGeometry(0.04, 0.05, 1.35, 8), indMats.chrome);
    mast.position.set(-0.48, 1.18, -0.82);
    exp.add(mast);
    explorerDish = new THREE.Mesh(new THREE.ConeGeometry(0.32, 0.22, 10), pbrMat(0x22d3ee, { emissive: 0x06b6d4, emissiveIntensity: 0.65 }));
    explorerDish.rotation.x = Math.PI * 0.35;
    explorerDish.position.set(-0.48, 1.9, -0.82);
    exp.add(explorerDish);
    explorerRadarRing = new THREE.Mesh(
      new THREE.RingGeometry(2.6, 2.85, 32),
      basicMat(0x22d3ee, { side: THREE.DoubleSide, transparent: true, opacity: 0.45 })
    );
    explorerRadarRing.rotation.x = -Math.PI / 2;
    explorerRadarRing.position.y = 0.06;
    exp.add(explorerRadarRing);
    [[-0.78, -0.78], [0.78, -0.78], [-0.78, 0.78], [0.78, 0.78]].forEach(([wx, wz]) => {
      const w = createWheelMesh(0.34, 0.22);
      w.position.set(wx, 0.34, wz);
      exp.add(w);
      eWheels.push(w);
    });
    playerVehicleRoot.add(exp);
    vehicleMeshes.explorer_buggy = exp;
    vehicleWheels.explorer_buggy = eWheels;
  })();

  // =========================================================================
  // 11. POOLED PARTICLES, DEBRIS CHUNKS & FLYING RESOURCE ARCS
  // =========================================================================
  const MAX_PARTICLES = 48;
  const particlePool = [];
  let particleCursor = 0;
  for (let pi = 0; pi < MAX_PARTICLES; pi++) {
    const pm = new THREE.Mesh(organicRockGeoA, pbrMat(0xffffff, { roughness: 0.6 }));
    pm.visible = false;
    scene.add(pm);
    particlePool.push({
      mesh: pm, active: false, vx: 0, vy: 0, vz: 0, rotSpd: 0, baseScale: 0.16, age: 0, life: 0.62
    });
  }

  function spawnBurst(x, y, z, colorHex, count, sizeScale) {
    const scBase = sizeScale || 0.16;
    const total = count || 6;
    for (let i = 0; i < total; i++) {
      let slot = null;
      for (let tries = 0; tries < MAX_PARTICLES; tries++) {
        const candidate = particlePool[(particleCursor + tries) % MAX_PARTICLES];
        if (!candidate.active) {
          slot = candidate;
          particleCursor = (particleCursor + tries + 1) % MAX_PARTICLES;
          break;
        }
      }
      if (!slot) {
        slot = particlePool[particleCursor];
        particleCursor = (particleCursor + 1) % MAX_PARTICLES;
      }
      setSrgbHex(slot.mesh.material.color, colorHex);
      slot.mesh.scale.setScalar(scBase);
      slot.mesh.position.set(x, y, z);
      slot.mesh.visible = true;
      const ang = Math.random() * Math.PI * 2;
      const spd = 2.0 + Math.random() * 3.8;
      slot.active = true;
      slot.vx = Math.cos(ang) * spd;
      slot.vy = 3.2 + Math.random() * 3.5;
      slot.vz = Math.sin(ang) * spd;
      slot.rotSpd = (Math.random() - 0.5) * 12;
      slot.baseScale = scBase;
      slot.age = 0;
      slot.life = 0.62;
    }
  }

  function spawnFlyingDrop(fromPos, toTarget, colorHex) {
    const m = createProductCrate(colorHex, false);
    m.scale.setScalar(0.7);
    m.position.copy(fromPos);
    scene.add(m);
    flyingDrops.push({ mesh: m, start: fromPos.clone(), target: toTarget, t: 0, dur: 0.36 });
  }

  // =========================================================================
  // 11B. HOSTILE INSECT AI (4 TYPES + 6-STATE FSM) & 4 WORLD BOSSES
  // =========================================================================
  const INSECT_STATES = {
    IDLE: "IDLE",
    DETECT_PLAYER: "DETECT_PLAYER",
    WARNING: "WARNING",
    CHASE: "CHASE",
    ATTACK: "ATTACK",
    RETURN_TO_PATROL: "RETURN_TO_PATROL"
  };

  const INSECT_TYPES = {
    beetle:      { name: "Giant Beetle",   maxHp: 36, speed: 2.5, dmg: 12, detectR: 7.5, attackR: 2.0, cooldown: 1.5, coins: 45,  xp: 18, col: 0x166534, drop: "stone",   hoverY: 0 },
    spider:      { name: "Forest Spider",  maxHp: 24, speed: 4.5, dmg: 9,  detectR: 8.5, attackR: 1.8, cooldown: 1.0, coins: 40,  xp: 16, col: 0x581c87, drop: "coal",    hoverY: 0 },
    wasp:        { name: "Flying Wasp",    maxHp: 22, speed: 4.9, dmg: 10, detectR: 9.0, attackR: 2.1, cooldown: 1.15, coins: 55, xp: 20, col: 0xeab308, drop: "copper",  hoverY: 1.15 },
    crystal_bug: { name: "Crystal Bug",    maxHp: 44, speed: 3.1, dmg: 14, detectR: 7.8, attackR: 2.0, cooldown: 1.35, coins: 95, xp: 32, col: 0x22d3ee, drop: "crystal", hoverY: 0 }
  };

  function createInsectMesh(type, isBoss, accentHex) {
    const grp = new THREE.Group();
    const sc = isBoss ? 2.15 : 1.0;
    addContactShadow(grp, 0.9 * sc);

    const bodyPivot = new THREE.Group();
    bodyPivot.scale.setScalar(sc);
    grp.add(bodyPivot);

    const cfg = INSECT_TYPES[type] || INSECT_TYPES.beetle;
    const baseCol = accentHex || cfg.col;
    const shellMat = metalMat(baseCol, {
      emissive: isBoss ? baseCol : 0x000000,
      emissiveIntensity: isBoss ? 0.25 : 0,
      roughness: type === "crystal_bug" ? 0.18 : 0.42
    });
    const darkMat = pbrMat(0x0f172a, { roughness: 0.7 });

    const abdomen = new THREE.Mesh(sphereGeo, shellMat);
    abdomen.scale.set(0.46, 0.34, 0.62);
    abdomen.position.set(0, 0.42 + cfg.hoverY, -0.2);
    abdomen.castShadow = true;
    bodyPivot.add(abdomen);

    const head = new THREE.Mesh(sphereGeo, darkMat);
    head.scale.set(0.3, 0.26, 0.3);
    head.position.set(0, 0.42 + cfg.hoverY, 0.38);
    bodyPivot.add(head);

    [-0.12, 0.12].forEach((ex) => {
      const eye = new THREE.Mesh(sphereGeo, pbrMat(0xef4444, { emissive: 0xef4444, emissiveIntensity: 0.9 }));
      eye.scale.setScalar(0.065);
      eye.position.set(ex, 0.47 + cfg.hoverY, 0.62);
      bodyPivot.add(eye);
    });

    if (type === "beetle") {
      [-0.14, 0.14].forEach((hx, hi) => {
        const horn = new THREE.Mesh(new THREE.ConeGeometry(0.07, 0.36, 6), indMats.brass);
        horn.rotation.x = Math.PI * 0.42;
        horn.rotation.z = hi === 0 ? 0.2 : -0.2;
        horn.position.set(hx, 0.44, 0.7);
        bodyPivot.add(horn);
      });
    } else if (type === "crystal_bug") {
      for (let ci = 0; ci < 3; ci++) {
        const sp = new THREE.Mesh(crystalPrismGeo, pbrMat(0xf472b6, { emissive: 0x22d3ee, emissiveIntensity: 0.65 }));
        sp.scale.set(0.32, 0.45, 0.32);
        sp.position.set((ci - 1) * 0.22, 0.78, -0.2);
        bodyPivot.add(sp);
      }
    }

    let wingL = null;
    let wingR = null;
    if (type === "wasp") {
      const wingMat = pbrMat(0xe0f2fe, { transparent: true, opacity: 0.62, roughness: 0.15 });
      wingL = new THREE.Mesh(makeRoundedBoxGeo(0.68, 0.03, 0.28, 0.01), wingMat);
      wingL.position.set(-0.46, 0.68 + cfg.hoverY, -0.05);
      bodyPivot.add(wingL);
      wingR = new THREE.Mesh(makeRoundedBoxGeo(0.68, 0.03, 0.28, 0.01), wingMat);
      wingR.position.set(0.46, 0.68 + cfg.hoverY, -0.05);
      bodyPivot.add(wingR);
    }

    const legs = [];
    const legPairs = type === "spider" ? 4 : 3;
    for (let li = 0; li < legPairs; li++) {
      const lz = 0.22 - li * 0.24;
      [-1, 1].forEach((side) => {
        const leg = new THREE.Mesh(makeCapsuleGeo(0.04, 0.46, 6), darkMat);
        leg.position.set(side * 0.48, 0.22 + cfg.hoverY * 0.6, lz);
        leg.rotation.z = side * 0.65;
        bodyPivot.add(leg);
        legs.push(leg);
      });
    }

    // Visual Warning Indicator ('!' Diamond) + Ground Warning Ring
    const warnSign = new THREE.Mesh(octaGeo, basicMat(0xfef08a));
    warnSign.scale.set(0.22 * sc, 0.38 * sc, 0.22 * sc);
    warnSign.position.set(0, (1.45 + cfg.hoverY) * sc, 0);
    warnSign.visible = false;
    grp.add(warnSign);

    const warnRing = new THREE.Mesh(
      new THREE.RingGeometry(cfg.attackR * 0.6 * sc, cfg.attackR * 0.85 * sc, 24),
      basicMat(0xef4444, { side: THREE.DoubleSide, transparent: true, opacity: 0.65 })
    );
    warnRing.rotation.x = -Math.PI / 2;
    warnRing.position.y = 0.06;
    warnRing.visible = false;
    grp.add(warnRing);

    // Overhead Health Bar
    const hpBarGrp = new THREE.Group();
    hpBarGrp.position.set(0, (1.15 + cfg.hoverY) * sc, 0);
    hpBarGrp.visible = isBoss;
    const hpBg = new THREE.Mesh(new THREE.PlaneGeometry(1.1 * sc, 0.14 * sc), basicMat(0x0f172a));
    hpBarGrp.add(hpBg);
    const hpFill = new THREE.Mesh(new THREE.PlaneGeometry(1.02 * sc, 0.09 * sc), basicMat(isBoss ? 0xe879f9 : 0xef4444));
    hpFill.position.z = 0.01;
    hpBarGrp.add(hpFill);
    grp.add(hpBarGrp);

    return { grp, bodyPivot, wingL, wingR, legs, warnSign, warnRing, hpBarGrp, hpFill };
  }

  const enemies = [];
  const HOSTILE_SPECS = [
    { id: "beetle_f1",   biome: "forest",                type: "beetle",      x: -10.2, z: -2.4,  patrolR: 3.0 },
    { id: "spider_f1",   biome: "forest",                type: "spider",      x: 11.4,  z: 2.2,   patrolR: 3.2 },
    { id: "wasp_f1",     biome: "forest",                type: "wasp",        x: -2.0,  z: -12.0, patrolR: 3.5 },
    { id: "beetle_m1",   biome: "mountains",             type: "beetle",      x: -7.5,  z: -36.5, patrolR: 3.5 },
    { id: "crystal_f1",  biome: "frozen",                type: "crystal_bug", x: -38.5, z: -6.5,  patrolR: 3.2 },
    { id: "spider_d1",   biome: "desert",                type: "spider",      x: 38.5,  z: 4.5,   patrolR: 3.5 },
    { id: "wasp_j1",     biome: "jungle",                type: "wasp",        x: 34.0,  z: -36.0, patrolR: 3.8 },
    { id: "crystal_c1",  biome: "crystal_desert",        type: "crystal_bug", x: 64.0,  z: -4.0,  patrolR: 3.5 },
    { id: "crystal_c2",  biome: "crystal_desert",        type: "crystal_bug", x: 74.0,  z: 5.0,   patrolR: 3.2 },
    { id: "wasp_sky1",   biome: "sky_islands",           type: "wasp",        x: -6.0,  z: -72.0, patrolR: 4.0 },
    { id: "spider_sw1",  biome: "toxic_swamp",           type: "spider",      x: -38.5, z: 38.5,  patrolR: 3.5 },
    { id: "crystal_fm1", biome: "frozen_mountains",      type: "crystal_bug", x: -72.0, z: -5.0,  patrolR: 3.4 },
    { id: "beetle_vl1",  biome: "volcanic_lands",        type: "beetle",      x: -6.5,  z: 71.5,  patrolR: 3.5 },
    { id: "crystal_am1", biome: "ancient_machine_world", type: "crystal_bug", x: 30.0,  z: 38.0,  patrolR: 3.6 }
  ];

  HOSTILE_SPECS.forEach((sp, idx) => {
    const cfg = INSECT_TYPES[sp.type];
    const built = createInsectMesh(sp.type, false, null);
    built.grp.position.set(sp.x, 0, sp.z);
    getBiomeChunk(sp.biome).add(built.grp);
    enemies.push({
      id: sp.id,
      isBoss: false,
      biome: sp.biome,
      type: sp.type,
      name: cfg.name,
      homeX: sp.x,
      homeZ: sp.z,
      x: sp.x,
      z: sp.z,
      patrolR: sp.patrolR,
      leashR: 12.5,
      detectR: cfg.detectR,
      attackR: cfg.attackR,
      speed: cfg.speed,
      dmg: cfg.dmg,
      maxHp: cfg.maxHp,
      hp: cfg.maxHp,
      attackCd: cfg.cooldown,
      coins: cfg.coins,
      xp: cfg.xp,
      drop: cfg.drop,
      fsm: INSECT_STATES.IDLE,
      stateTimer: 0,
      atkTimer: 0,
      respawnTimer: 0,
      patrolAngle: idx * 1.1,
      mesh: built.grp,
      vis: built
    });
  });

  // 4 Unique Optional World Bosses in Frontier Arenas
  const worldBosses = [];
  const BOSS_SPECS = [
    { id: "stag_goliath",        name: "Goliath Stag",     biome: "mountains",             type: "beetle",      x: -9.5,  z: -40.5, hp: 180, dmg: 20, col: 0xf59e0b, coins: 1200,  xp: 220, drop: "gold" },
    { id: "sandstorm_scorp",     name: "Dune Empress",     biome: "crystal_desert",        type: "spider",      x: 74.5,  z: -8.5,  hp: 280, dmg: 26, col: 0xf472b6, coins: 18000, xp: 480, drop: "quartz" },
    { id: "cryo_queen",          name: "Cryo Wasp Queen",  biome: "frozen_mountains",      type: "wasp",        x: -74.5, z: 8.5,   hp: 360, dmg: 32, col: 0x67e8f9, coins: 55000, xp: 750, drop: "frostite" },
    { id: "clockwork_leviathan", name: "Clockwork Titan",  biome: "ancient_machine_world", type: "crystal_bug", x: 39.5,  z: 40.5,  hp: 480, dmg: 38, col: 0x38bdf8, coins: 160000,xp: 1200,drop: "titan_alloy" }
  ];

  BOSS_SPECS.forEach((bs) => {
    const built = createInsectMesh(bs.type, true, bs.col);
    built.grp.position.set(bs.x, 0, bs.z);
    getBiomeChunk(bs.biome).add(built.grp);
    const arenaRing = new THREE.Mesh(
      new THREE.RingGeometry(4.2, 4.6, 32),
      basicMat(bs.col, { side: THREE.DoubleSide, transparent: true, opacity: 0.38 })
    );
    arenaRing.rotation.x = -Math.PI / 2;
    arenaRing.position.set(bs.x, 0.05, bs.z);
    getBiomeChunk(bs.biome).add(arenaRing);

    worldBosses.push({
      id: bs.id,
      isBoss: true,
      biome: bs.biome,
      type: bs.type,
      name: bs.name,
      homeX: bs.x,
      homeZ: bs.z,
      x: bs.x,
      z: bs.z,
      patrolR: 2.2,
      leashR: 11.5,
      detectR: 8.5,
      attackR: 3.1,
      speed: bs.type === "wasp" ? 4.4 : 3.2,
      dmg: bs.dmg,
      maxHp: bs.hp,
      hp: bs.hp,
      attackCd: 1.5,
      coins: bs.coins,
      xp: bs.xp,
      drop: bs.drop,
      fsm: INSECT_STATES.IDLE,
      stateTimer: 0,
      atkTimer: 0,
      respawnTimer: 0,
      patrolAngle: 0,
      mesh: built.grp,
      arenaRing,
      vis: built
    });
  });

  // Build Sculpted Terrains, Basins & Dense Scenery for Maps 2-12 using complete spatial pad footprints
  if (window.BiomeEnvBuilder) {
    const padLookup = {};
    const registerPad = (biomeId, wx, wz, r) => {
      const bObj = BIOMES.find((b) => b.id === biomeId);
      if (!bObj) return;
      if (!padLookup[biomeId]) padLookup[biomeId] = [];
      padLookup[biomeId].push({ lx: wx - bObj.cx, lz: wz - bObj.cz, r });
    };
    BUILDING_DEFS.forEach((d) => registerPad(d.biome, d.x, d.z, 3.2));
    MACHINE_DEFS.forEach((m) => registerPad(m.biome, m.x, m.z, 3.4));
    resourceNodes.forEach((n) => registerPad(n.biome, n.x, n.z, 2.1));
    LANDMARK_SPECS.forEach((l) => registerPad(l.biome, l.x, l.z, 2.9));
    CAVE_SPECS.forEach((c) => registerPad(c.biome, c.x, c.z, 2.6));
    envHazards.forEach((h) => registerPad(h.biome, h.x, h.z, h.r + 0.6));
    HOSTILE_SPECS.forEach((e) => registerPad(e.biome, e.x, e.z, 2.4));
    BOSS_SPECS.forEach((b) => registerPad(b.biome, b.x, b.z, 4.9));

    const sceneryDeps = {
      texRock, texFoliage, texBark,
      organicRockGeoA, organicRockGeoB, crystalPrismGeo, gearWheelGeo, indMats, addObstacle
    };
    BIOMES.forEach((b, bi) => {
      if (b.id === "forest") return; // Map 1 preserved untouched
      const chunkGrp = getBiomeChunk(b.id);
      const pads = padLookup[b.id] || [];
      window.BiomeEnvBuilder.setBiomePads(b.id, pads);

      const topGeo = window.BiomeEnvBuilder.buildBiomeTerrainGeometry(THREE, b, bi, pads, BIOME_PALETTES);
      const isIcy = b.id === "frozen" || b.id === "frozen_mountains";
      const padMat = pbrMat(0xffffff, {
        map: biomeFloorTex[b.id] || texRock,
        vertexColors: true,
        roughness: isIcy ? 0.35 : (b.id === "ancient_machine_world" ? 0.55 : 0.86),
        metalness: isIcy ? 0.1 : (b.id === "ancient_machine_world" ? 0.3 : 0.02)
      });
      const topMesh = new THREE.Mesh(topGeo, padMat);
      topMesh.position.set(b.cx, 0.01, b.cz);
      topMesh.receiveShadow = true;
      chunkGrp.add(topMesh);

      const animList = window.BiomeEnvBuilder.getAnimList(b.id);
      window.BiomeEnvBuilder.addBasinFeature(THREE, chunkGrp, b, extraEnvTex, animList);
      window.BiomeEnvBuilder.populateBiomeScenery(THREE, chunkGrp, b, bi, pads, sceneryDeps, animList);
    });
  }

  // Initialize coherent Hexagon Kit enclaves across Maps 2-12 (Map 1 forest untouched)
  if (window.HexagonDistrictBuilder) {
    window.HexagonDistrictBuilder.initDistricts(THREE, getBiomeChunk, pbrMat);
  }

  function steerAroundObstacles(ex, ez, dirX, dirZ) {
    let sx = dirX, sz = dirZ;
    for (let oi = 0; oi < worldObstacles.length; oi++) {
      const ob = worldObstacles[oi];
      const dx = ex - ob.x;
      const dz = ez - ob.z;
      const d = Math.hypot(dx, dz);
      const minD = ob.r + 0.85;
      if (d < minD && d > 0.001) {
        const push = (minD - d) / minD;
        sx += (dx / d) * push * 2.4;
        sz += (dz / d) * push * 2.4;
      }
    }
    const len = Math.hypot(sx, sz) || 1;
    return { x: sx / len, z: sz / len };
  }

  // =========================================================================
  // 12. VIRTUAL JOYSTICK (ONE-HAND CONTROL ONLY)
  // =========================================================================
  const joyZone = document.getElementById("joystick-zone");
  const joyBase = document.getElementById("joystick-base");
  const joyKnob = document.getElementById("joystick-knob");
  let activePointerId = null;
  let joyOriginX = 0;
  let joyOriginY = 0;
  let moveX = 0;
  let moveZ = 0;
  const maxJoyRadius = 46;

  function updateJoystickVisual(dx, dy) {
    const dist = Math.hypot(dx, dy);
    const clamped = Math.min(dist, maxJoyRadius);
    const ang = Math.atan2(dy, dx);
    const kx = Math.cos(ang) * clamped;
    const ky = Math.sin(ang) * clamped;
    joyKnob.style.transform = `translate3d(${kx}px, ${ky}px, 0)`;
    if (dist > 4) {
      const mag = clamped / maxJoyRadius;
      moveX = Math.cos(ang) * mag;
      moveZ = Math.sin(ang) * mag;
    } else {
      moveX = 0;
      moveZ = 0;
    }
  }

  joyZone.addEventListener("pointerdown", (e) => {
    ensureAudio();
    if (isMenuMode || activePointerId !== null) return;
    activePointerId = e.pointerId;
    joyZone.setPointerCapture(e.pointerId);
    const rect = joyBase.getBoundingClientRect();
    joyOriginX = rect.left + rect.width / 2;
    joyOriginY = rect.top + rect.height / 2;
    joyBase.classList.add("active");
    updateJoystickVisual(e.clientX - joyOriginX, e.clientY - joyOriginY);
  });

  joyZone.addEventListener("pointermove", (e) => {
    if (e.pointerId !== activePointerId) return;
    updateJoystickVisual(e.clientX - joyOriginX, e.clientY - joyOriginY);
  });

  function resetJoystick(e) {
    if (e && e.pointerId !== activePointerId) return;
    activePointerId = null;
    moveX = 0;
    moveZ = 0;
    joyKnob.style.transform = "translate3d(0px, 0px, 0)";
    joyBase.classList.remove("active");
  }
  joyZone.addEventListener("pointerup", resetJoystick);
  joyZone.addEventListener("pointercancel", resetJoystick);

  // =========================================================================
  // 12B. UNIFIED THIRD-PERSON & MENU CAMERA CONTROLLER (SINGLE STATE MACHINE)
  // =========================================================================
  const CAM_MODE_MENU = "MENU";
  const CAM_MODE_TRANSITION = "TRANSITION_TO_GAME";
  const CAM_MODE_GAMEPLAY = "GAMEPLAY";
  let camMode = CAM_MODE_MENU;

  function getPreferredCamDist() {
    const d = typeof state.cameraDistance === "number" ? state.cameraDistance : 12.2;
    return clampVal(d, 8.0, 18.0);
  }
  function getCameraSensitivity() {
    const s = typeof state.cameraSensitivity === "number" ? state.cameraSensitivity : 1.0;
    return clampVal(s, 0.5, 2.0);
  }

  let camTargetYaw = 0;
  let camYaw = 0;
  let camTargetPitch = 0.54;
  let camPitch = 0.54;
  let camTargetDist = 12.2;
  let camDist = 12.2;
  let camSafeDist = 12.2;
  const camLookTarget = new THREE.Vector3(0, 0.92, 2.0);
  const currentCamLookAt = new THREE.Vector3(0, 1.05, 1.4);

  // Smooth menu -> gameplay transition state (no dual-controller fighting)
  const transStartPos = new THREE.Vector3();
  const transStartLook = new THREE.Vector3();
  const transGoalPos = new THREE.Vector3();
  let transStartFov = MENU_FOV;
  let transStartPlayerYaw = 0;
  let transElapsed = 0;
  const TRANS_DURATION = 0.78;
  let menuShowcasePhase = 0;

  const camPointers = new Map();
  let prevPinchDist = null;
  let camTapStart = null;
  let lastCamTapTime = 0;

  function clampVal(v, lo, hi) {
    return v < lo ? lo : (v > hi ? hi : v);
  }

  function getTwoPointerDist() {
    const it = camPointers.values();
    const a = it.next().value;
    const b = it.next().value;
    if (!a || !b) return 0;
    return Math.hypot(b.x - a.x, b.y - a.y);
  }

  canvas.addEventListener("pointerdown", (e) => {
    ensureAudio();
    if (isMenuMode || camMode !== CAM_MODE_GAMEPLAY) return;
    if (e.target && (e.target === joyZone || joyZone.contains(e.target))) return;
    if (e.pointerId === activePointerId) return;
    camPointers.set(e.pointerId, { x: e.clientX, y: e.clientY });
    if (camPointers.size === 1) {
      camTapStart = { x: e.clientX, y: e.clientY, t: performance.now(), moved: false };
      prevPinchDist = null;
    } else if (camPointers.size === 2) {
      camTapStart = null;
      prevPinchDist = getTwoPointerDist();
    }
  });

  window.addEventListener("pointermove", (e) => {
    if (!camPointers.has(e.pointerId)) return;
    const prev = camPointers.get(e.pointerId);
    const dx = e.clientX - prev.x;
    const dy = e.clientY - prev.y;
    camPointers.set(e.pointerId, { x: e.clientX, y: e.clientY });

    if (isMenuMode || camMode !== CAM_MODE_GAMEPLAY) return;
    const sens = getCameraSensitivity();
    if (camPointers.size === 1) {
      if (camTapStart && Math.hypot(e.clientX - camTapStart.x, e.clientY - camTapStart.y) > 10) {
        camTapStart.moved = true;
      }
      camTargetYaw -= dx * 0.0062 * sens;
      camTargetPitch = clampVal(camTargetPitch + dy * 0.0048 * sens, 0.22, 1.18);
    } else if (camPointers.size >= 2) {
      const curDist = getTwoPointerDist();
      if (prevPinchDist !== null && curDist > 0) {
        const delta = curDist - prevPinchDist;
        camTargetDist = clampVal(camTargetDist - delta * 0.036, 6.8, 22.0);
      }
      prevPinchDist = curDist;
    }
  });

  function releaseCamPointer(e) {
    if (!camPointers.has(e.pointerId)) return;
    camPointers.delete(e.pointerId);
    if (camPointers.size === 0 && camTapStart && !camTapStart.moved && !isMenuMode && camMode === CAM_MODE_GAMEPLAY) {
      const nowT = performance.now();
      if (nowT - lastCamTapTime < 290) {
        camTargetYaw = 0;
        camTargetPitch = 0.54;
        camTargetDist = getPreferredCamDist();
        lastCamTapTime = 0;
      } else {
        lastCamTapTime = nowT;
      }
    }
    if (camPointers.size < 2) prevPinchDist = null;
    camTapStart = null;
  }
  window.addEventListener("pointerup", releaseCamPointer);
  window.addEventListener("pointercancel", releaseCamPointer);

  canvas.addEventListener("wheel", (e) => {
    if (isMenuMode || camMode !== CAM_MODE_GAMEPLAY) return;
    e.preventDefault();
    camTargetDist = clampVal(camTargetDist + e.deltaY * 0.012, 6.8, 22.0);
  }, { passive: false });

  const keysDown = {};
  window.addEventListener("keydown", (e) => { keysDown[e.key.toLowerCase()] = true; });
  window.addEventListener("keyup", (e) => { keysDown[e.key.toLowerCase()] = false; });
  window.addEventListener("pointerdown", function () { ensureAudio(); }, { passive: true });

  // =========================================================================
  // 13. MOVEMENT COLLISION, BIOME BOUNDS & ANDROID BRIDGE
  // =========================================================================
  function isWalkable(x, z) {
    for (const b of BIOMES) {
      if (!isBiomeUnlocked(b.id)) continue;
      if (Math.abs(x - b.cx) <= 15.0 && Math.abs(z - b.cz) <= 15.0) return true;
    }
    if (isBiomeUnlocked("mountains") && Math.abs(x) <= 2.3 && z <= -14.5 && z >= -19.5) return true;
    if (isBiomeUnlocked("desert") && Math.abs(z) <= 2.3 && x >= 14.5 && x <= 19.5) return true;
    if (isBiomeUnlocked("frozen") && Math.abs(z) <= 2.3 && x <= -14.5 && x >= -19.5) return true;
    if (isBiomeUnlocked("volcanic") && Math.abs(x) <= 2.3 && z >= 14.5 && z <= 19.5) return true;
    if (isBiomeUnlocked("jungle") && Math.abs(x - 34) <= 2.3 && z <= -14.5 && z >= -19.5) return true;
    if (isBiomeUnlocked("crystal_desert") && Math.abs(z) <= 2.3 && x >= 48.5 && x <= 53.5) return true;
    if (isBiomeUnlocked("sky_islands") && Math.abs(x) <= 2.3 && z <= -48.5 && z >= -53.5) return true;
    if (isBiomeUnlocked("toxic_swamp") && Math.abs(x + 34) <= 2.3 && z >= 14.5 && z <= 19.5) return true;
    if (isBiomeUnlocked("frozen_mountains") && Math.abs(z) <= 2.3 && x <= -48.5 && x >= -53.5) return true;
    if (isBiomeUnlocked("volcanic_lands") && Math.abs(x) <= 2.3 && z >= 48.5 && z <= 53.5) return true;
    if (isBiomeUnlocked("ancient_machine_world") && Math.abs(x - 34) <= 2.3 && z >= 14.5 && z <= 19.5) return true;
    if (window.HexagonDistrictBuilder && window.HexagonDistrictBuilder.isWalkable(x, z, isBiomeUnlocked)) return true;
    return false;
  }

  function currentBiomeAt(x, z) {
    let best = BIOMES[0];
    let bestDist = Infinity;
    for (const b of BIOMES) {
      const dist = Math.hypot(x - b.cx, z - b.cz);
      if (dist < bestDist) { bestDist = dist; best = b; }
    }
    return best;
  }

  function syncToAndroid(force) {
    const now = performance.now();
    if (!isMenuMode && !force && Math.hypot(playerGroup.position.x - (state.playerPosX || 0), playerGroup.position.z - (state.playerPosZ || 2)) > 1.5 && now - lastSyncTime > 2000) {
      stateDirty = true;
    }
    if (!force && (!stateDirty || now - lastSyncTime < 250)) return;
    stateDirty = false;
    lastSyncTime = now;
    if (!isMenuMode) {
      state.playerPosX = Math.round(playerGroup.position.x * 100) / 100;
      state.playerPosZ = Math.round(playerGroup.position.z * 100) / 100;
      state.lastBiomeId = activeBiomeId;
    }
    if (window.AndroidBridge && window.AndroidBridge.syncState) {
      try { window.AndroidBridge.syncState(JSON.stringify(state)); } catch (_) {}
    }
  }

  // Camera Anti-Clipping AABBs & Terrain Clearance Solver
  const camColliders = [];
  function addCamBox(cx, cz, halfW, maxY, halfD, activeFn) {
    camColliders.push({
      minX: cx - halfW, maxX: cx + halfW,
      minY: 0, maxY: maxY,
      minZ: cz - halfD, maxZ: cz + halfD,
      activeFn: activeFn || null
    });
  }
  // Trading Post stall
  addCamBox(SELL_ZONE.x - 1.95, SELL_ZONE.z + 1.95, 1.55, 3.25, 1.35);
  // Biome Gate pillars
  BIOMES.forEach((b) => {
    if (!b.gate) return;
    if (b.gate.dir === "z") {
      addCamBox(b.gate.x - 2.4, b.gate.z, 0.55, 3.6, 0.55);
      addCamBox(b.gate.x + 2.4, b.gate.z, 0.55, 3.6, 0.55);
    } else {
      addCamBox(b.gate.x, b.gate.z - 2.4, 0.55, 3.6, 0.55);
      addCamBox(b.gate.x, b.gate.z + 2.4, 0.55, 3.6, 0.55);
    }
  });
  // Buildings
  BUILDING_DEFS.forEach((def) => {
    addCamBox(def.x, def.z, 1.55, 4.6, 1.55, () => (state.buildingLevels[def.id] || 0) > 0);
  });
  // Industrial Machines
  MACHINE_DEFS.forEach((def) => {
    const isBelt = def.id === "conveyor_belt";
    addCamBox(def.x, def.z, isBelt ? 1.25 : 1.55, 5.2, isBelt ? 2.85 : 1.55, () => getMachineLevel(def.id) > 0);
  });
  // Fleet Vehicles & Containers
  addCamBox(-9.2, 7.6, 1.15, 2.1, 1.9, () => getFleetLevel("delivery_truck") > 0);
  addCamBox(11.6, -6.5, 1.5, 2.35, 1.5, () => getFleetLevel("storage_container") > 0);
  addCamBox(-11.8, -11.4, 1.25, 2.1, 1.35, () => getFleetLevel("excavator") > 0);
  // Tall Trees & Exploration Landmarks
  resourceNodes.forEach((n) => {
    if (n.kind === "wood") {
      addCamBox(n.x, n.z, 0.45, 3.3, 0.45, () => n.mesh.visible);
    }
  });
  worldLandmarks.forEach((lm) => {
    addCamBox(lm.x, lm.z, 1.35, 4.4, 1.35);
  });

  function raycastCamColliders(ox, oy, oz, dx, dy, dz, maxDist) {
    let bestT = maxDist;
    const invX = Math.abs(dx) > 1e-5 ? 1 / dx : 1e8;
    const invY = Math.abs(dy) > 1e-5 ? 1 / dy : 1e8;
    const invZ = Math.abs(dz) > 1e-5 ? 1 / dz : 1e8;
    for (let i = 0; i < camColliders.length; i++) {
      const box = camColliders[i];
      if (box.activeFn && !box.activeFn()) continue;
      const t1 = (box.minX - ox) * invX;
      const t2 = (box.maxX - ox) * invX;
      const t3 = (box.minY - oy) * invY;
      const t4 = (box.maxY - oy) * invY;
      const t5 = (box.minZ - oz) * invZ;
      const t6 = (box.maxZ - oz) * invZ;
      const tMin = Math.max(Math.min(t1, t2), Math.min(t3, t4), Math.min(t5, t6));
      const tMax = Math.min(Math.max(t1, t2), Math.max(t3, t4), Math.max(t5, t6));
      if (tMax >= 0 && tMin <= tMax && tMin > 0.35 && tMin < bestT) {
        bestT = tMin;
      }
    }
    return bestT;
  }

  function getTerrainFloorY(x, z) {
    for (let i = 0; i < BIOMES.length; i++) {
      const b = BIOMES[i];
      if (Math.abs(x - b.cx) <= 16.4 && Math.abs(z - b.cz) <= 16.4) {
        if (b.id !== "forest" && window.BiomeEnvBuilder) {
          const ly = window.BiomeEnvBuilder.sampleBiomeTerrainLocalY(b.id, x - b.cx, z - b.cz, i);
          return Math.max(0.08, ly + 0.14);
        }
        const edge = Math.max(Math.abs(x - b.cx), Math.abs(z - b.cz));
        return edge > 13.6 ? 0.35 : 0.12;
      }
    }
    if ((Math.abs(x) <= 2.8 && Math.abs(z) <= 55.0) ||
        (Math.abs(z) <= 2.8 && Math.abs(x) <= 55.0) ||
        (Math.abs(x - 34) <= 2.8 && Math.abs(z) <= 20.5) ||
        (Math.abs(x + 34) <= 2.8 && z >= 14.0 && z <= 20.5)) {
      return 0.25;
    }
    if (window.HexagonDistrictBuilder) {
      const hexY = window.HexagonDistrictBuilder.getFloorY(x, z);
      if (hexY !== null) return hexY;
    }
    return -0.88;
  }

  function updateWorldStreaming(px, pz) {
    for (let i = 0; i < BIOMES.length; i++) {
      const b = BIOMES[i];
      const cg = biomeChunkGroups[b.id];
      if (!cg) continue;
      const vis = (isMenuMode && b.id === "forest") || Math.hypot(px - b.cx, pz - b.cz) < 78;
      cg.visible = vis;
      if (vis && !isMenuMode && b.id !== "forest" && window.ModelAssetManager) {
        window.ModelAssetManager.ensureBiomeAssets(THREE, b.id);
      }
    }
  }

  function computeMenuCameraPose(phase, px, pz, outPos, outLook) {
    const orbitAng = Math.sin(phase) * 0.34;
    outPos.set(
      px + Math.sin(orbitAng) * 3.1 + 0.65,
      1.92 + Math.sin(phase * 0.7) * 0.10,
      pz + Math.cos(orbitAng) * 3.85
    );
    outLook.set(px + 0.12, 1.08, pz - 1.35);
  }

  function computeGameplayCameraPose(px, pz, dist, yaw, pitch, outPos) {
    const dirX = Math.cos(pitch) * Math.sin(yaw);
    const dirY = Math.sin(pitch);
    const dirZ = Math.cos(pitch) * Math.cos(yaw);
    const hitDist = raycastCamColliders(px, 0.92, pz, dirX, dirY, dirZ, dist);
    const safeD = Math.max(3.2, hitDist < dist ? hitDist - 0.52 : dist);
    const gx = px + dirX * safeD;
    const gz = pz + dirZ * safeD;
    const floorClearY = getTerrainFloorY(gx, gz) + 0.95;
    const gy = Math.max(floorClearY, 0.92 + dirY * safeD);
    outPos.set(gx, gy, gz);
    return safeD;
  }

  function startMenuShowcaseCamera(immediate) {
    isMenuMode = true;
    camMode = CAM_MODE_MENU;
    // Always frame the Main Menu showcase inside the detailed Forest Settlement clearing
    playerGroup.position.set(0, 0, 2);
    updateWorldStreaming(0, 2);
    if (joyZoneEl) joyZoneEl.style.display = "none";
    if (floatLayer) floatLayer.style.display = "none";
    resetJoystick({ pointerId: activePointerId });
    camPointers.clear();
    camTapStart = null;
    prevPinchDist = null;
    if (immediate) {
      computeMenuCameraPose(menuShowcasePhase, 0, 2, camera.position, currentCamLookAt);
      camera.fov = MENU_FOV;
      camera.updateProjectionMatrix();
      camera.lookAt(currentCamLookAt);
    }
  }

  function beginMenuToGameplayTransition(targetX, targetZ) {
    // 1. Disable menu camera controller immediately and clear any drag pointers
    isMenuMode = false;
    camMode = CAM_MODE_TRANSITION;
    camPointers.clear();
    camTapStart = null;
    prevPinchDist = null;
    resetJoystick({ pointerId: activePointerId });
    if (joyZoneEl) joyZoneEl.style.display = "block";
    if (floatLayer) floatLayer.style.display = "block";

    // 2. Snapshot exact start pose so transition begins seamlessly without jump
    transStartPos.copy(camera.position);
    transStartLook.copy(currentCamLookAt);
    transStartFov = camera.fov;
    transStartPlayerYaw = playerGroup.rotation.y;
    transElapsed = 0;

    // 3. Lock target gameplay spherical params so hand-off to GAMEPLAY has 0 discontinuity
    camTargetYaw = 0;
    camYaw = 0;
    camTargetPitch = 0.54;
    camPitch = 0.54;
    camTargetDist = getPreferredCamDist();
    camDist = camTargetDist;
    camLookTarget.set(targetX, 0.92, targetZ);
    camSafeDist = computeGameplayCameraPose(
      targetX, targetZ, camDist, camYaw, camPitch, transGoalPos
    );
    updateWorldStreaming(targetX, targetZ);
  }

  window.enterGameplay = function (isContinue, savedX, savedZ, savedBiomeId) {
    ensureAudio();
    const useContinue = Boolean(isContinue);
    const hasCoords = typeof savedX === "number" && !isNaN(savedX) && typeof savedZ === "number" && !isNaN(savedZ);
    const targetX = useContinue && hasCoords ? savedX : 0;
    const targetZ = useContinue && hasCoords ? savedZ : 2;
    const biomeStr = typeof savedBiomeId === "string" && savedBiomeId ? savedBiomeId : "forest";

    // Guard against duplicate entry calls that could interrupt an active transition/gameplay camera
    if (!isMenuMode && camMode !== CAM_MODE_MENU && !useContinue) {
      return;
    }

    const sameClearing = Math.hypot(targetX - 0, targetZ - 2) < 5.5 && biomeStr === "forest";
    if (camMode === CAM_MODE_MENU && sameClearing) {
      playerGroup.position.set(targetX, 0, targetZ);
      beginMenuToGameplayTransition(targetX, targetZ);
    } else {
      // Distant biome/position restore behind loading overlay: switch directly & cleanly
      isMenuMode = false;
      camMode = CAM_MODE_GAMEPLAY;
      camPointers.clear();
      camTapStart = null;
      prevPinchDist = null;
      resetJoystick({ pointerId: activePointerId });
      if (joyZoneEl) joyZoneEl.style.display = "block";
      if (floatLayer) floatLayer.style.display = "block";
      playerGroup.position.set(targetX, 0, targetZ);
      playerGroup.rotation.y = 0;
      camTargetYaw = 0; camYaw = 0;
      camTargetPitch = 0.54; camPitch = 0.54;
      camTargetDist = getPreferredCamDist();
      camDist = camTargetDist;
      camLookTarget.set(targetX, 0.92, targetZ);
      currentCamLookAt.copy(camLookTarget);
      camSafeDist = computeGameplayCameraPose(targetX, targetZ, camDist, camYaw, camPitch, camera.position);
      camera.fov = GAMEPLAY_FOV;
      camera.updateProjectionMatrix();
      camera.lookAt(camLookTarget);
      updateWorldStreaming(targetX, targetZ);
      const bNow = currentBiomeAt(targetX, targetZ);
      if (bNow.id !== activeBiomeId) {
        activeBiomeId = bNow.id;
        if (window.AndroidBridge && window.AndroidBridge.onBiomeChanged) {
          try { window.AndroidBridge.onBiomeChanged(bNow.id); } catch (_) {}
        }
      }
    }
    emitAmbientTelemetry(
      performance.now(),
      currentBiomeAt(playerGroup.position.x, playerGroup.position.z).id,
      playerGroup.position.x,
      playerGroup.position.z,
      true
    );
  };

  window.setMenuMode = function (inMenu) {
    const wantMenu = Boolean(inMenu);
    if (wantMenu) {
      if (camMode === CAM_MODE_MENU && isMenuMode) return;
      if (!isMenuMode) {
        syncToAndroid(true);
      }
      startMenuShowcaseCamera(false);
      emitAmbientTelemetry(performance.now(), "forest", 0, 2, true);
    } else {
      if (camMode !== CAM_MODE_MENU && !isMenuMode) return;
      window.enterGameplay(false, playerGroup.position.x, playerGroup.position.z, activeBiomeId);
    }
  };

  window.setAppBackgrounded = function (bg) {
    isAppBackgrounded = Boolean(bg);
    if (audioCtx) {
      if (isAppBackgrounded) {
        if (audioCtx.state === "running") {
          audioCtx.suspend().catch(function () {});
        }
      } else if (!state.isMuted && audioCtx.state === "suspended") {
        audioCtx.resume().catch(function () {});
      }
    }
    if (!isAppBackgrounded) {
      prevTime = performance.now();
    }
  };

  document.addEventListener("visibilitychange", function () {
    window.setAppBackgrounded(Boolean(document.hidden));
  });

  window.applyGameState = function (incoming) {
    if (!incoming) return;
    const prevCamDistPref = typeof state.cameraDistance === "number" ? state.cameraDistance : 12.2;
    state = Object.assign({}, state, incoming);
    if (!Array.isArray(state.discoveredSecrets)) state.discoveredSecrets = [];
    if (!Array.isArray(state.defeatedBosses)) state.defeatedBosses = [];
    if (!Array.isArray(state.unlockedVehicles)) state.unlockedVehicles = [];
    if (typeof state.insectsDefeated !== "number") state.insectsDefeated = 0;
    if (typeof state.playerHp !== "number" || state.playerHp <= 0) state.playerHp = getPlayerMaxHp();
    state.playerHp = Math.min(state.playerHp, getPlayerMaxHp());
    if (typeof state.cameraDistance === "number" && Math.abs(state.cameraDistance - prevCamDistPref) > 0.01) {
      camTargetDist = getPreferredCamDist();
    }
    if (audioCtx && (state.isMuted || isAppBackgrounded) && audioCtx.state === "running") {
      audioCtx.suspend().catch(function () {});
    } else if (audioCtx && !state.isMuted && !isAppBackgrounded && audioCtx.state === "suspended") {
      audioCtx.resume().catch(function () {});
    }
    applyGraphicsQuality();
    refreshCharacterVisuals();
    refreshWorldState();
    if (incoming.settlement && window.HexagonDistrictBuilder) {
      window.HexagonDistrictBuilder.syncSettlement(incoming.settlement);
    }
  };

  window.teleportPlayer = function (x, z, biomeId) {
    if (isMenuMode || camMode === CAM_MODE_MENU) {
      isMenuMode = false;
      if (joyZoneEl) joyZoneEl.style.display = "block";
      if (floatLayer) floatLayer.style.display = "block";
    }
    camMode = CAM_MODE_GAMEPLAY;
    playerGroup.position.set(x, 0, z);
    camLookTarget.set(x, 0.92, z);
    currentCamLookAt.copy(camLookTarget);
    camSafeDist = computeGameplayCameraPose(x, z, camDist, camYaw, camPitch, camera.position);
    camera.fov = GAMEPLAY_FOV;
    camera.updateProjectionMatrix();
    camera.lookAt(camLookTarget);
    updateWorldStreaming(x, z);
    const bNow = currentBiomeAt(x, z);
    if (bNow.id !== activeBiomeId) {
      activeBiomeId = bNow.id;
      if (window.AndroidBridge && window.AndroidBridge.onBiomeChanged) {
        try { window.AndroidBridge.onBiomeChanged(bNow.id); } catch (_) {}
      }
    }
    if (!RAIN_BIOMES[bNow.id] && weatherState !== "CLEAR") {
      weatherState = "CLEAR";
      weatherTimer = 14 + Math.random() * 14;
    }
    emitAmbientTelemetry(performance.now(), bNow.id, x, z, true);
    const found = BIOMES.find(function (b) { return b.id === biomeId; });
    showBanner("Traveled to " + (found ? found.name : "Region"));
  };

  window.triggerUpgradeCelebration = function () {
    const px = playerGroup.position.x, pz = playerGroup.position.z;
    spawnBurst(px, 1.35, pz, 0xfbbf24, 14, 0.2);
    spawnBurst(px, 1.65, pz, 0x22d3ee, 10, 0.16);
    playSfx("upgrade");
  };

  window.showNativeBanner = function (msg) {
    showBanner(msg);
    playSfx(msg && (msg.indexOf("Tier") !== -1 || msg.indexOf("Unlocked") !== -1) ? "upgrade" : "unlock");
  };

  window.flushSaveToAndroid = function () {
    stateDirty = true;
    syncToAndroid(true);
  };

  window.resetWorld = function () {
    playerGroup.position.set(0, 0, 2);
    camLookTarget.set(0, 0.92, 2);
    camTargetYaw = 0; camYaw = 0;
    camTargetPitch = 0.54; camPitch = 0.54;
    const prefDist = getPreferredCamDist();
    camTargetDist = prefDist; camDist = prefDist; camSafeDist = prefDist;
    state.playerHp = getPlayerMaxHp();
    state.activeVehicleId = null;
    state.playerPosX = 0.0;
    state.playerPosZ = 2.0;
    state.lastBiomeId = "forest";
    resourceNodes.forEach((n) => {
      n.hp = n.maxHp;
      n.respawnTimer = 0;
      n.fallTimer = 0;
      n.visualRoot.rotation.set(0, 0, 0);
      if (n.crackMesh) n.crackMesh.visible = false;
      n.mesh.visible = true;
      n.mesh.scale.set(1, 1, 1);
    });
    enemies.concat(worldBosses).forEach((e) => {
      e.x = e.homeX;
      e.z = e.homeZ;
      e.hp = e.maxHp;
      e.fsm = INSECT_STATES.IDLE;
      e.stateTimer = 0;
      e.atkTimer = 0;
      e.respawnTimer = 0;
      e.mesh.position.set(e.homeX, 0, e.homeZ);
      e.mesh.visible = true;
      e.vis.warnSign.visible = false;
      e.vis.warnRing.visible = false;
      e.vis.hpFill.scale.x = 1;
      e.vis.hpBarGrp.visible = e.isBoss;
    });
    weatherState = "CLEAR";
    weatherTimer = 26.0;
    rainIntensity = 0.0;
    overcastFactor = 0.0;
    baseFogDensity = 0.0024;
    baseSunIntensity = 1.38;
    scene.fog.density = baseFogDensity;
    sunLight.intensity = baseSunIntensity;
    hemiLight.intensity = BASE_HEMI_INTENSITY;
    ambientLight.intensity = BASE_AMBIENT_INTENSITY;
    rimLight.intensity = BASE_RIM_INTENSITY;
    rainMesh.visible = false;
    refreshCharacterVisuals();
    refreshWorldState();
    emitAmbientTelemetry(performance.now(), "forest", 0, 2, true);
    showBanner("New Frontier Started!");
  };

  function refreshWorldState() {
    BIOMES.forEach((b) => {
      const g = biomeGates[b.id];
      if (!g) return;
      const unlocked = isBiomeUnlocked(b.id);
      g.barrier.visible = !unlocked;
      g.padRing.visible = !unlocked;
    });

    BUILDING_DEFS.forEach((def) => {
      const wb = worldBuildings[def.id];
      const lvl = state.buildingLevels[def.id] || 0;
      const workers = getBuildingWorkers(def.id);
      const lines = getBuildingLines(def.id);
      wb.struct.visible = lvl > 0;
      setSrgbHex(wb.ring.material.color, lvl > 0 ? 0xfbbf24 : 0x10b981);
      wb.lineModules.forEach((m, idx) => { m.visible = lvl > 0 && idx < lines; });
      wb.workerMeshes.forEach((m, idx) => { m.visible = lvl > 0 && idx < workers; });
    });

    // Update All Industrial Machines
    MACHINE_DEFS.forEach((def) => {
      const wm = worldMachines[def.id];
      if (!wm) return;
      const lvl = getMachineLevel(def.id);
      const running = isMachineRunning(def.id);
      const biomeOpen = isBiomeUnlocked(def.biome);
      wm.group.visible = biomeOpen || lvl > 0;
      wm.rig.visible = lvl > 0;
      setSrgbHex(wm.statusLed.material.color, running ? 0x22c55e : 0xef4444);
      setSrgbHex(wm.statusLed.material.emissive, running ? 0x22c55e : 0xef4444);
    });

    // Update Fleet Vehicles & Cargo Containers
    worldFleet.forklift.group.visible = getFleetLevel("forklift") > 0;
    worldFleet.mining_cart.group.visible = getFleetLevel("mining_cart") > 0;
    worldFleet.delivery_truck.group.visible = getFleetLevel("delivery_truck") > 0;
    worldFleet.excavator.group.visible = getFleetLevel("excavator") > 0;
    const cLvl = getFleetLevel("storage_container");
    worldFleet.storage_container.group.visible = cLvl > 0;
    worldFleet.storage_container.containers.forEach((c, idx) => {
      c.visible = (idx === 0 && cLvl >= 1) || (idx === 1 && cLvl >= 2) || (idx === 2 && cLvl >= 4);
    });

    // Update Secret Caves & Optional World Bosses
    secretCaves.forEach((sc) => {
      const discovered = Array.isArray(state.discoveredSecrets) && state.discoveredSecrets.indexOf(sc.def.id) !== -1;
      sc.beacon.visible = !discovered;
      sc.chest.scale.setScalar(discovered ? 0.72 : 1.0);
    });
    worldBosses.forEach((boss) => {
      const defeated = Array.isArray(state.defeatedBosses) && state.defeatedBosses.indexOf(boss.id) !== -1;
      boss.mesh.visible = !defeated;
      if (boss.arenaRing) boss.arenaRing.visible = !defeated;
    });

    if (window.HexagonDistrictBuilder) {
      window.HexagonDistrictBuilder.refreshCausewayVisibility(isBiomeUnlocked);
    }
  }

  // =========================================================================
  // 14. MAIN GAME LOOP
  // =========================================================================
  let prevTime = performance.now();
  let swingCooldown = 0;
  let swingAnimTimer = 0;
  let sellCooldown = 0;
  let passiveAccum = 0;
  let fullBagWarnCooldown = 0;
  let activeBiomeId = "forest";
  let smokeTimer = 0;
  let combatDamageCooldown = 0;
  let hpRegenAccum = 0;

  // =========================================================================
  // 14A. DYNAMIC WEATHER SYSTEM, RIVER AUDIO ZONES & INDOOR SENSING
  // =========================================================================
  const RAIN_BIOMES = {
    forest: true,
    mountains: true,
    jungle: true,
    toxic_swamp: true,
    sky_islands: true
  };
  let weatherState = "CLEAR"; // "CLEAR" | "OVERCAST" | "RAIN"
  let weatherTimer = 26.0;
  let rainIntensity = 0.0;
  let overcastFactor = 0.0;
  let lastWeatherBiomeId = "forest";

  // Pooled 3D rain streaks (zero per-frame geometry/material allocation; hidden when clear)
  const RAIN_DROP_COUNT = 160;
  const rainPositions = new Float32Array(RAIN_DROP_COUNT * 6);
  const rainMeta = new Float32Array(RAIN_DROP_COUNT * 4); // xOff, yPos, zOff, speed
  for (let ri = 0; ri < RAIN_DROP_COUNT; ri++) {
    const idx4 = ri * 4;
    rainMeta[idx4] = (Math.random() - 0.5) * 26;
    rainMeta[idx4 + 1] = Math.random() * 14;
    rainMeta[idx4 + 2] = (Math.random() - 0.5) * 26;
    rainMeta[idx4 + 3] = 16 + Math.random() * 8;
  }
  const rainGeo = new THREE.BufferGeometry();
  const rainPosAttr = new THREE.BufferAttribute(rainPositions, 3);
  rainPosAttr.setUsage(THREE.DynamicDrawUsage);
  rainGeo.setAttribute("position", rainPosAttr);
  const rainMat = new THREE.LineBasicMaterial({
    color: 0xd8ecff,
    transparent: true,
    opacity: 0.0,
    depthTest: true,
    depthWrite: false,
    fog: false
  });
  const rainMesh = new THREE.LineSegments(rainGeo, rainMat);
  rainMesh.frustumCulled = false;
  rainMesh.renderOrder = 5;
  rainMesh.visible = false;
  scene.add(rainMesh);

  window.setWeatherState = function (nextWeather) {
    if (nextWeather === "RAIN" || nextWeather === "OVERCAST" || nextWeather === "CLEAR") {
      weatherState = nextWeather;
      weatherTimer = nextWeather === "RAIN" ? 32.0 : (nextWeather === "OVERCAST" ? 10.0 : 38.0);
    }
  };

  window.getWeatherDebugState = function () {
    return {
      weatherState: weatherState,
      rainIntensity: Math.round(rainIntensity * 1000) / 1000,
      overcastFactor: Math.round(overcastFactor * 1000) / 1000,
      fogDensity: scene.fog ? Math.round(scene.fog.density * 100000) / 100000 : 0,
      baseFogDensity: Math.round(baseFogDensity * 100000) / 100000,
      sunIntensity: Math.round(sunLight.intensity * 1000) / 1000,
      hemiIntensity: Math.round(hemiLight.intensity * 1000) / 1000,
      ambientIntensity: Math.round(ambientLight.intensity * 1000) / 1000,
      rainVisible: rainMesh.visible,
      rainOpacity: Math.round(rainMat.opacity * 1000) / 1000
    };
  };

  function tickWeather(dt, biomeId, px, pz) {
    const canRain = Boolean(RAIN_BIOMES[biomeId]);
    if (biomeId !== lastWeatherBiomeId) {
      lastWeatherBiomeId = biomeId;
      if (!canRain && weatherState !== "CLEAR") {
        weatherState = "CLEAR";
        weatherTimer = 18.0;
      }
    }

    if (!isMenuMode && !isAppBackgrounded) {
      weatherTimer -= dt;
      if (weatherTimer <= 0) {
        if (!canRain) {
          weatherState = "CLEAR";
          weatherTimer = 25.0;
        } else if (weatherState === "CLEAR") {
          weatherState = "OVERCAST";
          weatherTimer = 8.0 + Math.random() * 4.0;
        } else if (weatherState === "OVERCAST") {
          weatherState = "RAIN";
          weatherTimer = 24.0 + Math.random() * 14.0;
        } else {
          weatherState = "CLEAR";
          weatherTimer = 36.0 + Math.random() * 24.0;
        }
      }
    }

    const targetRain = (weatherState === "RAIN" && canRain && !isMenuMode) ? 1.0 : 0.0;
    const targetOvercast = (weatherState === "RAIN" && canRain && !isMenuMode)
      ? 1.0
      : ((weatherState === "OVERCAST" && canRain && !isMenuMode) ? 0.4 : 0.0);

    const rainRate = targetRain > rainIntensity ? 0.35 : 0.28;
    const overRate = targetOvercast > overcastFactor ? 0.32 : 0.25;
    rainIntensity += (targetRain - rainIntensity) * Math.min(1, dt * rainRate * 2.2);
    overcastFactor += (targetOvercast - overcastFactor) * Math.min(1, dt * overRate * 2.2);
    if (rainIntensity < 0.002 && targetRain === 0) rainIntensity = 0;
    if (overcastFactor < 0.002 && targetOvercast === 0) overcastFactor = 0;

    if (rainIntensity > 0.01 && !isAppBackgrounded) {
      const q = typeof state.graphicsQuality === "number" ? state.graphicsQuality : 1;
      const activeCount = q === 0 ? 85 : RAIN_DROP_COUNT;
      rainMesh.visible = true;
      rainMat.opacity = Math.min(0.38, rainIntensity * 0.38);
      rainGeo.setDrawRange(0, activeCount * 2);
      const camX = camera.position.x;
      const camY = camera.position.y;
      const camZ = camera.position.z;
      for (let ri = 0; ri < activeCount; ri++) {
        const idx4 = ri * 4;
        let ry = rainMeta[idx4 + 1] - rainMeta[idx4 + 3] * dt;
        if (ry < 0) ry = 13.5 + (ry % 1.5);
        rainMeta[idx4 + 1] = ry;
        const rx = px + rainMeta[idx4];
        const rz = pz + rainMeta[idx4 + 2];
        const idx6 = ri * 6;
        // Skip drops right in front of the camera near-plane so streaks never block the lens
        const dCamSq = (rx - camX) * (rx - camX) + (ry - camY) * (ry - camY) + (rz - camZ) * (rz - camZ);
        if (dCamSq < 4.4) {
          rainPositions[idx6] = rx;
          rainPositions[idx6 + 1] = -10;
          rainPositions[idx6 + 2] = rz;
          rainPositions[idx6 + 3] = rx;
          rainPositions[idx6 + 4] = -10;
          rainPositions[idx6 + 5] = rz;
        } else {
          rainPositions[idx6] = rx;
          rainPositions[idx6 + 1] = ry;
          rainPositions[idx6 + 2] = rz;
          rainPositions[idx6 + 3] = rx - 0.11;
          rainPositions[idx6 + 4] = ry - 0.56;
          rainPositions[idx6 + 5] = rz - 0.06;
        }
      }
      rainPosAttr.needsUpdate = true;
    } else {
      rainMesh.visible = false;
    }
  }

  // Exact River, Waterfall & Water-Channel Audio Zones (precomputed once at startup)
  const RIVER_MAX_HEAR_DIST = 14.0;
  const RIVER_SEGMENTS = [
    // Mountains Hexagon Sanctuary River (ox = -27.5, oz = -34.0)
    { ax: -17.1, az: -38.50, bx: -19.7, bz: -34.00, boost: 0.0 },
    { ax: -19.7, az: -34.00, bx: -22.3, bz: -29.50, boost: 0.0 },
    { ax: -22.3, az: -29.50, bx: -18.4, bz: -31.75, boost: 0.0 },
    // Jungle Hexagon River Outpost (ox = 57.5, oz = -34.0)
    { ax: 61.4,  az: -36.25, bx: 58.8,  bz: -31.75, boost: 0.0 },
    { ax: 58.8,  az: -31.75, bx: 64.0,  bz: -31.75, boost: 0.0 }
  ];
  const WATER_POINTS = [
    // Mountains sanctuary pond & watermill
    { x: -15.8, z: -31.75, bias: 0.0 },
    { x: -18.4, z: -31.75, bias: 0.0 },
    // Mountains alpine lake & cliff waterfall
    { x: 6.4,   z: -39.8,  bias: 1.5 },
    { x: 9.29,  z: -41.89, bias: 0.0 },
    // Jungle lagoon & cliff waterfall
    { x: 27.8,  z: -28.2,  bias: 1.5 },
    { x: 25.0,  z: -26.1,  bias: 0.0 },
    // Sky islands celestial pond & waterfall
    { x: -5.8,  z: -62.0,  bias: 2.0 },
    { x: -8.4,  z: -60.1,  bias: 0.5 },
    // Desert oasis, Crystal desert pool, Glacial tarn (gentle edge water)
    { x: 26.5,  z: 5.4,    bias: 3.5 },
    { x: 62.0,  z: 5.4,    bias: 4.0 },
    { x: -61.8, z: -5.8,   bias: 4.0 },
    // Inter-island arched bridges crossing the lagoon water (heard only on/near bridge)
    { x: 0,   z: -17, bias: 4.5 },
    { x: 17,  z: 0,   bias: 4.5 },
    { x: -17, z: 0,   bias: 4.5 },
    { x: 0,   z: 17,  bias: 4.5 },
    { x: 34,  z: -17, bias: 4.5 },
    { x: 51,  z: 0,   bias: 4.5 },
    { x: -34, z: 17,  bias: 4.5 },
    { x: -51, z: 0,   bias: 4.5 },
    { x: 0,   z: 51,  bias: 4.5 },
    { x: 34,  z: 17,  bias: 4.5 }
  ];

  function computeNearestRiverDistance(px, pz) {
    let best = 999.0;
    for (let i = 0; i < RIVER_SEGMENTS.length; i++) {
      const s = RIVER_SEGMENTS[i];
      const d = distToSegment(px, pz, s.ax, s.az, s.bx, s.bz) + s.boost;
      if (d < best) best = d;
    }
    for (let i = 0; i < WATER_POINTS.length; i++) {
      const wp = WATER_POINTS[i];
      const d = Math.hypot(px - wp.x, pz - wp.z) + wp.bias;
      if (d < best) best = d;
    }
    return best;
  }

  function computeIndoorFactor(px, pz) {
    let indoor = 0.0;
    // Underground cave entrances
    for (let i = 0; i < CAVE_SPECS.length; i++) {
      const c = CAVE_SPECS[i];
      const d = Math.hypot(px - c.x, pz - c.z);
      if (d < 3.6) {
        const t = Math.max(0, Math.min(1, (3.6 - d) / 1.8));
        if (t * 0.88 > indoor) indoor = t * 0.88;
      }
    }
    // Trading Post covered pavilion stall
    const dStall = Math.hypot(px - (SELL_ZONE.x - 1.95), pz - (SELL_ZONE.z + 1.95));
    if (dStall < 2.2) {
      const t = Math.max(0, Math.min(1, (2.2 - dStall) / 1.1));
      if (t * 0.55 > indoor) indoor = t * 0.55;
    }
    // Built factory interiors/roof overhangs
    for (let i = 0; i < BUILDING_DEFS.length; i++) {
      const bd = BUILDING_DEFS[i];
      if ((state.buildingLevels[bd.id] || 0) <= 0) continue;
      const dB = Math.hypot(px - bd.x, pz - bd.z);
      if (dB < 2.4) {
        const t = Math.max(0, Math.min(1, (2.4 - dB) / 1.2));
        if (t * 0.62 > indoor) indoor = t * 0.62;
      }
    }
    return indoor;
  }

  let lastTelemetryEmitMs = 0;
  let lastTelemetryBiome = "";
  let lastTelemetryWeather = "";
  const telemetryPayload = {
    biomeId: "forest",
    weather: "CLEAR",
    rainIntensity: 0,
    nearestRiverDist: 999,
    riverMaxDist: RIVER_MAX_HEAR_DIST,
    indoorFactor: 0,
    isMenuMode: true
  };

  function emitAmbientTelemetry(now, biomeId, px, pz, force) {
    if (!window.AndroidBridge || !window.AndroidBridge.updateAmbientTelemetry) return;
    if (!force && biomeId === lastTelemetryBiome && weatherState === lastTelemetryWeather && now - lastTelemetryEmitMs < 85) {
      return;
    }
    lastTelemetryEmitMs = now;
    lastTelemetryBiome = biomeId;
    lastTelemetryWeather = weatherState;

    telemetryPayload.biomeId = biomeId;
    telemetryPayload.weather = weatherState;
    telemetryPayload.rainIntensity = Math.round(rainIntensity * 1000) / 1000;
    telemetryPayload.nearestRiverDist = Math.round(computeNearestRiverDistance(px, pz) * 100) / 100;
    telemetryPayload.riverMaxDist = RIVER_MAX_HEAR_DIST;
    telemetryPayload.indoorFactor = Math.round(computeIndoorFactor(px, pz) * 1000) / 1000;
    telemetryPayload.isMenuMode = isMenuMode;

    try {
      window.AndroidBridge.updateAmbientTelemetry(JSON.stringify(telemetryPayload));
    } catch (_) {}
  }

  function applyPlayerDamage(amount, label) {
    if (isMenuMode || amount <= 0) return;
    const v = getActiveVehicle();
    const armor = v ? v.armor : 0;
    const finalDmg = Math.max(1, Math.round(amount * (1 - armor)));
    state.playerHp = Math.max(0, state.playerHp - finalDmg);
    combatDamageCooldown = 4.0;
    stateDirty = true;
    playSfx("player_hurt");
    haptic("light");
    spawnBurst(playerGroup.position.x, 1.0, playerGroup.position.z, 0xef4444, 6, 0.14);
    spawnFloatingText(playerGroup.position, label || `-${finalDmg} HP`, "#ef4444");
    if (state.playerHp <= 0) {
      state.playerHp = getPlayerMaxHp();
      playerGroup.position.set(0, 0, 2);
      camLookTarget.set(0, 0.92, 2);
      updateWorldStreaming(0, 2);
      showBanner("Retreated to Base Camp!");
      syncToAndroid(true);
    }
  }

  let lastFramePresentMs = 0;
  const menuGoalPos = new THREE.Vector3();
  const menuGoalLook = new THREE.Vector3();

  function tick(now) {
    requestAnimationFrame(tick);
    try {
      const fpsCap = state.fpsLimit === 30 ? 30 : 60;
      if (fpsCap === 30 && lastFramePresentMs > 0 && (now - lastFramePresentMs) < 31.5) {
        return;
      }
      lastFramePresentMs = now;

      syncViewportSize();
      const dt = Math.min((now - prevTime) / 1000, 0.05);
      prevTime = now;

      const curPx = playerGroup.position.x;
      const curPz = playerGroup.position.z;
      const bNow = currentBiomeAt(curPx, curPz);
      tickWeather(dt, bNow.id, curPx, curPz);
      const indoorNow = computeIndoorFactor(curPx, curPz);
      tickBackgroundMusic(dt, bNow.id, indoorNow, rainIntensity);
      emitAmbientTelemetry(now, bNow.id, curPx, curPz, false);

      // Gentle foliage & forest tree wind, fireflies & cloud drift
      for (let ci = 0; ci < skyClouds.length; ci++) {
        skyClouds[ci].position.x += dt * (0.42 + ci * 0.08);
        if (skyClouds[ci].position.x > 52) skyClouds[ci].position.x = -52;
      }
      for (let gi = 0; gi < grassTufts.length; gi++) {
        grassTufts[gi].rotation.z = Math.sin(now * 0.0024 + gi) * 0.065;
      }
      const forestTreeList = nodesByBiome.forest;
      if (forestTreeList) {
        for (let ti = 0; ti < forestTreeList.length; ti++) {
          const tn = forestTreeList[ti];
          if (tn.kind === "wood" && tn.fallTimer === 0 && tn.respawnTimer === 0 && tn.bounceTimer === 0) {
            tn.visualRoot.rotation.z = Math.sin(now * 0.0014 + ti * 1.3) * 0.026;
            tn.visualRoot.rotation.x = Math.cos(now * 0.0011 + ti) * 0.016;
          }
        }
      }
      for (let fi = 0; fi < fireflies.length; fi++) {
        const f = fireflies[fi];
        f.mesh.position.x = f.baseX + Math.sin(now * 0.001 * f.speed + f.phase) * 0.85;
        f.mesh.position.y = f.baseY + Math.sin(now * 0.0018 * f.speed + f.phase) * 0.35;
        f.mesh.position.z = f.baseZ + Math.cos(now * 0.0012 * f.speed + f.phase) * 0.85;
      }

      // 1. Player Movement (strictly enabled only when active camera controller is GAMEPLAY)
      const controlsActive = !isMenuMode && camMode === CAM_MODE_GAMEPLAY;
      let inputX = controlsActive ? moveX : 0;
      let inputZ = controlsActive ? moveZ : 0;
      if (controlsActive && activePointerId === null) {
        let kx = 0, kz = 0;
        if (keysDown["arrowleft"] || keysDown["a"]) kx -= 1;
        if (keysDown["arrowright"] || keysDown["d"]) kx += 1;
        if (keysDown["arrowup"] || keysDown["w"]) kz -= 1;
        if (keysDown["arrowdown"] || keysDown["s"]) kz += 1;
        if (kx !== 0 || kz !== 0) {
          const kl = Math.hypot(kx, kz);
          inputX = kx / kl;
          inputZ = kz / kl;
        }
      }

      const activeV = controlsActive ? getActiveVehicle() : null;
      let envSpeedMult = 1.0;
      for (let hi = 0; hi < envHazards.length; hi++) {
        const hz = envHazards[hi];
        if ((hz.kind === "ice_boost" || hz.kind === "wind_boost") && Math.hypot(curPx - hz.x, curPz - hz.z) <= hz.r) {
          envSpeedMult = Math.max(envSpeedMult, hz.kind === "ice_boost" ? 1.35 : 1.32);
        }
      }
      const speedBase = MOVE_SPEEDS[Math.min(state.moveSpeedLevel, 8) - 1];
      const vehicleSpeedMult = activeV ? activeV.speedMult : 1.0;
      const speed = (hasUniqueItem("golden_compass") ? speedBase * 1.15 : speedBase) * vehicleSpeedMult * envSpeedMult;
      const cosYaw = Math.cos(camYaw);
      const sinYaw = Math.sin(camYaw);
      const worldMoveX = inputX * cosYaw + inputZ * sinYaw;
      const worldMoveZ = -inputX * sinYaw + inputZ * cosYaw;
      const isMoving = controlsActive && Math.hypot(worldMoveX, worldMoveZ) > 0.05;
      if (playerAuraRing.visible) playerAuraRing.rotation.z += dt * 1.8;

      if (activeV && explorerDish) {
        if (activeV.id === "explorer_buggy") {
          explorerDish.rotation.y += dt * 3.0;
          const rPulse = 1 + Math.sin(now * 0.006) * 0.12;
          explorerRadarRing.scale.set(rPulse, rPulse, 1);
        }
      }

      if (isMoving) {
        const nx = playerGroup.position.x + worldMoveX * speed * dt;
        const nz = playerGroup.position.z + worldMoveZ * speed * dt;
        if (isWalkable(nx, playerGroup.position.z)) playerGroup.position.x = nx;
        if (isWalkable(playerGroup.position.x, nz)) playerGroup.position.z = nz;

        const targetYaw = Math.atan2(worldMoveX, worldMoveZ);
        let diff = targetYaw - playerGroup.rotation.y;
        while (diff > Math.PI) diff -= Math.PI * 2;
        while (diff < -Math.PI) diff += Math.PI * 2;
        playerGroup.rotation.y += diff * Math.min(1, dt * 14);

        if (activeV) {
          playerBodyPivot.position.y = 0.40 + Math.sin(now * 0.025) * 0.018;
          playerBodyPivot.rotation.x = 0.04;
          legL.rotation.x = -0.95;
          legR.rotation.x = -0.95;
          const wList = vehicleWheels[activeV.id] || [];
          for (let wi = 0; wi < wList.length; wi++) wList[wi].rotation.x += speed * dt * 2.2;
        } else {
          const gait = Math.sin(now * 0.015) * 0.58;
          legL.rotation.x = gait;
          legR.rotation.x = -gait;
          armL.rotation.x = -gait * 0.7;
          playerBodyPivot.position.y = Math.abs(Math.sin(now * 0.015)) * 0.06;
          playerBodyPivot.rotation.x = 0.07;
        }
      } else {
        if (activeV) {
          playerBodyPivot.position.y = 0.40;
          playerBodyPivot.rotation.x *= 0.85;
          legL.rotation.x = -0.95;
          legR.rotation.x = -0.95;
          armL.rotation.x *= 0.8;
        } else {
          legL.rotation.x *= 0.8;
          legR.rotation.x *= 0.8;
          armL.rotation.x *= 0.8;
          playerBodyPivot.rotation.x *= 0.85;
          playerBodyPivot.position.y = Math.sin(now * 0.003) * 0.024;
        }
        if (camMode === CAM_MODE_MENU) {
          playerGroup.rotation.y = Math.sin(menuShowcasePhase * 1.55) * 0.28;
        }
      }

      // 2. Single Active Camera Controller (MENU | TRANSITION_TO_GAME | GAMEPLAY)
      const px = playerGroup.position.x;
      const pz = playerGroup.position.z;
      updateWorldStreaming(px, pz);

      if (camMode === CAM_MODE_MENU) {
        menuShowcasePhase += dt * 0.42;
        computeMenuCameraPose(menuShowcasePhase, px, pz, menuGoalPos, menuGoalLook);
        const menuLerp = 1 - Math.exp(-4.5 * dt);
        camera.position.lerp(menuGoalPos, menuLerp);
        currentCamLookAt.lerp(menuGoalLook, menuLerp);
        if (Math.abs(MENU_FOV - camera.fov) > 0.03) {
          camera.fov += (MENU_FOV - camera.fov) * (1 - Math.exp(-5.0 * dt));
          camera.updateProjectionMatrix();
        }
        camera.lookAt(currentCamLookAt);
      } else if (camMode === CAM_MODE_TRANSITION) {
        transElapsed = Math.min(TRANS_DURATION, transElapsed + dt);
        const u = transElapsed / TRANS_DURATION;
        const ease = u < 0.5 ? 4 * u * u * u : 1 - Math.pow(-2 * u + 2, 3) / 2;

        camera.position.lerpVectors(transStartPos, transGoalPos, ease);
        currentCamLookAt.lerpVectors(transStartLook, camLookTarget, ease);
        const fovNow = transStartFov + (GAMEPLAY_FOV - transStartFov) * ease;
        if (Math.abs(fovNow - camera.fov) > 0.01) {
          camera.fov = fovNow;
          camera.updateProjectionMatrix();
        }
        let yawDiff = 0 - transStartPlayerYaw;
        while (yawDiff > Math.PI) yawDiff -= Math.PI * 2;
        while (yawDiff < -Math.PI) yawDiff += Math.PI * 2;
        playerGroup.rotation.y = transStartPlayerYaw + yawDiff * ease;
        camera.lookAt(currentCamLookAt);

        if (transElapsed >= TRANS_DURATION) {
          camMode = CAM_MODE_GAMEPLAY;
          camera.position.copy(transGoalPos);
          currentCamLookAt.copy(camLookTarget);
          camera.fov = GAMEPLAY_FOV;
          camera.updateProjectionMatrix();
          camera.lookAt(currentCamLookAt);
        }
      } else {
        if (Math.abs(GAMEPLAY_FOV - camera.fov) > 0.04) {
          camera.fov += (GAMEPLAY_FOV - camera.fov) * (1 - Math.exp(-8.0 * dt));
          camera.updateProjectionMatrix();
        }
        const followLerp = 1 - Math.exp(-10.0 * dt);
        const orbitLerp = 1 - Math.exp(-11.0 * dt);
        const zoomLerp = 1 - Math.exp(-8.5 * dt);
        camLookTarget.x += (px - camLookTarget.x) * followLerp;
        camLookTarget.y += (0.92 - camLookTarget.y) * followLerp;
        camLookTarget.z += (pz - camLookTarget.z) * followLerp;

        camYaw += (camTargetYaw - camYaw) * orbitLerp;
        camPitch += (camTargetPitch - camPitch) * orbitLerp;
        camDist += (camTargetDist - camDist) * zoomLerp;

        const dirX = Math.cos(camPitch) * Math.sin(camYaw);
        const dirY = Math.sin(camPitch);
        const dirZ = Math.cos(camPitch) * Math.cos(camYaw);

        const hitDist = raycastCamColliders(
          camLookTarget.x, camLookTarget.y, camLookTarget.z,
          dirX, dirY, dirZ,
          camDist
        );
        const desiredSafeDist = Math.max(3.2, hitDist < camDist ? hitDist - 0.52 : camDist);
        const safeRate = desiredSafeDist < camSafeDist ? 18.0 : 5.5;
        camSafeDist += (desiredSafeDist - camSafeDist) * (1 - Math.exp(-safeRate * dt));

        const finalX = camLookTarget.x + dirX * camSafeDist;
        const finalZ = camLookTarget.z + dirZ * camSafeDist;
        const floorClearY = getTerrainFloorY(finalX, finalZ) + 0.95;
        const finalY = Math.max(floorClearY, camLookTarget.y + dirY * camSafeDist);

        camera.position.set(finalX, finalY, finalZ);
        currentCamLookAt.copy(camLookTarget);
        camera.lookAt(currentCamLookAt);
      }

      sunLight.position.set(px + 16, 26, pz + 14);
      sunLight.target.position.set(px, 0, pz);
      sunLight.target.updateMatrixWorld();
      rimLight.position.set(px - 14, 18, pz - 16);
      rimLight.target.position.set(px, 0, pz);
      rimLight.target.updateMatrixWorld();
      skyDome.position.set(px, 0, pz);

      // 3. Biome Atmosphere Transition & Compose HUD Sync
      const bNow2 = currentBiomeAt(px, pz);
      if (bNow2.id !== activeBiomeId) {
        activeBiomeId = bNow2.id;
        if (window.AndroidBridge && window.AndroidBridge.onBiomeChanged) {
          try { window.AndroidBridge.onBiomeChanged(bNow2.id); } catch (_) {}
        }
      }
      currentSkyColor.lerp(linCol(bNow2.sky), dt * 2.5);
      currentFogColor.lerp(linCol(bNow2.fog), dt * 2.5);

      if (window.BiomeEnvBuilder && window.BiomeEnvBuilder.BIOME_ATMOSPHERE[bNow2.id]) {
        const atmo = window.BiomeEnvBuilder.BIOME_ATMOSPHERE[bNow2.id];
        const atmoRate = Math.min(1, dt * 2.2);
        baseFogDensity += (atmo.fogDensity - baseFogDensity) * atmoRate;
        currentBaseHemiSky.lerp(linCol(atmo.hemiSky), atmoRate);
        currentBaseHemiGround.lerp(linCol(atmo.hemiGround), atmoRate);
        currentBaseSunColor.lerp(linCol(atmo.sunCol), atmoRate);
        baseSunIntensity += (atmo.sunInt - baseSunIntensity) * atmoRate;
        window.BiomeEnvBuilder.updateAnimations(now, dt, biomeChunkGroups);
      }

      // Compute final weather-blended atmosphere statelessly from base biome targets (zero exponential compounding)
      const cloudBlend = Math.min(0.62, overcastFactor * 0.62);
      currentRenderedSkyColor.copy(currentSkyColor).lerp(overcastSkyTop, cloudBlend);
      currentRenderedFogColor.copy(currentFogColor).lerp(overcastFogHorizon, cloudBlend * 0.88);
      currentRenderedBottomColor.copy(currentRenderedFogColor).multiplyScalar(0.54);

      skyUniforms.uTop.value.copy(currentRenderedSkyColor);
      skyUniforms.uHorizon.value.copy(currentRenderedFogColor);
      skyUniforms.uBottom.value.copy(currentRenderedBottomColor);
      scene.fog.color.copy(currentRenderedFogColor);
      renderer.setClearColor(currentRenderedSkyColor, 1);

      scene.fog.density = Math.min(0.0085, baseFogDensity * (1 + overcastFactor * 0.36));

      sunLight.intensity = baseSunIntensity * (1 - overcastFactor * 0.28);
      sunLight.color.copy(currentBaseSunColor).lerp(overcastSunColor, overcastFactor * 0.32);
      hemiLight.color.copy(currentBaseHemiSky).lerp(overcastFogHorizon, overcastFactor * 0.38);
      hemiLight.groundColor.copy(currentBaseHemiGround).lerp(overcastGroundTint, overcastFactor * 0.25);
      hemiLight.intensity = BASE_HEMI_INTENSITY + overcastFactor * 0.16;
      ambientLight.intensity = BASE_AMBIENT_INTENSITY + overcastFactor * 0.14;
      rimLight.intensity = BASE_RIM_INTENSITY + overcastFactor * 0.08;
      if (window.HexagonDistrictBuilder) {
        window.HexagonDistrictBuilder.updateAnimations(now, dt, biomeChunkGroups);
      }

      // 3B. Landmarks, Environmental Hazards & Secret Underground Caves
      for (let li = 0; li < worldLandmarks.length; li++) {
        const lm = worldLandmarks[li];
        lm.orb.rotation.y += dt * 1.4;
        const baseY = lm.baseOrbY !== undefined ? lm.baseOrbY : 4.05;
        lm.orb.position.y = baseY + Math.sin(now * 0.0022 + li) * 0.16;
      }

      for (let hi = 0; hi < envHazards.length; hi++) {
        const hz = envHazards[hi];
        hz.cooldown = Math.max(0, (hz.cooldown || 0) - dt);
        if (hz.kind === "wind_boost") {
          hz.ring.rotation.z += dt * 2.8;
        } else if (hz.kind === "spore_vent" || hz.kind === "geyser") {
          const cyc = ((now * 0.001 + (hz.phase || 0)) % 4.4);
          if (cyc > 2.4 && cyc < 3.5) {
            const wp = 1 + Math.sin(now * 0.024) * 0.16;
            hz.ring.scale.set(wp, wp, 1);
            hz.core.scale.y = 1.4;
          } else if (cyc >= 3.5) {
            hz.ring.scale.set(1.2, 1.2, 1);
            hz.core.scale.y = 2.6;
            if (!isMenuMode && hz.cooldown <= 0 && Math.hypot(px - hz.x, pz - hz.z) < hz.r) {
              hz.cooldown = 1.25;
              playSfx("geyser");
              spawnBurst(hz.x, 0.9, hz.z, hz.kind === "geyser" ? 0xf97316 : 0x84cc16, 8, 0.18);
              applyPlayerDamage(hz.kind === "geyser" ? 8 : 6, hz.kind === "geyser" ? "Lava Vent!" : "Toxic Spore!");
            }
          } else {
            hz.ring.scale.set(1, 1, 1);
            hz.core.scale.y = 1;
          }
        }
      }

      if (!isMenuMode) {
        for (let ci = 0; ci < secretCaves.length; ci++) {
          const sc = secretCaves[ci];
          sc.beacon.rotation.y += dt * 2.2;
          if (activeV && activeV.radarRadius > 0 && Math.hypot(px - sc.def.x, pz - sc.def.z) < activeV.radarRadius) {
            const rp = 1.35 + Math.sin(now * 0.008) * 0.35;
            sc.beacon.scale.setScalar(0.24 * rp);
          } else {
            sc.beacon.scale.setScalar(0.24);
          }
          if (!isBiomeUnlocked(sc.def.biome)) continue;
          const already = Array.isArray(state.discoveredSecrets) && state.discoveredSecrets.indexOf(sc.def.id) !== -1;
          if (!already && Math.hypot(px - sc.def.x, pz - sc.def.z) < 2.45) {
            state.discoveredSecrets.push(sc.def.id);
            state.coins += sc.def.coins;
            state.lifetimeEarned += sc.def.coins;
            state.xp = (state.xp || 0) + sc.def.xp;
            const room = Math.max(0, getMaxBag() - getBagCount());
            const dropQty = Math.min(2, room);
            if (dropQty > 0) {
              state.inventory[sc.def.drop] = (state.inventory[sc.def.drop] || 0) + dropQty;
              state.gatheredTotals[sc.def.drop] = (state.gatheredTotals[sc.def.drop] || 0) + dropQty;
            }
            stateDirty = true;
            refreshCharacterVisuals();
            refreshWorldState();
            playSfx("secret_found");
            haptic("unlock");
            spawnBurst(sc.def.x, 1.4, sc.def.z, 0x22d3ee, 14, 0.2);
            showBanner(`Secret Cave Found! +${formatMoney(sc.def.coins)}`);
            syncToAndroid(true);
          }
        }
      }

      // 4. Resource Nodes, Hostile Insect AI, Bosses & Auto-Gathering/Combat
      swingCooldown = Math.max(0, swingCooldown - dt);
      fullBagWarnCooldown = Math.max(0, fullBagWarnCooldown - dt);
      combatDamageCooldown = Math.max(0, combatDamageCooldown - dt);
      if (!isMenuMode && combatDamageCooldown <= 0 && state.playerHp < getPlayerMaxHp()) {
        hpRegenAccum += 6 * dt;
        if (hpRegenAccum >= 1) {
          const heal = Math.floor(hpRegenAccum);
          hpRegenAccum -= heal;
          state.playerHp = Math.min(getPlayerMaxHp(), state.playerHp + heal);
          stateDirty = true;
        }
      }

      let nearestEnemy = null;
      let nearestEnemyDist = 2.85;
      const allCreatures = enemies.concat(worldBosses);
      for (let ei = 0; ei < allCreatures.length; ei++) {
        const e = allCreatures[ei];
        if (e.isBoss && Array.isArray(state.defeatedBosses) && state.defeatedBosses.indexOf(e.id) !== -1) {
          e.mesh.visible = false;
          continue;
        }
        const chunk = biomeChunkGroups[e.biome];
        if (chunk && !chunk.visible) continue;
        if (e.respawnTimer > 0) {
          e.respawnTimer -= dt;
          if (e.respawnTimer <= 0) {
            e.x = e.homeX;
            e.z = e.homeZ;
            e.hp = e.maxHp;
            e.fsm = INSECT_STATES.IDLE;
            e.mesh.position.set(e.homeX, 0, e.homeZ);
            e.mesh.visible = true;
            e.vis.hpFill.scale.x = 1;
            e.vis.hpBarGrp.visible = e.isBoss;
          }
          continue;
        }

        e.atkTimer = Math.max(0, e.atkTimer - dt);
        const dPlayer = isMenuMode ? 999 : Math.hypot(px - e.x, pz - e.z);
        const dHome = Math.hypot(e.x - e.homeX, e.z - e.homeZ);
        const canAggro = !isMenuMode && isBiomeUnlocked(e.biome);

        if (e.fsm === INSECT_STATES.IDLE) {
          e.patrolAngle += dt * 0.55;
          const tx = e.homeX + Math.cos(e.patrolAngle) * e.patrolR;
          const tz = e.homeZ + Math.sin(e.patrolAngle) * e.patrolR;
          const pdx = tx - e.x, pdz = tz - e.z;
          const pLen = Math.hypot(pdx, pdz) || 1;
          const st = steerAroundObstacles(e.x, e.z, pdx / pLen, pdz / pLen);
          e.x += st.x * e.speed * 0.35 * dt;
          e.z += st.z * e.speed * 0.35 * dt;
          e.mesh.rotation.y = Math.atan2(st.x, st.z);
          if (canAggro && dPlayer < e.detectR) {
            e.fsm = INSECT_STATES.DETECT_PLAYER;
            e.stateTimer = 0.16;
          }
        } else if (e.fsm === INSECT_STATES.DETECT_PLAYER) {
          e.mesh.rotation.y = Math.atan2(px - e.x, pz - e.z);
          e.stateTimer -= dt;
          if (e.stateTimer <= 0) {
            e.fsm = INSECT_STATES.WARNING;
            e.stateTimer = e.isBoss ? 0.72 : 0.58;
            e.vis.warnSign.visible = true;
            e.vis.warnRing.visible = true;
            if (e.isBoss) playSfx("boss_roar");
          }
        } else if (e.fsm === INSECT_STATES.WARNING) {
          e.mesh.rotation.y = Math.atan2(px - e.x, pz - e.z);
          e.vis.warnSign.rotation.y += dt * 5;
          const wPulse = 1 + Math.sin(now * 0.025) * 0.15;
          e.vis.warnRing.scale.set(wPulse, wPulse, 1);
          e.stateTimer -= dt;
          if (!canAggro || dPlayer > e.detectR * 1.35) {
            e.vis.warnSign.visible = false;
            e.vis.warnRing.visible = false;
            e.fsm = INSECT_STATES.RETURN_TO_PATROL;
          } else if (e.stateTimer <= 0) {
            e.vis.warnSign.visible = false;
            e.vis.warnRing.visible = false;
            e.fsm = INSECT_STATES.CHASE;
          }
        } else if (e.fsm === INSECT_STATES.CHASE) {
          if (!canAggro || dHome > e.leashR || dPlayer > e.detectR * 1.6) {
            e.fsm = INSECT_STATES.RETURN_TO_PATROL;
          } else if (dPlayer <= e.attackR) {
            e.fsm = INSECT_STATES.ATTACK;
          } else {
            const cdx = px - e.x, cdz = pz - e.z;
            const cLen = Math.hypot(cdx, cdz) || 1;
            const st = steerAroundObstacles(e.x, e.z, cdx / cLen, cdz / cLen);
            e.x += st.x * e.speed * dt;
            e.z += st.z * e.speed * dt;
            e.mesh.rotation.y = Math.atan2(st.x, st.z);
          }
        } else if (e.fsm === INSECT_STATES.ATTACK) {
          e.mesh.rotation.y = Math.atan2(px - e.x, pz - e.z);
          if (!canAggro || dHome > e.leashR) {
            e.fsm = INSECT_STATES.RETURN_TO_PATROL;
          } else if (dPlayer > e.attackR + 0.65) {
            e.fsm = INSECT_STATES.CHASE;
          } else if (e.atkTimer <= 0) {
            e.atkTimer = e.attackCd;
            e.vis.bodyPivot.position.z = 0.25;
            applyPlayerDamage(e.dmg);
          }
        } else if (e.fsm === INSECT_STATES.RETURN_TO_PATROL) {
          const rdx = e.homeX - e.x, rdz = e.homeZ - e.z;
          const rLen = Math.hypot(rdx, rdz);
          if (rLen < 0.75) {
            e.hp = e.maxHp;
            e.vis.hpFill.scale.x = 1;
            e.vis.hpBarGrp.visible = e.isBoss;
            e.fsm = INSECT_STATES.IDLE;
          } else {
            const st = steerAroundObstacles(e.x, e.z, rdx / rLen, rdz / rLen);
            e.x += st.x * e.speed * 0.9 * dt;
            e.z += st.z * e.speed * 0.9 * dt;
            e.mesh.rotation.y = Math.atan2(st.x, st.z);
          }
        }

        e.vis.bodyPivot.position.z *= 0.85;
        e.mesh.position.set(e.x, 0, e.z);
        if (e.type === "wasp" && e.vis.wingL && e.vis.wingR) {
          const flap = Math.sin(now * 0.04) * 0.45;
          e.vis.wingL.rotation.z = flap;
          e.vis.wingR.rotation.z = -flap;
        }
        const walkSwing = Math.sin(now * 0.018) * 0.22;
        for (let li = 0; li < e.vis.legs.length; li++) {
          e.vis.legs[li].rotation.x = (li % 2 === 0 ? 1 : -1) * walkSwing;
        }
        e.vis.hpBarGrp.lookAt(camera.position);

        if (canAggro && dPlayer < nearestEnemyDist) {
          nearestEnemyDist = dPlayer;
          nearestEnemy = e;
        }
      }

      let nearestNode = null;
      let nearestDist = 2.65;

      for (const node of resourceNodes) {
        if (node.fallTimer > 0) {
          node.fallTimer -= dt;
          const u = 1 - Math.max(0, node.fallTimer) / node.fallTotal;
          node.visualRoot.rotation.z = node.fallDirZ * (u * u) * 1.48;
          if (node.fallTimer <= 0) {
            node.visualRoot.rotation.set(0, 0, 0);
            node.mesh.visible = false;
            node.respawnTimer = 4.2;
            const rDef = RESOURCES[node.kind];
            spawnBurst(node.x, 0.7, node.z, 0x22c55e, 7, 0.2);
            spawnBurst(node.x, 0.5, node.z, rDef.color, 6, 0.18);
          }
          continue;
        }
        if (node.respawnTimer > 0) {
          node.respawnTimer -= dt;
          if (node.respawnTimer <= 0) {
            node.hp = node.maxHp;
            node.visualRoot.rotation.set(0, 0, 0);
            if (node.crackMesh) node.crackMesh.visible = false;
            node.mesh.visible = true;
            node.mesh.scale.set(0.1, 0.1, 0.1);
          }
          continue;
        }
        if (node.mesh.scale.x < 1) {
          const ns = Math.min(1, node.mesh.scale.x + dt * 4);
          node.mesh.scale.set(ns, ns, ns);
        }
        if (node.bounceTimer > 0) {
          node.bounceTimer -= dt;
          const wobble = 1 + Math.sin(node.bounceTimer * 35) * 0.1;
          node.mesh.scale.set(wobble, 2 - wobble, wobble);
          if (node.kind === "wood") node.visualRoot.rotation.z = Math.sin(node.bounceTimer * 40) * 0.12;
        } else if (node.mesh.scale.x === 1) {
          node.mesh.scale.set(1, 1, 1);
          if (node.kind === "wood") node.visualRoot.rotation.z = Math.sin(now * 0.0016 + node.id) * 0.022;
        }

        if (isMenuMode || !isBiomeUnlocked(node.biome)) continue;
        const dist = Math.hypot(node.x - px, node.z - pz);
        if (dist < nearestDist) { nearestDist = dist; nearestNode = node; }
      }

      const chainsawLvl = getFleetLevel("chainsaw_drill");
      const vDrillBonus = activeV ? activeV.drillBonus : 0;
      if (!isMenuMode && nearestEnemy && swingCooldown <= 0) {
        const atkLvl = Math.max(state.axeLevel, state.pickaxeLevel);
        const gatherBase = GATHER_RATES[Math.min(state.gatherSpeedLevel, 8) - 1];
        swingCooldown = 0.50 / (gatherBase * (1 + (atkLvl - 1) * 0.14 + vDrillBonus));
        swingAnimTimer = 0.24;
        if (!isMoving) playerGroup.rotation.y = Math.atan2(nearestEnemy.x - px, nearestEnemy.z - pz);

        const baseAtk = TOOL_DAMAGES[Math.min(atkLvl, 8) - 1] * 4;
        const atkDmg = Math.max(4, Math.round(baseAtk * (1 + chainsawLvl * 0.25 + vDrillBonus)));
        nearestEnemy.hp -= atkDmg;
        nearestEnemy.vis.hpBarGrp.visible = true;
        nearestEnemy.vis.hpFill.scale.x = Math.max(0.01, nearestEnemy.hp / nearestEnemy.maxHp);
        playSfx("hit_enemy");
        haptic("light");
        spawnBurst(nearestEnemy.x, 0.7, nearestEnemy.z, 0xfbbf24, 6, 0.14);
        spawnFloatingText(nearestEnemy.mesh.position, `-${atkDmg}`, "#fbbf24");

        if (nearestEnemy.fsm === INSECT_STATES.IDLE || nearestEnemy.fsm === INSECT_STATES.RETURN_TO_PATROL) {
          nearestEnemy.fsm = INSECT_STATES.CHASE;
        }
        if (nearestEnemy.hp <= 0) {
          nearestEnemy.vis.warnSign.visible = false;
          nearestEnemy.vis.warnRing.visible = false;
          nearestEnemy.mesh.visible = false;
          state.coins += nearestEnemy.coins;
          state.lifetimeEarned += nearestEnemy.coins;
          state.xp = (state.xp || 0) + nearestEnemy.xp;
          state.insectsDefeated = (state.insectsDefeated || 0) + 1;
          const room = Math.max(0, getMaxBag() - getBagCount());
          if (room > 0 && nearestEnemy.drop) {
            state.inventory[nearestEnemy.drop] = (state.inventory[nearestEnemy.drop] || 0) + 1;
            state.gatheredTotals[nearestEnemy.drop] = (state.gatheredTotals[nearestEnemy.drop] || 0) + 1;
            refreshCharacterVisuals();
          }
          if (nearestEnemy.isBoss) {
            if (!Array.isArray(state.defeatedBosses)) state.defeatedBosses = [];
            if (state.defeatedBosses.indexOf(nearestEnemy.id) === -1) state.defeatedBosses.push(nearestEnemy.id);
            if (nearestEnemy.arenaRing) nearestEnemy.arenaRing.visible = false;
            playSfx("unlock");
            showBanner(`Boss Defeated: ${nearestEnemy.name}! +${formatMoney(nearestEnemy.coins)}`);
          } else {
            nearestEnemy.respawnTimer = 18.0;
            playSfx("pickup");
            spawnFloatingText(nearestEnemy.mesh.position, `+${formatMoney(nearestEnemy.coins)}`, "#4ade80");
          }
          spawnBurst(nearestEnemy.x, 0.85, nearestEnemy.z, 0x22d3ee, 12, 0.22);
          stateDirty = true;
          syncToAndroid(true);
        }
      } else if (!isMenuMode && nearestNode && swingCooldown <= 0) {
        const isTree = nearestNode.kind === "wood";
        const newKind = isTree ? "axe" : "pick";
        if (activeToolKind !== newKind) {
          activeToolKind = newKind;
          updateActiveToolMesh();
        }
        const toolLvl = isTree ? state.axeLevel : state.pickaxeLevel;

        if (getBagCount() >= getMaxBag()) {
          if (fullBagWarnCooldown <= 0) {
            spawnFloatingText(playerGroup.position, "BACKPACK FULL!", "#ef4444");
            fullBagWarnCooldown = 1.6;
          }
        } else {
          if (!isMoving) {
            playerGroup.rotation.y = Math.atan2(nearestNode.x - px, nearestNode.z - pz);
          }
          const gatherBase = GATHER_RATES[Math.min(state.gatherSpeedLevel, 8) - 1];
          const toolSpeedBonus = 1 + (toolLvl - 1) * 0.14;
          const drillSpeedBonus = 1 + chainsawLvl * 0.16 + vDrillBonus;
          swingCooldown = 0.52 / (gatherBase * toolSpeedBonus * drillSpeedBonus);
          swingAnimTimer = 0.24;

          const baseDmg = TOOL_DAMAGES[Math.min(toolLvl, 8) - 1];
          const dmg = Math.max(1, Math.round(baseDmg * (1 + chainsawLvl * 0.25 + vDrillBonus)));
          nearestNode.hp -= dmg;
          nearestNode.bounceTimer = 0.22;

          const dmgRatio = 1 - Math.max(0, nearestNode.hp) / nearestNode.maxHp;
          if (nearestNode.crackMesh && dmgRatio > 0.05) {
            nearestNode.crackMesh.visible = true;
            nearestNode.crackMesh.scale.setScalar(0.75 + dmgRatio * 0.5);
          }

          const rDef = RESOURCES[nearestNode.kind];
          playMaterialHitSfx(nearestNode.kind, dmgRatio);
          haptic("light");
          spawnBurst(nearestNode.x, 1.0, nearestNode.z, rDef.color, toolLvl >= 6 ? 7 : 5, 0.14);
          if (chainsawLvl > 0) {
            spawnBurst(nearestNode.x, 0.9, nearestNode.z, 0xf97316, 3, 0.11);
          }

          if (nearestNode.hp <= 0) {
            playMaterialBreakSfx(nearestNode.kind);
            haptic("upgrade");

            if (nearestNode.kind === "wood") {
              nearestNode.fallDirZ = (px <= nearestNode.x) ? -1 : 1;
              nearestNode.fallTimer = nearestNode.fallTotal;
            } else {
              nearestNode.respawnTimer = 4.2;
              nearestNode.mesh.visible = false;
              spawnBurst(nearestNode.x, 0.75, nearestNode.z, rDef.color, 10, 0.22);
              spawnBurst(nearestNode.x, 0.65, nearestNode.z, 0x64748b, 5, 0.2);
            }

            const effLvl = getResUpgradeLevel("gatherEfficiency");
            const effChance = (effLvl - 1) * 0.12 + (state.backpackLevel - 1) * 0.05;
            const effBonus = Math.random() < effChance ? 1 : 0;
            const rareBonus = (!isTree && (nearestNode.kind === "gold" || nearestNode.kind === "crystal" || nearestNode.kind === "rare") && toolLvl >= 4) ? 1 : 0;
            const fleetOreChance = !isTree ? (getFleetLevel("mining_cart") * 0.12 + getFleetLevel("excavator") * 0.22) : 0;
            const fleetBonus = Math.floor(fleetOreChance) + (Math.random() < (fleetOreChance % 1) ? 1 : 0);
            const baseYield = 1 + Math.floor((toolLvl - 1) / 2) + rareBonus + effBonus + fleetBonus;
            const yieldAmt = Math.min(baseYield, getMaxBag() - getBagCount());
            if (yieldAmt > 0) {
              state.inventory[nearestNode.kind] = (state.inventory[nearestNode.kind] || 0) + yieldAmt;
              state.gatheredTotals[nearestNode.kind] = (state.gatheredTotals[nearestNode.kind] || 0) + yieldAmt;
              stateDirty = true;
              refreshCharacterVisuals();
              playSfx("pickup");
              spawnFloatingText(nearestNode.mesh.position, `+${yieldAmt} ${rDef.name}`, rDef.css);
              spawnFlyingDrop(new THREE.Vector3(nearestNode.x, 1.0, nearestNode.z), playerGroup.position, rDef.color);
            }

            const rareLvl = getResUpgradeLevel("rareDiscovery");
            const rareChance = (rareLvl - 1) * 0.08 + (state.pickaxeLevel - 1) * 0.03;
            if (rareChance > 0 && getBagCount() < getMaxBag() && Math.random() < rareChance) {
              const bonusId = state.unlockedBiomes.indexOf("frozen") !== -1 ? "crystal" : "gold";
              const bonusDef = RESOURCES[bonusId];
              state.inventory[bonusId] = (state.inventory[bonusId] || 0) + 1;
              state.gatheredTotals[bonusId] = (state.gatheredTotals[bonusId] || 0) + 1;
              stateDirty = true;
              refreshCharacterVisuals();
              spawnFloatingText(new THREE.Vector3(nearestNode.x, 1.8, nearestNode.z), `+1 Rare ${bonusDef.name}!`, bonusDef.css);
            }
          }
        }
      }

      if (swingAnimTimer > 0) {
        swingAnimTimer -= dt;
        armRPivot.rotation.x = -1.65 * Math.sin((swingAnimTimer / 0.24) * Math.PI);
      } else if (isMenuMode) {
        armRPivot.rotation.x = -0.68 + Math.sin(now * 0.0022) * 0.05;
      } else {
        armRPivot.rotation.x = isMoving ? Math.sin(now * 0.015) * 0.35 : -0.18;
      }

      // 5. Auto-Sell Zone
      sellCooldown = Math.max(0, sellCooldown - dt);
      coinSign.rotation.z += dt * 2.2;
      if (!isMenuMode) {
        const distToSell = Math.hypot(px - SELL_ZONE.x, pz - SELL_ZONE.z);
        if (distToSell <= SELL_ZONE.radius && sellCooldown <= 0) {
          const invKeys = Object.keys(state.inventory).filter((k) => state.inventory[k] > 0);
          if (invKeys.length > 0) {
            const k = invKeys[0];
            const qty = Math.min(state.inventory[k], 3);
            state.inventory[k] -= qty;
            if (state.inventory[k] <= 0) delete state.inventory[k];
            const earned = qty * getSellValue(k);
            state.coins += earned;
            state.lifetimeEarned += earned;
            stateDirty = true;
            sellCooldown = 0.14;
            refreshCharacterVisuals();
            playSfx("coin");
            haptic("coin");
            spawnFloatingText(new THREE.Vector3(SELL_ZONE.x, 1.2, SELL_ZONE.z), `+${formatMoney(earned)}`, "#fde047");
            spawnFlyingDrop(new THREE.Vector3(px, 1.4, pz), new THREE.Vector3(SELL_ZONE.x, 1.1, SELL_ZONE.z), RESOURCES[k].color);
          }
        }
      }

      // 6. Auto-Unlock Biome Gates on Walk-Up Pad
      for (const id in biomeGates) {
        const g = biomeGates[id];
        if (isBiomeUnlocked(id)) continue;
        const dGate = isMenuMode ? 999 : Math.hypot(px - g.biome.gate.x, pz - g.biome.gate.z);
        if (dGate < 2.6) {
          if (!meetsBiomeGear(g.biome)) {
            if (fullBagWarnCooldown <= 0) {
              spawnFloatingText(g.group.position, "Upgrade Gear First!", "#fbbf24");
              fullBagWarnCooldown = 1.6;
            }
            g.charge = 0;
          } else if (state.coins >= g.biome.unlockCost) {
            g.charge += dt;
            const pulse = 1 + Math.sin(now * 0.02) * 0.12;
            g.padRing.scale.set(pulse, pulse, 1);
            if (g.charge >= 0.75) {
              state.coins -= g.biome.unlockCost;
              state.unlockedBiomes.push(id);
              stateDirty = true;
              refreshWorldState();
              playSfx("unlock");
              haptic("unlock");
              spawnBurst(g.biome.gate.x, 1.5, g.biome.gate.z, g.biome.accent, 14, 0.2);
              showBanner(`${g.biome.name} Unlocked!`);
              syncToAndroid(true);
            }
          }
        } else {
          g.charge = 0;
          g.padRing.scale.set(1, 1, 1);
        }
      }

      // 7. World Buildings & Worker Drones
      const autoSpeedBoost = 1 + (getResUpgradeLevel("autoProduction") - 1) * 0.22;
      for (const id in worldBuildings) {
        const wb = worldBuildings[id];
        const lvl = state.buildingLevels[id] || 0;
        const lines = getBuildingLines(id);
        if (lvl > 0) {
          wb.spinner.rotation.z += dt * (1.8 + lvl * 0.35 + lines * 0.5) * autoSpeedBoost;
          wb.workerGroup.rotation.y += dt * 1.4 * autoSpeedBoost;
        }
        if (isMenuMode || !isBiomeUnlocked(wb.def.biome) || lvl >= 8) continue;
        const cost = Math.round(wb.def.baseCost * Math.pow(wb.def.growth, lvl));
        const dBld = Math.hypot(px - wb.def.x, pz - wb.def.z);
        if (dBld < 2.25 && state.coins >= cost) {
          wb.charge += dt;
          if (wb.charge >= 0.85) {
            wb.charge = 0;
            state.coins -= cost;
            state.buildingLevels[id] = lvl + 1;
            stateDirty = true;
            refreshWorldState();
            playSfx("upgrade");
            haptic("upgrade");
            spawnBurst(wb.def.x, 1.5, wb.def.z, 0xfbbf24, 12, 0.2);
            showBanner(`${wb.def.title} Tier ${lvl + 1}!`);
            syncToAndroid(true);
          }
        } else {
          wb.charge = 0;
        }
      }

      // 8. Animate 10 Industrial Machines, Conveyors, Smoke & 5 Fleet Vehicles
      smokeTimer += dt;
      const beltBoost = 1 + getMachineLevel("conveyor_belt") * 0.20;
      for (const id in worldMachines) {
        const wm = worldMachines[id];
        if (!isMachineRunning(id)) continue;
        const mBoost = (1 + (getMachineLevel(id) - 1) * 0.3) * beltBoost;
        if (wm.anim.scrollBelt) {
          texConveyorBelt.offset.y = (texConveyorBelt.offset.y - dt * 0.9 * mBoost) % 1;
        }
        for (const rp of wm.anim.rotParts) {
          rp.mesh.rotation[rp.axis] += dt * rp.speed * mBoost;
        }
        for (const p of wm.anim.pistons) {
          p.mesh.position.y = p.baseY + Math.sin(now * 0.001 * p.speed * mBoost) * p.amp;
        }
        for (const c of wm.anim.crates) {
          c.mesh.position[c.axis] += dt * c.speed * mBoost;
          if (c.mesh.position[c.axis] > c.max) c.mesh.position[c.axis] = c.min;
        }
        if (wm.anim.smokeOrigin && smokeTimer > 0.45) {
          const so = wm.anim.smokeOrigin;
          const cR = Math.cos(wm.def.rotY || 0);
          const sR = Math.sin(wm.def.rotY || 0);
          spawnMachineSmoke(
            wm.def.x + so.lx * cR + so.lz * sR,
            so.ly,
            wm.def.z - so.lx * sR + so.lz * cR,
            so.col
          );
        }
      }
      if (smokeTimer > 0.45) smokeTimer = 0;

      for (let si = machineSmoke.length - 1; si >= 0; si--) {
        const sm = machineSmoke[si];
        sm.age += dt;
        if (sm.age >= sm.life) {
          scene.remove(sm.mesh);
          machineSmoke.splice(si, 1);
          continue;
        }
        sm.mesh.position.y += sm.vy * dt;
        const sc = 1 + sm.age * 1.4;
        sm.mesh.scale.setScalar(sc);
        sm.mesh.material.opacity = Math.max(0, 0.55 * (1 - sm.age / sm.life));
      }

      if (worldFleet.forklift.group.visible) {
        const tFork = now * 0.0011;
        worldFleet.forklift.group.position.z = 1.0 + Math.sin(tFork) * 3.1;
        worldFleet.forklift.group.rotation.y = Math.cos(tFork) >= 0 ? 0 : Math.PI;
        worldFleet.forklift.forkGrp.position.y = 0.45 + Math.sin(now * 0.003) * 0.18;
      }
      if (worldFleet.mining_cart.group.visible) {
        worldFleet.mining_cart.cart.position.x = Math.sin(now * 0.0014) * 2.1;
      }
      if (worldFleet.delivery_truck.group.visible) {
        const pulse = 0.7 + Math.sin(now * 0.008) * 0.6;
        worldFleet.delivery_truck.beacon.material.emissiveIntensity = pulse;
      }
      if (worldFleet.excavator.group.visible) {
        worldFleet.excavator.turret.rotation.y = Math.sin(now * 0.0012) * 0.55;
        worldFleet.excavator.boomPivot.rotation.x = Math.sin(now * 0.0024) * 0.22;
      }

      // 9. Passive Income Tick
      const pps = getPassivePerSec();
      if (pps > 0) {
        passiveAccum += pps * dt;
        if (passiveAccum >= 1) {
          const whole = Math.floor(passiveAccum);
          passiveAccum -= whole;
          state.coins += whole;
          state.lifetimeEarned += whole;
          stateDirty = true;
        }
      }

      // 10. Animate Pooled Debris Particles, Flying Drops & Floating Badges
      for (let i = 0; i < particlePool.length; i++) {
        const p = particlePool[i];
        if (!p.active) continue;
        p.age += dt;
        if (p.age >= p.life) {
          p.active = false;
          p.mesh.visible = false;
          continue;
        }
        p.vy -= 15 * dt;
        p.mesh.position.x += p.vx * dt;
        p.mesh.position.y = Math.max(0.1, p.mesh.position.y + p.vy * dt);
        p.mesh.position.z += p.vz * dt;
        p.mesh.rotation.x += p.rotSpd * dt;
        p.mesh.rotation.y += p.rotSpd * dt;
        p.mesh.scale.setScalar(p.baseScale * (1 - p.age / p.life));
      }

      for (let i = flyingDrops.length - 1; i >= 0; i--) {
        const f = flyingDrops[i];
        f.t += dt;
        const u = Math.min(1, f.t / f.dur);
        f.mesh.position.x = f.start.x + (f.target.x - f.start.x) * u;
        f.mesh.position.z = f.start.z + (f.target.z - f.start.z) * u;
        f.mesh.position.y = f.start.y + ((f.target.y || 1.1) - f.start.y) * u + Math.sin(u * Math.PI) * 1.4;
        f.mesh.rotation.y += dt * 10;
        if (u >= 1) { scene.remove(f.mesh); flyingDrops.splice(i, 1); }
      }

      const { w: vpW, h: vpH } = getViewportSize();
      const hw = vpW / 2, hh = vpH / 2;
      const proj = new THREE.Vector3();
      for (let i = floatingBadges.length - 1; i >= 0; i--) {
        const b = floatingBadges[i];
        b.age += dt;
        if (b.age >= b.life) { b.el.remove(); floatingBadges.splice(i, 1); continue; }
        proj.set(b.wx, b.wy + b.age * 1.6, b.wz);
        proj.project(camera);
        b.el.style.left = ((proj.x * hw) + hw) + "px";
        b.el.style.top = (-(proj.y * hh) + hh) + "px";
        b.el.style.opacity = String(1 - Math.pow(b.age / b.life, 2));
      }

      syncToAndroid(false);
      renderer.render(scene, camera);
    } catch (err) {
      console.error("Tick error:", err);
    }
  }

  refreshCharacterVisuals();
  refreshWorldState();
  syncViewportSize();
  startMenuShowcaseCamera(true);
  emitAmbientTelemetry(performance.now(), "forest", 0, 2, true);
  renderer.render(scene, camera);
  document.getElementById("loading-overlay").classList.add("hidden");
  if (window.AndroidBridge && window.AndroidBridge.onEngineReady) {
    try { window.AndroidBridge.onEngineReady(); } catch (_) {}
  }
  requestAnimationFrame(tick);
})();
